package de.uniluebeck.itm.uberlay.core.router;

import org.jboss.netty.channel.ChannelSink;
import org.jboss.netty.channel.ChannelUpstreamHandler;

public interface Router extends ChannelUpstreamHandler, ChannelSink {

	String INJECTION_NAME_LOCAL_ADDRESS = "localAddress";

}
