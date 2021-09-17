package us.noks.smallpractice.objects;

import java.util.UUID;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Ladder;

public class CustomInventory {
	private Ladder ladder;
	private String name;
	private int slot;
	private Inventory inventory;
	private ItemStack[] armor;
	
	public CustomInventory(Ladder ladder, String name, int slot, Inventory inv, ItemStack[] armor) {
		this.ladder = ladder;
		this.name = name;
		this.slot = slot;
		this.inventory = inv;
		this.armor = armor;
	}
	
	public CustomInventory(UUID uuid) {
		// TODO: when DB enabled load the player inventory here
	}
	
	public Ladder getLadder() {
		return this.ladder;
	}
	
	public String getName() {
		return this.name;
	}
	
	public int getSlot() {
		return this.slot;
	}
	
	public Inventory getInventory() {
		return this.inventory;
	}
	
	public ItemStack[] getArmor() {
		return this.armor;
	}
}
