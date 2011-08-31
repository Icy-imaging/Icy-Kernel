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
package icy.math;

import icy.image.IcyBufferedImage;
import icy.type.TypeUtil;
import icy.type.collection.array.ArrayUtil;

import java.util.Arrays;

import edu.emory.mathcs.jtransforms.fft.DoubleFFT_2D;

/**
 * @author Stephane
 */
public class FFT
{
    /**
     * Compute the FFT of the given image and return it as 2 components image<br>
     * first component : module (amplitude)<br>
     * second component : phase<br>
     * 
     * @param image
     * @return IcyBufferedImage
     */
    public static IcyBufferedImage computeFFT(IcyBufferedImage image)
    {
        final int imgW = image.getWidth();
        final int imgH = image.getHeight();
        final int imgSizeC = image.getSizeC();
        final int imgLen = imgW * imgH;

        int w = (int) MathUtil.nextPow2(imgW - 1);
        int h = (int) MathUtil.nextPow2(imgH - 1);

        // we want square 2^n sized image
        if (w < h)
            w = h;
        else
            h = w;

        final int len = w * h;
        final DoubleFFT_2D fft = new DoubleFFT_2D(w, h);
        // use size x2 to store the imaginary part
        final double[] doubleData = new double[len * 2];
        final IcyBufferedImage result = new IcyBufferedImage(w, h, 2, TypeUtil.TYPE_DOUBLE);

        for (int c = 0; c < image.getSizeC(); c++)
        {
            int offsetIn = 0;
            int offsetOut = 0;
            // copy and convert data to double
            for (int y = 0; y < imgH; y++)
            {
                ArrayUtil.arrayToDoubleArray1D(image.getDataXY(c), offsetIn, doubleData, offsetOut, imgW,
                        image.isSignedDataType());
                // clear the part outside image
                Arrays.fill(doubleData, offsetOut + imgW, offsetOut + w, 0);
                offsetIn += imgW;
                offsetOut += w;
            }

            // compute FFT
            fft.realForwardFull(doubleData);
        }

        return result;
    }
}
