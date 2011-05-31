package de.uniluebeck.itm.uberlay.protocols.up;

import de.uniluebeck.itm.uberlay.protocols.up.UP;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelUpstreamHandler;

public interface UPRouter extends ChannelUpstreamHandler {

	public void route(final UP.UPPacket packet, final ChannelFuture callerFuture);

}
