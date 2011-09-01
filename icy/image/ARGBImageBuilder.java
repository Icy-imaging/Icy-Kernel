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
package icy.image;

import icy.image.lut.LUT;
import icy.math.Scaler;
import icy.system.SystemUtil;
import icy.system.thread.Processor;
import icy.system.thread.ThreadUtil;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

/**
 * @author Stephane
 */
class ARGBImageBuilder
{
    private static final int BLOC_SIZE = 256 * 256;
    private static final int PARALLEL_PROCESS = SystemUtil.getAvailableProcessors() * 2;

    private class BlockBuilder implements Runnable
    {
        /**
         * working buffer
         */
        int[][] componentValues;

        /**
         * processor
         */
        private Processor processor;

        /**
         * processing flag
         */
        boolean processing;

        /**
         * cached variables
         */
        private IcyBufferedImage image;
        private LUT lut;
        private int dest[];
        private int offset;
        private int length;
        private int numComponents;

        BlockBuilder()
        {
            super();

            // default
            componentValues = new int[0][0];

            // no queue
            processor = new Processor(1, 1);
            // don't change priority else our image won't never be build if
            // normal priority thread take all available time
            // processor.setPriority(Processor.MIN_PRIORITY + 1);
            processing = false;
        }

        private void prepare(IcyBufferedImage image, LUT lut, int[] dest, int offset, int length)
        {
            this.image = image;
            // use internal lut if specified lut is null
            if (lut == null)
                this.lut = image.getLUT();
            else
                this.lut = lut;
            this.dest = dest;
            this.offset = offset;
            this.length = length;

            numComponents = image.getNumComponents();

            if (lut.getNumComponents() != numComponents)
                throw new IllegalArgumentException("LUT.numComponents != IMAGE.numComponents");
        }

        private void clean()
        {
            // release reference
            image = null;
            lut = null;
            dest = null;
        }

        boolean build(IcyBufferedImage image, LUT lut, int dest[], int offset, int length)
        {
            synchronized (this)
            {
                if (processing)
                    return false;
                processing = true;
            }

            // prepare variables
            prepare(image, lut, dest, offset, length);
            // add task
            final boolean result = processor.addTask(this, false);

            // task not added ?
            if (!result)
            {
                synchronized (this)
                {
                    processing = false;
                }
            }

            return result;
        }

        @Override
        public void run()
        {
            try
            {
                // rebuild buffer if needed
                if (componentValues.length != numComponents)
                    componentValues = new int[numComponents][BLOC_SIZE];

                // update output image buffer
                final Scaler[] scalers = lut.getScalers();
                final boolean signed = image.getIcyColorModel().isSignedDataType();

                // scale component values
                for (int comp = 0; comp < numComponents; comp++)
                    scalers[comp].scale(image.getDataXY(comp), offset, componentValues[comp], 0, length, signed);

                // build ARGB destination buffer
                lut.getColorSpace().fillARGBBuffer(componentValues, dest, offset, length);
            }
            catch (Exception E)
            {
                // we just ignore any exceptions here as we can be in asynch process
            }
            finally
            {
                // clean up
                clean();

                synchronized (this)
                {
                    processing = false;
                }
            }
        }

    }

    // builders
    private final BlockBuilder builders[];

    /**
     * 
     */
    ARGBImageBuilder()
    {
        super();

        builders = new BlockBuilder[PARALLEL_PROCESS];
        for (int i = 0; i < PARALLEL_PROCESS; i++)
            builders[i] = new BlockBuilder();
    }

    private BufferedImage getImage(IcyBufferedImage in, BufferedImage out)
    {
        if ((out != null) && ImageUtil.sameSize(in, out))
            return out;

        return new BufferedImage(in.getWidth(), in.getHeight(), BufferedImage.TYPE_INT_ARGB);
    }

    synchronized BufferedImage buildARGBImage(IcyBufferedImage image, LUT lut, BufferedImage out)
    {
        // planar size
        final int imageSize = image.getSizeX() * image.getSizeY();
        final int step = imageSize / BLOC_SIZE;
        final BufferedImage result = getImage(image, out);
        // destination buffer
        final int[] dest = ((DataBufferInt) result.getRaster().getDataBuffer()).getData();

        int offset = 0;
        for (int i = 0; i < step; i++)
        {
            // build bloc
            sendBuild(image, lut, dest, offset, BLOC_SIZE);
            offset += BLOC_SIZE;
        }

        // last bloc
        if (offset < imageSize)
            sendBuild(image, lut, dest, offset, imageSize - offset);

        // wait until image is built
        waitCompletion();

        return result;
    }

    private void sendBuild(IcyBufferedImage image, LUT lut, int dest[], int offset, int length)
    {
        boolean done = false;

        while (!done)
        {
            final BlockBuilder builder = getAvailableBuilder();
            done = builder.build(image, lut, dest, offset, length);
        }
    }

    /**
     * Get first available builder, wait until we get one
     */
    private BlockBuilder getAvailableBuilder()
    {
        while (true)
        {
            for (BlockBuilder builder : builders)
                if (!builder.processing)
                    return builder;

            // allow other thread to process
            ThreadUtil.sleep(1);
        }
    }

    boolean isProcessing()
    {
        for (BlockBuilder builder : builders)
            if (builder.processing)
                return true;

        return false;
    }

    /**
     * wait until all process ended
     */
    private void waitCompletion()
    {
        while (isProcessing())
            ThreadUtil.sleep(1);
    }
}
