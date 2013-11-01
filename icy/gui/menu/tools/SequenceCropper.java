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
package icy.gui.menu.tools;

import icy.gui.dialog.MessageDialog;
import icy.gui.viewer.Viewer;
import icy.main.Icy;
import icy.roi.ROI;
import icy.sequence.Sequence;
import icy.sequence.SequenceUtil;
import icy.system.thread.ThreadUtil;

import java.util.List;

/**
 * Helper to do crop operation on focused sequence
 * 
 * @author Stephane
 */
public class SequenceCropper
{
    private SequenceCropper()
    {
        super();
    }

    public static boolean doRoiCrop()
    {
        final Viewer viewer = Icy.getMainInterface().getActiveViewer();
        if (viewer == null)
            return false;

        final Sequence seq = viewer.getSequence();
        if (seq == null)
            return false;

        List<ROI> rois = seq.getROIs();
        int size = rois.size();

        if (size == 0)
        {
            MessageDialog.showDialog("There is no ROI in the current sequence.\nCrop operation need a ROI.",
                    MessageDialog.INFORMATION_MESSAGE);
            return false;
        }
        else if (size > 1)
        {
            rois = seq.getSelectedROIs();
            size = rois.size();

            if (size == 0)
            {
                MessageDialog.showDialog("You need to select a ROI to do the crop operation.",
                        MessageDialog.INFORMATION_MESSAGE);
                return false;
            }
            else if (size > 1)
            {
                MessageDialog.showDialog("You must have only one selected ROI to do the crop operation.",
                        MessageDialog.INFORMATION_MESSAGE);
                return false;
            }
        }

        return doRoiCrop(viewer, rois.get(0));
    }

    public static boolean doRoiCrop(final Viewer viewer, final ROI roi)
    {
        final Sequence seq = viewer.getSequence();
        if (seq == null)
            return false;

        ThreadUtil.bgRun(new Runnable()
        {
            @Override
            public void run()
            {

                // create output sequence
                final Sequence out = SequenceUtil.getSubSequence(seq, roi);

                ThreadUtil.invokeLater(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        // get output viewer
                        final Viewer vout = new Viewer(out);
                        // copy colormap from input viewer
                        vout.getLut().copyFrom(viewer.getLut());
                    }
                });
            }
        });

        return true;
    }
}
