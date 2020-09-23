package us.noks.smallpractice.objects.managers;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

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
			um.setDisplayName(ChatColor.YELLOW + "Unranked Direct Queue");
			um.spigot().setUnbreakable(true);
			u.setItemMeta(um);
			
			ItemStack r = new ItemStack(Material.DIAMOND_SWORD, 1);
			ItemMeta rm = r.getItemMeta();
			rm.setDisplayName(ChatColor.YELLOW + "Ranked Direct Queue");
			rm.spigot().setUnbreakable(true);
			r.setItemMeta(rm);
			
			ItemStack n = new ItemStack(Material.NAME_TAG, 1);
			ItemMeta nm = n.getItemMeta();
			nm.setDisplayName(ChatColor.YELLOW + "Create Party");
			n.setItemMeta(nm);
			
			player.getInventory().setItem(0, u);
			player.getInventory().setItem(1, r);
			player.getInventory().setItem(8, n);
		} else {
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
			
			giveLeaveItem(player, "Party", false);
			
			player.getInventory().setItem(0, a);
			player.getInventory().setItem(2, b);
			player.getInventory().setItem(5, p);
		}
		player.updateInventory();
	}
	
	public void giveLeaveItem(Player player, String string) {
		giveLeaveItem(player, string, true);
	}
	private void giveLeaveItem(Player player, String string, boolean clearInventory) {
		// Queue - Spectate - Party
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
		
		ItemStack r = new ItemStack(Material.REDSTONE, 1);
		ItemMeta rm = r.getItemMeta();
		rm.setDisplayName(ChatColor.RED + "Leave Moderation");
		r.setItemMeta(rm);
		
		player.getInventory().setItem(8, r);
		player.getInventory().setItem(0, s);
		player.getInventory().setItem(1, a);
		player.updateInventory();
	}
	
	public void giveKit(Player player) {
		player.getInventory().clear();
		player.getInventory().setArmorContents(null);
		player.setItemOnCursor(null);
		
		ItemStack swo = new ItemStack(Material.DIAMOND_SWORD, 1);
		swo.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 2);
		swo.addUnsafeEnchantment(Enchantment.FIRE_ASPECT, 2);
		swo.addUnsafeEnchantment(Enchantment.DURABILITY, 3);
		
		ItemStack hel = new ItemStack(Material.DIAMOND_HELMET, 1);
		hel.addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 2);
		hel.addUnsafeEnchantment(Enchantment.DURABILITY, 3);
		
		ItemStack che = new ItemStack(Material.DIAMOND_CHESTPLATE, 1);
		che.addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 2);
		che.addUnsafeEnchantment(Enchantment.DURABILITY, 3);
		
		ItemStack leg = new ItemStack(Material.DIAMOND_LEGGINGS, 1);
		leg.addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 2);
		leg.addUnsafeEnchantment(Enchantment.DURABILITY, 3);
		
		ItemStack boo = new ItemStack(Material.DIAMOND_BOOTS, 1);
		boo.addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 2);
		boo.addUnsafeEnchantment(Enchantment.PROTECTION_FALL, 4);
		boo.addUnsafeEnchantment(Enchantment.DURABILITY, 3);
		
		ItemStack pearl = new ItemStack(Material.ENDER_PEARL, 16);
		ItemStack steak = new ItemStack(Material.COOKED_BEEF, 64);
		ItemStack speed = new ItemStack(Material.POTION, 1, (short) 8226);
		ItemStack fire = new ItemStack(Material.POTION, 1, (short) 8259);
		
		player.getInventory().setHelmet(hel);
		player.getInventory().setChestplate(che);
		player.getInventory().setLeggings(leg);
		player.getInventory().setBoots(boo);
		
		while (player.getInventory().firstEmpty() != -1) {
			player.getInventory().addItem(new ItemStack(Material.POTION, 1, (short) 16421));
		}
		
		player.getInventory().setItem(0, swo);
		player.getInventory().setItem(1, pearl);
		player.getInventory().setItem(2, speed);
		player.getInventory().setItem(3, fire);
		player.getInventory().setItem(8, steak);
		
		player.getInventory().setItem(17, speed);
		player.getInventory().setItem(26, speed);
		player.updateInventory();
	}
}
