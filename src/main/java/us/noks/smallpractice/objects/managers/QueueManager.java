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

	public List<UUID> queue = Lists.newArrayList();
	
	public void addToQueue(Player player, boolean ranked) {
		if (PlayerManager.get(player.getUniqueId()).getStatus() != PlayerStatus.SPAWN) {
			return;
		}
		if (!this.queue.contains(player.getUniqueId())) {
			this.queue.add(player.getUniqueId());
			PlayerManager.get(player.getUniqueId()).setStatus(PlayerStatus.QUEUE);
			if (this.queue.size() == 1) {
				PlayerManager.get(player.getUniqueId()).giveQueueItem();
			}
			player.sendMessage(ChatColor.GREEN + "You have been added to the queue. Waiting for another player..");
		}
		if (this.queue.size() == 1 && this.queue.contains(player.getUniqueId())) {
			addToQueue(player, ranked);
		} else if (this.queue.size() == 2) {
			Player first = Bukkit.getPlayer(this.queue.get(0));
			Player second = Bukkit.getPlayer(this.queue.get(1));
			
			if (first == player && second == first) {
				this.queue.clear();
				addToQueue(player, ranked);
				return;
			}
			List<UUID> firstTeam = Lists.newArrayList();
			firstTeam.add(first.getUniqueId());
			List<UUID> secondTeam = Lists.newArrayList();
			secondTeam.add(second.getUniqueId());
			
			DuelManager.getInstance().startDuel(null, null, firstTeam, secondTeam, ranked, 1);
			this.queue.remove(first.getUniqueId());
			this.queue.remove(second.getUniqueId());
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
