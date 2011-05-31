package de.uniluebeck.itm.uberlay;

import de.uniluebeck.itm.uberlay.protocols.up.UPAddress;
import org.jboss.netty.channel.Channel;

public interface RoutingTableEntry {

	UPAddress getNextHop();

	Channel getNextHopChannel();
}
