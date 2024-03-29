package net.azisaba.tabbukkitbridge.data.providers;

import net.azisaba.tabbukkitbridge.data.DataKey;
import net.azisaba.tabbukkitbridge.util.Util;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.group.Group;
import org.bukkit.entity.Player;

public class LuckPermsDataProvider {
    public static void register() {
        DataKey.LUCKPERMS_PRIMARY_GROUP.register(Util.isPluginEnabledPredicate("LuckPerms"), Util.nonNullMapper(p -> LuckPermsProvider.get().getPlayerAdapter(Player.class).getUser(p).getPrimaryGroup()), DataKey.PRIMARY_GROUP);
        DataKey.PRIMARY_GROUP_WEIGHT.register(Util.isPluginEnabledPredicate("LuckPerms"), Util.nonNullMapper(p -> {
            LuckPerms api = LuckPermsProvider.get();
            String groupName = api.getPlayerAdapter(Player.class).getUser(p).getPrimaryGroup();
            Group group = api.getGroupManager().loadGroup(groupName).join().orElseThrow(AssertionError::new);
            return group.getWeight().orElse(0);
        }));
    }
}
