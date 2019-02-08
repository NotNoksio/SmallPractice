package us.noks.smallpractice.objects;

import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import com.google.common.collect.Lists;

public class Duel {
	
	private UUID firstTeamPartyLeaderUUID;
    private UUID secondTeamPartyLeaderUUID;
	private List<UUID> firstTeamUUID;
	private List<UUID> secondTeamUUID;
	private List<UUID> firstTeamAlive;
	private List<UUID> secondTeamAlive;
	private boolean ranked;
	private List<UUID> spectators = Lists.newArrayList();
	
	public Duel(UUID firstTeamPartyLeaderUUID, UUID secondTeamPartyLeaderUUID, List<UUID> firstTeamUUID, List<UUID> secondTeamUUID) {
		this.firstTeamPartyLeaderUUID = firstTeamPartyLeaderUUID;
		this.secondTeamPartyLeaderUUID = secondTeamPartyLeaderUUID;
		this.firstTeamUUID = firstTeamUUID;
		this.secondTeamUUID = secondTeamUUID;
		this.firstTeamAlive = Lists.newArrayList(firstTeamUUID);
		this.secondTeamAlive = Lists.newArrayList(secondTeamUUID);
		this.ranked = false;
	}
	
	public Duel(UUID firstTeamPartyLeaderUUID, UUID secondTeamPartyLeaderUUID, List<UUID> firstTeamUUID, List<UUID> secondTeamUUID, boolean ranked) {
		this.firstTeamPartyLeaderUUID = firstTeamPartyLeaderUUID;
		this.secondTeamPartyLeaderUUID = secondTeamPartyLeaderUUID;
		this.firstTeamUUID = firstTeamUUID;
		this.secondTeamUUID = secondTeamUUID;
		this.firstTeamAlive = Lists.newArrayList(firstTeamUUID);
		this.secondTeamAlive = Lists.newArrayList(secondTeamUUID);
		this.ranked = ranked;
	}
	
	public List<UUID> getFirstTeamUUID() {
		return firstTeamUUID;
	}
	
	public List<UUID> getSecondTeamUUID() {
		return secondTeamUUID;
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
	
	public List<UUID> getAllSpectatorsUUID() {
		return this.spectators;
	}
	
	public void sendMessage(String message) {
		sendSoundedMessage(message, null);
	}
	
	public void sendSoundedMessage(String message, Sound sound) {
		List<UUID> duelPlayers = Lists.newArrayList(getFirstTeamUUID());
		duelPlayers.addAll(getSecondTeamUUID());
		
		for (UUID uuid : duelPlayers) {
			Player player = Bukkit.getPlayer(uuid);
			
			if (player == null) {
                continue;
            }
			
			player.sendMessage(message);
			if (sound != null) {
				player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
			}
		}
		duelPlayers.clear();
		Iterator<UUID> iterator = getAllSpectatorsUUID().iterator();
		while (iterator.hasNext()) {
			Player spec = Bukkit.getPlayer(iterator.next());
			spec.sendMessage(message);
		}
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
}
