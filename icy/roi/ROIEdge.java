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
package icy.roi;

import icy.canvas.IcyCanvas;
import icy.image.IcyBufferedImage;
import icy.image.ImageUtil;
import icy.image.lut.LUT;
import icy.sequence.Sequence;
import icy.util.ColorUtil;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.Arrays;

/**
 * @author Stephane
 */
public class ROIEdge extends ROI
{
    protected class ROIEdgePainter extends ROIPainter
    {
        final Point2D mousePos;
        BufferedImage edgeImage;

        /**
         * 
         */
        public ROIEdgePainter()
        {
            super();

            mousePos = new Point2D.Double();
            edgeImage = null;
        }

        /*
         * (non-Javadoc)
         * 
         * @see icy.painter.AbstractPainter#mouseMove(java.awt.event.MouseEvent,
         * java.awt.geom.Point2D, icy.canvas.IcyCanvas)
         */
        @Override
        public void mouseMove(MouseEvent e, Point2D imagePoint, IcyCanvas canvas)
        {
            super.mouseMove(e, imagePoint, canvas);

            updateEdge(canvas.getCurrentImage(), canvas.getLut(), imagePoint);
            mousePos.setLocation(imagePoint);

            // cause repaint
            changed();
        }

        /*
         * (non-Javadoc)
         * 
         * @see icy.painter.AbstractPainter#paint(java.awt.Graphics2D, icy.sequence.Sequence,
         * icy.canvas.IcyCanvas)
         */
        @Override
        public void paint(Graphics2D g, Sequence sequence, IcyCanvas canvas)
        {
            super.paint(g, sequence, canvas);

            g.setColor(Color.yellow);
            g.fillOval((int) mousePos.getX(), (int) mousePos.getY(), 10, 10);
            g.drawImage(edgeImage, null, 0, 0);
        }

        private void updateEdge(IcyBufferedImage image, LUT lut, Point2D pos)
        {
            prepareImage(image);

            final int[] data = ((DataBufferInt) edgeImage.getRaster().getDataBuffer()).getData();

            // clear image
            Arrays.fill(data, 0);

            final int x = (int) pos.getX();
            final int y = (int) pos.getY();
            final Color color = ColorUtil.getGrayColorLum(new Color(image.getRGB(x, y, lut)));

            edgeImage.setRGB(x, y, 0x80FFFFFF);

            expand(x, y, color, image, lut);
        }

        private void expand(int x, int y, Color color, IcyBufferedImage image, LUT lut)
        {
            if ((x < 1) || (y < 1) || (x >= image.getWidth() - 1) || (y >= image.getHeight() - 1))
                return;

            if (checkPixel(x - 1, y - 1, color, image, lut))
                expand(x - 1, y - 1, color, image, lut);
            if (checkPixel(x, y - 1, color, image, lut))
                expand(x, y - 1, color, image, lut);
            if (checkPixel(x + 1, y - 1, color, image, lut))
                expand(x + 1, y - 1, color, image, lut);
            if (checkPixel(x - 1, y, color, image, lut))
                expand(x - 1, y, color, image, lut);
            if (checkPixel(x + 1, y, color, image, lut))
                expand(x + 1, y, color, image, lut);
            if (checkPixel(x - 1, y + 1, color, image, lut))
                expand(x - 1, y + 1, color, image, lut);
            if (checkPixel(x, y + 1, color, image, lut))
                expand(x, y + 1, color, image, lut);
            if (checkPixel(x + 1, y + 1, color, image, lut))
                expand(x + 1, y + 1, color, image, lut);

        }

        private boolean checkPixel(int x, int y, Color color, IcyBufferedImage image, LUT lut)
        {
            if (edgeImage.getRGB(x, y) == 0)
            {
                final Color curColor = ColorUtil.getGrayColorLum(new Color(image.getRGB(x, y, lut)));
                if (Math.abs(curColor.getRed() - color.getRed()) < 10)
                {
                    edgeImage.setRGB(x, y, 0x80FFFFFF);
                    return true;
                }
            }

            return false;
        }

        private void prepareImage(IcyBufferedImage image)
        {
            // update image
            if ((edgeImage == null) || !ImageUtil.sameSize(edgeImage, image))
                edgeImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
        }

    }

    /**
     * 
     */
    public ROIEdge()
    {
        super();
    }

    @Override
    protected ROIEdgePainter createPainter()
    {
        return new ROIEdgePainter();
    }

}
