package de.uniluebeck.itm.uberlay.core.protocols.rtt;


import de.uniluebeck.itm.uberlay.core.LinkMetric;
import org.jboss.netty.channel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class RoundtripTimeProtocolHandler extends SimpleChannelHandler {

	private static final Logger log = LoggerFactory.getLogger(RoundtripTimeProtocolHandler.class);

	private final ScheduledExecutorService executorService;

	private final int rttRequestDelay;

	private final TimeUnit rttRequestTimeunit;

	private final Runnable sendRttRequestRunnable = new Runnable() {
		@Override
		public void run() {
			if (channel != null) {
				long now = System.currentTimeMillis();
				RoundtripTimeProtocol.RoundtripTimeRequest rttRequest =
						RoundtripTimeProtocol.RoundtripTimeRequest.newBuilder()
								.setUnixEpochInMillis(now)
								.build();
				channel.write(rttRequest);
				log.trace("Sent RoundtripTimeRequest at {}", now);
			}
		}
	};

	private ScheduledFuture<?> sendRttRequestRunnableSchedule;

	private Channel channel;

	public RoundtripTimeProtocolHandler(final ScheduledExecutorService executorService, final int rttRequestDelay,
										final TimeUnit rttRequestTimeunit) {

		this.executorService = executorService;
		this.rttRequestDelay = rttRequestDelay;
		this.rttRequestTimeunit = rttRequestTimeunit;
	}

	@Override
	public void channelDisconnected(final ChannelHandlerContext ctx, final ChannelStateEvent e) throws Exception {

		assert channel != null;

		channel = null;
		sendRttRequestRunnableSchedule.cancel(false);
	}

	@Override
	public void channelConnected(final ChannelHandlerContext ctx, final ChannelStateEvent e) throws Exception {

		assert channel == null;

		channel = e.getChannel();
		sendRttRequestRunnableSchedule = executorService.schedule(
				sendRttRequestRunnable,
				rttRequestDelay,
				rttRequestTimeunit
		);
	}

	@Override
	public void messageReceived(final ChannelHandlerContext ctx, final MessageEvent e) throws Exception {

		if (e.getMessage() instanceof RoundtripTimeProtocol.RoundtripTimeResponse) {

			RoundtripTimeProtocol.RoundtripTimeResponse rttResponse =
					(RoundtripTimeProtocol.RoundtripTimeResponse) e.getMessage();

			long now = System.currentTimeMillis();
			long diff = now - rttResponse.getRequestUnixEpochInMillis();

			// ignore faulty input message
			if (diff > 0) {
				ctx.sendUpstream(new UpstreamMessageEvent(
						ctx.getChannel(),
						new LinkMetric(diff),
						ctx.getChannel().getRemoteAddress()
				)
				);
			}

		} else {
			ctx.sendUpstream(e);
		}

	}
}
