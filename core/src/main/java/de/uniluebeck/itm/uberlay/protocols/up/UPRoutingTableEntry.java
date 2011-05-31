package de.uniluebeck.itm.uberlay.protocols.up;

import org.jboss.netty.channel.Channel;

public interface UPRoutingTableEntry {

	UPAddress getNextHop();

	Channel getNextHopChannel();
}
