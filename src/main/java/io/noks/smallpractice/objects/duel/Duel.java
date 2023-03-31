package io.noks.smallpractice.objects.duel;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import com.google.common.collect.Lists;

import io.noks.smallpractice.Main;
import io.noks.smallpractice.arena.Arena;
import io.noks.smallpractice.enums.Ladders;
import io.noks.smallpractice.enums.PlayerStatus;
import io.noks.smallpractice.objects.managers.PlayerManager;
import net.minecraft.util.com.google.common.collect.Sets;

public class Duel {
	private Arena arena;
	private Ladders ladder;
	private SimpleDuel simpleDuel;
	private FFADuel ffaDuel;
	private boolean ranked;
	private List<UUID> spectators;
	private int timeBeforeDuel = 5;
	private Set<UUID> drops;
	protected BukkitTask xpBarRunnable;
	private long startTime;
	
	public Duel(Arena arena, Ladders ladder, SimpleDuel simpleDuel, boolean ranked) {
		this.arena = arena;
		this.ladder = ladder;
		this.simpleDuel = simpleDuel;
		this.spectators = Lists.newArrayList();
		this.ranked = ranked;
		this.drops = Sets.newHashSet();
		this.xpBarRunnable = this.initXpBarRunnable();
		this.startTime = System.currentTimeMillis();
	}
	
	public Duel(Arena arena, Ladders ladder, FFADuel ffaDuel) {
		this.arena = arena;
		this.ladder = ladder;
		this.ffaDuel = ffaDuel;
		this.spectators = Lists.newArrayList();
		this.drops = Sets.newHashSet();
		this.ranked = false;
		this.xpBarRunnable = this.initXpBarRunnable();
		this.startTime = System.currentTimeMillis();
    }
	
	public Arena getArena() {
		return this.arena;
	}
	
	public Ladders getLadder() {
		return this.ladder;
	}
	
	public SimpleDuel getSimpleDuel() {
		return this.simpleDuel;
	}
	
	public FFADuel getFFADuel() {
		return this.ffaDuel;
	}
	
	public List<UUID> getAllTeams() {
		List<UUID> teams = Lists.newArrayList();
		if (this.simpleDuel != null) {
			if (!this.simpleDuel.firstTeam.isEmpty()) {
				teams.addAll(this.simpleDuel.firstTeam);
			}
			if (!this.simpleDuel.secondTeam.isEmpty()) {
				teams.addAll(this.simpleDuel.secondTeam);
			}
		}
		if (this.ffaDuel != null) {
			if (!this.ffaDuel.getFfaPlayers().isEmpty()) {
				teams.addAll(this.ffaDuel.getFfaPlayers());
			}
		}
		return teams;
	}
	
	public List<UUID> getAllAliveTeams() {
		List<UUID> teams = Lists.newArrayList();
		if (this.simpleDuel != null) {
			if (!this.simpleDuel.firstTeamAlive.isEmpty()) {
				teams.addAll(this.simpleDuel.firstTeamAlive);
			}
			if (!this.simpleDuel.secondTeamAlive.isEmpty()) {
				teams.addAll(this.simpleDuel.secondTeamAlive);
			}
		}
		if (this.ffaDuel != null) {
			if (!this.ffaDuel.getFfaAlivePlayers().isEmpty()) {
				teams.addAll(this.ffaDuel.getFfaAlivePlayers());
			}
		}
		return teams;
	}
	
	public List<UUID> getAllAliveTeamsAndSpectators() {
		List<UUID> teams = Lists.newArrayList(getAllAliveTeams());
		if (!this.spectators.isEmpty()) {
			teams.addAll(this.spectators);
		}
		return teams;
	}
	
	public void killPlayer(UUID killedUUID) {
		if (this.simpleDuel != null) {
			if (this.simpleDuel.firstTeamAlive.contains(killedUUID)) {
				this.simpleDuel.firstTeamAlive.remove(killedUUID);
			}
			if (this.simpleDuel.secondTeamAlive.contains(killedUUID)) {
				this.simpleDuel.secondTeamAlive.remove(killedUUID);
			}
			return;
		}
		if (this.ffaDuel != null) {
			this.ffaDuel.getFfaAlivePlayers().remove(killedUUID);
		}
	}

	public boolean isRanked() {
		return ranked;
	}

	public void setRanked(boolean ranked) {
		this.ranked = ranked;
	}
	
	public void addSpectator(UUID spec) {
		this.spectators.add(spec);
	}
	
	public void removeSpectator(UUID spec) {
		this.spectators.remove(spec);
	}
	
	public boolean hasSpectators() {
		return !this.spectators.isEmpty();
	}
	
	public List<UUID> getAllSpectators() {
		return this.spectators;
	}
	
	public void sendMessage(String message) {
		sendSoundedMessage(message, null);
	}
	
	public void sendSoundedMessage(String message, Sound sound) {
		sendSoundedMessage(message, sound, 1.0f, 1.0f);
	}
	private void sendSoundedMessage(String message, Sound sound, float volume, float pitch) {
		List<UUID> duelPlayers = getAllTeams();
		if (!getAllSpectators().isEmpty()) duelPlayers.addAll(getAllSpectators());
		
		for (UUID uuid : duelPlayers) {
			final Player player = Bukkit.getPlayer(uuid);
			if (player == null) continue;
			
			player.sendMessage(message);
			if (sound != null) player.playSound(player.getLocation(), sound, volume, pitch);
		}
		duelPlayers.clear();
	}
	
	public void showDuelMates() {
		if (!isValid()) {
			return;
		}
		if (this.simpleDuel == null) {
			return;
		}
		if (!this.simpleDuel.firstTeamAlive.isEmpty() && !this.simpleDuel.secondTeamAlive.isEmpty()) {
			if (this.simpleDuel.firstTeamAlive.size() > 1) {
				for (UUID firstUUID : this.simpleDuel.firstTeamAlive) {
					final Player first = Bukkit.getPlayer(firstUUID);
					for (UUID firstSecondUUID : this.simpleDuel.firstTeamAlive) {
						if (firstUUID == firstSecondUUID) continue;
			            final Player second = Bukkit.getPlayer(firstSecondUUID);
			            first.showPlayer(second);
						second.showPlayer(first);
					}
				}
			}
			if (this.simpleDuel.secondTeamAlive.size() > 1) {
				for (UUID firstUUID : this.simpleDuel.secondTeamAlive) {
					final Player first = Bukkit.getPlayer(firstUUID);
					for (UUID firstSecondUUID : this.simpleDuel.secondTeamAlive) {
						if (firstUUID == firstSecondUUID) continue;
			            final Player second = Bukkit.getPlayer(firstSecondUUID);
			            first.showPlayer(second);
						second.showPlayer(first);
					}
				}
			}
		} 
	}
	
	public void showDuelPlayer() {
		if (!isValid()) {
			return;
		}
		if (this.simpleDuel != null) {
			if (!this.simpleDuel.firstTeamAlive.isEmpty() && !this.simpleDuel.secondTeamAlive.isEmpty()) {
				for (UUID firstUUID : this.simpleDuel.firstTeamAlive) {
					final Player first = Bukkit.getPlayer(firstUUID);
					for (UUID secondUUID : this.simpleDuel.secondTeamAlive) {
		                final Player second = Bukkit.getPlayer(secondUUID);
						first.showPlayer(second);
						second.showPlayer(first);
					}
				}
			}
		}
		if (this.ffaDuel != null) {
			if (!this.ffaDuel.getFfaAlivePlayers().isEmpty()) {
				for (UUID firstUUID : ffaDuel.getFfaAlivePlayers()) {
					final Player first = Bukkit.getPlayer(firstUUID);
					for (UUID secondUUID : ffaDuel.getFfaAlivePlayers()) {
						if (secondUUID == firstUUID) continue;
						final Player second = Bukkit.getPlayer(secondUUID);
						first.showPlayer(second);
						second.showPlayer(first);
					}
				}
			}
		}
	}
	
	public boolean containPlayer(Player player) {
		if (player == null) {
			return false;
		}
		if (this.simpleDuel != null) {
			return (this.simpleDuel.firstTeam.contains(player.getUniqueId()) || this.simpleDuel.secondTeam.contains(player.getUniqueId()));
		}
		if (this.ffaDuel != null) {
			return this.ffaDuel.getFfaPlayers().contains(player.getUniqueId());
		}
		return false;
	}
	
	public boolean isValid() {
		if (this.simpleDuel != null) {
			return (!this.simpleDuel.firstTeam.isEmpty() && !this.simpleDuel.secondTeam.isEmpty() && !this.simpleDuel.firstTeamAlive.isEmpty() && !this.simpleDuel.secondTeamAlive.isEmpty());
		}
		if (this.ffaDuel != null) {
			return (!this.ffaDuel.getFfaPlayers().isEmpty() && !this.ffaDuel.getFfaAlivePlayers().isEmpty());
		}
		return false;
	}
	
	public void setDuelPlayersStatusTo(PlayerStatus status) {
		for (UUID playersUUID : getAllTeams()) {
			if (Bukkit.getPlayer(playersUUID) == null) continue;
			PlayerManager.get(playersUUID).setStatus(status);
		}
	}
	
	public int getTimeBeforeDuel() {
		return this.timeBeforeDuel;
	}
	
	public void addDrops(UUID uuid) {
		this.drops.add(uuid);
	}
	
	public void removeDrops(UUID uuid) {
		this.drops.remove(uuid);
	}
	
	public boolean containDrops(UUID uuid) {
		return this.drops.contains(uuid);
	}
	
	public void clearDrops() {
		if (this.drops.isEmpty()) {
			return;
		}
		final World world = Bukkit.getWorld("world");
		final Iterator<Entity> it = world.getEntities().iterator();
		while (it.hasNext()) {
			Entity entities = it.next();
			if (entities == null || (!(entities instanceof Item) && !(entities instanceof Arrow)) && !this.drops.contains(entities.getUniqueId())) continue;
			entities.remove();
		}
	}
	
	private BukkitTask initXpBarRunnable() {
		if (this.ladder != Ladders.NODEBUFF && this.ladder != Ladders.NOENCHANT) {
			return null;
		}
		return new BukkitRunnable() {
			
			@Override
			public void run() {
				for (UUID uuids : Duel.this.getAllAliveTeams()) {
					final PlayerManager pm = PlayerManager.get(uuids);
					if (pm == null) continue;
					if (!pm.getMatchStats().isEnderPearlCooldownActive()) continue;
					Duel.this.updateXpBar(pm);
				}
			}
		}.runTaskTimerAsynchronously(Main.getInstance(), 1, 1);
	}
	
	public void cancelTask() {
		if (this.xpBarRunnable == null) {
			return;
		}
		this.xpBarRunnable.cancel();
	}
	
	private void updateXpBar(PlayerManager pm) {
		final Player player = pm.getPlayer();
	    final float xpPercentage = Math.max(0.0f, Math.min(99.9f, ((float) pm.getMatchStats().getEnderPearlCooldown() / (14 * 1000)) * 100));
	    player.setExp(xpPercentage / 100);
	}
	
	public long getStartTime() {
		return this.startTime;
	}
}
