package io.noks.smallpractice.objects.managers;

import java.util.Map;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import io.noks.smallpractice.Main;
import io.noks.smallpractice.enums.Ladders;
import io.noks.smallpractice.enums.PlayerStatus;
import io.noks.smallpractice.objects.Queue;
import io.noks.smallpractice.party.Party;
import io.noks.smallpractice.utils.WeakHashSet;
import net.minecraft.util.com.google.common.collect.Maps;

public class QueueManager {
	private Map<UUID, Queue> queue;
	public Map<UUID, Queue> getQueueMap() {
		return this.queue;
	}
	
	private Main main;
	public QueueManager(Main main) {
		this.main = main;
		this.queue = Maps.newConcurrentMap();
	}
	
	public void addToQueue(UUID uuid, Ladders ladder, boolean ranked, boolean to2, int pingDiffParam) {
		final Party party = (to2 ? this.main.getPartyManager().getParty(uuid) : null);
		final PlayerManager pm = PlayerManager.get(uuid);
		if (!this.queue.containsKey(uuid)) {
			final Player player = pm.getPlayer();
			this.queue.put(uuid, new Queue(ladder, ranked, to2, pingDiffParam));
			pm.setStatus(PlayerStatus.QUEUE);
			player.getInventory().clear();
			this.main.getItemManager().giveLeaveItem(player, "Queue", true);
			if (!to2) {
				player.sendMessage(ChatColor.GREEN + "You have been added to the " + (ranked ? "Ranked " : "Unranked ") + ladder.getColor() + ladder.getName() + ChatColor.GREEN + " queue. Waiting for another player..");
			} else {
				party.notify(ChatColor.GREEN + "Your party has been added to the " + (ranked ? "Ranked " : "Unranked ") + ladder.getColor() + ladder.getName() + ChatColor.GREEN + " 2v2 queue. Waiting for another party..");
			}
			this.main.getInventoryManager().updateQueueInventory(ranked);
		}
		if (!this.is2PlayersOrMore(ladder, ranked, to2)) {
			return;
		}
		UUID secondUUID = null;
		for (Map.Entry<UUID, Queue> potent : this.queue.entrySet()) {
			final UUID potentialUUID = potent.getKey();
			// Dont init variable if not needed!
			if (uuid == potentialUUID || Math.abs(pm.getPlayer().getPing() - this.main.getServer().getPlayer(potentialUUID).getPing()) > pingDiffParam) { 
				continue;
			}
			final Queue queue = potent.getValue();
			
			if (queue.isRanked() != ranked || queue.getLadder() != ladder || queue.isTO2() != to2) {
				continue;
			}
			secondUUID = potentialUUID;
			break;
		}
		if (secondUUID == null) {
			return;
		}
		this.queue.remove(uuid);
		this.queue.remove(secondUUID);
		if (lastUpdated.contains(uuid)) {
			lastUpdated.remove(uuid);
		}
		if (lastUpdated.contains(secondUUID)) {
			lastUpdated.remove(secondUUID);
		}
		this.updatePingDiffFromQueue();
		if (ranked) {
			// TODO: update elo range
		}
		if (to2) {
			this.main.getDuelManager().startDuel(this.main.getArenaManager().getRandomArena(ladder), ladder, uuid, secondUUID, party.getMembersIncludingLeader(), this.main.getPartyManager().getParty(secondUUID).getMembersIncludingLeader(), ranked);
			return;
		}
		this.main.getDuelManager().startDuel(this.main.getArenaManager().getRandomArena(ladder), ladder, uuid, secondUUID, ranked);
	}
	
	private WeakHashSet<UUID> lastUpdated = new WeakHashSet<UUID>(); // DONT SPAM QUEUE!!
	public WeakHashSet<UUID> getLastUpdatedSet(){ return this.lastUpdated; }
	private void updatePingDiffFromQueue() {
		if (this.queue.isEmpty() || this.queue.size() == 1) {
			return;
		}
		for (Map.Entry<UUID, Queue> queues : this.queue.entrySet()) {
			final Queue queue = queues.getValue();
			if (queue.getPingDiff() == 300) {
				continue;
			}
			final UUID uuid = queues.getKey();
			if (lastUpdated.contains(uuid)) {
				lastUpdated.remove(uuid);
				continue;
			}
			lastUpdated.add(uuid);
			final int oldPingDiff = queue.getPingDiff();
			queue.updatePingDiff();
			this.main.getServer().getPlayer(uuid).sendMessage(ChatColor.DARK_AQUA + "Ping Difference: " + ChatColor.YELLOW + oldPingDiff + ChatColor.DARK_AQUA + " -> " + ChatColor.YELLOW + queue.getPingDiff());
			addToQueue(queues.getKey(), queue.getLadder(), queue.isRanked(), queue.isTO2(), queue.getPingDiff());
		}
	} 
	
	private boolean is2PlayersOrMore(Ladders ladder, boolean ranked, boolean to2) {
		if (this.queue.isEmpty() || this.queue.size() == 1) {
			return false;
		}
		for (Queue queue : this.queue.values()) {
			if (ladder == queue.getLadder() && ranked == queue.isRanked() && to2 == queue.isTO2()) {
				return true;
			}
		}
		return false;
	}
	
	public void quitQueue(Player player) {
		if (!this.queue.containsKey(player.getUniqueId())) {
			return;
		}
		final boolean ranked = this.queue.get(player.getUniqueId()).isRanked();
		this.queue.remove(player.getUniqueId());
		PlayerManager.get(player.getUniqueId()).setStatus(PlayerStatus.SPAWN);
		this.main.getItemManager().giveSpawnItem(player);
		player.sendMessage(ChatColor.RED + "You have been removed from the queue.");
		this.main.getInventoryManager().updateQueueInventory(ranked);
	}
	
	public int getQueuedFromLadder(Ladders ladder, boolean ranked) {
		int count = 0;
		for (Queue queues : this.queue.values()) {
			if (queues.getLadder() == ladder && queues.isRanked() == ranked) {
				count++;
			}
		}
		return count;
	}
}
