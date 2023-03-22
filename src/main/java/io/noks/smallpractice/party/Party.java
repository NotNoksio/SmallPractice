package io.noks.smallpractice.party;

import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.google.common.collect.Lists;

import io.noks.smallpractice.Main;
import io.noks.smallpractice.objects.managers.EloManager;
import io.noks.smallpractice.utils.DBUtils;

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
    }
    
    public UUID getPartner() {
    	return this.memberUUIDs.get(0);
    }
    
    public void addMember(UUID uuid) {
        this.memberUUIDs.add(uuid);
        if (getMembersIncludingLeader().size() != 2) {
        	if (this.partyEloManager != null) {
        		this.partyEloManager = null;
        	}
        	return;
        }
        final DBUtils db = Main.getInstance().getDatabaseUtil();
        if (db.isDuoExist(this.partyLeader, getPartner())) {
        	this.partyEloManager = db.loadOrCreateDuo(this.partyLeader, getPartner());
        	return;
        }
        if (db.isDuoExist(getPartner(), this.partyLeader)) {
        	this.partyEloManager = db.loadOrCreateDuo(getPartner(), this.partyLeader);
        }
    }
    
    public void removeMember(UUID uuid) {
        this.memberUUIDs.remove(uuid);
        if (getMembersIncludingLeader().size() != 2) {
        	if (this.partyEloManager != null) {
        		this.partyEloManager = null;
        	}
        	return;
        }
        final DBUtils db = Main.getInstance().getDatabaseUtil();
        if (db.isDuoExist(this.partyLeader, getPartner())) {
        	this.partyEloManager = db.loadOrCreateDuo(this.partyLeader, getPartner());
        	return;
        }
        if (db.isDuoExist(getPartner(), this.partyLeader)) {
        	this.partyEloManager = db.loadOrCreateDuo(getPartner(), this.partyLeader);
        }
    }
    
    public void setOpen(boolean open) {
        this.open = open;
        if (open) {
        	Bukkit.broadcastMessage(ChatColor.YELLOW + this.leaderName + ChatColor.GREEN + "'s party is now open!");
        }
    }
    
    public void updateElo() {
    	this.partyEloManager = Main.getInstance().getDatabaseUtil().loadOrCreateDuo(this.partyLeader, getPartner());
    	Main.getInstance().getItemManager().giveSpawnItem(Bukkit.getPlayer(this.partyLeader));
    	Main.getInstance().getItemManager().giveSpawnItem(Bukkit.getPlayer(getPartner()));
    	Main.getInstance().getInventoryManager().setLeaderboardInventory();
    }

    public void setPartyState(PartyState state) {
        this.partyState = state;
    }
    
    public List<UUID> getMembers() {
        return this.memberUUIDs;
    }
    
    public List<UUID> getMembersIncludingLeader() {
    	final List<UUID> list = Lists.newArrayList(this.memberUUIDs);
    	list.add(partyLeader);
        return list;
    }
    
    public void setNewLeader(UUID newLeader, String newLeaderName) {
    	this.partyLeader = newLeader;
    	this.leaderName = newLeaderName;
    	if (this.memberUUIDs.contains(newLeader)) removeMember(newLeader);
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
