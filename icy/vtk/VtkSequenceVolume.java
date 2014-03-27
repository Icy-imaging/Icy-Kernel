/**
 * 
 */
package icy.vtk;

import icy.sequence.Sequence;
import icy.sequence.SequenceEvent;
import icy.sequence.SequenceListener;
import icy.util.StringUtil;

/**
 * Class to represent a {@link Sequence} as a 3D VTK volume object.
 * 
 * @author Stephane
 */
public class VtkSequenceVolume extends VtkImageVolume implements SequenceListener
{
    protected final Sequence sequence;
    protected int t;
    protected int c;

    public VtkSequenceVolume(Sequence sequence)
    {
        super();

        this.sequence = sequence;

        // default T and C position
        t = 0;
        c = -1;

        // build image
        rebuildImageData();
        // setup volume scaling
        setScale(sequence.getPixelSizeX(), sequence.getPixelSizeY(), sequence.getPixelSizeZ());

        // listen sequence changes
        sequence.addListener(this);
    }

    @Override
    public void release()
    {
        super.release();

        sequence.removeListener(this);
    }

    public int getPositionT()
    {
        return t;
    }

    public void setPositionT(int value)
    {
        if (t != value)
        {
            t = value;
            rebuildImageData();
        }
    }

    public int getPositionC()
    {
        return c;
    }

    public void setPositionC(int value)
    {
        if (c != value)
        {
            c = value;
            rebuildImageData();
        }
    }

    public void setPosition(int t, int c)
    {
        if ((this.t != t) || (this.c != c))
        {
            this.t = t;
            this.c = c;
            rebuildImageData();
        }
    }

    @Override
    public boolean setVolumeMapperType(VtkVolumeMapperType value)
    {
        final boolean result = super.setVolumeMapperType(value);

        if (result)
        {
            // prepare channel position
            int newc = -2;

            // set channel position regarding multi channel support
            if (isMultiChannelVolumeMapper(getVolumeMapperType()))
                newc = -1;
            else if (getPositionC() == -1)
                newc = 0;

            // need to change the channel position ?
            if ((newc != -2) && (newc != getPositionC()))
                setPositionC(newc);
        }

        return result;
    }

    /**
     * Force image data rebuild
     */
    public void rebuildImageData()
    {
        final int posT = getPositionT();
        final int posC = getPositionC();

        // all component ?
        if (posC == -1)
        {
            setVolumeData(sequence.getDataCopyCXYZ(posT), sequence.getDataType_(), sequence.getSizeX(),
                    sequence.getSizeY(), sequence.getSizeZ(), sequence.getSizeC());
        }
        else
        {
            setVolumeData(sequence.getDataCopyXYZ(posT, posC), sequence.getDataType_(), sequence.getSizeX(),
                    sequence.getSizeY(), sequence.getSizeZ(), 1);
        }
    }

    @Override
    public void sequenceChanged(SequenceEvent event)
    {
        switch (event.getSourceType())
        {
            case SEQUENCE_DATA:
                // rebuild image data
                rebuildImageData();
                break;

            case SEQUENCE_META:
                final String metadataName = (String) event.getSource();

                // need to set scale ?
                if (StringUtil.isEmpty(metadataName)
                        || (StringUtil.equals(metadataName, Sequence.ID_PIXEL_SIZE_X)
                                || StringUtil.equals(metadataName, Sequence.ID_PIXEL_SIZE_Y) || StringUtil.equals(
                                metadataName, Sequence.ID_PIXEL_SIZE_Z)))
                    setScale(sequence.getPixelSizeX(), sequence.getPixelSizeY(), sequence.getPixelSizeZ());
                break;

            case SEQUENCE_TYPE:
                break;
        }
    }

    @Override
    public void sequenceClosed(Sequence sequence)
    {
        // avoid to retain memory
        sequence.removeListener(this);
    }
}
