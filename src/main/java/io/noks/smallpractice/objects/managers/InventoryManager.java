package io.noks.smallpractice.objects.managers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

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
import io.noks.smallpractice.party.PartyEvents;
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
	
	private Main main;
	public InventoryManager(Main main) {
		this.main = main;
		this.selectingDuel = new WeakHashMap<UUID, Request>();
		this.arenasInventory = new Inventory[] {main.getServer().createInventory(null, this.calculateSize(main.getArenaManager().getArenaList(false, false).size() + 1), "Arena Selection"), // Non sumo and spleef arena
												main.getServer().createInventory(null, this.calculateSize(main.getArenaManager().getArenaList(true, false).size() + 1), "Arena Selection"), // Sumo arena
												main.getServer().createInventory(null, this.calculateSize(main.getArenaManager().getArenaList(false, true).size() + 1), "Arena Selection"), // Spleef arena
												main.getServer().createInventory(null, this.calculateSize(main.getArenaManager().getFullArenaList().size()), "Arena Selection")}; // All arena
		this.unrankedInventory = main.getServer().createInventory(null, this.calculateSize(Ladders.values().length), "Unranked Selection");
		this.rankedInventory = main.getServer().createInventory(null, this.calculateSize(Ladders.values().length), "Ranked Selection");
		this.laddersInventory = main.getServer().createInventory(null, this.calculateSize(Ladders.values().length), "Ladder Selection");
		this.editingInventory = main.getServer().createInventory(null, 9, "Editing Selection");
		this.selectionInventory = main.getServer().createInventory(null, 27, "Selector");
		this.partyGameInventory = main.getServer().createInventory(null, 27, "Select Gamemode");
		this.offlineInventories = new WeakHashMap<UUID, Inventory>();
		this.leaderBoardInventory = new Inventory[] {main.getServer().createInventory(null, 36, "Leaderboard"),
													 main.getServer().createInventory(null, 36, "2v2 Leaderboard")};
		this.setArenasInventory();
		this.setUnrankedInventory();
		this.setRankedInventory();
		this.setLaddersInventory();
		this.setEditingInventory();
		this.setSelectionInventory();
		this.setPartyGameInventory();
		setLeaderboardInventory();
	}
	public void clearCache() {
		if (!this.selectingDuel.isEmpty()) {
			this.selectingDuel.clear();
		}
		if (!this.offlineInventories.isEmpty()) {
			this.offlineInventories.clear();
		}
	}
	
	private void setArenasInventory() {
		this.arenasInventory[0].clear();
		this.arenasInventory[1].clear();
		this.arenasInventory[2].clear();
		this.arenasInventory[3].clear();
		for (Arena arena : this.main.getArenaManager().getFullArenaList()) {
			ItemStack item = ItemBuilder.createNewItemStack(arena.getIcon(), ChatColor.GOLD + arena.getName(), Arrays.asList(ChatColor.GREEN + "Arena Type: " + ChatColor.YELLOW +  (arena.isSumo() ? "Sumo" : (arena.isSpleef() ? "Spleef" : "Potion & BuildUHC"))));
			this.arenasInventory[3].addItem(item); // All arena
			item = ItemBuilder.createNewItemStack(arena.getIcon(), ChatColor.GOLD + arena.getName());
			if (!arena.isSumo() && !arena.isSpleef()) {
				this.arenasInventory[0].addItem(item); // Non sumo and spleef
				continue;
			}
			if (arena.isSumo()) {
				this.arenasInventory[1].addItem(item); // Sumo
				continue;
			}
			this.arenasInventory[2].addItem(item); // Spleef
		}
		for (int i = 0; i <= 2; i++) {
			this.arenasInventory[i].setItem(arenasInventory[i].getSize() - 1, ItemBuilder.createNewItemStackByMaterial(Material.RECORD_11, ChatColor.RED + "Random"));
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
			final int fighting = this.main.getDuelManager().getFightFromLadder(ladders, false);
			final int waiting = this.main.getQueueManager().getQueuedFromLadder(ladders, false);
			final ItemStack item = ItemBuilder.createNewItemStack(ladders.getIcon(), ladders.getColor() + ladders.getName(), (ladders.isEnable() ? Arrays.asList(new String[] {" ", ChatColor.DARK_AQUA + "Fighting: " + ChatColor.YELLOW + fighting, ChatColor.DARK_AQUA + "Waiting: " + ChatColor.YELLOW + waiting}) : Arrays.asList(new String[] {ChatColor.RED + "Disabled!"})), Math.min(fighting, 64));
			this.unrankedInventory.addItem(item);
		}
	}
	
	private void setRankedInventory() {
		this.rankedInventory.clear();
		for (Ladders ladders : Ladders.values()) {
			final int fighting = this.main.getDuelManager().getFightFromLadder(ladders, true);
			final int waiting = this.main.getQueueManager().getQueuedFromLadder(ladders, true);
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
			if (!ladders.isEnable()) continue;
			final ItemStack item = ItemBuilder.createNewItemStack(ladders.getIcon(), ladders.getColor() + ladders.getName(), Arrays.asList(new String[] {ChatColor.GREEN + "Click to configurate"}));
			this.editingInventory.addItem(item);
		}
	}
	
	public Inventory getSettingsInventory(PlayerManager pm) {
		final Inventory settingsInventory = this.main.getServer().createInventory(null, InventoryType.DISPENSER, "Settings Configuration");
		if (settingsInventory.firstEmpty() == -1) {
			settingsInventory.clear();
		}
		final PlayerSettings ps = pm.getSettings();
		this.fillWithGlass(settingsInventory);
		settingsInventory.setItem(0, ItemBuilder.createNewItemStack(new ItemStack(Material.FEATHER, 1), ChatColor.GREEN + "Ping Difference", Arrays.asList(new String[] {ChatColor.DARK_AQUA + "Actual value: " + (ps.getQueuePingDiff() == 300 ? ChatColor.RED.toString() + "Unlimited" : ChatColor.GREEN.toString() + ps.getQueuePingDiff() + "ms"), ChatColor.GRAY + "(Click to change)"})));
		settingsInventory.setItem(1, ItemBuilder.createNewItemStack(new ItemStack(Material.PAPER, 1), ChatColor.GREEN + "Toggle Private Message", Arrays.asList(new String[] {ChatColor.DARK_AQUA + "Private Message: " + (ps.isPrivateMessageToggled() ? ChatColor.GREEN + "Allowed" : ChatColor.RED + "Disallowed"), ChatColor.GRAY + "(Click to change)"})));
		settingsInventory.setItem(2, ItemBuilder.createNewItemStack(new ItemStack(Material.ANVIL, 1), ChatColor.GREEN + "Toggle Party Invite", Arrays.asList(new String[] {ChatColor.DARK_AQUA + "Party Invite: " + (ps.isPartyInviteToggled() ? ChatColor.GREEN + "Allowed" : ChatColor.RED + "Disallowed"), ChatColor.GRAY + "(Click to change)"})));
		settingsInventory.setItem(3, ItemBuilder.createNewItemStack(new ItemStack(Material.DIAMOND_SWORD, 1), ChatColor.GREEN + "Toggle Duel Request", Arrays.asList(new String[] {ChatColor.DARK_AQUA + "Duel Request: " + (ps.isDuelRequestToggled() ? ChatColor.GREEN + "Allowed" : ChatColor.RED + "Disallowed"), ChatColor.GRAY + "(Click to change)"})));
		settingsInventory.setItem(4, ItemBuilder.createNewItemStack(new ItemStack(Material.SIGN, 1), ChatColor.GREEN + "Toggle Scoreboard", Arrays.asList(new String[] {ChatColor.DARK_AQUA + "Scoreboard: " + (ps.isScoreboardToggled() ? ChatColor.GREEN + "Enabled" : ChatColor.RED + "Disabled"), ChatColor.GRAY + "(Click to change)"})));
		if (pm.getPlayer().hasPermission("setting.request.delay") && ps.isDuelRequestToggled()) {
			settingsInventory.setItem(8, ItemBuilder.createNewItemStack(new ItemStack(Material.WATCH, 1), ChatColor.GREEN + "Request Delay", Arrays.asList(new String[] {ChatColor.DARK_AQUA + "Actual value: " + ChatColor.GREEN + ps.getSecondsBeforeRerequest() + " seconds", ChatColor.GRAY + "(Click to change)"})));
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
		int i = 11;
		for (PartyEvents event : PartyEvents.values()) {
			this.partyGameInventory.setItem(i, ItemBuilder.createNewItemStack(event.icon(), ChatColor.GREEN + event.getName(), Arrays.asList(event.lore())));
			i++;
		}
	}
	
	// TODO: THIS IS UGLY ASF
	public void setLeaderboardInventory() {
		this.leaderBoardInventory[0].clear();
		this.leaderBoardInventory[1].clear();
		this.fillWithGlass(this.leaderBoardInventory[0]);
		this.fillWithGlass(this.leaderBoardInventory[1]);
		int i = 18;
		ChatColor color = null;
		for (Ladders ladders : Ladders.values()) {
			final Map<UUID, Integer> map = this.main.getDatabaseUtil().getTopEloLadderList(ladders);
			List<String> lore;
			if (map == null) {
				lore = Collections.singletonList(ChatColor.RED + "Database not connected!");
			} else {
				lore = new ArrayList<>(10);
				int rank = 1;
				for (Map.Entry<UUID, Integer> entry : map.entrySet()) {
					color = (rank == 1 ? ChatColor.AQUA : (rank == 2 ? ChatColor.GOLD : (rank == 3 ? ChatColor.GREEN : ChatColor.GRAY)));
					lore.add(color + "#" + rank + " " + ChatColor.DARK_AQUA + this.main.getServer().getOfflinePlayer(entry.getKey()).getName() + " " + ChatColor.YELLOW + "(" + entry.getValue() + ")");
					rank++;
				}
			}
			this.leaderBoardInventory[0].setItem(i, ItemBuilder.createNewItemStack(ladders.getIcon(), ladders.getColor() + ladders.getName(), lore));
			final Map<UUID, PartnerCache> map2 = this.main.getDatabaseUtil().getDuoTopEloLadderList(ladders);
			if (map2 == null) {
				lore = Collections.singletonList(ChatColor.RED + "Database not connected!");
			} else {
				lore = new ArrayList<>(10);
				int rank = 1;
				for (Map.Entry<UUID, PartnerCache> entry : map2.entrySet()) {
					final PartnerCache cache = entry.getValue();
					color = (rank == 1 ? ChatColor.AQUA : (rank == 2 ? ChatColor.GOLD : (rank == 3 ? ChatColor.GREEN : ChatColor.GRAY)));
					lore.add(color + "#" + rank + " " + ChatColor.DARK_AQUA + this.main.getServer().getOfflinePlayer(entry.getKey()).getName() + color + " & " + ChatColor.DARK_AQUA + this.main.getServer().getOfflinePlayer(cache.getPartner()).getName() + " " + ChatColor.YELLOW + "(" + cache.getElo() + ")");
					rank++;
				}
			}
			this.leaderBoardInventory[1].setItem(i, ItemBuilder.createNewItemStack(ladders.getIcon(), ladders.getColor() + ladders.getName(), lore));
			i++;
		}
		final Map<UUID, Integer> globalMap = this.main.getDatabaseUtil().getGlobalTopEloList();
		List<String> lore;
		if (globalMap == null) {
			lore = Collections.singletonList(ChatColor.RED + "Database not connected!");
		} else {
			lore = new ArrayList<>(10);
			int rank = 1;
			for (Map.Entry<UUID, Integer> entry : globalMap.entrySet()) {
				color = (rank == 1 ? ChatColor.AQUA : (rank == 2 ? ChatColor.GOLD : (rank == 3 ? ChatColor.GREEN : ChatColor.GRAY)));
				lore.add(color + "#" + rank + " " + ChatColor.DARK_AQUA + this.main.getServer().getOfflinePlayer(entry.getKey()).getName() + " " + ChatColor.YELLOW + "(" + entry.getValue() + ")");
				rank++;
			}
		}
		this.leaderBoardInventory[0].setItem(4, ItemBuilder.createNewItemStack(new ItemStack(Material.DIAMOND, 1), ChatColor.DARK_AQUA + "Global Top", lore));
		final Map<UUID, PartnerCache> map2 = this.main.getDatabaseUtil().getDuoGlobalTopEloList();
		if (map2 == null) {
			lore = Collections.singletonList(ChatColor.RED + "Database not connected!");
		} else {
			lore = new ArrayList<>(10);
			int rank = 1;
			for (Map.Entry<UUID, PartnerCache> entry : map2.entrySet()) {
				final PartnerCache cache = entry.getValue();
				color = (rank == 1 ? ChatColor.AQUA : (rank == 2 ? ChatColor.GOLD : (rank == 3 ? ChatColor.GREEN : ChatColor.GRAY)));
				lore.add(color + "#" + rank + " " + ChatColor.DARK_AQUA + this.main.getServer().getOfflinePlayer(entry.getKey()).getName() + color + " & " + ChatColor.DARK_AQUA + this.main.getServer().getOfflinePlayer(cache.getPartner()).getName() + " " + ChatColor.YELLOW + "(" + cache.getElo() + ")");
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
		return this.arenasInventory[3];
	}
	public Inventory getArenasInventory(boolean sumo, boolean spleef) {
		if (sumo) {
			return this.arenasInventory[1];
		}
		if (spleef) {
			return this.arenasInventory[2];
		}
		return this.arenasInventory[0];
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
		final Inventory inventory = this.main.getServer().createInventory(null, 36, ladder.getName() + " Edit Layout");
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
				final List<String> nameLore = Collections.singletonList(ChatColor.DARK_AQUA + "Actual Name: " + ChatColor.YELLOW + ChatColor.translateAlternateColorCodes('&', customKit.getName()));
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
		return Math.min((sizeNeeded * 9), 54);
	}
}
