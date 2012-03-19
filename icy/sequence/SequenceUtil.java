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
package icy.sequence;

import icy.image.IcyBufferedImage;

import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.TreeMap;

/**
 * This is a pool of tools to manipulate the organization of the image within a sequence
 * 
 * @author fab & stephane
 */
public class SequenceUtil
{
    /**
     * Exchange 2 T stack on the whole sequence<br>
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
            sequence.removeVolumetricImage(t1);
            sequence.removeVolumetricImage(t2);

            // safe volume image copy (TODO : check if we can't direct set volume image internally)
            if (vi1 != null)
            {
                final TreeMap<Integer, IcyBufferedImage> images = vi1.getImages();

                // copy images of volume image 1 at position t2
                for (Entry<Integer, IcyBufferedImage> entry : images.entrySet())
                    sequence.setImage(t2, entry.getKey().intValue(), entry.getValue());
            }
            if (vi2 != null)
            {
                final TreeMap<Integer, IcyBufferedImage> images = vi2.getImages();

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
     * modify stack t position<br>
     * the previous stack present at 'newT' position is lost
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

        if ((t < 0) || (t >= sizeT) || (newT < 0))
            return;

        // get volume image at position t
        final VolumetricImage vi = sequence.getVolumetricImage(t);

        sequence.beginUpdate();
        try
        {
            // remove volume image (if any) at position newT
            sequence.removeVolumetricImage(newT);

            if (vi != null)
            {
                final TreeMap<Integer, IcyBufferedImage> images = vi.getImages();

                // copy images of volume image at position newT
                for (Entry<Integer, IcyBufferedImage> entry : images.entrySet())
                    sequence.setImage(newT, entry.getKey().intValue(), entry.getValue());

                // remove volume image at position t
                sequence.removeVolumetricImage(t);
            }
        }
        finally
        {
            sequence.endUpdate();
        }
    }

    /**
     * modify t position of a range of stacks by the specified offset
     * 
     * @param sequence
     * @param from
     *        start of stacks range (t position)
     * @param to
     *        end of stacks range (t position)
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
     * remove all a stack at time t
     * 
     * @param sequence
     * @param t
     */
    public static void removeT(Sequence sequence, int t)
    {
        final int sizeT = sequence.getSizeT();

        if ((t < 0) || (t >= sizeT))
            return;

        sequence.removeVolumetricImage(t);
    }

    /**
     * remove a complete stack at position t and shift all the further t by -1
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
     * Reverse T stacks order
     */
    public static void reverseT(Sequence sequence)
    {
        final int sizeT = sequence.getSizeT();

        sequence.beginUpdate();
        try
        {
            for (int t = 0; t < (sizeT >> 1); t++)
            {
                final int te = sizeT - (t + 1);

                // get volume images at position t & te
                final VolumetricImage vi = sequence.getVolumetricImage(t);
                final VolumetricImage vie = sequence.getVolumetricImage(te);

                // start by removing old volume image (if any)
                sequence.removeVolumetricImage(t);
                sequence.removeVolumetricImage(te);

                // safe volume image copy (TODO : check if we can't direct set volume image
                // internally)
                if (vi != null)
                {
                    final TreeMap<Integer, IcyBufferedImage> images = vi.getImages();

                    // copy images of volume image at position te
                    for (Entry<Integer, IcyBufferedImage> entry : images.entrySet())
                        sequence.setImage(te, entry.getKey().intValue(), entry.getValue());
                }
                if (vie != null)
                {
                    final TreeMap<Integer, IcyBufferedImage> images = vie.getImages();

                    // copy images of volume image end at position t
                    for (Entry<Integer, IcyBufferedImage> entry : images.entrySet())
                        sequence.setImage(t, entry.getKey().intValue(), entry.getValue());
                }
            }
        }
        finally
        {
            sequence.endUpdate();
        }
    }

    /**
     * Exchange 2 Z slices on the whole sequence<br>
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
     * modify images z position on the whole sequence<br>
     * previous images present at 'newZ' position are lost
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

        if ((z < 0) || (z >= sizeZ) || (newZ < 0))
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
     * modify z position of a range of images by the specified offset
     * 
     * @param sequence
     * @param from
     *        start of images range (z position)
     * @param to
     *        end of images range (t position)
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
     * remove a specific z in all the sequence.
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
     * remove a specific z and shift z above in all the sequence.
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
     * Reverse Z stack order
     */
    public static void reverseZ(Sequence sequence)
    {
        final int sizeT = sequence.getSizeT();
        final int sizeZ = sequence.getSizeZ();

        sequence.beginUpdate();
        try
        {
            for (int t = 0; t < sizeT; t++)
            {
                for (int z = 0; z < (sizeZ >> 1); z++)
                {
                    final int ze = sizeZ - (z + 1);
                    final IcyBufferedImage image1 = sequence.getImage(t, z);
                    final IcyBufferedImage image2 = sequence.getImage(t, ze);

                    // set image at new position
                    if (image1 != null)
                        sequence.setImage(t, ze, image1);
                    else
                        sequence.removeImage(t, ze);
                    if (image2 != null)
                        sequence.setImage(t, z, image2);
                    else
                        sequence.removeImage(t, z);
                }
            }
        }
        finally
        {
            sequence.endUpdate();
        }
    }

    /**
     * Flatten the sequence to have only single image stacks.<br>
     * (all images are set in T dimension)
     */
    public static void convertToTime(Sequence sequence)
    {
        sequence.beginUpdate();
        try
        {
            final ArrayList<IcyBufferedImage> images = sequence.getAllImage();

            sequence.removeAllImage();
            for (int i = 0; i < images.size(); i++)
                sequence.setImage(i, 0, images.get(i));
        }
        finally
        {
            sequence.endUpdate();
        }
    }

    /**
     * Convert the sequence to a single stack with all the images.<br>
     * (all images are set in Z dimension)
     */
    public static void convertToVolume(Sequence sequence)
    {
        sequence.beginUpdate();
        try
        {

            final ArrayList<IcyBufferedImage> images = sequence.getAllImage();

            sequence.removeAllImage();
            for (int i = 0; i < images.size(); i++)
                sequence.setImage(0, i, images.get(i));
        }
        finally
        {
            sequence.endUpdate();
        }
    }

    // Add an other sequence at the end of the current sequence
    // Insert other sequence at t
    // reverse Z

}
