package io.noks.smallpractice.arena;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;

import com.google.common.collect.Maps;

import io.noks.smallpractice.enums.Ladders;
import net.minecraft.util.com.google.common.collect.Sets;

public class Arena {
	private Map<Integer, Arenas> arenaList = Maps.newConcurrentMap();
	private static Arena instance = new Arena();
	public static  Arena getInstance() {
		return instance;
	}
	
	public Arena() {
		setupArena();
	}
	
	private void setupArena() {
		if (!arenaList.isEmpty()) {
			return;
		}
		final Location[] arena1 = {new Location(Bukkit.getWorld("world"), 1037.5D, 64.0D, 0.5D, 90.0F, 0.0F), new Location(Bukkit.getWorld("world"), 962.5D, 64.0D, 0.5D, -90.0F, -1.0F)};
		final Location[] arena2 = {new Location(Bukkit.getWorld("world"), 4000.5D, 64.0D, 41.5D, 179.0F, 0.0F), new Location(Bukkit.getWorld("world"), 4000.5D, 64.0D, -41.5D, 0.0F, 0.0F)};
		final Location[] arena3 = {new Location(Bukkit.getWorld("world"), 9000.5D, 64.0D, 40.5D, -179.0F, 0.0F), new Location(Bukkit.getWorld("world"), 9000.5D, 64.0D, -45.5D, 0.0F, 0.0F)};
		final Location[] arena4 = {new Location(Bukkit.getWorld("world"), 15000.5D, 64.0D, -38.5D, 0.0F, 0.0F), new Location(Bukkit.getWorld("world"), 15000.5D, 64.0D, 40.5D, -179.0F, 0.0F)};
		final Location[] arena5 = {new Location(Bukkit.getWorld("world"), 12954.5D, 54.0D, -1.5D, 0.0F, 0.0F), new Location(Bukkit.getWorld("world"), 13042.5D, 54.0D, -1.5D, 90.0F, 0.0F)};
		final Location[] arena6 = {new Location(Bukkit.getWorld("world"), 0.5D, 64.0D, 36995.5D, 0.0F, 0.0F), new Location(Bukkit.getWorld("world"), 0.5D, 64.0D, 37005.5D, 179.0F, 0.0F)};
		final Location[] arena7 = {new Location(Bukkit.getWorld("world"), 1.5D, 64.0D, 35994.5D, 0.0F, 0.0F), new Location(Bukkit.getWorld("world"), 1.5D, 64.0D, 36006.5D, 179.0F, 0.0F)};
		final Location[] arena8 = {new Location(Bukkit.getWorld("world"), 14041.5D, 64.0D, -0.5D, 89.0F, 0.0F), new Location(Bukkit.getWorld("world"), 13965.5D, 64.0D, -0.5D, -89.0F, 0.0F)};
		    
		arenaList.put(1, new Arenas("Cave", arena1, new ItemStack(Material.STONE, 1), false));
		arenaList.put(2, new Arenas("Rock", arena2, new ItemStack(Material.BRICK, 1), false));
		arenaList.put(3, new Arenas("Grass", arena3, new ItemStack(Material.GRASS, 1), false));
		arenaList.put(4, new Arenas("Soccer", arena4, new ItemStack(Material.WOOL, 1), false));
		arenaList.put(5, new Arenas("Pokemon", arena5, new ItemStack(Material.BONE, 1), false));
		arenaList.put(6, new Arenas("Sumo1", arena6, new ItemStack(Material.ANVIL, 1), true));
		arenaList.put(7, new Arenas("Sumo2", arena7, new ItemStack(Material.LADDER, 1), true));
		arenaList.put(8, new Arenas("Shy Guy", arena8, new ItemStack(Material.ARROW, 1), false));
	}
	
	public Arenas getRandomArena(Ladders ladder) {
		final List<Arenas> arenas = this.arenaList.values().stream().filter((ladder == Ladders.SUMO ? Arenas::isSumo : not(Arenas::isSumo))).collect(Collectors.toList());
		final int random = new Random().nextInt(arenas.size());
		return arenas.get(random);
	}
	private <T> Predicate<T> not(Predicate<T> p) { return t -> !p.test(t); }
	
	public Arenas getArenaByInteger(int i) {
		if (this.arenaList.containsKey(i)) {
			return this.arenaList.get(i);
		}
		return null;
	}
	
	public Map<Integer, Arenas> getArenaList() {
		return this.arenaList;
	}
	
	public class Arenas {
		private String name;
		private Location[] locations;
		private ItemStack icon;
		private boolean sumo;
		private Set<UUID> spectators;
		
		public Arenas(String name, Location[] locations, ItemStack icon, boolean sumo) {
			this.name = name;
			this.locations = locations;
			this.icon = icon;
			this.sumo = sumo;
			this.spectators = Sets.newHashSet();
		}
		
		public World getWorld() { // In case we need someday
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
}
