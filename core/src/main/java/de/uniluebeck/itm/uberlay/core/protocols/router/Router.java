package de.uniluebeck.itm.uberlay.core.protocols.router;

import de.uniluebeck.itm.uberlay.core.protocols.up.UPAddress;
import org.jboss.netty.channel.ChannelSink;
import org.jboss.netty.channel.ChannelUpstreamHandler;

public interface Router extends ChannelUpstreamHandler, ChannelSink {

	void addLocalAddress(UPAddress address);

	void removeLocalAddress(UPAddress address);

}
