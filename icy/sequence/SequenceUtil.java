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
package icy.sequence;

import icy.common.listener.ProgressListener;
import icy.image.IcyBufferedImage;
import icy.image.IcyBufferedImageUtil;
import icy.image.IcyBufferedImageUtil.FilterType;
import icy.image.lut.LUT;
import icy.math.Scaler;
import icy.painter.Overlay;
import icy.roi.BooleanMask2D;
import icy.roi.ROI;
import icy.type.DataType;
import icy.type.collection.array.Array1DUtil;
import icy.type.rectangle.Rectangle5D;
import icy.util.OMEUtil;
import icy.util.StringUtil;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.swing.SwingConstants;

import loci.formats.ome.OMEXMLMetadataImpl;

/**
 * {@link Sequence} utilities class.<br>
 * You can find here tools to manipulate the sequence organization, its data type, its size...
 * 
 * @author Stephane
 */
public class SequenceUtil
{
    public static class AddZHelper
    {
        public static IcyBufferedImage getExtendedImage(Sequence sequence, int t, int z, int insertPosition,
                int numInsert, int copyLast)
        {
            if (z < insertPosition)
                return sequence.getImage(t, z);

            final int pos = z - insertPosition;

            // return new image
            if (pos < numInsert)
            {
                // return copy of previous image(s)
                if ((insertPosition > 0) && (copyLast > 0))
                {
                    // should be < insert position
                    final int duplicate = Math.min(insertPosition, copyLast);
                    final int baseReplicate = insertPosition - duplicate;

                    return sequence.getImage(t, baseReplicate + (pos % duplicate));
                }

                // return new empty image
                return new IcyBufferedImage(sequence.getSizeX(), sequence.getSizeY(), sequence.getSizeC(),
                        sequence.getDataType_());
            }

            return sequence.getImage(t, z - numInsert);
        }
    }

    public static class AddTHelper
    {
        public static IcyBufferedImage getExtendedImage(Sequence sequence, int t, int z, int insertPosition,
                int numInsert, int copyLast)
        {
            if (t < insertPosition)
                return sequence.getImage(t, z);

            final int pos = t - insertPosition;

            // return new image
            if (pos < numInsert)
            {
                // return copy of previous image(s)
                if ((insertPosition > 0) && (copyLast > 0))
                {
                    // should be < insert position
                    final int duplicate = Math.min(insertPosition, copyLast);
                    final int baseReplicate = insertPosition - duplicate;

                    return sequence.getImage(baseReplicate + (pos % duplicate), z);
                }

                // return new empty image
                return new IcyBufferedImage(sequence.getSizeX(), sequence.getSizeY(), sequence.getSizeC(),
                        sequence.getDataType_());
            }

            return sequence.getImage(t - numInsert, z);
        }
    }

    public static class MergeCHelper
    {
        private static IcyBufferedImage getImageFromSequenceInternal(Sequence seq, int t, int z, int c,
                boolean fillEmpty)
        {
            IcyBufferedImage img = seq.getImage(t, z, c);

            if ((img == null) && fillEmpty)
            {
                int curZ = z;

                // missing Z slice ?
                if (z >= seq.getSizeZ())
                {
                    // searching in previous slice
                    while ((img == null) && (curZ > 0))
                        img = seq.getImage(t, --curZ, c);
                }

                if (img == null)
                {
                    int curT = t;

                    // searching in previous frame
                    while ((img == null) && (curT > 0))
                        img = seq.getImage(--curT, z, c);
                }

                return img;
            }

            return img;
        }

        public static IcyBufferedImage getImage(Sequence[] sequences, int[] channels, int sizeX, int sizeY, int t,
                int z, boolean fillEmpty, boolean rescale)
        {
            if (sequences.length == 0)
                return null;

            final List<BufferedImage> images = new ArrayList<BufferedImage>();

            for (int i = 0; i < sequences.length; i++)
            {
                final Sequence seq = sequences[i];
                final int c = channels[i];

                IcyBufferedImage img = getImageFromSequenceInternal(seq, t, z, c, fillEmpty);

                // create an empty image
                if (img == null)
                    img = new IcyBufferedImage(sizeX, sizeY, 1, seq.getDataType_());
                // resize X and Y dimension if needed
                else if ((img.getSizeX() != sizeX) || (img.getSizeY() != sizeY))
                    img = IcyBufferedImageUtil.scale(img, sizeX, sizeY, rescale, SwingConstants.CENTER,
                            SwingConstants.CENTER, FilterType.BILINEAR);

                images.add(img);
            }

            return IcyBufferedImage.createFrom(images);
        }
    }

    public static class MergeZHelper
    {
        private static IcyBufferedImage getImageFromSequenceInternal(Sequence seq, int t, int z, boolean fillEmpty)
        {
            IcyBufferedImage img = seq.getImage(t, z);

            if ((img == null) && fillEmpty)
            {
                int curZ = z;

                // missing Z slice ?
                if (z >= seq.getSizeZ())
                {
                    // searching in previous slice
                    while ((img == null) && (curZ > 0))
                        img = seq.getImage(t, --curZ);
                }

                if (img == null)
                {
                    int curT = t;

                    // searching in previous frame
                    while ((img == null) && (curT > 0))
                        img = seq.getImage(--curT, z);
                }

                return img;
            }

            return img;
        }

        private static IcyBufferedImage getImageInternal(Sequence[] sequences, int t, int z, boolean interlaced,
                boolean fillEmpty)
        {
            int zRemaining = z;

            if (interlaced)
            {
                int zInd = 0;

                while (zRemaining >= 0)
                {
                    for (Sequence seq : sequences)
                    {
                        if (zInd < seq.getSizeZ())
                        {
                            if (zRemaining-- == 0)
                                return getImageFromSequenceInternal(seq, t, zInd, fillEmpty);
                        }
                    }

                    zInd++;
                }
            }
            else
            {
                for (Sequence seq : sequences)
                {
                    final int sizeZ = seq.getSizeZ();

                    // we found the sequence
                    if (zRemaining < sizeZ)
                        return getImageFromSequenceInternal(seq, t, zRemaining, fillEmpty);

                    zRemaining -= sizeZ;
                }
            }

            return null;
        }

        public static IcyBufferedImage getImage(Sequence[] sequences, int sizeX, int sizeY, int sizeC, int t, int z,
                boolean interlaced, boolean fillEmpty, boolean rescale)
        {
            final IcyBufferedImage origin = getImageInternal(sequences, t, z, interlaced, fillEmpty);
            IcyBufferedImage img = origin;

            if (img != null)
            {
                // resize X and Y dimension if needed
                if ((img.getSizeX() != sizeX) || (img.getSizeY() != sizeY))
                    img = IcyBufferedImageUtil.scale(img, sizeX, sizeY, rescale, SwingConstants.CENTER,
                            SwingConstants.CENTER, FilterType.BILINEAR);

                final int imgSizeC = img.getSizeC();

                // resize C dimension if needed
                if (imgSizeC < sizeC)
                    return IcyBufferedImageUtil.addChannels(img, imgSizeC, sizeC - imgSizeC);

                if (img == origin)
                    return img;
            }

            return img;
        }
    }

    public static class MergeTHelper
    {
        private static IcyBufferedImage getImageFromSequenceInternal(Sequence seq, int t, int z, boolean fillEmpty)
        {
            IcyBufferedImage img = seq.getImage(t, z);

            if ((img == null) && fillEmpty)
            {
                int curT = t;

                // missing T frame?
                if (t >= seq.getSizeT())
                {
                    // searching in previous frame
                    while ((img == null) && (curT > 0))
                        img = seq.getImage(--curT, z);
                }

                if (img == null)
                {
                    int curZ = z;

                    // searching in previous slice
                    while ((img == null) && (curZ > 0))
                        img = seq.getImage(t, --curZ);
                }

                return img;
            }

            return img;
        }

        private static IcyBufferedImage getImageInternal(Sequence[] sequences, int t, int z, boolean interlaced,
                boolean fillEmpty)
        {
            int tRemaining = t;

            if (interlaced)
            {
                int tInd = 0;

                while (tRemaining >= 0)
                {
                    for (Sequence seq : sequences)
                    {
                        if (tInd < seq.getSizeT())
                        {
                            if (tRemaining-- == 0)
                                return getImageFromSequenceInternal(seq, tInd, z, fillEmpty);
                        }
                    }

                    tInd++;
                }
            }
            else
            {
                for (Sequence seq : sequences)
                {
                    final int sizeT = seq.getSizeT();

                    // we found the sequence
                    if (tRemaining < sizeT)
                        return getImageFromSequenceInternal(seq, tRemaining, z, fillEmpty);

                    tRemaining -= sizeT;
                }
            }

            return null;
        }

        public static IcyBufferedImage getImage(Sequence[] sequences, int sizeX, int sizeY, int sizeC, int t, int z,
                boolean interlaced, boolean fillEmpty, boolean rescale)
        {
            final IcyBufferedImage origin = getImageInternal(sequences, t, z, interlaced, fillEmpty);
            IcyBufferedImage img = origin;

            if (img != null)
            {
                // resize X and Y dimension if needed
                if ((img.getSizeX() != sizeX) || (img.getSizeY() != sizeY))
                    img = IcyBufferedImageUtil.scale(img, sizeX, sizeY, rescale, SwingConstants.CENTER,
                            SwingConstants.CENTER, FilterType.BILINEAR);

                final int imgSizeC = img.getSizeC();

                // resize C dimension if needed
                if (imgSizeC < sizeC)
                    return IcyBufferedImageUtil.addChannels(img, imgSizeC, sizeC - imgSizeC);

                if (img == origin)
                    return img;
            }

            return img;
        }
    }

    public static class AdjustZTHelper
    {
        public static IcyBufferedImage getImage(Sequence sequence, int t, int z, int newSizeZ, int newSizeT,
                boolean reverseOrder)
        {
            final int sizeZ = sequence.getSizeZ();
            final int sizeT = sequence.getSizeT();

            // out of range
            if ((t >= newSizeT) || (z >= newSizeZ))
                return null;

            final int index;

            // calculate index of wanted image
            if (reverseOrder)
                index = (z * newSizeT) + t;
            else
                index = (t * newSizeZ) + z;

            final int tOrigin = index / sizeZ;
            final int zOrigin = index % sizeZ;

            // bounding --> return new image
            if (tOrigin >= sizeT)
                return new IcyBufferedImage(sequence.getSizeX(), sequence.getSizeY(), sequence.getSizeC(),
                        sequence.getDataType_());

            return sequence.getImage(tOrigin, zOrigin);
        }
    }

    /**
     * Add one or severals frames at position t.
     * 
     * @param t
     *        Position where to add frame(s)
     * @param num
     *        Number of frame to add
     * @param copyLast
     *        Number of last frame(s) to copy to fill added frames.<br>
     *        0 means that new frames are empty.<br>
     *        1 means we duplicate the last frame.<br>
     *        2 means we duplicate the two last frames.<br>
     *        and so on...
     */
    public static void addT(Sequence sequence, int t, int num, int copyLast)
    {
        final int sizeZ = sequence.getSizeZ();
        final int sizeT = sequence.getSizeT();

        sequence.beginUpdate();
        try
        {
            moveT(sequence, t, sizeT - 1, num);

            for (int i = 0; i < num; i++)
                for (int z = 0; z < sizeZ; z++)
                    sequence.setImage(t + i, z, IcyBufferedImageUtil.getCopy(AddTHelper.getExtendedImage(sequence, t
                            + i, z, t, num, copyLast)));
        }
        finally
        {
            sequence.endUpdate();
        }
    }

    /**
     * Add one or severals frames at position t.
     * 
     * @param t
     *        Position where to add frame(s)
     * @param num
     *        Number of frame to add
     */
    public static void addT(Sequence sequence, int t, int num)
    {
        addT(sequence, t, num, 0);
    }

    /**
     * Add one or severals frames at position t.
     * 
     * @param num
     *        Number of frame to add
     */
    public static void addT(Sequence sequence, int num)
    {
        addT(sequence, sequence.getSizeT(), num, 0);
    }

    /**
     * Add one or severals frames at position t.
     * 
     * @param num
     *        Number of frame to add
     * @param copyLast
     *        If true then the last frame is copied in added frames.
     */
    public static void addT(Sequence sequence, int num, boolean copyLast)
    {
        addT(sequence, sequence.getSizeT(), num, 0);
    }

    /**
     * Exchange 2 frames position on the sequence.
     */
    public static void swapT(Sequence sequence, int t1, int t2)
    {
        final int sizeT = sequence.getSizeT();

        if ((t1 < 0) || (t2 < 0) || (t1 >= sizeT) || (t2 >= sizeT))
            return;

        // get volume images at position t1 & t2
        final VolumetricImage vi1 = sequence.getVolumetricImage(t1);
        final VolumetricImage vi2 = sequence.getVolumetricImage(t2);

        sequence.beginUpdate();
        try
        {
            // start by removing old volume image (if any)
            sequence.removeAllImages(t1);
            sequence.removeAllImages(t2);

            // safe volume image copy (TODO : check if we can't direct set volume image internally)
            if (vi1 != null)
            {
                final Map<Integer, IcyBufferedImage> images = vi1.getImages();

                // copy images of volume image 1 at position t2
                for (Entry<Integer, IcyBufferedImage> entry : images.entrySet())
                    sequence.setImage(t2, entry.getKey().intValue(), entry.getValue());
            }
            if (vi2 != null)
            {
                final Map<Integer, IcyBufferedImage> images = vi2.getImages();

                // copy images of volume image 2 at position t1
                for (Entry<Integer, IcyBufferedImage> entry : images.entrySet())
                    sequence.setImage(t1, entry.getKey().intValue(), entry.getValue());
            }
        }
        finally
        {
            sequence.endUpdate();
        }
    }

    /**
     * Modify frame position.<br>
     * The previous frame present at <code>newT</code> position is lost.
     * 
     * @param sequence
     * @param t
     *        current t position
     * @param newT
     *        wanted t position
     */
    public static void moveT(Sequence sequence, int t, int newT)
    {
        final int sizeT = sequence.getSizeT();

        if ((t < 0) || (t >= sizeT) || (newT < 0) || (t == newT))
            return;

        // get volume image at position t
        final VolumetricImage vi = sequence.getVolumetricImage(t);

        sequence.beginUpdate();
        try
        {
            // remove volume image (if any) at position newT
            sequence.removeAllImages(newT);

            if (vi != null)
            {
                final TreeMap<Integer, IcyBufferedImage> images = vi.getImages();

                // copy images of volume image at position newT
                for (Entry<Integer, IcyBufferedImage> entry : images.entrySet())
                    sequence.setImage(newT, entry.getKey().intValue(), entry.getValue());

                // remove volume image at position t
                sequence.removeAllImages(t);
            }
        }
        finally
        {
            sequence.endUpdate();
        }
    }

    /**
     * Modify T position of a range of frame by the specified offset
     * 
     * @param sequence
     * @param from
     *        start of range (t position)
     * @param to
     *        end of range (t position)
     * @param offset
     *        position shift
     */
    public static void moveT(Sequence sequence, int from, int to, int offset)
    {
        sequence.beginUpdate();
        try
        {
            if (offset > 0)
            {
                for (int t = to; t >= from; t--)
                    moveT(sequence, t, t + offset);
            }
            else
            {
                for (int t = from; t <= to; t++)
                    moveT(sequence, t, t + offset);
            }
        }
        finally
        {
            sequence.endUpdate();
        }
    }

    /**
     * Remove a frame at position t.
     * 
     * @param sequence
     * @param t
     */
    public static void removeT(Sequence sequence, int t)
    {
        final int sizeT = sequence.getSizeT();

        if ((t < 0) || (t >= sizeT))
            return;

        sequence.removeAllImages(t);
    }

    /**
     * Remove a frame at position t and shift all the further t by -1.
     * 
     * @param sequence
     * @param t
     */
    public static void removeTAndShift(Sequence sequence, int t)
    {
        final int sizeT = sequence.getSizeT();

        if ((t < 0) || (t >= sizeT))
            return;

        sequence.beginUpdate();
        try
        {
            removeT(sequence, t);
            moveT(sequence, t + 1, sizeT - 1, -1);
        }
        finally
        {
            sequence.endUpdate();
        }
    }

    /**
     * Reverse T frames order.
     */
    public static void reverseT(Sequence sequence)
    {
        final int sizeT = sequence.getSizeT();
        final int sizeZ = sequence.getSizeZ();

        final Sequence save = new Sequence();

        save.beginUpdate();
        try
        {
            for (int t = 0; t < sizeT; t++)
                for (int z = 0; z < sizeZ; z++)
                    save.setImage(t, z, sequence.getImage(t, z));
        }
        finally
        {
            save.endUpdate();
        }

        sequence.beginUpdate();
        try
        {
            sequence.removeAllImages();

            for (int t = 0; t < sizeT; t++)
                for (int z = 0; z < sizeZ; z++)
                    sequence.setImage(sizeT - (t + 1), z, save.getImage(t, z));
        }
        finally
        {
            sequence.endUpdate();
        }
    }

    /**
     * Add one or severals slices at position z.
     * 
     * @param z
     *        Position where to add slice(s)
     * @param num
     *        Number of slice to add
     * @param copyLast
     *        Number of last slice(s) to copy to fill added slices.<br>
     *        0 means that new slices are empty.<br>
     *        1 means we duplicate the last slice.<br>
     *        2 means we duplicate the two last slices.<br>
     *        and so on...
     */
    public static void addZ(Sequence sequence, int z, int num, int copyLast)
    {
        final int sizeZ = sequence.getSizeZ();
        final int sizeT = sequence.getSizeT();

        sequence.beginUpdate();
        try
        {
            moveZ(sequence, z, sizeZ - 1, num);

            for (int i = 0; i < num; i++)
                for (int t = 0; t < sizeT; t++)
                    sequence.setImage(t, z + i, IcyBufferedImageUtil.getCopy(AddZHelper.getExtendedImage(sequence, t, z
                            + i, z, num, copyLast)));
        }
        finally
        {
            sequence.endUpdate();
        }
    }

    /**
     * Add one or severals slices at position z.
     * 
     * @param z
     *        Position where to add slice(s)
     * @param num
     *        Number of slice to add
     */
    public static void addZ(Sequence sequence, int z, int num)
    {
        addZ(sequence, z, num, 0);
    }

    /**
     * Add one or severals slices at position z.
     * 
     * @param num
     *        Number of slice to add
     */
    public static void addZ(Sequence sequence, int num)
    {
        addZ(sequence, sequence.getSizeZ(), num, 0);
    }

    /**
     * Add one or severals slices at position z.
     * 
     * @param num
     *        Number of slice to add
     * @param copyLast
     *        If true then the last slice is copied in added slices.
     */
    public static void addZ(Sequence sequence, int num, boolean copyLast)
    {
        addZ(sequence, sequence.getSizeZ(), num, 0);
    }

    /**
     * Exchange 2 slices position on the sequence.
     */
    public static void swapZ(Sequence sequence, int z1, int z2)
    {
        final int sizeZ = sequence.getSizeZ();
        final int sizeT = sequence.getSizeT();

        if ((z1 < 0) || (z2 < 0) || (z1 >= sizeZ) || (z2 >= sizeZ))
            return;

        sequence.beginUpdate();
        try
        {
            for (int t = 0; t < sizeT; t++)
            {
                final IcyBufferedImage image1 = sequence.getImage(t, z1);
                final IcyBufferedImage image2 = sequence.getImage(t, z2);

                // set image at new position
                if (image1 != null)
                    sequence.setImage(t, z2, image1);
                else
                    sequence.removeImage(t, z2);
                if (image2 != null)
                    sequence.setImage(t, z1, image2);
                else
                    sequence.removeImage(t, z1);
            }
        }
        finally
        {
            sequence.endUpdate();
        }
    }

    /**
     * Modify slice position.<br>
     * The previous slice present at <code>newZ</code> position is lost.
     * 
     * @param sequence
     * @param z
     *        current z position
     * @param newZ
     *        wanted z position
     */
    public static void moveZ(Sequence sequence, int z, int newZ)
    {
        final int sizeZ = sequence.getSizeZ();
        final int sizeT = sequence.getSizeT();

        if ((z < 0) || (z >= sizeZ) || (newZ < 0) || (z == newZ))
            return;

        sequence.beginUpdate();
        try
        {
            for (int t = 0; t < sizeT; t++)
            {
                final IcyBufferedImage image = sequence.getImage(t, z);

                if (image != null)
                {
                    // set image at new position
                    sequence.setImage(t, newZ, image);
                    // and remove image at old position z
                    sequence.removeImage(t, z);
                }
                else
                    // just set null image at new position (equivalent to no image)
                    sequence.removeImage(t, newZ);
            }
        }
        finally
        {
            sequence.endUpdate();
        }
    }

    /**
     * Modify Z position of a range of slice by the specified offset
     * 
     * @param sequence
     * @param from
     *        start of range (z position)
     * @param to
     *        end of range (z position)
     * @param offset
     *        position shift
     */
    public static void moveZ(Sequence sequence, int from, int to, int offset)
    {
        sequence.beginUpdate();
        try
        {
            if (offset > 0)
            {
                for (int z = to; z >= from; z--)
                    moveZ(sequence, z, z + offset);
            }
            else
            {
                for (int z = from; z <= to; z++)
                    moveZ(sequence, z, z + offset);
            }
        }
        finally
        {
            sequence.endUpdate();
        }
    }

    /**
     * Remove a slice at position Z.
     * 
     * @param sequence
     * @param z
     */
    public static void removeZ(Sequence sequence, int z)
    {
        final int sizeZ = sequence.getSizeZ();

        if ((z < 0) || (z >= sizeZ))
            return;

        sequence.beginUpdate();
        try
        {
            final int maxT = sequence.getSizeT();

            for (int t = 0; t < maxT; t++)
                sequence.removeImage(t, z);
        }
        finally
        {
            sequence.endUpdate();
        }
    }

    /**
     * Remove a slice at position t and shift all the further t by -1.
     * 
     * @param sequence
     * @param z
     */
    public static void removeZAndShift(Sequence sequence, int z)
    {
        final int sizeZ = sequence.getSizeZ();

        if ((z < 0) || (z >= sizeZ))
            return;

        sequence.beginUpdate();
        try
        {
            removeZ(sequence, z);
            moveZ(sequence, z + 1, sizeZ - 1, -1);
        }
        finally
        {
            sequence.endUpdate();
        }
    }

    /**
     * Reverse Z slices order.
     */
    public static void reverseZ(Sequence sequence)
    {
        final int sizeT = sequence.getSizeT();
        final int sizeZ = sequence.getSizeZ();

        final Sequence save = new Sequence();

        save.beginUpdate();
        try
        {
            for (int t = 0; t < sizeT; t++)
                for (int z = 0; z < sizeZ; z++)
                    save.setImage(t, z, sequence.getImage(t, z));
        }
        finally
        {
            save.endUpdate();
        }

        sequence.beginUpdate();
        try
        {
            sequence.removeAllImages();

            for (int t = 0; t < sizeT; t++)
                for (int z = 0; z < sizeZ; z++)
                    sequence.setImage(t, sizeZ - (z + 1), save.getImage(t, z));
        }
        finally
        {
            sequence.endUpdate();
        }
    }

    /**
     * Set all images of the sequence in T dimension.
     */
    public static void convertToTime(Sequence sequence)
    {
        sequence.beginUpdate();
        try
        {
            final List<IcyBufferedImage> images = sequence.getAllImage();

            sequence.removeAllImages();
            for (int i = 0; i < images.size(); i++)
                sequence.setImage(i, 0, images.get(i));
        }
        finally
        {
            sequence.endUpdate();
        }
    }

    /**
     * Set all images of the sequence in Z dimension.
     */
    public static void convertToStack(Sequence sequence)
    {
        sequence.beginUpdate();
        try
        {
            final List<IcyBufferedImage> images = sequence.getAllImage();

            sequence.removeAllImages();
            for (int i = 0; i < images.size(); i++)
                sequence.setImage(0, i, images.get(i));
        }
        finally
        {
            sequence.endUpdate();
        }
    }

    /**
     * @deprecated Use {@link #convertToStack(Sequence)} instead.
     */
    @Deprecated
    public static void convertToVolume(Sequence sequence)
    {
        convertToStack(sequence);
    }

    /**
     * Remove the specified channel from the source sequence.
     * 
     * @param source
     *        Source sequence
     * @param channel
     *        Channel index to remove
     */
    public static void removeChannel(Sequence source, int channel)
    {
        final int sizeC = source.getSizeC();

        if (channel >= sizeC)
            return;

        final int[] keep = new int[sizeC - 1];

        int i = 0;
        for (int c = 0; c < sizeC; c++)
            if (c != channel)
                keep[i++] = c;

        final Sequence tmp = extractChannels(source, keep);

        source.beginUpdate();
        try
        {
            // we need to clear the source sequence to change its type
            source.removeAllImages();

            // get back all images
            for (int t = 0; t < tmp.getSizeT(); t++)
                for (int z = 0; z < tmp.getSizeZ(); z++)
                    source.setImage(t, z, tmp.getImage(t, z));

            // get back modified metadata
            source.setMetaData(tmp.getMetadata());
            // and colormaps
            for (int c = 0; c < tmp.getSizeC(); c++)
                source.setDefaultColormap(c, tmp.getDefaultColorMap(c), false);
        }
        finally
        {
            source.endUpdate();
        }
    }

    /**
     * Returns the max size of specified dimension for the given sequences.
     */
    public static int getMaxDim(Sequence[] sequences, DimensionId dim)
    {
        int result = 0;

        for (Sequence seq : sequences)
        {
            switch (dim)
            {
                case X:
                    result = Math.max(result, seq.getSizeX());
                    break;
                case Y:
                    result = Math.max(result, seq.getSizeY());
                    break;
                case C:
                    result = Math.max(result, seq.getSizeC());
                    break;
                case Z:
                    result = Math.max(result, seq.getSizeZ());
                    break;
                case T:
                    result = Math.max(result, seq.getSizeT());
                    break;
            }
        }

        return result;
    }

    /**
     * Create and returns a new sequence by concatenating all given sequences on C dimension.
     * 
     * @param sequences
     *        Sequences to concatenate (use array order).
     * @param channels
     *        Selected channel for each sequence (<code>channels.length = sequences.length</code>)<br>
     *        If you want to select 2 or more channels from a sequence, just duplicate the sequence
     *        entry in the <code>sequences</code> parameter :</code><br>
     *        <code>sequences[n] = Sequence1; channels[n] = 0;</code><br>
     *        <code>sequences[n+1] = Sequence1; channels[n+1] = 2;</code><br>
     *        <code>...</code>
     * @param fillEmpty
     *        Replace empty image by the previous non empty one.
     * @param rescale
     *        Images are scaled to all fit in the same XY dimension.
     * @param pl
     *        ProgressListener to indicate processing progress.
     */
    public static Sequence concatC(Sequence[] sequences, int[] channels, boolean fillEmpty, boolean rescale,
            ProgressListener pl)
    {
        final int sizeX = getMaxDim(sequences, DimensionId.X);
        final int sizeY = getMaxDim(sequences, DimensionId.Y);
        final int sizeZ = getMaxDim(sequences, DimensionId.Z);
        final int sizeT = getMaxDim(sequences, DimensionId.T);

        final Sequence result = new Sequence();

        if (sequences.length > 0)
            result.setMetaData(OMEUtil.createOMEMetadata(sequences[0].getMetadata()));
        result.setName("C Merge");

        int ind = 0;
        for (int t = 0; t < sizeT; t++)
        {
            for (int z = 0; z < sizeZ; z++)
            {
                if (pl != null)
                    pl.notifyProgress(ind, sizeT * sizeZ);

                result.setImage(t, z,
                        MergeCHelper.getImage(sequences, channels, sizeX, sizeY, t, z, fillEmpty, rescale));

                ind++;
            }
        }

        int c = 0;
        for (Sequence seq : sequences)
        {
            for (int sc = 0; sc < seq.getSizeC(); sc++, c++)
            {
                final String channelName = seq.getChannelName(sc);

                // not default channel name --> we keep it
                if (!StringUtil.equals(seq.getDefaultChannelName(sc), channelName))
                    result.setChannelName(c, channelName);
            }
        }

        return result;
    }

    /**
     * Create and returns a new sequence by concatenating all given sequences on C dimension.
     * 
     * @param sequences
     *        Sequences to concatenate (use array order).
     * @param fillEmpty
     *        Replace empty image by the previous non empty one.
     * @param rescale
     *        Images are scaled to all fit in the same XY dimension.
     * @param pl
     *        ProgressListener to indicate processing progress.
     */
    public static Sequence concatC(Sequence[] sequences, boolean fillEmpty, boolean rescale, ProgressListener pl)
    {
        final int channels[] = new int[sequences.length];
        Arrays.fill(channels, -1);
        return concatC(sequences, channels, fillEmpty, rescale, pl);
    }

    /**
     * Create and returns a new sequence by concatenating all given sequences on C dimension.
     * 
     * @param sequences
     *        Sequences to concatenate (use array order).
     * @param fillEmpty
     *        Replace empty image by the previous non empty one.
     * @param rescale
     *        Images are scaled to all fit in the same XY dimension.
     */
    public static Sequence concatC(Sequence[] sequences, boolean fillEmpty, boolean rescale)
    {
        return concatC(sequences, fillEmpty, rescale, null);
    }

    /**
     * Create and returns a new sequence by concatenating all given sequences on C dimension.
     * 
     * @param sequences
     *        Sequences to concatenate (use array order).
     */
    public static Sequence concatC(Sequence[] sequences)
    {
        return concatC(sequences, true, false, null);
    }

    /**
     * Create and returns a new sequence by concatenating all given sequences on Z dimension.
     * 
     * @param sequences
     *        Sequences to concatenate (use array order).
     * @param interlaced
     *        Interlace images.<br>
     *        normal : 1,1,1,2,2,2,3,3,..<br>
     *        interlaced : 1,2,3,1,2,3,..<br>
     * @param fillEmpty
     *        Replace empty image by the previous non empty one.
     * @param rescale
     *        Images are scaled to all fit in the same XY dimension.
     * @param pl
     *        ProgressListener to indicate processing progress.
     */
    public static Sequence concatZ(Sequence[] sequences, boolean interlaced, boolean fillEmpty, boolean rescale,
            ProgressListener pl)
    {
        final int sizeX = getMaxDim(sequences, DimensionId.X);
        final int sizeY = getMaxDim(sequences, DimensionId.Y);
        final int sizeC = getMaxDim(sequences, DimensionId.C);
        final int sizeT = getMaxDim(sequences, DimensionId.T);
        int sizeZ = 0;

        for (Sequence seq : sequences)
            sizeZ += seq.getSizeZ();

        final Sequence result = new Sequence();

        if (sequences.length > 0)
            result.setMetaData(OMEUtil.createOMEMetadata(sequences[0].getMetadata()));
        result.setName("Z Merge");

        int ind = 0;
        for (int t = 0; t < sizeT; t++)
        {
            for (int z = 0; z < sizeZ; z++)
            {
                if (pl != null)
                    pl.notifyProgress(ind, sizeT * sizeZ);

                result.setImage(t, z, IcyBufferedImageUtil.getCopy(MergeZHelper.getImage(sequences, sizeX, sizeY,
                        sizeC, t, z, interlaced, fillEmpty, rescale)));

                ind++;
            }
        }

        return result;
    }

    /**
     * Create and returns a new sequence by concatenating all given sequences on Z dimension.
     * 
     * @param sequences
     *        Sequences to concatenate (use array order).
     * @param interlaced
     *        Interlace images.<br>
     *        normal : 1,1,1,2,2,2,3,3,..<br>
     *        interlaced : 1,2,3,1,2,3,..<br>
     * @param fillEmpty
     *        Replace empty image by the previous non empty one.
     * @param rescale
     *        Images are scaled to all fit in the same XY dimension.
     */
    public static Sequence concatZ(Sequence[] sequences, boolean interlaced, boolean fillEmpty, boolean rescale)
    {
        return concatZ(sequences, interlaced, fillEmpty, rescale, null);

    }

    /**
     * Create and returns a new sequence by concatenating all given sequences on Z dimension.
     * 
     * @param sequences
     *        Sequences to concatenate (use array order).
     */
    public static Sequence concatZ(Sequence[] sequences)
    {
        return concatZ(sequences, false, true, false, null);
    }

    /**
     * Create and returns a new sequence by concatenating all given sequences on T dimension.
     * 
     * @param sequences
     *        sequences to concatenate (use array order).
     * @param interlaced
     *        interlace images.<br>
     *        normal : 1,1,1,2,2,2,3,3,..<br>
     *        interlaced : 1,2,3,1,2,3,..<br>
     * @param fillEmpty
     *        replace empty image by the previous non empty one.
     * @param rescale
     *        Images are scaled to all fit in the same XY dimension.
     * @param pl
     *        ProgressListener to indicate processing progress.
     */
    public static Sequence concatT(Sequence[] sequences, boolean interlaced, boolean fillEmpty, boolean rescale,
            ProgressListener pl)
    {
        final int sizeX = getMaxDim(sequences, DimensionId.X);
        final int sizeY = getMaxDim(sequences, DimensionId.Y);
        final int sizeC = getMaxDim(sequences, DimensionId.C);
        final int sizeZ = getMaxDim(sequences, DimensionId.Z);
        int sizeT = 0;

        for (Sequence seq : sequences)
            sizeT += seq.getSizeT();

        final Sequence result = new Sequence();

        if (sequences.length > 0)
            result.setMetaData(OMEUtil.createOMEMetadata(sequences[0].getMetadata()));
        result.setName("T Merge");

        int ind = 0;
        for (int t = 0; t < sizeT; t++)
        {
            for (int z = 0; z < sizeZ; z++)
            {
                if (pl != null)
                    pl.notifyProgress(ind, sizeT * sizeZ);

                result.setImage(t, z, IcyBufferedImageUtil.getCopy(MergeTHelper.getImage(sequences, sizeX, sizeY,
                        sizeC, t, z, interlaced, fillEmpty, rescale)));

                ind++;
            }
        }

        return result;
    }

    /**
     * Create and returns a new sequence by concatenating all given sequences on T dimension.
     * 
     * @param sequences
     *        Sequences to concatenate (use array order).
     * @param interlaced
     *        Interlace images.<br>
     *        normal : 1,1,1,2,2,2,3,3,..<br>
     *        interlaced : 1,2,3,1,2,3,..<br>
     * @param fillEmpty
     *        Replace empty image by the previous non empty one.
     * @param rescale
     *        Images are scaled to all fit in the same XY dimension.
     */
    public static Sequence concatT(Sequence[] sequences, boolean interlaced, boolean fillEmpty, boolean rescale)
    {
        return concatT(sequences, interlaced, fillEmpty, rescale, null);

    }

    /**
     * Create and returns a new sequence by concatenating all given sequences on T dimension.
     * 
     * @param sequences
     *        Sequences to concatenate (use array order).
     */
    public static Sequence concatT(Sequence[] sequences)
    {
        return concatT(sequences, false, true, false, null);
    }

    /**
     * Adjust Z and T dimension of the sequence.
     * 
     * @param reverseOrder
     *        Means that images are T-Z ordered instead of Z-T ordered
     * @param newSizeZ
     *        New Z size of the sequence
     * @param newSizeT
     *        New T size of the sequence
     */
    public static void adjustZT(Sequence sequence, int newSizeZ, int newSizeT, boolean reverseOrder)
    {
        final int sizeZ = sequence.getSizeZ();
        final int sizeT = sequence.getSizeT();

        final Sequence tmp = new Sequence();

        tmp.beginUpdate();
        sequence.beginUpdate();
        try
        {
            try
            {
                for (int t = 0; t < sizeT; t++)
                {
                    for (int z = 0; z < sizeZ; z++)
                    {
                        tmp.setImage(t, z, sequence.getImage(t, z));
                        sequence.removeImage(t, z);
                    }
                }
            }
            finally
            {
                tmp.endUpdate();
            }

            for (int t = 0; t < newSizeT; t++)
                for (int z = 0; z < newSizeZ; z++)
                    sequence.setImage(t, z, AdjustZTHelper.getImage(tmp, t, z, newSizeZ, newSizeT, reverseOrder));
        }
        finally
        {
            sequence.endUpdate();
        }
    }

    /**
     * Build a new single channel sequence (grey) from the specified channel of the source sequence.
     * 
     * @param source
     *        Source sequence
     * @param channel
     *        Channel index to extract from the source sequence.
     * @return Sequence
     */
    public static Sequence extractChannel(Sequence source, int channel)
    {
        return extractChannels(source, channel);
    }

    /**
     * @deprecated Use {@link #extractChannels(Sequence, int...)} instead.
     */
    @Deprecated
    public static Sequence extractChannels(Sequence source, List<Integer> channels)
    {
        final Sequence outSequence = new Sequence(OMEUtil.createOMEMetadata(source.getMetadata()));

        outSequence.beginUpdate();
        try
        {
            for (int t = 0; t < source.getSizeT(); t++)
                for (int z = 0; z < source.getSizeZ(); z++)
                    outSequence.setImage(t, z, IcyBufferedImageUtil.extractChannels(source.getImage(t, z), channels));
        }
        finally
        {
            outSequence.endUpdate();
        }

        // sequence name
        if (channels.size() > 1)
        {
            String s = "";
            for (int i = 0; i < channels.size(); i++)
                s += " " + channels.get(i).toString();

            outSequence.setName(source.getName() + " (channels" + s + ")");
        }
        else if (channels.size() == 1)
            outSequence.setName(source.getName() + " (" + source.getChannelName(channels.get(0).intValue()) + ")");

        // channel name
        int c = 0;
        for (Integer i : channels)
        {
            outSequence.setChannelName(c, source.getChannelName(i.intValue()));
            c++;
        }

        return outSequence;
    }

    /**
     * Build a new sequence by extracting the specified channels from the source sequence.
     * 
     * @param source
     *        Source sequence
     * @param channels
     *        Channel indexes to extract from the source sequence.
     * @return Sequence
     */
    public static Sequence extractChannels(Sequence source, int... channels)
    {
        final Sequence outSequence = new Sequence(OMEUtil.createOMEMetadata(source.getMetadata()));
        final int sizeT = source.getSizeT();
        final int sizeZ = source.getSizeZ();
        final int sizeC = source.getSizeC();

        outSequence.beginUpdate();
        try
        {
            for (int t = 0; t < sizeT; t++)
                for (int z = 0; z < sizeZ; z++)
                    outSequence.setImage(t, z, IcyBufferedImageUtil.extractChannels(source.getImage(t, z), channels));
        }
        finally
        {
            outSequence.endUpdate();
        }

        final OMEXMLMetadataImpl metadata = outSequence.getMetadata();

        // remove channel metadata
        for (int ch = MetaDataUtil.getNumChannel(metadata, 0) - 1; ch >= 0; ch--)
        {
            boolean remove = true;

            for (int i : channels)
            {
                if (i == ch)
                {
                    remove = false;
                    break;
                }
            }

            if (remove)
                MetaDataUtil.removeChannel(metadata, 0, ch);
        }

        // sequence name
        if (channels.length > 1)
        {
            String s = "";
            for (int i = 0; i < channels.length; i++)
                s += " " + channels[i];

            outSequence.setName(source.getName() + " (channels" + s + ")");
        }
        else if (channels.length == 1)
            outSequence.setName(source.getName() + " (" + source.getChannelName(channels[0]) + ")");

        // copy channel name and colormap
        int c = 0;
        for (int channel : channels)
        {
            if (channel < sizeC)
            {
                outSequence.setChannelName(c, source.getChannelName(channel));
                outSequence.setDefaultColormap(c, source.getDefaultColorMap(channel), false);
            }

            c++;
        }

        return outSequence;
    }

    /**
     * Build a new sequence by extracting the specified Z slice from the source sequence.
     * 
     * @param source
     *        Source sequence
     * @param z
     *        Slice index to extract from the source sequence.
     * @return Sequence
     */
    public static Sequence extractSlice(Sequence source, int z)
    {
        final Sequence outSequence = new Sequence(OMEUtil.createOMEMetadata(source.getMetadata()));

        outSequence.beginUpdate();
        try
        {
            for (int t = 0; t < source.getSizeT(); t++)
                outSequence.setImage(t, 0, source.getImage(t, z));
        }
        finally
        {
            outSequence.endUpdate();
        }

        outSequence.setName(source.getName() + " (slice " + z + ")");

        return outSequence;
    }

    /**
     * Build a new sequence by extracting the specified T frame from the source sequence.
     * 
     * @param source
     *        Source sequence
     * @param t
     *        Frame index to extract from the source sequence.
     * @return Sequence
     */
    public static Sequence extractFrame(Sequence source, int t)
    {
        final Sequence outSequence = new Sequence(OMEUtil.createOMEMetadata(source.getMetadata()));

        outSequence.beginUpdate();
        try
        {
            for (int z = 0; z < source.getSizeZ(); z++)
                outSequence.setImage(0, z, source.getImage(t, z));
        }
        finally
        {
            outSequence.endUpdate();
        }

        outSequence.setName(source.getName() + " (frame " + t + ")");

        return outSequence;
    }

    /**
     * Converts the source sequence to the specified data type.<br>
     * This method returns a new sequence (the source sequence is not modified).
     * 
     * @param source
     *        Source sequence to convert
     * @param dataType
     *        Data type wanted
     * @param rescale
     *        Indicate if we want to scale data value according to data (or data type) range
     * @param useDataBounds
     *        Only used when <code>rescale</code> parameter is true.<br>
     *        Specify if we use the data bounds for rescaling instead of data type bounds.
     * @return converted sequence
     */
    public static Sequence convertToType(Sequence source, DataType dataType, boolean rescale, boolean useDataBounds)
    {
        if (!rescale)
            return convertToType(source, dataType, null);

        // convert with rescale
        final double boundsSrc[];
        final double boundsDst[] = dataType.getDefaultBounds();

        if (useDataBounds)
            boundsSrc = source.getChannelsGlobalBounds();
        else
            boundsSrc = source.getChannelsGlobalTypeBounds();

        // use scaler to scale data
        return convertToType(source, dataType,
                new Scaler(boundsSrc[0], boundsSrc[1], boundsDst[0], boundsDst[1], false));
    }

    /**
     * Converts the source sequence to the specified data type.<br>
     * This method returns a new sequence (the source sequence is not modified).
     * 
     * @param source
     *        Source sequence to convert
     * @param dataType
     *        data type wanted
     * @param rescale
     *        indicate if we want to scale data value according to data type range
     * @return converted sequence
     */
    public static Sequence convertToType(Sequence source, DataType dataType, boolean rescale)
    {
        return convertToType(source, dataType, rescale, false);
    }

    /**
     * Converts the source sequence to the specified data type.<br>
     * This method returns a new sequence (the source sequence is not modified).
     * 
     * @param source
     *        Source sequence to convert
     * @param dataType
     *        data type wanted.
     * @param scaler
     *        scaler for scaling internal data during conversion.
     * @return converted image
     */
    public static Sequence convertToType(Sequence source, DataType dataType, Scaler scaler)
    {
        final Sequence output = new Sequence(OMEUtil.createOMEMetadata(source.getMetadata()));

        output.beginUpdate();
        try
        {
            for (int t = 0; t < source.getSizeT(); t++)
            {
                for (int z = 0; z < source.getSizeZ(); z++)
                {
                    final IcyBufferedImage converted = IcyBufferedImageUtil.convertToType(source.getImage(t, z),
                            dataType, scaler);

                    // FIXME : why we did that ??
                    // this is not a good idea to force bounds when rescale = false

                    // set bounds manually for the converted image
                    // for (int c = 0; c < getSizeC(); c++)
                    // {
                    // converted.setComponentBounds(c, boundsDst);
                    // converted.setComponentUserBounds(c, boundsDst);
                    // }

                    output.setImage(t, z, converted);
                }
            }

            output.setName(source.getName() + " (" + output.getDataType_() + ")");
        }
        finally
        {
            output.endUpdate();
        }

        return output;
    }

    /**
     * Return a rotated version of the source sequence with specified parameters.
     * 
     * @param source
     *        source image
     * @param xOrigin
     *        X origin for the rotation
     * @param yOrigin
     *        Y origin for the rotation
     * @param angle
     *        rotation angle in radian
     * @param filterType
     *        filter resampling method used
     */
    public static Sequence rotate(Sequence source, double xOrigin, double yOrigin, double angle, FilterType filterType)
    {
        final int sizeT = source.getSizeT();
        final int sizeZ = source.getSizeZ();
        final Sequence result = new Sequence(OMEUtil.createOMEMetadata(source.getMetadata()));

        result.beginUpdate();
        try
        {
            for (int t = 0; t < sizeT; t++)
                for (int z = 0; z < sizeZ; z++)
                    result.setImage(t, z,
                            IcyBufferedImageUtil.rotate(source.getImage(t, z), xOrigin, yOrigin, angle, filterType));
        }
        finally
        {
            result.endUpdate();
        }

        result.setName(source.getName() + " (rotated)");

        return result;
    }

    /**
     * Return a rotated version of the source Sequence with specified parameters.
     * 
     * @param source
     *        source image
     * @param angle
     *        rotation angle in radian
     * @param filterType
     *        filter resampling method used
     */
    public static Sequence rotate(Sequence source, double angle, FilterType filterType)
    {
        if (source == null)
            return null;

        return rotate(source, source.getSizeX() / 2d, source.getSizeY() / 2d, angle, filterType);
    }

    /**
     * Return a rotated version of the source Sequence with specified parameters.
     * 
     * @param source
     *        source image
     * @param angle
     *        rotation angle in radian
     */
    public static Sequence rotate(Sequence source, double angle)
    {
        if (source == null)
            return null;

        return rotate(source, source.getSizeX() / 2d, source.getSizeY() / 2d, angle, FilterType.BILINEAR);
    }

    /**
     * Return a copy of the source sequence with specified size, alignment rules and filter type.
     * 
     * @param source
     *        source sequence
     * @param resizeContent
     *        indicate if content should be resized or not (empty area are 0 filled)
     * @param xAlign
     *        horizontal image alignment (SwingConstants.LEFT / CENTER / RIGHT)<br>
     *        (used only if resizeContent is false)
     * @param yAlign
     *        vertical image alignment (SwingConstants.TOP / CENTER / BOTTOM)<br>
     *        (used only if resizeContent is false)
     * @param filterType
     *        filter method used for scale (used only if resizeContent is true)
     */
    public static Sequence scale(Sequence source, int width, int height, boolean resizeContent, int xAlign, int yAlign,
            FilterType filterType)
    {
        final int sizeT = source.getSizeT();
        final int sizeZ = source.getSizeZ();
        final Sequence result = new Sequence(OMEUtil.createOMEMetadata(source.getMetadata()));

        result.beginUpdate();
        try
        {
            for (int t = 0; t < sizeT; t++)
                for (int z = 0; z < sizeZ; z++)
                    result.setImage(t, z, IcyBufferedImageUtil.scale(source.getImage(t, z), width, height,
                            resizeContent, xAlign, yAlign, filterType));
        }
        finally
        {
            result.endUpdate();
        }

        result.setName(source.getName() + " (resized)");
        // content was resized ?
        if (resizeContent)
        {
            final double sx = (double) source.getSizeX() / result.getSizeX();
            final double sy = (double) source.getSizeY() / result.getSizeY();

            // update pixel size
            if ((sx != 0d) && !Double.isInfinite(sx))
                result.setPixelSizeX(result.getPixelSizeX() * sx);
            if ((sy != 0d) && !Double.isInfinite(sy))
                result.setPixelSizeY(result.getPixelSizeY() * sy);
        }

        return result;
    }

    /**
     * Return a copy of the sequence with specified size.<br>
     * By default the FilterType.BILINEAR is used as filter method if resizeContent is true
     * 
     * @param source
     *        source sequence
     * @param resizeContent
     *        indicate if content should be resized or not (empty area are 0 filled)
     * @param xAlign
     *        horizontal image alignment (SwingConstants.LEFT / CENTER / RIGHT)<br>
     *        (used only if resizeContent is false)
     * @param yAlign
     *        vertical image alignment (SwingConstants.TOP / CENTER / BOTTOM)<br>
     *        (used only if resizeContent is false)
     */
    public static Sequence scale(Sequence source, int width, int height, boolean resizeContent, int xAlign, int yAlign)
    {
        return scale(source, width, height, resizeContent, xAlign, yAlign, FilterType.BILINEAR);
    }

    /**
     * Return a copy of the sequence with specified size.
     * 
     * @param source
     *        source sequence
     * @param filterType
     *        filter method used for scale (used only if resizeContent is true)
     */
    public static Sequence scale(Sequence source, int width, int height, FilterType filterType)
    {
        return scale(source, width, height, true, 0, 0, filterType);
    }

    /**
     * Return a copy of the sequence with specified size.<br>
     * By default the FilterType.BILINEAR is used as filter method.
     */
    public static Sequence scale(Sequence source, int width, int height)
    {
        return scale(source, width, height, FilterType.BILINEAR);
    }

    /**
     * Creates a new sequence from the specified region of the source sequence.
     */
    public static Sequence getSubSequence(Sequence source, Rectangle5D.Integer region)
    {
        final Sequence result = new Sequence(OMEUtil.createOMEMetadata(source.getMetadata()));

        final Rectangle region2d = region.toRectangle2D().getBounds();
        final int startZ;
        final int endZ;
        final int startT;
        final int endT;

        if (region.isInfiniteZ())
        {
            startZ = 0;
            endZ = source.getSizeZ();
        }
        else
        {
            startZ = Math.max(0, region.z);
            endZ = Math.min(source.getSizeZ(), region.z + region.sizeZ);
        }
        if (region.isInfiniteT())
        {
            startT = 0;
            endT = source.getSizeT();
        }
        else
        {
            startT = Math.max(0, region.t);
            endT = Math.min(source.getSizeT(), region.t + region.sizeT);
        }

        result.beginUpdate();
        try
        {
            for (int t = startT; t < endT; t++)
            {
                for (int z = startZ; z < endZ; z++)
                {
                    final IcyBufferedImage img = source.getImage(t, z);

                    if (img != null)
                        result.setImage(t - startT, z - startZ,
                                IcyBufferedImageUtil.getSubImage(img, region2d, region.c, region.sizeC));
                    else
                        result.setImage(t - startT, z - startZ, null);
                }
            }
        }
        finally
        {
            result.endUpdate();
        }

        result.setName(source.getName() + " (crop)");

        return result;
    }

    /**
     * @deprecated Use {@link #getSubSequence(Sequence, icy.type.rectangle.Rectangle5D.Integer)}
     *             instead.
     */
    @Deprecated
    public static Sequence getSubSequence(Sequence source, int startX, int startY, int startC, int startZ, int startT,
            int sizeX, int sizeY, int sizeC, int sizeZ, int sizeT)
    {
        return getSubSequence(source, new Rectangle5D.Integer(startX, startY, startZ, startT, startC, sizeX, sizeY,
                sizeZ, sizeT, sizeC));
    }

    /**
     * @deprecated Use {@link #getSubSequence(Sequence, icy.type.rectangle.Rectangle5D.Integer)}
     *             instead.
     */
    @Deprecated
    public static Sequence getSubSequence(Sequence source, int startX, int startY, int startZ, int startT, int sizeX,
            int sizeY, int sizeZ, int sizeT)
    {
        return getSubSequence(source, startX, startY, 0, startZ, startT, sizeX, sizeY, source.getSizeC(), sizeZ, sizeT);
    }

    /**
     * Creates a new sequence which is a sub part of the source sequence defined by the specified
     * {@link ROI} bounds.<br>
     * 
     * @param source
     *        the source sequence
     * @param roi
     *        used to define to region to retain.
     * @param nullValue
     *        the returned sequence is created by using the ROI rectangular bounds.<br>
     *        if <code>nullValue</code> is different of <code>Double.NaN</code> then any pixel
     *        outside the ROI region will be set to <code>nullValue</code>
     */
    public static Sequence getSubSequence(Sequence source, ROI roi, double nullValue)
    {
        final Rectangle5D.Integer bounds = roi.getBounds5D().toInteger();
        final Sequence result = getSubSequence(source, bounds);

        // use null value ?
        if (!Double.isNaN(nullValue))
        {
            final int offX = (bounds.x == Integer.MIN_VALUE) ? 0 : (int) bounds.x;
            final int offY = (bounds.y == Integer.MIN_VALUE) ? 0 : (int) bounds.y;
            final int offZ = (bounds.z == Integer.MIN_VALUE) ? 0 : (int) bounds.z;
            final int offT = (bounds.t == Integer.MIN_VALUE) ? 0 : (int) bounds.t;
            final int offC = (bounds.c == Integer.MIN_VALUE) ? 0 : (int) bounds.c;
            final int sizeX = result.getSizeX();
            final int sizeY = result.getSizeY();
            final int sizeZ = result.getSizeZ();
            final int sizeT = result.getSizeT();
            final int sizeC = result.getSizeC();
            final DataType dataType = result.getDataType_();

            for (int t = 0; t < sizeT; t++)
            {
                for (int z = 0; z < sizeZ; z++)
                {
                    for (int c = 0; c < sizeC; c++)
                    {
                        final BooleanMask2D mask = roi.getBooleanMask2D(z + offZ, t + offT, c + offC, false);
                        final Object data = result.getDataXY(t, z, c);
                        int offset = 0;

                        for (int y = 0; y < sizeY; y++)
                            for (int x = 0; x < sizeX; x++, offset++)
                                if (!mask.contains(x + offX, y + offY))
                                    Array1DUtil.setValue(data, offset, dataType, nullValue);
                    }
                }
            }

            result.dataChanged();
        }

        return result;
    }

    /**
     * Creates a new sequence which is a sub part of the source sequence defined by the specified
     * {@link ROI} bounds.
     */
    public static Sequence getSubSequence(Sequence source, ROI roi)
    {
        return getSubSequence(source, roi, Double.NaN);
    }

    /**
     * Creates and return a copy of the sequence.
     * 
     * @param source
     *        the source sequence to copy
     * @param copyROI
     *        Copy the ROI from source sequence
     * @param copyOverlay
     *        Copy the Overlay from source sequence
     * @param nameSuffix
     *        add the suffix <i>" (copy)"</i> to the new Sequence name to distinguish it
     */
    public static Sequence getCopy(Sequence source, boolean copyROI, boolean copyOverlay, boolean nameSuffix)
    {
        final Sequence result = new Sequence(OMEUtil.createOMEMetadata(source.getMetadata()));

        result.beginUpdate();
        try
        {
            result.copyDataFrom(source);
            if (copyROI)
            {
                for (ROI roi : source.getROIs())
                    result.addROI(roi);
            }
            if (copyOverlay)
            {
                for (Overlay overlay : source.getOverlays())
                    result.addOverlay(overlay);
            }
            if (nameSuffix)
                result.setName(source.getName() + " (copy)");
        }
        finally
        {
            result.endUpdate();
        }

        return result;
    }

    /**
     * Creates and return a copy of the sequence.<br>
     * Note that only data and metadata are copied, overlays and ROIs are not preserved.
     */
    public static Sequence getCopy(Sequence source)
    {
        return getCopy(source, false, false, true);
    }

    /**
     * Convert the specified sequence to gray sequence (single channel)
     */
    public static Sequence toGray(Sequence source)
    {
        return convertColor(source, BufferedImage.TYPE_BYTE_GRAY, null);
    }

    /**
     * Convert the specified sequence to RGB sequence (3 channels)
     */
    public static Sequence toRGB(Sequence source)
    {
        return convertColor(source, BufferedImage.TYPE_INT_RGB, null);
    }

    /**
     * Convert the specified sequence to ARGB sequence (4 channels)
     */
    public static Sequence toARGB(Sequence source)
    {
        return convertColor(source, BufferedImage.TYPE_INT_ARGB, null);
    }

    /**
     * Do color conversion of the specified {@link Sequence} into the specified type.<br>
     * The resulting Sequence will have 4, 3 or 1 channel(s) depending the selected type.
     * 
     * @param source
     *        source sequence
     * @param imageType
     *        wanted image type, only the following is accepted :<br>
     *        BufferedImage.TYPE_INT_ARGB (4 channels)<br>
     *        BufferedImage.TYPE_INT_RGB (3 channels)<br>
     *        BufferedImage.TYPE_BYTE_GRAY (1 channel)<br>
     * @param lut
     *        lut used for color calculation (source sequence lut is used if null)
     */
    public static Sequence convertColor(Sequence source, int imageType, LUT lut)
    {
        final Sequence result = new Sequence(OMEUtil.createOMEMetadata(source.getMetadata()));
        // image receiver
        final BufferedImage imgOut = new BufferedImage(source.getSizeX(), source.getSizeY(), imageType);

        result.beginUpdate();
        try
        {
            for (int t = 0; t < source.getSizeT(); t++)
                for (int z = 0; z < source.getSizeZ(); z++)
                    result.setImage(t, z, IcyBufferedImageUtil.toBufferedImage(source.getImage(t, z), imgOut, lut));

            // rename channels and set final name
            switch (imageType)
            {
                default:
                case BufferedImage.TYPE_INT_ARGB:
                    result.setChannelName(0, "red");
                    result.setChannelName(1, "green");
                    result.setChannelName(2, "blue");
                    result.setChannelName(3, "alpha");
                    result.setName(source.getName() + " (ARGB rendering)");
                    break;

                case BufferedImage.TYPE_INT_RGB:
                    result.setChannelName(0, "red");
                    result.setChannelName(1, "green");
                    result.setChannelName(2, "blue");
                    result.setName(source.getName() + " (RGB rendering)");
                    break;

                case BufferedImage.TYPE_BYTE_GRAY:
                    result.setChannelName(0, "gray");
                    result.setName(source.getName() + " (gray rendering)");
                    break;
            }
        }
        finally
        {
            result.endUpdate();
        }

        return result;
    }
}
