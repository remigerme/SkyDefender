package fr.dyosir.skydefender;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Difficulty;
import org.bukkit.FireworkEffect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.plugin.java.JavaPlugin;

import fr.dyosir.skydefender.commands.CommandPos;
import fr.dyosir.skydefender.commands.CommandRules;
import fr.dyosir.skydefender.commands.CommandGame;
import fr.dyosir.skydefender.commands.CommandTeam;
import fr.dyosir.skydefender.scoreboard.ScoreboardSign;
import fr.dyosir.skydefender.tasks.DefenderTpCooldown;
import fr.dyosir.skydefender.tasks.SkyDefenderTask;
import fr.dyosir.skydefender.team.Team;

public class SkyDefender extends JavaPlugin {
	
	private SkyDefenderGame current;
	private boolean soloMode = false;
	private boolean spectatorMode = true;
	private boolean noHunger = true;
	private boolean friendlyFire = true;
	private int fallDamageCap;
	private int pvpCap;
	private int bannerCap;
	private boolean fallDamageStatus = false;
	private boolean bannerStatus = false;
	private int teamSize = 4;
	private int teamDefenderSize = teamSize + 2;
	private String worldName;
	private World world;
	private int worldWidth;
	private Location defenderSpawn;
	private Location posTp1;
	private Location posTp2;
	private Location posBanner;
	private List<Team> teams = new ArrayList<>();
	private int defenderTpCd;
	private HashMap<Player, Integer> playersOnCd = new HashMap<Player, Integer>();
	private SkyDefenderTask eventCap;
	private DefenderTpCooldown defenderTpCooldown;
	private Map<Player, ScoreboardSign> boards = new HashMap<>();
	public SkyDefenderTitles title = new SkyDefenderTitles();
	
	@Override
	public void onEnable() {
		current = SkyDefenderGame.WAITING;
		
		getServer().getPluginManager().registerEvents(new SkyDefenderListeners(this), this);
		
		getCommand("pos").setExecutor(new CommandPos(this));
		getCommand("rules").setExecutor(new CommandRules(this));
		getCommand("game").setExecutor(new CommandGame(this));
		getCommand("team").setExecutor(new CommandTeam(this));

		getConfig().options().copyDefaults(true);
		saveConfig();
		
		ConfigurationSection sectionTeams = getConfig().getConfigurationSection("teams");
		for(String team: sectionTeams.getKeys(false)) {
			String name = sectionTeams.getString(team + ".name");
			String tag = sectionTeams.getString(team + ".color");
			byte data = (byte) sectionTeams.getInt(team + ".data");
			teams.add(new Team(name, tag, data));
		}
		
		ConfigurationSection sectionParams = getConfig().getConfigurationSection("params");
		worldName = sectionParams.getString("worldname");
		teamSize = sectionParams.getInt("teamsize");
		teamDefenderSize = sectionParams.getInt("teamdefendersize");
		fallDamageCap = sectionParams.getInt("falldamagecap");
		pvpCap = sectionParams.getInt("pvpcap");
		bannerCap = sectionParams.getInt("bannercap");
		worldWidth = sectionParams.getInt("worldwidth");
		defenderTpCd = sectionParams.getInt("defendertpcd");
		
		world = Bukkit.getWorld(worldName);
		defenderSpawn = new Location(world, 0, 180, 0);
		
		world.setDifficulty(Difficulty.PEACEFUL);
		world.setPVP(false);
		world.setTime(0);
		
		// avoiding null pointer exception
		posTp1 = new Location(world, 0, 120, 0);
		posTp2 = new Location(world, 0, 80, 0);
		posBanner = new Location(world, 0, 160, 0);
	}
	
	@Override
	public void onDisable() {
		System.out.println("SkyDefender plugin has stopped.");
	}
	
	public void setState(SkyDefenderGame state) {
		current = state;
	}
	
	public boolean isState(SkyDefenderGame state) {
		return current == state;
	}
	
	public void addPlayer(Player player, Team team) {
		
		String tag = team.getTag() + team.getName();
		
		if(team.getPlayers().contains(player)) {
			player.sendMessage("You already are in this team.");
			return;
		}
		
		if(team.getName().equalsIgnoreCase("defender")) {
			if(team.getSize() >= teamDefenderSize) {
				player.sendMessage("The " + tag + "§f team is full.");
				return;
			}
		} else {
			if(team.getSize() >= teamSize) {
				player.sendMessage("The " + tag + "§f team if full.");
				return;
			}
		}
				
		team.addPlayer(player);
		player.setPlayerListName(team.getTag() + player.getName());
		player.sendMessage("You join the " + tag + "§f team.");
	}
	
	public void removePlayer(Player player) {
		for(Team team: teams) {
			if(team.getPlayers().contains(player)) {
				team.removePlayer(player);
				player.setPlayerListName("§f" + player.getName());
			}
		}
	}
	
	public void randomTeam(Player player) {
		boolean playerInTeam = false;
		Random rand = new Random();
		while(!playerInTeam) {
			Team team = teams.get(0);
			if(soloMode) {
				if(rand.nextInt(2) == 0) {
					team = teams.get(0);
				} else {
					team = teams.get(8);
				}
			} else {
				int nb = rand.nextInt(9);
				team = teams.get(nb);
			}
			if((team.getName().equalsIgnoreCase("defender") && team.getSize() < teamDefenderSize) || team.getSize() < teamSize) {
				team.addPlayer(player);
				playerInTeam = true;
				player.setPlayerListName(team.getTag() + player.getName());
				player.sendMessage("You join the " + team.getTag() + team.getName() + "§f team.");
			}
		}			
	}
		
	public void startEventCap() {
		eventCap = new SkyDefenderTask(this);
		eventCap.runTaskTimer(this, 20, 20);
	}
	
	public void startDefenderTpCooldown() {
		defenderTpCooldown = new DefenderTpCooldown(this);
		defenderTpCooldown.runTaskTimer(this, 20, 20);
	}
	
	public void win(Team winner) {
		spectatorMode = true;
		current = SkyDefenderGame.FINISH;
		for(Team team: teams) {
			for(Player player: team.getPlayers()) {
				title.sendTitle(player, winner.getTag() + winner.getName() + " team wins !", "Thanks all for playing, gg guys", 40);
				if(team != winner) {
					player.setGameMode(GameMode.SPECTATOR);
				}
			}
		}
		for(int i = 0; i < 50; i++) {
			Random rand = new Random();
			for(Player player: winner.getPlayers()) {
				double x = player.getLocation().getX() + rand.nextDouble() * 10;
				double y = player.getLocation().getY() + rand.nextDouble() * 10;
				double z = player.getLocation().getZ() + rand.nextDouble() * 10;
				spawnFirefork(new Location(world, x, y, z));
			}
		}
	}
	
	public void win(Player winner) {
		spectatorMode = true;
		current = SkyDefenderGame.FINISH;
		for(Team team: teams) {
			for(Player player: team.getPlayers()) {
				title.sendTitle(player, winner.getName() + " wins !", "Thanks all for playing, gg guys", 40);
				if(player != winner) {
					player.setGameMode(GameMode.SPECTATOR);
				}
			}
		}
		for(int i = 0; i < 50; i++) {
			Random rand = new Random();
			double x = winner.getLocation().getX() + rand.nextDouble() * 10;
			double y = winner.getLocation().getY() + rand.nextDouble() * 10;
			double z = winner.getLocation().getZ() + rand.nextDouble() * 10;
			spawnFirefork(new Location(world, x, y, z));
		}
	}
	
	public void spawnFirefork(Location loc) {
		Firework fw = world.spawn(loc, Firework.class);
		FireworkMeta fwm = fw.getFireworkMeta();
		FireworkEffect.Builder builder = FireworkEffect.builder();
		
		Random rand = new Random();
	    builder.withColor(getRandomColors(rand.nextInt(5)));
	    builder.withFade(getRandomColors(rand.nextInt(3)));
	    builder.flicker(rand.nextBoolean());
	    builder.trail(rand.nextBoolean());
	    builder.with(FireworkEffect.Type.values()[rand.nextInt(5)]);

	    fwm.setPower(1 + rand.nextInt(4));

	    fwm.addEffect(builder.build());
	    fw.setFireworkMeta(fwm);
	}
	
	public Color getRandomColors(int arg) {
		Color color = Color.AQUA;
		switch(arg) {
		case 0:
			color = Color.AQUA;
			break;
		case 1:
			color = Color.GREEN;
			break;
		case 2:
			color = Color.NAVY;
			break;
		case 3:
			color = Color.ORANGE;
			break;
		case 4:
			color = Color.PURPLE;
			break;
		case 5:
			color = Color.YELLOW;
			break;
		}
		return color;
	}
	
	public List<Team> getTeams() {
		return teams;
	}

	public boolean isSpectatorMode() {
		return spectatorMode;
	}

	public void setSpectatorMode(boolean spectatorMode) {
		this.spectatorMode = spectatorMode;
	}

	public int getTeamSize() {
		return teamSize;
	}

	public void setTeamSize(int teamSize) {
		this.teamSize = teamSize;
	}

	public String getWorldName() {
		return worldName;
	}

	public void setWorldName(String worldName) {
		this.worldName = worldName;
	}

	public boolean isNoHunger() {
		return noHunger;
	}

	public void setNoHunger(boolean noHunger) {
		this.noHunger = noHunger;
	}

	public int getFallDamageCap() {
		return fallDamageCap;
	}

	public void setFallDamageCap(int fallDamageCap) {
		this.fallDamageCap = fallDamageCap;
	}

	public int getPvpCap() {
		return pvpCap;
	}

	public void setPvpCap(int pvpCap) {
		this.pvpCap = pvpCap;
	}

	public int getBannerCap() {
		return bannerCap;
	}

	public void setBannerCap(int bannerCap) {
		this.bannerCap = bannerCap;
	}

	public boolean isFallDamageStatus() {
		return fallDamageStatus;
	}

	public void setFallDamageStatus(boolean fallDamageStatus) {
		this.fallDamageStatus = fallDamageStatus;
	}

	public boolean isBannerStatus() {
		return bannerStatus;
	}

	public void setBannerStatus(boolean bannerStatus) {
		this.bannerStatus = bannerStatus;
	}

	public Location getDefenderSpawn() {
		return defenderSpawn;
	}

	public void setDefenderSpawn(Location defenderSpawn) {
		this.defenderSpawn = defenderSpawn;
	}
	
	public World getWorld() {
		return world;
	}

	public int getWorldWidth() {
		return worldWidth;
	}

	public void setWorldWidth(int worldWidth) {
		this.worldWidth = worldWidth;
	}

	public Location getPosBanner() {
		return posBanner;
	}

	public void setPosBanner(Location posBanner) {
		this.posBanner = posBanner;
	}

	public Location getPosTp1() {
		return posTp1;
	}

	public void setPosTp1(Location posTp1) {
		this.posTp1 = posTp1;
	}

	public Location getPosTp2() {
		return posTp2;
	}

	public void setPosTp2(Location posTp2) {
		this.posTp2 = posTp2;
	}

	public HashMap<Player, Integer> getPlayersOnCd() {
		return playersOnCd;
	}

	public void setPlayersOnCd(HashMap<Player, Integer> playersOnCd) {
		this.playersOnCd = playersOnCd;
	}

	public int getDefenderTpCd() {
		return defenderTpCd;
	}

	public void setDefenderTpCd(int defenderTpCd) {
		this.defenderTpCd = defenderTpCd;
	}

	public boolean isFriendlyFire() {
		return friendlyFire;
	}

	public void setFriendlyFire(boolean friendlyFire) {
		this.friendlyFire = friendlyFire;
	}

	public boolean isSoloMode() {
		return soloMode;
	}

	public void setSoloMode(boolean soloMode) {
		this.soloMode = soloMode;
	}

	public Map<Player, ScoreboardSign> getBoards() {
		return boards;
	}

	public void setBoards(Map<Player, ScoreboardSign> boards) {
		this.boards = boards;
	}

}
