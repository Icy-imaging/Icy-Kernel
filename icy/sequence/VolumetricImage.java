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
 * @author Fabrice de Chaumont
 */
public class VolumetricImage
{
    final Sequence sequence;
    final TreeMap<Integer, IcyBufferedImage> images;

    public VolumetricImage(Sequence seq)
    {
        sequence = seq;
        images = new TreeMap<Integer, IcyBufferedImage>();
    }

    public VolumetricImage()
    {
        this(null);
    }

    /**
     * Return number of loaded image
     */
    public int getNumImage()
    {
        int result = 0;

        synchronized (images)
        {
            for (Entry<Integer, IcyBufferedImage> entry : images.entrySet())
                if (entry.getValue() != null)
                    result++;
        }

        return result;
    }

    /**
     * Return the size of list
     */
    public int getSize()
    {
        synchronized (images)
        {
            if (images.isEmpty())
                return 0;

            return images.lastKey().intValue() + 1;
        }
    }

    /**
     * Return true if the volumetricImage doesn't contains any image
     */
    public boolean isEmpty()
    {
        return (getSize() == 0);
    }

    /**
     * Return the first image
     */
    public IcyBufferedImage getFirstImage()
    {
        final Entry<Integer, IcyBufferedImage> entry;

        synchronized (images)
        {
            entry = images.firstEntry();
        }

        if (entry != null)
            return entry.getValue();

        return null;
    }

    /**
     * Return the last image
     */
    public IcyBufferedImage getLastImage()
    {
        final Entry<Integer, IcyBufferedImage> entry;

        synchronized (images)
        {
            entry = images.lastEntry();
        }

        if (entry != null)
            return entry.getValue();

        return null;
    }

    /**
     * Return image at position z
     */
    public IcyBufferedImage getImage(int z)
    {
        synchronized (images)
        {
            return images.get(Integer.valueOf(z));
        }
    }

    /**
     * Remove all image
     */
    public void clear()
    {
        if (sequence != null)
            sequence.beginUpdate();

        try
        {
            synchronized (images)
            {
                while (!images.isEmpty())
                {
                    final IcyBufferedImage image = images.pollFirstEntry().getValue();
                    // raise event on sequence
                    if ((image != null) && (sequence != null))
                        sequence.onImageRemoved(image);
                }
            }
        }
        finally
        {
            if (sequence != null)
                sequence.endUpdate();
        }
    }

    /**
     * Remove image at position z
     */
    public boolean removeImage(int z)
    {
        final IcyBufferedImage image;

        synchronized (images)
        {
            image = images.remove(Integer.valueOf(z));
        }

        // raise event on sequence
        if ((image != null) && (sequence != null))
            sequence.onImageRemoved(image);

        return image != null;
    }

    /**
     * Set an image at the specified position
     * 
     * @param image
     */
    public void setImage(int z, IcyBufferedImage image)
    {
        final IcyBufferedImage oldImg = getImage(z);

        if (sequence != null)
            sequence.beginUpdate();
        try
        {
            // set the new image
            synchronized (images)
            {
                images.put(new Integer(z), image);
            }

            // raise event on sequence
            if (sequence != null)
            {
                // we are replacing a previous image ?
                if (oldImg != null)
                    sequence.onImageReplaced(oldImg, image);
                else
                    sequence.onImageAdded(image);
            }
        }
        finally
        {
            if (sequence != null)
                sequence.endUpdate();
        }
    }

    /**
     * Return all images of volume image as TreeMap (contains z position)
     */
    public TreeMap<Integer, IcyBufferedImage> getImages()
    {
        synchronized (images)
        {
            return new TreeMap<Integer, IcyBufferedImage>(images);
        }
    }

    /**
     * Return all images of volume image
     */
    public ArrayList<IcyBufferedImage> getAllImage()
    {
        synchronized (images)
        {
            return new ArrayList<IcyBufferedImage>(images.values());
        }
    }

    /**
     * Remove empty element of image list
     */
    public void pack()
    {
        if (sequence != null)
            sequence.beginUpdate();
        try
        {
            synchronized (images)
            {
                for (Entry<Integer, IcyBufferedImage> entry : images.entrySet())
                {
                    final IcyBufferedImage image = entry.getValue();

                    if (image == null)
                        removeImage(entry.getKey().intValue());
                }
            }
        }
        finally
        {
            if (sequence != null)
                sequence.endUpdate();
        }
    }

}
