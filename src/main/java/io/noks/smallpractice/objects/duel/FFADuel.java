package io.noks.smallpractice.objects.duel;

import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.google.common.collect.Lists;

import io.noks.smallpractice.arena.Arena;
import io.noks.smallpractice.enums.Ladders;

public class FFADuel extends Duel {
	private UUID ffaPartyLeaderUUID;
    private List<UUID> ffaPlayers;
    private List<UUID> ffaAlivePlayers;
    
    public FFADuel(Arena arena, Ladders ladder, UUID ffaLeaderUUID, List<UUID> ffaPlayers) {
    	super(arena, ladder, false);
    	this.ffaPartyLeaderUUID = ffaLeaderUUID;
		this.ffaPlayers = Lists.newArrayList(ffaPlayers);
		this.ffaAlivePlayers = Lists.newArrayList(ffaPlayers);
    }
    
    public UUID getFfaPartyLeaderUUID() {
        return this.ffaPartyLeaderUUID;
    }
    
    public List<UUID> getFfaPlayers() {
        return this.ffaPlayers;
    }
    
    public List<UUID> getFfaAlivePlayers() {
        return this.ffaAlivePlayers;
    }

	@Override
	public List<UUID> getAllTeams() {
		return Lists.newArrayList(this.ffaPlayers);
	}

	@Override
	public List<UUID> getAllAliveTeams() {
		return this.ffaAlivePlayers;
	}

	@Override
	public void killPlayer(UUID killedUUID) {
		if (!this.ffaAlivePlayers.contains(killedUUID)) {
			new Exception("INVALID PLAYER");
			return;
		}
		this.ffaAlivePlayers.remove(killedUUID);
	}

	@Override
	public void showDuelPlayer() {
		if (!this.getFfaAlivePlayers().isEmpty()) {
			for (UUID firstUUID : getFfaAlivePlayers()) {
				final Player first = Bukkit.getPlayer(firstUUID);
				for (UUID secondUUID : getFfaAlivePlayers()) {
					if (secondUUID == firstUUID) continue;
					final Player second = Bukkit.getPlayer(secondUUID);
					first.showPlayer(second);
					second.showPlayer(first);
				}
			}
		}
	}

	@Override
	public boolean containPlayer(Player player) {
		return this.ffaPlayers.contains(player.getUniqueId());
	}
}
