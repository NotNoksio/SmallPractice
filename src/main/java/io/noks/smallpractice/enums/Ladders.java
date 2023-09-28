package io.noks.smallpractice.enums;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public enum Ladders {
	NODEBUFF("NoDebuff", ChatColor.AQUA, new ItemStack(Material.POTION, 1, (short) 16421), true, true, true, true),
	ARCHER("Archer", ChatColor.RED, new ItemStack(Material.BOW, 1), false, true, true, false),
	AXE("Axe", ChatColor.LIGHT_PURPLE, new ItemStack(Material.IRON_AXE, 1), false, true, true, false),
	SOUP("Soup", ChatColor.GOLD, new ItemStack(Material.MUSHROOM_SOUP, 1), false, false, true, false),
	EARLY_HG("EarlyHG", ChatColor.GREEN, new ItemStack(Material.STONE_SWORD, 1), false, false, true, false),
	GAPPLE("Gapple", ChatColor.BLUE, new ItemStack(Material.GOLDEN_APPLE, 1, (short) 1), true, true, true, false),
	BOXING("Boxing", ChatColor.DARK_AQUA, new ItemStack(Material.NOTE_BLOCK, 1), false, false, true, false),
	COMBO("Combo", ChatColor.DARK_GREEN, new ItemStack(Material.FEATHER, 1), true, true, true, false),
	SUMO("Sumo", ChatColor.YELLOW, new ItemStack(Material.CLAY_BRICK, 1), false, false, false, false), // Disabled bc on the server where the plugin is used, there's no sumo arena :)
	NOENCHANT("NoEnchant", ChatColor.DARK_PURPLE, new ItemStack(Material.DIAMOND_SWORD), true, true, true, true),
	CLASSIC("Classic", ChatColor.DARK_RED, new ItemStack(Material.FISHING_ROD), true, true, true, false),
	SPLEEF("Spleef", ChatColor.WHITE, new ItemStack(Material.DIAMOND_SPADE), false, false, false, false),
	BUILDUHC("BuildUHC", ChatColor.GRAY, new ItemStack(Material.WOOD), true, true, false, false);
	
	private String name;
	private ChatColor color;
	private ItemStack icon;
	private boolean editable, needFood, enable /* This will be removed or be enabled/disabled by a command */, enderpearl;
	
	Ladders(String name, ChatColor color, ItemStack icon, boolean editable, boolean needFood, boolean enable, boolean enderpearl) {
		this.name = name;
		this.color = color;
		this.icon = icon;
		this.editable = editable;
		this.needFood = needFood;
		this.enable = enable;
		this.enderpearl = enderpearl;
	}
	
	public String getName() {
		return this.name;
	}
	
	public ChatColor getColor() {
		return this.color;
	}
	
	public ItemStack getIcon() {
		return this.icon;
	}
	
	public boolean isEditable() {
		return this.editable;
	}
	
	public boolean needFood() {
		return this.needFood;
	}
	
	public boolean isEnable() {
		return this.enable;
	}
	
	public boolean containsEnderpearl() {
		return this.enderpearl;
	}
	
	public static Ladders getLadderFromName(String name) {
		for (Ladders ladders : values()) {
			if (ladders.getName().toLowerCase().equals(name.toLowerCase())) {
				return ladders;
			}
		} 
		return null;
	}
	  
	public static boolean contains(String name) { 
		return getLadderFromName(name) != null; 
	}
}
