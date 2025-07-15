package com.blussful.lore;

import com.blussful.lore.modules.InfectionManager;
import com.blussful.lore.update.Updater;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.util.Locale;


public class CommandHandler implements CommandExecutor {

    private final Lore plugin;
    private final InfectionManager infectionManager;
    private final Updater updater;

    public CommandHandler(Lore plugin, InfectionManager infectionManager, Updater updater) {
        this.plugin = plugin;
        this.infectionManager = infectionManager;
        this.updater = updater;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("§cИспользование: /lr stats <ник>, /lr set <ник> <уровень>, /lr reload");
            return true;
        }

        String subcommand = args[0].toLowerCase(Locale.ROOT);

        if (subcommand.equals("stats")) {
            if (args.length != 2) {
                sender.sendMessage("§cИспользование: /lr stats <ник>");
                return true;
            }
            Player target = Bukkit.getPlayerExact(args[1]);
            if (target == null) {
                sender.sendMessage("§cИгрок не найден.");
                return true;
            }
            int level = infectionManager.getInfectionLevel(target.getUniqueId());
            sender.sendMessage("§aУровень заражения игрока §f" + target.getName() + "§a: §e" + level);
            return true;
        }

        if (subcommand.equals("set")) {
            if (args.length != 3) {
                sender.sendMessage("§cИспользование: /lr set <ник> <уровень>");
                return true;
            }
            Player target = Bukkit.getPlayerExact(args[1]);
            if (target == null) {
                sender.sendMessage("§cИгрок не найден.");
                return true;
            }
            int level;
            try {
                level = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                sender.sendMessage("§cУровень должен быть числом.");
                return true;
            }
            int maxLevel = plugin.getConfig().getInt("max-infection-level", 100);
            if (level < 0 || level > maxLevel) {
                sender.sendMessage("§cУровень должен быть от 0 до " + maxLevel);
                return true;
            }
            infectionManager.setInfectionLevel(target.getUniqueId(), level);
            sender.sendMessage("§aУровень заражения игрока §f" + target.getName() + "§a установлен на §e" + level);
            target.sendMessage("§5Ваш уровень заражения был изменён администратором на §e" + level);
            return true;
        }

        if (subcommand.equals("reload")) {
            if (!sender.hasPermission("lore.admin")) {
                sender.sendMessage("§cУ вас нет прав на использование этой команды.");
                return true;
            }
            updater.reloadPlugin(sender);
            return true;
        }

        sender.sendMessage("§cНеизвестная команда. Используйте /lr stats <ник>, /lr set <ник> <уровень> или /lr reload");
        return true;
    }
}
