package io.noks.smallpractice.objects.duel;

import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.google.common.collect.Lists;

import io.noks.smallpractice.arena.Arena;
import io.noks.smallpractice.enums.Ladders;

public class SimpleDuel extends Duel {
	private final UUID firstTeamPartyLeaderUUID;
	private final UUID secondTeamPartyLeaderUUID;
	private final List<UUID> firstTeam;
	private final List<UUID> secondTeam;
	private final List<UUID> firstTeamAlive;
	private final List<UUID> secondTeamAlive;
	
	public SimpleDuel(Arena arena, Ladders ladder, UUID firstTeamLeaderUUID, UUID secondTeamLeaderUUID, List<UUID> firstTeam, List<UUID> secondTeam, boolean ranked) {
		super(arena, ladder, ranked);
		this.firstTeamPartyLeaderUUID = firstTeamLeaderUUID;
		this.secondTeamPartyLeaderUUID = secondTeamLeaderUUID;
		this.firstTeam = Lists.newArrayList(firstTeam);
		this.secondTeam = Lists.newArrayList(secondTeam);
		this.firstTeamAlive = Lists.newArrayList(firstTeam);
		this.secondTeamAlive = Lists.newArrayList(secondTeam);
	}
	
	public UUID getFirstTeamPartyLeaderUUID() {
		return this.firstTeamPartyLeaderUUID;
	}
	public UUID getSecondTeamPartyLeaderUUID() {
		return this.secondTeamPartyLeaderUUID;
	}
	
	public List<UUID> getFirstTeam(){
		return this.firstTeam;
	}
	public List<UUID> getSecondTeam(){
		return this.secondTeam;
	}
	
	public List<UUID> getFirstTeamAlive(){
		return this.firstTeamAlive;
	}
	public List<UUID> getSecondTeamAlive(){
		return this.secondTeamAlive;
	}

	@Override
	public List<UUID> getAllTeams() {
		List<UUID> teams = Lists.newArrayList();
		if (!this.getFirstTeam().isEmpty()) {
			teams.addAll(this.getFirstTeam());
		}
		if (!this.getSecondTeam().isEmpty()) {
			teams.addAll(this.getSecondTeam());
		}
		return teams;
	}

	@Override
	public List<UUID> getAllAliveTeams() {
		List<UUID> teams = Lists.newArrayList();
		if (!this.getFirstTeamAlive().isEmpty()) {
			teams.addAll(this.getFirstTeamAlive());
		}
		if (!this.getSecondTeamAlive().isEmpty()) {
			teams.addAll(this.getSecondTeamAlive());
		}
		return teams;
	}

	@Override
	public void killPlayer(UUID killedUUID) {
		if (this.getFirstTeamAlive().contains(killedUUID)) {
			this.getFirstTeamAlive().remove(killedUUID);
			return;
		}
		if (this.getSecondTeamAlive().contains(killedUUID)) {
			this.getSecondTeamAlive().remove(killedUUID);
		}
	}
	
	@Override
	protected void showDuelMates() {
		if (!this.getFirstTeamAlive().isEmpty() && !this.getSecondTeamAlive().isEmpty()) {
			if (this.getFirstTeamAlive().size() > 1) {
				for (UUID firstUUID : this.getFirstTeamAlive()) {
					final Player first = Bukkit.getPlayer(firstUUID);
					for (UUID firstSecondUUID : this.getFirstTeamAlive()) {
						if (firstUUID == firstSecondUUID) continue;
			            final Player second = Bukkit.getPlayer(firstSecondUUID);
			            first.showPlayer(second);
						second.showPlayer(first);
					}
				}
			}
			if (this.getSecondTeamAlive().size() > 1) {
				for (UUID firstUUID : this.getSecondTeamAlive()) {
					final Player first = Bukkit.getPlayer(firstUUID);
					for (UUID firstSecondUUID : this.getSecondTeamAlive()) {
						if (firstUUID == firstSecondUUID) continue;
			            final Player second = Bukkit.getPlayer(firstSecondUUID);
			            first.showPlayer(second);
						second.showPlayer(first);
					}
				}
			}
		}
	}
	
	@Override
	public void showDuelPlayer() {
		if (!this.getFirstTeamAlive().isEmpty() && !this.getSecondTeamAlive().isEmpty()) {
			for (UUID firstUUID : this.getFirstTeamAlive()) {
				final Player first = Bukkit.getPlayer(firstUUID);
				for (UUID secondUUID : this.getSecondTeamAlive()) {
		            final Player second = Bukkit.getPlayer(secondUUID);
					first.showPlayer(second);
					second.showPlayer(first);
				}
			}
		}
	}

	@Override
	public boolean containPlayer(Player player) {
		return this.firstTeam.contains(player.getUniqueId()) || this.secondTeam.contains(player.getUniqueId());
	}
}
