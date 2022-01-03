package net.azisaba.tabBukkitBridge.data.providers;

import net.azisaba.tabBukkitBridge.data.DataKey;
import net.azisaba.tabBukkitBridge.util.Util;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;

public class VaultDataProvider {
    public static void register() {
        if (Bukkit.getPluginManager().isPluginEnabled("Vault")) {
            Permission perm = Bukkit.getServicesManager().getRegistration(Permission.class).getProvider();
            Chat chat = Bukkit.getServicesManager().getRegistration(Chat.class).getProvider();
            Economy economy = Bukkit.getServicesManager().getRegistration(Economy.class).getProvider();
            DataKey.PRIMARY_GROUP.register(Util.isPluginEnabledPredicate("Vault"), perm::getPrimaryGroup);
            DataKey.PREFIX.register(Util.isPluginEnabledPredicate("Vault"), chat::getPlayerPrefix);
            DataKey.SUFFIX.register(Util.isPluginEnabledPredicate("Vault"), chat::getPlayerSuffix);
            DataKey.MONEY.register(Util.isPluginEnabledPredicate("Vault"), economy::getBalance);
            DataKey.MONEY_FORMATTED.register(Util.isPluginEnabledPredicate("Vault"), p -> Util.formatWithCommas(economy.getBalance(p)));
        }
    }
}
