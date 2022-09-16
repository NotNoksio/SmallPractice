package io.noks.smallpractice.enums;

import org.bukkit.Bukkit;
import org.bukkit.Location;

public enum Warps {
	BRIDGE("Bridge", new Location(Bukkit.getWorld("world"), -1193.5D, 109.5D, 4654.5D, 90.0F, 0.0F));
	
	private String name;
	private Location lobbyLocation;
	
	Warps(String name, Location lobbyLocation) {
		this.name = name;
		this.lobbyLocation = lobbyLocation;
	}
	
	public String getName() {
		return this.name;
	}
	
	public Location getLobbyLocation() {
		return this.lobbyLocation;
	}
}
