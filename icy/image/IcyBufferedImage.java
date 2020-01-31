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

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.ComponentSampleModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferDouble;
import java.awt.image.DataBufferFloat;
import java.awt.image.DataBufferInt;
import java.awt.image.DataBufferShort;
import java.awt.image.DataBufferUShort;
import java.awt.image.ImageObserver;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.nio.channels.ClosedByInterruptException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.media.jai.PlanarImage;

import icy.common.CollapsibleEvent;
import icy.common.UpdateEventHandler;
import icy.common.exception.TooLargeArrayException;
import icy.common.exception.UnsupportedFormatException;
import icy.common.listener.ChangeListener;
import icy.image.IcyBufferedImageEvent.IcyBufferedImageEventType;
import icy.image.cache.ImageCache;
import icy.image.colormap.IcyColorMap;
import icy.image.colormap.LinearColorMap;
import icy.image.colormodel.IcyColorModel;
import icy.image.colormodel.IcyColorModelEvent;
import icy.image.colormodel.IcyColorModelListener;
import icy.image.lut.LUT;
import icy.math.ArrayMath;
import icy.math.MathUtil;
import icy.math.Scaler;
import icy.preferences.GeneralPreferences;
import icy.sequence.Sequence;
import icy.sequence.SequenceIdImporter;
import icy.system.SystemUtil;
import icy.type.DataType;
import icy.type.TypeUtil;
import icy.type.collection.array.Array1DUtil;
import icy.type.collection.array.Array2DUtil;
import icy.type.collection.array.ArrayUtil;
import icy.type.collection.array.ByteArrayConvert;
import icy.util.ReflectionUtil;
import icy.util.StringUtil;
import loci.formats.FormatException;
import loci.formats.IFormatReader;
import loci.formats.gui.SignedByteBuffer;
import loci.formats.gui.SignedShortBuffer;
import loci.formats.gui.UnsignedIntBuffer;
import plugins.kernel.importer.LociImporterPlugin;

/**
 * @author stephane
 */
public class IcyBufferedImage extends BufferedImage implements IcyColorModelListener, ChangeListener
{
    static class WeakIcyBufferedImageReference extends WeakReference<IcyBufferedImage>
    {
        final int hc;

        WeakIcyBufferedImageReference(IcyBufferedImage image)
        {
            super(image);

            hc = System.identityHashCode(image);
        }

        @Override
        public int hashCode()
        {
            return hc;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (obj instanceof WeakIcyBufferedImageReference)
                return obj.hashCode() == hashCode();

            return super.equals(obj);
        }
    }

    public static class ImageSourceInfo
    {
        // importer
        public final SequenceIdImporter imp;
        // series index
        public final int series;
        // resolution
        public final int resolution;
        // region
        public final Rectangle region;
        // T, Z, C position
        public final int t;
        public final int z;
        public final int c;

        public ImageSourceInfo(SequenceIdImporter imp, int series, int resolution, Rectangle region, int t, int z,
                int c)
        {
            super();

            this.imp = imp;
            this.series = series;
            this.resolution = resolution;
            this.region = region;
            this.t = t;
            this.z = z;
            this.c = c;
        }

        @Override
        public String toString()
        {
            return imp.toString() + " s=" + series + " r=" + resolution + " t=" + t + " z=" + z + " c=" + c;
        }
    }

    private static class ImageDataLoaderWorker implements Callable<Object>
    {
        final WeakReference<IcyBufferedImage> imageRef;

        ImageDataLoaderWorker(IcyBufferedImage image)
        {
            super();

            this.imageRef = new WeakReference<IcyBufferedImage>(image);
        }

        @Override
        public Object call() throws Exception
        {
            final IcyBufferedImage image = imageRef.get();

            // image has been released, we probably don't need its data anymore...
            if (image == null)
                return null;

            // not null here
            final ImageSourceInfo imageSourceInfo = image.imageSourceInfo;
            final SequenceIdImporter imp = imageSourceInfo.imp;

            // importer not opened ? --> cannot load
            if (StringUtil.isEmpty(imp.getOpened()))
                throw new IOException("Cannot load image data: Sequence importer is closed.");

            final int sizeC = image.getSizeC();
            // create the result array (always 2D native type)
            final Object[] result = Array2DUtil.createArray(image.getDataType_(), sizeC);

            // all channels ?
            if ((imageSourceInfo.c == -1) && (sizeC > 1))
            {
                // better to directly load image
                final IcyBufferedImage newImage = imp.getImage(imageSourceInfo.series, imageSourceInfo.resolution,
                        imageSourceInfo.region, imageSourceInfo.z, imageSourceInfo.t);
                // we want data in memory
                newImage.setVolatile(false);
                // then get data
                for (int c = 0; c < sizeC; c++)
                    result[c] = newImage.getDataXY(c);
            }
            else
            {
                // all channel for single channel image --> channel 0
                final int startC = (imageSourceInfo.c == -1) ? 0 : imageSourceInfo.c;
                // directly load pixel data
                for (int c = 0; c < sizeC; c++)
                    result[c] = imp.getPixels(imageSourceInfo.series, imageSourceInfo.resolution,
                            imageSourceInfo.region, imageSourceInfo.z, imageSourceInfo.t, startC + c);
            }

            return result;
        }

        IcyBufferedImage getImage()
        {
            return imageRef.get();
        }
    }

    private static class ImageDataLoaderTask extends FutureTask<Object>
    {
        final ImageDataLoaderWorker worker;

        ImageDataLoaderTask(ImageDataLoaderWorker worker)
        {
            super(worker);

            this.worker = worker;
        }

        IcyBufferedImage getImage()
        {
            return worker.getImage();
        }
    }

    private static class ImageDataLoader
    {
        final ThreadPoolExecutor executor;

        public ImageDataLoader()
        {
            super();

            int numWorker = SystemUtil.getNumberOfCPUs();
            executor = new ThreadPoolExecutor(numWorker, numWorker * 2, 5L, TimeUnit.SECONDS,
                    new LinkedBlockingQueue<Runnable>());
        }

        Object loadImageData(IcyBufferedImage image) throws ExecutionException, InterruptedException
        {
            final ImageDataLoaderTask task = new ImageDataLoaderTask(new ImageDataLoaderWorker(image));

            executor.execute(task);

            try
            {
                return task.get();
            }
            catch (InterruptedException e)
            {
                // process interrupted ? remove task from executor queue if possible
                executor.remove(task);
                // cancel the task (without interrupting current running task as this close the importer)
                task.cancel(false);

                // re throw interrupt
                throw e;
            }
        }

        void cancelTasks(IcyBufferedImage image)
        {
            final List<ImageDataLoaderTask> tasks = new ArrayList<ImageDataLoaderTask>();
            final BlockingQueue<Runnable> queue = executor.getQueue();

            synchronized (queue)
            {
                for (Runnable task : queue)
                {
                    final ImageDataLoaderTask imgTask = (ImageDataLoaderTask) task;
                    final IcyBufferedImage imgImage = imgTask.getImage();

                    if ((imgImage == null) || (imgImage == image))
                        tasks.add(imgTask);
                }
            }

            // remove pending tasks for that image
            for (ImageDataLoaderTask task : tasks)
            {
                executor.remove(task);
                task.cancel(false);
            }
        }
    }

    /**
     * Used for image / data loading from importer
     */
    static ImageDataLoader imageDataLoader = new ImageDataLoader();

    /**
     * Used internally to find out an image from its identity hash code
     */
    static Map<Integer, WeakIcyBufferedImageReference> images = new HashMap<Integer, WeakIcyBufferedImageReference>();
    // static Map<Integer, Object> imagesMax = new HashMap<Integer, Object>();

    /**
     * Retrieve an {@link IcyBufferedImage} from its identity hash code
     */
    public static IcyBufferedImage getIcyBufferedImage(Integer idHashCode)
    {
        final WeakIcyBufferedImageReference ref;

        synchronized (images)
        {
            ref = images.get(idHashCode);
        }

        if (ref != null)
            return ref.get();

        return null;
    }

    /**
     * Retrieve an {@link IcyBufferedImage} from its identity hash code
     */
    public static IcyBufferedImage getIcyBufferedImage(int idHashCode)
    {
        return getIcyBufferedImage(Integer.valueOf(idHashCode));
    }

    /**
     * @deprecated
     */
    @Deprecated
    public static int TYPE_BYTE = TypeUtil.TYPE_BYTE;
    /**
     * @deprecated
     */
    @Deprecated
    public static int TYPE_DOUBLE = TypeUtil.TYPE_DOUBLE;
    /**
     * @deprecated
     */
    @Deprecated
    public static int TYPE_FLOAT = TypeUtil.TYPE_FLOAT;
    /**
     * @deprecated
     */
    @Deprecated
    public static int TYPE_INT = TypeUtil.TYPE_INT;
    /**
     * @deprecated
     */
    @Deprecated
    public static int TYPE_SHORT = TypeUtil.TYPE_SHORT;
    /**
     * @deprecated
     */
    @Deprecated
    public static int TYPE_UNDEFINED = TypeUtil.TYPE_UNDEFINED;

    /**
     * @deprecated Use {@link IcyBufferedImageUtil.FilterType} instead.
     */
    @Deprecated
    public static enum FilterType
    {
        NEAREST, BILINEAR, BICUBIC
    };

    /**
     * @deprecated
     */
    @Deprecated
    protected static IcyBufferedImageUtil.FilterType getNewFilterType(FilterType ft)
    {
        switch (ft)
        {
            default:
            case NEAREST:
                return IcyBufferedImageUtil.FilterType.NEAREST;
            case BILINEAR:
                return IcyBufferedImageUtil.FilterType.BILINEAR;
            case BICUBIC:
                return IcyBufferedImageUtil.FilterType.BICUBIC;
        }
    }

    /**
     * Convert a list of BufferedImage to an IcyBufferedImage (multi component).<br>
     * IMPORTANT : source images can be used as part or as the whole result<br>
     * so consider them as "lost"
     * 
     * @param imageList
     *        list of {@link BufferedImage}
     * @return {@link IcyBufferedImage}
     * @deprecated
     *             use {@link #createFrom} instead
     */
    @Deprecated
    public static IcyBufferedImage convert(List<BufferedImage> imageList)
    {
        return createFrom(imageList);
    }

    /**
     * Create an IcyBufferedImage (multi component) from a list of BufferedImage.<br>
     * IMPORTANT: source images can be used as part or as the whole result so consider them as 'lost'.
     * 
     * @param imageList
     *        list of {@link BufferedImage}
     * @return {@link IcyBufferedImage}
     * @throws IllegalArgumentException
     *         if imageList is empty or contains incompatible images.
     */
    public static IcyBufferedImage createFrom(List<? extends BufferedImage> imageList) throws IllegalArgumentException
    {
        if (imageList.size() == 0)
            throw new IllegalArgumentException("imageList should contains at least 1 image");

        final List<IcyBufferedImage> icyImageList = new ArrayList<IcyBufferedImage>();

        // transform images to icy images
        for (BufferedImage image : imageList)
            icyImageList.add(IcyBufferedImage.createFrom(image));

        final IcyBufferedImage firstImage = icyImageList.get(0);

        if (icyImageList.size() == 1)
            return firstImage;

        final DataType dataType = firstImage.getDataType_();
        final int width = firstImage.getWidth();
        final int height = firstImage.getHeight();

        // calculate channel number
        int numChannel = 0;
        for (IcyBufferedImage image : icyImageList)
            numChannel += image.getSizeC();

        final Object[] data = Array2DUtil.createArray(dataType, numChannel);
        final IcyColorMap[] colormaps = new IcyColorMap[numChannel];

        // get data from all images
        int destC = 0;
        for (IcyBufferedImage image : icyImageList)
        {
            if (dataType != image.getDataType_())
                throw new IllegalArgumentException("All images contained in imageList should have the same dataType");
            if ((width != image.getWidth()) || (height != image.getHeight()))
                throw new IllegalArgumentException("All images contained in imageList should have the same dimension");

            for (int c = 0; c < image.getSizeC(); c++)
            {
                data[destC] = image.getDataXY(c);
                colormaps[destC++] = image.getColorMap(c);
            }
        }

        // create result image
        final IcyBufferedImage result = new IcyBufferedImage(width, height, data, dataType.isSigned());

        // restore colormaps
        for (int c = 0; c < result.getSizeC(); c++)
            result.setColorMap(c, colormaps[c], false);

        return result;
    }

    /**
     * Convert a BufferedImage to an IcyBufferedImage.<br>
     * IMPORTANT : source image can be used as part or as the whole result<br>
     * so consider it as "lost"
     * 
     * @param image
     *        {@link BufferedImage}
     * @return {@link IcyBufferedImage}
     * @deprecated
     *             use {@link #createFrom} instead
     */
    @Deprecated
    public static IcyBufferedImage convert(BufferedImage image)
    {
        return createFrom(image);
    }

    /**
     * Create an IcyBufferedImage from a {@link PlanarImage}.<br>
     * IMPORTANT : source image can be used as part or as the whole result<br>
     * so consider it as lost.
     * 
     * @param image
     *        {@link PlanarImage}
     * @return {@link IcyBufferedImage}
     */
    public static IcyBufferedImage createFrom(PlanarImage image, boolean signedDataType)
    {
        final DataBuffer db = image.getData().getDataBuffer();
        final int w = image.getWidth();
        final int h = image.getHeight();

        if (db instanceof DataBufferByte)
            return new IcyBufferedImage(w, h, ((DataBufferByte) db).getBankData(), signedDataType);
        else if (db instanceof DataBufferShort)
            return new IcyBufferedImage(w, h, ((DataBufferShort) db).getBankData(), signedDataType);
        else if (db instanceof DataBufferUShort)
            return new IcyBufferedImage(w, h, ((DataBufferUShort) db).getBankData(), signedDataType);
        else if (db instanceof DataBufferInt)
            return new IcyBufferedImage(w, h, ((DataBufferInt) db).getBankData(), signedDataType);
        else if (db instanceof DataBufferFloat)
            return new IcyBufferedImage(w, h, ((DataBufferFloat) db).getBankData(), true);
        else if (db instanceof javax.media.jai.DataBufferFloat)
            return new IcyBufferedImage(w, h, ((javax.media.jai.DataBufferFloat) db).getBankData(), true);
        else if (db instanceof DataBufferDouble)
            return new IcyBufferedImage(w, h, ((DataBufferDouble) db).getBankData(), true);
        else if (db instanceof javax.media.jai.DataBufferDouble)
            return new IcyBufferedImage(w, h, ((javax.media.jai.DataBufferDouble) db).getBankData(), true);
        else
            // JAI keep dataType and others stuff in their BufferedImage
            return IcyBufferedImage.createFrom(image.getAsBufferedImage());
    }

    /**
     * Create an IcyBufferedImage from a {@link PlanarImage}.<br>
     * IMPORTANT : source image can be used as part or as the whole result<br>
     * so consider it as lost.
     * 
     * @param image
     *        {@link PlanarImage}
     * @return {@link IcyBufferedImage}
     */
    public static IcyBufferedImage createFrom(PlanarImage image)
    {
        return createFrom(image, false);
    }

    /**
     * Create an IcyBufferedImage from a BufferedImage.<br>
     * IMPORTANT : source image can be used as part or as the whole result<br>
     * so consider it as lost.
     * 
     * @param image
     *        {@link BufferedImage}
     * @return {@link IcyBufferedImage}
     */
    public static IcyBufferedImage createFrom(BufferedImage image)
    {
        // IcyBufferedImage --> no conversion needed
        if (image instanceof IcyBufferedImage)
            return (IcyBufferedImage) image;

        // sort of IcyBufferedImage (JAI can return that type) --> no conversion needed
        if (image.getColorModel() instanceof IcyColorModel)
            return new IcyBufferedImage(
                    IcyColorModel.createInstance((IcyColorModel) image.getColorModel(), false, false),
                    image.getRaster());

        final int w = image.getWidth();
        final int h = image.getHeight();
        final int type = image.getType();
        final BufferedImage temp;
        final Graphics g;

        // we first want a component based image
        switch (type)
        {
            case BufferedImage.TYPE_INT_RGB:
            case BufferedImage.TYPE_INT_BGR:
            case BufferedImage.TYPE_USHORT_555_RGB:
            case BufferedImage.TYPE_USHORT_565_RGB:
                temp = new BufferedImage(w, h, BufferedImage.TYPE_3BYTE_BGR);
                g = temp.createGraphics();
                g.drawImage(image, 0, 0, null);
                g.dispose();
                break;

            case BufferedImage.TYPE_INT_ARGB:
            case BufferedImage.TYPE_INT_ARGB_PRE:
            case BufferedImage.TYPE_4BYTE_ABGR_PRE:
                temp = new BufferedImage(w, h, BufferedImage.TYPE_4BYTE_ABGR);
                g = temp.createGraphics();
                g.drawImage(image, 0, 0, null);
                g.dispose();
                break;

            case BufferedImage.TYPE_3BYTE_BGR:
            case BufferedImage.TYPE_4BYTE_ABGR:
                temp = image;
                break;

            default:
                // if we have severals components with an unknown / incompatible sampleModel
                if ((image.getColorModel().getNumComponents() > 1)
                        && (!(image.getSampleModel() instanceof ComponentSampleModel)))
                {
                    // change it to a basic ABGR components image
                    temp = new BufferedImage(w, h, BufferedImage.TYPE_4BYTE_ABGR);
                    g = temp.createGraphics();
                    g.drawImage(image, 0, 0, null);
                    g.dispose();
                }
                else
                    temp = image;
                break;
        }

        // convert initial data type in our data type
        final DataType dataType = DataType.getDataTypeFromDataBufferType(temp.getColorModel().getTransferType());
        // get number of components
        final int numComponents = temp.getRaster().getNumBands();

        // create a compatible image in our format
        final IcyBufferedImage result = new IcyBufferedImage(w, h, numComponents, dataType);

        // copy data from the source image
        result.copyData(temp);

        // in some case we want to restore colormaps from source image
        switch (type)
        {
            case BufferedImage.TYPE_BYTE_BINARY:
            case BufferedImage.TYPE_BYTE_INDEXED:
                if (numComponents == 2)
                    result.setColorMaps(image);
                break;

            case BufferedImage.TYPE_INT_ARGB:
            case BufferedImage.TYPE_INT_ARGB_PRE:
            case BufferedImage.TYPE_4BYTE_ABGR_PRE:
            case BufferedImage.TYPE_4BYTE_ABGR:
                if (numComponents == 4)
                    result.setColorMap(3, LinearColorMap.alpha_, true);
                break;
        }

        return result;
    }

    /**
     * @deprecated Use
     *             {@link LociImporterPlugin#getThumbnailCompatible(IFormatReader, int, int, int)}
     *             instead.
     */
    @Deprecated
    public static IcyBufferedImage createCompatibleThumbnailFrom(IFormatReader reader, int z, int t)
            throws FormatException, IOException
    {
        return LociImporterPlugin.getThumbnailCompatible(reader, z, t, -1);
    }

    /**
     * @deprecated Use {@link LociImporterPlugin#getThumbnail(IFormatReader, int, int, int)}
     *             instead.
     */
    @Deprecated
    public static IcyBufferedImage createThumbnailFrom(IFormatReader reader, int z, int t)
            throws FormatException, IOException
    {
        return LociImporterPlugin.getThumbnail(reader, z, t, -1);
    }

    /**
     * @deprecated Use {@link LociImporterPlugin#getImage(IFormatReader, Rectangle, int, int, int, int)}
     *             instead.
     */
    @Deprecated
    public static IcyBufferedImage createFrom(IFormatReader reader, int x, int y, int w, int h, int z, int t, int c)
            throws FormatException, IOException
    {
        return LociImporterPlugin.getImage(reader, new Rectangle(x, y, w, h), z, t, c, 0);
    }

    /**
     * @deprecated Use {@link LociImporterPlugin#getImage(IFormatReader, Rectangle, int, int)}
     *             instead.
     */
    @Deprecated
    public static IcyBufferedImage createFrom(IFormatReader reader, int z, int t) throws FormatException, IOException
    {
        return LociImporterPlugin.getImage(reader, null, z, t);
    }

    /**
     * @deprecated Use {@link #IcyBufferedImage(int, int, IcyColorModel)} instead.
     */
    @Deprecated
    public static IcyBufferedImage createEmptyImage(int width, int height, IcyColorModel cm)
    {
        return new IcyBufferedImage(width, height, cm);
    }

    /**
     * Image source information used for delayed image loading
     */
    protected ImageSourceInfo imageSourceInfo;

    /**
     * automatic update of channel bounds
     */
    protected boolean autoUpdateChannelBounds;

    /**
     * required cached field as raster is volatile
     */
    protected final int width;
    protected final int height;
    protected final int minX;
    protected final int minY;
    protected final int offsetX;
    protected final int offsetY;

    /**
     * parent <i>raster</i> field (required for volatile data)
     */
    protected Field rasterField;

    /**
     * data initialized state
     */
    protected boolean dataInitialized;

    // internal lock counter
    protected int lockedCount = 0;
    // internal constructed state (needed for proper data initialization)
    private boolean constructed = false;

    /**
     * internal updater
     */
    protected final UpdateEventHandler updater;
    /**
     * listeners
     */
    protected final List<IcyBufferedImageListener> listeners;

    /**
     * Build an Icy formatted BufferedImage, takes an IcyColorModel and a WritableRaster as input
     * 
     * @param cm
     *        {@link IcyColorModel}
     * @param wr
     *        {@link WritableRaster}
     * @param autoUpdateChannelBounds
     *        If true then channel bounds are automatically calculated.
     * @param dataInitialized
     *        When set to <code>true</code> (default), we assume the image is created with initialized data (stored in the {@link WritableRaster} object)
     *        otherwise data will be initialized / loaded on first data request (lazy loading).
     * @param forceVolatileData
     *        If set to <code>true</code> then image data is volatile regardless of {@link GeneralPreferences#getVirtualMode()} state and can be lost if not
     *        specifically stored using <code>setDataxx(..)</code> methods.<br>
     *        Image cache is used to handle data storage and can move data on disk when memory is getting low.<br>
     *        Note that Default value for this parameter is <code>false</code>.
     */
    protected IcyBufferedImage(IcyColorModel cm, WritableRaster wr, boolean autoUpdateChannelBounds,
            boolean dataInitialized, boolean forceVolatileData)
    {
        super(cm, wr, false, null);

        // store it in the hashmap (weak reference)
        synchronized (images)
        {
            images.put(Integer.valueOf(System.identityHashCode(this)), new WeakIcyBufferedImageReference(this));
        }

        imageSourceInfo = null;
        width = wr.getWidth();
        height = wr.getHeight();
        minX = wr.getMinX();
        minY = wr.getMinY();
        offsetX = wr.getSampleModelTranslateX();
        offsetY = wr.getSampleModelTranslateY();

        // automatic update of channel bounds
        this.autoUpdateChannelBounds = autoUpdateChannelBounds;

        updater = new UpdateEventHandler(this, false);
        listeners = new ArrayList<IcyBufferedImageListener>();

        // default
        rasterField = null;

        // we want volatile data ?
        if (forceVolatileData || GeneralPreferences.getVirtualMode())
        {
            try
            {
                // we need access to parent raster field
                rasterField = ReflectionUtil.getField(BufferedImage.class, "raster", true);
                // set it to null so it won't retain data
                if (rasterField != null)
                    rasterField.set(this, null);
            }
            catch (Exception e)
            {
                System.out.println(e.getMessage());
                System.out.println("Warning: cannot get access to internal BufferedImage.raster field.");
                System.out.println("         Image caching cannot be used for this image.");

                // no access to parent raster...
                rasterField = null;
            }
        }

        this.dataInitialized = dataInitialized;

        // we have initialized data ? --> save them in cache
        if (dataInitialized)
        {
            // save data in cache (for volatile image)
            saveRasterInCache(wr, true);
            // update image components bounds
            if (autoUpdateChannelBounds)
                updateChannelsBounds();
        }
        // else
        // {
        // // we want to force loading from Importer if needed so we don't reference data
        // volatileRaster = null;
        // volatileData = null;
        // }

        // add listener to colorModel
        cm.addListener(this);

        constructed = true;
    }

    /**
     * Build an Icy formatted BufferedImage, takes an IcyColorModel and a WritableRaster as input
     * 
     * @param cm
     *        {@link IcyColorModel}
     * @param wr
     *        {@link WritableRaster}
     * @param autoUpdateChannelBounds
     *        If true then channel bounds are automatically calculated.<br>
     */
    protected IcyBufferedImage(IcyColorModel cm, WritableRaster wr, boolean autoUpdateChannelBounds)
    {
        this(cm, wr, autoUpdateChannelBounds, true, false);
    }

    /**
     * Create an Icy formatted BufferedImage, takes an IcyColorModel and a WritableRaster as input
     * 
     * @param cm
     *        {@link IcyColorModel}
     * @param wr
     *        {@link WritableRaster}
     */
    protected IcyBufferedImage(IcyColorModel cm, WritableRaster wr)
    {
        this(cm, wr, false, true, false);

    }

    /**
     * Create an Icy formatted BufferedImage with specified IcyColorModel, width and height.<br>
     * Private version, {@link IcyColorModel} is directly used internally.
     */
    protected IcyBufferedImage(IcyColorModel cm, int width, int height, boolean forceVolatileData)
    {
        this(cm, cm.createCompatibleWritableRaster(width, height), false, false, forceVolatileData);
    }

    /**
     * Create an Icy formatted BufferedImage with specified IcyColorModel, width and height.<br>
     * Private version, {@link IcyColorModel} is directly used internally.
     */
    protected IcyBufferedImage(IcyColorModel cm, int width, int height)
    {
        this(cm, cm.createCompatibleWritableRaster(width, height), false, false, false);
    }

    /**
     * Create an Icy formatted BufferedImage with specified colorModel, width, height and input data.<br>
     * ex : <code>img = new IcyBufferedImage(640, 480, new byte[3][640 * 480], true);</code><br>
     * <br>
     * This constructor provides the best performance for massive image creation and computation as it allow you to
     * directly send the data array and disable the channel bounds calculation.
     * 
     * @param cm
     *        the color model
     * @param width
     * @param height
     * @param data
     *        image data<br>
     *        Should be a 2D array with first dimension giving the number of component<br>
     *        and second dimension equals to <code>width * height</code><br>
     *        The array data type specify the internal data type and should match the given color
     *        model parameter.
     * @param autoUpdateChannelBounds
     *        If true then channel bounds are automatically calculated.<br>
     *        When set to false, you have to set bounds manually by calling
     *        {@link #updateChannelsBounds()} or #setC
     */
    protected IcyBufferedImage(IcyColorModel cm, Object[] data, int width, int height, boolean autoUpdateChannelBounds)
    {
        this(cm, cm.createWritableRaster(data, width, height), autoUpdateChannelBounds);
    }

    /**
     * Create an Icy formatted BufferedImage with specified width, height and input data.<br>
     * ex : <code>img = new IcyBufferedImage(640, 480, new byte[3][640 * 480], true);</code><br>
     * <br>
     * This constructor provides the best performance for massive image creation and computation as
     * it allow you to directly send the data array and disable the channel bounds calculation.
     * 
     * @param width
     * @param height
     * @param data
     *        image data<br>
     *        Should be a 2D array with first dimension giving the number of component<br>
     *        and second dimension equals to <code>width * height</code><br>
     *        The array data type specify the internal data type.
     * @param signed
     *        use signed data for data type
     * @param autoUpdateChannelBounds
     *        If true then channel bounds are automatically calculated.<br>
     *        When set to false, you have to set bounds manually by calling
     *        {@link #updateChannelsBounds()} or #setC
     */
    public IcyBufferedImage(int width, int height, Object[] data, boolean signed, boolean autoUpdateChannelBounds)
    {
        this(IcyColorModel.createInstance(data.length, ArrayUtil.getDataType(data[0], signed)), data, width, height,
                autoUpdateChannelBounds);
    }

    /**
     * Create an Icy formatted BufferedImage with specified width, height and input data.<br>
     * ex : <code>img = new IcyBufferedImage(640, 480, new byte[3][640 * 480]);</code>
     * 
     * @param width
     * @param height
     * @param data
     *        image data<br>
     *        Should be a 2D array with first dimension giving the number of component<br>
     *        and second dimension equals to <code>width * height</code><br>
     *        The array data type specify the internal data type.
     * @param signed
     *        use signed data for data type
     */
    public IcyBufferedImage(int width, int height, Object[] data, boolean signed)
    {
        this(IcyColorModel.createInstance(data.length, ArrayUtil.getDataType(data[0], signed)), data, width, height,
                false);
    }

    /**
     * Create an Icy formatted BufferedImage with specified width, height and input data.<br>
     * ex : <code>img = new IcyBufferedImage(640, 480, new byte[3][640 * 480]);</code>
     * 
     * @param width
     * @param height
     * @param data
     *        image data<br>
     *        Should be a 2D array with first dimension giving the number of component<br>
     *        and second dimension equals to <code>width * height</code><br>
     *        The array data type specify the internal data type.
     */
    public IcyBufferedImage(int width, int height, Object[] data)
    {
        this(width, height, data, false);
    }

    /**
     * Create a single channel Icy formatted BufferedImage with specified width, height and input data.<br>
     * ex : <code>img = new IcyBufferedImage(640, 480, new byte[640 * 480], true);</code><br>
     * <br>
     * This constructor provides the best performance for massive image creation and computation as it allow you to
     * directly send the data array and disable the channel bounds calculation.
     * 
     * @param width
     * @param height
     * @param data
     *        image data array.<br>
     *        The length of the array should be equals to <code>width * height</code>.<br>
     *        The array data type specify the internal data type.
     * @param signed
     *        use signed data for data type
     * @param autoUpdateChannelBounds
     *        If true then channel bounds are automatically calculated.<br>
     *        When set to false, you have to set bounds manually by calling
     *        {@link #updateChannelsBounds()} or #setC
     * @see #IcyBufferedImage(int, int, Object[], boolean, boolean)
     */
    public IcyBufferedImage(int width, int height, Object data, boolean signed, boolean autoUpdateChannelBounds)
    {
        this(width, height, ArrayUtil.encapsulate(data), signed, autoUpdateChannelBounds);
    }

    /**
     * Create a single channel Icy formatted BufferedImage with specified width, height and input
     * data.<br>
     * ex : <code>img = new IcyBufferedImage(640, 480, new byte[640 * 480]);</code>
     * 
     * @param width
     * @param height
     * @param data
     *        image data<br>
     *        The length of the array should be equals to <code>width * height</code>.<br>
     *        The array data type specify the internal data type.
     * @param signed
     *        use signed data for data type
     */
    public IcyBufferedImage(int width, int height, Object data, boolean signed)
    {
        this(width, height, ArrayUtil.encapsulate(data), signed);
    }

    /**
     * Create a single channel Icy formatted BufferedImage with specified width, height and input
     * data.<br>
     * ex : <code>img = new IcyBufferedImage(640, 480, new byte[640 * 480]);</code>
     * 
     * @param width
     * @param height
     * @param data
     *        image data<br>
     *        The length of the array should be equals to <code>width * height</code>.<br>
     *        The array data type specify the internal data type.
     */
    public IcyBufferedImage(int width, int height, Object data)
    {
        this(width, height, ArrayUtil.encapsulate(data));
    }

    /**
     * Create an ICY formatted BufferedImage with specified width, height,<br>
     * number of component and dataType.
     * 
     * @param width
     * @param height
     * @param numComponents
     * @param dataType
     *        image data type {@link DataType}
     */
    public IcyBufferedImage(int width, int height, int numComponents, DataType dataType, boolean forceVolatileData)
    {
        this(IcyColorModel.createInstance(numComponents, dataType), width, height, forceVolatileData);
    }

    /**
     * Create an ICY formatted BufferedImage with specified width, height,<br>
     * number of component and dataType.
     * 
     * @param width
     * @param height
     * @param numComponents
     * @param dataType
     *        image data type {@link DataType}
     */
    public IcyBufferedImage(int width, int height, int numComponents, DataType dataType)
    {
        this(IcyColorModel.createInstance(numComponents, dataType), width, height, false);
    }

    /**
     * Create an ICY formatted BufferedImage with specified width, height and IcyColorModel
     * type.<br>
     */
    public IcyBufferedImage(int width, int height, IcyColorModel cm)
    {
        this(width, height, cm.getNumComponents(), cm.getDataType_());
    }

    /**
     * @deprecated use {@link #IcyBufferedImage(int, int, int, DataType)} instead
     */
    @Deprecated
    public IcyBufferedImage(int width, int height, int numComponents, int dataType, boolean signed)
    {
        this(IcyColorModel.createInstance(numComponents, dataType, signed), width, height);
    }

    /**
     * @deprecated use {@link #IcyBufferedImage(int, int, int, DataType)} instead
     */
    @Deprecated
    public IcyBufferedImage(int width, int height, int numComponents, int dataType)
    {
        this(IcyColorModel.createInstance(numComponents, dataType, false), width, height);
    }

    @Override
    protected void finalize() throws Throwable
    {
        // cancel any pending loading tasks for this image
        imageDataLoader.cancelTasks(this);
        // image has been released, be sure to clear cache
        if (ImageCache.isEnabled())
            ImageCache.remove(this);

        // remove it from hashmap
        synchronized (images)
        {
            images.remove(Integer.valueOf(System.identityHashCode(this)));
        }

        super.finalize();
    }

    public ImageSourceInfo getImageSourceInfo()
    {
        return imageSourceInfo;
    }

    /**
     * Set the image source information that will be used later for lazy image data loading.
     */
    public void setImageSourceInfo(SequenceIdImporter imp, int series, int resolution, Rectangle region, int t, int z,
            int c)
    {
        imageSourceInfo = new ImageSourceInfo(imp, series, resolution, region, t, z, c);
    }

    /**
     * Returns <code>true</code> if data is initialized
     */
    public boolean isDataInitialized()
    {
        return dataInitialized;
    }

    /**
     * Same as {@link #isDataInitialized()}
     * 
     * @see #isDataInitialized()
     */
    public boolean isDataLoaded()
    {
        return isDataInitialized();
    }

    /**
     * Returns <code>true</code> if data is currently loaded in memory.<br>
     * It returns <code>false</code> if data has not yet be initialized (see {@link #isDataInitialized()}) or if data is cached on disk (not anymore in memory)
     */
    public boolean isDataInMemory()
    {
        return (!ImageCache.isEnabled()) || ImageCache.isOnMemoryCache(this);
    }

    /**
     * Returns <code>true</code> if image data is volatile.<br>
     * Volatile data means <b>there is no strong reference on the internal data arrays</b> (data can be cached on disk) and so <b>any <i>external</i> changes on
     * them can be lost</b> if they has not been specifically set using setDataxx() methods.
     * Volatile is useful when you want to load many images with low memory consumption but you should use it carefully as it has some limitations.<br>
     * 
     * @see #setVolatile(boolean)
     */
    public boolean isVolatile()
    {
        return rasterField != null;
    }

    /**
     * Sets the <i>volatile</i> state for this image.<br>
     * Volatile data means <b>there is no strong reference on the internal data arrays</b> (data can be cached on disk) and so <b>any <i>external</i> changes on
     * them can be lost</b> if they has not been specifically set using setDataxx() methods.
     * Volatile is useful when you want to load many images with low memory consumption but you should use it carefully as it has some limitations.<br>
     * Setting the image to volatile <b>immediately release internal strong reference on data arrays</b> (see {@link #lockRaster()} and
     * {@link #releaseRaster(boolean)} methods).<br>
     * 
     * @throws OutOfMemoryError
     *         if there is not enough memory available to store image
     *         data when setting back to <i>non volatile</i> state
     * @throws UnsupportedOperationException
     *         if cache engine is not initialized (error at initialization).
     */
    public void setVolatile(boolean value) throws OutOfMemoryError, UnsupportedOperationException
    {
        // we want volatile data ?
        if (value)
        {
            if (rasterField == null)
            {
                // cache engine couldn't be used
                if (!ImageCache.isEnabled())
                    throw new UnsupportedOperationException(
                            "IcyBufferedImage.setVolatile(..) error: Image cache is disabled !");

                try
                {
                    // we need access to parent raster field
                    rasterField = ReflectionUtil.getField(BufferedImage.class, "raster", true);

                    if (rasterField != null)
                    {
                        // save data in cache as we will release the strong reference
                        saveDataInCache();
                        // immediately remove strong reference to data
                        rasterField.set(this, null);
                    }
                }
                catch (Exception e)
                {
                    System.out.println(e.getMessage());
                    System.out.println("Warning: cannot get access to internal BufferedImage.raster field.");
                    System.out.println("         Image caching cannot be used for this image.");

                    // no access to parent raster...
                    rasterField = null;
                }
            }
        }
        // no volatile anymore ?
        else
        {
            // were we volatile ?
            if (rasterField != null)
            {
                try
                {
                    // need to set back raster strong reference
                    if (super.getRaster() == null)
                    {
                        // data not yet initialized ?
                        if (!isDataInitialized())
                            // we don't want to force data loading for that
                            rasterField.set(this, buildRaster(createEmptyRasterData()));
                        else
                            rasterField.set(this, getRaster());
                    }

                    // set it to null so it won't retain data
                    rasterField = null;
                    // clear data set in cache (no more used)
                    ImageCache.remove(this);
                }
                catch (OutOfMemoryError e)
                {
                    System.err.println(e.getMessage());
                    System.err.println(
                            "IcyBufferedImage.setVolatile(false) error: not enough memory to set image data back in memory.");
                    throw e;
                }
                catch (Throwable e)
                {
                    System.err.println(e.getMessage());
                    System.err.println(
                            "IcyBufferedImage.setVolatile(..) error: cannot set parent raster field (data lost).");
                }
            }
        }
    }

    /**
     * Force loading data for this image (so channel bounds can be correctly computed even with lazy loading)
     */
    public void loadData()
    {
        if (!isDataInitialized())
            // that is enough to get data loaded
            getRaster();
    }

    protected synchronized WritableRaster getRaster(boolean retain)
    {
        // always try first from parent
        WritableRaster result = super.getRaster();

        // data not yet initialized ?
        if (constructed && !isDataInitialized())
        {
            // initialize data
            final Object rasterData = initializeData();

            // could not initialize data ? --> use temporary empty data (we want to retry data initialization later)
            if (rasterData == null)
                return buildRaster(createEmptyRasterData());

            // save them in cache (for volatile image) but don't need to be eternal
            saveRasterDataInCache(rasterData, false);
            // we have the parent raster ? --> update its data (important to do it before setting data initialized)
            if (result != null)
                setRasterData(result, rasterData);

            // data is initialized (important to set it before updating channel bounds)
            dataInitialized = true;

            // update image channels bounds
            if (autoUpdateChannelBounds)
                updateChannelsBounds();
        }

        // we don't have the parent raster ? (mean we have a volatile image)
        if (result == null)
        {
            // get it from cache
            result = loadRasterFromCache();

            try
            {
                // store it in the original strong reference if asked
                if (retain && (rasterField != null))
                    rasterField.set(this, result);
            }
            catch (Exception e)
            {
                System.out.println(e.getMessage());
                System.out.println("Warning: cannot set internal BufferedImage.raster field.");
            }
        }

        return result;
    }

    @Override
    public WritableRaster getRaster()
    {
        return getRaster(false);
    }

    @Override
    public WritableRaster getAlphaRaster()
    {
        return getColorModel().getAlphaRaster(getRaster());
    }

    /**
     * Return <code>true</code> if raster data is strongly referenced, <code>false</code> otherwise.<br>
     * Note that it doesn't necessary mean that we called {@link #lockRaster()} method.
     * 
     * @see #lockRaster()
     * @see #isVolatile()
     */
    public synchronized boolean isRasterLocked()
    {
        try
        {
            return (rasterField == null) || (rasterField.get(this) != null);
        }
        catch (Exception e)
        {
            // something bad happened, just assume false then..
            return false;
        }
    }

    /**
     * Ensure raster data remains strongly referenced until we call {@link #releaseRaster(boolean)}.<br>
     * This is important to lock / release raster for Volatile image when you are modifying data externally otherwise data could be lost.
     * 
     * @see #releaseRaster(boolean)
     * @see #isVolatile()
     */
    public synchronized void lockRaster()
    {
        if (lockedCount++ != 0)
            return;

        getRaster(true);
    }

    /**
     * Release the raster object.
     * 
     * @param saveInCache
     *        force to save raster data in cache (for volatile image only)
     */
    public synchronized void releaseRaster(boolean saveInCache)
    {
        if (--lockedCount != 0)
            return;

        try
        {
            // force saving changed data in cache before releasing raster
            if (saveInCache)
                saveDataInCache();
            // set parent raster to null (release raster object)
            if (rasterField != null)
                rasterField.set(this, null);
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
            System.out.println("Warning: cannot set internal BufferedImage.raster field.");
        }
    }

    @Override
    public int getWidth()
    {
        return width;
    }

    @Override
    public int getHeight()
    {
        return height;
    }

    @Override
    public int getWidth(ImageObserver observer)
    {
        return width;
    }

    @Override
    public int getHeight(ImageObserver observer)
    {
        return height;
    }

    @Override
    public int getMinX()
    {
        return minX;
    }

    @Override
    public int getMinY()
    {
        return minY;
    }

    @Override
    public int getTileWidth()
    {
        return getWidth();
    }

    @Override
    public int getTileHeight()
    {
        return getHeight();
    }

    @Override
    public int getTileGridXOffset()
    {
        return offsetX;
    }

    @Override
    public int getTileGridYOffset()
    {
        return offsetY;
    }

    @Override
    public SampleModel getSampleModel()
    {
        return getRaster().getSampleModel();
    }

    @Override
    public void coerceData(boolean isAlphaPremultiplied)
    {
        // don't need to do any conversion here...
    }

    @Override
    public WritableRaster copyData(WritableRaster outRaster)
    {
        lockRaster();
        try
        {
            return super.copyData(outRaster);
        }
        finally
        {
            releaseRaster(true);
        }
    }

    @Override
    public Raster getTile(int tileX, int tileY)
    {
        lockRaster();
        try
        {
            return super.getTile(tileX, tileY);
        }
        finally
        {
            releaseRaster(false);
        }
    }

    @Override
    public WritableRaster getWritableTile(int tileX, int tileY)
    {
        lockRaster();
        try
        {
            return super.getWritableTile(tileX, tileY);
        }
        finally
        {
            releaseRaster(false);
        }
    }

    @Override
    public Raster getData()
    {
        lockRaster();
        try
        {
            return super.getData();
        }
        finally
        {
            releaseRaster(false);
        }
    }

    @Override
    public Raster getData(Rectangle rect)
    {
        lockRaster();
        try
        {
            return super.getData(rect);
        }
        finally
        {
            releaseRaster(false);
        }
    }

    @Override
    public void setData(Raster r)
    {
        lockRaster();
        try
        {
            super.setData(r);
        }
        finally
        {
            releaseRaster(false);
        }
    }

    @Override
    public int getRGB(int x, int y)
    {
        lockRaster();
        try
        {
            return super.getRGB(x, y);
        }
        finally
        {
            releaseRaster(false);
        }
    }

    @Override
    public int[] getRGB(int startX, int startY, int w, int h, int[] rgbArray, int offset, int scansize)
    {
        lockRaster();
        try
        {
            return super.getRGB(startX, startY, w, h, rgbArray, offset, scansize);
        }
        finally
        {
            releaseRaster(false);
        }
    }

    @Override
    public synchronized void setRGB(int x, int y, int rgb)
    {
        lockRaster();
        try
        {
            super.setRGB(x, y, rgb);
        }
        finally
        {
            // FIXME: implement delayed cache saving to avoid very poor performance here
            releaseRaster(true);
        }
    }

    @Override
    public void setRGB(int startX, int startY, int w, int h, int[] rgbArray, int offset, int scansize)
    {
        lockRaster();
        try
        {
            super.setRGB(startX, startY, w, h, rgbArray, offset, scansize);
        }
        finally
        {
            releaseRaster(true);
        }
    }

    /**
     * Return the owner sequence (can be null if the image is not owned in any Sequence)
     */
    public Sequence getOwnerSequence()
    {
        // just use the listeners to find it (Sequence always listen image event)
        for (IcyBufferedImageListener listener : listeners)
            if (listener instanceof Sequence)
                return (Sequence) listener;

        return null;
    }

    protected WritableRaster loadRasterFromCache()
    {
        Object rasterData = null;
        boolean datalost = false;

        try
        {
            // get data from cache
            rasterData = ImageCache.get(this);
        }
        catch (Throwable e)
        {
            datalost = true;
            System.err.println(e.getMessage());
        }

        // should happen only for unmodified data
        if (rasterData == null)
        {
            // we should be able to initialize data back
            rasterData = initializeData();

            // couldn't initialize data ? create empty data without saving in cache (we want to retry later)
            if (rasterData == null)
            {
                rasterData = createEmptyRasterData();

                //// don't notify twice
                // if (!datalost)
                // System.err.println("IcyBufferedImage.loadRasterFromCache: cannot get image data (data lost)");
            }
            else
            {
                // data could not be loaded from cache but was correctly restored
                if (datalost)
                    System.out.println("Data re-initialized (changes are lost)");

                // save it in cache (not eternal here as this is default data)
                saveRasterDataInCache(rasterData, false);
            }
        }

        return buildRaster(rasterData);
    }

    /**
     * Explicitly save the image data in cache (only for volatile image)
     * 
     * @see #isVolatile()
     */
    public void saveDataInCache()
    {
        // need to be saved only if initialized
        if (isDataInitialized())
            saveRasterInCache(getRaster());
    }

    protected void saveRasterInCache(WritableRaster wr, boolean eternal)
    {
        saveRasterDataInCache(getRasterData(wr), eternal);
    }

    protected void saveRasterInCache(WritableRaster wr)
    {
        saveRasterInCache(wr, true);
    }

    protected void saveRasterDataInCache(Object rasterData, boolean eternal)
    {
        // save data in cache (volatile image only)
        if (isVolatile())
        {
            try
            {
                ImageCache.set(this, rasterData, eternal);
            }
            catch (Throwable e)
            {
                System.err.println(e.getMessage());
            }
        }
    }

    protected void saveRasterDataInCache(Object rasterData)
    {
        saveRasterDataInCache(rasterData, true);
    }

    /**
     * Build raster from an Object (internally the Object is always a 2D array of native data type)
     */
    protected WritableRaster buildRaster(Object data)
    {
        return IcyColorModel.createWritableRaster(data, getWidth(), getHeight());
    }

    /**
     * Returns raster data in Object format (internally we always have a 2D array of native data type)
     */
    protected static Object getRasterData(WritableRaster wr)
    {
        final DataBuffer db = wr.getDataBuffer();

        switch (db.getDataType())
        {
            case DataBuffer.TYPE_BYTE:
                return ((DataBufferByte) db).getBankData();
            case DataBuffer.TYPE_USHORT:
                return ((DataBufferUShort) db).getBankData();
            case DataBuffer.TYPE_SHORT:
                return ((DataBufferShort) db).getBankData();
            case DataBuffer.TYPE_INT:
                return ((DataBufferInt) db).getBankData();
            case DataBuffer.TYPE_FLOAT:
                return ((DataBufferFloat) db).getBankData();
            case DataBuffer.TYPE_DOUBLE:
                return ((DataBufferDouble) db).getBankData();
            default:
                return null;
        }
    }

    /**
     * Set raster data (Object is always a 2D array of native data type)
     */
    protected void setRasterData(WritableRaster wr, Object data)
    {
        final Object dest;
        final DataBuffer db = wr.getDataBuffer();

        switch (db.getDataType())
        {
            case DataBuffer.TYPE_BYTE:
                dest = ((DataBufferByte) db).getBankData();
                break;
            case DataBuffer.TYPE_USHORT:
                dest = ((DataBufferUShort) db).getBankData();
                break;
            case DataBuffer.TYPE_SHORT:
                dest = ((DataBufferShort) db).getBankData();
                break;
            case DataBuffer.TYPE_INT:
                dest = ((DataBufferInt) db).getBankData();
                break;
            case DataBuffer.TYPE_FLOAT:
                dest = ((DataBufferFloat) db).getBankData();
                break;
            case DataBuffer.TYPE_DOUBLE:
                dest = ((DataBufferDouble) db).getBankData();
                break;
            default:
                dest = null;
                break;
        }

        if (dest != null)
        {
            final int len = Array.getLength(dest);
            for (int i = 0; i < len; i++)
            {
                final Object destSub = Array.get(dest, i);
                System.arraycopy(Array.get(data, i), 0, destSub, 0, Array.getLength(destSub));
            }
        }
    }

    protected Object initializeData()
    {
        try
        {
            // load data from importer
            return loadDataFromImporter();
        }
        catch (InterruptedException e)
        {
            System.err.println(
                    "IcyBufferedImage.loadDataFromImporter() warning: image loading from ImageProvider was interrupted (image data not retrieved).");

            // we want to keep the interrupted state here
            Thread.currentThread().interrupt();

            return null;
        }
        catch (ClosedByInterruptException e)
        {
            // this one should never happen as loading is done in a separate thread (executor)
            System.err.println(
                    "IcyBufferedImage.loadDataFromImporter() error: image loading from ImageProvider was interrupted (further image won't be loaded) !");

            // we want to keep the interrupted state here
            Thread.currentThread().interrupt();

            return null;
        }
        catch (Exception e)
        {
            System.err.println(e);
            System.err.println(
                    "IcyBufferedImage.loadDataFromImporter() warning: cannot get image from ImageProvider (possible data loss).");

            return null;
        }
    }

    protected Object createEmptyRasterData()
    {
        final DataType dataType = getDataType_();
        final int sizeC = getSizeC();
        final int sizeXY = getSizeX() * getSizeY();

        // create the result array (always 2D native type)
        final Object[] result = Array2DUtil.createArray(dataType, sizeC);

        for (int c = 0; c < sizeC; c++)
            result[c] = Array1DUtil.createArray(dataType, sizeXY);

        return result;
    }

    protected Object loadDataFromImporter() throws UnsupportedFormatException, IOException, InterruptedException
    {
        // image source information not defined (not attached to importer) ? --> create empty data
        if (imageSourceInfo == null)
            return createEmptyRasterData();

        try
        {
            // get data from importer using
            return imageDataLoader.loadImageData(this);
        }
        catch (ExecutionException e)
        {
            final Throwable cause = e.getCause();

            if (cause instanceof UnsupportedFormatException)
                throw ((UnsupportedFormatException) cause);
            if (cause instanceof IOException)
                throw ((IOException) cause);
            throw new IOException(cause);
        }
    }

    /**
     * @return true is channel bounds are automatically updated when image data is modified.
     * @see #setAutoUpdateChannelBounds(boolean)
     */
    public boolean getAutoUpdateChannelBounds()
    {
        return autoUpdateChannelBounds;
    }

    /**
     * If set to <code>true</code> (default) then channel bounds will be automatically recalculated
     * when image data is modified.<br>
     * This can consume some time if you make many updates on a large image.<br>
     * In this case you should do your updates in a {@link #beginUpdate()} ... {@link #endUpdate()}
     * block to avoid
     * severals recalculation.
     */
    public void setAutoUpdateChannelBounds(boolean value)
    {
        if (autoUpdateChannelBounds != value)
        {
            if (value)
                updateChannelsBounds();

            autoUpdateChannelBounds = value;
        }
    }

    /**
     * @deprecated Uses
     *             {@link IcyBufferedImageUtil#toBufferedImage(IcyBufferedImage, BufferedImage, LUT)}
     *             instead.
     */
    @Deprecated
    public BufferedImage convertToBufferedImage(BufferedImage out, LUT lut)
    {
        return IcyBufferedImageUtil.toBufferedImage(this, out, lut);
    }

    /**
     * @deprecated Uses
     *             {@link IcyBufferedImageUtil#toBufferedImage(IcyBufferedImage, BufferedImage)}
     *             instead.
     */
    @Deprecated
    public BufferedImage convertToBufferedImage(BufferedImage out)
    {
        return IcyBufferedImageUtil.toBufferedImage(this, out);
    }

    /**
     * @deprecated Uses
     *             {@link IcyBufferedImageUtil#toBufferedImage(IcyBufferedImage, BufferedImage, LUT)}
     *             instead.
     */
    @Deprecated
    public BufferedImage getARGBImage(LUT lut, BufferedImage out)
    {
        return IcyBufferedImageUtil.getARGBImage(this, lut, out);
    }

    /**
     * @deprecated Use {@link IcyBufferedImageUtil#toBufferedImage(IcyBufferedImage, BufferedImage)}
     *             instead.
     */
    @Deprecated
    public BufferedImage getARGBImage(BufferedImage out)
    {
        return IcyBufferedImageUtil.getARGBImage(this, out);
    }

    /**
     * @deprecated Use {@link IcyBufferedImageUtil#getARGBImage(IcyBufferedImage, LUT)} instead.
     */
    @Deprecated
    public BufferedImage getARGBImage(LUT lut)
    {
        return IcyBufferedImageUtil.getARGBImage(this, lut);
    }

    /**
     * @deprecated Use {@link IcyBufferedImageUtil#getARGBImage(IcyBufferedImage)} instead.
     */
    @Deprecated
    public BufferedImage getARGBImage()
    {
        return IcyBufferedImageUtil.getARGBImage(this);
    }

    /**
     * @deprecated Uses
     *             {@link IcyBufferedImageUtil#convertType(IcyBufferedImage, DataType, Scaler[])}
     *             instead.
     */
    @Deprecated
    public IcyBufferedImage convertToType(DataType dataType, Scaler scaler)
    {
        return IcyBufferedImageUtil.convertToType(this, dataType, scaler);
    }

    /**
     * @deprecated Uses
     *             {@link IcyBufferedImageUtil#convertType(IcyBufferedImage,DataType, Scaler[])}
     *             instead.
     */
    @Deprecated
    public IcyBufferedImage convertToType(int dataType, boolean signed, Scaler scaler)
    {
        return IcyBufferedImageUtil.convertToType(this, DataType.getDataType(dataType, signed), scaler);
    }

    /**
     * @deprecated Uses
     *             {@link IcyBufferedImageUtil#convertToType(IcyBufferedImage, DataType, boolean)}
     *             instead.
     */
    @Deprecated
    public IcyBufferedImage convertToType(DataType dataType, boolean rescale)
    {
        return IcyBufferedImageUtil.convertToType(this, dataType, rescale);
    }

    /**
     * @deprecated Uses
     *             {@link IcyBufferedImageUtil#convertToType(IcyBufferedImage,DataType, boolean)}
     *             instead
     */
    @Deprecated
    public IcyBufferedImage convertToType(int dataType, boolean signed, boolean rescale)
    {
        return convertToType(DataType.getDataType(dataType, signed), rescale);
    }

    /**
     * @deprecated Use {@link IcyBufferedImageUtil#toBufferedImage(IcyBufferedImage, int, LUT)}
     *             instead
     */
    @Deprecated
    public BufferedImage convertToBufferedImage(LUT lut, int imageType)
    {
        return IcyBufferedImageUtil.toBufferedImage(this, imageType, lut);
    }

    /**
     * @deprecated Use {@link IcyBufferedImageUtil#toBufferedImage(IcyBufferedImage, int, LUT)}
     *             instead
     */
    @Deprecated
    public BufferedImage convertToBufferedImage(int imageType, LUT lut)
    {
        return IcyBufferedImageUtil.toBufferedImage(this, imageType, lut);
    }

    /**
     * @deprecated Use {@link IcyBufferedImageUtil#getCopy(IcyBufferedImage)} instead
     */
    @Deprecated
    public IcyBufferedImage getCopy()
    {
        return IcyBufferedImageUtil.getCopy(this);
    }

    /**
     * @deprecated Uses
     *             {@link IcyBufferedImageUtil#getSubImage(IcyBufferedImage, int, int, int, int)}
     *             instead
     */
    @Deprecated
    public IcyBufferedImage getSubImageCopy(int x, int y, int w, int h)
    {
        return IcyBufferedImageUtil.getSubImage(this, x, y, w, h);
    }

    /**
     * Not supported on IcyBufferedImage, use getSubImageCopy instead.
     */
    @Deprecated
    @Override
    public IcyBufferedImage getSubimage(int x, int y, int w, int h)
    {
        // IcyBufferedImage doesn't support subImaging (incorrect draw and copy operation)
        throw new UnsupportedOperationException(
                "IcyBufferedImage doesn't support getSubimage method, use getSubImageCopy instead.");

        // return new IcyBufferedImage(getIcyColorModel(), getRaster().createWritableChild(x, y, w,
        // h, 0, 0, null));
    }

    /**
     * Return a single component image corresponding to the component c of current image.<br>
     * This actually create a new image which share its data with internal image
     * so any modifications to one affect the other.<br>
     * if <code>(c == -1)</code> then current image is directly returned<br>
     * if <code>((c == 0) || (sizeC == 1))</code> then current image is directly returned<br>
     * if <code>((c < 0) || (c >= sizeC))</code> then it returns <code>null</code>
     * 
     * @see IcyBufferedImageUtil#extractChannel(IcyBufferedImage, int)
     * @since version 1.0.3.3b
     */
    public IcyBufferedImage getImage(int c)
    {
        if (c == -1)
            return this;

        final int sizeC = getSizeC();

        if ((c < 0) || (c >= sizeC))
            return null;
        if (sizeC == 1)
            return this;

        return new IcyBufferedImage(getWidth(), getHeight(), getDataXY(c), isSignedDataType());
    }

    /**
     * @deprecated Use {@link IcyBufferedImageUtil#extractChannel(IcyBufferedImage, int)} instead.
     */
    @Deprecated
    public IcyBufferedImage extractChannel(int channelNumber)
    {
        return IcyBufferedImageUtil.extractChannel(this, channelNumber);
    }

    /**
     * @deprecated Use {@link IcyBufferedImageUtil#extractChannels(IcyBufferedImage, List)} instead.
     */
    @Deprecated
    public IcyBufferedImage extractChannels(List<Integer> channelNumbers)
    {
        return IcyBufferedImageUtil.extractChannels(this, channelNumbers);
    }

    /**
     * @deprecated Use {@link IcyBufferedImageUtil#extractChannel(IcyBufferedImage, int)} instead
     */
    @Deprecated
    public IcyBufferedImage extractBand(int bandNumber)
    {
        return IcyBufferedImageUtil.extractChannel(this, bandNumber);
    }

    /**
     * @deprecated Use {@link IcyBufferedImageUtil#extractChannels(IcyBufferedImage, List)} instead
     */
    @Deprecated
    public IcyBufferedImage extractBands(List<Integer> bandNumbers)
    {
        return IcyBufferedImageUtil.extractChannels(this, bandNumbers);
    }

    /**
     * @deprecated Uses
     *             {@link IcyBufferedImageUtil#scale(IcyBufferedImage, int, int, boolean, int, int, IcyBufferedImageUtil.FilterType)}
     *             instead.
     */
    @Deprecated
    public IcyBufferedImage getScaledCopy(int width, int height, boolean resizeContent, int xAlign, int yAlign,
            FilterType filterType)
    {
        return IcyBufferedImageUtil.scale(this, width, height, resizeContent, xAlign, yAlign,
                getNewFilterType(filterType));
    }

    /**
     * @deprecated Uses
     *             {@link IcyBufferedImageUtil#scale(IcyBufferedImage, int, int, boolean, int, int)}
     *             instead.
     */
    @Deprecated
    public IcyBufferedImage getScaledCopy(int width, int height, boolean resizeContent, int xAlign, int yAlign)
    {
        return IcyBufferedImageUtil.scale(this, width, height, resizeContent, xAlign, yAlign);
    }

    /**
     * @deprecated Uses
     *             {@link IcyBufferedImageUtil#scale(IcyBufferedImage, int, int, IcyBufferedImageUtil.FilterType)}
     *             instead.
     */
    @Deprecated
    public IcyBufferedImage getScaledCopy(int width, int height, FilterType filterType)
    {
        return IcyBufferedImageUtil.scale(this, width, height, getNewFilterType(filterType));
    }

    /**
     * @deprecated Use {@link IcyBufferedImageUtil#scale(IcyBufferedImage, int, int)} instead.
     */
    @Deprecated
    public IcyBufferedImage getScaledCopy(int width, int height)
    {
        return IcyBufferedImageUtil.scale(this, width, height);
    }

    /**
     * @deprecated Use {@link IcyBufferedImageUtil#translate(IcyBufferedImage, int, int, int)}
     *             instead.
     */
    @Deprecated
    public void translate(int dx, int dy, int channel)
    {
        IcyBufferedImageUtil.translate(this, dx, dy, channel);
    }

    /**
     * @deprecated Use {@link IcyBufferedImageUtil#translate(IcyBufferedImage, int, int)} instead.
     */
    @Deprecated
    public void translate(int dx, int dy)
    {
        IcyBufferedImageUtil.translate(this, dx, dy);
    }

    /**
     * Get calculated image channel bounds (min and max values)
     */
    protected double[] getCalculatedChannelBounds(int channel)
    {
        // don't load data for that, just wait that data is loaded naturally
        if (!isDataInitialized())
            return new double[] {0d, 0d};

        final DataType dataType = getDataType_();

        final boolean signed = dataType.isSigned();
        final Object data = getDataXY(channel);

        final double min = ArrayMath.min(data, signed);
        final double max = ArrayMath.max(data, signed);

        return new double[] {min, max};
    }

    /**
     * Adjust specified bounds depending internal data type
     */
    protected double[] adjustBoundsForDataType(double[] bounds)
    {
        double min, max;

        min = bounds[0];
        max = bounds[1];

        // only for integer data type
        if (!isFloatDataType())
        {
            // we force min to 0 if > 0
            if (min > 0d)
                min = 0d;
            // we force max to 0 if < 0
            if (max < 0d)
                max = 0d;
        }

        final DataType dataType = getDataType_();

        switch (dataType.getJavaType())
        {
            default:
            case BYTE:
                // return default bounds ([0..255] / [-128..127])
                return dataType.getDefaultBounds();

            case SHORT:
            case INT:
            case LONG:
                min = MathUtil.prevPow2((long) min + 1);
                max = MathUtil.nextPow2Mask((long) max);
                break;

            case FLOAT:
            case DOUBLE:
                // if [min..max] is included in [-1..1]
                if ((min >= -1d) && (max <= 1d))
                {
                    min = MathUtil.prevPow10(min);
                    max = MathUtil.nextPow10(max);
                }
                break;
        }

        return new double[] {min, max};
    }

    /**
     * Get the data type minimum value.
     */
    public double getDataTypeMin()
    {
        return getDataType_().getMinValue();
    }

    /**
     * Get the data type maximum value.
     */
    public double getDataTypeMax()
    {
        return getDataType_().getMaxValue();
    }

    /**
     * Get data type bounds (min and max values)
     */
    public double[] getDataTypeBounds()
    {
        return new double[] {getDataTypeMin(), getDataTypeMax()};
    }

    /**
     * Get the minimum type value for the specified channel.
     */
    public double getChannelTypeMin(int channel)
    {
        return getIcyColorModel().getComponentAbsMinValue(channel);
    }

    /**
     * Get the maximum type value for the specified channel.
     */
    public double getChannelTypeMax(int channel)
    {
        return getIcyColorModel().getComponentAbsMaxValue(channel);
    }

    /**
     * Get type bounds (min and max values) for the specified channel.
     */
    public double[] getChannelTypeBounds(int channel)
    {
        return getIcyColorModel().getComponentAbsBounds(channel);
    }

    /**
     * Get type bounds (min and max values) for all channels.
     */
    public double[][] getChannelsTypeBounds()
    {
        final int sizeC = getSizeC();
        final double[][] result = new double[sizeC][];

        for (int c = 0; c < sizeC; c++)
            result[c] = getChannelTypeBounds(c);

        return result;
    }

    /**
     * Get global type bounds (min and max values) for all channels.
     */
    public double[] getChannelsGlobalTypeBounds()
    {
        final int sizeC = getSizeC();
        final double[] result = getChannelTypeBounds(0);

        for (int c = 1; c < sizeC; c++)
        {
            final double[] bounds = getChannelTypeBounds(c);
            result[0] = Math.min(bounds[0], result[0]);
            result[1] = Math.max(bounds[1], result[1]);
        }

        return result;
    }

    /**
     * @deprecated Use {@link #getChannelsGlobalTypeBounds()} instead.
     */
    @Deprecated
    public double[] getChannelTypeGlobalBounds()
    {
        return getChannelsGlobalTypeBounds();
    }

    /**
     * @deprecated Use {@link #getChannelTypeGlobalBounds()} instead.
     */
    @Deprecated
    public double[] getGlobalChannelTypeBounds()
    {
        return getChannelTypeGlobalBounds();
    }

    /**
     * @deprecated Use {@link #getChannelTypeMin(int)} instead.
     */
    @Deprecated
    public double getComponentAbsMinValue(int component)
    {
        return getChannelTypeMin(component);
    }

    /**
     * @deprecated Use {@link #getChannelTypeMax(int)} instead.
     */
    @Deprecated
    public double getComponentAbsMaxValue(int component)
    {
        return getChannelTypeMax(component);
    }

    /**
     * @deprecated Use {@link #getChannelTypeBounds(int)} instead.
     */
    @Deprecated
    public double[] getComponentAbsBounds(int component)
    {
        return getChannelTypeBounds(component);
    }

    /**
     * @deprecated Use {@link #getChannelsTypeBounds()} instead.
     */
    @Deprecated
    public double[][] getComponentsAbsBounds()
    {
        return getChannelsTypeBounds();
    }

    /**
     * @deprecated Use {@link #getGlobalChannelTypeBounds()} instead.
     */
    @Deprecated
    public double[] getGlobalComponentAbsBounds()
    {
        return getChannelTypeGlobalBounds();
    }

    /**
     * Get the minimum value for the specified channel.
     */
    public double getChannelMin(int channel)
    {
        return getIcyColorModel().getComponentUserMinValue(channel);
    }

    /**
     * Get maximum value for the specified channel.
     */
    public double getChannelMax(int channel)
    {
        return getIcyColorModel().getComponentUserMaxValue(channel);
    }

    /**
     * Get bounds (min and max values) for the specified channel.
     */
    public double[] getChannelBounds(int channel)
    {
        return getIcyColorModel().getComponentUserBounds(channel);
    }

    /**
     * Get bounds (min and max values) for all channels.
     */
    public double[][] getChannelsBounds()
    {
        final int sizeC = getSizeC();
        final double[][] result = new double[sizeC][];

        for (int c = 0; c < sizeC; c++)
            result[c] = getChannelBounds(c);

        return result;
    }

    /**
     * Get global bounds (min and max values) for all channels.
     */
    public double[] getChannelsGlobalBounds()
    {
        final int sizeC = getSizeC();
        final double[] result = new double[2];

        result[0] = Double.MAX_VALUE;
        result[1] = -Double.MAX_VALUE;

        for (int c = 0; c < sizeC; c++)
        {
            final double[] bounds = getChannelBounds(c);

            if (bounds[0] < result[0])
                result[0] = bounds[0];
            if (bounds[1] > result[1])
                result[1] = bounds[1];
        }

        return result;
    }

    /**
     * @deprecated Use {@link #getChannelMin(int)} instead.
     */
    @Deprecated
    public double getComponentUserMinValue(int component)
    {
        return getChannelMin(component);
    }

    /**
     * @deprecated Use {@link #getChannelMax(int)} instead.
     */
    @Deprecated
    public double getComponentUserMaxValue(int component)
    {
        return getChannelMax(component);
    }

    /**
     * @deprecated Use {@link #getChannelBounds(int)} instead.
     */
    @Deprecated
    public double[] getComponentUserBounds(int component)
    {
        return getChannelBounds(component);
    }

    /**
     * @deprecated Use {@link #getChannelsBounds()} instead.
     */
    @Deprecated
    public double[][] getComponentsUserBounds()
    {
        return getChannelsBounds();
    }

    /**
     * Set the preferred data type minimum value for the specified channel.
     */
    public void setChannelTypeMin(int channel, double min)
    {
        getIcyColorModel().setComponentAbsMinValue(channel, min);
    }

    /**
     * Set the preferred data type maximum value for the specified channel.
     */
    public void setChannelTypeMax(int channel, double max)
    {
        getIcyColorModel().setComponentAbsMaxValue(channel, max);
    }

    /**
     * /**
     * Set the preferred data type min and max values for the specified channel.
     */
    public void setChannelTypeBounds(int channel, double min, double max)
    {
        getIcyColorModel().setComponentAbsBounds(channel, min, max);
    }

    /**
     * Set the preferred data type bounds (min and max values) for all channels.
     */
    public void setChannelsTypeBounds(double[][] bounds)
    {
        getIcyColorModel().setComponentsAbsBounds(bounds);
    }

    /**
     * @deprecated Use {@link #setChannelTypeMin(int, double)} instead.
     */
    @Deprecated
    public void setComponentAbsMinValue(int component, double min)
    {
        setChannelTypeMin(component, min);
    }

    /**
     * @deprecated Use {@link #setChannelTypeMax(int, double)} instead.
     */
    @Deprecated
    public void setComponentAbsMaxValue(int component, double max)
    {
        setChannelTypeMax(component, max);
    }

    /**
     * @deprecated Use {@link #setChannelTypeBounds(int, double, double)} instead.
     */
    @Deprecated
    public void setComponentAbsBounds(int component, double[] bounds)
    {
        setChannelTypeBounds(component, bounds[0], bounds[1]);
    }

    /**
     * @deprecated Use {@link #setChannelTypeBounds(int, double, double)} instead.
     */
    @Deprecated
    public void setComponentAbsBounds(int component, double min, double max)
    {
        setChannelTypeBounds(component, min, max);
    }

    /**
     * @deprecated Use {@link #setChannelsTypeBounds(double[][])} instead.
     */
    @Deprecated
    public void setComponentsAbsBounds(double[][] bounds)
    {
        setChannelsTypeBounds(bounds);
    }

    /**
     * Set channel minimum value.
     */
    public void setChannelMin(int channel, double min)
    {
        final IcyColorModel cm = getIcyColorModel();

        if ((min < cm.getComponentAbsMinValue(channel)))
            cm.setComponentAbsMinValue(channel, min);
        cm.setComponentUserMinValue(channel, min);
    }

    /**
     * Set channel maximum value.
     */
    public void setChannelMax(int channel, double max)
    {
        final IcyColorModel cm = getIcyColorModel();

        if ((max > cm.getComponentAbsMaxValue(channel)))
            cm.setComponentAbsMinValue(channel, max);
        cm.setComponentUserMaxValue(channel, max);
    }

    /**
     * Set channel bounds (min and max values)
     */
    public void setChannelBounds(int channel, double min, double max)
    {
        final IcyColorModel cm = getIcyColorModel();
        final double[] typeBounds = cm.getComponentAbsBounds(channel);

        if ((min < typeBounds[0]) || (max > typeBounds[1]))
            cm.setComponentAbsBounds(channel, min, max);
        cm.setComponentUserBounds(channel, min, max);
    }

    /**
     * Set all channel bounds (min and max values)
     */
    public void setChannelsBounds(double[][] bounds)
    {
        // we use the setChannelBounds(..) method so we do range check
        for (int c = 0; c < bounds.length; c++)
        {
            final double[] b = bounds[c];
            setChannelBounds(c, b[0], b[1]);
        }
    }

    /**
     * @deprecated Use {@link #setChannelMin(int, double)} instead.
     */
    @Deprecated
    public void setComponentUserMinValue(int component, double min)
    {
        setChannelMin(component, min);
    }

    /**
     * @deprecated Use {@link #setChannelMax(int, double)} instead.
     */
    @Deprecated
    public void setComponentUserMaxValue(int component, double max)
    {
        setChannelMax(component, max);
    }

    /**
     * @deprecated Use {@link #setChannelBounds(int, double, double)} instead.
     */
    @Deprecated
    public void setComponentUserBounds(int component, double[] bounds)
    {
        setChannelBounds(component, bounds[0], bounds[1]);
    }

    /**
     * @deprecated Use {@link #setChannelBounds(int, double, double)} instead
     */
    @Deprecated
    public void setComponentUserBounds(int component, double min, double max)
    {
        setChannelBounds(component, min, max);
    }

    /**
     * @deprecated Use {@link #setChannelsBounds(double[][])} instead.
     */
    @Deprecated
    public void setComponentsUserBounds(double[][] bounds)
    {
        setChannelsBounds(bounds);
    }

    /**
     * Update channels bounds (min and max values).
     */
    public void updateChannelsBounds()
    {
        final IcyColorModel cm = getIcyColorModel();

        if (cm != null)
        {
            final int sizeC = getSizeC();

            for (int c = 0; c < sizeC; c++)
            {
                // get data type bounds
                final double[] bounds = getCalculatedChannelBounds(c);

                cm.setComponentAbsBounds(c, adjustBoundsForDataType(bounds));
                cm.setComponentUserBounds(c, bounds);

                // we do user bounds adjustment on "non ALPHA" component only
                // if (cm.getColorMap(c).getType() != IcyColorMapType.ALPHA)
                // cm.setComponentUserBounds(c, bounds);
            }
        }
    }

    /**
     * @deprecated Use {@link #updateChannelsBounds()} instead.
     */
    @SuppressWarnings("unused")
    @Deprecated
    public void updateComponentsBounds(boolean updateChannelBounds, boolean adjustByteToo)
    {
        updateChannelsBounds();
    }

    /**
     * @deprecated Use {@link #updateChannelsBounds()} instead.
     */
    @SuppressWarnings("unused")
    @Deprecated
    public void updateComponentsBounds(boolean updateUserBounds)
    {
        updateChannelsBounds();
    }

    /**
     * Return true if point is inside the image
     */
    public boolean isInside(Point p)
    {
        return isInside(p.x, p.y);
    }

    /**
     * Return true if point of coordinate (x, y) is inside the image
     */
    public boolean isInside(int x, int y)
    {
        return (x >= 0) && (x < getSizeX()) && (y >= 0) && (y < getSizeY());
    }

    /**
     * Return true if point of coordinate (x, y) is inside the image
     */
    public boolean isInside(double x, double y)
    {
        return (x >= 0) && (x < getSizeX()) && (y >= 0) && (y < getSizeY());
    }

    /**
     * Return the IcyColorModel
     * 
     * @return IcyColorModel
     */
    public IcyColorModel getIcyColorModel()
    {
        return (IcyColorModel) getColorModel();
    }

    /**
     * Return the data type of this image
     * 
     * @return dataType
     * @see DataType
     */
    public DataType getDataType_()
    {
        return getIcyColorModel().getDataType_();
    }

    /**
     * @deprecated use {@link #getDataType_()} instead
     */
    @Deprecated
    public int getDataType()
    {
        return getIcyColorModel().getDataType();
    }

    /**
     * Return true if this is a float data type image
     */
    public boolean isFloatDataType()
    {
        return getDataType_().isFloat();
    }

    /**
     * Return true if this is a signed data type image
     */
    public boolean isSignedDataType()
    {
        return getDataType_().isSigned();
    }

    /**
     * @deprecated Use {@link #getSizeC()} instead.
     */
    @Deprecated
    public int getNumComponents()
    {
        return getSizeC();
    }

    /**
     * @return the number of components of this image
     */
    public int getSizeC()
    {
        return getColorModel().getNumComponents();
    }

    /**
     * @return the width of the image
     */
    public int getSizeX()
    {
        return getWidth();
    }

    /**
     * @return the height of the image
     */
    public int getSizeY()
    {
        return getHeight();
    }

    /**
     * Return 2D dimension of image {sizeX, sizeY}
     */
    public Dimension getDimension()
    {
        return new Dimension(getSizeX(), getSizeY());
    }

    /**
     * Return 2D bounds of image {0, 0, sizeX, sizeY}
     */
    public Rectangle getBounds()
    {
        return new Rectangle(getSizeX(), getSizeY());
    }

    /**
     * Return the number of sample.<br>
     * This is equivalent to<br>
     * <code>getSizeX() * getSizeY() * getSizeC()</code>
     */
    public int getNumSample()
    {
        return getSizeX() * getSizeY() * getSizeC();
    }

    /**
     * Return the offset for specified (x, y) location
     */
    public int getOffset(int x, int y)
    {
        return (y * getWidth()) + x;
    }

    /**
     * create a compatible LUT for this image.
     * 
     * @param createColorModel
     *        set to <code>true</code> to create a LUT using a new compatible ColorModel else it
     *        will use the image
     *        internal ColorModel
     */
    public LUT createCompatibleLUT(boolean createColorModel)
    {
        final IcyColorModel cm;

        if (createColorModel)
            cm = IcyColorModel.createInstance(getIcyColorModel(), false, false);
        else
            cm = getIcyColorModel();

        return new LUT(cm);
    }

    /**
     * create a compatible LUT for this image
     */
    public LUT createCompatibleLUT()
    {
        return createCompatibleLUT(true);
    }

    /**
     * @deprecated No attached LUT to an image.<br/>
     *             Use {@link #createCompatibleLUT(boolean)} instead.
     */
    @Deprecated
    public LUT getLUT()
    {
        return createCompatibleLUT();
    }

    /**
     * Return a direct reference to internal 2D array data [C][XY]
     */
    public Object getDataXYC()
    {
        switch (getDataType_().getJavaType())
        {
            case BYTE:
                return getDataXYCAsByte();
            case SHORT:
                return getDataXYCAsShort();
            case INT:
                return getDataXYCAsInt();
            case FLOAT:
                return getDataXYCAsFloat();
            case DOUBLE:
                return getDataXYCAsDouble();
            default:
                return null;
        }
    }

    /**
     * Return a direct reference to internal 1D array data [XY] for specified c
     */
    public Object getDataXY(int c)
    {
        switch (getDataType_().getJavaType())
        {
            case BYTE:
                return getDataXYAsByte(c);
            case SHORT:
                return getDataXYAsShort(c);
            case INT:
                return getDataXYAsInt(c);
            case FLOAT:
                return getDataXYAsFloat(c);
            case DOUBLE:
                return getDataXYAsDouble(c);
            default:
                return null;
        }
    }

    /**
     * Return a 1D array data copy [XYC] of internal 2D array data [C][XY]
     */
    public Object getDataCopyXYC()
    {
        return getDataCopyXYC(null, 0);
    }

    /**
     * Return a 1D array data copy [XYC] of internal 2D array data [C][XY]<br>
     * If (out != null) then it's used to store result at the specified offset
     */
    public Object getDataCopyXYC(Object out, int offset)
    {
        switch (getDataType_().getJavaType())
        {
            case BYTE:
                return getDataCopyXYCAsByte((byte[]) out, offset);
            case SHORT:
                return getDataCopyXYCAsShort((short[]) out, offset);
            case INT:
                return getDataCopyXYCAsInt((int[]) out, offset);
            case FLOAT:
                return getDataCopyXYCAsFloat((float[]) out, offset);
            case DOUBLE:
                return getDataCopyXYCAsDouble((double[]) out, offset);
            default:
                return null;
        }
    }

    /**
     * Return a 1D array data copy [XY] of internal 1D array data [XY] for specified c
     */
    public Object getDataCopyXY(int c)
    {
        return getDataCopyXY(c, null, 0);
    }

    /**
     * Return a 1D array data copy [XY] of internal 1D array data [XY] for specified c<br>
     * If (out != null) then it's used to store result at the specified offset
     */
    public Object getDataCopyXY(int c, Object out, int offset)
    {
        switch (getDataType_().getJavaType())
        {
            case BYTE:
                return getDataCopyXYAsByte(c, (byte[]) out, offset);
            case SHORT:
                return getDataCopyXYAsShort(c, (short[]) out, offset);
            case INT:
                return getDataCopyXYAsInt(c, (int[]) out, offset);
            case FLOAT:
                return getDataCopyXYAsFloat(c, (float[]) out, offset);
            case DOUBLE:
                return getDataCopyXYAsDouble(c, (double[]) out, offset);
            default:
                return null;
        }
    }

    /**
     * Return a 1D array data copy [CXY] of internal 2D array data [C][XY]
     */
    public Object getDataCopyCXY()
    {
        return getDataCopyCXY(null, 0);
    }

    /**
     * Return a 1D array data copy [CXY] of internal 2D array data [C][XY]<br>
     * If (out != null) then it's used to store result at the specified offset
     */
    public Object getDataCopyCXY(Object out, int offset)
    {
        switch (getDataType_().getJavaType())
        {
            case BYTE:
                return getDataCopyCXYAsByte((byte[]) out, offset);
            case SHORT:
                return getDataCopyCXYAsShort((short[]) out, offset);
            case INT:
                return getDataCopyCXYAsInt((int[]) out, offset);
            case FLOAT:
                return getDataCopyCXYAsFloat((float[]) out, offset);
            case DOUBLE:
                return getDataCopyCXYAsDouble((double[]) out, offset);
            default:
                return null;
        }

    }

    /**
     * Return a 1D array data copy [C] of specified (x, y) position
     */
    public Object getDataCopyC(int x, int y)
    {
        return getDataCopyC(x, y, null, 0);
    }

    /**
     * Return a 1D array data copy [C] of specified (x, y) position<br>
     * If (out != null) then it's used to store result at the specified offset
     */
    public Object getDataCopyC(int x, int y, Object out, int offset)
    {
        switch (getDataType_().getJavaType())
        {
            case BYTE:
                return getDataCopyCAsByte(x, y, (byte[]) out, offset);
            case SHORT:
                return getDataCopyCAsShort(x, y, (short[]) out, offset);
            case INT:
                return getDataCopyCAsInt(x, y, (int[]) out, offset);
            case FLOAT:
                return getDataCopyCAsFloat(x, y, (float[]) out, offset);
            case DOUBLE:
                return getDataCopyCAsDouble(x, y, (double[]) out, offset);
            default:
                return null;
        }
    }

    /**
     * Set internal 1D byte array data ([XY]) for specified component
     */
    public void setDataXY(int c, Object values)
    {
        lockRaster();
        try
        {
            ArrayUtil.arrayToArray(values, getDataXY(c), getDataType_().isSigned());
        }
        finally
        {
            releaseRaster(true);
        }

        // notify data changed
        dataChanged();
    }

    /**
     * Set 1D array data [C] of specified (x, y) position
     */
    public void setDataC(int x, int y, Object values)
    {
        switch (getDataType_().getJavaType())
        {
            case BYTE:
                setDataCAsByte(x, y, (byte[]) values);
                break;

            case SHORT:
                setDataCAsShort(x, y, (short[]) values);
                break;

            case INT:
                setDataCAsInt(x, y, (int[]) values);
                break;

            case FLOAT:
                setDataCAsFloat(x, y, (float[]) values);
                break;

            case DOUBLE:
                setDataCAsDouble(x, y, (double[]) values);
                break;

            default:
                // nothing here
                break;
        }
    }

    /**
     * Return a direct reference to internal 2D array data [C][XY]
     */
    public byte[][] getDataXYCAsByte()
    {
        return ((DataBufferByte) getRaster().getDataBuffer()).getBankData();
    }

    /**
     * Return a direct reference to internal 2D array data [C][XY]
     */
    public short[][] getDataXYCAsShort()
    {
        final DataBuffer db = getRaster().getDataBuffer();
        if (db instanceof DataBufferUShort)
            return ((DataBufferUShort) db).getBankData();
        return ((DataBufferShort) db).getBankData();
    }

    /**
     * Return a direct reference to internal 2D array data [C][XY]
     */
    public int[][] getDataXYCAsInt()
    {
        return ((DataBufferInt) getRaster().getDataBuffer()).getBankData();
    }

    /**
     * Return a direct reference to internal 2D array data [C][XY]
     */
    public float[][] getDataXYCAsFloat()
    {
        return ((DataBufferFloat) getRaster().getDataBuffer()).getBankData();
    }

    /**
     * Return a direct reference to internal 2D array data [C][XY]
     */
    public double[][] getDataXYCAsDouble()
    {
        return ((DataBufferDouble) getRaster().getDataBuffer()).getBankData();
    }

    /**
     * Return a direct reference to internal 1D array data [XY] for specified c
     */
    public byte[] getDataXYAsByte(int c)
    {
        return ((DataBufferByte) getRaster().getDataBuffer()).getData(c);
    }

    /**
     * Return a direct reference to internal 1D array data [XY] for specified c
     */
    public short[] getDataXYAsShort(int c)
    {
        final DataBuffer db = getRaster().getDataBuffer();
        if (db instanceof DataBufferUShort)
            return ((DataBufferUShort) db).getData(c);
        return ((DataBufferShort) db).getData(c);
    }

    /**
     * Return a direct reference to internal 1D array data [XY] for specified c
     */
    public int[] getDataXYAsInt(int c)
    {
        return ((DataBufferInt) getRaster().getDataBuffer()).getData(c);
    }

    /**
     * Return a direct reference to internal 1D array data [XY] for specified c
     */
    public float[] getDataXYAsFloat(int c)
    {
        return ((DataBufferFloat) getRaster().getDataBuffer()).getData(c);
    }

    /**
     * Return a direct reference to internal 1D array data [XY] for specified c
     */
    public double[] getDataXYAsDouble(int c)
    {
        return ((DataBufferDouble) getRaster().getDataBuffer()).getData(c);
    }

    /**
     * Return a 1D array data copy [XYC] of internal 2D array data [C][XY]
     */
    public byte[] getDataCopyXYCAsByte()
    {
        return getDataCopyXYCAsByte(null, 0);
    }

    /**
     * Return a 1D array data copy [XYC] of internal 2D array data [C][XY] If (out != null) then
     * it's used to store result at the specified offset
     */
    public byte[] getDataCopyXYCAsByte(byte[] out, int off)
    {
        final long sizeC = getSizeC();
        final long len = (long) getSizeX() * (long) getSizeY();
        if ((len * sizeC) >= Integer.MAX_VALUE)
            throw new TooLargeArrayException();

        final byte[][] banks = ((DataBufferByte) getRaster().getDataBuffer()).getBankData();
        final byte[] result = Array1DUtil.allocIfNull(out, (int) (len * sizeC));
        int offset = off;

        for (int c = 0; c < sizeC; c++)
        {
            final byte[] src = banks[c];
            System.arraycopy(src, 0, result, offset, (int) len);
            offset += len;
        }

        return result;
    }

    /**
     * Return a 1D array data copy [XYC] of internal 2D array data [C][XY]
     */
    public short[] getDataCopyXYCAsShort()
    {
        return getDataCopyXYCAsShort(null, 0);
    }

    /**
     * Return a 1D array data copy [XYC] of internal 2D array data [C][XY] If (out != null) then
     * it's used to store result at the specified offset
     */
    public short[] getDataCopyXYCAsShort(short[] out, int off)
    {
        final long sizeC = getSizeC();
        final long len = (long) getSizeX() * (long) getSizeY();
        if ((len * sizeC) >= Integer.MAX_VALUE)
            throw new TooLargeArrayException();

        final DataBuffer db = getRaster().getDataBuffer();
        final short[][] banks;
        if (db instanceof DataBufferUShort)
            banks = ((DataBufferUShort) db).getBankData();
        else
            banks = ((DataBufferShort) db).getBankData();
        final short[] result = Array1DUtil.allocIfNull(out, (int) (len * sizeC));
        int offset = off;

        for (int c = 0; c < sizeC; c++)
        {
            final short[] src = banks[c];
            System.arraycopy(src, 0, result, offset, (int) len);
            offset += len;
        }

        return result;
    }

    /**
     * Return a 1D array data copy [XYC] of internal 2D array data [C][XY]
     */
    public int[] getDataCopyXYCAsInt()
    {
        return getDataCopyXYCAsInt(null, 0);
    }

    /**
     * Return a 1D array data copy [XYC] of internal 2D array data [C][XY] If (out != null) then
     * it's used to store result at the specified offset
     */
    public int[] getDataCopyXYCAsInt(int[] out, int off)
    {
        final long sizeC = getSizeC();
        final long len = (long) getSizeX() * (long) getSizeY();
        if ((len * sizeC) >= Integer.MAX_VALUE)
            throw new TooLargeArrayException();

        final int[][] banks = ((DataBufferInt) getRaster().getDataBuffer()).getBankData();
        final int[] result = Array1DUtil.allocIfNull(out, (int) (len * sizeC));
        int offset = off;

        for (int c = 0; c < sizeC; c++)
        {
            final int[] src = banks[c];
            System.arraycopy(src, 0, result, offset, (int) len);
            offset += len;
        }

        return result;
    }

    /**
     * Return a 1D array data copy [XYC] of internal 2D array data [C][XY]
     */
    public float[] getDataCopyXYCAsFloat()
    {
        return getDataCopyXYCAsFloat(null, 0);
    }

    /**
     * Return a 1D array data copy [XYC] of internal 2D array data [C][XY] If (out != null) then
     * it's used to store result at the specified offset
     */
    public float[] getDataCopyXYCAsFloat(float[] out, int off)
    {
        final long sizeC = getSizeC();
        final long len = (long) getSizeX() * (long) getSizeY();
        if ((len * sizeC) >= Integer.MAX_VALUE)
            throw new TooLargeArrayException();

        final float[][] banks = ((DataBufferFloat) getRaster().getDataBuffer()).getBankData();
        final float[] result = Array1DUtil.allocIfNull(out, (int) (len * sizeC));
        int offset = off;

        for (int c = 0; c < sizeC; c++)
        {
            final float[] src = banks[c];
            System.arraycopy(src, 0, result, offset, (int) len);
            offset += len;
        }

        return result;
    }

    /**
     * Return a 1D array data copy [XYC] of internal 2D array data [C][XY]
     */
    public double[] getDataCopyXYCAsDouble()
    {
        return getDataCopyXYCAsDouble(null, 0);
    }

    /**
     * Return a 1D array data copy [XYC] of internal 2D array data [C][XY] If (out != null) then
     * it's used to store result at the specified offset
     */
    public double[] getDataCopyXYCAsDouble(double[] out, int off)
    {
        final long sizeC = getSizeC();
        final long len = (long) getSizeX() * (long) getSizeY();
        if ((len * sizeC) >= Integer.MAX_VALUE)
            throw new TooLargeArrayException();

        final double[][] banks = ((DataBufferDouble) getRaster().getDataBuffer()).getBankData();
        final double[] result = Array1DUtil.allocIfNull(out, (int) (len * sizeC));
        int offset = off;

        for (int c = 0; c < sizeC; c++)
        {
            final double[] src = banks[c];
            System.arraycopy(src, 0, result, offset, (int) len);
            offset += len;
        }

        return result;
    }

    /**
     * Return a 1D array data copy [XY] of internal 1D array data [XY] for specified c<br>
     */
    public byte[] getDataCopyXYAsByte(int c)
    {
        return getDataCopyXYAsByte(c, null, 0);
    }

    /**
     * Return a 1D array data copy [XY] of internal 1D array data [XY] for specified c<br>
     * If (out != null) then it's used to store result at the specified offset
     */
    public byte[] getDataCopyXYAsByte(int c, byte[] out, int off)
    {
        final int len = getSizeX() * getSizeY();
        final byte[] src = ((DataBufferByte) getRaster().getDataBuffer()).getData(c);
        final byte[] result = Array1DUtil.allocIfNull(out, len);

        System.arraycopy(src, 0, result, off, len);

        return result;
    }

    /**
     * Return a 1D array data copy [XY] of internal 1D array data [XY] for specified c<br>
     */
    public short[] getDataCopyXYAsShort(int c)
    {
        return getDataCopyXYAsShort(c, null, 0);
    }

    /**
     * Return a 1D array data copy [XY] of internal 1D array data [XY] for specified c<br>
     * If (out != null) then it's used to store result at the specified offset
     */
    public short[] getDataCopyXYAsShort(int c, short[] out, int off)
    {
        final int len = getSizeX() * getSizeY();
        final DataBuffer db = getRaster().getDataBuffer();
        final short[] src;
        if (db instanceof DataBufferUShort)
            src = ((DataBufferUShort) db).getData(c);
        else
            src = ((DataBufferShort) db).getData(c);
        final short[] result = Array1DUtil.allocIfNull(out, len);

        System.arraycopy(src, 0, result, off, len);

        return result;
    }

    /**
     * Return a 1D array data copy [XY] of internal 1D array data [XY] for specified c<br>
     */
    public int[] getDataCopyXYAsInt(int c)
    {
        return getDataCopyXYAsInt(c, null, 0);
    }

    /**
     * Return a 1D array data copy [XY] of internal 1D array data [XY] for specified c<br>
     * If (out != null) then it's used to store result at the specified offset
     */
    public int[] getDataCopyXYAsInt(int c, int[] out, int off)
    {
        final int len = getSizeX() * getSizeY();
        final int[] src = ((DataBufferInt) getRaster().getDataBuffer()).getData(c);
        final int[] result = Array1DUtil.allocIfNull(out, len);

        System.arraycopy(src, 0, result, off, len);

        return result;
    }

    /**
     * Return a 1D array data copy [XY] of internal 1D array data [XY] for specified c<br>
     */
    public float[] getDataCopyXYAsFloat(int c)
    {
        return getDataCopyXYAsFloat(c, null, 0);
    }

    /**
     * Return a 1D array data copy [XY] of internal 1D array data [XY] for specified c<br>
     * If (out != null) then it's used to store result at the specified offset
     */
    public float[] getDataCopyXYAsFloat(int c, float[] out, int off)
    {
        final int len = getSizeX() * getSizeY();
        final float[] src = ((DataBufferFloat) getRaster().getDataBuffer()).getData(c);
        final float[] result = Array1DUtil.allocIfNull(out, len);

        System.arraycopy(src, 0, result, off, len);

        return result;
    }

    /**
     * Return a 1D array data copy [XY] of internal 1D array data [XY] for specified c<br>
     */
    public double[] getDataCopyXYAsDouble(int c)
    {
        return getDataCopyXYAsDouble(c, null, 0);
    }

    /**
     * Return a 1D array data copy [XY] of internal 1D array data [XY] for specified c<br>
     * If (out != null) then it's used to store result at the specified offset
     */
    public double[] getDataCopyXYAsDouble(int c, double[] out, int off)
    {
        final int len = getSizeX() * getSizeY();
        final double[] src = ((DataBufferDouble) getRaster().getDataBuffer()).getData(c);
        final double[] result = Array1DUtil.allocIfNull(out, len);

        System.arraycopy(src, 0, result, off, len);

        return result;
    }

    /**
     * Return a 1D array data copy [CXY] of internal 2D array data [C][XY]<br>
     */
    public byte[] getDataCopyCXYAsByte()
    {
        return getDataCopyCXYAsByte(null, 0);
    }

    /**
     * Return a 1D array data copy [CXY] of internal 2D array data [C][XY]<br>
     * If (out != null) then it's used to store result at the specified offset
     */
    public byte[] getDataCopyCXYAsByte(byte[] out, int off)
    {
        final long sizeC = getSizeC();
        final long len = (long) getSizeX() * (long) getSizeY();
        if ((len * sizeC) >= Integer.MAX_VALUE)
            throw new TooLargeArrayException();

        final byte[][] banks = ((DataBufferByte) getRaster().getDataBuffer()).getBankData();
        final byte[] result = Array1DUtil.allocIfNull(out, (int) (len * sizeC));

        for (int c = 0; c < sizeC; c++)
        {
            final byte[] src = banks[c];
            int offset = c + off;
            for (int i = 0; i < len; i++, offset += sizeC)
                result[offset] = src[i];
        }

        return result;
    }

    /**
     * Return a 1D array data copy [CXY] of internal 2D array data [C][XY]<br>
     */
    public short[] getDataCopyCXYAsShort()
    {
        return getDataCopyCXYAsShort(null, 0);
    }

    /**
     * Return a 1D array data copy [CXY] of internal 2D array data [C][XY]<br>
     * If (out != null) then it's used to store result at the specified offset
     */
    public short[] getDataCopyCXYAsShort(short[] out, int off)
    {
        final long sizeC = getSizeC();
        final long len = (long) getSizeX() * (long) getSizeY();
        if ((len * sizeC) >= Integer.MAX_VALUE)
            throw new TooLargeArrayException();

        final DataBuffer db = getRaster().getDataBuffer();
        final short[][] banks;
        if (db instanceof DataBufferUShort)
            banks = ((DataBufferUShort) db).getBankData();
        else
            banks = ((DataBufferShort) db).getBankData();
        final short[] result = Array1DUtil.allocIfNull(out, (int) (len * sizeC));

        for (int c = 0; c < sizeC; c++)
        {
            final short[] src = banks[c];
            int offset = c + off;
            for (int i = 0; i < len; i++, offset += sizeC)
                result[offset] = src[i];
        }

        return result;
    }

    /**
     * Return a 1D array data copy [CXY] of internal 2D array data [C][XY]<br>
     */
    public int[] getDataCopyCXYAsInt()
    {
        return getDataCopyCXYAsInt(null, 0);
    }

    /**
     * Return a 1D array data copy [CXY] of internal 2D array data [C][XY]<br>
     * If (out != null) then it's used to store result at the specified offset
     */
    public int[] getDataCopyCXYAsInt(int[] out, int off)
    {
        final long sizeC = getSizeC();
        final long len = (long) getSizeX() * (long) getSizeY();
        if ((len * sizeC) >= Integer.MAX_VALUE)
            throw new TooLargeArrayException();

        final int[][] banks = ((DataBufferInt) getRaster().getDataBuffer()).getBankData();
        final int[] result = Array1DUtil.allocIfNull(out, (int) (len * sizeC));

        for (int c = 0; c < sizeC; c++)
        {
            final int[] src = banks[c];
            int offset = c + off;
            for (int i = 0; i < len; i++, offset += sizeC)
                result[offset] = src[i];
        }

        return result;
    }

    /**
     * Return a 1D array data copy [CXY] of internal 2D array data [C][XY]<br>
     */
    public float[] getDataCopyCXYAsFloat()
    {
        return getDataCopyCXYAsFloat(null, 0);
    }

    /**
     * Return a 1D array data copy [CXY] of internal 2D array data [C][XY]<br>
     * If (out != null) then it's used to store result at the specified offset
     */
    public float[] getDataCopyCXYAsFloat(float[] out, int off)
    {
        final long sizeC = getSizeC();
        final long len = (long) getSizeX() * (long) getSizeY();
        if ((len * sizeC) >= Integer.MAX_VALUE)
            throw new TooLargeArrayException();

        final float[][] banks = ((DataBufferFloat) getRaster().getDataBuffer()).getBankData();
        final float[] result = Array1DUtil.allocIfNull(out, (int) (len * sizeC));

        for (int c = 0; c < sizeC; c++)
        {
            final float[] src = banks[c];
            int offset = c + off;
            for (int i = 0; i < len; i++, offset += sizeC)
                result[offset] = src[i];
        }

        return result;
    }

    /**
     * Return a 1D array data copy [CXY] of internal 2D array data [C][XY]<br>
     */
    public double[] getDataCopyCXYAsDouble()
    {
        return getDataCopyCXYAsDouble(null, 0);
    }

    /**
     * Return a 1D array data copy [CXY] of internal 2D array data [C][XY]<br>
     * If (out != null) then it's used to store result at the specified offset
     */
    public double[] getDataCopyCXYAsDouble(double[] out, int off)
    {
        final long sizeC = getSizeC();
        final long len = (long) getSizeX() * (long) getSizeY();
        if ((len * sizeC) >= Integer.MAX_VALUE)
            throw new TooLargeArrayException();

        final double[][] banks = ((DataBufferDouble) getRaster().getDataBuffer()).getBankData();
        final double[] result = Array1DUtil.allocIfNull(out, (int) (len * sizeC));

        for (int c = 0; c < sizeC; c++)
        {
            final double[] src = banks[c];
            int offset = c + off;
            for (int i = 0; i < len; i++, offset += sizeC)
                result[offset] = src[i];
        }

        return result;
    }

    /**
     * Return a 1D array data copy [C] of specified (x, y) position
     */
    public byte[] getDataCopyCAsByte(int x, int y)
    {
        return getDataCopyCAsByte(x, y, null, 0);
    }

    /**
     * Return a 1D array data copy [C] of specified (x, y) position<br>
     * If (out != null) then it's used to store result at the specified offset
     */
    public byte[] getDataCopyCAsByte(int x, int y, byte[] out, int off)
    {
        final int sizeC = getSizeC();
        final int offset = x + (y * getWidth());
        final byte[][] data = ((DataBufferByte) getRaster().getDataBuffer()).getBankData();
        final byte[] result = Array1DUtil.allocIfNull(out, sizeC);

        for (int c = 0; c < sizeC; c++)
            // ignore band offset as it's always 0 here
            result[c + off] = data[c][offset];

        return result;
    }

    /**
     * Return a 1D array data copy [C] of specified (x, y) position
     */
    public short[] getDataCopyCAsShort(int x, int y)
    {
        return getDataCopyCAsShort(x, y, null, 0);
    }

    /**
     * Return a 1D array data copy [C] of specified (x, y) position<br>
     * If (out != null) then it's used to store result at the specified offset
     */
    public short[] getDataCopyCAsShort(int x, int y, short[] out, int off)
    {
        final int sizeC = getSizeC();
        final int offset = x + (y * getWidth());
        final DataBuffer db = getRaster().getDataBuffer();
        final short[][] data;
        if (db instanceof DataBufferUShort)
            data = ((DataBufferUShort) db).getBankData();
        else
            data = ((DataBufferShort) db).getBankData();
        final short[] result = Array1DUtil.allocIfNull(out, sizeC);

        for (int c = 0; c < sizeC; c++)
            // ignore band offset as it's always 0 here
            result[c + off] = data[c][offset];

        return result;
    }

    /**
     * Return a 1D array data copy [C] of specified (x, y) position
     */
    public int[] getDataCopyCAsInt(int x, int y)
    {
        return getDataCopyCAsInt(x, y, null, 0);
    }

    /**
     * Return a 1D array data copy [C] of specified (x, y) position<br>
     * If (out != null) then it's used to store result at the specified offset
     */
    public int[] getDataCopyCAsInt(int x, int y, int[] out, int off)
    {
        final int sizeC = getSizeC();
        final int offset = x + (y * getWidth());
        final int[][] data = ((DataBufferInt) getRaster().getDataBuffer()).getBankData();
        final int[] result = Array1DUtil.allocIfNull(out, sizeC);

        for (int c = 0; c < sizeC; c++)
            // ignore band offset as it's always 0 here
            result[c + off] = data[c][offset];

        return result;
    }

    /**
     * Return a 1D array data copy [C] of specified (x, y) position
     */
    public float[] getDataCopyCAsFloat(int x, int y)
    {
        return getDataCopyCAsFloat(x, y, null, 0);
    }

    /**
     * Return a 1D array data copy [C] of specified (x, y) position<br>
     * If (out != null) then it's used to store result at the specified offset
     */
    public float[] getDataCopyCAsFloat(int x, int y, float[] out, int off)
    {
        final int sizeC = getSizeC();
        final int offset = x + (y * getWidth());
        final float[][] data = ((DataBufferFloat) getRaster().getDataBuffer()).getBankData();
        final float[] result = Array1DUtil.allocIfNull(out, sizeC);

        for (int c = 0; c < sizeC; c++)
            // ignore band offset as it's always 0 here
            result[c + off] = data[c][offset];

        return result;
    }

    /**
     * Return a 1D array data copy [C] of specified (x, y) position
     */
    public double[] getDataCopyCAsDouble(int x, int y)
    {
        return getDataCopyCAsDouble(x, y, null, 0);
    }

    /**
     * Return a 1D array data copy [C] of specified (x, y) position<br>
     * If (out != null) then it's used to store result at the specified offset
     */
    public double[] getDataCopyCAsDouble(int x, int y, double[] out, int off)
    {
        final int sizeC = getSizeC();
        final int offset = x + (y * getWidth());
        final double[][] data = ((DataBufferDouble) getRaster().getDataBuffer()).getBankData();
        final double[] result = Array1DUtil.allocIfNull(out, sizeC);

        for (int c = 0; c < sizeC; c++)
            // ignore band offset as it's always 0 here
            result[c + off] = data[c][offset];

        return result;
    }

    /**
     * Set internal 1D byte array data ([XY]) for specified component
     */
    public void setDataXYAsByte(int c, byte[] values)
    {
        lockRaster();
        try
        {
            System.arraycopy(values, 0, getDataXYAsByte(c), 0, getSizeX() * getSizeY());
        }
        finally
        {
            releaseRaster(true);
        }

        // notify data changed
        dataChanged();
    }

    /**
     * Set internal 1D byte array data ([XY]) for specified component
     */
    public void setDataXYAsShort(int c, short[] values)
    {
        lockRaster();
        try
        {
            System.arraycopy(values, 0, getDataXYAsShort(c), 0, getSizeX() * getSizeY());
        }
        finally
        {
            releaseRaster(true);
        }

        // notify data changed
        dataChanged();
    }

    /**
     * Set internal 1D byte array data ([XY]) for specified component
     */
    public void setDataXYAsInt(int c, int[] values)
    {
        lockRaster();
        try
        {
            System.arraycopy(values, 0, getDataXYAsInt(c), 0, getSizeX() * getSizeY());
        }
        finally
        {
            releaseRaster(true);
        }

        // notify data changed
        dataChanged();
    }

    /**
     * Set internal 1D byte array data ([XY]) for specified component
     */
    public void setDataXYAsFloat(int c, float[] values)
    {
        lockRaster();
        try
        {
            System.arraycopy(values, 0, getDataXYAsFloat(c), 0, getSizeX() * getSizeY());
        }
        finally
        {
            releaseRaster(true);
        }

        // notify data changed
        dataChanged();
    }

    /**
     * Set internal 1D byte array data ([XY]) for specified component
     */
    public void setDataXYAsDouble(int c, double[] values)
    {
        lockRaster();
        try
        {
            System.arraycopy(values, 0, getDataXYAsDouble(c), 0, getSizeX() * getSizeY());
        }
        finally
        {
            releaseRaster(true);
        }

        // notify data changed
        dataChanged();
    }

    /**
     * Set 1D array data [C] of specified (x, y) position
     */
    public void setDataCAsByte(int x, int y, byte[] values)
    {
        final int offset = x + (y * getWidth());
        final int len = values.length;
        final WritableRaster wr = getRaster();
        final byte[][] data = ((DataBufferByte) wr.getDataBuffer()).getBankData();

        for (int comp = 0; comp < len; comp++)
            // ignore band offset as it's always 0 here
            data[comp][offset] = values[comp];

        // save changed data in cache (need to do cache behind here and still that is terribly slow !!)
        saveRasterInCache(wr);
        // notify data changed
        dataChanged();
    }

    /**
     * Set 1D array data [C] of specified (x, y) position
     */
    public void setDataCAsShort(int x, int y, short[] values)
    {
        final int offset = x + (y * getWidth());
        final int len = values.length;
        final WritableRaster wr = getRaster();
        final DataBuffer db = wr.getDataBuffer();
        final short[][] data;
        if (db instanceof DataBufferUShort)
            data = ((DataBufferUShort) db).getBankData();
        else
            data = ((DataBufferShort) db).getBankData();

        for (int comp = 0; comp < len; comp++)
            // ignore band offset as it's always 0 here
            data[comp][offset] = values[comp];

        // save changed data in cache (need to do cache behind here and still that is terribly slow !!)
        saveRasterInCache(wr);
        // notify data changed
        dataChanged();
    }

    /**
     * Set 1D array data [C] of specified (x, y) position
     */
    public void setDataCAsInt(int x, int y, int[] values)
    {
        final int offset = x + (y * getWidth());
        final int len = values.length;
        final WritableRaster wr = getRaster();
        final int[][] data = ((DataBufferInt) wr.getDataBuffer()).getBankData();

        for (int comp = 0; comp < len; comp++)
            // ignore band offset as it's always 0 here
            data[comp][offset] = values[comp];

        // save changed data in cache (need to do cache behind here and still that is terribly slow !!)
        saveRasterInCache(wr);
        // notify data changed
        dataChanged();
    }

    /**
     * Set 1D array data [C] of specified (x, y) position
     */
    public void setDataCAsFloat(int x, int y, float[] values)
    {
        final int offset = x + (y * getWidth());
        final int len = values.length;
        final WritableRaster wr = getRaster();
        final float[][] data = ((DataBufferFloat) wr.getDataBuffer()).getBankData();

        for (int comp = 0; comp < len; comp++)
            // ignore band offset as it's always 0 here
            data[comp][offset] = values[comp];

        // save changed data in cache (need to do cache behind here and still that is terribly slow !!)
        saveRasterInCache(wr);
        // notify data changed
        dataChanged();
    }

    /**
     * Set 1D array data [C] of specified (x, y) position
     */
    public void setDataCAsDouble(int x, int y, double[] values)
    {
        final int offset = x + (y * getWidth());
        final int len = values.length;
        final WritableRaster wr = getRaster();
        final double[][] data = ((DataBufferDouble) wr.getDataBuffer()).getBankData();

        for (int comp = 0; comp < len; comp++)
            // ignore band offset as it's always 0 here
            data[comp][offset] = values[comp];

        // save changed data in cache (need to do cache behind here and still that is terribly slow !!)
        saveRasterInCache(wr);
        // notify data changed
        dataChanged();
    }

    /**
     * Return the value located at (x, y, c) position as a double
     * whatever is the internal data type
     */
    public double getData(int x, int y, int c)
    {
        return Array1DUtil.getValue(getDataXY(c), getOffset(x, y), getDataType_());
    }

    /**
     * Set the value located at (x, y, c) position as a double
     * whatever is the internal data type
     */
    public void setData(int x, int y, int c, double value)
    {
        lockRaster();
        try
        {
            // set value
            Array1DUtil.setValue(getDataXY(c), getOffset(x, y), getDataType_(), value);
        }
        finally
        {
            // FIXME : save changed data in cache (need to do cache behind here and still that is terribly slow !!)
            releaseRaster(true);
        }

        // notify data changed
        dataChanged();
    }

    /**
     * Returns the data value located at position (x, y, c) as double whatever is the internal data type.<br>
     * The value is interpolated depending the current double (x,y) coordinates.<br>
     * It returns 0d if value is out of range.
     */
    public double getDataInterpolated(double x, double y, int c)
    {
        final int xi = (int) x;
        final int xip = xi + 1;
        final int yi = (int) y;
        final int yip = yi + 1;
        final int sx = getSizeX();
        final int sy = getSizeY();

        double result = 0d;

        // at least one pixel inside
        if ((xi < sx) && (yi < sy) && (xip >= 0) && (yip >= 0))
        {
            final double ratioNextX = x - (double) xi;
            final double ratioCurX = 1d - ratioNextX;
            final double ratioNextY = y - (double) yi;
            final double ratioCurY = 1d - ratioNextY;

            if (yi >= 0)
            {
                if (xi >= 0)
                    result += getData(xi, yi, c) * (ratioCurX * ratioCurY);
                if (xip < sx)
                    result += getData(xip, yi, c) * (ratioNextX * ratioCurY);
            }
            if (yip < sy)
            {
                if (xi >= 0)
                    result += getData(xi, yip, c) * (ratioCurX * ratioNextY);
                if (xip < sx)
                    result += getData(xip, yip, c) * (ratioNextX * ratioNextY);
            }
        }

        return result;
    }

    /**
     * Return the value located at (x, y, c) position
     */
    public byte getDataAsByte(int x, int y, int c)
    {
        // ignore band offset as it's always 0 here
        return (((DataBufferByte) getRaster().getDataBuffer()).getData(c))[x + (y * getWidth())];
    }

    /**
     * Set the value located at (x, y, c) position
     */
    public void setDataAsByte(int x, int y, int c, byte value)
    {
        final WritableRaster wr = getRaster();
        // ignore band offset as it's always 0 here
        (((DataBufferByte) wr.getDataBuffer()).getData(c))[x + (y * getWidth())] = value;
        // save changed data in cache
        saveRasterInCache(wr);
        // notify data changed
        dataChanged();
    }

    /**
     * Return the value located at (x, y, c) position
     */
    public short getDataAsShort(int x, int y, int c)
    {
        // ignore band offset as it's always 0 here
        final DataBuffer db = getRaster().getDataBuffer();

        if (db instanceof DataBufferUShort)
            return (((DataBufferUShort) db).getData(c))[x + (y * getWidth())];

        return (((DataBufferShort) db).getData(c))[x + (y * getWidth())];
    }

    /**
     * Set the value located at (x, y, c) position
     */
    public void setDataAsShort(int x, int y, int c, short value)
    {
        final WritableRaster wr = getRaster();
        final DataBuffer db = wr.getDataBuffer();
        if (db instanceof DataBufferUShort)
            // ignore band offset as it's always 0 here
            (((DataBufferUShort) db).getData(c))[x + (y * getWidth())] = value;
        else
            (((DataBufferShort) db).getData(c))[x + (y * getWidth())] = value;
        // save changed data in cache
        saveRasterInCache(wr);
        // notify data changed
        dataChanged();
    }

    /**
     * Return the value located at (x, y, c) position
     */
    public int getDataAsInt(int x, int y, int c)
    {
        // ignore band offset as it's always 0 here
        return (((DataBufferInt) getRaster().getDataBuffer()).getData(c))[x + (y * getWidth())];
    }

    /**
     * Set the value located at (x, y, c) position
     */
    public void setDataAsInt(int x, int y, int c, int value)
    {
        final WritableRaster wr = getRaster();
        // ignore band offset as it's always 0 here
        (((DataBufferInt) wr.getDataBuffer()).getData(c))[x + (y * getWidth())] = value;
        // save changed data in cache
        saveRasterInCache(wr);
        // notify data changed
        dataChanged();
    }

    /**
     * Return the value located at (x, y, c) position
     */
    public float getDataAsFloat(int x, int y, int c)
    {
        // ignore band offset as it's always 0 here
        return (((DataBufferFloat) getRaster().getDataBuffer()).getData(c))[x + (y * getWidth())];
    }

    /**
     * Set the value located at (x, y, c) position
     */
    public void setDataAsFloat(int x, int y, int c, float value)
    {
        final WritableRaster wr = getRaster();
        // ignore band offset as it's always 0 here
        (((DataBufferFloat) wr.getDataBuffer()).getData(c))[x + (y * getWidth())] = value;
        // save changed data in cache
        saveRasterInCache(wr);
        // notify data changed
        dataChanged();
    }

    /**
     * Return the value located at (x, y, c) position
     */
    public double getDataAsDouble(int x, int y, int c)
    {
        // ignore band offset as it's always 0 here
        return (((DataBufferDouble) getRaster().getDataBuffer()).getData(c))[x + (y * getWidth())];
    }

    /**
     * Set the value located at (x, y, c) position
     */
    public void setDataAsDouble(int x, int y, int c, double value)
    {
        final WritableRaster wr = getRaster();
        // ignore band offset as it's always 0 here
        (((DataBufferDouble) wr.getDataBuffer()).getData(c))[x + (y * getWidth())] = value;
        // save changed data in cache
        saveRasterInCache(wr);
        // notify data changed
        dataChanged();
    }

    /**
     * Same as getRGB but by using the specified LUT instead of internal one
     * 
     * @see java.awt.image.BufferedImage#getRGB(int, int)
     */
    public int getRGB(int x, int y, LUT lut)
    {
        return getIcyColorModel().getRGB(getRaster().getDataElements(x, y, null), lut);
    }

    /**
     * Internal copy data from an icy image (notify data changed)
     * 
     * @param srcImage
     *        source icy image
     * @param srcRect
     *        source region
     * @param dstPt
     *        destination X,Y position
     * @param srcChannel
     *        source channel
     * @param dstChannel
     *        destination channel
     */
    protected void fastCopyData(IcyBufferedImage srcImage, Rectangle srcRect, Point dstPt, int srcChannel,
            int dstChannel)
    {
        final int srcSizeX = srcImage.getSizeX();
        final int dstSizeX = getSizeX();

        // limit to source image size
        Rectangle adjSrcRect = srcRect.intersection(new Rectangle(srcSizeX, srcImage.getSizeY()));
        // negative destination x position
        if (dstPt.x < 0)
            // adjust source rect
            adjSrcRect.x += -dstPt.x;
        // negative destination y position
        if (dstPt.y < 0)
            // adjust source rect
            adjSrcRect.y += -dstPt.y;

        final Rectangle dstRect = new Rectangle(dstPt.x, dstPt.y, adjSrcRect.width, adjSrcRect.height);
        // limit to destination image size
        final Rectangle adjDstRect = dstRect.intersection(new Rectangle(dstSizeX, getSizeY()));

        final int w = Math.min(adjSrcRect.width, adjDstRect.width);
        final int h = Math.min(adjSrcRect.height, adjDstRect.height);

        // nothing to copy
        if ((w == 0) || (h == 0))
            return;

        lockRaster();
        try
        {
            final boolean signed = srcImage.getDataType_().isSigned();
            final Object src = srcImage.getDataXY(srcChannel);
            final Object dst = getDataXY(dstChannel);

            int srcOffset = adjSrcRect.x + (adjSrcRect.y * srcSizeX);
            int dstOffset = adjDstRect.x + (adjDstRect.y * dstSizeX);

            for (int y = 0; y < h; y++)
            {
                ArrayUtil.arrayToArray(src, srcOffset, dst, dstOffset, w, signed);
                srcOffset += srcSizeX;
                dstOffset += dstSizeX;
            }
        }
        finally
        {
            releaseRaster(true);
        }

        // notify data changed
        dataChanged();
    }

    /**
     * Internal copy data from a compatible image (notify data changed)
     * 
     * @param srcImage
     *        source image
     */
    protected void internalCopyData(int srcChannel, int dstChannel, DataBuffer src_db, DataBuffer dst_db, int[] indices,
            int[] band_offsets, int[] bank_offsets, int scanlineStride_src, int pixelStride_src, int maxX, int maxY,
            int decOffsetSrc)
    {
        final int scanlineStride_dst = getSizeX();

        final int bank = indices[srcChannel];
        final int offset = band_offsets[srcChannel] + bank_offsets[bank] - decOffsetSrc;

        switch (getDataType_().getJavaType())
        {
            case BYTE:
            {
                final byte[] src;
                final byte[] dst = ((DataBufferByte) dst_db).getData(dstChannel);

                // LOCI use its own buffer classes
                if (src_db instanceof SignedByteBuffer)
                    src = ((SignedByteBuffer) src_db).getData(bank);
                else
                    src = ((DataBufferByte) src_db).getData(bank);

                int offset_src = offset;
                int offset_dst = 0;
                for (int y = 0; y < maxY; y++)
                {
                    int offset_src_pix = offset_src;
                    int offset_dst_pix = offset_dst;

                    for (int x = 0; x < maxX; x++)
                    {
                        dst[offset_dst_pix] = src[offset_src_pix];
                        offset_src_pix += pixelStride_src;
                        offset_dst_pix++;
                    }

                    offset_src += scanlineStride_src;
                    offset_dst += scanlineStride_dst;
                }
                break;
            }

            case SHORT:
            {
                final short[] src;
                final short[] dst;

                // LOCI use its own buffer classes
                if (src_db instanceof SignedShortBuffer)
                    src = ((SignedShortBuffer) src_db).getData(bank);
                else if (src_db instanceof DataBufferShort)
                    src = ((DataBufferShort) src_db).getData(bank);
                else
                    src = ((DataBufferUShort) src_db).getData(bank);

                if (dst_db instanceof DataBufferShort)
                    dst = ((DataBufferShort) dst_db).getData(dstChannel);
                else
                    dst = ((DataBufferUShort) dst_db).getData(dstChannel);

                int offset_src = offset;
                int offset_dst = 0;
                for (int y = 0; y < maxY; y++)
                {
                    int offset_src_pix = offset_src;
                    int offset_dst_pix = offset_dst;

                    for (int x = 0; x < maxX; x++)
                    {
                        dst[offset_dst_pix] = src[offset_src_pix];
                        offset_src_pix += pixelStride_src;
                        offset_dst_pix++;
                    }

                    offset_src += scanlineStride_src;
                    offset_dst += scanlineStride_dst;
                }
                break;
            }

            case INT:
            {
                final int[] src;
                final int[] dst = ((DataBufferInt) dst_db).getData(dstChannel);

                // LOCI use its own buffer classes
                if (src_db instanceof UnsignedIntBuffer)
                    src = ((UnsignedIntBuffer) src_db).getData(bank);
                else
                    src = ((DataBufferInt) src_db).getData(bank);

                int offset_src = offset;
                int offset_dst = 0;
                for (int y = 0; y < maxY; y++)
                {
                    int offset_src_pix = offset_src;
                    int offset_dst_pix = offset_dst;

                    for (int x = 0; x < maxX; x++)
                    {
                        dst[offset_dst_pix] = src[offset_src_pix];
                        offset_src_pix += pixelStride_src;
                        offset_dst_pix++;
                    }

                    offset_src += scanlineStride_src;
                    offset_dst += scanlineStride_dst;
                }
                break;
            }

            case FLOAT:
            {
                final float[] src = ((DataBufferFloat) src_db).getData(bank);
                final float[] dst = ((DataBufferFloat) dst_db).getData(dstChannel);

                int offset_src = offset;
                int offset_dst = 0;
                for (int y = 0; y < maxY; y++)
                {
                    int offset_src_pix = offset_src;
                    int offset_dst_pix = offset_dst;

                    for (int x = 0; x < maxX; x++)
                    {
                        dst[offset_dst_pix] = src[offset_src_pix];
                        offset_src_pix += pixelStride_src;
                        offset_dst_pix++;
                    }

                    offset_src += scanlineStride_src;
                    offset_dst += scanlineStride_dst;
                }
                break;
            }

            case DOUBLE:
            {
                final double[] src = ((DataBufferDouble) src_db).getData(bank);
                final double[] dst = ((DataBufferDouble) dst_db).getData(dstChannel);

                int offset_src = offset;
                int offset_dst = 0;
                for (int y = 0; y < maxY; y++)
                {
                    int offset_src_pix = offset_src;
                    int offset_dst_pix = offset_dst;

                    for (int x = 0; x < maxX; x++)
                    {
                        dst[offset_dst_pix] = src[offset_src_pix];
                        offset_src_pix += pixelStride_src;
                        offset_dst_pix++;
                    }

                    offset_src += scanlineStride_src;
                    offset_dst += scanlineStride_dst;
                }
                break;
            }

            default:
                // do nothing here
                break;
        }
    }

    /**
     * Copy channel data from a compatible sample model and writable raster (notify data changed).
     * 
     * @param sampleModel
     *        source sample model
     * @param raster
     *        source writable raster to read data from
     * @param srcChannel
     *        source channel (-1 for all channels)
     * @param dstChannel
     *        destination channel (only significant if source channel != -1)
     * @return <code>true</code> if the copy operation succeed, <code>false</code> otherwise
     */
    public boolean copyData(ComponentSampleModel sampleModel, WritableRaster sourceRaster, int srcChannel,
            int dstChannel)
    {
        // not compatible sample model
        if (DataType.getDataTypeFromDataBufferType(sampleModel.getDataType()) != getDataType_())
            return false;

        final DataBuffer src_db = sourceRaster.getDataBuffer();
        final WritableRaster dst_raster = getRaster();
        final DataBuffer dst_db = dst_raster.getDataBuffer();
        final int[] indices = sampleModel.getBankIndices();
        final int[] band_offsets = sampleModel.getBandOffsets();
        final int[] bank_offsets = src_db.getOffsets();
        final int scanlineStride_src = sampleModel.getScanlineStride();
        final int pixelStride_src = sampleModel.getPixelStride();
        final int maxX = Math.min(getSizeX(), sampleModel.getWidth());
        final int maxY = Math.min(getSizeY(), sampleModel.getHeight());
        final int decOffsetSrc = sourceRaster.getSampleModelTranslateX()
                + (sourceRaster.getSampleModelTranslateY() * scanlineStride_src);

        // all channels
        if (srcChannel == -1)
        {
            final int numBands = sampleModel.getNumBands();

            for (int band = 0; band < numBands; band++)
                internalCopyData(band, band, src_db, dst_db, indices, band_offsets, bank_offsets, scanlineStride_src,
                        pixelStride_src, maxX, maxY, decOffsetSrc);
        }
        else
        {
            internalCopyData(srcChannel, dstChannel, src_db, dst_db, indices, band_offsets, bank_offsets,
                    scanlineStride_src, pixelStride_src, maxX, maxY, decOffsetSrc);
        }

        // save in cache changed data
        saveRasterInCache(dst_raster);
        // notify data changed
        dataChanged();

        return true;
    }

    /**
     * Copy data to specified location from an data array.
     * 
     * @param data
     *        source data array (should be same type than image data type)
     * @param dataDim
     *        source data dimension (array length should be >= Dimension.width * Dimension.heigth)
     * @param signed
     *        if the source data array should be considered as signed data (meaningful for integer
     *        data type only)
     * @param dstPt
     *        destination X,Y position (assume [0,0] if null)
     * @param dstChannel
     *        destination channel
     */
    public void copyData(Object data, Dimension dataDim, boolean signed, Point dstPt, int dstChannel)
    {
        if ((data == null) || (dataDim == null))
            return;

        // source image size
        final Rectangle adjSrcRect = new Rectangle(dataDim);
        // negative destination x position
        if (dstPt.x < 0)
            // adjust source rect
            adjSrcRect.x += -dstPt.x;
        // negative destination y position
        if (dstPt.y < 0)
            // adjust source rect
            adjSrcRect.y += -dstPt.y;

        final Rectangle dstRect = new Rectangle(dstPt.x, dstPt.y, adjSrcRect.width, adjSrcRect.height);
        // limit to destination image size
        final Rectangle adjDstRect = dstRect.intersection(new Rectangle(getSizeX(), getSizeY()));

        final int w = Math.min(adjSrcRect.width, adjDstRect.width);
        final int h = Math.min(adjSrcRect.height, adjDstRect.height);

        // nothing to copy
        if ((w == 0) || (h == 0))
            return;

        lockRaster();
        try
        {
            final Object dst = getDataXY(dstChannel);
            final int srcSizeX = dataDim.width;
            final int dstSizeX = getSizeX();

            int srcOffset = adjSrcRect.x + (adjSrcRect.y * srcSizeX);
            int dstOffset = adjDstRect.x + (adjDstRect.y * dstSizeX);

            for (int y = 0; y < h; y++)
            {
                // do data copy (and conversion if needed)
                ArrayUtil.arrayToArray(data, srcOffset, dst, dstOffset, w, signed);
                srcOffset += srcSizeX;
                dstOffset += dstSizeX;
            }
        }
        finally
        {
            releaseRaster(true);
        }

        // notify data changed
        dataChanged();
    }

    /**
     * Copy data from an image (notify data changed)
     * 
     * @param srcImage
     *        source image
     * @param srcRect
     *        source region to copy (assume whole image if null)
     * @param dstPt
     *        destination X,Y position (assume [0,0] if null)
     * @param srcChannel
     *        source channel (-1 for all channels)
     * @param dstChannel
     *        destination channel (only significant if source channel != -1)
     */
    public void copyData(IcyBufferedImage srcImage, Rectangle srcRect, Point dstPt, int srcChannel, int dstChannel)
    {
        if (srcImage == null)
            return;

        final Rectangle adjSrcRect;
        final Point adjDstPt;

        if (srcRect == null)
            adjSrcRect = new Rectangle(srcImage.getSizeX(), srcImage.getSizeY());
        else
            adjSrcRect = srcRect;
        if (dstPt == null)
            adjDstPt = new Point(0, 0);
        else
            adjDstPt = dstPt;

        // copy all possible components
        if (srcChannel == -1)
        {
            final int sizeC = Math.min(srcImage.getSizeC(), getSizeC());

            beginUpdate();
            try
            {
                for (int c = 0; c < sizeC; c++)
                    fastCopyData(srcImage, adjSrcRect, adjDstPt, c, c);
            }
            finally
            {
                endUpdate();
            }
        }
        else
            fastCopyData(srcImage, adjSrcRect, adjDstPt, srcChannel, dstChannel);
    }

    /**
     * Copy data from an image (notify data changed)
     * 
     * @param srcImage
     *        source image
     * @param srcRect
     *        source region to copy (assume whole image if null)
     * @param dstPt
     *        destination (assume [0,0] if null)
     */
    public void copyData(IcyBufferedImage srcImage, Rectangle srcRect, Point dstPt)
    {
        if (srcImage == null)
            return;

        copyData(srcImage, srcRect, dstPt, -1, 0);
    }

    /**
     * Copy data from an image (notify data changed)
     * 
     * @param srcImage
     *        source image
     * @param srcChannel
     *        source channel to copy (-1 for all channels)
     * @param dstChannel
     *        destination channel to receive data (only significant if source channel != -1)
     */
    public void copyData(BufferedImage srcImage, int srcChannel, int dstChannel)
    {
        if (srcImage == null)
            return;

        if (srcImage instanceof IcyBufferedImage)
            copyData(((IcyBufferedImage) srcImage), null, null, srcChannel, dstChannel);
        else
        {
            final boolean done;

            // try to use faster copy for compatible image
            if (srcImage.getSampleModel() instanceof ComponentSampleModel)
                done = copyData((ComponentSampleModel) srcImage.getSampleModel(), srcImage.getRaster(), srcChannel,
                        dstChannel);
            else
                done = false;

            if (!done)
            {
                final WritableRaster wr = getRaster();
                // image not compatible, use generic (and slow) data copy
                srcImage.copyData(wr);
                // save changed data in cache
                saveRasterInCache(wr);
                // notify data changed
                dataChanged();
            }
        }
    }

    /**
     * Copy data from an image (notify data changed)
     * 
     * @param srcImage
     *        source image
     */
    public void copyData(BufferedImage srcImage)
    {
        copyData(srcImage, -1, -1);
    }

    /**
     * Return raw data component as an array of byte
     * 
     * @param c
     *        component index
     * @param out
     *        output array (can be null)
     * @param offset
     *        output offset
     * @param little
     *        little endian order
     */
    public byte[] getRawData(int c, byte[] out, int offset, boolean little)
    {
        // alloc output array if needed
        final byte[] result = Array1DUtil.allocIfNull(out,
                offset + (getSizeX() * getSizeY() * getDataType_().getSize()));

        return ByteArrayConvert.toByteArray(getDataXY(c), 0, result, offset, little);
    }

    /**
     * Return raw data component as an array of byte
     * 
     * @param c
     *        component index
     * @param little
     *        little endian order
     */
    public byte[] getRawData(int c, boolean little)
    {
        return getRawData(c, null, 0, little);
    }

    /**
     * Return raw data for all components as an array of byte
     * 
     * @param out
     *        output array (can be null)
     * @param offset
     *        output offset
     * @param little
     *        little endian order
     */
    public byte[] getRawData(byte[] out, int offset, boolean little)
    {
        final int sizeXY = getSizeX() * getSizeY();
        final int sizeC = getSizeC();
        final int sizeType = getDataType_().getSize();

        // alloc output array if needed
        final byte[] result = Array1DUtil.allocIfNull(out, offset + (sizeC * sizeXY * sizeType));

        int outOff = offset;
        for (int c = 0; c < sizeC; c++)
        {
            getRawData(c, result, outOff, little);
            outOff += sizeXY * sizeType;
        }

        return result;
    }

    /**
     * Return raw data for all components as an array of byte
     * 
     * @param little
     *        little endian order
     */
    public byte[] getRawData(boolean little)
    {
        return getRawData(null, 0, little);
    }

    /**
     * Set raw data component from an array of byte (notify data changed)
     * 
     * @param c
     *        component index
     * @param data
     *        data as byte array
     * @param offset
     *        input offset
     * @param little
     *        little endian order
     */
    public void setRawData(int c, byte[] data, int offset, boolean little)
    {
        if (data == null)
            return;

        lockRaster();
        try
        {
            ByteArrayConvert.byteArrayTo(data, offset, getDataXY(c), 0, -1, little);
        }
        finally
        {
            releaseRaster(true);
        }

        // notify data changed
        dataChanged();
    }

    /**
     * Set raw data component from an array of byte (notify data changed)
     * 
     * @param c
     *        component index
     * @param data
     *        data as byte array
     * @param little
     *        little endian order
     */
    public void setRawData(int c, byte[] data, boolean little)
    {
        setRawData(c, data, 0, little);
    }

    /**
     * Set raw data for all components from an array of byte (notify data changed).<br/>
     * Data are arranged in the following dimension order: XYC
     * 
     * @param data
     *        data as byte array
     * @param offset
     *        input offset
     * @param little
     *        little endian order
     */
    public void setRawData(byte[] data, int offset, boolean little)
    {
        if (data == null)
            return;

        final int sizeXY = getSizeX() * getSizeY();
        final int sizeC = getSizeC();
        final int sizeType = getDataType_().getSize();

        beginUpdate();
        try
        {
            int inOff = offset;
            for (int c = 0; c < sizeC; c++)
            {
                setRawData(c, data, inOff, little);
                inOff += sizeXY * sizeType;
            }
        }
        finally
        {
            endUpdate();
        }
    }

    /**
     * Set raw data for all components from an array of byte (notify data changed)
     * 
     * @param data
     *        data as byte array
     * @param little
     *        little endian order
     */
    public void setRawData(byte[] data, boolean little)
    {
        setRawData(data, 0, little);
    }

    /**
     * Return the colormap of the specified channel.
     */
    public IcyColorMap getColorMap(int channel)
    {
        return getIcyColorModel().getColorMap(channel);
    }

    /**
     * @deprecated Use {@link #getColorMap(int)} instead (different case).
     */
    @Deprecated
    public IcyColorMap getColormap(int channel)
    {
        return getColorMap(channel);
    }

    /**
     * @deprecated Use {@link #setColorMaps(BufferedImage)} instead.
     */
    @Deprecated
    public void copyColormap(BufferedImage srcImage)
    {
        setColorMaps(srcImage);
    }

    /**
     * Set colormaps from specified image.
     */
    public void setColorMaps(BufferedImage srcImage)
    {
        getIcyColorModel().setColorMaps(srcImage.getColorModel());
    }

    /**
     * @deprecated Use {@link #setColorMaps(BufferedImage)} instead (different case).
     */
    @Deprecated
    public void setColormaps(BufferedImage srcImage)
    {
        setColorMaps(srcImage);
    }

    /**
     * Set the colormap for the specified channel.
     * 
     * @param channel
     *        channel we want to set the colormap
     * @param map
     *        source colorspace to copy
     * @param setAlpha
     *        also set the alpha information
     */
    public void setColorMap(int channel, IcyColorMap map, boolean setAlpha)
    {
        getIcyColorModel().setColorMap(channel, map, setAlpha);
    }

    /**
     * Set the colormap for the specified channel.
     * 
     * @param channel
     *        channel we want to set the colormap
     * @param map
     *        source colorspace to copy
     */
    public void setColorMap(int channel, IcyColorMap map)
    {
        getIcyColorModel().setColorMap(channel, map, map.isAlpha());
    }

    /**
     * @deprecated Use {@link #setColorMap(int, IcyColorMap, boolean)} instead.
     */
    @Deprecated
    public void setColormap(int channel, IcyColorMap map)
    {
        setColorMap(channel, map, true);
    }

    /**
     * notify image data has changed
     */
    public void dataChanged()
    {
        updater.changed(new IcyBufferedImageEvent(this, IcyBufferedImageEventType.DATA_CHANGED));
    }

    /**
     * notify image colorMap has changed
     */
    protected void colormapChanged(int component)
    {
        updater.changed(new IcyBufferedImageEvent(this, IcyBufferedImageEventType.COLORMAP_CHANGED, component));
    }

    /**
     * notify image channels bounds has changed
     */
    public void channelBoundsChanged(int channel)
    {
        updater.changed(new IcyBufferedImageEvent(this, IcyBufferedImageEventType.BOUNDS_CHANGED, channel));
    }

    /**
     * @deprecated Use {@link #channelBoundsChanged(int)} instead.
     */
    @Deprecated
    public void componentBoundsChanged(int component)
    {
        channelBoundsChanged(component);
    }

    /**
     * fire change event
     */
    protected void fireChangeEvent(IcyBufferedImageEvent e)
    {
        for (IcyBufferedImageListener listener : new ArrayList<IcyBufferedImageListener>(listeners))
            listener.imageChanged(e);
    }

    public void addListener(IcyBufferedImageListener listener)
    {
        listeners.add(listener);
    }

    public void removeListener(IcyBufferedImageListener listener)
    {
        listeners.remove(listener);
    }

    public void beginUpdate()
    {
        updater.beginUpdate();
    }

    public void endUpdate()
    {
        updater.endUpdate();
    }

    public boolean isUpdating()
    {
        return updater.isUpdating();
    }

    @Override
    public void onChanged(CollapsibleEvent object)
    {
        IcyBufferedImageEvent event = (IcyBufferedImageEvent) object;

        switch (event.getType())
        {
            // do here global process on image data change
            case DATA_CHANGED:
                // update image components bounds
                if (autoUpdateChannelBounds)
                    updateChannelsBounds();
                break;

            // do here global process on image bounds change
            case BOUNDS_CHANGED:
                break;

            // do here global process on image colormap change
            case COLORMAP_CHANGED:
                break;
        }

        // notify listener we have changed
        fireChangeEvent(event);
    }

    @Override
    public void colorModelChanged(IcyColorModelEvent e)
    {
        switch (e.getType())
        {
            case COLORMAP_CHANGED:
                colormapChanged(e.getComponent());
                break;

            case SCALER_CHANGED:
                channelBoundsChanged(e.getComponent());
                break;
        }
    }

    @Override
    public String toString()
    {
        return "IcyBufferedImage: " + getSizeX() + " x " + getSizeY() + " - " + getSizeC() + " ch (" + getDataType_()
                + ")";
    }
}
