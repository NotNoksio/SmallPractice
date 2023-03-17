package io.noks.smallpractice.commands;

import java.util.List;
import java.util.UUID;

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
import io.noks.smallpractice.party.Party;
import io.noks.smallpractice.party.PartyState;
import net.minecraft.util.com.google.common.collect.Lists;

public class SpectateCommand implements CommandExecutor {
	
	private Main main;
	public SpectateCommand(Main main) {
		this.main = main;
		this.main.getCommand("spectate").setExecutor(this);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player)) {
			return false;
		}
		if (args.length != 1) {
			sender.sendMessage(ChatColor.RED + "Usage: /spectate <player>");
			return false;
		}
		final Player player = (Player) sender;
		final PlayerManager pm = PlayerManager.get(player.getUniqueId());
		
		if (this.main.getPartyManager().hasParty(player.getUniqueId())) {
			player.sendMessage(ChatColor.RED + "You are in party!");
			return false;
		}
		if (pm.getStatus() != PlayerStatus.SPAWN && pm.getStatus() != PlayerStatus.SPECTATE) {
			player.sendMessage(ChatColor.RED + "You are not in the spawn!");
			return false;
		}
		final Player target = this.main.getServer().getPlayer(args[0]);
		
		if (target == null) {
			player.sendMessage(ChatColor.RED + "This player is not online.");
			return false;
		}
		if (target == player) {
			player.sendMessage(ChatColor.RED + "You can't execute that command on yourself!");
			return false;
		}
		final PlayerManager tm = PlayerManager.get(target.getUniqueId());
		
		if (tm.getStatus() != PlayerStatus.WAITING && tm.getStatus() != PlayerStatus.DUEL) {
			player.sendMessage(ChatColor.RED + "That player isn't in duel!");
			return false;
		}
		if (this.main.getPartyManager().hasParty(player.getUniqueId()) && this.main.getPartyManager().hasParty(target.getUniqueId())) {
			final Party party = this.main.getPartyManager().getParty(player.getUniqueId());
			final Party targetParty = this.main.getPartyManager().getParty(target.getUniqueId());
				
			if (party != targetParty) {
				return false;
			}
			if (targetParty.getPartyState() != PartyState.DUELING) {
				return false;
			}
			this.setPlayerInSpectator(player, pm, target);
			return true;
		}
		if (pm.getStatus() != PlayerStatus.SPECTATE) {
			pm.setStatus(PlayerStatus.SPECTATE);
		} else if (pm.getSpectate() != null) {
			final Duel duel = this.main.getDuelManager().getDuelFromPlayerUUID(pm.getSpectate().getUniqueId());
			duel.removeSpectator(player.getUniqueId());
		} else {
			for (Arenas allArenas : Arena.getInstance().getArenaList()) {
				if (!allArenas.getAllSpectators().contains(player.getUniqueId())) continue;
				allArenas.removeSpectator(player.getUniqueId());
			}
		}
		this.setPlayerInSpectator(player, pm, target);
		return true;
	}
	
	private void setPlayerInSpectator(Player player, PlayerManager pm, Player target) {
		pm.hideAllPlayer();
		pm.setSpectate(target);
		player.setScoreboard(target.getScoreboard());
		
		final Duel duel = this.main.getDuelManager().getDuelFromPlayerUUID(target.getUniqueId());
		duel.addSpectator(player.getUniqueId());
		
		player.setAllowFlight(true);
		player.setFlying(true);
		player.teleport(target.getLocation().add(0, 2, 0));
		final List<UUID> duelPlayers = Lists.newArrayList(duel.getAllAliveTeams());
			
		for (UUID uuid : duelPlayers) {
			Player dplayers = this.main.getServer().getPlayer(uuid);
			player.showPlayer(dplayers);
		}
		this.main.getItemManager().giveSpectatorItems(player);
		player.sendMessage(ChatColor.GREEN + "You are now spectating " + ChatColor.YELLOW + target.getName());
		duel.sendMessage(ChatColor.YELLOW + player.getName() + ChatColor.DARK_AQUA + " is now spectating.");
	}
}
