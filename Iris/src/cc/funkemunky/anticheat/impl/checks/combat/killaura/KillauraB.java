package cc.funkemunky.anticheat.impl.checks.combat.killaura;

import cc.funkemunky.anticheat.api.checks.CancelType;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.utils.MiscUtils;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import lombok.val;
import org.bukkit.event.Event;

@Packets(packets = {
        Packet.Client.POSITION_LOOK,
        Packet.Client.LOOK,
        Packet.Client.LEGACY_POSITION_LOOK,
        Packet.Client.LEGACY_LOOK})
public class KillauraB extends Check {
    public KillauraB(String name, CancelType cancelType, int maxVL) {
        super(name, cancelType, maxVL);
    }

    private float lastPitchDelta, lastYawDelta;
    private double vl;
    private long lastGCD;

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        if(getData().getLastAttack().hasNotPassed(4)) {
            val to = getData().getMovementProcessor().getTo();
            val from = getData().getMovementProcessor().getFrom();
            val pitchDifference = Math.abs(from.getPitch() - to.getPitch());
            val yawDifference = Math.abs(from.getYaw() - to.getYaw());

            val offset = 16777216L;
            val pitchGCD = MiscUtils.gcd((long) (pitchDifference * offset), (long) (lastPitchDelta * offset));

            if (Math.abs(to.getPitch()) < 85.0f && pitchDifference > 0 && getData().getMovementProcessor().getOptifineTicks() < 6 && pitchGCD < 131072L) {
                if(vl++ > 125) {
                    flag(String.valueOf(pitchGCD / 2000), true, true);
                }
            } else {
                vl -= vl > 0 ? 0.5 : 0;
            }

            debug("VL: " + vl + " PITCH: " + pitchGCD + " OPTIFINE: " + getData().isCinematicMode() + " CYAW: " + getData().getMovementProcessor().getCinematicPitchDelta());

            lastPitchDelta = pitchDifference;
            lastYawDelta = yawDifference;
            lastGCD = pitchGCD;
        }
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
