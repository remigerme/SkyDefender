package fr.dyosir.skydefender.commands;

import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import fr.dyosir.skydefender.SkyDefender;

public class CommandPos implements CommandExecutor {
	
	private SkyDefender main;
	
	public CommandPos(SkyDefender skyDefender) {
		this.main = skyDefender;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String msg, String[] args) {
		
		if(args.length == 1) {
			
			if(args[0].equalsIgnoreCase("defenderspawn")) {
				if(sender instanceof Player) {
					Player player = (Player) sender;
					Location ploc = player.getLocation();
					double x = ploc.getX();
					double y = ploc.getY();
					double z = ploc.getZ();
					main.setDefenderSpawn(new Location(main.getWorld(), x, y, z));
					player.sendMessage("Defender spawn set.");
					return true;
				}
				
			} else if(args[0].equalsIgnoreCase("banner")) {
				if(sender instanceof Player) {
					Player player = (Player) sender;
					Block targetBlock = player.getTargetBlock((Set<Material>) null, 10);
					if(targetBlock.getType() == Material.WALL_BANNER) {
						main.setPosBanner(targetBlock.getLocation());
						player.sendMessage("Banner position set.");
						return true;
					} else {
						player.sendMessage("You must be looking at a wall banner.");
						return true;
					}
				} else {
					sender.sendMessage("Must be a player.");
					return false;
				}
				
			} else if(args[0].equalsIgnoreCase("tp1")) {
				if(sender instanceof Player) {
					Player player = (Player) sender;
					Block targetBlock = player.getTargetBlock((Set<Material>) null, 10);
					if(targetBlock.getType() == Material.STONE_PLATE || targetBlock.getType() == Material.IRON_PLATE) {
						main.setPosTp1(targetBlock.getLocation());
						player.sendMessage("First tp set.");
						return true;
					} else {
						player.sendMessage("You must be looking at a stone or iron pressure plate.");
						return true;
					}
				} else {
					sender.sendMessage("Must be a player.");
					return false;
				}
				
			} else if(args[0].equalsIgnoreCase("tp2")) {
				if(sender instanceof Player) {
					Player player = (Player) sender;
					Block targetBlock = player.getTargetBlock((Set<Material>) null, 10);
					if(targetBlock.getType() == Material.STONE_PLATE || targetBlock.getType() == Material.IRON_PLATE) {
						main.setPosTp2(targetBlock.getLocation());
						player.sendMessage("Second tp set.");
						return true;
					} else {
						player.sendMessage("You must be looking at a stone or iron pressure plate.");
						return true;
					}
				} else {
					sender.sendMessage("Must be a player.");
					return false;
				}
				
			} else {
				return false;
			}
		}
		return false;
	}

}
