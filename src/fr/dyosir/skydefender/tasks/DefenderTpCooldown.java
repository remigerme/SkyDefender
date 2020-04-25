package fr.dyosir.skydefender.tasks;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import fr.dyosir.skydefender.SkyDefender;
import fr.dyosir.skydefender.SkyDefenderGame;

public class DefenderTpCooldown extends BukkitRunnable {
	
	private SkyDefender main;
	
	public DefenderTpCooldown(SkyDefender skyDefender) {
		this.main = skyDefender;
	}

	@Override
	public void run() {
		
		if(main.isState(SkyDefenderGame.FINISH)) {
			this.cancel();
			return;
		}
		
		List<Player> playersToRemove = new ArrayList<>();
		for(Entry<Player, Integer> entry : main.getPlayersOnCd().entrySet()) {
			int timeleft = entry.getValue();
			if(timeleft > 1) {
				main.getPlayersOnCd().put(entry.getKey(), timeleft - 1);
			} else {
				playersToRemove.add(entry.getKey());
			}
		}
		for(Player player: playersToRemove) {
			main.getPlayersOnCd().remove(player);
		}
	}

}
