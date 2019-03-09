package us.noks.smallpractice.utils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;

import com.google.common.collect.Lists;

import net.minecraft.util.com.google.common.collect.Maps;
import us.noks.smallpractice.objects.managers.PlayerManager;

public class InvView implements Listener {

	static InvView instance = new InvView();
	public static InvView getInstance() {
		return instance;
	}
	  
    private Map<UUID, Inventory> inventorymap = Maps.newHashMap();
    
	public void saveInv(Player p) {
		PlayerManager pm = PlayerManager.get(p);
		pm.setLastFailedPotions(pm.getFailedPotions());
		pm.setFailedPotions(0);
		if(pm.getCombo() > pm.getLongestCombo()) {
    		pm.setLongestCombo(pm.getCombo());
    	}
		pm.setCombo(0);
		
		Inventory inv = Bukkit.createInventory(null, 54, ChatColor.RED + p.getName() + "'s Inventory");
		
		for (int i = 0; i < 9; i++) {
			inv.setItem(i + 27, p.getInventory().getItem(i));
		}
		for (int i = 0; i < p.getInventory().getSize() - 9; i++) {
			inv.setItem(i, p.getInventory().getItem(i + 9));
		}
		
		ItemStack noarmor = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 14);
		ItemMeta nm = noarmor.getItemMeta();
		nm.setDisplayName(ChatColor.RED + "No Armor");
		noarmor.setItemMeta(nm);
		
		inv.setItem(36, (p.getInventory().getHelmet() != null ? p.getInventory().getHelmet() : noarmor));
		inv.setItem(37, (p.getInventory().getChestplate() != null ? p.getInventory().getChestplate() : noarmor));
		inv.setItem(38, (p.getInventory().getLeggings() != null ? p.getInventory().getLeggings() : noarmor));
		inv.setItem(39, (p.getInventory().getBoots() != null ? p.getInventory().getBoots() : noarmor));
		
		if (p.getHealth() > 0) {
			ItemStack vie = new ItemStack(Material.SPECKLED_MELON, Integer.valueOf((int) p.getHealth()).intValue());
			ItemMeta v = vie.getItemMeta();
			v.setDisplayName(ChatColor.DARK_AQUA + "Hearts: " + ChatColor.RESET + Math.ceil(p.getHealth() / 2.0D) + ChatColor.RED + " hp");
			vie.setItemMeta(v);
			
			inv.setItem(48, vie);
		} else {
			ItemStack vie = new ItemStack(Material.SKULL_ITEM, 1, (short) SkullType.SKELETON.ordinal());
			ItemMeta v = vie.getItemMeta();
			v.setDisplayName(ChatColor.DARK_AQUA + "Player Died");
			vie.setItemMeta(v);
			
			inv.setItem(48, vie);
		}
		
		ItemStack bouffe = new ItemStack(Material.COOKED_BEEF, p.getFoodLevel());
		ItemMeta b = bouffe.getItemMeta();
		b.setDisplayName(ChatColor.DARK_AQUA + "Food points: " + ChatColor.RESET + p.getFoodLevel());
		bouffe.setItemMeta(b);
		inv.setItem(49, bouffe);
      
		ItemStack item2 = new ItemStack(Material.BREWING_STAND_ITEM, p.getActivePotionEffects().size());
		ItemMeta itemm2 = item2.getItemMeta();
		itemm2.setDisplayName(ChatColor.DARK_AQUA + "Potion Effects:");
		List<String> lore = Lists.newArrayList();
		if (p.getActivePotionEffects().size() == 0) {
			lore.add(ChatColor.RED + "No potion effects");
		} else {
			for (PotionEffect pe : p.getActivePotionEffects()) {
				int realtime = pe.getDuration() / 20;
				String emp = convertToRoman(pe.getAmplifier() + 1);
          
				lore.add(ChatColor.GRAY + "-> " + ChatColor.RED + WordUtils.capitalizeFully(pe.getType().getName().replaceAll("_", " ")) + " " + emp + " for " + ChatColor.RESET + convertToPotionFormat(realtime));
			}
		}
		itemm2.setLore(lore);
		item2.setItemMeta(itemm2);
		inv.setItem(50, item2);
      
		int amount = (p.getInventory().contains(new ItemStack(Material.POTION, 1, (short)16421)) ? Integer.valueOf(p.getInventory().all(new ItemStack(Material.POTION, 1, (short)16421)).size()).intValue() : 0);
		
		ItemStack pots = new ItemStack(Material.POTION, amount > 64 ? 64 : amount, (short)16421);
		ItemMeta po = pots.getItemMeta();
		po.setDisplayName(ChatColor.YELLOW.toString() + amount + ChatColor.DARK_AQUA + " health pot(s) left");
		po.setLore(Arrays.asList(ChatColor.GRAY + "-> " + ChatColor.DARK_AQUA + "Missed potions: " + ChatColor.YELLOW + pm.getLastFailedPotions()));
		pots.setItemMeta(po);
		inv.setItem(45, pots);
		
		ItemStack stats = new ItemStack(Material.DIAMOND_SWORD, 1);
		ItemMeta sm = stats.getItemMeta();
		sm.setDisplayName(ChatColor.GOLD + "Statistics");
		sm.setLore(Arrays.asList(ChatColor.GRAY + "-> " + ChatColor.DARK_AQUA + "Total hit: " + ChatColor.YELLOW + pm.getHit(), ChatColor.GRAY + "-> " + ChatColor.DARK_AQUA + "Longest combo: " + ChatColor.YELLOW + pm.getLongestCombo()));
		stats.setItemMeta(sm);
		inv.setItem(46, stats);
      
		this.inventorymap.put(p.getUniqueId(), inv);
	}
    
	public void openInv(Player p, UUID t) {
		if (this.inventorymap.containsKey(t)) {
			p.openInventory(this.inventorymap.get(t));
		}
	}
	
	@EventHandler(priority=EventPriority.LOW)
	public void onInvsClick(InventoryClickEvent e) {
		if (e.getInventory().getTitle().toLowerCase().endsWith("'s inventory")) {
			e.setCancelled(true);
		}
	}
    
	private String convertToPotionFormat(long paramLong) {
		if (paramLong < 0L) {
			return null;
		}
		return String.format("%01dm %02ds", new Object[] { Long.valueOf(paramLong / 60L), Long.valueOf(paramLong % 60L) });
	}
	
	private String convertToRoman(int N) {
		String roman = "";
		for (int i = 0; i < numbers.length; i++) {
			while (N >= numbers[i]) {
				roman = roman + letters[i];
				N -= numbers[i];
			}
		}
		return roman;
	}
	
	private int[] numbers = { 1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1 };
	private String[] letters = { "M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I" };
}
