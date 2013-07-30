/**
 * 
 */
package icy.gui.main;

import icy.roi.ROI;

import java.util.EventListener;

/**
 * Global {@link ROI} listener class.
 * Used to listen add and remove event for all roi.
 * 
 * @author Stephane
 */
public interface GlobalROIListener extends EventListener
{
    /**
     * A ROI was just added to its first sequence
     */
    public void roiAdded(ROI roi);

    /**
     * A ROI was just removed from its last sequence
     */
    public void roiRemoved(ROI roi);
}
