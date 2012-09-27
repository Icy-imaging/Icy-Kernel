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
package icy.gui.dialog;

import icy.main.Icy;
import icy.resource.ResourceUtil;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
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

    protected final JPanel mainPanel;
    protected final JPanel buttonPanel;

    final JButton okBtn;
    final JButton cancelBtn;

    private ActionListener okAction;
    private boolean closeAfterAction;

    /**
     * @wbp.parser.constructor
     */
    public ActionDialog(String title)
    {
        this(Icy.getMainInterface().getMainFrame(), title);
    }

    public ActionDialog(Frame owner, String title)
    {
        super(owner, title, true);

        setIconImages(ResourceUtil.getIcyIconImages());

        // GUI
        mainPanel = new JPanel();

        buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        okBtn = new JButton("Ok");
        cancelBtn = new JButton("Cancel");

        buttonPanel.add(Box.createHorizontalStrut(10));
        buttonPanel.add(Box.createHorizontalGlue());
        buttonPanel.add(okBtn);
        buttonPanel.add(Box.createHorizontalStrut(10));
        buttonPanel.add(cancelBtn);
        buttonPanel.add(Box.createHorizontalGlue());
        buttonPanel.add(Box.createHorizontalStrut(10));

        getContentPane().add(mainPanel, BorderLayout.CENTER);
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);

        // OTHERS
        okBtn.setActionCommand(OK_CMD);
        cancelBtn.setActionCommand(CANCEL_CMD);
        okBtn.addActionListener(this);
        cancelBtn.addActionListener(this);

        okAction = null;
        closeAfterAction = true;
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
            // do action here
            if (okAction != null)
                okAction.actionPerformed(e);
            // close if wanted
            if (closeAfterAction)
                dispose();
        }
    }

}
