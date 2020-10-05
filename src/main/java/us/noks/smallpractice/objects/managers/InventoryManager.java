package us.noks.smallpractice.objects.managers;

import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import us.noks.smallpractice.arena.Arena;
import us.noks.smallpractice.arena.Arena.Arenas;

public class InventoryManager {
	private Inventory arenasInventory;
	private Map<UUID, UUID> selectingDuel;
	
	private static InventoryManager instance = new InventoryManager();
	public static InventoryManager getInstance() {
		return instance;
	}
	
	public InventoryManager() {
		this.selectingDuel = new WeakHashMap<UUID, UUID>();
		this.arenasInventory = Bukkit.createInventory(null, 18, "Arena Selection");
		setArenasInventory();
	}
	
	private void setArenasInventory() {
		this.arenasInventory.clear();
		for (Map.Entry<Integer, Arenas> mapEntry : Arena.getInstance().getArenaList().entrySet()) {
			int i = mapEntry.getKey();
			Arenas arena = mapEntry.getValue();
			ItemStack book = new ItemStack(Material.BOOK, 1);
			ItemMeta bookm = book.getItemMeta();
			bookm.setDisplayName(ChatColor.GOLD + arena.getName());
			book.setItemMeta(bookm);
			
			this.arenasInventory.setItem(i - 1, book);
		}
	}
	
	public Inventory getArenasInventory() {
		return this.arenasInventory;
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
