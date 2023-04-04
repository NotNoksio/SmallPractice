package io.noks.smallpractice.party;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.inventory.ItemStack;

public enum PartyEvents {
	SPLIT_TEAM("Split Team", new ItemStack(Material.SHEARS, 1), new String[] {ChatColor.GRAY + "Split your team randomly", ChatColor.GRAY + "in 2 and fight!"}),
	FFA("FFA", new ItemStack(Material.SKULL_ITEM, 1, (short)SkullType.PLAYER.ordinal()), new String[] {ChatColor.GRAY + "Free For All"}),
	REDROVER("RedRover", new ItemStack(Material.DIAMOND_SWORD, 1), new String[] {ChatColor.RED + "Coming for season 2!"});
	
	private String name;
	private ItemStack icon;
	private String[] lore;
	
	PartyEvents(String name, ItemStack icon, String[] lore){
		this.name = name;
		this.icon = icon;
		this.lore = lore;
	}
	
	public String getName() {
		return this.name;
	}
	
	public ItemStack icon() {
		return this.icon;
	}
	
	public String[] lore() {
		return this.lore;
	}
	
	public static PartyEvents getByName(String name) {
		for (PartyEvents event : values()) {
			if (event.getName().toLowerCase().equals(name.toLowerCase())) {
				return event;
			}
		}
		return null;
	}
}
