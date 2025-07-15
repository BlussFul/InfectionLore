package com.blussful.lore;

import org.bukkit.plugin.java.JavaPlugin;
import com.blussful.lore.modules.InfectionManager;
import com.blussful.lore.update.Updater;

public class Lore extends JavaPlugin {

    private InfectionManager infectionManager;
    private Updater updater;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        infectionManager = new InfectionManager(this);
        infectionManager.start();

        updater = new Updater(this);

        getCommand("lr").setExecutor(new CommandHandler(this, infectionManager, updater));

        getServer().getPluginManager().registerEvents(infectionManager, this);

        getLogger().info("Lore плагин включен");
    }

    @Override
    public void onDisable() {
        infectionManager.stop();
        getLogger().info("Lore плагин выключен");
    }
}
