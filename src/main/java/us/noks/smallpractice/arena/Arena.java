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
			arena1_Pos1 = new Location(Bukkit.getWorld("world"), -2121.5D, 120.0D, 1667.5D, -163.0F, 0.0F);
		    arena1_Pos2 = new Location(Bukkit.getWorld("world"), -2092.5D, 120.0D, 1590.5D, 22.0F, -1.0F);
		    arena2_Pos1 = new Location(Bukkit.getWorld("world"), -878.5D, 46.0D, 1984.5D, -35.0F, 0.0F);
		    arena2_Pos2 = new Location(Bukkit.getWorld("world"), -832.5D, 46.0D, 2056.5D, -202.0F, 0.0F);
		    
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
