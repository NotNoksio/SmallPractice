package us.noks.smallpractice.objects.managers;

import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import us.noks.smallpractice.arena.Arena;
import us.noks.smallpractice.arena.Arena.Arenas;
import us.noks.smallpractice.enums.Ladders;

public class InventoryManager {
	private Inventory arenasInventory;
	private Inventory unrankedInventory;
	private Map<UUID, UUID> selectingDuel;
	
	private static InventoryManager instance = new InventoryManager();
	public static InventoryManager getInstance() {
		return instance;
	}
	
	public InventoryManager() {
		this.selectingDuel = new WeakHashMap<UUID, UUID>();
		this.arenasInventory = Bukkit.createInventory(null, 18, "Arena Selection");
		this.unrankedInventory = Bukkit.createInventory(null, 9, "Unranked Selection");
		setArenasInventory();
		setUnrankedInventory();
	}
	
	private void setArenasInventory() {
		this.arenasInventory.clear();
		for (Map.Entry<Integer, Arenas> mapEntry : Arena.getInstance().getArenaList().entrySet()) {
			int i = mapEntry.getKey();
			Arenas arena = mapEntry.getValue();
			ItemStack item = arena.getIcon();
			ItemMeta itemm = item.getItemMeta();
			itemm.setDisplayName(ChatColor.GOLD + arena.getName());
			item.setItemMeta(itemm);
			
			this.arenasInventory.setItem(i - 1, item);
		}
	}
	
	private void setUnrankedInventory() {
		this.unrankedInventory.clear();
		for (Ladders ladders : Ladders.values()) {
			ItemStack item = ladders.getIcon();
			ItemMeta itemm = item.getItemMeta();
			itemm.setDisplayName(ladders.getColor() + ladders.getName());
			itemm.setLore(Arrays.asList(new String[] {" ", ChatColor.DARK_AQUA + "Fighting: " + ChatColor.RED + "Soon..", ChatColor.DARK_AQUA + "Waiting: " + ChatColor.RED + "Soon.."}));
			item.setItemMeta(itemm);
			
			this.unrankedInventory.addItem(item);
		}
	}
	
	public Inventory getArenasInventory() {
		return this.arenasInventory;
	}
	
	public Inventory getUnrankedInventory() {
		return this.unrankedInventory;
	}
	
	public void setSelectingDuel(UUID uuid, UUID uuid1) { 
		this.selectingDuel.put(uuid, uuid1); 
	}
	  
	public UUID getSelectingDuelPlayerUUID(UUID uuid) { 
		return this.selectingDuel.get(uuid); 
	}
	  
	public void removeSelectingDuel(UUID uuid) { 
		this.selectingDuel.remove(uuid); 
	}
}
