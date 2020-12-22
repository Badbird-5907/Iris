package cc.funkemunky.anticheat.impl.commands.iris.arguments;

import cc.funkemunky.anticheat.Iris;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.data.PlayerData;
import cc.funkemunky.api.commands.FunkeArgument;
import cc.funkemunky.api.commands.FunkeCommand;
import cc.funkemunky.api.utils.Color;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class DebugArgument extends FunkeArgument {

    public DebugArgument(FunkeCommand parent, String name, String display, String description, String... permission) {
        super(parent, name, display, description, permission);

        addTabComplete(2, "none");
        addTabComplete(2, "box");
        List<String> checks = new ArrayList<>();
        Iris.getInstance().getCheckManager().getChecks().forEach(check -> checks.add(check.getName().replaceAll(" ", "_")));

        String[] checkArray = new String[checks.size()];

        for (int i = 0; i < checks.size(); i++) {
            checkArray[i] = checks.get(i);
        }
        addTabComplete(2, checkArray);
    }

    @Override
    public void onArgument(CommandSender sender, Command command, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            PlayerData data = Iris.getInstance().getDataManager().getPlayerData(player.getUniqueId());

            if (data != null) {
                if(args.length > 1) {
                    if (args[1].equalsIgnoreCase("box")) {
                        data.setDebuggingBox(!data.isDebuggingBox());
                        sender.sendMessage(Color.Gray + "Set box debug to: " + Color.White + data.isDebuggingBox());
                    } else if(args[1].equalsIgnoreCase("none")) {
                        data.setDebuggingCheck(null);
                        data.setDebuggingPlayer(null);
                        sender.sendMessage(Color.Red + "Stopped any debug messages from being sent to you.");
                    } else {
                        Check check = Iris.getInstance().getCheckManager().getCheck(args[1].replaceAll("_", " "));


                        if (check == null) {
                            sender.sendMessage(Color.Red + "The check \"" + args[1] + "\" does not exist!");
                            return;
                        }

                        if (args.length == 2) {
                            data.setDebuggingPlayer(player.getUniqueId());
                            data.setDebuggingCheck(check);

                            sender.sendMessage(Color.Green + "You are now debugging yourself on check " + check.getName() + "!");
                        } else {
                            Player target = Bukkit.getPlayer(args[2]);

                            if (target != null) {
                                data.setDebuggingPlayer(target.getUniqueId());
                                data.setDebuggingCheck(check);
                                sender.sendMessage(Color.Green + "You are now debugging " + target.getName() + " on check " + check.getName() + "!");
                            } else {
                                sender.sendMessage(Color.Red + "The player \"" + args[2] + "\" is not online!");
                            }
                        }
                    }
                } else {
                    sender.sendMessage(getParent().getCommandMessages().getErrorColor() + getParent().getCommandMessages().getInvalidArguments());
                }
            } else {
                sender.sendMessage(Color.Red + "There was an error trying to find your data object.");
            }
        } else {
            sender.sendMessage(Color.Red + "You cannot debug as a non-player.");
        }
    }
}
