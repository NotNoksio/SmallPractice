package io.noks.smallpractice.objects.settings;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import io.noks.smallpractice.abstracts.Settings;

public class MessageSetting extends Settings {
	private boolean privateMessage;
	
	public MessageSetting() {
		super(new ItemStack(Material.PAPER, 1), "Toggle Private Message", 1);
		this.privateMessage = true;
	}
	public MessageSetting(boolean privateMessage) {
		super(new ItemStack(Material.PAPER, 1), "Toggle Private Message", 1);
		this.privateMessage = privateMessage;
	}
	
	@Override
	protected void update() {
		this.privateMessage = !this.privateMessage;
	}
	
	public boolean isToggled() {
		return this.privateMessage;
	}
}
