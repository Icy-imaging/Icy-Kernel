/**
 * 
 */
package icy.undo;

import javax.swing.undo.UndoableEdit;

/**
 * Use this edit to force end edition collapsing.
 */
public class EndUndoableEdit extends IcyUndoableEdit
{
    public EndUndoableEdit(Object source)
    {
        super(source);
    }

    @Override
    public boolean addEdit(UndoableEdit anEdit)
    {
        if (anEdit instanceof EndUndoableEdit)
            return true;

        return false;
    }

    @Override
    public boolean replaceEdit(UndoableEdit anEdit)
    {
        if (anEdit instanceof EndUndoableEdit)
            return true;

        return false;
    }

    @Override
    public boolean isSignificant()
    {
        return false;
    }
}
