package io.noks.smallpractice.objects.duel;

import java.util.List;
import java.util.UUID;

import com.google.common.collect.Lists;

public class SimpleDuel {
	public UUID firstTeamPartyLeaderUUID;
	public UUID secondTeamPartyLeaderUUID;
	public List<UUID> firstTeam;
	public List<UUID> secondTeam;
	public List<UUID> firstTeamAlive;
	public List<UUID> secondTeamAlive;
	
	public SimpleDuel(UUID firstTeamLeaderUUID, UUID secondTeamLeaderUUID, List<UUID> firstTeam, List<UUID> secondTeam) {
		this.firstTeamPartyLeaderUUID = firstTeamLeaderUUID;
		this.secondTeamPartyLeaderUUID = secondTeamLeaderUUID;
		this.firstTeam = Lists.newArrayList(firstTeam);
		this.secondTeam = Lists.newArrayList(secondTeam);
		this.firstTeamAlive = Lists.newArrayList(firstTeam);
		this.secondTeamAlive = Lists.newArrayList(secondTeam);
	}
}
