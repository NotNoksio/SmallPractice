package io.noks.smallpractice.objects.managers;

import java.util.Collections;
import java.util.Map;
import java.util.Random;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffectType;

import io.noks.smallpractice.Main;
import io.noks.smallpractice.enums.Ladders;
import io.noks.smallpractice.enums.PlayerStatus;
import io.noks.smallpractice.objects.EditedLadderKit;
import io.noks.smallpractice.party.Party;
import io.noks.smallpractice.party.PartyState;
import io.noks.smallpractice.utils.ItemBuilder;
import net.minecraft.util.com.google.common.collect.Maps;

public class ItemManager {
	private Main main;
	private Map<Ladders, Inventory> DEFAULT_LADDER_INVENTORIES = Maps.newHashMap();
	public ItemManager(Main main) {
		this.main = main;
		for (Ladders ladders : Ladders.values()) {
			this.DEFAULT_LADDER_INVENTORIES.putIfAbsent(ladders, getDefaultFightItems(ladders));
		}
		Collections.unmodifiableMap(this.DEFAULT_LADDER_INVENTORIES);
	}
	public void clearCache() {
		this.DEFAULT_LADDER_INVENTORIES.clear();
	}
	
	public void giveSpawnItem(Player player) {
		player.getInventory().clear();
		player.getInventory().setArmorContents(null);
		if (player.getItemOnCursor() != null) {
			player.setItemOnCursor(null);
		}
		if (player.getGameMode() != GameMode.SURVIVAL) {
			player.setGameMode(GameMode.SURVIVAL);
		}
		
		if (!this.main.getPartyManager().hasParty(player.getUniqueId())) {
			player.getInventory().setItem(0, ItemBuilder.createNewItemStackByMaterial(Material.IRON_SWORD, ChatColor.YELLOW + "Unranked Queue", true));
			player.getInventory().setItem(1, ItemBuilder.createNewItemStackByMaterial(Material.DIAMOND_SWORD, ChatColor.YELLOW + "Ranked Queue", true));
			player.getInventory().setItem(4, ItemBuilder.createNewItemStackByMaterial(Material.NAME_TAG, ChatColor.YELLOW + "Create Party"));
			player.getInventory().setItem(5, ItemBuilder.createNewItemStackByMaterial(Material.EMERALD, ChatColor.YELLOW + "Leaderboards"));
			player.getInventory().setItem(8, ItemBuilder.createNewItemStackByMaterial(Material.BOOK, ChatColor.YELLOW + "Kit Creator/Settings"));
		} else {
			if (PlayerManager.get(player.getUniqueId()).getStatus() == PlayerStatus.DUEL || PlayerManager.get(player.getUniqueId()).getStatus() == PlayerStatus.WAITING) {
				return;
			}
			final Party party = this.main.getPartyManager().getParty(player.getUniqueId());
			ItemStack glass = ItemBuilder.createNewItemStack(new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 14), ChatColor.RED + "2 players needed");
			player.getInventory().setItem(0, party.getSize() == 2 ? ItemBuilder.createNewItemStackByMaterial(Material.IRON_AXE, ChatColor.YELLOW + "2v2 Unranked Queue", true) : glass);
			if (party.getSize() == 2) {
				if (party.getPartyEloManager() != null) {
					player.getInventory().setItem(1, ItemBuilder.createNewItemStackByMaterial(Material.DIAMOND_AXE, ChatColor.YELLOW + "2v2 Ranked Queue", true));
				} else {
					glass = ItemBuilder.createNewItemStack(new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 14), ChatColor.RED + "type \"/party confirm\" to access");
					player.getInventory().setItem(1, glass);
				}
			} else {
				player.getInventory().setItem(1, glass);
			}
			player.getInventory().setItem(5, party.getSize() > 1 ? ItemBuilder.createNewItemStackByMaterial(Material.GOLD_HOE, ChatColor.YELLOW + "Party Game", true) : glass);
			if (party.getPartyState() == PartyState.DUELING) {
				player.getInventory().setItem(2, ItemBuilder.createNewItemStackByMaterial(Material.EYE_OF_ENDER, ChatColor.YELLOW + "Spectate Actual Match"));
			}
			
			giveLeaveItem(player, "Party", false, false);
			player.getInventory().setItem(4, ItemBuilder.createNewItemStackByMaterial(Material.BOOK, ChatColor.YELLOW + "Fight Other Parties"));
			player.getInventory().setItem(7, ItemBuilder.createNewItemStackByMaterial(Material.PAPER, ChatColor.YELLOW + "Party Information"));
		}
		player.updateInventory();
	}
	
	public void giveLeaveItem(Player player, String string, boolean updateInventory) {
		giveLeaveItem(player, string, true, updateInventory);
	}
	private void giveLeaveItem(Player player, String string, boolean clearInventory, boolean updateInventory) {
		// Queue - Spectate - Party - Moderation
		if (clearInventory) {
			player.getInventory().clear();
			player.getInventory().setArmorContents(null);
			if (player.getItemOnCursor() != null) {
				player.setItemOnCursor(null);
			}
		}
		player.getInventory().setItem(8, ItemBuilder.createNewItemStackByMaterial(Material.REDSTONE, ChatColor.RED + "Leave " + string));
		if (updateInventory) {
			player.updateInventory();
		}
	}
	
	public void giveModerationItem(Player player) {
		player.getInventory().clear();
		player.getInventory().setArmorContents(null);
		if (player.getItemOnCursor() != null) {
			player.setItemOnCursor(null);
		}
		if (player.getGameMode() != GameMode.CREATIVE) {
			player.setGameMode(GameMode.CREATIVE);
		}
		
		final ItemStack s = ItemBuilder.createNewItemStackByMaterial(Material.WOOD_SWORD, ChatColor.RED + "Knockback V", true);
		s.addUnsafeEnchantment(Enchantment.KNOCKBACK, 5);
		
		giveLeaveItem(player, "Moderation", false);
		
		player.getInventory().setItem(0, s);
		player.getInventory().setItem(1, ItemBuilder.createNewItemStackByMaterial(Material.WATCH, ChatColor.RED + "See Random Player"));
		player.getInventory().setItem(2, ItemBuilder.createNewItemStackByMaterial(Material.PACKED_ICE, ChatColor.RED + "Freeze Someone"));
		player.getInventory().setItem(3, ItemBuilder.createNewItemStackByMaterial(Material.BOOK, ChatColor.RED + "Inspection Tool"));
		player.updateInventory();
	}
	
	public void giveSpectatorItems(Player player) {
		final boolean spectatingPlayer = PlayerManager.get(player.getUniqueId()).getSpectate() != null;
		final boolean hasParty = this.main.getPartyManager().hasParty(player.getUniqueId());
		player.getInventory().clear();
		player.getInventory().setArmorContents(null);
		if (player.getItemOnCursor() != null) {
			player.setItemOnCursor(null);
		}
		
		giveLeaveItem(player, "Spectate", false);
		
		if (!hasParty) {
			player.getInventory().setItem(0, ItemBuilder.createNewItemStackByMaterial((spectatingPlayer ? Material.WATCH : Material.MAP), ChatColor.GREEN + (spectatingPlayer ? "See current arena" : "Change arena")));
		}
		player.getInventory().setItem((!hasParty ? 1 : 0), ItemBuilder.createNewItemStackByMaterial(Material.EYE_OF_ENDER, ChatColor.GREEN + "See all spectators"));
		player.getInventory().setItem((!hasParty ? 2 : 1), ItemBuilder.createNewItemStack(new ItemStack(Material.WOOL, 1, (short) new Random().nextInt(15)), ChatColor.GREEN + "Change Fly/Walk Speed"));
		player.updateInventory();
	}
	
	public void giveKitSelectionItems(Player player, Ladders ladder) {
		player.getInventory().clear();
		player.getInventory().setArmorContents(null);
		if (player.getItemOnCursor() != null) {
			player.setItemOnCursor(null);
		}
		if (ladder != Ladders.SUMO) {
			final PlayerManager pm = PlayerManager.get(player.getUniqueId());
			player.getInventory().setItem(0, ItemBuilder.createNewItemStackByMaterial(Material.ENCHANTED_BOOK, ChatColor.YELLOW + ladder.getName() + " default kit"));
			if (ladder.isEditable() && !pm.getCustomLadderKitFromLadder(ladder).isEmpty()) {
				for (EditedLadderKit customKit : pm.getCustomLadderKitFromLadder(ladder)) {
					player.getInventory().setItem(customKit.getSlot() + 1, ItemBuilder.createNewItemStackByMaterial(Material.ENCHANTED_BOOK, ChatColor.YELLOW + ChatColor.translateAlternateColorCodes('&', customKit.getName())));
				}
			}
		}
		player.updateInventory();
	}
	public void giveFightItems(Player player, Ladders ladder, int slot) {
		this.giveFightItems(player, ladder, slot, true, false);
	}
	public void giveFightItems(Player player, Ladders ladder, int slot, boolean armor, boolean edit) {
		player.getInventory().clear();
		final EditedLadderKit customKit = PlayerManager.get(player.getUniqueId()).getCustomLadderKitFromSlot(ladder, (slot - (!edit ? 1 : 0)));
		Inventory inventory = (slot == 0 ? this.main.getItemManager().getDefaultFightItems(ladder) : customKit.getInventory());
		final boolean isDefault = inventory.getContents() == this.getDefaultFightItems(ladder).getContents();
		int i = 0;
		for (ItemStack items : inventory.getContents()) {
			player.getInventory().setItem(i, items);
			i++;
			if (i >= 36) break; // cut the loop
		}
		if (!edit) {
			player.sendMessage(ChatColor.GREEN.toString() + "You equipped the " + (isDefault ? "default" : ChatColor.translateAlternateColorCodes('&', customKit.getName())) + ChatColor.GREEN + " kit for " + ladder.getName() + ".");
		}
		if (armor) {
			if (!isDefault) {
				inventory = this.getDefaultFightItems(ladder);
			}
			player.getInventory().setArmorContents(new ItemStack[] {inventory.getItem(36), inventory.getItem(37), inventory.getItem(38), inventory.getItem(39)});
		}
        player.updateInventory();
	}
	public Inventory getDefaultFightItems(Ladders ladder) {
		if (this.DEFAULT_LADDER_INVENTORIES.containsKey(ladder)) {
			return this.DEFAULT_LADDER_INVENTORIES.get(ladder);
		}
		final Inventory inventory = this.main.getServer().createInventory(null, InventoryType.PLAYER);
		ItemStack attackItem = new ItemStack(Material.DIAMOND_SWORD, 1);
		ItemStack helmet = new ItemStack(Material.DIAMOND_HELMET, 1);
		ItemStack chestplate = new ItemStack(Material.DIAMOND_CHESTPLATE, 1);
		ItemStack leggings = new ItemStack(Material.DIAMOND_LEGGINGS, 1);
		ItemStack boots = new ItemStack(Material.DIAMOND_BOOTS, 1);
		
		switch (ladder) {
		case NODEBUFF: {
			attackItem.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 2);
			attackItem.addUnsafeEnchantment(Enchantment.FIRE_ASPECT, 2);
			attackItem.addUnsafeEnchantment(Enchantment.DURABILITY, 3);
			
			helmet.addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 2);
			helmet.addUnsafeEnchantment(Enchantment.DURABILITY, 3);
			
			chestplate.addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 2);
			chestplate.addUnsafeEnchantment(Enchantment.DURABILITY, 3);
			
			leggings.addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 2);
			leggings.addUnsafeEnchantment(Enchantment.DURABILITY, 3);
			
			boots.addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 2);
			boots.addUnsafeEnchantment(Enchantment.PROTECTION_FALL, 4);
			boots.addUnsafeEnchantment(Enchantment.DURABILITY, 3);
			
			final ItemStack pearl = new ItemStack(Material.ENDER_PEARL, 16);
			final ItemStack steak = new ItemStack(Material.COOKED_BEEF, 64);
			final ItemStack speed = new ItemStack(Material.POTION, 1, (short) 8226);
			final ItemStack fire = new ItemStack(Material.POTION, 1, (short) 8259);
			
			while (inventory.firstEmpty() != -1) {
				inventory.addItem(new ItemStack(Material.POTION, 1, (short) 16421));
			}
			
			inventory.setItem(1, pearl);
			inventory.setItem(2, speed);
			inventory.setItem(3, fire);
			inventory.setItem(8, steak);
			
			inventory.setItem(17, speed);
			inventory.setItem(26, speed);
			break;
		}
		case ARCHER: {
			attackItem = new ItemStack(Material.BOW, 1);
			attackItem.addUnsafeEnchantment(Enchantment.ARROW_DAMAGE, 3);
			attackItem.addUnsafeEnchantment(Enchantment.ARROW_INFINITE, 1);
			
			helmet = new ItemStack(Material.LEATHER_HELMET, 1);
			helmet.addUnsafeEnchantment(Enchantment.PROTECTION_PROJECTILE, 1);
			
			chestplate = new ItemStack(Material.LEATHER_CHESTPLATE, 1);
			chestplate.addUnsafeEnchantment(Enchantment.PROTECTION_PROJECTILE, 1);
			
			leggings = new ItemStack(Material.LEATHER_LEGGINGS, 1);
			leggings.addUnsafeEnchantment(Enchantment.PROTECTION_PROJECTILE, 1);
			
			boots = new ItemStack(Material.LEATHER_BOOTS, 1);
			boots.addUnsafeEnchantment(Enchantment.PROTECTION_PROJECTILE, 1);
			
			final ItemStack carrots = new ItemStack(Material.GOLDEN_CARROT, 16);
			final ItemStack arrow = new ItemStack(Material.ARROW, 1);
			
			inventory.setItem(1, carrots);
			inventory.setItem(2, arrow);
			break;
		}
		case AXE: {
			attackItem = new ItemStack(Material.IRON_AXE, 1);
			attackItem.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 3);
			
			helmet = new ItemStack(Material.IRON_HELMET, 1);
			chestplate = new ItemStack(Material.IRON_CHESTPLATE, 1);
			leggings = new ItemStack(Material.IRON_LEGGINGS, 1);
			boots = new ItemStack(Material.IRON_BOOTS, 1);
			final ItemStack apples = new ItemStack(Material.GOLDEN_APPLE, 16);
			final ItemStack speed = new ItemStack(Material.POTION, 1, (short) 8226);
			final ItemStack heal = new ItemStack(Material.POTION, 1, (short) 16421);
			
			inventory.setItem(1, apples);
			inventory.setItem(2, speed);
			inventory.setItem(3, heal);
			inventory.setItem(4, heal);
			inventory.setItem(5, heal);
			inventory.setItem(6, heal);
			inventory.setItem(7, heal);
			inventory.setItem(8, heal);
			
			inventory.setItem(34, heal);
			inventory.setItem(35, speed);
			break;
		}
		case SOUP: {
			helmet = new ItemStack(Material.IRON_HELMET, 1);
			chestplate = new ItemStack(Material.IRON_CHESTPLATE, 1);
			leggings = new ItemStack(Material.IRON_LEGGINGS, 1);
			boots = new ItemStack(Material.IRON_BOOTS, 1);
			
			final ItemStack speed = new ItemStack(Material.POTION, 1, (short) 8226);
			
			while (inventory.firstEmpty() != -1) {
				inventory.addItem(new ItemStack(Material.MUSHROOM_SOUP, 1));
			}
			
			inventory.setItem(1, speed);
			
			inventory.setItem(17, speed);
			inventory.setItem(26, speed);
			inventory.setItem(35, speed);
			break;
		}
		case EARLY_HG: {
			attackItem = new ItemStack(Material.STONE_SWORD, 1);
			final ItemMeta im = attackItem.getItemMeta();
			im.spigot().setUnbreakable(true);
			attackItem.setItemMeta(im);
			
			helmet = chestplate = leggings = boots = null;
			inventory.setItem(14, new ItemStack(Material.BOWL, 32));
			inventory.setItem(13, new ItemStack(Material.RED_MUSHROOM, 32));
			inventory.setItem(15, new ItemStack(Material.BROWN_MUSHROOM, 32));
			while (inventory.firstEmpty() != -1) {
				inventory.addItem(new ItemStack(Material.MUSHROOM_SOUP));
			} 
			break;
		}
		case GAPPLE: {
			attackItem.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 5);
			attackItem.addUnsafeEnchantment(Enchantment.FIRE_ASPECT, 2);
			attackItem.addUnsafeEnchantment(Enchantment.DURABILITY, 3);
			
			helmet.addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 4);
			helmet.addUnsafeEnchantment(Enchantment.DURABILITY, 3);
			chestplate.addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 4);
			chestplate.addUnsafeEnchantment(Enchantment.DURABILITY, 3);
			leggings.addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 4);
			leggings.addUnsafeEnchantment(Enchantment.DURABILITY, 3);
			boots.addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 4);
			boots.addUnsafeEnchantment(Enchantment.DURABILITY, 3);
			
			inventory.setItem(1, new ItemStack(Material.GOLDEN_APPLE, 64, (short) 1));
			inventory.setItem(2, helmet);
			inventory.setItem(3, chestplate);
			inventory.setItem(4, leggings);
			inventory.setItem(5, boots);
			inventory.setItem(7, ItemBuilder.createCustomPotionItem(ChatColor.YELLOW + "Potion of Speed II",PotionEffectType.SPEED, 480, 1));
			inventory.setItem(8, ItemBuilder.createCustomPotionItem(ChatColor.YELLOW + "Potion of Force II", PotionEffectType.INCREASE_DAMAGE, 480, 1));
			break;
		}
		case COMBO: {
			attackItem.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 5);
			attackItem.addUnsafeEnchantment(Enchantment.FIRE_ASPECT, 2);
			attackItem.addUnsafeEnchantment(Enchantment.DURABILITY, 3);
			
			helmet.addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 4);
			helmet.addUnsafeEnchantment(Enchantment.DURABILITY, 3);
			chestplate.addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 4);
			chestplate.addUnsafeEnchantment(Enchantment.DURABILITY, 3);
			leggings.addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 4);
			leggings.addUnsafeEnchantment(Enchantment.DURABILITY, 3);
			boots.addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 4);
			boots.addUnsafeEnchantment(Enchantment.DURABILITY, 3);
			
			inventory.setItem(1, new ItemStack(Material.GOLDEN_APPLE, 64, (short) 1));
			inventory.setItem(2, helmet);
			inventory.setItem(3, chestplate);
			inventory.setItem(4, leggings);
			inventory.setItem(5, boots);
			inventory.setItem(7, ItemBuilder.createCustomPotionItem(ChatColor.YELLOW + "Potion of Speed II", PotionEffectType.SPEED, 480, 1));
			inventory.setItem(8, ItemBuilder.createCustomPotionItem(ChatColor.YELLOW + "Potion of Force I", PotionEffectType.INCREASE_DAMAGE, 480, 0));
			break;
		}
		case BOXING: {
			attackItem = new ItemStack(Material.WOOD_SWORD, 1);
			ItemMeta swordMeta = attackItem.getItemMeta();
			swordMeta.spigot().setUnbreakable(true);
			attackItem.setItemMeta(swordMeta);
			attackItem.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 1);
			inventory.setItem(1, attackItem);
			
			attackItem = new ItemStack(Material.STONE_SWORD, 1);
			swordMeta = attackItem.getItemMeta();
			swordMeta.spigot().setUnbreakable(true);
			attackItem.setItemMeta(swordMeta);
			attackItem.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 1);
			inventory.setItem(2, attackItem);
			
			attackItem = new ItemStack(Material.IRON_SWORD, 1);
			swordMeta = attackItem.getItemMeta();
			swordMeta.spigot().setUnbreakable(true);
			attackItem.setItemMeta(swordMeta);
			attackItem.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 1);
			inventory.setItem(3, attackItem);
			
			attackItem = new ItemStack(Material.GOLD_SWORD, 1);
			swordMeta = attackItem.getItemMeta();
			swordMeta.spigot().setUnbreakable(true);
			attackItem.setItemMeta(swordMeta);
			attackItem.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 1);
			inventory.setItem(4, attackItem);
			
			attackItem = new ItemStack(Material.DIAMOND_SWORD, 1);
			swordMeta = attackItem.getItemMeta();
			swordMeta.spigot().setUnbreakable(true);
			attackItem.setItemMeta(swordMeta);
			attackItem.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 1);
			helmet = chestplate = leggings = boots = null;
			break;
		}
		case NOENCHANT: {
			final ItemStack pearl = new ItemStack(Material.ENDER_PEARL, 16);
			final ItemStack steak = new ItemStack(Material.COOKED_BEEF, 64);
			final ItemStack speed = new ItemStack(Material.POTION, 1, (short) 8226);
			
			while (inventory.firstEmpty() != -1) {
				inventory.addItem(new ItemStack(Material.POTION, 1, (short) 16421));
			}
			
			inventory.setItem(1, pearl);
			inventory.setItem(2, speed);
			inventory.setItem(8, steak);
			
			inventory.setItem(17, speed);
			inventory.setItem(26, speed);
			break;
		}
		case CLASSIC: {
			final ItemStack gapple = new ItemStack(Material.GOLDEN_APPLE, 8);
			final ItemStack arrow = new ItemStack(Material.ARROW, 12);
			final ItemStack bow = new ItemStack(Material.BOW, 1);
			final ItemStack rod = new ItemStack(Material.FISHING_ROD, 1);
			final ItemStack steak = new ItemStack(Material.COOKED_BEEF, 64);
			
			inventory.setItem(1, bow);
			inventory.setItem(2, rod);
			inventory.setItem(3, gapple);
			inventory.setItem(8, steak);
			
			inventory.setItem(17, arrow);
			break;
		}
		default:
			break;
		}
		inventory.setItem(36, boots);
		inventory.setItem(37, leggings);
		inventory.setItem(38, chestplate);
		inventory.setItem(39, helmet);
		inventory.setItem(0, attackItem);
		return inventory;
	}
}
