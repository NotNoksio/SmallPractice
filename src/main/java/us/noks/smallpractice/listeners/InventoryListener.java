package us.noks.smallpractice.listeners;

import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

import us.noks.smallpractice.Main;
import us.noks.smallpractice.arena.Arena;
import us.noks.smallpractice.enums.Ladders;
import us.noks.smallpractice.enums.PlayerStatus;
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
		if (title.equals("unranked selection")) {
			event.setCancelled(true);
			String itemName = item.getItemMeta().getDisplayName();
			String itemNameWithoutColor = itemName.substring(2, itemName.length());
			if (!Ladders.contains(itemNameWithoutColor)) {
				return;
			}
			Ladders ladder = Ladders.getLadderFromName(itemNameWithoutColor);
			if (!ladder.isEnable()) {
				player.sendMessage(ChatColor.RED + "This gamemode is coming soon ^^");
				player.closeInventory();
				return;
			}
			QueueManager.getInstance().addToQueue(player.getUniqueId(), ladder, false, false);
			player.closeInventory();
		}
		if (title.equals("fight other parties")) {
			event.setCancelled(true);
			
			String[] itemName = splitString(item.getItemMeta().getDisplayName());
            itemName[0] = ChatColor.stripColor(itemName[0]);
            
            if (player.getName().toLowerCase().equals(itemName[0].toLowerCase())) {
            	player.sendMessage(ChatColor.RED + "You can't execute that command on yourself!");
            	return;
            }
            player.closeInventory();
            Bukkit.dispatchCommand(player, "duel " + itemName[0]); 
		}
		if (title.equals("arena selection")) {
			event.setCancelled(true);
			Player target = Bukkit.getPlayer(InventoryManager.getInstance().getSelectingDuelPlayerUUID(player.getUniqueId()));
			
			if (target == null) {
				player.sendMessage(ChatColor.RED + "Player not found!");
				player.closeInventory();
				return;
			} 
			int slotTranslation = event.getSlot() + 1;
			if (Arena.getInstance().getArenaByInteger(slotTranslation) == null) {
				return;
			}
			RequestManager.getInstance().sendDuelRequest(Arena.getInstance().getArenaByInteger(slotTranslation), Ladders.NODEBUFF, player, target);
			player.closeInventory();
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
		
		if (title.equals("arena selection")) {
			final Player player = (Player) event.getPlayer();
			InventoryManager.getInstance().removeSelectingDuel(player.getUniqueId());
		}
	}
	
	private String[] splitString(final String string) {
		return string.split(this.splitPattern.pattern());
	}
}
