package io.noks.smallpractice.objects.managers;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
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
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.Ladder;
import org.bukkit.potion.PotionEffect;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import io.noks.smallpractice.Main;
import io.noks.smallpractice.arena.Arena.Arenas;
import io.noks.smallpractice.enums.Ladders;
import io.noks.smallpractice.enums.PlayerStatus;
import io.noks.smallpractice.objects.CommandCooldown;
import io.noks.smallpractice.objects.CustomInventory;
import io.noks.smallpractice.objects.EditedLadderKit;
import io.noks.smallpractice.objects.MatchStats;
import io.noks.smallpractice.objects.Request;
import ru.tehkode.permissions.bukkit.PermissionsEx;

public class PlayerManager {
	private static final Map<UUID, PlayerManager> players = Maps.newConcurrentMap();
	private Player player;
	private UUID playerUUID;
	private Map<UUID, Request> request = new WeakHashMap<UUID, Request>();
	private Collection<UUID> invite = Collections.newSetFromMap(new WeakHashMap<>());
	private PlayerStatus status;
	private Player spectate;
	private String prefix, suffix;
	private EloManager eloManager;
	private MatchStats matchStats;
	private CommandCooldown cooldown;
	private Inventory savedInventory;
	private UUID msgedUUID;
	private List<CustomInventory> savedDuelInventory = Lists.newArrayList();
	private List<EditedLadderKit> customLadderKit = Lists.newArrayList();
	private Long nextHitTick = 0L;
	
	public PlayerManager(UUID playerUUID) {
	    this.playerUUID = playerUUID;
	    this.player = Bukkit.getPlayer(this.playerUUID);
	    this.status = PlayerStatus.SPAWN;
	    this.prefix = (!Main.getInstance().isPermissionsPluginHere() ? (this.player.isOp() ? "&d" : "&a") : PermissionsEx.getPermissionManager().getUser(getPlayer()).getPrefix());
	    this.suffix = (!Main.getInstance().isPermissionsPluginHere() ? "" : PermissionsEx.getPermissionManager().getUser(getPlayer()).getSuffix());
	    this.spectate = null;
	    this.eloManager = new EloManager();
	    this.matchStats = new MatchStats();
	    this.cooldown = new CommandCooldown();
	    if (Main.getInstance().getOfflineInventories().containsKey(playerUUID)) {
	    	this.savedInventory = Main.getInstance().getOfflineInventories().get(playerUUID);
	    	Main.getInstance().getOfflineInventories().remove(playerUUID);
	    }
	    players.putIfAbsent(playerUUID, this);
	}

	public static PlayerManager get(UUID playerUUID) {
		if (!players.containsKey(playerUUID)) {
			return null;
		}
		return players.get(playerUUID);
	}
	
	public Long getNextHitTick() {
		return this.nextHitTick;
	}
	
	public void updateNextHitTick() {
		this.nextHitTick = System.currentTimeMillis() + 500;
	}

	public void remove() {
		if (this.savedInventory != null) {
			Main.getInstance().getOfflineInventories().put(this.playerUUID, this.savedInventory);
		}
		players.remove(this.playerUUID);
	}

	public Player getPlayer() {
		return this.player;
	}
	
	public UUID getPlayerUUID() {
		return this.playerUUID;
	}
	
	public boolean isAlive() {
		return !this.player.isDead() && this.player.getHealth() > 0.0D;
	}

	public boolean isAllowedToBuild() {
		return getStatus() == PlayerStatus.BUILD || getStatus() == PlayerStatus.BRIDGE;
	}
	
	public Player getSpectate() {
		return this.spectate;
	}
	
	public void setSpectate(Player spec) {
		this.spectate = spec;
	}
	
	public boolean hasInvited(UUID invitedUUID) {
		return this.invite.contains(invitedUUID);
	}
	
	public void addInvite(UUID targetUUID) {
		this.invite.add(targetUUID);
	}
	
	public Collection<UUID> getInvites() {
		return this.invite;
	}
	
	public void addRequest(UUID targetUUID, Arenas arena, Ladders ladder) {
		this.request.put(targetUUID, new Request(ladder, arena));
	}
	
	public Map<UUID, Request> getRequests() {
		return this.request;
	}
	
	public boolean hasRequested(UUID targetUUID) {
		return this.request.containsKey(targetUUID);
	}
	
	public void clearRequest() {
		this.request.clear();
	}

	public PlayerStatus getStatus() {
		return status;
	}

	public void setStatus(PlayerStatus status) {
		this.status = status;
	}
	
	public EloManager getEloManager() {
		return this.eloManager;
	}
	
	public MatchStats getMatchStats() {
		return this.matchStats;
	}
	
	public void hideAllPlayer() {
		for (Player allPlayers : Bukkit.getOnlinePlayers()) {
			getPlayer().hidePlayer(allPlayers);
			if (get(allPlayers.getUniqueId()).getStatus() != PlayerStatus.MODERATION) {
				allPlayers.hidePlayer(getPlayer());
			}
		}
	}
	
	public void showAllPlayer() {
		for (Player allPlayers : Bukkit.getOnlinePlayers()) {
			PlayerManager pm = get(allPlayers.getUniqueId());
			
			if (pm.getStatus() != PlayerStatus.MODERATION) {
				getPlayer().showPlayer(allPlayers);
			}
			if (pm.getStatus() != PlayerStatus.DUEL && pm.getStatus() != PlayerStatus.WAITING) {
				allPlayers.showPlayer(getPlayer());
			}
			if (pm.getStatus() == PlayerStatus.MODERATION) {
				allPlayers.showPlayer(getPlayer());
				getPlayer().hidePlayer(allPlayers);
			}
		}
	}
	
	public String getPrefix() {
		if (!Main.getInstance().isPermissionsPluginHere()) {
			return this.prefix;
		}
		if (this.prefix != PermissionsEx.getPermissionManager().getUser(getPlayer()).getPrefix()) {
			this.prefix = PermissionsEx.getPermissionManager().getUser(getPlayer()).getPrefix();
			getPlayer().setPlayerListName(getPrefixColors() + getPlayer().getName());
		}
		return this.prefix;
	}
	
	public String getColoredPrefix() {
		return ChatColor.translateAlternateColorCodes('&', getPrefix());
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

		//                 |Tab||   Chat Prefix    |
		//                 |   ||                  |
		// PREFIX FORMAT -> &3&l&f[&3Developer&f] &3
	}
	
	public String getSuffix() {
		return this.suffix;
	}
	
	public String getColoredSuffix() {
		return ChatColor.translateAlternateColorCodes('&', getSuffix());
	}
	
	public boolean hasVoted() {
		return false;
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
	
	public CommandCooldown getCooldown() {
		return this.cooldown;
	}
	
	public void heal(boolean forFight) {
		//if (!isAlive()) getPlayer().spigot().respawn();
		getPlayer().setHealth(20.0D);
		getPlayer().extinguish();
		if (!getPlayer().getActivePotionEffects().isEmpty()) {
			for (PotionEffect effect : getPlayer().getActivePotionEffects()) {
				getPlayer().removePotionEffect(effect.getType());
			}
		}
		getPlayer().setFoodLevel(20);
		getPlayer().setSaturation(forFight ? 20F : 1000F);
	}
	
	public void saveInventory() {
		final MatchStats stats = getMatchStats();
		
		stats.setLastFailedPotions(stats.getFailedPotions());
		stats.setFailedPotions(0);
		if(stats.getCombo() > stats.getLongestCombo()) {
			stats.setLongestCombo(stats.getCombo());
    	}
		stats.setCombo(0);
		
		this.savedInventory = Bukkit.createInventory(null, 54, ChatColor.RED + player.getName() + "'s Inventory");
		
		for (int i = 0; i < 9; i++) {
			this.savedInventory.setItem(i + 27, player.getInventory().getItem(i));
		}
		for (int i = 0; i < player.getInventory().getSize() - 9; i++) {
			this.savedInventory.setItem(i, player.getInventory().getItem(i + 9));
		}
		
		final ItemStack noarmor = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 14);
		final ItemMeta nm = noarmor.getItemMeta();
		nm.setDisplayName(ChatColor.RED + "No Armor");
		noarmor.setItemMeta(nm);
		
		this.savedInventory.setItem(36, (player.getInventory().getHelmet() != null ? player.getInventory().getHelmet() : noarmor));
		this.savedInventory.setItem(37, (player.getInventory().getChestplate() != null ? player.getInventory().getChestplate() : noarmor));
		this.savedInventory.setItem(38, (player.getInventory().getLeggings() != null ? player.getInventory().getLeggings() : noarmor));
		this.savedInventory.setItem(39, (player.getInventory().getBoots() != null ? player.getInventory().getBoots() : noarmor));
		
		final ItemStack life = (isAlive() ? new ItemStack(Material.SPECKLED_MELON, Integer.valueOf((int) player.getHealth()).intValue()) : new ItemStack(Material.SKULL_ITEM, 1, (short) SkullType.SKELETON.ordinal()));
		final ItemMeta lm = life.getItemMeta();
		lm.setDisplayName((isAlive() ? ChatColor.DARK_AQUA + "Hearts: " + ChatColor.RESET + Math.ceil(player.getHealth() / 2.0D) + ChatColor.RED + " hp" : ChatColor.DARK_AQUA + "Player Died"));
		life.setItemMeta(lm);
		this.savedInventory.setItem(48, life);
			
		final ItemStack food = new ItemStack(Material.COOKED_BEEF, player.getFoodLevel());
		final ItemMeta fm = food.getItemMeta();
		fm.setDisplayName(ChatColor.DARK_AQUA + "Food points: " + ChatColor.RESET + player.getFoodLevel());
		food.setItemMeta(fm);
		this.savedInventory.setItem(49, food);
      
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
          
				potionEffectLore.add(ChatColor.GRAY + "-> " + ChatColor.DARK_AQUA + WordUtils.capitalizeFully(pe.getType().getName().replaceAll("_", " ")) + " " + emp + " for " + ChatColor.YELLOW + convertToPotionFormat(realtime));
			}
		}
		pem.setLore(potionEffectLore);
		potEffect.setItemMeta(pem);
		this.savedInventory.setItem(50, potEffect);
      
		final int amount = (player.getInventory().contains(new ItemStack(Material.POTION, 1, (short)16421)) ? Integer.valueOf(player.getInventory().all(new ItemStack(Material.POTION, 1, (short)16421)).size()).intValue() : 0);
		
		final ItemStack pots = new ItemStack(Material.POTION, Math.min(amount, 64), (short)16421);
		final ItemMeta po = pots.getItemMeta();
		po.setDisplayName(ChatColor.YELLOW.toString() + amount + ChatColor.DARK_AQUA + " health pot(s) left");
		po.setLore(Arrays.asList(ChatColor.GRAY + "-> " + ChatColor.DARK_AQUA + "Missed potions: " + ChatColor.YELLOW + stats.getLastFailedPotions()));
		pots.setItemMeta(po);
		this.savedInventory.setItem(45, pots);
		
		final ItemStack statistics = new ItemStack(Material.DIAMOND_SWORD, 1);
		final ItemMeta sm = statistics.getItemMeta();
		sm.setDisplayName(ChatColor.GOLD + "Statistics");
		sm.setLore(Arrays.asList(ChatColor.GRAY + "-> " + ChatColor.DARK_AQUA + "Total hit: " + ChatColor.YELLOW + stats.getHit(), ChatColor.GRAY + "-> " + ChatColor.DARK_AQUA + "Longest combo: " + ChatColor.YELLOW + stats.getLongestCombo()));
		statistics.setItemMeta(sm);
		this.savedInventory.setItem(46, statistics);
	}
	
	public Inventory getSavedInventory() {
		return this.savedInventory;
	}
	
	public List<EditedLadderKit> getCustomLadderKit() {
		return this.customLadderKit;
	}
	
	public void saveCustomLadderKit(Ladder ladder, String name, int slot, PlayerInventory inventory) {
		this.customLadderKit.add(new EditedLadderKit(ladder, name, slot, inventory));
	}
	
	public void setMessagedUUID(UUID newUUID) {
		if (this.msgedUUID == newUUID) { // dont do useless action
			return;
		}
		this.msgedUUID = newUUID;
	}
	
	public UUID getMessagedUUID() {
		return this.msgedUUID;
	}
	
	public boolean hasCustomInventory() {
		return this.savedDuelInventory.isEmpty();
	}
	
	public void saveDuelInventory(Ladder ladder, String name, int slot) {
		this.savedDuelInventory.add(new CustomInventory(ladder, name, slot, this.player.getInventory(), this.getPlayer().getInventory().getArmorContents()));
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
