package cc.funkemunky.anticheat.api.utils.menu.button;

import cc.funkemunky.anticheat.api.utils.menu.Menu;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

@FunctionalInterface
public interface ClickAction {

    /**
     * Preforms an operation on the given argument(s)
     *
     * @param player                         The {@link Player} that has acted
     * @param buttonClickTypeInformationPair The {@link InformationPair} that contains the {@link Button} clicked, the {@link Menu}, and the {@link ClickType}
     */
    void accept(Player player, InformationPair buttonClickTypeInformationPair);

    @Getter
    @Setter
    @AllArgsConstructor
    class InformationPair {
        private Button button;
        private ClickType clickType;
        private Menu menu;
    }

}
