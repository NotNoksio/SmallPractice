package io.noks.smallpractice.objects.duel;

import java.util.List;
import java.util.UUID;

import com.google.common.collect.Lists;

public class SimpleDuel {
	private final UUID firstTeamPartyLeaderUUID;
	private final UUID secondTeamPartyLeaderUUID;
	private final List<UUID> firstTeam;
	private final List<UUID> secondTeam;
	private final List<UUID> firstTeamAlive;
	private final List<UUID> secondTeamAlive;
	
	public SimpleDuel(UUID firstTeamLeaderUUID, UUID secondTeamLeaderUUID, List<UUID> firstTeam, List<UUID> secondTeam) {
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
}
