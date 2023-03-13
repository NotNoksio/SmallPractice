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
		final Location[] arena1 = {new Location(Bukkit.getWorld("world"), -2121.5D, 120.0D, 1667.5D, -163.0F, 0.0F), new Location(Bukkit.getWorld("world"), -2092.5D, 120.0D, 1590.5D, 22.0F, -1.0F)};
		final Location[] arena2 = {new Location(Bukkit.getWorld("world"), -878.5D, 46.0D, 1984.5D, -35.0F, 0.0F), new Location(Bukkit.getWorld("world"), -832.5D, 46.0D, 2056.5D, -202.0F, 0.0F)};
		final Location[] arena3 = {new Location(Bukkit.getWorld("world"), 7032.5D, 100.0D, 6992.5D, -178.0F, 0.0F), new Location(Bukkit.getWorld("world"), 7032.5D, 100.0D, 6925.5D, -358.0F, 0.0F)};
		final Location[] arena4 = {new Location(Bukkit.getWorld("world"), -1079.5D, 89.0D, 2491.5D, -28.0F, 0.0F), new Location(Bukkit.getWorld("world"), -1042.5D, 89.0D, 2558.5D, -209.0F, 0.0F)};
		final Location[] arena5 = {new Location(Bukkit.getWorld("world"), 45.5D, 36.0D, 1283.5D, -155.0F, 0.0F), new Location(Bukkit.getWorld("world"), 79.5D, 38.0D, 1205.5D, 1.0F, 0.0F)};
		final Location[] arena6 = {new Location(Bukkit.getWorld("world"), -483.5D, 68.0D, 1748.5D, 179.0F, 0.0F), new Location(Bukkit.getWorld("world"), -483.5D, 68.0D, 1670.5D, 0.0F, 0.0F)};
		final Location[] arena7 = {new Location(Bukkit.getWorld("world"), -835.5D, 4.0D, 888.5D, 177.0F, 0.0F), new Location(Bukkit.getWorld("world"), -835.5D, 4.0D, 816.5D, 0.0F, 0.0F)};
		final Location[] arena8 = {new Location(Bukkit.getWorld("world"), -2670.5D, 16.0D, 1874.5D, 177.0F, 0.0F), new Location(Bukkit.getWorld("world"), -2715.5D, 16.0D, 1801.5D, -3.0F, 0.0F)};
		final Location[] arena9 = {new Location(Bukkit.getWorld("world"), -3287.5D, 150.0D, 2523.5D, 0.0F, 0.0F), new Location(Bukkit.getWorld("world"), -3287.5D, 150.0D, 2603.5D, 178.0F, 0.0F)};
		final Location[] arena10 = {new Location(Bukkit.getWorld("world"), -3657.5D, 153.0D, 1488.5D, 63.0F, 0.0F), new Location(Bukkit.getWorld("world"), -3724.5D, 153.0D, 1527.5D, -125.0F, 0.0F)};
		final Location[] arena11 = {new Location(Bukkit.getWorld("world"), 751.5D, 4.0D, 971.5D, -43.0F, 0.0F), new Location(Bukkit.getWorld("world"), 804.5D, 4.0D, 1025.5D, 134.0F, 0.0F)};
		    
		arenaList.put(1, new Arenas("River", arena1, new ItemStack(Material.WATER_BUCKET, 1), false));
		arenaList.put(2, new Arenas("Rock", arena2, new ItemStack(Material.STONE, 1), false));
		arenaList.put(3, new Arenas("Logo", arena3, new ItemStack(Material.GOLDEN_APPLE, 1), false));
		arenaList.put(4, new Arenas("Stalagmites", arena4, new ItemStack(Material.BEDROCK, 1), false));
		arenaList.put(5, new Arenas("Rocks", arena5, new ItemStack(Material.COBBLESTONE, 1), false));
		arenaList.put(6, new Arenas("Sphinx", arena6, new ItemStack(Material.SAND, 1), false));
		arenaList.put(7, new Arenas("American-Foot", arena7, new ItemStack(Material.GRASS, 1), false));
		arenaList.put(8, new Arenas("Lava", arena8, new ItemStack(Material.LAVA_BUCKET, 1), false));
		arenaList.put(9, new Arenas("Book", arena9, new ItemStack(Material.BOOK, 1), false));
		arenaList.put(10, new Arenas("End", arena10, new ItemStack(Material.ENDER_STONE, 1), false));
		arenaList.put(11, new Arenas("WoolWorld", arena11, new ItemStack(Material.WOOL, 1, (short) new Random().nextInt(15)), false));
	}
	
	public Arenas getRandomArena(Ladders ladder) {
		final List<Arenas> arenas = this.arenaList.values().stream().filter((ladder == Ladders.SUMO ? Arenas::isSumo : not(Arenas::isSumo))).collect(Collectors.toList());
		final int random = new Random().nextInt(arenas.size());
		return arenas.get(random);
	}
	private <T> Predicate<T> not(Predicate<T> p) { return t -> !p.test(t); }
	
	public Arenas getArenaByName(String name) {
		for (Arenas arenas : this.arenaList.values()) {
			if (arenas.getName().toLowerCase().equals(name)) {
				return arenas;
			}
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
