package io.noks.smallpractice.party;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.google.common.collect.Lists;

import io.noks.smallpractice.Main;
import io.noks.smallpractice.objects.managers.EloManager;
import io.noks.smallpractice.utils.DBUtils;

public class Party {
	private final Main main = Main.getInstance();
	private final List<UUID> memberUUIDs;
    private UUID partyLeader;
    private String leaderName;
    private PartyState state;
    private boolean open;
    private @Nullable EloManager eloManager;
    
    public Party(UUID partyLeader, String leaderName) {
        this.memberUUIDs = new LinkedList<UUID>();
        this.partyLeader = partyLeader;
        this.leaderName = leaderName;
        this.state = PartyState.LOBBY;
    }
    
    public UUID getPartner() {
    	return this.memberUUIDs.get(0);
    }
    
    public void addMember(UUID uuid) {
        this.memberUUIDs.add(uuid);
        if (getMembersIncludingLeader().size() != 2) {
        	if (this.eloManager != null) {
        		this.eloManager = null;
        	}
        	return;
        }
        final DBUtils db = Main.getInstance().getDatabaseUtil();
        if (db.isDuoExist(this.partyLeader, getPartner())) {
        	this.eloManager = db.loadOrCreateDuo(this.partyLeader, getPartner());
        	return;
        }
        if (db.isDuoExist(getPartner(), this.partyLeader)) {
        	this.eloManager = db.loadOrCreateDuo(getPartner(), this.partyLeader);
        }
    }
    
    public void removeMember(UUID uuid) {
        this.memberUUIDs.remove(uuid);
        if (getMembersIncludingLeader().size() != 2) {
        	if (this.eloManager != null) {
        		this.eloManager = null;
        	}
        	return;
        }
        final DBUtils db = Main.getInstance().getDatabaseUtil();
        if (db.isDuoExist(this.partyLeader, getPartner())) {
        	this.eloManager = db.loadOrCreateDuo(this.partyLeader, getPartner());
        	return;
        }
        if (db.isDuoExist(getPartner(), this.partyLeader)) {
        	this.eloManager = db.loadOrCreateDuo(getPartner(), this.partyLeader);
        }
    }
    
    public void setOpen(boolean open) {
        this.open = open;
        if (open) {
        	main.getServer().broadcastMessage(ChatColor.YELLOW + this.leaderName + ChatColor.GREEN + "'s party is now open!");
        }
    }
    
    public void initElo() {
    	this.eloManager = main.getDatabaseUtil().loadOrCreateDuo(this.partyLeader, getPartner());
    	main.getItemManager().giveSpawnItem(main.getServer().getPlayer(this.partyLeader));
    	main.getItemManager().giveSpawnItem(main.getServer().getPlayer(getPartner()));
    	main.getInventoryManager().setLeaderboardInventory();
    }

    public void setPartyState(PartyState state) {
        this.state = state;
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
    
    public PartyState getState() {
        return this.state;
    }
    
    public int getSize() {
        return (this.getMembers().size() + 1);
    }
    
    public void notify(String message) {
        final Player leaderPlayer = main.getServer().getPlayer(this.partyLeader);
        leaderPlayer.sendMessage(message);
        for (UUID uuid : this.memberUUIDs) {
            Player memberPlayer = main.getServer().getPlayer(uuid);
            if (memberPlayer == null) continue;
            memberPlayer.sendMessage(message);
        }
    }
    
    public EloManager getEloManager() {
    	return this.eloManager;
    }
}
