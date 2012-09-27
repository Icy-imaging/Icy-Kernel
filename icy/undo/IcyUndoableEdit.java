/**
 * 
 */
package icy.undo;

import icy.resource.ResourceUtil;
import icy.resource.icon.IcyIcon;
import icy.util.StringUtil;

import java.awt.Image;

import javax.swing.UIManager;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;

/**
 * @author Stephane
 */
public class IcyUndoableEdit implements UndoableEdit
{
    private static final IcyIcon DEFAULT_ICON = new IcyIcon(ResourceUtil.ICON_LIGHTING, 16);

    /**
     * Source of the UndoableEdit (weak reference as it should not retain it)
     */
    // final private WeakReference<Object> source;
    private Object source;

    /**
     * Defaults to true; becomes false if this edit is undone, true
     * again if it is redone.
     */
    private boolean hasBeenDone;

    /**
     * True if this edit has not received <code>die</code>; defaults
     * to <code>true</code>.
     */
    private boolean alive;

    private IcyIcon icon;

    /**
     * Creates an <code>UndoableAction</code> which defaults <code>hasBeenDone</code> and
     * <code>alive</code> to <code>true</code>.
     */
    public IcyUndoableEdit(Object source, Image icon)
    {
        super();

        // this.source = new WeakReference<Object>(source);
        this.source = source;
        hasBeenDone = true;
        alive = true;

        if (icon != null)
            this.icon = new IcyIcon(icon, 16);
        else
            this.icon = DEFAULT_ICON;
    }

    /**
     * Creates an <code>UndoableAction</code> which defaults <code>hasBeenDone</code> and
     * <code>alive</code> to <code>true</code>.
     */
    public IcyUndoableEdit(Object source, String iconName)
    {
        this(source, ResourceUtil.getAlphaIconAsImage(iconName));

    }

    /**
     * Creates an <code>UndoableAction</code> which defaults <code>hasBeenDone</code> and
     * <code>alive</code> to <code>true</code>.
     */
    public IcyUndoableEdit(Object source)
    {
        this(source, (Image) null);
    }

    /**
     * Retrieve source of this edit
     */
    public Object getSource()
    {
        // return source.get();
        return source;
    }

    /**
     * @return the icon
     */
    public IcyIcon getIcon()
    {
        return icon;
    }

    /**
     * Sets <code>alive</code> to false. Note that this
     * is a one way operation; dead edits cannot be resurrected.
     * Sending <code>undo</code> or <code>redo</code> to
     * a dead edit results in an exception being thrown.
     * <p>
     * Typically an edit is killed when it is consolidated by another edit's <code>addEdit</code> or
     * <code>replaceEdit</code> method, or when it is dequeued from an <code>UndoManager</code>.
     */
    @Override
    public void die()
    {
        alive = false;
        // clear source reference
        // source.clear();
        source = null;
    }

    /**
     * Throws <code>CannotUndoException</code> if <code>canUndo</code> returns <code>false</code>.
     * Sets <code>hasBeenDone</code> to <code>false</code>. Subclasses should override to undo the
     * operation represented by this edit. Override should begin with
     * a call to super.
     * 
     * @exception CannotUndoException
     *            if <code>canUndo</code> returns <code>false</code>
     * @see #canUndo
     */
    @Override
    public void undo() throws CannotUndoException
    {
        if (!canUndo())
            throw new CannotUndoException();

        hasBeenDone = false;
    }

    /**
     * Returns true if this edit is <code>alive</code> and <code>hasBeenDone</code> is
     * <code>true</code>.
     * 
     * @return true if this edit is <code>alive</code> and <code>hasBeenDone</code> is
     *         <code>true</code>
     * @see #die
     * @see #undo
     * @see #redo
     */
    @Override
    public boolean canUndo()
    {
        checkIsAlive();
        return alive && hasBeenDone;
    }

    /**
     * Throws <code>CannotRedoException</code> if <code>canRedo</code> returns false. Sets
     * <code>hasBeenDone</code> to <code>true</code>.
     * Subclasses should override to redo the operation represented by
     * this edit. Override should begin with a call to super.
     * 
     * @exception CannotRedoException
     *            if <code>canRedo</code> returns <code>false</code>
     * @see #canRedo
     */
    @Override
    public void redo() throws CannotRedoException
    {
        if (!canRedo())
            throw new CannotRedoException();

        hasBeenDone = true;
    }

    /**
     * Returns <code>true</code> if this edit is <code>alive</code> and <code>hasBeenDone</code> is
     * <code>false</code>.
     * 
     * @return <code>true</code> if this edit is <code>alive</code> and <code>hasBeenDone</code> is
     *         <code>false</code>
     * @see #die
     * @see #undo
     * @see #redo
     */
    @Override
    public boolean canRedo()
    {
        checkIsAlive();
        return alive && !hasBeenDone;
    }

    /**
     * This default implementation returns false.
     * 
     * @param anEdit
     *        the edit to be added
     * @return false
     * @see UndoableEdit#addEdit
     */
    @Override
    public boolean addEdit(UndoableEdit anEdit)
    {
        return false;
    }

    /**
     * This default implementation returns false.
     * 
     * @param anEdit
     *        the edit to replace
     * @return false
     * @see UndoableEdit#replaceEdit
     */
    @Override
    public boolean replaceEdit(UndoableEdit anEdit)
    {
        return false;
    }

    /**
     * This default implementation returns true.
     * 
     * @return true
     * @see UndoableEdit#isSignificant
     */
    @Override
    public boolean isSignificant()
    {
        return true;
    }

    /**
     * This default implementation returns "". Used by <code>getUndoPresentationName</code> and
     * <code>getRedoPresentationName</code> to
     * construct the strings they return. Subclasses should override to
     * return an appropriate description of the operation this edit
     * represents.
     * 
     * @return the empty string ""
     * @see #getUndoPresentationName
     * @see #getRedoPresentationName
     */
    @Override
    public String getPresentationName()
    {
        return "";
    }

    /**
     * Retrieves the value from the defaults table with key
     * <code>AbstractUndoableEdit.undoText</code> and returns
     * that value followed by a space, followed by <code>getPresentationName</code>.
     * If <code>getPresentationName</code> returns "",
     * then the defaults value is returned alone.
     * 
     * @return the value from the defaults table with key <code>AbstractUndoableEdit.undoText</code>
     *         , followed
     *         by a space, followed by <code>getPresentationName</code> unless
     *         <code>getPresentationName</code> is "" in which
     *         case, the defaults value is returned alone.
     * @see #getPresentationName
     */
    @Override
    public String getUndoPresentationName()
    {
        String name = getPresentationName();

        if (!StringUtil.isEmpty(name))
            name = UIManager.getString("AbstractUndoableEdit.undoText") + " " + name;
        else
            name = UIManager.getString("AbstractUndoableEdit.undoText");

        return name;
    }

    /**
     * Retrieves the value from the defaults table with key
     * <code>AbstractUndoableEdit.redoText</code> and returns
     * that value followed by a space, followed by <code>getPresentationName</code>.
     * If <code>getPresentationName</code> returns "",
     * then the defaults value is returned alone.
     * 
     * @return the value from the defaults table with key <code>AbstractUndoableEdit.redoText</code>
     *         , followed
     *         by a space, followed by <code>getPresentationName</code> unless
     *         <code>getPresentationName</code> is "" in which
     *         case, the defaults value is returned alone.
     * @see #getPresentationName
     */
    @Override
    public String getRedoPresentationName()
    {
        String name = getPresentationName();

        if (!StringUtil.isEmpty(name))
            name = UIManager.getString("AbstractUndoableEdit.redoText") + " " + name;
        else
            name = UIManager.getString("AbstractUndoableEdit.redoText");

        return name;
    }

    /**
     * Returns a string that displays and identifies this
     * object's properties.
     * 
     * @return a String representation of this object
     */
    @Override
    public String toString()
    {
        return super.toString() + " hasBeenDone: " + hasBeenDone + " alive: " + alive;
    }

    /**
     * Check that source of edit is still alive
     */
    private void checkIsAlive()
    {
        // no more reference on source --> die
        if (getSource() == null)
            die();
    }
}
