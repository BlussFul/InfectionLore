package com.blussful.lore.modules;

import com.blussful.lore.Lore;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerBedLeaveEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class InfectionManager implements Listener {

    private final Lore plugin;
    private final Map<UUID, Integer> infectionLevels = new HashMap<>();
    private final Random random = new Random();

    private int maxInfectionLevel;
    private int infectionYLevelLow;
    private int infectionYLevelHigh;

    private final List<BukkitRunnable> tasks = new ArrayList<>();

    public InfectionManager(Lore plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    private void loadConfig() {
        maxInfectionLevel = plugin.getConfig().getInt("max-infection-level", 100);
        infectionYLevelLow = plugin.getConfig().getInt("y-threshold-low", 16);
        infectionYLevelHigh = plugin.getConfig().getInt("y-threshold-high", 20);
    }

    public void start() {
        // 1) Каждые 20 минут (24000 тиков) - повышение уровня заражения если низкий уровень Y
        BukkitRunnable task1 = new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    Location loc = player.getLocation();
                    if (loc.getBlockY() <= infectionYLevelLow) {
                        UUID uuid = player.getUniqueId();
                        int currentLevel = infectionLevels.getOrDefault(uuid, 0);
                        if (currentLevel < maxInfectionLevel) {
                            infectionLevels.put(uuid, currentLevel + 1);
                        }
                    }
                }
            }
        };
        task1.runTaskTimer(plugin, 0L, 24000L);
        tasks.add(task1);

        // 2) Каждые 15 минут (18000 тиков) - шанс 10% дать слепоту 3 секунды, если заражение >= 3
        BukkitRunnable task2 = new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    UUID uuid = player.getUniqueId();
                    int level = infectionLevels.getOrDefault(uuid, 0);
                    if (level >= 3) {
                        if (random.nextInt(100) < 10) { // 10% шанс
                            player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 60, 0, false, false, true));
                        }
                        if (level >= 10 && random.nextInt(100) < 10) {
                            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 1, false, false, true));
                        }
                    }
                }
            }
        };
        task2.runTaskTimer(plugin, 0L, 18000L);
        tasks.add(task2);

        // 3) Каждые 30 секунд (600 тиков) - шанс 3% поджечь если заражение > 30 и около бедрока (y <= 5)
        BukkitRunnable task3 = new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    Location loc = player.getLocation();
                    UUID uuid = player.getUniqueId();
                    int level = infectionLevels.getOrDefault(uuid, 0);
                    if (level > 30 && loc.getBlockY() <= 5) {
                        if (random.nextInt(100) < 3) { // 3% шанс
                            player.setFireTicks(40);
                        }
                    }
                }
            }
        };
        task3.runTaskTimer(plugin, 0L, 600L);
        tasks.add(task3);

        // 4) Каждые 2 минуты (2400 тиков) - шанс 15% наложить иссушение 3 уровня на 4 секунды, если заражение > 35 и в темноте (light level <= 7)
        BukkitRunnable task4 = new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    Location loc = player.getLocation();
                    UUID uuid = player.getUniqueId();
                    int level = infectionLevels.getOrDefault(uuid, 0);
                    if (level > 35 && loc.getBlockY() < infectionYLevelHigh) {
                        int lightLevel = loc.getBlock().getLightLevel();
                        if (lightLevel <= 7) {
                            if (random.nextInt(100) < 15) {
                                player.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 80, 2, false, false, true));
                            }
                        }
                    }
                }
            }
        };
        task4.runTaskTimer(plugin, 0L, 2400L);
        tasks.add(task4);
    }

    public void stop() {
        for (BukkitRunnable task : tasks) {
            task.cancel();
        }
        infectionLevels.clear();
    }

    public Map<UUID, Integer> getInfectionLevels() {
        return infectionLevels;
    }

    @EventHandler
    public void onPlayerLeaveBed(PlayerBedLeaveEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        int level = infectionLevels.getOrDefault(uuid, 0);

        if (level > 0 && random.nextInt(100) < 20) { // 20% шанс
            infectionLevels.put(uuid, level - 1);
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("§5кажется, вам стало чутка лучше"));
        }
    }

    public void setInfectionLevel(UUID uuid, int level) {
        infectionLevels.put(uuid, level);
    }

    public int getInfectionLevel(UUID uuid) {
        return infectionLevels.getOrDefault(uuid, 0);
    }
}
