
/**
 * PlugEdit: a level editor for Plughole.
 * <br>Copyright 2010 Ian Cameron Smith
 *
 * <p>This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2
 * as published by the Free Software Foundation (see COPYING).
 * 
 * <p>This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */


package org.hermit.plughole.editor;


import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SpinnerNumberModel;
import javax.swing.WindowConstants;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.table.TableColumn;


/**
 * Main aplication frame.
 */
public class PlugEdit
    extends JFrame
    implements WindowListener
{

    // ******************************************************************** //
    // Constructors.
    // ******************************************************************** //

    /**
     * Initialize the application window.
     */
    public PlugEdit() {
        // Create all of our GUI actions and dialogs.
        makeActions();
        makeDialogs();

        // Add the menus.
        setJMenuBar(makeMenuBar());

        // Add the toolbar.
        Container pane = getContentPane();
        pane.add(makeToolBar(), BorderLayout.NORTH);

        // Add the main GUI.
        pane.add(makeMainPanel(), BorderLayout.CENTER);

        // Handle window events on this frame.
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(this);

        pack();
        setTitle("Wordy");
        setVisible(true);

        // Make the dictionary.
        try {
//            dictionary = new Dictionary(new File(DEF_DICT));
        }
        catch (java.lang.Exception e) {
            reportException(e);
        }
    }


    // ******************************************************************** //
    // GUI Construction.
    // ******************************************************************** //

    /**
     * Make the GUI dialog boxes.
     */
    private void makeDialogs() {
        fileChooser = new JFileChooser(System.getProperty("user.dir"));
    }


    /**
     * Make the GUI actions.
     */
    private void makeActions() {
        fileNew = new AbstractAction("Open...") {
            public void actionPerformed(ActionEvent e) { fileNew(); }
            private static final long serialVersionUID = 1L;
        };
        fileNew.putValue(Action.SMALL_ICON,
                         new ImageIcon(ICONDIR + "actions/filenew.png"));
        fileNew.putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_N));
        fileNew.putValue(Action.SHORT_DESCRIPTION, "Create a new level");

        fileOpen = new AbstractAction("Open...") {
            public void actionPerformed(ActionEvent e) { fileOpen(); }
            private static final long serialVersionUID = 1L;
        };
        fileOpen.putValue(Action.SMALL_ICON,
                          new ImageIcon(ICONDIR + "actions/fileopen.png"));
        fileOpen.putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_O));
        fileOpen.putValue(Action.SHORT_DESCRIPTION, "Load a level from a file");

        fileQuit = new AbstractAction("Quit") {
            public void actionPerformed(ActionEvent e) { fileQuit(); }
            private static final long serialVersionUID = 1L;
        };
        fileQuit.putValue(Action.SMALL_ICON,
                          new ImageIcon(ICONDIR + "actions/fileclose.png"));
        fileQuit.putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_Q));
        fileQuit.putValue(Action.SHORT_DESCRIPTION, "Quit this application");
    }


    /**
     * Make the menu bar.
     *
     * @return            A new menu bar.
     */
    private JMenuBar makeMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu menu;

        // Build the "File" menu.
        menu = new JMenu("File");
        menu.setMnemonic(KeyEvent.VK_F);
        menuBar.add(menu);

        menu.add(new JMenuItem(fileNew));
        menu.add(new JMenuItem(fileOpen));
        menu.add(new JMenuItem(fileQuit));

        // Build the "Level" menu.
        menu = new JMenu("Level");
        menu.setMnemonic(KeyEvent.VK_L);
        menuBar.add(menu);

        return menuBar;
    }


    /**
     * Make the tool bar.
     *
     * @return            A new tool bar.
     */
    private JToolBar makeToolBar() {
        JToolBar toolBar = new JToolBar();

        toolBar.add(fileNew);
        toolBar.add(fileOpen);

        toolBar.addSeparator();

        return toolBar;
    }


    /**
     * Make the main GUI panel.
     *
     * @return            A new Panel.
     */
    private JComponent makeMainPanel() {
        // Make the panel with a grid layout.  Set up the column weights.
        GridPanel panel = new GridPanel();
        panel.colWeights(new int[] { 0, 1 });

        panel.newRow();
        panel.rowWeight(1);
        panel.add(makeControls());
        panel.add(new EditPanel(this), 1, true, true);

        panel.newRow();
        panel.rowWeight(0);
        panel.add(makeStatusBar(), 2, true, false);

        return panel;
    }


    /**
     * Make a panel containing the main view controls.
     *
     *    [ letter field ]
     *    [] Use all letters
     *    [] contain [ contain text ]
     *    {5} words max
     *    {2} min length
     *
     * @return            A new Panel.
     */
    private JComponent makeControls() {
        // Make the panel with a grid layout.  Set up the column weights.
        GridPanel panel = new GridPanel();
        panel.colWeights(new int[] { 0, 1, 1 });
        Border opad = BorderFactory.createEmptyBorder(3, 3, 3, 3);
        Border ipad = BorderFactory.createEmptyBorder(4, 4, 4, 4);
        Border etch = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
        Border out = BorderFactory.createCompoundBorder(opad, etch);
        panel.setBorder(BorderFactory.createCompoundBorder(out, ipad));

        panel.widthMult(2);

        // If we're not in julia mode, make the function choice and label.
        // (In julia mode, the function is driven from the map fractal
        // window.)  Note that calling addItem fires an ActionEvent, so
        // add the listener after adding the items.
        panel.newRow();
        panel.add(lettersField = new JTextField("", 14), 2, true, false);

        panel.newRow();
        panel.add(allCheckbox = new JCheckBox("Use all letters"));
        allCheckbox.setSelected(true);

        panel.newRow();
        panel.add(apostCheckbox = new JCheckBox("Allow apsotrophes"));

        panel.newRow();
        panel.add(containCheckbox = new JCheckBox("Contains:"));
        panel.add(containField = new JTextField("", 10), true);

        panel.newRow();
        panel.add(patternCheckbox = new JCheckBox("Pattern:"));
        panel.add(patternField = new JTextField("", 10), true);

        panel.newRow("Max words:");
        maxWordsSpin = new JSpinner(new SpinnerNumberModel(2, 1, 255, 1));
        panel.add(maxWordsSpin);

        return panel;
    }


    /**
     * Make a panel to display the results.
     *
     * @return            A new Panel.
     */
    private JComponent makeResultsPanel() {
        resultsTable = new JTable(1, 2);
        TableColumn col = resultsTable.getColumn("A");
        col.setPreferredWidth(100);
        col = resultsTable.getColumn("B");
        col.setPreferredWidth(144);

        JScrollPane scrollpane = new JScrollPane(resultsTable);
        scrollpane.setPreferredSize(new Dimension(262, 100));
        Border opad = BorderFactory.createEmptyBorder(3, 3, 3, 3);
        Border etch = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
        scrollpane.setBorder(BorderFactory.createCompoundBorder(opad, etch));

        // resultsTable.setReadOnly(true);
        return scrollpane;
    }


    /**
     * Make a panel containing the status bar.
     *
     * @return            A new Panel.
     */
    private JComponent makeStatusBar() {
        // Make the panel with a grid layout.  Leave the default even
        // column weighting.
        GridPanel panel = new GridPanel();
        Border opad = BorderFactory.createEmptyBorder(2, 2, 2, 2);
        Border ipad = BorderFactory.createEmptyBorder(2, 2, 2, 2);
        Border bevel = BorderFactory.createBevelBorder(BevelBorder.LOWERED);
        Border out = BorderFactory.createCompoundBorder(opad, bevel);
        panel.setBorder(BorderFactory.createCompoundBorder(out, ipad));

        // Add the status bar.
        panel.add(statusLabel = new JLabel(" "), 4, true, false);

        return panel;
    }


    // ******************************************************************** //
    // Window Event Handlers.
    // ******************************************************************** //

    /**
     * Event handler: the user has asked to close the window.
     *
     * @param evt     The event descriptor.
     */
    public void windowClosing(WindowEvent evt) {
        fileQuit();
    }


    public void windowOpened(WindowEvent e) { }
    public void windowClosed(WindowEvent e) { }
    public void windowIconified(WindowEvent e) { }
    public void windowDeiconified(WindowEvent e) { }
    public void windowActivated(WindowEvent e) { }
    public void windowDeactivated(WindowEvent e) { }


    // ******************************************************************** //
    // File Actions.
    // ******************************************************************** //

    /**
     * Open a file and load a fractal view specification from it.
     */
    private void fileNew() {
    }


    /**
     * Open a file and load a fractal view specification from it.
     */
    private void fileOpen() {
        try {
            int returnVal = fileChooser.showOpenDialog(this);
            /*      if (returnVal == JFileChooser.APPROVE_OPTION)
    loadFile(fileChooser.getSelectedFile());*/
        }
        catch (java.lang.Exception e) {
            reportException(e);
        }
    }


    /**
     * Quit.
     */
    private void fileQuit() {
        try {
            int ret = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to quit?",
                    "Quit?",
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.WARNING_MESSAGE);
            if (ret == JOptionPane.YES_OPTION)
                System.exit(0);
        }
        catch (java.lang.Exception e) {
            reportException(e);
        }
    }


    // ******************************************************************** //
    // Status Display.
    // ******************************************************************** //

    /**
     * Display the given message in the status bar.
     * The message will stay there until explicitly changed
     * or cleared.
     * 
     * @param text        The text to display.
     */
    private void showStatus(String text) {
        statusLabel.setText(text);
    }


    // ******************************************************************** //
    // Utility Functions.
    // ******************************************************************** //

    /**
     * Report the given exception in a dialog box.
     *
     * @param e       Exception to report.
     */
    private void reportException(Exception e) {
        String message;
        String backtrace = "";
        int type;

        if (e instanceof UserException) {
            type = JOptionPane.WARNING_MESSAGE;
            message = "";
        } else {
            type = JOptionPane.ERROR_MESSAGE;
            if (e instanceof AppException)
                message = "Application Error: ";
            else
                message = "Unexpected Exception: ";

            // Get the backtrace.  Exception can only print it, so print it to
            // a string.
            java.io.CharArrayWriter charWriter = new java.io.CharArrayWriter();
            java.io.PrintWriter printWriter = new java.io.PrintWriter(charWriter);
            e.printStackTrace(printWriter);
            printWriter.close();
            charWriter.close();

            // Trim the backtrace to at most 16 lines.
            backtrace = charWriter.toString();
            int index = 0;
            for (int i = 0; i < 16; ++i) {
                index = backtrace.indexOf('\n', index);
                if (index < 0)
                    break;
                ++index;
            }
            if (index > 0 && index + 1 < backtrace.length())
                backtrace = backtrace.substring(0, index) + ". . . (truncated)\n";
        }

        // Display a message for the error.
        JOptionPane.showMessageDialog(this,
                message + e.getMessage() +
                "\n\n" + backtrace,
                "FracTest Error",
                type);
    }


    // ******************************************************************** //
    // Main.
    // ******************************************************************** //

    /**
     * Main function: pop up an application frame.
     *
     * There is no command-line processing with this class.  If you want
     * it, use ClFracTestApp instead.
     *
     * @param args        Command-line arguments.
     */
    public static void main(String[] args) {
        PlugEdit win = new PlugEdit();
    }


    // ******************************************************************** //
    // Private Constants.
    // ******************************************************************** //

    /**
     * UUID.
     */
    private static final long serialVersionUID = 6885015266747286657L;

    /**
     * Directory where we get icons from.
     */
    private static final String ICONDIR =
        "/opt/kde3/share/icons/crystalsvg/16x16/";

    /**
     * Default dictionary; null to use an's built-in default.
     */
    private static final String DEF_DICT =
        "/usr/share/dict/british-80.scowl6";


    // ******************************************************************** //
    // Member Data.
    // ******************************************************************** //

    /*
     * GUI widgets.
     */
    private JTextField lettersField = null;
    private JCheckBox allCheckbox = null;
    private JCheckBox apostCheckbox = null;
    private JCheckBox containCheckbox = null;
    private JTextField containField = null;
    private JCheckBox patternCheckbox = null;
    private JTextField patternField = null;
    private JSpinner maxWordsSpin = null;
    private JLabel statusLabel = null;
    private JTable resultsTable = null;

    /*
     * Actions.
     */
    private Action fileNew = null;
    private Action fileOpen = null;
    private Action fileQuit = null;

    /*
     * Dialogs.
     */
    private JFileChooser fileChooser = null;

}

