package net.azisaba.tabbukkitbridge.data.providers;

import net.azisaba.tabbukkitbridge.data.DataKey;
import net.azisaba.tabbukkitbridge.util.Util;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.group.Group;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LuckPermsDataProvider {
    private static final Map<String, Integer> PRIMARY_GROUP_WEIGHT = new ConcurrentHashMap<>();

    public static void register() {
        DataKey.LUCKPERMS_PRIMARY_GROUP.register(Util.isPluginEnabledPredicate("LuckPerms"), Util.nonNullMapper(p -> LuckPermsProvider.get().getPlayerAdapter(Player.class).getUser(p).getPrimaryGroup()), DataKey.PRIMARY_GROUP);
        DataKey.PRIMARY_GROUP_WEIGHT.register(Util.isPluginEnabledPredicate("LuckPerms"), Util.nonNullMapper(p -> {
            LuckPerms api = LuckPermsProvider.get();
            String groupName = api.getPlayerAdapter(Player.class).getUser(p).getPrimaryGroup();
            if (PRIMARY_GROUP_WEIGHT.containsKey(groupName)) {
                return PRIMARY_GROUP_WEIGHT.get(groupName);
            }
            Group group = api.getGroupManager().loadGroup(groupName).join().orElseThrow(AssertionError::new);
            int weight = group.getWeight().orElse(0);
            PRIMARY_GROUP_WEIGHT.put(groupName, weight);
            return weight;
        }));
    }
}
