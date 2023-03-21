package io.noks.smallpractice.objects.managers;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;

import io.noks.smallpractice.enums.Ladders;

public class EloManager {
	private Map<Ladders, Integer> laddersElo = Maps.newHashMap();
	private int DEFAULT_ELO;
	private int winnedUnranked;
	
	public EloManager() {
		this.DEFAULT_ELO = 1200;
		this.winnedUnranked = 0;
	}
	public EloManager(List<Integer> elo) {
		int i = 0;
		for (Ladders ladders : Ladders.values()) {
			laddersElo.put(ladders, elo.get(i));
			i++;
		}
		this.winnedUnranked = 10;
	}
	public EloManager(List<Integer> elo, int winnedUnranked) {
		int i = 0;
		for (Ladders ladders : Ladders.values()) {
			laddersElo.put(ladders, elo.get(i));
			i++;
		}
		this.winnedUnranked = winnedUnranked;
	}
	
	public int getFrom(Ladders ladder) {
		if (!laddersElo.containsKey(ladder)) laddersElo.put(ladder, DEFAULT_ELO);
		return laddersElo.get(ladder);
	}
	
	public void addTo(Ladders ladder, int amount) {
		final int currentElo = getFrom(ladder);
		this.laddersElo.put(ladder, currentElo + amount);
	}
	
	public void removeFrom(Ladders ladder, int amount) {
		final int currentElo = getFrom(ladder);
		this.laddersElo.put(ladder, currentElo - amount);
	}
	
	public int getGlobal() {
		int global = 0;
		for (Ladders ladders : Ladders.values()) {
			global += getFrom(ladders);
		}
		return (global / Ladders.values().length);
	}
	
	public void addUnrankedWinned() {
		if (this.winnedUnranked >= 10) {
			return;
		}
		this.winnedUnranked++;
	}
	
	public boolean canAccessRanked() {
		return this.winnedUnranked >= 10;
	}
	
	public int getWinnedUnranked() {
		return this.winnedUnranked;
	}
}
