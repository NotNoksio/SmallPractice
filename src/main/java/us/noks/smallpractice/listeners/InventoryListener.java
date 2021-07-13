package us.noks.smallpractice.listeners;

import java.util.List;
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
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

import com.google.common.collect.Lists;

import us.noks.smallpractice.Main;
import us.noks.smallpractice.arena.Arena;
import us.noks.smallpractice.arena.Arena.Arenas;
import us.noks.smallpractice.enums.Ladders;
import us.noks.smallpractice.enums.PlayerStatus;
import us.noks.smallpractice.objects.Duel;
import us.noks.smallpractice.objects.Request;
import us.noks.smallpractice.objects.managers.DuelManager;
import us.noks.smallpractice.objects.managers.InventoryManager;
import us.noks.smallpractice.objects.managers.PlayerManager;
import us.noks.smallpractice.objects.managers.QueueManager;
import us.noks.smallpractice.objects.managers.RequestManager;

public class InventoryListener implements Listener {
	private Main main;
	public InventoryListener(Main plugin) {
		this.main = plugin;
	    this.main.getServer().getPluginManager().registerEvents(this, this.main);
	}
	
	private Pattern splitPattern = Pattern.compile("\\s");
	
	@EventHandler(priority=EventPriority.LOWEST)
	public void onInventoryClick(InventoryClickEvent event) {
		if (!event.getInventory().getType().equals(InventoryType.CHEST)) {
			return;
		}
		final ItemStack item = event.getCurrentItem();
		
		if (item == null || item.getType() == null || item.getItemMeta() == null || item.getItemMeta().getDisplayName() == null) {
			return;
		}
		final String title = event.getInventory().getTitle().toLowerCase();

		if (title.endsWith("inventory")) {
            event.setCancelled(true);
            return;
        }
		final Player player = (Player) event.getWhoClicked();
		if (title.equals("unranked selection") || title.equals("ranked selection") || title.equals("ladder selection")) {
			event.setCancelled(true);
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
				Request request = InventoryManager.getInstance().getSelectingDuelPlayerUUID(player.getUniqueId());
				request.setLadder(ladder);
				player.openInventory(InventoryManager.getInstance().getArenasInventory());
				return;
			}
			QueueManager.getInstance().addToQueue(player.getUniqueId(), ladder, title.equals("ranked selection"));
		}
		if (title.equals("fight other parties")) {
			event.setCancelled(true);
			
			if (item.getData().getData() == SkullType.WITHER.ordinal()) {
				player.sendMessage(ChatColor.RED + "This party is not in spawn!");
				player.closeInventory();
				return;
			}
			String[] itemName = splitString(ChatColor.stripColor(item.getItemMeta().getDisplayName()));
            //itemName[0] = ChatColor.stripColor(itemName[0]);
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
			if (InventoryManager.getInstance().getSelectingDuelPlayerUUID(player.getUniqueId()) != null) {
				final Request request = InventoryManager.getInstance().getSelectingDuelPlayerUUID(player.getUniqueId());
				final Player target = Bukkit.getPlayer(request.getRequestedUUID());
				if (target == null) {
					player.sendMessage(ChatColor.RED + "Player not found!");
					player.closeInventory();
					return;
				} 
				RequestManager.getInstance().sendDuelRequest(Arena.getInstance().getArenaByInteger(slotTranslation), request.getLadder(), player, target);
			} else {
				final Arenas selectedArena = Arena.getInstance().getArenaByInteger(slotTranslation);
				for (Arenas allArenas : Arena.getInstance().getArenaList().values()) {
    				if (!allArenas.getAllSpectators().contains(player.getUniqueId())) continue;
    				allArenas.removeSpectator(player.getUniqueId());
    			}
				
				final List <UUID> playersInArena = Lists.newArrayList();
                for (Duel duel : DuelManager.getInstance().getAllDuels()) {
                	if (selectedArena != duel.getArena()) continue;
                	playersInArena.addAll(duel.getFirstAndSecondTeamsAlive());
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
		if (title.equals("selector")) {
			event.setCancelled(true);
			String itemName = ChatColor.stripColor(item.getItemMeta().getDisplayName().toLowerCase());
			if (itemName.equals("kit creator")) {
				player.closeInventory();
				player.openInventory(InventoryManager.getInstance().getEditingInventory());
				return;
			}
			if (itemName.equals("configurate settings")) {
				player.closeInventory();
				player.openInventory(InventoryManager.getInstance().getSettingsInventory());
			}
		}
	}
	
	@EventHandler(priority=EventPriority.LOW)
	public void onDrag(InventoryClickEvent event) {
		if (event.getInventory().getType().equals(InventoryType.CREATIVE) || event.getInventory().getType().equals(InventoryType.CRAFTING) || event.getInventory().getType().equals(InventoryType.PLAYER)) {
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
	
	@EventHandler
	public void onCloseInventory(InventoryCloseEvent event) {
		if (!event.getInventory().getType().equals(InventoryType.CHEST)) {
			return;
		}
		final String title = event.getInventory().getTitle().toLowerCase();
		
		if (title.equals("unranked selection")) {
			InventoryManager.getInstance().updateUnrankedInventory();
		}
	}
	
	private String[] splitString(final String string) {
		return string.split(this.splitPattern.pattern());
	}
}
