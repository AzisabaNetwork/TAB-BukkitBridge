package net.azisaba.tabBukkitBridge.data.providers;

import net.azisaba.tabBukkitBridge.data.DataKey;
import net.azisaba.tabBukkitBridge.data.Skip;
import net.azisaba.tabBukkitBridge.util.Util;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Team;

import java.util.Locale;

public class PlayerDataProvider {
    public static void register() {
        DataKey.INVISIBLE.register(p -> true, Util.nonNullMapper(p -> p.hasPotionEffect(PotionEffectType.INVISIBILITY)));
        DataKey.WORLD.register(p -> true, Util.nonNullMapper(p -> p.getWorld().getName()));
        DataKey.DISPLAY_NAME.register(p -> true, Util.nonNullMapper(Player::getDisplayName));
        DataKey.NAME.register(p -> true, Util.nonNullMapper(Player::getName));
        DataKey.POSITION_X.register(p -> true, Util.nonNullMapper(p -> p.getLocation().getX()));
        DataKey.POSITION_Y.register(p -> true, Util.nonNullMapper(p -> p.getLocation().getY()));
        DataKey.POSITION_Z.register(p -> true, Util.nonNullMapper(p -> p.getLocation().getZ()));
        DataKey.GAMEMODE.register(p -> true, Util.nonNullMapper(p -> p.getGameMode().name().toLowerCase(Locale.ROOT)));
        DataKey.VANISHED.register(p -> true, Util.nonNullMapper(p -> {
            if (p.hasMetadata("vanished") && !p.getMetadata("vanished").isEmpty()) {
                return p.getMetadata("vanished").get(0).asBoolean();
            }
            throw Skip.SKIP;
        }));
        DataKey.TEAM_NAME.register(p -> true, Util.nonNullMapper(p -> Util.nonNullMap(Util.nonNullMap(p.getScoreboard(), s -> s.getEntryTeam(p.getName())), Team::getName)));
    }
}
