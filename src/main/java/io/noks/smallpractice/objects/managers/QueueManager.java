package io.noks.smallpractice.objects.managers;

import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import io.noks.smallpractice.Main;
import io.noks.smallpractice.arena.Arena;
import io.noks.smallpractice.enums.Ladders;
import io.noks.smallpractice.enums.PlayerStatus;
import io.noks.smallpractice.objects.Queue;
import io.noks.smallpractice.party.Party;
import io.noks.smallpractice.utils.WeakHashSet;
import net.minecraft.util.com.google.common.collect.Maps;

public class QueueManager {
	private Map<UUID, Queue> queue = Maps.newConcurrentMap();
	public Map<UUID, Queue> getQueueMap() {
		return this.queue;
	}
	
	public void addToQueue(UUID uuid, Ladders ladder, boolean ranked, boolean to2, int pingDiffParam) {
		final Party party = (to2 ? Main.getInstance().getPartyManager().getParty(uuid) : null);
		final PlayerManager pm = PlayerManager.get(uuid);
		if (!this.queue.containsKey(uuid)) {
			final Player player = pm.getPlayer();
			this.queue.put(uuid, new Queue(ladder, ranked, to2, pingDiffParam));
			pm.setStatus(PlayerStatus.QUEUE);
			player.getInventory().clear();
			Main.getInstance().getItemManager().giveLeaveItem(player, "Queue", true);
			if (!to2) {
				player.sendMessage(ChatColor.GREEN + "You have been added to the " + (ranked ? "Ranked " : "Unranked ") + ladder.getColor() + ladder.getName() + ChatColor.GREEN + " queue. Waiting for another player..");
			} else {
				party.notify(ChatColor.GREEN + "Your party has been added to the " + (ranked ? "Ranked " : "Unranked ") + ladder.getColor() + ladder.getName() + ChatColor.GREEN + " 2v2 queue. Waiting for another party..");
			}
			Main.getInstance().getInventoryManager().updateQueueInventory(ranked);
		}
		if (!this.is2PlayersOrMore(ladder, ranked, to2)) {
			return;
		}
		UUID secondUUID = null;
		for (UUID potentialUUID : this.queue.keySet()) {
			if (uuid == potentialUUID || Math.abs(pm.getPlayer().getPing() - Bukkit.getPlayer(potentialUUID).getPing()) > pingDiffParam) {
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
		Main.getInstance().getInventoryManager().updateQueueInventory(ranked);
		if (to2) {
			Main.getInstance().getDuelManager().startDuel(Arena.getInstance().getRandomArena(ladder), ladder, uuid, secondUUID, party.getMembersIncludingLeader(), Main.getInstance().getPartyManager().getParty(secondUUID).getMembersIncludingLeader(), ranked);
			return;
		}
		Main.getInstance().getDuelManager().startDuel(Arena.getInstance().getRandomArena(ladder), ladder, uuid, secondUUID, ranked);
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
			Bukkit.getPlayer(uuid).sendMessage(ChatColor.DARK_AQUA + "Ping Difference: " + ChatColor.YELLOW + oldPingDiff + ChatColor.DARK_AQUA + " -> " + ChatColor.YELLOW + queue.getPingDiff());
			addToQueue(queues.getKey(), queue.getLadder(), queue.isRanked(), queue.isTO2(), queue.getPingDiff());
		}
	} 
	
	private boolean is2PlayersOrMore(Ladders ladder, boolean ranked, boolean to2) {
		if (this.queue.isEmpty() || this.queue.size() == 1) {
			return false;
		}
		for (Queue queue : this.queue.values()) {
			if (ladder.getName() == queue.getLadder().getName() && ranked == queue.isRanked() && to2 == queue.isTO2()) {
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
		Main.getInstance().getItemManager().giveSpawnItem(player);
		player.sendMessage(ChatColor.RED + "You have been removed from the queue.");
		Main.getInstance().getInventoryManager().updateQueueInventory(ranked);
	}
	
	public int getQueuedFromLadder(Ladders ladder, boolean ranked) {
		int count = 0;
		for (Map.Entry<UUID, Queue> map : this.queue.entrySet()) {
			final Queue value = map.getValue();
			if (value.getLadder() == ladder && value.isRanked() == ranked) {
				count++;
			}
		}
		return count;
	}
}
