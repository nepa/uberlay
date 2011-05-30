package de.uniluebeck.itm.uberlay.core.router;

import com.google.inject.Inject;
import com.google.protobuf.ByteString;
import de.uniluebeck.itm.uberlay.core.protocols.up.UP;
import de.uniluebeck.itm.uberlay.core.protocols.up.UPAddress;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.*;

import javax.inject.Named;

public class RouterImpl extends AbstractChannelSink implements Router {

	@Inject
	private RoutingTable routingTable;

	@Inject
	private Channel channel;

	@Inject
	@Named(INJECTION_NAME_LOCAL_ADDRESS)
	private UPAddress localAddress;

	@Override
	public void eventSunk(final ChannelPipeline pipeline, final ChannelEvent e) throws Exception {

		if (e instanceof DownstreamMessageEvent) {
			handleDownstreamMessageEvent((DownstreamMessageEvent) e);
		}
		// TODO implement
	}

	@Override
	public void exceptionCaught(final ChannelPipeline pipeline, final ChannelEvent e,
								final ChannelPipelineException cause) throws Exception {

		// TODO implement
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

	private void route(final UP.UPPacket packet, final ChannelFuture callerFuture) {

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
		final UpstreamMessageEvent event = new UpstreamMessageEvent(channel, payload, source);

		channel.getPipeline().sendUpstream(event);
		callerFuture.setSuccess();
	}
}
