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
package plugins.test;

import icy.canvas.IcyCanvas;
import icy.image.IcyBufferedImage;
import icy.image.ImageUtil;
import icy.image.lut.LUT;
import icy.roi.ROI2D;
import icy.sequence.Sequence;
import icy.util.ColorUtil;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.Arrays;

/**
 * @author Stephane
 */
public class ROI2DEdge extends ROI2D
{
    protected class ROI2DEdgePainter extends ROI2DPainter
    {
        final Point2D mousePos;
        BufferedImage edgeImage;

        /**
         * 
         */
        public ROI2DEdgePainter()
        {
            super();

            mousePos = new Point2D.Double();
            edgeImage = null;
        }

        @Override
        public void mouseMove(MouseEvent e, Point2D imagePoint, IcyCanvas canvas)
        {
            super.mouseMove(e, imagePoint, canvas);

            updateEdge(canvas.getCurrentImage(), canvas.getLut(), imagePoint);
            mousePos.setLocation(imagePoint);

            // cause repaint
            changed();
        }

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
    public ROI2DEdge()
    {
        super();
    }

    @Override
    protected ROI2DEdgePainter createPainter()
    {
        return new ROI2DEdgePainter();
    }

    @Override
    public boolean canAddPoint()
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean canRemovePoint()
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean addPointAt(Point2D pos, boolean ctrl)
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean removePointAt(IcyCanvas canvas, Point2D imagePoint)
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    protected boolean removeSelectedPoint(IcyCanvas canvas, Point2D imagePoint)
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean hasSelectedPoint()
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isOver(IcyCanvas canvas, double x, double y)
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isOverPoint(IcyCanvas canvas, double x, double y)
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean contains(double x, double y)
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean contains(double x, double y, double w, double h)
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Rectangle2D getBounds2D()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean intersects(double x, double y, double w, double h)
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void translate(double dx, double dy)
    {
        // TODO Auto-generated method stub

    }

}
