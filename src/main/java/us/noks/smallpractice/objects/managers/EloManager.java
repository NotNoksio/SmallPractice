package us.noks.smallpractice.objects.managers;

import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.google.common.collect.Maps;

public class EloManager {
	private Map<UUID, Integer> playersElo = Maps.newHashMap();
	private int DEFAULT_ELO = 1000;
	
	public void tranferElo(UUID winnerUUID, UUID loserUUID) {
		final double expectedp = 1.0D / (1.0D + Math.pow(10.0D, (PlayerManager.get(winnerUUID).getElo() - PlayerManager.get(loserUUID).getElo()) / 400.0D));
		final int scoreChange = this.limit((expectedp * 32.0D), 4, 25);
		
		PlayerManager.get(winnerUUID).addElo(scoreChange);
		PlayerManager.get(loserUUID).removeElo(scoreChange);
		
		updatePlayerElo(winnerUUID, loserUUID);
		
		final Player winner = Bukkit.getPlayer(winnerUUID);
		final Player loser = Bukkit.getPlayer(loserUUID);

		final String eloMessage = ChatColor.GOLD + "Elo Changes: " + ChatColor.GREEN + winner.getName() + " (+" + scoreChange + ") " + ChatColor.RED + loser.getName() + " (-" + scoreChange + ")";
		winner.sendMessage(eloMessage);
		loser.sendMessage(eloMessage);
	}
	
	public int getPlayerElo(UUID uuid) {
		if (!playersElo.containsKey(uuid)) playersElo.put(uuid, DEFAULT_ELO);
		return playersElo.get(uuid);
	}
	
	private void updatePlayerElo(UUID... uuid) {
		for (UUID uuids : uuid) {
			playersElo.put(uuids, PlayerManager.get(uuids).getElo());
		}
	}
	
	private int limit(double actual, int min, int max) {
		return (int) Math.min(Math.max(actual, min), max);
	}
}
