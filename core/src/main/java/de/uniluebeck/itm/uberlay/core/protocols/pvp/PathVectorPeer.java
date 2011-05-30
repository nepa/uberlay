package de.uniluebeck.itm.uberlay.core.protocols.pvp;

import de.uniluebeck.itm.uberlay.core.router.RoutingTable;
import de.uniluebeck.itm.uberlay.core.protocols.up.UPAddress;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class PathVectorPeer {

	public static void main(String[] args) {

		if (args.length < 3 || args.length == 4) {
			System.out.println("Usage: java " + PathVectorPeer.class
					.getCanonicalName() + " NODE_NAME LOCAL_HOST_NAME LOCAL_PORT [REMOTE_HOST_NAME REMOTE_PORT]"
			);
			System.exit(1);
		}

		final String nodeName = args[0];
		final String localHostName = args[1];
		final int localPort = Integer.parseInt(args[2]);

		final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(10);
		final RoutingTable routingTable = new PathVectorRoutingTable(new UPAddress(nodeName), 1, TimeUnit.MINUTES);
		final PathVectorChannelPipelineFactory pipelineFactory = new PathVectorChannelPipelineFactory(
				executorService,
				nodeName,
				routingTable
		);

		startServer(localHostName, localPort, executorService, pipelineFactory);

		final boolean startClient = args.length > 4;
		if (startClient) {

			final String remoteHostName = args[3];
			final int remotePort = Integer.parseInt(args[4]);

			startClient(remoteHostName, remotePort, executorService, pipelineFactory);
		}
	}

	private static Channel startClient(final String remoteHostName, final int remotePort,
									   final ScheduledExecutorService executorService,
									   final PathVectorChannelPipelineFactory pipelineFactory) {
		final ChannelFactory clientChannelFactory = new NioClientSocketChannelFactory(
				executorService,
				executorService
		);

		ClientBootstrap clientBootstrap = new ClientBootstrap(clientChannelFactory);
		clientBootstrap.setPipelineFactory(pipelineFactory);
		clientBootstrap.setOption("tcpNoDelay", true);
		clientBootstrap.setOption("keepAlive", true);

		return clientBootstrap
				.connect(new InetSocketAddress(remoteHostName, remotePort))
				.awaitUninterruptibly()
				.getChannel();
	}

	private static void startServer(final String localHostName, final int localPort,
									final ScheduledExecutorService executorService,
									final PathVectorChannelPipelineFactory pipelineFactory) {

		final ChannelFactory serverChannelFactory = new NioServerSocketChannelFactory(
				executorService,
				executorService
		);

		final ServerBootstrap serverBootstrap = new ServerBootstrap(serverChannelFactory);
		serverBootstrap.setPipelineFactory(pipelineFactory);
		serverBootstrap.setOption("child.tcpNoDelay", true);
		serverBootstrap.setOption("child.keepAlive", true);
		serverBootstrap.bind(new InetSocketAddress(localHostName, localPort));

	}

}
