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
import org.bukkit.inventory.meta.ItemMeta;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import us.noks.smallpractice.party.Party;
import us.noks.smallpractice.party.PartyState;

public class PartyManager {
	private static PartyManager instance = new PartyManager();
	public static PartyManager getInstance() {
		return instance;
	}
	
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
        addParty(party);
        return party;
    }
    
    public void transferLeader(UUID actualLeader) {
    	Party party = this.leaderUUIDtoParty.get(actualLeader);
    	if (party.getSize() > 1) {
    		UUID newLeader = party.getMembers().get(0);
    		
    		party.setNewLeader(newLeader, Bukkit.getPlayer(newLeader).getName());
    		this.leaderUUIDtoParty.remove(actualLeader);
    		
    		this.leaderUUIDtoParty.put(newLeader, party);
    		party = this.leaderUUIDtoParty.get(newLeader);
    		
    		notifyParty(party, ChatColor.RED + "Your party leader has left, so the new party leader is " + party.getLeaderName());
    		ItemManager.getInstace().giveSpawnItem(Bukkit.getPlayer(newLeader));
    		updateParty(party);
    		return;
    	}
    	destroyParty(actualLeader);
    }
    
    public void destroyParty(UUID leader) {
    	deleteParty(this.leaderUUIDtoParty.get(leader));
        this.leaderUUIDtoParty.remove(leader);
    }
    
    public void leaveParty(UUID player) {
        UUID leader = this.playerUUIDtoLeaderUUID.get(player);
        this.playerUUIDtoLeaderUUID.remove(player);
        Party party = this.leaderUUIDtoParty.get(leader);
        party.removeMember(player);
        updateParty(party);
    }
    
    public void joinParty(UUID leader, UUID player) {
        Party party = this.leaderUUIDtoParty.get(leader);
        party.addMember(player);
        this.playerUUIDtoLeaderUUID.put(player, leader);
        updateParty(party);
    }
    
    public void notifyParty(Party party, String message) {
        Player leaderPlayer = Bukkit.getPlayer(party.getLeader());
        leaderPlayer.sendMessage(message);
        for (UUID uuid : party.getMembers()) {
            Player memberPlayer = Bukkit.getPlayer(uuid);
            if (memberPlayer == null) continue;
            memberPlayer.sendMessage(message);
        }
    }
    
    public Inventory getPartiesInventory() {
    	return this.partiesInventory;
    }
    
    public void addParty(Party party) {
        Player player = Bukkit.getPlayer(party.getLeader());
        ItemStack skull = new ItemStack(Material.SKULL_ITEM, party.getSize(), (short)SkullType.PLAYER.ordinal());
        ItemMeta skullm = skull.getItemMeta();
        skullm.setDisplayName(ChatColor.DARK_AQUA + player.getName() + " (" + ChatColor.YELLOW + party.getSize() + ChatColor.DARK_AQUA + ")");
        skull.setItemMeta(skullm);
        this.partiesInventory.addItem(skull);
    }
    
    public void deleteParty(Party party) {
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
    
    public void updateParty(Party party) {
        Player player = Bukkit.getPlayer(party.getLeader());
        String leaderName = (player == null ? party.getLeaderName() : player.getName());
        List<String> lores = Lists.newArrayList();
        for (UUID uuid : party.getMembers()) {
            Player memberPlayer = Bukkit.getPlayer(uuid);
            if (memberPlayer == null) continue;
            lores.add(ChatColor.GRAY + "-> " + ChatColor.YELLOW + memberPlayer.getName());
        }
        for (ItemStack itemStack : this.partiesInventory.getContents()) {
            if (itemStack == null) continue;
            if (itemStack.getItemMeta().hasDisplayName() && itemStack.getItemMeta().getDisplayName().contains(leaderName)) {
            	itemStack = (party.getPartyState() == PartyState.LOBBY ? new ItemStack(Material.SKULL_ITEM, party.getSize(), (short)SkullType.PLAYER.ordinal()) : new ItemStack(Material.SKULL_ITEM, party.getSize(), (short)SkullType.WITHER.ordinal()));
            	ItemMeta itemMeta = itemStack.getItemMeta();
            	itemMeta.setLore(lores);
            	itemMeta.setDisplayName(ChatColor.DARK_AQUA + leaderName + " (" + ChatColor.YELLOW + party.getSize() + ChatColor.DARK_AQUA + ")");
            	itemStack.setItemMeta(itemMeta);
            	break;
            }
        }
    }
}
