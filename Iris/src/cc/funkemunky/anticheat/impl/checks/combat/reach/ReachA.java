package cc.funkemunky.anticheat.impl.checks.combat.reach;

import cc.funkemunky.anticheat.Iris;
import cc.funkemunky.anticheat.api.checks.CancelType;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.data.PlayerData;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.anticheat.api.utils.Verbose;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInUseEntityPacket;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;

@Packets(packets = {Packet.Client.USE_ENTITY})
public class ReachA extends Check {
    private Verbose verbose = new Verbose();

    public ReachA(String name, CancelType cancelType, int maxVL) {
        super(name, cancelType, maxVL);
    }

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        /* A very simple maximum-reach distance check for a player in combat */
        if (getData().getPlayer().getGameMode().toString().contains("CREATIVE") || getData().getPlayer().getAllowFlight()) return;

        WrappedInUseEntityPacket use = new WrappedInUseEntityPacket(packet, getData().getPlayer());

        Player player = getData().getPlayer();

        if (use.getAction().equals(WrappedInUseEntityPacket.EnumEntityUseAction.ATTACK) && use.getEntity() instanceof LivingEntity) {
            LivingEntity entity = (LivingEntity) use.getEntity();

            double delta = player.getEyeLocation().distance(((LivingEntity) use.getEntity()).getEyeLocation()) - 0.3;

            double threshold = player.getEyeLocation().toVector().subtract(new Vector(0, (float) (player.getEyeHeight() / 2), 0)).subtract(((LivingEntity) use.getEntity()).getEyeLocation().toVector().subtract(new Vector(0, (float) ((LivingEntity) use.getEntity()).getEyeHeight() / 2, 0))).angle(player.getEyeLocation().getDirection()) + 0.8f;

            double combinedPing = getData().getTransPing();

            if (entity instanceof Player) { //If the player is not an entity, we want to get its latency factored in too.
                PlayerData dumbassData = Iris.getInstance().getDataManager().getPlayerData(entity.getUniqueId());

                if (dumbassData != null) combinedPing += dumbassData.getTransPing();
            }

            //We want to get the velocities of the players factored in since the resolution of data is poor and latency can make that worse.
            threshold += (player.getVelocity().lengthSquared() + use.getEntity().getVelocity().lengthSquared()) * (2.0 + combinedPing / 100);

            if (delta > threshold) {
                if (verbose.flag(16, 2000L)) { //We add a verbose threshold to prevent any false positives caused by errors in calculation or mishaps with packets.
                    flag(delta + ">-" + threshold, true, true);
                }
            } else {
                verbose.deduct(2);
            }
        }
        return;
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
