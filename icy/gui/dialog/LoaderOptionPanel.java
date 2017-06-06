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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Callable;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import icy.common.exception.UnsupportedFormatException;
import icy.file.Loader;
import icy.file.SequenceFileImporter;
import icy.gui.component.RangeComponent;
import icy.gui.component.Region2DComponent;
import icy.gui.component.SpecialValueSpinner;
import icy.gui.component.ThumbnailComponent;
import icy.gui.component.model.SpecialValueSpinnerModel;
import icy.resource.ResourceUtil;
import icy.sequence.MetaDataUtil;
import icy.system.thread.ThreadUtil;
import icy.util.StringUtil;
import loci.formats.ome.OMEXMLMetadataImpl;
import ome.xml.meta.OMEXMLMetadata;

public class LoaderOptionPanel extends JPanel
{
    private class PreviewUpdater extends Thread
    {
        final String fileId;
        int z;
        int t;
        boolean imageRefreshOnly;

        PreviewUpdater(String fileId, int z, int t, boolean imageRefreshOnly)
        {
            super("Image preview");

            this.fileId = fileId;
            this.z = z;
            this.t = t;
            this.imageRefreshOnly = imageRefreshOnly;
        }

        public PreviewUpdater(String fileId, int z, int t)
        {
            this(fileId, z, t, true);
        }

        public PreviewUpdater(String fileId)
        {
            this(fileId, -1, -1, false);
        }

        @Override
        public void run()
        {
            // interrupt process
            if (isInterrupted())
                return;

            // get current selected series
            final int s = getSelectedSeries();

            boolean thumbnailDone = false;

            // only need to update image
            if (imageRefreshOnly)
            {
                preview.setImage(ResourceUtil.ICON_WAIT);

                try
                {
                    // use last - 1 resolution
                    final int res = Math.max(0, resolutionSlider.getMaximum() - 1);

                    // not defined --> use middle
                    if (z == -1)
                        z = MetaDataUtil.getSizeZ(metadata, s) / 2;
                    // not defined --> use middle
                    if (t == -1)
                        t = MetaDataUtil.getSizeT(metadata, s) / 2;

                    final List<SequenceFileImporter> importers = Loader.getSequenceFileImporters(fileId);

                    for (SequenceFileImporter importer : importers)
                    {
                        // interrupt process
                        if (isInterrupted())
                            break;

                        try
                        {
                            if (importer.open(fileId, 0))
                            {
                                try
                                {
                                    // interrupt process
                                    if (isInterrupted())
                                        break;

                                    // default position --> use thumbnail
                                    if ((z == 0) && (t == 0) && (pCh == -1))
                                        preview.setImage(importer.getThumbnail(s));
                                    // all channel
                                    else if (pCh == -1)
                                        preview.setImage(importer.getImage(s, res, z, t));
                                    // specific channel
                                    else
                                        preview.setImage(importer.getImage(s, res, z, t, pCh));

                                    // done
                                    thumbnailDone = true;
                                }
                                finally
                                {
                                    importer.close();
                                }

                                // done
                                break;
                            }
                        }
                        catch (UnsupportedFormatException e)
                        {
                            // try next importer...
                        }
                        catch (RuntimeException e)
                        {
                            // try next importer...
                        }
                        catch (IOException e)
                        {
                            // try next importer...
                        }
                    }
                }
                catch (Throwable t)
                {
                    // fatal error --> cancel image update
                }

                // cannot get thumbnail
                if (!thumbnailDone)
                {
                    // no other process --> failed
                    if ((previewThread == Thread.currentThread()) || (previewThread == null)
                            || !previewThread.isAlive())
                        preview.setImage(ResourceUtil.ICON_DELETE);

                }

                // image updated
                return;
            }

            boolean metaDataDone = false;
            metadata = null;

            if (StringUtil.isEmpty(fileId))
            {
                preview.setImage(null);
                preview.setInfos("");

                metadata = new OMEXMLMetadataImpl();

                metaDataDone = true;
                thumbnailDone = true;

                return;
            }

            preview.setImage(ResourceUtil.ICON_WAIT);
            preview.setInfos("loading...");

            try
            {
                // use Callable as we can get interrupted here...
                ThreadUtil.invokeNow(new Callable<Boolean>()
                {
                    @Override
                    public Boolean call() throws Exception
                    {
                        // disable panel while we are loading metadata
                        disablePanel();

                        return Boolean.TRUE;
                    }
                });

                final List<SequenceFileImporter> importers = Loader.getSequenceFileImporters(fileId);

                for (SequenceFileImporter importer : importers)
                {
                    // interrupt process
                    if (isInterrupted())
                        break;

                    try
                    {
                        if (importer.open(fileId, 0))
                        {
                            try
                            {
                                // interrupt process
                                if (isInterrupted())
                                    break;

                                if (!metaDataDone)
                                {
                                    metadata = importer.getOMEXMLMetaData();

                                    // update it as soon as possible (use Callable as we can get interrupted here...)
                                    ThreadUtil.invokeNow(new Callable<Boolean>()
                                    {
                                        @Override
                                        public Boolean call() throws Exception
                                        {
                                            // update panel
                                            updatePanel();

                                            return Boolean.TRUE;
                                        }
                                    });

                                    // now the different "metadata" fields are up to date
                                    metadataFieldsOk = true;

                                    // initial preview --> update default range and channel positions
                                    pZMin = getZMin();
                                    pZMax = getZMax();
                                    pTMin = getTMin();
                                    pTMax = getTMax();
                                    pCh = getChannel();

                                    final int sizeC = MetaDataUtil.getSizeC(metadata, s);

                                    // load metadata first
                                    preview.setInfos(MetaDataUtil.getSizeX(metadata, s) + " x "
                                            + MetaDataUtil.getSizeY(metadata, s) + " - "
                                            + MetaDataUtil.getSizeZ(metadata, s) + "Z x "
                                            + MetaDataUtil.getSizeT(metadata, s) + "T - " + sizeC + " ch ("
                                            + MetaDataUtil.getDataType(metadata, s) + ")");

                                    metaDataDone = true;
                                }

                                // interrupt process
                                if (isInterrupted())
                                    break;

                                if (!thumbnailDone)
                                {
                                    // initial preview --> use thumbnail
                                    preview.setImage(importer.getThumbnail(s));

                                    // done
                                    thumbnailDone = true;
                                }
                            }
                            finally
                            {
                                importer.close();
                            }
                        }
                    }
                    catch (UnsupportedFormatException e)
                    {
                        // try next importer...
                    }
                    catch (RuntimeException e)
                    {
                        // try next importer...
                    }
                    catch (IOException e)
                    {
                        // try next importer...
                    }

                    // we correctly loaded both metadata and thumbnail --> exit
                    if (metaDataDone && thumbnailDone)
                        break;
                }
            }
            catch (InterruptedException t)
            {
                // no need to do more here...
                return;
            }
            catch (Throwable t)
            {
                // ignore
            }

            try
            {
                // cannot read metadata
                if (!metaDataDone)
                {
                    preview.setInfos("Cannot read file");

                    // use Callable as we can get interrupted here...
                    ThreadUtil.invokeNow(new Callable<Boolean>()
                    {
                        @Override
                        public Boolean call() throws Exception
                        {
                            // update panel
                            updatePanel();

                            return Boolean.TRUE;
                        }
                    });
                }

                // cannot get thumbnail
                if (!thumbnailDone)
                {
                    // no other process --> failed
                    if ((previewThread == Thread.currentThread()) || (previewThread == null)
                            || !previewThread.isAlive())
                        preview.setImage(ResourceUtil.ICON_DELETE);

                }
            }
            catch (Throwable t)
            {
                // probably interrupted...
            }
        }
    }

    /**
     * 
     */
    private static final long serialVersionUID = 4180367632912879286L;

    /**
     * GUI
     */
    protected ThumbnailComponent preview;
    protected JPanel optionsPanel;
    protected JLabel loadInSeparatedLabel;
    protected JLabel autoOrderLabel;
    protected JCheckBox separateSeqCheck;
    protected JCheckBox autoOrderCheck;
    protected JSlider resolutionSlider;
    protected JLabel resolutionLevelLabel;
    protected JLabel zRangeLabel;
    protected JLabel tRangeLabel;
    protected JLabel channelLabel;
    protected RangeComponent zRangeComp;
    protected RangeComponent tRangeComp;
    protected SpecialValueSpinner channelSpinner;
    protected JLabel resolutionFixLabel;
    protected JLabel xyRegionLabel;
    protected JLabel seriesLabel;
    protected SpecialValueSpinner seriesSpinner;
    protected Region2DComponent xyRegionComp;
    protected JCheckBox xyRegionLoadingCheck;

    // internals
    protected boolean autoOrderEnable;
    protected boolean metadataFieldsOk;
    protected PreviewUpdater previewThread;
    protected OMEXMLMetadata metadata;
    protected int series;
    protected int pZMin;
    protected int pZMax;
    protected int pTMin;
    protected int pTMax;
    protected int pCh;
    protected boolean multiFile;
    protected boolean updatingPanel;

    /**
     * Create the panel.
     */
    public LoaderOptionPanel(boolean separate, boolean autoOrder)
    {
        super();

        autoOrderEnable = true;
        previewThread = null;
        metadataFieldsOk = false;
        metadata = null;
        series = -1;
        pZMin = -1;
        pZMax = -1;
        pTMin = -1;
        pTMax = -1;
        pCh = -1;
        updatingPanel = false;

        initialize(separate, autoOrder);

        // default
        multiFile = true;
        setMultiFile(false);
        updatePanel();
    }

    private void initialize(boolean separate, boolean autoOrder)
    {
        setBorder(BorderFactory.createTitledBorder((Border) null));
        setLayout(new BorderLayout());

        preview = new ThumbnailComponent(false);
        preview.setShortDisplay(true);

        add(preview, BorderLayout.CENTER);

        optionsPanel = new JPanel();
        add(optionsPanel, BorderLayout.SOUTH);
        GridBagLayout gbl_optionsPanel = new GridBagLayout();
        gbl_optionsPanel.columnWidths = new int[] {0, 50, 0, 0, 0, 0};
        gbl_optionsPanel.rowHeights = new int[] {0, 0, 0, 0, 0, 0, 0, 0};
        gbl_optionsPanel.columnWeights = new double[] {0.0, 1.0, 1.0, 1.0, 0.0, Double.MIN_VALUE};
        gbl_optionsPanel.rowWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
        optionsPanel.setLayout(gbl_optionsPanel);
        GridBagConstraints gbc_loadInSeparatedLabel = new GridBagConstraints();
        gbc_loadInSeparatedLabel.gridwidth = 4;
        gbc_loadInSeparatedLabel.anchor = GridBagConstraints.WEST;
        gbc_loadInSeparatedLabel.insets = new Insets(0, 0, 5, 5);
        gbc_loadInSeparatedLabel.gridx = 0;
        gbc_loadInSeparatedLabel.gridy = 0;
        loadInSeparatedLabel = new JLabel("Load in separated sequences");
        loadInSeparatedLabel.setToolTipText("All images are opened in its own sequence");
        optionsPanel.add(loadInSeparatedLabel, gbc_loadInSeparatedLabel);

        // setting GUI
        separateSeqCheck = new JCheckBox();
        separateSeqCheck.setToolTipText("All images are opened in its own sequence");
        loadInSeparatedLabel.setLabelFor(separateSeqCheck);
        separateSeqCheck.setSelected(separate);
        separateSeqCheck.addChangeListener(new ChangeListener()
        {
            @Override
            public void stateChanged(ChangeEvent e)
            {
                updateAutoOrderEnableState();
            }
        });
        GridBagConstraints gbc_separateSeqCheck = new GridBagConstraints();
        gbc_separateSeqCheck.anchor = GridBagConstraints.EAST;
        gbc_separateSeqCheck.insets = new Insets(0, 0, 5, 0);
        gbc_separateSeqCheck.gridx = 4;
        gbc_separateSeqCheck.gridy = 0;
        optionsPanel.add(separateSeqCheck, gbc_separateSeqCheck);

        autoOrderLabel = new JLabel("Automatic ordering");
        autoOrderLabel.setToolTipText("Try to automatically set Z, T, C position of an image from their file name");
        GridBagConstraints gbc_autoOrderLabel = new GridBagConstraints();
        gbc_autoOrderLabel.gridwidth = 4;
        gbc_autoOrderLabel.anchor = GridBagConstraints.WEST;
        gbc_autoOrderLabel.insets = new Insets(0, 0, 5, 5);
        gbc_autoOrderLabel.gridx = 0;
        gbc_autoOrderLabel.gridy = 1;
        optionsPanel.add(autoOrderLabel, gbc_autoOrderLabel);

        autoOrderCheck = new JCheckBox();
        autoOrderCheck.setToolTipText("Try to automatically set Z, T, C position of an image from their file name");
        autoOrderLabel.setLabelFor(autoOrderCheck);
        autoOrderCheck.setSelected(autoOrder);
        GridBagConstraints gbc_autoOrderCheck = new GridBagConstraints();
        gbc_autoOrderCheck.anchor = GridBagConstraints.EAST;
        gbc_autoOrderCheck.insets = new Insets(0, 0, 5, 0);
        gbc_autoOrderCheck.gridx = 4;
        gbc_autoOrderCheck.gridy = 1;

        optionsPanel.add(autoOrderCheck, gbc_autoOrderCheck);

        seriesLabel = new JLabel("Series");
        seriesLabel.setToolTipText("Series to load (only for multi serie image)");
        GridBagConstraints gbc_seriesLabel = new GridBagConstraints();
        gbc_seriesLabel.anchor = GridBagConstraints.WEST;
        gbc_seriesLabel.insets = new Insets(0, 0, 5, 5);
        gbc_seriesLabel.gridx = 0;
        gbc_seriesLabel.gridy = 2;
        optionsPanel.add(seriesLabel, gbc_seriesLabel);

        seriesSpinner = new SpecialValueSpinner(new SpecialValueSpinnerModel(-1, -1, 0, 1, -1, "ALL"));
        seriesSpinner.setPreferredSize(new Dimension(50, 22));
        seriesSpinner.addChangeListener(new ChangeListener()
        {
            @Override
            public void stateChanged(ChangeEvent e)
            {
                seriesChanged();
            }
        });
        seriesSpinner.setToolTipText("Series to load (only for multi serie image)");
        GridBagConstraints gbc_seriesSpinner = new GridBagConstraints();
        gbc_seriesSpinner.insets = new Insets(0, 0, 5, 5);
        gbc_seriesSpinner.anchor = GridBagConstraints.WEST;
        gbc_seriesSpinner.gridx = 1;
        gbc_seriesSpinner.gridy = 2;
        optionsPanel.add(seriesSpinner, gbc_seriesSpinner);

        channelLabel = new JLabel("Channel");
        channelLabel.setToolTipText("Channel to load");
        GridBagConstraints gbc_channelLabel = new GridBagConstraints();
        gbc_channelLabel.anchor = GridBagConstraints.EAST;
        gbc_channelLabel.insets = new Insets(0, 0, 5, 5);
        gbc_channelLabel.gridx = 2;
        gbc_channelLabel.gridy = 2;
        optionsPanel.add(channelLabel, gbc_channelLabel);

        channelSpinner = new SpecialValueSpinner(new SpecialValueSpinnerModel(-1, -1, 0, 1, -1, "ALL"));
        channelSpinner.setPreferredSize(new Dimension(50, 22));
        channelSpinner.addChangeListener(new ChangeListener()
        {
            @Override
            public void stateChanged(ChangeEvent e)
            {
                rangeChanged();
            }
        });
        channelSpinner.setToolTipText("Channel to load");
        GridBagConstraints gbc_channelSpinner = new GridBagConstraints();
        gbc_channelSpinner.anchor = GridBagConstraints.EAST;
        gbc_channelSpinner.gridwidth = 2;
        gbc_channelSpinner.insets = new Insets(0, 0, 5, 0);
        gbc_channelSpinner.gridx = 3;
        gbc_channelSpinner.gridy = 2;
        optionsPanel.add(channelSpinner, gbc_channelSpinner);

        resolutionFixLabel = new JLabel("Resolution");
        resolutionFixLabel.setToolTipText("Select resolution level to open");
        GridBagConstraints gbc_resolutionFixLabel = new GridBagConstraints();
        gbc_resolutionFixLabel.anchor = GridBagConstraints.WEST;
        gbc_resolutionFixLabel.insets = new Insets(0, 0, 5, 5);
        gbc_resolutionFixLabel.gridx = 0;
        gbc_resolutionFixLabel.gridy = 3;
        optionsPanel.add(resolutionFixLabel, gbc_resolutionFixLabel);

        resolutionSlider = new JSlider();
        resolutionSlider.setSnapToTicks(true);
        resolutionSlider.setMinimumSize(new Dimension(36, 22));
        resolutionSlider.setMaximumSize(new Dimension(100, 22));
        resolutionSlider.setPreferredSize(new Dimension(100, 20));
        resolutionSlider.setToolTipText("Select resolution level to open");
        resolutionSlider.setValue(0);
        resolutionSlider.setMaximum(10);
        resolutionSlider.setFocusable(false);
        resolutionSlider.addChangeListener(new ChangeListener()
        {
            @Override
            public void stateChanged(ChangeEvent e)
            {
                updateFinaleResolution();
            }
        });

        resolutionLevelLabel = new JLabel("");
        GridBagConstraints gbc_resolutionLevelLabel = new GridBagConstraints();
        gbc_resolutionLevelLabel.anchor = GridBagConstraints.WEST;
        gbc_resolutionLevelLabel.insets = new Insets(0, 0, 5, 5);
        gbc_resolutionLevelLabel.gridx = 1;
        gbc_resolutionLevelLabel.gridy = 3;
        optionsPanel.add(resolutionLevelLabel, gbc_resolutionLevelLabel);
        GridBagConstraints gbc_resolutionSlider = new GridBagConstraints();
        gbc_resolutionSlider.anchor = GridBagConstraints.EAST;
        gbc_resolutionSlider.fill = GridBagConstraints.VERTICAL;
        gbc_resolutionSlider.gridwidth = 3;
        gbc_resolutionSlider.insets = new Insets(0, 0, 5, 0);
        gbc_resolutionSlider.gridx = 2;
        gbc_resolutionSlider.gridy = 3;
        optionsPanel.add(resolutionSlider, gbc_resolutionSlider);

        xyRegionLabel = new JLabel("XY region");
        xyRegionLabel.setToolTipText("Rectangular region to load (X,Y,W,H) in original resolution coordinates (pixel)");
        GridBagConstraints gbc_xyRegionLabel = new GridBagConstraints();
        gbc_xyRegionLabel.anchor = GridBagConstraints.WEST;
        gbc_xyRegionLabel.insets = new Insets(0, 0, 5, 5);
        gbc_xyRegionLabel.gridx = 0;
        gbc_xyRegionLabel.gridy = 4;
        optionsPanel.add(xyRegionLabel, gbc_xyRegionLabel);

        xyRegionComp = new Region2DComponent();
        xyRegionComp.setMaximumSize(new Dimension(160, 24));
        xyRegionComp.setInteger(true);
        xyRegionComp.setToolTipText("Rectangular region to load (X,Y,W,H) in original resolution coordinates");
        GridBagConstraints gbc_xyRegionComp = new GridBagConstraints();
        gbc_xyRegionComp.fill = GridBagConstraints.HORIZONTAL;
        gbc_xyRegionComp.gridwidth = 3;
        gbc_xyRegionComp.insets = new Insets(0, 0, 5, 5);
        gbc_xyRegionComp.gridx = 1;
        gbc_xyRegionComp.gridy = 4;
        optionsPanel.add(xyRegionComp, gbc_xyRegionComp);

        xyRegionLoadingCheck = new JCheckBox();
        xyRegionLoadingCheck.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                xyRegionComp.setEnabled(xyRegionLoadingCheck.isSelected());
            }
        });
        xyRegionLoadingCheck.setToolTipText("Enable region loading");
        GridBagConstraints gbc_xyRegionLoadingCheck = new GridBagConstraints();
        gbc_xyRegionLoadingCheck.anchor = GridBagConstraints.EAST;
        gbc_xyRegionLoadingCheck.insets = new Insets(0, 0, 5, 0);
        gbc_xyRegionLoadingCheck.gridx = 4;
        gbc_xyRegionLoadingCheck.gridy = 4;
        optionsPanel.add(xyRegionLoadingCheck, gbc_xyRegionLoadingCheck);

        zRangeLabel = new JLabel("Z range  ");
        zRangeLabel.setToolTipText("Z interval to load");
        GridBagConstraints gbc_zRangeLabel = new GridBagConstraints();
        gbc_zRangeLabel.anchor = GridBagConstraints.WEST;
        gbc_zRangeLabel.insets = new Insets(0, 0, 5, 5);
        gbc_zRangeLabel.gridx = 0;
        gbc_zRangeLabel.gridy = 5;
        optionsPanel.add(zRangeLabel, gbc_zRangeLabel);

        zRangeComp = new RangeComponent();
        zRangeComp.addChangeListener(new ChangeListener()
        {
            @Override
            public void stateChanged(ChangeEvent e)
            {
                rangeChanged();
            }
        });
        zRangeComp.setToolTipText("Z interval to load");
        zRangeComp.setMinimumSize(new Dimension(130, 22));
        zRangeComp.setMaximumSize(new Dimension(180, 22));
        zRangeComp.getHighSpinner().setPreferredSize(new Dimension(50, 20));
        zRangeComp.getHighSpinner().setMaximumSize(new Dimension(50, 20));
        zRangeComp.getLowSpinner().setMaximumSize(new Dimension(50, 20));
        zRangeComp.getLowSpinner().setPreferredSize(new Dimension(50, 20));
        zRangeComp.setPreferredSize(new Dimension(180, 22));
        zRangeComp.getSlider().setPreferredSize(new Dimension(70, 22));
        zRangeComp.setSliderVisible(true);
        GridBagConstraints gbc_zRangeComp = new GridBagConstraints();
        gbc_zRangeComp.gridwidth = 4;
        gbc_zRangeComp.insets = new Insets(0, 0, 5, 0);
        gbc_zRangeComp.fill = GridBagConstraints.HORIZONTAL;
        gbc_zRangeComp.gridx = 1;
        gbc_zRangeComp.gridy = 5;
        optionsPanel.add(zRangeComp, gbc_zRangeComp);

        tRangeLabel = new JLabel("T range  ");
        tRangeLabel.setToolTipText("T interval to load");
        GridBagConstraints gbc_tRangeLabel = new GridBagConstraints();
        gbc_tRangeLabel.anchor = GridBagConstraints.WEST;
        gbc_tRangeLabel.insets = new Insets(0, 0, 0, 5);
        gbc_tRangeLabel.gridx = 0;
        gbc_tRangeLabel.gridy = 6;
        optionsPanel.add(tRangeLabel, gbc_tRangeLabel);

        tRangeComp = new RangeComponent();
        tRangeComp.addChangeListener(new ChangeListener()
        {
            @Override
            public void stateChanged(ChangeEvent e)
            {
                rangeChanged();
            }
        });
        tRangeComp.setToolTipText("T interval to load");
        tRangeComp.setMinimumSize(new Dimension(130, 22));
        tRangeComp.setMaximumSize(new Dimension(180, 22));
        tRangeComp.getLowSpinner().setPreferredSize(new Dimension(50, 20));
        tRangeComp.getHighSpinner().setPreferredSize(new Dimension(50, 20));
        tRangeComp.getHighSpinner().setMaximumSize(new Dimension(50, 20));
        tRangeComp.getLowSpinner().setMaximumSize(new Dimension(50, 20));
        tRangeComp.setPreferredSize(new Dimension(180, 22));
        tRangeComp.getSlider().setPreferredSize(new Dimension(70, 22));
        tRangeComp.setSliderVisible(true);
        GridBagConstraints gbc_tRangeComp = new GridBagConstraints();
        gbc_tRangeComp.gridwidth = 4;
        gbc_tRangeComp.fill = GridBagConstraints.HORIZONTAL;
        gbc_tRangeComp.gridx = 1;
        gbc_tRangeComp.gridy = 6;
        optionsPanel.add(tRangeComp, gbc_tRangeComp);
    }

    protected void updatePreviewOnChange()
    {
        if (updatingPanel)
            return;

        int v;
        int z = -1;
        int t = -1;
        boolean changed = false;

        // detect change on z, t, c position in preview
        v = getZMin();
        if (pZMin != v)
        {
            z = v;
            pZMin = v;
            changed = true;
        }
        v = getZMax();
        if (pZMax != v)
        {
            z = v;
            pZMax = v;
            changed = true;
        }

        v = getTMin();
        if (pTMin != v)
        {
            t = v;
            pTMin = v;
            changed = true;
        }
        v = getTMax();
        if (pTMax != v)
        {
            t = v;
            pTMax = v;
            changed = true;
        }

        v = getChannel();
        if (pCh != v)
        {
            pCh = v;
            changed = true;
        }

        // changed ? --> update preview...
        if (changed)
            updatePreview(z, t);
    }

    protected void rangeChanged()
    {
        updatePreviewOnChange();
    }

    protected void seriesChanged()
    {
        // update series index
        updatePreview(previewThread.fileId, ((Integer) seriesSpinner.getValue()).intValue());
    }

    void updateAutoOrderEnableState()
    {
        autoOrderCheck.setEnabled(autoOrderEnable && !isSeparateSequenceSelected());
    }

    void updateXYRegion()
    {
        if (metadata != null)
        {
            final Rectangle2D r = xyRegionComp.getRegion();
            final int s = getSelectedSeries();
            final int sizeX = MetaDataUtil.getSizeX(metadata, s);
            final int sizeY = MetaDataUtil.getSizeY(metadata, s);

            xyRegionLoadingCheck.setEnabled(true);
            // xyRegionLoadingCheck.setSelected(false);
            xyRegionComp.setEnabled(xyRegionLoadingCheck.isSelected());
            // re-init region if out of bounds
            if ((r.getMaxX() > sizeX) || (r.getMaxY() > sizeY))
                xyRegionComp.setRegion(0, 0, sizeX, sizeY);
        }
        else
        {
            xyRegionComp.setEnabled(false);
            xyRegionLoadingCheck.setEnabled(false);
        }
    }

    void updateZRange()
    {
        if (metadata != null)
        {
            final int s = getSelectedSeries();
            final int sizeZ = Math.max(MetaDataUtil.getSizeZ(metadata, s), 1);

            zRangeComp.setMinMaxStep(0d, sizeZ - 1d, 1d);
            zRangeComp.setLowHigh(0d, sizeZ - 1d);
            zRangeComp.setEnabled(sizeZ > 1);
        }
        else
        {
            zRangeComp.setMinMaxStep(0d, 0d, 1d);
            zRangeComp.setEnabled(false);
        }
    }

    void updateTRange()
    {
        if (metadata != null)
        {
            final int s = getSelectedSeries();
            final int sizeT = Math.max(MetaDataUtil.getSizeT(metadata, s), 1);

            tRangeComp.setMinMaxStep(0d, sizeT - 1d, 1d);
            tRangeComp.setLowHigh(0d, sizeT - 1d);
            tRangeComp.setEnabled(sizeT > 1);
        }
        else
        {
            tRangeComp.setMinMaxStep(0d, 0d, 1d);
            tRangeComp.setEnabled(false);
        }
    }

    void updateChannelRange()
    {
        if (metadata != null)
        {
            final int s = getSelectedSeries();
            final int sizeC = Math.max(MetaDataUtil.getSizeC(metadata, s), 1);

            channelSpinner.setModel(new SpecialValueSpinnerModel(-1, -1, sizeC - 1, 1, -1, "ALL"));
            channelSpinner.setEnabled(sizeC > 1);
        }
        else
        {
            channelSpinner.setModel(new SpecialValueSpinnerModel(-1, -1, 0, 1, -1, "ALL"));
            channelSpinner.setEnabled(false);
        }
    }

    void updateSeriesRange()
    {
        if (metadata != null)
        {
            final int numSeries = Math.max(MetaDataUtil.getNumSeries(metadata), 1);

            seriesSpinner.setModel(new SpecialValueSpinnerModel(getSeries(), -1, numSeries - 1, 1, -1, "ALL"));
            seriesSpinner.setEnabled(numSeries > 1);
        }
        else
        {
            seriesSpinner.setModel(new SpecialValueSpinnerModel(-1, -1, 0, 1, -1, "ALL"));
            seriesSpinner.setEnabled(false);
        }
    }

    void updateResolutionSlider()
    {
        // min sub resolution to open (full resolution by default)
        int minRes = 0;
        // max sub resolution to open
        int maxRes = 0;

        if (metadata != null)
        {
            final int s = getSelectedSeries();
            long sizeXY;

            // // size of XY plane
            // sizeXY = (long) MetaDataUtil.getSizeX(metadata, s) * (long) MetaDataUtil.getSizeY(metadata, s);
            //
            // // we can't handle that plane size
            // while (sizeXY > Integer.MAX_VALUE)
            // {
            // // reduce resolution until XY plane size is acceptable
            // minRes++;
            // sizeXY /= 4;
            // }

            // size of XY plane
            sizeXY = (long) MetaDataUtil.getSizeX(metadata, s) * (long) MetaDataUtil.getSizeY(metadata, s);

            // no need to get lower than 128x128
            while (sizeXY > 16384)
            {
                // increase max sub resolution until XY plane is too low
                maxRes++;
                sizeXY /= 4;
            }
        }

        // apply
        resolutionSlider.setMinimum(minRes);
        resolutionSlider.setMaximum(maxRes);
        resolutionSlider.setValue(minRes);

        // no need to enable it
        resolutionSlider.setEnabled(maxRes > 0);

        updateFinaleResolution();
    }

    void updateFinaleResolution()
    {
        if (metadata != null)
        {
            final int res = resolutionSlider.getValue();
            final int s = getSelectedSeries();
            final int baseX = MetaDataUtil.getSizeX(metadata, s);
            final int baseY = MetaDataUtil.getSizeY(metadata, s);
            final double diviser = Math.pow(2d, res);

            resolutionLevelLabel.setText(res + " (" + Integer.toString((int) (baseX / diviser)) + " x "
                    + Integer.toString((int) (baseY / diviser)) + ")");
        }
        else
            resolutionLevelLabel.setText("");
    }

    void disablePanel()
    {
        resolutionSlider.setEnabled(false);
        resolutionLevelLabel.setText("");
        xyRegionComp.setEnabled(false);
        xyRegionLoadingCheck.setEnabled(false);
        tRangeComp.setEnabled(false);
        zRangeComp.setEnabled(false);
        channelSpinner.setEnabled(false);
        seriesSpinner.setEnabled(false);
    }

    void updatePanel()
    {
        updatingPanel = true;
        try
        {
            updateResolutionSlider();
            updateXYRegion();
            updateTRange();
            updateZRange();
            updateChannelRange();
            updateSeriesRange();
        }
        finally
        {
            updatingPanel = false;
        }
    }

    void setAutoOrderEnabled(boolean value)
    {
        autoOrderEnable = value;
        updateAutoOrderEnableState();
    }

    public boolean isSeparateSequenceSelected()
    {
        return separateSeqCheck.isVisible() && separateSeqCheck.isSelected();
    }

    public boolean isAutoOrderSelected()
    {
        return autoOrderCheck.isVisible() && autoOrderCheck.isSelected();
    }

    public int getResolutionLevel()
    {
        if (resolutionSlider.isVisible())
            return resolutionSlider.getValue();

        return 0;
    }

    public Rectangle getXYRegion()
    {
        if (xyRegionComp.isVisible() && xyRegionComp.isEnabled())
            return (Rectangle) xyRegionComp.getRegion();

        return null;
    }

    public void setXYRegion(Rectangle region)
    {
        // enable region loading
        xyRegionLoadingCheck.setSelected(true);
        xyRegionComp.setEnabled(true);
        xyRegionComp.setRegion(region);
    }

    public int getZMin()
    {
        if (zRangeComp.isVisible())
            return (int) zRangeComp.getLow();

        return 0;
    }

    public int getZMax()
    {
        if (zRangeComp.isVisible())
            return (int) zRangeComp.getHigh();

        return 0;
    }

    public int getTMin()
    {
        if (tRangeComp.isVisible())
            return (int) tRangeComp.getLow();

        return 0;
    }

    public int getTMax()
    {
        if (tRangeComp.isVisible())
            return (int) tRangeComp.getHigh();

        return 0;
    }

    public int getChannel()
    {
        if (channelSpinner.isVisible())
            return ((Integer) channelSpinner.getValue()).intValue();

        // all by default
        return -1;
    }

    public int getSeries()
    {
        if (seriesSpinner.isVisible())
            // use cached value
            return series;

        // all by default
        return -1;
    }

    public void setSeries(int series)
    {
        // set default series
        seriesSpinner.setValue(Integer.valueOf(series));
    }

    /**
     * Same as {@link #getSeries()} except it returns 0 instead of -1 to ensure we have one selected series
     */
    public int getSelectedSeries()
    {
        final int result = getSeries();

        return (result == -1) ? 0 : result;
    }

    /**
     * Asynchronous image preview refresh only ({@link #updatePreview(String)} should have be called once before to give
     * the fileId)
     */
    protected void updatePreview(int z, int t)
    {
        // interrupt previous preview refresh
        cancelPreview();

        // only if we had a previous load
        if (previewThread != null)
        {
            previewThread = new PreviewUpdater(previewThread.fileId, z, t);
            previewThread.start();
        }
    }

    /**
     * Asynchronous preview refresh
     */
    public void updatePreview(String fileId, int s)
    {
        // interrupt previous preview refresh
        cancelPreview();

        // reset metadata and series index
        metadata = null;
        metadataFieldsOk = false;
        series = s;

        previewThread = new PreviewUpdater(fileId);
        previewThread.start();
    }

    /**
     * Asynchronous preview refresh
     */
    public void updatePreview(String fileId)
    {
        updatePreview(fileId, -1);
    }

    /**
     * Cancel preview refresh
     */
    public void cancelPreview()
    {
        // brutal interruption of previous execution
        if (previewThread != null)
            previewThread.interrupt();
    }

    public boolean isMultiFile()
    {
        // we can use visible state for this one
        return separateSeqCheck.isVisible();
    }

    /**
     * Enable multi file mode for option panel
     */
    public void setMultiFile(boolean value)
    {
        // no change
        if (multiFile == value)
            return;

        multiFile = value;

        final GridBagLayout layout = (GridBagLayout) optionsPanel.getLayout();
        final int margin = value ? 3 : 0;
        final int marginInv = 3 - margin;

        // layout modification
        layout.getConstraints(loadInSeparatedLabel).insets.bottom = margin;
        layout.getConstraints(separateSeqCheck).insets.bottom = margin;
        layout.getConstraints(autoOrderLabel).insets.bottom = margin;
        layout.getConstraints(autoOrderCheck).insets.bottom = margin;

        layout.getConstraints(channelLabel).insets.bottom = marginInv;
        layout.getConstraints(channelSpinner).insets.bottom = marginInv;
        layout.getConstraints(seriesLabel).insets.bottom = marginInv;
        layout.getConstraints(seriesSpinner).insets.bottom = marginInv;
        layout.getConstraints(resolutionFixLabel).insets.bottom = marginInv;
        layout.getConstraints(resolutionSlider).insets.bottom = marginInv;
        layout.getConstraints(xyRegionLabel).insets.bottom = marginInv;
        layout.getConstraints(xyRegionComp).insets.bottom = marginInv;
        layout.getConstraints(xyRegionLoadingCheck).insets.bottom = marginInv;
        layout.getConstraints(zRangeLabel).insets.bottom = marginInv;
        layout.getConstraints(zRangeComp).insets.bottom = marginInv;
        layout.getConstraints(tRangeLabel).insets.bottom = marginInv;
        layout.getConstraints(tRangeComp).insets.bottom = marginInv;

        loadInSeparatedLabel.setVisible(value);
        separateSeqCheck.setVisible(value);
        autoOrderLabel.setVisible(value);
        autoOrderCheck.setVisible(value);

        seriesLabel.setVisible(!value);
        seriesSpinner.setVisible(!value);
        channelLabel.setVisible(!value);
        channelSpinner.setVisible(!value);
        resolutionFixLabel.setVisible(!value);
        resolutionSlider.setVisible(!value);
        xyRegionLabel.setVisible(!value);
        xyRegionComp.setVisible(!value);
        xyRegionLoadingCheck.setVisible(!value);
        zRangeLabel.setVisible(!value);
        zRangeComp.setVisible(!value);
        tRangeLabel.setVisible(!value);
        tRangeComp.setVisible(!value);

        // by default we enable auto ordering with multi file
        setAutoOrderEnabled(value);

        // multiple file --> no preview
        if (value)
        {
            cancelPreview();
            metadata = null;
            metadataFieldsOk = true;
            if (previewThread != null)
            {
                try
                {
                    previewThread.join();
                }
                catch (InterruptedException e)
                {
                    // ignore
                }
            }

            preview.setImage(ResourceUtil.ICON_PICTURE);
            preview.setInfos("Multiple selection...");
        }
    }

    /**
     * We are closing the Open Dialog.
     * Ensure metadata are correctly loaded (it's important that this method is called from EDT)
     */
    public void closingFromEDT()
    {
        if (previewThread == null)
            return;

        // interrupt process from thread
        previewThread.interrupt();

        // need update metadata fields
        if (!metadataFieldsOk)
        {
            // get fileId
            final String fileId = previewThread.fileId;
            final List<SequenceFileImporter> importers = Loader.getSequenceFileImporters(fileId);

            for (SequenceFileImporter importer : importers)
            {
                try
                {
                    if (importer.open(fileId, 0))
                    {
                        try
                        {
                            metadata = importer.getOMEXMLMetaData();

                            // update panel (we are on EDT)
                            updatePanel();

                            // done
                            return;
                        }
                        finally
                        {
                            importer.close();
                        }
                    }
                }
                catch (UnsupportedFormatException e)
                {
                    // try next importer...
                }
                catch (RuntimeException e)
                {
                    // try next importer...
                }
                catch (IOException e)
                {
                    // try next importer...
                }

                // next importer
            }
        }
    }

}
