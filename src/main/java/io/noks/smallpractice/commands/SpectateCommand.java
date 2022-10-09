package io.noks.smallpractice.commands;

import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import io.noks.smallpractice.Main;
import io.noks.smallpractice.arena.Arena;
import io.noks.smallpractice.arena.Arena.Arenas;
import io.noks.smallpractice.enums.PlayerStatus;
import io.noks.smallpractice.objects.duel.Duel;
import io.noks.smallpractice.objects.managers.PlayerManager;
import net.minecraft.util.com.google.common.collect.Lists;

public class SpectateCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player)) {
			return false;
		}
		if (args.length != 1) {
			sender.sendMessage(ChatColor.RED + "Usage: /spectate <player>");
			return false;
		}
		Player player = (Player) sender;
		PlayerManager pm = PlayerManager.get(player.getUniqueId());
		
		if (Main.getInstance().getPartyManager().hasParty(player.getUniqueId())) {
			player.sendMessage(ChatColor.RED + "You are in party!");
			return false;
		}
		if (pm.getStatus() != PlayerStatus.SPAWN && pm.getStatus() != PlayerStatus.SPECTATE) {
			player.sendMessage(ChatColor.RED + "You are not in the spawn!");
			return false;
		}
		Player target = Bukkit.getPlayer(args[0]);
		
		if (target == null) {
			player.sendMessage(ChatColor.RED + "This player is not online.");
			return false;
		}
		if (target == player) {
			player.sendMessage(ChatColor.RED + "You can't execute that command on yourself!");
			return false;
		}
		PlayerManager tm = PlayerManager.get(target.getUniqueId());
		/*if (PartyManager.getInstance().hasParty(target.getUniqueId()) && tm.getStatus() == PlayerStatus.SPAWN) {
			Party party = PartyManager.getInstance().getParty(target.getUniqueId());
			
			if (party.getPartyState() == PartyState.DUELING) {
				
			}
		}*/
		
		if (tm.getStatus() != PlayerStatus.WAITING && tm.getStatus() != PlayerStatus.DUEL) {
			player.sendMessage(ChatColor.RED + "That player isn't in duel!");
			return false;
		}
		if (pm.getStatus() != PlayerStatus.SPECTATE) {
			pm.setStatus(PlayerStatus.SPECTATE);
		} else if (pm.getSpectate() != null) {
			Duel duel = Main.getInstance().getDuelManager().getDuelFromPlayerUUID(pm.getSpectate().getUniqueId());
			duel.removeSpectator(player.getUniqueId());
		} else {
			for (Arenas allArenas : Arena.getInstance().getArenaList().values()) {
				if (!allArenas.getAllSpectators().contains(player.getUniqueId())) continue;
				allArenas.removeSpectator(player.getUniqueId());
			}
		}
		pm.hideAllPlayer();
		pm.setSpectate(target);
		player.setScoreboard(target.getScoreboard());
		
		Duel duel = Main.getInstance().getDuelManager().getDuelFromPlayerUUID(target.getUniqueId());
		duel.addSpectator(player.getUniqueId());
		
		player.setAllowFlight(true);
		player.setFlying(true);
		player.teleport(target.getLocation().add(0, 2, 0));
		List<UUID> duelPlayers = Lists.newArrayList(duel.getAllAliveTeams());
			
		for (UUID uuid : duelPlayers) {
			Player dplayers = Bukkit.getPlayer(uuid);
			player.showPlayer(dplayers);
		}
		Main.getInstance().getItemManager().giveSpectatorItems(player);
		player.sendMessage(ChatColor.GREEN + "You are now spectating " + ChatColor.YELLOW + target.getName());
		duel.sendMessage(ChatColor.YELLOW + player.getName() + ChatColor.DARK_AQUA + " is now spectating.");
		return true;
	}
}
