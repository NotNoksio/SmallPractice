package io.noks.smallpractice.objects;

import io.noks.smallpractice.enums.Ladders;

public class Queue {
	private Ladders ladder;
	private boolean ranked;
	private boolean teamOf2;
	private int pingDiff;
		
	public Queue(Ladders ladder, boolean ranked, boolean to2, int pingDiff) {
		this.ladder = ladder;
		this.ranked = ranked;
		this.teamOf2 = to2;
		this.pingDiff = pingDiff;
	}
		
	public Ladders getLadder() {
		return this.ladder;
	}
		
	public boolean isRanked() {
		return this.ranked;
	}
		
	public boolean isTO2() {
		return this.teamOf2;
	}
	
	public int getPingDiff() {
		return this.pingDiff;
	}
	
	public void updatePingDiff() {
		this.pingDiff += 50;
	}
}
