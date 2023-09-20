package io.noks.smallpractice.objects.settings;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import io.noks.smallpractice.abstracts.Settings;

public class DuelSetting extends Settings {
	private boolean duelRequest;
	
	public DuelSetting() {
		super(new ItemStack(Material.ANVIL, 1), "Toggle Duel Request", 3);
		this.duelRequest = true;
	}
	public DuelSetting(boolean duelRequest) {
		super(new ItemStack(Material.ANVIL, 1), "Toggle Duel Request", 3);
		this.duelRequest = duelRequest;
	}
	
	@Override
	protected void update() {
		this.duelRequest = !this.duelRequest;
	}
	
	public boolean isToggled() {
		return this.duelRequest;
	}
}