package net.Indyuce.bountyhunters.command;

import net.Indyuce.bountyhunters.api.language.Message;
import net.Indyuce.bountyhunters.gui.Leaderboard;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class HuntersCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command is for players only.");
            return true;
        }

        if (!sender.hasPermission("bountyhunters.leaderboard")) {
            Message.NOT_ENOUGH_PERMS.format().send(sender);
            return true;
        }

        new Leaderboard((Player) sender).open();
        return true;
    }
}
