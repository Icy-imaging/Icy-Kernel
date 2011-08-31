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

import icy.canvas.IcyCanvas;
import icy.gui.dialog.MessageDialog;
import icy.main.Icy;
import icy.painter.AbstractPainter;
import icy.roi.ROI;
import icy.roi.ROI2D;
import icy.roi.ROI2DShape;
import icy.sequence.Sequence;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

/**
 * Helper to do crop operation on focused sequence
 * 
 * @author Stephane
 */
public class SequenceCropper extends AbstractPainter
{
    private Sequence seq;

    public SequenceCropper()
    {
        super();

        seq = Icy.getMainInterface().getFocusedSequence();

        if (seq == null)
            return;

        final ArrayList<ROI2D> rois = seq.getROI2Ds();
        final int size = rois.size();

        if (size == 0)
        {
            MessageDialog.showDialog("There is no ROI in the current sequence.\nCrop operation need a ROI.",
                    MessageDialog.INFORMATION_MESSAGE);
            return;
        }

        if (size == 1)
            crop(rois.get(0).getBounds2D());
        else
            seq.addPainter(this);
    }

    private ROI2D getSelectedROI()
    {
        final ROI roi = seq.getFocusedROI();

        if ((roi != null) && (roi instanceof ROI2D))
            return (ROI2D) roi;

        return null;
    }

    @Override
    public void mouseClick(MouseEvent e, Point2D imagePoint, IcyCanvas canvas)
    {
        final ROI2D selectedROI = getSelectedROI();

        if ((selectedROI != null) && (!selectedROI.getBounds2D().isEmpty()))
            crop(selectedROI.getBounds2D());

        // so cropper can be released
        seq.removePainter(this);
    }

    @Override
    public void paint(Graphics2D g, Sequence sequence, IcyCanvas canvas)
    {
        final Graphics2D g2 = (Graphics2D) g.create();

        g2.setColor(Color.darkGray);
        g2.drawString("Click on the edge of a ROI to perform the crop", 11, 21);
        g2.setColor(Color.white);
        g2.drawString("Click on the edge of a ROI to perform the crop", 10, 20);

        final ROI2D selectedROI = getSelectedROI();

        if (selectedROI != null)
        {
            g2.setColor(Color.yellow);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
            if (selectedROI instanceof ROI2DShape)
                g2.fill((ROI2DShape) selectedROI);
            else
                g2.fill(selectedROI.getBounds2D());
        }

        g2.dispose();
    }

    public void crop(Rectangle2D rect)
    {
        // get intersect rectangle
        final Rectangle2D adjustedRect = seq.getBounds().createIntersection(rect);

        final int x = (int) adjustedRect.getMinX();
        final int y = (int) adjustedRect.getMinY();
        final int w = (int) adjustedRect.getWidth();
        final int h = (int) adjustedRect.getHeight();

        Icy.addSequence(seq.getSubSequence(x, y, 0, 0, w, h, seq.getSizeZ(), seq.getSizeT()));
    }
}
