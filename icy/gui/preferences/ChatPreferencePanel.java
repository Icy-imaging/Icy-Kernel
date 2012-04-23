package icy.gui.preferences;

import icy.gui.main.MainFrame;
import icy.main.Icy;
import icy.preferences.ChatPreferences;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

public class ChatPreferencePanel extends PreferencePanel
{
    /**
     * 
     */
    private static final long serialVersionUID = 2856629717614258089L;

    public static final String NODE_NAME = "Chat";
    private JTextField realNameField;
    private JPasswordField passwordField;
    JTextField extraChannelsField;
    private JCheckBox connectAtStartCheckBox;
    private JCheckBox enableDesktopOverlayCheckBox;
    JTextField desktopChannelsField;

    /**
     * Create the panel.
     */
    public ChatPreferencePanel(PreferenceFrame parent)
    {
        super(parent, NODE_NAME, PreferenceFrame.NODE_NAME);

        initialize();

        validate();

        load();
    }

    void initialize()
    {
        GridBagLayout gridBagLayout = new GridBagLayout();
        gridBagLayout.columnWidths = new int[] {46, 97, 0, 0, 18, 60, 0, 0};
        gridBagLayout.rowHeights = new int[] {0, 0, 0, 0, 0, 0};
        gridBagLayout.columnWeights = new double[] {0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
        gridBagLayout.rowWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
        mainPanel.setLayout(gridBagLayout);

        connectAtStartCheckBox = new JCheckBox("Connect at start up");
        connectAtStartCheckBox.setToolTipText("Automatically connect when application starts");
        GridBagConstraints gbc_connectAtStartCheckBox = new GridBagConstraints();
        gbc_connectAtStartCheckBox.anchor = GridBagConstraints.WEST;
        gbc_connectAtStartCheckBox.gridwidth = 2;
        gbc_connectAtStartCheckBox.insets = new Insets(0, 0, 5, 5);
        gbc_connectAtStartCheckBox.gridx = 0;
        gbc_connectAtStartCheckBox.gridy = 0;
        mainPanel.add(connectAtStartCheckBox, gbc_connectAtStartCheckBox);
        
        enableDesktopOverlayCheckBox = new JCheckBox("Enable desktop chat");
        enableDesktopOverlayCheckBox.setToolTipText("Display chat in the application desktop");
        GridBagConstraints gbc_enableDesktopOverlayCheckBox = new GridBagConstraints();
        gbc_enableDesktopOverlayCheckBox.anchor = GridBagConstraints.WEST;
        gbc_enableDesktopOverlayCheckBox.gridwidth = 2;
        gbc_enableDesktopOverlayCheckBox.insets = new Insets(0, 0, 5, 5);
        gbc_enableDesktopOverlayCheckBox.gridx = 0;
        gbc_enableDesktopOverlayCheckBox.gridy = 1;
        mainPanel.add(enableDesktopOverlayCheckBox, gbc_enableDesktopOverlayCheckBox);

        desktopChannelsField = new JTextField();
        desktopChannelsField
                .setToolTipText("Channel(s) to display on dekstop chat. You can enter severals channels (ex : \"icy;icy-support\")");
        desktopChannelsField.setText("icy");
        GridBagConstraints gbc_desktopChannelsField = new GridBagConstraints();
        gbc_desktopChannelsField.gridwidth = 2;
        gbc_desktopChannelsField.insets = new Insets(0, 0, 5, 5);
        gbc_desktopChannelsField.fill = GridBagConstraints.HORIZONTAL;
        gbc_desktopChannelsField.gridx = 2;
        gbc_desktopChannelsField.gridy = 1;
        mainPanel.add(desktopChannelsField, gbc_desktopChannelsField);
        desktopChannelsField.setColumns(10);

        JButton btnDefault_1 = new JButton("Default");
        btnDefault_1.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                desktopChannelsField.setText(ChatPreferences.getDefaultDesktopChannels());
            }
        });
        GridBagConstraints gbc_btnDefault_1 = new GridBagConstraints();
        gbc_btnDefault_1.insets = new Insets(0, 0, 5, 5);
        gbc_btnDefault_1.gridx = 5;
        gbc_btnDefault_1.gridy = 1;
        mainPanel.add(btnDefault_1, gbc_btnDefault_1);

        JLabel lblNewLabel_2 = new JLabel("Real name");
        lblNewLabel_2.setToolTipText("Real name (give more information about user)");
        GridBagConstraints gbc_lblNewLabel_2 = new GridBagConstraints();
        gbc_lblNewLabel_2.anchor = GridBagConstraints.EAST;
        gbc_lblNewLabel_2.insets = new Insets(0, 0, 5, 5);
        gbc_lblNewLabel_2.gridx = 0;
        gbc_lblNewLabel_2.gridy = 2;
        mainPanel.add(lblNewLabel_2, gbc_lblNewLabel_2);

        realNameField = new JTextField();
        realNameField.setToolTipText("Real name (give more information about user)");
        GridBagConstraints gbc_realNameField = new GridBagConstraints();
        gbc_realNameField.fill = GridBagConstraints.HORIZONTAL;
        gbc_realNameField.gridwidth = 3;
        gbc_realNameField.insets = new Insets(0, 0, 5, 5);
        gbc_realNameField.gridx = 1;
        gbc_realNameField.gridy = 2;
        mainPanel.add(realNameField, gbc_realNameField);
        realNameField.setColumns(10);

        JLabel lblPassword = new JLabel("Password");
        lblPassword.setToolTipText("Password for registered nickname only");
        GridBagConstraints gbc_lblPassword = new GridBagConstraints();
        gbc_lblPassword.anchor = GridBagConstraints.EAST;
        gbc_lblPassword.insets = new Insets(0, 0, 5, 5);
        gbc_lblPassword.gridx = 0;
        gbc_lblPassword.gridy = 3;
        mainPanel.add(lblPassword, gbc_lblPassword);

        passwordField = new JPasswordField();
        passwordField.setColumns(12);
        passwordField.setToolTipText("Password for registered nickname only");
        GridBagConstraints gbc_passwordField = new GridBagConstraints();
        gbc_passwordField.gridwidth = 3;
        gbc_passwordField.fill = GridBagConstraints.HORIZONTAL;
        gbc_passwordField.insets = new Insets(0, 0, 5, 5);
        gbc_passwordField.gridx = 1;
        gbc_passwordField.gridy = 3;
        mainPanel.add(passwordField, gbc_passwordField);

        JLabel lblChannels = new JLabel("Extra channels");
        lblChannels
                .setToolTipText("Extra channels to join at start up. You can enter severals channels (ex : \"icy-news;icy-support;others\")");
        GridBagConstraints gbc_lblChannels = new GridBagConstraints();
        gbc_lblChannels.anchor = GridBagConstraints.EAST;
        gbc_lblChannels.insets = new Insets(0, 0, 0, 5);
        gbc_lblChannels.gridx = 0;
        gbc_lblChannels.gridy = 4;
        mainPanel.add(lblChannels, gbc_lblChannels);

        extraChannelsField = new JTextField();
        extraChannelsField
                .setToolTipText("Extra channel(s) to join at start up. You can enter severals channels (ex : \"icy-news;icy-support;...\")");
        GridBagConstraints gbc_channelsField = new GridBagConstraints();
        gbc_channelsField.gridwidth = 3;
        gbc_channelsField.insets = new Insets(0, 0, 0, 5);
        gbc_channelsField.fill = GridBagConstraints.HORIZONTAL;
        gbc_channelsField.gridx = 1;
        gbc_channelsField.gridy = 4;
        mainPanel.add(extraChannelsField, gbc_channelsField);
        extraChannelsField.setColumns(10);

        JButton btnDefault = new JButton("Default");
        btnDefault.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                extraChannelsField.setText(ChatPreferences.getDefaultExtraChannels());
            }
        });
        GridBagConstraints gbc_btnDefault = new GridBagConstraints();
        gbc_btnDefault.insets = new Insets(0, 0, 0, 5);
        gbc_btnDefault.gridx = 5;
        gbc_btnDefault.gridy = 4;
        mainPanel.add(btnDefault, gbc_btnDefault);
    }

    @Override
    protected void load()
    {
        realNameField.setText(ChatPreferences.getRealname());
        passwordField.setText(ChatPreferences.getUserPassword());
        extraChannelsField.setText(ChatPreferences.getExtraChannels());
        connectAtStartCheckBox.setSelected(ChatPreferences.getAutoConnect());
        enableDesktopOverlayCheckBox.setSelected(ChatPreferences.getDesktopOverlay());
        desktopChannelsField.setText(ChatPreferences.getDesktopChannels());
    }

    @Override
    protected void save()
    {
        ChatPreferences.setRealname(realNameField.getText());
        ChatPreferences.setUserPassword(new String(passwordField.getPassword()));
        ChatPreferences.setExtraChannels(extraChannelsField.getText());
        ChatPreferences.setAutoConnect(connectAtStartCheckBox.isSelected());
        ChatPreferences.setDesktopOverlay(enableDesktopOverlayCheckBox.isSelected());
        ChatPreferences.setDesktopChannels(desktopChannelsField.getText());

        final MainFrame mainFrame = Icy.getMainInterface().getMainFrame();

        // repaint desktop pane for desktop overlay change
        if (mainFrame != null)
            mainFrame.getChat().refreshDesktopOverlayState();
    }

}
