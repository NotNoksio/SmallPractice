package io.noks.smallpractice.objects;

import javax.annotation.Nullable;

import org.bukkit.inventory.Inventory;

import io.noks.smallpractice.enums.Ladders;

public class EditedLadderKit {
	private Ladders ladder;
	private String name;
	private int slot;
	private Inventory inventory;
	
	public EditedLadderKit(Ladders ladder, int slot, Inventory inventory) {
		this.ladder = ladder;
		this.name = ladder.getName() + " #" + slot;
		this.slot = slot;
		this.inventory = inventory;
	}
	
	public EditedLadderKit(Ladders ladder, @Nullable String name, int slot, Inventory inventory) {
		this.ladder = ladder;
		this.name = (name == null ? ladder.getName() + " #" + slot : name);
		this.slot = slot;
		this.inventory = inventory;
	}
	
	public Ladders getLadder() {
		return this.ladder;
	}
	
	public String getName() {
		return this.name;
	}
	
	public void rename(String newName) {
		this.name = newName;
	}
	
	public int getSlot() {
		return this.slot;
	}
	
	public Inventory getInventory() {
		return this.inventory;
	}
}
