package io.noks.smallpractice.party;

import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.google.common.collect.Lists;

import io.noks.smallpractice.objects.managers.EloManager;

public class Party {
	private List<UUID> memberUUIDs;
    private UUID partyLeader;
    private String leaderName;
    private PartyState partyState;
    private boolean open;
    private EloManager partyEloManager;
    
    public Party(UUID partyLeader, String leaderName) {
        this.memberUUIDs = Lists.newArrayList();
        this.partyLeader = partyLeader;
        this.leaderName = leaderName;
        this.partyState = PartyState.LOBBY;
        this.partyEloManager = new EloManager();
    }
    
    public void addMember(UUID uuid) {
        this.memberUUIDs.add(uuid);
    }
    
    public void removeMember(UUID uuid) {
        this.memberUUIDs.remove(uuid);
    }
    
    public void setOpen(boolean open) {
        this.open = open;
    }

    public void setPartyState(PartyState state) {
        this.partyState = state;
    }
    
    public List<UUID> getMembers() {
        return this.memberUUIDs;
    }
    
    public List<UUID> getMembersIncludeLeader() {
    	List<UUID> list = Lists.newArrayList(this.memberUUIDs);
    	list.add(partyLeader);
        return list;
    }
    
    public void setNewLeader(UUID newLeader, String newLeaderName) {
    	this.partyLeader = newLeader;
    	this.leaderName = newLeaderName;
    	if (this.memberUUIDs.contains(newLeader)) this.memberUUIDs.remove(newLeader);
    }
    
    public UUID getLeader() {
        return this.partyLeader;
    }
    
    public String getLeaderName() {
        return this.leaderName;
    }
    
    public boolean isOpen() {
        return this.open;
    }
    
    public PartyState getPartyState() {
        return this.partyState;
    }
    
    public int getSize() {
        return (this.getMembers().size() + 1);
    }

    public List<UUID> getAllMembersOnline() {
        List<UUID> membersOnline = Lists.newArrayList();

        for(UUID memberUUID : this.memberUUIDs) {
            Player member = Bukkit.getPlayer(memberUUID);

            if(member == null) {
            	continue;
            }
            membersOnline.add(member.getUniqueId());
        }
        membersOnline.add(partyLeader);
        return membersOnline;
    }
    
    public void notify(String message) {
        Player leaderPlayer = Bukkit.getPlayer(this.partyLeader);
        leaderPlayer.sendMessage(message);
        for (UUID uuid : this.memberUUIDs) {
            Player memberPlayer = Bukkit.getPlayer(uuid);
            if (memberPlayer == null) continue;
            memberPlayer.sendMessage(message);
        }
    }
    
    public EloManager getPartyEloManager() {
    	return this.partyEloManager;
    }
}
