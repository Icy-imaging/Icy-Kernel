/**
 * 
 */
package icy.gui.inspector;

import icy.gui.frame.ActionFrame;
import icy.preferences.XMLPreferences;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Frame to change ROI table settings.
 * 
 * @author Stephane
 */
public class RoiSettingFrame extends ActionFrame
{
    final RoiSettingPanel settingPanel;

    public RoiSettingFrame(XMLPreferences viewPreferences, XMLPreferences exportPreferences, final Runnable onValidate)
    {
        super("ROI table setting", true);

        settingPanel = new RoiSettingPanel(viewPreferences, exportPreferences);
        getMainPanel().add(settingPanel, BorderLayout.CENTER);

        setPreferredSize(new Dimension(520, 480));
        setOkAction(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                // save setting
                settingPanel.save();
                // call callback
                if (onValidate != null)
                    onValidate.run();
            }
        });

        pack();
        addToDesktopPane();
        setVisible(true);
        center();
        requestFocus();
    }
}
