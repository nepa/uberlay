package de.uniluebeck.itm.uberlay.protocols.upls;

import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelUpstreamHandler;

public interface UPLSRouter extends ChannelUpstreamHandler {

	void route(ChannelHandlerContext context, UPLS.UPLSPacket packet, final ChannelFuture callerFuture);

}
