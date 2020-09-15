package us.noks.smallpractice.arena;

import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import com.google.common.collect.Maps;

public class Arena {
	private Map<Integer, Arenas> arenaList = Maps.newConcurrentMap();
	private Location arena1_Pos1, arena2_Pos1;
	private Location arena1_Pos2, arena2_Pos2;
	
	public Arena() {
		setupArena();
	}
	
	private void setupArena() {
		if (arenaList.isEmpty()) {
			arena1_Pos1 = new Location(Bukkit.getWorld("world"), -1094.5D, 77.0D, 1225.5D, -27.0F, 0.0F);
		    arena1_Pos2 = new Location(Bukkit.getWorld("world"), -1055.5D, 76.0D, 1292.5D, 121.0F, -1.0F);
		    arena2_Pos1 = new Location(Bukkit.getWorld("world"), -1359.5D, 127.0D, 685.5D, 90.0F, 0.0F);
		    arena2_Pos2 = new Location(Bukkit.getWorld("world"), -1419.5D, 127.0D, 685.5D, -90.0F, 0.0F);
		    
		    arenaList.put(1, new Arenas(this, new Location[] {arena1_Pos1, arena1_Pos2}));
			arenaList.put(2, new Arenas(this, new Location[] {arena2_Pos1, arena2_Pos2}));
		}
	}
	
	public Arena getArena(int random) {
		return this.arenaList.get(random).getArena();
	}
	
	public Location[] getPositions(int random) {
		return this.arenaList.get(random).getLocations();
	}
	
	public Map<Integer, Arenas> getArenaList() {
		return this.arenaList;
	}
	
	private class Arenas {
		private Arena arena;
		private Location[] locations;
		
		private Arenas(Arena arena, Location[] locations) {
			this.arena = arena;
			this.locations = locations;
		}
		
		private Arena getArena() {
			return this.arena;
		}
		
		private Location[] getLocations() {
			return this.locations;
		}
	}
}
