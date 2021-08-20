package us.noks.smallpractice.enums;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public enum Ladders {
	NODEBUFF("NoDebuff", ChatColor.AQUA, new ItemStack(Material.POTION, 1, (short) 16421), true, true),
	ARCHER("Archer", ChatColor.RED, new ItemStack(Material.BOW, 1), false, true),
	AXE("Axe", ChatColor.LIGHT_PURPLE, new ItemStack(Material.IRON_AXE, 1), false, true),
	SOUP("Soup", ChatColor.GOLD, new ItemStack(Material.MUSHROOM_SOUP, 1), false, true),
	EARLY_HG("Early-HG", ChatColor.GREEN, new ItemStack(Material.STONE_SWORD, 1), false, true),
	GAPPLE("Gapple", ChatColor.BLUE, new ItemStack(Material.GOLDEN_APPLE, 1, (short) 1), false, true),
	BOXING("Boxing", ChatColor.DARK_AQUA, new ItemStack(Material.NOTE_BLOCK, 1), false, true),
	COMBO("Combo", ChatColor.DARK_GREEN, new ItemStack(Material.FEATHER, 1), false, false),
	SUMO("Sumo", ChatColor.YELLOW, new ItemStack(Material.CLAY_BRICK, 1), false, true);
	
	private String name;
	private ChatColor color;
	private ItemStack icon;
	private boolean editable;
	private boolean enable; // this will be removed or be enabled/disabled by a command
	private Inventory defaultInventory; // TODO
	
	Ladders(String name, ChatColor color, ItemStack icon, boolean editable, boolean enable) {
		this.name = name;
		this.color = color;
		this.icon = icon;
		this.editable = editable;
		this.enable = enable;
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
	
	public boolean isEnable() {
		return this.enable;
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
