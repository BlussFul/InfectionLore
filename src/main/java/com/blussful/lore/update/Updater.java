package com.blussful.lore.update;

import com.blussful.lore.Lore;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class Updater {

    private final Lore plugin;
    private final String updateUrl = "https://github.com/BlussFul/InfectionLore/blob/main/target/lore-1.0.jar"; // Укажите реальный URL

    public Updater(Lore plugin) {
        this.plugin = plugin;
    }

    public void reloadPlugin(CommandSender sender) {
        sender.sendMessage("§aНачинаю обновление плагина...");

        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    File pluginFolder = plugin.getDataFolder().getParentFile();
                    File pluginFile = new File(pluginFolder, "lore.jar");
                    File backupFile = new File(pluginFolder, "lore_old.jar");

                    // Скачиваем новый файл
                    File newFile = new File(pluginFolder, "lore_new.jar");
                    downloadFile(updateUrl, newFile);

                    // Переименовываем старый файл
                    if (pluginFile.exists()) {
                        if (backupFile.exists()) backupFile.delete();
                        boolean renamed = pluginFile.renameTo(backupFile);
                        if (!renamed) {
                            sender.sendMessage("§cНе удалось сделать резервную копию старого плагина.");
                            return;
                        }
                    }

                    // Переименовываем новый файл в основной
                    boolean renamedNew = newFile.renameTo(pluginFile);
                    if (!renamedNew) {
                        sender.sendMessage("§cНе удалось заменить плагин новым файлом.");
                        return;
                    }

                    sender.sendMessage("§aПлагин обновлен. Перезагрузите сервер для применения обновления.");
                } catch (IOException e) {
                    sender.sendMessage("§cОшибка при обновлении плагина: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }.runTaskAsynchronously(plugin);
    }

    private void downloadFile(String urlStr, File destination) throws IOException {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestProperty("User-Agent", "Mozilla/5.0");
        try (InputStream in = conn.getInputStream();
             FileOutputStream out = new FileOutputStream(destination)) {
            byte[] buffer = new byte[4096];
            int n;
            while ((n = in.read(buffer)) != -1) {
                out.write(buffer, 0, n);
            }
        }
    }
}
