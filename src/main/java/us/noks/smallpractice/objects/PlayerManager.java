package us.noks.smallpractice.objects;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import us.noks.smallpractice.utils.PlayerStatus;

public class PlayerManager {

	public static List<PlayerManager> players = new ArrayList<PlayerManager>();
	private Player player;
	private boolean canbuild;
	private Player opponent;
	private Map<Player, Player> request = new HashMap<Player, Player>();
	private PlayerStatus status;

	public PlayerManager(Player p) {
	    this.player = p;
	    this.setCanbuild(false);
	    this.setOpponent(null);
	    this.setStatus(PlayerStatus.SPAWN);
	}

	public static PlayerManager get(Player p) {
		for (PlayerManager ip : players) {
			if (ip.getPlayer().equals(p)) {
				return ip;
			}
		}
		PlayerManager ip = new PlayerManager(p);
		players.add(ip);
		return ip;
	}

	public static void remove(Player p) {
		PlayerManager m = new PlayerManager(p);
		players.remove(m);
	}

	public Player getPlayer() {
		return this.player;
	}

	public boolean isCanbuild() {
		return canbuild;
	}

	public void setCanbuild(boolean canbuild) {
		this.canbuild = canbuild;
	}

	public Player getOpponent() {
		return opponent;
	}

	public void setOpponent(Player opponent) {
		this.opponent = opponent;
	}
	
	public boolean hasRequest(Player p) {
		return this.request.containsValue(p) && this.request.containsKey(this.player) && this.request.get(this.player).equals(p);
	}
	
	public void setRequestTo(Player target) {
		this.request.put(this.player, target);
	}
	
	public void removeRequest() {
		this.request.remove(this.player);
	}

	public PlayerStatus getStatus() {
		return status;
	}

	public void setStatus(PlayerStatus status) {
		this.status = status;
	}
	
	public void giveKit() {
		Player p = getPlayer();
		
		p.getInventory().clear();
		p.getInventory().setArmorContents(null);
		
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
		ItemStack heal = new ItemStack(Material.POTION, 1, (short) 16421);
		ItemStack speed = new ItemStack(Material.POTION, 1, (short) 8226);
		ItemStack fire = new ItemStack(Material.POTION, 1, (short) 8259);
		
		p.getInventory().setHelmet(hel);
		p.getInventory().setChestplate(che);
		p.getInventory().setLeggings(leg);
		p.getInventory().setBoots(boo);
		
		for (int i = 0; i < 36; i++) {
			p.getInventory().setItem(i, heal);
		}
		
		p.getInventory().setItem(0, swo);
		p.getInventory().setItem(1, pearl);
		p.getInventory().setItem(2, speed);
		p.getInventory().setItem(3, fire);
		p.getInventory().setItem(8, steak);
		
		p.getInventory().setItem(17, speed);
		p.getInventory().setItem(26, speed);
		p.getInventory().setItem(35, speed);
		
		p.updateInventory();
	}
	
	public void hideAllPlayer() {
		for (Player allPlayers : Bukkit.getOnlinePlayers()) {
			getPlayer().hidePlayer(allPlayers);
			allPlayers.hidePlayer(getPlayer());
		}
	}
	
	public void showAllPlayer() {
		for (Player allPlayers : Bukkit.getOnlinePlayers()) {
			PlayerManager pm = PlayerManager.get(allPlayers);
			getPlayer().showPlayer(allPlayers);
			if (pm.getStatus() != PlayerStatus.DUEL && pm.getStatus() != PlayerStatus.WAITING) {
				allPlayers.showPlayer(getPlayer());
			}
		}
	}
}
