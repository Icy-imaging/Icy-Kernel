/*
 * Copyright 2010-2013 Institut Pasteur.
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
package icy.gui.lut;

import icy.canvas.IcyCanvas3D;
import icy.file.xml.XMLPersistentHelper;
import icy.gui.component.button.IcyButton;
import icy.gui.component.button.IcyToggleButton;
import icy.gui.component.renderer.ColormapComboBoxRenderer;
import icy.gui.dialog.LoadDialog;
import icy.gui.dialog.SaveDialog;
import icy.gui.util.ComponentUtil;
import icy.gui.util.GuiUtil;
import icy.gui.viewer.Viewer;
import icy.image.colormap.FireColorMap;
import icy.image.colormap.GlowColorMap;
import icy.image.colormap.HSVColorMap;
import icy.image.colormap.IceColorMap;
import icy.image.colormap.IcyColorMap;
import icy.image.colormap.IcyColorMap.IcyColorMapType;
import icy.image.colormap.IcyColorMapEvent;
import icy.image.colormap.IcyColorMapListener;
import icy.image.colormap.JETColorMap;
import icy.image.colormap.LinearColorMap;
import icy.image.lut.LUT;
import icy.image.lut.LUT.LUTChannel;
import icy.resource.ResourceUtil;
import icy.resource.icon.IcyIcon;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;

/**
 * @author stephane
 */
public class ColormapPanel extends JPanel implements IcyColorMapListener
{
    /**
     * 
     */
    private static final long serialVersionUID = -4042084504553770641L;

    private static final String DEFAULT_COLORMAP_DIR = "colormap";
    private static final String DEFAULT_COLORMAP_NAME = "colormap.xml";

    /**
     * gui
     */
    private final ColormapViewer colormapViewer;
    final IcyToggleButton rgbBtn;
    final IcyToggleButton grayBtn;
    final IcyToggleButton alphaBtn;
    final ButtonGroup colormapTypeBtnGrp;
    final JComboBox colormapComboBox;

    /**
     * associated Viewer & LUTBand
     */
    public final Viewer viewer;
    public final LUTChannel lutChannel;

    /**
     * cached
     */
    final IcyColorMap colormap;

    public ColormapPanel(final Viewer viewer, final LUTChannel lutChannel)
    {
        super();

        this.viewer = viewer;
        this.lutChannel = lutChannel;

        colormap = lutChannel.getColorMap();
        colormap.setName("Custom");

        colormapViewer = new ColormapViewer(lutChannel);

        // colormap type
        rgbBtn = new IcyToggleButton(new IcyIcon(ResourceUtil.ICON_RGB_COLOR, false));
        rgbBtn.setToolTipText("Set colormap type to Color");
        rgbBtn.setFocusPainted(false);
        ComponentUtil.setFixedWidth(rgbBtn, 26);
        grayBtn = new IcyToggleButton(new IcyIcon(ResourceUtil.ICON_GRAY_COLOR, false));
        grayBtn.setToolTipText("Set colormap type to Gray");
        grayBtn.setFocusPainted(false);
        ComponentUtil.setFixedWidth(grayBtn, 26);
        alphaBtn = new IcyToggleButton(new IcyIcon(ResourceUtil.ICON_ALPHA_COLOR, false));
        alphaBtn.setToolTipText("Set colormap type to Alpha (transparency)");
        alphaBtn.setFocusPainted(false);
        ComponentUtil.setFixedWidth(alphaBtn, 26);

        colormapTypeBtnGrp = new ButtonGroup();

        colormapTypeBtnGrp.add(rgbBtn);
        colormapTypeBtnGrp.add(grayBtn);
        colormapTypeBtnGrp.add(alphaBtn);

        // select item according to current colormap type
        updateColormapType(colormap.getType());

        rgbBtn.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                colormap.setType(IcyColorMapType.RGB);
            }
        });
        grayBtn.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                colormap.setType(IcyColorMapType.GRAY);
            }
        });
        alphaBtn.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                colormap.setType(IcyColorMapType.ALPHA);
            }
        });

        // alpha checkbox
        // final JCheckBox alphaEnabled = new JCheckBox("alpha", null, true);
        // alphaEnabled.setToolTipText("Enable alpha control");
        // alphaEnabled.addItemListener(new ItemListener()
        // {
        // @Override
        // public void itemStateChanged(ItemEvent e)
        // {
        // // set alpha
        // colormapViewer.setAlphaEnabled(e.getStateChange() == ItemEvent.SELECTED);
        // }
        // });

        final IcyColorMap defaultColormap;
        // retrieve the default sequence LUT
        final LUT defaultLUT = viewer.getSequence().getDefaultLUT();

        // compatible colormap (should always be the case)
        if (defaultLUT.isCompatible(lutChannel.getLut()))
        {
            defaultColormap = defaultLUT.getLutChannel(lutChannel.getChannel()).getColorMap();
            defaultColormap.setName("Default");
        }
        else
        {
            // asynchronous sequence change, get a default colormap from current one
            defaultColormap = new IcyColorMap("Default");
            // copy from current colormap
            defaultColormap.copyFrom(colormap);
        }

        // build colormap list
        final List<IcyColorMap> colormaps = new ArrayList<IcyColorMap>();

        colormaps.add(defaultColormap);

        if (!defaultColormap.equals(LinearColorMap.gray_))
            colormaps.add(LinearColorMap.gray_);
        if (!defaultColormap.equals(LinearColorMap.gray_inv_))
            colormaps.add(LinearColorMap.gray_inv_);
        if (!defaultColormap.equals(LinearColorMap.red_))
            colormaps.add(LinearColorMap.red_);
        if (!defaultColormap.equals(LinearColorMap.green_))
            colormaps.add(LinearColorMap.green_);
        if (!defaultColormap.equals(LinearColorMap.blue_))
            colormaps.add(LinearColorMap.blue_);
        if (!defaultColormap.equals(LinearColorMap.magenta_))
            colormaps.add(LinearColorMap.magenta_);
        if (!defaultColormap.equals(LinearColorMap.yellow_))
            colormaps.add(LinearColorMap.yellow_);
        if (!defaultColormap.equals(LinearColorMap.cyan_))
            colormaps.add(LinearColorMap.cyan_);
        if (!defaultColormap.equals(LinearColorMap.alpha_))
            colormaps.add(LinearColorMap.alpha_);

        colormaps.add(new IceColorMap());
        colormaps.add(new FireColorMap());
        colormaps.add(new HSVColorMap());
        colormaps.add(new JETColorMap());
        colormaps.add(new GlowColorMap(true));

        // this is the custom colormap
        colormaps.add(colormap);

        // build colormap selector
        colormapComboBox = new JComboBox(colormaps.toArray());
        colormapComboBox.setRenderer(new ColormapComboBoxRenderer(colormapComboBox, 64, 16));
        // limit size
        ComponentUtil.setFixedWidth(colormapComboBox, 64 + 30);
        colormapComboBox.setToolTipText("Select colormap model");
        // don't want focusable here
        colormapComboBox.setFocusable(false);
        // set to current colormap
        colormapComboBox.setSelectedItem(colormap);

        colormapComboBox.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                setColorMap((IcyColorMap) colormapComboBox.getSelectedItem());
            }
        });

        // load button
        final IcyButton loadButton = new IcyButton(new IcyIcon(ResourceUtil.ICON_OPEN));
        loadButton.setFlat(true);
        loadButton.setToolTipText("Load colormap from file");

        // action to load colormap
        loadButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                final String filename = LoadDialog.chooseFile("Load colormap...", DEFAULT_COLORMAP_DIR,
                        DEFAULT_COLORMAP_NAME);

                if (filename != null)
                {
                    final IcyColorMap map = new IcyColorMap();
                    XMLPersistentHelper.loadFromXML(map, filename);
                    setColorMap(map);
                }
            }
        });

        // save button
        final IcyButton saveButton = new IcyButton(new IcyIcon(ResourceUtil.ICON_SAVE));
        saveButton.setFlat(true);
        saveButton.setToolTipText("Save colormap to file");

        // action to save colormap
        saveButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                final String filename = SaveDialog.chooseFile("Save colormap...", DEFAULT_COLORMAP_DIR,
                        DEFAULT_COLORMAP_NAME);

                if (filename != null)
                    XMLPersistentHelper.saveToXML(colormap, filename);
            }
        });

        // set up GUI
        final JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BorderLayout());
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));

        bottomPanel.add(GuiUtil.createLineBoxPanel(rgbBtn, grayBtn, alphaBtn), BorderLayout.WEST);
        bottomPanel.add(Box.createGlue(), BorderLayout.CENTER);
        bottomPanel.add(
                GuiUtil.createLineBoxPanel(colormapComboBox, new JSeparator(SwingConstants.VERTICAL), loadButton,
                        Box.createHorizontalStrut(2), saveButton), BorderLayout.EAST);
        // bottomPanel.add(Box.createHorizontalStrut(6));
        // bottomPanel.add(alphaEnabled);

        bottomPanel.validate();

        setLayout(new BorderLayout());

        add(colormapViewer, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        validate();
    }

    @Override
    public void addNotify()
    {
        super.addNotify();

        // listen colormap changes
        colormap.addListener(this);
    }

    @Override
    public void removeNotify()
    {
        colormap.removeListener(this);

        super.removeNotify();
    }

    /**
     * @return the colormapViewer
     */
    public ColormapViewer getColormapViewer()
    {
        return colormapViewer;
    }

    private void updateColormapType(IcyColorMapType type)
    {
        switch (type)
        {
            case RGB:
                colormapTypeBtnGrp.setSelected(rgbBtn.getModel(), true);
                break;
            case GRAY:
                colormapTypeBtnGrp.setSelected(grayBtn.getModel(), true);
                break;
            case ALPHA:
                colormapTypeBtnGrp.setSelected(alphaBtn.getModel(), true);
                break;
        }
    }

    /**
     * Set the colormap.
     */
    public void setColorMap(IcyColorMap src)
    {
        if (colormap == src)
            return;

        colormap.setName(src.getName());

        if (viewer.getCanvas() instanceof IcyCanvas3D)
        {
            // copy alpha component only if we have specific alpha info
            // copyAlpha = !src.alpha.isAllSame();
            colormap.beginUpdate();
            try
            {
                colormap.copyFrom(src, false);
                colormap.setDefaultAlphaFor3D();
            }
            finally
            {
                colormap.endUpdate();
            }
        }
        else
            colormap.copyFrom(src, true);

        colormapComboBox.setSelectedItem(colormap);
    }

    /**
     * @deprecated Use {@link #setColorMap(IcyColorMap)} instead.
     */
    @Deprecated
    public void copyColorMap(IcyColorMap src)
    {
        setColorMap(src);
    }

    @Override
    public void colorMapChanged(IcyColorMapEvent e)
    {
        switch (e.getType())
        {
            case TYPE_CHANGED:
                // colormap type has changed ? --> update combo state
                updateColormapType(e.getColormap().getType());
                break;

            case MAP_CHANGED:
                // update the combo box colormap representation
                colormapComboBox.setSelectedItem(colormap);
                colormapComboBox.repaint();
                break;
        }
    }
}
