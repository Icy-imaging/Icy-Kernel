/**
 * 
 */
package icy.sequence;

import java.awt.Image;
import java.util.EventListener;

/**
 * Sequence Model.<br>
 * <br>
 * Basic sequence model (5D image data structure).
 * 
 * @author Stephane
 */
public interface SequenceModel
{
    public static interface SequenceModelListener extends EventListener
    {
        /**
         * Sequence model image changed.
         */
        public void imageChanged();

        /**
         * Sequence dimension image changed.
         */
        public void dimensionChanged();
    }

    /**
     * Get dimension X size
     */
    public int getSizeX();

    /**
     * Get dimension Y size
     */
    public int getSizeY();

    /**
     * Get dimension Z size
     */
    public int getSizeZ();

    /**
     * Get dimension T size
     */
    public int getSizeT();

    /**
     * Get dimension C size
     */
    public int getSizeC();

    /**
     * Get image at position [T, Z]
     */
    public Image getImage(int t, int z);

    /**
     * Get image at position [T, Z, C]
     */
    public Image getImage(int t, int z, int c);

    /**
     * fire model image changed event
     */
    public void fireModelImageChangedEvent();

    /**
     * fire model dimension changed event
     */
    public void fireModelDimensionChangedEvent();

    public void addSequenceModelListener(SequenceModelListener listener);

    public void removeSequenceModelListener(SequenceModelListener listener);
}
