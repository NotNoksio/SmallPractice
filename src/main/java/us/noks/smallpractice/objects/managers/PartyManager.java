package us.noks.smallpractice.objects.managers;

import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import net.minecraft.util.com.google.common.collect.Maps;
import us.noks.smallpractice.party.Party;

public class PartyManager {
	
	static PartyManager instance = new PartyManager();
	public static PartyManager getInstance() {
		return instance;
	}
	
	private Map<UUID, Party> leaderUUIDtoParty = Maps.newHashMap();
    private Map<UUID, UUID> playerUUIDtoLeaderUUID = Maps.newHashMap();
    
    public Party getParty(UUID player) {
        if (this.leaderUUIDtoParty.containsKey(player)) {
            return this.leaderUUIDtoParty.get(player);
        }
        if (this.playerUUIDtoLeaderUUID.containsKey(player)) {
            UUID leader = this.playerUUIDtoLeaderUUID.get(player);
            return this.leaderUUIDtoParty.get(leader);
        }
        return null;
    }
    
    public Map<UUID, Party> getPartyMap() {
        return this.leaderUUIDtoParty;
    }
    
    public boolean hasParty(UUID player) {
    	return getParty(player) != null;
    }
    
    public Party createParty(UUID leader, String leadername) {
        Party party = new Party(leader, leadername);
        this.leaderUUIDtoParty.put(leader, party);
        return party;
    }
    
    public void destroyParty(UUID leader) {
        Party party = this.leaderUUIDtoParty.get(leader);
        this.leaderUUIDtoParty.remove(leader);
        for (UUID member : party.getMembers()) {
            this.playerUUIDtoLeaderUUID.remove(member);
        }
    }
    
    public void leaveParty(UUID player) {
        UUID leader = this.playerUUIDtoLeaderUUID.get(player);
        this.playerUUIDtoLeaderUUID.remove(player);
        Party party = this.leaderUUIDtoParty.get(leader);
        party.removeMember(player);
    }
    
    public void joinParty(UUID leader, UUID player) {
        Party party = this.leaderUUIDtoParty.get(leader);
        party.addMember(player);
        this.playerUUIDtoLeaderUUID.put(player, leader);
    }
    
    public void notifyParty(Party party, String message) {
        Player leaderPlayer = Bukkit.getPlayer(party.getLeader());
        leaderPlayer.sendMessage(message);
        for (UUID uuid : party.getMembers()) {
            Player memberPlayer = Bukkit.getPlayer(uuid);
            if (memberPlayer == null) {
                continue;
            }
            memberPlayer.sendMessage(message);
        }
    }
}
