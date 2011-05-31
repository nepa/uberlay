package de.uniluebeck.itm.uberlay;

import com.google.common.net.HostSpecifier;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.inject.Guice;
import de.uniluebeck.itm.uberlay.protocols.up.UPAddress;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static com.google.common.base.Preconditions.checkArgument;
import static org.jboss.netty.channel.Channels.pipeline;

public class UberlayMain {

	private static final Logger log = LoggerFactory.getLogger(UberlayMain.class);

	private static final int EXIT_CODE_INVALID_ARGUMENTS = 1;

	private static final int CORE_POOL_SIZE = 1;

	public static void main(String[] args) throws UnknownHostException, ExecutionException, InterruptedException {

		if (args.length < 3) {
			System.err.println("Usage: " + UberlayMain.class.getCanonicalName() +
					" REMOTE_HOST REMOTE_PORT LOCAL_UP_ADDRESS [LOCAL_HOST LOCAL_PORT]"
			);
			System.exit(EXIT_CODE_INVALID_ARGUMENTS);
		}

		final InetSocketAddress remoteSocketAddress = buildSocketAddress(args[0], args[1]);
		final UPAddress localAddress = new UPAddress(args[2]);
		final InetSocketAddress localSocketAddress = args.length > 4 ? buildSocketAddress(args[3], args[4]) : null;

		final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(
				CORE_POOL_SIZE,
				new ThreadFactoryBuilder().setNameFormat("Uberlay %d").build()
		);

		final ChannelPipelineFactory applicationPipelineFactory = new ChannelPipelineFactory() {
			public ChannelPipeline getPipeline() throws Exception {
				return pipeline(new DefaultLoggingHandler());
			}
		};
		final UberlayBootstrap bootstrap = Guice.createInjector(new UberlayModule(executorService, localAddress))
				.getInstance(UberlayBootstrap.class);

		if (localSocketAddress != null) {
			log.info("Binding local server socket on {}:{}...", localSocketAddress.getHostName(),
					localSocketAddress.getPort()
			);
			bootstrap.bind(localSocketAddress).get();
			log.info("Bound to {}:{}!", localSocketAddress.getHostName(), localSocketAddress.getPort());
		}

		bootstrap.connect(remoteSocketAddress).get();

		final Channel applicationChannel = bootstrap.getApplicationChannel().get();

		final ChannelFuture writeFuture = applicationChannel.write(
				ChannelBuffers.wrappedBuffer(new byte[]{1, 2, 3}),
				new UPAddress("de:uniluebeck")
		);

		log.info("Writing packet...");
		writeFuture.awaitUninterruptibly();
		log.info("Packet written!");

		log.info("Shutting down...");
		bootstrap.shutdown();
		log.info("Shutdown complete!");

	}

	private static InetSocketAddress buildSocketAddress(final String host, final String portString) {
		final int port = Integer.parseInt(portString);
		checkHostAndPort(host, port);
		return new InetSocketAddress(host, port);
	}

	private static void checkHostAndPort(final String host, final int port) {
		checkArgument(HostSpecifier.isValid(host), "\"" + host + "\" is not a valid hostname!");
		checkArgument(port >= 0 && port < 65535, port + " is not a valid port number!");
	}

}
