package us.noks.smallpractice.listeners;

import java.util.Collections;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
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
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;

import com.google.common.collect.Lists;

import ru.tehkode.permissions.bukkit.PermissionsEx;
import us.noks.smallpractice.Main;
import us.noks.smallpractice.enums.PlayerStatus;
import us.noks.smallpractice.enums.Warps;
import us.noks.smallpractice.objects.Duel;
import us.noks.smallpractice.objects.managers.DuelManager;
import us.noks.smallpractice.objects.managers.ItemManager;
import us.noks.smallpractice.objects.managers.PartyManager;
import us.noks.smallpractice.objects.managers.PlayerManager;
import us.noks.smallpractice.objects.managers.QueueManager;
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
		
		this.checkVote(player);
		PlayerManager.create(player.getUniqueId());
		
		player.setExp(0.0F);
		player.setLevel(0);
		
		PlayerManager.get(player.getUniqueId()).heal(false);
		player.setAllowFlight(false);
		player.setFlying(false);
		player.setGameMode(GameMode.SURVIVAL);
		
		player.setScoreboard(this.main.getServer().getScoreboardManager().getNewScoreboard());
		
		player.teleport(player.getWorld().getSpawnLocation());
		ItemManager.getInstace().giveSpawnItem(player);
		
		this.sendJoinMessage(event);
		
		for (Player allPlayers : Bukkit.getOnlinePlayers()) {
			final PlayerManager pmAll = PlayerManager.get(allPlayers.getUniqueId());
			if (pmAll.getStatus() == PlayerStatus.WAITING || pmAll.getStatus() == PlayerStatus.DUEL || pmAll.getStatus() == PlayerStatus.MODERATION) {
				player.hidePlayer(allPlayers);
			}
		}
	}
	
	private void checkVote(Player player) {
		String rank = PermissionsEx.getPermissionManager().getUser(player).getParentIdentifiers().get(0);

        if (rank.equals("default") || rank.equals("verified")) {
            WebUtil.getResponse(Main.getInstance(), "https://api.namemc.com/server/devmc.noks.io/votes?profile=" + player.getUniqueId(),
                    response -> {
                        switch (response) {
                            case "false":
                                if (rank.equals("verified")) {
                                	PermissionsEx.getPermissionManager().getUser(player).removeGroup("verified");
                                	PermissionsEx.getPermissionManager().getUser(player).addGroup("default");
                                    player.sendMessage(ChatColor.RED + "Your voter rank has been removed because you removed your vote! :(");
                                }
                                break;
                            case "true":
                                if (rank.equals("default")) {
                                	PermissionsEx.getPermissionManager().getUser(player).removeGroup("default");
                                	PermissionsEx.getPermissionManager().getUser(player).addGroup("verified");
                                    player.sendMessage(ChatColor.GREEN + "Thanks for voting! You've been given the Voter rank.");
                                }
                                break;
                        }
                    }
            );
        }
	}
	
	private void sendJoinMessage(PlayerJoinEvent event) {
		final Player player = event.getPlayer();
		for (int i = 0; i < 100; i++) {
			player.sendMessage(""); 
		}
		player.sendMessage(ChatColor.DARK_AQUA + "Welcome back on " + ChatColor.YELLOW + "Goneko" + ChatColor.GRAY + " (Practice)");
		player.sendMessage("");
		player.sendMessage(ChatColor.GRAY + "-> " + ChatColor.DARK_AQUA + "Discord: " + ChatColor.GRAY + "https://discord.gg/8v8Mzhd");
		player.sendMessage(ChatColor.GRAY + "-> " + ChatColor.DARK_AQUA + "NameMC: " + ChatColor.GRAY + "https://namemc.com/server/devmc.noks.io");
		player.sendMessage("");
		player.setPlayerListName(PlayerManager.get(player.getUniqueId()).getPrefixColors() + player.getName());
	}
	
	@EventHandler(priority=EventPriority.HIGH)
	public void onQuit(PlayerQuitEvent event) {
		event.setQuitMessage(null);
		final Player player = event.getPlayer();
		
        if (PartyManager.getInstance().hasParty(player.getUniqueId())) {
        	final Party party = PartyManager.getInstance().getParty(player.getUniqueId());
            if (party.getLeader().equals(player.getUniqueId())) {
            	PartyManager.getInstance().transferLeader(player.getUniqueId());
            } else {
            	PartyManager.getInstance().leaveParty(player.getUniqueId());
            }
        }
		if (QueueManager.getInstance().getQueue().contains(player.getUniqueId())) {
			QueueManager.getInstance().getQueue().remove(player.getUniqueId());
		}
		if ((PlayerManager.get(player.getUniqueId()).getStatus() == PlayerStatus.DUEL || PlayerManager.get(player.getUniqueId()).getStatus() == PlayerStatus.WAITING)) {
			DuelManager.getInstance().removePlayerFromDuel(player);
		}
		PlayerManager.get(player.getUniqueId()).remove();
	}
	
	@EventHandler(priority=EventPriority.HIGH)
	public void onDeath(PlayerDeathEvent event) {
		event.setDeathMessage(null);
		event.getDrops().clear();
		event.setDroppedExp(0);
		
		if (event.getEntity() instanceof Player) {
			final Player killed = event.getEntity();
			//Duel duel = DuelManager.getInstance().getDuelFromPlayerUUID(killed.getUniqueId());
			//duel.addDrops(event.getDrops());
			DuelManager.getInstance().removePlayerFromDuel(killed);
		}
	}
	
	@EventHandler(priority=EventPriority.HIGHEST)
	public void onRespawn(PlayerRespawnEvent event) {
		final Player player = event.getPlayer();
		
		if (DuelManager.getInstance().getDuelFromPlayerUUID(player.getUniqueId()) == null) {
			final PlayerManager pm = PlayerManager.get(player.getUniqueId());
			pm.heal(false);
			player.teleport(player.getWorld().getSpawnLocation());
			pm.showAllPlayer();
			ItemManager.getInstace().giveSpawnItem(player);
		}
	}
	
	@EventHandler(priority=EventPriority.LOWEST)
	public void onVoidDamage(EntityDamageEvent event) {
		if (event.getEntity() instanceof Player) {
			final Player player = (Player) event.getEntity();
			final PlayerManager pm = PlayerManager.get(player.getUniqueId());
			
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
					player.setNoDamageTicks(40);
					event.setCancelled(true);
					ItemManager.getInstace().giveBridgeItems(player);
					player.teleport(Warps.BRIDGE.getLobbyLocation());
					break;
				default:
					break;
				}
			}
			if (pm.getStatus() == PlayerStatus.SPECTATE) {
				event.setCancelled(true);
				return;
			}
		}
	}
	
	@EventHandler(priority=EventPriority.LOWEST)
	public void onDamage(EntityDamageByEntityEvent event) {
		if (event.getEntity() instanceof Player && event.getDamager() instanceof Player) {
			final Player attacked = (Player) event.getEntity();
			final Player attacker = (Player) event.getDamager();
				
			if (PlayerManager.get(attacker.getUniqueId()).getStatus() == PlayerStatus.MODERATION || PlayerManager.get(attacker.getUniqueId()).getStatus() == PlayerStatus.BRIDGE) {
				if (attacker.getNoDamageTicks() > 0) {
					event.setCancelled(true);
					return;
				}
				event.setDamage(0.0D);
				return;
			}
			if (PlayerManager.get(attacker.getUniqueId()).getStatus() == PlayerStatus.SPECTATE || PlayerManager.get(attacker.getUniqueId()).getStatus() != PlayerStatus.DUEL && PlayerManager.get(attacked.getUniqueId()).getStatus() != PlayerStatus.DUEL) {
				event.setCancelled(true);
				return;
			}
			if (PlayerManager.get(attacker.getUniqueId()).getStatus() == PlayerStatus.DUEL) {
				Duel currentDuel = DuelManager.getInstance().getDuelFromPlayerUUID(attacker.getUniqueId());
				
				if (currentDuel == null) {
					return;
				}
				if (currentDuel.getArena().isSumo()) {
					event.setDamage(0.0D);
				}
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
			if (DuelManager.getInstance().getDuelFromPlayerUUID(receiver.getUniqueId()) != null) {
				final Duel currentDuel = DuelManager.getInstance().getDuelFromPlayerUUID(receiver.getUniqueId());
				
				if (!currentDuel.containPlayer(owner) && !currentDuel.containDrops(item)) event.setCancelled(true);
				return;
			}
			if (!receiver.canSee(owner)) event.setCancelled(true);
		}
	}
	
	@EventHandler(priority=EventPriority.LOWEST)
	public void onDrop(PlayerDropItemEvent event) {
		PlayerManager pm = PlayerManager.get(event.getPlayer().getUniqueId());
		if (event.getPlayer().getGameMode() != GameMode.CREATIVE && pm.getStatus() != PlayerStatus.WAITING && pm.getStatus() != PlayerStatus.DUEL && !pm.isAllowedToBuild()) {
			event.setCancelled(true);
			return;
		}
		if (event.getItemDrop().getItemStack().getType() == Material.GLASS_BOTTLE) {
			event.getItemDrop().remove();
			return;
		}
		if (pm.getStatus() == PlayerStatus.WAITING || pm.getStatus() == PlayerStatus.DUEL) {
			Duel duel = DuelManager.getInstance().getDuelFromPlayerUUID(event.getPlayer().getUniqueId());
			
			if (duel == null) {
				return;
			}
			duel.addDrops(event.getItemDrop());
		}
	}
	
	@EventHandler(priority=EventPriority.LOWEST)
	public void onClickItem(PlayerInteractEvent event) {
		final Player player = event.getPlayer();
        if (player.getInventory().getItemInHand() == null || !player.getInventory().getItemInHand().hasItemMeta() || !player.getInventory().getItemInHand().getItemMeta().hasDisplayName()) {
            return;
        }
        final ItemStack item = player.getItemInHand();
        if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK) || event.getAction().equals(Action.RIGHT_CLICK_AIR)) {
        	PlayerManager pm = PlayerManager.get(player.getUniqueId());
        	
        	switch (pm.getStatus()) {
			case SPAWN:
				if (!PartyManager.getInstance().hasParty(player.getUniqueId())) {
					if (item.getType() == Material.IRON_SWORD && item.getItemMeta().getDisplayName().toLowerCase().equals(ChatColor.YELLOW + "unranked direct queue")) {
		                event.setCancelled(true);
		                QueueManager.getInstance().addToQueue(player.getUniqueId(), false, false);
		                break;
		            }
					if (item.getType() == Material.DIAMOND_SWORD && item.getItemMeta().getDisplayName().toLowerCase().equals(ChatColor.YELLOW + "ranked direct queue")) {
		                event.setCancelled(true);
		                player.sendMessage(ChatColor.GOLD + "This action coming soon ^^");
		                break;
		            }
					if (item.getType() == Material.NAME_TAG && item.getItemMeta().getDisplayName().toLowerCase().equals(ChatColor.YELLOW + "create party")) {
		                event.setCancelled(true);
		                Bukkit.dispatchCommand(player, "party create");
		                break;
		            }
					if (item.getType() == Material.COMPASS && item.getItemMeta().getDisplayName().toLowerCase().equals(ChatColor.YELLOW + "warps selection")) {
						player.sendMessage(ChatColor.GOLD + "Successfully teleported to the Bridge warps (because it's the only one ^^')");
						player.teleport(Warps.BRIDGE.getLobbyLocation());
						pm.setStatus(PlayerStatus.BRIDGE);
						ItemManager.getInstace().giveBridgeItems(player);
						player.setNoDamageTicks(40);
						break;
					}
					if (item.getType() == Material.BOOK && item.getItemMeta().getDisplayName().toLowerCase().equals(ChatColor.YELLOW + "edit kit/settings")) {
						player.sendMessage(ChatColor.GOLD + "This action coming soon ^^");
						break;
					}
				} else {
					final Party currentParty = PartyManager.getInstance().getParty(player.getUniqueId());
					final boolean isPartyLeader = currentParty.getLeader() == player.getUniqueId();
					
					if (item.getType() == Material.IRON_SWORD && item.getItemMeta().getDisplayName().toLowerCase().equals(ChatColor.YELLOW + "2v2 unranked queue")) {
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
		                player.sendMessage(ChatColor.GOLD + "This action coming soon ^^");
		                break;
		            }
					if (item.getType() == Material.DIAMOND_SWORD && item.getItemMeta().getDisplayName().toLowerCase().equals(ChatColor.YELLOW + "2v2 ranked queue")) {
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
		                player.sendMessage(ChatColor.GOLD + "This action coming soon ^^");
		                break;
		            }
					if (item.getType() == Material.ARROW && item.getItemMeta().getDisplayName().toLowerCase().equals(ChatColor.YELLOW + "split teams")) {
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
                        DuelManager.getInstance().createSplitTeamsDuel(currentParty);
                        break;
		            }
					if (item.getType() == Material.BOOK && item.getItemMeta().getDisplayName().toLowerCase().equals(ChatColor.YELLOW + "fight other parties")) {
						player.openInventory(PartyManager.getInstance().getPartiesInventory());
						break;
					}
					if (item.getType() == Material.PAPER && item.getItemMeta().getDisplayName().toLowerCase().equals(ChatColor.YELLOW + "party information")) {
						Bukkit.dispatchCommand(player, "party info");
						break;
					}
					if (item.getType() == Material.REDSTONE && item.getItemMeta().getDisplayName().toLowerCase().equals(ChatColor.RED + "leave party")) {
						event.setCancelled(true);
						if (currentParty.getLeader().equals(player.getUniqueId())) {
			                PartyManager.getInstance().transferLeader(player.getUniqueId());
			            } else {
			                PartyManager.getInstance().notifyParty(currentParty, ChatColor.RED + player.getName() + " has left the party");
			                PartyManager.getInstance().leaveParty(player.getUniqueId());
			            }
			        	ItemManager.getInstace().giveSpawnItem(player);
					}
				}
				break;
			case QUEUE:
				if (item.getType() == Material.REDSTONE && item.getItemMeta().getDisplayName().toLowerCase().equals(ChatColor.RED + "leave queue")) {
	                event.setCancelled(true);
	                QueueManager.getInstance().quitQueue(player);
	            }
				break;
			case WAITING:
				if (item.getType() == Material.ENCHANTED_BOOK && item.getItemMeta().getDisplayName().toLowerCase().equals(ChatColor.YELLOW + "nodebuff default kit")) {
	                ItemManager.getInstace().giveFightItems(player);
	                player.sendMessage(ChatColor.GREEN + "NoDebuff kit successfully given.");
	            }
				break;
			case DUEL:
				if (item.getType() == Material.ENCHANTED_BOOK && item.getItemMeta().getDisplayName().toLowerCase().equals(ChatColor.YELLOW + "nodebuff default kit")) {
	                ItemManager.getInstace().giveFightItems(player);
	                player.sendMessage(ChatColor.GREEN + "NoDebuff kit successfully given.");
	            }
				break;
			case SPECTATE:
				if (item.getType() == Material.REDSTONE && item.getItemMeta().getDisplayName().toLowerCase().equals(ChatColor.RED + "leave spectate")) {
	                event.setCancelled(true);
	                final Player spectatePlayer = pm.getSpectate();
	                final Duel spectatedDuel = DuelManager.getInstance().getDuelFromPlayerUUID(spectatePlayer.getUniqueId());
	                
	                if (spectatedDuel != null) {
	                	spectatedDuel.sendMessage(ChatColor.YELLOW + player.getName() + ChatColor.DARK_AQUA + " is no longer spectating.");
	                	spectatedDuel.removeSpectator(player.getUniqueId());
	                }
	        		
	        		player.setAllowFlight(false);
	        		player.setFlying(false);
	        		pm.setStatus(PlayerStatus.SPAWN);
	        		pm.showAllPlayer();
	        		pm.setSpectate(null);
	        		player.teleport(player.getWorld().getSpawnLocation());
	        		ItemManager.getInstace().giveSpawnItem(player);
	            }
				break;
			case MODERATION:
				if (item.getType() == Material.REDSTONE && item.getItemMeta().getDisplayName().toLowerCase().equals(ChatColor.RED + "leave moderation")) {
	                event.setCancelled(true);
	                Bukkit.dispatchCommand(player, "mod");
	                break;
	            }
				if (item.getType() == Material.WATCH && item.getItemMeta().getDisplayName().toLowerCase().equals(ChatColor.RED + "see random player")) {
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
	
	@EventHandler
	public void onInteractWithBlock(PlayerInteractEvent e) {
		if (e.getClickedBlock() != null && (e.getClickedBlock().getType() == Material.SIGN_POST || e.getClickedBlock().getType() == Material.SIGN || (e.getClickedBlock().getType() == Material.WALL_SIGN && e.getAction() == Action.RIGHT_CLICK_BLOCK))) {
			Sign s = (Sign)e.getClickedBlock().getState();
			if (s.getLine(0).equalsIgnoreCase("-*-") && s.getLine(1).equalsIgnoreCase("Back to spawn") && s.getLine(2).equalsIgnoreCase("-*-")) {
				e.setCancelled(true);
				Player p = e.getPlayer();
				PlayerManager pm = PlayerManager.get(p.getUniqueId());
				
				pm.setStatus(PlayerStatus.SPAWN);
				p.teleport(p.getWorld().getSpawnLocation());
				ItemManager.getInstace().giveSpawnItem(p);
			}
		}
	}
	
	@EventHandler(priority=EventPriority.LOWEST)
	public void onClickPlayer(PlayerInteractEntityEvent event) {
		if (event.getRightClicked() instanceof Player) {
			Player player = event.getPlayer();
			PlayerManager pm = PlayerManager.get(player.getUniqueId());
	      
			if (pm.getStatus() != PlayerStatus.MODERATION || player.getItemInHand().getItemMeta() == null || player.getItemInHand().getItemMeta().getDisplayName() == null) {
				return;
			}
			Player target = (Player)event.getRightClicked();
			if (player.getItemInHand().getType() == Material.BOOK && player.getItemInHand().getItemMeta().getDisplayName().toLowerCase().equals(ChatColor.RED + "inspection tool")) {
				Bukkit.dispatchCommand(player, "verif " + target.getName());
				return;
			}
			if (player.getItemInHand().getType() == Material.PACKED_ICE && player.getItemInHand().getItemMeta().getDisplayName().toLowerCase().equals(ChatColor.RED + "freeze player")) {
				Bukkit.dispatchCommand(player, "freeze " + target.getName());
			}
		} 
	}
	
	@EventHandler(priority=EventPriority.LOWEST)
	public void onFeed(FoodLevelChangeEvent event) {
		if (event.getEntity() instanceof Player) {
			final Player player = (Player) event.getEntity();
			
			if (PlayerManager.get(player.getUniqueId()).getStatus() != PlayerStatus.DUEL) {
				event.setCancelled(true);
			}
		}
	}
}
