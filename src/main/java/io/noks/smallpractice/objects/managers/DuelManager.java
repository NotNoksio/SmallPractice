package io.noks.smallpractice.objects.managers;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import io.noks.smallpractice.Main;
import io.noks.smallpractice.arena.Arena;
import io.noks.smallpractice.arena.Arena.Arenas;
import io.noks.smallpractice.enums.Ladders;
import io.noks.smallpractice.enums.PlayerStatus;
import io.noks.smallpractice.enums.RemoveReason;
import io.noks.smallpractice.objects.Duel;
import io.noks.smallpractice.party.Party;
import io.noks.smallpractice.party.PartyState;
import io.noks.smallpractice.utils.ComponentJoiner;
import io.noks.smallpractice.utils.MathUtils;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.minecraft.util.com.google.common.collect.Sets;

public class DuelManager {
	private Map<UUID, Duel> uuidIdentifierToDuel = Maps.newHashMap();
	public Duel getDuelFromPlayerUUID(UUID uuid) {
        return this.uuidIdentifierToDuel.get(uuid);
    }
	
	public Set<Duel> getAllDuels() {
		Set<Duel> list = Sets.newHashSet();
		for (Duel allDuels : uuidIdentifierToDuel.values()) {
			list.add(allDuels);
		}
		return list;
	}
	
	public void startDuel(Arenas arena, Ladders ladder, UUID partyLeaderUUID, List<UUID> ffaPlayers) {
		
	}
	
	public void startDuel(Arenas arena, Ladders ladder, UUID player1, UUID player2, boolean ranked) {
		List<UUID> firstTeam = Lists.newArrayList();
		firstTeam.add(player1);
		List<UUID> secondTeam = Lists.newArrayList();
		secondTeam.add(player2);
		startDuel(arena, ladder, null, null, firstTeam, secondTeam, ranked);
	}
	
	public void startDuel(Arenas arena, Ladders ladder, UUID firstPartyLeaderUUID, UUID secondPartyLeaderUUID, List<UUID> firstTeam, List<UUID> secondTeam, boolean ranked) {
		if (arena == null) {
			List<UUID> allTeam = Lists.newArrayList(firstTeam);
			allTeam.addAll(secondTeam);
			for (UUID uuids : allTeam) {
				Player player = Bukkit.getPlayer(uuids);
				
				if (allTeam.isEmpty()) continue;
				
				PlayerManager pm = PlayerManager.get(uuids);
				pm.clearRequest();
				pm.setStatus(PlayerStatus.SPAWN);
				Main.getInstance().getItemManager().giveSpawnItem(player);
				player.sendMessage(ChatColor.RED + "No arena created in this gamemode!");
			}
			allTeam.clear();
			return;
		}
		final Scoreboard firstPlayerScoreboard = Bukkit.getServer().getScoreboardManager().getNewScoreboard();
		final Team red1 = firstPlayerScoreboard.registerNewTeam("red");
		red1.setPrefix(ChatColor.RED.toString());
		final Team green1 = firstPlayerScoreboard.registerNewTeam("green");
		green1.setPrefix(ChatColor.GREEN.toString());
		green1.setAllowFriendlyFire(false);
        
		final Scoreboard secondPlayerScoreboard = Bukkit.getServer().getScoreboardManager().getNewScoreboard();
		final Team red2 = secondPlayerScoreboard.registerNewTeam("red");
		red2.setPrefix(ChatColor.RED.toString());
		final Team green2 = secondPlayerScoreboard.registerNewTeam("green");
		green2.setPrefix(ChatColor.GREEN.toString());
		green2.setAllowFriendlyFire(false);
		
		final boolean teamFight = (firstPartyLeaderUUID != null && secondPartyLeaderUUID != null);
		this.setupTeam(firstTeam, secondPartyLeaderUUID, secondTeam, ladder, firstPlayerScoreboard, green1, red2, teamFight, ranked);
		this.setupTeam(secondTeam, firstPartyLeaderUUID, firstTeam, ladder, secondPlayerScoreboard, green2, red1, teamFight, ranked);
		
		if (teamFight) {
			List<Party> partyList = Lists.newArrayList(Main.getInstance().getPartyManager().getParty(firstPartyLeaderUUID), Main.getInstance().getPartyManager().getParty(secondPartyLeaderUUID));
            for (Party parties : partyList) {
            	if (parties == null) continue;
            	parties.setPartyState(PartyState.DUELING);
            	Main.getInstance().getPartyManager().updatePartyInventory(parties);
            }
            partyList.clear();
        }
        if (firstTeam.size() == 1 && secondTeam.size() == 1 && (firstPartyLeaderUUID == null && secondPartyLeaderUUID == null)) {
        	Main.getInstance().getInventoryManager().updateQueueInventory(ranked);
        }
		teleportRandomArena(new Duel(arena, ladder, firstPartyLeaderUUID, secondPartyLeaderUUID, firstTeam, secondTeam, ranked));
	}
	
	private void setupTeam(List<UUID> team, UUID enemyPartyLeaderUUID, List<UUID> enemyTeam, Ladders ladder, Scoreboard scoreboard, Team team1, Team team2, boolean teamFight, boolean ranked) {
		final String duelMessage = ChatColor.DARK_AQUA + "Starting duel against " + ChatColor.YELLOW + (teamFight ? Bukkit.getPlayer(enemyPartyLeaderUUID).getName() + "'s party" : Bukkit.getPlayer(enemyTeam.get(0)).getName() + (ranked ? ChatColor.GRAY + " (" + (!teamFight ? PlayerManager.get(enemyTeam.get(0)).getEloManager().getElo(ladder) : Main.getInstance().getPartyManager().getParty(enemyPartyLeaderUUID).getPartyEloManager().getElo(ladder)) + ")" : ""));
		for (UUID teamUUID : team) {
			Player player = Bukkit.getPlayer(teamUUID);
			
			if (player == null) {
				team.remove(teamUUID);
				continue;
			}
			if (team.isEmpty()) continue;
			
			final PlayerManager pm = PlayerManager.get(teamUUID);
			pm.clearRequest();
			pm.setStatus(PlayerStatus.WAITING);
			
			player.setGameMode(GameMode.SURVIVAL);
			player.sendMessage(duelMessage);
			if (ladder == Ladders.COMBO) {
				player.setMaximumNoDamageTicks(2);
				player.setKnockbackReduction(0.3f);
			}
			team1.addEntry(player.getName());
			team2.addEntry(player.getName());
			player.setScoreboard(scoreboard);
		}
	}
	 
	public void createSplitTeamsDuel(Party party, Ladders ladder) {
		for (UUID membersUUID : party.getMembers()) {
			Player members = Bukkit.getPlayer(membersUUID);
			
			if (members == null) continue;
			final PlayerManager membersManager = PlayerManager.get(membersUUID);
			
			if (membersManager.getStatus() != PlayerStatus.SPAWN) {
				Bukkit.getPlayer(party.getLeader()).sendMessage(ChatColor.RED + "A member in your party isn't in the spawn!");
				return;
			}
		}
		List<UUID> shuffle = Lists.newArrayList(party.getAllMembersOnline());
        Collections.shuffle(shuffle);
        final List<UUID> firstTeam = shuffle.subList(0, (int)(shuffle.size() / 2.0));
        final List<UUID> secondTeam = shuffle.subList((int)(shuffle.size() / 2.0), shuffle.size());
        startDuel(Arena.getInstance().getRandomArena(ladder == Ladders.SUMO), ladder, party.getLeader(), party.getLeader(), firstTeam, secondTeam, false);
	}
	
	// TODO: FFA END
	public void endDuel(Duel duel, int winningTeamNumber, boolean forceEnding) {
		if (winningTeamNumber == 0) {
			return;
		}
		this.deathMessage(duel, winningTeamNumber);
		
		if (duel.isRanked() && !forceEnding) {
			List<UUID> winnersList = (winningTeamNumber == 1 ? duel.getFirstTeam() : duel.getSecondTeam());
			List<UUID> losersList = (winnersList == duel.getFirstTeam() ? duel.getSecondTeam() : duel.getFirstTeam());
			
			this.tranferElo(winnersList, losersList, duel.getLadder());
		}
		
		if (!duel.getAllSpectators().isEmpty()) {
			Iterator<UUID> specIt = duel.getAllSpectators().iterator();
			while (specIt.hasNext()) {
				Player spec = Bukkit.getPlayer(specIt.next());
				if (spec == null) continue;
				final PlayerManager sm = PlayerManager.get(spec.getUniqueId());
				
				spec.setFlySpeed(0.1f);
				spec.setWalkSpeed(0.2f);
				spec.setAllowFlight(false);
				spec.setFlying(false);
				sm.setStatus(PlayerStatus.SPAWN);
				sm.showAllPlayer();
				sm.setSpectate(null);
				spec.teleport(spec.getWorld().getSpawnLocation());
				Main.getInstance().getItemManager().giveSpawnItem(spec);
				spec.setScoreboard(Main.getInstance().getServer().getScoreboardManager().getNewScoreboard());
				specIt.remove();
			}
		}
		if (duel.getFirstTeamPartyLeaderUUID() != null && duel.getSecondTeamPartyLeaderUUID() != null) {
			List<Party> partyList = Lists.newArrayList(Main.getInstance().getPartyManager().getParty(duel.getFirstTeamPartyLeaderUUID()), Main.getInstance().getPartyManager().getParty(duel.getSecondTeamPartyLeaderUUID()));
            for (Party parties : partyList) {
            	if (parties == null) continue;
            	parties.setPartyState(PartyState.LOBBY);
            	Main.getInstance().getPartyManager().updatePartyInventory(parties);
            }
            partyList.clear();
        }
        if (duel.getFirstTeam().size() == 1 && duel.getSecondTeam().size() == 1 && (duel.getFirstTeamPartyLeaderUUID() == null && duel.getSecondTeamPartyLeaderUUID() == null)) {
        	Main.getInstance().getInventoryManager().updateQueueInventory(duel.isRanked());
        }
	}
	
	private void tranferElo(List<UUID> winners, List<UUID> losers, Ladders ladder) {
		final UUID winnerUUID = winners.get(0);
		final UUID loserUUID = losers.get(0);
		int winnersElo = PlayerManager.get(winnerUUID).getEloManager().getElo(ladder);
		int losersElo = PlayerManager.get(loserUUID).getEloManager().getElo(ladder);
		boolean to2 = false;
		if (winners.size() == 2 && losers.size() == 2) {
			winnersElo = Main.getInstance().getPartyManager().getParty(winnerUUID).getPartyEloManager().getElo(ladder);
			losersElo = Main.getInstance().getPartyManager().getParty(loserUUID).getPartyEloManager().getElo(ladder);
			to2 = true;
		}
		final double expectedp = 1.0D / (1.0D + Math.pow(10.0D, (winnersElo - losersElo) / 400.0D));
		final int scoreChange = MathUtils.limit((expectedp * 32.0D), 4, 40);
		final String eloMessage = ChatColor.GOLD + "Elo Changes: " + ChatColor.GREEN + Bukkit.getPlayer(winnerUUID).getName() + (to2 ? ", " + Bukkit.getPlayer(winners.get(1)).getName() : "") +  " (+" + scoreChange + ") " + ChatColor.RED + Bukkit.getPlayer(loserUUID).getName() + (to2 ? ", " + Bukkit.getPlayer(losers.get(1)).getName() : "") + " (-" + scoreChange + ")";
		if (!to2) {
			PlayerManager.get(winnerUUID).getEloManager().addElo(ladder, scoreChange);
			PlayerManager.get(loserUUID).getEloManager().removeElo(ladder, scoreChange);
		} else {
			Main.getInstance().getPartyManager().getParty(winnerUUID).getPartyEloManager().addElo(ladder, scoreChange);
			Main.getInstance().getPartyManager().getParty(loserUUID).getPartyEloManager().removeElo(ladder, scoreChange);
		}
		for (UUID winnersUUID : winners) {
			Player winner = Bukkit.getPlayer(winnersUUID);
			winner.sendMessage(eloMessage);
		}
		for (UUID losersUUID : losers) {
			Player loser = Bukkit.getPlayer(losersUUID);
			loser.sendMessage(eloMessage);
		}
	}
	
	// TODO: Sometimes there's an NULLPOINTEREXCEPTION appear
	private void deathMessage(Duel duel, int winningTeamNumber) {
		if (winningTeamNumber == 0) {
			return;
		}
		List<UUID> winnerTeam = null;
		List<UUID> loserTeam = null;
		switch (winningTeamNumber) {
		case 1:
			winnerTeam = duel.getFirstTeam();
			loserTeam = duel.getSecondTeam();
			break;
		case 2:
			winnerTeam = duel.getSecondTeam();
			loserTeam = duel.getFirstTeam();
			break;
		default:
			break;
		}
		final boolean partyFight = (duel.getFirstTeamPartyLeaderUUID() != null && duel.getSecondTeamPartyLeaderUUID() != null);
		final String winnerMessage = ChatColor.DARK_AQUA + "Winner: " + ChatColor.YELLOW + Bukkit.getPlayer(winnerTeam.get(0)).getName() + (partyFight ? "'s party" : "");
			
		TextComponent invTxt = new TextComponent("Inventories (Click): ");
		invTxt.setColor(net.md_5.bungee.api.ChatColor.DARK_AQUA);
		    
		ComponentJoiner joiner = new ComponentJoiner(ChatColor.DARK_AQUA + ", ", ChatColor.DARK_AQUA + ".");    
		
		if (winningTeamNumber != 3) {
			for (UUID wUUID : winnerTeam) {
				final OfflinePlayer winners = Bukkit.getOfflinePlayer(wUUID);
				TextComponent wtxt = new TextComponent(winners.getName());
			    	
				wtxt.setColor(net.md_5.bungee.api.ChatColor.GREEN);
				wtxt.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(ChatColor.GREEN + "Click to view " + winners.getName() + "'s inventory").create()));
				wtxt.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/inventory " + winners.getUniqueId()));
				    
				joiner.add(wtxt);
			}
			for (UUID lUUID : loserTeam) {
				final OfflinePlayer losers = Bukkit.getOfflinePlayer(lUUID);
				TextComponent ltxt = new TextComponent(losers.getName());
			    	
				ltxt.setColor(net.md_5.bungee.api.ChatColor.RED);
				ltxt.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(ChatColor.RED + "Click to view " + losers.getName() + "'s inventory").create()));
				ltxt.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/inventory " + losers.getUniqueId()));
				    
				joiner.add(ltxt);
			}
		} /*else {
			for (UUID uuids : duel.getFfaPlayers()) {
				final OfflinePlayer player = Bukkit.getOfflinePlayer(uuids);
				TextComponent txt = new TextComponent(player.getName());
				boolean isWinner = duel.getFfaAlivePlayers().contains(uuids);
				
				txt.setColor((isWinner ? net.md_5.bungee.api.ChatColor.GREEN : net.md_5.bungee.api.ChatColor.RED));
				txt.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder((isWinner ? ChatColor.GREEN : ChatColor.RED) + "Click to view " + player.getName() + "'s inventory").create()));
				txt.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/inventory " + player.getUniqueId()));
				    
				joiner.add(txt);
			}
		}*/
		    
		invTxt.addExtra(joiner.toTextComponent());
		    
		StringJoiner spect = new StringJoiner(ChatColor.DARK_AQUA + ", ");
		if (duel.hasSpectator()) {
			for (UUID specs : duel.getAllSpectators()) {
				final Player spec = Bukkit.getPlayer(specs);
				spect.add(ChatColor.YELLOW + spec.getName());
			}
		}
		final String spectatorMessage = ChatColor.DARK_AQUA + "Spectator" + (duel.getAllSpectators().size() > 1 ? "s: " : ": ") + spect.toString() + ChatColor.DARK_AQUA + ".";
		    
		List<UUID> duelPlayers = Lists.newArrayList(duel.getAllTeams());
		duelPlayers.addAll(duel.getAllSpectators());
		    
		for (UUID dpUUID : duelPlayers) {
			final Player duelPlayer = Bukkit.getPlayer(dpUUID);
			if (duelPlayer == null) continue;
			duelPlayer.sendMessage(winnerMessage);
			duelPlayer.spigot().sendMessage(invTxt);
			if (duel.hasSpectator()) duelPlayer.sendMessage(spectatorMessage);
		}
	}
	
	private void sendWaitingMessage(Duel duel) {
		new BukkitRunnable() {
			int num = duel.getTimeBeforeDuel();
			
			@Override
			public void run() {
				if (!duel.isValid()) {
					duel.sendMessage(ChatColor.RED + "The current duel has been cancelled due to his invalidity.");
					finishDuel(duel, true);
					this.cancel();
				}
				if (num <= 0) {
					duel.sendSoundedMessage(ChatColor.GREEN + "Duel has started!", Sound.FIREWORK_BLAST);
					duel.showDuelPlayer();
					duel.setDuelPlayersStatusTo(PlayerStatus.DUEL);
					this.cancel();
				}
				if (num > 0) {
					duel.sendSoundedMessage(ChatColor.DARK_AQUA + "Duel start in " + ChatColor.YELLOW + num + ChatColor.DARK_AQUA + " second" + (num > 1 ? "s.." : ".."), Sound.NOTE_PLING);
					num--;
				}
			}
		}.runTaskTimer(Main.getInstance(), 10L, 20L);
	}
	
	private void teleportRandomArena(Duel duel) {
		if (duel.getFirstTeam().isEmpty() || duel.getSecondTeam().isEmpty()) {
        	duel.sendMessage(ChatColor.RED + "The duel has been cancelled due to an empty team.");
        	finishDuel(duel, true);
        	return;
        }
		
		final boolean healForFight = (duel.getLadder() != Ladders.SOUP && duel.getLadder() != Ladders.SUMO && duel.getLadder() != Ladders.BOXING && duel.getLadder() != Ladders.SPLEEF);
		for (UUID firstUUID : duel.getFirstTeam()) {
			Player first = Bukkit.getPlayer(firstUUID);
			
			if (first == null) continue;
			
			PlayerManager pmf = PlayerManager.get(firstUUID);
			this.uuidIdentifierToDuel.put(firstUUID, duel);
			
			pmf.heal(healForFight);
			first.setNoDamageTicks(50);
			
			pmf.hideAllPlayer();
			Main.getInstance().getItemManager().giveKitSelectionItems(first, duel.getLadder());
			
			if (duel.getLadder() == Ladders.BOXING) {
				first.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1));
			}
			
			first.teleport(duel.getArena().getLocations()[0]);
			first.setSneaking(false);
		}
		for (UUID secondUUID : duel.getSecondTeam()) {
			Player second = Bukkit.getPlayer(secondUUID);
			
			if (second == null) continue;
			
			PlayerManager pms = PlayerManager.get(secondUUID);
			this.uuidIdentifierToDuel.put(secondUUID, duel);
			
			pms.heal(healForFight);
			second.setNoDamageTicks(50);
			
			pms.hideAllPlayer();
			Main.getInstance().getItemManager().giveKitSelectionItems(second, duel.getLadder());
			
			if (duel.getLadder() == Ladders.BOXING) {
				second.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1));
			}
			
			second.teleport(duel.getArena().getLocations()[1]);
			second.setSneaking(false);
		}
		Arenas arena = duel.getArena();
		// TODO: CHANGE THAT! THAT'S ABSOLUTE TRASH - start
		for (UUID firstUUID : duel.getFirstTeamAlive()) {
			if (arena.hasSpectators()) {
				for (UUID spectatorsUUID : arena.getAllSpectators()) {
					Player spectator = Bukkit.getPlayer(spectatorsUUID);
					Player first = Bukkit.getPlayer(firstUUID);
					spectator.showPlayer(first);
				}
			}
			for (UUID secondFirstUUID : duel.getFirstTeamAlive()) {
				Player first = Bukkit.getPlayer(firstUUID);
				Player secondFirst = Bukkit.getPlayer(secondFirstUUID);
				first.showPlayer(secondFirst);
				secondFirst.showPlayer(first);
			}
		}
		for (UUID firstSecondUUID : duel.getSecondTeamAlive()) {
			if (arena.hasSpectators()) {
				for (UUID spectatorsUUID : arena.getAllSpectators()) {
					Player spectator = Bukkit.getPlayer(spectatorsUUID);
					Player second = Bukkit.getPlayer(firstSecondUUID);
					spectator.showPlayer(second);
				}
			}
			for (UUID secondUUID : duel.getSecondTeamAlive()) {
				Player firstSecond = Bukkit.getPlayer(firstSecondUUID);
				Player second = Bukkit.getPlayer(secondUUID);
				firstSecond.showPlayer(second);
				second.showPlayer(firstSecond);
			}
		}
		// TODO: CHANGE THAT! THAT'S ABSOLUTE TRASH - end
		sendWaitingMessage(duel);
	}
	
	// TODO: SETTING > RESPAWN AS A SPECTATOR
	public void removePlayerFromDuel(Player player, RemoveReason reason) {
		final Duel currentDuel = getDuelFromPlayerUUID(player.getUniqueId());
		
		if (currentDuel == null) return;
		this.uuidIdentifierToDuel.remove(player.getUniqueId());
		final PlayerManager pm = PlayerManager.get(player.getUniqueId());
		pm.saveInventory();
		if (player.getMaximumNoDamageTicks() != 10) {
			player.setMaximumNoDamageTicks(10);
		}
		if (player.getKnockbackReduction() != 0.0f) {
			player.setKnockbackReduction(0.0f);
		}
		if (player.getLevel() != 0) {
			player.setLevel(0);
		}
		if (player.getExp() != 0) {
			player.setExp(0);
		}
		
		currentDuel.killPlayer(player.getUniqueId());
		final String message = (reason == RemoveReason.KILLED ? player.getName() + (player.getKiller() != null ? " has been killed by " + player.getKiller().getName() : " died") : player.getName() + " has disconnected");
		currentDuel.sendMessage(message);
		
		pm.setStatus(PlayerStatus.SPAWN);
		
		if (!currentDuel.getFirstTeamAlive().isEmpty() && !currentDuel.getSecondTeamAlive().isEmpty()) {
			return;
		}
		int winningTeamNumber = 0;
		if (currentDuel.getFirstTeamAlive().isEmpty()) {
			winningTeamNumber = 2;
		} else if (currentDuel.getSecondTeamAlive().isEmpty()) {
			winningTeamNumber = 1;
		}
		if (winningTeamNumber == 0) {
			return;
		}
		for (UUID lastPlayersUUID : (winningTeamNumber == 1 ? currentDuel.getFirstTeamAlive() : currentDuel.getSecondTeamAlive())) {
			Player lastPlayers = Bukkit.getPlayer(lastPlayersUUID);
			this.doEndDuelAction(lastPlayers);
    			
			new BukkitRunnable() {
    				
				@Override
				public void run() {
					if (lastPlayers != null) {
						lastPlayers.teleport(lastPlayers.getWorld().getSpawnLocation());
						Main.getInstance().getItemManager().giveSpawnItem(lastPlayers);
					}
					finishDuel(currentDuel, false);
				}
			}.runTaskLater(Main.getInstance(), 50L);
		}
		endDuel(currentDuel, winningTeamNumber, false);
	}
	
	public int getFightFromLadder(Ladders ladder, boolean ranked) {
		int count = 0;
		for (Duel duel : this.uuidIdentifierToDuel.values()) {
			if (duel.getLadder() == ladder && duel.isRanked() == ranked) {
				count++;
			}
		} 
		return count;
	}
	
	public void finishDuel(Duel duel, boolean cancelled) {
		duel.clearDrops();
		for (UUID dpUUID : duel.getAllTeams()) {
			Player duelPlayer = Bukkit.getPlayer(dpUUID);
			if (duelPlayer == null) continue;
			final PlayerManager dpm = PlayerManager.get(duelPlayer.getUniqueId());
			
			duelPlayer.setScoreboard(Bukkit.getServer().getScoreboardManager().getNewScoreboard());
			if (duelPlayer.getMaximumNoDamageTicks() != 10) {
	        	duelPlayer.setMaximumNoDamageTicks(10);
	        }
			
			dpm.setStatus(PlayerStatus.SPAWN);
			dpm.heal(false);
			dpm.showAllPlayer();
			if (cancelled || (duel.getLadder() == Ladders.BOXING || duel.getLadder() == Ladders.SUMO) || duelPlayer.getInventory().getContents() == null) {
				duelPlayer.teleport(duelPlayer.getWorld().getSpawnLocation());
				Main.getInstance().getItemManager().giveSpawnItem(duelPlayer);
			}
			dpm.getMatchStats().resetDuelStats();
			
			dpm.getMatchStats().removeEnderPearlCooldown();
			this.uuidIdentifierToDuel.remove(duelPlayer.getUniqueId());
		}
		Main.getInstance().getInventoryManager().updateQueueInventory(duel.isRanked());
	}
	
	private void doEndDuelAction(Player player) {
		final PlayerManager pm = PlayerManager.get(player.getUniqueId());
		pm.saveInventory();
        pm.heal(false);
        pm.setStatus(PlayerStatus.SPAWN);
        if (player.getMaximumNoDamageTicks() != 10) {
        	player.setMaximumNoDamageTicks(10);
        }
        if (player.getKnockbackReduction() != 0.0f) {
			player.setKnockbackReduction(0.0f);
		}
        player.getInventory().clear();
        player.getInventory().setArmorContents(null);
        if (!player.getActivePotionEffects().isEmpty()) {
			for (PotionEffect effect : player.getActivePotionEffects()) {
				player.removePotionEffect(effect.getType());
			}
		}
        if (player.getLevel() != 0) {
        	player.setLevel(0);
        }
        if (player.getExp() != 0) {
			player.setExp(0);
		}
        player.extinguish();
        player.setItemOnCursor(null);
	}
}
