package us.noks.smallpractice.objects.managers;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import us.noks.smallpractice.enums.PlayerStatus;
import us.noks.smallpractice.party.Party;
import us.noks.smallpractice.utils.Messages;

public class RequestManager {
	private static RequestManager instance = new RequestManager();
	public static RequestManager getInstance() {
		return instance;
	}
    
    public void sendDuelRequest(Player requester, Player requested) {
		if (PlayerManager.get(requester.getUniqueId()).getStatus() != PlayerStatus.SPAWN || PlayerManager.get(requested.getUniqueId()).getStatus() != PlayerStatus.SPAWN) {
			requester.sendMessage(Messages.getInstance().TARGET_OR_PLAYER_ARENT_IN_THE_SPAWN);
			return;
		}
		TextComponent l1 = new TextComponent();
		l1.setText(requester.getName());
		l1.setColor(net.md_5.bungee.api.ChatColor.YELLOW);
	    
		TextComponent l1a = new TextComponent();
		l1a.setText(" has requested to duel ");
		l1a.setColor(net.md_5.bungee.api.ChatColor.DARK_AQUA);
	    
		TextComponent l1b = new TextComponent();
		l1b.setText("Click here to accept.");
		l1b.setColor(net.md_5.bungee.api.ChatColor.GREEN);
		l1b.setBold(true);
		l1b.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(net.md_5.bungee.api.ChatColor.GREEN + "Click this message to accept " + requester.getName()).create()));
		l1b.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/accept " + requester.getName()));
	    
		l1.addExtra(l1a);
		l1.addExtra(l1b);
	    
		requested.spigot().sendMessage(l1);
		requester.sendMessage(ChatColor.DARK_AQUA + "You sent a duel request to " + ChatColor.YELLOW + requested.getName());
		PlayerManager.get(requester.getUniqueId()).setRequestTo(requested.getUniqueId());
	}
	
	public void acceptDuelRequest(Player requested, Player requester) {
		if (PlayerManager.get(requester.getUniqueId()).getStatus() != PlayerStatus.SPAWN || PlayerManager.get(requested.getUniqueId()).getStatus() != PlayerStatus.SPAWN) {
			requested.sendMessage(Messages.getInstance().TARGET_OR_PLAYER_ARENT_IN_THE_SPAWN);
			return;
		}
		if (PlayerManager.get(requester.getUniqueId()).getRequestTo() != requested.getUniqueId()) {
			requested.sendMessage(ChatColor.RED + "This player doesn't request you to duel!");
			return;
		}
		PlayerManager.get(requester.getUniqueId()).setRequestTo(null);
		Party requesterParty = PartyManager.getInstance().getParty(requester.getUniqueId());
        Party requestedParty = PartyManager.getInstance().getParty(requested.getUniqueId());
        if ((requesterParty != null && requestedParty == null) || (requestedParty != null && requesterParty == null)) {
            requested.sendMessage(ChatColor.RED + "Either you or this player are in a party!");
            return;
        }
		if (requestedParty != null && requesterParty != null) {
			DuelManager.getInstance().startDuel(requester.getUniqueId(), requested.getUniqueId(), requesterParty.getAllMembersOnline(), requestedParty.getAllMembersOnline(), false);
			return;
		}
		DuelManager.getInstance().startDuel(requester.getUniqueId(), requested.getUniqueId(), false);
	}
}
