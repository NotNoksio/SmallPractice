package io.noks.smallpractice.utils;

import java.util.UUID;

public class PartnerCache {
	private UUID partner;
	private Integer elo;
	
	public PartnerCache(UUID partner, Integer elo) {
		this.partner = partner;
		this.elo = elo;
	}
	
	public UUID getPartner() {
		return this.partner;
	}
	
	public Integer getElo() {
		return this.elo;
	}
}
