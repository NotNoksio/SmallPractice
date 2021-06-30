package us.noks.smallpractice.objects.managers;

import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
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
	private Inventory rankedInventory;
	private Inventory laddersInventory;
	private Inventory editingInventory;
	private Inventory settingsInventory;
	private Inventory selectionInventory;
	private Map<UUID, Request> selectingDuel;
	
	private static InventoryManager instance = new InventoryManager();
	public static InventoryManager getInstance() {
		return instance;
	}
	
	public InventoryManager() {
		this.selectingDuel = new WeakHashMap<UUID, Request>();
		this.arenasInventory = Bukkit.createInventory(null, 18, "Arena Selection");
		this.unrankedInventory = Bukkit.createInventory(null, this.calculateSize(Ladders.values().length), "Unranked Selection");
		this.rankedInventory = Bukkit.createInventory(null, this.calculateSize(Ladders.values().length), "Ranked Selection");
		this.laddersInventory = Bukkit.createInventory(null, this.calculateSize(Ladders.values().length), "Ladder Selection");
		this.editingInventory = Bukkit.createInventory(null, 9, "Editing Selection");
		this.settingsInventory = Bukkit.createInventory(null, 27, "Settings Configuration");
		this.selectionInventory = Bukkit.createInventory(null, 27, "Selector");
		this.setArenasInventory();
		this.setUnrankedInventory();
		this.setLaddersInventory();
		this.setEditingInventory();
		this.setSettingsInventory();
		this.setSelectionInventory();
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
			int fighting = DuelManager.getInstance().getFightFromLadder(ladders, false);
			int waiting = QueueManager.getInstance().getQueuedFromLadder(ladders, false);
			ItemStack item = ItemBuilder.getInstance().createNewItemStack(ladders.getIcon(), ladders.getColor() + ladders.getName(), (ladders.isEnable() ? Arrays.asList(new String[] {" ", ChatColor.DARK_AQUA + "Fighting: " + ChatColor.YELLOW + fighting, ChatColor.DARK_AQUA + "Waiting: " + ChatColor.YELLOW + waiting}) : Arrays.asList(new String[] {ChatColor.RED + "No arena created"})), Math.min(fighting, 64));
			this.unrankedInventory.addItem(item);
		}
	}
	
	public void updateRankedInventory() {
		this.setRankedInventory();
	}
	private void setRankedInventory() {
		this.rankedInventory.clear();
		for (Ladders ladders : Ladders.values()) {
			int fighting = DuelManager.getInstance().getFightFromLadder(ladders, true);
			int waiting = QueueManager.getInstance().getQueuedFromLadder(ladders, true);
			ItemStack item = ItemBuilder.getInstance().createNewItemStack(ladders.getIcon(), ladders.getColor() + ladders.getName(), (ladders.isEnable() ? Arrays.asList(new String[] {" ", ChatColor.DARK_AQUA + "Fighting: " + ChatColor.YELLOW + fighting, ChatColor.DARK_AQUA + "Waiting: " + ChatColor.YELLOW + waiting}) : Arrays.asList(new String[] {ChatColor.RED + "No arena created"})), Math.min(fighting, 64));
			this.rankedInventory.addItem(item);
		}
	}
	
	private void setLaddersInventory() {
		this.laddersInventory.clear();
		for (Ladders ladders : Ladders.values()) {
			ItemStack item = ItemBuilder.getInstance().createNewItemStack(ladders.getIcon(), ladders.getColor() + ladders.getName());
			this.laddersInventory.addItem(item);
		}
	}
	
	private void setEditingInventory() {
		this.editingInventory.clear();
		for (Ladders ladders : Ladders.values()) {
			if (!ladders.isEditable()) continue;
			ItemStack item = ItemBuilder.getInstance().createNewItemStack(ladders.getIcon(), ladders.getColor() + ladders.getName(), Arrays.asList(new String[] {ChatColor.GREEN + "Click to configurate"}));
			this.editingInventory.addItem(item);
		}
	}
	
	private void setSettingsInventory() {
		this.settingsInventory.clear();
		for (int i = 0; i < 0; i++) {
			this.settingsInventory.setItem(i, ItemBuilder.getInstance().createNewItemStack(new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 15), " "));
		}
	}
	
	private void setSelectionInventory() {
		this.selectionInventory.clear();
		for (int i = 0; i < 0; i++) {
			this.selectionInventory.setItem(i, ItemBuilder.getInstance().createNewItemStack(new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 15), " "));
		}
		this.selectionInventory.setItem(11, ItemBuilder.getInstance().createNewItemStackByMaterial(Material.DIAMOND_CHESTPLATE, ChatColor.GREEN + "Kit Creator"));
		this.selectionInventory.setItem(15, ItemBuilder.getInstance().createNewItemStackByMaterial(Material.ANVIL, ChatColor.RED + "Configurate Settings"));
	}
	
	public Inventory getArenasInventory() {
		return this.arenasInventory;
	}
	
	public Inventory getUnrankedInventory() {
		return this.unrankedInventory;
	}
	
	public Inventory getRankedInventory() {
		return this.rankedInventory;
	}
	
	public Inventory getLaddersInventory() {
		return this.laddersInventory;
	}
	
	public Inventory getEditingInventory() {
		return this.editingInventory;
	}
	
	public Inventory getSettingsInventory() {
		return this.settingsInventory;
	}
	
	public Inventory getSelectionInventory() {
		return this.selectionInventory;
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
	
	private int calculateSize(int amountOfLadders) {
		if (amountOfLadders > 9) {
			return 18;
		}
		return 9;
	}
}
