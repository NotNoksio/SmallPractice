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
import us.noks.smallpractice.enums.PlayerStatus;
import us.noks.smallpractice.listeners.EnderDelay;
import us.noks.smallpractice.objects.Duel;
import us.noks.smallpractice.party.Party;
import us.noks.smallpractice.party.PartyState;
import us.noks.smallpractice.utils.InvView;
import us.noks.smallpractice.utils.Messages;

public class DuelManager {
	
	public static DuelManager instance = new DuelManager();
	public static DuelManager getInstance() {
		return instance;
	}
	
	private Map<UUID, Duel> uuidIdentifierToDuel = Maps.newHashMap();
	public Duel getDuelFromPlayerUUID(UUID uuid) {
        return this.uuidIdentifierToDuel.get(uuid);
    }
	
	public void startDuel(UUID firstPartyLeaderUUID, UUID secondPartyLeaderUUID, List<UUID> firstTeam, List<UUID> secondTeam, boolean ranked, int round) {
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
			
			if (first == null) continue;
			
			PlayerManager fm = PlayerManager.get(firstUUID);
			fm.removeRequest();
			fm.setStatus(PlayerStatus.WAITING);
			
			first.setGameMode(GameMode.SURVIVAL);
			first.sendMessage(ChatColor.DARK_AQUA + "Starting duel against " + ChatColor.YELLOW + (secondPartyLeaderUUID != null ? Bukkit.getPlayer(secondPartyLeaderUUID).getName() + "'s party" : Bukkit.getPlayer(secondTeam.get(0)).getName()) + " (" + Rounds.getDisplayNameByRound(round) + ")");
			fm.heal();
			
			green1.addEntry(first.getName());
			red2.addEntry(first.getName());
			first.setScoreboard(firstPlayerScoreboard);
		}
		for (UUID secondUUID : secondTeam) {
			Player second = Bukkit.getPlayer(secondUUID);
			
			if (second == null) continue;
			
			PlayerManager sm = PlayerManager.get(secondUUID);
			sm.removeRequest();
			sm.setStatus(PlayerStatus.WAITING);
			
			second.setGameMode(GameMode.SURVIVAL);
			second.sendMessage(ChatColor.DARK_AQUA + "Starting duel against " + ChatColor.YELLOW + (firstPartyLeaderUUID != null ? Bukkit.getPlayer(firstPartyLeaderUUID).getName() + "'s party" : Bukkit.getPlayer(firstTeam.get(0)).getName()) + " (" + Rounds.getDisplayNameByRound(round) + ")");
			sm.heal();
			
			green2.addEntry(second.getName());
			red1.addEntry(second.getName());
			second.setScoreboard(secondPlayerScoreboard);
		}
		round--;
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
		teleportRandomArena(new Duel(firstPartyLeaderUUID, secondPartyLeaderUUID, firstTeam, secondTeam, ranked, round));
	}
	
	public void createSplitTeamsDuel(Party party) {
		List<UUID> shuffle = Lists.newArrayList(party.getAllMembersOnline());
        Collections.shuffle(shuffle);
        
        List<UUID> firstTeam = shuffle.subList(0, (int)(shuffle.size() / 2.0));
        List<UUID> secondTeam = shuffle.subList((int)(shuffle.size() / 2.0), shuffle.size());
        
        startDuel(party.getLeader(), party.getLeader(), firstTeam, secondTeam, false, 1);
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
			PlayerManager sm = PlayerManager.get(specIt.next());
			
			spec.setAllowFlight(false);
			spec.setFlying(false);
			sm.setStatus(PlayerStatus.SPAWN);
			sm.showAllPlayer();
			sm.setSpectate(null);
			spec.teleport(Main.getInstance().getSpawnLocation());
			sm.giveSpawnItem();
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
        
        Iterator<UUID> duelIt = duelPlayerUUID.iterator(); 
		while (duelIt.hasNext()) {
			Player duelPlayer = Bukkit.getPlayer(duelIt.next());
			if (duelPlayer == null) continue;
			PlayerManager dpm = PlayerManager.get(duelIt.next());
			
			duelPlayer.setScoreboard(Main.getInstance().getServer().getScoreboardManager().getNewScoreboard());
			duelPlayer.extinguish();
			duelPlayer.clearPotionEffect();
			
			dpm.setStatus(PlayerStatus.SPAWN);
			dpm.showAllPlayer();
			dpm.resetDuelStats();
			
			EnderDelay.getInstance().removeCooldown(duelPlayer);
			this.uuidIdentifierToDuel.remove(duelPlayer.getUniqueId());
			duelIt.remove();
		}
		if (duel.hasRemainingRound() && (duel.getFirstTeam().isEmpty() || duel.getSecondTeam().isEmpty())) {
			duel.sendMessage(ChatColor.RED + "The duel has been cancelled due to an empty team.");
			return;
		}
		if (duel.hasRemainingRound() && !duel.getFirstTeam().isEmpty() && !duel.getSecondTeam().isEmpty()) {
			startDuel(duel.getFirstTeamPartyLeaderUUID(), duel.getSecondTeamPartyLeaderUUID(), duel.getFirstTeam(), duel.getSecondTeam(), duel.isRanked(), duel.getRound());
		}
	}
	
	public void sendWaitingMessage(Duel duel) {
		Map<Duel, Integer> cooldown = Maps.newHashMap();
		cooldown.put(duel, 5);
		
		new BukkitRunnable() {
			int num = cooldown.get(duel);
			
			@Override
			public void run() {
				if (!cooldown.containsKey(duel)) {
					this.cancel();
				}
				if (!duel.isValid()) {
					duel.sendMessage(ChatColor.RED + "The current duel has been cancelled due to his invalidity.");
					endDuel(duel, (duel.getFirstTeamAlive().isEmpty() ? 2 : 1));
					this.cancel();
				}
				if (num <= 0) {
					duel.sendSoundedMessage(ChatColor.GREEN + "Duel has stated!", Sound.FIREWORK_BLAST);
					duel.showDuelPlayer();
					duel.setDuelPlayersStatusToDuel();
					cooldown.remove(duel);
					this.cancel();
				}
				if (num > 0) {
					duel.sendSoundedMessage(ChatColor.DARK_AQUA + "Duel start in " + ChatColor.YELLOW + num + ChatColor.DARK_AQUA + " second" + (num > 1 ? "s.." : ".."), Sound.NOTE_PLING);
					cooldown.put(duel, num--);
				}
			}
		}.runTaskTimer(Main.getInstance(), 20L, 20L);
	}
	
	private void teleportRandomArena(Duel duel) {
		Random random = new Random();
		int pickedArena = random.nextInt(Main.getInstance().arenaList.size()) + 1;
		
		for (UUID firstUUID : duel.getFirstTeam()) {
			Player first = Bukkit.getPlayer(firstUUID);
			
			if (first == null) continue;
			
			this.uuidIdentifierToDuel.put(firstUUID, duel);
			PlayerManager pmf = PlayerManager.get(firstUUID);
			
			pmf.heal();
			first.setNoDamageTicks(50);
			
			pmf.hideAllPlayer();
			pmf.giveKit();
			
			first.teleport(Main.getInstance().arenaList.get(pickedArena)[0]);
			first.setSneaking(false);
		}
		for (UUID secondUUID : duel.getSecondTeam()) {
			Player second = Bukkit.getPlayer(secondUUID);
			
			if (second == null) continue;
			
			this.uuidIdentifierToDuel.put(secondUUID, duel);
			PlayerManager pms = PlayerManager.get(secondUUID);
			
			pms.heal();
			second.setNoDamageTicks(50);
			
			pms.hideAllPlayer();
			pms.giveKit();
			
			second.teleport(Main.getInstance().arenaList.get(pickedArena)[1]);
			second.setSneaking(false);
		}
		sendWaitingMessage(duel);
	}
	
	public void removePlayerFromDuel(Player player, boolean disconnect) {
		Duel currentDuel = getDuelFromPlayerUUID(player.getUniqueId());
		
		if (currentDuel == null) return;
		this.uuidIdentifierToDuel.remove(player.getUniqueId());
		
		InvView.getInstance().saveInv(player);
		currentDuel.sendMessage(player.getName() + (!disconnect ? " has been killed" : " has disconnected") + (player.getKiller() != null ? (!disconnect ? " by " : " while fighting ") + player.getKiller().getName() : ""));
		
		if (currentDuel.getFirstTeam().contains(player.getUniqueId())) {
			currentDuel.killFirstTeamPlayer(player.getUniqueId());
			if (disconnect) currentDuel.removeFirstTeamPlayer(player.getUniqueId());
		} else {
			currentDuel.killSecondTeamPlayer(player.getUniqueId());
			if (disconnect) currentDuel.removeSecondTeamPlayer(player.getUniqueId());
		}
		
		if (currentDuel.getFirstTeamAlive().isEmpty()) {
			for (UUID lastPlayersUUID : currentDuel.getSecondTeamAlive()) {
                Player lastPlayers = Bukkit.getPlayer(lastPlayersUUID);
                InvView.getInstance().saveInv(lastPlayers);
                
                if (lastPlayers == null) continue;
                
                lastPlayers.setHealth(20.0D);
                lastPlayers.setFoodLevel(20);
                lastPlayers.setSaturation(10000f);
        			
                if (currentDuel.hasRemainingRound()) continue;
                	
                new BukkitRunnable() {
        				
                	@Override
                	public void run() {
                		if (lastPlayers != null) {
                			lastPlayers.teleport(Main.getInstance().getSpawnLocation());
                			PlayerManager.get(lastPlayersUUID).giveSpawnItem();
                		}
                	}
                }.runTaskLaterAsynchronously(Main.getInstance(), 40L);
            }
			endDuel(currentDuel, 2);
		} else if (currentDuel.getSecondTeamAlive().isEmpty()) {
			for (UUID lastPlayersUUID : currentDuel.getFirstTeamAlive()) {
                Player lastPlayers = Bukkit.getPlayer(lastPlayersUUID);
                InvView.getInstance().saveInv(lastPlayers);
                
                if (lastPlayers == null) continue;
                
                lastPlayers.setHealth(20.0D);
                lastPlayers.setFoodLevel(20);
                lastPlayers.setSaturation(10000f);
        			
                if (currentDuel.hasRemainingRound()) continue;
                	
                new BukkitRunnable() {
        				
                	@Override
                	public void run() {
                		if (lastPlayers != null) {
                			lastPlayers.teleport(Main.getInstance().getSpawnLocation());
                			PlayerManager.get(lastPlayersUUID).giveSpawnItem();
                		}
                	}
                }.runTaskLaterAsynchronously(Main.getInstance(), 40L);
            }
			endDuel(currentDuel, 1);
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
