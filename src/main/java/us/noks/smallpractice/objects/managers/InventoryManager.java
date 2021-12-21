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

import us.noks.smallpractice.Main;
import us.noks.smallpractice.arena.Arena;
import us.noks.smallpractice.arena.Arena.Arenas;
import us.noks.smallpractice.enums.Ladders;
import us.noks.smallpractice.objects.Request;
import us.noks.smallpractice.utils.ItemBuilder;

public class InventoryManager {
	private Inventory arenasInventory;
	private Inventory unrankedInventory;
	private Inventory rankedInventory;
	private Inventory[] laddersInventory;
	private Inventory editingInventory;
	private Inventory settingsInventory;
	private Inventory selectionInventory;
	private Map<UUID, Request> selectingDuel;
	
	public InventoryManager() {
		this.selectingDuel = new WeakHashMap<UUID, Request>();
		this.arenasInventory = Bukkit.createInventory(null, this.calculateSize(Arena.getInstance().getArenaList().size()), "Arena Selection");
		this.unrankedInventory = Bukkit.createInventory(null, this.calculateSize(Ladders.values().length), "Unranked Selection");
		this.rankedInventory = Bukkit.createInventory(null, this.calculateSize(Ladders.values().length), "Ranked Selection");
		this.laddersInventory[0] = this.laddersInventory[1] = Bukkit.createInventory(null, this.calculateSize(Ladders.values().length), "Ladder Selection");
		this.editingInventory = Bukkit.createInventory(null, 9, "Editing Selection");
		this.settingsInventory = Bukkit.createInventory(null, 27, "Settings Configuration");
		this.selectionInventory = Bukkit.createInventory(null, 27, "Selector");
		this.setArenasInventory();
		this.setUnrankedInventory();
		this.setRankedInventory();
		this.setLaddersInventory();
		this.setEditingInventory();
		this.setSettingsInventory();
		this.setSelectionInventory();
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
		    this.setUnrankedInventory();
		    return;
		}
		this.setRankedInventory();
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
		this.laddersInventory[0].clear();
		this.laddersInventory[1].clear();
		for (Ladders ladders : Ladders.values()) {
			ItemStack item = ItemBuilder.createNewItemStack(ladders.getIcon(), ladders.getColor() + ladders.getName());
			this.laddersInventory[0].addItem(item);
			if (!ladders.isMultiplayer()) continue;
			this.laddersInventory[1].addItem(item);
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
	
	public Inventory getArenasInventory() {
		return this.arenasInventory;
	}
	
	public Inventory getUnrankedInventory() {
		return this.unrankedInventory;
	}
	
	public Inventory getRankedInventory() {
		return this.rankedInventory;
	}
	
	public Inventory[] getLaddersInventory() {
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
	
	private int calculateSize(int size) {
		int sizeNeeded = size / 9;
		if ((size % 9) != 0) {
			sizeNeeded += 1;
		}
		return (sizeNeeded * 9);
	}
}
