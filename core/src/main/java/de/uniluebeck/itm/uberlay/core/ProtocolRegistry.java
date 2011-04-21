package de.uniluebeck.itm.uberlay.core;

import com.google.common.collect.Maps;
import com.google.protobuf.MessageLite;
import de.uniluebeck.itm.uberlay.core.protocols.pvp.PathVectorMessages;
import de.uniluebeck.itm.uberlay.core.protocols.rtt.RoundtripTimeMessages;

import java.util.Map;

public class ProtocolRegistry {

	public static final Map<Integer, MessageLite> REGISTRY = Maps.newHashMap();

	public static final int HEADER_FIELD_LENGTH = 1;

	static {
		REGISTRY.put(0, RoundtripTimeMessages.RoundtripTimeRequest.getDefaultInstance());
		REGISTRY.put(1, RoundtripTimeMessages.RoundtripTimeResponse.getDefaultInstance());
		REGISTRY.put(2, PathVectorMessages.PathVectorUpdate.getDefaultInstance());
	}

}
