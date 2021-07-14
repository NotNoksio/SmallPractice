package us.noks.smallpractice.objects.managers;

import java.util.Random;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import us.noks.smallpractice.enums.Ladders;
import us.noks.smallpractice.party.Party;
import us.noks.smallpractice.party.PartyState;
import us.noks.smallpractice.utils.ItemBuilder;

public class ItemManager {
	
	public void giveSpawnItem(Player player) {
		player.getInventory().clear();
		player.getInventory().setArmorContents(null);
		player.setItemOnCursor(null);
		
		player.setGameMode(GameMode.SURVIVAL);
		
		if (!PartyManager.getInstance().hasParty(player.getUniqueId())) {
			player.getInventory().setItem(0, ItemBuilder.getInstance().createNewItemStackByMaterial(Material.IRON_SWORD, ChatColor.YELLOW + "Unranked Queue", true));
			player.getInventory().setItem(1, ItemBuilder.getInstance().createNewItemStackByMaterial(Material.DIAMOND_SWORD, ChatColor.YELLOW + "Ranked Queue", true));
			player.getInventory().setItem(4, ItemBuilder.getInstance().createNewItemStackByMaterial(Material.NAME_TAG, ChatColor.YELLOW + "Create Party"));
			player.getInventory().setItem(5, ItemBuilder.getInstance().createNewItemStackByMaterial(Material.GOLD_AXE, ChatColor.YELLOW + "Mini-Game", true));
			player.getInventory().setItem(8, ItemBuilder.getInstance().createNewItemStackByMaterial(Material.BOOK, ChatColor.YELLOW + "Kit Creator/Settings"));
		} else {
			Party party = PartyManager.getInstance().getParty(player.getUniqueId());
			ItemStack glass = ItemBuilder.getInstance().createNewItemStack(new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 14), ChatColor.RED + "2 players needed");
			
			player.getInventory().setItem(0, glass);
			player.getInventory().setItem(1, glass);
			player.getInventory().setItem(5, glass);
			
			final boolean able = party.getSize() > 1;
			if (able) {
				player.getInventory().setItem(0, ItemBuilder.getInstance().createNewItemStackByMaterial(Material.IRON_SWORD, ChatColor.YELLOW + "2v2 Unranked Queue", true));
				player.getInventory().setItem(1, ItemBuilder.getInstance().createNewItemStackByMaterial(Material.DIAMOND_SWORD, ChatColor.YELLOW + "2v2 Ranked Queue", true));
				player.getInventory().setItem(5, ItemBuilder.getInstance().createNewItemStackByMaterial(Material.ARROW, ChatColor.YELLOW + "Split Teams"));
			}
			if (party.getPartyState() == PartyState.DUELING) {
				player.getInventory().setItem(2, ItemBuilder.getInstance().createNewItemStackByMaterial(Material.BONE, ChatColor.YELLOW + "Spectate Actual Match"));
			}
			
			giveLeaveItem(player, "Party", false);
			player.getInventory().setItem(4, ItemBuilder.getInstance().createNewItemStackByMaterial(Material.BOOK, ChatColor.YELLOW + "Fight Other Parties"));
			player.getInventory().setItem(7, ItemBuilder.getInstance().createNewItemStackByMaterial(Material.PAPER, ChatColor.YELLOW + "Party Information"));
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
			player.setItemOnCursor(null);
		}
		player.getInventory().setItem(8, ItemBuilder.getInstance().createNewItemStackByMaterial(Material.REDSTONE, ChatColor.RED + "Leave " + string));
		if (updateInventory) {
			player.updateInventory();
		}
	}
	
	public void giveModerationItem(Player player) {
		player.getInventory().clear();
		player.getInventory().setArmorContents(null);
		player.setItemOnCursor(null);
		
		player.setGameMode(GameMode.CREATIVE);
		
		ItemStack s = ItemBuilder.getInstance().createNewItemStackByMaterial(Material.WOOD_SWORD, ChatColor.RED + "Knockback V", true);
		s.addUnsafeEnchantment(Enchantment.KNOCKBACK, 5);
		
		giveLeaveItem(player, "Moderation", false);
		
		player.getInventory().setItem(0, s);
		player.getInventory().setItem(1, ItemBuilder.getInstance().createNewItemStackByMaterial(Material.WATCH, ChatColor.RED + "See Random Player"));
		player.getInventory().setItem(2, ItemBuilder.getInstance().createNewItemStackByMaterial(Material.PACKED_ICE, ChatColor.RED + "Freeze Someone"));
		player.getInventory().setItem(3, ItemBuilder.getInstance().createNewItemStackByMaterial(Material.BOOK, ChatColor.RED + "Inspection Tool"));
		player.updateInventory();
	}
	
	// TODO: Spectating a player/spectating an arena (different inventory)
	public void giveSpectatorItems(Player player, boolean spectatingPlayer) {
		player.getInventory().clear();
		player.getInventory().setArmorContents(null);
		player.setItemOnCursor(null);
		
		giveLeaveItem(player, "Spectate", false);
		
		player.getInventory().setItem(0, ItemBuilder.getInstance().createNewItemStackByMaterial((spectatingPlayer ? Material.WATCH : Material.MAP), ChatColor.GREEN + (spectatingPlayer ? "See current arena" : "Change arena")));
		player.getInventory().setItem(1, ItemBuilder.getInstance().createNewItemStackByMaterial(Material.EYE_OF_ENDER, ChatColor.GREEN + "See all spectators"));
		player.getInventory().setItem(2, ItemBuilder.getInstance().createNewItemStack(new ItemStack(Material.WOOL, 1, (short) new Random().nextInt(15)), ChatColor.GREEN + "Change Fly/Walk Speed"));
		player.updateInventory();
	}
	
	public void givePreFightItems(Player player, Ladders ladder) {
		player.getInventory().clear();
		player.getInventory().setArmorContents(null);
		player.setItemOnCursor(null);
		
		if (ladder != Ladders.SUMO) {
			player.getInventory().setItem(0, ItemBuilder.getInstance().createNewItemStackByMaterial(Material.ENCHANTED_BOOK, ChatColor.YELLOW + ladder.getName() + " default kit"));
		}
		player.updateInventory();
	}
	public void giveFightItems(Player player, Ladders ladder) {
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
			
			ItemStack pearl = new ItemStack(Material.ENDER_PEARL, 16);
			ItemStack steak = new ItemStack(Material.COOKED_BEEF, 64);
			ItemStack speed = new ItemStack(Material.POTION, 1, (short) 8226);
			ItemStack fire = new ItemStack(Material.POTION, 1, (short) 8259);
			
			while (player.getInventory().firstEmpty() != -1) {
				player.getInventory().addItem(new ItemStack(Material.POTION, 1, (short) 16421));
			}
			
			player.getInventory().setItem(1, pearl);
			player.getInventory().setItem(2, speed);
			player.getInventory().setItem(3, fire);
			player.getInventory().setItem(8, steak);
			
			player.getInventory().setItem(17, speed);
			player.getInventory().setItem(26, speed);
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
			
			ItemStack carrots = new ItemStack(Material.GOLDEN_CARROT, 16);
			ItemStack arrow = new ItemStack(Material.ARROW, 1);
			
			player.getInventory().setItem(1, carrots);
			player.getInventory().setItem(2, arrow);
			break;
		}
		case AXE: {
			attackItem = new ItemStack(Material.IRON_AXE, 1);
			attackItem.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 3);
			
			helmet = new ItemStack(Material.IRON_HELMET, 1);
			chestplate = new ItemStack(Material.IRON_CHESTPLATE, 1);
			leggings = new ItemStack(Material.IRON_LEGGINGS, 1);
			boots = new ItemStack(Material.IRON_BOOTS, 1);
			ItemStack apples = new ItemStack(Material.GOLDEN_APPLE, 16);
			ItemStack speed = new ItemStack(Material.POTION, 1, (short) 8226);
			ItemStack heal = new ItemStack(Material.POTION, 1, (short) 16421);
			
			player.getInventory().setItem(1, apples);
			player.getInventory().setItem(2, speed);
			player.getInventory().setItem(3, heal);
			player.getInventory().setItem(4, heal);
			player.getInventory().setItem(5, heal);
			player.getInventory().setItem(6, heal);
			player.getInventory().setItem(7, heal);
			player.getInventory().setItem(8, heal);
			
			player.getInventory().setItem(34, heal);
			player.getInventory().setItem(35, speed);
			break;
		}
		case SOUP: {
			helmet = new ItemStack(Material.IRON_HELMET, 1);
			chestplate = new ItemStack(Material.IRON_CHESTPLATE, 1);
			leggings = new ItemStack(Material.IRON_LEGGINGS, 1);
			boots = new ItemStack(Material.IRON_BOOTS, 1);
			
			ItemStack speed = new ItemStack(Material.POTION, 1, (short) 8226);
			
			while (player.getInventory().firstEmpty() != -1) {
				player.getInventory().addItem(new ItemStack(Material.MUSHROOM_SOUP, 1));
			}
			
			player.getInventory().setItem(1, speed);
			
			player.getInventory().setItem(17, speed);
			player.getInventory().setItem(26, speed);
			player.getInventory().setItem(35, speed);
			break;
		}
		case EARLY_HG: {
			attackItem = new ItemStack(Material.STONE_SWORD, 1);
			ItemMeta im = attackItem.getItemMeta();
			im.spigot().setUnbreakable(true);
			attackItem.setItemMeta(im);
			
			helmet = null;
			chestplate = null;
			leggings = null;
			boots = null;
			player.getInventory().setItem(14, new ItemStack(Material.BOWL, 32));
			player.getInventory().setItem(13, new ItemStack(Material.RED_MUSHROOM, 32));
			player.getInventory().setItem(15, new ItemStack(Material.BROWN_MUSHROOM, 32));
			while (player.getInventory().firstEmpty() != -1) {
				player.getInventory().addItem(new ItemStack(Material.MUSHROOM_SOUP));
			} 
			break;
		}
		default:
			break;
		}
		player.getInventory().setArmorContents(new ItemStack[] {boots, leggings, chestplate, helmet});
		player.getInventory().setItem(0, attackItem);
		
		player.updateInventory();
	}
	
	public void giveBridgeItems(Player player) {
		PlayerManager pm = PlayerManager.get(player.getUniqueId());
		player.getInventory().clear();
		player.getInventory().setArmorContents(null);
		player.setItemOnCursor(null);
	    
	    ItemStack item = new ItemStack(Material.SANDSTONE, 64, (short)2);
	    for (int i = 0; i < player.getInventory().getSize(); i++) {
	    	player.getInventory().setItem(i, item);
	    }
	    
	    ItemStack i = new ItemStack(Material.WOOD_PICKAXE);
	    ItemMeta im = i.getItemMeta();
	    im.spigot().setUnbreakable(true);
	    i.setItemMeta(im);
	    i.addUnsafeEnchantment(Enchantment.DIG_SPEED, 2);
	    i.addUnsafeEnchantment(Enchantment.KNOCKBACK, 5);
	    i.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 1);
	    
	    player.getInventory().setItem(0, i);
	    
	    player.updateInventory();
	    pm.showAllPlayer();
	}
}
