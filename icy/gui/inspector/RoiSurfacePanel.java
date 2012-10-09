/**
 * 
 */
package icy.gui.inspector;

import icy.math.MathUtil;
import icy.math.UnitUtil;
import icy.math.UnitUtil.UnitPrefix;
import icy.roi.ROIUtil.ROIInfos;
import icy.util.StringUtil;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * @author Stephane
 */
public class RoiSurfacePanel extends JPanel
{
    /**
     * 
     */
    private static final long serialVersionUID = -7796039523699617091L;

    // GUI
    private JLabel surfaceValueLabel;
    private JLabel surfaceUnitLabel;
    private JLabel perimeterUnitLabel;
    private JLabel perimeterValueLabel;
    private JLabel volumeValueLabel;
    private JLabel volumeUnitLabel;
    private JLabel spaceValueLabel;
    private JLabel spaceUnitLabel;

    public RoiSurfacePanel(ROIInfos infos)
    {
        super();

        initialize();

        refreshInfos(infos);

        validate();
    }

    void initialize()
    {
        GridBagLayout gbl_panel = new GridBagLayout();
        gbl_panel.columnWidths = new int[] {80, 46, 40, 0};
        gbl_panel.rowHeights = new int[] {0, 0, 0, 0, 0};
        gbl_panel.columnWeights = new double[] {0.0, 1.0, 0.0, Double.MIN_VALUE};
        gbl_panel.rowWeights = new double[] {0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
        setLayout(gbl_panel);

        JLabel lblPerimeter = new JLabel("Perimeter");
        GridBagConstraints gbc_lblPerimeter = new GridBagConstraints();
        gbc_lblPerimeter.anchor = GridBagConstraints.EAST;
        gbc_lblPerimeter.insets = new Insets(0, 0, 5, 5);
        gbc_lblPerimeter.gridx = 0;
        gbc_lblPerimeter.gridy = 0;
        add(lblPerimeter, gbc_lblPerimeter);

        perimeterValueLabel = new JLabel("0");
        GridBagConstraints gbc_perimeterValueLabel = new GridBagConstraints();
        gbc_perimeterValueLabel.anchor = GridBagConstraints.EAST;
        gbc_perimeterValueLabel.insets = new Insets(0, 0, 5, 5);
        gbc_perimeterValueLabel.gridx = 1;
        gbc_perimeterValueLabel.gridy = 0;
        add(perimeterValueLabel, gbc_perimeterValueLabel);

        perimeterUnitLabel = new JLabel("mm");
        GridBagConstraints gbc_perimeterUnitLabel = new GridBagConstraints();
        gbc_perimeterUnitLabel.anchor = GridBagConstraints.WEST;
        gbc_perimeterUnitLabel.insets = new Insets(0, 0, 5, 0);
        gbc_perimeterUnitLabel.gridx = 2;
        gbc_perimeterUnitLabel.gridy = 0;
        add(perimeterUnitLabel, gbc_perimeterUnitLabel);

        JLabel lblSurface = new JLabel("Surface");
        GridBagConstraints gbc_lblSurface = new GridBagConstraints();
        gbc_lblSurface.anchor = GridBagConstraints.EAST;
        gbc_lblSurface.insets = new Insets(0, 0, 5, 5);
        gbc_lblSurface.gridx = 0;
        gbc_lblSurface.gridy = 1;
        add(lblSurface, gbc_lblSurface);

        surfaceValueLabel = new JLabel("0");
        GridBagConstraints gbc_surfaceValueLabel = new GridBagConstraints();
        gbc_surfaceValueLabel.anchor = GridBagConstraints.EAST;
        gbc_surfaceValueLabel.insets = new Insets(0, 0, 5, 5);
        gbc_surfaceValueLabel.gridx = 1;
        gbc_surfaceValueLabel.gridy = 1;
        add(surfaceValueLabel, gbc_surfaceValueLabel);

        surfaceUnitLabel = new JLabel("mm");
        GridBagConstraints gbc_surfaceUnitLabel = new GridBagConstraints();
        gbc_surfaceUnitLabel.anchor = GridBagConstraints.WEST;
        gbc_surfaceUnitLabel.insets = new Insets(0, 0, 5, 0);
        gbc_surfaceUnitLabel.gridx = 2;
        gbc_surfaceUnitLabel.gridy = 1;
        add(surfaceUnitLabel, gbc_surfaceUnitLabel);

        JLabel lblVolume = new JLabel("Volume");
        GridBagConstraints gbc_lblVolume = new GridBagConstraints();
        gbc_lblVolume.anchor = GridBagConstraints.EAST;
        gbc_lblVolume.insets = new Insets(0, 0, 5, 5);
        gbc_lblVolume.gridx = 0;
        gbc_lblVolume.gridy = 2;
        add(lblVolume, gbc_lblVolume);

        volumeValueLabel = new JLabel("0");
        GridBagConstraints gbc_volumeValueLabel = new GridBagConstraints();
        gbc_volumeValueLabel.anchor = GridBagConstraints.EAST;
        gbc_volumeValueLabel.insets = new Insets(0, 0, 5, 5);
        gbc_volumeValueLabel.gridx = 1;
        gbc_volumeValueLabel.gridy = 2;
        add(volumeValueLabel, gbc_volumeValueLabel);

        volumeUnitLabel = new JLabel("mm");
        GridBagConstraints gbc_volumeUnitLabel = new GridBagConstraints();
        gbc_volumeUnitLabel.insets = new Insets(0, 0, 5, 0);
        gbc_volumeUnitLabel.anchor = GridBagConstraints.WEST;
        gbc_volumeUnitLabel.gridx = 2;
        gbc_volumeUnitLabel.gridy = 2;
        add(volumeUnitLabel, gbc_volumeUnitLabel);

        JLabel lblSpace = new JLabel("Space");
        GridBagConstraints gbc_lblSpace = new GridBagConstraints();
        gbc_lblSpace.anchor = GridBagConstraints.EAST;
        gbc_lblSpace.insets = new Insets(0, 0, 0, 5);
        gbc_lblSpace.gridx = 0;
        gbc_lblSpace.gridy = 3;
        add(lblSpace, gbc_lblSpace);

        spaceValueLabel = new JLabel("0");
        GridBagConstraints gbc_spaceValueLabel = new GridBagConstraints();
        gbc_spaceValueLabel.anchor = GridBagConstraints.EAST;
        gbc_spaceValueLabel.insets = new Insets(0, 0, 0, 5);
        gbc_spaceValueLabel.gridx = 1;
        gbc_spaceValueLabel.gridy = 3;
        add(spaceValueLabel, gbc_spaceValueLabel);

        spaceUnitLabel = new JLabel("mm");
        GridBagConstraints gbc_spaceUnitLabel = new GridBagConstraints();
        gbc_spaceUnitLabel.anchor = GridBagConstraints.WEST;
        gbc_spaceUnitLabel.gridx = 2;
        gbc_spaceUnitLabel.gridy = 3;
        add(spaceUnitLabel, gbc_spaceUnitLabel);
    }

    void refreshInfos(ROIInfos infos)
    {
        if (infos != null)
        {
            double area;
            UnitPrefix unit;

            area = infos.area;
            unit = UnitUtil.getBestUnit(area, UnitPrefix.MICRO);
            area = UnitUtil.getValueInUnit(area, UnitPrefix.MICRO, unit);

            surfaceUnitLabel.setText(unit.toString() + "m2");

            surfaceValueLabel.setText(StringUtil.toString(infos.numPixels));
            surfaceValueLabel.setToolTipText(StringUtil.toString(infos.numPixels));
            surfaceUnitLabel.setText(StringUtil.toString(MathUtil.roundSignificant(area, 5)));
            surfaceUnitLabel.setToolTipText(StringUtil.toString(area));
        }
        else
        {
            surfaceValueLabel.setText("");
            surfaceValueLabel.setToolTipText("");
            surfaceUnitLabel.setText("µm2");
            surfaceUnitLabel.setText("");
            surfaceUnitLabel.setToolTipText("");
        }
    }
}
