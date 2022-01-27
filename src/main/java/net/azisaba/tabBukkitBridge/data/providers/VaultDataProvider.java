package net.azisaba.tabBukkitBridge.data.providers;

import net.azisaba.tabBukkitBridge.data.DataKey;
import net.azisaba.tabBukkitBridge.util.Util;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;

import java.util.Objects;

public class VaultDataProvider {
    public static void register() {
        if (Bukkit.getPluginManager().isPluginEnabled("Vault")) {
            Permission perm = Bukkit.getServicesManager().load(Permission.class);
            Chat chat = Bukkit.getServicesManager().load(Chat.class);
            Economy economy = Bukkit.getServicesManager().load(Economy.class);
            DataKey.PRIMARY_GROUP.register(p -> perm != null, p -> Objects.requireNonNull(perm).getPrimaryGroup(p));
            DataKey.PREFIX.register(p -> chat != null, p -> Objects.requireNonNull(chat).getPlayerPrefix(p), DataKey.VAULT_PREFIX);
            DataKey.SUFFIX.register(p -> chat != null, p -> Objects.requireNonNull(chat).getPlayerSuffix(p), DataKey.VAULT_SUFFIX);
            DataKey.MONEY.register(p -> economy != null, p -> Objects.requireNonNull(economy).getBalance(p));
            DataKey.MONEY_FORMATTED.register(p -> economy != null, p -> Util.formatWithCommas(Objects.requireNonNull(economy).getBalance(p)));
        }
    }
}
