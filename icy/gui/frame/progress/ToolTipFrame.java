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
package icy.gui.frame.progress;

import icy.gui.frame.IcyFrame;
import icy.gui.util.GuiUtil;
import icy.preferences.GeneralPreferences;
import icy.system.thread.ThreadUtil;
import icy.util.StringUtil;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JEditorPane;
import javax.swing.UIManager;
import javax.swing.text.html.HTMLDocument;

/**
 * @author Stephane
 */
public class ToolTipFrame extends TaskFrame
{
    Timer timer;
    JEditorPane editorPane;
    JCheckBox doNotDisplayCheckbox;

    final int liveTime;
    final String id;

    /**
     * Show an tool tip with specified parameters
     * 
     * @param message
     *        message to display in tool tip
     * @param liveTime
     *        life time in second (0 = infinite)
     * @param id
     *        toolTip id, it's used to display the "Do not display in future" checkbox<br>
     *        and remember its value
     */
    public ToolTipFrame(final String message, int liveTime, String id)
    {
        super();

        this.liveTime = liveTime;
        this.id = id;

        if (!StringUtil.isEmpty(id))
        {
            // tool tip should not be displayed ?
            if (!GeneralPreferences.getPreferencesToolTips().getBoolean(id, true) || alreadyExist(id))
            {
                // close and exit
                close();
                return;
            }
        }

        if (liveTime != 0)
        {
            timer = new Timer("ToolTip timer");
            timer.schedule(new TimerTask()
            {
                @Override
                public void run()
                {
                    // EDT safe
                    doClose();
                }
            }, liveTime * 1000);
        }

        ThreadUtil.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                editorPane = new JEditorPane("text/html", message);
                editorPane.setMinimumSize(new Dimension(240, 60));
                editorPane.setEditable(false);
                editorPane.setToolTipText("Click to close the tool tip");
                // set same font as JLabel for JEditorPane
                final Font font = UIManager.getFont("Label.font");
                final String bodyRule = "body { font-family: " + font.getFamily() + "; " + "font-size: "
                        + font.getSize() + "pt; }";
                ((HTMLDocument) editorPane.getDocument()).getStyleSheet().addRule(bodyRule);
                editorPane.addMouseListener(new MouseAdapter()
                {
                    @Override
                    public void mouseClicked(MouseEvent e)
                    {
                        doClose();
                    }
                });

                doNotDisplayCheckbox = new JCheckBox("Do not display again", false);
                doNotDisplayCheckbox.setToolTipText("Do not display this tooltip the next time");

                mainPanel.setLayout(new BorderLayout());

                mainPanel.add(editorPane, BorderLayout.CENTER);
                if (!StringUtil.isEmpty(ToolTipFrame.this.id))
                    mainPanel.add(GuiUtil.createLineBoxPanel(doNotDisplayCheckbox, Box.createHorizontalGlue()),
                            BorderLayout.SOUTH);
                pack();
            }
        });
    }

    /**
     * Show an tool tip with specified message
     * 
     * @param message
     *        message to display in tool tip
     * @param id
     *        toolTip id, it's used to display the "Do not display in future" checkbox<br>
     *        and remember its value
     */
    public ToolTipFrame(String message, String id)
    {
        this(message, 0, id);
    }

    /**
     * Show an tool tip with specified message
     * 
     * @param message
     *        message to display in tool tip
     * @param liveTime
     *        life time in second (0 = infinite)
     */
    public ToolTipFrame(String message, int liveTime)
    {
        this(message, liveTime, "");
    }

    /**
     * Show an tool tip with specified message
     * 
     * @param message
     *        message to display in tool tip
     */
    public ToolTipFrame(String message)
    {
        this(message, 0, "");
    }

    /**
     * Return true if a tooltip with the same is is already active
     */
    private boolean alreadyExist(String id)
    {
        final List<IcyFrame> frames = IcyFrame.getAllFrames(ToolTipFrame.class);

        for (IcyFrame f : frames)
            if ((f != this) && ((ToolTipFrame) f).id.equals(id))
                return true;

        return false;
    }

    void doClose()
    {
        // save display flag only if set to false
        if (!StringUtil.isEmpty(id) && doNotDisplayCheckbox.isSelected())
            GeneralPreferences.getPreferencesToolTips().putBoolean(id, false);

        close();
    }

    public void setText(final String text)
    {
        ThreadUtil.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                editorPane.setText(text);
                pack();
            }
        });
    }

    @Override
    public void internalClose()
    {
        // stop timer
        if (timer != null)
            timer.cancel();

        super.internalClose();
    }

}
