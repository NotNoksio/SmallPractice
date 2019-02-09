package us.noks.smallpractice.objects.managers;

import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.google.common.collect.Maps;

import net.minecraft.util.com.google.common.collect.Lists;
import ru.tehkode.permissions.bukkit.PermissionsEx;
import us.noks.smallpractice.enums.PlayerStatus;

public class PlayerManager {

	public static List<PlayerManager> players = Lists.newArrayList();
	private Player player;
	private Map<Player, Player> request = Maps.newHashMap();
	private Map<Player, Player> invite = Maps.newHashMap();
	private PlayerStatus status;
	private Player spectate;
	private String prefix;
	private int elo;
	private int failedPotions;
	private int lastFailedPotions;
	private int hit;
	private int combo;
	private int longestCombo;
	
	public PlayerManager(Player player) {
	    this.player = player;
	    this.status = PlayerStatus.SPAWN;
	    this.spectate = null;
	    this.prefix = PermissionsEx.getPermissionManager().getUser(getPlayer()).getPrefix();
	    this.elo = EloManager.getInstance().getPlayerElo(this.player.getUniqueId());
	    this.failedPotions = 0;
	    this.lastFailedPotions = 0;
	    this.hit = 0;
	    this.combo = 0;
	    this.longestCombo = 0;
	}

	public static PlayerManager get(Player player) {
		for (PlayerManager pm : players) {
			if (pm.getPlayer().equals(player)) {
				return pm;
			}
		}
		PlayerManager pm = new PlayerManager(player);
		players.add(pm);
		return pm;
	}

	public void remove() {
		players.remove(this);
	}

	public Player getPlayer() {
		return this.player;
	}

	public boolean isCanBuild() {
		return getStatus() == PlayerStatus.BUILD;
	}
	
	public Player getSpectate() {
		return this.spectate;
	}
	
	public void setSpectate(Player spec) {
		this.spectate = spec;
	}
	
	public boolean hasInvited(Player invited) {
		return this.invite.containsValue(invited) && this.invite.containsKey(this.player) && this.invite.get(this.player) == invited;
	}
	
	public void setInviteTo(Player target) {
		this.invite.put(this.player, target);
	}
	
	public void removePartyInvite() {
		this.invite.remove(this.player);
	}
	
	public boolean hasRequest(Player requested) {
		return this.request.containsValue(requested) && this.request.containsKey(this.player) && this.request.get(this.player) == requested;
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
	
	public int getElo() {
		return elo;
	}
	
	public void addElo(int elo) {
		this.elo += elo;
	}
	
	public void removeElo(int elo) {
		this.elo -= elo;
	}
	
	public int getFailedPotions() {
		return failedPotions;
	}
	
	public void setFailedPotions(int pots) {
		this.failedPotions = pots;
	}
	
	public int getLastFailedPotions() {
		return lastFailedPotions;
	}
	
	public void setLastFailedPotions(int pots) {
		this.lastFailedPotions = pots;
	}
	
	public void giveSpawnItem() {
		getPlayer().getInventory().clear();
		getPlayer().getInventory().setArmorContents(null);
		getPlayer().setItemOnCursor(null);
		
		getPlayer().setGameMode(GameMode.SURVIVAL);
		
		if (!PartyManager.getInstance().hasParty(getPlayer().getUniqueId())) {
			ItemStack r = new ItemStack(Material.DIAMOND_SWORD, 1);
			ItemMeta rm = r.getItemMeta();
			rm.setDisplayName(ChatColor.YELLOW + "Direct Queue");
			rm.spigot().setUnbreakable(true);
			r.setItemMeta(rm);
			
			ItemStack n = new ItemStack(Material.NAME_TAG, 1);
			ItemMeta nm = n.getItemMeta();
			nm.setDisplayName(ChatColor.YELLOW + "Create Party");
			n.setItemMeta(nm);
			
			getPlayer().getInventory().setItem(0, r);
			getPlayer().getInventory().setItem(8, n);
		} else {
			ItemStack a = new ItemStack(Material.ARROW, 1);
			ItemMeta am = a.getItemMeta();
			am.setDisplayName(ChatColor.YELLOW + "Split Teams");
			a.setItemMeta(am);
			
			ItemStack b = new ItemStack(Material.BOOK, 1);
			ItemMeta bm = b.getItemMeta();
			bm.setDisplayName(ChatColor.YELLOW + "Fight Other Parties");
			b.setItemMeta(bm);
			
			ItemStack r = new ItemStack(Material.REDSTONE, 1);
			ItemMeta rm = r.getItemMeta();
			rm.setDisplayName(ChatColor.RED + "Leave Party");
			r.setItemMeta(rm);
			
			getPlayer().getInventory().setItem(0, a);
			getPlayer().getInventory().setItem(2, b);
			getPlayer().getInventory().setItem(8, r);
		}
		getPlayer().updateInventory();
	}
	
	public void giveQueueItem() {
		getPlayer().getInventory().clear();
		getPlayer().getInventory().setArmorContents(null);
		getPlayer().setItemOnCursor(null);
		
		ItemStack r = new ItemStack(Material.REDSTONE, 1);
		ItemMeta rm = r.getItemMeta();
		rm.setDisplayName(ChatColor.RED + "Leave Queue");
		r.setItemMeta(rm);
		
		getPlayer().getInventory().setItem(8, r);
		getPlayer().updateInventory();
	}
	
	public void giveSpectateItem() {
		getPlayer().getInventory().clear();
		getPlayer().getInventory().setArmorContents(null);
		getPlayer().setItemOnCursor(null);
		
		ItemStack r = new ItemStack(Material.REDSTONE, 1);
		ItemMeta rm = r.getItemMeta();
		rm.setDisplayName(ChatColor.RED + "Leave Spectate");
		r.setItemMeta(rm);
		
		getPlayer().getInventory().setItem(8, r);
		getPlayer().updateInventory();
	}
	
	public void giveModerationItem() {
		getPlayer().getInventory().clear();
		getPlayer().getInventory().setArmorContents(null);
		getPlayer().setItemOnCursor(null);
		
		getPlayer().setGameMode(GameMode.CREATIVE);
		
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
		
		getPlayer().getInventory().setItem(8, r);
		getPlayer().getInventory().setItem(0, s);
		getPlayer().getInventory().setItem(1, a);
		getPlayer().updateInventory();
	}
	
	public void giveKit() {
		getPlayer().getInventory().clear();
		getPlayer().getInventory().setArmorContents(null);
		getPlayer().setItemOnCursor(null);
		
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
		
		getPlayer().getInventory().setHelmet(hel);
		getPlayer().getInventory().setChestplate(che);
		getPlayer().getInventory().setLeggings(leg);
		getPlayer().getInventory().setBoots(boo);
		
		for (int i = 0; i < 36; i++) {
			getPlayer().getInventory().setItem(i, heal);
		}
		
		getPlayer().getInventory().setItem(0, swo);
		getPlayer().getInventory().setItem(1, pearl);
		getPlayer().getInventory().setItem(2, speed);
		getPlayer().getInventory().setItem(3, fire);
		getPlayer().getInventory().setItem(8, steak);
		
		getPlayer().getInventory().setItem(17, speed);
		getPlayer().getInventory().setItem(26, speed);
		getPlayer().getInventory().setItem(35, speed);
		
		getPlayer().updateInventory();
	}
	
	public void hideAllPlayer() {
		for (Player allPlayers : Bukkit.getOnlinePlayers()) {
			getPlayer().hidePlayer(allPlayers);
			if (get(allPlayers).getStatus() != PlayerStatus.MODERATION) allPlayers.hidePlayer(getPlayer());
		}
	}
	
	public void showAllPlayer() {
		for (Player allPlayers : Bukkit.getOnlinePlayers()) {
			PlayerManager pm = get(allPlayers);
			
			if (pm.getStatus() != PlayerStatus.MODERATION) getPlayer().showPlayer(allPlayers);
			if (pm.getStatus() != PlayerStatus.DUEL && pm.getStatus() != PlayerStatus.WAITING) allPlayers.showPlayer(getPlayer());
			if (pm.getStatus() == PlayerStatus.MODERATION) {
				allPlayers.showPlayer(getPlayer());
				getPlayer().hidePlayer(allPlayers);
			}
		}
	}
	
	public String getPrefix() {
		if (this.prefix != PermissionsEx.getPermissionManager().getUser(getPlayer()).getPrefix()) {
			this.prefix = PermissionsEx.getPermissionManager().getUser(getPlayer()).getPrefix();
			getPlayer().setPlayerListName(getColorPrefix() + getPlayer().getName());
		}
		return this.prefix;
	}
	
	public String getColoredPrefix() {
		return ChatColor.translateAlternateColorCodes('&', getPrefix()) + "";
	}
	
	public String getColorPrefix() {
		if (getPrefix().isEmpty()) {
			return "";
		}

		ChatColor color;
		ChatColor magicColor;

		char code = 'f';
		char magic = 'f';
		int count = 0;

		for (String string : getPrefix().split("&")) {
			if (!(string.isEmpty())) {
				if (ChatColor.getByChar(string.toCharArray()[0]) != null) {
					if (count == 0 && !isMagicColor(string.toCharArray()[0])) {
						code = string.toCharArray()[0];
						count++;
					} else if (count == 1 && isMagicColor(string.toCharArray()[0])) {
						magic = string.toCharArray()[0];
						count++;
					}
				}
			}
		}

		color = ChatColor.getByChar(code);
		magicColor = ChatColor.getByChar(magic);
		return count == 1 ? color.toString() : color.toString() + magicColor.toString();
	}
	
	private boolean isMagicColor(char letter) {
		switch (letter) {
		case 'k':
			return true;
		case 'l':
			return true;
		case 'm':
			return true;
		case 'n':
			return true;
		case 'o':
			return true;
		case 'r':
			return true;
		default:
			break;
		}
		return false;
	}
	
	public void resetDuelStats() {
		this.failedPotions = 0;
		this.hit = 0;
		this.combo = 0;
		this.longestCombo = 0;
	}

	public int getHit() {
		return hit;
	}

	public void setHit(int hit) {
		this.hit = hit;
	}

	public int getCombo() {
		return combo;
	}

	public void setCombo(int combo) {
		this.combo = combo;
	}

	public int getLongestCombo() {
		return longestCombo;
	}

	public void setLongestCombo(int longestCombo) {
		this.longestCombo = longestCombo;
	}
}
