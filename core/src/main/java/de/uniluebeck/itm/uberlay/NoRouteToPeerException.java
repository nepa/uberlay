package de.uniluebeck.itm.uberlay;

import de.uniluebeck.itm.uberlay.protocols.up.UPAddress;

public class NoRouteToPeerException extends Exception {

	private UPAddress peerAddress;

	public NoRouteToPeerException(final UPAddress peerAddress) {
		this.peerAddress = peerAddress;
	}

	public UPAddress getPeerAddress() {
		return peerAddress;
	}
}
