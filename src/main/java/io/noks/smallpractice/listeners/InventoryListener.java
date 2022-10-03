package io.noks.smallpractice.listeners;

import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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

import io.noks.smallpractice.Main;
import io.noks.smallpractice.arena.Arena;
import io.noks.smallpractice.arena.Arena.Arenas;
import io.noks.smallpractice.enums.Ladders;
import io.noks.smallpractice.enums.PlayerStatus;
import io.noks.smallpractice.objects.Duel;
import io.noks.smallpractice.objects.Request;
import io.noks.smallpractice.objects.managers.PlayerManager;
import net.minecraft.util.com.google.common.collect.Sets;

public class InventoryListener implements Listener {
	private Main main;
	public InventoryListener(Main plugin) {
		this.main = plugin;
	    this.main.getServer().getPluginManager().registerEvents(this, this.main);
	}
	
	@EventHandler(priority=EventPriority.LOWEST)
	public void onInventoryClick(InventoryClickEvent event) {
		final Inventory inventory = event.getInventory();
		if (inventory == null) {
			return;
		}
		if (inventory.getType() != InventoryType.CHEST) {
			return;
		}
		final ItemStack item = event.getCurrentItem();
		
		if (item == null || item.getType() == null) {
			return;
		}
		final String title = inventory.getTitle().toLowerCase();
		if (title.endsWith("inventory")) {
            event.setCancelled(true);
            return;
        }
		if (item.getItemMeta() == null || item.getItemMeta().getDisplayName() == null){
			return;
		}
		final Player player = (Player) event.getWhoClicked();
		if (title.equals("unranked selection") || title.equals("ranked selection") || title.equals("ladder selection") || title.equals("editing selection")) {
			event.setCancelled(true);
			if (title.startsWith("editing")) {
				return;
			}
			String itemName = ChatColor.stripColor(item.getItemMeta().getDisplayName());
			if (!Ladders.contains(itemName)) {
				return;
			}
			Ladders ladder = Ladders.getLadderFromName(itemName);
			if (!ladder.isEnable()) {
				player.sendMessage(ChatColor.RED + "No arena created!");
				player.closeInventory();
				return;
			}
			player.closeInventory();
			if (title.contains("ladder")) {
				if (this.main.getInventoryManager().getSelectingDuelPlayerUUID(player.getUniqueId()) != null) {
					Request request = this.main.getInventoryManager().getSelectingDuelPlayerUUID(player.getUniqueId());
					request.setLadder(ladder);
					player.openInventory(this.main.getInventoryManager().getArenasInventory());
					return;
				}
				this.main.getDuelManager().createSplitTeamsDuel(this.main.getPartyManager().getParty(player.getUniqueId()), ladder);
				return;
			}
			this.main.getQueueManager().addToQueue(player.getUniqueId(), ladder, title.equals("ranked selection"), this.main.getPartyManager().hasParty(player.getUniqueId()));
		}
		if (title.equals("fight other parties")) {
			event.setCancelled(true);
			
			if (item.getData().getData() == SkullType.WITHER.ordinal()) {
				player.sendMessage(ChatColor.RED + "This party is not in spawn!");
				player.closeInventory();
				return;
			}
			String[] itemName = splitString(ChatColor.stripColor(item.getItemMeta().getDisplayName()));
            if (player.getName().toLowerCase().equals(itemName[0].toLowerCase())) {
            	player.sendMessage(ChatColor.RED + "You can't execute that command on yourself!");
            	return;
            }
            player.closeInventory();
            Bukkit.dispatchCommand(player, "duel " + itemName[0]); 
		}
		if (title.equals("arena selection")) {
			event.setCancelled(true);
			int slotTranslation = event.getSlot() + 1;
			if (Arena.getInstance().getArenaByInteger(slotTranslation) == null) {
				return;
			}
			if (this.main.getInventoryManager().getSelectingDuelPlayerUUID(player.getUniqueId()) != null) {
				final Request request = this.main.getInventoryManager().getSelectingDuelPlayerUUID(player.getUniqueId());
				final Player target = Bukkit.getPlayer(request.getRequestedUUID());
				if (target == null) {
					player.sendMessage(ChatColor.RED + "Player not found!");
					player.closeInventory();
					return;
				} 
				this.main.getRequestManager().sendDuelRequest(Arena.getInstance().getArenaByInteger(slotTranslation), request.getLadder(), player, target);
			} else {
				final Arenas selectedArena = Arena.getInstance().getArenaByInteger(slotTranslation);
				for (Arenas allArenas : Arena.getInstance().getArenaList().values()) {
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
	                	Player playerInArena = Bukkit.getPlayer(playerInArenaUUID);
	                	if (!player.canSee(playerInArena)) player.showPlayer(playerInArena);
	                }
                }
                selectedArena.addSpectator(player.getUniqueId());
				player.teleport(selectedArena.getLocations()[0]);
				playersInArena.clear();
			}
			player.closeInventory();
		}
		if (title.endsWith("configuration")) {
			event.setCancelled(true);
			return;
		}
		if (title.equals("selector")) {
			event.setCancelled(true);
			String itemName = ChatColor.stripColor(item.getItemMeta().getDisplayName().toLowerCase());
			if (itemName.equals("kit creator")) {
				player.closeInventory();
				player.openInventory(this.main.getInventoryManager().getEditingInventory());
				return;
			}
			if (itemName.equals("configurate settings")) {
				player.closeInventory();
				player.openInventory(this.main.getInventoryManager().getSettingsInventory());
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
			
			if (pm.isAllowedToBuild()) {
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
			
			if (inventory.getType() != InventoryType.CRAFTING && inventory.getType() != InventoryType.CHEST && inventory.getType() != InventoryType.PLAYER) {
				event.setCancelled(true);
			}
		}
	}
	
	private Pattern splitPattern = Pattern.compile("\\s");
	private String[] splitString(final String string) {
		return string.split(this.splitPattern.pattern());
	}
}
