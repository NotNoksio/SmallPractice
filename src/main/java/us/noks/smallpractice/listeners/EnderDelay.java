package us.noks.smallpractice.listeners;

import java.text.DecimalFormat;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EnderpearlLandEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import us.noks.smallpractice.objects.managers.PlayerManager;
import us.noks.smallpractice.utils.PlayerStatus;

public class EnderDelay implements Listener {

	private Map<UUID, Long> enderpearlCooldown = new WeakHashMap<UUID, Long>();
	private int cooldowntime = 14;
	static EnderDelay instance = new EnderDelay();

	public static EnderDelay getInstance() {
		return instance;
	}

	@EventHandler(priority=EventPriority.NORMAL)
	public void onPlayerInteract(PlayerInteractEvent e) {
		if (e.getPlayer() instanceof Player) {
			Player p = e.getPlayer();
			if (e.hasItem()) {
				ItemStack item = e.getItem();
				if ((e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) && item.getType() == Material.ENDER_PEARL && p.getGameMode() != GameMode.CREATIVE) {
					if (PlayerManager.get(p).getStatus() == PlayerStatus.DUEL) {
						if (isEnderPearlCooldownActive(p)) {
							e.setUseItemInHand(Result.DENY);
							double time = getEnderPearlCooldown(p) / 1000.0D;
							DecimalFormat df = new DecimalFormat("0.0");
							String text = ChatColor.DARK_AQUA + "Pearl cooldown: " + ChatColor.YELLOW + df.format(time) + " second";
							if (time > 1.0D) {
								text = text + "s";
							}
							p.sendMessage(text);
							p.updateInventory();
						} else {
							applyCooldown(p);
						}
					} else {
						e.setUseItemInHand(Result.DENY);
						p.sendMessage(ChatColor.RED + "You cannot use enderpearl here!");
						p.updateInventory();
					}
				}
			}
		}
	}
	
	@EventHandler(priority=EventPriority.LOWEST)
	public void onTeleport(EnderpearlLandEvent event) {
		if (event.getEntity().getShooter() instanceof Player) {
			Player p = (Player) event.getEntity().getShooter();
			
			if (PlayerManager.get(p).getStatus() != PlayerStatus.DUEL) {
				event.setCancelled(true);
			}
		}
	}

	public boolean isEnderPearlCooldownActive(Player p) {
		if (!this.enderpearlCooldown.containsKey(p.getUniqueId())) {
			return false;
		}
		return this.enderpearlCooldown.get(p.getUniqueId()).longValue() > System.currentTimeMillis();
	}

	public long getEnderPearlCooldown(Player p) {
		if (this.enderpearlCooldown.containsKey(p.getUniqueId())) {
			return Math.max(0L, this.enderpearlCooldown.get(p.getUniqueId()).longValue() - System.currentTimeMillis());
		}
		return 0L;
	}

	public void applyCooldown(Player p) {
		this.enderpearlCooldown.put(p.getUniqueId(), Long.valueOf(System.currentTimeMillis() + this.cooldowntime * 1000));
	}

	public void removeCooldown(Player p) {
		if (this.enderpearlCooldown.containsKey(p.getUniqueId())) {
			this.enderpearlCooldown.remove(p.getUniqueId());
		}
	}
}
