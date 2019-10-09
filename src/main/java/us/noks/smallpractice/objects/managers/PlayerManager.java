package us.noks.smallpractice.objects.managers;

import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.google.common.collect.Maps;

import ru.tehkode.permissions.bukkit.PermissionsEx;
import us.noks.smallpractice.enums.PlayerStatus;
import us.noks.smallpractice.objects.MatchStats;

public class PlayerManager {
	private static final Map<UUID, PlayerManager> players = Maps.newConcurrentMap();
	private Player player;
	private UUID playerUUID;
	private Map<UUID, UUID> request = new WeakHashMap<UUID, UUID>();
	private Map<UUID, UUID> invite = new WeakHashMap<UUID, UUID>();
	private PlayerStatus status;
	private Player spectate;
	private String prefix;
	private int elo;
	private MatchStats matchStats;
	
	public PlayerManager(UUID playerUUID) {
	    this.playerUUID = playerUUID;
	    this.player = Bukkit.getPlayer(this.playerUUID);
	    this.status = PlayerStatus.SPAWN;
	    this.prefix = PermissionsEx.getPermissionManager().getUser(getPlayer()).getPrefix();
	    this.spectate = null;
	    this.elo = EloManager.getInstance().getPlayerElo(this.player.getUniqueId());
	    this.matchStats = new MatchStats();
	}

	public static void create(UUID uuid) {
		players.putIfAbsent(uuid, new PlayerManager(uuid));
	}

	public static PlayerManager get(UUID playerUUID) {
		if (!players.containsKey(playerUUID)) {
			return null;
		}
		return players.get(playerUUID);
	}

	public void remove() {
		players.remove(this.playerUUID);
	}

	public Player getPlayer() {
		return this.player;
	}
	
	public UUID getPlayerUUID() {
		return this.playerUUID;
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
	
	public boolean hasInvited(UUID invitedUUID) {
		return this.invite.containsValue(invitedUUID) && this.invite.containsKey(this.player.getUniqueId()) && this.invite.get(this.player.getUniqueId()) == invitedUUID;
	}
	
	public void setInviteTo(UUID targetUUID) {
		this.invite.put(this.player.getUniqueId(), targetUUID);
	}
	
	public UUID getInviteTo() {
		return this.invite.get(this.player.getUniqueId());
	}
	
	public void removePartyInvite() {
		if (this.invite.containsKey(this.player.getUniqueId())) {
			this.invite.remove(this.player.getUniqueId());
		}
	}
	
	public void setRequestTo(UUID targetUUID) {
		this.request.put(this.player.getUniqueId(), targetUUID);
	}
	
	public UUID getRequestTo() {
		return this.request.get(this.player.getUniqueId());
	}
	
	public void removeRequest() {
		if (this.request.containsKey(this.player.getUniqueId())) {
			this.request.remove(this.player.getUniqueId());
		}
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
	
	public MatchStats getMatchStats() {
		return this.matchStats;
	}
	
	public void giveSpawnItem() {
		getPlayer().getInventory().clear();
		getPlayer().getInventory().setArmorContents(null);
		getPlayer().setItemOnCursor(null);
		
		getPlayer().setGameMode(GameMode.SURVIVAL);
		
		if (!PartyManager.getInstance().hasParty(getPlayer().getUniqueId())) {
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
			
			getPlayer().getInventory().setItem(0, u);
			getPlayer().getInventory().setItem(1, r);
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
			
			ItemStack p = new ItemStack(Material.PAPER, 1);
			ItemMeta pm = p.getItemMeta();
			pm.setDisplayName(ChatColor.YELLOW + "Party Information");
			p.setItemMeta(pm);
			
			ItemStack r = new ItemStack(Material.REDSTONE, 1);
			ItemMeta rm = r.getItemMeta();
			rm.setDisplayName(ChatColor.RED + "Leave Party");
			r.setItemMeta(rm);
			
			getPlayer().getInventory().setItem(0, a);
			getPlayer().getInventory().setItem(2, b);
			getPlayer().getInventory().setItem(5, p);
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
		ItemStack speed = new ItemStack(Material.POTION, 1, (short) 8226);
		ItemStack fire = new ItemStack(Material.POTION, 1, (short) 8259);
		
		while (getPlayer().getInventory().firstEmpty() != -1) {
			getPlayer().getInventory().addItem(new ItemStack(Material.POTION, 1, (short) 16421));
		}
		
		getPlayer().getInventory().setHelmet(hel);
		getPlayer().getInventory().setChestplate(che);
		getPlayer().getInventory().setLeggings(leg);
		getPlayer().getInventory().setBoots(boo);
		
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
			if (getPlayer().canSee(allPlayers)) getPlayer().hidePlayer(allPlayers);
			if (get(allPlayers.getUniqueId()).getStatus() != PlayerStatus.MODERATION) {
				if (allPlayers.canSee(getPlayer())) allPlayers.hidePlayer(getPlayer());
			}
		}
	}
	
	public void showAllPlayer() {
		for (Player allPlayers : Bukkit.getOnlinePlayers()) {
			PlayerManager pm = get(allPlayers.getUniqueId());
			
			if (pm.getStatus() != PlayerStatus.MODERATION) {
				if (!getPlayer().canSee(allPlayers)) getPlayer().showPlayer(allPlayers);
			}
			if (pm.getStatus() != PlayerStatus.DUEL && pm.getStatus() != PlayerStatus.WAITING) {
				if (!allPlayers.canSee(getPlayer())) allPlayers.showPlayer(getPlayer());
			}
			if (pm.getStatus() == PlayerStatus.MODERATION) {
				if (!allPlayers.canSee(getPlayer())) allPlayers.showPlayer(getPlayer());
				if (getPlayer().canSee(allPlayers)) getPlayer().hidePlayer(allPlayers);
			}
		}
	}
	
	public String getPrefix() {
		if (this.prefix != PermissionsEx.getPermissionManager().getUser(getPlayer()).getPrefix()) {
			this.prefix = PermissionsEx.getPermissionManager().getUser(getPlayer()).getPrefix();
			getPlayer().setPlayerListName(getPrefixColors() + getPlayer().getName());
		}
		return this.prefix;
	}
	
	public String getColoredPrefix() {
		return ChatColor.translateAlternateColorCodes('&', getPrefix()) + "";
	}
	
	public String getPrefixColors() {
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
	
	public void heal() {
		if (getPlayer().isDead()) getPlayer().spigot().respawn();
		getPlayer().setHealth(20.0D);
		getPlayer().clearPotionEffect();
		getPlayer().extinguish();
		getPlayer().setFoodLevel(20);
		getPlayer().setSaturation(20f);
	}
}
