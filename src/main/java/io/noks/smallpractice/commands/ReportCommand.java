package io.noks.smallpractice.commands;

import java.util.StringJoiner;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import io.noks.smallpractice.objects.Cooldown;
import io.noks.smallpractice.objects.managers.PlayerManager;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class ReportCommand implements CommandExecutor {
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player)) {
			return false;
		}
		if (args.length < 2) {
			sender.sendMessage(org.bukkit.ChatColor.RED + "Usage: /report <player> <reason>");
			return false;
		}
		final Player target = Bukkit.getPlayer(args[0]);
			
		if (target == null) {
			sender.sendMessage(ChatColor.RED + "This player is not online.");
			return false;
		}
		final Player player = (Player) sender;
		if (target == player) {
			player.sendMessage(ChatColor.RED + "You can't execute that command on yourself!");
			return false;
		}
		final Cooldown cooldown = PlayerManager.get(player.getUniqueId()).getCooldown();
		if (cooldown.isActive(this.getClass().getSimpleName())) {
			final long secondsLeft = ((cooldown.getTime(this.getClass().getSimpleName()) / 1000) + 30) - (System.currentTimeMillis() / 1000);
			if (secondsLeft > 0) {
				player.sendMessage(org.bukkit.ChatColor.RED + "You cant report for another " + secondsLeft + " seconds!");
				return false;
			}
		}
		StringJoiner reason = new StringJoiner(" ");
		for (int i = 1; i < args.length; i++) {
			reason.add(args[i]);
		}
				
		final TextComponent l1 = new TextComponent();
		l1.setText("(");
		l1.setColor(ChatColor.GOLD);

		final TextComponent l1a = new TextComponent();
		l1a.setText("REPORT");
		l1a.setColor(ChatColor.DARK_AQUA);
		l1a.setBold(true);

		final TextComponent l1b = new TextComponent();
		l1b.setText(") ");
		l1b.setColor(ChatColor.GOLD);

		final TextComponent l1c = new TextComponent();
		l1c.setText(player.getName());
		l1c.setColor(ChatColor.YELLOW);

		final TextComponent l1d = new TextComponent();
		l1d.setText(" has reported ");
		l1d.setColor(ChatColor.GRAY);

		final TextComponent l1e = new TextComponent();
		l1e.setText(target.getName());
		l1e.setColor(ChatColor.RED);

		final TextComponent l1f = new TextComponent();
		l1f.setText(" for ");
		l1f.setColor(ChatColor.GRAY);

		final TextComponent l1g = new TextComponent();
		l1g.setText("\"" + reason.toString() + "\" ");
		l1g.setColor(ChatColor.GREEN);

		final TextComponent l1h = new TextComponent();
		l1h.setText("[Teleport]");
		l1h.setColor(ChatColor.BLUE);
		l1h.setBold(Boolean.valueOf(true));
		l1h.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(org.bukkit.ChatColor.GREEN + "Click to teleport you to " + target.getName()).create()));
		l1h.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tp " + target.getName()));

		final TextComponent l1i = new TextComponent();
		l1i.setText("[Inspect]");
		l1i.setColor(ChatColor.GOLD);
		l1i.setBold(Boolean.valueOf(true));
		l1i.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(org.bukkit.ChatColor.GREEN + "Click to inspect " + target.getName()).create()));
		l1i.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/inspect " + target.getName()));

		l1.addExtra(l1a);
		l1.addExtra(l1b);
		l1.addExtra(l1c);
		l1.addExtra(l1d);
		l1.addExtra(l1e);
		l1.addExtra(l1f);
		l1.addExtra(l1g);
		l1.addExtra(l1h);
		l1.addExtra(" ");
		l1.addExtra(l1i);
		for (Player staff : Bukkit.getOnlinePlayers()) {
			if (staff.hasPermission("report.receive")) {
				staff.spigot().sendMessage(l1);
			}
		}
		player.sendMessage(org.bukkit.ChatColor.GREEN + "You have reported " + target.getName() + " for " + reason.toString() + ".");
		cooldown.add(this.getClass().getSimpleName());
		return true;
	}
}
