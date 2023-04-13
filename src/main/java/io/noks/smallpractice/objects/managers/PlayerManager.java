package io.noks.smallpractice.objects.managers;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.stream.Collectors;

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
import com.google.common.collect.Maps;

import io.noks.smallpractice.Main;
import io.noks.smallpractice.arena.Arena;
import io.noks.smallpractice.enums.Ladders;
import io.noks.smallpractice.enums.PlayerStatus;
import io.noks.smallpractice.objects.Cooldown;
import io.noks.smallpractice.objects.EditedLadderKit;
import io.noks.smallpractice.objects.MatchStats;
import io.noks.smallpractice.objects.PlayerSettings;
import io.noks.smallpractice.objects.Request;

public class PlayerManager {
	public static final Map<UUID, PlayerManager> players = Maps.newConcurrentMap();
	private final Player player;
	private final UUID playerUUID;
	private final Map<UUID, Request> request = new WeakHashMap<UUID, Request>();
	private final Collection<UUID> invite = Collections.newSetFromMap(new WeakHashMap<>());
	private PlayerStatus status, previousStatus;
	private Player spectate;
	private final EloManager eloManager;
	private final MatchStats matchStats;
	private final Cooldown cooldown;
	private Inventory savedInventory;
	private final List<EditedLadderKit> customLadderKit;
	private final PlayerSettings settings;
	
	public PlayerManager(UUID playerUUID) {
	    this.playerUUID = playerUUID;
	    this.player = Bukkit.getPlayer(this.playerUUID);
	    this.status = PlayerStatus.SPAWN;
	    this.spectate = null;
	    this.eloManager = new EloManager();
	    this.matchStats = new MatchStats();
	    this.cooldown = new Cooldown();
	    if (Main.getInstance().getInventoryManager().getOfflineInventories().containsKey(playerUUID)) {
	    	this.savedInventory = Main.getInstance().getInventoryManager().getOfflineInventories().get(playerUUID);
	    	Main.getInstance().getInventoryManager().getOfflineInventories().remove(playerUUID);
	    }
	    this.customLadderKit = Lists.newArrayList();
	    this.settings = new PlayerSettings();
	    players.putIfAbsent(playerUUID, this);
	}
	public PlayerManager(UUID playerUUID, EloManager elo, PlayerSettings settings, List<EditedLadderKit> customKits) {
	    this.playerUUID = playerUUID;
	    this.player = Bukkit.getPlayer(this.playerUUID);
	    this.status = PlayerStatus.SPAWN;
	    this.spectate = null;
	    this.eloManager = elo;
	    this.matchStats = new MatchStats();
	    this.cooldown = new Cooldown();
	    if (Main.getInstance().getInventoryManager().getOfflineInventories().containsKey(playerUUID)) {
	    	this.savedInventory = Main.getInstance().getInventoryManager().getOfflineInventories().get(playerUUID);
	    	Main.getInstance().getInventoryManager().getOfflineInventories().remove(playerUUID);
	    }
	    this.customLadderKit = customKits;
	    this.settings = settings;
	    players.putIfAbsent(playerUUID, this);
	}

	public static PlayerManager get(UUID playerUUID) {
		if (!players.containsKey(playerUUID)) {
			return null;
		}
		return players.get(playerUUID);
	}

	public void remove() {
		this.matchStats.resetDuelStats();
		if (this.savedInventory != null) {
			Main.getInstance().getInventoryManager().getOfflineInventories().put(this.playerUUID, this.savedInventory);
		}
		this.request.clear();
		this.invite.clear();
		this.customLadderKit.clear();
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
		return this.status == PlayerStatus.BUILD;
	}
	public boolean isFrozen() {
		return this.status == PlayerStatus.FREEZE;
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
	
	public void addRequest(UUID targetUUID, Arena arena, Ladders ladder) {
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
	public PlayerStatus getPreviousStatus() {
		return this.previousStatus;
	}

	public void setStatus(PlayerStatus status) {
		this.previousStatus = this.status;
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
	
	public Cooldown getCooldown() {
		return this.cooldown;
	}
	
	public void heal(boolean forFight) {
		this.player.setHealth(20.0D);
		this.player.extinguish();
		if (!this.player.getActivePotionEffects().isEmpty()) {
			for (PotionEffect effect : this.player.getActivePotionEffects()) {
				this.player.removePotionEffect(effect.getType());
			}
		}
		if (this.player.getArrowsStuck() != 0) {
			this.player.setArrowsStuck(0);
		}
		this.player.setFoodLevel(20);
		this.player.setSaturation(!forFight ? 1000F : 20F);
		if (!forFight && this.status != PlayerStatus.WAITING && this.status != PlayerStatus.DUEL && this.player.getKnockbackReduction() > 0.0D) {
			this.player.setKnockbackReduction(0.0f);
		}
	}
	
	public void saveInventory() {
		final MatchStats stats = getMatchStats();
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
		final List<String> potionEffectLore = Lists.newArrayList();
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
		po.setLore(Arrays.asList(ChatColor.GRAY + "-> " + ChatColor.DARK_AQUA + "Missed potions: " + ChatColor.YELLOW + stats.getFailedPotions()));
		pots.setItemMeta(po);
		this.savedInventory.setItem(45, pots);
		
		final ItemStack statistics = new ItemStack(Material.DIAMOND_SWORD, 1);
		final ItemMeta sm = statistics.getItemMeta();
		sm.setDisplayName(ChatColor.GOLD + "Statistics");
		sm.setLore(Arrays.asList(ChatColor.GRAY + "-> " + ChatColor.DARK_AQUA + "Total hit: " + ChatColor.YELLOW + stats.getTotalHit(), ChatColor.GRAY + "-> " + ChatColor.DARK_AQUA + "Longest combo: " + ChatColor.YELLOW + stats.getLongestCombo()));
		statistics.setItemMeta(sm);
		this.savedInventory.setItem(46, statistics);
	}
	
	public Inventory getSavedInventory() {
		return this.savedInventory;
	}
	
	public List<EditedLadderKit> getCustomLadderKitList() {
		return this.customLadderKit;
	}
	
	public List<EditedLadderKit> getCustomLadderKitFromLadder(Ladders ladder) {
		final List<EditedLadderKit> customKitsFromLadder = this.customLadderKit.stream().filter(kit -> kit.getLadder() == ladder).collect(Collectors.toList());
		return customKitsFromLadder.isEmpty() ? Collections.emptyList() : customKitsFromLadder;
	}
	
	public EditedLadderKit getCustomLadderKitFromSlot(Ladders ladder, int slot) {
		if (getCustomLadderKitFromLadder(ladder).isEmpty()) {
			return null;
		}
		for (EditedLadderKit customKit : getCustomLadderKitFromLadder(ladder)) {
			if (customKit.getSlot() == slot) {
				return customKit;
			}
		}
		return null;
	}
	
	public void saveCustomLadderKit(Ladders ladder, int slot, Inventory inventory) {
		final EditedLadderKit kit = getCustomLadderKitFromSlot(ladder, slot);
		if (kit != null) {
			kit.updateInventory(inventory);
			return;
		}
		this.customLadderKit.add(new EditedLadderKit(ladder, slot, inventory));
	}
	
	public void deleteCustomLadderKit(Ladders ladder, int slot) {
		Main.getInstance().getDatabaseUtil().deleteCustomKit(this.playerUUID, ladder, slot);
		this.customLadderKit.remove(getCustomLadderKitFromSlot(ladder, slot));
	}
	
	public PlayerSettings getSettings() {
		return this.settings;
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
