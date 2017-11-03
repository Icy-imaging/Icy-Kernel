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
package icy.painter;

import icy.canvas.IcyCanvas;
import icy.canvas.IcyCanvas2D;
import icy.common.CollapsibleEvent;
import icy.painter.OverlayEvent.OverlayEventType;
import icy.painter.PainterEvent.PainterEventType;
import icy.sequence.Sequence;
import icy.system.thread.ThreadUtil;
import icy.type.point.Point5D;
import icy.util.EventUtil;
import icy.util.ShapeUtil;
import icy.util.XMLUtil;
import icy.vtk.IcyVtkPanel;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EventListener;
import java.util.List;

import org.w3c.dom.Node;

import plugins.kernel.canvas.VtkCanvas;
import vtk.vtkActor;
import vtk.vtkInformation;
import vtk.vtkPolyDataMapper;
import vtk.vtkProp;
import vtk.vtkSphereSource;

/**
 * Anchor2D class, used for 2D point control.
 * 
 * @author Stephane
 */
public class Anchor2D extends Overlay implements VtkPainter, Runnable
{
    /**
     * Interface to listen Anchor2D position change
     */
    public static interface Anchor2DPositionListener extends EventListener
    {
        public void positionChanged(Anchor2D source);
    }

    /**
     * @deprecated Use {@link Anchor2DPositionListener} listener or {@link OverlayListener}
     */
    @Deprecated
    public static interface Anchor2DListener extends PainterListener
    {
        public void positionChanged(Anchor2D source);
    }

    public static class Anchor2DEvent implements CollapsibleEvent
    {
        private final Anchor2D source;

        public Anchor2DEvent(Anchor2D source)
        {
            super();

            this.source = source;
        }

        /**
         * @return the source
         */
        public Anchor2D getSource()
        {
            return source;
        }

        @Override
        public boolean collapse(CollapsibleEvent event)
        {
            if (equals(event))
            {
                // nothing to do here
                return true;
            }

            return false;
        }

        @Override
        public int hashCode()
        {
            return source.hashCode();
        }

        @Override
        public boolean equals(Object obj)
        {
            if (obj instanceof Anchor2DEvent)
            {
                final Anchor2DEvent event = (Anchor2DEvent) obj;

                return (event.getSource() == source);
            }

            return super.equals(obj);
        }
    }

    protected static final String ID_COLOR = "color";
    protected static final String ID_SELECTEDCOLOR = "selected_color";
    protected static final String ID_SELECTED = "selected";
    protected static final String ID_POS_X = "pos_x";
    protected static final String ID_POS_Y = "pos_y";
    protected static final String ID_RAY = "ray";
    protected static final String ID_VISIBLE = "visible";

    public static final int DEFAULT_RAY = 6;
    public static final Color DEFAULT_NORMAL_COLOR = Color.YELLOW;
    public static final Color DEFAULT_SELECTED_COLOR = Color.WHITE;

    public static final String PROPERTY_COLOR = ID_COLOR;
    public static final String PROPERTY_SELECTEDCOLOR = ID_SELECTEDCOLOR;
    public static final String PROPERTY_SELECTED = ID_SELECTED;
    public static final String PROPERTY_RAY = ID_RAY;

    /**
     * position (canvas)
     */
    protected final Point2D.Double position;
    /**
     * position Z (default = -1 = ALL)
     */
    protected int z;

    /**
     * radius
     */
    protected int ray;

    /**
     * color
     */
    protected Color color;
    /**
     * selection color
     */
    protected Color selectedColor;
    /**
     * selection flag
     */
    protected boolean selected;
    /**
     * flag that indicate if anchor is visible
     */
    protected boolean visible;

    /**
     * internals
     */
    protected final Ellipse2D ellipse;
    protected Point2D startDragMousePosition;
    protected Point2D startDragPainterPosition;

    // VTK 3D objects
    vtkSphereSource vtkSource;
    protected vtkPolyDataMapper polyMapper;
    protected vtkActor actor;
    protected vtkInformation vtkInfo;

    // 3D internal
    protected boolean needRebuild;
    protected boolean needPropertiesUpdate;
    protected double scaling[];
    protected WeakReference<VtkCanvas> canvas3d;

    protected final List<Anchor2DListener> anchor2Dlisteners;
    protected final List<Anchor2DPositionListener> anchor2DPositionlisteners;

    public Anchor2D(double x, double y, int ray, Color color, Color selectedColor)
    {
        super("Anchor", OverlayPriority.SHAPE_NORMAL);

        position = new Point2D.Double(x, y);
        z = -1;
        this.ray = ray;
        this.color = color;
        this.selectedColor = selectedColor;
        selected = false;
        visible = true;

        ellipse = new Ellipse2D.Double();
        startDragMousePosition = null;
        startDragPainterPosition = null;

        vtkSource = null;
        polyMapper = null;
        actor = null;
        vtkInfo = null;

        scaling = new double[3];
        Arrays.fill(scaling, 1d);

        needRebuild = true;
        needPropertiesUpdate = false;

        canvas3d = new WeakReference<VtkCanvas>(null);

        anchor2Dlisteners = new ArrayList<Anchor2DListener>();
        anchor2DPositionlisteners = new ArrayList<Anchor2DPositionListener>();
    }

    public Anchor2D(double x, double y, int ray, Color color)
    {
        this(x, y, ray, color, DEFAULT_SELECTED_COLOR);
    }

    /**
     * @param x
     * @param y
     * @param ray
     */
    public Anchor2D(double x, double y, int ray)
    {
        this(x, y, ray, DEFAULT_NORMAL_COLOR, DEFAULT_SELECTED_COLOR);
    }

    /**
     * @param x
     * @param y
     * @param color
     */
    public Anchor2D(double x, double y, Color color, Color selectedColor)
    {
        this(x, y, DEFAULT_RAY, color, selectedColor);
    }

    /**
     * @param x
     * @param y
     * @param color
     */
    public Anchor2D(double x, double y, Color color)
    {
        this(x, y, DEFAULT_RAY, color, DEFAULT_SELECTED_COLOR);
    }

    /**
     * @param x
     * @param y
     */
    public Anchor2D(double x, double y)
    {
        this(x, y, DEFAULT_RAY, DEFAULT_NORMAL_COLOR, DEFAULT_SELECTED_COLOR);
    }

    /**
     */
    public Anchor2D()
    {
        this(0d, 0d, DEFAULT_RAY, DEFAULT_NORMAL_COLOR, DEFAULT_SELECTED_COLOR);
    }

    /**
     * @deprecated Use {@link #Anchor2D(double, double, int, Color, Color)} instead.
     */
    @Deprecated
    public Anchor2D(Point2D position, int ray, Color color, Color selectedColor)
    {
        this(position.getX(), position.getY(), ray, color, selectedColor);
    }

    /**
     * @deprecated Use {@link #Anchor2D(double, double, int, Color)} instead.
     */
    @Deprecated
    public Anchor2D(Point2D position, int ray, Color color)
    {
        this(position.getX(), position.getY(), ray, color, DEFAULT_SELECTED_COLOR);
    }

    /**
     * @deprecated Use {@link #Anchor2D(double, double, int)} instead.
     */
    @Deprecated
    public Anchor2D(Point2D position, int ray)
    {
        this(position.getX(), position.getY(), ray, DEFAULT_NORMAL_COLOR, DEFAULT_SELECTED_COLOR);
    }

    /**
     * @deprecated Use {@link #Anchor2D(double, double, Color, Color)} instead.
     */
    @Deprecated
    public Anchor2D(Point2D position, Color color, Color selectedColor)
    {
        this(position.getX(), position.getY(), DEFAULT_RAY, color, selectedColor);
    }

    /**
     * @deprecated Use {@link #Anchor2D(double, double, Color)} instead.
     */
    @Deprecated
    public Anchor2D(Point2D position, Color color)
    {
        this(position.getX(), position.getY(), DEFAULT_RAY, color, DEFAULT_SELECTED_COLOR);
    }

    /**
     * @deprecated Use {@link #Anchor2D(double, double)} instead.
     */
    @Deprecated
    public Anchor2D(Point2D position)
    {
        this(position.getX(), position.getY(), DEFAULT_RAY, DEFAULT_NORMAL_COLOR, DEFAULT_SELECTED_COLOR);
    }

    /**
     * @deprecated Use {@link Anchor2D#Anchor2D(double, double, int, Color, Color)} instead.
     */
    @Deprecated
    public Anchor2D(Sequence sequence, double x, double y, int ray, Color color, Color selectedColor)
    {
        this(x, y, ray, color, selectedColor);
        sequence.addOverlay(this);
    }

    /**
     * @deprecated Use {@link Anchor2D#Anchor2D(double, double, int, Color, Color)} instead.
     */
    @Deprecated
    public Anchor2D(Sequence sequence, Point2D position, int ray, Color color, Color selectedColor)
    {
        this(position.getX(), position.getY(), ray, color, selectedColor);
        sequence.addOverlay(this);
    }

    /**
     * @deprecated Use {@link Anchor2D#Anchor2D(Point2D, int, Color)} instead.
     */
    @Deprecated
    public Anchor2D(Sequence sequence, Point2D position, int ray, Color color)
    {
        this(position.getX(), position.getY(), ray, color, DEFAULT_SELECTED_COLOR);
        sequence.addOverlay(this);
    }

    /**
     * @deprecated Use {@link Anchor2D#Anchor2D(double, double, int)} instead.
     */
    @Deprecated
    public Anchor2D(Sequence sequence, double x, double y, int ray)
    {
        this(x, y, ray, DEFAULT_NORMAL_COLOR, DEFAULT_SELECTED_COLOR);
        sequence.addOverlay(this);
    }

    /**
     * @deprecated Use {@link Anchor2D#Anchor2D(double, double, Color)} instead.
     */
    @Deprecated
    public Anchor2D(Sequence sequence, Point2D position, Color color)
    {
        this(position.getX(), position.getY(), DEFAULT_RAY, color, DEFAULT_SELECTED_COLOR);
        sequence.addOverlay(this);
    }

    /**
     * @deprecated Use {@link Anchor2D#Anchor2D(double, double, int)} instead.
     */
    @Deprecated
    public Anchor2D(Sequence sequence, Point2D position, int ray)
    {
        this(position.getX(), position.getY(), ray, DEFAULT_NORMAL_COLOR, DEFAULT_SELECTED_COLOR);
        sequence.addOverlay(this);
    }

    /**
     * @deprecated Use {@link Anchor2D#Anchor2D(double, double, int)} instead.
     */
    @Deprecated
    public Anchor2D(Sequence sequence, double x, double y, Color color, Color selectedColor)
    {
        this(x, y, DEFAULT_RAY, color, selectedColor);
        sequence.addOverlay(this);
    }

    /**
     * @deprecated Use {@link Anchor2D#Anchor2D(double, double, Color, Color)} instead.
     */
    @Deprecated
    public Anchor2D(Sequence sequence, Point2D position, Color color, Color selectedColor)
    {
        this(position.getX(), position.getY(), DEFAULT_RAY, color, selectedColor);
        sequence.addOverlay(this);
    }

    /**
     * @deprecated Use {@link Anchor2D#Anchor2D(double, double, Color, Color)} instead.
     */
    @Deprecated
    public Anchor2D(Sequence sequence, double x, double y, Color color)
    {
        this(x, y, DEFAULT_RAY, color, DEFAULT_SELECTED_COLOR);
        sequence.addOverlay(this);
    }

    /**
     * @deprecated Use {@link Anchor2D#Anchor2D(double, double)} instead.
     */
    @Deprecated
    public Anchor2D(Sequence sequence, double x, double y)
    {
        this(sequence, x, y, DEFAULT_RAY, DEFAULT_NORMAL_COLOR, DEFAULT_SELECTED_COLOR);
        sequence.addOverlay(this);
    }

    /**
     * @deprecated Use {@link Anchor2D#Anchor2D(double, double)} instead.
     */
    @Deprecated
    public Anchor2D(Sequence sequence, Point2D position)
    {
        this(position.getX(), position.getY(), DEFAULT_RAY, DEFAULT_NORMAL_COLOR, DEFAULT_SELECTED_COLOR);
        sequence.addOverlay(this);
    }

    /**
     * @deprecated Use {@link Anchor2D#Anchor2D()} instead.
     */
    @Deprecated
    public Anchor2D(Sequence sequence)
    {
        this(0d, 0d, DEFAULT_RAY, DEFAULT_NORMAL_COLOR, DEFAULT_SELECTED_COLOR);
        sequence.addOverlay(this);
    }

    @Override
    protected void finalize() throws Throwable
    {
        super.finalize();

        // release allocated VTK resources
        if (vtkSource != null)
            vtkSource.Delete();
        if (actor != null)
        {
            actor.SetPropertyKeys(null);
            actor.Delete();
        }
        if (vtkInfo != null)
        {
            vtkInfo.Remove(VtkCanvas.visibilityKey);
            vtkInfo.Delete();
        }
        if (polyMapper != null)
            polyMapper.Delete();
    }

    /**
     * @return the x
     */
    public double getX()
    {
        return position.x;
    }

    /**
     * @param x
     *        the x to set
     */
    public void setX(double x)
    {
        setPosition(x, position.y);
    }

    /**
     * @return the y
     */
    public double getY()
    {
        return position.y;
    }

    /**
     * @param y
     *        the y to set
     */
    public void setY(double y)
    {
        setPosition(position.x, y);
    }

    /**
     * Get anchor position (return the internal reference)
     */
    public Point2D getPositionInternal()
    {
        return position;
    }

    public Point2D getPosition()
    {
        return new Point2D.Double(position.x, position.y);
    }

    public double getPositionX()
    {
        return position.x;
    }

    public double getPositionY()
    {
        return position.y;
    }

    public void moveTo(Point2D p)
    {
        setPosition(p.getX(), p.getY());
    }

    public void moveTo(double x, double y)
    {
        setPosition(x, y);
    }

    public void setPosition(Point2D p)
    {
        setPosition(p.getX(), p.getY());
    }

    public void setPosition(double x, double y)
    {
        if ((position.x != x) || (position.y != y))
        {
            position.x = x;
            position.y = y;

            needRebuild = true;
            positionChanged();
            painterChanged();
        }
    }

    public void translate(double dx, double dy)
    {
        setPosition(position.x + dx, position.y + dy);
    }

    /**
     * @return Z position (-1 = ALL)
     */
    public int getZ()
    {
        return z;
    }

    /**
     * Set Z position (-1 = ALL)
     */
    public void setZ(int value)
    {
        final int v;

        // special value for infinite dimension --> change to -1
        if (value == Integer.MIN_VALUE)
            v = -1;
        else
            v = value;

        if (this.z != v)
        {
            this.z = v;

            needRebuild = true;
            painterChanged();
            // FIXME: do this really need to send a position change event ?
            positionChanged();
        }
    }

    /**
     * @return the ray
     */
    public int getRay()
    {
        return ray;
    }

    /**
     * @param value
     *        the ray to set
     */
    public void setRay(int value)
    {
        if (ray != value)
        {
            ray = value;
            needRebuild = true;
            painterChanged();
        }
    }

    /**
     * @return the color
     */
    public Color getColor()
    {
        return color;
    }

    /**
     * @param value
     *        the color to set
     */
    public void setColor(Color value)
    {
        if (color != value)
        {
            color = value;
            needPropertiesUpdate = true;
            propertyChanged(PROPERTY_COLOR);
            painterChanged();
        }
    }

    /**
     * @return the selectedColor
     */
    public Color getSelectedColor()
    {
        return selectedColor;
    }

    /**
     * @param value
     *        the selectedColor to set
     */
    public void setSelectedColor(Color value)
    {
        if (selectedColor != value)
        {
            selectedColor = value;
            needPropertiesUpdate = true;
            propertyChanged(PROPERTY_SELECTEDCOLOR);
            painterChanged();
        }
    }

    /**
     * @return the selected
     */
    public boolean isSelected()
    {
        return selected;
    }

    /**
     * @param value
     *        the selected to set
     */
    public void setSelected(boolean value)
    {
        if (selected != value)
        {
            selected = value;

            // end drag
            if (!value)
                startDragMousePosition = null;

            needPropertiesUpdate = true;
            propertyChanged(PROPERTY_SELECTED);
            painterChanged();
        }
    }

    // /**
    // * @return the canRemove
    // */
    // public boolean isCanRemove()
    // {
    // return canRemove;
    // }
    //
    // /**
    // * @param value
    // * the canRemove to set
    // */
    // public void setCanRemove(boolean value)
    // {
    // canRemove = value;
    // }

    /**
     * @return the visible
     */
    public boolean isVisible()
    {
        return visible;
    }

    /**
     * @param value
     *        the visible to set
     */
    public void setVisible(boolean value)
    {
        if (visible != value)
        {
            visible = value;
            needPropertiesUpdate = true;
            painterChanged();
        }
    }

    protected void initVtkObjects()
    {
        // init 3D painters stuff
        vtkSource = new vtkSphereSource();
        vtkSource.SetRadius(getRay());
        vtkSource.SetThetaResolution(12);
        vtkSource.SetPhiResolution(12);

        polyMapper = new vtkPolyDataMapper();
        polyMapper.SetInputConnection((vtkSource).GetOutputPort());

        actor = new vtkActor();
        actor.SetMapper(polyMapper);

        // use vtkInformations to store outline visibility state (hacky)
        vtkInfo = new vtkInformation();
        vtkInfo.Set(VtkCanvas.visibilityKey, 0);
        // VtkCanvas use this to restore correctly outline visibility flag
        actor.SetPropertyKeys(vtkInfo);

        // initialize color
        final Color col = getColor();
        actor.GetProperty().SetColor(col.getRed() / 255d, col.getGreen() / 255d, col.getBlue() / 255d);
    }

    /**
     * update 3D painter for 3D canvas (called only when VTK is loaded).
     */
    protected boolean rebuildVtkObjects()
    {
        final VtkCanvas canvas = canvas3d.get();
        // canvas was closed
        if (canvas == null)
            return false;

        final IcyVtkPanel vtkPanel = canvas.getVtkPanel();
        // canvas was closed
        if (vtkPanel == null)
            return false;

        final Sequence seq = canvas.getSequence();
        // nothing to update
        if (seq == null)
            return false;

        final Point2D pos = getPosition();
        double curZ = getZ();

        // all slices ?
        if (curZ == -1d)
            // set object at middle of the volume
            curZ = seq.getSizeZ() / 2d;

        // actor can be accessed in canvas3d for rendering so we need to synchronize access
        vtkPanel.lock();
        try
        {
            // need to handle scaling on radius and position to keep a "round" sphere (else we obtain ellipsoid)
            vtkSource.SetCenter(pos.getX() * scaling[0], pos.getY() * scaling[1], (curZ + 0.5d) * scaling[2]);
            polyMapper.Update();

            // actor.SetScale(scaling);
        }
        finally
        {
            vtkPanel.unlock();
        }

        // need to repaint
        painterChanged();

        return true;
    }

    protected void updateVtkDisplayProperties()
    {
        if (actor == null)
            return;

        final VtkCanvas cnv = canvas3d.get();
        final Color col = isSelected() ? getSelectedColor() : getColor();
        final double r = col.getRed() / 255d;
        final double g = col.getGreen() / 255d;
        final double b = col.getBlue() / 255d;
        // final float opacity = getOpacity();

        final IcyVtkPanel vtkPanel = (cnv != null) ? cnv.getVtkPanel() : null;

        // we need to lock canvas as actor can be accessed during rendering
        if (vtkPanel != null)
            vtkPanel.lock();
        try
        {
            actor.GetProperty().SetColor(r, g, b);
            if (isVisible())
            {
                actor.SetVisibility(1);
                vtkInfo.Set(VtkCanvas.visibilityKey, 1);
            }
            else
            {
                actor.SetVisibility(0);
                vtkInfo.Set(VtkCanvas.visibilityKey, 0);
            }
        }
        finally
        {
            if (vtkPanel != null)
                vtkPanel.unlock();
        }
        // need to repaint
        painterChanged();
    }

    protected void updateVtkRadius()
    {
        final VtkCanvas canvas = canvas3d.get();
        // canvas was closed
        if (canvas == null)
            return;

        final IcyVtkPanel vtkPanel = canvas.getVtkPanel();
        // canvas was closed
        if (vtkPanel == null)
            return;

        if (vtkSource == null)
            return;

        // update sphere radius base on canvas scale X
        final double radius = getAdjRay(canvas) * scaling[0];

        if (vtkSource.GetRadius() != radius)
        {
            // actor can be accessed in canvas3d for rendering so we need to synchronize access
            vtkPanel.lock();
            try
            {
                vtkSource.SetRadius(radius);
                vtkSource.Update();
            }
            finally
            {
                vtkPanel.unlock();
            }

            // need to repaint
            painterChanged();
        }
    }

    @Override
    public vtkProp[] getProps()
    {
        // initialize VTK objects if not yet done
        if (actor == null)
            initVtkObjects();

        return new vtkActor[] {actor};
    }

    /**
     * Returns <code>true</code> if specified Point3D is over the anchor in the specified canvas
     */
    public boolean isOver(IcyCanvas canvas, Point2D imagePoint)
    {
        updateEllipseForCanvas(canvas);

        // specific VTK canvas processing
        if (canvas instanceof VtkCanvas)
        {
            // faster to use picked object
            return (actor != null) && (actor == ((VtkCanvas) canvas).getPickedObject());
        }

        // at this point we need image position
        if (imagePoint == null)
            return false;

        // fast contains test to start with
        if (ellipse.getBounds2D().contains(imagePoint))
            return ellipse.contains(imagePoint);

        return false;
    }

    public boolean isOver(IcyCanvas canvas, double x, double y)
    {
        return isOver(canvas, new Point2D.Double(x, y));
    }

    protected double getAdjRay(IcyCanvas canvas)
    {
        // assume X dimension is ok here
        return canvas.canvasToImageLogDeltaX(ray);
    }

    /**
     * Update internal ellipse for specified Canvas
     */
    protected void updateEllipseForCanvas(IcyCanvas canvas)
    {
        // specific VTK canvas processing
        if (canvas instanceof VtkCanvas)
        {
            // 3D canvas
            final VtkCanvas cnv = (VtkCanvas) canvas;
            // update reference if needed
            if (canvas3d.get() != cnv)
                canvas3d = new WeakReference<VtkCanvas>(cnv);

            // FIXME : need a better implementation
            final double[] s = cnv.getVolumeScale();

            // scaling changed ?
            if (!Arrays.equals(scaling, s))
            {
                // update scaling
                scaling = s;
                // need rebuild
                needRebuild = true;
            }

            if (needRebuild)
            {
                // initialize VTK objects if not yet done
                if (actor == null)
                    initVtkObjects();

                // request rebuild 3D objects
                ThreadUtil.runSingle(this);
                needRebuild = false;
            }

            if (needPropertiesUpdate)
            {
                updateVtkDisplayProperties();
                needPropertiesUpdate = false;
            }

            // update sphere radius
            updateVtkRadius();
        }
        else
        {
            final double adjRay = getAdjRay(canvas);

            ellipse.setFrame(position.x - adjRay, position.y - adjRay, adjRay * 2, adjRay * 2);
        }
    }

    protected boolean updateDrag(InputEvent e, double x, double y)
    {
        // not dragging --> exit
        if (startDragMousePosition == null)
            return false;

        double dx = x - startDragMousePosition.getX();
        double dy = y - startDragMousePosition.getY();

        // shift action --> limit to one direction
        if (EventUtil.isShiftDown(e))
        {
            // X drag
            if (Math.abs(dx) > Math.abs(dy))
                dy = 0;
            // Y drag
            else
                dx = 0;
        }

        // set new position
        setPosition(startDragPainterPosition.getX() + dx, startDragPainterPosition.getY() + dy);

        return true;
    }

    protected boolean updateDrag(InputEvent e, Point2D pt)
    {
        return updateDrag(e, pt.getX(), pt.getY());
    }

    /**
     * called when anchor position has changed
     */
    protected void positionChanged()
    {
        updater.changed(new Anchor2DEvent(this));
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onChanged(CollapsibleEvent object)
    {
        if (object instanceof Anchor2DEvent)
        {
            firePositionChangedEvent(((Anchor2DEvent) object).getSource());
            return;
        }

        // provide event backward compatibility
        if (object instanceof OverlayEvent)
        {
            final OverlayEvent event = (OverlayEvent) object;

            if (event.getType() == OverlayEventType.PAINTER_CHANGED)
            {
                final PainterEvent pe = new PainterEvent(this, PainterEventType.PAINTER_CHANGED);

                for (Anchor2DListener listener : new ArrayList<Anchor2DListener>(anchor2Dlisteners))
                    listener.painterChanged(pe);
            }
        }

        super.onChanged(object);
    }

    protected void firePositionChangedEvent(Anchor2D source)
    {
        for (Anchor2DPositionListener listener : new ArrayList<Anchor2DPositionListener>(anchor2DPositionlisteners))
            listener.positionChanged(source);

        // backward compatibility
        for (Anchor2DListener listener : new ArrayList<Anchor2DListener>(anchor2Dlisteners))
            listener.positionChanged(source);
    }

    public void addPositionListener(Anchor2DPositionListener listener)
    {
        anchor2DPositionlisteners.add(listener);
    }

    public void removePositionListener(Anchor2DPositionListener listener)
    {
        anchor2DPositionlisteners.remove(listener);
    }

    /**
     * @deprecated Use {@link #addPositionListener(Anchor2DPositionListener)} or
     *             {@link #addOverlayListener(OverlayListener)} instead.
     */
    @Deprecated
    public void addAnchorListener(Anchor2DListener listener)
    {
        anchor2Dlisteners.add(listener);
    }

    /**
     * @deprecated Use {@link #removePositionListener(Anchor2DPositionListener)} or
     *             {@link #removeOverlayListener(OverlayListener)} instead.
     */
    @Deprecated
    public void removeAnchorListener(Anchor2DListener listener)
    {
        anchor2Dlisteners.remove(listener);
    }

    /**
     * @deprecated Use {@link #addPositionListener(Anchor2DPositionListener)} or
     *             {@link #addOverlayListener(OverlayListener)} instead.
     */
    @Deprecated
    public void addListener(Anchor2DListener listener)
    {
        addAnchorListener(listener);
    }

    /**
     * @deprecated Use {@link #removePositionListener(Anchor2DPositionListener)} or
     *             {@link #removeOverlayListener(OverlayListener)} instead.
     */
    @Deprecated
    public void removeListener(Anchor2DListener listener)
    {
        removeAnchorListener(listener);
    }

    public void paint(Graphics2D g, Sequence sequence, IcyCanvas canvas, boolean simplified)
    {
        // this will update VTK objects if needed
        updateEllipseForCanvas(canvas);

        if (canvas instanceof IcyCanvas2D)
        {
            // nothing to do here when not visible
            if (!isVisible())
                return;

            // get canvas Z position
            final int cnvZ = canvas.getPositionZ();
            final int z = getZ();

            boolean sameZPos = ((z == -1) || (cnvZ == -1) || (z == cnvZ));

            if (sameZPos)
            {
                // trivial paint optimization
                if (ShapeUtil.isVisible(g, ellipse))
                {
                    final Graphics2D g2 = (Graphics2D) g.create();

                    // draw content
                    if (isSelected())
                        g2.setColor(getSelectedColor());
                    else
                        g2.setColor(getColor());

                    // simplified small drawing
                    if (simplified)
                    {
                        final int ray = (int) canvas.canvasToImageDeltaX(2);
                        g2.fillRect((int) getPositionX() - ray, (int) getPositionY() - ray, ray * 2, ray * 2);
                    }
                    // normal drawing
                    else
                    {
                        // draw ellipse content
                        g2.fill(ellipse);
                        // draw black border
                        g2.setStroke(new BasicStroke((float) (getAdjRay(canvas) / 8f)));
                        g2.setColor(Color.black);
                        g2.draw(ellipse);
                    }

                    g2.dispose();
                }
            }
        }
        else if (canvas instanceof VtkCanvas)
        {
            // nothing to do here
        }
    }

    @Override
    public void paint(Graphics2D g, Sequence sequence, IcyCanvas canvas)
    {
        paint(g, sequence, canvas, false);
    }

    /*
     * only for backward compatibility
     */
    @Deprecated
    @Override
    public void keyPressed(KeyEvent e, Point2D imagePoint, IcyCanvas canvas)
    {
        if (!isVisible())
            return;

        // VtkCanvas not handled here
        if (canvas instanceof VtkCanvas)
            return;
        // no image position --> exit
        if (imagePoint == null)
            return;

        // just for the shift key state change
        updateDrag(e, imagePoint);
    }

    /*
     * only for backward compatibility
     */
    @Deprecated
    @Override
    public void keyReleased(KeyEvent e, Point2D imagePoint, IcyCanvas canvas)
    {
        if (!isVisible())
            return;

        // VtkCanvas not handled here
        if (canvas instanceof VtkCanvas)
            return;
        // no image position --> exit
        if (imagePoint == null)
            return;

        // just for the shift key state change
        updateDrag(e, imagePoint);
    }

    /*
     * only for backward compatibility
     */
    @Deprecated
    @Override
    public void mousePressed(MouseEvent e, Point2D imagePoint, IcyCanvas canvas)
    {
        if (!isVisible())
            return;

        if (e.isConsumed())
            return;

        // VtkCanvas not handled here
        if (canvas instanceof VtkCanvas)
            return;
        // no image position --> exit
        if (imagePoint == null)
            return;

        if (EventUtil.isLeftMouseButton(e))
        {
            // consume event to activate drag
            if (isSelected())
            {
                startDragMousePosition = imagePoint;
                startDragPainterPosition = getPosition();
                e.consume();
            }
        }
    }

    /*
     * only for backward compatibility
     */
    @Deprecated
    @Override
    public void mouseReleased(MouseEvent e, Point2D imagePoint, IcyCanvas canvas)
    {
        startDragMousePosition = null;
    }

    /*
     * only for backward compatibility
     */
    @Deprecated
    @Override
    public void mouseDrag(MouseEvent e, Point2D imagePoint, IcyCanvas canvas)
    {
        if (!isVisible())
            return;

        if (e.isConsumed())
            return;

        // VtkCanvas not handled here
        if (canvas instanceof VtkCanvas)
            return;
        // no image position --> exit
        if (imagePoint == null)
            return;

        if (EventUtil.isLeftMouseButton(e))
        {
            // if selected then move according to mouse position
            if (isSelected())
            {
                // force start drag if not already the case
                if (startDragMousePosition == null)
                {
                    startDragMousePosition = getPosition();
                    startDragPainterPosition = getPosition();
                }

                updateDrag(e, imagePoint);

                e.consume();
            }
        }
    }

    /*
     * only for backward compatibility
     */
    @Deprecated
    @Override
    public void mouseMove(MouseEvent e, Point2D imagePoint, IcyCanvas canvas)
    {
        if (!isVisible())
            return;

        // VtkCanvas not handled here
        if (canvas instanceof VtkCanvas)
            return;
        // no image position --> exit
        if (imagePoint == null)
            return;

        // already consumed, no selection possible
        if (e.isConsumed())
            setSelected(false);
        else
        {
            final boolean overlapped = isOver(canvas, imagePoint.getX(), imagePoint.getY());

            setSelected(overlapped);

            // so we can only have one selected at once
            if (overlapped)
                e.consume();
        }
    }

    @Override
    public void keyPressed(KeyEvent e, Point5D.Double imagePoint, IcyCanvas canvas)
    {
        if (!isVisible())
            return;

        // no image position --> exit
        if (imagePoint == null)
            return;

        // just for the shift key state change
        updateDrag(e, imagePoint.x, imagePoint.y);
    }

    @Override
    public void keyReleased(KeyEvent e, Point5D.Double imagePoint, IcyCanvas canvas)
    {
        if (!isVisible())
            return;

        // no image position --> exit
        if (imagePoint == null)
            return;

        // just for the shift key state change
        updateDrag(e, imagePoint.x, imagePoint.y);
    }

    @Override
    public void mousePressed(MouseEvent e, Point5D.Double imagePoint, IcyCanvas canvas)
    {
        if (!isVisible())
            return;

        if (e.isConsumed())
            return;

        // no image position --> exit
        if (imagePoint == null)
            return;

        if (EventUtil.isLeftMouseButton(e))
        {
            // consume event to activate drag
            if (isSelected())
            {
                startDragMousePosition = imagePoint.toPoint2D();
                startDragPainterPosition = getPosition();
                e.consume();
            }
        }
    }

    @Override
    public void mouseReleased(MouseEvent e, Point5D.Double imagePoint, IcyCanvas canvas)
    {
        startDragMousePosition = null;
    }

    @Override
    public void mouseDrag(MouseEvent e, Point5D.Double imagePoint, IcyCanvas canvas)
    {
        if (!isVisible())
            return;

        if (e.isConsumed())
            return;

        // no image position --> exit
        if (imagePoint == null)
            return;

        if (EventUtil.isLeftMouseButton(e))
        {
            // if selected then move according to mouse position
            if (isSelected())
            {
                // force start drag if not already the case
                if (startDragMousePosition == null)
                {
                    startDragMousePosition = getPosition();
                    startDragPainterPosition = getPosition();
                }

                updateDrag(e, imagePoint.x, imagePoint.y);

                e.consume();
            }
        }
    }

    @Override
    public void mouseMove(MouseEvent e, Point5D.Double imagePoint, IcyCanvas canvas)
    {
        if (!isVisible())
            return;

        // already consumed, no selection possible
        if (e.isConsumed())
            setSelected(false);
        else
        {
            final boolean overlapped = isOver(canvas, (imagePoint != null) ? imagePoint.toPoint2D() : null);

            setSelected(overlapped);

            // so we can only have one selected at once
            if (overlapped)
                e.consume();
        }
    }

    @Override
    public void run()
    {
        rebuildVtkObjects();
    }

    public boolean loadPositionFromXML(Node node)
    {
        if (node == null)
            return false;

        beginUpdate();
        try
        {
            setX(XMLUtil.getElementDoubleValue(node, ID_POS_X, 0d));
            setY(XMLUtil.getElementDoubleValue(node, ID_POS_Y, 0d));
        }
        finally
        {
            endUpdate();
        }

        return true;
    }

    public boolean savePositionToXML(Node node)
    {
        if (node == null)
            return false;

        XMLUtil.setElementDoubleValue(node, ID_POS_X, getX());
        XMLUtil.setElementDoubleValue(node, ID_POS_Y, getY());

        return true;
    }

    @Override
    public boolean loadFromXML(Node node)
    {
        if (node == null)
            return false;

        beginUpdate();
        try
        {
            setColor(new Color(XMLUtil.getElementIntValue(node, ID_COLOR, DEFAULT_NORMAL_COLOR.getRGB())));
            setSelectedColor(
                    new Color(XMLUtil.getElementIntValue(node, ID_SELECTEDCOLOR, DEFAULT_SELECTED_COLOR.getRGB())));
            setX(XMLUtil.getElementDoubleValue(node, ID_POS_X, 0d));
            setY(XMLUtil.getElementDoubleValue(node, ID_POS_Y, 0d));
            setRay(XMLUtil.getElementIntValue(node, ID_RAY, DEFAULT_RAY));
            setVisible(XMLUtil.getElementBooleanValue(node, ID_VISIBLE, true));
        }
        finally
        {
            endUpdate();
        }

        return true;
    }

    @Override
    public boolean saveToXML(Node node)
    {
        if (node == null)
            return false;

        XMLUtil.setElementIntValue(node, ID_COLOR, getColor().getRGB());
        XMLUtil.setElementIntValue(node, ID_SELECTEDCOLOR, getSelectedColor().getRGB());
        XMLUtil.setElementDoubleValue(node, ID_POS_X, getX());
        XMLUtil.setElementDoubleValue(node, ID_POS_Y, getY());
        XMLUtil.setElementIntValue(node, ID_RAY, getRay());
        XMLUtil.setElementBooleanValue(node, ID_VISIBLE, isVisible());

        return true;
    }
}
