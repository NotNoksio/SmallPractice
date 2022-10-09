package io.noks.smallpractice.objects.duel;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import io.noks.smallpractice.arena.Arena.Arenas;
import io.noks.smallpractice.enums.Ladders;
import io.noks.smallpractice.enums.PlayerStatus;
import io.noks.smallpractice.objects.managers.PlayerManager;
import net.minecraft.util.com.google.common.collect.Sets;

public class Duel {
	private Arenas arena;
	private Ladders ladder;
	private SimpleDuel simpleDuel;
	private FFADuel ffaDuel;
	private boolean ranked;
	private List<UUID> spectators;
	private int timeBeforeDuel = 5;
	private Set<UUID> drops;
	private Set<Location> brokenBlocks;
	
	public Duel(Arenas arena, Ladders ladder, SimpleDuel simpleDuel, boolean ranked) {
		this.arena = arena;
		this.ladder = ladder;
		this.simpleDuel = simpleDuel;
		this.spectators = Lists.newArrayList();
		this.ranked = ranked;
		this.drops = Sets.newHashSet();
		if (ladder == Ladders.SPLEEF) {
			this.brokenBlocks = Sets.newHashSet();
		}
	}
	
	public Duel(Arenas arena, Ladders ladder, FFADuel ffaDuel) {
		this.arena = arena;
		this.ladder = ladder;
		this.ffaDuel = ffaDuel;
		this.drops = Sets.newHashSet();
		this.ranked = false;
    }
	
	public Arenas getArena() {
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
		if (this.brokenBlocks.isEmpty()) {
			return;
		}
		// TODO: Show broken blocks
	}
	
	public void removeSpectator(UUID spec) {
		this.spectators.remove(spec);
		if (this.brokenBlocks.isEmpty()) {
			return;
		}
		// TODO: Put broken blocks in place
	}
	
	public boolean hasSpectator() {
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
	
	public void showDuelPlayer() {
		if (!isValid()) {
			return;
		}
		if (this.simpleDuel != null) {
			if (!this.simpleDuel.firstTeamAlive.isEmpty() && !this.simpleDuel.secondTeamAlive.isEmpty()) {
				for (UUID firstUUID : this.simpleDuel.firstTeamAlive) {
					for (UUID secondUUID : this.simpleDuel.secondTeamAlive) {
		                Player first = Bukkit.getPlayer(firstUUID);
		                Player second = Bukkit.getPlayer(secondUUID);
						first.showPlayer(second);
						second.showPlayer(first);
					}
				}
			}
		}
		if (this.ffaDuel != null) {
			if (!this.ffaDuel.getFfaAlivePlayers().isEmpty()) {
				
			}
		}
	}
	
	public boolean containPlayer(Player player) {
		Preconditions.checkNotNull(player, "Player cannot be null");
		if (this.simpleDuel != null) {
			return (this.simpleDuel.firstTeam.contains(player.getUniqueId()) || this.simpleDuel.secondTeam.contains(player.getUniqueId()));
		}
		return false;
	}
	
	public boolean isValid() {
		if (this.simpleDuel != null) {
			return (!this.simpleDuel.firstTeam.isEmpty() && !this.simpleDuel.secondTeam.isEmpty() && !this.simpleDuel.firstTeamAlive.isEmpty() && !this.simpleDuel.secondTeamAlive.isEmpty());
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
	
	public void addDrops(Item item) {
		this.drops.add(item.getUniqueId());
	}
	
	public void removeDrops(Item item) {
		this.drops.remove(item.getUniqueId());
	}
	
	public boolean containDrops(Item item) {
		return this.drops.contains(item.getUniqueId());
	}
	
	public void clearDrops() {
		if (this.drops.isEmpty()) {
			return;
		}
		final World world = Bukkit.getWorld("world");
		for (Entity entities : world.getEntities()) {
			if (entities == null || !(entities instanceof Item) && !this.drops.contains(entities.getUniqueId())) continue;
			entities.remove();
		}
	}
	
	public void addBrokenBlocksLocation(Location loc) {
		if (brokenBlocks == null) {
			return;
		}
		this.brokenBlocks.add(loc);
	}
	
	public Set<Location> getBrokenBlocksLocation(){
		return this.brokenBlocks;
	}
}
