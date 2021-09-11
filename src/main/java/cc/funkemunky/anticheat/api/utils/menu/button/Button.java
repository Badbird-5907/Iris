package cc.funkemunky.anticheat.api.utils.menu.button;

import lombok.Getter;
import lombok.NonNull;
import org.bukkit.inventory.ItemStack;

@Getter
public class Button {

    private boolean moveable;
    private ItemStack stack;
    private ClickAction consumer;

    public Button(boolean moveable, @NonNull ItemStack stack, ClickAction consumer) {
        this.moveable = moveable;
        this.stack = stack;
        this.consumer = consumer;
    }

    public Button(boolean moveable, ItemStack stack) {
        this(moveable, stack, null);
    }
}
