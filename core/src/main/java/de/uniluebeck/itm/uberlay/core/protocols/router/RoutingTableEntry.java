package de.uniluebeck.itm.uberlay.core.protocols.router;

import de.uniluebeck.itm.uberlay.core.protocols.up.UPAddress;
import org.jboss.netty.channel.Channel;

public interface RoutingTableEntry {

	UPAddress getNextHop();

	Channel getNextHopChannel();
}
