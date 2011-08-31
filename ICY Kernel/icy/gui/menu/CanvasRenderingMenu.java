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
package icy.gui.menu;

import icy.canvas.IcyCanvas;
import icy.gui.frame.progress.ProgressFrame;
import icy.gui.viewer.Viewer;
import icy.main.Icy;
import icy.sequence.Sequence;
import icy.system.thread.ThreadUtil;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

/**
 * @author Stephane
 */
public class CanvasRenderingMenu extends JMenu
{
    enum ColorType
    {
        ARGB, RGB, GRAY
    };

    private class LUTRenderMenuItem extends JMenuItem
    {
        /**
         * 
         */
        private static final long serialVersionUID = -1478044600537048870L;

        public LUTRenderMenuItem(final ColorType colorType)
        {
            super();

            final String text;

            switch (colorType)
            {
                default:
                    text = "ARGB image";
                    break;

                case RGB:
                    text = "RGB image";
                    break;

                case GRAY:
                    text = "Gray image";
                    break;
            }

            setText(text);

            addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    // so it won't change during process
                    final IcyCanvas canvas = viewer.getCanvas();
                    final Sequence seqIn = viewer.getSequence();

                    if ((seqIn != null) && (canvas != null))
                    {
                        // launch in background as it can take sometime
                        ThreadUtil.bgRun(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                final ProgressFrame pf = new ProgressFrame("Rendering...");
                                try
                                {
                                    final Sequence seqOut = canvas.getRenderedSequence(colorTypeToImageType(colorType),
                                            canvasView);

                                    if (seqOut != null)
                                    {
                                        // set sequence name
                                        seqOut.setName("LUT rendering of " + seqIn.getName());
                                        // add sequence
                                        Icy.addSequence(seqOut);
                                    }
                                }
                                finally
                                {
                                    pf.close();
                                }
                            }
                        });
                    }
                }
            });
        }

        int colorTypeToImageType(ColorType value)
        {
            switch (value)
            {
                default:
                    return BufferedImage.TYPE_INT_ARGB;

                case RGB:
                    return BufferedImage.TYPE_INT_RGB;

                case GRAY:
                    return BufferedImage.TYPE_BYTE_GRAY;
            }
        }
    }

    /**
     * 
     */
    private static final long serialVersionUID = -6947809364093890964L;

    boolean canvasView;
    boolean displayCanvasViewItem;
    final Viewer viewer;

    private CanvasRenderingMenu(Viewer viewer, boolean canvasView, boolean displayCanvasViewItem)
    {
        super("Render as");

        this.viewer = viewer;
        this.canvasView = canvasView;
        this.displayCanvasViewItem = displayCanvasViewItem;

        build();
    }

    public CanvasRenderingMenu(Viewer viewer, boolean canvasView)
    {
        this(viewer, canvasView, false);
    }

    public CanvasRenderingMenu(Viewer viewer)
    {
        this(viewer, false, true);
    }

    private void build()
    {
        final Sequence sequence = viewer.getSequence();

        removeAll();

        if (sequence != null)
        {
            add(new LUTRenderMenuItem(ColorType.ARGB));
            add(new LUTRenderMenuItem(ColorType.RGB));
            add(new LUTRenderMenuItem(ColorType.GRAY));

            if (displayCanvasViewItem)
            {
                final JCheckBoxMenuItem canvasItem = new JCheckBoxMenuItem("canvas view");
                canvasItem.setSelected(canvasView);
                canvasItem.addActionListener(new ActionListener()
                {
                    @Override
                    public void actionPerformed(ActionEvent e)
                    {
                        canvasView = canvasItem.isSelected();
                    }
                });

                addSeparator();
                add(canvasItem);
            }

            setEnabled(true);
        }
        else
            setEnabled(false);
    }
}
