package de.uniluebeck.itm.uberlay.core.protocols.router;

import com.google.inject.Inject;
import de.uniluebeck.itm.uberlay.core.protocols.pvp.RoutingTable;
import de.uniluebeck.itm.uberlay.core.protocols.pvp.RoutingTableEntry;
import de.uniluebeck.itm.uberlay.core.protocols.up.UPAddress;
import org.jboss.netty.channel.*;

public class RouterImpl implements Router {

	@Inject
	private RoutingTable routingTable;

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

		// TODO implement
	}

	private void handleDownstreamMessageEvent(final DownstreamMessageEvent e) {

		final UPAddress remoteAddress = (UPAddress) e.getRemoteAddress();
		final Channel channel = routingTable.getNextHopChannel(remoteAddress.getAddress());

		channel.write(e.getMessage()).addListener(new ChannelFutureListener() {
			@Override
			public void operationComplete(final ChannelFuture future) throws Exception {
				e.getFuture().setSuccess();
			}
		});
	}
}
