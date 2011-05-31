package de.uniluebeck.itm.uberlay;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.jboss.netty.channel.AbstractChannel;
import org.jboss.netty.channel.ChannelConfig;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelSink;

import java.net.SocketAddress;

public class ApplicationChannelImpl extends AbstractChannel implements ApplicationChannel {

	@Inject
	ApplicationChannelImpl(@Named(Injection.APPLICATION_PIPELINE) final ChannelPipeline pipeline,
						   @Named(Injection.APPLICATION_CHANNEL_SINK) final ChannelSink sink) {

		super(null, null, pipeline, sink);
	}

	@Override
	public ChannelConfig getConfig() {
		return ((ApplicationChannelSink) getPipeline().getSink()).getConfig();
	}

	@Override
	public boolean isBound() {
		return ((ApplicationChannelSink) getPipeline().getSink()).isBound();
	}

	@Override
	public boolean isConnected() {
		return ((ApplicationChannelSink) getPipeline().getSink()).isConnected();
	}

	@Override
	public SocketAddress getLocalAddress() {
		return ((ApplicationChannelSink) getPipeline().getSink()).getLocalAddress();
	}

	@Override
	public SocketAddress getRemoteAddress() {
		return null;
	}

}