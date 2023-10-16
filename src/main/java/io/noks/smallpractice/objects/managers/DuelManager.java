package io.noks.smallpractice.objects.managers;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.StringJoiner;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import io.noks.smallpractice.Main;
import io.noks.smallpractice.arena.Arena;
import io.noks.smallpractice.enums.DuelState;
import io.noks.smallpractice.enums.Ladders;
import io.noks.smallpractice.enums.PlayerStatus;
import io.noks.smallpractice.enums.RemoveReason;
import io.noks.smallpractice.objects.duel.Duel;
import io.noks.smallpractice.objects.duel.FFADuel;
import io.noks.smallpractice.objects.duel.SimpleDuel;
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
	private Map<UUID, Duel> uuidIdentifierToDuel;
	private Main main;
	
	public DuelManager(Main main) {
		this.main = main;
		this.uuidIdentifierToDuel = Maps.newHashMap();
	}
	public Duel getDuelFromPlayerUUID(UUID uuid) {
        return this.uuidIdentifierToDuel.get(uuid);
    }
	public Map<UUID, Duel> getUniqueIDIdentifierToDuelMap(){
		return this.uuidIdentifierToDuel;
	}
	
	public Set<Duel> getAllDuels() {
		Set<Duel> list = Sets.newHashSet();
		for (Duel allDuels : uuidIdentifierToDuel.values()) {
			list.add(allDuels);
		}
		return list;
	}
	
	public void startDuel(Arena arena, Ladders ladder, UUID partyLeaderUUID, List<UUID> ffaPlayers) { // FFA
		if (arena == null || ladder == null) {
			final List<UUID> allTeam = Lists.newArrayList(ffaPlayers);
			for (UUID uuids : allTeam) {
				final Player player = this.main.getServer().getPlayer(uuids);
				
				if (allTeam.isEmpty() || player == null) continue;
				
				PlayerManager pm = PlayerManager.get(uuids);
				pm.clearRequest();
				pm.setStatus(PlayerStatus.SPAWN);
				this.main.getItemManager().giveSpawnItem(player);
				player.sendMessage(ChatColor.RED + "MATCH ERROR!!!");
			}
			allTeam.clear();
			return;
		}
		// TODO: IN FFA PLAYERS SEE EVERYONE IN RED WHILE THE PLAYER NEED TO SEE HIMSELF GREEN
		final Scoreboard scoreboard = this.main.getServer().getScoreboardManager().getNewScoreboard();
		final Team red = scoreboard.registerNewTeam("red");
		red.setPrefix(ChatColor.RED.toString());
		final Team green = scoreboard.registerNewTeam("green");
		green.setPrefix(ChatColor.GREEN.toString());
		
		final Party party = this.main.getPartyManager().getParty(partyLeaderUUID);
		
		this.setupTeam(ffaPlayers, null, null, ladder, scoreboard, green, red, false, false, true);
		
        party.setPartyState(PartyState.DUELING);
        this.main.getPartyManager().updatePartyInventory(party);
		this.teleportToArena(new Duel(arena, ladder, new FFADuel(partyLeaderUUID, ffaPlayers)));
	}
	
	public void startDuel(Arena arena, Ladders ladder, UUID player1, UUID player2, boolean ranked) { // DUEL -> SimpleDuel
		if (arena == null || ladder == null) {
			this.main.getServer().getPlayer(player1).sendMessage(ChatColor.RED + "MATCH ERROR!!!");
			this.main.getServer().getPlayer(player2).sendMessage(ChatColor.RED + "MATCH ERROR!!!");
			return;
		}
		// TODO: Check if it does shit
		startDuel(arena, ladder, null, null, Collections.singletonList(player1), Collections.singletonList(player2), ranked);
	}
	
	public void startDuel(Arena arena, Ladders ladder, UUID firstPartyLeaderUUID, UUID secondPartyLeaderUUID, List<UUID> firstTeam, List<UUID> secondTeam, boolean ranked) { // SIMPLEDUEL
		if (arena == null || ladder == null) {
			final List<UUID> allTeam = Lists.newArrayList(firstTeam);
			allTeam.addAll(secondTeam);
			for (UUID uuids : allTeam) {
				final Player player = this.main.getServer().getPlayer(uuids);
				
				if (allTeam.isEmpty()) return;
				if (player == null) continue;
				
				PlayerManager pm = PlayerManager.get(uuids);
				pm.clearRequest();
				pm.setStatus(PlayerStatus.SPAWN);
				this.main.getItemManager().giveSpawnItem(player);
				player.sendMessage(ChatColor.RED + "MATCH ERROR!!!");
			}
			allTeam.clear();
			return;
		}
		final Scoreboard firstPlayerScoreboard = this.main.getServer().getScoreboardManager().getNewScoreboard();
		final Team red1 = firstPlayerScoreboard.registerNewTeam("red");
		red1.setPrefix(ChatColor.RED.toString());
		final Team green1 = firstPlayerScoreboard.registerNewTeam("green");
		green1.setPrefix(ChatColor.GREEN.toString());
		green1.setAllowFriendlyFire(false);
        
		final Scoreboard secondPlayerScoreboard = this.main.getServer().getScoreboardManager().getNewScoreboard();
		final Team red2 = secondPlayerScoreboard.registerNewTeam("red");
		red2.setPrefix(ChatColor.RED.toString());
		final Team green2 = secondPlayerScoreboard.registerNewTeam("green");
		green2.setPrefix(ChatColor.GREEN.toString());
		green2.setAllowFriendlyFire(false);
		
		final boolean teamFight = (firstPartyLeaderUUID != null && secondPartyLeaderUUID != null);
		this.setupTeam(firstTeam, secondPartyLeaderUUID, secondTeam, ladder, firstPlayerScoreboard, green1, red2, teamFight, ranked, false);
		this.setupTeam(secondTeam, firstPartyLeaderUUID, firstTeam, ladder, secondPlayerScoreboard, green2, red1, teamFight, ranked, false);
		
		if (teamFight) {
			List<Party> partyList = Lists.newArrayList(this.main.getPartyManager().getParty(firstPartyLeaderUUID), this.main.getPartyManager().getParty(secondPartyLeaderUUID));
            for (Party parties : partyList) {
            	if (parties == null) continue;
            	parties.setPartyState(PartyState.DUELING);
            	this.main.getPartyManager().updatePartyInventory(parties);
            }
            partyList.clear();
        }
        if (firstTeam.size() == 1 && secondTeam.size() == 1 && (firstPartyLeaderUUID == null && secondPartyLeaderUUID == null)) {
        	this.main.getInventoryManager().updateQueueInventory(ranked);
        }
		this.teleportToArena(new Duel(arena, ladder, new SimpleDuel(firstPartyLeaderUUID, secondPartyLeaderUUID, firstTeam, secondTeam), ranked));
	}
	
	private void setupTeam(List<UUID> team, UUID enemyPartyLeaderUUID, List<UUID> enemyTeam, Ladders ladder, Scoreboard scoreboard, Team team1, Team team2, boolean teamFight, boolean ranked, boolean ffa) {
		if (ladder == Ladders.CLASSIC || ladder == Ladders.ARCHER) {
			final Objective life = scoreboard.registerNewObjective("life", "health");
			life.setDisplaySlot(DisplaySlot.BELOW_NAME);
			life.setDisplayName(ChatColor.RED.toString() + String.valueOf("❤"));
		}
		final String duelMessage = ChatColor.DARK_AQUA + "Starting" + (ffa ? " FFA party game" : " duel against " + ChatColor.YELLOW + (teamFight ? this.main.getServer().getPlayer(enemyPartyLeaderUUID).getName() + "'s party" : this.main.getServer().getPlayer(enemyTeam.get(0)).getName() + (ranked ? ChatColor.GRAY + " (" + (!teamFight ? PlayerManager.get(enemyTeam.get(0)).getEloManager().getFrom(ladder) : (this.main.getPartyManager().getParty(enemyPartyLeaderUUID).getEloManager() != null ? this.main.getPartyManager().getParty(enemyPartyLeaderUUID).getEloManager().getFrom(ladder) : "")) + ")" : "")));
		for (UUID teamUUID : team) {
			final Player player = this.main.getServer().getPlayer(teamUUID);
			
			if (team.isEmpty()) return;
			if (player == null) {
				team.remove(teamUUID);
				continue;
			}
			if (player.hasMetadata("pack")) {
	        	player.removeMetadata("pack", this.main);
	        }
			final PlayerManager pm = PlayerManager.get(teamUUID);
			pm.clearRequest();
			pm.setStatus(PlayerStatus.WAITING);
			
			if (pm.getSettings().isScoreboardToggled()) {
				if (scoreboard.getObjective(DisplaySlot.SIDEBAR) == null) {
					final Objective sidebar = scoreboard.registerNewObjective("sidebar", "dummy");
					sidebar.setDisplaySlot(DisplaySlot.SIDEBAR);
					sidebar.setDisplayName(ChatColor.DARK_AQUA.toString() + ChatColor.BOLD + "Practice");
					if (scoreboard.getTeam("line1") == null) {
						final Team line1 = scoreboard.registerNewTeam("line1");
						line1.setPrefix(ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + "-------");
						line1.addEntry("-----");
						line1.setSuffix("-------");
						sidebar.getScore("-----").setScore(15);
					}
					if (!ffa && scoreboard.getTeam("opp") == null) {
						final Team opp = scoreboard.registerNewTeam("opp");
						opp.setPrefix(ChatColor.RED.toString() + ChatColor.BOLD + "Opp");
						opp.addEntry("onent: " + ChatColor.RESET);
						opp.setSuffix((enemyPartyLeaderUUID != null ? this.main.getServer().getPlayer(enemyPartyLeaderUUID).getName() : this.main.getServer().getPlayer(enemyTeam.get(0)).getName()));
						sidebar.getScore("onent: " + ChatColor.RESET).setScore(14);
					}
					if (scoreboard.getTeam("time") == null) {
						final Team time = scoreboard.registerNewTeam("time");
						time.setPrefix(ChatColor.DARK_AQUA.toString() + ChatColor.BOLD + "Dura");
						time.addEntry("tion: ");
						time.setSuffix(ChatColor.RESET + "Starting...");
						sidebar.getScore("tion: ").setScore(13);
					}
					if (scoreboard.getTeam("line2") == null) {
						final Team line2 = scoreboard.registerNewTeam("line2");
						line2.setPrefix(ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + "-------");
						line2.addEntry(ChatColor.RESET.toString() + ChatColor.GRAY + ChatColor.STRIKETHROUGH + "-----");
						line2.setSuffix("-------");
						sidebar.getScore(ChatColor.RESET.toString() + ChatColor.GRAY + ChatColor.STRIKETHROUGH + "-----").setScore(12);
					}
				}
			} else {
				if (scoreboard.getObjective(DisplaySlot.SIDEBAR) != null) {
					scoreboard.getObjective(DisplaySlot.SIDEBAR).unregister();
				}
			}
			
			player.setGameMode(GameMode.SURVIVAL);
			player.sendMessage(duelMessage);
			team1.addEntry(player.getName());
			team2.addEntry(player.getName());
			player.setScoreboard(scoreboard);
		}
	}
	 
	public void createSplitTeamsDuel(Party party, Ladders ladder) {
		for (UUID membersUUID : party.getMembers()) {
			final Player members = this.main.getServer().getPlayer(membersUUID);
			
			if (members == null) continue;
			final PlayerManager membersManager = PlayerManager.get(membersUUID);
			
			if (membersManager.getStatus() != PlayerStatus.SPAWN) {
				this.main.getServer().getPlayer(party.getLeader()).sendMessage(ChatColor.RED + "A member in your party isn't in the spawn!");
				return;
			}
		}
		final List<UUID> shuffle = Lists.newArrayList(party.getMembersIncludingLeader());
        Collections.shuffle(shuffle);
        final List<UUID> firstTeam = shuffle.subList(0, (int)(shuffle.size() / 2.0));
        final List<UUID> secondTeam = shuffle.subList((int)(shuffle.size() / 2.0), shuffle.size());
        startDuel(this.main.getArenaManager().getRandomArena(ladder), ladder, party.getLeader(), party.getLeader(), firstTeam, secondTeam, false);
	}
	
	private void tranferElo(List<UUID> winners, List<UUID> losers, Ladders ladder, List<UUID> spectators) {
		final UUID winnerUUID = winners.get(0);
		final UUID loserUUID = losers.get(0);
		final PlayerManager wm = PlayerManager.get(winnerUUID);
		final PlayerManager lm = PlayerManager.get(loserUUID);
		int winnersElo = wm.getEloManager().getFrom(ladder);
		int losersElo = lm.getEloManager().getFrom(ladder);
		boolean to2 = false;
		final Party winnerParty = this.main.getPartyManager().getParty(winnerUUID);
		final Party loserParty = this.main.getPartyManager().getParty(loserUUID);
		if (winners.size() == 2 && losers.size() == 2) {
			winnersElo = winnerParty.getEloManager().getFrom(ladder);
			losersElo = loserParty.getEloManager().getFrom(ladder);
			to2 = true;
		}
		// Rinny - K-Factor = 32, Scale Factor = 400 & Exponent Base = 10 = FULL RATING SYSTEM IN 2 LINES
		final double expectedp = 1.0D / (1.0D + Math.pow(10.0D, (winnersElo - losersElo) / 400.0D));
		final int scoreChange = MathUtils.limit((expectedp * 32.0D), 4, 25);
		// Rinny
		if (!to2) {
			wm.getEloManager().addTo(ladder, scoreChange);
			lm.getEloManager().removeFrom(ladder, scoreChange);
			this.main.getDatabaseUtil().savePlayerSingleElo(wm, ladder);
			this.main.getDatabaseUtil().savePlayerSingleElo(lm, ladder);
			this.main.getDatabaseUtil().updateSoloTopLadder(ladder, true);
		} else {
			winnerParty.getEloManager().addTo(ladder, scoreChange);
			loserParty.getEloManager().removeFrom(ladder, scoreChange);
			this.main.getDatabaseUtil().saveDuoElo(winnerParty, ladder);
			this.main.getDatabaseUtil().saveDuoElo(loserParty, ladder);
			this.main.getDatabaseUtil().updateDuoTopLadder(ladder, true);
		}
		final String eloMessage = ChatColor.GOLD + "Elo Changes: " + ChatColor.GREEN + this.main.getServer().getPlayer(winnerUUID).getName() + (to2 ? ", " + this.main.getServer().getPlayer(winners.get(1)).getName() : "") +  " (+" + scoreChange + ") " + ChatColor.RED + this.main.getServer().getPlayer(loserUUID).getName() + (to2 ? ", " + this.main.getServer().getPlayer(losers.get(1)).getName() : "") + " (-" + scoreChange + ")";
		for (UUID winnersUUID : winners) {
			final Player winner = this.main.getServer().getPlayer(winnersUUID);
			if (winner == null) continue;
			winner.sendMessage(eloMessage);
		}
		for (UUID losersUUID : losers) {
			final Player loser = this.main.getServer().getPlayer(losersUUID);
			if (loser == null) continue;
			loser.sendMessage(eloMessage);
		}
		this.main.getInventoryManager().setLeaderboardInventory();
		if (spectators.isEmpty()) {
			return;
		}
		for (UUID specUUID : spectators) {
			final Player spec = this.main.getServer().getPlayer(specUUID);
			if (spec == null) continue;
			spec.sendMessage(eloMessage);
		}
		this.main.getInventoryManager().setLeaderboardInventory();
	}
	
	private void deathMessage(Duel duel, int winningTeamNumber) {
		if (winningTeamNumber == 0) {
			return;
		}
		List<UUID> winnerTeam = null;
		List<UUID> loserTeam = null;
		switch (winningTeamNumber) {
		case 1:
			winnerTeam = duel.getSimpleDuel().getFirstTeam();
			loserTeam = duel.getSimpleDuel().getSecondTeam();
			break;
		case 2:
			winnerTeam = duel.getSimpleDuel().getSecondTeam();
			loserTeam = duel.getSimpleDuel().getFirstTeam();
			break;
		case 3:
			winnerTeam = duel.getFFADuel().getFfaAlivePlayers();
			final List<UUID> losers = Lists.newArrayList(duel.getFFADuel().getFfaPlayers());
			losers.remove(duel.getFFADuel().getFfaAlivePlayers().get(0));
			loserTeam = losers;
		default:
			break;
		}
		final boolean partyFight = (duel.getSimpleDuel() != null && duel.getSimpleDuel().getFirstTeamPartyLeaderUUID() != null && duel.getSimpleDuel().getSecondTeamPartyLeaderUUID() != null || duel.getFFADuel() != null);
		final String winnerMessage = ChatColor.DARK_AQUA + "Winner: " + ChatColor.YELLOW + this.main.getServer().getOfflinePlayer(winnerTeam.get(0)).getName() + (partyFight ? "'s party" : "");
			
		final TextComponent invTxt = new TextComponent("Inventories (Click): ");
		invTxt.setColor(net.md_5.bungee.api.ChatColor.DARK_AQUA);
		    
		final ComponentJoiner joiner = new ComponentJoiner(ChatColor.DARK_AQUA + ", ", ChatColor.DARK_AQUA + ".");    
		
		if (winningTeamNumber != 3) {
			for (UUID wUUID : winnerTeam) {
				final OfflinePlayer winners = this.main.getServer().getOfflinePlayer(wUUID);
				final PlayerManager pm = PlayerManager.get(wUUID);
				if (pm != null && !duel.isRanked()) {
					pm.getEloManager().addUnrankedWinned();
				}
				final TextComponent wtxt = new TextComponent(winners.getName());
			    	
				wtxt.setColor(net.md_5.bungee.api.ChatColor.GREEN);
				wtxt.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(ChatColor.GREEN + "Click to view " + winners.getName() + "'s inventory").create()));
				wtxt.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/inventory " + winners.getUniqueId()));
				    
				joiner.add(wtxt);
			}
			for (UUID lUUID : loserTeam) {
				final OfflinePlayer losers = this.main.getServer().getOfflinePlayer(lUUID);
				final TextComponent ltxt = new TextComponent(losers.getName());
			    	
				ltxt.setColor(net.md_5.bungee.api.ChatColor.RED);
				ltxt.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(ChatColor.RED + "Click to view " + losers.getName() + "'s inventory").create()));
				ltxt.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/inventory " + losers.getUniqueId()));
				    
				joiner.add(ltxt);
			}
		} else {
			final FFADuel ffaDuel = duel.getFFADuel();
			for (UUID uuids : ffaDuel.getFfaPlayers()) {
				final OfflinePlayer player = this.main.getServer().getOfflinePlayer(uuids);
				final TextComponent txt = new TextComponent(player.getName());
				final boolean isWinner = ffaDuel.getFfaAlivePlayers().contains(uuids);
				
				txt.setColor((isWinner ? net.md_5.bungee.api.ChatColor.GREEN : net.md_5.bungee.api.ChatColor.RED));
				txt.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder((isWinner ? ChatColor.GREEN : ChatColor.RED) + "Click to view " + player.getName() + "'s inventory").create()));
				txt.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/inventory " + player.getUniqueId()));
				    
				joiner.add(txt);
			}
		}
		    
		invTxt.addExtra(joiner.toTextComponent());
		    
		final StringJoiner spect = new StringJoiner(ChatColor.DARK_AQUA + ", ");
		if (duel.hasSpectators()) {
			for (UUID specs : duel.getAllSpectators()) {
				final Player spec = this.main.getServer().getPlayer(specs);
				if (spec == null) continue;
				spect.add(ChatColor.YELLOW + spec.getName());
			}
		}
		final String spectatorMessage = ChatColor.DARK_AQUA + "Spectator" + (duel.getAllSpectators().size() > 1 ? "s: " : ": ") + spect.toString() + ChatColor.DARK_AQUA + ".";
		    
		final List<UUID> duelPlayers = Lists.newArrayList(duel.getAllTeams());
		duelPlayers.addAll(duel.getAllSpectators());
		    
		for (UUID dpUUID : duelPlayers) {
			final Player duelPlayer = this.main.getServer().getPlayer(dpUUID);
			if (duelPlayer == null) continue;
			duelPlayer.sendMessage(winnerMessage);
			duelPlayer.spigot().sendMessage(invTxt);
			if (duel.hasSpectators()) duelPlayer.sendMessage(spectatorMessage);
		}
	}
	
	private void teleportToArena(Duel duel) {
		/*if (!duel.isValid()) {
        	duel.sendMessage(ChatColor.RED + "The duel has been cancelled due to an empty team.");
        	finishDuel(duel, true);
        	return;
        }*/
		
		if (duel.getSimpleDuel() != null) {
			for (UUID firstUUID : duel.getSimpleDuel().getFirstTeam()) {
				final Player first = this.main.getServer().getPlayer(firstUUID);
				
				if (first == null) continue;
				
				PlayerManager pmf = PlayerManager.get(firstUUID);
				this.uuidIdentifierToDuel.put(firstUUID, duel);
				
				pmf.heal(duel.getLadder().needFood());
				first.setNoDamageTicks(50);
				
				pmf.hideAllPlayer();
				this.main.getItemManager().giveKitSelectionItems(first, duel.getLadder());
				
				if (duel.getLadder() == Ladders.COMBO) {
					first.setMaximumNoDamageTicks(4);
					first.setVerticalKnockbackReduction(0.175f);
				}
				if (duel.getLadder() == Ladders.BOXING) {
					first.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1));
				}
				
				first.teleport(duel.getArena().getLocations()[0]);
				first.setSneaking(false);
			}
			for (UUID secondUUID : duel.getSimpleDuel().getSecondTeam()) {
				final Player second = this.main.getServer().getPlayer(secondUUID);
				
				if (second == null) continue;
				
				PlayerManager pms = PlayerManager.get(secondUUID);
				this.uuidIdentifierToDuel.put(secondUUID, duel);
				
				pms.heal(duel.getLadder().needFood());
				second.setNoDamageTicks(50);
				
				pms.hideAllPlayer();
				this.main.getItemManager().giveKitSelectionItems(second, duel.getLadder());
				
				if (duel.getLadder() == Ladders.COMBO) {
					second.setMaximumNoDamageTicks(4);
					second.setVerticalKnockbackReduction(0.175f);
				}
				if (duel.getLadder() == Ladders.BOXING) {
					second.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1));
				}
				
				second.teleport(duel.getArena().getLocations()[1]);
				second.setSneaking(false);
			}
		}
		if (duel.getFFADuel() != null) {
			for (UUID uuids : duel.getFFADuel().getFfaPlayers()) {
				final Player player = this.main.getServer().getPlayer(uuids);
				
				if (player == null) continue;
				
				PlayerManager pm = PlayerManager.get(uuids);
				this.uuidIdentifierToDuel.put(uuids, duel);
				
				pm.heal(duel.getLadder().needFood());
				player.setNoDamageTicks(50);
				
				pm.hideAllPlayer();
				this.main.getItemManager().giveKitSelectionItems(player, duel.getLadder());
				
				if (duel.getLadder() == Ladders.BOXING) {
					player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1));
				}
				
				int i = new Random().nextInt(3);
				player.teleport(i < 2 ? duel.getArena().getLocations()[i] : duel.getArena().getMiddle());
				player.setSneaking(false);
			}
		}
		duel.launchCountdownTask();
		final Arena arena = duel.getArena();
		if (duel.getSimpleDuel() != null) {
			if (arena.hasSpectators()) {
				for (UUID firstUUID : duel.getSimpleDuel().getFirstTeamAlive()) {
					final Player first = this.main.getServer().getPlayer(firstUUID);
					for (UUID spectatorsUUID : arena.getAllSpectators()) {
						final Player spectator = this.main.getServer().getPlayer(spectatorsUUID);
						spectator.showPlayer(first);
					}
				}
				for (UUID firstSecondUUID : duel.getSimpleDuel().getSecondTeamAlive()) {
					final Player firstSecond = this.main.getServer().getPlayer(firstSecondUUID);
					for (UUID spectatorsUUID : arena.getAllSpectators()) {
						final Player spectator = this.main.getServer().getPlayer(spectatorsUUID);
						spectator.showPlayer(firstSecond);
					}
				}
			}
			return;
		}
		if (duel.getFFADuel() != null) {
			if (arena.hasSpectators()) {
				for (UUID firstUUID : duel.getFFADuel().getFfaAlivePlayers()) {
					final Player first = this.main.getServer().getPlayer(firstUUID);
					for (UUID spectatorsUUID : arena.getAllSpectators()) {
						final Player spectator = this.main.getServer().getPlayer(spectatorsUUID);
						spectator.showPlayer(first);
					}
				}
			}
		}
	}
	
	// TODO: SETTING > RESPAWN AS A SPECTATOR
	public void removePlayerFromDuel(Player player, RemoveReason reason) {
		final Duel currentDuel = getDuelFromPlayerUUID(player.getUniqueId());
		
		if (currentDuel == null) return;
		this.uuidIdentifierToDuel.remove(player.getUniqueId());
		final PlayerManager pm = PlayerManager.get(player.getUniqueId());
		pm.saveInventory();
		if (player.getMaximumNoDamageTicks() != 20) {
			player.setMaximumNoDamageTicks(20);
		}
		if (player.getVerticalKnockbackReduction() != 0.0f) {
			player.setVerticalKnockbackReduction(0.0f);
		}
		if (player.getLevel() != 0) {
			player.setLevel(0);
		}
		if (player.getExp() != 0) {
			player.setExp(0);
		}
		
		player.setScoreboard(this.main.getServer().getScoreboardManager().getMainScoreboard());
		currentDuel.killPlayer(player.getUniqueId());
		currentDuel.sendMessage(reason == RemoveReason.KILLED ? player.getName() + (player.getKiller() != null ? " has been killed by " + player.getKiller().getName() : " died") : player.getName() + " has disconnected");
		
		pm.setStatus(PlayerStatus.SPAWN);
		int winningTeamNumber = 0;
		if (currentDuel.getSimpleDuel() != null) {
			if (!currentDuel.getSimpleDuel().getFirstTeamAlive().isEmpty() && !currentDuel.getSimpleDuel().getSecondTeamAlive().isEmpty()) {
				return;
			}
			if (currentDuel.getSimpleDuel().getFirstTeamAlive().isEmpty()) {
				winningTeamNumber = 2;
			} else if (currentDuel.getSimpleDuel().getSecondTeamAlive().isEmpty()) {
				winningTeamNumber = 1;
			}
		}
		if (currentDuel.getFFADuel() != null) {
			if (currentDuel.getFFADuel().getFfaAlivePlayers().size() != 1) {
				return;
			}
			winningTeamNumber = 3;
		}
		if (winningTeamNumber == 0) {
			return;
		}
		for (UUID lastPlayersUUID : (winningTeamNumber == 1 ? currentDuel.getSimpleDuel().getFirstTeamAlive() : (winningTeamNumber == 2 ? currentDuel.getSimpleDuel().getSecondTeamAlive() : currentDuel.getFFADuel().getFfaAlivePlayers()))) {
			final Player lastPlayers = this.main.getServer().getPlayer(lastPlayersUUID);
			this.doEndDuelAction(lastPlayers);
    			
			new BukkitRunnable() {
    				
				@Override
				public void run() {
					if (lastPlayers != null) {
						lastPlayers.teleport(lastPlayers.getWorld().getSpawnLocation());
						main.getItemManager().giveSpawnItem(lastPlayers);
					}
					finishDuel(currentDuel, false);
				}
			}.runTaskLater(this.main, 50L);
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
	
	public void endDuel(Duel duel, int winningTeamNumber, boolean forceEnding) {
		if (winningTeamNumber == 0) {
			return;
		}
		duel.updateState(DuelState.ENDED);
		duel.cancelTask();
		if (duel.isRanked() && !forceEnding) {
			final List<UUID> winnersList = (winningTeamNumber == 1 ? duel.getSimpleDuel().getFirstTeam() : duel.getSimpleDuel().getSecondTeam());
			final List<UUID> losersList = (winnersList == duel.getSimpleDuel().getFirstTeam() ? duel.getSimpleDuel().getSecondTeam() : duel.getSimpleDuel().getFirstTeam());
			
			this.tranferElo(winnersList, losersList, duel.getLadder(), duel.getAllSpectators());
		}
		this.deathMessage(duel, winningTeamNumber);
		
		if (!duel.getAllSpectators().isEmpty()) {
			Iterator<UUID> specIt = duel.getAllSpectators().iterator();
			while (specIt.hasNext()) {
				final Player spec = this.main.getServer().getPlayer(specIt.next());
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
				this.main.getItemManager().giveSpawnItem(spec);
				spec.setScoreboard(this.main.getServer().getScoreboardManager().getMainScoreboard());
				specIt.remove();
			}
		}
		if (duel.getSimpleDuel() != null && duel.getSimpleDuel().getFirstTeamPartyLeaderUUID() != null && duel.getSimpleDuel().getSecondTeamPartyLeaderUUID() != null || duel.getFFADuel() != null && duel.getFFADuel().getFfaPartyLeaderUUID() != null) {
			final List<Party> partyList = Lists.newArrayList();
			if (duel.getSimpleDuel() != null) {
				final SimpleDuel sd = duel.getSimpleDuel();
				UUID firstUUID = sd.getFirstTeamPartyLeaderUUID();
				if (!sd.getFirstTeam().isEmpty() && this.main.getServer().getPlayer(firstUUID) == null && firstUUID != sd.getFirstTeam().get(0)) {
					firstUUID = sd.getFirstTeam().get(0);
				}
				if (this.main.getPartyManager().getParty(firstUUID) != null) {
					partyList.add(this.main.getPartyManager().getParty(firstUUID));
				}
				UUID secondUUID = sd.getSecondTeamPartyLeaderUUID();
				if (!sd.getSecondTeam().isEmpty() && this.main.getServer().getPlayer(secondUUID) == null && secondUUID != sd.getSecondTeam().get(0)) {
					secondUUID = sd.getSecondTeam().get(0);
				}
				if (this.main.getPartyManager().getParty(secondUUID) != null) {
					partyList.add(this.main.getPartyManager().getParty(secondUUID));
				}
			} else if (duel.getFFADuel() != null) {
				final FFADuel ffad = duel.getFFADuel();
				UUID uuid = ffad.getFfaPartyLeaderUUID();
				if (ffad.getFfaPlayers().size() > 1 && this.main.getServer().getPlayer(uuid) == null) {
					uuid = ffad.getFfaPlayers().get(0);
				}
				if (this.main.getPartyManager().getParty(uuid) != null) {
					partyList.add(this.main.getPartyManager().getParty(uuid));
				}
			}
			if (!partyList.isEmpty()) {
				for (Party parties : partyList) {
	            	if (parties == null) continue;
	            	parties.setPartyState(PartyState.LOBBY);
	            	this.main.getPartyManager().updatePartyInventory(parties);
	            }
	            partyList.clear();
			}
        }
        if (duel.getSimpleDuel() != null && duel.getSimpleDuel().getFirstTeam().size() == 1 && duel.getSimpleDuel().getSecondTeam().size() == 1 && (duel.getSimpleDuel().getFirstTeamPartyLeaderUUID() == null && duel.getSimpleDuel().getSecondTeamPartyLeaderUUID() == null)) {
        	this.main.getInventoryManager().updateQueueInventory(duel.isRanked());
        }
	}
	
	public void finishDuel(Duel duel, boolean cancelled) {
		if (cancelled) {
			duel.cancelTask();
		}
		duel.clearDrops();
		duel.replaceBlockFromStorage();
		for (UUID dpUUID : duel.getAllTeams()) {
			final Player duelPlayer = this.main.getServer().getPlayer(dpUUID);
			if (duelPlayer == null) continue;
			final PlayerManager dpm = PlayerManager.get(duelPlayer.getUniqueId());
			
			duelPlayer.setScoreboard(this.main.getServer().getScoreboardManager().getMainScoreboard());
			if (duelPlayer.getMaximumNoDamageTicks() != 20) {
	        	duelPlayer.setMaximumNoDamageTicks(20);
	        }
			
			dpm.setStatus(PlayerStatus.SPAWN);
			dpm.heal(false);
			dpm.showAllPlayer();
			if (cancelled || (duel.getLadder() == Ladders.BOXING || duel.getLadder() == Ladders.SUMO) || duelPlayer.getInventory().getContents() == null) {
				duelPlayer.teleport(duelPlayer.getWorld().getSpawnLocation());
				this.main.getItemManager().giveSpawnItem(duelPlayer);
			}
			dpm.getMatchStats().resetDuelStats();
			dpm.getMatchStats().removeEnderPearlCooldown();
			this.uuidIdentifierToDuel.remove(duelPlayer.getUniqueId());
			this.main.getInventoryManager().updateQueueInventory(duel.isRanked());
		}
	}
	
	private void doEndDuelAction(Player player) {
		final PlayerManager pm = PlayerManager.get(player.getUniqueId());
		pm.saveInventory();
        pm.heal(false);
        pm.setStatus(PlayerStatus.SPAWN);
        if (player.getMaximumNoDamageTicks() != 20) {
        	player.setMaximumNoDamageTicks(20);
        }
        if (player.getVerticalKnockbackReduction() != 0.0f) {
			player.setVerticalKnockbackReduction(0.0f);
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
        if (player.getItemOnCursor() != null) {
        	player.setItemOnCursor(null);
        }
	}
}
