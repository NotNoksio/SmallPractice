package io.noks.smallpractice.objects;

public class PlayerSettings {
	private int queuePingDiff;
	
	public PlayerSettings() {
		this.queuePingDiff = 300;
	}
	
	public int getQueuePingDiff() {
		return this.queuePingDiff;
	}
	
	public void updatePingDiff() {
		if (this.queuePingDiff == 300) {
			this.queuePingDiff = 50;
			return;
		}
		this.queuePingDiff += 50;
	}
}
