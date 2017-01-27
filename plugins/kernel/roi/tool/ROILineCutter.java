package plugins.kernel.roi.tool;

import icy.canvas.IcyCanvas;
import icy.roi.ROI;
import icy.roi.ROIUtil;
import icy.sequence.Sequence;
import icy.sequence.edit.ROIReplacesSequenceEdit;
import icy.system.thread.ThreadUtil;
import icy.type.point.Point5D;
import icy.type.point.Point5D.Double;
import icy.type.rectangle.Rectangle2DUtil;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.List;

import plugins.kernel.roi.roi2d.ROI2DLine;

/**
 * ROI Helper class for ROI cutting action
 * 
 * @author Stephane
 */
public class ROILineCutter extends ROI2DLine
{
    public class ROILineCutterPainter extends ROI2DLinePainter
    {
        @Override
        public void mouseReleased(MouseEvent e, Double imagePoint, IcyCanvas canvas)
        {
            super.mouseReleased(e, imagePoint, canvas);

            // do that in background as it can take sometime
            ThreadUtil.bgRun(new Runnable()
            {
                @Override
                public void run()
                {
                    // get sequences where we are attached first
                    final List<Sequence> sequences = getSequences();

                    // remove the ROI, we don't need it anymore...
                    ROILineCutter.this.remove(false);

                    // and do cutting now
                    splitOverlappedROIs(sequences);
                }
            });
        }

        @Override
        protected void drawShape(Graphics2D g, Sequence sequence, IcyCanvas canvas, boolean simplified)
        {
            final Line2D extendedLine = getExtendedLine(sequence);

            if (extendedLine != null)
            {
                final Graphics2D g2 = (Graphics2D) g.create();

                // draw extended line
                g2.setStroke(new BasicStroke((float) (ROI.getAdjustedStroke(canvas, stroke) / 2f)));
                g2.setColor(getDisplayColor());
                g2.draw(extendedLine);

                g2.dispose();
            }

            super.drawShape(g, sequence, canvas, getLine(), simplified);
        }
    }

    public ROILineCutter(Point5D pt)
    {
        super(pt);
    }

    public ROILineCutter()
    {
        super();
    }

    @Override
    protected ROI2DShapePainter createPainter()
    {
        return new ROILineCutterPainter();
    }

    protected Line2D getExtendedLine(Sequence sequence)
    {
        return Rectangle2DUtil.getIntersectionLine(sequence.getBounds2D(), getLine());
    }

    /**
     * This is a special function of this ROI, it cuts all overlapped ROI from given Sequences based on the current ROI
     * shape (line).
     * 
     * @return <code>true</code> if some ROIS were cuts
     */
    public boolean splitOverlappedROIs(List<Sequence> sequences)
    {
        boolean result = false;

        for (Sequence sequence : sequences)
        {
            final List<ROI> removedROI = new ArrayList<ROI>();
            final List<ROI> addedROI = new ArrayList<ROI>();

            sequence.beginUpdate();
            try
            {
                for (ROI roi : sequence.getROIs())
                {
                    final List<ROI> resultRois = ROIUtil.split(roi, getLine());

                    // ROI was cut ?
                    if (resultRois != null)
                    {
                        removedROI.add(roi);
                        addedROI.addAll(resultRois);
                        result = true;
                    }
                }

                if (!removedROI.isEmpty())
                {
                    sequence.removeROIs(removedROI, false);
                    sequence.addROIs(addedROI, false);

                    // add undo operation
                    sequence.addUndoableEdit(new ROIReplacesSequenceEdit(sequence, removedROI, addedROI));
                }
            }
            finally
            {
                sequence.endUpdate();
            }
        }

        return result;
    }
}
