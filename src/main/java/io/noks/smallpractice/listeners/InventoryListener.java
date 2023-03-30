package io.noks.smallpractice.listeners;

import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import io.noks.smallpractice.Main;
import io.noks.smallpractice.arena.Arena;
import io.noks.smallpractice.enums.Ladders;
import io.noks.smallpractice.enums.PlayerStatus;
import io.noks.smallpractice.objects.EditedLadderKit;
import io.noks.smallpractice.objects.PlayerSettings;
import io.noks.smallpractice.objects.Request;
import io.noks.smallpractice.objects.duel.Duel;
import io.noks.smallpractice.objects.managers.PlayerManager;
import io.noks.smallpractice.party.Party;
import net.minecraft.util.com.google.common.collect.Sets;

public class InventoryListener implements Listener {
	private Main main;
	public InventoryListener(Main plugin) {
		this.main = plugin;
	    this.main.getServer().getPluginManager().registerEvents(this, this.main);
	}
	
	@EventHandler(priority=EventPriority.LOWEST)
	public void onInventoryClick(InventoryClickEvent event) {
		final Inventory inventory = event.getClickedInventory();
		if (inventory == null) {
			return;
		}
		if (inventory.getType() != InventoryType.CHEST && inventory.getType() != InventoryType.DISPENSER) {
			return;
		}
		final Player player = (Player) event.getWhoClicked();
		final PlayerManager pm = PlayerManager.get(player.getUniqueId());
		if (pm.isAllowedToBuild()) {
			return;
		}
		event.setCancelled(true);
		final ItemStack item = event.getCurrentItem();
		
		if (item == null || item.getType() == null) {
			return;
		}
		final String title = inventory.getTitle().toLowerCase();
		if (title.endsWith("inventory")) {
            return;
        }
		if (item.getItemMeta() == null || item.getItemMeta().getDisplayName() == null){
			return;
		}
		if (title.equals("unranked selection") || title.equals("ranked selection") || title.equals("ladder selection") || title.equals("editing selection")) {
			final String itemName = ChatColor.stripColor(item.getItemMeta().getDisplayName());
			if (!Ladders.contains(itemName)) {
				return;
			}
			final Ladders ladder = Ladders.getLadderFromName(itemName);
			if (title.startsWith("editing")) {
				player.openInventory(this.main.getInventoryManager().getKitEditingLayout(pm, ladder));
				return;
			}
			if (!ladder.isEnable()) {
				player.sendMessage(ChatColor.RED + "No arena created!");
				player.closeInventory();
				return;
			}
			if (title.contains("ladder")) {
				if (this.main.getInventoryManager().getSelectingDuelPlayerUUID(player.getUniqueId()) != null) {
					Request request = this.main.getInventoryManager().getSelectingDuelPlayerUUID(player.getUniqueId());
					request.setLadder(ladder);
					player.openInventory(this.main.getInventoryManager().getArenasInventory(ladder == Ladders.SUMO));
					return;
				}
				player.openInventory(this.main.getInventoryManager().getPartyGameInventory());
				player.setMetadata("ladder", new FixedMetadataValue(this.main, ladder.getName()));
				return;
			}
			final PlayerSettings settings = pm.getSettings();
			this.main.getQueueManager().addToQueue(player.getUniqueId(), ladder, !title.startsWith("unranked"), this.main.getPartyManager().hasParty(player.getUniqueId()), settings.getQueuePingDiff());
			player.closeInventory();
			return;
		}
		if (title.equals("select gamemode")) {
			if (!player.hasMetadata("ladder")) {
				player.closeInventory();
				return;
			}
			final Ladders ladder = Ladders.getLadderFromName(player.getMetadata("ladder").get(0).asString());
			final Party party = this.main.getPartyManager().getParty(player.getUniqueId());
			if (item.getType() == Material.SHEARS) {
				this.main.getDuelManager().createSplitTeamsDuel(party, ladder);
				player.closeInventory();
				player.removeMetadata("ladder", this.main);
				return;
			}
			if (item.getType() == Material.DIAMOND_SWORD && item.getItemMeta().getDisplayName().toLowerCase().contains("ffa")) {
				this.main.getDuelManager().startDuel(this.main.getArenaManager().getRandomArena(ladder), ladder, player.getUniqueId(), party.getMembersIncludingLeader());
				player.closeInventory();
				player.removeMetadata("ladder", this.main);
				return;
			}
			if (item.getType() == Material.WOOL) {
				player.sendMessage(ChatColor.RED + "Coming SOON..");
			}
		}
		if (title.equals("fight other parties")) {
			if (item.getData().getData() == SkullType.WITHER.ordinal()) {
				player.sendMessage(ChatColor.RED + "This party is currently in a match!");
				player.closeInventory();
				return;
			}
			final String[] itemName = splitString(ChatColor.stripColor(item.getItemMeta().getDisplayName()));
            if (player.getName().toLowerCase().equals(itemName[0].toLowerCase())) {
            	player.sendMessage(ChatColor.RED + "You can't execute that command on yourself!");
            	return;
            }
            player.closeInventory();
            this.main.getServer().dispatchCommand(player, "duel " + itemName[0]); 
		}
		if (title.equals("arena selection")) {
			final String itemName = ChatColor.stripColor(item.getItemMeta().getDisplayName().toLowerCase());
			if (this.main.getInventoryManager().getSelectingDuelPlayerUUID(player.getUniqueId()) != null) {
				final Request request = this.main.getInventoryManager().getSelectingDuelPlayerUUID(player.getUniqueId());
				final Player target = this.main.getServer().getPlayer(request.getRequestedUUID());
				if (target == null) {
					player.sendMessage(ChatColor.RED + "Player not found!");
					player.closeInventory();
					return;
				} 
				final Arena arena = (itemName.equals("random") ? this.main.getArenaManager().getRandomArena(request.getLadder()) : this.main.getArenaManager().getArenaByName(itemName));
				if (arena == null) {
					return;
				}
				this.main.getRequestManager().sendDuelRequest(arena, request.getLadder(), player, target);
			} else if (pm.getStatus() == PlayerStatus.SPECTATE) {
				final Arena selectedArena = this.main.getArenaManager().getArenaByName(itemName);
				if (selectedArena == null) {
					return;
				}
				for (Arena allArenas : this.main.getArenaManager().getArenaList()) {
    				if (!allArenas.getAllSpectators().contains(player.getUniqueId())) continue;
    				allArenas.removeSpectator(player.getUniqueId());
    			}
				
				final Set<UUID> playersInArena = Sets.newHashSet();
                for (Duel duel : this.main.getDuelManager().getAllDuels()) {
                	if (selectedArena != duel.getArena()) continue;
                	playersInArena.addAll(duel.getAllAliveTeams());
                }
                if (!playersInArena.isEmpty()) {
	                for (UUID playerInArenaUUID : playersInArena) {
	                	Player playerInArena = this.main.getServer().getPlayer(playerInArenaUUID);
	                	if (!player.canSee(playerInArena)) player.showPlayer(playerInArena);
	                }
                }
                selectedArena.addSpectator(player.getUniqueId());
				player.teleport(selectedArena.getLocations()[0]);
				playersInArena.clear();
			} else {
				final Arena selectedArena = this.main.getArenaManager().getArenaByName(itemName);
				if (selectedArena == null) {
					return;
				}
				player.teleport(selectedArena.getMiddle());
				player.sendMessage(ChatColor.GREEN + "Teleported to " + selectedArena.getName() + " arena.");
			}
			player.closeInventory();
		}
		if (title.endsWith("configuration")) {
			final String itemName = ChatColor.stripColor(item.getItemMeta().getDisplayName().toLowerCase());
			final PlayerSettings settings = pm.getSettings();
			if (itemName.startsWith("ping")) {
				settings.updatePingDiff();
				player.openInventory(this.main.getInventoryManager().getSettingsInventory(pm));
				return;
			}
			if (itemName.equals("request delay")) {
				settings.updateSecondsBeforeRerequest();
				player.openInventory(this.main.getInventoryManager().getSettingsInventory(pm));
				return;
			}
			if (itemName.startsWith("toggle")) {
				if (itemName.endsWith("private message")) {
					settings.updatePrivateMessage();
				}
				if (itemName.endsWith("party invite")) {
					settings.updatePartyInvite();
				}
				if (itemName.endsWith("duel request")) {
					settings.updateDuelRequest();
				}
				player.openInventory(this.main.getInventoryManager().getSettingsInventory(pm));
				return;
			}
			return;
		}
		if (title.equals("selector")) {
			final String itemName = ChatColor.stripColor(item.getItemMeta().getDisplayName().toLowerCase());
			if (itemName.equals("kit creator")) {
				player.closeInventory();
				player.openInventory(this.main.getInventoryManager().getEditingInventory());
				return;
			}
			if (itemName.equals("configurate settings")) {
				player.closeInventory();
				player.openInventory(this.main.getInventoryManager().getSettingsInventory(pm));
				return;
			}
			return;
		}
		if (title.contains("leaderboard")) {
			if (item.getType() == Material.CARPET) {
				final String itemName = ChatColor.stripColor(item.getItemMeta().getDisplayName().toLowerCase());
				player.openInventory(this.main.getInventoryManager().getLeaderboardInventory(!itemName.startsWith("1v1")));
			}
			return;
		}
		if (title.endsWith("edit layout")) {
			final String itemName = ChatColor.stripColor(item.getItemMeta().getDisplayName().toLowerCase());
			final Ladders ladder = Ladders.getLadderFromName(title.split(" ")[0]);
			if (ladder == null) {
				player.closeInventory();
				return;
			}
			if (itemName.startsWith("create") || itemName.startsWith("save")) {
				final boolean save = itemName.startsWith("save");
				Inventory createdInventory = this.main.getItemManager().getFightItems(ladder);
				if (save) {
					final ItemStack[] defaultArmorContent = {createdInventory.getItem(36), createdInventory.getItem(37), createdInventory.getItem(38), createdInventory.getItem(39)};
					final Inventory savedInventory = Bukkit.createInventory(null, InventoryType.PLAYER);
					for (ItemStack items : player.getInventory().getContents()) {
						savedInventory.addItem(items);
					}
					savedInventory.setItem(36, defaultArmorContent[0]);
					savedInventory.setItem(37, defaultArmorContent[1]);
					savedInventory.setItem(38, defaultArmorContent[2]);
					savedInventory.setItem(39, defaultArmorContent[3]);
					createdInventory = savedInventory;
				}
				pm.saveCustomLadderKit(ladder, event.getSlot(), createdInventory);
				player.openInventory(this.main.getInventoryManager().getKitEditingLayout(pm, ladder));
				if (save) {
					this.main.getItemManager().giveSpawnItem(player);
				}
				return;
			}
			if (itemName.equals("delete")) {
				pm.deleteCustomLadderKit(ladder, event.getSlot() - 27);
				player.openInventory(this.main.getInventoryManager().getKitEditingLayout(pm, ladder));
				return;
			}
			if (itemName.equals("rename")) {
				player.setMetadata("renamekit", new FixedMetadataValue(this.main, pm.getCustomLadderKitFromSlot(ladder, event.getSlot() - 18)));
				player.sendMessage(ChatColor.GRAY + "Type a name for this kit (color code allowed)");
				player.sendMessage(ChatColor.GRAY + "Type " + ChatColor.RED.toString() + ChatColor.BOLD + "cancel" + ChatColor.GRAY + " to cancel");
				player.closeInventory();
				return;
			}
			if (itemName.equals("load/edit")) {
				final int kitSlot = event.getSlot() - 9;
				final EditedLadderKit editedKit = pm.getCustomLadderKitFromSlot(ladder, kitSlot);
				if (editedKit != null) {
					player.setMetadata("editing", new FixedMetadataValue(this.main, pm.getCustomLadderKitFromSlot(ladder, kitSlot)));
					this.main.getItemManager().giveFightItems(player, ladder, kitSlot, false, true);
					return;
				}
				player.setMetadata("editing", new FixedMetadataValue(this.main, pm.getCustomLadderKitFromSlot(ladder, kitSlot)));
				this.main.getItemManager().giveFightItems(player, ladder, 0, false, true);
			}
		}
	}
	
	@EventHandler(priority=EventPriority.LOW)
	public void blockSpawnMoveItem(InventoryClickEvent event) {
		final Inventory inventory = event.getClickedInventory();
		if (inventory == null) {
			return;
		}
		if (inventory.getType().equals(InventoryType.CREATIVE) || inventory.getType().equals(InventoryType.CRAFTING) || inventory.getType().equals(InventoryType.PLAYER)) {
			final Player player = (Player) event.getWhoClicked();
			final PlayerManager pm = PlayerManager.get(player.getUniqueId());
			
			if (pm.isAllowedToBuild() || player.hasMetadata("editing")) {
				return;
			}
			if (pm.getStatus() == PlayerStatus.MODERATION || pm.getStatus() != PlayerStatus.DUEL && pm.getStatus() != PlayerStatus.WAITING) {
				event.setCancelled(true);
				player.updateInventory();
			}
		}
	}
	
	@EventHandler(priority=EventPriority.LOWEST)
	public void onInventoryClose(InventoryCloseEvent event) {
		if (!event.getInventory().getType().equals(InventoryType.CHEST)) {
			return;
		}
		final String title = event.getInventory().getTitle().toLowerCase();
		
		if (title.startsWith("unranked") || title.startsWith("ranked")) {
			this.main.getInventoryManager().updateQueueInventory(title.contains("ranked"));
			return;
		} 
		final Player player = (Player) event.getPlayer();
		if (title.equals("select gamemode") && player.hasMetadata("ladder")) {
			player.removeMetadata("ladder", this.main);
			return;
		}
		if (title.endsWith("edit layout") && player.hasMetadata("editing")) {
			Bukkit.getScheduler().runTask(this.main, () -> {
				this.main.getItemManager().giveSpawnItem(player);
				player.removeMetadata("editing", this.main);
			});
		}
	}
	
	@EventHandler(priority=EventPriority.LOWEST)
	public void onInventoryOpen(InventoryOpenEvent event) {
		final Player player = (Player) event.getPlayer();
		final PlayerManager pm = PlayerManager.get(player.getUniqueId());
		if (pm.isAllowedToBuild()) {
			return;
		}
		if (pm.getStatus() == PlayerStatus.SPAWN || pm.getStatus() == PlayerStatus.QUEUE) {
			final Inventory inventory = event.getInventory();
			
			if (inventory.getType() != InventoryType.CRAFTING && inventory.getType() != InventoryType.CHEST && inventory.getType() != InventoryType.PLAYER && inventory.getType() != InventoryType.DISPENSER) {
				event.setCancelled(true);
			}
		}
	}
	
	private Pattern splitPattern = Pattern.compile("\\s");
	private String[] splitString(final String string) {
		return string.split(this.splitPattern.pattern());
	}
}
