package icy.gui.preferences;

import icy.gui.component.IcyTextField;
import icy.gui.dialog.ActionDialog;
import icy.gui.util.ComponentUtil;
import icy.preferences.RepositoryPreferences.RepositoryInfo;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class EditRepositoryDialog extends ActionDialog
{
    /**
     * 
     */
    private static final long serialVersionUID = 893945926064333575L;

    // GUI
    IcyTextField nameField;
    IcyTextField locationField;
    JCheckBox supportParamCheckBox;
    JCheckBox authCheckBox;
    IcyTextField loginField;
    JPasswordField passwordField;

    JLabel nameLabel;
    JLabel locationLabel;
    JLabel supportParamLabel;
    JLabel authLabel;
    JLabel loginLabel;
    JLabel passwordLabel;

    // internal
    final RepositoryInfo reposInf;

    /**
     * Create the dialog.
     */
    public EditRepositoryDialog(String title, RepositoryInfo reposInf)
    {
        super(title);

        this.reposInf = reposInf;

        initialize();

        updateAuthFields();

        // center on screen and make it visible
        pack();
        ComponentUtil.center(this);
        setVisible(true);
    }

    private void initialize()
    {
        setMinimumSize(new Dimension(400, 200));
        setBounds(100, 100, 450, 300);

        // setPreferredSize(new Dimension(600, 200));

        nameField = new IcyTextField();
        nameField.setText(reposInf.getName());
        ComponentUtil.setFixedHeight(nameField, 24);
        locationField = new IcyTextField();
        locationField.setText(reposInf.getLocation());
        ComponentUtil.setFixedHeight(locationField, 24);
        authCheckBox = new JCheckBox("", reposInf.isAuthenticationEnabled());
        ComponentUtil.setFixedHeight(authCheckBox, 24);
        loginField = new IcyTextField(reposInf.getLogin());
        loginField.setText(reposInf.getLogin());
        ComponentUtil.setFixedHeight(loginField, 24);
        passwordField = new JPasswordField(reposInf.getPassword());
        ComponentUtil.setFixedHeight(passwordField, 24);
        supportParamCheckBox = new JCheckBox("", reposInf.getSupportParam());
        ComponentUtil.setFixedHeight(supportParamCheckBox, 24);

        authCheckBox.addChangeListener(new ChangeListener()
        {
            @Override
            public void stateChanged(ChangeEvent e)
            {
                updateAuthFields();
            }
        });

        // save changes on validation
        setOkAction(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                reposInf.setName(nameField.getText());
                reposInf.setLocation(locationField.getText());
                reposInf.setSupportParam(supportParamCheckBox.isSelected());
                reposInf.setAuthenticationEnabled(authCheckBox.isSelected());
                reposInf.setLogin(loginField.getText());
                reposInf.setPassword(new String(passwordField.getPassword()));
            }
        });

        mainPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        mainPanel.setLayout(new BorderLayout(8, 8));

        final JPanel labelPanel = new JPanel();
        labelPanel.setLayout(new BoxLayout(labelPanel, BoxLayout.PAGE_AXIS));

        nameLabel = new JLabel("Name");
        nameLabel.setToolTipText("Repository name");
        ComponentUtil.setFixedHeight(nameLabel, 24);
        locationLabel = new JLabel("Location");
        locationLabel.setToolTipText("Repository address");
        ComponentUtil.setFixedHeight(locationLabel, 24);
        supportParamLabel = new JLabel("Enable extra parameters");
        supportParamLabel.setToolTipText("Enable extra parameters when querying the XML file (only for online repository)");
        ComponentUtil.setFixedHeight(supportParamLabel, 24);
        authLabel = new JLabel("Use authentication");
        authLabel.setToolTipText("Enable authentication to access the repository");
        ComponentUtil.setFixedHeight(authLabel, 24);
        loginLabel = new JLabel("Login");
        ComponentUtil.setFixedHeight(loginLabel, 24);
        passwordLabel = new JLabel("Password");
        ComponentUtil.setFixedHeight(passwordLabel, 24);

        labelPanel.add(nameLabel);
        labelPanel.add(Box.createVerticalStrut(4));
        labelPanel.add(locationLabel);
        labelPanel.add(Box.createVerticalStrut(4));
        labelPanel.add(supportParamLabel);
        labelPanel.add(Box.createVerticalStrut(4));
        labelPanel.add(authLabel);
        labelPanel.add(Box.createVerticalStrut(4));
        labelPanel.add(loginLabel);
        labelPanel.add(Box.createVerticalStrut(4));
        labelPanel.add(passwordLabel);
        labelPanel.add(Box.createVerticalGlue());

        final JPanel fieldPanel = new JPanel();
        fieldPanel.setLayout(new BoxLayout(fieldPanel, BoxLayout.PAGE_AXIS));

        fieldPanel.add(nameField);
        fieldPanel.add(Box.createVerticalStrut(4));
        fieldPanel.add(locationField);
        fieldPanel.add(Box.createVerticalStrut(4));
        fieldPanel.add(supportParamCheckBox);
        fieldPanel.add(Box.createVerticalStrut(4));
        fieldPanel.add(authCheckBox);
        fieldPanel.add(Box.createVerticalStrut(4));
        fieldPanel.add(loginField);
        fieldPanel.add(Box.createVerticalStrut(4));
        fieldPanel.add(passwordField);
        fieldPanel.add(Box.createVerticalGlue());

        mainPanel.add(labelPanel, BorderLayout.WEST);
        mainPanel.add(fieldPanel, BorderLayout.CENTER);
    }

    void updateAuthFields()
    {
        final boolean enabled = authCheckBox.isSelected();

        loginLabel.setEnabled(enabled);
        loginField.setEnabled(enabled);
        passwordLabel.setEnabled(enabled);
        passwordField.setEnabled(enabled);
    }
}
