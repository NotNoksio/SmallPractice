package us.noks.smallpractice.objects.managers;

import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.google.common.collect.Lists;

import us.noks.smallpractice.arena.Arena;
import us.noks.smallpractice.enums.PlayerStatus;

public class QueueManager {
	private static QueueManager instance = new QueueManager();
	public static QueueManager getInstance() {
		return instance;
	}

	private List<UUID> queue = Lists.newArrayList();
	public List<UUID> getQueue() {
		return this.queue;
	}
	
	public void addToQueue(UUID uuid, boolean ranked) {
		final PlayerManager pm = PlayerManager.get(uuid);
		
		if (pm.getStatus() != PlayerStatus.SPAWN) {
			return;
		}
		if (!this.queue.contains(uuid)) {
			final Player player = Bukkit.getPlayer(uuid);
			this.queue.add(uuid);
			pm.setStatus(PlayerStatus.QUEUE);
			player.getInventory().clear();
			if (this.queue.size() == 1) {
				ItemManager.getInstace().giveLeaveItem(player, "Queue");
			}
			player.sendMessage(ChatColor.GREEN + "You have been added to the queue. Waiting for another player..");
		}
		if (this.queue.size() < 2 && this.queue.contains(uuid)) {
			addToQueue(uuid, ranked);
			return;
		}
		if (this.queue.size() >= 2) {
			final UUID firstUUID = this.queue.get(0);
			final UUID secondUUID = this.queue.get(1);
			
			if (firstUUID == secondUUID) {
				this.queue.remove(0);
				this.queue.remove(1);
				addToQueue(uuid, ranked);
				return;
			}
			this.queue.remove(firstUUID);
			this.queue.remove(secondUUID);
			DuelManager.getInstance().startDuel(Arena.getInstance().getRandomArena(), firstUUID, secondUUID, ranked);
		}
	}
	
	public void quitQueue(Player player) {
		if (!this.queue.contains(player.getUniqueId())) {
			return;
		}
		this.queue.remove(player.getUniqueId());
		PlayerManager.get(player.getUniqueId()).setStatus(PlayerStatus.SPAWN);
		ItemManager.getInstace().giveSpawnItem(player);
		player.sendMessage(ChatColor.RED + "You have been removed from the queue.");
	}
}
