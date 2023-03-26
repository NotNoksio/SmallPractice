package io.noks.smallpractice.objects;

import org.bukkit.inventory.PlayerInventory;

import io.noks.smallpractice.enums.Ladders;

public class EditedLadderKit {
	private Ladders ladder;
	private String name;
	private int slot;
	private PlayerInventory customInventory;
	
	public EditedLadderKit(Ladders ladder, int slot, PlayerInventory inventory) {
		this.ladder = ladder;
		this.name = ladder.toString() + " #" + slot;
		this.slot = slot;
		this.customInventory = inventory;
	}
	
	public EditedLadderKit(Ladders ladder, String name, int slot, PlayerInventory inventory) {
		if (name == null) {
			new EditedLadderKit(ladder, slot, inventory);
			return;
		}
		this.ladder = ladder;
		this.name = name;
		this.slot = slot;
		this.customInventory = inventory;
	}
	
	public Ladders getLadder() {
		return this.ladder;
	}
	
	public String getName() {
		return this.name;
	}
	
	public int getSlot() {
		return this.slot;
	}
	
	public PlayerInventory getCustomInventory() {
		return this.customInventory;
	}
}
