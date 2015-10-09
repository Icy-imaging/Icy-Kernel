/*
 * Copyright 2010-2015 Institut Pasteur.
 * 
 * This file is part of Icy.
 * 
 * Icy is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Icy is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Icy. If not, see <http://www.gnu.org/licenses/>.
 */
package icy.script;

import icy.gui.frame.IcyFrame;
import icy.gui.util.WindowPositionSaver;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;

/**
 * @author fab
 */
public class ScriptEditor extends IcyFrame implements ActionListener
{

    private JTextArea codeArea = new JTextArea("print('Hello, world!')", 15, 20);
    private JTextArea messageArea = new JTextArea("message text", 5, 20);
    private JPanel mainPanel = new JPanel();
    JPanel codePanel = new JPanel();
    JPanel codeActionPanel = new JPanel();
    JPanel messagePanel = new JPanel();
    JPanel messageActionPanel = new JPanel();

    JButton runButton = new JButton("Run");

    JMenuItem loadFileMenuItem = new JMenuItem("Load...");
    JMenuItem saveFileMenuItem = new JMenuItem("Save...");

    // we need to keep reference on it as the object only use weak reference
    final WindowPositionSaver positionSaver;

    public ScriptEditor()
    {
        super("Script Editor", true, true, true, true);

        positionSaver = new WindowPositionSaver(this, "frame/scriptEditor", new Point(300, 300),
                new Dimension(400, 300));

        JMenuBar menuBar = new JMenuBar();
        this.setJMenuBar(menuBar);
        JMenu fileMenu = new JMenu("File");
        fileMenu.add(loadFileMenuItem);
        fileMenu.add(saveFileMenuItem);
        loadFileMenuItem.addActionListener(this);
        saveFileMenuItem.addActionListener(this);
        menuBar.add(fileMenu);

        this.setVisible(true);

        codePanel.setLayout(new BoxLayout(codePanel, BoxLayout.PAGE_AXIS));
        codeActionPanel.add(runButton);
        runButton.addActionListener(this);
        codeActionPanel.add(new JButton("Compile"));

        codePanel.add(codeArea);
        codePanel.add(codeActionPanel);
        codeArea.setLineWrap(true);

        messagePanel.setLayout(new BoxLayout(messagePanel, BoxLayout.PAGE_AXIS));
        messageActionPanel.add(new JButton("Clear"));

        messagePanel.add(messageArea);
        messagePanel.add(messageActionPanel);

        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));

        mainPanel.add(codePanel);
        mainPanel.add(messagePanel);

        codeArea.setVisible(true);
        codeArea.setTabSize(2);
        messageArea.setLineWrap(true);

        this.getContentPane().setLayout(new FlowLayout());

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, codePanel, messagePanel);

        this.getContentPane().setLayout(new GridLayout(1, 1));
        this.getContentPane().add(splitPane);

        this.pack();

        addToDesktopPane();

    }

    public void actionPerformed(ActionEvent e)
    {

        if (e.getSource() == runButton)
        {
            messageArea.setText("");

            ScriptEngineManager mgr = new ScriptEngineManager();
            ScriptEngine jsEngine = mgr.getEngineByName("JavaScript");
            if (jsEngine == null)
            {
                System.err.println("Unable to use ScriptEngine with JavaScript.");
                return;
            }
            try
            {
                jsEngine.eval(codeArea.getText());
            }
            catch (ScriptException ex)
            {
                messageArea.append("Line : " + ex.getLineNumber() + "\n");
                messageArea.append(ex.getMessage());
            }
        }

        if (e.getSource() == loadFileMenuItem)
        {

        }

        if (e.getSource() == saveFileMenuItem)
        {

        }

    }

}
