package dk.martinu.opti;

import javax.swing.SwingUtilities;

import dk.martinu.opti.ui.Gui;

public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            final Gui gui = new Gui();
            gui.setVisible(true);
        });
    }
}
