package us.noks.smallpractice.objects.managers;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import us.noks.smallpractice.Main;
import us.noks.smallpractice.arena.Arena;
import us.noks.smallpractice.arena.Arena.Arenas;
import us.noks.smallpractice.enums.Ladders;
import us.noks.smallpractice.enums.PlayerStatus;
import us.noks.smallpractice.enums.RemoveReason;
import us.noks.smallpractice.objects.Duel;
import us.noks.smallpractice.party.Party;
import us.noks.smallpractice.party.PartyState;
import us.noks.smallpractice.utils.ComponentJoiner;
import us.noks.smallpractice.utils.MathUtils;

public class DuelManager {
	private Map<UUID, Duel> uuidIdentifierToDuel = Maps.newHashMap();
	public Duel getDuelFromPlayerUUID(UUID uuid) {
        return this.uuidIdentifierToDuel.get(uuid);
    }
	
	public List<Duel> getAllDuels() {
		List<Duel> list = Lists.newArrayList();
		for (Duel allDuels : uuidIdentifierToDuel.values()) {
			list.add(allDuels);
		}
		return list;
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
			for (UUID firstUUID : firstTeam) {
				Player first = Bukkit.getPlayer(firstUUID);
				
				if (first == null) {
					firstTeam.remove(firstUUID);
					continue;
				}
				if (firstTeam.isEmpty()) continue;
				
				PlayerManager fm = PlayerManager.get(firstUUID);
				fm.clearRequest();
				fm.setStatus(PlayerStatus.SPAWN);
				Main.getInstance().getItemManager().giveSpawnItem(first);
				first.sendMessage(ChatColor.RED + "No arena created in this gamemode!");
			}
			for (UUID secondUUID : secondTeam) {
				Player second = Bukkit.getPlayer(secondUUID);
				
				if (second == null) {
					secondTeam.remove(secondUUID);
					continue;
				}
				if (secondTeam.isEmpty()) continue;
				
				PlayerManager sm = PlayerManager.get(secondUUID);
				sm.setStatus(PlayerStatus.SPAWN);
				Main.getInstance().getItemManager().giveSpawnItem(second);
				second.sendMessage(ChatColor.RED + "No arena created in this gamemode!");
			}
			return;
		}
		Scoreboard firstPlayerScoreboard = Bukkit.getServer().getScoreboardManager().getNewScoreboard();
		Team red1 = firstPlayerScoreboard.registerNewTeam("red");
		red1.setPrefix(ChatColor.RED.toString());
		Team green1 = firstPlayerScoreboard.registerNewTeam("green");
		green1.setPrefix(ChatColor.GREEN.toString());
		green1.setAllowFriendlyFire(false);
        
		Scoreboard secondPlayerScoreboard = Bukkit.getServer().getScoreboardManager().getNewScoreboard();
		Team red2 = secondPlayerScoreboard.registerNewTeam("red");
		red2.setPrefix(ChatColor.RED.toString());
		Team green2 = secondPlayerScoreboard.registerNewTeam("green");
		green2.setPrefix(ChatColor.GREEN.toString());
		green2.setAllowFriendlyFire(false);
		
		final boolean teamFight = (firstPartyLeaderUUID != null && secondPartyLeaderUUID != null);
		for (UUID firstUUID : firstTeam) {
			Player first = Bukkit.getPlayer(firstUUID);
			
			if (first == null) {
				firstTeam.remove(firstUUID);
				continue;
			}
			if (firstTeam.isEmpty()) continue;
			
			PlayerManager fm = PlayerManager.get(firstUUID);
			fm.clearRequest();
			fm.setStatus(PlayerStatus.WAITING);
			
			first.setGameMode(GameMode.SURVIVAL);
			first.sendMessage(ChatColor.DARK_AQUA + "Starting duel against " + ChatColor.YELLOW + (teamFight ? Bukkit.getPlayer(secondPartyLeaderUUID).getName() + "'s party" : Bukkit.getPlayer(secondTeam.get(0)).getName()));
			fm.heal(true);
			
			green1.addEntry(first.getName());
			red2.addEntry(first.getName());
			first.setScoreboard(firstPlayerScoreboard);
		}
		for (UUID secondUUID : secondTeam) {
			Player second = Bukkit.getPlayer(secondUUID);
			
			if (second == null) {
				secondTeam.remove(secondUUID);
				continue;
			}
			if (secondTeam.isEmpty()) continue;
			
			PlayerManager sm = PlayerManager.get(secondUUID);
			sm.clearRequest();
			sm.setStatus(PlayerStatus.WAITING);
			
			second.setGameMode(GameMode.SURVIVAL);
			second.sendMessage(ChatColor.DARK_AQUA + "Starting duel against " + ChatColor.YELLOW + (teamFight ? Bukkit.getPlayer(firstPartyLeaderUUID).getName() + "'s party" : Bukkit.getPlayer(firstTeam.get(0)).getName()));
			sm.heal(true);
			
			green2.addEntry(second.getName());
			red1.addEntry(second.getName());
			second.setScoreboard(secondPlayerScoreboard);
		}
		if (teamFight) {
			List<Party> partyList = Lists.newArrayList(Main.getInstance().getPartyManager().getParty(firstPartyLeaderUUID), Main.getInstance().getPartyManager().getParty(secondPartyLeaderUUID));
            for (Party parties : partyList) {
            	if (parties == null) continue;
            	parties.setPartyState(PartyState.DUELING);
            	Main.getInstance().getPartyManager().updateParty(parties);
            }
            partyList.clear(); // TODO: console error = remove
        }
        if (firstTeam.size() == 1 && secondTeam.size() == 1 && (firstPartyLeaderUUID == null && secondPartyLeaderUUID == null)) {
        	if (!ranked) {
        		Main.getInstance().getInventoryManager().updateUnrankedInventory();
        	} else {
        		Main.getInstance().getInventoryManager().updateRankedInventory();
        	}
        }
		teleportRandomArena(new Duel(arena, ladder, firstPartyLeaderUUID, secondPartyLeaderUUID, firstTeam, secondTeam, ranked));
	}
	
	public void createSplitTeamsDuel(Party party, Ladders ladder) {
		for (UUID membersUUID : party.getMembers()) {
			Player members = Bukkit.getPlayer(membersUUID);
			
			if (members == null) continue;
			PlayerManager membersManager = PlayerManager.get(membersUUID);
			
			if (membersManager.getStatus() != PlayerStatus.SPAWN) {
				Bukkit.getPlayer(party.getLeader()).sendMessage(ChatColor.RED + "A member in your party isn't in the spawn!");
				return;
			}
		}
		List<UUID> shuffle = Lists.newArrayList(party.getAllMembersOnline());
        Collections.shuffle(shuffle);
        
        List<UUID> firstTeam = shuffle.subList(0, (int)(shuffle.size() / 2.0));
        List<UUID> secondTeam = shuffle.subList((int)(shuffle.size() / 2.0), shuffle.size());
        
        startDuel(Arena.getInstance().getRandomArena(ladder == Ladders.SUMO), ladder, party.getLeader(), party.getLeader(), firstTeam, secondTeam, false);
	}
	
	public void endDuel(Duel duel, int winningTeamNumber) {
		deathMessage(duel, winningTeamNumber);
		
		if (duel.isRanked()) {
			UUID winnerUUID = (winningTeamNumber == 1 ? duel.getFirstTeam().get(0) : duel.getSecondTeam().get(0));
			UUID loserUUID = (winnerUUID == duel.getFirstTeam().get(0) ? duel.getSecondTeam().get(0) : duel.getFirstTeam().get(0));
			
			this.tranferElo(winnerUUID, loserUUID, duel.getLadder());
		}
		
		Iterator<UUID> specIt = duel.getAllSpectators().iterator();
		while (specIt.hasNext()) {
			Player spec = Bukkit.getPlayer(specIt.next());
			if (spec == null) continue;
			PlayerManager sm = PlayerManager.get(spec.getUniqueId());
			
			spec.setFlySpeed(0.1f);
			spec.setWalkSpeed(0.2f);
			spec.setAllowFlight(false);
			spec.setFlying(false);
			sm.setStatus(PlayerStatus.SPAWN);
			sm.showAllPlayer();
			sm.setSpectate(null);
			spec.teleport(spec.getWorld().getSpawnLocation());
			Main.getInstance().getItemManager().giveSpawnItem(spec);
			specIt.remove();
		}
		if (duel.getFirstTeamPartyLeaderUUID() != null && duel.getSecondTeamPartyLeaderUUID() != null) {
			List<Party> partyList = Lists.newArrayList(Main.getInstance().getPartyManager().getParty(duel.getFirstTeamPartyLeaderUUID()), Main.getInstance().getPartyManager().getParty(duel.getSecondTeamPartyLeaderUUID()));
            for (Party parties : partyList) {
            	if (parties == null) continue;
            	parties.setPartyState(PartyState.LOBBY);
            	Main.getInstance().getPartyManager().updateParty(parties);
            }
            partyList.clear(); // TODO: console error = remove
        }
        if (duel.getFirstTeam().size() == 1 && duel.getSecondTeam().size() == 1 && (duel.getFirstTeamPartyLeaderUUID() == null && duel.getSecondTeamPartyLeaderUUID() == null)) {
        	if (!duel.isRanked()) {
        		Main.getInstance().getInventoryManager().updateUnrankedInventory();
        	} else {
        		Main.getInstance().getInventoryManager().updateRankedInventory();
        	}
        }
		duel.clearDrops();
	}
	
	private void tranferElo(UUID winnerUUID, UUID loserUUID, Ladders ladder) {
		final EloManager wm = PlayerManager.get(winnerUUID).getEloManager();
		final EloManager lm = PlayerManager.get(loserUUID).getEloManager();
		final double expectedp = 1.0D / (1.0D + Math.pow(10.0D, (wm.getElo(ladder) - lm.getElo(ladder)) / 400.0D));
		final int scoreChange = MathUtils.limit((expectedp * 32.0D), 4, 25);
		
		wm.addElo(ladder, scoreChange);
		lm.removeElo(ladder, scoreChange);
		
		final Player winner = Bukkit.getPlayer(winnerUUID);
		final Player loser = Bukkit.getPlayer(loserUUID);

		final String eloMessage = ChatColor.GOLD + "Elo Changes: " + ChatColor.GREEN + winner.getName() + " (+" + scoreChange + ") " + ChatColor.RED + loser.getName() + " (-" + scoreChange + ")";
		winner.sendMessage(eloMessage);
		loser.sendMessage(eloMessage);
	}
	
	// TODO: Sometimes there's an NULLPOINTEREXCEPTION appear
	private void deathMessage(Duel duel, int winningTeamNumber) {
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
		
		for (UUID wUUID : winnerTeam) {
			final Player winners = Bukkit.getPlayer(wUUID);
			if (winners == null) continue;
			TextComponent wtxt = new TextComponent(winners.getName());
		    	
			wtxt.setColor(net.md_5.bungee.api.ChatColor.GREEN);
			wtxt.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(ChatColor.GREEN + "Click to view " + winners.getName() + "'s inventory").create()));
			wtxt.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/inventory " + winners.getUniqueId()));
			    
			joiner.add(wtxt);
		}
		for (UUID lUUID : loserTeam) {
			final Player losers = Bukkit.getPlayer(lUUID);
			if (losers == null) continue;
			TextComponent ltxt = new TextComponent(losers.getName());
		    	
			ltxt.setColor(net.md_5.bungee.api.ChatColor.RED);
			ltxt.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(ChatColor.RED + "Click to view " + losers.getName() + "'s inventory").create()));
			ltxt.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/inventory " + losers.getUniqueId()));
			    
			joiner.add(ltxt);
		}
		    
		invTxt.addExtra(joiner.toTextComponent());
		    
		StringJoiner spect = new StringJoiner(ChatColor.DARK_AQUA + ", ");
		if (duel.hasSpectator()) {
			for (UUID specs : duel.getAllSpectators()) {
				final Player spec = Bukkit.getPlayer(specs);
				spect.add(ChatColor.YELLOW + spec.getName());
			}
		}
		final String spectatorMessage = ChatColor.DARK_AQUA + "Spectator" + (duel.getAllSpectators().size() > 1 ? "s: " : ": ") + spect.toString();
		    
		List<UUID> duelPlayers = Lists.newArrayList(duel.getFirstAndSecondTeams());
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
					endDuel(duel, (duel.getFirstTeamAlive().isEmpty() ? 2 : 1));
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
        	endDuel(duel, (duel.getFirstTeam().isEmpty() ? 2 : 1));
        	return;
        }
		
		for (UUID firstUUID : duel.getFirstTeam()) {
			Player first = Bukkit.getPlayer(firstUUID);
			
			if (first == null) continue;
			
			PlayerManager pmf = PlayerManager.get(firstUUID);
			this.uuidIdentifierToDuel.put(firstUUID, duel);
			
			pmf.heal(true);
			first.setNoDamageTicks(50);
			
			pmf.hideAllPlayer();
			Main.getInstance().getItemManager().givePreFightItems(first, duel.getLadder());
			
			first.teleport(duel.getArena().getLocations()[0]);
			first.setSneaking(false);
		}
		for (UUID secondUUID : duel.getSecondTeam()) {
			Player second = Bukkit.getPlayer(secondUUID);
			
			if (second == null) continue;
			
			PlayerManager pms = PlayerManager.get(secondUUID);
			this.uuidIdentifierToDuel.put(secondUUID, duel);
			
			pms.heal(true);
			second.setNoDamageTicks(50);
			
			pms.hideAllPlayer();
			Main.getInstance().getItemManager().givePreFightItems(second, duel.getLadder());
			
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
		PlayerManager pm = PlayerManager.get(player.getUniqueId());
		pm.saveInventory();
		
		currentDuel.killPlayer(player.getUniqueId());
		final String message = (reason == RemoveReason.KILLED ? player.getName() + " has been killed" + (player.getKiller() != null ? " by " + player.getKiller().getName() : "") : player.getName() + " has disconnected");
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
				}
			}.runTaskLater(Main.getInstance(), 50L);
		}
		endDuel(currentDuel, winningTeamNumber);
		new BukkitRunnable() {
				
			@Override
			public void run() {
				finishDuel(currentDuel);
			}
		}.runTaskLater(Main.getInstance(), 50L);
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
	
	private void finishDuel(Duel duel) {
		List<UUID> duelPlayerUUID = Lists.newArrayList(duel.getFirstTeam());
        duelPlayerUUID.addAll(duel.getSecondTeam());
        
		for (UUID dpUUID : duelPlayerUUID) {
			Player duelPlayer = Bukkit.getPlayer(dpUUID);
			if (duelPlayer == null) continue;
			PlayerManager dpm = PlayerManager.get(duelPlayer.getUniqueId());
			
			duelPlayer.setScoreboard(Bukkit.getServer().getScoreboardManager().getNewScoreboard());
			
			dpm.setStatus(PlayerStatus.SPAWN);
			dpm.heal(false);
			if (dpm.isAlive()) {
				dpm.showAllPlayer();
			}
			dpm.getMatchStats().resetDuelStats();
			
			dpm.getMatchStats().removeEnderPearlCooldown();
			this.uuidIdentifierToDuel.remove(duelPlayer.getUniqueId());
		}
		duelPlayerUUID.clear();
	}
	
	private void doEndDuelAction(Player player) {
		PlayerManager pm = PlayerManager.get(player.getUniqueId());
        
		pm.saveInventory();
        pm.heal(false);
        pm.setStatus(PlayerStatus.SPAWN);
        player.getInventory().clear();
        player.getInventory().setArmorContents(null);
        if (!player.getActivePotionEffects().isEmpty()) {
			for (PotionEffect effect : player.getActivePotionEffects()) {
				player.removePotionEffect(effect.getType());
			}
		}
        player.extinguish();
        player.setItemOnCursor(null);
	}
}
