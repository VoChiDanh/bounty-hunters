package net.Indyuce.bountyhunters.api.player;

import net.Indyuce.bountyhunters.BountyHunters;
import net.Indyuce.bountyhunters.api.*;
import net.Indyuce.bountyhunters.api.event.HunterLevelUpEvent;
import net.Indyuce.bountyhunters.api.language.Language;
import net.Indyuce.bountyhunters.api.language.Message;
import net.Indyuce.bountyhunters.api.player.reward.BountyAnimation;
import net.Indyuce.bountyhunters.api.player.reward.HunterTitle;
import net.Indyuce.bountyhunters.api.player.reward.LevelUpItem;
import net.Indyuce.bountyhunters.manager.PlayerDataManager;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.*;
import java.util.logging.Level;

public class PlayerData implements OfflinePlayerData {

    private final OfflinePlayer offline;
    private final List<UUID> redeemHeads = new ArrayList<>();
    // Player data that must be saved when the server shuts down
    private int level, successful, claimed, illegalStreak, illegalKills;
    private BountyAnimation animation;
    private HunterTitle title;
    // Temp stuff that is not being saved when the server closes
    private long lastBounty, lastTarget, lastSelect;
    private PlayerHunting hunting;

    public PlayerData(OfflinePlayer player) {
        this.offline = player;
    }

    /**
     * @deprecated Use {@link PlayerDataManager#getLoaded()} instead
     */
    @Deprecated
    public static Collection<PlayerData> getLoaded() {
        return BountyHunters.getInstance().getPlayerDataManager().getLoaded();
    }

    public static PlayerData get(OfflinePlayer player) {
        return BountyHunters.getInstance().getPlayerDataManager().get(player.getUniqueId());
    }

    /**
     * @deprecated Use {@link PlayerDataManager#load(OfflinePlayer)} instead
     */
    @Deprecated
    public static void load(OfflinePlayer player) {
        BountyHunters.getInstance().getPlayerDataManager().load(player);
    }

    public static boolean isLoaded(UUID uuid) {
        return BountyHunters.getInstance().getPlayerDataManager().isLoaded(uuid);
    }

    public OfflinePlayer getOfflinePlayer() {
        return offline;
    }

    public long getLastBounty() {
        return lastBounty;
    }

    public long getLastTarget() {
        return lastTarget;
    }

    public UUID getUniqueId() {
        return offline.getUniqueId();
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int value) {
        level = Math.max(0, value);
    }

    public int getSuccessfulBounties() {
        return successful;
    }

    public void setSuccessfulBounties(int value) {
        successful = Math.max(0, value);
    }

    public int getClaimedBounties() {
        return claimed;
    }

    public void setClaimedBounties(int value) {
        claimed = Math.max(0, value);
    }

    public BountyAnimation getAnimation() {
        return animation;
    }

    public void setAnimation(BountyAnimation animation) {
        this.animation = animation;
    }

    public HunterTitle getTitle() {
        return title;
    }

    public void setTitle(HunterTitle title) {
        this.title = title;
    }

    public int getBountiesNeededToLevelUp() {
        int needed = BountyHunters.getInstance().getLevelManager().getBountiesPerLevel();
        return needed - (claimed % needed);
    }

    public String getLevelProgressBar() {
        StringBuilder advancement = new StringBuilder();
        int needed = BountyHunters.getInstance().getLevelManager().getBountiesPerLevel();
        for (int j = 0; j < needed; j++)
            advancement.append(getClaimedBounties() % needed > j ? ChatColor.GREEN : ChatColor.WHITE).append(AltChar.square);
        return advancement.toString();
    }

    /**
     * @return Item with no texture, the skull owner is set using
     * an async task when the inventory is opened otherwise
     * texture requests can freeze the main server thread.
     */
    public ItemStack getProfileItem() {
        ItemStack profile = CustomItem.PROFILE.toItemStack().clone();
        SkullMeta meta = (SkullMeta) profile.getItemMeta();
        meta.setDisplayName(meta.getDisplayName().replace("{name}", offline.getName()).replace("{level}", "" + getLevel()));
        List<String> profileLore = meta.getLore();

        String title = hasTitle() ? getTitle().format() : Language.NO_TITLE.format();
        profileLore.replaceAll(s -> s.replace("{level_progress}", getLevelProgressBar()).replace("{claimed_bounties}", "" + getClaimedBounties())
                .replace("{successful_bounties}", "" + getSuccessfulBounties()).replace("{current_title}", title)
                .replace("{level}", "" + getLevel()));

        meta.setLore(profileLore);
        profile.setItemMeta(meta);

        return profile;
    }

    public int getIllegalKillStreak() {
        return illegalStreak;
    }

    public void setIllegalKillStreak(int value) {
        illegalStreak = Math.max(0, value);
    }

    public int getIllegalKills() {
        return illegalKills;
    }

    public void setIllegalKills(int value) {
        illegalKills = Math.max(0, value);
    }

    public boolean hasUnlocked(LevelUpItem item) {
        return level >= item.getUnlockLevel();
    }

    public boolean hasAnimation() {
        return animation != null;
    }

    public boolean hasTitle() {
        return title != null;
    }

    public boolean canSelectItem() {
        return lastSelect + 3000 < System.currentTimeMillis();
    }

    public void log(String... message) {
        for (String line : message)
            BountyHunters.getInstance().getLogger().log(Level.WARNING, "[Player Data] " + offline.getName() + ": " + line);
    }

    public void setLastBounty() {
        lastBounty = System.currentTimeMillis();
    }

    public void setLastTarget() {
        lastTarget = System.currentTimeMillis();
    }

    public void setLastSelect() {
        lastSelect = System.currentTimeMillis();
    }

    public void addLevels(int value) {
        setLevel(level + value);
    }

    @Override
    public void addSuccessfulBounties(int value) {
        setSuccessfulBounties(getSuccessfulBounties() + value);
    }

    public void addClaimedBounties(int value) {
        setClaimedBounties(claimed + value);
    }

    public void addIllegalKills(int value) {
        setIllegalKills(illegalKills + value);
        setIllegalKillStreak(illegalStreak + value);
    }

    public void addRedeemableHead(UUID uuid) {
        redeemHeads.add(uuid);
    }

    public void removeRedeemableHead(UUID uuid) {
        redeemHeads.remove(uuid);
    }

    public List<UUID> getRedeemableHeads() {
        return redeemHeads;
    }

    @Override
    public void givePlayerHead(OfflinePlayer owner) {

        if (!offline.isOnline()) {
            redeemHeads.add(owner.getUniqueId());
            return;
        }

        Player player = offline.getPlayer();
        if (player.getInventory().firstEmpty() == -1) {
            redeemHeads.add(owner.getUniqueId());
            Message.MUST_REDEEM_HEAD.format("target", owner.getName()).send(player);
            return;
        }

        player.getInventory().addItem(Utils.getHead(owner));
        Message.OBTAINED_HEAD.format("target", owner.getName()).send(player);
    }

    public void refreshLevel(Player player) {
        while (levelUp(player))
            ;
    }

    private boolean levelUp(Player player) {
        int nextLevel = getLevel() + 1;
        int neededBounties = nextLevel * BountyHunters.getInstance().getLevelManager().getBountiesPerLevel();
        if (getClaimedBounties() < neededBounties)
            return false;

        Bukkit.getPluginManager().callEvent(new HunterLevelUpEvent(player, nextLevel));
        Message.LEVEL_UP.format("level", nextLevel, "bounties", BountyHunters.getInstance().getLevelManager().getBountiesPerLevel()).send(player);

        final List<String> chatDisplay = new ArrayList<>();

        // Titles
        for (HunterTitle title : BountyHunters.getInstance().getLevelManager().getTitles())
            if (nextLevel == title.getUnlockLevel())
                chatDisplay.add(title.format());

        // Death anims
        for (BountyAnimation anim : BountyHunters.getInstance().getLevelManager().getAnimations())
            if (nextLevel == anim.getUnlockLevel())
                chatDisplay.add(anim.format());

        // Send commands
        if (BountyHunters.getInstance().getLevelManager().hasCommands(nextLevel))
            BountyHunters.getInstance().getLevelManager().getCommands(nextLevel).forEach(
                    cmd -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), BountyHunters.getInstance().getPlaceholderParser().parse(player, cmd)));

        // Money
        final double moneyEarned = BountyHunters.getInstance().getLevelManager().calculateLevelMoney(nextLevel);
        BountyHunters.getInstance().getEconomy().depositPlayer(player, moneyEarned);

        // Send json list
        StringBuilder jsonList = new StringBuilder(moneyEarned > 0 ? "\n" + Language.LEVEL_UP_REWARD_MONEY.format("amount", new NumberFormat().format(moneyEarned)) : "");
        for (String s : chatDisplay)
            jsonList.append("\n").append(Language.LEVEL_UP_REWARD.format("reward", AltChar.apply(s)));
        player.spigot().sendMessage(ChatMessageType.CHAT, TextComponent.fromLegacyText("{\"text\":\"" + ChatColor.YELLOW + Language.LEVEL_UP_REWARDS.format()
                + "\",\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"" + jsonList.substring(1) + "\"}}}"));

        setLevel(nextLevel);
        return true;
    }

    public boolean isHunting() {
        return hunting != null;
    }

    @Nullable
    public PlayerHunting getHunting() {
        return Objects.requireNonNull(hunting, "Player is not hunting");
    }

    public void setHunting(@NotNull Bounty bounty) {
        Validate.notNull(bounty, "Bounty cannot be null");
        Validate.isTrue(!isHunting(), "Player is already hunting");

        hunting = new PlayerHunting(bounty);
    }

    public void stopHunting() {
        Validate.notNull(hunting, "Player is not hunting");

        // Close and collect garbage
        if (hunting.isCompassActive())
            hunting.disableCompass();
        hunting = null;
    }

    @Override
    public boolean equals(Object object) {
        return object instanceof PlayerData && ((PlayerData) object).getUniqueId().equals(getUniqueId());
    }

    @Override
    public String toString() {
        return "{Level=" + level + ", ClaimedBounties=" + claimed + ", SuccessfulBounties=" + successful + ", IllegalKills=" + illegalKills
                + ", IllegalKillStreak=" + illegalStreak + (hasTitle() ? ", Title=" + title.getId() : "")
                + (hasAnimation() ? ", Quote=" + animation.getId() : "") + ", RedeemHeads=" + redeemHeads + "}";
    }

    /**
     * Caution: this method does NOT save any of the player data. You MUST save
     * the player data using saveFile() before unloading the player data from
     * the map.
     *
     * @deprecated Use {@link PlayerDataManager#unload(UUID)} instead
     */
    @Deprecated
    public void unload() {
        BountyHunters.getInstance().getPlayerDataManager().unload(getUniqueId());
    }
}
