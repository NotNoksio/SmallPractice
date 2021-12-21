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
import us.noks.smallpractice.party.Party;

public class QueueManager {
	private Map<UUID, Queue> queue = Maps.newConcurrentMap();
	public Map<UUID, Queue> getQueueMap() {
		return this.queue;
	}
	
	public void addToQueue(UUID uuid, Ladders ladder, boolean ranked, boolean to2) { // TODO: ping detector due to 20ms vs 220ms
		Party party = (to2 ? party = Main.getInstance().getPartyManager().getParty(uuid) : null);
		if (!this.queue.containsKey(uuid)) {
			final PlayerManager pm = PlayerManager.get(uuid);
			final Player player = pm.getPlayer();
			this.queue.put(uuid, new Queue(ladder, ranked, to2));
			pm.setStatus(PlayerStatus.QUEUE);
			player.getInventory().clear();
			Main.getInstance().getItemManager().giveLeaveItem(player, "Queue", true);
			if (!to2) {
				player.sendMessage(ChatColor.GREEN + "You have been added to the " + ladder.getColor() + ladder.getName() + ChatColor.GREEN + " queue. Waiting for another player..");
			} else {
				party.notify(ChatColor.GREEN + "Your party has been added to the " + ladder.getColor() + ladder.getName() + ChatColor.GREEN + " 2v2 queue. Waiting for another party..");
			}
			Main.getInstance().getInventoryManager().updateQueueInventory(ranked);
		}
		if (this.queue.size() >= 2) {
			UUID secondUUID = uuid;
			for (Map.Entry<UUID, Queue> map : this.queue.entrySet()) {
			    UUID key = map.getKey();
			    Queue value = map.getValue();
			    
			    if (uuid == key || ladder.getName() != value.getLadder().getName() || ranked != value.isRanked() || to2 != value.isTeamOf2()) {
			    	continue;
			    }
			    secondUUID = key;
			}
			if (secondUUID == uuid) {
				return;
			}
			this.queue.remove(uuid);
			this.queue.remove(secondUUID);
			if (!to2) {
				Main.getInstance().getDuelManager().startDuel(Arena.getInstance().getRandomArena(ladder == Ladders.SUMO), ladder, uuid, secondUUID, ranked);
			} else {
				Main.getInstance().getDuelManager().startDuel(Arena.getInstance().getRandomArena(ladder == Ladders.SUMO), ladder, uuid, secondUUID, party.getMembersIncludeLeader(), Main.getInstance().getPartyManager().getParty(secondUUID).getMembersIncludeLeader(), ranked);
			}
			Main.getInstance().getInventoryManager().updateQueueInventory(ranked);
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
		Main.getInstance().getInventoryManager().updateQueueInventory(ranked);
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
		private boolean teamOf2;
		
		public Queue(Ladders ladder, boolean ranked, boolean to2) {
			this.ladder = ladder;
			this.ranked = ranked;
			this.teamOf2 = to2;
		}
		
		public Ladders getLadder() {
			return this.ladder;
		}
		
		public boolean isRanked() {
			return this.ranked;
		}
		
		public boolean isTeamOf2() {
			return this.teamOf2;
		}
	}
}
