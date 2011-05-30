package de.uniluebeck.itm.uberlay.protocols.rtt;


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
				RoundtripTimeMessages.RoundtripTimeRequest rttRequest =
						RoundtripTimeMessages.RoundtripTimeRequest.newBuilder()
								.setUnixEpochInMillis(now)
								.build();
				channel.write(rttRequest);
				log.trace("Sent RoundtripTimeRequest at {}", now);
			}
		}
	};

	private ScheduledFuture<?> sendRttRequestRunnableSchedule;

	private Channel channel;

	public RoundtripTimeProtocolHandler(final ScheduledExecutorService executorService, final int rttRequestInterval,
										final TimeUnit rttRequestIntervalTimeunit) {

		this.executorService = executorService;
		this.rttRequestDelay = rttRequestInterval;
		this.rttRequestTimeunit = rttRequestIntervalTimeunit;
	}

	@Override
	public void channelDisconnected(final ChannelHandlerContext ctx, final ChannelStateEvent e) throws Exception {

		assert channel != null;

		channel = null;
		sendRttRequestRunnableSchedule.cancel(false);

		super.channelDisconnected(ctx, e);
	}

	@Override
	public void channelConnected(final ChannelHandlerContext ctx, final ChannelStateEvent e) throws Exception {

		assert channel == null;

		channel = e.getChannel();
		sendRttRequestRunnableSchedule = executorService.scheduleWithFixedDelay(
				sendRttRequestRunnable,
				0,
				rttRequestDelay,
				rttRequestTimeunit
		);

		super.channelConnected(ctx, e);
	}

	@Override
	public void messageReceived(final ChannelHandlerContext ctx, final MessageEvent e) throws Exception {

		if (e.getMessage() instanceof RoundtripTimeMessages.RoundtripTimeRequest) {
			handleRequest(ctx, e);
		} else if (e.getMessage() instanceof RoundtripTimeMessages.RoundtripTimeResponse) {
			handleResponse(ctx, e);
		} else {
			super.messageReceived(ctx, e);
		}

	}

	private void handleResponse(final ChannelHandlerContext ctx, final MessageEvent e) {
		RoundtripTimeMessages.RoundtripTimeResponse rttResponse =
				(RoundtripTimeMessages.RoundtripTimeResponse) e.getMessage();

		log.debug("Received RoundTripTimeResponse {}", rttResponse);

		long now = System.currentTimeMillis();
		long diff = now - rttResponse.getRequestUnixEpochInMillis();

		log.debug("Link metric is : {}", diff);

		// ignore faulty input message
		if (diff > 0) {
			ctx.sendUpstream(new UpstreamMessageEvent(
					ctx.getChannel(),
					new RoundtripTimeMetric(diff),
					ctx.getChannel().getRemoteAddress()
			)
			);
		}
	}

	private void handleRequest(final ChannelHandlerContext ctx, final MessageEvent e) {

		// "read" request
		RoundtripTimeMessages.RoundtripTimeRequest request =
				(RoundtripTimeMessages.RoundtripTimeRequest) e.getMessage();

		// build response
		RoundtripTimeMessages.RoundtripTimeResponse response = RoundtripTimeMessages.RoundtripTimeResponse
				.newBuilder()
				.setRequestUnixEpochInMillis(request.getUnixEpochInMillis())
				.build();

		// send response
		channel.write(response);
	}
}
