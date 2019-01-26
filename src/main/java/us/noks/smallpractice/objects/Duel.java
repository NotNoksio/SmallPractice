package us.noks.smallpractice.objects;

import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import com.google.common.collect.Lists;

public class Duel {
	
	private UUID firstPlayerUUID;
	private UUID secondPlayerUUID;
	private boolean ranked;
	private List<UUID> spectators = Lists.newArrayList();
	
	public Duel(UUID firstPlayer, UUID secondPlayer) {
		this.firstPlayerUUID = firstPlayer;
		this.secondPlayerUUID = secondPlayer;
		this.ranked = false;
	}
	
	public Duel(UUID firstPlayer, UUID secondPlayer, boolean ranked) {
		this.firstPlayerUUID = firstPlayer;
		this.secondPlayerUUID = secondPlayer;
		this.ranked = ranked;
	}
	
	public UUID getFirstPlayerUUID() {
		return firstPlayerUUID;
	}
	
	public UUID getSecondPlayerUUID() {
		return secondPlayerUUID;
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
		Player firstPlayer = Bukkit.getPlayer(getFirstPlayerUUID());
		Player secondPlayer = Bukkit.getPlayer(getSecondPlayerUUID());
		firstPlayer.sendMessage(message);
		secondPlayer.sendMessage(message);
		if (sound != null) {
			firstPlayer.playSound(firstPlayer.getLocation(), sound, 1.0f, 1.0f);
			secondPlayer.playSound(secondPlayer.getLocation(), sound, 1.0f, 1.0f);
		}
		
		Iterator<UUID> iterator = getAllSpectatorsUUID().iterator();
		while (iterator.hasNext()) {
			Player spec = Bukkit.getPlayer(iterator.next());
			spec.sendMessage(message);
		}
	}
}
