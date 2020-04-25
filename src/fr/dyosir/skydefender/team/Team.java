package fr.dyosir.skydefender.team;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class Team {

	private String name;
	private String tag;
	private byte woolData;
	private List<Player> players = new ArrayList<>();
	
	public Team(String name, String tag, byte woolData) {
		this.name = name;
		this.tag = tag;
		this.woolData = woolData;
	}
	
	public ItemStack getIcon() {
		ItemStack i = new ItemStack(Material.WOOL, 1, woolData);
		ItemMeta iM = i.getItemMeta();
		iM.setDisplayName("Join the " + tag + name + "§f team.");
		i.setItemMeta(iM);
		return i;
	}
	
	public void addPlayer(Player player) {
		players.add(player);
	}
	
	public void removePlayer(Player player) {
		players.remove(player);
	}
	
	public List<Player> getPlayers() {
		return players;
	}
	
	public List<String> getPlayersName() {
		List<String> playersName = new ArrayList<>();
		for(Player player: players) {
			playersName.add(player.getName());
		}
		return playersName;
	}
	
	public Player getOldPlayer(String name) {
		Player player = players.get(0);
		for(Player iplayer: players) {
			if(iplayer.getName() == name) {
				player = iplayer;
			}
		}
		return player;
	}
	
	public int getSize() {
		return players.size();
	}

	public String getName() {
		return name;
	}

	public String getTag() {
		return tag;
	}

	public byte getWoolData() {
		return woolData;
	}

}
