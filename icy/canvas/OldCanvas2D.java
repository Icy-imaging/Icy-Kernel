/*
 * Copyright 2010, 2011 Institut Pasteur.
 * 
 * This file is part of ICY.
 * 
 * ICY is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * ICY is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with ICY. If not, see <http://www.gnu.org/licenses/>.
 */
package icy.canvas;

import icy.gui.component.ComponentUtil;
import icy.gui.main.MainFrame;
import icy.gui.menu.ToolRibbonTask;
import icy.gui.menu.ToolRibbonTask.ToolRibbonTaskListener;
import icy.gui.util.GuiUtil;
import icy.gui.viewer.TNavigationPanel;
import icy.gui.viewer.Viewer;
import icy.gui.viewer.ZNavigationPanel;
import icy.image.IcyBufferedImage;
import icy.main.Icy;
import icy.math.SmoothMover;
import icy.math.SmoothMover.SmoothMoveType;
import icy.math.SmoothMover.SmoothMoverListener;
import icy.preferences.ApplicationPreferences;
import icy.preferences.XMLPreferences;
import icy.resource.ResourceUtil;
import icy.roi.ROI;
import icy.roi.ROI2D;
import icy.sequence.DimensionId;
import icy.sequence.Sequence;
import icy.sequence.SequenceEvent.SequenceEventType;
import icy.system.thread.SingleProcessor;
import icy.system.thread.ThreadUtil;
import icy.util.EventUtil;

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Canvas 2D is the basic 2D viewer for ICY
 * 
 * @author Fabrice de Chaumont & Stephane
 * @deprecated
 */
@Deprecated
public class OldCanvas2D extends IcyCanvas2D implements SmoothMoverListener, ToolRibbonTaskListener
{
    static final Image ICON_TARGET = ResourceUtil.getAlphaIconAsImage("target.png", 20);

    class View2D extends JPanel implements MouseWheelListener, MouseMotionListener, MouseListener, ComponentListener
    {
        private static final long serialVersionUID = 1953205026927552167L;

        /** starting point of the mouse drag */
        private final Point2D.Double startPointDrag;
        private final Font graphicsFont;

        /**
         * mouse position data
         */
        // final Point mouseVisiblePos;
        final Point mouseCanvasPos;
        final Point2D.Double mouseImagePos;

        /** flag of the mouse drag */
        boolean isDragging;

        /**
         * cached
         */
        BufferedImage cachedImage;
        private final Point previousLocation;

        /**
         * constructor
         */
        public View2D()
        {
            addMouseWheelListener(this);
            addMouseMotionListener(this);
            addMouseListener(this);
            addComponentListener(this);

            setDoubleBuffered(true);

            graphicsFont = new Font("Arial", Font.BOLD, 20);
            startPointDrag = new Point2D.Double();

            // mouseVisiblePos = new Point();
            mouseCanvasPos = new Point();
            mouseImagePos = new Point2D.Double();

            isDragging = false;
            previousLocation = new Point();
            cachedImage = null;
        }

        /**
         * set location with limit verification
         */
        void checkLocationChanged()
        {
            final Point location = getLocation();

            // location changed externally ?
            if (!previousLocation.equals(location))
            {
                // get position delta in image pixel
                final Point2D delta = canvasToImageDelta(new Point(location.x - previousLocation.x, location.y
                        - previousLocation.y));
                // update mouse image position from this unexpected location change
                final Point2D pos = new Point2D.Double(mouseImagePos.getX() - delta.getX(), mouseImagePos.getY()
                        - delta.getY());
                setMouseImagePos(pos, null, false);
                // update others mouse positions
                updateMousePositionFromImagePos();
                // update internal copy
                previousLocation.setLocation(location);
            }
        }

        /**
         * set location with limit verification
         */
        public void setLocationSafe(int x, int y)
        {
            final Dimension viewSize = getCanvasSize();
            final Dimension totalSize = getPreferredSize();
            final int minXOffset = -(totalSize.width - viewSize.width);
            final int minYOffset = -(totalSize.height - viewSize.height);
            int adjustedX = x;
            int adjustedY = y;

            // positive value unauthorized
            if (adjustedX > 0)
                adjustedX = 0;
            if (adjustedY > 0)
                adjustedY = 0;
            // can't be < minOffset
            if (adjustedX < minXOffset)
                adjustedX = minXOffset;
            if (adjustedY < minYOffset)
                adjustedY = minYOffset;

            final Point newLocation = new Point(adjustedX, adjustedY);

            // location changed ?
            if (!newLocation.equals(getLocation()))
            {
                // set new location
                setLocation(newLocation);
                // keep an internal cached version
                previousLocation.setLocation(newLocation);
                // FIXME : why this is needed for scrollbar refresh ?
                revalidate();
            }
        }

        /**
         * set location with limit verification
         */
        public void setLocationSafe(Point newLocation)
        {
            setLocationSafe(newLocation.x, newLocation.y);
        }

        /**
         * refresh size (called on zoom factor change or resize event)
         */
        boolean updateSize()
        {
            final Dimension curSize = getPreferredSize();
            // get scene bounds size
            final Dimension sceneSize = getSceneBounds().getSize();

            // dimension changed ?
            if (!curSize.equals(sceneSize))
            {
                final Dimension viewSize = getCanvasSize();
                final Point location = getLocation();

                // scene size if larger than view size ?
                if (sceneSize.width > viewSize.width)
                {
                    // compute size delta
                    final int delta = curSize.width - sceneSize.width;
                    // adjust location
                    location.translate((delta + 1) / 2, 0);
                }
                // scene size if higher than view size ?
                if (sceneSize.height > viewSize.height)
                {
                    // compute size delta
                    final int delta = curSize.height - sceneSize.height;
                    // adjust location
                    location.translate(0, (delta + 1) / 2);
                }

                // force update size or it won't be done in time
                setPreferredSize(sceneSize);
                setSize(sceneSize);
                // modify location if needed
                setLocationSafe(location);
                // needed as we are in a scrollpane
                revalidate();
                // size changed
                return true;
            }

            // size unchanged
            return false;
        }

        /**
         * Rebuild cached image
         */
        void rebuildCachedImage(IcyBufferedImage image)
        {
            if (image != null)
                cachedImage = image.getARGBImage(getLut(), cachedImage);
        }

        public Point getViewCenter()
        {
            final Dimension viewSize = getCanvasSize();
            final Point location = getVisibleRect().getLocation();

            return new Point(location.x + (viewSize.width / 2), location.y + (viewSize.height / 2));
        }

        public void setViewCenter(Point pos)
        {
            final Point viewCenter = getViewCenter();
            setLocationSafe(pos.x - viewCenter.x, pos.y - viewCenter.y);
        }

        /**
         * @param pos
         *        the mousePosOnVisible to set
         */
        // public void setMousePosOnVisible(Point pos)
        // {
        // if (!mouseVisiblePos.equals(pos))
        // mouseVisiblePos.setLocation(pos);
        // }

        /**
         * @param pos
         *        the mousePosOnCanvas to set
         */
        public void setMouseCanvasPos(Point pos)
        {
            if (!mouseCanvasPos.equals(pos))
                mouseCanvasPos.setLocation(pos);
        }

        /**
         * @param pos
         *        the mouseImagePos to set
         */
        public void setMouseImagePos(Point2D pos, MouseEvent e, boolean drag)
        {
            if (!mouseImagePos.equals(pos))
            {
                setMouseImagePosInternal(pos);

                // common task on mouse image position change here

                // not in MOVE mode ?
                if (!currentTool.equals(ToolRibbonTask.MOVE))
                {
                    final MouseEvent mouseEvent;

                    if (e == null)
                    {
                        // manually build the mouseEvent object
                        final int id;

                        if (drag)
                            id = MouseEvent.MOUSE_DRAGGED;
                        else
                            id = MouseEvent.MOUSE_MOVED;

                        // mouse position in canvas
                        final Point mousePos = imageToCanvas(mouseImagePos);
                        final Point absMousePos = new Point(mousePos);
                        // absolute mouse position
                        SwingUtilities.convertPointToScreen(absMousePos, this);

                        mouseEvent = new MouseEvent(this, id, System.currentTimeMillis(), 0, mousePos.x, mousePos.y,
                                absMousePos.x, absMousePos.y, 0, false, 0);
                    }
                    else
                        mouseEvent = e;

                    // send mouse event to painters
                    for (Layer layer : getVisibleOrderedLayersForEvent())
                    {
                        if (layer.isVisible())
                        {
                            if (drag)
                                layer.getPainter().mouseDrag(mouseEvent, (Point2D) mouseImagePos.clone(),
                                        OldCanvas2D.this);
                            else
                                layer.getPainter().mouseMove(mouseEvent, (Point2D) mouseImagePos.clone(),
                                        OldCanvas2D.this);
                        }
                    }
                }

                updateCursor();
            }
        }

        /**
         * @param pos
         *        the mouseImagePos to set
         */
        protected void setMouseImagePosInternal(Point2D pos)
        {
            setMouseImagePosXInternal(pos.getX());
            setMouseImagePosYInternal(pos.getY());
        }

        public void updateMousePosition(Point pos, MouseEvent e, boolean drag)
        {
            // udpate mouse position infos
            setMouseCanvasPos(pos);
            // update mouse positions from new mouse canvas position
            updateMousePositionFromCanvasPos(e, drag);
        }

        public void updateMousePositionFromCanvasPos(MouseEvent e, boolean drag)
        {
            // udpate mouse position infos from mouse image position
            setMouseImagePos(canvasToImage(mouseCanvasPos), e, drag);
            // update mouse visible position
            // udpateMouseVisiblePosition();
        }

        public void updateMousePositionFromImagePos()
        {
            // udpate mouse position infos from mouse image position
            setMouseCanvasPos(imageToCanvas(mouseImagePos));
            // update mouse visible position
            // udpateMouseVisiblePosition();
        }

        // public void udpateMouseVisiblePosition()
        // {
        // final Point pos = new Point(mouseCanvasPos);
        // final Point location = getLocation();
        // pos.translate(location.x, location.y);
        // setMousePosOnVisible(pos);
        // }

        void updateCursor()
        {
            final Sequence seq = getSequence();

            if (seq != null)
            {
                if (currentTool.equals(ToolRibbonTask.MOVE))
                    setCursor(new Cursor(Cursor.MOVE_CURSOR));
                else
                {
                    // search if we are overriding control points of selected ROI
                    final ArrayList<ROI> selectedRois = seq.getSelectedROIs();

                    boolean selectedRoiOverCP = false;

                    for (ROI selectedRoi : selectedRois)
                    {
                        final Layer layer = getLayer(selectedRoi);

                        if (layer.isVisible())
                        {
                            if (selectedRoi instanceof ROI2D)
                            {
                                if (((ROI2D) selectedRoi).isOverPoint(OldCanvas2D.this, mouseImagePos))
                                {
                                    selectedRoiOverCP = true;
                                    break;
                                }
                            }
                        }
                    }

                    if (selectedRoiOverCP)
                        setCursor(new Cursor(Cursor.HAND_CURSOR));
                    else
                    {
                        final ROI overlappedRoi = seq.getFocusedROI();

                        if ((overlappedRoi != null) && getLayer(overlappedRoi).isVisible())

                            setCursor(new Cursor(Cursor.HAND_CURSOR));
                        else
                            setCursor(Cursor.getDefaultCursor());
                    }
                }
            }
            else
                setCursor(Cursor.getDefaultCursor());
        }

        @Override
        public void mouseMoved(MouseEvent e)
        {
            final Point pos = adjustMouseCanvasPosition(e.getPoint());

            // update mouse position info
            updateMousePosition(pos, e, false);
        }

        @Override
        public void mouseClicked(MouseEvent e)
        {
            // send to painters
            for (Layer layer : getVisibleOrderedLayersForEvent())
                if (layer.isVisible())
                    layer.getPainter().mouseClick(e, (Point2D) mouseImagePos.clone(), OldCanvas2D.this);
        }

        @Override
        public void mouseDragged(MouseEvent e)
        {
            final Point pos = adjustMouseCanvasPosition(e.getPoint());

            // update mouse position info
            updateMousePosition(pos, e, true);

            // MOVE mode ?
            if (currentTool.equals(ToolRibbonTask.MOVE))
            {
                // start drag operation
                if (!isDragging)
                {
                    startPointDrag.setLocation(mouseImagePos);
                    isDragging = true;
                }
                else
                {
                    // convert to canvas coordinate
                    final Point startDragCanvas = imageToCanvas(startPointDrag);

                    // move visible rect
                    final Point location = getLocation();
                    location.translate(pos.x - startDragCanvas.x, pos.y - startDragCanvas.y);
                    // new location
                    setLocationSafe(location);
                    // update mouse positions from the mouse image position
                    // as it shouldn't change during drag/move operation
                    updateMousePositionFromImagePos();
                }
            }
        }

        @Override
        public void mousePressed(MouseEvent e)
        {
            // not in MOVE mode ?
            if (!currentTool.equals(ToolRibbonTask.MOVE))
            {
                if (EventUtil.isLeftMouseButton(e))
                {
                    // roi tool ?
                    if (ToolRibbonTask.isROITool(currentTool))
                    {
                        // control action (force ROI creation)
                        if (EventUtil.isControlDown(e))
                        {
                            // try to create ROI from current selected tool
                            final ROI roi = ROI.create(currentTool, getSequence(), mouseImagePos, true);
                            // roi created ? --> it becomes the selected ROI
                            if (roi != null)
                                roi.setSelected(true, true);
                            // consume event
                            e.consume();
                        }
                    }
                }

                // send to painters
                for (Layer layer : getVisibleOrderedLayersForEvent())
                    if (layer.isVisible())
                        layer.getPainter().mousePressed(e, (Point2D) mouseImagePos.clone(), OldCanvas2D.this);

                // event not yet consumed ? --> can do special action
                if (!e.isConsumed())
                {
                    if (EventUtil.isLeftMouseButton(e))
                    {
                        // shift action
                        if (EventUtil.isShiftDown(e))
                        {
                            // do nothing, we use "shift" to remove/delete
                        }
                        else
                        {
                            // roi tool ?
                            if (ToolRibbonTask.isROITool(currentTool))
                            {
                                // return to default selection tool before ROI creation
                                Icy.getMainInterface().setSelectedTool(ToolRibbonTask.SELECT);

                                // try to create ROI from current selected tool
                                final ROI roi = ROI.create(prevTool, getSequence(), mouseImagePos, true);
                                // roi created ?
                                if (roi != null)
                                {
                                    // it becomes the selected ROI
                                    roi.setSelected(true, true);
                                    // consume event
                                    e.consume();
                                }
                            }
                        }
                    }
                }
            }

        }

        @Override
        public void mouseReleased(MouseEvent e)
        {
            // not in MOVE mode ?
            if (!currentTool.equals(ToolRibbonTask.MOVE))
            {
                // send to painters
                for (Layer layer : getVisibleOrderedLayersForEvent())
                    if (layer.isVisible())
                        layer.getPainter().mouseReleased(e, (Point2D) mouseImagePos.clone(), OldCanvas2D.this);
            }

            // no more dragging
            isDragging = false;
        }

        @Override
        public void mouseEntered(MouseEvent e)
        {

        }

        @Override
        public void mouseExited(MouseEvent e)
        {

        }

        @Override
        public void mouseWheelMoved(MouseWheelEvent e)
        {
            if (smoothZoomCheckBox.isSelected())
                zoomFactor.setType(SmoothMoveType.LOG);
            else
                zoomFactor.setType(SmoothMoveType.NONE);

            // calculate new zoom factor
            double newFactor = zoomFactor.getDestValue()
                    + (e.getWheelRotation() * 0.05d * Math.pow(zoomFactor.getDestValue(), 1.1));
            newFactor = Math.max(0.01d, newFactor);
            newFactor = Math.min(100d, newFactor);
            // want to move zoom to new factor
            zoomFactor.moveTo(newFactor);
        }

        @Override
        protected void paintComponent(Graphics g)
        {
            // update mouse position on unexpected location change
            checkLocationChanged();
            // paintComponent can be called before resize event
            // so we have to take care about image position here
            updateSize();

            super.paintComponent(g);

            // get cached image
            final IcyBufferedImage image = getCurrentImage();

            if (image != null)
            {
                final Graphics2D g2 = (Graphics2D) g.create();

                // for debug only
                // g2.setColor(Color.black);
                // g2.drawString(fpsMeter.update() + " fps", 20, 20);

                // if (interpolate)
                // g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                // RenderingHints.VALUE_INTERPOLATION_BILINEAR);

                final double zf = zoomFactor.getValue();
                final Point imagePos = getImagePosition();

                // apply transformation (translation + scale)
                g2.translate(imagePos.x, imagePos.y);
                g2.scale(zf, zf);

                // we use a cache for ARGB image (so we don't need to reconstruct it each time)
                if (cachedImage != null)
                    g2.drawImage(cachedImage, 0, 0, null);
                // g2.drawImage(image, 0, 0, null);

                if (drawLayers)
                {
                    final Sequence seq = getSequence();

                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    final ArrayList<Layer> layers = getVisibleOrderedLayersForEvent();

                    // draw them in inverse order to have first painter event at top
                    for (int i = layers.size() - 1; i >= 0; i--)
                    {
                        final Layer layer = layers.get(i);

                        final float alpha = layer.getAlpha();

                        if (alpha != 1f)
                            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
                        else
                            g2.setComposite(AlphaComposite.SrcOver);

                        layer.getPainter().paint(g2, seq, OldCanvas2D.this);
                    }
                }

                // synchronized and slave ?
                // if (isSynchronized() && !synchHeader)
                // g2.drawImage(ICON_TARGET, (int) getMouseImagePosX(), (int) getMouseImagePosY(),
                // null);

                g2.dispose();
            }

            // prepare for text display
            {
                final Graphics2D g2 = (Graphics2D) g.create();
                final Point viewCenter = getViewCenter();

                g2.setFont(graphicsFont);
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (image == null)
                {
                    // no image at this position
                    drawText(g2, "No image", viewCenter.x, viewCenter.y, 0.8f);
                }
                else
                {
                    // calculating image cache ?
                    if (cachedImage == null)
                        drawText(g2, "loading...", viewCenter.x, viewCenter.y, 0.8f);
                    else
                    {
                        // display zoom info ?
                        final float alpha = (float) zoomAlpha.getValue();

                        if (alpha > 0.3)
                            drawText(g2, "" + (int) (zoomFactor.getValue() * 100) + "%", viewCenter.x, viewCenter.y,
                                    alpha);
                    }
                }

                g2.dispose();
            }
        }

        private void drawText(Graphics2D g, String text, int x, int y, float alpha)
        {
            final Rectangle2D rect = GuiUtil.getStringBounds(g, text);
            final int w = (int) rect.getWidth();
            final int h = (int) rect.getHeight();

            g.setColor(Color.gray);
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            g.fillRoundRect(x - ((w / 2) + 16), y - ((h / 2) + 10), w + 32, h + 20, 20, 10);

            g.setColor(Color.white);
            // FIXME : why Y centering doesn't work here ??
            // g.drawString(text, x - (w / 2), y - (h / 2));
            g.drawString(text, x - (w / 2), y + 6);
        }

        @Override
        public void componentHidden(ComponentEvent e)
        {

        }

        @Override
        public void componentMoved(ComponentEvent e)
        {
            checkLocationChanged();
            offsetChanged(DimensionId.X);
            offsetChanged(DimensionId.Y);
        }

        @Override
        public void componentResized(ComponentEvent e)
        {
            offsetChanged(DimensionId.X);
            offsetChanged(DimensionId.Y);
        }

        @Override
        public void componentShown(ComponentEvent e)
        {

        }
    }

    private static final long serialVersionUID = 5224857946876470174L;

    private static final float viewBorderRatio = 0.02f;

    /**
     * pref ID
     */
    private static final String PREF_CANVAS2D_ID = "canvas2D";

    private static final String ID_SMOOTH_ZOOM = "zoomSmooth";
    private static final String ID_FIT_CANVAS = "fitCanvas";

    /**
     * zoom datas
     */
    final SmoothMover zoomAlpha;
    final SmoothMover zoomFactor;

    boolean interpolate;

    /**
     * preferences
     */
    final XMLPreferences preferences;

    /**
     * gui
     */
    private final JScrollPane scrollPane;
    final ZNavigationPanel zNav;
    final TNavigationPanel tNav;
    final View2D view2D;

    private final JLabel zoomLabel;

    final JButton zoom0_1x;
    final JButton zoom0_5x;
    final JButton zoom1x;
    final JButton zoom2x;
    final JButton zoom10x;
    final JButton zoomFitCanvas;
    final JButton zoomFitImage;
    final JCheckBox smoothZoomCheckBox;
    final JCheckBox fitToCanvasCheckBox;

    /**
     * internals
     */
    String prevTool;
    String currentTool;

    final SingleProcessor refreshProcessor;
    final Runnable refresher;

    /**
     * constructor
     */
    public OldCanvas2D(Viewer viewer)
    {
        super(viewer);

        // arrange to our dimension format
        if (posZ == -1)
            posZ = 0;
        if (posT == -1)
            posT = 0;
        posC = -1;

        setDoubleBuffered(true);
        setLayout(new BorderLayout());

        // variables initialisation
        preferences = ApplicationPreferences.getPreferences().node(PREF_CANVAS2D_ID);

        view2D = new View2D();

        zoomAlpha = new SmoothMover(0, SmoothMoveType.LINEAR);
        zoomFactor = new SmoothMover(1);
        // zoomFactor.setUpdateDelay(1);
        zoomAlpha.addListener(this);
        zoomFactor.addListener(this);

        // default mode : NORMAL
        // mode = modeType.NORMAL;

        // interpolation
        interpolate = false;

        // tool initialization
        currentTool = Icy.getMainInterface().getSelectedTool();
        prevTool = currentTool;
        // listen tool change
        Icy.getMainInterface().getToolRibbon().addListener(this);

        // refresh processor
        refreshProcessor = new SingleProcessor(true);

        refresher = new Runnable()
        {
            @Override
            public void run()
            {
                try
                { // refresh image
                    view2D.rebuildCachedImage(getCurrentImage());
                    // request repaint
                    repaint();
                }
                catch (Throwable t)
                {
                    // ignore
                }
            }
        };

        // GUI
        scrollPane = new JScrollPane(view2D);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        scrollPane.setFocusable(false);
        scrollPane.setWheelScrollingEnabled(false);
        scrollPane.setDoubleBuffered(true);
        scrollPane.getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE);
        // scrollPane.getViewport().setScrollMode(JViewport.BLIT_SCROLL_MODE);
        scrollPane.addComponentListener(new ComponentAdapter()
        {
            @Override
            public void componentResized(ComponentEvent e)
            {
                // auto FIT
                if (fitToCanvasCheckBox.isSelected())
                    fitImageToCanvas();
            }
        });

        // Z navigation
        zNav = new ZNavigationPanel();
        zNav.addChangeListener(new ChangeListener()
        {
            @Override
            public void stateChanged(ChangeEvent e)
            {
                // set the new Z position
                setZ(zNav.getValue());
            }
        });

        // T navigation
        tNav = new TNavigationPanel();
        tNav.addChangeListener(new ChangeListener()
        {
            @Override
            public void stateChanged(ChangeEvent e)
            {
                // set the new T position
                setT(tNav.getValue());
            }
        });

        add(zNav, BorderLayout.WEST);
        add(scrollPane, BorderLayout.CENTER);
        add(GuiUtil.createPageBoxPanel(tNav, mouseInfPanel), BorderLayout.SOUTH);

        // canvas panel (for inspector)
        panel = new JPanel();

        zoomLabel = new JLabel("100%", SwingConstants.LEFT);

        zoom0_1x = new JButton("0.1x");
        zoom0_1x.setToolTipText("Set zoom ration to 10% (10 image pixels for 1 canvas pixel)");
        zoom0_1x.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                setZoom(0.1f);
            }
        });
        ComponentUtil.setFixedWidth(zoom0_1x, 48);

        zoom0_5x = new JButton("0.5x");
        zoom0_5x.setToolTipText("Set zoom ration to 50% (2 image pixels for 1 canvas pixel)");
        zoom0_5x.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                setZoom(0.5f);
            }
        });
        ComponentUtil.setFixedWidth(zoom0_5x, 48);

        zoom1x = new JButton("1:1");
        zoom1x.setToolTipText("Set zoom ration to 100% (1 image pixel for 1 canvas pixel)");
        zoom1x.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                setZoom(1f);
            }
        });
        ComponentUtil.setFixedWidth(zoom1x, 40);

        zoom2x = new JButton("2x");
        zoom2x.setToolTipText("Set zoom ration to 200% (1 image pixel for 2 canvas pixels)");
        zoom2x.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                setZoom(2f);
            }
        });
        ComponentUtil.setFixedWidth(zoom2x, 36);

        zoom10x = new JButton("10x");
        zoom10x.setToolTipText("Set zoom ration to 1000% (1 image pixel for 10 canvas pixels)");
        zoom10x.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                setZoom(10f);
            }
        });
        ComponentUtil.setFixedWidth(zoom10x, 42);

        zoomFitCanvas = new JButton("Fit to canvas");
        zoomFitCanvas.setToolTipText("Set zoom ratio so image fit canvas size");
        zoomFitCanvas.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                fitImageToCanvas();
            }
        });

        zoomFitImage = new JButton("Fit to image");
        zoomFitImage.setToolTipText("Set canvas size to fit current image size (if possible)");
        zoomFitImage.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                fitCanvasToImage();
            }
        });

        smoothZoomCheckBox = new JCheckBox("Enabled smooth zoom", preferences.getBoolean(ID_SMOOTH_ZOOM, true));
        smoothZoomCheckBox.setToolTipText("Enabled smooth zoom animation");
        smoothZoomCheckBox.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                preferences.putBoolean(ID_SMOOTH_ZOOM, smoothZoomCheckBox.isSelected());
            }
        });

        fitToCanvasCheckBox = new JCheckBox("Fit image to canvas size", preferences.getBoolean(ID_FIT_CANVAS, false));
        fitToCanvasCheckBox.setToolTipText("Modify zoom ratio to keep image fitting to canvas size");
        fitToCanvasCheckBox.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                fitToCanvasCheckboxAction();
            }
        });

        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));

        panel.add(GuiUtil.createLineBoxPanel(smoothZoomCheckBox, Box.createHorizontalGlue()));
        panel.add(GuiUtil.createLineBoxPanel(fitToCanvasCheckBox, Box.createHorizontalGlue()));
        panel.add(Box.createVerticalStrut(8));
        panel.add(GuiUtil.createLineBoxPanel(Box.createHorizontalGlue(), new JLabel("Zoom : "), zoomLabel,
                Box.createHorizontalGlue()));
        panel.add(Box.createVerticalStrut(8));
        panel.add(GuiUtil.createLineBoxPanel(Box.createHorizontalGlue(), zoom0_1x, zoom0_5x, zoom1x, zoom2x, zoom10x,
                Box.createHorizontalGlue()));
        panel.add(Box.createVerticalStrut(4));
        panel.add(GuiUtil.createLineBoxPanel(Box.createHorizontalGlue(), zoomFitCanvas, Box.createHorizontalGlue(),
                zoomFitImage, Box.createHorizontalGlue()));
        panel.add(Box.createVerticalStrut(4));
        panel.add(Box.createVerticalGlue());

        mouseInfPanel.setInfoXVisible(true);
        mouseInfPanel.setInfoYVisible(true);
        // already visible in Z/T navigator
        mouseInfPanel.setInfoZVisible(false);
        mouseInfPanel.setInfoTVisible(false);
        // no C navigation with this canvas
        mouseInfPanel.setInfoCVisible(false);
        mouseInfPanel.setInfoDataVisible(true);
        mouseInfPanel.setInfoColorVisible(true);

        updateZNav();
        updateTNav();
        fitToCanvasCheckboxAction();

        // refresh canvas
        refresh();
    }

    @Override
    public void shutDown()
    {
        // preferences.putBoolean(key, value)
        // force zoom movement to stop
        zoomFactor.stop();
        // so we won't have any call to toolChanged with sequence set to null
        Icy.getMainInterface().getToolRibbon().removeListener(this);
    }

    /**
     * internal
     */
    void fitToCanvasCheckboxAction()
    {
        final boolean selected = fitToCanvasCheckBox.isSelected();

        preferences.putBoolean(ID_FIT_CANVAS, selected);

        zoom0_1x.setEnabled(!selected);
        zoom0_5x.setEnabled(!selected);
        zoom1x.setEnabled(!selected);
        zoom2x.setEnabled(!selected);
        zoom10x.setEnabled(!selected);
        zoomFitCanvas.setEnabled(!selected);
        zoomFitImage.setEnabled(!selected);

        // fit if enabled
        if (selected)
            fitImageToCanvas();
    }

    @Override
    public void fitImageToCanvas()
    {
        final int ix = getImageSizeX();
        final int iy = getImageSizeY();

        if ((ix > 0) && (iy > 0))
        {
            double sx = getCanvasSizeX();
            double sy = getCanvasSizeY();

            sx /= Math.ceil(ix * (1.0d + (viewBorderRatio * 2d)));
            sy /= Math.ceil(iy * (1.0d + (viewBorderRatio * 2d)));

            setZoomFactor(Math.min(sx, sy));
        }
    }

    @Override
    public void fitCanvasToImage()
    {
        final MainFrame mainFrame = Icy.getMainInterface().getFrame();
        final Dimension imageCanvasSize = getImageCanvasSize();

        if ((imageCanvasSize.width > 0) && (imageCanvasSize.height > 0) && (mainFrame != null))
        {
            final Dimension maxDim = mainFrame.getContentSize();

            // adjust to take border in account
            imageCanvasSize.width = (int) Math.ceil(imageCanvasSize.width * (1.0d + (viewBorderRatio * 2d)));
            imageCanvasSize.height = (int) Math.ceil(imageCanvasSize.height * (1.0d + (viewBorderRatio * 2d)));

            final Dimension adjImgCnvSize = canvasToViewer(imageCanvasSize);

            // fit in available space --> resize viewer
            viewer.setSize(Math.min(adjImgCnvSize.width, maxDim.width), Math.min(adjImgCnvSize.height, maxDim.height));
        }
    }

    // /**
    // * Get image size (in canvas pixel coordinate)
    // */
    // public Dimension getImageSize()
    // {
    // final Sequence seq = getSequence();
    //
    // if (seq != null)
    // {
    // final Dimension result = seq.getDimension();
    // final double zf = getZoomFactor();
    //
    // result.setSize(result.getWidth() * zf, result.getHeight() * zf);
    //
    // return result;
    // }
    //
    // return new Dimension();
    // }

    @Override
    public int getCanvasSizeX()
    {
        return scrollPane.getViewport().getWidth();
    }

    @Override
    public int getCanvasSizeY()
    {
        return scrollPane.getViewport().getHeight();
    }

    /**
     * Get visible area (in canvas pixel coordinate)
     * 
     * @return a Rectangle which represents the canvas visible area.
     */
    @Override
    public Rectangle getCanvasVisibleRect()
    {
        return view2D.getVisibleRect();
    }

    /**
     * Return the current Zoom Factor.<br>
     * So 1 pixel in the canvas represented is (1 / zoom factor) size in the original image.
     * 
     * @return the current Zoom Factor (1.00 = 100%)
     */
    public double getZoomFactor()
    {
        return zoomFactor.getValue();
    }

    /**
     * Return required zoom factor so the image fit in specified dimension.
     */
    public double getFitZoomFactor(Dimension dim)
    {
        final Sequence seq = getSequence();

        if (seq != null)
        {
            // get sequence 2D dimension
            final Dimension seqDim = seq.getDimension();

            if ((seqDim.width > 0) && (seqDim.height > 0))
                return Math.min(dim.getWidth() / seqDim.getWidth(), dim.getHeight() / seqDim.getHeight());
        }

        return 1.0d;
    }

    /**
     * get ROI bounds
     */
    private Rectangle getROIBounds()
    {
        final Rectangle result = new Rectangle();
        final Sequence seq = getSequence();

        if (seq != null)
        {
            final double zf = zoomFactor.getValue();

            // add ROI bounds
            for (ROI2D roi : seq.getROI2Ds())
            {
                final Rectangle2D r2d = roi.getBounds2D();
                final Rectangle r = new Rectangle();

                // convert image coordinate in pixel coordinate
                r.x = (int) Math.floor(r2d.getX() * zf);
                r.y = (int) Math.floor(r2d.getY() * zf);
                r.width = (int) Math.ceil(r2d.getWidth() * zf);
                r.height = (int) Math.ceil(r2d.getHeight() * zf);

                result.add(r);
            }
        }

        return result;
    }

    /**
     * Return scene bounds (image and ROI)<br>
     * top left corner of image is considered as (0,0) coordinate
     */
    Rectangle getSceneBounds()
    {
        final Rectangle result = new Rectangle();
        final IcyBufferedImage img = getCurrentImage();

        if (img != null)
        {
            final double zf = zoomFactor.getValue();
            result.width = (int) Math.ceil(img.getWidth() * zf);
            result.height = (int) Math.ceil(img.getHeight() * zf);

            // overlay activated ? add ROI bounds
            if (drawLayers)
                result.add(getROIBounds());
        }

        final Dimension viewSize = getCanvasSize();

        // adjust at least to have 1/10 of viewport size on border
        result.grow(Math.round(viewSize.width * viewBorderRatio), Math.round(viewSize.height * viewBorderRatio));

        // adjust at least to viewport size if needed
        final int adjustW = Math.max(viewSize.width - result.width, 0);
        final int adjustH = Math.max(viewSize.height - result.height, 0);

        result.grow(adjustW / 2, adjustH / 2);
        // take care of carry ;)
        if ((adjustW & 1) != 0)
            result.width++;
        if ((adjustH & 1) != 0)
            result.height++;

        return result;
    }

    public Point adjustMouseCanvasPosition(Point pos)
    {
        return new Point(pos.x + getCanvasOffsetX(), pos.y + getCanvasOffsetY());
    }

    @Override
    public double getMouseImagePosX()
    {
        return view2D.mouseImagePos.getX();
    }

    @Override
    public double getMouseImagePosY()
    {
        return view2D.mouseImagePos.getY();
    }

    @Override
    public int getOffsetX()
    {
        return getCanvasOffsetX() + getImageOffsetX();
    }

    @Override
    public int getOffsetY()
    {
        return getCanvasOffsetY() + getImageOffsetY();
    }

    @Override
    @Deprecated
    public int getCanvasOffsetX()
    {
        return view2D.getX();
    }

    @Override
    @Deprecated
    public int getCanvasOffsetY()
    {
        return view2D.getY();
    }

    @Override
    @Deprecated
    public int getImageOffsetX()
    {
        return getImagePosition().x;
    }

    @Override
    @Deprecated
    public int getImageOffsetY()
    {
        return getImagePosition().y;
    }

    @Override
    public double getScaleX()
    {
        return zoomFactor.getValue();
    }

    @Override
    public double getScaleY()
    {
        return zoomFactor.getValue();
    }

    Point getImagePosition()
    {
        // image is located to (0,0) in scene bounds
        final Rectangle r = getSceneBounds();
        // so image bounds relative position is (-x,-y)
        return new Point(-r.x, -r.y);
    }

    @Override
    protected void setPositionZInternal(int z)
    {
        super.setPositionZInternal(z);

        // this also modify mouse image position
        mouseImagePositionChanged(DimensionId.Z);
    }

    @Override
    protected void setPositionTInternal(int t)
    {
        super.setPositionTInternal(t);

        // this also modify mouse image position
        mouseImagePositionChanged(DimensionId.T);
    }

    @Override
    protected void setMouseImagePosXInternal(double value)
    {
        view2D.mouseImagePos.x = value;

        super.setMouseImagePosXInternal(value);
    }

    @Override
    protected void setMouseImagePosYInternal(double value)
    {
        view2D.mouseImagePos.y = value;

        super.setMouseImagePosYInternal(value);
    }

    @Override
    protected void setOffsetXInternal(int value)
    {
        // not able to implement it here
        // view2D.setLocationSafe(value - getImageOffsetX(), getOffsetY());
    }

    @Override
    protected void setOffsetYInternal(int value)
    {
        // not able to implement it here
        // view2D.setLocationSafe(getOffsetX(), value - getImageOffsetY());
    }

    @Override
    protected void setScaleXInternal(double value)
    {
        // setValue method launch 'valueChanged' event
        zoomFactor.setValue(value);
    }

    @Override
    protected void setScaleYInternal(double value)
    {
        // setValue method launch 'valueChanged' event
        zoomFactor.setValue(value);
    }

    public BufferedImage getRenderedImage(int t, int z, boolean canvasView)
    {
        final IcyBufferedImage srcImg = getImage(t, z);

        if (srcImg == null)
            return null;

        // save position
        final int prevT = getT();
        final int prevZ = getZ();

        // set wanted position (needed for correct overlay drawing)
        // we have to fire events else some stuff can miss the change
        setT(t);
        setZ(z);
        try
        {
            // FIXME : not really optimal in memory and processing
            final BufferedImage img = srcImg.getARGBImage(getLut());
            final BufferedImage result;
            final Graphics2D g;

            if (canvasView)
            {
                final Dimension size = getCanvasSize();

                // get result image and graphics object
                result = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_ARGB);
                g = result.createGraphics();

                // apply transformation (translation + scale)
                g.translate(getOffsetX(), getOffsetY());
                g.scale(getScaleX(), getScaleY());

                // draw transformed image
                g.drawImage(img, 0, 0, null);
            }
            else
            {
                // use the image Graphics object directly
                result = img;
                g = result.createGraphics();
            }

            if (drawLayers)
            {
                final Sequence seq = getSequence();

                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                for (Layer layer : getLayers())
                    if (layer.isVisible())
                        layer.getPainter().paint(g, seq, this);
            }

            g.dispose();

            return result;
        }
        finally
        {
            // restore position
            setT(prevT);
            setZ(prevZ);
        }
    }

    @Override
    public BufferedImage getRenderedImage(int t, int z, int c, boolean canvasView)
    {
        if (c != -1)
            throw new UnsupportedOperationException("getRenderedImage(..) with c != -1 not supported.");

        return getRenderedImage(t, z, canvasView);
    }

    @Override
    public void keyPressed(KeyEvent e)
    {
        switch (e.getKeyCode())
        {
            case KeyEvent.VK_LEFT:
                if (EventUtil.isControlDown(e))
                    setT(Math.max(getT() - 5, 0));
                else
                    setT(Math.max(getT() - 1, 0));
                e.consume();
                break;

            case KeyEvent.VK_RIGHT:
                if (EventUtil.isControlDown(e))
                    setT(getT() + 5);
                else
                    setT(getT() + 1);
                e.consume();
                break;

            case KeyEvent.VK_UP:
                if (EventUtil.isControlDown(e))
                    setZ(getZ() + 5);
                else
                    setZ(getZ() + 1);
                e.consume();
                break;

            case KeyEvent.VK_DOWN:
                if (EventUtil.isControlDown(e))
                    setZ(Math.max(getZ() - 5, 0));
                else
                    setZ(Math.max(getZ() - 1, 0));
                e.consume();
                break;

            case KeyEvent.VK_SPACE:
                // not in MOVE ?
                if (!currentTool.equals(ToolRibbonTask.MOVE))
                {
                    // set to MOVE tool
                    Icy.getMainInterface().setSelectedTool(ToolRibbonTask.MOVE);
                    e.consume();
                }
                break;
        }

        super.keyPressed(e);
    }

    @Override
    public void keyReleased(KeyEvent e)
    {
        switch (e.getKeyCode())
        {
            case KeyEvent.VK_SPACE:
                // restore previous tool if we still are in MOVE tool
                if (currentTool.equals(ToolRibbonTask.MOVE))
                {
                    Icy.getMainInterface().setSelectedTool(prevTool);
                    e.consume();
                }
                break;
        }

        super.keyReleased(e);
    }

    @Override
    public void refresh()
    {
        // background refresh
        refreshProcessor.requestProcess(refresher, false);
    }

    /**
     * update Z slider state
     */
    void updateZNav()
    {
        final int maxZ = getMaxZ();
        final int z = getZ();

        zNav.setMaximum(maxZ);
        if (z != -1)
            zNav.setValue(z);
        zNav.setVisible(maxZ > 0);
    }

    /**
     * update T slider state
     */
    void updateTNav()
    {
        final int maxT = getMaxT();
        final int t = getT();

        tNav.setMaximum(maxT);
        if (t != -1)
            tNav.setValue(t);
        tNav.setVisible(maxT > 0);
    }

    /**
     * @deprecated Use #setZoomFactor instead
     */
    @Deprecated
    public void setZoom(double zoom)
    {
        setZoomFactor(zoom);
    }

    /**
     * @param zoom
     *        1 for 100% .. 0.5 for 50%
     */
    public void setZoomFactor(double zoom)
    {
        // center mouse image position
        view2D.updateMousePosition(adjustMouseCanvasPosition(view2D.getViewCenter()), null, false);
        // setValue method launch 'valueChanged' event
        zoomFactor.setValue(zoom);
    }

    @Override
    public void changed(IcyCanvasEvent event)
    {
        super.changed(event);

        switch (event.getType())
        {
            case POSITION_CHANGED:
                switch (event.getDim())
                {
                    case C:
                        // should not happen here
                        break;

                    case Z:
                        final int curZ = getZ();

                        // ensure Z slider position
                        if (curZ != -1)
                            zNav.setValue(curZ);
                        break;

                    case T:
                        final int curT = getT();

                        // ensure T slider position
                        if (curT != -1)
                            tNav.setValue(curT);
                        break;
                }
                // refresh image
                refresh();
                break;

            case MOUSE_IMAGE_POSITION_CHANGED:
                // slave synchronized view --> repaint to display custom cursor
                // if (isSynchronized() && !isSynchHeader())
                // repaint();
                break;
        }
    }

    @Override
    protected void lutChanged(int component)
    {
        super.lutChanged(component);

        // refresh image
        refresh();
    }

    @Override
    public void layersChanged(LayersEvent event)
    {
        super.layersChanged(event);

        // repaint (image & painter)
        repaint();
    }

    @Override
    protected void sequenceDataChanged(IcyBufferedImage image, SequenceEventType type)
    {
        super.sequenceDataChanged(image, type);

        ThreadUtil.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                // update sliders bounds if needed
                updateZNav();
                updateTNav();
                // refresh image
                refresh();
            }
        });
    }

    @Override
    protected void sequenceROIChanged(ROI roi, SequenceEventType type)
    {
        super.sequenceROIChanged(roi, type);

        ThreadUtil.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                view2D.updateSize();
            }
        });
    }

    @Override
    public void valueChanged(SmoothMover source, double newValue, int pourcent)
    {
        if (source == zoomFactor)
        {
            zoomLabel.setText(Integer.toString((int) (newValue * 100)) + "%");

            // update mouse position on unexpected position change
            view2D.checkLocationChanged();
            // update view2D dimension
            view2D.updateSize();

            // calculate delta in mouse canvas position
            final Point newMouseCanvasPos = imageToCanvas(view2D.mouseImagePos);
            final int dx = newMouseCanvasPos.x - view2D.mouseCanvasPos.x;
            final int dy = newMouseCanvasPos.y - view2D.mouseCanvasPos.y;
            // add to current canvas offset
            view2D.setLocationSafe(getCanvasOffsetX() - dx, getCanvasOffsetY() - dy);

            // calculate new theorical mouse image position
            Point p = new Point(view2D.mouseCanvasPos);
            final Point2D newMouseImagePos = canvasToImage(p);

            // if distance between theorical mouse image pos and current pos > 1
            if (view2D.mouseImagePos.distance(newMouseImagePos) > (2 / zoomFactor.getValue()))
            {
                // we change to the new image position
                view2D.setMouseImagePos(newMouseImagePos, null, false);
                // update mouse positions from the mouse image position
                view2D.updateMousePositionFromImagePos();
            }

            scaleChanged(DimensionId.X);
            scaleChanged(DimensionId.Y);

            // force repaint as size and scroll can stay unchanged
            // while zoom factor changed (so image changed)
            repaint();
        }
        else if (source == zoomAlpha)
            repaint();
    }

    @Override
    public void moveEnded(SmoothMover source, double value)
    {
        if (source == zoomFactor)
        {
            // zoomAlpha.setValue(0);
            repaint();
        }
    }

    @Override
    public void moveStarted(SmoothMover source, double start, double end)
    {
        if (source == zoomFactor)
        {
            // modify zoom info alpha level
            zoomAlpha.setValue(1);
            zoomAlpha.moveTo(0);
            repaint();
        }
    }

    @Override
    public void moveModified(SmoothMover source, double start, double end)
    {
        if (source == zoomFactor)
        {
            // modify zoom info alpha level
            zoomAlpha.setValue(1);
            zoomAlpha.moveTo(0);
            repaint();
        }
    }

    @Override
    public void toolChanged(String command)
    {
        // update tool
        prevTool = currentTool;
        currentTool = command;

        // update cursor
        view2D.updateCursor();

        // unselect current selected ROI
        final Sequence seq = getSequence();
        if (seq != null)
            seq.setSelectedROI(null, false);
    }
}
