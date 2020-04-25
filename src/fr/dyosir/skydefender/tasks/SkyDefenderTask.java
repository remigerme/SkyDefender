package fr.dyosir.skydefender.tasks;

import java.text.SimpleDateFormat;
import java.util.Map.Entry;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import fr.dyosir.skydefender.SkyDefender;
import fr.dyosir.skydefender.SkyDefenderGame;
import fr.dyosir.skydefender.scoreboard.ScoreboardSign;
import fr.dyosir.skydefender.team.Team;

public class SkyDefenderTask extends BukkitRunnable {
	
	private int timer = 0;
	private int hours = 0;
	private SkyDefender main;
	
	public SkyDefenderTask(SkyDefender skyDefender) {
		this.main = skyDefender;
	}

	@Override
	public void run() {
		
		if(!main.isState(SkyDefenderGame.GAME)) {
			this.cancel();
			return;
		}
				
		if(timer == main.getFallDamageCap()) {
			main.setFallDamageStatus(true);
			for(Team team: main.getTeams()) {
				for(Player player: team.getPlayers()) {
					main.title.sendTitle(player, "Fall damage are now enabled", "", 20);
				}
			}
		} else if(timer == main.getPvpCap()) {
			main.getWorld().setPVP(true);
			for(Team team: main.getTeams()) {
				for(Player player: team.getPlayers()) {
					main.title.sendTitle(player, "PVP ON", "Fight !", 20);
				}
			}

		} else if(timer == main.getBannerCap()) {
			main.setBannerStatus(true);
			for(Team team: main.getTeams()) {
				for(Player player: team.getPlayers()) {
					main.title.sendTitle(player, "Banner ON", "You can now pick up the banner !", 20);
				}
			}
		}
		
		if(timer % 3600 == 0 && timer != 0) {
			hours++;
		}
		
		String nextEvent;
		String timeNextEvent;
		int hoursNextEvent = 0;
		if(!main.isFallDamageStatus()) {
			nextEvent = "Fall damage at :";
			timeNextEvent = "00:15";
		} else if(!main.getWorld().getPVP()) {
			nextEvent = "PVP at : ";
			int pvpCap = main.getPvpCap();
			hoursNextEvent = pvpCap / 3600;
			int r = pvpCap % 3600;
			timeNextEvent = new SimpleDateFormat("mm:ss").format(r * 1000);
		} else if(!main.isBannerStatus()) {
			nextEvent = "Banner at : ";
			int bannerCap = main.getBannerCap();
			hoursNextEvent = bannerCap / 3600;
			int r = bannerCap % 3600;
			timeNextEvent = new SimpleDateFormat("mm:ss").format(r * 1000);
		} else {
			nextEvent = "Hurry up for the win";
			timeNextEvent = "now";
		}
		
		for(Entry<Player, ScoreboardSign> entry: main.getBoards().entrySet()) {
			String timeformat = new SimpleDateFormat("mm:ss").format(timer * 1000);
			entry.getValue().setLine(1, hours + ":" + timeformat);
			entry.getValue().setLine(2, nextEvent);
			entry.getValue().setLine(3, hoursNextEvent + ":" + timeNextEvent);
		}
		timer++;
	}
	
}
