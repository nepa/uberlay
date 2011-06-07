package de.uniluebeck.itm.uberlay.protocols.upls;

import com.google.inject.Singleton;
import de.uniluebeck.itm.uberlay.protocols.up.UP;
import org.jboss.netty.channel.*;

@Singleton
public class UPLSRouterImpl implements ChannelUpstreamHandler, ChannelDownstreamHandler, UPLSRouter {

	@Override
	public void handleUpstream(final ChannelHandlerContext ctx, final ChannelEvent e) throws Exception {
		if (e instanceof UpstreamMessageEvent && ((UpstreamMessageEvent) e).getMessage() instanceof UPLS.UPLSPacket) {
			route((UPLS.UPLSPacket) ((UpstreamMessageEvent) e).getMessage(), e.getFuture());
		} else {
			ctx.sendUpstream(e);
		}
	}

	@Override
	public void handleDownstream(final ChannelHandlerContext ctx, final ChannelEvent e) throws Exception {
		if (e instanceof DownstreamMessageEvent && ((DownstreamMessageEvent) e).getMessage() instanceof UP.UPPacket) {
			final UP.UPPacket upPacket = (UP.UPPacket) ((DownstreamMessageEvent) e).getMessage();
			final UPLS.UPLSPacket uplsPacket = encapsulatePacket(upPacket);
			route(uplsPacket, e.getFuture());
		} else {
			ctx.sendDownstream(e);
		}
	}

	@Override
	public void route(final UPLS.UPLSPacket packet, final ChannelFuture callerFuture) {
		// TODO implement
	}

	private UPLS.UPLSPacket encapsulatePacket(final UP.UPPacket message) {
		// TODO implement
		return null;
	}
}
