package de.uniluebeck.itm.uberlay;

import de.uniluebeck.itm.uberlay.protocols.up.UPAddress;
import org.jboss.netty.channel.ChannelConfig;
import org.jboss.netty.channel.ChannelSink;

public interface ApplicationChannelSink extends ChannelSink {

	ChannelConfig getConfig();

	boolean isBound();

	boolean isConnected();

	UPAddress getLocalAddress();
}
