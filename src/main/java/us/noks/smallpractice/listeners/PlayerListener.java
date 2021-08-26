package us.noks.smallpractice.listeners;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
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
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import com.google.common.collect.Lists;

import ru.tehkode.permissions.bukkit.PermissionsEx;
import us.noks.smallpractice.Main;
import us.noks.smallpractice.arena.Arena;
import us.noks.smallpractice.arena.Arena.Arenas;
import us.noks.smallpractice.enums.Ladders;
import us.noks.smallpractice.enums.PlayerStatus;
import us.noks.smallpractice.enums.RemoveReason;
import us.noks.smallpractice.enums.Warps;
import us.noks.smallpractice.objects.Duel;
import us.noks.smallpractice.objects.managers.PlayerManager;
import us.noks.smallpractice.party.Party;
import us.noks.smallpractice.party.PartyState;
import us.noks.smallpractice.utils.WebUtil;

public class PlayerListener implements Listener {
	private Main main;
	public PlayerListener(Main plugin) {
		this.main = plugin;
	    this.main.getServer().getPluginManager().registerEvents(this, this.main);
	}
	
	@EventHandler(priority=EventPriority.HIGH)
	public void onJoin(PlayerJoinEvent event) {
		event.setJoinMessage(null);
		final Player player = event.getPlayer();
		
		this.checkLike(player);
		PlayerManager.create(player.getUniqueId());
		
		player.setExp(0.0F);
		player.setLevel(0);
		player.setFlySpeed(0.1f);
		player.setWalkSpeed(0.2f);
		
		PlayerManager.get(player.getUniqueId()).heal(false);
		player.setAllowFlight(false);
		player.setFlying(false);
		player.setGameMode(GameMode.SURVIVAL);
		
		player.setScoreboard(this.main.getServer().getScoreboardManager().getNewScoreboard());
		
		player.teleport(player.getWorld().getSpawnLocation());
		this.main.getItemManager().giveSpawnItem(player);
		
		this.sendJoinMessage(event);
		
		for (Player allPlayers : Bukkit.getOnlinePlayers()) {
			final PlayerManager pmAll = PlayerManager.get(allPlayers.getUniqueId());
			if (pmAll.getStatus() == PlayerStatus.WAITING || pmAll.getStatus() == PlayerStatus.DUEL || pmAll.getStatus() == PlayerStatus.MODERATION) {
				player.hidePlayer(allPlayers);
			}
		}
	}
	
	private void checkLike(Player player) {
		if (!this.main.isPermissionsPluginHere()) {
			return;
		}
		final String rank = PermissionsEx.getPermissionManager().getUser(player).getParentIdentifiers().get(0);
		final String domainName = this.main.getConfigManager().serverDomainName;
		
        if (rank.equals("default") || rank.equals("verified")) {
            WebUtil.getResponse(this.main, "https://api.namemc.com/server/" + domainName + "/votes?profile=" + player.getUniqueId(), response -> {
            	switch (response) {
            	case "false":
            		if (rank.equals("verified")) {
            			PermissionsEx.getPermissionManager().getUser(player).removeGroup("verified");
            			PermissionsEx.getPermissionManager().getUser(player).addGroup("default");
            			player.sendMessage(ChatColor.RED + "Your rank has been removed due to your unlike!");
            		}
            		break;
            	case "true":
            		if (rank.equals("default")) {
            			PermissionsEx.getPermissionManager().getUser(player).removeGroup("default");
            			PermissionsEx.getPermissionManager().getUser(player).addGroup("verified");
            			player.sendMessage(ChatColor.GREEN + "Thanks for liking the server on NameMC! You've now got the Verified rank.");
            		}
            		break;
            	}
            });
        }
	}
	
	private void sendJoinMessage(PlayerJoinEvent event) {
		final Player player = event.getPlayer();
		if (this.main.getConfigManager().clearChatOnJoin) {
			for (int i = 0; i < 100; i++) {
				player.sendMessage(""); 
			}
		}
		player.sendMessage(ChatColor.DARK_AQUA + "Welcome back on " + ChatColor.YELLOW + "Goneko" + ChatColor.GRAY + " (Practice)");
		player.sendMessage("");
		player.sendMessage(ChatColor.GRAY + "-> " + ChatColor.DARK_AQUA + "Discord: " + ChatColor.GRAY + "https://discord.gg/8v8Mzhd");
		player.sendMessage(ChatColor.GRAY + "-> " + ChatColor.DARK_AQUA + "NameMC: " + ChatColor.GRAY + "https://namemc.com/server/" + this.main.getConfigManager().serverDomainName);
		player.sendMessage("");
		player.setPlayerListName(PlayerManager.get(player.getUniqueId()).getPrefixColors() + player.getName());
	}
	
	@EventHandler(priority=EventPriority.HIGH)
	public void onQuit(PlayerQuitEvent event) {
		event.setQuitMessage(null);
		final Player player = event.getPlayer();
		if (this.main.getQueueManager().getQueueMap().containsKey(player.getUniqueId())) {
			this.main.getQueueManager().getQueueMap().remove(player.getUniqueId());
			this.main.getInventoryManager().updateUnrankedInventory();
			this.main.getInventoryManager().updateRankedInventory();
		}
		PlayerManager pm = PlayerManager.get(player.getUniqueId());
        if (this.main.getPartyManager().hasParty(player.getUniqueId())) {
        	final Party party = this.main.getPartyManager().getParty(player.getUniqueId());
            if (party.getLeader().equals(player.getUniqueId())) {
            	this.main.getPartyManager().transferLeader(player.getUniqueId());
            } else {
            	this.main.getPartyManager().leaveParty(player.getUniqueId());
            }
        }
		if (pm.getStatus() == PlayerStatus.SPECTATE && pm.getSpectate() == null) {
			for (Arenas allArenas : Arena.getInstance().getArenaList().values()) {
				if (!allArenas.getAllSpectators().contains(player.getUniqueId())) continue;
				allArenas.removeSpectator(player.getUniqueId());
			}
		}
		if ((pm.getStatus() == PlayerStatus.DUEL || pm.getStatus() == PlayerStatus.WAITING)) {
			this.main.getDuelManager().removePlayerFromDuel(player, RemoveReason.DISCONNECTED); // TODO: FIX A BUG WHERE'S fist/secondTeamPartyLeaderUUID is not changed if the party leader has deconnected
		}
		pm.remove();
	}
	
	@EventHandler(priority=EventPriority.HIGH)
	public void onDeath(PlayerDeathEvent event) {
		event.setDeathMessage(null);
		event.getDrops().clear();
		event.setDroppedExp(0);
		
		if (event.getEntity() instanceof Player) {
			final Player killed = event.getEntity();
			this.main.getDuelManager().removePlayerFromDuel(killed, RemoveReason.KILLED);
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
				case FALL:
					event.setCancelled(true);
					break;
				case VOID:
					event.setCancelled(true);
					if (pm.getStatus() != PlayerStatus.BRIDGE) {
						player.teleport(player.getWorld().getSpawnLocation());
						break;
					}
					player.setNoDamageTicks(50);
					event.setCancelled(true);
					this.main.getItemManager().giveBridgeItems(player);
					player.teleport(Warps.BRIDGE.getLobbyLocation());
					break;
				case BLOCK_EXPLOSION:
					event.setDamage(0.0D);
					break;
				case FIRE:
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
					if (handItem.getType() == Material.STONE_SWORD) damage -= 2.0D; // Fix too many damage (from my kitpvp soup plugin)
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
			Item item = event.getItem();
			final Player owner = (Player) item.getOwner();
			if (this.main.getDuelManager().getDuelFromPlayerUUID(receiver.getUniqueId()) != null) {
				final Duel currentDuel = this.main.getDuelManager().getDuelFromPlayerUUID(receiver.getUniqueId());
				
				if (!currentDuel.containPlayer(owner) && !currentDuel.containDrops(item)) event.setCancelled(true); // TODO: Does containPlayer can affect the event? | Does the containDrop will affect all the same drops?
				return;
			}
			if (!receiver.canSee(owner)) event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onEntitySpawnInWorld(EntitySpawnEvent event) {
		if (event.getEntity() instanceof Item) {
			Item itemDropped = (Item) event.getEntity();
			
			if (itemDropped.getOwner() != null && itemDropped.getOwner() instanceof Player) {
				UUID playerUUID = itemDropped.getOwner().getUniqueId();
				Duel duel = this.main.getDuelManager().getDuelFromPlayerUUID(playerUUID);
				
				if (duel == null) {
					return;
				}
				
				duel.addDrops(itemDropped);
			}
		}
	}
	
	@EventHandler(priority=EventPriority.LOWEST)
	public void onDrop(PlayerDropItemEvent event) {
		PlayerManager pm = PlayerManager.get(event.getPlayer().getUniqueId());
		if (event.getPlayer().getGameMode() != GameMode.CREATIVE && pm.getStatus() != PlayerStatus.WAITING && pm.getStatus() != PlayerStatus.DUEL && !pm.isAllowedToBuild()) {
			event.setCancelled(true);
			return;
		}
		ItemStack item = event.getItemDrop().getItemStack();
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
		                Bukkit.dispatchCommand(player, "party create");
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
				} else {
					final Party currentParty = this.main.getPartyManager().getParty(player.getUniqueId());
					final boolean isPartyLeader = currentParty.getLeader() == player.getUniqueId();
					
					if (item.getType() == Material.IRON_SWORD && itemName.equals(ChatColor.YELLOW + "2v2 unranked queue")) {
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
					if (item.getType() == Material.DIAMOND_SWORD && itemName.equals(ChatColor.YELLOW + "2v2 ranked queue")) {
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
					if (item.getType() == Material.ARROW && itemName.equals(ChatColor.YELLOW + "split teams")) {
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
						Bukkit.dispatchCommand(player, "party info");
						break;
					}
					if (item.getType() == Material.EYE_OF_ENDER && itemName.equals(ChatColor.YELLOW + "spectate actual match")) {
						event.setUseItemInHand(Result.DENY);
						if (currentParty.getPartyState() != PartyState.DUELING) {
							player.getItemInHand().setType(null);
							player.updateInventory();
							break;
						}
						Duel duel = this.main.getDuelManager().getDuelFromPlayerUUID(currentParty.getLeader());
						pm.hideAllPlayer();
						duel.addSpectator(player.getUniqueId());
						
						player.setAllowFlight(true);
						player.setFlying(true);
						player.teleport(duel.getArena().getLocations()[0].add(0, 2, 0));
						
						List<UUID> duelPlayers = Lists.newArrayList(duel.getFirstTeamAlive());
						duelPlayers.addAll(duel.getSecondTeamAlive());
							
						for (UUID uuid : duelPlayers) {
							Player dplayers = Bukkit.getPlayer(uuid);
							player.showPlayer(dplayers);
						}
						Main.getInstance().getItemManager().giveSpectatorItems(player);
						player.sendMessage(ChatColor.GREEN + "You are now spectating the current party duel");
						duel.sendMessage(ChatColor.YELLOW + player.getName() + ChatColor.DARK_AQUA + " is now spectating.");
						break;
					}
					if (item.getType() == Material.REDSTONE && itemName.equals(ChatColor.RED + "leave party")) {
						event.setCancelled(true);
						Bukkit.dispatchCommand(player, "party leave");
					}
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
	        		this.main.getItemManager().giveSpawnItem(player);
	        		break;
	            }
				if (item.getType() == Material.WATCH && itemName.equals(ChatColor.GREEN + "see current arena")) {
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
	                	playersInArena.addAll(duel.getFirstAndSecondTeamsAlive());
	                }
	                pm.hideAllPlayer(); // TODO: When see all spectators will be done; dont hide spectators
	                currentArena.addSpectator(player.getUniqueId());
	                if (!playersInArena.isEmpty()) {
		                for (UUID playerInArenaUUID : playersInArena) {
		                	Player playerInArena = Bukkit.getPlayer(playerInArenaUUID);
		                	player.showPlayer(playerInArena);
		                }
	                }
	                this.main.getItemManager().giveSpectatorItems(player);
	                playersInArena.clear();
	                break;
	            }
				if (item.getType() == Material.EYE_OF_ENDER && itemName.equals(ChatColor.GREEN + "see all spectators")) {
					// TODO
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
	                Bukkit.dispatchCommand(player, "mod");
	                break;
	            }
				if (item.getType() == Material.WATCH && itemName.equals(ChatColor.RED + "see random player")) {
	                event.setCancelled(true);
	                List<Player> online = Lists.newArrayList();
	                
	                for (Player onlinePlayers : Bukkit.getOnlinePlayers()) {
	                	if (onlinePlayers == player) continue;
	                	
	                	final PlayerManager om = PlayerManager.get(onlinePlayers.getUniqueId());
	                	if (om.getStatus() == PlayerStatus.MODERATION || om.getStatus() == PlayerStatus.SPAWN || om.getStatus() == PlayerStatus.QUEUE) continue;
	                	
	                	online.add(onlinePlayers);
	                }
	                if (online.isEmpty()) {
	                	player.sendMessage(ChatColor.RED + "No player to agree.");
	                	return;
	                }
	                Collections.shuffle(online);
	                final Player tooked = online.get(0);
	                
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
		String itemName = ChatColor.stripColor(name);
		String[] ladderName = itemName.split(" ");
		this.main.getItemManager().giveFightItems(player, Ladders.getLadderFromName(ladderName[0]));
        player.sendMessage(ChatColor.GREEN.toString() + ladderName[0] + " kit successfully given.");
	}
	
	@EventHandler
	public void onInteractWithBlock(PlayerInteractEvent event) {
		if (event.getClickedBlock() != null && (event.getClickedBlock().getType() == Material.SIGN_POST || event.getClickedBlock().getType() == Material.SIGN || (event.getClickedBlock().getType() == Material.WALL_SIGN && event.getAction() == Action.RIGHT_CLICK_BLOCK))) {
			Sign sign = (Sign)event.getClickedBlock().getState();
			if (sign.getLine(0).equalsIgnoreCase("-*-") && sign.getLine(1).equalsIgnoreCase("Back to spawn") && sign.getLine(2).equalsIgnoreCase("-*-")) {
				event.setCancelled(true);
				Player player = event.getPlayer();
				if (player.isSneaking()) {
					return;
				}
				PlayerManager pm = PlayerManager.get(player.getUniqueId());
				
				pm.setStatus(PlayerStatus.SPAWN);
				player.teleport(player.getWorld().getSpawnLocation());
				this.main.getItemManager().giveSpawnItem(player);
			}
		}
	}
	
	@EventHandler
	public void onPlayerInteractSoup(PlayerInteractEvent event) {
		if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			Player player = event.getPlayer();
			if (!player.isDead() && player.getItemInHand().getType() == Material.MUSHROOM_SOUP && player.getHealth() < player.getMaxHealth()) {
				double newHealth = Math.min(player.getHealth() + 7.0D, player.getMaxHealth());
				player.setHealth(newHealth);
				player.getItemInHand().setType(Material.BOWL);
				player.updateInventory();
			} 
		} 
	}
	
	@EventHandler(priority=EventPriority.LOW)
	public void onClickPlayer(PlayerInteractEntityEvent event) {
		if (event.getRightClicked() instanceof Player) {
			Player player = event.getPlayer();
			PlayerManager pm = PlayerManager.get(player.getUniqueId());
	      
			if (pm.getStatus() != PlayerStatus.MODERATION || player.getItemInHand().getItemMeta() == null || player.getItemInHand().getItemMeta().getDisplayName() == null) {
				return;
			}
			Player target = (Player)event.getRightClicked();
			if (player.getItemInHand().getType() == Material.BOOK && player.getItemInHand().getItemMeta().getDisplayName().toLowerCase().equals(ChatColor.RED + "inspection tool")) {
				Bukkit.dispatchCommand(player, "inspect " + target.getName());
				return;
			}
			if (player.getItemInHand().getType() == Material.PACKED_ICE && player.getItemInHand().getItemMeta().getDisplayName().toLowerCase().equals(ChatColor.RED + "freeze someone")) {
				Bukkit.dispatchCommand(player, "freeze " + target.getName());
			}
		} 
	}
	
	@EventHandler(priority=EventPriority.LOW)
	public void onFeed(FoodLevelChangeEvent event) {
		if (event.getEntity() instanceof Player) {
			final Player player = (Player) event.getEntity();
			
			if (PlayerManager.get(player.getUniqueId()).getStatus() == PlayerStatus.DUEL) {
				if (this.main.getDuelManager().getDuelFromPlayerUUID(player.getUniqueId()) != null) {
					Duel duel = this.main.getDuelManager().getDuelFromPlayerUUID(player.getUniqueId());
					
					if (duel.getArena().isSumo() || duel.getLadder() == Ladders.SOUP || duel.getLadder() == Ladders.EARLY_HG || duel.getLadder() == Ladders.BOXING) {
						event.setCancelled(true);
					}
				}
			}
			event.setCancelled(true);
		}
	}
	
	// My PlayerMoveEvent is not like everyone event (be careful)
	@EventHandler(priority=EventPriority.LOWEST)
	public void onMove(PlayerMoveEvent event) {
		final Player player = event.getPlayer();
		final Duel duel = this.main.getDuelManager().getDuelFromPlayerUUID(player.getUniqueId());
		if (duel != null && duel.getLadder() == Ladders.SUMO) {
			if (player.getLocation().getBlock().getType() == Material.WATER) {
				this.main.getDuelManager().removePlayerFromDuel(player, RemoveReason.KILLED);
			}
		}
	}
}
