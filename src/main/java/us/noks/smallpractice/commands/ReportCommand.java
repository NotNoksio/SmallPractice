package us.noks.smallpractice.commands;

import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class ReportCommand implements CommandExecutor {
	
	private int cooldownTime = 30;
	private Map<UUID, Long> cooldowns = new HashMap<UUID, Long>();

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if ((sender instanceof Player)) {
			Player p = (Player) sender;
			
			if (args.length < 2) {
				p.sendMessage(org.bukkit.ChatColor.RED + "Usage: /report <player> <reason>");
				return false;
			}
			Player target = Bukkit.getPlayer(args[0]);
			
			if (target == null) {
				p.sendMessage(org.bukkit.ChatColor.RED + "Player not found!");
				return false;
			}
			if (target == p) {
				p.sendMessage(org.bukkit.ChatColor.RED + "You can't report yourself!");
				return false;
			}
			if (cooldowns.containsKey(((Player) sender).getUniqueId())) {
				long secondsLeft = ((cooldowns.get(((Player) sender).getUniqueId()) / 1000) + cooldownTime) - (System.currentTimeMillis() / 1000);
	            if (secondsLeft > 0) {
	                sender.sendMessage(org.bukkit.ChatColor.RED + "You cant report for another " + secondsLeft + " seconds!");
	                return false;
	            }
			}
			StringJoiner reason = new StringJoiner(" ");
			for (int i = 1; i < args.length; i++) {
				reason.add(args[i]);
			}
				
			TextComponent l1 = new TextComponent();
			l1.setText("(");
			l1.setColor(ChatColor.GRAY);

			TextComponent l1a = new TextComponent();
			l1a.setText("REPORT");
			l1a.setColor(ChatColor.DARK_AQUA);
			l1a.setBold(true);

			TextComponent l1b = new TextComponent();
			l1b.setText(") ");
			l1b.setColor(ChatColor.GRAY);

			TextComponent l1c = new TextComponent();
			l1c.setText(p.getName());
			l1c.setColor(ChatColor.YELLOW);

			TextComponent l1d = new TextComponent();
			l1d.setText(" -> ");
			l1d.setColor(ChatColor.GRAY);

			TextComponent l1e = new TextComponent();
			l1e.setText(target.getName());
			l1e.setColor(ChatColor.RED);

			TextComponent l1f = new TextComponent();
			l1f.setText(" : ");
			l1f.setColor(ChatColor.GRAY);

			TextComponent l1g = new TextComponent();
			l1g.setText("\"");
			l1g.setColor(ChatColor.GREEN);

			TextComponent l1h = new TextComponent();
			l1h.setText(reason.toString());
			l1h.setColor(ChatColor.GREEN);

			TextComponent l1i = new TextComponent();
			l1i.setText("\" ");
			l1i.setColor(ChatColor.GREEN);

			TextComponent l1j = new TextComponent();
			l1j.setText("[TP] ");
			l1j.setColor(ChatColor.BLUE);
			l1j.setBold(Boolean.valueOf(true));
			l1j.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(org.bukkit.ChatColor.GREEN + "Click to teleport you to " + target.getName()).create()));
			l1j.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tp " + target.getName()));

			TextComponent l1k = new TextComponent();
			l1k.setText("[Inspect]");
			l1k.setColor(ChatColor.GOLD);
			l1k.setBold(Boolean.valueOf(true));
			l1k.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(org.bukkit.ChatColor.GREEN + "Click to inspect " + target.getName()).create()));
			l1k.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/verif " + target.getName()));

			l1.addExtra(l1a);
			l1.addExtra(l1b);
			l1.addExtra(l1c);
			l1.addExtra(l1d);
			l1.addExtra(l1e);
			l1.addExtra(l1f);
			l1.addExtra(l1g);
			l1.addExtra(l1h);
			l1.addExtra(l1i);
			l1.addExtra(l1j);
			l1.addExtra(l1k);
			for (Player staff : Bukkit.getOnlinePlayers()) {
				if (staff.hasPermission("report.rec")) {
					staff.spigot().sendMessage(l1);
				}
			}
			cooldowns.put(((Player) sender).getUniqueId(), System.currentTimeMillis());
			p.sendMessage(org.bukkit.ChatColor.GREEN + "You have reported " + target.getName() + " for " + reason.toString() + ".");
		}
		return true;
	}
}
