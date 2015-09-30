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
package icy.gui.dialog;

import icy.common.exception.UnsupportedFormatException;
import icy.file.Loader;
import icy.file.SequenceFileImporter;
import icy.gui.component.ThumbnailComponent;
import icy.gui.util.ComponentUtil;
import icy.image.IcyBufferedImage;
import icy.image.IcyBufferedImageUtil;
import icy.main.Icy;
import icy.resource.ResourceUtil;
import icy.sequence.MetaDataUtil;
import icy.util.OMEUtil;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

import loci.formats.IFormatReader;
import loci.formats.meta.IMetadata;
import loci.formats.ome.OMEXMLMetadataImpl;

public class SeriesSelectionDialog extends ActionDialog implements Runnable
{
    /**
     * 
     */
    private static final long serialVersionUID = -2133845128887305016L;

    protected static final int NUM_COL = 4;
    protected static final int THUMB_X = 160;
    protected static final int THUMB_Y = 140;

    // GUI
    protected JScrollPane scrollPane;
    protected JPanel gridPanel;
    protected ThumbnailComponent[] serieComponents;
    protected JButton selectAllBtn;
    protected JButton unselectAllBtn;

    // internal
    protected IFormatReader reader;
    protected SequenceFileImporter importer;
    protected String id;
    protected OMEXMLMetadataImpl metadata;
    protected int[] selectedSeries;
    protected final MouseAdapter serieDoubleClickAction;
    protected final Thread loadingThread;

    /**
     * @deprecated Use {@link #SeriesSelectionDialog(SequenceFileImporter, String)} instead.
     */
    @Deprecated
    public SeriesSelectionDialog(IFormatReader reader)
    {
        super(Icy.getMainInterface().getMainFrame(), "Series selection");

        this.reader = reader;
        // default is empty
        selectedSeries = new int[] {};

        initialize();

        // double cick action = direct selection
        serieDoubleClickAction = new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                if (e.getClickCount() == 2)
                {
                    final ThumbnailComponent thumb = (ThumbnailComponent) e.getSource();

                    for (int i = 0; i < serieComponents.length; i++)
                    {
                        if (serieComponents[i] == thumb)
                        {
                            selectedSeries = new int[] {i};
                            dispose();
                        }
                    }
                }
            }
        };

        final int series;

        if (reader != null)
        {
            metadata = OMEUtil.getOMEMetadata((IMetadata) reader.getMetadataStore());
            series = reader.getSeriesCount();
        }
        else
        {
            metadata = null;
            series = 0;
        }

        serieComponents = new ThumbnailComponent[series];

        // adjust number of row
        int numRow = series / NUM_COL;
        if (series > (NUM_COL * numRow))
            numRow++;

        ((GridLayout) gridPanel.getLayout()).setRows(numRow);

        for (int i = 0; i < numRow; i++)
        {
            for (int j = 0; j < NUM_COL; j++)
            {
                final int index = (i * NUM_COL) + j;

                if (index < series)
                {
                    final ThumbnailComponent thumb = new ThumbnailComponent(true);

                    // add mouse listener (double click action)
                    thumb.addMouseListener(serieDoubleClickAction);

                    // remove mouse listener (double click action)
                    if (serieComponents[index] != null)
                        serieComponents[index].removeMouseListener(serieDoubleClickAction);

                    serieComponents[index] = thumb;
                    thumb.setEnabled(true);
                    thumb.setTitle("loading...");
                    thumb.setInfos("");
                    thumb.setInfos2("");
                    gridPanel.add(thumb);
                }
                else
                    gridPanel.add(Box.createGlue());
            }
        }

        // load thumbnails...
        loadingThread = new Thread(this, "Series thumbnail loading");
        loadingThread.start();

        // action on "OK"
        setOkAction(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                int numSelected = 0;
                for (int i = 0; i < serieComponents.length; i++)
                    if (serieComponents[i].isSelected())
                        numSelected++;

                selectedSeries = new int[numSelected];

                int ind = 0;
                for (int i = 0; i < serieComponents.length; i++)
                    if (serieComponents[i].isSelected())
                        selectedSeries[ind++] = i;
            }
        });

        // action on "Select All"
        selectAllBtn.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                for (ThumbnailComponent thumb : serieComponents)
                    thumb.setSelected(true);
            }
        });

        // action on "Unselect All"
        unselectAllBtn.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                for (ThumbnailComponent thumb : serieComponents)
                    thumb.setSelected(false);
            }
        });

        setPreferredSize(new Dimension(740, 520));
        pack();
        ComponentUtil.center(SeriesSelectionDialog.this);
        setVisible(true);
    }

    /**
     * Create a new dialog to select the series to open from an image.
     * 
     * @throws IOException
     * @throws UnsupportedFormatException
     */
    public SeriesSelectionDialog(SequenceFileImporter importer, String id, OMEXMLMetadataImpl metadata)
            throws UnsupportedFormatException, IOException
    {
        super(Icy.getMainInterface().getMainFrame(), "Series selection");

        this.importer = importer;
        this.id = id;
        this.metadata = metadata;
        // default is empty
        selectedSeries = new int[] {};

        initialize();

        final int series = MetaDataUtil.getNumSerie(metadata);

        // double cick action = direct selection
        serieDoubleClickAction = new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                if (e.getClickCount() == 2)
                {
                    final ThumbnailComponent thumb = (ThumbnailComponent) e.getSource();

                    for (int i = 0; i < serieComponents.length; i++)
                    {
                        if (serieComponents[i] == thumb)
                        {
                            selectedSeries = new int[] {i};
                            dispose();
                        }
                    }
                }
            }
        };

        serieComponents = new ThumbnailComponent[series];

        // adjust number of row
        int numRow = series / NUM_COL;
        if (series > (NUM_COL * numRow))
            numRow++;

        ((GridLayout) gridPanel.getLayout()).setRows(numRow);

        for (int i = 0; i < numRow; i++)
        {
            for (int j = 0; j < NUM_COL; j++)
            {
                final int index = (i * NUM_COL) + j;

                if (index < series)
                {
                    final ThumbnailComponent thumb = new ThumbnailComponent(true);

                    // add mouse listener (double click action)
                    thumb.addMouseListener(serieDoubleClickAction);

                    // remove mouse listener (double click action)
                    if (serieComponents[index] != null)
                        serieComponents[index].removeMouseListener(serieDoubleClickAction);

                    serieComponents[index] = thumb;
                    thumb.setEnabled(true);
                    thumb.setTitle("loading...");
                    thumb.setInfos("");
                    thumb.setInfos2("");
                    gridPanel.add(thumb);
                }
                else
                    gridPanel.add(Box.createGlue());
            }
        }

        // load thumbnails...
        loadingThread = new Thread(this, "Series thumbnail loading");
        loadingThread.start();

        // action on "OK"
        setOkAction(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                int numSelected = 0;
                for (int i = 0; i < serieComponents.length; i++)
                    if (serieComponents[i].isSelected())
                        numSelected++;

                selectedSeries = new int[numSelected];

                int ind = 0;
                for (int i = 0; i < serieComponents.length; i++)
                    if (serieComponents[i].isSelected())
                        selectedSeries[ind++] = i;
            }
        });

        // action on "Select All"
        selectAllBtn.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                for (ThumbnailComponent thumb : serieComponents)
                    thumb.setSelected(true);
            }
        });

        // action on "Unselect All"
        unselectAllBtn.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                for (ThumbnailComponent thumb : serieComponents)
                    thumb.setSelected(false);
            }
        });

        setPreferredSize(new Dimension(740, 520));
        pack();
        ComponentUtil.center(SeriesSelectionDialog.this);
        setVisible(true);
    }

    /**
     * Create a new dialog to select the series to open from an image.
     * 
     * @throws IOException
     * @throws UnsupportedFormatException
     */
    public SeriesSelectionDialog(SequenceFileImporter importer, String id) throws UnsupportedFormatException,
            IOException
    {
        this(importer, id, Loader.getMetaData(importer, id));
    }

    /**
     * @return the selectedSeries
     */
    public int[] getSelectedSeries()
    {
        return selectedSeries;
    }

    void initialize()
    {
        JPanel panel = new JPanel();
        getContentPane().add(panel, BorderLayout.NORTH);
        panel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

        JLabel lblSelect = new JLabel("Click on a serie to select / unselect it or double click to directly open it.");
        ComponentUtil.setFontBold(lblSelect);
        ComponentUtil.setFontSize(lblSelect, 12);
        panel.add(lblSelect);

        scrollPane = new JScrollPane(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        gridPanel = new JPanel();
        scrollPane.setViewportView(gridPanel);
        gridPanel.setLayout(new GridLayout(2, NUM_COL, 0, 0));

        buttonPanel.removeAll();

        selectAllBtn = new JButton("Select all");
        unselectAllBtn = new JButton("Unselect all");

        buttonPanel.add(Box.createHorizontalStrut(4));
        buttonPanel.add(selectAllBtn);
        buttonPanel.add(Box.createHorizontalStrut(8));
        buttonPanel.add(unselectAllBtn);
        buttonPanel.add(Box.createHorizontalStrut(8));
        buttonPanel.add(Box.createHorizontalGlue());
        buttonPanel.add(Box.createHorizontalStrut(8));
        buttonPanel.add(okBtn);
        buttonPanel.add(Box.createHorizontalStrut(8));
        buttonPanel.add(cancelBtn);
        buttonPanel.add(Box.createHorizontalStrut(4));
    }

    @Override
    protected void onClosed()
    {
        super.onClosed();

        // kill loading task in 2 seconds
        new Timer().schedule(new TimerTask()
        {
            @SuppressWarnings("deprecation")
            @Override
            public void run()
            {
                if ((loadingThread != null) && loadingThread.isAlive())
                {
                    try
                    {
                        loadingThread.stop();
                    }
                    catch (Throwable t)
                    {
                        // ignore
                    }
                }
            }
        }, 2000);
    }

    @Override
    public void run()
    {
        try
        {
            // start by filling metadata only...
            for (int i = 0; i < serieComponents.length; i++)
            {
                // interrupt
                if (isClosed())
                    return;

                try
                {
                    final int sizeC = MetaDataUtil.getSizeC(metadata, i);

                    serieComponents[i].setTitle(metadata.getImageName(i));
                    serieComponents[i].setInfos(MetaDataUtil.getSizeX(metadata, i) + " x "
                            + MetaDataUtil.getSizeY(metadata, i) + " - " + MetaDataUtil.getSizeZ(metadata, i) + "Z x "
                            + MetaDataUtil.getSizeT(metadata, i) + "T");
                    serieComponents[i].setInfos2(sizeC + ((sizeC > 1) ? " channels (" : " channel (")
                            + MetaDataUtil.getDataType(metadata, i) + ")");
                }
                catch (Exception e)
                {
                    serieComponents[i].setTitle("Cannot read file");
                    serieComponents[i].setInfos("");
                    serieComponents[i].setInfos2("");
                }

                try
                {
                    // why does this sometime fails ???
                    serieComponents[i].setImage(ResourceUtil.ICON_PICTURE);
                }
                catch (Exception e)
                {
                    // ignore
                }
            }

            // then try to load thumbnail
            for (int i = 0; i < serieComponents.length; i++)
            {
                // interrupt
                if (isClosed())
                    return;

                try
                {
                    if (importer.open(id, 0))
                    {
                        try
                        {
                            final IcyBufferedImage img = importer.getThumbnail(i);
                            serieComponents[i].setImage(IcyBufferedImageUtil.getARGBImage(img));
                        }
                        finally
                        {
                            importer.close();
                        }
                    }
                    else
                        serieComponents[i].setImage(ResourceUtil.ICON_DELETE);
                }
                catch (OutOfMemoryError e)
                {
                    // error image, we just totally ignore error here...
                    serieComponents[i].setImage(ResourceUtil.ICON_DELETE);
                }
                catch (Exception e)
                {
                    // error image, we just totally ignore error here...
                    serieComponents[i].setImage(ResourceUtil.ICON_DELETE);
                }
            }
        }
        catch (ThreadDeath t)
        {
            // just stop process...
        }
    }
}
