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

import icy.gui.component.ComponentUtil;
import icy.gui.component.ExternalizablePanel;
import icy.gui.util.GuiUtil;
import icy.plugin.abstract_.Plugin;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintStream;
import java.util.EventListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

/**
 * @author Stephane
 */
public class OutputConsolePanel extends ExternalizablePanel implements ClipboardOwner
{
    public interface OutputConsoleChangeListener extends EventListener
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
            super.write(buf, off, len);

            if (buf == null)
            {
                throw new NullPointerException();
            }
            else if ((off < 0) || (off > buf.length) || (len < 0) || ((off + len) > buf.length) || ((off + len) < 0))
            {
                throw new IndexOutOfBoundsException();
            }
            else if (len == 0)
            {
                return;
            }

            final String text = new String(buf, off, len);
            addText(text, isStdErr);
        }
    }

    /**
     * 
     */
    private static final long serialVersionUID = 7142067146669860938L;

    final JTextPane textPane;
    final Color errorColor;
    final Color outColor;
    final Font font;

    public OutputConsolePanel()
    {
        super("Output");

        textPane = new JTextPane();
        errorColor = Color.red;
        outColor = Color.black;
        font = new Font("arial", Font.PLAIN, 11);

        final JScrollPane scrollPane = new JScrollPane(textPane);
        scrollPane.setPreferredSize(new Dimension(300, 200));
        final JButton clearLogButton = new JButton("Clear");
        final JButton copyLogButton = new JButton("Copy");
        final JButton reportLogButton = new JButton("Report");

        ComponentUtil.setFontSize(textPane, 10);
        textPane.setEditable(false);
        // FIXME: take a bit of time
        textPane.setContentType("text/html");

        final JPanel actionPanel = GuiUtil.generatePanel();
        actionPanel.add(GuiUtil.besidesPanel(clearLogButton, copyLogButton, reportLogButton));
        actionPanel.setMaximumSize(new Dimension(300, 0));

        clearLogButton.setToolTipText("Clear log info");
        copyLogButton.setToolTipText("Copy to clipboard log info");
        reportLogButton.setToolTipText("Send this as an envent report to the dev team");

        clearLogButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                textPane.setText("<html>");
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
                    final StringSelection contents = new StringSelection(textPane.getDocument().getText(0,
                            textPane.getDocument().getLength()));
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
                try
                {
                    // send report
                    Plugin.report(null, textPane.getDocument().getText(0, textPane.getDocument().getLength()));
                }
                catch (BadLocationException e1)
                {
                    // ignore
                }
            }
        });

        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

        add(scrollPane);
        add(actionPanel);

        // redirect standard output
        System.setOut(new WindowsOutPrintStream(System.out, false));
        System.setErr(new WindowsOutPrintStream(System.err, true));
    }

    public void addText(String text, boolean isError)
    {
        final SimpleAttributeSet set = new SimpleAttributeSet();

        if (isError)
            StyleConstants.setForeground(set, errorColor);
        else
            StyleConstants.setForeground(set, outColor);

        try
        {
            textPane.getStyledDocument().insertString(textPane.getStyledDocument().getLength(), text, set);
            textPane.setCaretPosition(textPane.getStyledDocument().getLength());
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
