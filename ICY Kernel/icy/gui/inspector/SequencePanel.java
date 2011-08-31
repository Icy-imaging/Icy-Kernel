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
package icy.gui.inspector;

import icy.gui.component.PopupPanel;

import java.awt.BorderLayout;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

/**
 * @author Stephane
 */
public class SequencePanel extends JPanel
{
    /**
     * 
     */
    private static final long serialVersionUID = -5727785928741370159L;

    private final PopupPanel canvasPopupPanel;
    private final PopupPanel lutPopupPanel;
    private final PopupPanel infosPopupPanel;
    private final JPanel canvasPanel;
    private final JPanel lutPanel;
    private final JPanel infosPanel;

    /**
     * 
     */
    public SequencePanel()
    {
        super();

        canvasPopupPanel = new PopupPanel("Canvas");
        canvasPanel = canvasPopupPanel.getMainPanel();
        canvasPanel.setLayout(new BorderLayout());
        canvasPopupPanel.expand();
        lutPopupPanel = new PopupPanel("Lookup Table");
        lutPanel = lutPopupPanel.getMainPanel();
        lutPanel.setLayout(new BorderLayout());
        lutPopupPanel.expand();
        infosPopupPanel = new PopupPanel("Sequence Informations");
        infosPanel = infosPopupPanel.getMainPanel();
        infosPanel.setLayout(new BorderLayout());
        infosPopupPanel.expand();

        final JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.PAGE_AXIS));

        topPanel.add(canvasPopupPanel);
        topPanel.add(lutPopupPanel);
        topPanel.add(infosPopupPanel);

        topPanel.validate();

        setLayout(new BorderLayout());

        add(topPanel, BorderLayout.NORTH);
        add(Box.createGlue(), BorderLayout.CENTER);

        validate();
    }

    public void setCanvasPanel(JPanel panel)
    {
        canvasPanel.removeAll();

        if (panel != null)
            canvasPanel.add(panel, BorderLayout.CENTER);

        // FIXME : why we need this ???
        canvasPanel.revalidate();
    }

    public void setLutPanel(JPanel panel)
    {
        lutPanel.removeAll();

        if (panel != null)
            lutPanel.add(panel, BorderLayout.CENTER);

        // FIXME : why we need this ???
        lutPanel.revalidate();
    }

    public void setInfosPanel(JPanel panel)
    {
        infosPanel.removeAll();

        if (panel != null)
            infosPanel.add(panel, BorderLayout.CENTER);

        // FIXME : why we need this ???
        infosPanel.revalidate();
    }
}
