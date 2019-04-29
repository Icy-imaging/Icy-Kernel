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
package icy.gui.system;

import icy.file.FileUtil;
import icy.gui.component.ExternalizablePanel;
import icy.gui.component.button.IcyButton;
import icy.gui.component.button.IcyToggleButton;
import icy.gui.frame.progress.ProgressFrame;
import icy.gui.util.GuiUtil;
import icy.preferences.GeneralPreferences;
import icy.resource.ResourceUtil;
import icy.resource.icon.IcyIcon;
import icy.system.IcyExceptionHandler;
import icy.util.EventUtil;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Writer;
import java.util.EventListener;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

/**
 * @author Stephane
 */
public class OutputConsolePanel extends ExternalizablePanel implements ClipboardOwner
{
    public static interface OutputConsoleChangeListener extends EventListener
    {
        public void outputConsoleChanged(OutputConsolePanel source, boolean isError);
    }

    private class WindowsOutPrintStream extends PrintStream
    {
        boolean isStdErr;

        public WindowsOutPrintStream(PrintStream out, boolean isStdErr)
        {
            super(out);

            this.isStdErr = isStdErr;
        }

        @Override
        public void write(byte[] buf, int off, int len)
        {
            try
            {
                super.write(buf, off, len);

                final String text = new String(buf, off, len);
                addText(text, isStdErr);
                // want file log as well ?
                if (fileLogButton.isSelected() && (logWriter != null))
                {
                    // write and save to file immediately
                    logWriter.write(text);
                    logWriter.flush();
                }
            }
            catch (Throwable t)
            {
                addText(t.getMessage(), isStdErr);
            }
        }
    }

    /**
     * 
     */
    private static final long serialVersionUID = 7142067146669860938L;

    final protected JTextPane textPane;
    final protected StyledDocument doc;
    final SimpleAttributeSet normalAttributes;
    final SimpleAttributeSet errorAttributes;

    final protected JSpinner logMaxLineField;
    final protected JTextField logMaxLineTextField;
    final public IcyButton clearLogButton;
    final public IcyButton copyLogButton;
    final public IcyButton reportLogButton;
    final public IcyToggleButton scrollLockButton;
    final public IcyToggleButton fileLogButton;
    final public JPanel bottomPanel;

    int nbUpdate;
    Writer logWriter;

    public OutputConsolePanel()
    {
        super("Output", "outputConsole");

        textPane = new JTextPane();
        doc = textPane.getStyledDocument();
        nbUpdate = 0;

        errorAttributes = new SimpleAttributeSet();
        normalAttributes = new SimpleAttributeSet();

        StyleConstants.setFontFamily(errorAttributes, "arial");
        StyleConstants.setFontSize(errorAttributes, 11);
        StyleConstants.setForeground(errorAttributes, Color.red);

        StyleConstants.setFontFamily(normalAttributes, "arial");
        StyleConstants.setFontSize(normalAttributes, 11);
        StyleConstants.setForeground(normalAttributes, Color.black);

        logMaxLineField = new JSpinner(
                new SpinnerNumberModel(GeneralPreferences.getOutputLogSize(), 100, 1000000, 100));
        logMaxLineTextField = ((JSpinner.DefaultEditor) logMaxLineField.getEditor()).getTextField();
        clearLogButton = new IcyButton(new IcyIcon(ResourceUtil.ICON_DELETE));
        copyLogButton = new IcyButton(new IcyIcon(ResourceUtil.ICON_DOC_COPY));
        reportLogButton = new IcyButton(new IcyIcon(ResourceUtil.ICON_DOC_EXPORT));
        scrollLockButton = new IcyToggleButton(new IcyIcon(ResourceUtil.ICON_LOCK_OPEN));
        fileLogButton = new IcyToggleButton(new IcyIcon(ResourceUtil.ICON_SAVE));
        fileLogButton.setSelected(GeneralPreferences.getOutputLogToFile());

        // ComponentUtil.setFontSize(textPane, 10);
        textPane.setEditable(false);

        clearLogButton.setFlat(true);
        copyLogButton.setFlat(true);
        reportLogButton.setFlat(true);
        scrollLockButton.setFlat(true);
        fileLogButton.setFlat(true);

        logMaxLineField.setPreferredSize(new Dimension(80, 24));
        // no focusable
        logMaxLineField.setFocusable(false);
        logMaxLineTextField.setFocusable(false);
        logMaxLineTextField.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                // get focus on double click to enable manual edition
                if (EventUtil.isLeftMouseButton(e))
                {
                    logMaxLineField.setFocusable(true);
                    logMaxLineTextField.setFocusable(true);
                    logMaxLineTextField.requestFocus();
                }
            }
        });
        logMaxLineTextField.addKeyListener(new KeyAdapter()
        {
            @Override
            public void keyPressed(KeyEvent e)
            {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
                {
                    // cancel manual edition ? --> remove focus
                    if (logMaxLineTextField.isFocusable())
                    {
                        logMaxLineTextField.setFocusable(false);
                        logMaxLineField.setFocusable(false);
                    }
                }
            }
        });
        logMaxLineField.addChangeListener(new ChangeListener()
        {
            @Override
            public void stateChanged(ChangeEvent e)
            {
                GeneralPreferences.setOutputLogSize(getLogMaxLine());

                // manual edition ? --> remove focus
                if (logMaxLineTextField.isFocusable())
                {
                    logMaxLineTextField.setFocusable(false);
                    logMaxLineField.setFocusable(false);
                }

                try
                {
                    limitLog();
                }
                catch (Exception ex)
                {
                    // ignore

                }
            }
        });
        logMaxLineField.setToolTipText("Double-click to edit the maximum number of line (max = 1000000)");
        clearLogButton.setToolTipText("Clear all");
        copyLogButton.setToolTipText("Copy to clipboard");
        reportLogButton.setToolTipText("Report content to dev team");
        scrollLockButton.setToolTipText("Scroll Lock");
        fileLogButton.setToolTipText("Enable/Disable log file saving (log.txt)");

        clearLogButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                textPane.setText("");
            }
        });
        copyLogButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                final Clipboard clipboard = getToolkit().getSystemClipboard();

                clipboard.setContents(new StringSelection(getText()), OutputConsolePanel.this);
            }
        });
        reportLogButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                final ProgressFrame progressFrame = new ProgressFrame("Sending report...");

                try
                {
                    // send report
                    IcyExceptionHandler.report(getText());
                }
                finally
                {
                    progressFrame.close();
                }
            }
        });
        scrollLockButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                if (scrollLockButton.isSelected())
                    scrollLockButton.setIconImage(ResourceUtil.ICON_LOCK_CLOSE);
                else
                    scrollLockButton.setIconImage(ResourceUtil.ICON_LOCK_OPEN);
            }
        });
        fileLogButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                GeneralPreferences.setOutputLogFile(fileLogButton.isSelected());
            }
        });

        bottomPanel = GuiUtil.createPageBoxPanel(Box.createVerticalStrut(4),
                GuiUtil.createLineBoxPanel(clearLogButton, Box.createHorizontalStrut(4), copyLogButton,
                        Box.createHorizontalStrut(4), reportLogButton, Box.createHorizontalGlue(),
                        Box.createHorizontalStrut(4), new JLabel("Limit"), Box.createHorizontalStrut(4),
                        logMaxLineField, Box.createHorizontalStrut(4), scrollLockButton, Box.createHorizontalStrut(4),
                        fileLogButton));

        final JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(textPane, BorderLayout.CENTER);

        final JScrollPane scrollPane = new JScrollPane(panel);

        setLayout(new BorderLayout());

        add(scrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        validate();

        // redirect standard output
        System.setOut(new WindowsOutPrintStream(System.out, false));
        System.setErr(new WindowsOutPrintStream(System.err, true));

        try
        {
            // define log file writer (always clear log.txt file if present)
            logWriter = new FileWriter(FileUtil.getApplicationDirectory() + "/icy.log", false);
        }
        catch (IOException e1)
        {
            logWriter = null;
        }
    }

    public void addText(String text, boolean isError)
    {
        try
        {
            nbUpdate++;

            // insert text
            synchronized (doc)
            {
                if (isError)
                    doc.insertString(doc.getLength(), text, errorAttributes);
                else
                    doc.insertString(doc.getLength(), text, normalAttributes);

                // do clean sometime..
                if ((nbUpdate & 0x7F) == 0)
                    limitLog();

                // scroll lock feature
                if (!scrollLockButton.isSelected())
                    textPane.setCaretPosition(doc.getLength());
            }
        }
        catch (Exception e)
        {
            // ignore
        }

        changed(isError);
    }

    /**
     * Get console content.
     */
    public String getText()
    {
        try
        {
            synchronized (doc)
            {
                return doc.getText(0, doc.getLength());
            }
        }
        catch (BadLocationException e)
        {
            return "";
        }
    }

    /**
     * Apply maximum line limitation to the log output
     * 
     * @throws BadLocationException
     */
    public void limitLog() throws BadLocationException
    {
        final Element root = doc.getDefaultRootElement();
        final int numLine = root.getElementCount();
        final int logMaxLine = getLogMaxLine();

        // limit to maximum wanted lines
        if (numLine > logMaxLine)
        {
            final Element line = root.getElement(numLine - (logMaxLine + 1));
            // remove "old" lines
            doc.remove(0, line.getEndOffset());
        }
    }

    /**
     * Returns maximum log line number
     */
    public int getLogMaxLine()
    {
        return ((Integer) logMaxLineField.getValue()).intValue();
    }

    /**
     * Sets maximum log line number
     */
    public void setLogMaxLine(int value)
    {
        logMaxLineField.setValue(Integer.valueOf(value));
    }

    private void changed(boolean isError)
    {
        fireChangedEvent(isError);
    }

    public void fireChangedEvent(boolean isError)
    {
        for (OutputConsoleChangeListener listener : listenerList.getListeners(OutputConsoleChangeListener.class))
            listener.outputConsoleChanged(this, isError);
    }

    public void addOutputConsoleChangeListener(OutputConsoleChangeListener listener)
    {
        listenerList.add(OutputConsoleChangeListener.class, listener);
    }

    public void removeOutputConsoleChangeListener(OutputConsoleChangeListener listener)
    {
        listenerList.remove(OutputConsoleChangeListener.class, listener);
    }

    @Override
    public void lostOwnership(Clipboard clipboard, Transferable contents)
    {
        // ignore
    }
}
