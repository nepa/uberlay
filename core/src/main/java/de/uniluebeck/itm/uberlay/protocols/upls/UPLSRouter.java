package de.uniluebeck.itm.uberlay.protocols.upls;

import de.uniluebeck.itm.uberlay.protocols.upls.UPLS;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelUpstreamHandler;

public interface UPLSRouter extends ChannelUpstreamHandler {

	void route(UPLS.UPLSPacket packet, final ChannelFuture callerFuture);

}
