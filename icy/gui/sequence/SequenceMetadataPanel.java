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
package icy.gui.sequence;

import icy.gui.component.model.XMLTreeModel;
import icy.sequence.Sequence;
import icy.system.thread.ThreadUtil;
import icy.util.OMEUtil;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;

/**
 * @author Stephane
 */
public class SequenceMetadataPanel extends JPanel
{
    /**
     * 
     */
    private static final long serialVersionUID = -3889529459830025973L;

    /**
     * Create the panel.
     */
    public SequenceMetadataPanel(final Sequence sequence)
    {
        super();

        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(320, 360));

        final JTree tree = new JTree();
        tree.setVisible(false);

        final JLabel loading = new JLabel("loading...");

        add(loading, BorderLayout.NORTH);
        add(new JScrollPane(tree), BorderLayout.CENTER);

        validate();

        // can take sometime so we do it in background
        ThreadUtil.bgRun(new Runnable()
        {
            @Override
            public void run()
            {
                tree.setModel(new XMLTreeModel(OMEUtil.getXMLDocument(sequence.getMetadata())));

                int row = 0;
                while (row < tree.getRowCount())
                {
                    tree.expandRow(row);
                    row++;
                }

                ThreadUtil.invokeLater(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        tree.setVisible(true);
                        loading.setVisible(false);
                    }
                });
            }
        });
    }
}
