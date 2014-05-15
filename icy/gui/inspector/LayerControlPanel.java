/**
 * 
 */
package icy.gui.inspector;

import icy.action.CanvasActions;
import icy.canvas.Layer;
import icy.gui.component.button.IcyButton;
import icy.system.thread.ThreadUtil;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.border.TitledBorder;

/**
 * @author Stephane
 */
public class LayerControlPanel extends JPanel
{
    /**
     * 
     */
    private static final long serialVersionUID = 6501341338561271486L;

    // GUI
    IcyButton deleteButton;

    // internal
    final LayersPanel layerPanel;

    public LayerControlPanel(LayersPanel layerPanel)
    {
        super();

        this.layerPanel = layerPanel;

        initialize();
    }

    private void initialize()
    {
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

        final JPanel actionPanel = new JPanel();
        actionPanel.setBorder(new TitledBorder(null, "Action", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        add(actionPanel);
        GridBagLayout gbl_actionPanel = new GridBagLayout();
        gbl_actionPanel.columnWidths = new int[] {0, 0};
        gbl_actionPanel.rowHeights = new int[] {0, 0};
        gbl_actionPanel.columnWeights = new double[] {1.0, Double.MIN_VALUE};
        gbl_actionPanel.rowWeights = new double[] {0.0, Double.MIN_VALUE};
        actionPanel.setLayout(gbl_actionPanel);

        final JToolBar toolBar = new JToolBar();
        toolBar.setRollover(true);
        toolBar.setFloatable(false);
        GridBagConstraints gbc_toolBar = new GridBagConstraints();
        gbc_toolBar.fill = GridBagConstraints.HORIZONTAL;
        gbc_toolBar.gridx = 0;
        gbc_toolBar.gridy = 0;
        actionPanel.add(toolBar, gbc_toolBar);

        deleteButton = new IcyButton(CanvasActions.deleteLayersAction);
        toolBar.add(deleteButton);

        final Component horizontalGlue = Box.createHorizontalGlue();
        toolBar.add(horizontalGlue);

        validate();
    }

    public void refresh()
    {
        final List<Layer> selectedLayers = layerPanel.getSelectedLayers();
        final boolean hasSelected = (selectedLayers.size() > 0);

        // boolean canEdit = false;
        boolean canRemove = false;

        for (Layer layer : selectedLayers)
        {
            // canEdit |= !layer.isReadOnly();
            canRemove |= layer.getCanBeRemoved();
        }

        final boolean enabled = hasSelected && canRemove;

        ThreadUtil.invokeNow(new Runnable()
        {
            @Override
            public void run()
            {
                deleteButton.setEnabled(enabled);
            }
        });
    }
}
