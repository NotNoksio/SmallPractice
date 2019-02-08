package us.noks.smallpractice.objects.managers;

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

public class DuelManager {
	
	public static DuelManager instance = new DuelManager();
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
        
		List<Player> duelPlayers = Lists.newArrayList();
		
		for (UUID firstUUID : firstTeam) {
			Player first = Bukkit.getPlayer(firstUUID);
			
			if (first == null) {
                continue;
            }
			
			PlayerManager fm = PlayerManager.get(first);
			fm.removeRequest();
			fm.setStatus(PlayerStatus.WAITING);
			
			first.setGameMode(GameMode.SURVIVAL);
			first.sendMessage(ChatColor.DARK_AQUA + "Starting duel against " + ChatColor.YELLOW + Bukkit.getPlayer(secondTeam.get(0)).getName());
			
			green1.addEntry(first.getName());
			red2.addEntry(first.getName());
			first.setScoreboard(firstPlayerScoreboard);
			duelPlayers.add(first);
		}
		for (UUID secondUUID : secondTeam) {
			Player second = Bukkit.getPlayer(secondUUID);
			
			if (second == null) {
                continue;
            }
			
			PlayerManager sm = PlayerManager.get(second);
			sm.removeRequest();
			sm.setStatus(PlayerStatus.WAITING);
			
			second.setGameMode(GameMode.SURVIVAL);
			second.sendMessage(ChatColor.DARK_AQUA + "Starting duel against " + ChatColor.YELLOW + Bukkit.getPlayer(firstTeam.get(0)).getName());
			
			green2.addEntry(second.getName());
			red1.addEntry(second.getName());
			second.setScoreboard(secondPlayerScoreboard);
			duelPlayers.add(second);
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
        for (Player team : duelPlayers) {
            for (Player ally : duelPlayers) {
                team.showPlayer(ally);
                ally.showPlayer(team);
            }
        }
		duelPlayers.clear();
		teleportRandomArena(new Duel(firstPartyLeaderUUID, secondPartyLeaderUUID, firstTeam, secondTeam, ranked));
	}
	
	public void endDuel(Duel duel, int winningTeamNumber) {
		InvView.getInstance().deathMsg(duel, winningTeamNumber);
		
		if (duel.isRanked()) {
			UUID winnerUUID = (winningTeamNumber == 1 ? duel.getFirstTeamUUID().get(0) : duel.getSecondTeamUUID().get(0));
			UUID loserUUID = (winnerUUID == duel.getFirstTeamUUID().get(0) ? duel.getSecondTeamUUID().get(0) : duel.getFirstTeamUUID().get(0));
			
			EloManager.getInstance().tranferElo(winnerUUID, loserUUID);
		}
		
		Iterator<UUID> it = duel.getAllSpectatorsUUID().iterator();
		while (it.hasNext()) {
			Player spec = Bukkit.getPlayer(it.next());
			PlayerManager sm = PlayerManager.get(spec);
			
			spec.setAllowFlight(false);
			spec.setFlying(false);
			sm.setStatus(PlayerStatus.SPAWN);
			sm.showAllPlayer();
			sm.setSpectate(null);
			spec.teleport(Main.getInstance().getSpawnLocation());
			sm.giveSpawnItem();
			
			it.remove();
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
		for (UUID firstUUID : duel.getFirstTeamUUID()) {
			Player first = Bukkit.getPlayer(firstUUID);
			PlayerManager pmf = PlayerManager.get(first);
			
			first.setScoreboard(Main.getInstance().getServer().getScoreboardManager().getNewScoreboard());
			first.extinguish();
			first.clearPotionEffect();
			
			pmf.setStatus(PlayerStatus.SPAWN);
			pmf.showAllPlayer();
			pmf.resetDuelStats();
			
			EnderDelay.getInstance().removeCooldown(first);
			this.uuidIdentifierToDuel.remove(first.getUniqueId());
		}
		for (UUID secondUUID : duel.getSecondTeamUUID()) {
			Player second = Bukkit.getPlayer(secondUUID);
			PlayerManager pms = PlayerManager.get(second);
			
			second.setScoreboard(Main.getInstance().getServer().getScoreboardManager().getNewScoreboard());
			second.extinguish();
			second.clearPotionEffect();
			
			pms.setStatus(PlayerStatus.SPAWN);
			pms.showAllPlayer();
			pms.resetDuelStats();
			
			EnderDelay.getInstance().removeCooldown(second);
			this.uuidIdentifierToDuel.remove(second.getUniqueId());
		}
	}
	
	public void sendWaitingMessage(Duel duel) {
		Map<Duel, Integer> cooldown = Maps.newHashMap();
		List<UUID> duelPlayers = Lists.newArrayList(duel.getFirstTeamAlive());
		duelPlayers.addAll(duel.getSecondTeamAlive());
		
		cooldown.put(duel, 5);
		
		new BukkitRunnable() {
			int num = cooldown.get(duel);
			
			@Override
			public void run() {
				if (!cooldown.containsKey(duel)) {
					this.cancel();
				}
				for (UUID uuid : duelPlayers) {
					Player player = Bukkit.getPlayer(uuid);
					
					if (player == null) {
						this.cancel();
					}
					if(player.isDead()) {
						this.cancel();
					}
					if (PlayerManager.get(player).getStatus() != PlayerStatus.WAITING) {
						this.cancel();
					}
				}
				if (num <= 0) {
					duel.sendSoundedMessage(ChatColor.GREEN + "Duel has stated!", Sound.FIREWORK_BLAST);
					for (UUID uuid : duelPlayers) {
						Player player = Bukkit.getPlayer(uuid);
						PlayerManager.get(player).setStatus(PlayerStatus.DUEL);
					}
					for (UUID firstUUID : duel.getFirstTeamAlive()) {
						Player first = Bukkit.getPlayer(firstUUID);
							
						for (UUID secondUUID : duel.getSecondTeamAlive()) {
							Player second = Bukkit.getPlayer(secondUUID);
								
							first.showPlayer(second);
							second.showPlayer(first);
						}
					}
					cooldown.remove(duel);
					duelPlayers.clear();
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
		
		for (UUID firstUUID : duel.getFirstTeamUUID()) {
			this.uuidIdentifierToDuel.put(firstUUID, duel);
			Player first = Bukkit.getPlayer(firstUUID);
			PlayerManager pmf = PlayerManager.get(first);
			
			pmf.hideAllPlayer();
			pmf.giveKit();
			
			first.setHealth(20.0D);
			first.clearPotionEffect();
			first.setFoodLevel(20);
			first.setSaturation(20f);
			first.setNoDamageTicks(50);
			first.teleport(Main.getInstance().arenaList.get(pickedArena)[0]);
			first.setSneaking(false);
		}
		for (UUID secondUUID : duel.getSecondTeamUUID()) {
			this.uuidIdentifierToDuel.put(secondUUID, duel);
			Player second = Bukkit.getPlayer(secondUUID);
			PlayerManager pms = PlayerManager.get(second);
			
			pms.hideAllPlayer();
			pms.giveKit();
			
			second.setHealth(20.0D);
			second.clearPotionEffect();
			second.setFoodLevel(20);
			second.setSaturation(20f);
			second.setNoDamageTicks(50);
			second.teleport(Main.getInstance().arenaList.get(pickedArena)[1]);
			second.setSneaking(false);
		}
		sendWaitingMessage(duel);
	}
	
	public void removePlayerFromDuel(Player player) {
		Duel currentDuel = getDuelFromPlayerUUID(player.getUniqueId());
		
		if (currentDuel == null) return;
		
		InvView.getInstance().saveInv(player);
		currentDuel.sendMessage(player.getName() + " has been killed" + ((player.getKiller() != null) ? (" by " + player.getKiller().getName()) : ""));
		
		if (currentDuel.getFirstTeamUUID().contains(player.getUniqueId())) {
			currentDuel.killFirstTeamPlayer(player.getUniqueId());
		} else {
			currentDuel.killSecondTeamPlayer(player.getUniqueId());
		}
		
		if (currentDuel.getFirstTeamAlive().size() == 0) {
			for (UUID lastPlayersUUID : currentDuel.getSecondTeamAlive()) {
                Player lastPlayers = Bukkit.getPlayer(lastPlayersUUID);
                InvView.getInstance().saveInv(lastPlayers);
                
                if (lastPlayers != null) {
                	lastPlayers.setHealth(20.0D);
                	lastPlayers.setFoodLevel(20);
                	lastPlayers.setSaturation(10000f);
        			
        			new BukkitRunnable() {
        				
        				@Override
        				public void run() {
        					if (lastPlayers != null) {
        						lastPlayers.teleport(Main.getInstance().getSpawnLocation());
        						PlayerManager.get(lastPlayers).giveSpawnItem();
        					}
        				}
        			}.runTaskLater(Main.getInstance(), 40L);
        		}
            }
			endDuel(currentDuel, 2);
		} else if (currentDuel.getSecondTeamAlive().size() == 0) {
			for (UUID lastPlayersUUID : currentDuel.getFirstTeamAlive()) {
                Player lastPlayers = Bukkit.getPlayer(lastPlayersUUID);
                InvView.getInstance().saveInv(lastPlayers);
                
                if (lastPlayers != null) {
                	lastPlayers.setHealth(20.0D);
                	lastPlayers.setFoodLevel(20);
                	lastPlayers.setSaturation(10000f);
        			
        			new BukkitRunnable() {
        				
        				@Override
        				public void run() {
        					if (lastPlayers != null) {
        						lastPlayers.teleport(Main.getInstance().getSpawnLocation());
        						PlayerManager.get(lastPlayers).giveSpawnItem();
        					}
        				}
        			}.runTaskLater(Main.getInstance(), 40L);
        		}
            }
			endDuel(currentDuel, 1);
		}
	}
}
