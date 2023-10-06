package io.noks.smallpractice.objects;

import org.bukkit.Location;

public class BlockCache {
	private Location location;
	private int id, oldID;
	private int data, oldDATA;

	public BlockCache(Location loc, int id, int data, int oldID, int oldDATA) {
		this.location = loc;
		this.id = id;
		this.data = data;
		this.oldID = oldID;
		this.oldDATA = oldDATA;
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
	
	public int oldID() {
		return this.oldID;
	}
	
	public int oldDATA() {
		return this.oldDATA;
	}
}
