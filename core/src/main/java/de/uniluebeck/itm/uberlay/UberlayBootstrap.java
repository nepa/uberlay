package de.uniluebeck.itm.uberlay;

import de.uniluebeck.itm.uberlay.protocols.up.UPAddress;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;

import javax.inject.Inject;
import java.net.InetSocketAddress;
import java.util.concurrent.Future;

public class UberlayBootstrap {

	@Inject
	private UberlayNexus uberlayNexus;

	private UberlayBootstrap() {
	}

	/**
	 * Returns or opens an Uberlay channel that is bound to the {@link UPAddress} {@code localAddress}. That means that all
	 * messaging running through this channels instance will be addressed to {@code localAddress} or are being sent from
	 * {@code localAddress} respectively.
	 *
	 * @return an {@link ApplicationChannelImpl} instance bound to {@code localAddress}
	 */
	public Future<Channel> getApplicationChannel() {
		return uberlayNexus.getApplicationChannel();
	}

	/**
	 * Tries to connect with a remote Uberlay peer on the given {@code remoteSocketAddress}.
	 *
	 * @return a {@link ChannelFuture} instance holding the {@link Channel} to the remote Uberlay peer
	 */
	public Future<Channel> connect(final InetSocketAddress remoteSocketAddress) {
		return uberlayNexus.connect(remoteSocketAddress);
	}

	/**
	 * Binds to a local server socket to allow other Uberlay peers to connect themselves to it.
	 *
	 * @param localSocketAddress the address of the local server socket
	 *
	 * @return a {@link ChannelFuture} instance
	 */
	public Future<Channel> bind(final InetSocketAddress localSocketAddress) {
		return uberlayNexus.bind(localSocketAddress);
	}

	public void shutdown() {
		uberlayNexus.shutdown();
	}

}
