package us.noks.smallpractice.objects.managers;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.google.common.collect.Lists;

import net.minecraft.util.com.google.common.collect.Maps;
import us.noks.smallpractice.Main;
import us.noks.smallpractice.enums.PlayerStatus;
import us.noks.smallpractice.party.Party;
import us.noks.smallpractice.utils.DuelRequest;
import us.noks.smallpractice.utils.Messages;

public class RequestManager {
	
	public static RequestManager instance = new RequestManager();
	public static RequestManager getInstance() {
		return instance;
	}
	
	private Map<UUID, Map<UUID, DuelRequest>> duelRequestMap = Maps.newHashMap();
	
	public void addDuelRequest(Player requested, Player requester, DuelRequest request) {
        this.duelRequestMap.get(requested.getUniqueId()).put(requester.getUniqueId(), request);
    }
    
    public boolean hasDuelRequestFromPlayer(Player requested, Player requester) {
        return this.duelRequestMap.get(requested.getUniqueId()).containsKey(requester.getUniqueId());
    }
    
    public boolean hasDuelRequests(Player player) {
        return this.duelRequestMap.containsKey(player.getUniqueId());
    }
    
    public void removeDuelRequest(Player player, Player requester) {
        this.duelRequestMap.get(player.getUniqueId()).remove(requester.getUniqueId());
    }
    
    public DuelRequest getDuelRequest(Player requested, Player requester) {
        return this.duelRequestMap.get(requested.getUniqueId()).get(requester.getUniqueId());
    }
    
    public void sendDuelRequest(Player requester, Player requested) {
		if (PlayerManager.get(requester.getUniqueId()).getStatus() != PlayerStatus.SPAWN || PlayerManager.get(requested.getUniqueId()).getStatus() != PlayerStatus.SPAWN) {
			requester.sendMessage(Messages.getInstance().TARGET_OR_PLAYER_ARENT_IN_THE_SPAWN);
			return;
		}
		PlayerManager.get(requester.getUniqueId()).setRequestTo(requested.getUniqueId());
		requester.openInventory(Main.getInstance().getRoundInventory());
	}
	
	public void acceptDuelRequest(Player requested, Player requester, DuelRequest duelrequest) {
		if (PlayerManager.get(requester.getUniqueId()).getStatus() != PlayerStatus.SPAWN || PlayerManager.get(requested.getUniqueId()).getStatus() != PlayerStatus.SPAWN) {
			requested.sendMessage(Messages.getInstance().TARGET_OR_PLAYER_ARENT_IN_THE_SPAWN);
			return;
		}
		if (!hasDuelRequestFromPlayer(requested, requester)) {
			requested.sendMessage(ChatColor.RED + "This player doesn't request you to duel!");
			return;
		}
		removeDuelRequest(requested, requester);
		Party requesterParty = PartyManager.getInstance().getParty(requester.getUniqueId());
        Party requestedParty = PartyManager.getInstance().getParty(requested.getUniqueId());
        if ((requesterParty != null && requestedParty == null) || (requestedParty != null && requesterParty == null)) {
            requested.sendMessage(ChatColor.RED + "Either you or this player are in a party!");
            return;
        }
		if (requestedParty != null && requesterParty != null) {
			DuelManager.getInstance().startDuel(requester.getUniqueId(), requested.getUniqueId(), requesterParty.getAllMembersOnline(), requestedParty.getAllMembersOnline(), false, duelrequest.getRounds());
			return;
		}
		List<UUID> firstTeam = Lists.newArrayList();
		firstTeam.add(requester.getUniqueId());
		List<UUID> secondTeam = Lists.newArrayList();
		secondTeam.add(requested.getUniqueId());
		
		DuelManager.getInstance().startDuel(null, null, firstTeam, secondTeam, false, duelrequest.getRounds());
	}
}
