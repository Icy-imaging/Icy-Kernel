/**
 * 
 */
package icy.gui.preferences;

import icy.network.NetworkUtil;
import icy.preferences.NetworkPreferences;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

/**
 * @author stephane
 */
public class NetworkPreferencePanel extends PreferencePanel
{
    /**
     * 
     */
    private static final long serialVersionUID = -2311019090865779672L;

    public static final String NODE_NAME = "Network";

    private JTextField httpHostField;
    private JSpinner httpPortField;
    private JTextField httpsHostField;
    private JTextField ftpHostField;
    private JSpinner httpsPortField;
    private JSpinner ftpPortField;
    private JTextField socksHostField;
    private JSpinner socksPortField;
    private JComboBox proxySettingComboBox;

    public NetworkPreferencePanel(PreferenceFrame parent)
    {
        super(parent, NODE_NAME, PreferenceFrame.NODE_NAME);

        initialize();
        validate();

        proxySettingComboBox.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent arg0)
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
            }
        });

        load();
    }

    void initialize()
    {
        GridBagLayout gridBagLayout = new GridBagLayout();
        gridBagLayout.columnWidths = new int[] {69, 239, 97, 0, 0};
        gridBagLayout.rowHeights = new int[] {0, 0, 0, 0, 0, 0, 0};
        gridBagLayout.columnWeights = new double[] {0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
        gridBagLayout.rowWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
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

        httpHostField = new JTextField();
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

        httpsHostField = new JTextField();
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

        ftpHostField = new JTextField();
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

        socksHostField = new JTextField();
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

        NetworkUtil.updateNetworkSetting();
    }

}
