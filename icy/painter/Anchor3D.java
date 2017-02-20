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
import icy.sequence.Sequence;
import icy.system.thread.ThreadUtil;
import icy.type.point.Point3D;
import icy.type.point.Point5D;
import icy.util.EventUtil;
import icy.util.GraphicsUtil;
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
 * Anchor3D class, used for 3D point control.
 * 
 * @author Stephane
 */
public class Anchor3D extends Overlay implements VtkPainter, Runnable
{
    /**
     * Interface to listen Anchor3D position change
     */
    public static interface Anchor3DPositionListener extends EventListener
    {
        public void positionChanged(Anchor3D source);
    }

    public static class Anchor3DEvent implements CollapsibleEvent
    {
        private final Anchor3D source;

        public Anchor3DEvent(Anchor3D source)
        {
            super();

            this.source = source;
        }

        /**
         * @return the source
         */
        public Anchor3D getSource()
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
            if (obj == this)
                return true;

            if (obj instanceof Anchor3DEvent)
            {
                final Anchor3DEvent event = (Anchor3DEvent) obj;

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
    protected static final String ID_POS_Z = "pos_z";
    protected static final String ID_RAY = "ray";
    protected static final String ID_VISIBLE = "visible";

    public static final int DEFAULT_RAY = 6;
    public static final Color DEFAULT_NORMAL_COLOR = Color.GREEN;
    public static final Color DEFAULT_SELECTED_COLOR = Color.WHITE;

    /**
     * position (canvas)
     */
    protected final Point3D.Double position;
    /**
     * radius (integer as we express it in pixel)
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

    // drag internals
    protected Point3D startDragMousePosition;
    protected Point3D startDragPainterPosition;

    // 2D shape for X,Y contains test
    protected final Ellipse2D ellipse;

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

    // listeners
    protected final List<Anchor3DPositionListener> anchor3DPositionlisteners;

    public Anchor3D(double x, double y, double z, int ray, Color color, Color selectedColor)
    {
        super("Anchor", OverlayPriority.SHAPE_NORMAL);

        position = new Point3D.Double(x, y, z);
        this.ray = ray;
        this.color = color;
        this.selectedColor = selectedColor;
        selected = false;
        visible = true;

        startDragMousePosition = null;
        startDragPainterPosition = null;

        ellipse = new Ellipse2D.Double();

        vtkSource = null;
        polyMapper = null;
        actor = null;
        vtkInfo = null;

        scaling = new double[3];
        Arrays.fill(scaling, 1d);

        needRebuild = true;
        needPropertiesUpdate = false;

        canvas3d = new WeakReference<VtkCanvas>(null);

        anchor3DPositionlisteners = new ArrayList<Anchor3DPositionListener>();
    }

    public Anchor3D(double x, double y, double z, Color color, Color selectedColor)
    {
        this(x, y, z, DEFAULT_RAY, color, selectedColor);
    }

    public Anchor3D(double x, double y, double z, int ray)
    {
        this(x, y, z, ray, DEFAULT_NORMAL_COLOR, DEFAULT_SELECTED_COLOR);
    }

    public Anchor3D(double x, double y, double z)
    {
        this(x, y, z, DEFAULT_RAY, DEFAULT_NORMAL_COLOR, DEFAULT_SELECTED_COLOR);
    }

    public Anchor3D()
    {
        this(0d, 0d, 0d, DEFAULT_RAY, DEFAULT_NORMAL_COLOR, DEFAULT_SELECTED_COLOR);
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
     * @return X coordinate position
     */
    public double getX()
    {
        return position.x;
    }

    /**
     * Sets the X coordinate position
     */
    public void setX(double value)
    {
        setPosition(value, position.y, position.z);
    }

    /**
     * @return Y coordinate position
     */
    public double getY()
    {
        return position.y;
    }

    /**
     * Sets the Y coordinate position
     */
    public void setY(double value)
    {
        setPosition(position.x, value, position.z);
    }

    /**
     * @return Z coordinate position
     */
    public double getZ()
    {
        return position.z;
    }

    /**
     * Sets the Z coordinate position
     */
    public void setZ(double value)
    {
        setPosition(position.x, position.y, value);
    }

    /**
     * Get anchor position (return the internal reference)
     */
    public Point3D getPositionInternal()
    {
        return position;
    }

    /**
     * Get anchor position
     */
    public Point3D getPosition()
    {
        return new Point3D.Double(position.x, position.y, position.z);
    }

    /**
     * Sets anchor position
     */
    public void setPosition(Point3D p)
    {
        setPosition(p.getX(), p.getY(), p.getZ());
    }

    /**
     * Sets anchor position
     */
    public void setPosition(double x, double y, double z)
    {
        if ((position.x != x) || (position.y != y) || (position.z != z))
        {
            position.x = x;
            position.y = y;
            position.z = z;

            needRebuild = true;
            positionChanged();
            painterChanged();
        }
    }

    /**
     * Performs a translation on the anchor position
     */
    public void translate(double dx, double dy, double dz)
    {
        setPosition(position.x + dx, position.y + dy, position.z + dz);
    }

    /**
     * @return the ray
     */
    public int getRay()
    {
        return ray;
    }

    /**
     * Sets the ray
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
     * Sets the color
     */
    public void setColor(Color value)
    {
        if (color != value)
        {
            color = value;
            needPropertiesUpdate = true;
            painterChanged();
        }
    }

    /**
     * @return the <code>selected</code> state color
     */
    public Color getSelectedColor()
    {
        return selectedColor;
    }

    /**
     * Sets the <code>selected</code> state color
     */
    public void setSelectedColor(Color value)
    {
        if (selectedColor != value)
        {
            selectedColor = value;
            needPropertiesUpdate = true;
            painterChanged();
        }
    }

    /**
     * @return the <code>selected</code> state
     */
    public boolean isSelected()
    {
        return selected;
    }

    /**
     * Sets the <code>selected</code> state
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
            painterChanged();
        }
    }

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

    /**
     * Returns <code>true</code> if specified Point3D is over the anchor in the specified canvas
     */
    public boolean isOver(IcyCanvas canvas, Point3D imagePoint)
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

        final double x = imagePoint.getX();
        final double y = imagePoint.getY();
        final double z = imagePoint.getZ();

        // default processing for other canvas
        final int cnvZ = canvas.getPositionZ();

        // same Z position ?
        if ((cnvZ == -1) || (z == -1d) || (cnvZ == (int) z))
        {
            // fast contains test to start with
            if (ellipse.getBounds2D().contains(x, y))
                return ellipse.contains(x, y);
        }

        return false;
    }

    /**
     * Returns adjusted ray for specified Canvas
     */
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

    protected boolean updateDrag(InputEvent e, double x, double y, double z)
    {
        // not dragging --> exit
        if (startDragMousePosition == null)
            return false;

        double dx = x - startDragMousePosition.getX();
        double dy = y - startDragMousePosition.getY();
        double dz = z - startDragMousePosition.getZ();

        // shift action --> limit to one direction
        if (EventUtil.isShiftDown(e))
        {
            // X or Z drag
            if (Math.abs(dx) > Math.abs(dy))
            {
                dy = 0d;

                // Z drag
                if (Math.abs(dz) > Math.abs(dx))
                    dx = 0d;
                else
                    dz = 0d;
            }
            // Y or Z drag
            else
            {
                dx = 0d;

                // Z drag
                if (Math.abs(dz) > Math.abs(dy))
                    dy = 0d;
                else
                    dz = 0d;
            }
        }

        // set new position
        setPosition(new Point3D.Double(startDragPainterPosition.getX() + dx, startDragPainterPosition.getY() + dy,
                startDragPainterPosition.getZ() + dz));

        return true;
    }

    protected boolean updateDrag(InputEvent e, Point3D pt)
    {
        return updateDrag(e, pt.getX(), pt.getY(), pt.getZ());
    }

    /**
     * called when anchor position has changed
     */
    protected void positionChanged()
    {
        updater.changed(new Anchor3DEvent(this));
    }

    @Override
    public void onChanged(CollapsibleEvent object)
    {
        // we got a position change event
        if (object instanceof Anchor3DEvent)
        {
            firePositionChangedEvent(((Anchor3DEvent) object).getSource());
            return;
        }

        super.onChanged(object);
    }

    protected void firePositionChangedEvent(Anchor3D source)
    {
        for (Anchor3DPositionListener listener : new ArrayList<Anchor3DPositionListener>(anchor3DPositionlisteners))
            listener.positionChanged(source);
    }

    public void addPositionListener(Anchor3DPositionListener listener)
    {
        anchor3DPositionlisteners.add(listener);
    }

    public void removePositionListener(Anchor3DPositionListener listener)
    {
        anchor3DPositionlisteners.remove(listener);
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

        final Point3D pos = getPosition();

        // actor can be accessed in canvas3d for rendering so we need to synchronize access
        vtkPanel.lock();
        try
        {
            // need to handle scaling on radius and position to keep a "round" sphere (else we obtain ellipsoid)
            vtkSource.SetCenter(pos.getX() * scaling[0], pos.getY() * scaling[1], pos.getZ() * scaling[2]);
            vtkSource.Update();
            polyMapper.Update();

            // actor.SetScale(scaling[0], scaling[1], scaling[0]);
        }
        finally
        {
            vtkPanel.unlock();
        }

        // need to repaint
        painterChanged();

        return true;
    }

    protected boolean updateVtkDisplayProperties()
    {
        if (actor == null)
            return false;

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

        return true;
    }

    protected boolean updateVtkRadius()
    {
        final VtkCanvas canvas = canvas3d.get();
        // canvas was closed
        if (canvas == null)
            return false;

        final IcyVtkPanel vtkPanel = canvas.getVtkPanel();
        // canvas was closed
        if (vtkPanel == null)
            return false;

        if (vtkSource == null)
            return false;

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

        return true;
    }

    @Override
    public vtkProp[] getProps()
    {
        // initialize VTK objects if not yet done
        if (actor == null)
            initVtkObjects();

        return new vtkActor[] {actor};
    }

    @Override
    public void paint(Graphics2D g, Sequence sequence, IcyCanvas canvas)
    {
        paint(g, sequence, canvas, false);
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
            // get delta Z (difference between canvas Z position and anchor Z pos)
            final int dz = Math.abs(((int) getZ()) - cnvZ);
            // calculate z fade range
            final int zRange = Math.min(10, Math.max(3, sequence.getSizeZ() / 10));

            // not visible on this Z position
            if (dz > zRange)
                return;

            // trivial paint optimization
            final boolean shapeVisible = ShapeUtil.isVisible(g, ellipse);

            if (shapeVisible)
            {
                final Graphics2D g2 = (Graphics2D) g.create();
                // ration for size / opacity
                final float ratio = 1f - ((float) dz / (float) zRange);

                if (ratio != 1f)
                    GraphicsUtil.mixAlpha(g2, ratio);

                // draw content
                if (isSelected())
                    g2.setColor(getSelectedColor());
                else
                    g2.setColor(getColor());

                // simplified small drawing
                if (simplified)
                {
                    final int ray = (int) canvas.canvasToImageDeltaX(2);
                    g2.fillRect((int) getX() - ray, (int) getY() - ray, ray * 2, ray * 2);
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
        else if (canvas instanceof VtkCanvas)
        {
            // nothing to do here
        }
    }

    @Override
    public void keyPressed(KeyEvent e, Point5D.Double imagePoint, IcyCanvas canvas)
    {
        if (!isVisible() && !getReceiveKeyEventOnHidden())
            return;

        // no image position --> exit
        if (imagePoint == null)
            return;

        // just for the shift key state change
        updateDrag(e, imagePoint.x, imagePoint.y, imagePoint.z);
    }

    @Override
    public void keyReleased(KeyEvent e, Point5D.Double imagePoint, IcyCanvas canvas)
    {
        if (!isVisible() && !getReceiveKeyEventOnHidden())
            return;

        // no image position --> exit
        if (imagePoint == null)
            return;

        // just for the shift key state change
        updateDrag(e, imagePoint.x, imagePoint.y, imagePoint.z);
    }

    @Override
    public void mousePressed(MouseEvent e, Point5D.Double imagePoint, IcyCanvas canvas)
    {
        if (!isVisible() && !getReceiveMouseEventOnHidden())
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
                startDragMousePosition = imagePoint.toPoint3D();
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
        if (!isVisible() && !getReceiveMouseEventOnHidden())
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
                    startDragMousePosition = imagePoint.toPoint3D();
                    startDragPainterPosition = getPosition();
                }

                updateDrag(e, imagePoint.x, imagePoint.y, imagePoint.z);

                e.consume();
            }
        }
    }

    @Override
    public void mouseMove(MouseEvent e, Point5D.Double imagePoint, IcyCanvas canvas)
    {
        if (!isVisible() && !getReceiveMouseEventOnHidden())
            return;

        // already consumed, no selection possible
        if (e.isConsumed())
            setSelected(false);
        else
        {
            final boolean overlapped = isOver(canvas, (imagePoint != null) ? imagePoint.toPoint3D() : null);

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
            setZ(XMLUtil.getElementDoubleValue(node, ID_POS_Z, 0d));
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
        XMLUtil.setElementDoubleValue(node, ID_POS_Z, getZ());

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
            setSelectedColor(new Color(XMLUtil.getElementIntValue(node, ID_SELECTEDCOLOR,
                    DEFAULT_SELECTED_COLOR.getRGB())));
            setX(XMLUtil.getElementDoubleValue(node, ID_POS_X, 0d));
            setY(XMLUtil.getElementDoubleValue(node, ID_POS_Y, 0d));
            setZ(XMLUtil.getElementDoubleValue(node, ID_POS_Z, 0d));
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
        XMLUtil.setElementDoubleValue(node, ID_POS_Z, getY());
        XMLUtil.setElementIntValue(node, ID_RAY, getRay());
        XMLUtil.setElementBooleanValue(node, ID_VISIBLE, isVisible());

        return true;
    }
}
