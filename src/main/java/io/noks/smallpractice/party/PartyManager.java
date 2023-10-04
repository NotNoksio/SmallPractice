package io.noks.smallpractice.party;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import io.noks.smallpractice.Main;
import io.noks.smallpractice.enums.PlayerStatus;
import io.noks.smallpractice.objects.managers.PlayerManager;

public class PartyManager {
	private Map<UUID, Party> leaderUUIDtoParty;
    private Map<UUID, UUID> playerUUIDtoLeaderUUID;
    private Inventory partiesInventory;
	private Main main;
	public PartyManager(Main main) {
		this.main = main;
		this.leaderUUIDtoParty = Maps.newHashMap();
		this.playerUUIDtoLeaderUUID = Maps.newHashMap();
		this.partiesInventory = main.getServer().createInventory(null, 54, "Fight other parties");
	}
    
    public Party getParty(UUID player) {
        if (this.leaderUUIDtoParty.containsKey(player)) {
        	return this.leaderUUIDtoParty.get(player);
        }
        if (this.playerUUIDtoLeaderUUID.containsKey(player)) {
            final UUID leader = this.playerUUIDtoLeaderUUID.get(player);
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
        final Party party = new Party(leader, leadername);
        this.leaderUUIDtoParty.put(leader, party);
        addPartyToInventory(party);
        return party;
    }
    
    public void transferLeader(UUID actualLeader) {
    	Party party = this.leaderUUIDtoParty.get(actualLeader);
    	deletePartyFromInventory(party);
    	if (party.getSize() > 1) {
    		UUID newLeader = party.getMembers().get(0);
    		
    		party.setNewLeader(newLeader, this.main.getServer().getPlayer(newLeader).getName());
    		this.leaderUUIDtoParty.remove(actualLeader);
    		if (party.getMembers().contains(newLeader)) {
    			party.removeMember(newLeader);
    		}
    		if (this.playerUUIDtoLeaderUUID.containsKey(newLeader)) {
    			this.playerUUIDtoLeaderUUID.remove(newLeader);
    		}
    		this.leaderUUIDtoParty.put(newLeader, party);
    		
    		party = this.leaderUUIDtoParty.get(newLeader);
    		for (Map.Entry<UUID, UUID> entry : this.playerUUIDtoLeaderUUID.entrySet()) {
    			final UUID leaderEntry = entry.getValue();
    			if (leaderEntry != actualLeader) {
    				continue;
    			}
    			final UUID member = entry.getKey();
    			this.playerUUIDtoLeaderUUID.put(member, newLeader);
    		}
    		
    		party.notify(ChatColor.RED + "Your party leader has left, so the new party leader is " + party.getLeaderName());
    		if (party.getState() == PartyState.LOBBY) this.main.getItemManager().giveSpawnItem(this.main.getServer().getPlayer(newLeader));
    		addPartyToInventory(party);
    		return;
    	}
    	destroyParty(actualLeader);
    }
    
    public void destroyParty(UUID leader) {
        for (Map.Entry<UUID, UUID> entry : this.playerUUIDtoLeaderUUID.entrySet()) {
        	final UUID leaderEntry = entry.getValue();
        	if (leaderEntry != leader) {
        		continue;
        	}
        	this.playerUUIDtoLeaderUUID.remove(entry.getKey());
        	if (getParty(leader).getState() != PartyState.DUELING) {
        		this.main.getItemManager().giveSpawnItem(this.main.getServer().getPlayer(entry.getKey()));
        	}
        }
        deletePartyFromInventory(this.leaderUUIDtoParty.get(leader));
        this.leaderUUIDtoParty.remove(leader);
    }
    
    public void leaveParty(UUID player) {
        UUID leader = this.playerUUIDtoLeaderUUID.get(player);
        this.playerUUIDtoLeaderUUID.remove(player);
        Party party = this.leaderUUIDtoParty.get(leader);
        party.removeMember(player);
        updatePartyInventory(party);
    }
    
    public void joinParty(UUID leader, UUID player) {
        Party party = this.leaderUUIDtoParty.get(leader);
        party.addMember(player);
        this.playerUUIDtoLeaderUUID.put(player, leader);
        updatePartyInventory(party);
    }
    
    public Inventory getPartiesInventory() {
    	return this.partiesInventory;
    }
    
    
    // UNDER THIS LINE IS ALREADY RECODED
    
    
    public void addPartyToInventory(Party party) {
        final Player player = this.main.getServer().getPlayer(party.getLeader());
        final ItemStack skull = new ItemStack(Material.SKULL_ITEM, party.getSize(), (short)(party.getState() == PartyState.LOBBY ? SkullType.PLAYER.ordinal() : SkullType.WITHER.ordinal()));
        final SkullMeta skullm = (SkullMeta) skull.getItemMeta();
        if (skull.getDurability() == SkullType.PLAYER.ordinal()) {
        	skullm.setOwner(player.getName());
        }
        skullm.setDisplayName(ChatColor.DARK_AQUA + player.getName() + " (" + ChatColor.YELLOW + party.getSize() + ChatColor.DARK_AQUA + ")");
        if (!party.getMembers().isEmpty()) {
        	final List<String> lore = Lists.newArrayList();
        	for (UUID membersUUID : party.getMembers()) {
        		final Player member = this.main.getServer().getPlayer(membersUUID);
        		lore.add(ChatColor.GRAY + "-> " + ChatColor.YELLOW + member.getName());
        		if (lore.size() >= 15) {
        			lore.add(ChatColor.GRAY + "..and " + (party.getMembers().size() - 15) + ChatColor.GRAY + " more..");
        			break;
        		}
        		if (PlayerManager.get(member.getUniqueId()).getStatus() == PlayerStatus.SPAWN && party.getSize() < 3) {
        			this.main.getItemManager().giveSpawnItem(member);
                }
        	}
        	skullm.setLore(lore);
        }
        skull.setItemMeta(skullm);
        this.partiesInventory.addItem(skull);
    }
    
    public void deletePartyFromInventory(Party party) {
        final Player player = this.main.getServer().getPlayer(party.getLeader());
        final String leaderName = (player == null ? party.getLeaderName() : player.getName());
        for (ItemStack itemStack : this.partiesInventory.getContents()) {
            if (itemStack == null) continue;
            if (itemStack.getItemMeta().hasDisplayName() && itemStack.getItemMeta().getDisplayName().contains(leaderName)) {
            	this.partiesInventory.remove(itemStack);
            	break;
            }
        }
    }
    
    public void updatePartyInventory(Party party) {
    	this.partiesInventory.clear();
        for (Party parties : this.leaderUUIDtoParty.values()) {
        	addPartyToInventory(parties);
        }
        final Player leader = this.main.getServer().getPlayer(party.getLeader());
        if (party.getSize() < 3 && leader != null) {
        	this.main.getItemManager().giveSpawnItem(leader);
        }
    }
}
