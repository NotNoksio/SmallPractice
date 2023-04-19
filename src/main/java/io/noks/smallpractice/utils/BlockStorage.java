package io.noks.smallpractice.utils;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.block.Block;

import io.noks.smallpractice.objects.BlockCache;
import net.minecraft.util.com.google.common.collect.Lists;

public class BlockStorage {
	private List<BlockCache> blocks;
	
	public BlockStorage() {
		this.blocks = Lists.newLinkedList();
	}
	
	public void add(Block block) {
		this.blocks.add(new BlockCache(block.getLocation(), block.getTypeId(), block.getData()));
	}
	
	public void addAir(Location loc) {
		this.blocks.add(new BlockCache(loc, 0, 0));
	}
	
	public void clearCache() {
		this.blocks.clear();
	}
	
	public Location[] getAllLocations() {
	    return this.blocks.stream().map(BlockCache::location).toArray(Location[]::new);
	}
	
	public int[] getAllIds() {
	    return this.blocks.stream().mapToInt(BlockCache::id).toArray();
	}
	
	public int[] getAllDatas() {
	    return this.blocks.stream().mapToInt(BlockCache::data).toArray();
	}
}
