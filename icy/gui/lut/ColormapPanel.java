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
package icy.gui.lut;

import icy.file.xml.XMLPersistentHelper;
import icy.gui.component.ComponentUtil;
import icy.gui.component.button.IcyButton;
import icy.gui.dialog.LoadDialog;
import icy.gui.dialog.SaveDialog;
import icy.gui.lut.abstract_.IcyColormapPanel;
import icy.gui.viewer.Viewer;
import icy.image.colormap.FireColorMap;
import icy.image.colormap.HSVColorMap;
import icy.image.colormap.IceColorMap;
import icy.image.colormap.IcyColorMap;
import icy.image.colormap.IcyColorMap.IcyColorMapType;
import icy.image.colormap.IcyColorMapEvent;
import icy.image.colormap.IcyColorMapListener;
import icy.image.colormap.JETColorMap;
import icy.image.colormap.LinearColorMap;
import icy.image.lut.LUTBand;
import icy.resource.ResourceUtil;
import icy.util.StringUtil;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

/**
 * @author stephane
 */
public class ColormapPanel extends IcyColormapPanel implements IcyColorMapListener
{
    /**
     * 
     */
    private static final long serialVersionUID = -4042084504553770641L;

    private static final String DEFAULT_COLORMAP_DIR = "colormap";
    private static final String DEFAULT_COLORMAP_NAME = "colormap.map";

    private static final String STRING_RGB = "RGB";
    private static final String STRING_GRAY = "Gray";
    private static final String STRING_ALPHA = "Alpha";

    /**
     * gui
     */
    private final ColormapViewer colormapViewer;
    final JComboBox colorMapTypeCombo;

    // private final JRadioButton rgbButton;
    // private final JRadioButton redButton;
    // private final JRadioButton greenButton;
    // private final JRadioButton blueButton;
    // private final JRadioButton grayButton;

    /**
     * cached
     */
    final IcyColorMap colormap;

    public ColormapPanel(final Viewer viewer, final LUTBand lutBand)
    {
        super(viewer, lutBand);

        colormap = lutBand.getColorMap();

        colormapViewer = new ColormapViewer(lutBand);

        // set up GUI
        final JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.PAGE_AXIS));

        final JPanel colormapSettingPanel = new JPanel();
        colormapSettingPanel.setLayout(new BoxLayout(colormapSettingPanel, BoxLayout.LINE_AXIS));

        // colormap type
        final String[] values = {STRING_RGB, STRING_GRAY, STRING_ALPHA};
        colorMapTypeCombo = new JComboBox(values);
        colorMapTypeCombo.setToolTipText("Colormap type");
        ComponentUtil.setFixedWidth(colorMapTypeCombo, 70);

        // action to change colormap type
        colorMapTypeCombo.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                colormap.setType(itemToType(colorMapTypeCombo.getSelectedItem()));
            }
        });

        // select item according to current colormap type
        colorMapTypeCombo.setSelectedItem(typeToItem(colormap.getType()));

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

        // restore default button
        final IcyButton restoreDefaultButton = new IcyButton(null, "undo", 20);
        restoreDefaultButton.setFlat(true);
        restoreDefaultButton.setToolTipText("Restore default colormap");

        // action to restore colormap
        restoreDefaultButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                // copy colormap from default sequence colormap
                lutBand.copyColorMap(viewer.getSequence().createCompatibleLUT().getLutBand(lutBand.getComponent())
                        .getColorMap());
            }
        });

        // load button
        final IcyButton loadModelButton = new IcyButton(ResourceUtil.ICON_DOC, 20);
        loadModelButton.setFlat(true);
        loadModelButton.setToolTipText("Load colormap model");

        // action to load colormap
        loadModelButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                showColormapModelMenu(loadModelButton);
            }
        });

        // load button
        final IcyButton loadButton = new IcyButton(ResourceUtil.ICON_OPEN, 20);
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
                    XMLPersistentHelper.loadFromXML(colormap, filename);
            }
        });

        // save button
        final IcyButton saveButton = new IcyButton(ResourceUtil.ICON_SAVE);
        saveButton.setFlat(true);
        saveButton.setIconSize(20);
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

        colormapSettingPanel.add(colorMapTypeCombo);
        colormapSettingPanel.add(Box.createHorizontalStrut(6));
        colormapSettingPanel.add(restoreDefaultButton);
        colormapSettingPanel.add(Box.createHorizontalStrut(2));
        colormapSettingPanel.add(loadModelButton);
        colormapSettingPanel.add(Box.createHorizontalStrut(2));
        colormapSettingPanel.add(loadButton);
        colormapSettingPanel.add(Box.createHorizontalStrut(2));
        colormapSettingPanel.add(saveButton);
        // colormapSettingPanel.add(Box.createHorizontalStrut(6));
        // colormapSettingPanel.add(alphaEnabled);

        colormapSettingPanel.add(Box.createHorizontalGlue());
        colormapSettingPanel.validate();

        bottomPanel.add(Box.createVerticalStrut(4));
        bottomPanel.add(colormapSettingPanel);
        bottomPanel.validate();

        setLayout(new BorderLayout());

        add(colormapViewer, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        validate();
    }

    /**
     * @return the colormapViewer
     */
    public ColormapViewer getColormapViewer()
    {
        return colormapViewer;
    }

    IcyColorMapType itemToType(Object item)
    {
        if (item instanceof String)
        {
            final String value = (String) item;

            if (StringUtil.equals(value, STRING_RGB))
                return IcyColorMapType.RGB;
            if (StringUtil.equals(value, STRING_GRAY))
                return IcyColorMapType.GRAY;
            if (StringUtil.equals(value, STRING_ALPHA))
                return IcyColorMapType.ALPHA;
        }

        return null;
    }

    private Object typeToItem(IcyColorMapType type)
    {
        switch (type)
        {
            case RGB:
                return STRING_RGB;
            case GRAY:
                return STRING_GRAY;
            case ALPHA:
                return STRING_ALPHA;
        }

        return null;
    }

    /**
     * show popup menu
     */
    void showColormapModelMenu(Component comp)
    {
        final JPopupMenu menu = new JPopupMenu();

        final JMenu menuLinear = new JMenu("Linear");

        final JMenuItem linearBlack = new JMenuItem("Black (band off)");
        final JMenuItem linearWhite = new JMenuItem("White");
        final JMenuItem linearBlue = new JMenuItem("Blue");
        final JMenuItem linearCyan = new JMenuItem("Cyan");
        final JMenuItem linearYellow = new JMenuItem("Yellow");
        final JMenuItem linearPink = new JMenuItem("Pink");
        final JMenuItem linearGreen = new JMenuItem("Green");
        final JMenuItem linearMagenta = new JMenuItem("Magenta");
        final JMenuItem linearRed = new JMenuItem("Red");
        final JMenuItem linearOrange = new JMenuItem("Orange");

        final JMenuItem Ice = new JMenuItem("Ice");
        final JMenuItem Fire = new JMenuItem("Fire");
        final JMenuItem HSV = new JMenuItem("HSV");
        final JMenuItem JET = new JMenuItem("JET");

        menuLinear.add(linearBlack);
        menuLinear.add(linearWhite);
        menuLinear.add(linearBlue);
        menuLinear.add(linearCyan);
        menuLinear.add(linearYellow);
        menuLinear.add(linearPink);
        menuLinear.add(linearGreen);
        menuLinear.add(linearMagenta);
        menuLinear.add(linearRed);
        menuLinear.add(linearOrange);

        menu.add(menuLinear);
        menu.add(Ice);
        menu.add(Fire);
        menu.add(HSV);
        menu.add(JET);

        menu.pack();
        menu.validate();

        final ActionListener actionListener = new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                final LUTBand lutBand = getLutBand();

                if (e.getSource() == linearBlack)
                    lutBand.copyColorMap(LinearColorMap.black_);
                if (e.getSource() == linearWhite)
                    lutBand.copyColorMap(LinearColorMap.white_);
                if (e.getSource() == linearBlue)
                    lutBand.copyColorMap(LinearColorMap.blue_);
                if (e.getSource() == linearCyan)
                    lutBand.copyColorMap(LinearColorMap.cyan_);
                if (e.getSource() == linearYellow)
                    lutBand.copyColorMap(LinearColorMap.yellow_);
                if (e.getSource() == linearPink)
                    lutBand.copyColorMap(LinearColorMap.pink_);
                if (e.getSource() == linearGreen)
                    lutBand.copyColorMap(LinearColorMap.green_);
                if (e.getSource() == linearMagenta)
                    lutBand.copyColorMap(LinearColorMap.magenta_);
                if (e.getSource() == linearRed)
                    lutBand.copyColorMap(LinearColorMap.red_);
                if (e.getSource() == linearOrange)
                    lutBand.copyColorMap(LinearColorMap.orange_);
                if (e.getSource() == Ice)
                    lutBand.copyColorMap(new IceColorMap());
                if (e.getSource() == Fire)
                    lutBand.copyColorMap(new FireColorMap());
                if (e.getSource() == HSV)
                    lutBand.copyColorMap(new HSVColorMap());
                if (e.getSource() == JET)
                    lutBand.copyColorMap(new JETColorMap());

            }
        };

        linearBlack.addActionListener(actionListener);
        linearWhite.addActionListener(actionListener);
        linearBlue.addActionListener(actionListener);
        linearCyan.addActionListener(actionListener);
        linearYellow.addActionListener(actionListener);
        linearPink.addActionListener(actionListener);
        linearGreen.addActionListener(actionListener);
        linearMagenta.addActionListener(actionListener);
        linearRed.addActionListener(actionListener);
        linearOrange.addActionListener(actionListener);
        Ice.addActionListener(actionListener);
        Fire.addActionListener(actionListener);
        JET.addActionListener(actionListener);
        HSV.addActionListener(actionListener);

        // display menu
        menu.show(comp, 0, comp.getHeight());
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

    @Override
    public void colorMapChanged(IcyColorMapEvent e)
    {
        switch (e.getType())
        {
            // colormap type has changed ?
            case TYPE_CHANGED:
                // update combo state
                colorMapTypeCombo.setSelectedItem(typeToItem(e.getColormap().getType()));
                // getRadioButton(e.getColormap().getType()).setSelected(true);
                break;

            case MAP_CHANGED:
                // nothing to do here

                break;
        }
    }

}
