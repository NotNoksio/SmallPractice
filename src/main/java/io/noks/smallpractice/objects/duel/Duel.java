package io.noks.smallpractice.objects.duel;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.DisplaySlot;

import com.google.common.collect.Lists;

import io.noks.smallpractice.Main;
import io.noks.smallpractice.arena.Arena;
import io.noks.smallpractice.enums.DuelState;
import io.noks.smallpractice.enums.Ladders;
import io.noks.smallpractice.enums.PlayerStatus;
import io.noks.smallpractice.objects.managers.PlayerManager;
import net.minecraft.util.com.google.common.collect.Sets;

public class Duel {
	private final Arena arena;
	private final Ladders ladder;
	private SimpleDuel simpleDuel;
	private FFADuel ffaDuel;
	private final boolean ranked;
	private final List<UUID> spectators;
	private final Set<UUID> drops;
	private DuelState state;
	protected BukkitTask task;
	
	public Duel(Arena arena, Ladders ladder, SimpleDuel simpleDuel, boolean ranked) {
		this.arena = arena;
		this.ladder = ladder;
		this.simpleDuel = simpleDuel;
		this.spectators = Lists.newArrayList();
		this.ranked = ranked;
		this.drops = Sets.newHashSet();
		this.state = DuelState.WAITING;
	}
	
	public Duel(Arena arena, Ladders ladder, FFADuel ffaDuel) {
		this.arena = arena;
		this.ladder = ladder;
		this.ffaDuel = ffaDuel;
		this.spectators = Lists.newArrayList();
		this.drops = Sets.newHashSet();
		this.ranked = false;
		this.state = DuelState.WAITING;
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
			if (!this.simpleDuel.getFirstTeam().isEmpty()) {
				teams.addAll(this.simpleDuel.getFirstTeam());
			}
			if (!this.simpleDuel.getSecondTeam().isEmpty()) {
				teams.addAll(this.simpleDuel.getSecondTeam());
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
			if (!this.simpleDuel.getFirstTeamAlive().isEmpty()) {
				teams.addAll(this.simpleDuel.getFirstTeamAlive());
			}
			if (!this.simpleDuel.getSecondTeamAlive().isEmpty()) {
				teams.addAll(this.simpleDuel.getSecondTeamAlive());
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
			if (this.simpleDuel.getFirstTeamAlive().contains(killedUUID)) {
				this.simpleDuel.getFirstTeamAlive().remove(killedUUID);
			}
			if (this.simpleDuel.getSecondTeamAlive().contains(killedUUID)) {
				this.simpleDuel.getSecondTeamAlive().remove(killedUUID);
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
	
	private void showDuelMates() {
		if (this.simpleDuel == null) {
			return;
		}
		if (!this.simpleDuel.getFirstTeamAlive().isEmpty() && !this.simpleDuel.getSecondTeamAlive().isEmpty()) {
			if (this.simpleDuel.getFirstTeamAlive().size() > 1) {
				for (UUID firstUUID : this.simpleDuel.getFirstTeamAlive()) {
					final Player first = Bukkit.getPlayer(firstUUID);
					for (UUID firstSecondUUID : this.simpleDuel.getFirstTeamAlive()) {
						if (firstUUID == firstSecondUUID) continue;
			            final Player second = Bukkit.getPlayer(firstSecondUUID);
			            first.showPlayer(second);
						second.showPlayer(first);
					}
				}
			}
			if (this.simpleDuel.getSecondTeamAlive().size() > 1) {
				for (UUID firstUUID : this.simpleDuel.getSecondTeamAlive()) {
					final Player first = Bukkit.getPlayer(firstUUID);
					for (UUID firstSecondUUID : this.simpleDuel.getSecondTeamAlive()) {
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
		if (this.simpleDuel != null) {
			if (!this.simpleDuel.getFirstTeamAlive().isEmpty() && !this.simpleDuel.getSecondTeamAlive().isEmpty()) {
				for (UUID firstUUID : this.simpleDuel.getFirstTeamAlive()) {
					final Player first = Bukkit.getPlayer(firstUUID);
					for (UUID secondUUID : this.simpleDuel.getSecondTeamAlive()) {
		                final Player second = Bukkit.getPlayer(secondUUID);
						first.showPlayer(second);
						second.showPlayer(first);
					}
				}
			}
			return;
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
			return (this.simpleDuel.getFirstTeam().contains(player.getUniqueId()) || this.simpleDuel.getSecondTeam().contains(player.getUniqueId()));
		}
		if (this.ffaDuel != null) {
			return this.ffaDuel.getFfaPlayers().contains(player.getUniqueId());
		}
		return false;
	}
	
	public void setDuelPlayersStatusTo(PlayerStatus status) {
		for (UUID playersUUID : getAllTeams()) {
			if (Bukkit.getPlayer(playersUUID) == null) continue;
			PlayerManager.get(playersUUID).setStatus(status);
		}
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
	
	public void updateState(DuelState state) {
		this.state = state;
	}
	
	public void clearDrops() {
		if (this.drops.isEmpty()) {
			return;
		}
		final World world = Bukkit.getWorld("world");
		final Iterator<Entity> it = world.getEntities().iterator();
		while (it.hasNext()) {
			final Entity entities = it.next();
			if (entities == null || (!(entities instanceof Item) && !(entities instanceof Arrow)) && !this.drops.contains(entities.getUniqueId())) continue;
			entities.remove();
		}
	}
	
	public void launchCountdownTask() {
		this.task = this.initCountdownTask();
	}
	private BukkitTask initCountdownTask() {
		return new BukkitRunnable() {
			private int timeBeforeDuel = 5;
			
			@Override
			public void run() {
				if (Duel.this.state != DuelState.WAITING) {
					return;
				}
				if (this.timeBeforeDuel <= 0) {
					Duel.this.sendSoundedMessage(ChatColor.GREEN + "Duel has started!", Sound.FIREWORK_BLAST);
					Duel.this.showDuelPlayer();
					Duel.this.setDuelPlayersStatusTo(PlayerStatus.DUEL);
					Duel.this.updateState(DuelState.STARTED);
					this.cancel();
					Duel.this.task = Duel.this.initDuelTask();
					return;
				}
				if (this.timeBeforeDuel == 5) {
					Duel.this.showDuelMates();
				}
				if (this.timeBeforeDuel > 0) {
					Duel.this.sendSoundedMessage(ChatColor.DARK_AQUA + "Duel start in " + ChatColor.YELLOW + this.timeBeforeDuel + ChatColor.DARK_AQUA + " second" + (this.timeBeforeDuel > 1 ? "s.." : ".."), Sound.NOTE_PLING);
					this.timeBeforeDuel--;
				}
			}
		}.runTaskTimer(Main.getInstance(), 5, 20);
	}
	private BukkitTask initDuelTask() {
		return new BukkitRunnable() {
			private int tick = 0;
			private long startTime = -1;
			
			private void updateXpBar(PlayerManager pm) {
			    final float xpPercentage = Math.max(0.0f, Math.min(99.9f, ((float) pm.getMatchStats().getEnderPearlCooldown() / (14 * 1000)) * 100));
			    pm.getPlayer().setExp(xpPercentage / 100);
			}
			
			private String format(int sec) {
				return String.format("%02d:%02d", new Object[] { Integer.valueOf(sec / 60), Integer.valueOf(sec % 60) });
			}
			
			@Override
			public void run() {
				if (Duel.this.state != DuelState.STARTED) {
					return;
				}
				if (this.startTime == -1) {
					this.startTime = System.currentTimeMillis();
				}
				this.tick++;
				for (UUID uuids : Duel.this.getAllAliveTeamsAndSpectators()) {
					final PlayerManager pm = PlayerManager.get(uuids);
					if (pm == null) {
						continue;
					}
					if (tick == 10 && pm.getSettings().isScoreboardToggled() && pm.getPlayer().getScoreboard() != null && pm.getPlayer().getScoreboard().getObjective(DisplaySlot.SIDEBAR) != null) {
						pm.getPlayer().getScoreboard().getTeam("time").setSuffix(ChatColor.RESET + this.format((int)((System.currentTimeMillis() - this.startTime) / 1000.0D)));
					}
					if (Duel.this.ladder != Ladders.NODEBUFF && Duel.this.ladder != Ladders.NOENCHANT || Duel.this.spectators.contains(uuids)) {
						continue;
					}
					if (!pm.getMatchStats().isEnderPearlCooldownActive()) continue;
					this.updateXpBar(pm);
				}
				if (tick == 10) {
					this.tick = 0;
				}
			}
		}.runTaskTimerAsynchronously(Main.getInstance(), 1, 1);
	}
	
	public void cancelTask() {
		if (this.task == null) {
			return;
		}
		this.task.cancel();
	}
}
