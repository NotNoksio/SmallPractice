package us.noks.smallpractice.objects.managers;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

import com.google.common.collect.Maps;

import ru.tehkode.permissions.bukkit.PermissionsEx;
import us.noks.smallpractice.Main;
import us.noks.smallpractice.enums.PlayerStatus;
import us.noks.smallpractice.objects.CommandCooldown;
import us.noks.smallpractice.objects.MatchStats;

public class PlayerManager {
	private static final Map<UUID, PlayerManager> players = Maps.newConcurrentMap();
	private Player player;
	private UUID playerUUID;
	private Collection<UUID> request = Collections.newSetFromMap(new WeakHashMap<>());
	private Collection<UUID> invite = Collections.newSetFromMap(new WeakHashMap<>());
	private PlayerStatus status;
	private Player spectate;
	private String prefix, suffix;
	private int elo;
	private MatchStats matchStats;
	private CommandCooldown cooldown;
	
	public PlayerManager(UUID playerUUID) {
	    this.playerUUID = playerUUID;
	    this.player = Bukkit.getPlayer(this.playerUUID);
	    this.status = PlayerStatus.SPAWN;
	    this.prefix = (!Main.getInstance().isPermissionsPluginHere() ? "&a" : PermissionsEx.getPermissionManager().getUser(getPlayer()).getPrefix());
	    this.suffix = (!Main.getInstance().isPermissionsPluginHere() ? "" : PermissionsEx.getPermissionManager().getUser(getPlayer()).getSuffix());
	    this.spectate = null;
	    this.elo = EloManager.getInstance().getPlayerElo(this.player.getUniqueId());
	    this.matchStats = new MatchStats();
	    this.cooldown = new CommandCooldown();
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
	
	public boolean isAlive() {
		return !this.player.isDead() && this.player.getHealth() > 0.0D;
	}

	public boolean isCanBuild() {
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
	
	public void addRequest(UUID targetUUID) {
		this.request.add(targetUUID);
	}
	
	public Collection<UUID> getRequests() {
		return this.request;
	}
	
	public boolean hasRequest(UUID invitedUUID) {
		return this.request.contains(invitedUUID);
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
		if (!Main.getInstance().isPermissionsPluginHere()) {
			return "";
		}
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
		//                 ↓   ↓↓                  ↓
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
}
