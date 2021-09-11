package cc.funkemunky.anticheat.api.utils.menu.type.impl;


import cc.funkemunky.anticheat.api.utils.menu.Menu;
import cc.funkemunky.anticheat.api.utils.menu.button.Button;
import cc.funkemunky.anticheat.api.utils.menu.type.BukkitInventoryHolder;
import cc.funkemunky.anticheat.api.utils.menu.util.ArrayIterator;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Iterator;
import java.util.Optional;
import java.util.stream.IntStream;

public class ChestMenu implements Menu {

    @Getter
    private final String title;
    private final MenuDimension dimension;
    @Setter
    private Menu parent;
    private BukkitInventoryHolder holder;
    private Button[] contents;
    private CloseHandler closeHandler;

    public ChestMenu(@NonNull String title, int size) {
        this.title = title.length() > 32 ? title.substring(0, 32) : title;
        if (size <= 0 || size > 6) {
            throw new IndexOutOfBoundsException("A menu can only have between 1 & 6 for a size (rows)");
        }
        this.dimension = new MenuDimension(size, 9);
        this.contents = new Button[size * 9];
    }

    @Override
    public MenuDimension getMenuDimension() {
        return dimension;
    }

    @Override
    public void addItem(Button button) {
        setItem(getFirstEmptySlot(), button);
    }

    @Override
    public void setItem(int index, Button button) {
        checkBounds(index);
        contents[index] = button;
    }

    @Override
    public void fill(Button button) {
        fillRange(0, dimension.getSize(), button);
    }

    @Override
    public Button[] getContents() {
        return contents;
    }

    @Override
    public void setContents(Button[] contents) {
        this.contents = contents;
    }

    @Override
    public void fillRange(int startingIndex, int endingIndex, Button button) {
        IntStream.range(startingIndex, endingIndex).forEach(i -> setItem(i, button));
    }

    @Override
    public int getFirstEmptySlot() {
        for (int i = 0; i < contents.length; i++) {
            Button button = contents[i];
            if (button == null) {
                return i;
            }
        }
        return -1; // Will throw when #checkBounds is called.
    }

    @Override
    public void checkBounds(int index) throws IndexOutOfBoundsException {
        if (index < 0 || index > (dimension.getSize())) {
            throw new IndexOutOfBoundsException(String.format("setItem(); %s is out of bounds!", index));
        }
    }

    @Override
    public Optional<Button> getButtonByIndex(int index) {
        return Optional.ofNullable(contents[index]);
    }

    @Override
    public void buildInventory(boolean initial) {
        if (initial) {
            this.holder = new BukkitInventoryHolder(this);
            holder.setInventory(Bukkit.createInventory(holder, dimension.getSize(), title));
        }
        for (int i = 0; i < contents.length; i++) {
            Button button = contents[i];
            if (button != null) {
                holder.getInventory().setItem(i, button.getStack());
            }
        }
    }

    @Override
    public void showMenu(Player player) {
        if (holder == null) {
            buildInventory(true);
        } else {
            buildInventory(false);
        }
        player.openInventory(holder.getInventory());
    }

    @Override
    public void close(Player player) {
        player.closeInventory();
        handleClose(player);
    }

    @Override
    public void setCloseHandler(CloseHandler handler) {
        this.closeHandler = handler;
    }

    @Override
    public void handleClose(Player player) {
        if (closeHandler != null) {
            closeHandler.accept(player, this);
        }
    }

    @Override
    public Optional<Menu> getParent() {
        return Optional.ofNullable(parent);
    }

    @Override
    public Iterator<Button> iterator() {
        return new ArrayIterator<>(contents);
    }
}