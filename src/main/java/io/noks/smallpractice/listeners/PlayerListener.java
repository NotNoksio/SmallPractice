package io.noks.smallpractice.listeners;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import com.google.common.collect.Lists;

import io.noks.smallpractice.Main;
import io.noks.smallpractice.arena.Arena;
import io.noks.smallpractice.arena.Arena.Arenas;
import io.noks.smallpractice.enums.Ladders;
import io.noks.smallpractice.enums.PlayerStatus;
import io.noks.smallpractice.enums.RemoveReason;
import io.noks.smallpractice.enums.Warps;
import io.noks.smallpractice.objects.duel.Duel;
import io.noks.smallpractice.objects.managers.PlayerManager;
import io.noks.smallpractice.party.Party;
import io.noks.smallpractice.party.PartyState;
import net.md_5.bungee.api.chat.TextComponent;
import net.minecraft.util.org.apache.commons.lang3.BooleanUtils;

public class PlayerListener implements Listener {
	private Main main;
	private String[] WELCOME_MESSAGE;
	public PlayerListener(Main plugin) {
		this.main = plugin;
		this.WELCOME_MESSAGE = new String[]{ChatColor.DARK_AQUA + "Welcome back on " + ChatColor.YELLOW + "Noks.io" + ChatColor.GRAY + " (Practice)",
				"",
				ChatColor.GRAY + "-> " + ChatColor.DARK_AQUA + "Discord: " + ChatColor.GRAY + "discord." + this.main.getConfigManager().serverDomainName,
				ChatColor.GRAY + "-> " + ChatColor.DARK_AQUA + "NameMC: " + ChatColor.GRAY + "namemc." + this.main.getConfigManager().serverDomainName,
				""};
	    this.main.getServer().getPluginManager().registerEvents(this, this.main);
	}
	
	@EventHandler(priority=EventPriority.HIGH)
	public void onJoin(PlayerJoinEvent event) {
		if (this.main.getConfigManager().sendJoinAndQuitMessageToOP) {
			if (this.main.getServer().getOnlinePlayers().size() > 1) {
				for (Player opPlayers : this.main.getServer().getOnlinePlayers()) {
					if (!opPlayers.isOp()) {
						continue;
					}
					opPlayers.sendMessage(event.getJoinMessage());
				}
			}
		}
		event.setJoinMessage(null);
		final Player player = event.getPlayer();
		
		this.main.getDatabaseUtil().loadPlayer(player.getUniqueId());
		
		player.setExp(0.0F);
		player.setLevel(0);
		player.setFlySpeed(0.1f);
		player.setWalkSpeed(0.2f);
		player.setKnockbackReduction(0.0f);
		
		player.setAllowFlight(false);
		player.setFlying(false);
		player.setGameMode(GameMode.SURVIVAL);
		
		player.setScoreboard(this.main.getServer().getScoreboardManager().getNewScoreboard());
		player.setPlayerListHeaderFooter(TextComponent.fromLegacyText("NOKS.IO"), TextComponent.fromLegacyText(this.main.getDescription().getWebsite()));
		
		player.teleport(player.getWorld().getSpawnLocation());
		this.main.getItemManager().giveSpawnItem(player);
		player.sendMessage(this.WELCOME_MESSAGE);
		
		for (Player allPlayers : this.main.getServer().getOnlinePlayers()) {
			final PlayerManager pmAll = PlayerManager.get(allPlayers.getUniqueId());
			if (pmAll.getStatus() == PlayerStatus.WAITING || pmAll.getStatus() == PlayerStatus.DUEL || pmAll.getStatus() == PlayerStatus.MODERATION) {
				player.hidePlayer(allPlayers);
			}
		}
	}
	
	@EventHandler(priority=EventPriority.HIGH)
	public void onQuit(PlayerQuitEvent event) {
		if (this.main.getConfigManager().sendJoinAndQuitMessageToOP) {
			if (this.main.getServer().getOnlinePlayers().size() > 1) {
				for (Player opPlayers : this.main.getServer().getOnlinePlayers()) {
					if (!opPlayers.isOp()) {
						continue;
					}
					opPlayers.sendMessage(event.getQuitMessage());
				}
			}
		}
		event.setQuitMessage(null);
		final UUID playerUUID = event.getPlayer().getUniqueId();
		if (this.main.getQueueManager().getQueueMap().containsKey(playerUUID)) {
			this.main.getQueueManager().getQueueMap().remove(playerUUID);
			if (this.main.getQueueManager().getLastUpdatedSet().contains(playerUUID)) {
				this.main.getQueueManager().getLastUpdatedSet().remove(playerUUID);
			}
			for (int i = 0; i != 2; i++) {
				Main.getInstance().getInventoryManager().updateQueueInventory(BooleanUtils.toBoolean(i));
			}
		}
		final PlayerManager pm = PlayerManager.get(playerUUID);
        if (this.main.getPartyManager().hasParty(playerUUID)) {
        	final Party party = this.main.getPartyManager().getParty(playerUUID);
            if (party.getLeader().equals(playerUUID)) {
            	this.main.getPartyManager().transferLeader(playerUUID);
            } else {
            	this.main.getPartyManager().leaveParty(playerUUID);
            }
        }
		if (pm.getStatus() == PlayerStatus.SPECTATE && pm.getSpectate() == null) {
			for (Arenas allArenas : Arena.getInstance().getArenaList().values()) {
				if (!allArenas.getAllSpectators().contains(playerUUID)) continue;
				allArenas.removeSpectator(playerUUID);
			}
		}
		if ((pm.getStatus() == PlayerStatus.DUEL || pm.getStatus() == PlayerStatus.WAITING)) {
			this.main.getDuelManager().removePlayerFromDuel(this.main.getServer().getPlayer(playerUUID), RemoveReason.DISCONNECTED); // TODO: FIX A BUG WHERE'S fist/secondTeamPartyLeaderUUID is not changed if the party leader has deconnected -> is it fixed?
		}
		this.main.getDatabaseUtil().savePlayer(pm);
	}
	
	@EventHandler(priority=EventPriority.HIGHEST)
	public void onGettingKicked(PlayerKickEvent event) {
		onQuit(new PlayerQuitEvent(event.getPlayer(), event.getLeaveMessage()));
		event.setLeaveMessage(null);
	}
	
	@EventHandler(priority=EventPriority.HIGH)
	public void onPlayerDeath(PlayerDeathEvent event) {
		event.setDeathMessage(null);
		event.setDroppedExp(0);
		
		if (event.getEntity() instanceof Player) {
			final Player killed = event.getEntity();
			this.main.getDuelManager().removePlayerFromDuel(killed, RemoveReason.KILLED);
			// TODO: Work on a better autorespawn
			new BukkitRunnable() {
				
				@Override
				public void run() {
					if (killed.isDead()) {
						killed.spigot().respawn();
					}
				}
			}.runTaskLater(this.main, 45L);
		}
	}
	
	@EventHandler
	public void onRespawn(PlayerRespawnEvent event) {
		final Player player = event.getPlayer();
		event.setRespawnLocation(player.getWorld().getSpawnLocation());
		
		if (this.main.getDuelManager().getDuelFromPlayerUUID(player.getUniqueId()) == null) {
			final PlayerManager pm = PlayerManager.get(player.getUniqueId());
			pm.heal(false);
			pm.showAllPlayer();
			this.main.getItemManager().giveSpawnItem(player);
		}
	}
	
	@EventHandler(priority=EventPriority.LOWEST)
	public void onDamage(EntityDamageEvent event) {
		if (event.getEntity() instanceof Player) {
			final Player player = (Player) event.getEntity();
			final PlayerManager pm = PlayerManager.get(player.getUniqueId());
			
			if (pm.getStatus() == PlayerStatus.SPECTATE) {
				event.setCancelled(true);
				return;
			}
			if (pm.getStatus() == PlayerStatus.SPAWN || pm.getStatus() == PlayerStatus.QUEUE || pm.getStatus() == PlayerStatus.BRIDGE) {
				switch (event.getCause()) {
				case VOID:
					event.setCancelled(true);
					if (pm.getStatus() != PlayerStatus.BRIDGE) {
						player.teleport(player.getWorld().getSpawnLocation());
						break;
					}
					player.setNoDamageTicks(50);
					this.main.getItemManager().giveBridgeItems(player);
					player.teleport(Warps.BRIDGE.getLobbyLocation());
					break;
				case FALL:
					event.setCancelled(true);
					break;
				case BLOCK_EXPLOSION:
					event.setDamage(0.0D);
					break;
				case FIRE:
					event.setCancelled(true);
					break;
				case DROWNING:
					event.setCancelled(true);
					break;
				case CONTACT:
					event.setCancelled(true);
					break;
				case LAVA:
					event.setCancelled(true);
					break;
				default:
					break;
				}
			}
		}
	}
	
	@EventHandler(priority=EventPriority.LOWEST)
	public void onEntityDamage(EntityDamageByEntityEvent event) {
		if (event.getEntity() instanceof Player && event.getDamager() instanceof Player) {
			final Player attacker = (Player) event.getDamager();
			final PlayerManager attackerManager = PlayerManager.get(attacker.getUniqueId());	
			
			if (attackerManager.getStatus() == PlayerStatus.MODERATION || attackerManager.getStatus() == PlayerStatus.BRIDGE) {
				if (attacker.getNoDamageTicks() > 0) {
					event.setCancelled(true);
					return;
				}
				event.setDamage(0.0D);
				return;
			}
			final Player attacked = (Player) event.getEntity();
			if (attackerManager.getStatus() == PlayerStatus.SPECTATE || attackerManager.getStatus() != PlayerStatus.DUEL && PlayerManager.get(attacked.getUniqueId()).getStatus() != PlayerStatus.DUEL) {
				event.setCancelled(true);
				return;
			}
			if (attackerManager.getStatus() == PlayerStatus.DUEL) {
				Duel currentDuel = this.main.getDuelManager().getDuelFromPlayerUUID(attacker.getUniqueId());
				
				if (currentDuel == null) {
					return;
				}
				double damage = event.getDamage();
				if (currentDuel.getArena().isSumo() || currentDuel.getLadder() == Ladders.BOXING) {
					damage = 0.0D;
				}
				if (currentDuel.getLadder() == Ladders.EARLY_HG) {
					ItemStack handItem = attacker.getItemInHand();
					if (handItem.getType() == Material.MUSHROOM_SOUP) {
						return;
					}
					if (handItem.getType() == Material.STONE_SWORD) damage -= 2.25D; // Fix too many damage (from my kitpvp soup plugin)
				}
				event.setDamage(damage);
			}
		}
	}
	
	@EventHandler(priority=EventPriority.LOWEST)
	public void onReceiveDrop(PlayerPickupItemEvent event) {
		if (event.getItem().getOwner() instanceof Player) {
			final Player receiver = event.getPlayer();
			final PlayerManager pm = PlayerManager.get(receiver.getUniqueId());
			
			if (pm.getStatus() != PlayerStatus.DUEL && pm.getStatus() != PlayerStatus.WAITING && !pm.isAllowedToBuild()) {
				event.setCancelled(true);
				return;
			}
			final Item item = event.getItem();
			final Player owner = (Player) item.getOwner();
			if (this.main.getDuelManager().getDuelFromPlayerUUID(receiver.getUniqueId()) != null) {
				final Duel currentDuel = this.main.getDuelManager().getDuelFromPlayerUUID(receiver.getUniqueId());
				
				if (!currentDuel.containPlayer(owner) && !currentDuel.containDrops(item)) {
					event.setCancelled(true);
					return;
				}
				currentDuel.removeDrops(item);
				return;
			}
			if (!receiver.canSee(owner)) event.setCancelled(true);
		}
	}
	
	@EventHandler(priority=EventPriority.LOWEST)
	public void onDrop(PlayerDropItemEvent event) {
		final PlayerManager pm = PlayerManager.get(event.getPlayer().getUniqueId());
		if (event.getPlayer().getGameMode() != GameMode.CREATIVE && pm.getStatus() != PlayerStatus.WAITING && pm.getStatus() != PlayerStatus.DUEL && !pm.isAllowedToBuild()) {
			event.setCancelled(true);
			return;
		}
		final ItemStack item = event.getItemDrop().getItemStack();
		if (item.getType() == Material.GLASS_BOTTLE || item.getType() == Material.BOWL) {
			event.getItemDrop().remove();
			return;
		}
		if (pm.getStatus() == PlayerStatus.WAITING || pm.getStatus() == PlayerStatus.DUEL) {
			if (item.getType() == Material.ENCHANTED_BOOK) {
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler(priority=EventPriority.LOWEST)
	public void onClickItem(PlayerInteractEvent event) {
		if (!event.hasItem()) {
			return;
		}
		final Player player = event.getPlayer();
        if (player.getInventory().getItemInHand() == null || !player.getInventory().getItemInHand().hasItemMeta() || !player.getInventory().getItemInHand().getItemMeta().hasDisplayName()) {
            return;
        }
        if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK) || event.getAction().equals(Action.RIGHT_CLICK_AIR)) {
        	final ItemStack item = player.getItemInHand();
        	final PlayerManager pm = PlayerManager.get(player.getUniqueId());
        	final String itemName = item.getItemMeta().getDisplayName().toLowerCase();
        	
        	switch (pm.getStatus()) {
			case SPAWN:
				if (!this.main.getPartyManager().hasParty(player.getUniqueId())) {
					if (item.getType() == Material.IRON_SWORD && itemName.equals(ChatColor.YELLOW + "unranked queue")) {
		                event.setCancelled(true);
		                player.openInventory(this.main.getInventoryManager().getUnrankedInventory());
		                break;
		            }
					if (item.getType() == Material.DIAMOND_SWORD && itemName.equals(ChatColor.YELLOW + "ranked queue")) {
		                event.setCancelled(true);
		                player.openInventory(this.main.getInventoryManager().getRankedInventory());
		                break;
		            }
					if (item.getType() == Material.NAME_TAG && itemName.equals(ChatColor.YELLOW + "create party")) {
		                event.setCancelled(true);
		                this.main.getServer().dispatchCommand(player, "party create");
		                break;
		            }
					if (item.getType() == Material.GOLD_AXE && itemName.equals(ChatColor.YELLOW + "mini-game")) {
						player.sendMessage(ChatColor.GOLD + "Successfully teleported to the Bridge game (because it's the only one ^^')");
						player.teleport(Warps.BRIDGE.getLobbyLocation());
						pm.setStatus(PlayerStatus.BRIDGE);
						this.main.getItemManager().giveBridgeItems(player);
						player.setNoDamageTicks(50);
						break;
					}
					if (item.getType() == Material.BOOK && itemName.equals(ChatColor.YELLOW + "kit creator/settings")) {
						player.openInventory(this.main.getInventoryManager().getSelectionInventory());
						break;
					}
					break;
				}
				final Party currentParty = this.main.getPartyManager().getParty(player.getUniqueId());
				final boolean isPartyLeader = currentParty.getLeader() == player.getUniqueId();
					
				if (item.getType() == Material.IRON_AXE && itemName.equals(ChatColor.YELLOW + "2v2 unranked queue")) {
					event.setCancelled(true);
					if (!isPartyLeader) {
						player.sendMessage(ChatColor.RED + "You are not the leader of this party!");
						break;
					}
					if (currentParty.getPartyState() == PartyState.DUELING) {
						player.sendMessage(ChatColor.RED + "Your party is currently busy and cannot fight.");
						break;
					}
					if (currentParty.getMembers().isEmpty() || currentParty.getSize() > 2) {
						player.sendMessage(ChatColor.RED + "There must be at least 2 players in your party to do this.");
						break;
					}
					player.openInventory(this.main.getInventoryManager().getUnrankedInventory());
					break;
				}
				if (item.getType() == Material.DIAMOND_AXE && itemName.equals(ChatColor.YELLOW + "2v2 ranked queue")) {
					event.setCancelled(true);
					if (!isPartyLeader) {
						player.sendMessage(ChatColor.RED + "You are not the leader of this party!");
						break;
					}
					if (currentParty.getPartyState() == PartyState.DUELING) {
						player.sendMessage(ChatColor.RED + "Your party is currently busy and cannot fight.");
						break;
					}
					if (currentParty.getMembers().isEmpty() || currentParty.getSize() > 2) {
						player.sendMessage(ChatColor.RED + "There must be at least 2 players in your party to do this.");
						break;
					}
					player.openInventory(this.main.getInventoryManager().getRankedInventory());
					break;
				}
				if (item.getType() == Material.GOLD_HOE && itemName.equals(ChatColor.YELLOW + "party game")) {
					if (!isPartyLeader) {
						player.sendMessage(ChatColor.RED + "You are not the leader of this party!");
						break;
					}
					if (currentParty.getPartyState() == PartyState.DUELING) {
						player.sendMessage(ChatColor.RED + "Your party is currently busy and cannot fight.");
						break;
					}
					if (currentParty.getMembers().isEmpty()) {
						player.sendMessage(ChatColor.RED + "There must be at least 2 players in your party to do this.");
						break;
					}
					player.openInventory(this.main.getInventoryManager().getLaddersInventory());
					break;
				}
				if (item.getType() == Material.BOOK && itemName.equals(ChatColor.YELLOW + "fight other parties")) {
					player.openInventory(this.main.getPartyManager().getPartiesInventory());
					break;
				}
				if (item.getType() == Material.PAPER && itemName.equals(ChatColor.YELLOW + "party information")) {
					this.main.getServer().dispatchCommand(player, "party info");
					break;
				}
				if (item.getType() == Material.EYE_OF_ENDER && itemName.equals(ChatColor.YELLOW + "spectate actual match")) {
					// TODO: SUPPOSED TO BE FIXED -> DOESNT WORK MAKE IT WORK
					event.setUseItemInHand(Result.DENY);
					if (currentParty.getPartyState() != PartyState.DUELING) {
						player.getItemInHand().setType(null);
						player.updateInventory();
						break;
					}
					Duel duel = null;
					for (UUID uuid : currentParty.getMembersIncludingLeader()) {
						final PlayerManager um = PlayerManager.get(uuid);
						if (um.getStatus() != PlayerStatus.WAITING && um.getStatus() != PlayerStatus.DUEL) continue;
						duel = this.main.getDuelManager().getDuelFromPlayerUUID(uuid);
						player.setScoreboard(this.main.getServer().getPlayer(uuid).getScoreboard());
						break;
					}
					if (duel == null) {
						player.sendMessage(ChatColor.RED + "No duel found!");
						player.getItemInHand().setType(null);
						player.updateInventory();
						return;
					}
					pm.hideAllPlayer();
					duel.addSpectator(player.getUniqueId());
						
					player.setAllowFlight(true);
					player.setFlying(true);
					player.teleport(duel.getArena().getLocations()[0].add(0, 2, 0));
						
					final List<UUID> duelPlayers = Lists.newArrayList(duel.getSimpleDuel().firstTeamAlive);
					duelPlayers.addAll(duel.getSimpleDuel().secondTeamAlive);
							
					for (UUID uuid : duelPlayers) {
						Player dplayers = this.main.getServer().getPlayer(uuid);
						player.showPlayer(dplayers);
					}
					Main.getInstance().getItemManager().giveSpectatorItems(player);
					player.sendMessage(ChatColor.GREEN + "You are now spectating the current party duel");
					duel.sendMessage(ChatColor.YELLOW + player.getName() + ChatColor.DARK_AQUA + " is now spectating.");
					break;
				}
				if (item.getType() == Material.REDSTONE && itemName.equals(ChatColor.RED + "leave party")) {
					event.setCancelled(true);
					this.main.getServer().dispatchCommand(player, "party leave");
				}
				break;
			case QUEUE:
				if (item.getType() == Material.REDSTONE && itemName.equals(ChatColor.RED + "leave queue")) {
	                event.setCancelled(true);
	                this.main.getQueueManager().quitQueue(player);
	            }
				break;
			case WAITING:
				if (item.getType() == Material.ENCHANTED_BOOK && itemName.contains("default kit")) {
					this.giveFightItems(player, item.getItemMeta().getDisplayName());
					break;
	            }
				break;
			case DUEL:
				if (item.getType() == Material.ENCHANTED_BOOK && itemName.contains("default kit")) {
					this.giveFightItems(player, item.getItemMeta().getDisplayName());
	            }
				break;
			case SPECTATE:
				event.setCancelled(true);
				if (item.getType() == Material.REDSTONE && itemName.equals(ChatColor.RED + "leave spectate")) {
	                if (pm.getSpectate() != null) {
		                final Player spectatePlayer = pm.getSpectate();
		                final Duel spectatedDuel = this.main.getDuelManager().getDuelFromPlayerUUID(spectatePlayer.getUniqueId());
		                
		                if (spectatedDuel != null) {
		                	spectatedDuel.sendMessage(ChatColor.YELLOW + player.getName() + ChatColor.DARK_AQUA + " is no longer spectating.");
		                	spectatedDuel.removeSpectator(player.getUniqueId());
		                }
		                pm.setSpectate(null);
	                } else {
	                	for (Arenas allArenas : Arena.getInstance().getArenaList().values()) {
	        				if (!allArenas.getAllSpectators().contains(player.getUniqueId())) continue;
	        				allArenas.removeSpectator(player.getUniqueId());
	        			}
	                }
	                player.setFlySpeed(0.1f);
	        		player.setWalkSpeed(0.2f);
	        		player.setAllowFlight(false);
	        		player.setFlying(false);
	        		pm.setStatus(PlayerStatus.SPAWN);
	        		pm.showAllPlayer();
	        		player.teleport(player.getWorld().getSpawnLocation());
	        		player.setScoreboard(Main.getInstance().getServer().getScoreboardManager().getNewScoreboard());
	        		this.main.getItemManager().giveSpawnItem(player);
	        		break;
	            }
				if (item.getType() == Material.WATCH && itemName.equals(ChatColor.GREEN + "see current arena")) {
					event.setUseItemInHand(Result.DENY);
	                final Player spectatePlayer = pm.getSpectate();
	                final Duel spectatedDuel = this.main.getDuelManager().getDuelFromPlayerUUID(spectatePlayer.getUniqueId());
	                final Arenas currentArena = spectatedDuel.getArena();
	                
	                if (spectatedDuel != null) {
	                	spectatedDuel.sendMessage(ChatColor.YELLOW + player.getName() + ChatColor.DARK_AQUA + " is no longer spectating.");
	                	spectatedDuel.removeSpectator(player.getUniqueId());
	                	pm.setSpectate(null);
	                }
	                final List <UUID> playersInArena = Lists.newArrayList();
	                for (Duel duel : this.main.getDuelManager().getAllDuels()) {
	                	if (currentArena != duel.getArena()) continue;
	                	playersInArena.addAll(duel.getAllAliveTeams());
	                }
	                pm.hideAllPlayer(); // TODO: When see all spectators will be done; dont hide spectators
	                currentArena.addSpectator(player.getUniqueId());
	                if (!playersInArena.isEmpty()) {
		                for (UUID playerInArenaUUID : playersInArena) {
		                	Player playerInArena = this.main.getServer().getPlayer(playerInArenaUUID);
		                	player.showPlayer(playerInArena);
		                }
	                }
	                this.main.getItemManager().giveSpectatorItems(player);
	                playersInArena.clear();
	                break;
	            }
				if (item.getType() == Material.EYE_OF_ENDER && itemName.equals(ChatColor.GREEN + "see all spectators")) {
					// TODO
					player.sendMessage(ChatColor.RED + "Coming ASAP");
					break;
				}
				if (item.getType() == Material.MAP && itemName.equals(ChatColor.GREEN + "change arena")) {
					player.openInventory(this.main.getInventoryManager().getArenasInventory());
					break;
				}
				if (item.getType() == Material.WOOL && itemName.equals(ChatColor.GREEN + "change fly/walk speed")) {
					float speed = 0.0f;
					if (player.isOnGround()) {
						speed = player.getWalkSpeed();
						if (speed == 1.0f) {
							player.setWalkSpeed(0.2f);
							player.sendMessage(ChatColor.GREEN + "Walk speed has been reset");
							break;
						}
						speed += 0.2f;
						player.setWalkSpeed(speed);
						player.sendMessage(ChatColor.GREEN + "Walk speed x" + ((speed / 2) * 10));
						break;
					}
					speed = player.getFlySpeed();
					if (speed == 0.5f) {
						player.setFlySpeed(0.1f);
						player.sendMessage(ChatColor.GREEN + "Fly speed has been reset");
						break;
					}
					speed += 0.1f;
					player.setFlySpeed(speed);
					player.sendMessage(ChatColor.GREEN + "Fly speed x" + (speed * 10));
					break;
				}
				break;
			case MODERATION:
				if (item.getType() == Material.REDSTONE && itemName.equals(ChatColor.RED + "leave moderation")) {
	                event.setCancelled(true);
	                this.main.getServer().dispatchCommand(player, "mod");
	                break;
	            }
				if (item.getType() == Material.WATCH && itemName.equals(ChatColor.RED + "see random player")) {
	                event.setCancelled(true);
	                List<Player> online = Lists.newArrayList(this.main.getServer().getOnlinePlayers());
	                if (online.size() <= 1) {
	                	return;
	                }
	                Collections.shuffle(online);
	                Player tooked = null;
	                for (Player onlinePlayers : online) {
	                	if (onlinePlayers == player) continue;
	                	
	                	final PlayerManager om = PlayerManager.get(onlinePlayers.getUniqueId());
	                	if (om.getStatus() == PlayerStatus.MODERATION || om.getStatus() == PlayerStatus.SPAWN || om.getStatus() == PlayerStatus.QUEUE) continue;
	                	
	                	tooked = onlinePlayers;
	                	break;
	                }
	                if (tooked == null) {
	                	player.sendMessage(ChatColor.RED + "No player to agree.");
	                	return;
	                }
	                player.teleport(tooked.getLocation().clone().add(0, 2, 0));
	                player.sendMessage(ChatColor.GREEN + "You've been teleported to " + tooked.getName());
	                online.clear();
	            }
				break;
			default:
				break;
			}
        }
	}
	
	private void giveFightItems(Player player, String name) {
		player.getInventory().clear();
		final String[] ladderName = ChatColor.stripColor(name).split(" ");
		this.main.getItemManager().giveFightItems(player, Ladders.getLadderFromName(ladderName[0]));
        player.sendMessage(ChatColor.GREEN.toString() + ladderName[0] + " kit successfully given.");
	}
	
	@EventHandler(priority=EventPriority.LOWEST)
	public void onInteractWithBlock(PlayerInteractEvent event) {
		if (!event.hasBlock()) { // TODO: check if good
			return;
		}
		if (event.getClickedBlock() != null && (event.getClickedBlock().getType() == Material.SIGN_POST || event.getClickedBlock().getType() == Material.SIGN || (event.getClickedBlock().getType() == Material.WALL_SIGN && event.getAction() == Action.RIGHT_CLICK_BLOCK))) {
			final Sign sign = (Sign)event.getClickedBlock().getState();
			if (sign.getLine(0).equalsIgnoreCase("-*-") && sign.getLine(1).equalsIgnoreCase("Back to spawn") && sign.getLine(2).equalsIgnoreCase("-*-")) {
				event.setCancelled(true);
				final Player player = event.getPlayer();
				if (player.isSneaking()) {
					return;
				}
				PlayerManager.get(player.getUniqueId()).setStatus(PlayerStatus.SPAWN);
				player.teleport(player.getWorld().getSpawnLocation());
				this.main.getItemManager().giveSpawnItem(player);
			}
		}
	}
	
	@EventHandler
	public void onPlayerInteractSoup(PlayerInteractEvent event) {
		if (!event.hasItem()) {
			return;
		}
		if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			final Player player = event.getPlayer();
			if (!player.isDead() && player.getItemInHand().getType() == Material.MUSHROOM_SOUP && player.getHealth() < player.getMaxHealth()) {
				final double newHealth = Math.min(player.getHealth() + 7.0D, player.getMaxHealth());
				player.setHealth(newHealth);
				player.getItemInHand().setType(Material.BOWL);
				player.updateInventory();
			} 
		} 
	}
	
	@EventHandler(priority=EventPriority.LOW)
	public void onClickPlayer(PlayerInteractEntityEvent event) {
		if (event.getRightClicked() instanceof Player) {
			final Player player = event.getPlayer();
			final PlayerManager pm = PlayerManager.get(player.getUniqueId());
	      
			if (pm.getStatus() != PlayerStatus.MODERATION || player.getItemInHand().getItemMeta() == null || player.getItemInHand().getItemMeta().getDisplayName() == null) {
				return;
			}
			final Player target = (Player)event.getRightClicked();
			if (player.getItemInHand().getType() == Material.BOOK && player.getItemInHand().getItemMeta().getDisplayName().toLowerCase().equals(ChatColor.RED + "inspection tool")) {
				this.main.getServer().dispatchCommand(player, "inspect " + target.getName());
				return;
			}
			if (player.getItemInHand().getType() == Material.PACKED_ICE && player.getItemInHand().getItemMeta().getDisplayName().toLowerCase().equals(ChatColor.RED + "freeze someone")) {
				this.main.getServer().dispatchCommand(player, "freeze " + target.getName());
			}
		} 
	}
	
	@EventHandler(priority=EventPriority.LOW)
	public void onFeed(FoodLevelChangeEvent event) {
		if (event.getEntity() instanceof Player) {
			final Player player = (Player) event.getEntity();
			final PlayerManager pm = PlayerManager.get(player.getUniqueId());
			
			if (pm.getStatus() == PlayerStatus.WAITING || pm.getStatus() == PlayerStatus.DUEL) {
				if (this.main.getDuelManager().getDuelFromPlayerUUID(player.getUniqueId()) != null) {
					Duel duel = this.main.getDuelManager().getDuelFromPlayerUUID(player.getUniqueId());
					
					if (!duel.getLadder().needFood()) {
						event.setCancelled(true);
					}
				}
				return;
			}
			event.setCancelled(true);
		}
	}
}
