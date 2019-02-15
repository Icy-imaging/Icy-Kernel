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
import java.nio.channels.ClosedByInterruptException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JToggleButton;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import icy.file.Loader;
import icy.file.SequenceFileGroupImporter;
import icy.file.SequenceFileImporter;
import icy.file.SequenceFileSticher.SequenceFileGroup;
import icy.gui.component.PopupPanel;
import icy.gui.component.RangeComponent;
import icy.gui.component.Region2DComponent;
import icy.gui.component.SpecialValueSpinner;
import icy.gui.component.ThumbnailComponent;
import icy.gui.component.model.SpecialValueSpinnerModel;
import icy.image.IcyBufferedImage;
import icy.resource.ResourceUtil;
import icy.sequence.MetaDataUtil;
import icy.sequence.SequenceIdImporter;
import icy.system.IcyExceptionHandler;
import icy.system.thread.ThreadUtil;
import icy.type.collection.CollectionUtil;
import icy.util.OMEUtil;
import ome.xml.meta.OMEXMLMetadata;
import plugins.kernel.importer.LociImporterPlugin;

public class LoaderOptionPanel extends JPanel
{
    public enum LoaderLoadingType
    {
        GROUP
        {
            @Override
            public String toString()
            {
                return "Group files";
            }
        },
        GROUP_NO_ORDERING
        {
            @Override
            public String toString()
            {
                return "Group files (no ordering)";
            }
        },
        NO_GROUP
        {
            @Override
            public String toString()
            {
                return "Separate files";
            }
        }
    }

    private class PreviewSingleUpdate extends Thread
    {
        SequenceFileImporter importer;
        List<SequenceFileImporter> importers;
        List<String> files;
        int z;
        int t;
        boolean imageRefreshOnly;
        // boolean softInterrupted;

        PreviewSingleUpdate(SequenceFileImporter importer, List<String> files, int z, int t, boolean imageRefreshOnly)
        {
            super("Preview single update");

            this.importer = importer;
            this.importers = null;
            this.files = Loader.cleanNonImageFile(files);
            this.z = z;
            this.t = t;
            this.imageRefreshOnly = imageRefreshOnly;
            // softInterrupted = false;
        }

        public List<SequenceFileImporter> getSingleFileImporters()
        {
            // get importer and open image
            final List<SequenceFileImporter> result = Loader.getSequenceFileImporters(files.get(0));

            // adjust importers setting if needed
            for (SequenceFileImporter imp : result)
            {
                // LOCI importer ?
                if (imp instanceof LociImporterPlugin)
                {
                    // separate loading wanted ? --> disable grouping in LOCI importer
                    if (isSeparateSequenceSelected())
                        ((LociImporterPlugin) imp).setGroupFiles(false);
                }
            }

            return result;
        }

        void getImporters()
        {
            // already done ? --> exit
            if (importer != null)
                return;

            // importers for single file opening
            importers = getSingleFileImporters();

            // want to open a group of file ? --> insert the SequenceFileGroupImporter importer
            if (isGroupedSequenceSelected() && isMultiFile())
                importers.add(0, new SequenceFileGroupImporter());

            // get default importer
            importer = importers.get(0);
        }

        void close()
        {
            try
            {
                // close previous importer (shouldn't exist here)
                if (importer != null)
                    importer.close();
            }
            catch (IOException e)
            {
                // ignore
            }

            importer = null;
        }

        boolean internalOpen(SequenceFileImporter imp) throws ClosedByInterruptException
        {
            try
            {
                if (imp instanceof SequenceFileGroupImporter)
                    ((SequenceFileGroupImporter) imp).open(files, SequenceIdImporter.FLAG_METADATA_MINIMUM);
                else
                    imp.open(files.get(0), SequenceIdImporter.FLAG_METADATA_MINIMUM);
            }
            catch (ClosedByInterruptException e)
            {
                throw e;
            }
            catch (Exception e)
            {
                try
                {
                    imp.close();
                }
                catch (Throwable e2)
                {
                    // ignore
                }
            }

            // correctly opened ? --> use this importer
            return imp.getOpened() != null;
        }

        boolean open() throws ClosedByInterruptException
        {
            // get importers first if not already done
            getImporters();

            // already opened ? --> exit
            if (importer.getOpened() != null)
                return true;

            // case where we just have the good importer that we need to re-open
            if (importers == null)
                return internalOpen(importer);

            // try to open from the given importers
            for (SequenceFileImporter imp : importers)
            {
                // correctly opened ? --> use this importer
                if (internalOpen(imp))
                {
                    importer = imp;
                    return true;
                }
            }

            // can't open
            return false;
        }

        OMEXMLMetadata getMetaData() throws Exception
        {
            if (!open())
                throw new Exception("Can't open importer !");

            return importer.getOMEXMLMetaData();
        }

        IcyBufferedImage getThumbnail(int s) throws Exception
        {
            if (!open())
                throw new Exception("Can't open importer !");

            return importer.getThumbnail(s);
        }

        IcyBufferedImage getImage(int s, int res) throws Exception
        {
            if (!open())
                throw new Exception("Can't open importer !");

            if (pCh == -1)
                return importer.getImage(s, res, z, t);

            // specific channel
            return importer.getImage(s, res, z, t, pCh);
        }

        public boolean isMultiFile()
        {
            try
            {
                // return true if we have multiple file selection
                if (files.size() > 1)
                    return true;

                if (importer instanceof SequenceFileGroupImporter)
                {
                    final SequenceFileGroup group = ((SequenceFileGroupImporter) importer).getOpenedGroup();

                    if (group != null)
                        return (group.positions.size() > 1);
                }
            }
            catch (Throwable t)
            {
                // ignore
            }

            return false;
        }

        // PreviewSingleUpdate(SequenceFileImporter importer, String[] files, int z, int t, boolean imageRefreshOnly)
        // {
        // this(importer, CollectionUtil.asList(files), z, t, imageRefreshOnly);
        // }
        //
        // PreviewSingleUpdate(SequenceFileImporter importer, int z, int t)
        // {
        // this(importer, new String[0], z, t, true);
        // }
        //
        // PreviewSingleUpdate(String[] files)
        // {
        // this(null, files, -1, -1, false);
        // }
        //
        // PreviewSingleUpdate(List<String> files)
        // {
        // this(null, files, -1, -1, false);
        // }

        @Override
        public void run()
        {
            // interrupt process
            if (isInterrupted())
                return;

            // get current selected series
            final int s = getSelectedSeries();

            // only need to update image
            if (imageRefreshOnly)
            {
                // can't re open importer ? --> nothing to do here
                if ((importer == null) && (files.size() == 0))
                    return;

                try
                {
                    // use last - 1 resolution
                    final int res = Math.max(0, resolutionSlider.getMaximum() - 1);

                    try
                    {
                        // can't open ? --> error
                        if (!open())
                            throw new Exception("Can't open '" + files.get(0) + "' image file..");

                        // interrupted ? --> stop here
                        if (isInterrupted())
                            return;

                        // not defined --> use first
                        if (z == -1)
                            z = 0;
                        // not defined --> use first
                        if (t == -1)
                            t = 0;

                        // default position --> use thumbnail
                        if ((z == 0) && (t == 0) && (pCh == -1))
                            preview.setImage(getThumbnail(s));
                        // specific image
                        else
                            preview.setImage(getImage(s, res));
                    }
                    finally
                    {
                        // just close internals importers
                        if (importer instanceof SequenceFileGroupImporter)
                            ((SequenceFileGroupImporter) importer).closeInternalsImporters();
                    }
                }
                catch (ClosedByInterruptException e)
                {
                    // can't use importer anymore
                    close();
                }
                catch (InterruptedException e2)
                {
                    // can't use importer anymore
                    close();
                }
                catch (Throwable e)
                {
                    // can't use importer anymore
                    if (e.getCause() instanceof ClosedByInterruptException)
                        close();

                    // no more update ? --> show that an error happened
                    if (!previewUpdater.getNeedUpdate())
                        preview.setImage(ResourceUtil.ICON_DELETE);
                }

                // image updated
                return;
            }

            try
            {
                metadata = null;

                // no files ?
                if (files.size() == 0)
                {
                    preview.setImage(null);
                    preview.setInfos("");
                    metadata = OMEUtil.createOMEXMLMetadata();

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

                    // nothing more to do
                    return;
                }

                // loading...
                preview.setImage(ResourceUtil.ICON_WAIT);
                preview.setInfos("loading...");

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

                // close previous importer (shouldn't exist here)
                close();

                // open file(s)...
                if (!open())
                    throw new Exception("Can't open '" + files.get(0) + "' image file..");

                try
                {
                    // interrupted ? --> stop here
                    if (isInterrupted())
                        return;

                    metadata = getMetaData();

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
                    preview.setInfos(MetaDataUtil.getSizeX(metadata, s) + " x " + MetaDataUtil.getSizeY(metadata, s)
                            + " - " + MetaDataUtil.getSizeZ(metadata, s) + "Z x " + MetaDataUtil.getSizeT(metadata, s)
                            + "T - " + sizeC + " ch (" + MetaDataUtil.getDataType(metadata, s) + ")");

                    // interrupted ? --> stop here
                    if (isInterrupted())
                        return;

                    // initial preview --> use thumbnail
                    preview.setImage(getThumbnail(s));
                }
                finally
                {
                    // just close internals importers
                    if (importer instanceof SequenceFileGroupImporter)
                        ((SequenceFileGroupImporter) importer).closeInternalsImporters();
                }
            }
            catch (ClosedByInterruptException e)
            {
                // can't use importer anymore
                close();
            }
            catch (InterruptedException t)
            {
                // can't use importer anymore
                close();
            }
            catch (Throwable t1)
            {
                // System.out.println("Preview: can't load all image information.");
                // IcyExceptionHandler.showErrorMessage(t1, false);

                // no more update ? --> show that an error happened
                if (!previewUpdater.getNeedUpdate())
                {
                    // fatal error --> failed image
                    preview.setImage(ResourceUtil.ICON_DELETE);

                    // cannot even read metadata
                    if (!metadataFieldsOk)
                    {
                        preview.setInfos("Cannot read file");

                        try
                        {
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
                        catch (Throwable t2)
                        {
                            // probably interrupted...

                        }
                    }
                }
            }
        }

        // boolean isSoftInterrupted()
        // {
        // return softInterrupted;
        // }
        //
        // void softInterrupt()
        // {
        // softInterrupted = true;
        // }
    }

    private class PreviewUpdater extends Thread
    {
        SequenceFileImporter newImporter;
        List<String> newFiles;
        int newZ;
        int newT;
        boolean newImageRefreshOnly;

        boolean needUpdate;

        PreviewSingleUpdate singleUpdater;

        PreviewUpdater()
        {
            super("Preview updater");

            singleUpdater = null;
            needUpdate = false;
        }

        public boolean getNeedUpdate()
        {
            return needUpdate;
        }

        public boolean isUpdating()
        {
            return getNeedUpdate() || ((singleUpdater != null) && singleUpdater.isAlive());
        }

        public boolean isMultiFile()
        {
            if (singleUpdater == null)
                return false;

            return singleUpdater.isMultiFile();
        }

        /**
         * previous preview canceled ?
         */
        public boolean isPreviewCanceled()
        {
            if (singleUpdater == null)
                return true;

            return !singleUpdater.isAlive();
        }

        /**
         * Cancel preview refresh
         */
        public void cancelPreview()
        {
            // interrupt (soft interrupt to not close IO channel) current preview update
            if (singleUpdater != null)
                singleUpdater.interrupt();
        }

        /**
         * Asynchronous image preview refresh only.<br>
         * ({@link #updatePreview(String)} should have be called once before to give the fileId)
         */
        protected synchronized void updatePreview(int z, int t)
        {
            // nothing to do
            if (singleUpdater == null)
                return;

            // interrupt previous preview refresh
            cancelPreview();

            // prepare params
            newImporter = singleUpdater.importer;
            newFiles = singleUpdater.files;
            // use previous value
            if (z == -1)
                newZ = singleUpdater.z;
            else
                newZ = z;
            // use previous value
            if (t == -1)
                newT = singleUpdater.t;
            else
                newT = t;
            newImageRefreshOnly = true;

            // request preview update
            needUpdate = true;
        }

        /**
         * Asynchronous preview refresh (complete preview refresh)
         */
        public synchronized void updatePreview()
        {
            updatePreview(singleUpdater.files, series);
        }

        /**
         * Asynchronous preview refresh.<br>
         * ({@link #updatePreview(List, int)} should have be called once before to give the fileId)
         */
        public synchronized void updatePreview(int s)
        {
            updatePreview(singleUpdater.files, s);
        }

        /**
         * Asynchronous preview refresh
         */
        public synchronized void updatePreview(String[] files, int s)
        {
            // interrupt previous preview refresh
            cancelPreview();

            // reset metadata and series index
            metadata = null;
            metadataFieldsOk = false;
            series = s;

            // prepare params
            newImporter = null;
            newFiles = CollectionUtil.asList(files);
            newZ = -1;
            newT = -1;
            newImageRefreshOnly = false;

            // request preview update
            needUpdate = true;
        }

        /**
         * Asynchronous preview refresh
         */
        public synchronized void updatePreview(List<String> files, int s)
        {
            // interrupt previous preview refresh
            cancelPreview();

            // reset metadata and series index
            metadata = null;
            metadataFieldsOk = false;
            series = s;

            // prepare params
            newImporter = null;
            newFiles = files;
            newZ = -1;
            newT = -1;
            newImageRefreshOnly = false;

            // request preview update
            needUpdate = true;
        }

        // /**
        // * Asynchronous preview refresh
        // */
        // public synchronized void updatePreview(String[] files)
        // {
        // updatePreview(files, -1);
        // }

        /**
         * We are closing the Open Dialog.<br>
         * Don't forget to call this method otherwise the updater thread will remain active !!<br>
         * Ensure metadata are correctly loaded (it's important that this method is called from EDT)
         */
        public synchronized void close()
        {
            // interrupt preview
            cancelPreview();

            if (singleUpdater != null)
            {
                // need update metadata fields
                if (!metadataFieldsOk)
                {
                    try
                    {
                        // open file(s) if needed...
                        if (!singleUpdater.open())
                            return;

                        try
                        {
                            metadata = singleUpdater.importer.getOMEXMLMetaData();

                            // update panel (we are on EDT)
                            updatePanel();
                        }
                        finally
                        {
                            singleUpdater.close();
                        }
                    }
                    catch (Throwable t)
                    {
                        // we tried...
                    }
                }
            }

            // finally interrupt the updater thread
            interrupt();
        }

        @Override
        public void run()
        {
            try
            {
                // interrupt process
                while (!isInterrupted())
                {
                    // sleep a bit
                    Thread.sleep(10);

                    // need to be synchronized
                    synchronized (this)
                    {
                        // previous preview done ?
                        if (isPreviewCanceled())
                        {
                            // need preview update ?
                            if (needUpdate)
                            {
                                // create new single updater and start it
                                singleUpdater = new PreviewSingleUpdate(newImporter, newFiles, newZ, newT,
                                        newImageRefreshOnly);
                                singleUpdater.start();

                                // done
                                needUpdate = false;
                            }
                        }
                    }
                }
            }
            catch (InterruptedException t)
            {
                // no need to do more here...
            }
            catch (Throwable t)
            {
                System.out.println("Preview updater interrupted !");
                IcyExceptionHandler.showErrorMessage(t, false);
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
    protected PopupPanel popupPanel;
    protected JComboBox loadingTypeCombo;
    protected JLabel loadInSeparatedLabel;
    protected JSlider resolutionSlider;
    protected JLabel resolutionLevelLabel;
    protected JLabel zRangeLabel;
    protected JLabel tRangeLabel;
    protected JLabel channelLabel;
    protected RangeComponent zRangeComp;
    protected RangeComponent tRangeComp;
    protected SpecialValueSpinner channelSpinner;
    protected JLabel resolutionFixLabel;
    protected JToggleButton xyRegionLoadingToggle;
    protected JLabel seriesLabel;
    protected SpecialValueSpinner seriesSpinner;
    protected Region2DComponent xyRegionComp;

    // internals
    protected boolean metadataFieldsOk;
    protected PreviewUpdater previewUpdater;
    protected OMEXMLMetadata metadata;
    protected int series;
    protected int pZMin;
    protected int pZMax;
    protected int pTMin;
    protected int pTMax;
    protected int pCh;
    // protected boolean multiFile;
    protected boolean updatingPanel;

    /**
     * Create the panel.
     */
    public LoaderOptionPanel(LoaderLoadingType loadType)
    {
        super();

        metadataFieldsOk = false;
        metadata = null;
        series = -1;
        pZMin = -1;
        pZMax = -1;
        pTMin = -1;
        pTMax = -1;
        pCh = -1;
        updatingPanel = false;

        initialize(loadType);

        // need to be done *before* updatePanel
        previewUpdater = new PreviewUpdater();
        previewUpdater.start();

        updatePanel();
    }

    private void initialize(LoaderLoadingType loadType)
    {
        setBorder(BorderFactory.createTitledBorder((Border) null));
        setLayout(new BorderLayout());

        preview = new ThumbnailComponent(false);
        preview.setMinimumSize(new Dimension(200, 160));
        preview.setPreferredSize(new Dimension(240, 200));
        preview.setShortDisplay(true);

        add(preview, BorderLayout.CENTER);

        optionsPanel = new JPanel();
        popupPanel = new PopupPanel("Advanced options", optionsPanel);
        popupPanel.setExpanded(true);
        add(popupPanel, BorderLayout.SOUTH);

        GridBagLayout gbl_optionsPanel = new GridBagLayout();
        gbl_optionsPanel.columnWidths = new int[] {0, 100, 0, 0, 0};
        gbl_optionsPanel.rowHeights = new int[] {0, 0, 0, 0, 0, 0, 0};
        gbl_optionsPanel.columnWeights = new double[] {0.0, 1.0, 1.0, 1.0, Double.MIN_VALUE};
        gbl_optionsPanel.rowWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
        optionsPanel.setLayout(gbl_optionsPanel);
        GridBagConstraints gbc_loadInSeparatedLabel = new GridBagConstraints();
        gbc_loadInSeparatedLabel.anchor = GridBagConstraints.WEST;
        gbc_loadInSeparatedLabel.insets = new Insets(0, 0, 5, 5);
        gbc_loadInSeparatedLabel.gridx = 0;
        gbc_loadInSeparatedLabel.gridy = 0;
        loadInSeparatedLabel = new JLabel("Load type");
        loadInSeparatedLabel.setToolTipText(
                "Define if we try to group files / series or not (and eventually automatic set Z, T, C ordering from file name)");
        optionsPanel.add(loadInSeparatedLabel, gbc_loadInSeparatedLabel);

        loadingTypeCombo = new JComboBox();
        loadingTypeCombo.setToolTipText(
                "Define if we try to group files or not (and eventually automatic set Z, T, C ordering from file name)");
        loadingTypeCombo.setModel(new DefaultComboBoxModel(LoaderLoadingType.values()));
        loadingTypeCombo.setSelectedIndex(loadType.ordinal());
        loadingTypeCombo.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                loadingTypeChanged();
            }
        });
        GridBagConstraints gbc_loadingTypeCombo = new GridBagConstraints();
        gbc_loadingTypeCombo.fill = GridBagConstraints.HORIZONTAL;
        gbc_loadingTypeCombo.gridwidth = 3;
        gbc_loadingTypeCombo.insets = new Insets(0, 0, 5, 0);
        gbc_loadingTypeCombo.gridx = 1;
        gbc_loadingTypeCombo.gridy = 0;
        optionsPanel.add(loadingTypeCombo, gbc_loadingTypeCombo);

        seriesLabel = new JLabel("Series");
        seriesLabel.setToolTipText("Series to load (only for multi serie image)");
        GridBagConstraints gbc_seriesLabel = new GridBagConstraints();
        gbc_seriesLabel.anchor = GridBagConstraints.WEST;
        gbc_seriesLabel.insets = new Insets(0, 0, 5, 5);
        gbc_seriesLabel.gridx = 0;
        gbc_seriesLabel.gridy = 1;
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
        gbc_seriesSpinner.gridy = 1;
        optionsPanel.add(seriesSpinner, gbc_seriesSpinner);

        channelLabel = new JLabel("Channel");
        channelLabel.setToolTipText("Channel to load");
        GridBagConstraints gbc_channelLabel = new GridBagConstraints();
        gbc_channelLabel.anchor = GridBagConstraints.EAST;
        gbc_channelLabel.insets = new Insets(0, 0, 5, 5);
        gbc_channelLabel.gridx = 2;
        gbc_channelLabel.gridy = 1;
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
        gbc_channelSpinner.insets = new Insets(0, 0, 5, 0);
        gbc_channelSpinner.gridx = 3;
        gbc_channelSpinner.gridy = 1;
        optionsPanel.add(channelSpinner, gbc_channelSpinner);

        resolutionFixLabel = new JLabel("Resolution");
        resolutionFixLabel.setToolTipText("Select resolution level to open");
        GridBagConstraints gbc_resolutionFixLabel = new GridBagConstraints();
        gbc_resolutionFixLabel.anchor = GridBagConstraints.WEST;
        gbc_resolutionFixLabel.insets = new Insets(0, 0, 5, 5);
        gbc_resolutionFixLabel.gridx = 0;
        gbc_resolutionFixLabel.gridy = 2;
        optionsPanel.add(resolutionFixLabel, gbc_resolutionFixLabel);

        resolutionSlider = new JSlider();
        resolutionSlider.setSnapToTicks(true);
        resolutionSlider.setMinimumSize(new Dimension(80, 22));
        resolutionSlider.setMaximumSize(new Dimension(100, 22));
        resolutionSlider.setPreferredSize(new Dimension(100, 22));
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
        gbc_resolutionLevelLabel.gridy = 2;
        optionsPanel.add(resolutionLevelLabel, gbc_resolutionLevelLabel);
        GridBagConstraints gbc_resolutionSlider = new GridBagConstraints();
        gbc_resolutionSlider.fill = GridBagConstraints.BOTH;
        gbc_resolutionSlider.gridwidth = 2;
        gbc_resolutionSlider.insets = new Insets(0, 0, 5, 0);
        gbc_resolutionSlider.gridx = 2;
        gbc_resolutionSlider.gridy = 2;
        optionsPanel.add(resolutionSlider, gbc_resolutionSlider);

        xyRegionLoadingToggle = new JToggleButton("XY region");
        xyRegionLoadingToggle.setFocusPainted(false);
        xyRegionLoadingToggle.setIconTextGap(0);
        xyRegionLoadingToggle.setMargin(new Insets(2, 4, 2, 4));
        xyRegionLoadingToggle
                .setToolTipText("Enable region loading (X,Y)-(W,H) in original resolution coordinates (pixel)");
        xyRegionLoadingToggle.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                regionLoadingToggleChanged();
            }
        });
        GridBagConstraints gbc_xyRegionLoadingCheck = new GridBagConstraints();
        gbc_xyRegionLoadingCheck.fill = GridBagConstraints.BOTH;
        gbc_xyRegionLoadingCheck.insets = new Insets(0, 0, 5, 5);
        gbc_xyRegionLoadingCheck.gridx = 0;
        gbc_xyRegionLoadingCheck.gridy = 3;
        optionsPanel.add(xyRegionLoadingToggle, gbc_xyRegionLoadingCheck);

        xyRegionComp = new Region2DComponent();
        xyRegionComp.setMaximumSize(new Dimension(160, 24));
        xyRegionComp.setInteger(true);
        xyRegionComp.setToolTipText("Rectangular region to load (X,Y,W,H) in original resolution coordinates");
        GridBagConstraints gbc_xyRegionComp = new GridBagConstraints();
        gbc_xyRegionComp.fill = GridBagConstraints.HORIZONTAL;
        gbc_xyRegionComp.gridwidth = 3;
        gbc_xyRegionComp.insets = new Insets(0, 0, 5, 0);
        gbc_xyRegionComp.gridx = 1;
        gbc_xyRegionComp.gridy = 3;
        optionsPanel.add(xyRegionComp, gbc_xyRegionComp);

        zRangeLabel = new JLabel("Z range  ");
        zRangeLabel.setToolTipText("Z interval to load");
        GridBagConstraints gbc_zRangeLabel = new GridBagConstraints();
        gbc_zRangeLabel.anchor = GridBagConstraints.WEST;
        gbc_zRangeLabel.insets = new Insets(0, 0, 5, 5);
        gbc_zRangeLabel.gridx = 0;
        gbc_zRangeLabel.gridy = 4;
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
        gbc_zRangeComp.gridwidth = 3;
        gbc_zRangeComp.insets = new Insets(0, 0, 5, 0);
        gbc_zRangeComp.fill = GridBagConstraints.HORIZONTAL;
        gbc_zRangeComp.gridx = 1;
        gbc_zRangeComp.gridy = 4;
        optionsPanel.add(zRangeComp, gbc_zRangeComp);

        tRangeLabel = new JLabel("T range  ");
        tRangeLabel.setToolTipText("T interval to load");
        GridBagConstraints gbc_tRangeLabel = new GridBagConstraints();
        gbc_tRangeLabel.anchor = GridBagConstraints.WEST;
        gbc_tRangeLabel.insets = new Insets(0, 0, 0, 5);
        gbc_tRangeLabel.gridx = 0;
        gbc_tRangeLabel.gridy = 5;
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
        gbc_tRangeComp.gridwidth = 3;
        gbc_tRangeComp.fill = GridBagConstraints.HORIZONTAL;
        gbc_tRangeComp.gridx = 1;
        gbc_tRangeComp.gridy = 5;
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
        // update preview series index
        previewUpdater.updatePreview(((Integer) seriesSpinner.getValue()).intValue());
    }

    void updateLoadingType()
    {
        final List<String> files = getFiles();

        loadingTypeCombo.setEnabled((files != null) && !files.isEmpty());
        // loadingTypeCombo.setEnabled(metadata != null);
    }

    void updateXYRegion()
    {
        if (metadata != null)
        {
            final Rectangle2D r = xyRegionComp.getRegion();
            final int s = getSelectedSeries();
            final int sizeX = MetaDataUtil.getSizeX(metadata, s);
            final int sizeY = MetaDataUtil.getSizeY(metadata, s);

            xyRegionLoadingToggle.setEnabled(canUseAdvancedSetting());
            xyRegionComp.setEnabled(canUseAdvancedSetting() && xyRegionLoadingToggle.isSelected());
            // re-init region if out of bounds
            if ((r.getMaxX() > sizeX) || (r.getMaxY() > sizeY))
                xyRegionComp.setRegion(0, 0, sizeX, sizeY);
        }
        else
        {
            xyRegionComp.setEnabled(false);
            xyRegionLoadingToggle.setEnabled(false);
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
            zRangeComp.setEnabled(canUseAdvancedSetting() && (sizeZ > 1));
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
            tRangeComp.setEnabled(canUseAdvancedSetting() && (sizeT > 1));
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
            channelSpinner.setEnabled(canUseAdvancedSetting() && (sizeC > 1));
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
            seriesSpinner.setEnabled(canUseAdvancedSetting() && (numSeries > 1));
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
        resolutionSlider.setEnabled(canUseAdvancedSetting() && (maxRes > 0));

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
        loadingTypeCombo.setEnabled(false);
        channelSpinner.setEnabled(false);
        seriesSpinner.setEnabled(false);
        resolutionLevelLabel.setText("");
        resolutionSlider.setEnabled(false);
        xyRegionLoadingToggle.setEnabled(false);
        xyRegionComp.setEnabled(false);
        tRangeComp.setEnabled(false);
        zRangeComp.setEnabled(false);
    }

    void updatePanel()
    {
        updatingPanel = true;
        try
        {
            updateLoadingType();
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

    void loadingTypeChanged()
    {
        updatePreview(-1);
    }

    void regionLoadingToggleChanged()
    {
        xyRegionComp.setEnabled(xyRegionLoadingToggle.isEnabled() && xyRegionLoadingToggle.isSelected());
    }

    public List<String> getFiles()
    {
        if (previewUpdater != null)
            return previewUpdater.newFiles;

        return new ArrayList<String>();
    }

    public boolean getOptionsVisible()
    {
        return popupPanel.isExpanded();
    }

    public void setOptionsVisible(boolean value)
    {
        popupPanel.setExpanded(value);
    }

    public boolean isSeparateSequenceSelected()
    {
        return loadingTypeCombo.getSelectedItem() == LoaderLoadingType.NO_GROUP;
    }

    public boolean isGroupedSequenceSelected()
    {
        return loadingTypeCombo.getSelectedItem() == LoaderLoadingType.GROUP;
    }

    public boolean isMultiFile()
    {
        return previewUpdater.isMultiFile();
    }

    public boolean canUseAdvancedSetting()
    {
        return !(isSeparateSequenceSelected() && isMultiFile());
    }

    public LoaderLoadingType getLoadingType()
    {
        return (LoaderLoadingType) loadingTypeCombo.getSelectedItem();
    }

    void setLoadingType(LoaderLoadingType value)
    {
        loadingTypeCombo.setSelectedItem(value);
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
        xyRegionLoadingToggle.setSelected(true);
        xyRegionComp.setEnabled(true);
        xyRegionComp.setRegion(region);

        // we want options to be visible in that case
        setOptionsVisible(true);
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

    public boolean getFullZRange()
    {
        return (getZMin() == 0) && (getZMax() == (int) zRangeComp.getMax());
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

    public boolean getFullTRange()
    {
        return (getTMin() == 0) && (getTMax() == (int) tRangeComp.getMax());
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

        // we want options to be visible in that case
        setOptionsVisible(true);
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
     * Cancel preview refresh
     */
    public void cancelPreview()
    {
        previewUpdater.cancelPreview();
    }

    protected void updatePreview(int series)
    {
        previewUpdater.updatePreview(series);
    }

    /**
     * Asynchronous image preview refresh only, ({@link #updatePreview(String)} should have be called once before to
     * give
     * the fileId)
     */
    protected void updatePreview(int z, int t)
    {
        previewUpdater.updatePreview(z, t);
    }

    /**
     * Asynchronous preview refresh
     */
    public void updatePreview(String[] files, int s)
    {
        // series or files selection changed ?
        if ((series != s) || !CollectionUtil.equals(getFiles(), CollectionUtil.asList(files)))
            previewUpdater.updatePreview(files, s);
    }

    /**
     * Asynchronous preview refresh
     */
    public void updatePreview(List<String> files, int s)
    {
        // series or files selection changed ?
        if ((series != s) || !CollectionUtil.equals(getFiles(), files))
            previewUpdater.updatePreview(files, s);
    }

    /**
     * Asynchronous preview refresh
     */
    public void updatePreview(String[] files)
    {
        // files selection changed ?
        if (!CollectionUtil.equals(getFiles(), CollectionUtil.asList(files)))
            previewUpdater.updatePreview(files, -1);
    }

    /**
     * We are closing the Open Dialog.
     * Ensure metadata are correctly loaded (it's important that this method is called from EDT)
     */
    public void closingFromEDT()
    {
        previewUpdater.close();
    }
}
