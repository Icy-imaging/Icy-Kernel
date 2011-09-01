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
package icy.gui.frame;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;

/**
 * @author Stephane
 */
public class ActionFrame extends TitledFrame implements ActionListener
{
    protected static final String OK_CMD = "ok";
    protected static final String CANCEL_CMD = "cancel";

    final JButton okBtn;
    final JButton cancelBtn;

    private ActionListener okAction;
    private boolean closeAfterAction;

    public ActionFrame(String title)
    {
        this(title, false, false);
    }

    public ActionFrame(String title, boolean resizable)
    {
        this(title, resizable, false);
    }

    public ActionFrame(String title, boolean resizable, boolean iconifiable)
    {
        super(title, resizable, true, false, iconifiable);

        final JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        // buttonPanel.setPreferredSize(new Dimension(600, 34));

        okBtn = new JButton("Ok");
        cancelBtn = new JButton("Cancel");

        buttonPanel.add(Box.createHorizontalGlue());
        buttonPanel.add(okBtn);
        buttonPanel.add(Box.createHorizontalStrut(10));
        buttonPanel.add(cancelBtn);

        add(buttonPanel, BorderLayout.SOUTH);

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
            close();
        else if (OK_CMD.equals(cmd))
        {
            // do action here
            if (okAction != null)
                okAction.actionPerformed(e);
            // close if wanted
            if (closeAfterAction)
                close();
        }
    }
}
