package us.noks.smallpractice.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import us.noks.smallpractice.objects.managers.DuelManager;
import us.noks.smallpractice.objects.managers.PlayerManager;
import us.noks.smallpractice.utils.PlayerStatus;

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
		
		DuelManager.getInstance().getDuelByPlayer(target).addSpectator(player);
		
		player.setAllowFlight(true);
		player.setFlying(true);
		player.teleport(target.getLocation().add(0, 2, 0));
		
		player.showPlayer(target);
		player.showPlayer(tm.getOpponent());
			
		pm.giveSpectateItem();
		player.sendMessage(ChatColor.GREEN + "You are now in spectator! " + ChatColor.YELLOW + "Do \"/leave\" to leave spectator mode.");
		DuelManager.getInstance().getDuelByPlayer(target).sendMessage(ChatColor.YELLOW + player.getName() + ChatColor.DARK_AQUA + " is now spectating.");
		return false;
	}
}
