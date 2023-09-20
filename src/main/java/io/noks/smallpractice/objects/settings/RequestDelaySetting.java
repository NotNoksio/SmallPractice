package io.noks.smallpractice.objects.settings;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import io.noks.smallpractice.abstracts.Settings;

public class RequestDelaySetting extends Settings {
	private int delay;
	
	public RequestDelaySetting() {
		super(new ItemStack(Material.WATCH, 1), "Request Delay", 8);
		this.delay = 300;
	}
	public RequestDelaySetting(int delay) {
		super(new ItemStack(Material.WATCH, 1), "Request Delay", 8);
		this.delay = delay;
	}
	
	@Override
	protected void update() {
		if (this.delay >= 60) {
			this.delay = 5;
			return;
		}
		this.delay += 5;
	}
	
	public int get() {
		return this.delay;
	}
}
