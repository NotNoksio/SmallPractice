package us.noks.smallpractice.objects.managers;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import us.noks.smallpractice.enums.PlayerStatus;
import us.noks.smallpractice.party.Party;

public class RequestManager {
	private static RequestManager instance = new RequestManager();
	public static RequestManager getInstance() {
		return instance;
	}
    
    public void sendDuelRequest(Player requester, Player requested) {
		if (PlayerManager.get(requester.getUniqueId()).getStatus() != PlayerStatus.SPAWN || PlayerManager.get(requested.getUniqueId()).getStatus() != PlayerStatus.SPAWN) {
			requester.sendMessage(ChatColor.RED + "Either you or this player are not in the spawn!");
			return;
		}
		TextComponent line = new TextComponent();
		line.setText(requester.getName());
		line.setColor(net.md_5.bungee.api.ChatColor.YELLOW);
	    
		TextComponent lineA = new TextComponent();
		lineA.setText(" has requested to duel ");
		lineA.setColor(net.md_5.bungee.api.ChatColor.DARK_AQUA);
	    
		TextComponent lineB = new TextComponent();
		lineB.setText("Click here to accept.");
		lineB.setColor(net.md_5.bungee.api.ChatColor.GREEN);
		lineB.setBold(true);
		lineB.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(net.md_5.bungee.api.ChatColor.GREEN + "Click this message to accept " + requester.getName()).create()));
		lineB.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/accept " + requester.getName()));
		
		TextComponent lineSpace = new TextComponent(" ");
		
		TextComponent lineC = new TextComponent();
		lineC.setText("Click here to deny.");
		lineC.setColor(net.md_5.bungee.api.ChatColor.RED);
		lineC.setBold(true);
		lineC.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(net.md_5.bungee.api.ChatColor.RED + "Click this message to deny " + requester.getName()).create()));
		lineC.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/deny " + requester.getName()));
	    
		line.addExtra(lineA);
		line.addExtra(lineB);
		line.addExtra(lineSpace);
		line.addExtra(lineC);
	    
		requested.spigot().sendMessage(line);
		requester.sendMessage(ChatColor.DARK_AQUA + "You sent a duel request to " + ChatColor.YELLOW + requested.getName());
		PlayerManager.get(requester.getUniqueId()).addRequest(requested.getUniqueId());
	}
	
	public void acceptDuelRequest(Player requested, Player requester) {
		if (PlayerManager.get(requester.getUniqueId()).getStatus() != PlayerStatus.SPAWN || PlayerManager.get(requested.getUniqueId()).getStatus() != PlayerStatus.SPAWN) {
			requested.sendMessage(ChatColor.RED + "Either you or this player are not in the spawn!");
			return;
		}
		if (!PlayerManager.get(requester.getUniqueId()).hasRequest(requested.getUniqueId())) {
			requested.sendMessage(ChatColor.RED + "This player doesn't sent you a duel request!");
			return;
		}
		PlayerManager.get(requester.getUniqueId()).clearRequest();;
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
	
	public void denyDuelRequest(Player requested, Player requester) {
		if (PlayerManager.get(requested.getUniqueId()).getStatus() != PlayerStatus.SPAWN) {
			requested.sendMessage(ChatColor.RED + "You are not in the spawn!");
			return;
		}
		if (!PlayerManager.get(requester.getUniqueId()).hasRequest(requested.getUniqueId())) {
			requested.sendMessage(ChatColor.RED + "This player doesn't sent you a duel request!");
			return;
		}
		PlayerManager.get(requester.getUniqueId()).getRequests().remove(requested.getUniqueId());
		requester.sendMessage(ChatColor.YELLOW + requested.getName() + ChatColor.RED + " has denied your duel request!");
		requested.sendMessage(ChatColor.RED + "You deny the request from " + ChatColor.YELLOW + requester.getName());
	}
}
