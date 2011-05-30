package de.uniluebeck.itm.uberlay.core.protocols.router;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import de.uniluebeck.itm.uberlay.core.protocols.pvp.RoutingTable;
import de.uniluebeck.itm.uberlay.core.protocols.pvp.RoutingTableEntry;
import de.uniluebeck.itm.uberlay.core.protocols.up.UP;
import de.uniluebeck.itm.uberlay.core.protocols.up.UPAddress;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.*;

public class RouterImpl extends AbstractChannelSink implements Router {

	@Inject
	private RoutingTable routingTable;

	@Inject
	private Channel channel;

	private ImmutableList<UPAddress> localAddresses = ImmutableList.of();

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
		final UPAddress destination = new UPAddress(upPacket.getDestination());

		if (localAddresses.contains(destination)) {

			final UPAddress source = new UPAddress(upPacket.getSource());
			final byte[] payloadBytes = upPacket.getPayload().toByteArray();
			final ChannelBuffer payload = ChannelBuffers.wrappedBuffer(payloadBytes);
			final UpstreamMessageEvent event = new UpstreamMessageEvent(channel, payload, source);

			channel.getPipeline().sendUpstream(event);
		}
	}

	private void handleDownstreamMessageEvent(final DownstreamMessageEvent e) {

		final UPAddress remoteAddress = (UPAddress) e.getRemoteAddress();
		final Channel channel = routingTable.getNextHopChannel(remoteAddress);

		channel.write(e.getMessage()).addListener(new ChannelFutureListener() {
			@Override
			public void operationComplete(final ChannelFuture future) throws Exception {
				e.getFuture().setSuccess();
			}
		});
	}

	@Override
	public void addLocalAddress(final UPAddress address) {
		// TODO implement
	}

	@Override
	public void removeLocalAddress(final UPAddress address) {
		// TODO implement
	}
}
