package de.uniluebeck.itm.uberlay;


import org.jboss.netty.channel.Channel;

import java.net.InetSocketAddress;
import java.util.concurrent.Future;

public interface UberlayNexus extends UberlayRouter, ApplicationChannelSink {

	Future<Channel> getApplicationChannel();

	Future<Channel> connect(InetSocketAddress remoteSocketAddress);

	Future<Channel> bind(InetSocketAddress localSocketAddress);

	void shutdown();

}
