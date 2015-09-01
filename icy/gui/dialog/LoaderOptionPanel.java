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
package icy.gui.dialog;

import icy.common.exception.UnsupportedFormatException;
import icy.file.FileUtil;
import icy.file.Loader;
import icy.file.SequenceFileImporter;
import icy.gui.component.ThumbnailComponent;
import icy.resource.ResourceUtil;
import icy.sequence.MetaDataUtil;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.IOException;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import loci.formats.ome.OMEXMLMetadataImpl;

public class LoaderOptionPanel extends JPanel
{
    private class PreviewUpdater extends Thread
    {
        final private String fileId;

        public PreviewUpdater(String fileId)
        {
            super("Image preview");

            this.fileId = fileId;
        }

        @Override
        public void run()
        {
            try
            {
                boolean metaDataDone = false;
                boolean thumbnailDone = false;

                preview.setImage(null);
                preview.setTitle("loading...");
                preview.setInfos("");
                preview.setInfos2("");

                final List<SequenceFileImporter> importers = Loader.getSequenceFileImporters(fileId);

                for (SequenceFileImporter importer : importers)
                {
                    try
                    {
                        if (importer.open(fileId, 0))
                        {
                            try
                            {
                                if (!metaDataDone)
                                {
                                    final OMEXMLMetadataImpl meta = importer.getMetaData();

                                    final int sizeC = MetaDataUtil.getSizeC(meta, 0);

                                    // load metadata first
                                    preview.setTitle(FileUtil.getFileName(fileId));
                                    preview.setInfos(MetaDataUtil.getSizeX(meta, 0) + " x "
                                            + MetaDataUtil.getSizeY(meta, 0) + " - " + MetaDataUtil.getSizeZ(meta, 0)
                                            + "Z x " + MetaDataUtil.getSizeT(meta, 0) + "T");
                                    preview.setInfos2(sizeC + ((sizeC > 1) ? " channels (" : " channel (")
                                            + MetaDataUtil.getDataType(meta, 0) + ")");

                                    metaDataDone = true;
                                }

                                if (!thumbnailDone)
                                {
                                    // then thumbnail
                                    preview.setImage(importer.getThumbnail(0));

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

                // cannot read metadata
                if (!metaDataDone)
                    preview.setTitle("Cannot read file");
                // cannot get thumbnail
                if (!thumbnailDone)
                    preview.setImage(ResourceUtil.ICON_DELETE);

            }
            catch (Throwable t)
            {
                // ignore
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
    ThumbnailComponent preview;
    private JPanel separateSeqPanel;
    private JCheckBox separateSeqCheck;
    private JLabel lblAutoDimension;
    private JCheckBox autoOrderCheck;

    // internals
    private boolean autoOrderEnable;
    private PreviewUpdater previewThread;

    /**
     * Create the panel.
     */
    public LoaderOptionPanel(boolean separate, boolean autoOrder)
    {
        super();

        autoOrderEnable = true;
        previewThread = null;
        initialize(separate, autoOrder);
    }

    private void initialize(boolean separate, boolean autoOrder)
    {
        setBorder(BorderFactory.createTitledBorder((Border) null));
        setLayout(new BorderLayout());

        preview = new ThumbnailComponent(false);

        add(preview, BorderLayout.CENTER);

        separateSeqPanel = new JPanel();
        add(separateSeqPanel, BorderLayout.SOUTH);
        GridBagLayout gbl_separateSeqPanel = new GridBagLayout();
        gbl_separateSeqPanel.columnWidths = new int[] {135, 21, 0};
        gbl_separateSeqPanel.rowHeights = new int[] {21, 0, 0};
        gbl_separateSeqPanel.columnWeights = new double[] {1.0, 0.0, Double.MIN_VALUE};
        gbl_separateSeqPanel.rowWeights = new double[] {0.0, 0.0, Double.MIN_VALUE};
        separateSeqPanel.setLayout(gbl_separateSeqPanel);
        GridBagConstraints gbc_lblLoadInSeparated = new GridBagConstraints();
        gbc_lblLoadInSeparated.anchor = GridBagConstraints.WEST;
        gbc_lblLoadInSeparated.insets = new Insets(0, 0, 5, 5);
        gbc_lblLoadInSeparated.gridx = 0;
        gbc_lblLoadInSeparated.gridy = 0;
        JLabel lblLoadInSeparated = new JLabel("Load in separated sequences");
        lblLoadInSeparated.setToolTipText("All images are opened in its own sequence");
        separateSeqPanel.add(lblLoadInSeparated, gbc_lblLoadInSeparated);

        // setting GUI
        separateSeqCheck = new JCheckBox();
        lblLoadInSeparated.setLabelFor(separateSeqCheck);
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
        gbc_separateSeqCheck.insets = new Insets(0, 0, 0, 0);
        gbc_separateSeqCheck.anchor = GridBagConstraints.NORTHWEST;
        gbc_separateSeqCheck.gridx = 1;
        gbc_separateSeqCheck.gridy = 0;
        separateSeqPanel.add(separateSeqCheck, gbc_separateSeqCheck);

        lblAutoDimension = new JLabel("Automatic ordering");
        lblAutoDimension.setToolTipText("Try to automatically set Z, T, C position of an image from their file name");
        GridBagConstraints gbc_lblAutoDimension = new GridBagConstraints();
        gbc_lblAutoDimension.anchor = GridBagConstraints.WEST;
        gbc_lblAutoDimension.insets = new Insets(0, 0, 0, 5);
        gbc_lblAutoDimension.gridx = 0;
        gbc_lblAutoDimension.gridy = 1;
        separateSeqPanel.add(lblAutoDimension, gbc_lblAutoDimension);

        autoOrderCheck = new JCheckBox("");
        lblAutoDimension.setLabelFor(autoOrderCheck);
        autoOrderCheck.setSelected(autoOrder);
        GridBagConstraints gbc_autoOrderCheck = new GridBagConstraints();
        gbc_autoOrderCheck.gridx = 1;
        gbc_autoOrderCheck.gridy = 1;

        separateSeqPanel.add(autoOrderCheck, gbc_autoOrderCheck);
    }

    void updateAutoOrderEnableState()
    {
        autoOrderCheck.setEnabled(autoOrderEnable && !isSeparateSequenceSelected());
    }

    ThumbnailComponent getPreview()
    {
        return preview;
    }

    public void setSeparateSequenceEnabled(boolean value)
    {
        separateSeqCheck.setEnabled(value);
    }

    public void setAutoOrderEnabled(boolean value)
    {
        autoOrderEnable = value;
        updateAutoOrderEnableState();
    }

    public boolean isSeparateSequenceSelected()
    {
        return separateSeqCheck.isSelected();
    }

    public boolean isAutoOrderSelected()
    {
        return autoOrderCheck.isSelected();
    }

    /**
     * Asynchronous preview refresh
     */
    public void updatePreview(String fileId)
    {
        // interrupt previous preview refresh
        cancelPreview();

        previewThread = new PreviewUpdater(fileId);
        previewThread.start();
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
}
