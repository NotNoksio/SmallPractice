package io.noks.smallpractice.objects.settings;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import io.noks.smallpractice.abstracts.Settings;

public class PartySetting extends Settings {
	private boolean partyInvite;
	
	public PartySetting() {
		super(new ItemStack(Material.ANVIL, 1), "Toggle Party Invite", 2);
		this.partyInvite = true;
	}
	public PartySetting(boolean partyInvite) {
		super(new ItemStack(Material.ANVIL, 1), "Toggle Party Invite", 2);
		this.partyInvite = partyInvite;
	}
	
	@Override
	protected void update() {
		this.partyInvite = !this.partyInvite;
	}
	
	public boolean isToggled() {
		return this.partyInvite;
	}
}
