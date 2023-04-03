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
		final Location[] arena2 = {new Location(world, -878.5D, 96.5D, 1984.5D, -35.0F, 0.0F), new Location(world, -832.5D, 96.5D, 2056.5D, -202.0F, 0.0F)};
		final Location[] arena3 = {new Location(world, 7032.5D, 100.5D, 6992.5D, -178.0F, 0.0F), new Location(world, 7032.5D, 100.5D, 6925.5D, -358.0F, 0.0F)};
		final Location[] arena4 = {new Location(world, -1079.5D, 89.5D, 2491.5D, -28.0F, 0.0F), new Location(world, -1042.5D, 89.5D, 2558.5D, -209.0F, 0.0F)};
		final Location[] arena5 = {new Location(world, 45.5D, 104.5D, 1283.5D, -155.0F, 0.0F), new Location(world, 79.5D, 106.5D, 1205.5D, 1.0F, 0.0F)};
		final Location[] arena6 = {new Location(world, -483.5D, 68.5D, 1748.5D, 179.0F, 0.0F), new Location(world, -483.5D, 68.5D, 1670.5D, 0.0F, 0.0F)};
		final Location[] arena7 = {new Location(world, -835.5D, 123.5D, 888.5D, 177.0F, 0.0F), new Location(world, -835.5D, 123.5D, 816.5D, 0.0F, 0.0F)};
		final Location[] arena8 = {new Location(world, -2670.5D, 86.5D, 1874.5D, 177.0F, 0.0F), new Location(world, -2715.5D, 86.5D, 1801.5D, -3.0F, 0.0F)};
		final Location[] arena9 = {new Location(world, -3287.5D, 150.5D, 2523.5D, 0.0F, 0.0F), new Location(world, -3287.5D, 150.5D, 2603.5D, 178.0F, 0.0F)};
		final Location[] arena10 = {new Location(world, -3657.5D, 153.5D, 1488.5D, 63.0F, 0.0F), new Location(world, -3724.5D, 153.5D, 1527.5D, -125.0F, 0.0F)};
		final Location[] arena11 = {new Location(world, 751.5D, 119.5D, 971.5D, -43.0F, 0.0F), new Location(world, 804.5D, 119.5D, 1025.5D, 134.0F, 0.0F)};
		final Location[] arena12 = {new Location(world, -1359.5D, 127.5D, 685.5D, 90.0F, 0.0F), new Location(world, -1419.5D, 127.5D, 685.5D, -89.0F, 0.0F)};
		final Location[] arena13 = {new Location(world, -1640.5D, 82.5D, 2201.5D, 68.0F, 0.0F), new Location(world, -1699.5D, 82.5D, 2215.5D, -92.0F, 0.0F)};
		final Location[] arena14 = {new Location(world, 8040.5D, 102.5D, 8006.5D, 0.0F, 0.0F), new Location(world, 8040.5D, 102.5D, 8091.5D, 180.0F, 0.0F)};
		final Location[] arena15 = {new Location(world, -1975.5D, 83.5D, 2159.5D, 180.0F, 0.0F), new Location(world, -1975.5D, 83.5D, 2088.5D, 0.0F, 0.0F)};
		final Location[] arena16 = {new Location(world, -257.5D, 88.5D, 1458.5D, 146.0F, 0.0F), new Location(world, -290.5D, 88.5D, 1389.5D, -26.0F, 0.0F)};
		final Location[] arena17 = {new Location(world, -446.5D, 72.5D, 2215.5D, -180.0F, 0.0F), new Location(world, -446.5D, 72.5D, 2130.5D, 0.0F, 0.0F)};
		    
		arenaList.add(new Arena("River", arena1, new ItemStack(Material.WATER_BUCKET, 1), false));
		arenaList.add(new Arena("Mine", arena2, new ItemStack(Material.COAL_ORE, 1), false));
		arenaList.add(new Arena("Hearth", arena3, new ItemStack(Material.GOLDEN_APPLE, 1), false));
		arenaList.add(new Arena("Stalagmites", arena4, new ItemStack(Material.BEDROCK, 1), false));
		arenaList.add(new Arena("Rock", arena5, new ItemStack(Material.COBBLESTONE, 1), false));
		arenaList.add(new Arena("Sphinx", arena6, new ItemStack(Material.SAND, 1), false));
		arenaList.add(new Arena("American Foot", arena7, new ItemStack(Material.GRASS, 1), false));
		arenaList.add(new Arena("Lava", arena8, new ItemStack(Material.LAVA_BUCKET, 1), false));
		arenaList.add(new Arena("Book", arena9, new ItemStack(Material.BOOK, 1), false));
		arenaList.add(new Arena("End", arena10, new ItemStack(Material.ENDER_STONE, 1), false));
		arenaList.add(new Arena("WoolWorld", arena11, new ItemStack(Material.WOOL, 1, (short) new Random().nextInt(15)), false));
		arenaList.add(new Arena("Pac-Man", arena12, new ItemStack(Material.GOLD_BLOCK, 1), false));
		arenaList.add(new Arena("Plastic World", arena13, new ItemStack(Material.CLAY, 1), false));
		arenaList.add(new Arena("Meteor", arena14, new ItemStack(Material.NETHERRACK, 1), false));
		arenaList.add(new Arena("Boat", arena15, new ItemStack(Material.BOAT, 1), false));
		arenaList.add(new Arena("Lost Island", arena16, new ItemStack(Material.FISHING_ROD, 1), false));
		arenaList.add(new Arena("Zen Garden", arena17, new ItemStack(Material.RED_ROSE, 1), false));
	}
	
	private final Random random = new Random();
	public Arena getRandomArena(Ladders ladder) {
		final List<Arena> arenas = this.arenaList.stream().filter((ladder == Ladders.SUMO ? Arena::isSumo : not(Arena::isSumo))).collect(Collectors.toList());
		return arenas.get(this.random.nextInt(arenas.size()));
	}
	private static <T> Predicate<T> not(Predicate<T> p) { 
		return p.negate();
	}
	
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
