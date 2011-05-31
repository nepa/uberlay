package de.uniluebeck.itm.uberlay;

import de.uniluebeck.itm.uberlay.protocols.up.UP;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelUpstreamHandler;

public interface UberlayRouter extends ChannelUpstreamHandler {

	public void route(final UP.UPPacket packet, final ChannelFuture callerFuture);

}
