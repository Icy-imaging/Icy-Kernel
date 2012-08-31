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
package icy.gui.menu.tools;

import icy.gui.dialog.MessageDialog;
import icy.gui.viewer.Viewer;
import icy.main.Icy;
import icy.roi.ROI2D;
import icy.sequence.Sequence;
import icy.sequence.SequenceUtil;
import icy.system.thread.ThreadUtil;

import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

/**
 * Helper to do crop operation on focused sequence
 * 
 * @author Stephane
 */
public class SequenceCropper
{
    public SequenceCropper()
    {
        super();

        final Viewer v = Icy.getMainInterface().getFocusedViewer();

        if ((v == null) || (v.getSequence() == null))
            return;

        final Sequence seq = v.getSequence();

        ArrayList<ROI2D> rois = seq.getROI2Ds();
        int size = rois.size();

        if (size == 0)
        {
            MessageDialog.showDialog("There is no ROI in the current sequence.\nCrop operation need a ROI.",
                    MessageDialog.INFORMATION_MESSAGE);
            return;
        }
        else if (size > 1)
        {
            rois = seq.getSelectedROI2Ds();
            size = rois.size();

            if (size == 0)
            {
                MessageDialog.showDialog("You need to select a ROI to do the crop operation.",
                        MessageDialog.INFORMATION_MESSAGE);
                return;
            }
            else if (size > 1)
            {
                MessageDialog.showDialog("You must have only one selected ROI to do the crop operation.",
                        MessageDialog.INFORMATION_MESSAGE);
                return;
            }
        }

        crop(v, rois.get(0).getBounds());
    }

    public static void crop(final Viewer viewer, final Rectangle rect)
    {
        if ((viewer == null) || (viewer.getSequence() == null))
            return;

        final Sequence in = viewer.getSequence();

        // get intersect rectangle
        final Rectangle2D adjustedRect = in.getBounds().createIntersection(rect);

        ThreadUtil.bgRun(new Runnable()
        {
            @Override
            public void run()
            {
                // create output sequence
                final Sequence out = SequenceUtil.getSubSequence(in, (int) adjustedRect.getMinX(),
                        (int) adjustedRect.getMinY(), 0, 0, (int) adjustedRect.getWidth(),
                        (int) adjustedRect.getHeight(), in.getSizeZ(), in.getSizeT());

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
    }
}
