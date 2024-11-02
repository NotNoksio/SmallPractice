package io.noks.smallpractice.objects.duel;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
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
import io.noks.smallpractice.utils.BlockStorage;
import net.minecraft.util.com.google.common.collect.Sets;

public abstract class Duel {
	private final Arena arena;
	private final Ladders ladder;
	private final boolean ranked;
	private final List<UUID> spectators;
	private final Set<UUID> drops;
	private DuelState state;
	protected BukkitTask task;
	private @Nullable BlockStorage blockStorage;
	
	public Duel(Arena arena, Ladders ladder, boolean ranked) {
		this.arena = arena;
		this.ladder = ladder;
		this.spectators = Lists.newArrayList();
		this.ranked = ranked;
		this.drops = Sets.newHashSet();
		this.state = DuelState.WAITING;
		if (ladder == Ladders.SPLEEF) {
			this.blockStorage = new BlockStorage();
		}
	}
	
	public Arena getArena() {
		return this.arena;
	}
	
	public Ladders getLadder() {
		return this.ladder;
	}
	
	public abstract List<UUID> getAllTeams();
	
	public abstract List<UUID> getAllAliveTeams();
	
	public List<UUID> getAllAliveTeamsAndSpectators() {
		List<UUID> teams = Lists.newArrayList(getAllAliveTeams());
		if (!this.spectators.isEmpty()) {
			teams.addAll(this.spectators);
		}
		return teams;
	}
	
	public abstract void killPlayer(UUID killedUUID);

	public boolean isRanked() {
		return ranked;
	}
	
	public void addSpectator(UUID spec) {
		this.spectators.add(spec);
	}
	
	public void removeSpectator(UUID spec) {
		if (this.blockStorage != null) {
			final Player player = Bukkit.getPlayer(spec);
			this.arena.getLocations()[0].getChunk().createFakeBlockUpdate(this.blockStorage.getAllLocations(), this.blockStorage.getAllOldIds(), this.blockStorage.getAllOldDatas()).sendTo(player);
		}
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
	
	protected void showDuelMates() {} // TODO y is this nowhere?
	
	public abstract void showDuelPlayer();
	
	public abstract boolean containPlayer(Player player);
	
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
		final Iterator<Entity> it = this.arena.getLocations()[0].getWorld().getEntities().iterator();
		while (it.hasNext()) {
			final Entity entities = it.next();
			if (entities == null || (!(entities instanceof Item) && !(entities instanceof Arrow)) && !this.drops.contains(entities.getUniqueId())) continue;
			entities.remove();
		}
	}
	
	public void replaceBlockFromStorage() {
		if (this.blockStorage == null) {
			return;
		}
		for (UUID uuids : this.getAllAliveTeamsAndSpectators()) {
			final Player duelPlayers = Bukkit.getServer().getPlayer(uuids);
			if (duelPlayers == null) continue;
			this.arena.getLocations()[0].getChunk().createFakeBlockUpdate(this.blockStorage.getAllLocations(), this.blockStorage.getAllOldIds(), this.blockStorage.getAllOldDatas()).sendTo(duelPlayers);
		}
	}
	
	public void launchCountdownTask() {
		this.task = this.initCountdownTask();
	}
	private BukkitTask initCountdownTask() {
		return new BukkitRunnable() {
			private byte timeBeforeDuel = 5;
			
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
			private short tick = 0;
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
					if (!Duel.this.ladder.containsEnderpearl() || Duel.this.spectators.contains(uuids)) {
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
	
	public BlockStorage getBlockStorage() {
		return this.blockStorage;
	}
	
	public void cancelTask() {
		if (this.task == null) {
			return;
		}
		this.task.cancel();
	}
}
