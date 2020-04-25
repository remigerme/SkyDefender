package fr.dyosir.skydefender.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import fr.dyosir.skydefender.SkyDefender;
import fr.dyosir.skydefender.team.Team;

public class CommandTeam implements CommandExecutor {
	
	private SkyDefender main;
	
	public CommandTeam(SkyDefender skyDefender) {
		this.main = skyDefender;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String msg, String[] args) {
		if(args.length >= 1) {
			
			if(args[0].equalsIgnoreCase("size")) {
				if(args.length >= 2) {
					main.setTeamSize(Integer.parseInt(args[1]));
					sender.sendMessage("Team size set.");
				} else {
					sender.sendMessage("Team size : " + main.getTeamSize());
				}
				return true;
				
			} else if(args[0].equalsIgnoreCase("remove")) {
				if(args.length >= 2) {
					for(Team team: main.getTeams()) {
						if(team.getPlayersName().contains(args[1])) {
							Player player = team.getOldPlayer(args[1]);
							main.removePlayer(player);
							return true;
						}
					}
				} else {
					sender.sendMessage("/team remove <player>");
					return true;
				}
				
			} else if(args[0].equalsIgnoreCase("add")) {
				if(args.length >= 3) {
					Team teamToAdd = main.getTeams().get(0);
					boolean teamError = true;
					for(Team team: main.getTeams()) {
						if(team.getName().equalsIgnoreCase(args[1])) {
							teamToAdd = team;
							teamError = false;
						}
					}
					Player playerToAdd = (Player) sender;
					boolean playerError = true;
					for(Player player: Bukkit.getOnlinePlayers()) {
						if(player.getName().equalsIgnoreCase(args[2])) {
							playerToAdd = player;
							playerError = false;
						}
					}
					main.addPlayer(playerToAdd, teamToAdd);
					if(teamError || playerError) {
						sender.sendMessage("An error happened : /team add <teamnanme> <player>");
						return true;
					} else {
						main.addPlayer(playerToAdd, teamToAdd);
						sender.sendMessage(playerToAdd.getName() + " joined " + teamToAdd.getTag() + teamToAdd.getName() + "§f team");
						return true;
					}
				} else {
					sender.sendMessage("/team add <teamname> <player>");
					return true;
				}
				
			} else if(args[0].equalsIgnoreCase("solo")) {
				if(args.length >= 2) {
					if(args[1].equalsIgnoreCase("true")) {
						sender.sendMessage("Enable solo mode");
						main.setSoloMode(true);
						main.setTeamSize(50);
						for(Player player: Bukkit.getOnlinePlayers()) {
							main.removePlayer(player);
							player.getInventory().clear();
							for(Team team: main.getTeams()) {
								if(team.getName().equalsIgnoreCase("red") || team.getName().equalsIgnoreCase("defender")) {
									player.getInventory().addItem(team.getIcon());
								}
							}
						}
						return true;						
					} else if(args[1].equalsIgnoreCase("false")) {
						sender.sendMessage("Disable solo mode");
						main.setTeamSize(4);
						main.setSoloMode(false);
						for(Player player: Bukkit.getOnlinePlayers()) {
							main.removePlayer(player);
							player.getInventory().clear();
							for(Team team: main.getTeams()) {
								player.getInventory().addItem(team.getIcon());
							}
						}
						return true;
					} else {
						sender.sendMessage("/team solo <true/false>");
						return true;
					}
				} else {
					sender.sendMessage("/team solo <true/false>");
					return true;
				}
				
			} else if(args[0].equalsIgnoreCase("random")) {
				for(Player player: Bukkit.getOnlinePlayers()) {
					main.removePlayer(player);
					main.randomTeam(player);
				}
				sender.sendMessage("Random team created");
				return true;
				
			} else {
				return false;
			}
		} else {
			return false;
		}
		return false;
	}
	
}
