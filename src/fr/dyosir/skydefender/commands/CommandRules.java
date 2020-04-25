package fr.dyosir.skydefender.commands;

import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import fr.dyosir.skydefender.SkyDefender;
import fr.dyosir.skydefender.team.Team;

public class CommandRules implements CommandExecutor {
	
	private SkyDefender main;
	
	public CommandRules(SkyDefender skyDefender) {
		this.main = skyDefender;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String msg, String[] args) {
		
		if(args.length >= 1) {
			
			if(args[0].equalsIgnoreCase("nohunger")) {
				if(args.length == 2) {
					if(args[1].equalsIgnoreCase("true")) {
						main.setNoHunger(true);
						sender.sendMessage("Enable no hunger.");
						return true;
					} else if(args[1].equalsIgnoreCase("false")) {
						main.setNoHunger(false);
						sender.sendMessage("Disable no hunger.");
						return true;
					} else {
						sender.sendMessage("/rules nohunger <true/false>");
						return false;
					}
				} else {
					sender.sendMessage("/rules nohunger <true/false>");
					return true;
				}
				
			} else if(args[0].equalsIgnoreCase("friendlyfire")) {
				if(args.length == 2) {
					if(args[1].equalsIgnoreCase("true")) {
						main.setFriendlyFire(true);
						sender.sendMessage("Enable friendly fire (can hurt teammates)");
						return true;
					} else if(args[1].equalsIgnoreCase("false")) {
						main.setFriendlyFire(false);
						sender.sendMessage("Disable friendly fire (can't hurt teammates)");
						return true;
					} else {
						sender.sendMessage("/rules friendlyfire <true/false>");
						return true;
					}
				} else {
					sender.sendMessage("/rules friendlyfire <true/false>");
					return true;
				}
				
			} else if(args[0].equalsIgnoreCase("spectatormode")) {
				if(args.length == 2) {
					if(args[1].equalsIgnoreCase("true")) {
						main.setSpectatorMode(true);
						sender.sendMessage("Enable spectator mode.");
						return true;
					} else if(args[1].equalsIgnoreCase("false")) {
						main.setSpectatorMode(false);
						sender.sendMessage("Disable spectator mode.");
						for(Team team: main.getTeams()) {
							for(Player player: team.getPlayers()) {
								if(player.getGameMode() == GameMode.SPECTATOR && !player.isOp()) {
									player.kickPlayer("Spectator mode disabled.");
								}
							}
						}
						return true;
					} else {
						sender.sendMessage("/rules spectatormode <true/false>");
						return true;
					}
				} else {
					sender.sendMessage("/rules spectatormode <true/false>");
					return true;
				}
				
			} else if(args[0].equalsIgnoreCase("pvpcap")) {
				if(args.length == 2) {
					int pvpCap;
					try {
						pvpCap = (Integer.parseInt(args[1]) - 1) * 1200;
						if(pvpCap < 0) {
							pvpCap = 0;
						}
					} catch(NumberFormatException e) {
						pvpCap = 1200;
						sender.sendMessage("/rules pvpcap <int>");
					}
					main.setPvpCap(pvpCap);
					sender.sendMessage("New PVP cap set to " + pvpCap + "s.");
					return true;
				} else {
					sender.sendMessage("/rules pvpcap <int>");
					return true;
				}
				
			} else if(args[0].equalsIgnoreCase("bannercap")) {
				if(args.length == 2) {
					int bannerCap;
					try {
						bannerCap = (Integer.parseInt(args[1]) - 1) * 1200;
						if(bannerCap < 0) {
							bannerCap = 0;
						}
					} catch(NumberFormatException e) {
						bannerCap = 7200;
						sender.sendMessage("/rules bannercap <int>");
					}
					main.setBannerCap(bannerCap);
					sender.sendMessage("New banner cap set to " + bannerCap + "s.");
					return true;
				}
				
			} else {
				return false;
			}
			
		} else {
			return false;
		}
		return false;
	}

}
