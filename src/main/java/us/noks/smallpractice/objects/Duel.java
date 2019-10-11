package us.noks.smallpractice.objects;

import java.util.List;
import java.util.UUID;
import java.util.ArrayList;
import java.util.Iterator;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import us.noks.smallpractice.enums.PlayerStatus;
import us.noks.smallpractice.objects.managers.PlayerManager;

public class Duel {
	private UUID firstTeamPartyLeaderUUID;
    private UUID secondTeamPartyLeaderUUID;
	private List<UUID> firstTeam;
	private List<UUID> secondTeam;
	private List<UUID> firstTeamAlive;
	private List<UUID> secondTeamAlive;
	private boolean ranked;
	private List<UUID> spectators = Lists.newArrayList();
	private int timeBeforeDuel = 5;
	
	public Duel(UUID firstTeamPartyLeaderUUID, UUID secondTeamPartyLeaderUUID, List<UUID> firstTeam, List<UUID> secondTeam, boolean ranked) {
		this.firstTeamPartyLeaderUUID = firstTeamPartyLeaderUUID;
		this.secondTeamPartyLeaderUUID = secondTeamPartyLeaderUUID;
		this.firstTeam = Lists.newArrayList(firstTeam);
		this.secondTeam = Lists.newArrayList(secondTeam);
		this.firstTeamAlive = Lists.newArrayList(firstTeam);
		this.secondTeamAlive = Lists.newArrayList(secondTeam);
		this.ranked = ranked;
	}
	
	public List<UUID> getFirstTeam() {
		return firstTeam;
	}
	
	public List<UUID> getSecondTeam() {
		return secondTeam;
	}
	
	public List<UUID> getFirstAndSecondTeams() {
		List<UUID> teams = Lists.newArrayList(this.firstTeam);
		teams.addAll(this.secondTeam);
		return teams;
	}
	
	public List<UUID> getFirstTeamAlive() {
		return firstTeamAlive;
	}
	
	public List<UUID> getSecondTeamAlive() {
		return secondTeamAlive;
	}
	
	public void killFirstTeamPlayer(UUID killedUUID) {
		this.firstTeamAlive.remove(killedUUID);
	}
	
	public void killSecondTeamPlayer(UUID killedUUID) {
		this.secondTeamAlive.remove(killedUUID);
	}

	public boolean isRanked() {
		return ranked;
	}

	public void setRanked(boolean ranked) {
		this.ranked = ranked;
	}
	
	public void addSpectator(UUID spec) {
		this.spectators.add(spec);
	}
	
	public void removeSpectator(UUID spec) {
		this.spectators.remove(spec);
	}
	
	public boolean hasSpectator() {
		return !this.spectators.isEmpty();
	}
	
	public List<UUID> getAllSpectators() {
		return this.spectators;
	}
	
	public void sendMessage(String message) {
		sendSoundedMessage(message, null);
	}
	
	public void sendSoundedMessage(String message, Sound sound) {
		List<UUID> duelPlayers = Lists.newArrayList(getFirstAndSecondTeams());
		duelPlayers.addAll(getAllSpectators());
		
		for (UUID uuid : duelPlayers) {
			final Player player = Bukkit.getPlayer(uuid);
			if (player == null) continue;
			
			player.sendMessage(message);
			if (sound != null) player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
		}
		duelPlayers.clear();
	}
	
	public void showDuelPlayer() {
		if (!isValid()) {
			return;
		}
		List<Player> firstTeamPlayer = Lists.newArrayList();
		List<Player> secondTeamPlayer = Lists.newArrayList();
		for (UUID firstTeamUUID : this.firstTeamAlive) {
			final Player first = Bukkit.getPlayer(firstTeamUUID);
			if (first == null) continue;
			firstTeamPlayer.add(first);
		}
		for (UUID secondTeamUUID : this.secondTeamAlive) {
			final Player second = Bukkit.getPlayer(secondTeamUUID);
			if (second == null) continue;
			secondTeamPlayer.add(second);
		}
        Iterator<Player> it = new ArrayList<>(secondTeamPlayer).iterator();
		for (Player first : firstTeamPlayer) {
			if (!first.canSee(first)) first.showPlayer(first);
			while (it.hasNext()) {
                Player second = it.next();
				if (!second.canSee(second)) second.showPlayer(second);
				if (!first.canSee(second)) first.showPlayer(second);
				if (!second.canSee(first)) second.showPlayer(first);
			}
		}
		firstTeamPlayer.clear();
		secondTeamPlayer.clear();
	}

	public UUID getFirstTeamPartyLeaderUUID() {
		return firstTeamPartyLeaderUUID;
	}

	public void setFirstTeamPartyLeaderUUID(UUID firstTeamPartyLeaderUUID) {
		this.firstTeamPartyLeaderUUID = firstTeamPartyLeaderUUID;
	}

	public UUID getSecondTeamPartyLeaderUUID() {
		return secondTeamPartyLeaderUUID;
	}

	public void setSecondTeamPartyLeaderUUID(UUID secondTeamPartyLeaderUUID) {
		this.secondTeamPartyLeaderUUID = secondTeamPartyLeaderUUID;
	}
	
	public boolean containPlayer(Player player) {
		Preconditions.checkNotNull(player, "Player cannot be null");
		return (this.firstTeam.contains(player.getUniqueId()) || this.secondTeam.contains(player.getUniqueId()));
	}
	
	public boolean isValid() {
		return (!this.firstTeam.isEmpty() && !this.secondTeam.isEmpty() && !this.firstTeamAlive.isEmpty() && !this.secondTeamAlive.isEmpty());
	}
	
	public void setDuelPlayersStatusTo(PlayerStatus status) {
		getFirstAndSecondTeams().forEach(playersUUID -> {
			PlayerManager.get(playersUUID).setStatus(status);
		});
	}
	
	public int getTimeBeforeDuel() {
		return this.timeBeforeDuel;
	}
}
