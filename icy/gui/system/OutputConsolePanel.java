/*
 * Copyright 2010, 2011 Institut Pasteur.
 * 
 * This file is part of ICY.
 * 
 * ICY is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * ICY is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with ICY. If not, see <http://www.gnu.org/licenses/>.
 */
package icy.gui.system;

import icy.gui.component.ExternalizablePanel;
import icy.gui.component.button.IcyButton;
import icy.gui.component.button.IcyToggleButton;
import icy.gui.frame.progress.ProgressFrame;
import icy.gui.util.GuiUtil;
import icy.resource.ResourceUtil;
import icy.resource.icon.IcyIcon;
import icy.system.IcyExceptionHandler;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintStream;
import java.util.EventListener;

import javax.swing.Box;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
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

    private static final int MAX_SIZE = 4 * 1024 * 1024; // 4 MB

    final JTextPane textPane;
    final StyledDocument doc;
    final SimpleAttributeSet normalAttributes;
    final SimpleAttributeSet errorAttributes;

    final public IcyButton clearLogButton;
    final public IcyButton copyLogButton;
    final public IcyButton reportLogButton;
    final public IcyToggleButton scrollLockButton;
    final public JPanel bottomPanel;

    public OutputConsolePanel()
    {
        super("Output", "outputConsole");

        textPane = new JTextPane();
        doc = textPane.getStyledDocument();

        errorAttributes = new SimpleAttributeSet();
        normalAttributes = new SimpleAttributeSet();

        StyleConstants.setFontFamily(errorAttributes, "arial");
        StyleConstants.setFontSize(errorAttributes, 11);
        StyleConstants.setForeground(errorAttributes, Color.red);

        StyleConstants.setFontFamily(normalAttributes, "arial");
        StyleConstants.setFontSize(normalAttributes, 11);
        StyleConstants.setForeground(normalAttributes, Color.black);

        clearLogButton = new IcyButton(new IcyIcon(ResourceUtil.ICON_DELETE));
        copyLogButton = new IcyButton(new IcyIcon(ResourceUtil.ICON_DOCCOPY));
        reportLogButton = new IcyButton(new IcyIcon(ResourceUtil.ICON_DOCEXPORT));
        scrollLockButton = new IcyToggleButton(new IcyIcon(ResourceUtil.ICON_LOCK_OPEN));

        // ComponentUtil.setFontSize(textPane, 10);
        textPane.setEditable(false);

        clearLogButton.setFlat(true);
        copyLogButton.setFlat(true);
        reportLogButton.setFlat(true);
        scrollLockButton.setFlat(true);

        clearLogButton.setToolTipText("Clear all");
        copyLogButton.setToolTipText("Copy to clipboard");
        reportLogButton.setToolTipText("Report content to dev team");
        scrollLockButton.setToolTipText("Scroll Lock");

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

                try
                {
                    final StringSelection contents = new StringSelection(doc.getText(0, doc.getLength()));
                    clipboard.setContents(contents, OutputConsolePanel.this);
                }
                catch (BadLocationException e1)
                {
                    // ignore
                }
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
                    IcyExceptionHandler.report(doc.getText(0, doc.getLength()));
                }
                catch (BadLocationException e1)
                {
                    // ignore
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

        bottomPanel = GuiUtil.createPageBoxPanel(
                Box.createVerticalStrut(4),
                GuiUtil.createLineBoxPanel(clearLogButton, Box.createHorizontalStrut(4), copyLogButton,
                        Box.createHorizontalStrut(4), reportLogButton, Box.createHorizontalGlue(),
                        Box.createHorizontalStrut(4), scrollLockButton));

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
    }

    public void addText(String text, boolean isError)
    {
        try
        {
            if (isError)
                doc.insertString(doc.getLength(), text, errorAttributes);
            else
                doc.insertString(doc.getLength(), text, normalAttributes);

            // limit to maximum size
            if (doc.getLength() > MAX_SIZE)
                doc.remove(0, doc.getLength() - MAX_SIZE);

            // scroll lock feature
            if (!scrollLockButton.isSelected())
                textPane.setCaretPosition(doc.getLength());
        }
        catch (Exception e)
        {
            // ignore
        }

        changed(isError);
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
