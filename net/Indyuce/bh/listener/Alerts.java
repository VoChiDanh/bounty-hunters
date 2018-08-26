package net.Indyuce.bh.listener;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import net.Indyuce.bh.api.Bounty;
import net.Indyuce.bh.api.PlayerData;
import net.Indyuce.bh.api.event.BountyCreateEvent;
import net.Indyuce.bh.resource.BountyCause;
import net.Indyuce.bh.resource.Message;
import net.Indyuce.bh.util.Utils;
import net.Indyuce.bh.util.VersionUtils;

public class Alerts {
	public static void claimBounty(Player p, Bounty bounty) {

		// message to claimer
		Message.CHAT_BAR.format(ChatColor.YELLOW).send(p);
		Message.BOUNTY_CLAIMED_BY_YOU.format(ChatColor.YELLOW, "%target%", bounty.getTarget().getName(), "%reward%", Utils.format(bounty.getReward())).send(p);

		// compass
		for (OfflinePlayer hunter : bounty.getHuntingPlayers()) {
			if (!hunter.isOnline())
				continue;

			Player h = (Player) hunter;
			h.setCompassTarget(h.getWorld().getSpawnLocation());
		}

		// message to server
		PlayerData playerData = PlayerData.get(p);
		String title = playerData.getString("current-title").equals("") ? "" : ChatColor.LIGHT_PURPLE + "[" + Utils.applySpecialChars(playerData.getString("current-title")) + "] ";
		for (Player t1 : Bukkit.getOnlinePlayers()) {
			VersionUtils.sound(t1, "ENTITY_PLAYER_LEVELUP", 1, 2);
			if (t1 != p)
				// +
				// ChatColor.getLastColors(Utils.msg("bounty-claimed").split(Pattern.quote("%killer%"))[0])
				Message.BOUNTY_CLAIMED.format(ChatColor.YELLOW, "%reward%", Utils.format(bounty.getReward()), "%killer%", title + p.getName(), "%target%", bounty.getTarget().getName()).send(t1);
		}
	}

	public static void newBounty(BountyCreateEvent e) {
		Bounty b = e.getBounty();
		double reward = e.getBounty().getReward();

		String toOnline = e.getCause() == BountyCause.PLAYER ? Message.NEW_BOUNTY_ON_PLAYER.formatRaw(ChatColor.YELLOW, "%creator%", b.getCreator().getName(), "%target%", b.getTarget().getName(), "%reward%", Utils.format(reward)) : (e.getCause() == BountyCause.AUTO_BOUNTY ? Message.NEW_BOUNTY_ON_PLAYER_ILLEGAL.formatRaw(ChatColor.YELLOW, "%target%", b.getTarget().getName(), "%reward%", Utils.format(reward)) : Message.NEW_BOUNTY_ON_PLAYER_UNDEFINED.formatRaw(ChatColor.YELLOW, "%target%", b.getTarget().getName(), "%reward%", Utils.format(reward)));
		String toTarget = e.getCause() == BountyCause.PLAYER ? Message.NEW_BOUNTY_ON_YOU.formatRaw(ChatColor.RED, "%creator%", b.getCreator().getName()) : (e.getCause() == BountyCause.AUTO_BOUNTY ? Message.NEW_BOUNTY_ON_YOU_ILLEGAL.formatRaw(ChatColor.RED) : Message.NEW_BOUNTY_ON_YOU_UNDEFINED.formatRaw(ChatColor.RED));

		for (Player t : Bukkit.getOnlinePlayers()) {
			if (b.getTarget() == t) {
				VersionUtils.sound(t, "ENTITY_ENDERMEN_HURT", 1, 0);
				t.sendMessage(toTarget);
				continue;
			}

			if (b.getCreator() == t) {
				VersionUtils.sound(t, "ENTITY_PLAYER_LEVELUP", 1, 2);
				Message.CHAT_BAR.format(ChatColor.YELLOW).send(t);
				Message.BOUNTY_CREATED.format(ChatColor.YELLOW, "%target%", b.getTarget().getName()).send(t);
				Message.BOUNTY_EXPLAIN.format(ChatColor.YELLOW, "%reward%", Utils.format(reward)).send(t);
				continue;
			}

			VersionUtils.sound(t, "ENTITY_PLAYER_LEVELUP", 1, 2);
			t.sendMessage(toOnline);
		}
	}

	public static void newHunter(Player t, Player h) {
		Message.NEW_HUNTER_ALERT.format(ChatColor.RED, "%hunter%", h.getName()).send(t);
		VersionUtils.sound(t.getLocation(), "ENTITY_ENDERMEN_HURT", 1, 1);
	}

	public static void bountyExpired(Bounty bounty) {
		for (Player p : Bukkit.getOnlinePlayers()) {
			VersionUtils.sound(p, "ENTITY_VILLAGER_NO", 1, 2);
			Message.BOUNTY_EXPIRED.format(ChatColor.YELLOW, "%target%", bounty.getTarget().getName()).send(p);
		}
	}

	public static void bountyChange(BountyCreateEvent e) {
		Bounty b = e.getBounty();
		for (Player p : Bukkit.getOnlinePlayers())
			Message.BOUNTY_CHANGE.format(ChatColor.YELLOW, "%player%", b.getTarget().getName(), "%reward%", Utils.format(b.getReward())).send(p);
		if (b.getTarget().isOnline())
			VersionUtils.sound((Player) b.getTarget(), "ENTITY_ENDERMEN_HURT", 1, 1);
	}
}
