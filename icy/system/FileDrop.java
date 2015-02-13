/*
 * Copyright 2010-2013 Institut Pasteur.
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
package icy.system;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.TooManyListenersException;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.border.Border;

/**
 * This class makes it easy to drag and drop files from the operating
 * system to a Java program. Any <tt>Component</tt> can be
 * dropped onto, but only <tt>JComponent</tt>s will indicate
 * the drop event with a changed
 * <p/>
 * To use this class, construct a new <tt>FileDrop</tt> by passing it the target component and a
 * <tt>Listener</tt> to receive notification when file(s) have been dropped. Here is an example:
 * <p/>
 * <code><pre>
 *      JPanel myPanel = new JPanel();
 *      new FileDrop( myPanel, new FileDrop.Listener()
 *      {   public void filesDropped( File[] files )
 *          {   
 *              // handle file drop
 *              ...
 *          }   // end filesDropped
 *      }); // end FileDrop.Listener
 * </pre></code>
 * <p/>
 * You can specify the border that will appear when files are being dragged by calling the
 * constructor with a <tt>Border</tt>. Only <tt>JComponent</tt>s will show any indication with a
 * <p/>
 * You can turn on some debugging features by passing a <tt>PrintStream</tt> object (such as
 * <tt>System.out</tt>) into the full constructor. A <tt>null</tt> value will result in no extra
 * debugging information being output.
 * <p/>
 * <p>
 * I'm releasing this code into the Public Domain. Enjoy.
 * </p>
 * <p>
 * <em>Original author: Robert Harder, rharder@usa.net</em>
 * </p>
 * <p>
 * 2007-09-12 Nathan Blomquist -- Linux (KDE/Gnome) support added.<br>
 * 2012-04-12 Stephane Dallogneville -- cleanup, modified for ICY
 * </p>
 * 
 * @author Robert Harder
 * @author rharder@users.sf.net
 * @author Stephane Dallongeville
 * @version 1.0.2
 */
public class FileDrop
{
    public static class TransferableObject implements Transferable
    {
        /**
         * The MIME type for {@link #DATA_FLAVOR} is
         * <tt>application/x-net.iharder.TransferableObject</tt>.
         * 
         * @since 1.1
         */
        public final static String MIME_TYPE = "application/x-net.iharder.TransferableObject";

        /**
         * The default {@link DataFlavor} for {@link TransferableObject} has
         * the representation class <tt>net.iharder.TransferableObject.class</tt> and the MIME
         * type <tt>application/x-net.iharder.TransferableObject</tt>.
         * 
         * @since 1.1
         */
        public final static DataFlavor DATA_FLAVOR = new DataFlavor(TransferableObject.class, MIME_TYPE);

        private Fetcher fetcher;
        private Object data;

        private DataFlavor customFlavor;

        /**
         * Creates a new {@link TransferableObject} that wraps <var>data</var>.
         * Along with the {@link #DATA_FLAVOR} associated with this class,
         * this creates a custom data flavor with a representation class
         * determined from <code>data.getClass()</code> and the MIME type
         * <tt>application/x-net.iharder.TransferableObject</tt>.
         * 
         * @param data
         *        The data to transfer
         * @since 1.1
         */
        public TransferableObject(Object data)
        {
            this.data = data;
            this.customFlavor = new DataFlavor(data.getClass(), MIME_TYPE);
        } // end constructor

        /**
         * Creates a new {@link TransferableObject} that will return the
         * object that is returned by <var>fetcher</var>.
         * No custom data flavor is set other than the default {@link #DATA_FLAVOR}.
         * 
         * @see Fetcher
         * @param fetcher
         *        The {@link Fetcher} that will return the data object
         * @since 1.1
         */
        public TransferableObject(Fetcher fetcher)
        {
            this.fetcher = fetcher;
        } // end constructor

        /**
         * Creates a new {@link TransferableObject} that will return the
         * object that is returned by <var>fetcher</var>.
         * Along with the {@link #DATA_FLAVOR} associated with this class,
         * this creates a custom data flavor with a representation class <var>dataClass</var>
         * and the MIME type <tt>application/x-net.iharder.TransferableObject</tt>.
         * 
         * @see Fetcher
         * @param dataClass
         *        The {@link Class} to use in the custom data flavor
         * @param fetcher
         *        The {@link Fetcher} that will return the data object
         * @since 1.1
         */
        public TransferableObject(Class dataClass, Fetcher fetcher)
        {
            this.fetcher = fetcher;
            this.customFlavor = new DataFlavor(dataClass, MIME_TYPE);
        } // end constructor

        /**
         * Returns the custom {@link DataFlavor} associated
         * with the encapsulated object or <tt>null</tt> if the {@link Fetcher} constructor was used
         * without passing a {@link Class}.
         * 
         * @return The custom data flavor for the encapsulated object
         * @since 1.1
         */
        public DataFlavor getCustomDataFlavor()
        {
            return customFlavor;
        } // end getCustomDataFlavor

        /* ******** T R A N S F E R A B L E M E T H O D S ******** */

        /**
         * Returns a two- or three-element array containing first
         * the custom data flavor, if one was created in the constructors,
         * second the default {@link #DATA_FLAVOR} associated with {@link TransferableObject}, and
         * third the {@link DataFlavor#stringFlavor}.
         * 
         * @return An array of supported data flavors
         * @since 1.1
         */
        @Override
        public DataFlavor[] getTransferDataFlavors()
        {
            if (customFlavor != null)
                return new DataFlavor[] {customFlavor, DATA_FLAVOR, DataFlavor.stringFlavor}; // end
                                                                                              // flavors
            return new DataFlavor[] {DATA_FLAVOR, DataFlavor.stringFlavor}; // end flavors array
        } // end getTransferDataFlavors

        /**
         * Returns the data encapsulated in this {@link TransferableObject}.
         * If the {@link Fetcher} constructor was used, then this is when
         * the {@link Fetcher#getObject getObject()} method will be called.
         * If the requested data flavor is not supported, then the {@link Fetcher#getObject
         * getObject()} method will not be called.
         * 
         * @param flavor
         *        The data flavor for the data to return
         * @return The dropped data
         * @since 1.1
         */
        @Override
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException
        {
            // Native object
            if (flavor.equals(DATA_FLAVOR))
                return fetcher == null ? data : fetcher.getObject();

            // String
            if (flavor.equals(DataFlavor.stringFlavor))
                return fetcher == null ? data.toString() : fetcher.getObject().toString();

            // We can't do anything else
            throw new UnsupportedFlavorException(flavor);
        } // end getTransferData

        /**
         * Returns <tt>true</tt> if <var>flavor</var> is one of the supported
         * flavors. Flavors are supported using the <code>equals(...)</code> method.
         * 
         * @param flavor
         *        The data flavor to check
         * @return Whether or not the flavor is supported
         * @since 1.1
         */
        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor)
        {
            // Native object
            if (flavor.equals(DATA_FLAVOR))
                return true;

            // String
            if (flavor.equals(DataFlavor.stringFlavor))
                return true;

            // We can't do anything else
            return false;
        } // end isDataFlavorSupported

        /* ******** I N N E R - I N T E R F A C E - F E T C H E R ******** */

        /**
         * Instead of passing your data directly to the {@link TransferableObject} constructor, you
         * may want to know exactly when your data was received
         * in case you need to remove it from its source (or do anyting else to it).
         * When the {@link #getTransferData getTransferData(...)} method is called
         * on the {@link TransferableObject}, the {@link Fetcher}'s {@link #getObject getObject()}
         * method will be called.
         * 
         * @author Robert Harder
         * @version 1.1
         * @since 1.1
         */
        public static interface Fetcher
        {
            /**
             * Return the object being encapsulated in the {@link TransferableObject}.
             * 
             * @return The dropped object
             * @since 1.1
             */
            public abstract Object getObject();
        } // end inner interface Fetcher

    } // end class TransferableObject

    transient Border normalBorder;
    transient DropTargetListener dropListener;

    // Default border color
    private static Color defaultBorderColor = new Color(0f, 0f, 1f, 0.25f);

    /**
     * Constructor with a default border and debugging optionally turned on.
     * With Debugging turned on, more status messages will be displayed to <tt>out</tt>. A common
     * way to use this constructor is with <tt>System.out</tt> or <tt>System.err</tt>. A
     * <tt>null</tt> value for
     * the parameter <tt>out</tt> will result in no debugging output.
     * 
     * @param out
     *        PrintStream to record debugging info or null for no debugging.
     * @param c
     *        Component on which files will be dropped.
     * @param listener
     *        Listens for <tt>filesDropped</tt>.
     * @since 1.0
     */
    public FileDrop(final Component c, final FileDropListener listener)
    {
        this(c, BorderFactory.createMatteBorder(2, 2, 2, 2, defaultBorderColor), false, listener);
    } // end constructor

    /**
     * Constructor with a default border, debugging optionally turned on
     * and the option to recursively set drop targets.
     * If your component is a <tt>Container</tt>, then each of its children
     * components will also listen for drops, though only the parent will change borders.
     * With Debugging turned on, more status messages will be displayed to <tt>out</tt>. A common
     * way to use this constructor is with <tt>System.out</tt> or <tt>System.err</tt>. A
     * <tt>null</tt> value for
     * the parameter <tt>out</tt> will result in no debugging output.
     * 
     * @param out
     *        PrintStream to record debugging info or null for no debugging.
     * @param c
     *        Component on which files will be dropped.
     * @param recursive
     *        Recursively set children as drop targets.
     * @param listener
     *        Listens for <tt>filesDropped</tt>.
     * @since 1.0
     */
    public FileDrop(final Component c, final boolean recursive, final FileDropListener listener)
    {
        this(c, BorderFactory.createMatteBorder(2, 2, 2, 2, defaultBorderColor), recursive, listener);
    } // end constructor

    /**
     * Constructor with a specified border
     * 
     * @param c
     *        Component on which files will be dropped.
     * @param dragBorder
     *        Border to use on <tt>JComponent</tt> when dragging occurs.
     * @param listener
     *        Listens for <tt>filesDropped</tt>.
     * @since 1.0
     */
    public FileDrop(final Component c, final Border dragBorder, final FileDropListener listener)
    {
        this(c, dragBorder, false, listener);
    } // end constructor

    /**
     * Constructor with a specified border and the option to recursively set drop targets.
     * If your component is a <tt>Container</tt>, then each of its children
     * components will also listen for drops, though only the parent will change borders.
     * 
     * @param c
     *        Component on which files will be dropped.
     * @param dragBorder
     *        Border to use on <tt>JComponent</tt> when dragging occurs.
     * @param recursive
     *        Recursively set children as drop targets.
     * @param listener
     *        Listens for <tt>filesDropped</tt>.
     * @since 1.0
     */
    public FileDrop(final Component c, final Border dragBorder, final boolean recursive, final FileDropListener listener)
    {
        this(c, dragBorder, recursive, listener, null);
    } // end constructor

    /**
     * Constructor with a specified border and the option to recursively set drop targets.
     * If your component is a <tt>Container</tt>, then each of its children
     * components will also listen for drops, though only the parent will change borders.
     * 
     * @param c
     *        Component on which files will be dropped.
     * @param dragBorder
     *        Border to use on <tt>JComponent</tt> when dragging occurs.
     * @param recursive
     *        Recursively set children as drop targets.
     * @param listener
     *        Listens for <tt>filesDropped</tt>.
     * @since 1.0
     */
    public FileDrop(final Component c, final Border dragBorder, final boolean recursive,
            final FileDropExtListener listener)
    {
        this(c, dragBorder, recursive, null, listener);
    }

    /**
     * Full constructor with a specified border and debugging optionally turned on.
     * With Debugging turned on, more status messages will be displayed to <tt>out</tt>. A common
     * way to use this constructor is with <tt>System.out</tt> or <tt>System.err</tt>. A
     * <tt>null</tt> value for
     * the parameter <tt>out</tt> will result in no debugging output.
     * 
     * @param out
     *        PrintStream to record debugging info or null for no debugging.
     * @param c
     *        Component on which files will be dropped.
     * @param dragBorder
     *        Border to use on <tt>JComponent</tt> when dragging occurs.
     * @param recursive
     *        Recursively set children as drop targets.
     * @param listener
     *        Listens for <tt>filesDropped</tt>.
     * @since 1.0
     */
    FileDrop(final Component c, final Border dragBorder, final boolean recursive, final FileDropListener listener,
            final FileDropExtListener listenerExt)
    {
        // Make a drop listener
        dropListener = new DropTargetListener()
        {
            @Override
            public void dragEnter(DropTargetDragEvent evt)
            {
                // Is this an acceptable drag event?
                if (isDragOk(evt))
                {
                    // If it's a Swing component, set its border
                    if (c instanceof JComponent)
                    {
                        JComponent jc = (JComponent) c;
                        normalBorder = jc.getBorder();
                        jc.setBorder(dragBorder);
                    }

                    // Acknowledge that it's okay to enter
                    // evt.acceptDrag( DnDConstants.ACTION_COPY_OR_MOVE );
                    evt.acceptDrag(DnDConstants.ACTION_COPY);
                }
                else
                    // Reject the drag event
                    evt.rejectDrag();
            }

            @Override
            public void dragOver(DropTargetDragEvent evt)
            { // This is called continually as long as the mouse is
              // over the drag target.
            } // end dragOver

            @Override
            public void drop(DropTargetDropEvent evt)
            {
                try
                { // Get whatever was dropped
                    Transferable tr = evt.getTransferable();

                    // Is it a file list?
                    if (tr.isDataFlavorSupported(DataFlavor.javaFileListFlavor))
                    {
                        // Say we'll take it.
                        // evt.acceptDrop ( DnDConstants.ACTION_COPY_OR_MOVE );
                        evt.acceptDrop(DnDConstants.ACTION_COPY);

                        // Get a useful list
                        List<File> fileList = (List<File>) tr.getTransferData(DataFlavor.javaFileListFlavor);
                        // Iterator<File> iterator = fileList.iterator();

                        // Convert list to array
                        File[] filesTemp = new File[fileList.size()];
                        fileList.toArray(filesTemp);
                        final File[] files = filesTemp;

                        // Alert listener to drop.
                        if (listener != null)
                            listener.filesDropped(files);
                        if (listenerExt != null)
                            listenerExt.filesDropped(evt, files);

                        // Mark that drop is completed.
                        evt.getDropTargetContext().dropComplete(true);
                    }
                    else
                    // this section will check for a reader flavor.
                    {
                        // Thanks, Nathan!
                        // BEGIN 2007-09-12 Nathan Blomquist -- Linux (KDE/Gnome) support added.
                        DataFlavor[] flavors = tr.getTransferDataFlavors();
                        boolean handled = false;
                        for (int zz = 0; zz < flavors.length; zz++)
                        {
                            if (flavors[zz].isRepresentationClassReader())
                            {
                                // Say we'll take it.
                                // evt.acceptDrop (
                                // DnDConstants.ACTION_COPY_OR_MOVE );
                                evt.acceptDrop(DnDConstants.ACTION_COPY);

                                Reader reader = flavors[zz].getReaderForText(tr);

                                BufferedReader br = new BufferedReader(reader);

                                if (listener != null)
                                    listener.filesDropped(createFileArray(br));
                                if (listenerExt != null)
                                    listenerExt.filesDropped(evt, createFileArray(br));

                                // Mark that drop is completed.
                                evt.getDropTargetContext().dropComplete(true);
                                handled = true;
                                break;
                            }
                        }

                        if (!handled)
                            evt.rejectDrop();
                        // END 2007-09-12 Nathan Blomquist -- Linux (KDE/Gnome) support added.
                    }
                }
                catch (IOException io)
                {
                    System.err.println("FileDrop: IOException - abort:");
                    io.printStackTrace();
                    evt.rejectDrop();
                }
                catch (UnsupportedFlavorException ufe)
                {
                    System.err.println("FileDrop: UnsupportedFlavorException - abort:");
                    ufe.printStackTrace();
                    evt.rejectDrop();
                }
                finally
                {
                    // If it's a Swing component, reset its border
                    if (c instanceof JComponent)
                    {
                        JComponent jc = (JComponent) c;
                        jc.setBorder(normalBorder);
                    }
                }
            }

            @Override
            public void dragExit(DropTargetEvent evt)
            {
                // If it's a Swing component, reset its border
                if (c instanceof JComponent)
                {
                    JComponent jc = (JComponent) c;
                    jc.setBorder(normalBorder);
                }
            }

            @Override
            public void dropActionChanged(DropTargetDragEvent evt)
            {
                // Is this an acceptable drag event?
                if (isDragOk(evt))
                    // evt.acceptDrag( DnDConstants.ACTION_COPY_OR_MOVE );
                    evt.acceptDrag(DnDConstants.ACTION_COPY);
                else
                    evt.rejectDrag();
            }
        };

        // Make the component (and possibly children) drop targets
        makeDropTarget(c, recursive);
    }

    // BEGIN 2007-09-12 Nathan Blomquist -- Linux (KDE/Gnome) support added.
    private static String ZERO_CHAR_STRING = "" + (char) 0;

    static File[] createFileArray(BufferedReader bReader)
    {
        try
        {
            List<File> list = new ArrayList<File>();
            String line = null;
            while ((line = bReader.readLine()) != null)
            {
                try
                {
                    // kde seems to append a 0 char to the end of the reader
                    if (ZERO_CHAR_STRING.equals(line))
                        continue;

                    File file = new File(new java.net.URI(line));
                    list.add(file);
                }
                catch (Exception ex)
                {
                    System.err.println("Error with " + line + ": " + ex.getMessage());
                }
            }

            return list.toArray(new File[list.size()]);
        }
        catch (IOException ex)
        {
            System.err.println("FileDrop: IOException");
        }

        return new File[0];
    }

    // END 2007-09-12 Nathan Blomquist -- Linux (KDE/Gnome) support added.

    private void makeDropTarget(final Component c, boolean recursive)
    {
        // Make drop target
        final DropTarget dt = new DropTarget();
        try
        {
            dt.addDropTargetListener(dropListener);
        } // end try
        catch (TooManyListenersException e)
        {
            e.printStackTrace();
            System.err
                    .println("FileDrop: Drop will not work due to previous error. Do you have another listener attached?");
        } // end catch

        // Listen for hierarchy changes and remove the drop target when the parent gets cleared out.
        c.addHierarchyListener(new HierarchyListener()
        {
            @Override
            public void hierarchyChanged(HierarchyEvent evt)
            {
                Component parent = c.getParent();

                if (parent == null)
                    c.setDropTarget(null);
                else
                    new DropTarget(c, dropListener);
            }
        });

        if (c.getParent() != null)
            new DropTarget(c, dropListener);

        if (recursive && (c instanceof Container))
        {
            // Get the container
            Container cont = (Container) c;
            // Get it's components
            Component[] comps = cont.getComponents();

            // Set it's components as listeners also
            for (int i = 0; i < comps.length; i++)
                makeDropTarget(comps[i], recursive);
        }
    }

    /** Determine if the dragged data is a file list. */
    boolean isDragOk(final DropTargetDragEvent evt)
    {
        boolean ok = false;

        // Get data flavors being dragged
        DataFlavor[] flavors = evt.getCurrentDataFlavors();

        // See if any of the flavors are a file list
        int i = 0;
        while (!ok && i < flavors.length)
        {
            // BEGIN 2007-09-12 Nathan Blomquist -- Linux (KDE/Gnome) support added.
            // Is the flavor a file list?
            final DataFlavor curFlavor = flavors[i];
            if (curFlavor.equals(DataFlavor.javaFileListFlavor) || curFlavor.isRepresentationClassReader())
                ok = true;
            // END 2007-09-12 Nathan Blomquist -- Linux (KDE/Gnome) support added.

            i++;
        }

        return ok;
    }

    /**
     * Removes the drag-and-drop hooks from the component and optionally
     * from the all children. You should call this if you add and remove
     * components after you've set up the drag-and-drop.
     * This will recursively unregister all components contained within
     * <var>c</var> if <var>c</var> is a {@link Container}.
     * 
     * @param c
     *        The component to unregister as a drop target
     * @since 1.0
     */
    public static boolean remove(Component c)
    {
        return remove(null, c, true);
    }

    /**
     * Removes the drag-and-drop hooks from the component and optionally
     * from the all children. You should call this if you add and remove
     * components after you've set up the drag-and-drop.
     * 
     * @param out
     *        Optional {@link PrintStream} for logging drag and drop messages
     * @param c
     *        The component to unregister
     * @param recursive
     *        Recursively unregister components within a container
     * @since 1.0
     */
    public static boolean remove(PrintStream out, Component c, boolean recursive)
    {
        // Make sure we support
        c.setDropTarget(null);

        if (recursive && (c instanceof Container))
        {
            Component[] comps = ((Container) c).getComponents();
            for (int i = 0; i < comps.length; i++)
                remove(out, comps[i], recursive);
            return true;
        }

        return false;
    }

    /* ******** I N N E R - I N T E R F A C E L I S T E N E R ******** */

    /**
     * Implement this inner interface to listen for when files are dropped. For example
     * your class declaration may begin like this: <code><pre>
     *      public class MyClass implements FileDrop.Listener
     *      ...
     *      public void filesDropped( File[] files )
     *      {
     *          ...
     *      }   // end filesDropped
     *      ...
     * </pre></code>
     * 
     * @since 1.1
     */
    public static interface FileDropListener
    {

        /**
         * This method is called when files have been successfully dropped.
         * 
         * @param files
         *        An array of <tt>File</tt>s that were dropped.
         * @since 1.0
         */
        public abstract void filesDropped(File[] files);
    }

    /**
     * Implement this inner interface to listen for when files are dropped. For example
     * your class declaration may begin like this: <code><pre>
     *      public class MyClass implements FileDrop.Listener
     *      ...
     *      public void filesDropped( File[] files )
     *      {
     *          ...
     *      }   // end filesDropped
     *      ...
     * </pre></code>
     * 
     * @since 1.1
     */
    public static interface FileDropExtListener
    {
        /**
         * This method is called when files have been successfully dropped.
         * 
         * @param evt
         *        The DropTargetDropEvent which initiated the drop operation.
         * @param files
         *        An array of <tt>File</tt>s that were dropped.
         * @since 2.0
         */
        public abstract void filesDropped(DropTargetDropEvent evt, File[] files);
    }

} // end class FileDrop
