package io.noks.smallpractice.utils;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

// For the pleasure of some
public class ItemBuilder {
	public static ItemStack createNewItemStackByMaterial(Material material, String displayName) {
		return createNewItemStackByMaterial(material, displayName, false);
	}
	public static ItemStack createNewItemStackByMaterial(Material material, String displayName, boolean unbreakable) {
		return createNewItemStack(new ItemStack(material), displayName, null, 1, unbreakable);
	}
	public static ItemStack createNewItemStack(ItemStack itemStack, String displayName) {
		return createNewItemStack(itemStack, displayName, null);
	}
	public static ItemStack createNewItemStack(ItemStack itemStack, String displayName, List<String> lore) {
		return createNewItemStack(itemStack, displayName, lore, 1);
	}
	public static ItemStack createNewItemStack(ItemStack itemStack, String displayName, List<String> lore, int amount) {
		return createNewItemStack(itemStack, displayName, lore, amount, false);
	}
	public static ItemStack createNewItemStack(ItemStack itemStack, String displayName, List<String> lore, int amount, boolean unbreakable) {
		ItemStack item = itemStack;
		ItemMeta itemMeta = item.getItemMeta();
		itemMeta.setDisplayName(displayName);
		if (lore != null) {
			itemMeta.setLore(lore);
		}
		itemMeta.spigot().setUnbreakable(unbreakable);
		item.setItemMeta(itemMeta);
		item.setAmount(Math.max(amount, 1));
		return item;
	}
	public static ItemStack createCustomPotionItem(String name, PotionEffectType type, int durationInSecond, int amplifier) {
		ItemStack potion = new ItemStack(Material.POTION, 1);
		PotionMeta pMeta = (PotionMeta) potion.getItemMeta();
		pMeta.setDisplayName(name);
		pMeta.addCustomEffect(new PotionEffect(type, (durationInSecond * 20), amplifier), true);
		potion.setItemMeta(pMeta);
		return potion;
	}
}
