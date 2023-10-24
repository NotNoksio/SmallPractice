package io.noks.smallpractice.enums;

import org.bukkit.ChatColor;

public enum DivisionsEnum {
	UNRANKED,
	SILVER,
	GOLD,
	PLATINUM,
	DIAMOND,
	RUBY,
	EMERALD,
	CHAMPION,
	GRAND_CHAMPION,
	MASTER;
	
	private String name;
	private ChatColor color;
	private int min, max;
}
