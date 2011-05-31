package de.uniluebeck.itm.uberlay.protocols;

import com.google.common.collect.Maps;
import com.google.protobuf.MessageLite;
import de.uniluebeck.itm.uberlay.protocols.pvp.PathVectorMessages;
import de.uniluebeck.itm.uberlay.protocols.rtt.RoundtripTimeMessages;
import de.uniluebeck.itm.uberlay.protocols.up.UP;

import java.util.Map;

public class ProtocolRegistry {

	public static final Map<Integer, MessageLite> REGISTRY = Maps.newHashMap();

	public static final int HEADER_FIELD_LENGTH = 1;

	static {
		REGISTRY.put(0, RoundtripTimeMessages.RoundtripTimeRequest.getDefaultInstance());
		REGISTRY.put(1, RoundtripTimeMessages.RoundtripTimeResponse.getDefaultInstance());
		REGISTRY.put(2, PathVectorMessages.PathVectorUpdate.getDefaultInstance());
		REGISTRY.put(3, UP.UPPacket.getDefaultInstance());
	}

}
