/**
 * 
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
