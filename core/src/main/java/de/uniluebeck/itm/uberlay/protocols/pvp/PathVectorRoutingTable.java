package de.uniluebeck.itm.uberlay.protocols.pvp;


import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import de.uniluebeck.itm.tr.util.TimedCache;
import de.uniluebeck.itm.uberlay.RoutingTable;
import de.uniluebeck.itm.uberlay.RoutingTableEntry;
import de.uniluebeck.itm.uberlay.RoutingTableEntryImpl;
import de.uniluebeck.itm.uberlay.protocols.up.UPAddress;
import org.jboss.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A routing table implementation that holds a mapping from destination node to a tuple of (cost, path). An entry is
 * kept in the table until a configurable timeout occurs. If the entry is updated in the mean time the entry timeout
 * will be reset.
 */
public class PathVectorRoutingTable implements RoutingTable {

	private static final Logger log = LoggerFactory.getLogger(PathVectorRoutingTable.class);

	private final UPAddress nodeName;

	/**
	 * The actual routing table as a {@link java.util.Map}, mapping the destination node to an {@link de.uniluebeck.itm.uberlay.RoutingTableEntryImpl}.
	 */
	private final TimedCache<UPAddress, RoutingTableEntryImpl> routingTable;

	/**
	 * Constructs a new routing table instance.
	 *
	 * @param nodeName				   the name of this node
	 * @param cacheEntryLifetime		 the maximum life time of a routing table entry after which it will be deleted if
	 *                                   it is not updated before the maximum lifetime occurs
	 * @param cacheEntryLifetimeTimeUnit the time unit of {@code cacheEntryLifeTime}
	 */
	public PathVectorRoutingTable(final UPAddress nodeName, final int cacheEntryLifetime,
								  final TimeUnit cacheEntryLifetimeTimeUnit) {
		this.nodeName = nodeName;
		this.routingTable = new TimedCache<UPAddress, RoutingTableEntryImpl>(cacheEntryLifetime, cacheEntryLifetimeTimeUnit);
	}

	/**
	 * Checks if the path contains a loop.
	 *
	 * @param path the path to check for loops
	 *
	 * @return {@code true} if the path contains a loop, {@code false} otherwise
	 */
	private boolean containsLoop(final List<UPAddress> path) {
		return path.contains(nodeName) || Sets.newHashSet(path).size() < path.size();
	}


	@Override
	public synchronized ImmutableMap<UPAddress, RoutingTableEntry> getEntries() {
		return ImmutableMap.<UPAddress, RoutingTableEntry>copyOf(routingTable);
	}

	@Override
	public synchronized RoutingTableEntry getEntry(UPAddress destination) {
		return routingTable.get(destination);
	}

	@Override
	public synchronized UPAddress getNextHop(UPAddress destination) {
		RoutingTableEntry entry = routingTable.get(destination);
		return entry != null ? entry.getNextHop() : null;
	}

	@Override
	public synchronized Channel getNextHopChannel(UPAddress destination) {
		RoutingTableEntry entry = routingTable.get(destination);
		return entry != null ? entry.getNextHopChannel() : null;
	}

	@Override
	public synchronized boolean updateEntry(final UPAddress destination, final long cost, final List<UPAddress> path,
											final Channel channel) {

		checkNotNull(destination);
		checkArgument(path.size() > 0);
		checkNotNull(channel);

		if (!containsLoop(path)) {

			RoutingTableEntryImpl destinationEntry = routingTable.get(destination);
			long oldCost = destinationEntry == null ? Long.MAX_VALUE : destinationEntry.getCost();

			if (cost <= oldCost) {

				RoutingTableEntryImpl entry = new RoutingTableEntryImpl(cost, path, channel);
				log.trace("Updating routing table entry: {}", entry);
				routingTable.put(destination, entry);
				if (log.isDebugEnabled()) {
					logRoutingTable();
				}
				return true;

			} else {
				if (log.isTraceEnabled()) {
					log.trace(
							"Not updating routing table entry because the update is not cheaper: dst={}, oldcost={}, newcost={}",
							new Object[]{
									destination, oldCost, cost
							}
					);
				}
				return false;
			}
		} else {

			if (log.isTraceEnabled()) {
				log.trace("Not updating routing table entry because the update contains a loop: dst={}, path={}",
						destination, Arrays.toString(path.toArray())
				);
			}
			return false;
		}
	}

	@Override
	public void removeRoutesOverNextHop(final UPAddress remoteNode) {
		for (Iterator<Map.Entry<UPAddress, RoutingTableEntryImpl>> iterator = routingTable.entrySet().iterator(); iterator.hasNext(); ) {
			Map.Entry<UPAddress, RoutingTableEntryImpl> entry = iterator.next();
			if (entry.getValue().getNextHop().equals(remoteNode)) {
				iterator.remove();
			}
		}
		if (log.isDebugEnabled()) {
			log.debug("Removed routes over \"{}\" as that next hop is now unavailable.", remoteNode);
			logRoutingTable();
		}
	}

	private void logRoutingTable() {
		StringBuilder b = new StringBuilder();
		b.append("RoutingTable:\n");
		for (Map.Entry<UPAddress, RoutingTableEntryImpl> entry : ImmutableMap.copyOf(routingTable).entrySet()) {
			b.append(entry.getKey());
			b.append(" => [");
			b.append(entry.getValue().getCost());
			b.append("] {");
			b.append(Joiner.on(", ").join(entry.getValue().getPath()));
			b.append("}\n");
		}
		log.debug(b.toString());
	}

}
