package de.uniluebeck.itm.uberlay.core.protocols.pvp;

import org.jboss.netty.channel.Channel;

/**
 * Created by IntelliJ IDEA. User: bimschas Date: 30.05.11 Time: 10:48 TODO change
 */
public interface RoutingTableEntry {

	String getNextHop();

	Channel getNextHopChannel();
}
