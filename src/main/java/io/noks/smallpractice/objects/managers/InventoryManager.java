package io.noks.smallpractice.objects.managers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
import io.noks.smallpractice.objects.PlayerSettings;
import io.noks.smallpractice.objects.Request;
import io.noks.smallpractice.utils.ItemBuilder;

public class InventoryManager {
	private Inventory[] arenasInventory;
	private Inventory unrankedInventory;
	private Inventory rankedInventory;
	private Inventory laddersInventory;
	private Inventory editingInventory;
	private Inventory selectionInventory;
	private Inventory partyGameInventory;
	private Map<UUID, Request> selectingDuel;
	private Map<UUID, Inventory> editKitSelection;
	private Map<UUID, PlayerInventory> editKitEditor;
	private Map<UUID, Inventory> offlineInventories;
	private Inventory leaderBoardInventory;
	
	public InventoryManager() {
		this.selectingDuel = new WeakHashMap<UUID, Request>();
		this.arenasInventory = new Inventory[] {Bukkit.createInventory(null, this.calculateSize(Arena.getInstance().getArenaList().size()), "Arena Selection"), Bukkit.createInventory(null, this.calculateSize(Arena.getInstance().getArenaList().size()), "Arena Selection"), Bukkit.createInventory(null, this.calculateSize(Arena.getInstance().getArenaList().size()), "Arena Selection")};
		this.unrankedInventory = Bukkit.createInventory(null, this.calculateSize(Ladders.values().length), "Unranked Selection");
		this.rankedInventory = Bukkit.createInventory(null, this.calculateSize(Ladders.values().length), "Ranked Selection");
		this.laddersInventory = Bukkit.createInventory(null, this.calculateSize(Ladders.values().length), "Ladder Selection");
		this.editingInventory = Bukkit.createInventory(null, 9, "Editing Selection");
		this.selectionInventory = Bukkit.createInventory(null, 27, "Selector");
		this.partyGameInventory = Bukkit.createInventory(null, 27, "Select Gamemode");
		this.offlineInventories = new WeakHashMap<UUID, Inventory>();
		this.leaderBoardInventory = Bukkit.createInventory(null, 36, "Leaderboard");
		this.setArenasInventory();
		this.setUnrankedInventory();
		this.setRankedInventory();
		this.setLaddersInventory();
		this.setEditingInventory();
		this.setSelectionInventory();
		this.setPartyGameInventory();
		setLeaderboardInventory();
	}
	
	private void setArenasInventory() {
		this.arenasInventory[0].clear();
		this.arenasInventory[1].clear();
		this.arenasInventory[2].clear();
		for (Arenas arena : Arena.getInstance().getArenaList().values()) {
			final ItemStack item = ItemBuilder.createNewItemStack(arena.getIcon(), ChatColor.GOLD + arena.getName());
			this.arenasInventory[2].addItem(item);
			if (!arena.isSumo()) {
				this.arenasInventory[0].addItem(item);
				continue;
			}
			this.arenasInventory[1].addItem(item);
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
			ItemStack item = ItemBuilder.createNewItemStack(ladders.getIcon(), ladders.getColor() + ladders.getName(), (ladders.isEnable() ? Arrays.asList(new String[] {" ", ChatColor.DARK_AQUA + "Fighting: " + ChatColor.YELLOW + fighting, ChatColor.DARK_AQUA + "Waiting: " + ChatColor.YELLOW + waiting}) : Arrays.asList(new String[] {ChatColor.RED + "Disabled!"})), Math.min(fighting, 64));
			this.unrankedInventory.addItem(item);
		}
	}
	
	private void setRankedInventory() {
		this.rankedInventory.clear();
		for (Ladders ladders : Ladders.values()) {
			int fighting = Main.getInstance().getDuelManager().getFightFromLadder(ladders, true);
			int waiting = Main.getInstance().getQueueManager().getQueuedFromLadder(ladders, true);
			ItemStack item = ItemBuilder.createNewItemStack(ladders.getIcon(), ladders.getColor() + ladders.getName(), (ladders.isEnable() ? Arrays.asList(new String[] {" ", ChatColor.DARK_AQUA + "Fighting: " + ChatColor.YELLOW + fighting, ChatColor.DARK_AQUA + "Waiting: " + ChatColor.YELLOW + waiting}) : Arrays.asList(new String[] {ChatColor.RED + "Disabled!"})), Math.min(fighting, 64));
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
	
	public Inventory getSettingsInventory(PlayerSettings ps) {
		final Inventory settingsInventory = Bukkit.createInventory(null, 27, "Settings Configuration");
		if (settingsInventory.firstEmpty() == -1) {
			settingsInventory.clear();
		}
		this.fillWithGlass(settingsInventory);
		settingsInventory.setItem(10, ItemBuilder.createNewItemStack(new ItemStack(Material.FEATHER, 1), ChatColor.GREEN + "Ping Difference", Arrays.asList(new String[] {ChatColor.DARK_AQUA + "Actual value: " + ChatColor.GREEN + ps.getQueuePingDiff() + "ms", ChatColor.GRAY + "(Click to change)"})));
		return settingsInventory;
	}
	
	private void setSelectionInventory() {
		this.selectionInventory.clear();
		this.fillWithGlass(this.selectionInventory);
		this.selectionInventory.setItem(11, ItemBuilder.createNewItemStackByMaterial(Material.DIAMOND_CHESTPLATE, ChatColor.GREEN + "Kit Creator"));
		this.selectionInventory.setItem(15, ItemBuilder.createNewItemStackByMaterial(Material.ANVIL, ChatColor.RED + "Configurate Settings"));
	}
	
	private void setPartyGameInventory() {
		this.partyGameInventory.clear();
		this.fillWithGlass(this.partyGameInventory);
		this.partyGameInventory.setItem(11, ItemBuilder.createNewItemStackByMaterial(Material.SHEARS, ChatColor.GREEN + "Split Team"));
		this.partyGameInventory.setItem(13, ItemBuilder.createNewItemStackByMaterial(Material.DIAMOND_SWORD, ChatColor.GREEN + "FFA"));
		this.partyGameInventory.setItem(15, ItemBuilder.createNewItemStack(new ItemStack(Material.WOOL, 1, (short) 14), ChatColor.RED + "RedRover"));
	}
	
	public void setLeaderboardInventory() {
		this.leaderBoardInventory.clear();
		this.fillWithGlass(this.leaderBoardInventory);
		int i = 18;
		for (Ladders ladders : Ladders.values()) {
			final Map<UUID, Integer> map = Main.getInstance().getDatabaseUtil().getTopEloLadder(ladders);
			final List<String> lore = new ArrayList<String>();
			if (map == null) {
				lore.add(ChatColor.RED + "Database not connected!");
			} else {
				int rank = 1;
				for (Map.Entry<UUID, Integer> entry : map.entrySet()) {
					lore.add(ChatColor.GRAY + "#" + rank + " " + ChatColor.DARK_AQUA + Main.getInstance().getServer().getOfflinePlayer(entry.getKey()).getName() + ": " + ChatColor.YELLOW + entry.getValue());
					rank++;
				}
			}
			this.leaderBoardInventory.setItem(i, ItemBuilder.createNewItemStack(ladders.getIcon(), ladders.getColor() + ladders.getName(), lore));
			i++;
		}
		this.leaderBoardInventory.setItem(4, ItemBuilder.createNewItemStack(new ItemStack(Material.DIAMOND, 1), ChatColor.DARK_AQUA + "Global Top", Arrays.asList(ChatColor.GREEN + "Coming soon :)")));
	}
	
	public Inventory getLeaderboardInventory() {
		return this.leaderBoardInventory;
	}
	
	public Inventory getAllArenasInInventory() {
		return this.arenasInventory[2];
	}
	public Inventory getArenasInventory(boolean sumo) {
		return sumo ? this.arenasInventory[1] : this.arenasInventory[0];
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
	
	private void fillWithGlass(Inventory inv) {
		for (int i = 0; i < inv.getSize(); i++) {
			inv.setItem(i, ItemBuilder.createNewItemStack(new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 15), " "));
		}
	}
	private int calculateSize(int size) {
		int sizeNeeded = size / 9;
		if ((size % 9) != 0) {
			sizeNeeded += 1;
		}
		return (sizeNeeded * 9);
	}
}
