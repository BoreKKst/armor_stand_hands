package com.borekkst.armshands;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public final class ArmsHands extends JavaPlugin implements CommandExecutor {
    private static ArmsHands instance;
    private DatabaseManager databaseManager;
    private ApiManager apiManager;
    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        databaseManager = new DatabaseManager(this);
        databaseManager.initDatabase();
        apiManager = new ApiManager(this);
        apiManager.startApi();
        if (getCommand("armshands") != null) getCommand("armshands").setExecutor(this);
        getServer().getPluginManager().registerEvents(new ArmorStandListener(this), this);
        getLogger().info(ChatColor.GREEN + "ArmsHands успешно запущен! SQLite и API активны.");
    }
    @Override
    public void onDisable() {
        if (apiManager != null) apiManager.stopApi();
        if (databaseManager != null) databaseManager.closeConnection();
        getLogger().info(ChatColor.RED + "ArmsHands выключен.");
    }
    public static ArmsHands getInstance() { return instance; }
    public DatabaseManager getDatabaseManager() { return databaseManager; }
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("armshands.admin")) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.no-permission", "&cНет прав!")));
            return true;
        }
        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            reloadConfig();
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.prefix") + getConfig().getString("messages.reloaded")));
            return true;
        }
        sender.sendMessage(ChatColor.DARK_RED + "=== ArmsHands Admin Menu ===");
        sender.sendMessage(ChatColor.GRAY + "/ah reload - Перезагрузить конфиг");
        sender.sendMessage(ChatColor.GRAY + "Зажми Shift и кликни ПКМ по стойке для активации рук и анимации!");
        return true;
    }
}
