package io.noks.smallpractice.arena;

import java.util.Set;
import java.util.UUID;

import javax.annotation.Nullable;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;

import net.minecraft.util.com.google.common.collect.Sets;

public class Arena {
	private String name;
	private Location[] locations;
	private ItemStack icon;
	private boolean sumo, spleef;
	private Set<UUID> spectators; 
	private @Nullable String[] builders;

	public Arena(String name, Location[] locations, ItemStack icon, boolean sumo, boolean spleef) {
		this.name = name;
		this.locations = locations;
		this.icon = icon;
		this.sumo = sumo;
		this.spleef = spleef;
		this.spectators = Sets.newHashSet();
	}

	public World getWorld() {
		return this.locations[0].getWorld();
	}

	public String getName() {
		return this.name;
	}

	public Location[] getLocations() {
		return this.locations;
	}

	public ItemStack getIcon() {
		return this.icon;
	}

	public boolean isSumo() {
		return this.sumo;
	}
	
	public boolean isSpleef() {
		return this.spleef;
	}

	public void addSpectator(UUID uuid) {
		this.spectators.add(uuid);
	}

	public void removeSpectator(UUID uuid) {
		this.spectators.remove(uuid);
	}

	public Set<UUID> getAllSpectators() {
		return this.spectators;
	}

	public boolean hasSpectators() {
		return !this.spectators.isEmpty();
	}

	public Location getMiddle() {
		return new Location(getWorld(), (this.locations[0].getX() + this.locations[1].getX()) / 2, (this.locations[0].getY() + this.locations[1].getY()) / 2, (this.locations[0].getZ() + this.locations[1].getZ()) / 2);
	}
}
