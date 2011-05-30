package de.uniluebeck.itm.uberlay;

import org.jboss.netty.channel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultLoggingHandler extends SimpleChannelHandler {

	private final Logger log = LoggerFactory.getLogger(DefaultLoggingHandler.class);

	@Override
	public void handleUpstream(final ChannelHandlerContext ctx, final ChannelEvent e)
			throws Exception {
		log.debug("{}", e);
		super.handleUpstream(ctx, e);
	}

	@Override
	public void handleDownstream(final ChannelHandlerContext ctx, final ChannelEvent e)
			throws Exception {
		log.debug("{}", e);
		super.handleDownstream(ctx, e);
	}

	@Override
	public void exceptionCaught(final ChannelHandlerContext ctx, final ExceptionEvent e)
			throws Exception {
		log.error("" + e, e);
		super.exceptionCaught(ctx, e);
	}

	@Override
	public void messageReceived(final ChannelHandlerContext ctx, final MessageEvent e) throws Exception {
		log.info("{}", e);
		super.messageReceived(ctx, e);
	}
}
