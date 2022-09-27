package io.noks.smallpractice.objects;

public class MatchStats {
	private int failedPotions = 0;
	private int lastFailedPotions = 0;
	private int hit = 0;
	private int combo = 0;
	private int longestCombo = 0;
	private Long enderpearlCooldown = 0L;
	private Long nextHitTick = 0L;
	
	public int getFailedPotions() {
		return failedPotions;
	}
	
	public void setFailedPotions(int pots) {
		this.failedPotions = pots;
	}
	
	public int getLastFailedPotions() {
		return lastFailedPotions;
	}
	
	public void setLastFailedPotions(int pots) {
		this.lastFailedPotions = pots;
	}

	public int getHit() {
		return hit;
	}

	public void setHit(int hit) {
		this.hit = hit;
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
	
	public Long getNextHitTick() {
		return this.nextHitTick;
	}
	
	public void updateNextHitTick() {
		this.nextHitTick = System.currentTimeMillis() + 450;
	}
	
	public void resetDuelStats() {
		this.failedPotions = 0;
		this.hit = 0;
		this.combo = 0;
		this.longestCombo = 0;
		this.removeEnderPearlCooldown();
	}
}
