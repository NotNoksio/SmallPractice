package us.noks.smallpractice.objects.managers;

import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.google.common.collect.Lists;

import us.noks.smallpractice.enums.PlayerStatus;

public class QueueManager {
	
	public static QueueManager instance = new QueueManager();
	public static QueueManager getInstance() {
		return instance;
	}

	private List<UUID> queue = Lists.newArrayList();
	public List<UUID> getQueue() {
		return this.queue;
	}
	
	public void addToQueue(UUID uuid, boolean ranked) {
		PlayerManager pm = PlayerManager.get(uuid);
		
		if (pm.getStatus() != PlayerStatus.SPAWN) {
			return;
		}
		if (!this.queue.contains(uuid)) {
			Player player = Bukkit.getPlayer(uuid);
			this.queue.add(uuid);
			pm.setStatus(PlayerStatus.QUEUE);
			player.getInventory().clear();
			if (this.queue.size() == 1) {
				pm.giveQueueItem();
			}
			player.sendMessage(ChatColor.GREEN + "You have been added to the queue. Waiting for another player..");
		}
		if (this.queue.size() < 2 && this.queue.contains(uuid)) {
			addToQueue(uuid, ranked);
		} else if (this.queue.size() >= 2) {
			Player first = Bukkit.getPlayer(this.queue.get(0));
			Player second = Bukkit.getPlayer(this.queue.get(1));
			
			if (first == second) {
				this.queue.clear();
				addToQueue(uuid, ranked);
				return;
			}
			List<UUID> firstTeam = Lists.newArrayList();
			firstTeam.add(first.getUniqueId());
			List<UUID> secondTeam = Lists.newArrayList();
			secondTeam.add(second.getUniqueId());
			
			this.queue.remove(first.getUniqueId());
			this.queue.remove(second.getUniqueId());
			DuelManager.getInstance().startDuel(null, null, firstTeam, secondTeam, ranked);
		}
	}
	
	public void quitQueue(Player player) {
		if (this.queue.contains(player.getUniqueId())) {
			this.queue.remove(player.getUniqueId());
			PlayerManager.get(player.getUniqueId()).setStatus(PlayerStatus.SPAWN);
			PlayerManager.get(player.getUniqueId()).giveSpawnItem();
			player.sendMessage(ChatColor.RED + "You have been removed from the queue.");
		}
	}
}
