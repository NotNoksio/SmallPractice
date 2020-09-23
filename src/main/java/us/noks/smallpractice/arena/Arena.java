package us.noks.smallpractice.arena;

import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import com.google.common.collect.Maps;

public class Arena {
	private Map<Integer, Arenas> arenaList = Maps.newConcurrentMap();
	private Location arena1_Pos1, arena1_Pos2;
	private Location arena2_Pos1, arena2_Pos2;
	private Location arena3_Pos1, arena3_Pos2;
	private Location arena4_Pos1, arena4_Pos2;
	private Location arena5_Pos1, arena5_Pos2;
	private Location arena6_Pos1, arena6_Pos2;
	
	public Arena() {
		setupArena();
	}
	
	private void setupArena() {
		if (arenaList.isEmpty()) {
			arena1_Pos1 = new Location(Bukkit.getWorld("world"), -2121.5D, 120.0D, 1667.5D, -163.0F, 0.0F);
		    arena1_Pos2 = new Location(Bukkit.getWorld("world"), -2092.5D, 120.0D, 1590.5D, 22.0F, -1.0F);
		    arena2_Pos1 = new Location(Bukkit.getWorld("world"), -878.5D, 46.0D, 1984.5D, -35.0F, 0.0F);
		    arena2_Pos2 = new Location(Bukkit.getWorld("world"), -832.5D, 46.0D, 2056.5D, -202.0F, 0.0F);
		    arena3_Pos1 = new Location(Bukkit.getWorld("world"), 7032.5D, 100.0D, 6992.5D, -178.0F, 0.0F);
		    arena3_Pos2 = new Location(Bukkit.getWorld("world"), 7032.5D, 100.0D, 6925.5D, -358.0F, 0.0F);
		    arena4_Pos1 = new Location(Bukkit.getWorld("world"), -1079.5D, 89.0D, 2491.5D, -28.0F, 0.0F);
		    arena4_Pos2 = new Location(Bukkit.getWorld("world"), -1042.5D, 89.0D, 2558.5D, -209.0F, 0.0F);
		    arena5_Pos1 = new Location(Bukkit.getWorld("world"), -1419.5D, 127.0D, 685.5D, 270.0F, 0.0F);
		    arena5_Pos2 = new Location(Bukkit.getWorld("world"), -1359.5D, 127.0D, 685.5D, 89.0F, 0.0F);
		    arena6_Pos1 = new Location(Bukkit.getWorld("world"), -483.5D, 68.0D, 1748.5D, 179.0F, 0.0F);
		    arena6_Pos2 = new Location(Bukkit.getWorld("world"), -483.5D, 68.0D, 1670.5D, 0.0F, 0.0F);
		    
		    arenaList.put(1, new Arenas(this, "River", new Location[] {arena1_Pos1, arena1_Pos2}, false));
			arenaList.put(2, new Arenas(this, "Rock", new Location[] {arena2_Pos1, arena2_Pos2}, false));
			arenaList.put(3, new Arenas(this, "Logo", new Location[] {arena3_Pos1, arena3_Pos2}, false));
			arenaList.put(4, new Arenas(this, "Stalagmites", new Location[] {arena4_Pos1, arena4_Pos2}, false));
			arenaList.put(5, new Arenas(this, "PacMan", new Location[] {arena5_Pos1, arena5_Pos2}, false));
			arenaList.put(6, new Arenas(this, "Sphinx", new Location[] {arena6_Pos1, arena6_Pos2}, false));
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
	
	public class Arenas {
		private Arena arena;
		private String name;
		private Location[] locations;
		private boolean sumo;
		
		public Arenas(Arena arena, String name, Location[] locations, boolean sumo) {
			this.arena = arena;
			this.name = name;
			this.locations = locations;
			this.sumo = sumo;
		}
		
		public Arena getArena() {
			return this.arena;
		}
		
		public String getName() {
			return this.name;
		}
		
		public Location[] getLocations() {
			return this.locations;
		}
		
		public boolean isSumo() {
			return this.sumo;
		}
	}
}
