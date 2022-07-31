package dk.martinu.opti.ui;

import java.io.File;
import java.util.Objects;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

public class Wizard {

    public final Gui gui;
    protected final JFileChooser fileChooser = new JFileChooser();

    public Wizard(final Gui gui) {
        this.gui = Objects.requireNonNull(gui, "gui is null");
    }

    public File showOpenFileDialog() {
        final FileFilter filter = new ImageFileFilter();
        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.addChoosableFileFilter(filter);
        fileChooser.setFileFilter(filter);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setMultiSelectionEnabled(false);

        final int value = fileChooser.showOpenDialog(gui);
        fileChooser.resetChoosableFileFilters();

        if (value == JFileChooser.APPROVE_OPTION)
            return fileChooser.getSelectedFile();
        else
            return null;
    }

    public static class ImageFileFilter extends FileFilter {

        private final String[] fileTypes;
        private final String description;

        public ImageFileFilter() {
            fileTypes = ImageIO.getReaderFileSuffixes();
            description = "Image Files (" + String.join(", ", fileTypes) + ")";
        }

        @Override
        public boolean accept(final File file) {
            if (file.isDirectory())
                return true;
            else
                for (String type : fileTypes) {
                    final String[] split = file.getName().split("\\.");
                    if (split[split.length - 1].equalsIgnoreCase(type))
                        return true;
                }
            return false;
        }

        @Override
        public String getDescription() {
            return description;
        }
    }
}
