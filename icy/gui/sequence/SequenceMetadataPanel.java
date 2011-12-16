/**
 * 
 */
package icy.gui.sequence;

import icy.gui.component.model.XMLTreeModel;
import icy.sequence.Sequence;
import icy.util.OMEUtil;

import java.awt.BorderLayout;

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
    public SequenceMetadataPanel(Sequence sequence)
    {
        super();

        setLayout(new BorderLayout());

        final JTree tree = new JTree(new XMLTreeModel(OMEUtil.getXMLDocument(sequence.getMetadata())));
        tree.expandRow(0);

        add(new JScrollPane(tree), BorderLayout.CENTER);

        validate();
    }
}
