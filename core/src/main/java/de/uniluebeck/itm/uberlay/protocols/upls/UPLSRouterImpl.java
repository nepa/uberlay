package de.uniluebeck.itm.uberlay.protocols.upls;

import org.jboss.netty.channel.ChannelDownstreamHandler;
import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelUpstreamHandler;
import org.jboss.netty.channel.DownstreamMessageEvent;
import org.jboss.netty.channel.UpstreamMessageEvent;

import com.google.inject.Singleton;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

import de.uniluebeck.itm.uberlay.protocols.up.UP;
import de.uniluebeck.itm.uberlay.protocols.up.UP.UPPacket;

@Singleton
public class UPLSRouterImpl implements ChannelUpstreamHandler, ChannelDownstreamHandler, UPLSRouter {
	private UPLSRoutingTable uplsRoutingTable = new UPLSRoutingTable();

	@Override
	public void handleUpstream(final ChannelHandlerContext ctx, final ChannelEvent e) throws Exception {
		if (e instanceof UpstreamMessageEvent && ((UpstreamMessageEvent) e).getMessage() instanceof UPLS.UPLSPacket) {
			route(ctx, (UPLS.UPLSPacket) ((UpstreamMessageEvent) e).getMessage(), e.getFuture());
		} else {
			ctx.sendUpstream(e);
		}
	}

	@Override
	public void handleDownstream(final ChannelHandlerContext ctx, final ChannelEvent e) throws Exception {
		if (e instanceof DownstreamMessageEvent && ((DownstreamMessageEvent) e).getMessage() instanceof UP.UPPacket) {
			final UP.UPPacket upPacket = (UP.UPPacket) ((DownstreamMessageEvent) e).getMessage();
			final UPLS.UPLSPacket uplsPacket = encapsulatePacket(upPacket);
			route(ctx, uplsPacket, e.getFuture());
		} else {
			ctx.sendDownstream(e);
		}
	}

	@Override
	public void route(ChannelHandlerContext context, final UPLS.UPLSPacket packet, final ChannelFuture callerFuture) {
		final int destinationLabel = packet.getLabel();
		final int localLabel = this.uplsRoutingTable.getLocalLabel();
		
		if (destinationLabel == localLabel)
		{
			try {
				UP.UPPacket upPacket;
				upPacket = UPPacket.parseFrom(packet.getPayload());			
				context.sendUpstream(new UpstreamMessageEvent(context.getChannel(), upPacket, null));				
			}
			catch (InvalidProtocolBufferException e)
			{
				callerFuture.setFailure(e);
			}

		}		
	}

	private UPLS.UPLSPacket encapsulatePacket(final UP.UPPacket message) {
		int uplsLabel = this.uplsRoutingTable.lookupLabel(message.getDestination());
		
		return UPLS.UPLSPacket.newBuilder()
			.setPayload(ByteString.copyFrom(message.toByteArray()))
			.setLabel(uplsLabel)
			.build();
	}
}
