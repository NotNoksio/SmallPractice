package us.noks.smallpractice.utils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;

import com.google.common.collect.Lists;

import us.noks.smallpractice.objects.MatchStats;
import us.noks.smallpractice.objects.managers.PlayerManager;

public class InvView {
	private static InvView instance = new InvView();
	public static InvView getInstance() {
		return instance;
	}
	  
    private Map<UUID, Inventory> inventories = new WeakHashMap<UUID, Inventory>();
    
	public void saveInv(Player player) {
		final MatchStats stats = PlayerManager.get(player.getUniqueId()).getMatchStats();
		
		stats.setLastFailedPotions(stats.getFailedPotions());
		stats.setFailedPotions(0);
		if(stats.getCombo() > stats.getLongestCombo()) {
			stats.setLongestCombo(stats.getCombo());
    	}
		stats.setCombo(0);
		
		Inventory inv = Bukkit.createInventory(null, 54, ChatColor.RED + player.getName() + "'s Inventory");
		
		for (int i = 0; i < 9; i++) {
			inv.setItem(i + 27, player.getInventory().getItem(i));
		}
		for (int i = 0; i < player.getInventory().getSize() - 9; i++) {
			inv.setItem(i, player.getInventory().getItem(i + 9));
		}
		
		final ItemStack noarmor = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 14);
		final ItemMeta nm = noarmor.getItemMeta();
		nm.setDisplayName(ChatColor.RED + "No Armor");
		noarmor.setItemMeta(nm);
		
		inv.setItem(36, (player.getInventory().getHelmet() != null ? player.getInventory().getHelmet() : noarmor));
		inv.setItem(37, (player.getInventory().getChestplate() != null ? player.getInventory().getChestplate() : noarmor));
		inv.setItem(38, (player.getInventory().getLeggings() != null ? player.getInventory().getLeggings() : noarmor));
		inv.setItem(39, (player.getInventory().getBoots() != null ? player.getInventory().getBoots() : noarmor));
		final boolean isAlive = player.getHealth() > 0.0D || !player.isDead();
		
		final ItemStack life = (isAlive ? new ItemStack(Material.SPECKLED_MELON, Integer.valueOf((int) player.getHealth()).intValue()) : new ItemStack(Material.SKULL_ITEM, 1, (short) SkullType.SKELETON.ordinal()));
		final ItemMeta lm = life.getItemMeta();
		lm.setDisplayName((isAlive ? ChatColor.DARK_AQUA + "Hearts: " + ChatColor.RESET + Math.ceil(player.getHealth() / 2.0D) + ChatColor.RED + " hp" : ChatColor.DARK_AQUA + "Player Died"));
		life.setItemMeta(lm);
		inv.setItem(48, life);
			
		final ItemStack food = new ItemStack(Material.COOKED_BEEF, player.getFoodLevel());
		final ItemMeta fm = food.getItemMeta();
		fm.setDisplayName(ChatColor.DARK_AQUA + "Food points: " + ChatColor.RESET + player.getFoodLevel());
		food.setItemMeta(fm);
		inv.setItem(49, food);
      
		final ItemStack potEffect = new ItemStack(Material.BREWING_STAND_ITEM, player.getActivePotionEffects().size());
		final ItemMeta pem = potEffect.getItemMeta();
		pem.setDisplayName(ChatColor.DARK_AQUA + "Potion Effects:");
		List<String> potionEffectLore = Lists.newArrayList();
		if (player.getActivePotionEffects().size() == 0) {
			potionEffectLore.add(ChatColor.RED + "No potion effects");
		} else {
			for (PotionEffect pe : player.getActivePotionEffects()) {
				final int realtime = pe.getDuration() / 20;
				final String emp = convertToRoman(pe.getAmplifier() + 1);
          
				potionEffectLore.add(ChatColor.GRAY + "-> " + ChatColor.RED + WordUtils.capitalizeFully(pe.getType().getName().replaceAll("_", " ")) + " " + emp + " for " + ChatColor.RESET + convertToPotionFormat(realtime));
			}
		}
		pem.setLore(potionEffectLore);
		potEffect.setItemMeta(pem);
		inv.setItem(50, potEffect);
      
		final int amount = (player.getInventory().contains(new ItemStack(Material.POTION, 1, (short)16421)) ? Integer.valueOf(player.getInventory().all(new ItemStack(Material.POTION, 1, (short)16421)).size()).intValue() : 0);
		
		final ItemStack pots = new ItemStack(Material.POTION, amount > 64 ? 64 : amount, (short)16421);
		final ItemMeta po = pots.getItemMeta();
		po.setDisplayName(ChatColor.YELLOW.toString() + amount + ChatColor.DARK_AQUA + " health pot(s) left");
		po.setLore(Arrays.asList(ChatColor.GRAY + "-> " + ChatColor.DARK_AQUA + "Missed potions: " + ChatColor.YELLOW + stats.getLastFailedPotions()));
		pots.setItemMeta(po);
		inv.setItem(45, pots);
		
		final ItemStack statistics = new ItemStack(Material.DIAMOND_SWORD, 1);
		final ItemMeta sm = statistics.getItemMeta();
		sm.setDisplayName(ChatColor.GOLD + "Statistics");
		sm.setLore(Arrays.asList(ChatColor.GRAY + "-> " + ChatColor.DARK_AQUA + "Total hit: " + ChatColor.YELLOW + stats.getHit(), ChatColor.GRAY + "-> " + ChatColor.DARK_AQUA + "Longest combo: " + ChatColor.YELLOW + stats.getLongestCombo()));
		statistics.setItemMeta(sm);
		inv.setItem(46, statistics);
      
		this.inventories.put(player.getUniqueId(), inv);
	}
    
	public void openInv(final Player player, final UUID targetUUID) {
		if (!this.inventories.containsKey(targetUUID)) {
			player.sendMessage(ChatColor.RED + "Inventory not found!");
			return;
		}
		player.openInventory(this.inventories.get(targetUUID));
	}
    
	private String convertToPotionFormat(final long paramLong) {
		if (paramLong < 0L) {
			return null;
		}
		return String.format("%01dm %02ds", new Object[] { Long.valueOf(paramLong / 60L), Long.valueOf(paramLong % 60L) });
	}
	
	private String convertToRoman(int number) {
		String roman = "";
		for (int i = 0; i < numbers.length; i++) {
			while (number >= numbers[i]) {
				roman = roman + letters[i];
				number -= numbers[i];
			}
		}
		return roman;
	}
	
	private int[] numbers = { 1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1 };
	private String[] letters = { "M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I" };
}
