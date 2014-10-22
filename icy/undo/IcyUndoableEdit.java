/**
 * 
 */
package icy.undo;

import icy.resource.icon.IcyIcon;

import javax.swing.undo.UndoableEdit;

/**
 * Icy {@link UndoableEdit} interface
 * 
 * @author Stephane
 */
public interface IcyUndoableEdit extends UndoableEdit
{
    /**
     * Retrieve source of this edit
     */
    public Object getSource();

    /**
     * @return the icon
     */
    public IcyIcon getIcon();

    /**
     * @return <code>true</code> if this edit can be merged with a compatible edit
     */
    public boolean isMergeable();
}