package us.noks.smallpractice.listeners;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import us.noks.smallpractice.objects.managers.PlayerManager;
import us.noks.smallpractice.objects.managers.RequestManager;
import us.noks.smallpractice.utils.DuelRequest;

public class InventoryListener implements Listener {
	
	@EventHandler(priority=EventPriority.LOWEST)
	public void onInventoryClick(InventoryClickEvent event) {
		Player player = (Player) event.getWhoClicked();
		ItemStack item = event.getCurrentItem();
		
		if (item == null || item.getType() == null || item.getItemMeta() == null || item.getItemMeta().getDisplayName() == null) {
			return;
		}
		if (event.getInventory().getTitle().toLowerCase().equals("how many rounds?")) {
			PlayerManager pm = PlayerManager.get(player.getUniqueId());
			Player requested = Bukkit.getPlayer(pm.getRequestTo());
			
			if (item.getType() == Material.ARROW) {
				player.closeInventory();
				
				TextComponent l1 = new TextComponent();
				l1.setText(player.getName());
				l1.setColor(net.md_5.bungee.api.ChatColor.YELLOW);
			    
				TextComponent l1a = new TextComponent();
				l1a.setText(" has requested to duel you with " + item.getAmount() + " rounds! ");
				l1a.setColor(net.md_5.bungee.api.ChatColor.DARK_AQUA);
			    
				TextComponent l1b = new TextComponent();
				l1b.setText("Click here to accept.");
				l1b.setColor(net.md_5.bungee.api.ChatColor.GREEN);
				l1b.setBold(true);
				l1b.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(net.md_5.bungee.api.ChatColor.GREEN + "Click this message to accept " + player.getName()).create()));
				l1b.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/accept " + player.getName()));
			    
				l1.addExtra(l1a);
				l1.addExtra(l1b);
			    
				requested.spigot().sendMessage(l1);
				player.sendMessage(ChatColor.DARK_AQUA + "You sent a duel request to " + ChatColor.YELLOW + requested.getName());
				RequestManager.getInstance().addDuelRequest(requested, player, new DuelRequest(item.getAmount()));
			}
		}
	}
}
