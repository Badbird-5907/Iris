package cc.funkemunky.anticheat.impl.checks.movement;

import cc.funkemunky.anticheat.api.checks.CancelType;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.utils.BlockUtils;
import cc.funkemunky.api.utils.ReflectionsUtil;
import lombok.val;
import org.bukkit.block.Block;
import org.bukkit.event.Event;

@Packets(packets = {Packet.Client.LEGACY_POSITION, Packet.Client.LEGACY_POSITION_LOOK, Packet.Client.POSITION, Packet.Client.POSITION_LOOK})
public class SpeedC extends Check {
    public SpeedC(String name, CancelType cancelType, int maxVL) {
        super(name, cancelType, maxVL);
    }

    private float lastMotion;
    private long lastTimeStamp;
    private boolean lastOnGround;
    private boolean lastLastOnGround;
    private int vl;

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        val move = getData().getMovementProcessor();
        val to = move.getTo();
        val from = move.getFrom();
        val onGround = move.isServerOnGround();
        if (!getData().isGeneralCancel() && timeStamp > lastTimeStamp + 5) {
            Block below = BlockUtils.getBlock(to.clone().toLocation(getData().getPlayer().getWorld()).subtract(0, 1, 0));

            val deltaXZ = (float) Math.hypot(to.getX() - from.getX(), to.getZ() - from.getZ());
            val friction = !move.isServerOnGround()|| !below.getType().isSolid() ? 0.68f : ReflectionsUtil.getFriction(below);
            val resistance = move.isServerOnGround() ? friction * 0.91f : 0.91f;
            val predicted = lastMotion * resistance;
            val delta = deltaXZ - predicted;


            if(!onGround && !lastLastOnGround && !lastOnGround) {
                if (delta > 0.028) {
                    if(delta > 0.2f && vl++ > 3) {
                        flag(delta + "", true, true);
                    }
                } else vl -= vl > 0 ? 1 : 0;
                debug("VL:" + vl + "DIFFERENCE: " + delta);
            }

            lastMotion = deltaXZ;
        }
        lastTimeStamp = timeStamp;
        lastLastOnGround = lastOnGround;
        lastOnGround = onGround;
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
