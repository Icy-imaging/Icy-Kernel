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
package icy.image;

import icy.image.lut.LUT;
import icy.math.Scaler;
import icy.system.SystemUtil;
import icy.system.thread.Processor;
import icy.system.thread.ThreadUtil;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * @author Stephane
 */
class ARGBImageBuilder
{
    private static final int BLOC_SIZE = 512 * 512;

    class BlockBuilder implements Runnable
    {
        /**
         * cached variables
         */
        private IcyBufferedImage image;
        private LUT lut;
        private int dest[];
        private int offset;
        private int length;
        private int numChannel;

        BlockBuilder(IcyBufferedImage image, LUT lut, int[] dest, int offset, int length)
        {
            super();

            this.image = image;
            // use internal lut if specified lut is null
            if (lut == null)
                this.lut = image.createCompatibleLUT(false);
            else
                this.lut = lut;
            this.dest = dest;
            this.offset = offset;
            this.length = length;

            numChannel = image.getSizeC();

            if (this.lut.getNumChannel() != numChannel)
                throw new IllegalArgumentException("ARGBImageBuilder.prepare(...): LUT.numChannel != IMAGE.numChannel");
        }

        @Override
        public void run()
        {
            int[][] componentValues = null;

            try
            {
                // get working buffer
                componentValues = requestBuffer(numChannel);

                if (componentValues != null)
                {
                    // update output image buffer
                    final Scaler[] scalers = lut.getScalers();
                    final boolean signed = image.getIcyColorModel().getDataType_().isSigned();

                    // scale component values
                    for (int comp = 0; comp < numChannel; comp++)
                        scalers[comp].scale(image.getDataXY(comp), offset, componentValues[comp], 0, length, signed);

                    // build ARGB destination buffer
                    lut.getColorSpace().fillARGBBuffer(componentValues, dest, offset, length);
                }
            }
            catch (Exception e)
            {
                // we just ignore any exceptions here as we can be in asynch process
            }
            finally
            {
                releaseBuffer(componentValues);
            }
        }
    }

    // processor
    private final Processor processor;
    // data buffer pool
    private final List<int[][]> buffers;

    /**
     * 
     */
    public ARGBImageBuilder()
    {
        super();

        if (SystemUtil.is32bits())
            processor = new Processor(Math.max(1, Math.min(SystemUtil.getNumberOfCPUs() - 1, 4)));
        else
            processor = new Processor(Math.max(1, Math.min(SystemUtil.getNumberOfCPUs() - 1, 16)));

        processor.setThreadName("ARGB Image builder");
        processor.setPriority(Processor.NORM_PRIORITY - 1);

        buffers = new ArrayList<int[][]>();
    }

    private static BufferedImage getImage(IcyBufferedImage in, BufferedImage out)
    {
        if ((out != null) && ImageUtil.sameSize(in, out))
            return out;

        return new BufferedImage(in.getWidth(), in.getHeight(), BufferedImage.TYPE_INT_ARGB);
    }

    int[][] requestBuffer(int numChannel)
    {
        if (numChannel <= 0)
            return null;

        synchronized (buffers)
        {
            for (int index = buffers.size() - 1; index >= 0; index--)
                if (buffers.get(index).length == numChannel)
                    return buffers.remove(index);
        }

        // allocate a new one
        return new int[numChannel][BLOC_SIZE];
    }

    void releaseBuffer(int[][] buffer)
    {
        if (buffer == null)
            return;

        // --> blocked
        synchronized (buffers)
        {
            buffers.add(buffer);
        }
    }

    /**
     * Convert the source {@link IcyBufferedImage} into the destination ARGB
     * {@link BufferedImage}<br>
     * If <code>out</code> is null then a new ARGB {@link BufferedImage} is returned.<br>
     * Note that output {@link BufferedImage} is fixed to ARGB type (TYPE_INT_ARGB) and the image
     * cannot be volatile accelerated.
     * 
     * @param image
     *        source image
     * @param lut
     *        {@link LUT} is used for color calculation (internal lut is used if null).
     * @param out
     *        destination image. Note that we access image data so it can't be volatile anymore
     *        which may result in slower drawing.
     */
    public BufferedImage buildARGBImage(IcyBufferedImage image, LUT lut, BufferedImage out)
    {
        // planar size
        final int imageSize = image.getSizeX() * image.getSizeY();
        final int step = imageSize / BLOC_SIZE;
        final BufferedImage result = getImage(image, out);
        // destination buffer
        final int[] dest = ((DataBufferInt) result.getRaster().getDataBuffer()).getData();
        final List<Future<?>> futures = new ArrayList<Future<?>>();

        int offset = 0;
        try
        {
            for (int i = 0; i < step; i++)
            {
                // build bloc
                futures.add(addBloc(image, lut, dest, offset, BLOC_SIZE));
                offset += BLOC_SIZE;
            }

            // last bloc
            if (offset < imageSize)
                futures.add(addBloc(image, lut, dest, offset, imageSize - offset));

            // wait until image is built
            waitCompletion(futures);
        }
        catch (IllegalArgumentException e)
        {
            // image has changed in the meantime, just ignore
        }

        // release working buffer memory
        synchronized (buffers)
        {
            buffers.clear();
        }

        return result;
    }

    /**
     * Convert the source {@link IcyBufferedImage} into an ARGB BufferedImage.
     * Note that output {@link BufferedImage} is not a volatile accelerated image (slower
     * drawing).<br>
     * Use {@link IcyBufferedImageUtil#toBufferedImage(IcyBufferedImage, int, LUT)} instead if you
     * want volatile accelerated image.
     * 
     * @param image
     *        source image
     * @param lut
     *        {@link LUT} is used for color calculation (internal lut is used if null).
     */
    public BufferedImage buildARGBImage(IcyBufferedImage image, LUT lut)
    {
        return buildARGBImage(image, lut, null);
    }

    private Future<?> addBloc(IcyBufferedImage image, LUT lut, int dest[], int offset, int length)
    {
        final BlockBuilder builder = new BlockBuilder(image, lut, dest, offset, length);
        Future<?> result = processor.submit(builder);

        // not accepted ? retry until it is accepted...
        while (result == null)
        {
            // wait a bit
            ThreadUtil.sleep(1);
            // and retry task submission
            result = processor.submit(builder);
        }

        return result;
    }

    private void waitCompletion(List<Future<?>> futures)
    {
        while (!futures.isEmpty())
        {
            // get last in queue
            final Future<?> f = futures.get(futures.size() - 1);

            try
            {
                // wait for it
                f.get();
            }
            catch (ExecutionException e)
            {
                // warning
                System.out.println("ARGBImageBuilder - Warning: " + e);
            }
            catch (InterruptedException e)
            {
                // ignore
            }

            // remove it
            futures.remove(f);
        }
    }

    /**
     * Returns <code>true</code> if the ARGB builder is processing an image.
     */
    public boolean isProcessing()
    {
        return processor.isProcessing();
    }

    /**
     * wait until all process ended
     */
    public void waitCompletion()
    {
        processor.waitAll();
    }
}
