package us.noks.smallpractice.objects;

import java.util.List;

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
}
