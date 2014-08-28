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
package icy.gui.lut;

import icy.gui.component.math.HistogramPanel;
import icy.gui.component.math.HistogramPanel.HistogramPanelListener;
import icy.gui.viewer.Viewer;
import icy.gui.viewer.ViewerEvent;
import icy.gui.viewer.ViewerEvent.ViewerEventType;
import icy.gui.viewer.ViewerListener;
import icy.image.lut.LUT.LUTChannel;
import icy.image.lut.LUT.LUTChannelEvent;
import icy.image.lut.LUT.LUTChannelEvent.LUTChannelEventType;
import icy.image.lut.LUT.LUTChannelListener;
import icy.math.ArrayMath;
import icy.math.Histogram;
import icy.math.MathUtil;
import icy.math.Scaler;
import icy.sequence.Sequence;
import icy.sequence.SequenceEvent;
import icy.sequence.SequenceEvent.SequenceEventSourceType;
import icy.sequence.SequenceListener;
import icy.system.thread.ThreadUtil;
import icy.type.DataType;
import icy.type.collection.array.Array1DUtil;
import icy.util.ColorUtil;
import icy.util.EventUtil;
import icy.util.GraphicsUtil;
import icy.util.StringUtil;
import ij.util.ArrayUtil;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Point2D;
import java.lang.reflect.Array;
import java.util.EventListener;

import javax.swing.JPanel;
import javax.swing.event.EventListenerList;

/**
 * @author stephane
 */
public class ScalerViewer extends JPanel implements SequenceListener, LUTChannelListener, ViewerListener
{
    private static enum actionType
    {
        NULL, MODIFY_LOWBOUND, MODIFY_HIGHBOUND
    }

    public static interface ScalerPositionListener extends EventListener
    {
        public void positionChanged(double index, int value, double normalizedValue);
    }

    private class ScalerHistogramPanel extends HistogramPanel implements MouseListener, MouseMotionListener,
            MouseWheelListener
    {
        /**
         * 
         */
        private static final long serialVersionUID = -7020904979961676368L;

        /**
         * internals
         */
        private actionType action;
        private final Point2D positionInfo;
        private boolean mouseOnLeft;

        public ScalerHistogramPanel(Scaler s)
        {
            super(s.getAbsLeftIn(), s.getAbsRightIn(), s.isIntegerData());

            action = actionType.NULL;
            positionInfo = new Point2D.Double();
            mouseOnLeft = false;

            // we want to display our own background
            // setOpaque(false);
            // dimension (don't change it or you will regret !)
            setMinimumSize(new Dimension(100, 100));
            setPreferredSize(new Dimension(240, 100));

            // add listeners
            addMouseListener(this);
            addMouseMotionListener(this);
            addMouseWheelListener(this);
        }

        /**
         * update mouse cursor
         */
        private void updateCursor(Point pos)
        {
            final int cursor;

            if (action != actionType.NULL)
                cursor = Cursor.W_RESIZE_CURSOR;
            else if (isOverX(pos, getLowBoundPos()) || isOverX(pos, getHighBoundPos()))
                cursor = Cursor.HAND_CURSOR;
            else
                cursor = Cursor.DEFAULT_CURSOR;

            // only if different
            if (getCursor().getType() != cursor)
                setCursor(Cursor.getPredefinedCursor(cursor));
        }

        private void setPositionInfo(double index, int value, double normalizedValue)
        {
            if ((positionInfo.getX() != index) || (positionInfo.getY() != value))
            {
                positionInfo.setLocation(index, normalizedValue);
                scalerPositionChanged(index, value, normalizedValue);
                repaint();
            }
        }

        /**
         * Check if Point p is over area (u, *)
         * 
         * @param p
         *        point
         * @param x
         *        area position
         * @return boolean
         */
        private boolean isOverX(Point p, int u)
        {
            return isOver(p.x, p.y, u, -1, ISOVER_DEFAULT_MARGIN);
        }

        /**
         * Check if (x, y) is over area (u, v)
         * 
         * @param x
         * @param y
         *        pointer
         * @param u
         * @param v
         *        area position
         * @param margin
         *        allowed margin
         * @return boolean
         */
        private boolean isOver(int x, int y, int u, int v, int margin)
        {
            final boolean x_ok;
            final boolean y_ok;

            x_ok = (u == -1) || ((x >= (u - margin)) && (x <= (u + margin)));
            y_ok = (v == -1) || ((y >= (v - margin)) && (y <= (v + margin)));

            return x_ok && y_ok;
        }

        public int getLowBoundPos()
        {
            return dataToPixel(getLowBound());
        }

        public int getHighBoundPos()
        {
            return dataToPixel(getHighBound());
        }

        private void setLowBoundPos(int pos)
        {
            setLowBound(pixelToData(pos));
        }

        private void setHighBoundPos(int pos)
        {
            setHighBound(pixelToData(pos));
        }

        @Override
        protected void paintComponent(Graphics g)
        {
            updateHisto();

            super.paintComponent(g);

            final Graphics2D g2 = (Graphics2D) g.create();
            try
            {
                // display mouse position infos
                if (positionInfo.getX() != -1)
                {
                    final int x = dataToPixel(positionInfo.getX());
                    final int hRange = getClientHeight() - 1;
                    final int bottom = hRange + getClientY();
                    final int y = bottom - (int) (positionInfo.getY() * hRange);

                    g2.setColor(ColorUtil.xor(getForeground()));
                    g2.drawLine(x, bottom, x, y);
                }

                paintBounds(g2);

                if (!StringUtil.isEmpty(message))
                {
                    // string display
                    g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));

                    final Rectangle hintBounds = GraphicsUtil.getHintBounds(g2, message, 10, 4);

                    if (mouseOnLeft)
                        GraphicsUtil.drawHint(g2, message, getWidth() - (10 + hintBounds.width), 4, getForeground(),
                                getBackground());
                    else
                        GraphicsUtil.drawHint(g2, message, 10, 4, getForeground(), getBackground());
                }
            }
            finally
            {
                g2.dispose();
            }
        }

        /**
         * draw bounds
         */
        private void paintBounds(Graphics2D g)
        {
            final int h = getClientHeight() - 1;
            final int y = getClientY();
            final int lowBound = getLowBoundPos();
            final int highBound = getHighBoundPos();

            g.setColor(ColorUtil.mix(Color.blue, Color.white, false));
            g.drawRect(lowBound - 2, y, 3, h);
            g.setColor(Color.blue);
            g.fillRect(lowBound - 1, y + 1, 2, h - 1);
            g.setColor(ColorUtil.mix(Color.red, Color.white, false));
            g.drawRect(highBound - 1, y, 3, h);
            g.setColor(Color.red);
            g.fillRect(highBound, y + 1, 2, h - 1);
        }

        @Override
        public void mouseClicked(MouseEvent e)
        {

        }

        @Override
        public void mouseEntered(MouseEvent e)
        {
            updateCursor(e.getPoint());
        }

        @Override
        public void mouseExited(MouseEvent e)
        {
            if (getCursor().getType() != Cursor.getDefaultCursor().getType())
                setCursor(Cursor.getDefaultCursor());

            // hide message
            setMessage("");
            setPositionInfo(-1, -1, -1);
        }

        @Override
        public void mousePressed(MouseEvent e)
        {
            final Point pos = e.getPoint();

            if (EventUtil.isLeftMouseButton(e))
            {
                if (isOverX(pos, getLowBoundPos()))
                    action = actionType.MODIFY_LOWBOUND;
                else if (isOverX(pos, getHighBoundPos()))
                    action = actionType.MODIFY_HIGHBOUND;

                // show message
                if (action != actionType.NULL)
                {
                    if (EventUtil.isShiftDown(e))
                        setMessage("GLOBAL MOVE");
                    else
                        setMessage("Maintain 'Shift' for global move");
                }

                updateCursor(e.getPoint());
            }
            else if (EventUtil.isRightMouseButton(e))
            {
                // showPopupMenu(pos);
            }
        }

        @Override
        public void mouseReleased(MouseEvent e)
        {
            if (EventUtil.isLeftMouseButton(e))
            {
                action = actionType.NULL;

                updateCursor(e.getPoint());

                setMessage("");
            }
        }

        @Override
        public void mouseDragged(MouseEvent e)
        {
            final Point pos = e.getPoint();

            mouseOnLeft = pos.x < (getWidth() / 2);

            final boolean shift = EventUtil.isShiftDown(e);

            switch (action)
            {
                case MODIFY_LOWBOUND:
                    setLowBoundPos(pos.x);
                    // also modify others bounds
                    if (shift)
                    {
                        final double newLowBound = getLowBound();
                        for (LUTChannel lc : lutChannel.getLut().getLutChannels())
                            lc.setMin(newLowBound);
                    }
                    break;

                case MODIFY_HIGHBOUND:
                    setHighBoundPos(pos.x);
                    // also modify others bounds
                    if (shift)
                    {
                        final double newHighBound = getHighBound();
                        for (LUTChannel lc : lutChannel.getLut().getLutChannels())
                            lc.setMax(newHighBound);
                    }
                    break;
            }

            // message
            if (action != actionType.NULL)
            {
                if (shift)
                    setMessage("GLOBAL MOVE");
                else
                    setMessage("Maintain 'Shift' for global move");
            }

            if (getBinNumber() > 0)
            {
                final int bin = pixelToBin(pos.x);
                double index = pixelToData(pos.x);
                final int value = getBinSize(bin);

                // use integer index with integer data type
                if (isIntegerType())
                    index = Math.floor(index);

                if (action == actionType.NULL)
                {
                    final String valueText = "value : " + MathUtil.roundSignificant(index, 5, true);
                    final String pixelText = "pixel number : " + value;

                    setMessage(valueText + "\n" + pixelText);
                    // setToolTipText("<html>" + valueText + "<br>" + pixelText);
                }

                setPositionInfo(index, value, getAdjustedBinSize(bin));
            }
        }

        @Override
        public void mouseMoved(MouseEvent e)
        {
            final Point pos = e.getPoint();

            mouseOnLeft = pos.x < (getWidth() / 2);

            updateCursor(e.getPoint());

            if (getBinNumber() > 0)
            {
                final int bin = pixelToBin(pos.x);
                double index = pixelToData(pos.x);
                final int value = getBinSize(bin);

                // use integer index with integer data type
                if (isIntegerType())
                    index = Math.round(index);

                final String valueText = "value : " + MathUtil.roundSignificant(index, 5, true);
                final String pixelText = "pixel number : " + value;

                setMessage(valueText + "\n" + pixelText);
                // setToolTipText("<html>" + valueText + "<br>" + pixelText);

                setPositionInfo(index, value, getAdjustedBinSize(bin));
            }
        }

        @Override
        public void mouseWheelMoved(MouseWheelEvent e)
        {

        }
    }

    /**
     * 
     */
    private static final long serialVersionUID = -1236985071716650592L;

    private static final int ISOVER_DEFAULT_MARGIN = 3;

    /**
     * associated viewer & lutChannel
     */
    Viewer viewer;
    LUTChannel lutChannel;
    /**
     * histogram
     */
    private ScalerHistogramPanel histogram;
    private boolean autoRefresh;
    private boolean autoBounds;
    private boolean histoNeedRefresh;

    /**
     * listeners
     */
    private final EventListenerList scalerMapPositionListeners;

    /**
     * internals
     */
    private final Runnable histoUpdater;
    String message;

    /**
     * 
     */
    public ScalerViewer(Viewer viewer, LUTChannel lutChannel)
    {
        super();

        this.viewer = viewer;
        this.lutChannel = lutChannel;

        message = "";
        scalerMapPositionListeners = new EventListenerList();
        histoUpdater = new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    // refresh histogram
                    refreshHistoDataInternal();
                }
                catch (Exception e)
                {
                    // just ignore error, it's permitted here
                }
            }
        };

        histogram = new ScalerHistogramPanel(lutChannel.getScaler());
        // listen for need refresh event
        histogram.addListener(new HistogramPanelListener()
        {
            @Override
            public void histogramNeedRefresh(HistogramPanel source)
            {
                internalRequestHistoDataRefresh();
            }
        });
        histoNeedRefresh = false;
        autoRefresh = true;
        autoBounds = false;

        setLayout(new BorderLayout());
        add(histogram, BorderLayout.CENTER);
        validate();

        // force first refresh
        internalRequestHistoDataRefresh();

        // add listeners
        final Sequence sequence = viewer.getSequence();

        if (sequence != null)
            sequence.addListener(this);
        viewer.addListener(this);
        lutChannel.addListener(this);
    }

    public void requestHistoDataRefresh()
    {
        internalRequestHistoDataRefresh();
    }

    private boolean isHistoVisible()
    {
        if (!isValid())
            return false;

        return getVisibleRect().intersects(histogram.getBounds());
    }

    void internalRequestHistoDataRefresh()
    {
        if (isHistoVisible())
            refreshHistoData();
        else
            histoNeedRefresh = true;
    }

    void updateHisto()
    {
        if (histoNeedRefresh)
        {
            refreshHistoData();
            histoNeedRefresh = false;
        }
    }

    private void refreshHistoData()
    {
        // send refresh operation
        ThreadUtil.bgRunSingle(histoUpdater);
    }

    // this method is called by processor, we don't mind about exception here
    void refreshHistoDataInternal()
    {
        final Histogram histo = histogram.getHistogram();
        final Sequence seq = viewer.getSequence();

        histogram.reset();
        try
        {
            if (seq != null)
            {
                final int maxZ;
                final int maxT;
                int t = viewer.getPositionT();
                int z = viewer.getPositionZ();

                if (t != -1)
                    maxT = t;
                else
                {
                    t = 0;
                    maxT = seq.getSizeT() - 1;
                }

                if (z != -1)
                    maxZ = z;
                else
                {
                    z = 0;
                    maxZ = seq.getSizeZ() - 1;
                }

                final int c = lutChannel.getChannel();

                for (; t <= maxT; t++)
                {
                    for (; z <= maxZ; z++)
                    {
                        final Object data = seq.getDataXY(t, z, c);
                        final DataType dataType = seq.getDataType_();
                        final int len = Array.getLength(data);

                        for (int i = 0; i < len; i++)
                        {
                            if ((i & 0xFFF) == 0)
                            {
                                // need to be recalculated so don't waste time here...
                                if (ThreadUtil.hasWaitingBgSingleTask(histoUpdater))
                                    return;
                            }

                            histo.addValue(Array1DUtil.getValue(data, i, dataType));
                        }
                    }
                }
            }
        }
        catch (Exception e)
        {
            // just redo it later
            refreshHistoData();
            // System.out.println("error");
        }
        finally
        {
            // notify that histogram computation is done
            histogram.done();

            // histogram changed in the meantime --> recompute
            if (histo != histogram.getHistogram())
                refreshHistoData();
        }
    }

    /**
     * @return the histogram
     */
    public HistogramPanel getHistogram()
    {
        return histogram;
    }

    /**
     * @return the histoData
     */
    public double[] getHistoData()
    {
        return histogram.getHistogramData();
    }

    /**
     * @return the scaler
     */
    public Scaler getScaler()
    {
        return lutChannel.getScaler();
    }

    public double getLowBound()
    {
        return lutChannel.getMin();
    }

    public double getHighBound()
    {
        return lutChannel.getMax();
    }

    void setLowBound(double value)
    {
        lutChannel.setMin(value);
    }

    void setHighBound(double value)
    {
        lutChannel.setMax(value);
    }

    /**
     * tasks to do on scaler changes
     */
    public void onScalerChanged()
    {
        final Scaler s = getScaler();

        histogram.setMinMaxIntValues(s.getAbsLeftIn(), s.getAbsRightIn(), s.isIntegerData());

        // repaint component now as bounds may have changed
        repaint();
    }

    /**
     * process on sequence change
     */
    void onSequenceDataChanged()
    {
        // update histogram
        if (autoRefresh)
            requestHistoDataRefresh();
    }

    /**
     * process on position changed
     */
    private void onPositionChanged()
    {
        // update histogram
        if (autoRefresh)
            requestHistoDataRefresh();
    }

    /**
     * @return the message
     */
    public String getMessage()
    {
        return message;
    }

    /**
     * @param value
     *        the message to set
     */
    public void setMessage(String value)
    {
        if (!StringUtil.equals(message, value))
        {
            message = value;
            repaint();
        }
    }

    /**
     * @return the autoBounds
     */
    public boolean getAutoBounds()
    {
        return autoBounds;
    }

    /**
     * Set the autoBounds
     */
    public void setAutoBounds(boolean value)
    {
        if (autoBounds != value)
        {
            autoBounds = value;
            histogram.repaint();
        }
    }

    /**
     * @return the autoRefresh
     */
    public boolean getAutoRefresh()
    {
        return autoRefresh;
    }

    /**
     * Set the autoRefresh
     */
    public void setAutoRefresh(boolean value)
    {
        if (autoRefresh != value)
        {
            if (value)
                requestHistoDataRefresh();
            autoRefresh = value;
        }
    }

    /**
     * @return the logScale
     */
    public boolean getLogScale()
    {
        return histogram.getLogScaling();
    }

    /**
     * @param value
     *        the logScale to set
     */
    public void setLogScale(boolean value)
    {
        histogram.setLogScaling(value);
    }

    /**
     * show popup menu
     */
    // private void showPopupMenu(final Point pos)
    // {
    // // rebuild menu
    // final JPopupMenu menu = new JPopupMenu();
    //
    // final JMenu scaleMenu = new JMenu("Scaling");
    //
    // final JCheckBoxMenuItem logItem = new JCheckBoxMenuItem("Log", logScale);
    // logItem.addActionListener(new ActionListener()
    // {
    // @Override
    // public void actionPerformed(ActionEvent e)
    // {
    // setLogScale(true);
    // }
    // });
    //
    // final JCheckBoxMenuItem linearItem = new JCheckBoxMenuItem("Linear", !logScale);
    // linearItem.addActionListener(new ActionListener()
    // {
    // @Override
    // public void actionPerformed(ActionEvent e)
    // {
    // setLogScale(false);
    // }
    // });
    //
    // scaleMenu.add(logItem);
    // scaleMenu.add(linearItem);
    //
    // menu.add(scaleMenu);
    //
    // menu.pack();
    // menu.validate();
    //
    // // display menu
    // menu.show(this, pos.x, pos.y);
    // }

    /**
     * Add a listener
     * 
     * @param listener
     */
    public void addScalerPositionListener(ScalerPositionListener listener)
    {
        scalerMapPositionListeners.add(ScalerPositionListener.class, listener);
    }

    /**
     * Remove a listener
     * 
     * @param listener
     */
    public void removeScalerPositionListener(ScalerPositionListener listener)
    {
        scalerMapPositionListeners.remove(ScalerPositionListener.class, listener);
    }

    /**
     * mouse position on scaler info changed
     */
    public void scalerPositionChanged(double index, int value, double normalizedValue)
    {
        for (ScalerPositionListener listener : scalerMapPositionListeners.getListeners(ScalerPositionListener.class))
            listener.positionChanged(index, value, normalizedValue);
    }

    @Override
    public void lutChannelChanged(LUTChannelEvent event)
    {
        if (event.getType() == LUTChannelEventType.SCALER_CHANGED)
            onScalerChanged();
    }

    @Override
    public void viewerChanged(ViewerEvent event)
    {
        if (event.getType() == ViewerEventType.POSITION_CHANGED)
            onPositionChanged();
    }

    @Override
    public void viewerClosed(Viewer viewer)
    {
        viewer.removeListener(this);
    }

    @Override
    public void sequenceChanged(SequenceEvent sequenceEvent)
    {
        if (sequenceEvent.getSourceType() == SequenceEventSourceType.SEQUENCE_DATA)
            onSequenceDataChanged();
    }

    @Override
    public void sequenceClosed(Sequence sequence)
    {
        sequence.removeListener(this);
    }

}
