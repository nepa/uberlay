package de.uniluebeck.itm.uberlay;

import com.google.common.util.concurrent.SettableFuture;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.protobuf.ByteString;
import de.uniluebeck.itm.uberlay.protocols.up.UP;
import de.uniluebeck.itm.uberlay.protocols.up.UPAddress;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.*;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;
import java.net.InetSocketAddress;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;

@Singleton
public class UberlayNexus extends AbstractChannelSink implements UberlayRouter, ApplicationChannelSink {

	private static final Logger log = LoggerFactory.getLogger(UberlayNexus.class);

	private final ChannelGroup uberlayClientChannels = new DefaultChannelGroup("UberlayNexus-UberlayClientChannels");

	private final ChannelGroup uberlayServerChannels = new DefaultChannelGroup("UberlayNexus-UberlayServerChannels");

	@Inject
	private ScheduledExecutorService scheduledExecutorService;

	@Inject
	private RoutingTable routingTable;

	@Inject
	@Named(Injection.LOCAL_ADDRESS)
	private UPAddress localAddress;

	@Inject
	@Named(Injection.UBERLAY_PIPELINE_FACTORY)
	private ChannelPipelineFactory uberlayPipelineFactory;

	@Inject
	@Named(Injection.APPLICATION_CHANNEL)
	private Channel applicationChannel;

	UberlayNexus() {
	}

	@Override
	public void eventSunk(final ChannelPipeline pipeline, final ChannelEvent e) throws Exception {

		if (e instanceof DownstreamMessageEvent) {
			handleDownstreamMessageEvent((DownstreamMessageEvent) e);
		} else if (e instanceof DownstreamChannelStateEvent) {
			handleDownstreamChannelStateEvent((ChannelStateEvent) e);
		}
	}

	private void handleDownstreamChannelStateEvent(final ChannelStateEvent e) {
		if (ChannelState.CONNECTED == e.getState() && e.getValue() == null) {
			shutdown();
			applicationChannel.getCloseFuture().setSuccess();
		}
	}

	@SuppressWarnings({"ThrowableResultOfMethodCallIgnored"})
	private void handleExceptionEvent(final ExceptionEvent e) {
		log.warn("Caught exception event. Sending it upstream! {}", e);
		applicationChannel.getPipeline().sendUpstream(new DefaultExceptionEvent(e.getChannel(), e.getCause()));
	}

	Future<Channel> connect(final InetSocketAddress remoteAddress) {

		final SettableFuture<Channel> returnFuture = SettableFuture.create();

		ClientBootstrap clientBootstrap = new ClientBootstrap(
				new NioClientSocketChannelFactory(scheduledExecutorService, scheduledExecutorService)
		);

		clientBootstrap.setPipelineFactory(uberlayPipelineFactory);
		clientBootstrap.connect(remoteAddress).addListener(new ChannelFutureListener() {
			@Override
			public void operationComplete(final ChannelFuture future) throws Exception {
				uberlayClientChannels.add(future.getChannel());
				returnFuture.set(future.getChannel());
			}
		}
		);

		return returnFuture;
	}

	Future<Channel> bind(final InetSocketAddress localAddress) {

		final SettableFuture<Channel> returnFuture = SettableFuture.create();

		final ServerBootstrap serverBootstrap = new ServerBootstrap(
				new NioServerSocketChannelFactory(scheduledExecutorService, scheduledExecutorService)
		);

		serverBootstrap.setPipelineFactory(uberlayPipelineFactory);
		final Channel channel = serverBootstrap.bind(localAddress);
		uberlayServerChannels.add(channel);
		returnFuture.set(channel);

		return returnFuture;
	}

	@Override
	public void exceptionCaught(final ChannelPipeline pipeline, final ChannelEvent e,
								final ChannelPipelineException cause) throws Exception {
		handleExceptionEvent((ExceptionEvent) e);
	}

	@Override
	public void handleUpstream(final ChannelHandlerContext ctx, final ChannelEvent e) throws Exception {

		if (e instanceof UpstreamMessageEvent) {
			handleUpstreamMessageEvent((UpstreamMessageEvent) e);
		}
	}

	private void handleUpstreamMessageEvent(final UpstreamMessageEvent e) {

		final UP.UPPacket upPacket = (UP.UPPacket) e.getMessage();
		final ChannelFuture callerFuture = e.getFuture();

		route(upPacket, callerFuture);
	}

	private void handleDownstreamMessageEvent(final DownstreamMessageEvent e) {

		final ChannelBuffer payload = (ChannelBuffer) e.getMessage();
		final UPAddress destination = (UPAddress) e.getRemoteAddress();
		final ChannelFuture callerFuture = e.getFuture();

		route(buildPacket(payload, destination), callerFuture);
	}

	@Override
	public void route(final UP.UPPacket packet, final ChannelFuture callerFuture) {

		final UPAddress destination = new UPAddress(packet.getDestination());
		final boolean isLoopBack = localAddress.equals(destination);

		if (isLoopBack) {
			sendUpstream(packet, callerFuture);
		} else {
			sendDownstream(packet, callerFuture, destination);
		}
	}

	private void sendDownstream(final UP.UPPacket packet, final ChannelFuture callerFuture,
								final UPAddress destination) {

		final Channel channel = routingTable.getNextHopChannel(destination);
		final boolean noRouteToHost = channel == null;

		if (noRouteToHost) {

			callerFuture.setFailure(new NoRouteToPeerException(destination));

		} else {

			final ChannelFutureListener listener = new ChannelFutureListener() {
				@Override
				public void operationComplete(final ChannelFuture future) throws Exception {
					callerFuture.setSuccess();
				}
			};
			channel.write(packet).addListener(listener);
		}

	}

	private UP.UPPacket buildPacket(final ChannelBuffer payload, final UPAddress destination) {
		return UP.UPPacket.newBuilder()
				.setDestination(destination.toString())
				.setSource(localAddress.toString())
				.setPayload(ByteString.copyFrom(payload.toByteBuffer()))
				.build();
	}

	private void sendUpstream(final UP.UPPacket upPacket, final ChannelFuture callerFuture) {

		final UPAddress source = new UPAddress(upPacket.getSource());
		final byte[] payloadBytes = upPacket.getPayload().toByteArray();
		final ChannelBuffer payload = ChannelBuffers.wrappedBuffer(payloadBytes);
		final UpstreamMessageEvent event = new UpstreamMessageEvent(applicationChannel, payload, source);

		applicationChannel.getPipeline().sendUpstream(event);
		callerFuture.setSuccess();
	}

	@Override
	public ChannelConfig getConfig() {
		return null;
	}

	@Override
	public boolean isBound() {
		return uberlayServerChannels.size() > 0;
	}

	@Override
	public boolean isConnected() {
		return uberlayClientChannels.size() > 0;
	}

	@Override
	public UPAddress getLocalAddress() {
		return localAddress;
	}

	void shutdown() {
		uberlayServerChannels.close().awaitUninterruptibly();
		uberlayClientChannels.close().awaitUninterruptibly();
	}

	Future<Channel> getApplicationChannel() {
		try {
			final SettableFuture<Channel> future = SettableFuture.create();
			future.set(applicationChannel);
			return future;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
