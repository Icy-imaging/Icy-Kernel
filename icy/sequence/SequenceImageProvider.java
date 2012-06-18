/**
 * 
 */
package icy.sequence;

import java.awt.Image;

/**
 * Interface for sequence image provider.
 * 
 * @author Stephane
 */
public interface SequenceImageProvider
{
    public Image getImage(int t, int z);
}
