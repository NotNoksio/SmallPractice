package us.noks.smallpractice.objects.managers;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import us.noks.smallpractice.enums.Ladders;
import us.noks.smallpractice.party.Party;

public class ItemManager {
	private static ItemManager instance = new ItemManager();
	public static ItemManager getInstace() {
		return instance;
	}
	
	public void giveSpawnItem(Player player) {
		player.getInventory().clear();
		player.getInventory().setArmorContents(null);
		player.setItemOnCursor(null);
		
		player.setGameMode(GameMode.SURVIVAL);
		
		if (!PartyManager.getInstance().hasParty(player.getUniqueId())) {
			ItemStack u = new ItemStack(Material.IRON_SWORD, 1);
			ItemMeta um = u.getItemMeta();
			um.setDisplayName(ChatColor.YELLOW + "Unranked Queue");
			um.spigot().setUnbreakable(true);
			u.setItemMeta(um);
			
			ItemStack r = new ItemStack(Material.DIAMOND_SWORD, 1);
			ItemMeta rm = r.getItemMeta();
			rm.setDisplayName(ChatColor.YELLOW + "Ranked Queue");
			rm.spigot().setUnbreakable(true);
			r.setItemMeta(rm);
			
			ItemStack n = new ItemStack(Material.NAME_TAG, 1);
			ItemMeta nm = n.getItemMeta();
			nm.setDisplayName(ChatColor.YELLOW + "Create Party");
			n.setItemMeta(nm);
			
			ItemStack c = new ItemStack(Material.COMPASS, 1);
			ItemMeta cm = c.getItemMeta();
			cm.setDisplayName(ChatColor.YELLOW + "Warps Selection");
			c.setItemMeta(cm);
			
			ItemStack b = new ItemStack(Material.BOOK, 1);
			ItemMeta bm = b.getItemMeta();
			bm.setDisplayName(ChatColor.YELLOW + "Edit Kit/Settings");
			b.setItemMeta(bm);
			
			player.getInventory().setItem(0, u);
			player.getInventory().setItem(1, r);
			player.getInventory().setItem(4, n);
			player.getInventory().setItem(5, c);
			player.getInventory().setItem(8, b);
		} else {
			Party party = PartyManager.getInstance().getParty(player.getUniqueId());
			
			ItemStack u = new ItemStack(Material.IRON_SWORD, 1);
			ItemMeta um = u.getItemMeta();
			um.setDisplayName(ChatColor.YELLOW + "2v2 Unranked Queue");
			um.spigot().setUnbreakable(true);
			u.setItemMeta(um);
			
			ItemStack r = new ItemStack(Material.DIAMOND_SWORD, 1);
			ItemMeta rm = r.getItemMeta();
			rm.setDisplayName(ChatColor.YELLOW + "2v2 Ranked Queue");
			rm.spigot().setUnbreakable(true);
			r.setItemMeta(rm);
			
			ItemStack a = new ItemStack(Material.ARROW, 1);
			ItemMeta am = a.getItemMeta();
			am.setDisplayName(ChatColor.YELLOW + "Split Teams");
			a.setItemMeta(am);
			
			ItemStack b = new ItemStack(Material.BOOK, 1);
			ItemMeta bm = b.getItemMeta();
			bm.setDisplayName(ChatColor.YELLOW + "Fight Other Parties");
			b.setItemMeta(bm);
			
			ItemStack p = new ItemStack(Material.PAPER, 1);
			ItemMeta pm = p.getItemMeta();
			pm.setDisplayName(ChatColor.YELLOW + "Party Information");
			p.setItemMeta(pm);
			
			ItemStack glass = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 14);
			ItemMeta glassm = glass.getItemMeta();
			glassm.setDisplayName(ChatColor.RED + "2 players needed");
			glass.setItemMeta(glassm);
			
			giveLeaveItem(player, "Party", false);
			
			final boolean able = party.getSize() > 1;
			player.getInventory().setItem(0, (able ? u : glass));
			player.getInventory().setItem(1, (able ? r : glass));
			player.getInventory().setItem(4, b);
			player.getInventory().setItem(5, (able ? a : glass));
			player.getInventory().setItem(7, p);
		}
		player.updateInventory();
	}
	
	public void giveLeaveItem(Player player, String string) {
		giveLeaveItem(player, string, true);
	}
	private void giveLeaveItem(Player player, String string, boolean clearInventory) {
		// Queue - Spectate - Party - Moderation
		if (clearInventory) {
			player.getInventory().clear();
			player.getInventory().setArmorContents(null);
			player.setItemOnCursor(null);
		}
		
		ItemStack r = new ItemStack(Material.REDSTONE, 1);
		ItemMeta rm = r.getItemMeta();
		rm.setDisplayName(ChatColor.RED + "Leave " + string);
		r.setItemMeta(rm);
		
		player.getInventory().setItem(8, r);
		player.updateInventory();
	}
	
	public void giveModerationItem(Player player) {
		player.getInventory().clear();
		player.getInventory().setArmorContents(null);
		player.setItemOnCursor(null);
		
		player.setGameMode(GameMode.CREATIVE);
		
		ItemStack s = new ItemStack(Material.WOOD_SWORD, 1);
		ItemMeta sm = s.getItemMeta();
		sm.setDisplayName(ChatColor.RED + "Knockback V");
		sm.spigot().setUnbreakable(true);
		s.setItemMeta(sm);
		s.addUnsafeEnchantment(Enchantment.KNOCKBACK, 5);
		
		ItemStack a = new ItemStack(Material.WATCH, 1);
		ItemMeta am = a.getItemMeta();
		am.setDisplayName(ChatColor.RED + "See Random Player");
		a.setItemMeta(am);
		
		ItemStack ice = new ItemStack(Material.PACKED_ICE, 1);
	    ItemMeta icem = ice.getItemMeta();
	    icem.setDisplayName(ChatColor.RED + "Freeze Player");
	    ice.setItemMeta(icem);
	    
	    ItemStack b = new ItemStack(Material.BOOK, 1);
	    ItemMeta bm = b.getItemMeta();
	    bm.setDisplayName(ChatColor.RED + "Inspection Tool");
	    b.setItemMeta(bm);
		
		giveLeaveItem(player, "Moderation", false);
		
		player.getInventory().setItem(0, s);
		player.getInventory().setItem(1, a);
		player.getInventory().setItem(2, ice);
		player.getInventory().setItem(3, b);
		player.updateInventory();
	}
	
	public void givePreFightItems(Player player, Ladders ladder) {
		player.getInventory().clear();
		player.getInventory().setArmorContents(null);
		player.setItemOnCursor(null);
		
		if (ladder != Ladders.SUMO) {
			ItemStack b = new ItemStack(Material.ENCHANTED_BOOK, 1);
			ItemMeta bm = b.getItemMeta();
			bm.setDisplayName(ChatColor.YELLOW + ladder.getName() + " default kit");
			b.setItemMeta(bm);
			
			player.getInventory().setItem(0, b);
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
			attackItem.addUnsafeEnchantment(Enchantment.ARROW_DAMAGE, 3);
			
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
		default:
			break;
		}
		player.getInventory().setArmorContents(new ItemStack[] {boots, leggings, chestplate, helmet});
		player.getInventory().setItem(0, attackItem);
		
		player.updateInventory();
	}
	
	public void giveBridgeItems(Player player) {
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
	    PlayerManager pm = PlayerManager.get(player.getUniqueId());
	    pm.showAllPlayer();
	}
}
