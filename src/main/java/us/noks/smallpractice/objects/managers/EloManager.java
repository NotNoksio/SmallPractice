package us.noks.smallpractice.objects.managers;

import java.util.Map;

import com.google.common.collect.Maps;

import us.noks.smallpractice.enums.Ladders;

public class EloManager {
	private Map<Ladders, Integer> laddersElo = Maps.newHashMap();
	private int DEFAULT_ELO = 1000;
	
	public int getElo(Ladders ladder) {
		if (!laddersElo.containsKey(ladder)) laddersElo.put(ladder, DEFAULT_ELO);
		return laddersElo.get(ladder);
	}
	
	public void addElo(Ladders ladder, int amount) {
		final int currentElo = getElo(ladder);
		this.laddersElo.put(ladder, currentElo + amount);
	}
	
	public void removeElo(Ladders ladder, int amount) {
		final int currentElo = getElo(ladder);
		this.laddersElo.put(ladder, currentElo - amount);
	}
}
