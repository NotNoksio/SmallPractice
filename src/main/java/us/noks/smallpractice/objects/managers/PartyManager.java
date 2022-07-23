package us.noks.smallpractice.objects.managers;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import us.noks.smallpractice.Main;
import us.noks.smallpractice.enums.PlayerStatus;
import us.noks.smallpractice.party.Party;
import us.noks.smallpractice.party.PartyState;

public class PartyManager {
	private Map<UUID, Party> leaderUUIDtoParty = Maps.newHashMap();
    private Map<UUID, UUID> playerUUIDtoLeaderUUID = Maps.newHashMap();
    private Inventory partiesInventory = Bukkit.createInventory(null, 54, "Fight other parties");
    
    public Party getParty(UUID player) {
        if (this.leaderUUIDtoParty.containsKey(player)) return this.leaderUUIDtoParty.get(player);
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
        addPartyToInventory(party);
        return party;
    }
    
    public void transferLeader(UUID actualLeader) {
    	Party party = this.leaderUUIDtoParty.get(actualLeader);
    	deletePartyFromInventory(party); // Just added
    	if (party.getSize() > 1) {
    		UUID newLeader = party.getMembers().get(0);
    		
    		party.setNewLeader(newLeader, Bukkit.getPlayer(newLeader).getName());
    		this.leaderUUIDtoParty.remove(actualLeader);
    		
    		this.leaderUUIDtoParty.put(newLeader, party);
    		party = this.leaderUUIDtoParty.get(newLeader);
    		
    		party.notify(ChatColor.RED + "Your party leader has left, so the new party leader is " + party.getLeaderName());
    		if (party.getPartyState() == PartyState.LOBBY) Main.getInstance().getItemManager().giveSpawnItem(Bukkit.getPlayer(newLeader));
    		addPartyToInventory(party);
    		return;
    	}
    	destroyParty(actualLeader);
    }
    
    public void destroyParty(UUID leader) {
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
    
    public void addPartyToInventory(Party party) {
        Player player = Bukkit.getPlayer(party.getLeader());
        ItemStack skull = new ItemStack(Material.SKULL_ITEM, party.getSize(), (short)SkullType.PLAYER.ordinal());
        SkullMeta skullm = (SkullMeta) skull.getItemMeta();
        skullm.setOwner(player.getName());
        skullm.setDisplayName(ChatColor.DARK_AQUA + player.getName() + " (" + ChatColor.YELLOW + party.getSize() + ChatColor.DARK_AQUA + ")");
        skull.setItemMeta(skullm);
        if (partiesInventory.contains(skull)) {
        	partiesInventory.remove(skull);
        }
        this.partiesInventory.addItem(skull);
    }
    
    public void deletePartyFromInventory(Party party) {
        Player player = Bukkit.getPlayer(party.getLeader());
        String leaderName = (player == null ? party.getLeaderName() : player.getName());
        for (ItemStack itemStack : this.partiesInventory.getContents()) {
            if (itemStack == null) continue;
            if (itemStack.getItemMeta().hasDisplayName() && itemStack.getItemMeta().getDisplayName().contains(leaderName)) {
            	this.partiesInventory.remove(itemStack);
            	break;
            }
        }
    }
    
    // TODO: need optimization & put the head of the leader on the first empty slot
    public void updatePartyInventory(Party party) {
        Player leader = Bukkit.getPlayer(party.getLeader());
        if (party.getSize() < 3) {
        	Main.getInstance().getItemManager().giveSpawnItem(leader);
        }
        String leaderName = (leader == null ? party.getLeaderName() : leader.getName());
        List<String> lores = Lists.newArrayList();
        for (UUID uuid : party.getMembers()) {
            Player members = Bukkit.getPlayer(uuid);
            if (members == null) continue;
            lores.add(ChatColor.GRAY + "-> " + ChatColor.YELLOW + members.getName());
            if (new PlayerManager().get(members.getUniqueId()).getStatus() == PlayerStatus.SPAWN && party.getSize() < 3) {
            	Main.getInstance().getItemManager().giveSpawnItem(members);
            }
        }
        for (ItemStack itemStack : this.partiesInventory.getContents()) {
            if (itemStack == null) continue;
            if (itemStack.getItemMeta().hasDisplayName() && itemStack.getItemMeta().getDisplayName().contains(leaderName)) {
            	this.partiesInventory.remove(itemStack);
            	itemStack = (party.getPartyState() == PartyState.LOBBY ? new ItemStack(Material.SKULL_ITEM, party.getSize(), (short)SkullType.PLAYER.ordinal()) : new ItemStack(Material.SKULL_ITEM, party.getSize(), (short)SkullType.WITHER.ordinal()));
            	SkullMeta itemMeta = (SkullMeta) itemStack.getItemMeta();
            	if (party.getPartyState() == PartyState.LOBBY) {
            		itemMeta.setOwner(leader.getName());
            	}
            	itemMeta.setDisplayName(ChatColor.DARK_AQUA + leaderName + " (" + ChatColor.YELLOW + party.getSize() + ChatColor.DARK_AQUA + ")");
            	itemMeta.setLore(lores);
            	itemStack.setItemMeta(itemMeta);
            	this.partiesInventory.addItem(itemStack);
            	break;
            }
        }
    }
}
