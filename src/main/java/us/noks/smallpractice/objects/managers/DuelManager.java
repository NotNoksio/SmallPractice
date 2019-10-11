package us.noks.smallpractice.objects.managers;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import com.google.common.collect.Lists;

import net.minecraft.util.com.google.common.collect.Maps;
import us.noks.smallpractice.Main;
import us.noks.smallpractice.arena.Arena;
import us.noks.smallpractice.enums.PlayerStatus;
import us.noks.smallpractice.listeners.EnderDelay;
import us.noks.smallpractice.objects.Duel;
import us.noks.smallpractice.party.Party;
import us.noks.smallpractice.party.PartyState;
import us.noks.smallpractice.utils.InvView;
import us.noks.smallpractice.utils.Messages;

public class DuelManager {
	private static DuelManager instance = new DuelManager();
	public static DuelManager getInstance() {
		return instance;
	}
	
	private Map<UUID, Duel> uuidIdentifierToDuel = Maps.newHashMap();
	public Duel getDuelFromPlayerUUID(UUID uuid) {
        return this.uuidIdentifierToDuel.get(uuid);
    }
	
	public void startDuel(UUID firstPartyLeaderUUID, UUID secondPartyLeaderUUID, List<UUID> firstTeam, List<UUID> secondTeam, boolean ranked) {
		Scoreboard firstPlayerScoreboard = Main.getInstance().getServer().getScoreboardManager().getNewScoreboard();
		Team red1 = firstPlayerScoreboard.registerNewTeam("red");
		red1.setPrefix(ChatColor.RED.toString());
		Team green1 = firstPlayerScoreboard.registerNewTeam("green");
		green1.setPrefix(ChatColor.GREEN.toString());
		green1.setAllowFriendlyFire(false);
        
		Scoreboard secondPlayerScoreboard = Main.getInstance().getServer().getScoreboardManager().getNewScoreboard();
		Team red2 = secondPlayerScoreboard.registerNewTeam("red");
		red2.setPrefix(ChatColor.RED.toString());
		Team green2 = secondPlayerScoreboard.registerNewTeam("green");
		green2.setPrefix(ChatColor.GREEN.toString());
		green2.setAllowFriendlyFire(false);
        
		for (UUID firstUUID : firstTeam) {
			Player first = Bukkit.getPlayer(firstUUID);
			
			if (first == null) {
				firstTeam.remove(firstUUID);
				continue;
			}
			if (firstTeam.isEmpty()) continue;
			
			PlayerManager fm = PlayerManager.get(firstUUID);
			fm.removeRequest();
			fm.setStatus(PlayerStatus.WAITING);
			
			first.setGameMode(GameMode.SURVIVAL);
			first.sendMessage(ChatColor.DARK_AQUA + "Starting duel against " + ChatColor.YELLOW + (secondPartyLeaderUUID != null ? Bukkit.getPlayer(secondPartyLeaderUUID).getName() + "'s party" : Bukkit.getPlayer(secondTeam.get(0)).getName()));
			fm.heal();
			
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
			sm.removeRequest();
			sm.setStatus(PlayerStatus.WAITING);
			
			second.setGameMode(GameMode.SURVIVAL);
			second.sendMessage(ChatColor.DARK_AQUA + "Starting duel against " + ChatColor.YELLOW + (firstPartyLeaderUUID != null ? Bukkit.getPlayer(firstPartyLeaderUUID).getName() + "'s party" : Bukkit.getPlayer(firstTeam.get(0)).getName()));
			sm.heal();
			
			green2.addEntry(second.getName());
			red1.addEntry(second.getName());
			second.setScoreboard(secondPlayerScoreboard);
		}
		if (firstPartyLeaderUUID != null) {
            Party party = PartyManager.getInstance().getParty(firstPartyLeaderUUID);
            if (party != null) {
                party.setPartyState(PartyState.DUELING);
            }
        }
        if (secondPartyLeaderUUID != null) {
        	Party party = PartyManager.getInstance().getParty(secondPartyLeaderUUID);
            if (party != null) {
                party.setPartyState(PartyState.DUELING);
            }
        }
		teleportRandomArena(new Duel(firstPartyLeaderUUID, secondPartyLeaderUUID, firstTeam, secondTeam, ranked));
	}
	
	public void createSplitTeamsDuel(Party party) {
		List<UUID> shuffle = Lists.newArrayList(party.getAllMembersOnline());
        Collections.shuffle(shuffle);
        
        List<UUID> firstTeam = shuffle.subList(0, (int)(shuffle.size() / 2.0));
        List<UUID> secondTeam = shuffle.subList((int)(shuffle.size() / 2.0), shuffle.size());
        
        startDuel(party.getLeader(), party.getLeader(), firstTeam, secondTeam, false);
	}
	
	public void endDuel(Duel duel, int winningTeamNumber) {
		Messages.getInstance().deathMessage(duel, winningTeamNumber);
		
		if (duel.isRanked()) {
			UUID winnerUUID = (winningTeamNumber == 1 ? duel.getFirstTeam().get(0) : duel.getSecondTeam().get(0));
			UUID loserUUID = (winnerUUID == duel.getFirstTeam().get(0) ? duel.getSecondTeam().get(0) : duel.getFirstTeam().get(0));
			
			EloManager.getInstance().tranferElo(winnerUUID, loserUUID);
		}
		
		Iterator<UUID> specIt = duel.getAllSpectators().iterator();
		while (specIt.hasNext()) {
			Player spec = Bukkit.getPlayer(specIt.next());
			if (spec == null) continue;
			PlayerManager sm = PlayerManager.get(spec.getUniqueId());
			
			spec.setAllowFlight(false);
			spec.setFlying(false);
			sm.setStatus(PlayerStatus.SPAWN);
			sm.showAllPlayer();
			sm.setSpectate(null);
			spec.teleport(Main.getInstance().getSpawnLocation());
			ItemManager.getInstace().giveSpawnItem(spec);
			specIt.remove();
		}
		if (duel.getFirstTeamPartyLeaderUUID() != null) {
            Party firstTeamParty = PartyManager.getInstance().getParty(duel.getFirstTeamPartyLeaderUUID());
            if (firstTeamParty != null) {
                firstTeamParty.setPartyState(PartyState.LOBBY);
            }
        }
        if (duel.getSecondTeamPartyLeaderUUID() != null) {
            Party secondTeamParty = PartyManager.getInstance().getParty(duel.getSecondTeamPartyLeaderUUID());
            if (secondTeamParty != null) {
                secondTeamParty.setPartyState(PartyState.LOBBY);
            }
        }
        List<UUID> duelPlayerUUID = Lists.newArrayList(duel.getFirstTeam());
        duelPlayerUUID.addAll(duel.getSecondTeam());
        
		for (UUID dpUUID : duelPlayerUUID) {
			Player duelPlayer = Bukkit.getPlayer(dpUUID);
			if (duelPlayer == null) continue;
			PlayerManager dpm = PlayerManager.get(duelPlayer.getUniqueId());
			
			duelPlayer.setScoreboard(Main.getInstance().getServer().getScoreboardManager().getNewScoreboard());
			duelPlayer.extinguish();
			duelPlayer.clearPotionEffect();
			
			dpm.setStatus(PlayerStatus.SPAWN);
			dpm.showAllPlayer();
			dpm.getMatchStats().resetDuelStats();
			
			new EnderDelay(Main.getInstance()).removeCooldown(duelPlayer);
			this.uuidIdentifierToDuel.remove(duelPlayer.getUniqueId());
		}
		duelPlayerUUID.clear();
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
        	duel.sendMessage(Messages.getInstance().EMPTY_TEAM);
        	endDuel(duel, (duel.getFirstTeam().isEmpty() ? 2 : 1));
        	return;
        }
		int random = new Random().nextInt(new Arena().getArenaList().size()) + 1;
		Arena pickedArena = new Arena().getArena(random);
		
		for (UUID firstUUID : duel.getFirstTeam()) {
			Player first = Bukkit.getPlayer(firstUUID);
			
			if (first == null) continue;
			
			PlayerManager pmf = PlayerManager.get(firstUUID);
			this.uuidIdentifierToDuel.put(firstUUID, duel);
			
			pmf.heal();
			first.setNoDamageTicks(50);
			
			pmf.hideAllPlayer();
			ItemManager.getInstace().giveKit(first);
			
			first.teleport(pickedArena.getPositions(random)[0]);
			first.setSneaking(false);
		}
		for (UUID secondUUID : duel.getSecondTeam()) {
			Player second = Bukkit.getPlayer(secondUUID);
			
			if (second == null) continue;
			
			PlayerManager pms = PlayerManager.get(secondUUID);
			this.uuidIdentifierToDuel.put(secondUUID, duel);
			
			pms.heal();
			second.setNoDamageTicks(50);
			
			pms.hideAllPlayer();
			ItemManager.getInstace().giveKit(second);
			
			second.teleport(pickedArena.getPositions(random)[1]);
			second.setSneaking(false);
		}
		sendWaitingMessage(duel);
	}
	
	public void removePlayerFromDuel(Player player) {
		Duel currentDuel = getDuelFromPlayerUUID(player.getUniqueId());
		
		if (currentDuel == null) return;
		this.uuidIdentifierToDuel.remove(player.getUniqueId());
		
		InvView.getInstance().saveInv(player);
		currentDuel.sendMessage(player.getName() + " has been killed" + (player.getKiller() != null ? " by " + player.getKiller().getName() : ""));
		
		if (currentDuel.getFirstTeam().contains(player.getUniqueId())) {
			currentDuel.killFirstTeamPlayer(player.getUniqueId());
		} else {
			currentDuel.killSecondTeamPlayer(player.getUniqueId());
		}
		
		byte winningTeamNumber = 0;
		if (currentDuel.getFirstTeamAlive().isEmpty()) {
			for (UUID lastPlayersUUID : currentDuel.getSecondTeamAlive()) {
                Player lastPlayers = Bukkit.getPlayer(lastPlayersUUID);
                InvView.getInstance().saveInv(lastPlayers);
                
                if (lastPlayers == null) continue;
                
                lastPlayers.setHealth(20.0D);
                lastPlayers.setFoodLevel(20);
                lastPlayers.setSaturation(10000f);
        			
                new BukkitRunnable() {
        				
                	@Override
                	public void run() {
                		if (lastPlayers != null) {
                			lastPlayers.teleport(Main.getInstance().getSpawnLocation());
                			ItemManager.getInstace().giveSpawnItem(lastPlayers);
                		}
                	}
                }.runTaskLater(Main.getInstance(), 40L);
            }
			winningTeamNumber = 2;
		} else if (currentDuel.getSecondTeamAlive().isEmpty()) {
			for (UUID lastPlayersUUID : currentDuel.getFirstTeamAlive()) {
                Player lastPlayers = Bukkit.getPlayer(lastPlayersUUID);
                InvView.getInstance().saveInv(lastPlayers);
                
                if (lastPlayers == null) continue;
                
                lastPlayers.setHealth(20.0D);
                lastPlayers.setFoodLevel(20);
                lastPlayers.setSaturation(10000f);
        			
                new BukkitRunnable() {
        				
                	@Override
                	public void run() {
                		if (lastPlayers != null) {
                			lastPlayers.teleport(Main.getInstance().getSpawnLocation());
                			ItemManager.getInstace().giveSpawnItem(lastPlayers);
                		}
                	}
                }.runTaskLater(Main.getInstance(), 40L);
            }
			winningTeamNumber = 1;
		}
		if (currentDuel.getFirstTeamAlive().isEmpty() || currentDuel.getSecondTeamAlive().isEmpty()) {
			endDuel(currentDuel, winningTeamNumber);
		}
	}
	
	protected enum Rounds {
	    FIRST_ROUND(5, "First Round"), 
	    SECOND_ROUND(4, "Second Round"), 
	    QUARTER_FINAL_ROUND(3, "Quarter Final Round"), 
	    SEMI_FINAL_ROUND(2, "Semi Final Round"), 
	    FINAL_ROUND(1, "Final Round");
		
		private int roundNumber;
		private String displayName;

		Rounds (int roundNumber, String displayName) {
			this.roundNumber = roundNumber;
			this.displayName = displayName;
		}
		
		public int getRoundNumber() {
			return this.roundNumber;
		}
		
		public String getDisplayName() {
			return this.displayName;
		}
		
		private static Rounds fromRound(int round) {
			for (Rounds type : values()) {
				if (type.getRoundNumber() == round) {
					return type;
				}
			}
			return null;
		}
		
		public static String getDisplayNameByRound(int round) {
			return fromRound(round).getDisplayName();
		}
	}
}
