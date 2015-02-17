/**
 * 
 */
package icy.gui.inspector;

import icy.action.CanvasActions;
import icy.canvas.IcyCanvas;
import icy.canvas.Layer;
import icy.gui.component.button.IcyButton;
import icy.gui.viewer.Viewer;
import icy.main.Icy;
import icy.system.thread.ThreadUtil;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.border.EmptyBorder;

/**
 * @author Stephane
 */
public class LayerControlPanel extends JPanel implements ChangeListener
{
    /**
     * 
     */
    private static final long serialVersionUID = 6501341338561271486L;

    // GUI
    JScrollPane scrollPane;
    JSlider opacitySlider;
    IcyButton deleteButton;

    // internal
    final LayersPanel layerPanel;

    public LayerControlPanel(LayersPanel layerPanel)
    {
        super();

        this.layerPanel = layerPanel;

        initialize();

        opacitySlider.addChangeListener(this);
    }

    private void initialize()
    {
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

        final JPanel actionPanel = new JPanel();
        actionPanel.setBorder(new EmptyBorder(2, 0, 0, 0));
        add(actionPanel);
        GridBagLayout gbl_actionPanel = new GridBagLayout();
        gbl_actionPanel.columnWidths = new int[] {0, 0, 0, 0};
        gbl_actionPanel.rowHeights = new int[] {0, 0, 0};
        gbl_actionPanel.columnWeights = new double[] {0.0, 1.0, 0.0, Double.MIN_VALUE};
        gbl_actionPanel.rowWeights = new double[] {1.0, 0.0, Double.MIN_VALUE};
        actionPanel.setLayout(gbl_actionPanel);

        scrollPane = new JScrollPane();
        scrollPane.setMaximumSize(new Dimension(32767, 400));
        GridBagConstraints gbc_scrollPane = new GridBagConstraints();
        gbc_scrollPane.gridwidth = 3;
        gbc_scrollPane.insets = new Insets(0, 0, 5, 5);
        gbc_scrollPane.fill = GridBagConstraints.BOTH;
        gbc_scrollPane.gridx = 0;
        gbc_scrollPane.gridy = 0;
        actionPanel.add(scrollPane, gbc_scrollPane);

        final JLabel lblOpacity = new JLabel(" Opacity  ");
        GridBagConstraints gbc_lblOpacity = new GridBagConstraints();
        gbc_lblOpacity.anchor = GridBagConstraints.WEST;
        gbc_lblOpacity.insets = new Insets(0, 0, 0, 5);
        gbc_lblOpacity.gridx = 0;
        gbc_lblOpacity.gridy = 1;
        actionPanel.add(lblOpacity, gbc_lblOpacity);

        opacitySlider = new JSlider();
        opacitySlider.setPreferredSize(new Dimension(120, 23));
        opacitySlider.setFocusable(false);
        opacitySlider.setMinimumSize(new Dimension(120, 23));
        GridBagConstraints gbc_opacitySlider = new GridBagConstraints();
        gbc_opacitySlider.fill = GridBagConstraints.HORIZONTAL;
        gbc_opacitySlider.insets = new Insets(0, 0, 0, 5);
        gbc_opacitySlider.gridx = 1;
        gbc_opacitySlider.gridy = 1;
        actionPanel.add(opacitySlider, gbc_opacitySlider);

        deleteButton = new IcyButton(CanvasActions.deleteLayersAction);
        deleteButton.setFlat(true);
        GridBagConstraints gbc_deleteButton = new GridBagConstraints();
        gbc_deleteButton.anchor = GridBagConstraints.EAST;
        gbc_deleteButton.gridx = 2;
        gbc_deleteButton.gridy = 1;
        actionPanel.add(deleteButton, gbc_deleteButton);

        validate();
    }

    public void refresh()
    {
        final List<Layer> selectedLayers = layerPanel.getSelectedLayers();
        final boolean hasSelected = (selectedLayers.size() > 0);
        final boolean singleSelected = (selectedLayers.size() == 1);
        final Layer firstSelected = hasSelected ? selectedLayers.get(0) : null;

        // boolean canEdit = false;
        boolean canRemove = false;

        for (Layer layer : selectedLayers)
        {
            // canEdit |= !layer.isReadOnly();
            canRemove |= layer.getCanBeRemoved();
        }

        final boolean canRemovef = canRemove;

        ThreadUtil.invokeNow(new Runnable()
        {
            @Override
            public void run()
            {
                if (hasSelected)
                {
                    opacitySlider.setValue((int) (firstSelected.getOpacity() * 100f));
                    opacitySlider.setEnabled(true);
                    deleteButton.setEnabled(canRemovef);
                }
                else
                {
                    opacitySlider.setEnabled(false);
                    deleteButton.setEnabled(false);
                }

                if (singleSelected)
                {
                    final JPanel panel = firstSelected.getOverlay().getOptionsPanel();

                    scrollPane.setViewportView(panel);
                    scrollPane.setVisible(panel != null);
                }
                else
                {
                    scrollPane.setVisible(false);
                    scrollPane.setViewportView(null);
                }

                revalidate();
            }
        });
    }

    @Override
    public void stateChanged(ChangeEvent e)
    {
        final Viewer viewer = Icy.getMainInterface().getActiveViewer();

        if (viewer != null)
        {
            final IcyCanvas canvas = viewer.getCanvas();

            if (canvas != null)
            {
                final List<Layer> selectedLayers = layerPanel.getSelectedLayers();
                final int value = opacitySlider.getValue();

                if (selectedLayers.size() > 0)
                {
                    canvas.beginUpdate();
                    try
                    {
                        // set layer transparency
                        for (Layer layer : selectedLayers)
                            layer.setOpacity(value / 100f);
                    }
                    finally
                    {
                        canvas.endUpdate();
                    }
                }
            }
        }

    }
}
