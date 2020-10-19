package us.noks.smallpractice.objects.managers;

import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import us.noks.smallpractice.arena.Arena;
import us.noks.smallpractice.arena.Arena.Arenas;
import us.noks.smallpractice.enums.Ladders;
import us.noks.smallpractice.objects.Request;
import us.noks.smallpractice.utils.ItemBuilder;

public class InventoryManager {
	private Inventory arenasInventory;
	private Inventory unrankedInventory;
	private Inventory laddersInventory;
	private Map<UUID, Request> selectingDuel;
	
	private static InventoryManager instance = new InventoryManager();
	public static InventoryManager getInstance() {
		return instance;
	}
	
	public InventoryManager() {
		this.selectingDuel = new WeakHashMap<UUID, Request>();
		this.arenasInventory = Bukkit.createInventory(null, 18, "Arena Selection");
		this.unrankedInventory = Bukkit.createInventory(null, 9, "Unranked Selection");
		this.laddersInventory = Bukkit.createInventory(null, 9, "Ladder Selection");
		this.setArenasInventory();
		this.setUnrankedInventory();
		this.setLaddersInventory();
	}
	
	private void setArenasInventory() {
		this.arenasInventory.clear();
		for (Arenas arena : Arena.getInstance().getArenaList().values()) {
			ItemStack item = ItemBuilder.getInstance().createNewItemStack(arena.getIcon(), ChatColor.GOLD + arena.getName());
			this.arenasInventory.addItem(item);
		}
	}
	
	public void updateUnrankedInventory() {
		this.setUnrankedInventory();
	}
	private void setUnrankedInventory() {
		this.unrankedInventory.clear();
		for (Ladders ladders : Ladders.values()) {
			int fighting = DuelManager.getInstance().getUnrankedFightFromLadder(ladders, false);
			int waiting = QueueManager.getInstance().getQueuedFromLadder(ladders, false);
			ItemStack item = ItemBuilder.getInstance().createNewItemStack(ladders.getIcon(), ladders.getColor() + ladders.getName(), (ladders.isEnable() ? Arrays.asList(new String[] {" ", ChatColor.DARK_AQUA + "Fighting: " + ChatColor.YELLOW + fighting, ChatColor.DARK_AQUA + "Waiting: " + ChatColor.YELLOW + waiting}) : Arrays.asList(new String[] {ChatColor.RED + "No arena created"})), Math.min(fighting, 64));
			this.unrankedInventory.addItem(item);
		}
	}
	
	private void setLaddersInventory() {
		this.laddersInventory.clear();
		for (Ladders ladders : Ladders.values()) {
			ItemStack item = ItemBuilder.getInstance().createNewItemStack(ladders.getIcon(), ladders.getColor() + ladders.getName());
			this.laddersInventory.addItem(item);
		}
	}
	
	public Inventory getArenasInventory() {
		return this.arenasInventory;
	}
	
	public Inventory getUnrankedInventory() {
		return this.unrankedInventory;
	}
	
	public Inventory getLaddersInventory() {
		return this.laddersInventory;
	}
	
	public void setSelectingDuel(UUID requester, UUID requested) { 
		this.selectingDuel.put(requester, new Request(requested, null, null)); 
	}
	  
	public Request getSelectingDuelPlayerUUID(UUID requester) { 
		return this.selectingDuel.get(requester); 
	}
	  
	public void removeSelectingDuel(UUID requester) { 
		this.selectingDuel.remove(requester); 
	}
}
