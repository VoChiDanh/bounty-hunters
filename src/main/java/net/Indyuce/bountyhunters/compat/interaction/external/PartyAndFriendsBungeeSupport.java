package net.Indyuce.bountyhunters.compat.interaction.external;

import de.simonsator.partyandfriends.spigot.api.pafplayers.PAFPlayerManager;
import net.Indyuce.bountyhunters.compat.interaction.InteractionRestriction;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class PartyAndFriendsBungeeSupport implements InteractionRestriction {
    private final PAFPlayerManager manager = PAFPlayerManager.getInstance();

    @Override
    public boolean canInteractWith(InteractionType interaction, Player claimer, OfflinePlayer target) {
        return !manager.getPlayer(claimer.getUniqueId()).isAFriendOf(manager.getPlayer(target.getUniqueId()));
    }
}