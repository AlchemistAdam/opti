package dk.martinu.opti.ui;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;

import javax.imageio.ImageIO;
import javax.swing.*;

import dk.martinu.kofi.*;
import dk.martinu.opti.*;
import dk.martinu.opti.engine.Engine;
import dk.martinu.opti.engine.Engines;

import static java.awt.event.KeyEvent.*;
import static javax.swing.JSplitPane.HORIZONTAL_SPLIT;
import static javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER;
import static javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED;
import static javax.swing.SwingUtilities.invokeLater;

public class Gui extends JFrame {

    public static final String AK_OPEN = "dk.martinu.opti.ui.OcrGui.open";
    public static final String AK_EXIT = "dk.martinu.opti.ui.OcrGui.exit";
    public static final String AK_VIEW_IMAGE = "dk.martinu.opti.ui.OcrGui.viewImage";
    public static final String AK_VIEW_SCRIPTS = "dk.martinu.opti.ui.OcrGui.viewScripts";

    public static final String CK_IMAGE_COMPONENT = "dk.martinu.opti.ui.OcrGui.imageComponent";
    public static final String CK_IMAGE_PANE = "dk.martinu.opti.ui.OcrGui.imagePane";
    public static final String CK_IMAGE_SPLIT_PANE = "dk.martinu.opti.ui.OcrGui.imageSplitPane";
    public static final String CK_M_OPEN_RECENT = "dk.martinu.opti.ui.OcrGui.mOpenRecent";
    public static final String CK_M_VIEW = "dk.martinu.opti.ui.OcrGui.mView";

    public static final Path CONFIG_PATH = Paths.get("config.kofi");

    static {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected final Document config;
    protected final HashMap<String, GuiAction> actionMap = new HashMap<>();
    protected final HashMap<String, JComponent> componentMap = new HashMap<>();
    protected final Wizard wizard;
    protected final ArrayDeque<JMenuItem> recentFiles;
    protected int recentFilesCount;

    // must be called on EDT
    public Gui() {
        if (!SwingUtilities.isEventDispatchThread())
            throw new RuntimeException("Opti GUI must be created on the Event Dispatch Thread");

        // get config document
        // TODO remove if-else when library cache is updated
        if (Files.exists(CONFIG_PATH))
            try {
                config = DocumentIO.readFile(CONFIG_PATH);
            }
            catch (Exception e) {
                throw new RuntimeException("could not read Opti config file", e);
            }
        else
            config = new Document();

        // init wizard
        wizard = new Wizard(this);
        if (config.contains("window", "dir")) {
            //noinspection ConstantConditions
            final File dir = new File(config.getString("window", "dir"));
            if (dir.isDirectory())
                wizard.fileChooser.setCurrentDirectory(dir);
        }

        // create recent files variables - initialized from config in createGUI()
        recentFilesCount = config.getInt("window", "recentFilesCount", 6);
        recentFiles = new ArrayDeque<>(recentFilesCount);

        // init ui
        createGUI();
    }

    protected void addRecentFile(final File file) {
        // must be called on EDT
        assert SwingUtilities.isEventDispatchThread();

        // recent files menu
        final JMenu menu = getComponent(CK_M_OPEN_RECENT);

        // move to top if item was already listed
        for (JMenuItem item : recentFiles) {
            if (file.equals(item.getClientProperty("file"))) {
                recentFiles.remove(item);
                recentFiles.addFirst(item);
                menu.remove(item);
                menu.add(item, 0);
                // return here - menu item is already listed
                return;
            }
        }

        // not listed, create new menu item
        final JMenuItem mRecent = new JMenuItem(new GuiAction(file.getName(), event -> openFile(file)));
        mRecent.setToolTipText(file.getAbsolutePath());
        mRecent.putClientProperty("file", file);

        // add item to list
        recentFiles.addFirst(mRecent);
        menu.add(mRecent, 0);
        menu.getAction().setEnabled(true);

        // remove last item if above count
        if (recentFiles.size() > recentFilesCount) {
            final JMenuItem item = recentFiles.removeLast();
            menu.remove(item);
        }
    }

    protected void createGUI() {
        // must be called on EDT
        assert SwingUtilities.isEventDispatchThread();

        /* --- COMPONENTS --- */

        final JPanel contentPane = new JPanel(new BorderLayout(), true);
        final JPanel imagePanel = createImagePanel();
        final JPanel scriptsPanel = createScriptsPanel();



        /* --- ACTIONS --- */

        final GuiAction openAction = new GuiAction("Open", VK_O, event -> {
            final File file = wizard.showOpenFileDialog();
            if (file == null)
                return;

            openFile(file);

            // persist file location
            config.addString("window", "dir", file.getParentFile().getAbsolutePath());
        });
        actionMap.put(AK_OPEN, openAction);

        final GuiAction exitAction = new GuiAction("Exit", VK_E, event ->
                invokeLater(() -> dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING)))
        );
        actionMap.put(AK_EXIT, exitAction);

        final GuiAction viewImageAction = new GuiAction("Image", VK_I, event ->
                invokeLater(() -> {
                    contentPane.remove(scriptsPanel);
                    contentPane.add(imagePanel, BorderLayout.CENTER);
                    contentPane.validate();
                    repaint();
                })
        );
        actionMap.put(AK_VIEW_IMAGE, viewImageAction);

        final GuiAction viewScriptsAction = new GuiAction("Scripts", VK_S, event ->
                invokeLater(() -> {
                    contentPane.remove(imagePanel);
                    contentPane.add(scriptsPanel, BorderLayout.CENTER);
                    contentPane.validate();
                    repaint();
                })
        );
        actionMap.put(AK_VIEW_SCRIPTS, viewScriptsAction);



        /* --- MENUS --- */

        final JMenuBar menuBar = new JMenuBar();
        final JMenu mFile = new JMenu(new GuiAction("File", VK_F));
        final JMenuItem mOpen = new JMenuItem(openAction);
        final JMenu mOpenRecent = new JMenu(new GuiAction("Open Recent", VK_R));
        final JMenuItem mExit = new JMenuItem(exitAction);
        final JMenu mView = new JMenu(new GuiAction("View", VK_V));
        final JRadioButtonMenuItem mImage = new JRadioButtonMenuItem(viewImageAction);
        final JRadioButtonMenuItem mScripts = new JRadioButtonMenuItem(viewScriptsAction);

        mOpenRecent.getAction().setEnabled(false);
        componentMap.put(CK_M_OPEN_RECENT, mOpenRecent);

        mFile.add(mOpen);
        mFile.add(mOpenRecent);
        mFile.addSeparator();
        mFile.add(mExit);

        mImage.setSelected(true);
        mView.add(mImage);
        mView.add(mScripts);
        componentMap.put(CK_M_VIEW, mView);
        {
            final ButtonGroup viewGroup = new ButtonGroup();
            viewGroup.add(mImage);
            viewGroup.add(mScripts);
        }

        menuBar.add(mFile);
        menuBar.add(mView);



        /* --- USER INTERFACE --- */

        contentPane.add(imagePanel, BorderLayout.CENTER);

        setContentPane(contentPane);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setJMenuBar(menuBar);
        setPreferredSize(new Dimension(800, 600));
        setTitle("Opti GUI");
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(final WindowEvent event) {
                // TODO ask user to confirm exit



                /* --- WINDOW CONFIG PROPERTIES --- */

                config.addObject("window", "bounds", KofiObject.reflect(getBounds()));
                {
                    final String state = switch (getExtendedState()) {
                        case Frame.MAXIMIZED_BOTH -> "maxBoth";
                        case Frame.MAXIMIZED_HORIZ -> "maxHorizontal";
                        case Frame.MAXIMIZED_VERT -> "maxVertical";
                        default -> "normal";
                    };
                    config.addString("window", "state", state);
                }

                config.addInt("window", "imageDividerLocation",
                        ((JSplitPane) getComponent(CK_IMAGE_SPLIT_PANE)).getDividerLocation());

                config.addInt("window", "recentFilesCount", recentFilesCount);
                {
                    final String[] files = recentFiles.stream().map(item -> {
                                if (item.getClientProperty("file") instanceof File file)
                                    return file.getAbsolutePath();
                                else
                                    return "";
                            })
                            .dropWhile(String::isEmpty)
                            .toArray(String[]::new);
                    config.addArray("window", "recentFilesMenu", KofiArray.reflect(files));
                }

                try {
                    DocumentIO.writeFile(CONFIG_PATH, config);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }


                // dispose frame
                dispose();
            }
        });

        pack();
        setLocationRelativeTo(null);



        /* --- CONFIG PROPERTIES --- */

        config.acceptObject("window", "bounds", bounds -> {
            try { setBounds(bounds.construct(Rectangle.class)); }
            catch (ReflectiveOperationException e) { e.printStackTrace(); }
        });
        config.acceptString("window", "state", state -> {
            switch (state) {
                case "maxBoth" -> setExtendedState(MAXIMIZED_BOTH);
                case "maxHorizontal" -> setExtendedState(MAXIMIZED_HORIZ);
                case "maxVertical" -> setExtendedState(MAXIMIZED_VERT);
                case "normal" -> setExtendedState(NORMAL);
            }
        });
        config.acceptInt("window", "imageDividerLocation", location -> {
            JSplitPane splitPane = getComponent(CK_IMAGE_SPLIT_PANE);
            splitPane.setDividerLocation(location);
        });
        config.acceptInt("window", "recentFilesCount", count -> this.recentFilesCount = count);
        config.acceptArray("window", "recentFilesMenu", array -> {
            for (String fileName : array.construct(String[].class))
                addRecentFile(new File(fileName));
        });
    }

    protected JPanel createImagePanel() {
        final ImageComponent imageComponent = new ImageComponent();
        final JScrollPane imagePane = new JScrollPane(imageComponent);

        final JTextArea textArea = new JTextArea();
        final JScrollPane textPane = new JScrollPane(textArea, VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_NEVER);

        final JSplitPane splitPane = new JSplitPane(HORIZONTAL_SPLIT, true, imagePane, textPane);
        final JPanel panel = new JPanel(new BorderLayout(), true);

        componentMap.put(CK_IMAGE_COMPONENT, imageComponent);

        componentMap.put(CK_IMAGE_PANE, imagePane);

        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);

        splitPane.setDividerSize(7);
        splitPane.setDividerLocation(400);
        splitPane.addPropertyChangeListener(JSplitPane.LAST_DIVIDER_LOCATION_PROPERTY, event ->
                invokeLater(() -> {
                    textArea.setSize(textPane.getViewport().getViewSize());
                    textArea.validate();
                })
        );
        componentMap.put(CK_IMAGE_SPLIT_PANE, splitPane);

        panel.add(splitPane, BorderLayout.CENTER);

        return panel;
    }

    protected JPanel createScriptsPanel() {
        final JPanel panel = new JPanel();
        return panel;
    }

    protected <T extends JComponent> T getComponent(final String key) {
        //noinspection unchecked
        return (T) componentMap.get(key);
    }

    // TODO image processing should be done asynchronously
    protected void openFile(final File file) {
        // must be called on EDT
        assert SwingUtilities.isEventDispatchThread();

        // attempt to read file
        final BufferedImage source;
        try {
            source = ImageIO.read(Objects.requireNonNull(file, "file is null"));
        }
        catch (IOException e) {
            e.printStackTrace();

            // remove menu item if file does not exist
            if (!file.exists())
                removeRecentFile(file);

            // TODO notify user

            return;
        }

        // display source image
        final ImageComponent imageComponent = getComponent(CK_IMAGE_COMPONENT);
        imageComponent.setSource(source);

        // revalidate image pane to update scroll bar models
        getComponent(CK_IMAGE_PANE).revalidate();

        // create contrast
        final BufferedImage contrast = ContrastImageFactory.getContrast(source);

        // get engine for source image
        final Engine engine = Engines.getEngine(contrast);
        if (engine != null) {
            // read text
            final Text text = engine.getText(source);

            // TODO set text in textArea of imagePanel
        }
//        else
//            return; // TODO notify user

        // add file to recent files menu
        addRecentFile(file);
    }

    protected void removeRecentFile(final File file) {
        // must be called on EDT
        assert SwingUtilities.isEventDispatchThread();

        // remove menu item from list if found
        for (JMenuItem item : recentFiles)
            if (file.equals(item.getClientProperty("file"))) {
                recentFiles.remove(item);
                final JMenu menu = getComponent(CK_M_OPEN_RECENT);
                menu.remove(item);
                return;
            }
    }
}
