package cc.funkemunky.anticheat.api.utils;

import cc.funkemunky.anticheat.api.data.PlayerData;
import cc.funkemunky.api.Atlas;
import cc.funkemunky.api.utils.BoundingBox;
import cc.funkemunky.api.utils.ReflectionsUtil;
import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.entity.Player;
import org.bukkit.plugin.*;
import org.bukkit.util.Vector;

import java.io.File;
import java.lang.reflect.Field;
import java.util.*;

public class MiscUtils {

    public static boolean shouldReturnArmAnimation(PlayerData data) {
        return data.isBreakingBlock() || data.getLastBlockPlace().hasNotPassed(2);
    }

    public static float convertToMouseDelta(float value) {
        return ((float) Math.cbrt((value / 0.15f) / 8f) - 0.2f) / .6f;
    }

    public static float getDistanceToGround(PlayerData data, float max) {
        BoundingBox toCheck = data.getBoundingBox().subtract(0, max, 0, 0, 0, 0);

        List<BoundingBox> boxes = Atlas.getInstance().getBlockBoxManager().getBlockBox().getCollidingBoxes(data.getPlayer().getWorld(), toCheck);

        BoundingBox highestBox = boxes.stream().min(Comparator.comparingDouble(box -> 500 - box.minY)).orElse(new BoundingBox(data.getMovementProcessor().getTo().toVector(), data.getMovementProcessor().getTo().toVector()));

        return data.getBoundingBox().minY - highestBox.maxY;
    }

    public static long gcd(long current, long previous) {
        return (previous <= 16384L) ? current : gcd(previous, current % previous);
    }

    public static boolean cancelForFlight(PlayerData data) {
        return cancelForFlight(data, 60);
    }

    public static double getDistanceToBox(Vector vec, BoundingBox box) {
        return vec.distance(getCenterOfBox(box));
    }

    public static Vector getCenterOfBox(BoundingBox box) {
        return box.getMinimum().midpoint(box.getMaximum());
    }

    public static boolean cancelForFlight(PlayerData data, int velocityTicks) {
        val move = data.getMovementProcessor();
        val player = data.getPlayer();
        val velocity = data.getVelocityProcessor();

        return player.getAllowFlight()
                || data.getLastServerPos().hasNotPassed(2)
                || move.getLastVehicle().hasNotPassed(5)
                || move.getLiquidTicks() > 0
                || move.getWebTicks() > 0
                || move.isLagging()
                || !Atlas.getInstance().getBlockBoxManager().getBlockBox().isChunkLoaded(data.getPlayer().getLocation())
                || data.getLastLogin().hasNotPassed(50)
                || move.getClimbTicks() > 0
                || data.getLastBlockPlace().hasNotPassed(5)
                || player.getActivePotionEffects().stream().anyMatch(effect -> effect.toString().toLowerCase().contains("levi"))
                || move.isOnHalfBlock()
                || move.isServerOnGround()
                || move.isRiptiding()
                || move.isOnSlimeBefore()
                || move.getLastRiptide().hasNotPassed(8)
                || move.isPistonsNear()
                || move.getTo().toVector().distance(move.getFrom().toVector()) < 0.005
                || velocity.getLastVelocity().hasNotPassed(velocityTicks)
                || data.getLastAbleToFly().hasNotPassed(150);
    }

    public static void allahAkbar(Player player) {
        Object packet = ReflectionsUtil.newInstance(ReflectionsUtil.getNMSClass("PacketPlayOutExplosion"), player.getEyeLocation().getX(), player.getEyeLocation().getY(), player.getEyeLocation().getZ(), 20, new ArrayList<>(), null);

        ReflectionsUtil.getMethodValue(ReflectionsUtil.getMethod(ReflectionsUtil.getNMSClass("EntityPlayer"), "sendPacket", Object.class), ReflectionsUtil.getEntityPlayer(player), packet);
    }

    public static float wrapAngleTo180_float(float value) {
        value = value % 360.0F;

        if (value >= 180.0F) {
            value -= 360.0F;
        }

        if (value < -180.0F) {
            value += 360.0F;
        }

        return value;
    }

    public static Class<?> getClass(String string) {
        try {
            return Class.forName(string);
        } catch(Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static String unloadPlugin(String pl) {
        PluginManager pm = Bukkit.getServer().getPluginManager();
        SimplePluginManager spm = (SimplePluginManager)pm;
        SimpleCommandMap cmdMap = null;
        List plugins = null;
        Map names = null;
        Map commands = null;
        Map listeners = null;
        boolean reloadlisteners = true;
        if(spm != null) {
            try {
                Field tp = spm.getClass().getDeclaredField("plugins");
                tp.setAccessible(true);
                plugins = (List)tp.get(spm);
                Field arr$ = spm.getClass().getDeclaredField("lookupNames");
                arr$.setAccessible(true);
                names = (Map)arr$.get(spm);

                Field len$;
                try {
                    len$ = spm.getClass().getDeclaredField("listeners");
                    len$.setAccessible(true);
                    listeners = (Map)len$.get(spm);
                } catch (Exception var19) {
                    reloadlisteners = false;
                }

                len$ = spm.getClass().getDeclaredField("commandMap");
                len$.setAccessible(true);
                cmdMap = (SimpleCommandMap)len$.get(spm);
                Field i$ = cmdMap.getClass().getDeclaredField("knownCommands");
                i$.setAccessible(true);
                commands = (Map)i$.get(cmdMap);
            } catch (IllegalAccessException | NoSuchFieldException var20) {
                return "Failed to unload plugin!";
            }
        }

        String var21 = "";
        Plugin[] var22 = Bukkit.getServer().getPluginManager().getPlugins();
        int var23 = var22.length;

        for(int var24 = 0; var24 < var23; ++var24) {
            Plugin p = var22[var24];
            if(p.getDescription().getName().equalsIgnoreCase(pl)) {
                pm.disablePlugin(p);
                var21 = var21 + p.getName() + " ";
                if(plugins != null && plugins.contains(p)) {
                    plugins.remove(p);
                }

                if(names != null && names.containsKey(pl)) {
                    names.remove(pl);
                }

                Iterator it;
                if(listeners != null && reloadlisteners) {
                    it = listeners.values().iterator();

                    while(it.hasNext()) {
                        SortedSet entry = (SortedSet)it.next();
                        Iterator c = entry.iterator();

                        while(c.hasNext()) {
                            RegisteredListener value = (RegisteredListener)c.next();
                            if(value.getPlugin() == p) {
                                c.remove();
                            }
                        }
                    }
                }

                if(cmdMap != null) {
                    it = commands.entrySet().iterator();

                    while(it.hasNext()) {
                        Map.Entry var25 = (Map.Entry) it.next();
                        if(var25.getValue() instanceof PluginCommand) {
                            PluginCommand var26 = (PluginCommand)var25.getValue();
                            if(var26.getPlugin() == p) {
                                var26.unregister(cmdMap);
                                it.remove();
                            }
                        }
                    }
                }
            }
        }

        return var21 + "has been unloaded and disabled!";
    }

    public static void loadPlugin(final String pl) {
        Plugin targetPlugin = null;
        String msg = "";
        final File pluginDir = new File("plugins");
        if (!pluginDir.isDirectory()) {
            return;
        }
        File pluginFile = new File(pluginDir, pl + ".jar");
        if (!pluginFile.isFile()) {
            for (final File f : pluginDir.listFiles()) {
                try {
                    if (f.getName().endsWith(".jar")) {
                        final PluginDescriptionFile pdf = Atlas.getInstance().getPluginLoader().getPluginDescription(f);
                        if (pdf.getName().equalsIgnoreCase(pl)) {
                            pluginFile = f;
                            msg = "(via search) ";
                            break;
                        }
                    }
                }
                catch (InvalidDescriptionException e2) {
                    return;
                }
            }
        }
        try {
            Atlas.getInstance().getServer().getPluginManager().loadPlugin(pluginFile);
            targetPlugin = getPlugin(pl);
            Atlas.getInstance().getServer().getPluginManager().enablePlugin(targetPlugin);
        }
        catch (UnknownDependencyException | InvalidPluginException | InvalidDescriptionException e3) {
            e3.printStackTrace();
        }
    }

    public static String getPlayerID(Player player) {
        if(player != null) {
            return player.getUniqueId().toString();
        }
        return "%%__NONCE__%%";
    }

    private static Plugin getPlugin(final String p) {
        for (final Plugin pl : Atlas.getInstance().getServer().getPluginManager().getPlugins()) {
            if (pl.getDescription().getName().equalsIgnoreCase(p)) {
                return pl;
            }
        }
        return null;
    }

}
