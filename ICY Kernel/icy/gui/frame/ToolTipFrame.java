package icy.gui.frame;

import icy.gui.component.ComponentUtil;
import icy.gui.util.GuiUtil;
import icy.preferences.GeneralPreferences;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JEditorPane;
import javax.swing.JPanel;

/**
 * Startup tooltips
 * 
 * @author Fabrice de Chaumont
 */
public class ToolTipFrame extends TitledFrame implements ActionListener
{
    String[] tipText = {
            "Welcome ! This window shows tooltips about ICY<br>Press '>' button to read them.",
            "The top left pink disc is the application menu, click on it to get access to file operation (load, save, save as..., recent files)",
            "ICY supports drag and drop: just take your images or folder and drop it !",
            "To download new plugins, click on the 'Plugins' tab and then 'online plugins'.",
            "If you download plugins one by one, they will appear in the 'Plugins' tab. If you download a full workspace, ICY will ask to restart, and a new tab will be created.",
            "The first time ICY starts, it sets up the amount of memory, you can override this setting in the 'preferences' menu.",
            "If you wish to develop plugins, you can browse the tutorial section and look at the source code, or the online documentation.",
            "Press F6 in a viewer to switch to 3D, F5 to go back to 2D."};

    JButton nextButton = new JButton(">");
    JButton previousButton = new JButton("<");
    JButton closeButton = new JButton("Close");
    JCheckBox displayAtNextStartUp = new JCheckBox("Display tooltips at startup", true);

    int currentToolTip = 0;
    JEditorPane editorPane = new JEditorPane("text/html", "text/html");

    public ToolTipFrame()
    {
        super("Startup tips", new Dimension(200, 100), false, true, false, false);

        if (!GeneralPreferences.getStatupTooltip())
            return;

        final JPanel panel = getMainPanel();

        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));

        ComponentUtil.setFixedHeight(editorPane, 100);
        ComponentUtil.setFixedWidth(editorPane, 350);
        panel.add(GuiUtil.createLineBoxPanel(Box.createVerticalStrut(10)));
        panel.add(GuiUtil.createLineBoxPanel(editorPane));
        panel.add(GuiUtil.createLineBoxPanel(Box.createVerticalStrut(10)));
        // panel.add( GuiUtil.createLineBoxPanel( displayAtNextStartUp , closeButton ) );
        panel.add(GuiUtil.createLineBoxPanel(displayAtNextStartUp, closeButton, previousButton, nextButton));

        editorPane.setEditable(false);
        editorPane.setBorder(BorderFactory.createTitledBorder("").getBorder());

        refreshTextArea();

        pack();
        addToMainDesktopPane();
        center();
        setVisible(true);
        requestFocus();

        nextButton.addActionListener(this);
        previousButton.addActionListener(this);
        closeButton.addActionListener(this);
        displayAtNextStartUp.addActionListener(this);
        nextButton.requestFocus();
    }

    private void refreshTextArea()
    {
        if (currentToolTip < 0)
            currentToolTip = tipText.length - 1;
        if (currentToolTip > tipText.length - 1)
            currentToolTip = 0;
        editorPane.setText("<html><center><br>" + tipText[currentToolTip] + "</center></html>");

    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        if (e.getSource() == nextButton)
        {
            currentToolTip++;
            refreshTextArea();
        }
        if (e.getSource() == previousButton)
        {
            currentToolTip--;
            refreshTextArea();
        }
        if (e.getSource() == closeButton)
        {
            close();
        }
        if (e.getSource() == displayAtNextStartUp)
        {
            GeneralPreferences.setStatupTooltip(displayAtNextStartUp.isSelected());
        }
    }
}
