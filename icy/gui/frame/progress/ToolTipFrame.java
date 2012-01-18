/**
 * 
 */
package icy.gui.frame.progress;

import icy.gui.util.GuiUtil;
import icy.preferences.GeneralPreferences;
import icy.preferences.XMLPreferences;
import icy.system.thread.ThreadUtil;
import icy.util.StringUtil;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JEditorPane;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.text.html.HTMLDocument;

/**
 * @author Stephane
 */
public class ToolTipFrame extends TaskFrame
{
    // property
    final private static String ID_DISPLAY = "display";

    Timer timer;
    JEditorPane editorPane;
    JCheckBox doNotDisplayCheckbox;

    final int liveTime;
    final String id;
    final XMLPreferences pref;

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
            pref = GeneralPreferences.getPreferencesToolTips().node(id);

            // tool tip should not be displayed ?
            if (!pref.getBoolean(ID_DISPLAY, true))
            {
                // close and exit
                close();
                return;
            }
        }
        else
            pref = null;

        timer = new Timer(liveTime * 1000, new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                processClose();
            }
        });
        timer.setRepeats(false);

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
                        processClose();
                    }
                });

                doNotDisplayCheckbox = new JCheckBox("Do not display in future", false);
                doNotDisplayCheckbox.setToolTipText("Do not display this tooltip the next time");

                mainPanel.setLayout(new BorderLayout());

                mainPanel.add(editorPane, BorderLayout.CENTER);
                if (pref != null)
                    mainPanel.add(GuiUtil.createLineBoxPanel(doNotDisplayCheckbox, Box.createHorizontalGlue()),
                            BorderLayout.SOUTH);
                pack();

                if (ToolTipFrame.this.liveTime != 0)
                    timer.start();
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

    void processClose()
    {
        close();

        // save display flag only if set to false
        if ((pref != null) && doNotDisplayCheckbox.isSelected())
            pref.putBoolean(ID_DISPLAY, false);
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
            timer.stop();

        super.internalClose();
    }
}
