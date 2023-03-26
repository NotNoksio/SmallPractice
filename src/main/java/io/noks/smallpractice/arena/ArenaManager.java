package io.noks.smallpractice.arena;

import java.util.List;
import java.util.Random;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;

import io.noks.smallpractice.enums.Ladders;
import net.minecraft.util.com.google.common.collect.Lists;

public class ArenaManager {
	private List<Arena> arenaList = Lists.newLinkedList();
	public ArenaManager() {
		setupArena();
	}
	
	private void setupArena() {
		if (!arenaList.isEmpty()) {
			return;
		}
		final World world = Bukkit.getWorld("world");
		final Location[] arena1 = {new Location(world, -2121.5D, 120.5D, 1667.5D, -163.0F, 0.0F), new Location(world, -2092.5D, 120.5D, 1590.5D, 22.0F, -1.0F)};
		final Location[] arena2 = {new Location(world, -878.5D, 46.5D, 1984.5D, -35.0F, 0.0F), new Location(world, -832.5D, 46.5D, 2056.5D, -202.0F, 0.0F)};
		final Location[] arena3 = {new Location(world, 7032.5D, 100.5D, 6992.5D, -178.0F, 0.0F), new Location(world, 7032.5D, 100.5D, 6925.5D, -358.0F, 0.0F)};
		final Location[] arena4 = {new Location(world, -1079.5D, 89.5D, 2491.5D, -28.0F, 0.0F), new Location(world, -1042.5D, 89.5D, 2558.5D, -209.0F, 0.0F)};
		final Location[] arena5 = {new Location(world, 45.5D, 36.5D, 1283.5D, -155.0F, 0.0F), new Location(world, 79.5D, 38.5D, 1205.5D, 1.0F, 0.0F)};
		final Location[] arena6 = {new Location(world, -483.5D, 68.5D, 1748.5D, 179.0F, 0.0F), new Location(world, -483.5D, 68.5D, 1670.5D, 0.0F, 0.0F)};
		final Location[] arena7 = {new Location(world, -835.5D, 4.5D, 888.5D, 177.0F, 0.0F), new Location(world, -835.5D, 4.5D, 816.5D, 0.0F, 0.0F)};
		final Location[] arena8 = {new Location(world, -2670.5D, 16.5D, 1874.5D, 177.0F, 0.0F), new Location(world, -2715.5D, 16.5D, 1801.5D, -3.0F, 0.0F)};
		final Location[] arena9 = {new Location(world, -3287.5D, 150.5D, 2523.5D, 0.0F, 0.0F), new Location(world, -3287.5D, 150.5D, 2603.5D, 178.0F, 0.0F)};
		final Location[] arena10 = {new Location(world, -3657.5D, 153.5D, 1488.5D, 63.0F, 0.0F), new Location(world, -3724.5D, 153.5D, 1527.5D, -125.0F, 0.0F)};
		final Location[] arena11 = {new Location(world, 751.5D, 4.5D, 971.5D, -43.0F, 0.0F), new Location(world, 804.5D, 4.5D, 1025.5D, 134.0F, 0.0F)};
		    
		arenaList.add(new Arena("River", arena1, new ItemStack(Material.WATER_BUCKET, 1), false));
		arenaList.add(new Arena("Rock", arena2, new ItemStack(Material.STONE, 1), false));
		arenaList.add(new Arena("Logo", arena3, new ItemStack(Material.GOLDEN_APPLE, 1), false));
		arenaList.add(new Arena("Stalagmites", arena4, new ItemStack(Material.BEDROCK, 1), false));
		arenaList.add(new Arena("Rocks", arena5, new ItemStack(Material.COBBLESTONE, 1), false));
		arenaList.add(new Arena("Sphinx", arena6, new ItemStack(Material.SAND, 1), false));
		arenaList.add(new Arena("American-Foot", arena7, new ItemStack(Material.GRASS, 1), false));
		arenaList.add(new Arena("Lava", arena8, new ItemStack(Material.LAVA_BUCKET, 1), false));
		arenaList.add(new Arena("Book", arena9, new ItemStack(Material.BOOK, 1), false));
		arenaList.add(new Arena("End", arena10, new ItemStack(Material.ENDER_STONE, 1), false));
		arenaList.add(new Arena("WoolWorld", arena11, new ItemStack(Material.WOOL, 1, (short) new Random().nextInt(15)), false));
	}
	
	public Arena getRandomArena(Ladders ladder) {
		final List<Arena> arenas = this.arenaList.stream().filter((ladder == Ladders.SUMO ? Arena::isSumo : not(Arena::isSumo))).collect(Collectors.toList());
		final int random = new Random().nextInt(arenas.size());
		return arenas.get(random);
	}
	private <T> Predicate<T> not(Predicate<T> p) { return t -> !p.test(t); }
	
	public Arena getArenaByName(String name) {
		for (Arena arenas : this.arenaList) {
			if (arenas.getName().toLowerCase().equals(name)) {
				return arenas;
			}
		}
		return null;
	}
	
	public List<Arena> getArenaList() {
		return this.arenaList;
	}
}
