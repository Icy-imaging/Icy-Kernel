/**
 * 
 */
package icy.sequence;

import java.awt.Image;

/**
 * Sequence Model.<br>
 * <br>
 * Basic sequence model (5D image data structure).
 * 
 * @author Stephane
 */
public interface SequenceModel extends SequenceImageProvider
{
    public int getSizeX();

    public int getSizeY();

    public int getSizeZ();

    public int getSizeT();

    public int getSizeC();

    public Image getImage(int t, int z, int c);
}
