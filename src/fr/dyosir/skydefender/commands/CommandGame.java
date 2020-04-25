package fr.dyosir.skydefender.commands;

import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import fr.dyosir.skydefender.SkyDefender;
import fr.dyosir.skydefender.SkyDefenderGame;
import fr.dyosir.skydefender.scoreboard.ScoreboardSign;
import fr.dyosir.skydefender.team.Team;

public class CommandGame implements CommandExecutor {
	
	private SkyDefender main;
	
	public CommandGame(SkyDefender skyDefender) {
		this.main = skyDefender;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String msg, String[] args) {
		
		if(args.length >= 1) {
			
			if(args[0].equalsIgnoreCase("start")) {
				main.getWorld().setDifficulty(Difficulty.NORMAL);
				main.getWorld().setTime(0);
				main.setState(SkyDefenderGame.GAME);
				Random random = new Random();
				for(Team team: main.getTeams()) {
					Location spawn;
					if(team.getName().equalsIgnoreCase("defender")) {
						spawn = main.getDefenderSpawn();
					} else {
						spawn = new Location(main.getWorld(), random.nextInt(main.getWorldWidth()) - main.getWorldWidth()/2, 130, random.nextInt(main.getWorldWidth()) - main.getWorldWidth()/2);
					}
					for(Player player: team.getPlayers()) {
						player.setGameMode(GameMode.SURVIVAL);
						player.getInventory().clear();
						player.setHealth(player.getMaxHealth());
						player.setFoodLevel(20);
						player.setTotalExperience(0);
						if(main.isSoloMode() && !team.getName().equalsIgnoreCase("defender")) {
							spawn = new Location(main.getWorld(), random.nextInt(main.getWorldWidth()) - main.getWorldWidth()/2, 130, random.nextInt(main.getWorldWidth()) - main.getWorldWidth()/2);
							player.teleport(spawn);
						} else {
							player.teleport(spawn);
						}
					}
				}
				main.startEventCap();
				main.startDefenderTpCooldown();
				for(Team team: main.getTeams()) {
					for(Player player: team.getPlayers()) {
						ScoreboardSign scoreboard = new ScoreboardSign(player, "§4SkyDefender");
						scoreboard.create();
						scoreboard.setLine(0, "Time elapsed :");
						scoreboard.setLine(1, "0:00:00");
						scoreboard.setLine(2, "Fall damage at :");
						scoreboard.setLine(3, "0:00:15");
						main.getBoards().put(player, scoreboard);
					}
				}
				return true;
				
			} else if(args[0].equalsIgnoreCase("reset")) {
				for(Player player: Bukkit.getOnlinePlayers()) {
					main.getBoards().remove(player);
				}
				main.setState(SkyDefenderGame.WAITING);
				
				main.getWorld().setDifficulty(Difficulty.PEACEFUL);
				main.getWorld().setPVP(false);
				main.getWorld().setTime(0);
								
				for(Player iplayer: Bukkit.getOnlinePlayers()) {
					iplayer.setHealth(iplayer.getMaxHealth());
					iplayer.setFoodLevel(20);
					iplayer.setGameMode(GameMode.ADVENTURE);
					iplayer.getInventory().clear();
					main.removePlayer(iplayer);
					for(Team team: main.getTeams()) {
						iplayer.getInventory().addItem(team.getIcon());
						team.getPlayersName().clear();
					}
				}
				main.setSoloMode(false);
				main.setTeamSize(4);
				main.setFallDamageStatus(false);
				main.setBannerStatus(false);
				return true;

			} else {
				return false;
			}
		}
	return false;
	}
	
}
