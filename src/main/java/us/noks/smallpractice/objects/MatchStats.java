package us.noks.smallpractice.objects;

public class MatchStats {
	private byte failedPotions = 0;
	private byte lastFailedPotions = 0;
	private int hit = 0;
	private int combo = 0;
	private int longestCombo = 0;
	
	public byte getFailedPotions() {
		return failedPotions;
	}
	
	public void setFailedPotions(byte pots) {
		this.failedPotions = pots;
	}
	
	public byte getLastFailedPotions() {
		return lastFailedPotions;
	}
	
	public void setLastFailedPotions(byte pots) {
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
	
	public void resetDuelStats() {
		this.failedPotions = 0;
		this.hit = 0;
		this.combo = 0;
		this.longestCombo = 0;
	}
}
