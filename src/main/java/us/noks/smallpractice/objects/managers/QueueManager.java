package us.noks.smallpractice.objects.managers;

import java.util.Map;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import net.minecraft.util.com.google.common.collect.Maps;
import us.noks.smallpractice.Main;
import us.noks.smallpractice.arena.Arena;
import us.noks.smallpractice.enums.Ladders;
import us.noks.smallpractice.enums.PlayerStatus;

public class QueueManager {
	private Map<UUID, Queue> queue = Maps.newConcurrentMap();
	public Map<UUID, Queue> getQueueMap() {
		return this.queue;
	}
	
	public void addToQueue(UUID uuid, Ladders ladder, boolean ranked) { // TODO: Do 2v2 queue & ping detector due to 20ms vs 220ms
		if (!this.queue.containsKey(uuid)) {
			final PlayerManager pm = PlayerManager.get(uuid);
			final Player player = pm.getPlayer();
			this.queue.put(uuid, new Queue(ladder, ranked));
			pm.setStatus(PlayerStatus.QUEUE);
			player.getInventory().clear();
			Main.getInstance().getItemManager().giveLeaveItem(player, "Queue", true);
			player.sendMessage(ChatColor.GREEN + "You have been added to the " + ladder.getColor() + ladder.getName() + ChatColor.GREEN + " queue. Waiting for another player..");
			Main.getInstance().getInventoryManager().updateUnrankedInventory();
		}
		if (this.queue.size() >= 2) {
			UUID secondUUID = uuid;
			for (Map.Entry<UUID, Queue> map : this.queue.entrySet()) {
			    UUID key = map.getKey();
			    Queue value = map.getValue();
			    
			    if (uuid == key || ladder.getName() != value.getLadder().getName() || ranked != value.isRanked()) {
			    	continue;
			    }
			    secondUUID = key;
			}
			if (secondUUID == uuid) {
				return;
			}
			this.queue.remove(uuid);
			this.queue.remove(secondUUID);
			Main.getInstance().getDuelManager().startDuel(Arena.getInstance().getRandomArena(ladder == Ladders.SUMO), ladder, uuid, secondUUID, ranked);
			if (!ranked) {
				Main.getInstance().getInventoryManager().updateUnrankedInventory();
			} else {
				Main.getInstance().getInventoryManager().updateRankedInventory();
			}
		}
	}
	
	public void quitQueue(Player player) {
		if (!this.queue.containsKey(player.getUniqueId())) {
			return;
		}
		final boolean ranked = this.queue.get(player.getUniqueId()).isRanked();
		this.queue.remove(player.getUniqueId());
		PlayerManager.get(player.getUniqueId()).setStatus(PlayerStatus.SPAWN);
		Main.getInstance().getItemManager().giveSpawnItem(player);
		player.sendMessage(ChatColor.RED + "You have been removed from the queue.");
		if (!ranked) {
			Main.getInstance().getInventoryManager().updateUnrankedInventory();
		} else {
			Main.getInstance().getInventoryManager().updateRankedInventory();
		}
	}
	
	public int getQueuedFromLadder(Ladders ladder, boolean ranked) {
		int count = 0;
		for (Map.Entry<UUID, Queue> map : this.queue.entrySet()) {
			Queue value = map.getValue();
			if (value.getLadder() == ladder && value.isRanked() == ranked) {
				count++;
			}
		}
		return count;
	}
	
	public class Queue {
		private Ladders ladder;
		private boolean ranked;
		
		public Queue(Ladders ladder, boolean ranked) {
			this.ladder = ladder;
			this.ranked = ranked;
		}
		
		public Ladders getLadder() {
			return this.ladder;
		}
		
		public boolean isRanked() {
			return this.ranked;
		}
	}
}
