package io.noks.smallpractice.objects;

import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

import net.minecraft.util.com.google.common.collect.Maps;

public class MatchStats {
	private int failedPotions = 0;
	private Map<UUID, Integer> hit = Maps.newConcurrentMap();
	private int combo = 0;
	private int longestCombo = 0;
	private Long enderpearlCooldown = 0L;
	private Map<UUID, Long> nextHitTick = new WeakHashMap<UUID, Long>();
	
	public int getFailedPotions() {
		return failedPotions;
	}
	
	public void addFailedPotions() {
		this.failedPotions++;
	}
	
	public int getTotalHit() {
		int hits = 0;
		for (Integer i : hit.values()) {
			hits += i;
		}
		return hits;
	}

	public int getHit(UUID uuid) {
		return hit.get(uuid);
	}
	
	public void addHit(UUID uuid) {
		if (!this.hit.containsKey(uuid)) {
			this.hit.put(uuid, 1);
			return;
		}
		final int currentHit = this.hit.get(uuid);
		this.hit.put(uuid, currentHit + 1);
	}

	public int getCombo() {
		return combo;
	}

	public void setCombo(int combo) {
		this.combo = combo;
	}

	public int getLongestCombo() {
		return longestCombo;
	}

	public void setLongestCombo(int longestCombo) {
		this.longestCombo = longestCombo;
	}
	
	public boolean isEnderPearlCooldownActive() {
		return this.enderpearlCooldown > System.currentTimeMillis();
	}

	public long getEnderPearlCooldown() {
		return Math.max(0L, this.enderpearlCooldown - System.currentTimeMillis());
	}

	public void applyEnderPearlCooldown() {
		this.enderpearlCooldown = Long.valueOf(System.currentTimeMillis() + 14 * 1000);
	}

	public void removeEnderPearlCooldown() {
		this.enderpearlCooldown = 0L;
	}
	
	public Long getNextHitTick(UUID uuid) {
		return this.nextHitTick.get(uuid);
	}
	
	public boolean containsNextHitUUID(UUID uuid) {
		return this.nextHitTick.containsKey(uuid);
	}
	
	public void updateNextHitTick(UUID uuid) {
		this.nextHitTick.put(uuid, System.currentTimeMillis() + 450);
	}
	
	public void resetDuelStats() {
		this.failedPotions = 0;
		this.hit.clear();
		this.combo = 0;
		this.longestCombo = 0;
		this.nextHitTick.clear();
		this.removeEnderPearlCooldown();
	}
}
