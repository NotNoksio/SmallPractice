package us.noks.smallpractice.commands;

import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.google.common.collect.Lists;

import us.noks.smallpractice.enums.PlayerStatus;
import us.noks.smallpractice.objects.Duel;
import us.noks.smallpractice.objects.managers.DuelManager;
import us.noks.smallpractice.objects.managers.PartyManager;
import us.noks.smallpractice.objects.managers.PlayerManager;

public class SpectateCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player)) {
			return false;
		}
		Player player = (Player) sender;
		PlayerManager pm = PlayerManager.get(player);
		
		if (args.length > 1 || args.length < 1) {
			player.sendMessage(ChatColor.RED + "Usage: /spectate <player>");
			return false;
		}
		if (PartyManager.getInstance().hasParty(player.getUniqueId())) {
			player.sendMessage(ChatColor.RED + "You are in party!");
			return false;
		}
		if (pm.getStatus() != PlayerStatus.SPAWN) {
			player.sendMessage(ChatColor.RED + "You are not in the spawn!");
			return false;
		}
		Player target = Bukkit.getPlayer(args[0]);
		
		if (target == null) {
			player.sendMessage(ChatColor.RED + "Invalid target!");
			return false;
		}
		if (target == player) {
			player.sendMessage(ChatColor.RED + "You can't spectate yourself.");
			return false;
		}
		PlayerManager tm = PlayerManager.get(target);
		
		if (tm.getStatus() != PlayerStatus.WAITING && tm.getStatus() != PlayerStatus.DUEL) {
			player.sendMessage(ChatColor.RED + "That player isn't in duel!");
			return false;
		}
		pm.setStatus(PlayerStatus.SPECTATE);
		pm.hideAllPlayer();
		pm.setSpectate(target);
		
		Duel duel = DuelManager.getInstance().getDuelFromPlayerUUID(target.getUniqueId());
		duel.addSpectator(player.getUniqueId());
		
		player.setAllowFlight(true);
		player.setFlying(true);
		player.teleport(target.getLocation().add(0, 2, 0));
		
		List<UUID> duelPlayers = Lists.newArrayList();
		duelPlayers.addAll(duel.getFirstTeamAlive());
		duelPlayers.addAll(duel.getSecondTeamAlive());
			
		for (UUID uuid : duelPlayers) {
			Player dplayers = Bukkit.getPlayer(uuid);
			player.showPlayer(dplayers);
		}
		pm.giveSpectateItem();
		player.sendMessage(ChatColor.GREEN + "You are now spectating " + ChatColor.YELLOW + target.getName());
		duel.sendMessage(ChatColor.YELLOW + player.getName() + ChatColor.DARK_AQUA + " is now spectating.");
		return false;
	}
}
