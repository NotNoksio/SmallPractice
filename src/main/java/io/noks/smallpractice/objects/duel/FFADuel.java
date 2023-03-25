package io.noks.smallpractice.objects.duel;

import java.util.List;
import java.util.UUID;

import com.google.common.collect.Lists;

public class FFADuel {
	private UUID ffaPartyLeaderUUID;
    private List<UUID> ffaPlayers;
    private List<UUID> ffaAlivePlayers;
    
    public FFADuel(UUID ffaLeaderUUID, List<UUID> ffaPlayers) {
    	this.ffaPartyLeaderUUID = ffaLeaderUUID;
		this.ffaPlayers = Lists.newArrayList(ffaPlayers);
		this.ffaAlivePlayers = Lists.newArrayList(ffaPlayers);
    }
    
    public UUID getFfaPartyLeaderUUID() {
        return this.ffaPartyLeaderUUID;
    }
    
    public List<UUID> getFfaPlayers() {
        return this.ffaPlayers;
    }
    
    public List<UUID> getFfaAlivePlayers() {
        return this.ffaAlivePlayers;
    }
}
