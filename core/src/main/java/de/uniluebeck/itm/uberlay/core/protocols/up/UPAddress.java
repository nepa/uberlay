package de.uniluebeck.itm.uberlay.core.protocols.up;

import java.net.SocketAddress;

public class UPAddress extends SocketAddress {

	private String address;

	public UPAddress(final String address) {
		this.address = address;
	}

	public String getAddress() {
		return address;
	}
}
