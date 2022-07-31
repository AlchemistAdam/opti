package dk.martinu.opti.ui;

import java.awt.event.ActionEvent;
import java.util.Objects;
import java.util.function.Consumer;

import javax.swing.*;

public class GuiAction extends AbstractAction {

    public final Consumer<ActionEvent> action;

    public GuiAction(final String name, final Consumer<ActionEvent> action) {
        super(name);
        this.action = Objects.requireNonNull(action, "action is null");
    }

    public GuiAction(final String name, final int mnemonic) {
        super(name);
        this.action = null;
        putValue(Action.MNEMONIC_KEY, mnemonic);
    }

    public GuiAction(final String name, final int mnemonic, final Consumer<ActionEvent> action) {
        super(name);
        this.action = Objects.requireNonNull(action, "action is null");
        putValue(Action.MNEMONIC_KEY, mnemonic);
    }

    public GuiAction(final String name, final int mnemonic, final KeyStroke accelerator,
            final Consumer<ActionEvent> action) {
        super(name);
        this.action = Objects.requireNonNull(action, "action is null");
        putValue(Action.ACCELERATOR_KEY, Objects.requireNonNull(accelerator, "accelerator is null"));
        putValue(Action.MNEMONIC_KEY, mnemonic);
    }

    @Override
    public void actionPerformed(final ActionEvent event) {
        if (action != null)
            action.accept(event);
    }
}
