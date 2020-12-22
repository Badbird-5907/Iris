package cc.funkemunky.anticheat.impl.checks.movement;

import cc.funkemunky.anticheat.api.checks.CancelType;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.utils.BukkitEvents;
import cc.funkemunky.anticheat.api.utils.Setting;
import cc.funkemunky.api.utils.Color;
import cc.funkemunky.api.utils.PlayerUtils;
import lombok.val;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffectType;

@BukkitEvents(events = {PlayerMoveEvent.class})
public class FastLadder extends Check {
    public FastLadder(String name, CancelType cancelType, int maxVL) {
        super(name, cancelType, maxVL);
    }

    @Setting(name = "threshold.verboseMaxSpeed")
    private float verboseMaxSpeed = 0.118f;

    @Setting(name = "threshold.maxSpeed")
    private float maxSpeed = 0.45f;

    private int vl;

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        return;
    }

    @Override
    public void onBukkitEvent(Event event) {
        PlayerMoveEvent e = (PlayerMoveEvent) event;
        val deltaY = (float) (e.getTo().getY() - e.getFrom().getY());

        if(getData().isGeneralCancel()) return;

        if(getData().getMovementProcessor().isOnClimbable()) {
            if(deltaY > verboseMaxSpeed) {
                if(vl++ > 7) {
                    flag(deltaY + ">-" + verboseMaxSpeed, true, true);
                }
            } else {
                vl-= vl > 0 ? 1 : 0;
            }

            val maxThreshold = maxSpeed + PlayerUtils.getPotionEffectLevel(e.getPlayer(), PotionEffectType.JUMP) * 0.1;

            if(deltaY > maxThreshold) {
                flag(deltaY  + ">-" + maxThreshold, true, true);
            }

            debug(vl + ": " + deltaY);
        }
        debug((getData().getMovementProcessor().isOnClimbable() ? Color.Green : Color.Red) + getData().getMovementProcessor().isOnClimbable());
    }
}
