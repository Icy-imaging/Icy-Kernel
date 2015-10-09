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
package icy.gui.component.ui;

import icy.gui.component.RangeSlider;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Map;

import javax.swing.ButtonModel;
import javax.swing.DefaultButtonModel;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.pushingpixels.lafwidget.LafWidgetUtilities;
import org.pushingpixels.substance.api.ColorSchemeAssociationKind;
import org.pushingpixels.substance.api.ComponentState;
import org.pushingpixels.substance.api.SubstanceColorScheme;
import org.pushingpixels.substance.api.painter.border.SubstanceBorderPainter;
import org.pushingpixels.substance.api.painter.fill.ClassicFillPainter;
import org.pushingpixels.substance.api.painter.fill.SubstanceFillPainter;
import org.pushingpixels.substance.internal.animation.StateTransitionTracker;
import org.pushingpixels.substance.internal.animation.TransitionAwareUI;
import org.pushingpixels.substance.internal.painter.BackgroundPaintingUtils;
import org.pushingpixels.substance.internal.ui.SubstanceSliderUI;
import org.pushingpixels.substance.internal.utils.HashMapKey;
import org.pushingpixels.substance.internal.utils.RolloverControlListener;
import org.pushingpixels.substance.internal.utils.SubstanceColorSchemeUtilities;
import org.pushingpixels.substance.internal.utils.SubstanceCoreUtilities;
import org.pushingpixels.substance.internal.utils.SubstanceOutlineUtilities;
import org.pushingpixels.substance.internal.utils.SubstanceSizeUtils;

/**
 * UI delegate for the RangeSlider component with Substance AndFeel.
 * RangeSliderUI paints two thumbs, one for the lower value and one for the upper value.
 * 
 * @author Stephane Dallongeville
 */
public class RangeSliderUI extends SubstanceSliderUI
{
    /** Location and size of thumb for upper value. */
    Rectangle upperThumbRect;
    /** Indicator that determines whether upper thumb is selected. */
    boolean upperThumbSelected;

    /** Indicator that determines whether lower thumb is being dragged. */
    transient boolean lowerDragging;
    /** Indicator that determines whether upper thumb is being dragged. */
    transient boolean upperDragging;

    /**
     * Surrogate button model for tracking the general slider transitions.
     */
    ButtonModel sliderModel;
    /**
     * Surrogate button model for tracking the upper thumb transitions.
     */
    ButtonModel upperThumbModel;
    /**
     * General slider transition tracker.
     */
    protected StateTransitionTracker sliderStateTransitionTracker;
    /**
     * Upper thumb transition tracker.
     */
    protected StateTransitionTracker upperThumbStateTransitionTracker;

    /**
     * Listener for general slider transition animations.
     */
    protected RolloverControlListener sliderRolloverListener;
    /**
     * Listener for upper thumb transition animations.
     */
    protected RolloverControlListener upperThumbRolloverListener;

    /**
     * Listener on property change events.
     */
    protected PropertyChangeListener sliderPropertyChangeListener;

    /**
     * Needed to return correct transition tracker on thumb paint.
     */
    private boolean paintingLowerThumb;
    private boolean paintingUpperThumb;

    /**
     * Constructs a RangeSliderUI for the specified slider component.
     * 
     * @param rangeSlider
     *        RangeSlider
     */
    public RangeSliderUI(RangeSlider rangeSlider)
    {
        super(rangeSlider);

        sliderModel = new DefaultButtonModel();
        sliderModel.setArmed(false);
        sliderModel.setSelected(false);
        sliderModel.setPressed(false);
        sliderModel.setRollover(false);
        sliderModel.setEnabled(rangeSlider.isEnabled());

        upperThumbModel = new DefaultButtonModel();
        upperThumbModel.setArmed(false);
        upperThumbModel.setSelected(false);
        upperThumbModel.setPressed(false);
        upperThumbModel.setRollover(false);
        upperThumbModel.setEnabled(rangeSlider.isEnabled());

        sliderStateTransitionTracker = new StateTransitionTracker(rangeSlider, sliderModel);
        upperThumbStateTransitionTracker = new StateTransitionTracker(rangeSlider, upperThumbModel);

        paintingLowerThumb = false;
        paintingUpperThumb = false;
    }

    /**
     * Installs this UI delegate on the specified component.
     */
    @Override
    public void installUI(JComponent c)
    {
        upperThumbRect = new Rectangle();
        super.installUI(c);
    }

    @Override
    protected void installListeners(JSlider slider)
    {
        super.installListeners(slider);

        sliderRolloverListener = new RolloverControlListener(new TransitionAwareUI()
        {
            @Override
            public boolean isInside(MouseEvent me)
            {
                final double x = me.getX();
                final double y = me.getY();
                return isInsideLowerThumbInternal(x, y) || isInsideUpperThumbInternal(x, y);
            }

            @Override
            public StateTransitionTracker getTransitionTracker()
            {
                return sliderStateTransitionTracker;
            }
        }, sliderModel);

        slider.addMouseListener(sliderRolloverListener);
        slider.addMouseMotionListener(sliderRolloverListener);

        upperThumbRolloverListener = new RolloverControlListener(new TransitionAwareUI()
        {
            @Override
            public boolean isInside(MouseEvent me)
            {
                return isInsideUpperThumb(me.getX(), me.getY());
            }

            @Override
            public StateTransitionTracker getTransitionTracker()
            {
                return upperThumbStateTransitionTracker;
            }
        }, upperThumbModel);

        slider.addMouseListener(upperThumbRolloverListener);
        slider.addMouseMotionListener(upperThumbRolloverListener);

        sliderPropertyChangeListener = new PropertyChangeListener()
        {
            @Override
            public void propertyChange(PropertyChangeEvent evt)
            {
                if ("enabled".equals(evt.getPropertyName()))
                {
                    final boolean enabled = RangeSliderUI.this.slider.isEnabled();

                    sliderModel.setEnabled(enabled);
                    upperThumbModel.setEnabled(enabled);
                }
            }
        };
        slider.addPropertyChangeListener(sliderPropertyChangeListener);

        sliderStateTransitionTracker.registerModelListeners();
        sliderStateTransitionTracker.registerFocusListeners();
        upperThumbStateTransitionTracker.registerModelListeners();
        upperThumbStateTransitionTracker.registerFocusListeners();
    }

    @Override
    protected void uninstallListeners(JSlider slider)
    {
        super.uninstallListeners(slider);

        slider.removeMouseListener(sliderRolloverListener);
        slider.removeMouseMotionListener(sliderRolloverListener);
        sliderRolloverListener = null;
        slider.removeMouseListener(upperThumbRolloverListener);
        slider.removeMouseMotionListener(upperThumbRolloverListener);
        upperThumbRolloverListener = null;

        slider.removePropertyChangeListener(sliderPropertyChangeListener);
        sliderPropertyChangeListener = null;

        sliderStateTransitionTracker.unregisterModelListeners();
        sliderStateTransitionTracker.unregisterFocusListeners();
        upperThumbStateTransitionTracker.unregisterModelListeners();
        upperThumbStateTransitionTracker.unregisterFocusListeners();
    }

    /**
     * Creates a listener to handle track events in the specified slider.
     */
    @Override
    protected TrackListener createTrackListener(JSlider slider)
    {
        return new RangeTrackListener();
    }

    /**
     * Creates a listener to handle change events in the specified slider.
     */
    @Override
    protected ChangeListener createChangeListener(JSlider slider)
    {
        return new ChangeHandler();
    }

    /**
     * Updates the dimensions for both thumbs.
     */
    @Override
    protected void calculateThumbSize()
    {
        // Call superclass method for lower thumb size.
        super.calculateThumbSize();

        // Set upper thumb size.
        upperThumbRect.setSize(thumbRect.width, thumbRect.height);
    }

    /**
     * Updates the locations for both thumbs.
     */
    @Override
    protected void calculateThumbLocation()
    {
        // Call superclass method for lower thumb location.
        super.calculateThumbLocation();

        // Adjust upper value to snap to ticks if necessary.
        if (slider.getSnapToTicks())
        {
            int upperValue = slider.getValue() + slider.getExtent();
            int snappedValue = upperValue;
            int majorTickSpacing = slider.getMajorTickSpacing();
            int minorTickSpacing = slider.getMinorTickSpacing();
            int tickSpacing = 0;

            if (minorTickSpacing > 0)
            {
                tickSpacing = minorTickSpacing;
            }
            else if (majorTickSpacing > 0)
            {
                tickSpacing = majorTickSpacing;
            }

            if (tickSpacing != 0)
            {
                // If it's not on a tick, change the value
                if ((upperValue - slider.getMinimum()) % tickSpacing != 0)
                {
                    float temp = (float) (upperValue - slider.getMinimum()) / (float) tickSpacing;
                    int whichTick = Math.round(temp);
                    snappedValue = slider.getMinimum() + (whichTick * tickSpacing);
                }

                if (snappedValue != upperValue)
                {
                    slider.setExtent(snappedValue - slider.getValue());
                }
            }
        }

        Rectangle trackRect = this.getPaintTrackRect();

        if (slider.getOrientation() == SwingConstants.HORIZONTAL)
        {
            int valuePosition = xPositionForValue(slider.getValue() + slider.getExtent());

            double centerY = trackRect.y + trackRect.height / 2.0;
            upperThumbRect.y = (int) (centerY - upperThumbRect.height / 2.0) + 1;
            upperThumbRect.x = valuePosition - upperThumbRect.width / 2;
        }
        else
        {
            int valuePosition = yPositionForValue(slider.getValue() + slider.getExtent());

            double centerX = trackRect.x + trackRect.width / 2.0;
            upperThumbRect.x = (int) (centerX - upperThumbRect.width / 2.0) + 1;
            upperThumbRect.y = valuePosition - (upperThumbRect.height / 2);
        }
    }

    @Override
    public boolean isInside(MouseEvent me)
    {
        return isInsideLowerThumb(me.getX(), me.getY());
    }

    public boolean isInsideLowerThumbInternal(double x, double y)
    {
        final Rectangle thumbB = this.thumbRect;
        return thumbB != null && thumbB.contains(x, y);
    }

    public boolean isInsideLowerThumb(double x, double y)
    {
        // inside lower ?
        if (isInsideLowerThumbInternal(x, y))
        {
            // also inside upper ?
            if (isInsideUpperThumbInternal(x, y))
            {
                final double dl = Point2D.distance(thumbRect.getCenterX(), thumbRect.getCenterY(), x, y);
                final double du = Point2D.distance(upperThumbRect.getCenterX(), upperThumbRect.getCenterY(), x, y);

                return (dl < du);
            }

            return true;
        }

        return false;
    }

    public boolean isInsideUpperThumbInternal(double x, double y)
    {
        final Rectangle upperThumbR = upperThumbRect;
        return (upperThumbR != null) && upperThumbR.contains(x, y);
    }

    public boolean isInsideUpperThumb(double x, double y)
    {
        // inside lower ?
        if (isInsideUpperThumbInternal(x, y))
        {
            // also inside upper ?
            if (isInsideLowerThumbInternal(x, y))
            {
                // find closest one
                final double dl = Point2D.distance(thumbRect.getCenterX(), thumbRect.getCenterY(), x, y);
                final double du = Point2D.distance(upperThumbRect.getCenterX(), upperThumbRect.getCenterY(), x, y);

                return (du <= dl);
            }

            return true;
        }

        return false;
    }

    @Override
    public StateTransitionTracker getTransitionTracker()
    {
        if (paintingLowerThumb)
            return super.getTransitionTracker();
        if (paintingUpperThumb)
            return upperThumbStateTransitionTracker;

        return sliderStateTransitionTracker;
    }

    /**
     * Returns the rectangle of track for painting.
     * 
     * @return The rectangle of track for painting.
     */
    private Rectangle getPaintTrackRect()
    {
        int trackLeft = 0;
        int trackRight;
        int trackTop = 0;
        int trackBottom;
        int trackWidth = this.getTrackWidth();

        if (this.slider.getOrientation() == SwingConstants.HORIZONTAL)
        {
            trackTop = 3 + this.insetCache.top + 2 * this.focusInsets.top;
            trackBottom = trackTop + trackWidth - 1;
            trackRight = this.trackRect.width;
            return new Rectangle(this.trackRect.x + trackLeft, trackTop, trackRight - trackLeft, trackBottom - trackTop);
        }

        if (this.slider.getPaintLabels() || this.slider.getPaintTicks())
        {
            if (this.slider.getComponentOrientation().isLeftToRight())
            {
                trackLeft = trackRect.x + this.insetCache.left + this.focusInsets.left;
                trackRight = trackLeft + trackWidth - 1;
            }
            else
            {
                trackRight = trackRect.x + trackRect.width - this.insetCache.right - this.focusInsets.right;
                trackLeft = trackRight - trackWidth - 1;
            }
        }
        else
        {
            // horizontally center the track
            if (this.slider.getComponentOrientation().isLeftToRight())
            {
                trackLeft = (this.insetCache.left + this.focusInsets.left + this.slider.getWidth()
                        - this.insetCache.right - this.focusInsets.right)
                        / 2 - trackWidth / 2;
                trackRight = trackLeft + trackWidth - 1;
            }
            else
            {
                trackRight = (this.insetCache.left + this.focusInsets.left + this.slider.getWidth()
                        - this.insetCache.right - this.focusInsets.right)
                        / 2 + trackWidth / 2;
                trackLeft = trackRight - trackWidth - 1;
            }
        }

        trackBottom = this.trackRect.height - 1;
        return new Rectangle(trackLeft, this.trackRect.y + trackTop, trackRight - trackLeft, trackBottom - trackTop);
    }

    @Override
    public void paint(Graphics g, final JComponent c)
    {
        Graphics2D graphics = (Graphics2D) g.create();

        ComponentState currState = ComponentState.getState(sliderModel, slider);
        float alpha = SubstanceColorSchemeUtilities.getAlpha(slider, currState);

        BackgroundPaintingUtils.updateIfOpaque(graphics, c);

        recalculateIfInsetsChanged();
        recalculateIfOrientationChanged();
        final Rectangle clip = graphics.getClipBounds();

        if (!clip.intersects(trackRect) && slider.getPaintTrack())
            calculateGeometry();

        graphics.setComposite(LafWidgetUtilities.getAlphaComposite(this.slider, alpha, g));
        if (slider.getPaintTrack() && clip.intersects(trackRect))
        {
            paintTrack(graphics);
        }
        if (slider.getPaintTicks() && clip.intersects(tickRect))
        {
            paintTicks(graphics);
        }
        // don't paint focus as component is not focusable
        // paintFocus(graphics);
        if (clip.intersects(thumbRect))
        {
            paintLowerThumb(graphics);
        }
        if (clip.intersects(upperThumbRect))
        {
            paintUpperThumb(graphics);
        }
        graphics.setComposite(LafWidgetUtilities.getAlphaComposite(this.slider, 1.0f, g));
        if (slider.getPaintLabels() && clip.intersects(labelRect))
        {
            paintLabels(graphics);
        }

        graphics.dispose();
    }

    public void paintLowerThumb(Graphics g)
    {
        paintingLowerThumb = true;

        // default implementation
        paintThumb(g);

        paintingLowerThumb = false;
    }

    public void paintUpperThumb(Graphics g)
    {
        paintingUpperThumb = true;

        final Graphics2D graphics = (Graphics2D) g.create();
        final Rectangle knobBounds = upperThumbRect;

        graphics.translate(knobBounds.x, knobBounds.y);

        final Icon icon = getIcon();

        if (slider.getOrientation() == SwingConstants.HORIZONTAL)
        {
            if (icon != null)
                icon.paintIcon(this.slider, graphics, -1, 0);
        }
        else
        {
            if (slider.getComponentOrientation().isLeftToRight())
            {
                if (icon != null)
                    icon.paintIcon(this.slider, graphics, 0, -1);
            }
            else
            {
                if (icon != null)
                    icon.paintIcon(this.slider, graphics, 0, 1);
            }
        }

        graphics.dispose();

        paintingUpperThumb = false;
    }

    @Override
    public void paintTrack(Graphics g)
    {
        Graphics2D graphics = (Graphics2D) g.create();

        boolean drawInverted = drawInverted();

        Rectangle paintRect = getPaintTrackRect();

        // Width and height of the painting rectangle.
        int width = paintRect.width;
        int height = paintRect.height;

        if (this.slider.getOrientation() == SwingConstants.VERTICAL)
        {
            // apply rotation / translate transformation on vertical
            // slider tracks
            int temp = width;
            // noinspection SuspiciousNameCombination
            width = height;
            height = temp;
            AffineTransform at = graphics.getTransform();
            at.translate(paintRect.x, width + paintRect.y);
            at.rotate(-Math.PI / 2);
            graphics.setTransform(at);
        }
        else
        {
            graphics.translate(paintRect.x, paintRect.y);
        }

        StateTransitionTracker.ModelStateInfo modelStateInfo = sliderStateTransitionTracker.getModelStateInfo();

        SubstanceColorScheme trackSchemeUnselected = SubstanceColorSchemeUtilities.getColorScheme(this.slider,
                slider.isEnabled() ? ComponentState.ENABLED : ComponentState.DISABLED_UNSELECTED);
        SubstanceColorScheme trackBorderSchemeUnselected = SubstanceColorSchemeUtilities.getColorScheme(this.slider,
                ColorSchemeAssociationKind.BORDER, this.slider.isEnabled() ? ComponentState.ENABLED
                        : ComponentState.DISABLED_UNSELECTED);
        paintSliderTrack(graphics, drawInverted, trackSchemeUnselected, trackBorderSchemeUnselected, width, height);

        Map<ComponentState, StateTransitionTracker.StateContributionInfo> activeStates = modelStateInfo
                .getStateContributionMap();
        for (Map.Entry<ComponentState, StateTransitionTracker.StateContributionInfo> activeEntry : activeStates
                .entrySet())
        {
            ComponentState activeState = activeEntry.getKey();
            if (!activeState.isActive())
                continue;

            float contribution = activeEntry.getValue().getContribution();
            if (contribution == 0.0f)
                continue;

            graphics.setComposite(LafWidgetUtilities.getAlphaComposite(slider, contribution, g));

            SubstanceColorScheme activeFillScheme = SubstanceColorSchemeUtilities.getColorScheme(this.slider,
                    activeState);
            SubstanceColorScheme activeBorderScheme = SubstanceColorSchemeUtilities.getColorScheme(this.slider,
                    ColorSchemeAssociationKind.BORDER, activeState);
            paintSliderTrackSelected(graphics, paintRect, activeFillScheme, activeBorderScheme, width, height);
        }

        graphics.dispose();
    }

    /**
     * Paints the slider track.
     * 
     * @param graphics
     *        Graphics.
     * @param drawInverted
     *        Indicates whether the value-range shown for the slider is
     *        reversed.
     * @param fillColorScheme
     *        Fill color scheme.
     * @param borderScheme
     *        Border color scheme.
     * @param width
     *        Track width.
     * @param height
     *        Track height.
     */
    private void paintSliderTrack(Graphics2D graphics, boolean drawInverted, SubstanceColorScheme fillColorScheme,
            SubstanceColorScheme borderScheme, int width, int height)
    {
        Graphics2D g2d = (Graphics2D) graphics.create();

        SubstanceFillPainter fillPainter = ClassicFillPainter.INSTANCE;
        SubstanceBorderPainter borderPainter = SubstanceCoreUtilities.getBorderPainter(this.slider);

        int componentFontSize = SubstanceSizeUtils.getComponentFontSize(this.slider);
        int borderDelta = (int) Math.floor(SubstanceSizeUtils.getBorderStrokeWidth(componentFontSize) / 2.0);
        float radius = SubstanceSizeUtils.getClassicButtonCornerRadius(componentFontSize) / 2.0f;
        int borderThickness = (int) SubstanceSizeUtils.getBorderStrokeWidth(componentFontSize);

        HashMapKey key = SubstanceCoreUtilities.getHashKey(width, height, radius, borderDelta, borderThickness,
                fillColorScheme.getDisplayName(), borderScheme.getDisplayName());

        BufferedImage trackImage = trackCache.get(key);
        if (trackImage == null)
        {
            trackImage = SubstanceCoreUtilities.getBlankImage(width + 1, height + 1);
            Graphics2D cacheGraphics = trackImage.createGraphics();

            Shape contour = SubstanceOutlineUtilities.getBaseOutline(width + 1, height + 1, radius, null, borderDelta);

            fillPainter.paintContourBackground(cacheGraphics, slider, width, height, contour, false, fillColorScheme,
                    false);

            GeneralPath contourInner = SubstanceOutlineUtilities.getBaseOutline(width + 1, height + 1, radius
                    - borderThickness, null, borderThickness + borderDelta);
            borderPainter
                    .paintBorder(cacheGraphics, slider, width + 1, height + 1, contour, contourInner, borderScheme);

            trackCache.put(key, trackImage);
            cacheGraphics.dispose();
        }

        g2d.drawImage(trackImage, 0, 0, null);

        g2d.dispose();
    }

    /**
     * Paints the selected part of the slider track.
     * 
     * @param graphics
     *        Graphics.
     * @param drawInverted
     *        Indicates whether the value-range shown for the slider is
     *        reversed.
     * @param paintRect
     *        Selected portion.
     * @param fillScheme
     *        Fill color scheme.
     * @param borderScheme
     *        Border color scheme.
     * @param width
     *        Track width.
     * @param height
     *        Track height.
     */
    private void paintSliderTrackSelected(Graphics2D graphics, Rectangle paintRect, SubstanceColorScheme fillScheme,
            SubstanceColorScheme borderScheme, int width, int height)
    {
        Graphics2D g2d = (Graphics2D) graphics.create();
        Insets insets = this.slider.getInsets();
        insets.top /= 2;
        insets.left /= 2;
        insets.bottom /= 2;
        insets.right /= 2;

        SubstanceFillPainter fillPainter = SubstanceCoreUtilities.getFillPainter(this.slider);
        SubstanceBorderPainter borderPainter = SubstanceCoreUtilities.getBorderPainter(this.slider);
        float radius = SubstanceSizeUtils.getClassicButtonCornerRadius(SubstanceSizeUtils.getComponentFontSize(slider)) / 2.0f;
        int borderDelta = (int) Math.floor(SubstanceSizeUtils.getBorderStrokeWidth(SubstanceSizeUtils
                .getComponentFontSize(slider)) / 2.0);

        // fill selected portion
        if (this.slider.isEnabled())
        {
            if (this.slider.getOrientation() == SwingConstants.HORIZONTAL)
            {
                int ltPos = thumbRect.x + (this.thumbRect.width / 2) - paintRect.x;
                int utPos = upperThumbRect.x + (this.upperThumbRect.width / 2) - paintRect.x;

                int fillMinX;
                int fillMaxX;

                if (ltPos < utPos)
                {
                    fillMinX = ltPos;
                    fillMaxX = utPos;
                }
                else
                {
                    fillMinX = utPos;
                    fillMaxX = ltPos;
                }

                int fillWidth = fillMaxX - fillMinX;
                int fillHeight = height + 1;
                if ((fillWidth > 0) && (fillHeight > 0))
                {
                    Shape contour = SubstanceOutlineUtilities.getBaseOutline(fillWidth, fillHeight, radius, null,
                            borderDelta);
                    g2d.translate(fillMinX, 0);
                    fillPainter.paintContourBackground(g2d, this.slider, fillWidth, fillHeight, contour, false,
                            fillScheme, false);
                    borderPainter.paintBorder(g2d, this.slider, fillWidth, fillHeight, contour, null, borderScheme);
                }
            }
            else
            {
                int ltPos = thumbRect.y + (this.thumbRect.height / 2) - paintRect.y;
                int utPos = upperThumbRect.y + (this.upperThumbRect.height / 2) - paintRect.y;
                int fillMin;
                int fillMax;

                if (ltPos < utPos)
                {
                    fillMin = ltPos;
                    fillMax = utPos;
                }
                else
                {
                    fillMin = utPos;
                    fillMax = ltPos;
                }

                // if (this.drawInverted())
                // {
                // fillMin = 0;
                // fillMax = middleOfThumb;
                // // fix for issue 368 - inverted vertical sliders
                // g2d.translate(width + 2 - middleOfThumb, 0);
                // }
                // else
                // {
                // fillMin = middleOfThumb;
                // fillMax = width + 1;
                // }

                int fillWidth = fillMax - fillMin;
                int fillHeight = height + 1;
                if ((fillWidth > 0) && (fillHeight > 0))
                {
                    Shape contour = SubstanceOutlineUtilities.getBaseOutline(fillWidth, fillHeight, radius, null,
                            borderDelta);
                    g2d.translate(paintRect.height - fillMax, 0);
                    fillPainter.paintContourBackground(g2d, this.slider, fillWidth, fillHeight, contour, false,
                            fillScheme, false);
                    borderPainter.paintBorder(g2d, this.slider, fillWidth, fillHeight, contour, null, borderScheme);
                }
            }
        }
        g2d.dispose();
    }

    /**
     * Sets the location of the upper thumb, and repaints the slider. This is
     * called when the upper thumb is dragged to repaint the slider. The
     * <code>setThumbLocation()</code> method performs the same task for the
     * lower thumb.
     */
    void setUpperThumbLocation(int x, int y)
    {
        upperThumbRect.setLocation(x, y);
        slider.repaint();
    }

    /**
     * Moves the selected thumb in the specified direction by a block increment.
     * This method is called when the user presses the Page Up or Down keys.
     */
    @Override
    public void scrollByBlock(int direction)
    {
        synchronized (slider)
        {
            int blockIncrement = (slider.getMaximum() - slider.getMinimum()) / 10;
            if (blockIncrement <= 0 && slider.getMaximum() > slider.getMinimum())
            {
                blockIncrement = 1;
            }
            int delta = blockIncrement * ((direction > 0) ? POSITIVE_SCROLL : NEGATIVE_SCROLL);

            if (upperThumbSelected)
            {
                int oldValue = ((RangeSlider) slider).getUpperValue();
                ((RangeSlider) slider).setUpperValue(oldValue + delta);
            }
            else
            {
                int oldValue = slider.getValue();
                slider.setValue(oldValue + delta);
            }
        }
    }

    /**
     * Moves the selected thumb in the specified direction by a unit increment.
     * This method is called when the user presses one of the arrow keys.
     */
    @Override
    public void scrollByUnit(int direction)
    {
        synchronized (slider)
        {
            int delta = 1 * ((direction > 0) ? POSITIVE_SCROLL : NEGATIVE_SCROLL);

            if (upperThumbSelected)
            {
                int oldValue = ((RangeSlider) slider).getUpperValue();
                ((RangeSlider) slider).setUpperValue(oldValue + delta);
            }
            else
            {
                int oldValue = slider.getValue();
                slider.setValue(oldValue + delta);
            }
        }
    }

    /**
     * Listener to handle model change events. This calculates the thumb
     * locations and repaints the slider if the value change is not caused by
     * dragging a thumb.
     */
    public class ChangeHandler implements ChangeListener
    {
        @Override
        public void stateChanged(ChangeEvent arg0)
        {
            if (!lowerDragging && !upperDragging)
            {
                calculateThumbLocation();
                slider.repaint();
            }
        }
    }

    /**
     * Listener to handle mouse movements in the slider track.
     */
    public class RangeTrackListener extends TrackListener
    {
        @Override
        public void mousePressed(MouseEvent e)
        {
            if (!slider.isEnabled())
                return;

            currentMouseX = e.getX();
            currentMouseY = e.getY();

            if (slider.isRequestFocusEnabled())
                slider.requestFocus();

            // Determine which thumb is pressed. If the upper thumb is
            // selected (last one dragged), then check its position first;
            // otherwise check the position of the lower thumb first.
            boolean lowerPressed = false;
            boolean upperPressed = false;
            if (isInsideLowerThumb(currentMouseX, currentMouseY))
                lowerPressed = true;
            else if (isInsideUpperThumb(currentMouseX, currentMouseY))
                upperPressed = true;

            // Handle lower thumb pressed.
            if (lowerPressed)
            {
                switch (slider.getOrientation())
                {
                    case SwingConstants.VERTICAL:
                        offset = currentMouseY - thumbRect.y;
                        break;
                    case SwingConstants.HORIZONTAL:
                        offset = currentMouseX - thumbRect.x;
                        break;
                }
                upperThumbSelected = false;
                lowerDragging = true;
                return;
            }
            lowerDragging = false;

            // Handle upper thumb pressed.
            if (upperPressed)
            {
                switch (slider.getOrientation())
                {
                    case SwingConstants.VERTICAL:
                        offset = currentMouseY - upperThumbRect.y;
                        break;
                    case SwingConstants.HORIZONTAL:
                        offset = currentMouseX - upperThumbRect.x;
                        break;
                }
                upperThumbSelected = true;
                upperDragging = true;
                return;
            }
            upperDragging = false;
        }

        @Override
        public void mouseReleased(MouseEvent e)
        {
            lowerDragging = false;
            upperDragging = false;
            slider.setValueIsAdjusting(false);
            super.mouseReleased(e);
        }

        @Override
        public void mouseDragged(MouseEvent e)
        {
            if (!slider.isEnabled())
            {
                return;
            }

            currentMouseX = e.getX();
            currentMouseY = e.getY();

            if (lowerDragging)
            {
                slider.setValueIsAdjusting(true);
                moveLowerThumb();

            }
            else if (upperDragging)
            {
                slider.setValueIsAdjusting(true);
                moveUpperThumb();
            }
        }

        @Override
        public boolean shouldScroll(int direction)
        {
            return false;
        }

        /**
         * Moves the location of the lower thumb, and sets its corresponding
         * value in the slider.
         */
        private void moveLowerThumb()
        {
            int thumbMiddle = 0;

            switch (slider.getOrientation())
            {
                case SwingConstants.VERTICAL:
                    int halfThumbHeight = thumbRect.height / 2;
                    int thumbTop = currentMouseY - offset;
                    int trackTop = trackRect.y;
                    int trackBottom = trackRect.y + (trackRect.height - 1);
                    int vMax = yPositionForValue(slider.getValue() + slider.getExtent());

                    // Apply bounds to thumb position.
                    if (drawInverted())
                    {
                        trackBottom = vMax;
                    }
                    else
                    {
                        trackTop = vMax;
                    }
                    thumbTop = Math.max(thumbTop, trackTop - halfThumbHeight);
                    thumbTop = Math.min(thumbTop, trackBottom - halfThumbHeight);

                    setThumbLocation(thumbRect.x, thumbTop);

                    // Update slider value.
                    thumbMiddle = thumbTop + halfThumbHeight;
                    slider.setValue(valueForYPosition(thumbMiddle));
                    break;

                case SwingConstants.HORIZONTAL:
                    int halfThumbWidth = thumbRect.width / 2;
                    int thumbLeft = currentMouseX - offset;
                    int trackLeft = trackRect.x;
                    int trackRight = trackRect.x + (trackRect.width - 1);
                    int hMax = xPositionForValue(slider.getValue() + slider.getExtent());

                    // Apply bounds to thumb position.
                    if (drawInverted())
                    {
                        trackLeft = hMax;
                    }
                    else
                    {
                        trackRight = hMax;
                    }
                    thumbLeft = Math.max(thumbLeft, trackLeft - halfThumbWidth);
                    thumbLeft = Math.min(thumbLeft, trackRight - halfThumbWidth);

                    setThumbLocation(thumbLeft, thumbRect.y);

                    // Update slider value.
                    thumbMiddle = thumbLeft + halfThumbWidth;
                    slider.setValue(valueForXPosition(thumbMiddle));
                    break;

                default:
                    return;
            }
        }

        /**
         * Moves the location of the upper thumb, and sets its corresponding
         * value in the slider.
         */
        private void moveUpperThumb()
        {
            int thumbMiddle = 0;

            switch (slider.getOrientation())
            {
                case SwingConstants.VERTICAL:
                    int halfThumbHeight = thumbRect.height / 2;
                    int thumbTop = currentMouseY - offset;
                    int trackTop = trackRect.y;
                    int trackBottom = trackRect.y + (trackRect.height - 1);
                    int vMin = yPositionForValue(slider.getValue());

                    // Apply bounds to thumb position.
                    if (drawInverted())
                    {
                        trackTop = vMin;
                    }
                    else
                    {
                        trackBottom = vMin;
                    }
                    thumbTop = Math.max(thumbTop, trackTop - halfThumbHeight);
                    thumbTop = Math.min(thumbTop, trackBottom - halfThumbHeight);

                    setUpperThumbLocation(thumbRect.x, thumbTop);

                    // Update slider extent.
                    thumbMiddle = thumbTop + halfThumbHeight;
                    slider.setExtent(valueForYPosition(thumbMiddle) - slider.getValue());
                    break;

                case SwingConstants.HORIZONTAL:
                    int halfThumbWidth = thumbRect.width / 2;
                    int thumbLeft = currentMouseX - offset;
                    int trackLeft = trackRect.x;
                    int trackRight = trackRect.x + (trackRect.width - 1);
                    int hMin = xPositionForValue(slider.getValue());

                    // Apply bounds to thumb position.
                    if (drawInverted())
                    {
                        trackRight = hMin;
                    }
                    else
                    {
                        trackLeft = hMin;
                    }
                    thumbLeft = Math.max(thumbLeft, trackLeft - halfThumbWidth);
                    thumbLeft = Math.min(thumbLeft, trackRight - halfThumbWidth);

                    setUpperThumbLocation(thumbLeft, thumbRect.y);

                    // Update slider extent.
                    thumbMiddle = thumbLeft + halfThumbWidth;
                    slider.setExtent(valueForXPosition(thumbMiddle) - slider.getValue());
                    break;

                default:
                    return;
            }
        }
    }
}
