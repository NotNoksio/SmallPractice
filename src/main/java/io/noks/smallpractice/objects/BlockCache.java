package io.noks.smallpractice.objects;

import org.bukkit.Location;

public class BlockCache {
	private Location location;
	private int id;
	private int data;

	public BlockCache(Location loc, int id, int data) {
		this.location = loc;
		this.id = id;
		this.data = data;
	}
	
	public Location location() {
		return this.location;
	}
	
	public int id() {
		return this.id;
	}
	
	public int data() {
		return this.data;
	}
}
