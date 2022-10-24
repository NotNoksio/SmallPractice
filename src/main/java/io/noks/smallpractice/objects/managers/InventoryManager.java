package io.noks.smallpractice.objects.managers;

import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import io.noks.smallpractice.Main;
import io.noks.smallpractice.arena.Arena;
import io.noks.smallpractice.arena.Arena.Arenas;
import io.noks.smallpractice.enums.Ladders;
import io.noks.smallpractice.objects.Request;
import io.noks.smallpractice.utils.ItemBuilder;

public class InventoryManager {
	private Inventory arenasInventory;
	private Inventory unrankedInventory;
	private Inventory rankedInventory;
	private Inventory laddersInventory;
	private Inventory editingInventory;
	private Inventory settingsInventory;
	private Inventory selectionInventory;
	private Inventory partyGameInventory;
	private Map<UUID, Request> selectingDuel;
	private Map<UUID, Inventory> editKitSelection;
	private Map<UUID, PlayerInventory> editKitEditor;
	private Map<UUID, Inventory> offlineInventories;
	
	public InventoryManager() {
		this.selectingDuel = new WeakHashMap<UUID, Request>();
		this.arenasInventory = Bukkit.createInventory(null, this.calculateSize(Arena.getInstance().getArenaList().size()), "Arena Selection");
		this.unrankedInventory = Bukkit.createInventory(null, this.calculateSize(Ladders.values().length), "Unranked Selection");
		this.rankedInventory = Bukkit.createInventory(null, this.calculateSize(Ladders.values().length), "Ranked Selection");
		this.laddersInventory = Bukkit.createInventory(null, this.calculateSize(Ladders.values().length), "Ladder Selection");
		this.editingInventory = Bukkit.createInventory(null, 9, "Editing Selection");
		this.settingsInventory = Bukkit.createInventory(null, 27, "Settings Configuration");
		this.selectionInventory = Bukkit.createInventory(null, 27, "Selector");
		this.partyGameInventory = Bukkit.createInventory(null, 27, "Select Gamemode");
		this.offlineInventories = new WeakHashMap<UUID, Inventory>();
		this.setArenasInventory();
		this.setUnrankedInventory();
		this.setRankedInventory();
		this.setLaddersInventory();
		this.setEditingInventory();
		this.setSettingsInventory();
		this.setSelectionInventory();
		this.setPartyGameInventory();
	}
	
	private void setArenasInventory() {
		this.arenasInventory.clear();
		for (Arenas arena : Arena.getInstance().getArenaList().values()) {
			ItemStack item = ItemBuilder.createNewItemStack(arena.getIcon(), ChatColor.GOLD + arena.getName());
			this.arenasInventory.addItem(item);
		}
	}
	
	public void updateQueueInventory(boolean ranked) {
		if (ranked) {
		    this.setRankedInventory();
		    return;
		}
		this.setUnrankedInventory();
	}
	
	private void setUnrankedInventory() {
		this.unrankedInventory.clear();
		for (Ladders ladders : Ladders.values()) {
			int fighting = Main.getInstance().getDuelManager().getFightFromLadder(ladders, false);
			int waiting = Main.getInstance().getQueueManager().getQueuedFromLadder(ladders, false);
			ItemStack item = ItemBuilder.createNewItemStack(ladders.getIcon(), ladders.getColor() + ladders.getName(), (ladders.isEnable() ? Arrays.asList(new String[] {" ", ChatColor.DARK_AQUA + "Fighting: " + ChatColor.YELLOW + fighting, ChatColor.DARK_AQUA + "Waiting: " + ChatColor.YELLOW + waiting}) : Arrays.asList(new String[] {ChatColor.RED + "No arena created"})), Math.min(fighting, 64));
			this.unrankedInventory.addItem(item);
		}
	}
	
	private void setRankedInventory() {
		this.rankedInventory.clear();
		for (Ladders ladders : Ladders.values()) {
			int fighting = Main.getInstance().getDuelManager().getFightFromLadder(ladders, true);
			int waiting = Main.getInstance().getQueueManager().getQueuedFromLadder(ladders, true);
			ItemStack item = ItemBuilder.createNewItemStack(ladders.getIcon(), ladders.getColor() + ladders.getName(), (ladders.isEnable() ? Arrays.asList(new String[] {" ", ChatColor.DARK_AQUA + "Fighting: " + ChatColor.YELLOW + fighting, ChatColor.DARK_AQUA + "Waiting: " + ChatColor.YELLOW + waiting}) : Arrays.asList(new String[] {ChatColor.RED + "No arena created"})), Math.min(fighting, 64));
			this.rankedInventory.addItem(item);
		}
	}
	
	private void setLaddersInventory() {
		for (Ladders ladders : Ladders.values()) {
			final ItemStack ladder = ItemBuilder.createNewItemStack(ladders.getIcon(), ladders.getColor() + ladders.getName(), Arrays.asList(" "));
			this.laddersInventory.addItem(ladder);
		}
	}
	
	private void setEditingInventory() {
		this.editingInventory.clear();
		for (Ladders ladders : Ladders.values()) {
			if (!ladders.isEditable()) continue;
			ItemStack item = ItemBuilder.createNewItemStack(ladders.getIcon(), ladders.getColor() + ladders.getName(), Arrays.asList(new String[] {ChatColor.GREEN + "Click to configurate"}));
			this.editingInventory.addItem(item);
		}
	}
	
	private void setSettingsInventory() {
		this.settingsInventory.clear();
		for (int i = 0; i < this.settingsInventory.getSize(); i++) {
			this.settingsInventory.setItem(i, ItemBuilder.createNewItemStack(new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 15), " "));
		}
	}
	
	private void setSelectionInventory() {
		this.selectionInventory.clear();
		for (int i = 0; i < this.selectionInventory.getSize(); i++) {
			this.selectionInventory.setItem(i, ItemBuilder.createNewItemStack(new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 15), " "));
		}
		this.selectionInventory.setItem(11, ItemBuilder.createNewItemStackByMaterial(Material.DIAMOND_CHESTPLATE, ChatColor.GREEN + "Kit Creator"));
		this.selectionInventory.setItem(15, ItemBuilder.createNewItemStackByMaterial(Material.ANVIL, ChatColor.RED + "Configurate Settings"));
	}
	
	private void setPartyGameInventory() {
		this.partyGameInventory.clear();
		for (int i = 0; i < this.selectionInventory.getSize(); i++) {
			this.partyGameInventory.setItem(i, ItemBuilder.createNewItemStack(new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 15), " "));
		}
		this.partyGameInventory.setItem(11, ItemBuilder.createNewItemStackByMaterial(Material.SHEARS, ChatColor.GREEN + "Split Team"));
		this.partyGameInventory.setItem(13, ItemBuilder.createNewItemStackByMaterial(Material.DIAMOND_SWORD, ChatColor.GREEN + "FFA"));
		this.partyGameInventory.setItem(15, ItemBuilder.createNewItemStack(new ItemStack(Material.WOOL, 1, (short) 14), ChatColor.RED + "RedRover"));
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
	
	public Inventory getPartyGameInventory() {
		return this.partyGameInventory;
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
	
	public Map<UUID, Inventory> getOfflineInventories() {
		return this.offlineInventories;
	}
	
	private int calculateSize(int size) {
		int sizeNeeded = size / 9;
		if ((size % 9) != 0) {
			sizeNeeded += 1;
		}
		return (sizeNeeded * 9);
	}
}
