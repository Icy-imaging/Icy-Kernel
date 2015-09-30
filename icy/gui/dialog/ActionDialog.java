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
package icy.gui.dialog;

import icy.main.Icy;
import icy.resource.ResourceUtil;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;

/**
 * @author Stephane
 */
public class ActionDialog extends JDialog implements ActionListener
{
    /**
     * 
     */
    private static final long serialVersionUID = -8071873763517931268L;

    protected static final String OK_CMD = "ok";
    protected static final String CANCEL_CMD = "cancel";

    protected JPanel mainPanel;
    protected JPanel buttonPanel;

    JButton okBtn;
    JButton cancelBtn;

    private ActionListener okAction;
    private boolean closeAfterAction;
    boolean opened;
    boolean canceled;
    boolean closed;

    public ActionDialog(String title, JComponent component, Frame owner)
    {
        super(owner, title, true);

        // init GUI
        initialize(component);

        // set action
        okBtn.setActionCommand(OK_CMD);
        cancelBtn.setActionCommand(CANCEL_CMD);
        okBtn.addActionListener(this);
        cancelBtn.addActionListener(this);

        addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowOpened(WindowEvent e)
            {
                opened = true;
            }

            @Override
            public void windowClosed(WindowEvent e)
            {
                closed = true;

                onClosed();
            }
        });

        okAction = null;
        closeAfterAction = true;
        opened = false;
        canceled = true;
        closed = false;
    }

    public ActionDialog(String title, JComponent component)
    {
        this(title, component, Icy.getMainInterface().getMainFrame());
    }

    /**
     * @wbp.parser.constructor
     */
    public ActionDialog(String title)
    {
        this(title, null, Icy.getMainInterface().getMainFrame());
    }

    /**
     * @deprecated Use {@link #ActionDialog(String, JComponent, Frame)} instead
     */
    @Deprecated
    public ActionDialog(Frame owner, String title)
    {
        this(title, null, owner);
    }

    private void initialize(JComponent component)
    {
        setIconImages(ResourceUtil.getIcyIconImages());
        // so we always pass in the closed event
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // GUI
        if (component instanceof JPanel)
            mainPanel = (JPanel) component;
        else
        {
            mainPanel = new JPanel();
            mainPanel.setLayout(new BorderLayout());

            if (component != null)
                mainPanel.add(component, BorderLayout.CENTER);
        }

        buttonPanel = new JPanel();
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        GridBagLayout gbl_buttonPanel = new GridBagLayout();
        gbl_buttonPanel.columnWidths = new int[] {0, 0, 45, 65, 0, 0, 0};
        gbl_buttonPanel.rowHeights = new int[] {23, 0};
        gbl_buttonPanel.columnWeights = new double[] {0.0, 0.0, 1.0, 1.0, 0.0, 0.0, Double.MIN_VALUE};
        gbl_buttonPanel.rowWeights = new double[] {0.0, Double.MIN_VALUE};
        buttonPanel.setLayout(gbl_buttonPanel);

        okBtn = new JButton("Ok");
        GridBagConstraints gbc_okBtn = new GridBagConstraints();
        gbc_okBtn.anchor = GridBagConstraints.EAST;
        gbc_okBtn.insets = new Insets(0, 0, 0, 5);
        gbc_okBtn.gridx = 2;
        gbc_okBtn.gridy = 0;
        buttonPanel.add(okBtn, gbc_okBtn);
        cancelBtn = new JButton("Cancel");
        GridBagConstraints gbc_cancelBtn = new GridBagConstraints();
        gbc_cancelBtn.insets = new Insets(0, 0, 0, 5);
        gbc_cancelBtn.anchor = GridBagConstraints.WEST;
        gbc_cancelBtn.gridx = 3;
        gbc_cancelBtn.gridy = 0;
        buttonPanel.add(cancelBtn, gbc_cancelBtn);

        getContentPane().add(mainPanel, BorderLayout.CENTER);
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);
    }

    /**
     * Easy "onclose" process
     */
    protected void onClosed()
    {
        //
    }

    public boolean isClosed()
    {
        return closed;
    }

    public boolean isCanceled()
    {
        return opened && canceled;
    }

    /**
     * @return the closeAfterAction
     */
    public boolean isCloseAfterAction()
    {
        return closeAfterAction;
    }

    /**
     * @param closeAfterAction
     *        the closeAfterAction to set
     */
    public void setCloseAfterAction(boolean closeAfterAction)
    {
        this.closeAfterAction = closeAfterAction;
    }

    /**
     * @return the okAction
     */
    public ActionListener getOkAction()
    {
        return okAction;
    }

    /**
     * @param okAction
     *        the okAction to set
     */
    public void setOkAction(ActionListener okAction)
    {
        this.okAction = okAction;
    }

    /**
     * @return the mainPanel
     */
    public JPanel getMainPanel()
    {
        return mainPanel;
    }

    /**
     * @return the buttonPanel
     */
    public JPanel getButtonPanel()
    {
        return buttonPanel;
    }

    /**
     * @return the okBtn
     */
    public JButton getOkBtn()
    {
        return okBtn;
    }

    /**
     * @return the cancelBtn
     */
    public JButton getCancelBtn()
    {
        return cancelBtn;
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        final String cmd = e.getActionCommand();

        if (CANCEL_CMD.equals(cmd))
            dispose();
        else if (OK_CMD.equals(cmd))
        {
            canceled = false;
            // do action here
            if (okAction != null)
                okAction.actionPerformed(e);
            // close if wanted
            if (closeAfterAction)
                dispose();
        }
    }
}
