package de.uniluebeck.itm.uberlay;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.inject.Guice;
import de.uniluebeck.itm.uberlay.protocols.up.UPAddress;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.handler.codec.string.StringDecoder;
import org.jboss.netty.handler.codec.string.StringEncoder;
import org.jboss.netty.util.CharsetUtil;
import org.jboss.netty.util.internal.ExecutorUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static org.jboss.netty.channel.Channels.pipeline;

public class UberlayMain {

	private static final Logger log = LoggerFactory.getLogger(UberlayMain.class);

	private static final int EXIT_CODE_INVALID_ARGUMENTS = 1;

	private static final int CORE_POOL_SIZE = 10;

	public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {

		if (args.length < 3) {
			System.err.println("Usage: " + UberlayMain.class.getCanonicalName() +
					" LOCAL_UP_ADDRESS LOCAL_HOST LOCAL_PORT [REMOTE_HOST REMOTE_PORT]"
			);
			System.exit(EXIT_CODE_INVALID_ARGUMENTS);
		}

		final UPAddress localUPAddress = new UPAddress(args[0]);
		final InetSocketAddress localSocketAddress = buildSocketAddress(args[1], args[2]);
		final InetSocketAddress remoteSocketAddress = args.length > 4 ? buildSocketAddress(args[3], args[4]) : null;

		final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(
				CORE_POOL_SIZE,
				new ThreadFactoryBuilder().setNameFormat("Uberlay %d").build()
		);

		final UberlayModule uberlayModule = new UberlayModule(
				executorService,
				localUPAddress,
				pipeline(
						new StringEncoder(CharsetUtil.UTF_8),
						new StringDecoder(CharsetUtil.UTF_8),
						new DefaultLoggingHandler()
				)
		);

		final UberlayBootstrap bootstrap = Guice.createInjector(uberlayModule).getInstance(UberlayBootstrap.class);

		log.info("Binding local server socket on {}:{}...", localSocketAddress.getHostName(),
				localSocketAddress.getPort()
		);
		final Channel serverChannel = bootstrap.bind(localSocketAddress).get();
		log.info("Bound to {}:{}!", localSocketAddress.getHostName(), localSocketAddress.getPort());

		if (remoteSocketAddress != null) {

			log.info("Connecting to remote peer on {}:{}...", remoteSocketAddress.getHostName(),
					remoteSocketAddress.getPort()
			);
			bootstrap.connect(remoteSocketAddress).get();
			log.info("Connected to remote peer on {}:{}!", remoteSocketAddress.getHostName(),
					remoteSocketAddress.getPort()
			);
		}

		final Channel applicationChannel = bootstrap.getApplicationChannel().get();

		/*
		final ChannelFuture writeFuture = applicationChannel.write(
				ChannelBuffers.wrappedBuffer(new byte[]{1, 2, 3}),
				new UPAddress("de:uniluebeck")
		);

		log.info("Writing packet...");
		writeFuture.awaitUninterruptibly();
		log.info("Packet written!");
		*/

		String input = null;
		while (!"exit".equals(input)) {
			input = new BufferedReader(new InputStreamReader(System.in)).readLine();
			final String[] split = input.split(" ");
			if (!"exit".equals(input) && split.length > 1) {
				log.info("Sending \"" + split[0] + "\" to \"" + split[1] + "\".");
				applicationChannel.write(ChannelBuffers.wrappedBuffer(split[0].getBytes()), new UPAddress(split[1])).awaitUninterruptibly();
				log.info("Sending done.");
			}
		}

		log.info("Shutting down...");
		bootstrap.shutdown();
		log.info("Shutdown complete!");

		ExecutorUtil.terminate(executorService);

	}

	private static InetSocketAddress buildSocketAddress(final String host, final String portString) {
		final int port = Integer.parseInt(portString);
		return new InetSocketAddress(host, port);
	}

}
