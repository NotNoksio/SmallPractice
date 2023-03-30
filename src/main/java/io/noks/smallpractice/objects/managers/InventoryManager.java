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
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import io.noks.smallpractice.Main;
import io.noks.smallpractice.arena.Arena;
import io.noks.smallpractice.enums.Ladders;
import io.noks.smallpractice.objects.EditedLadderKit;
import io.noks.smallpractice.objects.PlayerSettings;
import io.noks.smallpractice.objects.Request;
import io.noks.smallpractice.utils.ItemBuilder;
import io.noks.smallpractice.utils.PartnerCache;

public class InventoryManager {
	private Inventory[] arenasInventory;
	private Inventory unrankedInventory;
	private Inventory rankedInventory;
	private Inventory laddersInventory;
	private Inventory editingInventory;
	private Inventory selectionInventory;
	private Inventory partyGameInventory;
	private Map<UUID, Request> selectingDuel;
	private Map<UUID, Inventory> offlineInventories;
	private Inventory[] leaderBoardInventory;
	
	public InventoryManager() {
		this.selectingDuel = new WeakHashMap<UUID, Request>();
		this.arenasInventory = new Inventory[] {Bukkit.createInventory(null, this.calculateSize(Main.getInstance().getArenaManager().getArenaList().size()), "Arena Selection"), Bukkit.createInventory(null, this.calculateSize(Main.getInstance().getArenaManager().getArenaList().size()), "Arena Selection"), Bukkit.createInventory(null, this.calculateSize(Main.getInstance().getArenaManager().getArenaList().size()), "Arena Selection")};
		this.unrankedInventory = Bukkit.createInventory(null, this.calculateSize(Ladders.values().length), "Unranked Selection");
		this.rankedInventory = Bukkit.createInventory(null, this.calculateSize(Ladders.values().length), "Ranked Selection");
		this.laddersInventory = Bukkit.createInventory(null, this.calculateSize(Ladders.values().length), "Ladder Selection");
		this.editingInventory = Bukkit.createInventory(null, 9, "Editing Selection");
		this.selectionInventory = Bukkit.createInventory(null, 27, "Selector");
		this.partyGameInventory = Bukkit.createInventory(null, 27, "Select Gamemode");
		this.offlineInventories = new WeakHashMap<UUID, Inventory>();
		this.leaderBoardInventory = new Inventory[] {Bukkit.createInventory(null, 36, "Leaderboard"), Bukkit.createInventory(null, 36, "2v2 Leaderboard")};
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
		for (Arena arena : Main.getInstance().getArenaManager().getArenaList()) {
			final ItemStack item = ItemBuilder.createNewItemStack(arena.getIcon(), ChatColor.GOLD + arena.getName());
			this.arenasInventory[2].addItem(item);
			if (!arena.isSumo()) {
				this.arenasInventory[0].addItem(item);
				continue;
			}
			this.arenasInventory[1].addItem(item);
		}
		this.arenasInventory[0].setItem(arenasInventory[0].getSize() - 1, ItemBuilder.createNewItemStackByMaterial(Material.RECORD_11, ChatColor.RED + "Random"));
		this.arenasInventory[1].setItem(arenasInventory[1].getSize() - 1, ItemBuilder.createNewItemStackByMaterial(Material.RECORD_11, ChatColor.RED + "Random"));
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
			final int fighting = Main.getInstance().getDuelManager().getFightFromLadder(ladders, false);
			final int waiting = Main.getInstance().getQueueManager().getQueuedFromLadder(ladders, false);
			final ItemStack item = ItemBuilder.createNewItemStack(ladders.getIcon(), ladders.getColor() + ladders.getName(), (ladders.isEnable() ? Arrays.asList(new String[] {" ", ChatColor.DARK_AQUA + "Fighting: " + ChatColor.YELLOW + fighting, ChatColor.DARK_AQUA + "Waiting: " + ChatColor.YELLOW + waiting}) : Arrays.asList(new String[] {ChatColor.RED + "Disabled!"})), Math.min(fighting, 64));
			this.unrankedInventory.addItem(item);
		}
	}
	
	private void setRankedInventory() {
		this.rankedInventory.clear();
		for (Ladders ladders : Ladders.values()) {
			final int fighting = Main.getInstance().getDuelManager().getFightFromLadder(ladders, true);
			final int waiting = Main.getInstance().getQueueManager().getQueuedFromLadder(ladders, true);
			final ItemStack item = ItemBuilder.createNewItemStack(ladders.getIcon(), ladders.getColor() + ladders.getName(), (ladders.isEnable() ? Arrays.asList(new String[] {" ", ChatColor.DARK_AQUA + "Fighting: " + ChatColor.YELLOW + fighting, ChatColor.DARK_AQUA + "Waiting: " + ChatColor.YELLOW + waiting}) : Arrays.asList(new String[] {ChatColor.RED + "Disabled!"})), Math.min(fighting, 64));
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
			final ItemStack item = ItemBuilder.createNewItemStack(ladders.getIcon(), ladders.getColor() + ladders.getName(), Arrays.asList(new String[] {ChatColor.GREEN + "Click to configurate"}));
			this.editingInventory.addItem(item);
		}
	}
	
	public Inventory getSettingsInventory(PlayerManager pm) {
		final Inventory settingsInventory = Bukkit.createInventory(null, InventoryType.DISPENSER, "Settings Configuration");
		if (settingsInventory.firstEmpty() == -1) {
			settingsInventory.clear();
		}
		final PlayerSettings ps = pm.getSettings();
		this.fillWithGlass(settingsInventory);
		settingsInventory.setItem(0, ItemBuilder.createNewItemStack(new ItemStack(Material.FEATHER, 1), ChatColor.GREEN + "Ping Difference", Arrays.asList(new String[] {ChatColor.DARK_AQUA + "Actual value: " + ChatColor.GREEN + ps.getQueuePingDiff() + "ms", ChatColor.GRAY + "(Click to change)"})));
		settingsInventory.setItem(1, ItemBuilder.createNewItemStack(new ItemStack(Material.PAPER, 1), ChatColor.GREEN + "Toggle Private Message", Arrays.asList(new String[] {ChatColor.DARK_AQUA + "Private Message: " + (ps.isPrivateMessageToggled() ? ChatColor.GREEN + "Allowed" : ChatColor.RED + "Disallowed"), ChatColor.GRAY + "(Click to change)"})));
		settingsInventory.setItem(2, ItemBuilder.createNewItemStack(new ItemStack(Material.ANVIL, 1), ChatColor.GREEN + "Toggle Party Invite", Arrays.asList(new String[] {ChatColor.DARK_AQUA + "Party Invite: " + (ps.isPartyInviteToggled() ? ChatColor.GREEN + "Allowed" : ChatColor.RED + "Disallowed"), ChatColor.GRAY + "(Click to change)"})));
		settingsInventory.setItem(3, ItemBuilder.createNewItemStack(new ItemStack(Material.DIAMOND_SWORD, 1), ChatColor.GREEN + "Toggle Duel Request", Arrays.asList(new String[] {ChatColor.DARK_AQUA + "Duel Request: " + (ps.isDuelRequestToggled() ? ChatColor.GREEN + "Allowed" : ChatColor.RED + "Disallowed"), ChatColor.GRAY + "(Click to change)"})));
		// TODO: Toggle scoreboard
		if (pm.getPlayer().hasPermission("setting.request.delay") && ps.isDuelRequestToggled()) {
			settingsInventory.setItem(8, ItemBuilder.createNewItemStack(new ItemStack(Material.WATCH, 1), ChatColor.GREEN + "Request Delay", Arrays.asList(new String[] {ChatColor.DARK_AQUA + "Actual value: " + ChatColor.GREEN + ps.getSecondsBeforeRerequest() + "seconds", ChatColor.GRAY + "(Click to change)"})));
		}
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
	
	// TODO: THIS IS UGLY ASF
	public void setLeaderboardInventory() {
		this.leaderBoardInventory[0].clear();
		this.leaderBoardInventory[1].clear();
		this.fillWithGlass(this.leaderBoardInventory[0]);
		this.fillWithGlass(this.leaderBoardInventory[1]);
		int i = 18;
		for (Ladders ladders : Ladders.values()) {
			final Map<UUID, Integer> map = Main.getInstance().getDatabaseUtil().getTopEloLadder(ladders);
			List<String> lore = new ArrayList<String>();
			if (map == null) {
				lore.add(ChatColor.RED + "Database not connected!");
			} else {
				int rank = 1;
				for (Map.Entry<UUID, Integer> entry : map.entrySet()) {
					final ChatColor color = (rank == 1 ? ChatColor.AQUA : (rank == 2 ? ChatColor.GOLD : (rank == 3 ? ChatColor.GREEN : ChatColor.GRAY)));
					lore.add(color + "#" + rank + " " + ChatColor.DARK_AQUA + Main.getInstance().getServer().getOfflinePlayer(entry.getKey()).getName() + ": " + ChatColor.YELLOW + entry.getValue());
					rank++;
				}
			}
			this.leaderBoardInventory[0].setItem(i, ItemBuilder.createNewItemStack(ladders.getIcon(), ladders.getColor() + ladders.getName(), lore));
			final Map<UUID, PartnerCache> map2 = Main.getInstance().getDatabaseUtil().getDuoTopEloLadder(ladders);
			lore = new ArrayList<String>();
			if (map2 == null) {
				lore.add(ChatColor.RED + "Database not connected!");
			} else {
				int rank = 1;
				for (Map.Entry<UUID, PartnerCache> entry : map2.entrySet()) {
					final PartnerCache cache = entry.getValue();
					final ChatColor color = (rank == 1 ? ChatColor.AQUA : (rank == 2 ? ChatColor.GOLD : (rank == 3 ? ChatColor.GREEN : ChatColor.GRAY)));
					lore.add(color + "#" + rank + " " + ChatColor.DARK_AQUA + Main.getInstance().getServer().getOfflinePlayer(entry.getKey()).getName() + " & " + Main.getInstance().getServer().getOfflinePlayer(cache.getPartner()).getName() + ": " + ChatColor.YELLOW + cache.getElo());
					rank++;
				}
			}
			this.leaderBoardInventory[1].setItem(i, ItemBuilder.createNewItemStack(ladders.getIcon(), ladders.getColor() + ladders.getName(), lore));
			i++;
		}
		final Map<UUID, Integer> globalMap = Main.getInstance().getDatabaseUtil().getGlobalTopElo();
		List<String> lore = new ArrayList<String>();
		if (globalMap == null) {
			lore.add(ChatColor.RED + "Database not connected!");
		} else {
			int rank = 1;
			for (Map.Entry<UUID, Integer> entry : globalMap.entrySet()) {
				final ChatColor color = (rank == 1 ? ChatColor.AQUA : (rank == 2 ? ChatColor.GOLD : (rank == 3 ? ChatColor.GREEN : ChatColor.GRAY)));
				lore.add(color + "#" + rank + " " + ChatColor.DARK_AQUA + Main.getInstance().getServer().getOfflinePlayer(entry.getKey()).getName() + ": " + ChatColor.YELLOW + entry.getValue());
				rank++;
			}
		}
		this.leaderBoardInventory[0].setItem(4, ItemBuilder.createNewItemStack(new ItemStack(Material.DIAMOND, 1), ChatColor.DARK_AQUA + "Global Top", lore));
		final Map<UUID, PartnerCache> map2 = Main.getInstance().getDatabaseUtil().getDuoGlobalTopElo();
		lore = new ArrayList<String>();
		if (map2 == null) {
			lore.add(ChatColor.RED + "Database not connected!");
		} else {
			int rank = 1;
			for (Map.Entry<UUID, PartnerCache> entry : map2.entrySet()) {
				final PartnerCache cache = entry.getValue();
				final ChatColor color = (rank == 1 ? ChatColor.AQUA : (rank == 2 ? ChatColor.GOLD : (rank == 3 ? ChatColor.GREEN : ChatColor.GRAY)));
				lore.add(color + "#" + rank + " " + ChatColor.DARK_AQUA + Main.getInstance().getServer().getOfflinePlayer(entry.getKey()).getName() + " & " + Main.getInstance().getServer().getOfflinePlayer(cache.getPartner()).getName() + ": " + ChatColor.YELLOW + cache.getElo());
				rank++;
			}
		}
		this.leaderBoardInventory[1].setItem(4, ItemBuilder.createNewItemStack(new ItemStack(Material.DIAMOND, 1), ChatColor.DARK_AQUA + "Global Top", lore));
		// Switch item
		this.leaderBoardInventory[0].setItem(8, ItemBuilder.createNewItemStack(new ItemStack(Material.CARPET, 1, (short) 5), ChatColor.GREEN + "2v2 Leaderboard"));
		this.leaderBoardInventory[1].setItem(0, ItemBuilder.createNewItemStack(new ItemStack(Material.CARPET, 1, (short) 14), ChatColor.GREEN + "1v1 Leaderboard"));
	}
	
	public Inventory getLeaderboardInventory(boolean team) {
		return !team ? this.leaderBoardInventory[0] : this.leaderBoardInventory[1];
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
	
	public Inventory getKitEditingLayout(PlayerManager pm, Ladders ladder) {
		final Inventory inventory = Bukkit.createInventory(null, 36, ladder.getName() + " Edit Layout");
		if (inventory.firstEmpty() == -1) {
			inventory.clear();
		}
		this.fillWithGlass(inventory);
		int i = 1;
		while (i <= 7) {
			final EditedLadderKit customKit = pm.getCustomLadderKitFromSlot(ladder, i);
			if (customKit == null) {
				inventory.setItem(i, ItemBuilder.createNewItemStack(new ItemStack(Material.CHEST), ChatColor.GREEN + "Create " + ladder.getName() + " #" + i));
			} else {
				final List<String> nameLore = new ArrayList<String>();
				nameLore.add(ChatColor.DARK_AQUA + "Actual Name: " + ChatColor.YELLOW + ChatColor.translateAlternateColorCodes('&', customKit.getName()));
				inventory.setItem(i, ItemBuilder.createNewItemStack(new ItemStack(Material.CHEST), ChatColor.GREEN + "Save " + ladder.getColor() + ladder.getName() + " #" + i));
				inventory.setItem(i + 9, ItemBuilder.createNewItemStack(new ItemStack(Material.BOOK), ChatColor.GREEN + "Load/Edit"));
				inventory.setItem(i + 18, ItemBuilder.createNewItemStack(new ItemStack(Material.NAME_TAG), ChatColor.YELLOW + "Rename", nameLore));
				inventory.setItem(i + 27, ItemBuilder.createNewItemStack(new ItemStack(Material.WOOL, 1, (short) 14), ChatColor.RED + "Delete"));
			}
			if (i == 7) {
				break;
			}
			i++;
		}
		return inventory;
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
