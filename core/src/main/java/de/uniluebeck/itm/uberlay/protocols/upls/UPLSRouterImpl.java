package de.uniluebeck.itm.uberlay.protocols.upls;

import com.google.inject.Singleton;
import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;

@Singleton
public class UPLSRouterImpl extends SimpleChannelUpstreamHandler implements UPLSRouter {

	@Override
	public void route(final UPLS.UPLSPacket packet, final ChannelFuture callerFuture) {
		// TODO implement
	}

	@Override
	public void handleUpstream(final ChannelHandlerContext ctx, final ChannelEvent e) throws Exception {
		// TODO implement
		super.handleUpstream(ctx, e);
	}
}
