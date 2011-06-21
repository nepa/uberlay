package de.uniluebeck.itm.uberlay.protocols.upls;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelDownstreamHandler;
import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
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
	public void handleDownstream(final ChannelHandlerContext ctx, final ChannelEvent e) throws Exception
	{
		// Outgoing packet is an UP packet
		if (e instanceof DownstreamMessageEvent && ((DownstreamMessageEvent)e).getMessage() instanceof UP.UPPacket)
		{
			// So we encapsulate UP packet in UPLS packet...
			final UP.UPPacket upPacket = (UP.UPPacket)((DownstreamMessageEvent)e).getMessage();
			final UPLS.UPLSPacket uplsPacket = encapsulatePacket(upPacket);
			
			// ... and do label-based routing afterwards
			route(ctx, uplsPacket, e.getFuture());
		}
		// UPLS packets can be send downstream immediately
		else
		{
			ctx.sendDownstream(e);
		}
	}	
	
	@Override
	public void handleUpstream(final ChannelHandlerContext ctx, final ChannelEvent e) throws Exception
	{
		// Incoming packet is an UPLS packet
		if (e instanceof UpstreamMessageEvent && ((UpstreamMessageEvent)e).getMessage() instanceof UPLS.UPLSPacket)
		{
			// So we do label-based routing
			route(ctx, (UPLS.UPLSPacket)((UpstreamMessageEvent)e).getMessage(), e.getFuture());
		}
		// Otherwise UP packets are send upstream and handled by UP routing
		else
		{			
			ctx.sendUpstream(e);
		}
	}



	@Override
	public void route(ChannelHandlerContext context, final UPLS.UPLSPacket packet, final ChannelFuture callerFuture)
	{
		// Determine local UPLS label and destination label
		final int localLabel = this.uplsRoutingTable.getLocalLabel();
		final int destinationLabel = packet.getLabel();
		
		//
		// EGRESS ROUTER
		//
		// Router is packet's destination
		//
		
		// TODO Incoming link auch beruecksichtigen 
		if (destinationLabel == localLabel)
		{
			try
			{
				// Unpack UPLS packet and return payload to upstream (as UP packet)
				UP.UPPacket upPacket;
				upPacket = UPPacket.parseFrom(packet.getPayload());
				
				context.sendUpstream(new UpstreamMessageEvent(context.getChannel(), upPacket, null));				
			}
			catch (InvalidProtocolBufferException e)
			{
				callerFuture.setFailure(e);
			}
		}
		//
		// TRANSIT ROUTER
		//
		// Delegate UPLS packet to other router
		//
		else
		{
			final Channel incomingLink = callerFuture.getChannel();
			final int incomingLabel = destinationLabel;
			
			final Channel outgoingLink = this.uplsRoutingTable.lookupOutgoingLink(incomingLink, incomingLabel);
			final int outgoingLabel = this.uplsRoutingTable.lookupOutgoingLabel(incomingLink, incomingLabel);
			
			// Relabel packet with new local destination label
			this.relabelUPLSPacket(packet, outgoingLabel);
			
			// Send UPLS packet to outgoing link
			final boolean noOutgoingLink = (outgoingLink == null);

			if (noOutgoingLink)
			{
				callerFuture.setFailure(new NoLabelForSwitchingException(outgoingLink, outgoingLabel));
			}
			else
			{
				final ChannelFutureListener listener = new ChannelFutureListener()
				{
					@Override
					public void operationComplete(final ChannelFuture future) throws Exception
					{
						callerFuture.setSuccess();
					}
				};
				outgoingLink.write(packet).addListener(listener);
			}
		}
	}
	
	private UPLS.UPLSPacket relabelUPLSPacket(UPLS.UPLSPacket packet, int newLabel)
	{
		// Relabel packet with new destination label
		return UPLS.UPLSPacket.newBuilder(packet).setLabel(newLabel).build();
	}

	private UPLS.UPLSPacket encapsulatePacket(final UP.UPPacket message)
	{
		// Lookup UPLS label for label switched routing
		int uplsLabel = this.uplsRoutingTable.lookupLabel(message.getDestination());
		
		//
		// INGRESS ROUTER
		//
		// Return encapsulated packet
		//
		return UPLS.UPLSPacket.newBuilder()
			.setPayload(ByteString.copyFrom(message.toByteArray()))
			.setLabel(uplsLabel)
			.build();
	}
}
