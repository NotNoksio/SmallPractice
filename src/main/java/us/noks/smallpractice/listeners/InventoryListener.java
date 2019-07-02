package us.noks.smallpractice.listeners;

import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

import us.noks.smallpractice.enums.PlayerStatus;
import us.noks.smallpractice.objects.managers.PlayerManager;
import us.noks.smallpractice.utils.Messages;

public class InventoryListener implements Listener {
	
	private Pattern splitPattern = Pattern.compile("\\s");
	
	@EventHandler(priority=EventPriority.LOWEST)
	public void onInventoryClick(InventoryClickEvent event) {
		final Player player = (Player) event.getWhoClicked();
		final ItemStack item = event.getCurrentItem();
		
		if (item == null || item.getType() == null || item.getItemMeta() == null || item.getItemMeta().getDisplayName() == null) {
			return;
		}
		final String title = event.getInventory().getTitle().toLowerCase();
		
		if (title.endsWith("inventory")) {
            event.setCancelled(true);
        }
		if (title.equals("fight other parties")) {
			event.setCancelled(true);
			
			String[] itemName = splitString(item.getItemMeta().getDisplayName());
            itemName[0] = ChatColor.stripColor(itemName[0]);
            
            if (player.getName().toLowerCase().equals(itemName[0].toLowerCase())) {
            	player.sendMessage(Messages.getInstance().NOT_YOURSELF);
            	return;
            }
            
            player.closeInventory();
            Bukkit.dispatchCommand(player, "duel " + itemName[0]); 
		}
	}
	
	@EventHandler(priority=EventPriority.LOWEST)
	public void onDrag(InventoryClickEvent event) {
		if (event.getInventory().getType().equals(InventoryType.CREATIVE) || event.getInventory().getType().equals(InventoryType.CRAFTING) || event.getInventory().getType().equals(InventoryType.PLAYER)) {
			final Player player = (Player) event.getWhoClicked();
			final PlayerManager pm = PlayerManager.get(player.getUniqueId());
			
			if (pm.getStatus() == PlayerStatus.MODERATION || (pm.getStatus() != PlayerStatus.DUEL && pm.getStatus() != PlayerStatus.WAITING && !pm.isCanBuild())) {
				event.setCancelled(true);
				player.updateInventory();
			}
		}
	}
	
	private String[] splitString(final String string) {
		return string.split(this.splitPattern.pattern());
	}
}
