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
package icy.gui.preferences;

import icy.gui.component.IcyTextField;
import icy.gui.component.IcyTextField.TextChangeListener;
import icy.network.NetworkUtil;
import icy.preferences.NetworkPreferences;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * @author stephane
 */
public class NetworkPreferencePanel extends PreferencePanel implements ActionListener, TextChangeListener,
        ChangeListener, DocumentListener
{
    /**
     * 
     */
    private static final long serialVersionUID = -2311019090865779672L;

    public static final String NODE_NAME = "Network";

    private JComboBox proxySettingComboBox;
    private IcyTextField httpHostField;
    private JSpinner httpPortField;
    private IcyTextField httpsHostField;
    private IcyTextField ftpHostField;
    private JSpinner httpsPortField;
    private JSpinner ftpPortField;
    private IcyTextField socksHostField;
    private JSpinner socksPortField;
    private JCheckBox useAuthenticationChkBox;
    private JLabel lblLogin;
    private JLabel lblPassword;
    private IcyTextField userField;
    private JPasswordField passwordField;

    public NetworkPreferencePanel(PreferenceFrame parent)
    {
        super(parent, NODE_NAME, PreferenceFrame.NODE_NAME);

        initialize();

        validate();
        load();

        updateComponentsState();

        proxySettingComboBox.addActionListener(this);
        httpHostField.addTextChangeListener(this);
        httpPortField.addChangeListener(this);
        httpsHostField.addTextChangeListener(this);
        httpsPortField.addChangeListener(this);
        ftpHostField.addTextChangeListener(this);
        ftpPortField.addChangeListener(this);
        socksHostField.addTextChangeListener(this);
        socksPortField.addChangeListener(this);

        userField.addTextChangeListener(this);
        passwordField.getDocument().addDocumentListener(this);

        useAuthenticationChkBox.addActionListener(this);
    }

    void initialize()
    {
        GridBagLayout gridBagLayout = new GridBagLayout();
        gridBagLayout.columnWidths = new int[] {69, 239, 97, 0, 0};
        gridBagLayout.rowHeights = new int[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        gridBagLayout.columnWeights = new double[] {0.0, 1.0, 0.0, 1.0, Double.MIN_VALUE};
        gridBagLayout.rowWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
        mainPanel.setLayout(gridBagLayout);

        JLabel lblProxy = new JLabel("Proxy");
        GridBagConstraints gbc_lblProxy = new GridBagConstraints();
        gbc_lblProxy.anchor = GridBagConstraints.EAST;
        gbc_lblProxy.insets = new Insets(0, 0, 5, 5);
        gbc_lblProxy.gridx = 0;
        gbc_lblProxy.gridy = 0;
        mainPanel.add(lblProxy, gbc_lblProxy);

        proxySettingComboBox = new JComboBox();
        proxySettingComboBox.setModel(new DefaultComboBoxModel(
                new String[] {"No proxy", "System proxy", "Manual proxy"}));
        proxySettingComboBox.setToolTipText("Proxy setting");
        GridBagConstraints gbc_proxySettingComboBox = new GridBagConstraints();
        gbc_proxySettingComboBox.insets = new Insets(0, 0, 5, 5);
        gbc_proxySettingComboBox.fill = GridBagConstraints.HORIZONTAL;
        gbc_proxySettingComboBox.gridx = 1;
        gbc_proxySettingComboBox.gridy = 0;
        mainPanel.add(proxySettingComboBox, gbc_proxySettingComboBox);

        JLabel lblNewLabel = new JLabel("HTTP");
        GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
        gbc_lblNewLabel.anchor = GridBagConstraints.EAST;
        gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
        gbc_lblNewLabel.gridx = 0;
        gbc_lblNewLabel.gridy = 1;
        mainPanel.add(lblNewLabel, gbc_lblNewLabel);

        httpHostField = new IcyTextField();
        httpHostField.setToolTipText("HTTP proxy host");
        GridBagConstraints gbc_httpHostField = new GridBagConstraints();
        gbc_httpHostField.insets = new Insets(0, 0, 5, 5);
        gbc_httpHostField.fill = GridBagConstraints.HORIZONTAL;
        gbc_httpHostField.gridx = 1;
        gbc_httpHostField.gridy = 1;
        mainPanel.add(httpHostField, gbc_httpHostField);
        httpHostField.setColumns(10);

        httpPortField = new JSpinner();
        httpPortField.setModel(new SpinnerNumberModel(80, 0, 65535, 1));
        httpPortField.setToolTipText("HTTPS proxy port");
        GridBagConstraints gbc_httpPortField = new GridBagConstraints();
        gbc_httpPortField.fill = GridBagConstraints.HORIZONTAL;
        gbc_httpPortField.insets = new Insets(0, 0, 5, 5);
        gbc_httpPortField.gridx = 2;
        gbc_httpPortField.gridy = 1;
        mainPanel.add(httpPortField, gbc_httpPortField);

        JLabel lblNewLabel_1 = new JLabel("HTTPS");
        GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
        gbc_lblNewLabel_1.anchor = GridBagConstraints.EAST;
        gbc_lblNewLabel_1.insets = new Insets(0, 0, 5, 5);
        gbc_lblNewLabel_1.gridx = 0;
        gbc_lblNewLabel_1.gridy = 2;
        mainPanel.add(lblNewLabel_1, gbc_lblNewLabel_1);

        httpsHostField = new IcyTextField();
        httpsHostField.setToolTipText("HTTPS proxy host");
        GridBagConstraints gbc_httpsHostField = new GridBagConstraints();
        gbc_httpsHostField.insets = new Insets(0, 0, 5, 5);
        gbc_httpsHostField.fill = GridBagConstraints.HORIZONTAL;
        gbc_httpsHostField.gridx = 1;
        gbc_httpsHostField.gridy = 2;
        mainPanel.add(httpsHostField, gbc_httpsHostField);
        httpsHostField.setColumns(10);

        httpsPortField = new JSpinner();
        httpsPortField.setModel(new SpinnerNumberModel(443, 0, 65535, 1));
        httpsPortField.setToolTipText("HTTPS proxy port");
        GridBagConstraints gbc_httpsPortField = new GridBagConstraints();
        gbc_httpsPortField.insets = new Insets(0, 0, 5, 5);
        gbc_httpsPortField.fill = GridBagConstraints.HORIZONTAL;
        gbc_httpsPortField.gridx = 2;
        gbc_httpsPortField.gridy = 2;
        mainPanel.add(httpsPortField, gbc_httpsPortField);

        JLabel lblNewLabel_2 = new JLabel("FTP");
        GridBagConstraints gbc_lblNewLabel_2 = new GridBagConstraints();
        gbc_lblNewLabel_2.anchor = GridBagConstraints.EAST;
        gbc_lblNewLabel_2.insets = new Insets(0, 0, 5, 5);
        gbc_lblNewLabel_2.gridx = 0;
        gbc_lblNewLabel_2.gridy = 3;
        mainPanel.add(lblNewLabel_2, gbc_lblNewLabel_2);

        ftpHostField = new IcyTextField();
        ftpHostField.setToolTipText("FTP proxy host");
        GridBagConstraints gbc_ftpHostField = new GridBagConstraints();
        gbc_ftpHostField.insets = new Insets(0, 0, 5, 5);
        gbc_ftpHostField.fill = GridBagConstraints.HORIZONTAL;
        gbc_ftpHostField.gridx = 1;
        gbc_ftpHostField.gridy = 3;
        mainPanel.add(ftpHostField, gbc_ftpHostField);
        ftpHostField.setColumns(10);

        ftpPortField = new JSpinner();
        ftpPortField.setModel(new SpinnerNumberModel(21, 0, 65535, 1));
        ftpPortField.setToolTipText("FTP proxy port");
        GridBagConstraints gbc_ftpPortField = new GridBagConstraints();
        gbc_ftpPortField.insets = new Insets(0, 0, 5, 5);
        gbc_ftpPortField.fill = GridBagConstraints.HORIZONTAL;
        gbc_ftpPortField.gridx = 2;
        gbc_ftpPortField.gridy = 3;
        mainPanel.add(ftpPortField, gbc_ftpPortField);

        JLabel lblNewLabel_3 = new JLabel("SOCKS");
        GridBagConstraints gbc_lblNewLabel_3 = new GridBagConstraints();
        gbc_lblNewLabel_3.anchor = GridBagConstraints.EAST;
        gbc_lblNewLabel_3.insets = new Insets(0, 0, 5, 5);
        gbc_lblNewLabel_3.gridx = 0;
        gbc_lblNewLabel_3.gridy = 4;
        mainPanel.add(lblNewLabel_3, gbc_lblNewLabel_3);

        socksHostField = new IcyTextField();
        socksHostField.setToolTipText("SOCKS host");
        GridBagConstraints gbc_socksHostField = new GridBagConstraints();
        gbc_socksHostField.insets = new Insets(0, 0, 5, 5);
        gbc_socksHostField.fill = GridBagConstraints.HORIZONTAL;
        gbc_socksHostField.gridx = 1;
        gbc_socksHostField.gridy = 4;
        mainPanel.add(socksHostField, gbc_socksHostField);
        socksHostField.setColumns(10);

        socksPortField = new JSpinner();
        socksPortField.setModel(new SpinnerNumberModel(1080, 0, 65535, 1));
        socksPortField.setToolTipText("SOCKS port");
        GridBagConstraints gbc_socksPortField = new GridBagConstraints();
        gbc_socksPortField.insets = new Insets(0, 0, 5, 5);
        gbc_socksPortField.fill = GridBagConstraints.HORIZONTAL;
        gbc_socksPortField.gridx = 2;
        gbc_socksPortField.gridy = 4;
        mainPanel.add(socksPortField, gbc_socksPortField);

        useAuthenticationChkBox = new JCheckBox("Use authentication");
        GridBagConstraints gbc_chckbxUseAuthentication = new GridBagConstraints();
        gbc_chckbxUseAuthentication.anchor = GridBagConstraints.WEST;
        gbc_chckbxUseAuthentication.insets = new Insets(0, 0, 5, 5);
        gbc_chckbxUseAuthentication.gridx = 1;
        gbc_chckbxUseAuthentication.gridy = 5;
        mainPanel.add(useAuthenticationChkBox, gbc_chckbxUseAuthentication);

        lblLogin = new JLabel("User");
        GridBagConstraints gbc_lblLogin = new GridBagConstraints();
        gbc_lblLogin.anchor = GridBagConstraints.EAST;
        gbc_lblLogin.insets = new Insets(0, 0, 5, 5);
        gbc_lblLogin.gridx = 0;
        gbc_lblLogin.gridy = 6;
        mainPanel.add(lblLogin, gbc_lblLogin);

        userField = new IcyTextField();
        GridBagConstraints gbc_userField = new GridBagConstraints();
        gbc_userField.insets = new Insets(0, 0, 5, 5);
        gbc_userField.fill = GridBagConstraints.HORIZONTAL;
        gbc_userField.gridx = 1;
        gbc_userField.gridy = 6;
        mainPanel.add(userField, gbc_userField);
        userField.setColumns(10);

        lblPassword = new JLabel("Password");
        GridBagConstraints gbc_lblPassword = new GridBagConstraints();
        gbc_lblPassword.anchor = GridBagConstraints.EAST;
        gbc_lblPassword.insets = new Insets(0, 0, 5, 5);
        gbc_lblPassword.gridx = 0;
        gbc_lblPassword.gridy = 7;
        mainPanel.add(lblPassword, gbc_lblPassword);

        passwordField = new JPasswordField();
        GridBagConstraints gbc_passwordField = new GridBagConstraints();
        gbc_passwordField.insets = new Insets(0, 0, 5, 5);
        gbc_passwordField.fill = GridBagConstraints.HORIZONTAL;
        gbc_passwordField.gridx = 1;
        gbc_passwordField.gridy = 7;
        mainPanel.add(passwordField, gbc_passwordField);
    }

    private void updateComponentsState()
    {
        final boolean enabled = proxySettingComboBox.getSelectedIndex() == 2;

        httpHostField.setEnabled(enabled);
        httpPortField.setEnabled(enabled);
        httpsHostField.setEnabled(enabled);
        httpsPortField.setEnabled(enabled);
        ftpHostField.setEnabled(enabled);
        ftpPortField.setEnabled(enabled);
        socksHostField.setEnabled(enabled);
        socksPortField.setEnabled(enabled);
        useAuthenticationChkBox.setEnabled(enabled);

        final boolean authEnabled = enabled && useAuthenticationChkBox.isSelected();

        userField.setEnabled(authEnabled);
        passwordField.setEnabled(authEnabled);
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        final Object source = e.getSource();

        if ((source == proxySettingComboBox) || (source == useAuthenticationChkBox))
            updateComponentsState();

        // network setting changed, restart needed
        getPreferenceFrame().setNeedRestart();
    }

    @Override
    public void textChanged(IcyTextField source, boolean validate)
    {
        // network setting changed, restart needed
        if (validate)
            getPreferenceFrame().setNeedRestart();
    }

    @Override
    public void stateChanged(ChangeEvent e)
    {
        // network setting changed, restart needed
        getPreferenceFrame().setNeedRestart();
    }

    @Override
    protected void load()
    {
        proxySettingComboBox.setSelectedIndex(NetworkPreferences.getProxySetting());
        httpHostField.setText(NetworkPreferences.getProxyHTTPHost());
        httpPortField.setValue(Integer.valueOf(NetworkPreferences.getProxyHTTPPort()));
        httpsHostField.setText(NetworkPreferences.getProxyHTTPSHost());
        httpsPortField.setValue(Integer.valueOf(NetworkPreferences.getProxyHTTPSPort()));
        ftpHostField.setText(NetworkPreferences.getProxyFTPHost());
        ftpPortField.setValue(Integer.valueOf(NetworkPreferences.getProxyFTPPort()));
        socksHostField.setText(NetworkPreferences.getProxySOCKSHost());
        socksPortField.setValue(Integer.valueOf(NetworkPreferences.getProxySOCKSPort()));
        useAuthenticationChkBox.setSelected(NetworkPreferences.getProxyAuthentication());
        userField.setText(NetworkPreferences.getProxyUser());
        passwordField.setText(NetworkPreferences.getProxyPassword());
    }

    @Override
    protected void save()
    {
        NetworkPreferences.setProxySetting(proxySettingComboBox.getSelectedIndex());
        NetworkPreferences.setProxyHTTPHost(httpHostField.getText());
        NetworkPreferences.setProxyHTTPPort(((Integer) httpPortField.getValue()).intValue());
        NetworkPreferences.setProxyHTTPSHost(httpsHostField.getText());
        NetworkPreferences.setProxyHTTPSPort(((Integer) httpsPortField.getValue()).intValue());
        NetworkPreferences.setProxyFTPHost(ftpHostField.getText());
        NetworkPreferences.setProxyFTPPort(((Integer) ftpPortField.getValue()).intValue());
        NetworkPreferences.setProxySOCKSHost(socksHostField.getText());
        NetworkPreferences.setProxySOCKSPort(((Integer) socksPortField.getValue()).intValue());
        NetworkPreferences.setProxyAuthentication(useAuthenticationChkBox.isSelected());
        NetworkPreferences.setProxyUser(userField.getText());
        NetworkPreferences.setProxyPassword(new String(passwordField.getPassword()));

        NetworkUtil.updateNetworkSetting();
    }

    @Override
    public void insertUpdate(DocumentEvent e)
    {
        // network setting changed, restart needed
        getPreferenceFrame().setNeedRestart();
    }

    @Override
    public void removeUpdate(DocumentEvent e)
    {
        // network setting changed, restart needed
        getPreferenceFrame().setNeedRestart();
    }

    @Override
    public void changedUpdate(DocumentEvent e)
    {
        // network setting changed, restart needed
        getPreferenceFrame().setNeedRestart();
    }
}
