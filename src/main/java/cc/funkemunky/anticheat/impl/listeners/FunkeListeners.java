package cc.funkemunky.anticheat.impl.listeners;

import cc.funkemunky.anticheat.Iris;
import cc.funkemunky.anticheat.api.data.PlayerData;
import cc.funkemunky.anticheat.api.event.TickEvent;
import cc.funkemunky.api.Atlas;
import cc.funkemunky.api.events.AtlasListener;
import cc.funkemunky.api.events.Listen;
import cc.funkemunky.api.utils.Init;

@Init
public class FunkeListeners implements AtlasListener {

    @Listen
    public void onTickEvent(TickEvent event) {
        Atlas.getInstance().executeTask(() -> Iris.getInstance().getDataManager().getDataObjects().keySet().forEach(key -> {
            PlayerData data = Iris.getInstance().getDataManager().getDataObjects().get(key);

            data.getActionProcessor().update(data);
        }));
    }
}
