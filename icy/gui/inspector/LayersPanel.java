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
package icy.gui.inspector;

import icy.canvas.IcyCanvas;
import icy.canvas.Layer;
import icy.canvas.Layer.LayerListener;
import icy.canvas.LayersEvent;
import icy.canvas.LayersEvent.LayersEventType;
import icy.canvas.LayersListener;
import icy.gui.component.ComponentUtil;
import icy.gui.component.button.IcyButton;
import icy.gui.component.button.IcyToggleButton;
import icy.gui.main.MainAdapter;
import icy.gui.main.MainEvent;
import icy.gui.main.MainListener;
import icy.gui.viewer.Viewer;
import icy.gui.viewer.ViewerEvent;
import icy.gui.viewer.ViewerEvent.ViewerEventType;
import icy.gui.viewer.ViewerListener;
import icy.main.Icy;
import icy.resource.ResourceUtil;
import icy.resource.icon.IcyIcon;
import icy.roi.ROI;
import icy.sequence.Sequence;
import icy.system.thread.ThreadUtil;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.JToggleButton;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * @author Stephane
 */
public class LayersPanel extends JPanel implements ViewerListener, LayersListener
{
    static Image IMAGE_VISIBLE = ResourceUtil.getAlphaIconAsImage("eye_open.png", 18);
    static Image IMAGE_NOT_VISIBLE = ResourceUtil.getAlphaIconAsImage("eye_close.png", 18);
    static Image IMAGE_DELETE = ResourceUtil.getAlphaIconAsImage("delete.png", 18);

    private class LayerComponent extends JPanel implements LayerListener
    {
        /**
         * 
         */
        private static final long serialVersionUID = 8590616637272300324L;

        final Layer layer;

        /**
         * gui
         */
        JLabel label;
        JSlider alphaSlider;
        IcyToggleButton visibleButton;

        /**
         * 
         */
        public LayerComponent(Layer l)
        {
            super(true);

            this.layer = l;

            label = new JLabel();
            label.addMouseListener(new MouseAdapter()
            {
                @Override
                public void mouseClicked(MouseEvent e)
                {
                    final ROI attachedRoi = layer.getAttachedROI();

                    // select attached ROI
                    if (attachedRoi != null)
                        attachedRoi.setSelected(true, true);

                    // bring to front to first found viewer containing this painter
                    final Viewer viewer = Icy.getMainInterface().getFirstViewerContaining(layer.getPainter());
                    if (viewer != null)
                        viewer.toFront();
                }
            });

            alphaSlider = new JSlider(0, 100);
            ComponentUtil.setFixedWidth(alphaSlider, 80);
            alphaSlider.setToolTipText("Set transparency level");
            alphaSlider.addChangeListener(new ChangeListener()
            {
                @Override
                public void stateChanged(ChangeEvent e)
                {
                    layer.setAlpha(alphaSlider.getValue() / 100f);
                }
            });

            final IcyButton deleteButton = new IcyButton(IMAGE_DELETE);
            deleteButton.setFlat(true);
            if (layer.isAttachedToRoi())
                deleteButton.setEnabled(false);
            deleteButton.setToolTipText("Delete layer");
            // ComponentUtil.setFixedSize(deleteButton, new Dimension(18, 18));
            deleteButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    final Sequence seq = getSequence();

                    // remove layer (painter)
                    if (seq != null)
                        seq.removePainter(layer.getPainter());
                }
            });

            visibleButton = new IcyToggleButton(IMAGE_VISIBLE);
            visibleButton.setFlat(true);
            visibleButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    layer.setVisible(visibleButton.isSelected());
                    updateVisibleButton(visibleButton);
                }
            });

            final JPanel rightPanel = new JPanel();
            rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.LINE_AXIS));
            // ComponentUtil.setFixedHeight(topPanel, 20);

            rightPanel.add(alphaSlider);
            rightPanel.add(deleteButton);
            rightPanel.add(Box.createHorizontalStrut(2));
            rightPanel.add(visibleButton);
            rightPanel.add(Box.createHorizontalStrut(2));

            // setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
            // setBorder(BorderFactory.createEtchedBorder());

            setLayout(new BorderLayout());

            add(label, BorderLayout.CENTER);
            add(rightPanel, BorderLayout.EAST);
            add(new JSeparator(SwingConstants.HORIZONTAL), BorderLayout.SOUTH);

            refresh();
        }

        @Override
        public void addNotify()
        {
            super.addNotify();

            layer.addListener(this);
        }

        @Override
        public void removeNotify()
        {
            layer.removeListener(this);

            super.removeNotify();
        }

        void refresh()
        {
            label.setText(layer.getName());
            label.setToolTipText(layer.getName());
            alphaSlider.setValue((int) (layer.getAlpha() * 100d));
            visibleButton.setSelected(layer.isVisible());
            updateVisibleButton(visibleButton);

            validate();
        }

        // void updateDetailButton(JToggleButton btn)
        // {
        // if (btn.isSelected())
        // {
        // btn.setIcon(ICON_MINUS);
        // btn.setToolTipText("hide detail");
        // }
        // else
        // {
        // btn.setIcon(ICON_PLUS);
        // btn.setToolTipText("show detail");
        // }
        // }

        void updateVisibleButton(JToggleButton btn)
        {
            if (btn.isSelected())
            {
                btn.setIcon(new IcyIcon(IMAGE_VISIBLE));
                btn.setToolTipText("Hide layer");
            }
            else
            {
                btn.setIcon(new IcyIcon(IMAGE_NOT_VISIBLE));
                btn.setToolTipText("Show layer");
            }
        }

        // void setShowDetail(boolean value)
        // {
        // if (showDetail != value)
        // {
        // showDetail = value;
        // refresh();
        // }
        // }

        /**
         * @return the layer
         */
        public Layer getLayer()
        {
            return layer;
        }

        @Override
        public void layerChanged(Layer layer)
        {
            refresh();
        }
    }

    /**
     * 
     */
    private static final long serialVersionUID = 5016943076350616223L;

    private Viewer viewer;

    private final JPanel layersPanel;
    private final MainListener mainListener;

    /**
     * 
     */
    public LayersPanel()
    {
        super(true);

        viewer = null;

        mainListener = new MainAdapter()
        {
            @Override
            public void viewerFocused(MainEvent event)
            {
                final Viewer viewer = (Viewer) event.getSource();

                ThreadUtil.invokeLater(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        // selected viewer changed
                        setViewer(viewer);
                    }
                });
            }
        };

        layersPanel = new JPanel(true);
        layersPanel.setLayout(new BoxLayout(layersPanel, BoxLayout.PAGE_AXIS));

        final JPanel bottomPanel = new JPanel(true);
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.PAGE_AXIS));

        final JPanel localActionPanel = new JPanel(true);
        localActionPanel.setLayout(new BoxLayout(localActionPanel, BoxLayout.LINE_AXIS));

        // final JButton loadBtn = new JButton("load...");
        // final JButton saveBtn = new JButton("save...");
        // final JButton detachBtn = new JButton("detach");
        //
        // localActionPanel.add(loadBtn);
        // localActionPanel.add(saveBtn);
        // localActionPanel.add(detachBtn);
        //
        // final JPanel destActionPanel = new JPanel(true);
        // destActionPanel.setLayout(new BoxLayout(destActionPanel, BoxLayout.PAGE_AXIS));
        //
        // final JPanel destBtnActionPanel = new JPanel(true);
        // destBtnActionPanel.setLayout(new BoxLayout(destBtnActionPanel, BoxLayout.LINE_AXIS));
        //
        // final JButton moveBtn = new JButton("move");
        // moveBtn.addActionListener(new ActionListener()
        // {
        // @Override
        // public void actionPerformed(ActionEvent e)
        // {
        // // TODO Auto-generated method stub
        //
        // }
        // });
        //
        // final JButton attachBtn = new JButton("attach");
        // final JButton copyBtn = new JButton("copy");
        //
        // destBtnActionPanel.add(moveBtn);
        // destBtnActionPanel.add(attachBtn);
        // destBtnActionPanel.add(copyBtn);
        //
        // destActionPanel.add(destBtnActionPanel);
        //
        // bottomPanel.add(localActionPanel);
        // bottomPanel.add(destActionPanel);

        final JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());

        mainPanel.add(layersPanel, BorderLayout.NORTH);
        mainPanel.add(Box.createGlue(), BorderLayout.CENTER);

        final JScrollPane sc = new JScrollPane(mainPanel, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        setLayout(new BorderLayout());

        add(sc, BorderLayout.CENTER);
        // add(bottomPanel, BorderLayout.SOUTH);

        rebuildLayersPanel();

        validate();
        setVisible(true);
    }

    @Override
    public void addNotify()
    {
        super.addNotify();

        Icy.getMainInterface().addListener(mainListener);
        if (viewer != null)
        {
            viewer.addListener(this);
            final IcyCanvas canvas = viewer.getCanvas();
            // canvas can be null if viewer has just been closed
            if (canvas != null)
                canvas.addLayersListener(this);
        }
    }

    @Override
    public void removeNotify()
    {
        Icy.getMainInterface().removeListener(mainListener);
        if (viewer != null)
        {
            viewer.removeListener(this);
            final IcyCanvas canvas = viewer.getCanvas();
            // canvas can be null if viewer has just been closed
            if (canvas != null)
                canvas.removeLayersListener(this);
        }

        super.removeNotify();
    }

    void setViewer(Viewer v)
    {
        if (viewer != v)
        {
            if (viewer != null)
            {
                viewer.removeListener(this);

                final IcyCanvas canvas = viewer.getCanvas();
                // canvas can be null if viewer has just been closed
                if (canvas != null)
                    canvas.removeLayersListener(this);
            }

            viewer = v;

            if (v != null)
            {
                v.addListener(this);

                final IcyCanvas canvas = viewer.getCanvas();
                // canvas can be null if viewer has just been closed
                if (canvas != null)
                    canvas.addLayersListener(this);
            }

            rebuildLayersPanel();
        }
    }

    IcyCanvas getCanvas()
    {
        if (viewer != null)
            return viewer.getCanvas();

        return null;
    }

    Sequence getSequence()
    {
        if (viewer != null)
            return viewer.getSequence();

        return null;
    }

    void rebuildLayersPanel()
    {
        layersPanel.removeAll();

        final ArrayList<Layer> layers;

        if ((viewer != null) && (viewer.getCanvas() != null))
            layers = viewer.getCanvas().getLayers();
        else
            layers = new ArrayList<Layer>();

        for (Layer layer : layers)
            layersPanel.add(new LayerComponent(layer));

        layersPanel.validate();
        // as we use a scroll pane in tab, not nice...
        // layersPanel.getParent().validate();
        // layersPanel.getParent().repaint();
    }

    private LayerComponent getLayerComponent(Layer layer)
    {
        for (Component comp : layersPanel.getComponents())
        {
            if (comp instanceof LayerComponent)
            {
                final LayerComponent layerComponent = (LayerComponent) comp;

                if (layerComponent.getLayer() == layer)
                    return layerComponent;
            }
        }

        return null;
    }

    @Override
    public void viewerChanged(ViewerEvent event)
    {
        // canvas changed --> rebuild layers panel
        if (event.getType() == ViewerEventType.CANVAS_CHANGED)
        {
            final IcyCanvas canvas = viewer.getCanvas();
            // canvas can be null if viewer has just been closed
            if (canvas != null)
                canvas.addLayersListener(this);
            rebuildLayersPanel();
        }
    }

    @Override
    public void viewerClosed(Viewer viewer)
    {
        // handled with mainListener
    }

    @Override
    public void layersChanged(LayersEvent event)
    {
        final Layer layer = event.getSource();
        final LayersEventType type = event.getType();

        ThreadUtil.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                switch (type)
                {
                    case ADDED:
                    case REMOVED:
                        rebuildLayersPanel();
                        break;

                    case CHANGED:
                        // multiple changes
                        if (layer == null)
                            rebuildLayersPanel();
                        break;
                }
            }
        });
    }
}
