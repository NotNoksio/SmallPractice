package us.noks.smallpractice.objects;

import java.util.Iterator;
import java.util.List;

import org.bukkit.Sound;
import org.bukkit.entity.Player;

import com.google.common.collect.Lists;

public class Duel {
	
	private Player firstPlayer;
	private Player secondPlayer;
	private boolean ranked;
	private List<Player> spectators = Lists.newArrayList();
	
	public Duel(Player firstPlayer, Player secondPlayer) {
		this.firstPlayer = firstPlayer;
		this.secondPlayer = secondPlayer;
		this.ranked = false;
	}
	
	public Duel(Player firstPlayer, Player secondPlayer, boolean ranked) {
		this.firstPlayer = firstPlayer;
		this.secondPlayer = secondPlayer;
		this.ranked = ranked;
	}
	
	public Player getFirstPlayer() {
		return firstPlayer;
	}
	
	public Player getSecondPlayer() {
		return secondPlayer;
	}

	public boolean isRanked() {
		return ranked;
	}

	public void setRanked(boolean ranked) {
		this.ranked = ranked;
	}
	
	public void addSpectator(Player spec) {
		this.spectators.add(spec);
	}
	
	public void removeSpectator(Player spec) {
		this.spectators.remove(spec);
	}
	
	public boolean hasSpectator() {
		return !this.spectators.isEmpty();
	}
	
	public List<Player> getAllSpectators() {
		return this.spectators;
	}
	
	public void sendMessage(String message) {
		sendSoundedMessage(message, null);
	}
	
	public void sendSoundedMessage(String message, Sound sound) {
		getFirstPlayer().sendMessage(message);
		getSecondPlayer().sendMessage(message);
		if (sound != null) {
			getFirstPlayer().playSound(getFirstPlayer().getLocation(), sound, 1.0f, 1.0f);
			getSecondPlayer().playSound(getSecondPlayer().getLocation(), sound, 1.0f, 1.0f);
		}
		
		Iterator<Player> iterator = getAllSpectators().iterator();
		while (iterator.hasNext()) {
			Player spec = iterator.next();
			spec.sendMessage(message);
		}
	}
}
