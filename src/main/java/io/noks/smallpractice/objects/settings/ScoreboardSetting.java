package io.noks.smallpractice.objects.settings;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import io.noks.smallpractice.abstracts.Settings;

public class ScoreboardSetting extends Settings {
	private boolean scoreboard;
	
	public ScoreboardSetting() {
		super(new ItemStack(Material.SIGN, 1), "Toggle Scoreboard", 4);
		this.scoreboard = true;
	}
	public ScoreboardSetting(boolean scoreboard) {
		super(new ItemStack(Material.SIGN, 1), "Toggle Scoreboard", 4);
		this.scoreboard = scoreboard;
	}
	
	@Override
	protected void update() {
		this.scoreboard = !this.scoreboard;
	}
	
	public boolean isToggled() {
		return this.scoreboard;
	}
}