package us.noks.smallpractice.listeners;

import java.util.Collections;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
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
import org.bukkit.potion.PotionEffect;

import com.google.common.collect.Lists;

import us.noks.smallpractice.Main;
import us.noks.smallpractice.enums.PlayerStatus;
import us.noks.smallpractice.objects.Duel;
import us.noks.smallpractice.objects.managers.DuelManager;
import us.noks.smallpractice.objects.managers.ItemManager;
import us.noks.smallpractice.objects.managers.PartyManager;
import us.noks.smallpractice.objects.managers.PlayerManager;
import us.noks.smallpractice.objects.managers.QueueManager;
import us.noks.smallpractice.party.Party;
import us.noks.smallpractice.party.PartyState;

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
		
		PlayerManager.create(player.getUniqueId());
		
		player.setExp(0.0F);
		player.setLevel(0);
		
		player.setHealth(20.0D);
		player.setFoodLevel(20);
		player.setSaturation(1000f);
		player.extinguish();
		if (!player.getActivePotionEffects().isEmpty()) {
			for (PotionEffect effect : player.getActivePotionEffects()) {
				player.removePotionEffect(effect.getType());
			}
		}
		player.setAllowFlight(false);
		player.setFlying(false);
		player.setGameMode(GameMode.SURVIVAL);
		
		player.setScoreboard(this.main.getServer().getScoreboardManager().getNewScoreboard());
		
		player.teleport(player.getWorld().getSpawnLocation());
		ItemManager.getInstace().giveSpawnItem(player);
		
		for (int i = 0; i < 100; i++) {
			player.sendMessage(""); 
		}
		player.sendMessage(ChatColor.DARK_AQUA + "Welcome back on " + ChatColor.YELLOW + "Goneko");
		player.setPlayerListName(PlayerManager.get(player.getUniqueId()).getPrefixColors() + player.getName());
		
		for (Player allPlayers : Bukkit.getOnlinePlayers()) {
			final PlayerManager pmAll = PlayerManager.get(allPlayers.getUniqueId());
			if (pmAll.getStatus() == PlayerStatus.WAITING || pmAll.getStatus() == PlayerStatus.DUEL || pmAll.getStatus() == PlayerStatus.MODERATION) {
				player.hidePlayer(allPlayers);
			}
		}
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
			player.getInventory().clear();
			player.getInventory().setArmorContents(null);

			player.setHealth(20.0D);
			player.setFoodLevel(20);
			player.setSaturation(10000f);
			player.teleport(player.getWorld().getSpawnLocation());
			ItemManager.getInstace().giveSpawnItem(player);
		}
	}
	
	@EventHandler(priority=EventPriority.LOWEST)
	public void onVoidDamage(EntityDamageEvent event) {
		if (event.getEntity() instanceof Player) {
			final Player player = (Player) event.getEntity();
			final PlayerManager pm = PlayerManager.get(player.getUniqueId());
			
			if (pm.getStatus() == PlayerStatus.SPAWN || pm.getStatus() == PlayerStatus.QUEUE) {
				switch (event.getCause()) {
				case FALL:
					event.setCancelled(true);
					break;
				case VOID:
					event.setCancelled(true);
					player.teleport(player.getWorld().getSpawnLocation());
					break;
				default:
					break;
				}
			}
			if (pm.getStatus() == PlayerStatus.SPECTATE) {
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler(priority=EventPriority.LOWEST)
	public void onDamage(EntityDamageByEntityEvent event) {
		if (event.getEntity() instanceof Player && event.getDamager() instanceof Player) {
			final Player attacked = (Player) event.getEntity();
			final Player attacker = (Player) event.getDamager();
				
			if (PlayerManager.get(attacker.getUniqueId()).getStatus() == PlayerStatus.MODERATION) {
				event.setDamage(0.0D);
				return;
			}
			if (PlayerManager.get(attacker.getUniqueId()).getStatus() == PlayerStatus.SPECTATE) {
				event.setCancelled(true);
				return;
			}
			if (PlayerManager.get(attacker.getUniqueId()).getStatus() != PlayerStatus.DUEL && PlayerManager.get(attacked.getUniqueId()).getStatus() != PlayerStatus.DUEL) {
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler(priority=EventPriority.LOWEST)
	public void onReceiveDrop(PlayerPickupItemEvent event) {
		if (event.getItem().getOwner() instanceof Player) {
			final Player receiver = event.getPlayer();
			final PlayerManager pm = PlayerManager.get(receiver.getUniqueId());
			
			if (pm.getStatus() != PlayerStatus.DUEL && pm.getStatus() != PlayerStatus.WAITING && !pm.isCanBuild()) {
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
		if (event.getPlayer().getGameMode() != GameMode.CREATIVE && pm.getStatus() != PlayerStatus.WAITING && pm.getStatus() != PlayerStatus.DUEL) {
			event.setCancelled(true);
		}
		if (event.getItemDrop().getItemStack().getType() == Material.GLASS_BOTTLE) event.getItemDrop().remove();
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
		                QueueManager.getInstance().addToQueue(player.getUniqueId(), false);
		                break;
		            }
					if (item.getType() == Material.DIAMOND_SWORD && item.getItemMeta().getDisplayName().toLowerCase().equals(ChatColor.YELLOW + "ranked direct queue")) {
		                event.setCancelled(true);
		                player.sendMessage(ChatColor.GOLD + "This action comming soon ^^");
		                break;
		            }
					if (item.getType() == Material.NAME_TAG && item.getItemMeta().getDisplayName().toLowerCase().equals(ChatColor.YELLOW + "create party")) {
		                event.setCancelled(true);
		                PartyManager.getInstance().createParty(player.getUniqueId(), player.getName());
		                ItemManager.getInstace().giveSpawnItem(player);
		                break;
		            }
					if (item.getType() == Material.COMPASS && item.getItemMeta().getDisplayName().toLowerCase().equals(ChatColor.YELLOW + "warps selection")) {
						player.sendMessage(ChatColor.GOLD + "This action comming soon ^^");
						break;
					}
					if (item.getType() == Material.BOOK && item.getItemMeta().getDisplayName().toLowerCase().equals(ChatColor.YELLOW + "edit kit")) {
						player.sendMessage(ChatColor.GOLD + "This action comming soon ^^");
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
		                player.sendMessage(ChatColor.GOLD + "This action comming soon ^^");
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
		                player.sendMessage(ChatColor.GOLD + "This action comming soon ^^");
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
						if (isPartyLeader) {
							PartyManager.getInstance().transferLeader(player.getUniqueId());
						} else {
							currentParty.removeMember(player.getUniqueId());
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
			case SPECTATE:
				if (item.getType() == Material.REDSTONE && item.getItemMeta().getDisplayName().toLowerCase().equals(ChatColor.RED + "leave spectate")) {
	                event.setCancelled(true);
	                final Player spectatePlayer = pm.getSpectate();
	                final Duel spectatedDuel = DuelManager.getInstance().getDuelFromPlayerUUID(spectatePlayer.getUniqueId());
	                
	        		spectatedDuel.sendMessage(ChatColor.YELLOW + player.getName() + ChatColor.DARK_AQUA + " is no longer spectating.");
	        		spectatedDuel.removeSpectator(player.getUniqueId());
	        		
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
	
	@EventHandler(priority=EventPriority.LOWEST)
	public void onClickPlayer(PlayerInteractEntityEvent event) {
		if (event.getRightClicked() instanceof Player) {
			Player player = event.getPlayer();
			PlayerManager pm = PlayerManager.get(player.getUniqueId());
			
			if (pm.getStatus() != PlayerStatus.MODERATION) {
				return;
			}
			Player target = (Player)event.getRightClicked();
	      
			if (player.getItemInHand().getItemMeta() == null) {
				return;
			}
			if (player.getItemInHand().getItemMeta().getDisplayName() == null) {
				return;
			}
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
