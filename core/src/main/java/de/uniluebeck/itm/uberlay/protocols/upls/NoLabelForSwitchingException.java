package de.uniluebeck.itm.uberlay.protocols.upls;

import org.jboss.netty.channel.Channel;

public class NoLabelForSwitchingException extends Exception
{
	private Channel outgoingLink;
	private int outgoingLabel;
	
	public NoLabelForSwitchingException(final Channel outgoingLink, final int outgoingLabel)
	{
		this.outgoingLink = outgoingLink;
		this.outgoingLabel = outgoingLabel;
	}

	public Channel getOutgoingLink()
	{
		return this.outgoingLink;
	}
	
	public int getOutgoingLabel()
	{
		return this.outgoingLabel;
	}
}
