package cc.funkemunky.anticheat.impl.checks.combat.autoclicker;

import cc.funkemunky.anticheat.api.checks.CancelType;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.utils.MiscUtils;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.anticheat.api.utils.Setting;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import lombok.val;
import org.bukkit.event.Event;

@Packets(packets = {Packet.Client.ARM_ANIMATION})
public class AutoclickerA extends Check {

    @Setting(name = "maxCPS")
    private int maxCPS = 20;

    @Setting(name = "banCPS")
    private int banCPS = 30;

    @Setting(name = "verboseThreshold")
    private int verboseThreshold = 6;

    @Setting(name = "verboseDeduct")
    private double deduct = 0.25;

    private long lastTimeStamp;
    private double vl;

    public AutoclickerA(String name, CancelType cancelType, int maxVL) {
        super(name, cancelType, maxVL);
    }

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        if (MiscUtils.shouldReturnArmAnimation(getData()) || getData().isBreakingBlock() || getData().getLastBlockBreakStop().hasNotPassed(20)) return;

        val elapsed = timeStamp - lastTimeStamp;

        if(elapsed < 2) return;
        val cps = 1000D / elapsed;

        if (cps > maxCPS) {
            if (vl++ > verboseThreshold) {
                flag(cps + ">-" + maxCPS, false, cps > banCPS);
            }
        } else {
            vl -= vl > 0 ? deduct : 0;
        }

        debug("VL: " + vl + " CPS: " + cps);

        lastTimeStamp = timeStamp;
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
