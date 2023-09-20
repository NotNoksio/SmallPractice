package io.noks.smallpractice.objects.settings;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import io.noks.smallpractice.abstracts.Settings;

public class QueueSetting extends Settings {
	private int queuePingDiff;
	
	public QueueSetting() {
		super(new ItemStack(Material.FEATHER, 1), "Ping Difference", 0);
		this.queuePingDiff = 300;
	}
	public QueueSetting(int pingDiff) {
		super(new ItemStack(Material.FEATHER, 1), "Ping Difference", 0);
		this.queuePingDiff = pingDiff;
	}
	
	@Override
	protected void update() {
		if (this.queuePingDiff >= 300) {
			this.queuePingDiff = 50;
			return;
		}
		this.queuePingDiff += 50;
	}
}
