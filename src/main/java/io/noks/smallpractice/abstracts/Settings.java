package io.noks.smallpractice.abstracts;

import org.bukkit.inventory.ItemStack;

public abstract class Settings {
	private ItemStack icon;
	private String displayName;
	private int slot;
	
	public Settings(ItemStack icon, String displayName, int slot) {
		this.icon = icon;
		this.displayName = displayName;
		this.slot = slot;
	}
	
	public ItemStack getIcon() {
		return this.icon;
	}
	
	public String getDisplayName() {
		return this.displayName;
	}
	
	public int getSlot() {
		return this.slot;
	}
	
	protected abstract void update();
}
