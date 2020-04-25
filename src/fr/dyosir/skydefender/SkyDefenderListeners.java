package fr.dyosir.skydefender;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Banner;

import fr.dyosir.skydefender.scoreboard.ScoreboardSign;
import fr.dyosir.skydefender.team.Team;

public class SkyDefenderListeners implements Listener {
	
	private SkyDefender main;
	
	public SkyDefenderListeners(SkyDefender skyDefender) {
		this.main = skyDefender;
	}
	
	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		
		System.out.println(player.getName() + "");
		
		// reassign the team of the player if he has one before he quits
		for(Team team: main.getTeams()) {
			// we have to compare the names
			if(team.getPlayersName().contains(player.getName())) {
				Player oldPlayer = team.getOldPlayer(player.getName());
				if(main.isState(SkyDefenderGame.GAME)) {
					main.getBoards().remove(oldPlayer);
					ScoreboardSign scoreboard = new ScoreboardSign(player, "§4SkyDefender");
					scoreboard.create();
					scoreboard.setLine(0, "Time elapsed :");
					scoreboard.setLine(1, "0:00:00");
					main.getBoards().put(player, scoreboard);
				}
				main.removePlayer(oldPlayer);
				main.addPlayer(player, team);
			}
		}
		
		// verify if the player already is in a team
		boolean playerInTeam = false;
		for(Team team: main.getTeams()) {
			if(team.getPlayers().contains(player)) {
				playerInTeam = true;
			}
		}
		
		if(!main.isState(SkyDefenderGame.WAITING)) {
			if(!playerInTeam) {
				if(main.isSpectatorMode() || player.isOp()) {
					main.title.sendTitle(player, "Spectator mode", "", 30);
					player.setGameMode(GameMode.SPECTATOR);
					return;
				} else {
					player.kickPlayer("Game running, spectator mode is disabled.");
					return;
				}
			}
		}
		
		if(main.isState(SkyDefenderGame.WAITING)) {
			main.title.sendTitle(player, "§cSkyDefender", "Bienvenue sur le SkyDefender", 40);
			player.getInventory().clear();
			player.setGameMode(GameMode.ADVENTURE);
			player.setHealth(player.getMaxHealth());
			player.setFoodLevel(20);
			for(Team team: main.getTeams()) {
				player.getInventory().addItem(team.getIcon());
			}
		}
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler
	public void onInteract(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		ItemStack it = event.getItem();
				
		if(it != null && it.getType() == Material.WOOL && main.isState(SkyDefenderGame.WAITING)) {
			for(Team team: main.getTeams()) {
				if(team.getWoolData() == it.getData().getData()) {
					main.addPlayer(player, team);
					continue;
				}
				if(team.getPlayers().contains(player)) {
					team.getPlayers().remove(player);
				}
			}
			
		} else if(event.getAction().equals(Action.PHYSICAL)) {
			Block cb = event.getClickedBlock();
			 if(cb.getType().equals(Material.STONE_PLATE) || cb.getType().equals(Material.IRON_PLATE)) {
				 boolean playerInDefender = false;
				 for(Team team: main.getTeams()) {
					 if(team.getName().equalsIgnoreCase("defender")) {
						 for(Player iplayer: team.getPlayers()) {
							 if(player == iplayer) {
								 playerInDefender = true;
							 }
						 }
					 }
				 }
				 if(main.getPosTp1().getBlock().equals(cb)) {
					 if(playerInDefender) {
						 if(!main.getPlayersOnCd().containsKey(player)) {
							 player.teleport(main.getPosTp2());
							 main.getPlayersOnCd().put(player, main.getDefenderTpCd());

						 } else {
							 player.sendMessage("You are on cooldown, time left : " + main.getPlayersOnCd().get(player));
						 }
					 } else {
						 player.sendMessage("You must be in the defender team to do that.");
					 }
				 } else if(main.getPosTp2().getBlock().equals(cb)) {
					 if(playerInDefender) {
						 if(!main.getPlayersOnCd().containsKey(player)) {
							 player.teleport(main.getPosTp1());
							 main.getPlayersOnCd().put(player, main.getDefenderTpCd());
						 } else {
							 player.sendMessage("You are on cooldown, time left : " + main.getPlayersOnCd().get(player));
						 }
					 } else {
						 player.sendMessage("You must be in the defender team to do that.");
					 }
				 }
			 }
		}
	}
	
	@EventHandler
	public void onPlayerDamage(EntityDamageEvent event) {
		if(event.getCause() == DamageCause.FALL && (main.isState(SkyDefenderGame.WAITING) || !main.isFallDamageStatus())) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onHit(EntityDamageByEntityEvent event) {
		if(event.getEntity() instanceof Player && event.getDamager() instanceof Player) {
			Player damaged = (Player) event.getEntity();
			Player damager = (Player) event.getDamager();
			if(!main.isFriendlyFire()) {
				for(Team team: main.getTeams()) {
					if(team.getPlayers().contains(damaged) && team.getPlayers().contains(damager)) {
						if(main.isSoloMode() && !team.getName().equalsIgnoreCase("defender")) {
							event.setCancelled(false);
						} else {
							event.setCancelled(true);
						}
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		Player player = event.getEntity().getPlayer();
		if(main.isState(SkyDefenderGame.GAME)) {
			if(main.isSpectatorMode() || player.isOp()) {
				player.setGameMode(GameMode.SPECTATOR);
				main.removePlayer(player);
			} else {
				player.kickPlayer("Game running, spectator mode is disabled.");
				main.removePlayer(player);
			}
			boolean attackersAllDead = true;
			for(Team team: main.getTeams()) {
				if(team.getName().equalsIgnoreCase("defender") && team.getPlayers().size() == 0) {
					main.setBannerStatus(true);
					for(Team iteam: main.getTeams()) {
						for(Player iplayer: iteam.getPlayers()) {
							main.title.sendTitle(iplayer, "Banner ON", "All defenders are dead, you can access the banner", 20);	
						}
					}
				} else if(!team.getName().equalsIgnoreCase("defender") && team.getPlayers().size() >= 1) {
					attackersAllDead = false;
				}
			}
			if(attackersAllDead) {
				for(Team team: main.getTeams()) {
					if(team.getName().equalsIgnoreCase("defender")) {
						Team winner = team;
						main.win(winner);
					}
				}
			}
			
		} else if(main.isState(SkyDefenderGame.FINISH)) {
			player.setGameMode(GameMode.SPECTATOR);
		}
	}
	
	@EventHandler
	public void onFoodChange(FoodLevelChangeEvent event) {
		if(!main.isState(SkyDefenderGame.GAME) || main.isNoHunger()) {
			event.setFoodLevel(20);
		}
	}
	
	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		Player player = event.getPlayer();
		Block block = event.getBlock();
		Block underBlock1 = new Location(main.getWorld(), main.getPosTp1().getBlockX(), main.getPosTp1().getBlockY() - 1, main.getPosTp1().getZ()).getBlock();
		Block underBlock2 = new Location(main.getWorld(), main.getPosTp2().getBlockX(), main.getPosTp2().getBlockY() - 1, main.getPosTp2().getZ()).getBlock();
		boolean bannerDefined = false;
		Block support = new Location(main.getWorld(), 0, 200, 0).getBlock();
		if(main.getPosBanner().getBlock().getState().getData() instanceof Banner) {
			bannerDefined = true;
			Banner banner = (Banner) main.getPosBanner().getBlock().getState().getData();
			BlockFace face = banner.getAttachedFace();
			support = main.getPosBanner().getBlock().getRelative(face);
		}
		if(block.equals(main.getPosTp1().getBlock()) || block.equals(main.getPosTp2().getBlock()) || block.equals(underBlock1) || block.equals(underBlock2)) {
			player.sendMessage("You can't destroy that block");
			event.setCancelled(true);
		} else if(!main.isBannerStatus() && bannerDefined) {
			if(block.equals(main.getPosBanner().getBlock()) || block.equals(support)) {
				player.sendMessage("You can't destroy that block");
				event.setCancelled(true);
			}
		} else {
			if(bannerDefined && (block.equals(main.getPosBanner().getBlock()) || block.equals(support))) {
				boolean playerIsDefender = false;
				for(Team team: main.getTeams()) {
					for(Player iplayer: team.getPlayers()) {
						if(player == iplayer && team.getName().equalsIgnoreCase("defender")) {
							player.sendMessage("You can't destroy that block");
							playerIsDefender = true;
							event.setCancelled(true);
						}
					}
				}
				if(!playerIsDefender) {
					boolean defendersAreDead = false;
					for(Team team: main.getTeams()) {
						if(team.getName().equalsIgnoreCase("defender") && team.getSize() == 0) {
							defendersAreDead = true;
						}
					}
					if(defendersAreDead) {
						if(main.isSoloMode()) {
							main.win(player);
						} else {
							Team winner = main.getTeams().get(0);
							for(Team team: main.getTeams()) {
								for(Player iplayer: team.getPlayers()) {
									if(player == iplayer) {
										winner = team;
										main.win(winner);
									}
								}
							}
						}
					} else {
						player.sendMessage("You can't destroy that block");
						event.setCancelled(true);
					}
				}
			}
		}
	}
	
}
