package de.uniluebeck.itm.uberlay.protocols.upls;

import org.jboss.netty.channel.Channel;

/**
 * Created by IntelliJ IDEA. User: bimschas Date: 31.05.11 Time: 17:58 TODO change
 */
public class UPLSRoutingTable
{
	public int lookupLabel(final String destinationAddress)
	{
		return 0; // TODO Lookup label
	}
	
	public int getLocalLabel()
	{
		return 0; // TODO Return local label
	}
	
	public Channel lookupOutgoingLink(final Channel incomingLink, final int incomingLabel)
	{
		return null; // TODO Return outgoing link
	}		
	
	public int lookupOutgoingLabel(final Channel incomingLink, final int incomingLabel)
	{
		return 0; // TODO Return outgoing label
	}	
}
