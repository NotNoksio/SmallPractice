package us.noks.smallpractice.objects;

import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import us.noks.smallpractice.arena.Arena.Arenas;
import us.noks.smallpractice.enums.Ladders;
import us.noks.smallpractice.enums.PlayerStatus;
import us.noks.smallpractice.objects.managers.PlayerManager;

public class Duel {
	private Arenas arena;
	private Ladders ladder;
	private UUID firstTeamPartyLeaderUUID;
    private UUID secondTeamPartyLeaderUUID;
	private List<UUID> firstTeam;
	private List<UUID> secondTeam;
	private List<UUID> firstTeamAlive;
	private List<UUID> secondTeamAlive;
	private UUID ffaPartyLeaderUUID;
    //private List<UUID> ffaPlayers;
    //private List<UUID> ffaAlivePlayers;
	private boolean ranked;
	private List<UUID> spectators = Lists.newArrayList();
	private int timeBeforeDuel = 5;
	private List<Item> drops;
	
	public Duel(Arenas arena, Ladders ladder, UUID firstTeamPartyLeaderUUID, UUID secondTeamPartyLeaderUUID, List<UUID> firstTeam, List<UUID> secondTeam, boolean ranked) {
		this.arena = arena;
		this.ladder = ladder;
		this.firstTeamPartyLeaderUUID = firstTeamPartyLeaderUUID;
		this.secondTeamPartyLeaderUUID = secondTeamPartyLeaderUUID;
		this.firstTeam = Lists.newArrayList(firstTeam);
		this.secondTeam = Lists.newArrayList(secondTeam);
		this.firstTeamAlive = Lists.newArrayList(firstTeam);
		this.secondTeamAlive = Lists.newArrayList(secondTeam);
		this.ranked = ranked;
		this.drops = Lists.newLinkedList();
	}
	
	/*public Duel(Arenas arena, Ladders ladder, UUID ffaPartyLeaderUUID, List<UUID> ffaPlayers) {
		this.arena = arena;
		this.ladder = ladder;
		this.ffaPartyLeaderUUID = ffaPartyLeaderUUID;
		this.ffaPlayers = Lists.newArrayList(ffaPlayers);
		this.ffaAlivePlayers = Lists.newArrayList(ffaPlayers);
		this.drops = Lists.newLinkedList();
		this.ranked = false;
    }*/
	
	public Arenas getArena() {
		return this.arena;
	}
	
	public Ladders getLadder() {
		return this.ladder;
	}
	
	public List<UUID> getFirstTeam() {
		return firstTeam;
	}
	
	public List<UUID> getSecondTeam() {
		return secondTeam;
	}
	
	public List<UUID> getAllTeams() {
		List<UUID> teams = Lists.newArrayList();
		if (!this.firstTeam.isEmpty()){
			teams.addAll(firstTeam);
		}
		if (!this.secondTeam.isEmpty()){
			teams.addAll(secondTeam);
		}
		/*if (!this.ffaPlayers.isEmpty()) {
			teams.addAll(ffaPlayers);
		}*/
		return teams;
	}
	
	public List<UUID> getAllAliveTeams() {
		List<UUID> teams = Lists.newArrayList();
		if (!this.firstTeamAlive.isEmpty()){
			teams.addAll(firstTeamAlive);
		}
		if (!this.secondTeamAlive.isEmpty()){
			teams.addAll(secondTeamAlive);
		}
		/*if (!this.ffaAlivePlayers.isEmpty()) {
			teams.addAll(ffaAlivePlayers);
		}*/
		return teams;
	}
	
	public List<UUID> getFirstTeamAlive() {
		return firstTeamAlive;
	}
	
	public List<UUID> getSecondTeamAlive() {
		return secondTeamAlive;
	}
	
	public void killPlayer(UUID killedUUID) {
		if (this.firstTeamAlive.contains(killedUUID)) {
			this.firstTeamAlive.remove(killedUUID);
			return;
		}
		if (this.secondTeamAlive.contains(killedUUID)) {
			this.secondTeamAlive.remove(killedUUID);
			//return;
		}
		//this.ffaAlivePlayers.remove(killedUUID);
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
		sendSoundedMessage(message, sound, 1.0f, 1.0f);
	}
	public void sendSoundedMessage(String message, Sound sound, float volume, float pitch) {
		List<UUID> duelPlayers = getAllTeams();
		if (!getAllSpectators().isEmpty()) duelPlayers.addAll(getAllSpectators());
		
		for (UUID uuid : duelPlayers) {
			final Player player = Bukkit.getPlayer(uuid);
			if (player == null) continue;
			
			player.sendMessage(message);
			if (sound != null) player.playSound(player.getLocation(), sound, volume, pitch);
		}
		duelPlayers.clear();
	}
	
	public void showDuelPlayer() {
		if (!isValid()) {
			return;
		}
		if (!firstTeamAlive.isEmpty() && !secondTeamAlive.isEmpty()) {
			for (UUID firstUUID : this.firstTeamAlive) {
				for (UUID secondUUID : this.secondTeamAlive) {
	                Player first = Bukkit.getPlayer(firstUUID);
	                Player second = Bukkit.getPlayer(secondUUID);
					first.showPlayer(second);
					second.showPlayer(first);
				}
			}
		}
		/*if (!ffaAlivePlayers.isEmpty()) {
			// TODO: see all players in a ffa fight
		}*/
	}
	
	public void switchFirstTeamPartyLeader(UUID newPartyLeaderUUID) {
		this.firstTeamPartyLeaderUUID = newPartyLeaderUUID;
	}
	
	public void switchSecondTeamPartyLeader(UUID newPartyLeaderUUID) {
		this.secondTeamPartyLeaderUUID = newPartyLeaderUUID;
	}

	public UUID getFirstTeamPartyLeaderUUID() {
		return firstTeamPartyLeaderUUID;
	}

	public UUID getSecondTeamPartyLeaderUUID() {
		return secondTeamPartyLeaderUUID;
	}
	
	public UUID getFfaPartyLeaderUUID() {
        return this.ffaPartyLeaderUUID;
    }
    
    /*public List<UUID> getFfaPlayers() {
        return this.ffaPlayers;
    }
    
    public List<UUID> getFfaAlivePlayers() {
        return this.ffaAlivePlayers;
    }*/
	
	public boolean containPlayer(Player player) {
		Preconditions.checkNotNull(player, "Player cannot be null");
		return (this.firstTeam.contains(player.getUniqueId()) || this.secondTeam.contains(player.getUniqueId()));
	}
	
	public boolean isValid() {
		return (!this.firstTeam.isEmpty() && !this.secondTeam.isEmpty() && !this.firstTeamAlive.isEmpty() && !this.secondTeamAlive.isEmpty());
	}
	
	public void setDuelPlayersStatusTo(PlayerStatus status) {
		for (UUID playersUUID : getAllTeams()) {
			PlayerManager.get(playersUUID).setStatus(status);
		}
	}
	
	public int getTimeBeforeDuel() {
		return this.timeBeforeDuel;
	}
	
	public void addDrops(Item item) {
		Bukkit.broadcastMessage(item.toString() + " has been added to match Drops"); // DEBUG
		this.drops.add(item);
	}
	
	public boolean containDrops(Item item) {
		return this.drops.contains(item);
	}
	
	public void clearDrops() {
		// TODO: TRY TO MAKE THIS WORK!!!
		for (Item itemsOnTheGround : this.drops) {
			itemsOnTheGround.remove();
		}
	}
}
