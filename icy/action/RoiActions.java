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
package icy.action;

import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.w3c.dom.Document;

import icy.clipboard.Clipboard;
import icy.file.FileUtil;
import icy.gui.dialog.IdConfirmDialog;
import icy.gui.dialog.MessageDialog;
import icy.gui.dialog.OpenDialog;
import icy.gui.dialog.SaveDialog;
import icy.gui.inspector.RoisPanel;
import icy.gui.main.MainFrame;
import icy.main.Icy;
import icy.resource.ResourceUtil;
import icy.resource.icon.IcyIcon;
import icy.roi.ROI;
import icy.roi.ROI2D;
import icy.roi.ROI3D;
import icy.roi.ROI4D;
import icy.roi.ROIUtil;
import icy.sequence.Sequence;
import icy.sequence.SequenceDataIterator;
import icy.sequence.edit.ROIAddSequenceEdit;
import icy.sequence.edit.ROIAddsSequenceEdit;
import icy.sequence.edit.ROIReplacesSequenceEdit;
import icy.system.SystemUtil;
import icy.type.DataIteratorUtil;
import icy.util.ClassUtil;
import icy.util.ShapeUtil.BooleanOperator;
import icy.util.StringUtil;
import icy.util.XLSUtil;
import icy.util.XMLUtil;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import plugins.kernel.roi.roi2d.ROI2DRectangle;
import plugins.kernel.roi.roi3d.ROI3DStackRectangle;
import plugins.kernel.roi.roi4d.ROI4DStackRectangle;
import plugins.kernel.roi.roi5d.ROI5DStackRectangle;

/**
 * Roi actions (open / save / copy / paste / merge...)
 * 
 * @author Stephane
 */
public class RoiActions
{
    public static final String DEFAULT_ROI_DIR = "roi";
    public static final String DEFAULT_ROI_NAME = "roi.xml";

    public static class SequenceRoiList
    {
        public final Sequence sequence;
        public final List<ROI> rois;

        public SequenceRoiList(Sequence sequence, List<ROI> rois)
        {
            super();

            this.sequence = sequence;
            this.rois = rois;
        }
    }

    public static IcyAbstractAction loadAction = new IcyAbstractAction("Load ROI(s)",
            new IcyIcon(ResourceUtil.ICON_OPEN), "Load ROI(s) from file",
            "Load ROI(s) from a XML file and add them to the active sequence")
    {
        /**
         * 
         */
        private static final long serialVersionUID = 2378084039864016238L;

        @Override
        public boolean doAction(ActionEvent e)
        {
            final String filename = OpenDialog.chooseFile("Load roi(s)...", DEFAULT_ROI_DIR, DEFAULT_ROI_NAME);
            final Sequence sequence = Icy.getMainInterface().getActiveSequence();

            if ((filename != null) && (sequence != null))
            {
                final Document doc = XMLUtil.loadDocument(filename);

                if (doc != null)
                {
                    final List<ROI> rois = ROI.loadROIsFromXML(XMLUtil.getRootElement(doc));

                    sequence.beginUpdate();
                    try
                    {
                        // add to sequence
                        for (ROI roi : rois)
                            sequence.addROI(roi);
                    }
                    finally
                    {
                        sequence.endUpdate();
                    }

                    // add to undo manager
                    sequence.addUndoableEdit(new ROIAddsSequenceEdit(sequence, rois)
                    {
                        @Override
                        public String getPresentationName()
                        {
                            if (getROIs().size() > 1)
                                return "ROIs loaded from XML file";

                            return "ROI loaded from XML file";
                        };
                    });

                    return true;
                }
            }

            return false;
        }

        @Override
        public boolean isEnabled()
        {
            return super.isEnabled() && (Icy.getMainInterface().getActiveSequence() != null);
        }
    };

    public static IcyAbstractAction saveAction = new IcyAbstractAction("Save ROI(s)",
            new IcyIcon(ResourceUtil.ICON_SAVE), "Save selected ROI(s) to file",
            "Save the selected ROI(s) from active sequence into a XML file")
    {
        /**
         * 
         */
        private static final long serialVersionUID = 349358870716619748L;

        @Override
        public boolean doAction(ActionEvent e)
        {
            final String filename = SaveDialog.chooseFile("Save roi(s)...", DEFAULT_ROI_DIR, DEFAULT_ROI_NAME);
            final Sequence sequence = Icy.getMainInterface().getActiveSequence();

            if ((filename != null) && (sequence != null))
            {
                final List<ROI> rois = sequence.getSelectedROIs();

                if (rois.size() > 0)
                {
                    final Document doc = XMLUtil.createDocument(true);

                    if (doc != null)
                    {
                        ROI.saveROIsToXML(XMLUtil.getRootElement(doc), rois);
                        XMLUtil.saveDocument(doc, filename);
                        return true;
                    }
                }
            }

            return false;
        }

        @Override
        public boolean isEnabled()
        {
            return super.isEnabled() && (Icy.getMainInterface().getActiveSequence() != null);
        }
    };

    public static IcyAbstractAction copyAction = new IcyAbstractAction("Copy", new IcyIcon(ResourceUtil.ICON_COPY),
            "Copy selected ROI to clipboard (Ctrl+C)", KeyEvent.VK_C, SystemUtil.getMenuCtrlMask())
    {
        /**
         * 
         */
        private static final long serialVersionUID = -4716027958152503425L;

        @Override
        public boolean doAction(ActionEvent e)
        {
            final Sequence sequence = Icy.getMainInterface().getActiveSequence();

            if (sequence != null)
            {
                final List<ROI> rois = sequence.getSelectedROIs();

                if (rois.size() > 0)
                {
                    // need to get a copy of the ROI (as it can change meanwhile)
                    for (int i = 0; i < rois.size(); i++)
                    {
                        final ROI roi = rois.get(i).getCopy();

                        if (roi != null)
                            rois.set(i, roi);
                    }

                    // save in the Icy clipboard
                    Clipboard.put(Clipboard.TYPE_SEQUENCEROILIST, new SequenceRoiList(sequence, rois));
                    // clear system clipboard
                    Clipboard.clearSystem();

                    pasteAction.setEnabled(true);

                    return true;
                }
            }

            return false;
        }

        @Override
        public boolean isEnabled()
        {
            final Sequence sequence = Icy.getMainInterface().getActiveSequence();
            return super.isEnabled() && (sequence != null) && (sequence.getSelectedROIs().size() > 0);
        }
    };

    public static IcyAbstractAction copyLinkAction = new IcyAbstractAction("Copy link",
            new IcyIcon(ResourceUtil.ICON_LINK_COPY), "Copy link of selected ROI to clipboard (Alt+C)", KeyEvent.VK_C,
            InputEvent.ALT_MASK)
    {
        /**
         * 
         */
        private static final long serialVersionUID = -4716027958152503425L;

        @Override
        public boolean doAction(ActionEvent e)
        {
            final RoisPanel roisPanel = Icy.getMainInterface().getRoisPanel();

            if (roisPanel != null)
            {
                final List<ROI> rois = roisPanel.getSelectedRois();

                if (rois.size() > 0)
                {
                    // save in the Icy clipboard
                    Clipboard.put(Clipboard.TYPE_ROILINKLIST, rois);
                    // clear system clipboard
                    Clipboard.clearSystem();

                    pasteLinkAction.setEnabled(true);

                    return true;
                }
            }

            return false;
        }

        @Override
        public boolean isEnabled()
        {
            final Sequence sequence = Icy.getMainInterface().getActiveSequence();
            return super.isEnabled() && (sequence != null) && (sequence.getSelectedROIs().size() > 0);
        }
    };

    public static IcyAbstractAction pasteAction = new IcyAbstractAction("Paste", new IcyIcon(ResourceUtil.ICON_PASTE),
            "Paste ROI from clipboard (Ctrl+V)", KeyEvent.VK_V, SystemUtil.getMenuCtrlMask())
    {
        /**
         * 
         */
        private static final long serialVersionUID = 4878585451006567513L;

        @Override
        public boolean doAction(ActionEvent e)
        {
            final Sequence sequence = Icy.getMainInterface().getActiveSequence();

            if (sequence != null)
            {
                final SequenceRoiList sequenceRoiList = (SequenceRoiList) Clipboard.get(Clipboard.TYPE_SEQUENCEROILIST);
                final Sequence sequenceSrc = sequenceRoiList.sequence;
                final List<ROI> rois = sequenceRoiList.rois;

                if ((rois != null) && (rois.size() > 0))
                {
                    final List<ROI> copyRois = new ArrayList<ROI>();
                    sequence.beginUpdate();
                    try
                    {
                        // unselect all rois
                        sequence.setSelectedROI(null);

                        // add copy to sequence (so we can do the paste operation severals time)
                        for (ROI roi : rois)
                        {
                            // final ROI newROI = roi.getCopy();
                            final ROI newROI = ROIUtil.adjustToSequence(roi, sequenceSrc, sequence, true, true, true);

                            if (newROI != null)
                            {
                                copyRois.add(newROI);

                                // select the ROI
                                newROI.setSelected(true);
                                // and add it
                                sequence.addROI(newROI);
                            }
                        }
                    }
                    finally
                    {
                        sequence.endUpdate();
                    }

                    // add to undo manager
                    sequence.addUndoableEdit(new ROIAddsSequenceEdit(sequence, copyRois)
                    {
                        @Override
                        public String getPresentationName()
                        {
                            if (getROIs().size() > 1)
                                return "ROIs added from clipboard";

                            return "ROI added from clipboard";
                        };
                    });

                    return true;
                }
            }

            return false;
        }

        @Override
        public boolean isEnabled()
        {
            return super.isEnabled() && (Icy.getMainInterface().getActiveSequence() != null)
                    && Clipboard.getType().equals(Clipboard.TYPE_SEQUENCEROILIST);
        }
    };

    public static IcyAbstractAction pasteLinkAction = new IcyAbstractAction("Paste link",
            new IcyIcon(ResourceUtil.ICON_LINK_PASTE), "Paste ROI link from clipboard (Alt+V)", KeyEvent.VK_V,
            InputEvent.ALT_MASK)
    {
        /**
         * 
         */
        private static final long serialVersionUID = 4878585451006567513L;

        @Override
        public boolean doAction(ActionEvent e)
        {
            final Sequence sequence = Icy.getMainInterface().getActiveSequence();

            if (sequence != null)
            {
                @SuppressWarnings("unchecked")
                final List<ROI> rois = (List<ROI>) Clipboard.get(Clipboard.TYPE_ROILINKLIST);

                if ((rois != null) && (rois.size() > 0))
                {
                    sequence.beginUpdate();
                    try
                    {
                        // add to sequence
                        for (ROI roi : rois)
                            sequence.addROI(roi);
                    }
                    finally
                    {
                        sequence.endUpdate();
                    }

                    // add to undo manager
                    sequence.addUndoableEdit(new ROIAddsSequenceEdit(sequence, rois)
                    {
                        @Override
                        public String getPresentationName()
                        {
                            if (getROIs().size() > 1)
                                return "ROIs linked from clipboard";

                            return "ROI linked from clipboard";
                        };
                    });

                    return true;
                }
            }

            return false;
        }

        @Override
        public boolean isEnabled()
        {
            return super.isEnabled() && (Icy.getMainInterface().getActiveSequence() != null)
                    && Clipboard.getType().equals(Clipboard.TYPE_ROILINKLIST);
        }
    };

    // public static IcyAbstractAction clearClipboardAction = new IcyAbstractAction("Clear", new
    // IcyIcon(
    // ResourceUtil.ICON_CLIPBOARD_CLEAR), "Remove ROI saved in clipboard")
    // {
    // /**
    // *
    // */
    // private static final long serialVersionUID = 4878585451006567513L;
    //
    // @Override
    // public boolean doAction(ActionEvent e)
    // {
    // Clipboard.remove(ID_ROI_COPY_CLIPBOARD, false);
    // pasteAction.setEnabled(false);
    // }
    //
    // @Override
    // public boolean isEnabled()
    // {
    // return super.isEnabled() && (Icy.getMainInterface().getActiveSequence() != null)
    // && Clipboard.hasObjects(RoiActions.ID_ROI_COPY_CLIPBOARD, false);
    // }
    // };

    public static IcyAbstractAction selectAllAction = new IcyAbstractAction("SelectAll", (IcyIcon) null,
            "Select all ROI(s)")
    {
        /**
         * 
         */
        private static final long serialVersionUID = 3219000949426093919L;

        @Override
        public boolean doAction(ActionEvent e)
        {
            final Sequence sequence = Icy.getMainInterface().getActiveSequence();

            if (sequence != null)
            {
                sequence.setSelectedROIs((List<ROI>) sequence.getROIs());
                return true;
            }

            return false;
        }

        @Override
        public boolean isEnabled()
        {
            final Sequence sequence = Icy.getMainInterface().getActiveSequence();

            return super.isEnabled() && (sequence != null) && (sequence.getROIs().size() > 0);
        }
    };

    public static IcyAbstractAction unselectAction = new IcyAbstractAction("Unselect", (IcyIcon) null,
            "Unselect ROI(s)", KeyEvent.VK_ESCAPE)
    {
        /**
         * 
         */
        private static final long serialVersionUID = -6136680076368815566L;

        @Override
        public boolean doAction(ActionEvent e)
        {
            final Sequence sequence = Icy.getMainInterface().getActiveSequence();

            if (sequence != null)
            {
                sequence.setSelectedROI(null);
                return true;
            }

            return false;
        }

        @Override
        public boolean isEnabled()
        {
            return super.isEnabled() && (Icy.getMainInterface().getActiveSequence() != null);
        }
    };

    public static IcyAbstractAction deleteAction = new IcyAbstractAction("Delete",
            new IcyIcon(ResourceUtil.ICON_DELETE), "Delete selected ROI(s)",
            "Delete selected ROI(s) from the active sequence", KeyEvent.VK_DELETE, 0)
    {
        /**
         * 
         */
        private static final long serialVersionUID = 9079403002834893222L;

        @Override
        public boolean doAction(ActionEvent e)
        {
            final Sequence sequence = Icy.getMainInterface().getActiveSequence();

            if (sequence != null)
            {
                sequence.removeSelectedROIs(false, true);
                return true;
            }

            return false;
        }

        @Override
        public boolean isEnabled()
        {
            final Sequence sequence = Icy.getMainInterface().getActiveSequence();
            return super.isEnabled() && (sequence != null) && (sequence.getSelectedROIs().size() > 0);
        }
    };

    public static IcyAbstractAction boolNotAction = new IcyAbstractAction("Inversion",
            new IcyIcon(ResourceUtil.ICON_ROI_NOT), "Boolean inversion operation",
            "Create a new ROI representing the inverse of selected ROI", true, "Computing inverse...")
    {
        /**
         * 
         */
        private static final long serialVersionUID = 6360796066188754099L;

        @Override
        public boolean doAction(ActionEvent e)
        {
            final Sequence sequence = Icy.getMainInterface().getActiveSequence();
            final RoisPanel roisPanel = Icy.getMainInterface().getRoisPanel();

            if ((sequence != null) && (roisPanel != null))
            {
                // NOT operation
                sequence.beginUpdate();
                try
                {
                    final List<ROI> selectedROI = roisPanel.getSelectedRois();

                    // work only on single ROI
                    if (selectedROI.size() != 1)
                        return false;

                    final ROI roi = selectedROI.get(0);
                    final ROI seqRoi;

                    switch (roi.getDimension())
                    {
                        case 2:
                            final ROI2D roi2d = (ROI2D) roi;
                            final ROI2DRectangle seqRoi2d = new ROI2DRectangle(sequence.getBounds2D());
                            // set on same position
                            seqRoi2d.setZ(roi2d.getZ());
                            seqRoi2d.setT(roi2d.getT());
                            seqRoi2d.setC(roi2d.getC());
                            seqRoi = seqRoi2d;
                            break;

                        case 3:
                            final ROI3D roi3d = (ROI3D) roi;
                            final ROI3DStackRectangle seqRoi3d = new ROI3DStackRectangle(
                                    sequence.getBounds5D().toRectangle3D());
                            // set on same position
                            seqRoi3d.setT(roi3d.getT());
                            seqRoi3d.setC(roi3d.getC());
                            seqRoi = seqRoi3d;
                            break;

                        case 4:
                            final ROI4D roi4d = (ROI4D) roi;
                            final ROI4DStackRectangle seqRoi4d = new ROI4DStackRectangle(
                                    sequence.getBounds5D().toRectangle4D());
                            // set on same position
                            seqRoi4d.setC(roi4d.getC());
                            seqRoi = seqRoi4d;
                            break;

                        case 5:
                            seqRoi = new ROI5DStackRectangle(sequence.getBounds5D());
                            break;

                        default:
                            seqRoi = null;
                            break;
                    }

                    if (seqRoi != null)
                    {
                        // we do the NOT operation by subtracting current ROI to sequence bounds ROI
                        final ROI mergeROI = ROIUtil.subtract(seqRoi, roi);

                        if (mergeROI != null)
                        {
                            mergeROI.setName("Inverse");

                            sequence.addROI(mergeROI);
                            sequence.setSelectedROI(mergeROI);

                            // add to undo manager
                            sequence.addUndoableEdit(new ROIAddSequenceEdit(sequence, mergeROI, "ROI Inverse"));
                        }
                    }
                    else
                        MessageDialog.showDialog("Operation not supported", "Input ROI has incorrect dimension !",
                                MessageDialog.ERROR_MESSAGE);
                }
                catch (UnsupportedOperationException ex)
                {
                    MessageDialog.showDialog("Operation not supported", ex.getLocalizedMessage(),
                            MessageDialog.ERROR_MESSAGE);
                }
                finally
                {
                    sequence.endUpdate();
                }

                return true;
            }

            return false;
        }

        @Override
        public boolean isEnabled()
        {
            return super.isEnabled() && (Icy.getMainInterface().getActiveSequence() != null);
        }
    };

    public static IcyAbstractAction boolOrAction = new IcyAbstractAction("Union", new IcyIcon(ResourceUtil.ICON_ROI_OR),
            "Boolean union operation", "Create a new ROI representing the union of selected ROIs", true,
            "Computing union...")
    {
        /**
         * 
         */
        private static final long serialVersionUID = 1861052712498233441L;

        @Override
        public boolean doAction(ActionEvent e)
        {
            final Sequence sequence = Icy.getMainInterface().getActiveSequence();
            final RoisPanel roisPanel = Icy.getMainInterface().getRoisPanel();

            if ((sequence != null) && (roisPanel != null))
            {
                // OR operation
                sequence.beginUpdate();
                try
                {
                    final List<ROI> selectedROIs = roisPanel.getSelectedRois();
                    final ROI mergeROI = ROIUtil.getUnion(selectedROIs);

                    if (mergeROI != null)
                    {
                        mergeROI.setName("Union");

                        sequence.addROI(mergeROI);
                        sequence.setSelectedROI(mergeROI);

                        // add to undo manager
                        sequence.addUndoableEdit(new ROIAddSequenceEdit(sequence, mergeROI, "ROI Union"));
                    }
                }
                catch (UnsupportedOperationException ex)
                {
                    MessageDialog.showDialog("Operation not supported", ex.getLocalizedMessage(),
                            MessageDialog.ERROR_MESSAGE);
                }
                finally
                {
                    sequence.endUpdate();
                }

                return true;
            }

            return false;
        }

        @Override
        public boolean isEnabled()
        {
            return super.isEnabled() && (Icy.getMainInterface().getActiveSequence() != null);
        }
    };

    public static IcyAbstractAction boolAndAction = new IcyAbstractAction("Intersection",
            new IcyIcon(ResourceUtil.ICON_ROI_AND), "Boolean intersection operation",
            "Create a new ROI representing the intersection of selected ROIs", true, "Computing intersection...")
    {
        /**
         * 
         */
        private static final long serialVersionUID = -9103158044679039413L;

        @Override
        public boolean doAction(ActionEvent e)
        {
            final Sequence sequence = Icy.getMainInterface().getActiveSequence();
            final RoisPanel roisPanel = Icy.getMainInterface().getRoisPanel();

            if ((sequence != null) && (roisPanel != null))
            {
                // AND operation
                sequence.beginUpdate();
                try
                {
                    final List<ROI> selectedROIs = roisPanel.getSelectedRois();
                    final ROI mergeROI = ROIUtil.getIntersection(selectedROIs);

                    if (mergeROI != null)
                    {
                        mergeROI.setName("Intersection");

                        sequence.addROI(mergeROI);
                        sequence.setSelectedROI(mergeROI);

                        // add to undo manager
                        sequence.addUndoableEdit(new ROIAddSequenceEdit(sequence, mergeROI, "ROI Intersection"));
                    }
                }
                catch (UnsupportedOperationException ex)
                {
                    MessageDialog.showDialog("Operation not supported", ex.getLocalizedMessage(),
                            MessageDialog.ERROR_MESSAGE);
                }
                finally
                {
                    sequence.endUpdate();
                }

                return true;
            }

            return false;
        }

        @Override
        public boolean isEnabled()
        {
            return super.isEnabled() && (Icy.getMainInterface().getActiveSequence() != null);
        }
    };

    public static IcyAbstractAction boolXorAction = new IcyAbstractAction("Exclusive union",
            new IcyIcon(ResourceUtil.ICON_ROI_XOR), "Boolean exclusive union operation",
            "Create a new ROI representing the exclusive union of selected ROIs", true, "Computing exclusive union...")
    {
        /**
         * 
         */
        private static final long serialVersionUID = 1609345474914807703L;

        @Override
        public boolean doAction(ActionEvent e)
        {
            final Sequence sequence = Icy.getMainInterface().getActiveSequence();
            final RoisPanel roisPanel = Icy.getMainInterface().getRoisPanel();

            if ((sequence != null) && (roisPanel != null))
            {
                // XOR operation
                sequence.beginUpdate();
                try
                {
                    final List<ROI> selectedROIs = roisPanel.getSelectedRois();
                    final ROI mergeROI = ROIUtil.getExclusiveUnion(selectedROIs);

                    if (mergeROI != null)
                    {
                        mergeROI.setName("Exclusive union");

                        sequence.addROI(mergeROI);
                        sequence.setSelectedROI(mergeROI);

                        // add to undo manager
                        sequence.addUndoableEdit(new ROIAddSequenceEdit(sequence, mergeROI, "ROI Exclusive Union"));
                    }
                }
                catch (UnsupportedOperationException ex)
                {
                    MessageDialog.showDialog("Operation not supported", ex.getLocalizedMessage(),
                            MessageDialog.ERROR_MESSAGE);
                }
                finally
                {
                    sequence.endUpdate();
                }

                return true;
            }

            return false;
        }

        @Override
        public boolean isEnabled()
        {
            return super.isEnabled() && (Icy.getMainInterface().getActiveSequence() != null);
        }
    };

    public static IcyAbstractAction boolSubtractAction = new IcyAbstractAction("Subtraction",
            new IcyIcon(ResourceUtil.ICON_ROI_SUB), "Boolean subtraction",
            "Create 2 ROIs representing the result of (A - B) and (B - A)", true, "Computing subtraction...")
    {
        /**
         * 
         */
        private static final long serialVersionUID = 9094641559971542667L;

        @Override
        public boolean doAction(ActionEvent e)
        {
            final Sequence sequence = Icy.getMainInterface().getActiveSequence();
            final RoisPanel roisPanel = Icy.getMainInterface().getRoisPanel();

            if ((sequence != null) && (roisPanel != null))
            {
                // SUB operation
                sequence.beginUpdate();
                try
                {
                    final List<ROI> selectedROI = roisPanel.getSelectedRois();
                    final List<ROI> generatedROIs = new ArrayList<ROI>();

                    // Subtraction work only when 2 ROI are selected
                    if (selectedROI.size() != 2)
                        return false;

                    final ROI subtractAB = ROIUtil.subtract(selectedROI.get(0), selectedROI.get(1));
                    final ROI subtractBA = ROIUtil.subtract(selectedROI.get(1), selectedROI.get(0));

                    subtractAB.setName("Subtract A-B");
                    subtractBA.setName("Subtract B-A");

                    generatedROIs.add(subtractAB);
                    generatedROIs.add(subtractBA);

                    sequence.beginUpdate();
                    try
                    {
                        for (ROI roi : generatedROIs)
                            sequence.addROI(roi);

                        sequence.setSelectedROIs(generatedROIs);

                        // add to undo manager
                        sequence.addUndoableEdit(new ROIAddsSequenceEdit(sequence, generatedROIs, "ROI Subtraction"));
                    }
                    finally
                    {
                        sequence.endUpdate();
                    }
                }
                catch (UnsupportedOperationException ex)
                {
                    MessageDialog.showDialog("Operation not supported", ex.getLocalizedMessage(),
                            MessageDialog.ERROR_MESSAGE);
                }
                finally
                {
                    sequence.endUpdate();
                }

                return true;
            }

            return false;
        }

        @Override
        public boolean isEnabled()
        {
            return super.isEnabled() && (Icy.getMainInterface().getActiveSequence() != null);
        }
    };

    public static IcyAbstractAction fillInteriorAction = new IcyAbstractAction("Fill interior",
            new IcyIcon(ResourceUtil.ICON_ROI_INTERIOR), "Fill ROI(s) interior",
            "Fill interior of the selected ROI(s) with specified value", true, "Fill ROI(s) interior...")
    {
        @Override
        public boolean doAction(ActionEvent e)
        {
            final Sequence sequence = Icy.getMainInterface().getActiveSequence();

            if (sequence != null)
            {
                final MainFrame mainFrame = Icy.getMainInterface().getMainFrame();

                if (mainFrame != null)
                {
                    final double value = mainFrame.getMainRibbon().getROIRibbonTask().getFillValue();
                    // create undo point
                    final boolean canUndo = sequence.createUndoDataPoint("ROI fill interior");

                    // cannot backup
                    if (!canUndo)
                    {
                        // ask confirmation to continue
                        if (!IdConfirmDialog.confirm(
                                "Not enough memory to undo the operation, do you want to continue ?",
                                "ROIFillInteriorConfirm"))
                            return false;
                    }

                    for (ROI roi : sequence.getSelectedROIs())
                        DataIteratorUtil.set(new SequenceDataIterator(sequence, roi, true), value);

                    sequence.dataChanged();

                    // no undo, clear undo manager after modification
                    if (!canUndo)
                        sequence.clearUndoManager();

                    return true;
                }
            }

            return false;
        }

        @Override
        public boolean isEnabled()
        {
            final Sequence sequence = Icy.getMainInterface().getActiveSequence();

            return super.isEnabled() && (sequence != null) && !sequence.isEmpty();
        }
    };

    public static IcyAbstractAction fillExteriorAction = new IcyAbstractAction("Fill exterior",
            new IcyIcon(ResourceUtil.ICON_ROI_NOT), "Fill ROI(s) exterior",
            "Fill exterior of the selected ROI(s) with specified value", true, "Fill ROI(s) exterior...")
    {
        @Override
        public boolean doAction(ActionEvent e)
        {
            final Sequence sequence = Icy.getMainInterface().getActiveSequence();

            if (sequence != null)
            {
                final MainFrame mainFrame = Icy.getMainInterface().getMainFrame();

                if (mainFrame != null)
                {
                    final double value = mainFrame.getMainRibbon().getROIRibbonTask().getFillValue();
                    // create undo point
                    final boolean canUndo = sequence.createUndoDataPoint("ROI fill exterior");

                    // cannot backup
                    if (!canUndo)
                    {
                        // ask confirmation to continue
                        if (!IdConfirmDialog.confirm(
                                "Not enough memory to undo the operation, do you want to continue ?",
                                "ROIFillExteriorConfirm"))
                            return false;
                    }

                    try
                    {
                        final ROI roiUnion = ROIUtil.merge(sequence.getSelectedROIs(), BooleanOperator.OR);
                        final ROI roiSeq = new ROI5DStackRectangle(sequence.getBounds5D());
                        final ROI roi = roiSeq.getSubtraction(roiUnion);

                        DataIteratorUtil.set(new SequenceDataIterator(sequence, roi), value);

                        sequence.dataChanged();

                        // no undo, clear undo manager after modification
                        if (!canUndo)
                            sequence.clearUndoManager();

                        return true;
                    }
                    catch (UnsupportedOperationException ex)
                    {
                        // undo operation if possible
                        if (canUndo)
                            sequence.undo();

                        MessageDialog.showDialog("Operation not supported", ex.getLocalizedMessage(),
                                MessageDialog.ERROR_MESSAGE);

                        return false;
                    }
                }
            }

            return false;
        }

        @Override
        public boolean isEnabled()
        {
            final Sequence sequence = Icy.getMainInterface().getActiveSequence();

            return super.isEnabled() && (sequence != null) && !sequence.isEmpty();
        }
    };

    public static IcyAbstractAction xlsExportAction = new IcyAbstractAction("Excel export",
            new IcyIcon(ResourceUtil.ICON_XLS_EXPORT), "ROI Excel export",
            "Export the content of the ROI table into a XLS/CSV file", true, "Exporting ROI informations...")
    {
        /**
         * 
         */
        private static final long serialVersionUID = 9094641559971542667L;

        @Override
        public boolean doAction(ActionEvent e)
        {
            final Sequence sequence = Icy.getMainInterface().getActiveSequence();
            final RoisPanel roisPanel = Icy.getMainInterface().getRoisPanel();

            if ((sequence != null) && (roisPanel != null))
            {
                final String content = roisPanel.getCSVFormattedInfos();

                if (StringUtil.isEmpty(content) || roisPanel.getVisibleRois().isEmpty())
                {
                    MessageDialog.showDialog("Nothing to export !", MessageDialog.INFORMATION_MESSAGE);
                    return true;
                }

                final String filename = SaveDialog.chooseFileForResult("Export ROIs...", "result", ".xls");

                if (filename != null)
                {
                    try
                    {
                        // CSV format wanted ?
                        if (!FileUtil.getFileExtension(filename, false).toLowerCase().startsWith("xls"))
                        {
                            // just write CSV content
                            final PrintWriter out = new PrintWriter(filename);
                            out.println(content);
                            out.close();
                        }
                        // XLS export
                        else
                        {
                            final WritableWorkbook workbook = XLSUtil.createWorkbook(filename);
                            final WritableSheet sheet = XLSUtil.createNewPage(workbook, "ROIS");

                            if (XLSUtil.setFromCSV(sheet, content))
                                XLSUtil.saveAndClose(workbook);
                            else
                            {
                                MessageDialog.showDialog("Error",
                                        "Error while exporting ROIs table content to XLS file.",
                                        MessageDialog.ERROR_MESSAGE);
                                return false;
                            }
                        }
                    }
                    catch (Exception e1)
                    {
                        MessageDialog.showDialog("Error", e1.getMessage(), MessageDialog.ERROR_MESSAGE);
                        return false;
                    }
                }

                return true;
            }

            return false;
        }

        @Override
        public boolean isEnabled()
        {
            final Sequence sequence = Icy.getMainInterface().getActiveSequence();

            return super.isEnabled() && (sequence != null);
        }
    };

    public static IcyAbstractAction settingAction = new IcyAbstractAction("Preferences",
            new IcyIcon(ResourceUtil.ICON_COG), "ROI table preferences")
    {
        @Override
        public boolean doAction(ActionEvent e)
        {
            final RoisPanel roisPanel = Icy.getMainInterface().getRoisPanel();

            if (roisPanel != null)
            {
                roisPanel.showSettingPanel();

                return true;
            }

            return false;
        }
    };

    public static IcyAbstractAction convertToStackAction = new IcyAbstractAction("to 3D stack",
            new IcyIcon(ResourceUtil.ICON_LAYER_V2), "Convert to 3D stack ROI",
            "Convert selected 2D ROI to 3D stack ROI by stacking it along the Z axis")
    {
        @Override
        public boolean doAction(ActionEvent e)
        {
            final Sequence sequence = Icy.getMainInterface().getActiveSequence();

            if (sequence != null)
            {
                final int maxZ = sequence.getSizeZ() - 1;

                // ROI Z stack conversion
                sequence.beginUpdate();
                try
                {
                    final List<ROI2D> selectedROIs = sequence.getSelectedROI2Ds();
                    final List<ROI> removedROIs = new ArrayList<ROI>();
                    final List<ROI> addedROIs = new ArrayList<ROI>();

                    for (ROI2D roi : selectedROIs)
                    {
                        final ROI stackedRoi = ROIUtil.convertToStack(roi, 0, maxZ);

                        if (stackedRoi != null)
                        {
                            // select it by default
                            stackedRoi.setSelected(true);

                            sequence.removeROI(roi);
                            sequence.addROI(stackedRoi);

                            // add to undo manager
                            removedROIs.add(roi);
                            addedROIs.add(stackedRoi);
                        }
                    }

                    if (!addedROIs.isEmpty())
                        sequence.addUndoableEdit(new ROIReplacesSequenceEdit(sequence, removedROIs, addedROIs,
                                (addedROIs.size() > 1) ? "ROIs 3D stack conversion" : "ROI 3D stack conversion"));
                }
                catch (UnsupportedOperationException ex)
                {
                    MessageDialog.showDialog("Operation not supported", ex.toString(), MessageDialog.ERROR_MESSAGE);
                }
                finally
                {
                    sequence.endUpdate();
                }

                return true;
            }

            return false;
        }

        @Override
        public boolean isEnabled()
        {
            return super.isEnabled() && (Icy.getMainInterface().getActiveSequence() != null);
        }
    };

    public static IcyAbstractAction convertToMaskAction = new IcyAbstractAction("to Mask",
            new IcyIcon(ResourceUtil.ICON_BOOL_MASK), "Convert Shape ROI to Mask ROI",
            "Convert selected Shape ROI to Mask ROI by using their boolean mask")
    {
        @Override
        public boolean doAction(ActionEvent e)
        {
            final Sequence sequence = Icy.getMainInterface().getActiveSequence();

            if (sequence != null)
            {
                // ROI mask conversion
                sequence.beginUpdate();
                try
                {
                    final List<ROI> selectedROIs = sequence.getSelectedROIs();
                    final List<ROI> removedROIs = new ArrayList<ROI>();
                    final List<ROI> addedROIs = new ArrayList<ROI>();

                    for (ROI roi : selectedROIs)
                    {
                        final ROI maskRoi = ROIUtil.convertToMask(roi);

                        if (maskRoi != null)
                        {
                            // select it by default
                            maskRoi.setSelected(true);

                            sequence.removeROI(roi);
                            sequence.addROI(maskRoi);

                            // add to undo manager
                            removedROIs.add(roi);
                            addedROIs.add(maskRoi);
                        }
                    }

                    if (!addedROIs.isEmpty())
                        sequence.addUndoableEdit(new ROIReplacesSequenceEdit(sequence, removedROIs, addedROIs,
                                (addedROIs.size() > 1) ? "ROIs mask conversion" : "ROI mask conversion"));
                }
                catch (UnsupportedOperationException ex)
                {
                    MessageDialog.showDialog("Operation not supported", ex.toString(), MessageDialog.ERROR_MESSAGE);
                }
                finally
                {
                    sequence.endUpdate();
                }

                return true;
            }

            return false;
        }

        @Override
        public boolean isEnabled()
        {
            return super.isEnabled() && (Icy.getMainInterface().getActiveSequence() != null);
        }
    };

    public static IcyAbstractAction convertToPointAction = new IcyAbstractAction("to Point",
            new IcyIcon(ResourceUtil.ICON_ROI_POINT), "Convert ROI to Point ROI",
            "Converts selected ROI(s) to ROI Point (2D or 3D) representing the mass center of the input ROI(s)")
    {
        @Override
        public boolean doAction(ActionEvent e)
        {
            final Sequence sequence = Icy.getMainInterface().getActiveSequence();

            if (sequence != null)
            {
                // ROI point conversion
                sequence.beginUpdate();
                try
                {
                    final List<ROI> selectedROIs = sequence.getSelectedROIs();
                    final List<ROI> removedROIs = new ArrayList<ROI>();
                    final List<ROI> addedROIs = new ArrayList<ROI>();

                    for (ROI roi : selectedROIs)
                    {
                        final ROI roiPoint = ROIUtil.convertToPoint(roi);

                        if (roiPoint != null)
                        {
                            // select it by default
                            roiPoint.setSelected(true);

                            sequence.removeROI(roi);
                            sequence.addROI(roiPoint);

                            // add to undo manager
                            removedROIs.add(roi);
                            addedROIs.add(roiPoint);
                        }
                    }

                    if (!addedROIs.isEmpty())
                        sequence.addUndoableEdit(new ROIReplacesSequenceEdit(sequence, removedROIs, addedROIs,
                                (addedROIs.size() > 1) ? "ROIs point conversion" : "ROI point conversion"));
                }
                catch (UnsupportedOperationException ex)
                {
                    MessageDialog.showDialog("Operation not supported", ex.toString(), MessageDialog.ERROR_MESSAGE);
                }
                finally
                {
                    sequence.endUpdate();
                }

                return true;
            }

            return false;
        }

        @Override
        public boolean isEnabled()
        {
            return super.isEnabled() && (Icy.getMainInterface().getActiveSequence() != null);
        }
    };

    public static IcyAbstractAction convertToEllipseAction = new IcyAbstractAction("to Circle",
            new IcyIcon(ResourceUtil.ICON_ROI_OVAL), "Convert ROI to Circle ROI",
            "Converts selected ROI(s) to Circle ROI centered on the mass center of the input ROI(s)")
    {
        @Override
        public boolean doAction(ActionEvent e)
        {
            final Sequence sequence = Icy.getMainInterface().getActiveSequence();

            if (sequence != null)
            {
                final MainFrame mainFrame = Icy.getMainInterface().getMainFrame();

                if (mainFrame != null)
                {
                    final double radius = mainFrame.getMainRibbon().getROIRibbonTask().getRadius();

                    // ROI point conversion
                    sequence.beginUpdate();
                    try
                    {
                        final List<ROI> selectedROIs = sequence.getSelectedROIs();
                        final List<ROI> removedROIs = new ArrayList<ROI>();
                        final List<ROI> addedROIs = new ArrayList<ROI>();

                        for (ROI roi : selectedROIs)
                        {
                            final ROI resultRoi;

                            if (radius == 0)
                                resultRoi = ROIUtil.convertToPoint(roi);
                            else
                                resultRoi = ROIUtil.convertToEllipse(roi, radius, radius);

                            if (resultRoi != null)
                            {
                                // select it by default
                                resultRoi.setSelected(true);

                                sequence.removeROI(roi);
                                sequence.addROI(resultRoi);

                                // add to undo manager
                                removedROIs.add(roi);
                                addedROIs.add(resultRoi);
                            }
                        }

                        if (!addedROIs.isEmpty())
                            sequence.addUndoableEdit(new ROIReplacesSequenceEdit(sequence, removedROIs, addedROIs,
                                    (addedROIs.size() > 1) ? "ROIs circle conversion" : "ROI circle conversion"));
                    }
                    catch (UnsupportedOperationException ex)
                    {
                        MessageDialog.showDialog("Operation not supported", ex.toString(), MessageDialog.ERROR_MESSAGE);
                    }
                    finally
                    {
                        sequence.endUpdate();
                    }
                }

                return true;
            }

            return false;
        }

        @Override
        public boolean isEnabled()
        {
            return super.isEnabled() && (Icy.getMainInterface().getActiveSequence() != null);
        }
    };

    public static IcyAbstractAction convertToRectangleAction = new IcyAbstractAction("to Square",
            new IcyIcon(ResourceUtil.ICON_ROI_RECTANGLE), "Convert ROI to Square ROI",
            "Converts selected ROI(s) to Square ROI centered on the mass center of the input ROI(s)")
    {
        @Override
        public boolean doAction(ActionEvent e)
        {
            final Sequence sequence = Icy.getMainInterface().getActiveSequence();

            if (sequence != null)
            {
                final MainFrame mainFrame = Icy.getMainInterface().getMainFrame();

                if (mainFrame != null)
                {
                    final double size = mainFrame.getMainRibbon().getROIRibbonTask().getRadius() * 2;

                    // ROI point conversion
                    sequence.beginUpdate();
                    try
                    {
                        final List<ROI> selectedROIs = sequence.getSelectedROIs();
                        final List<ROI> removedROIs = new ArrayList<ROI>();
                        final List<ROI> addedROIs = new ArrayList<ROI>();

                        for (ROI roi : selectedROIs)
                        {
                            final ROI resultRoi;

                            if (size == 0)
                                resultRoi = ROIUtil.convertToPoint(roi);
                            else
                                resultRoi = ROIUtil.convertToRectangle(roi, size, size);

                            if (resultRoi != null)
                            {
                                // select it by default
                                resultRoi.setSelected(true);

                                sequence.removeROI(roi);
                                sequence.addROI(resultRoi);

                                // add to undo manager
                                removedROIs.add(roi);
                                addedROIs.add(resultRoi);
                            }
                        }

                        if (!addedROIs.isEmpty())
                            sequence.addUndoableEdit(new ROIReplacesSequenceEdit(sequence, removedROIs, addedROIs,
                                    (addedROIs.size() > 1) ? "ROIs square conversion" : "ROI square conversion"));
                    }
                    catch (UnsupportedOperationException ex)
                    {
                        MessageDialog.showDialog("Operation not supported", ex.toString(), MessageDialog.ERROR_MESSAGE);
                    }
                    finally
                    {
                        sequence.endUpdate();
                    }
                }

                return true;
            }

            return false;
        }

        @Override
        public boolean isEnabled()
        {
            return super.isEnabled() && (Icy.getMainInterface().getActiveSequence() != null);
        }
    };

    public static IcyAbstractAction convertToShapeAction = new IcyAbstractAction("to Shape",
            new IcyIcon(ResourceUtil.ICON_ROI_POLYGON), "Convert Mask ROI to Polygon shape ROI",
            "Convert selected Mask ROI to Shape ROI using polygon approximation")
    {
        @Override
        public boolean doAction(ActionEvent e)
        {
            final Sequence sequence = Icy.getMainInterface().getActiveSequence();

            if (sequence != null)
            {
                // ROI shape conversion
                sequence.beginUpdate();
                try
                {
                    final List<ROI> selectedROIs = sequence.getSelectedROIs();
                    final List<ROI> removedROIs = new ArrayList<ROI>();
                    final List<ROI> addedROIs = new ArrayList<ROI>();

                    for (ROI roi : selectedROIs)
                    {
                        final ROI shapeRoi = ROIUtil.convertToShape(roi, -1);

                        if (shapeRoi != null)
                        {
                            // select it by default
                            shapeRoi.setSelected(true);

                            sequence.removeROI(roi);
                            sequence.addROI(shapeRoi);

                            // add to undo manager
                            removedROIs.add(roi);
                            addedROIs.add(shapeRoi);
                        }
                    }

                    if (!addedROIs.isEmpty())
                        sequence.addUndoableEdit(new ROIReplacesSequenceEdit(sequence, removedROIs, addedROIs,
                                (addedROIs.size() > 1) ? "ROIs shape conversion" : "ROI shape conversion"));
                }
                catch (UnsupportedOperationException ex)
                {
                    MessageDialog.showDialog("Operation not supported", ex.toString(), MessageDialog.ERROR_MESSAGE);
                }
                finally
                {
                    sequence.endUpdate();
                }

                return true;
            }

            return false;
        }

        @Override
        public boolean isEnabled()
        {
            return super.isEnabled() && (Icy.getMainInterface().getActiveSequence() != null);
        }
    };

    public static IcyAbstractAction separateObjectsAction = new IcyAbstractAction("Separate",
            new IcyIcon(ResourceUtil.ICON_ROI_COMP), "Separate regions from selected Mask ROI(s)",
            "Separate unconnected regions from selected Mask ROI(s)")
    {
        @Override
        public boolean doAction(ActionEvent e)
        {
            final Sequence sequence = Icy.getMainInterface().getActiveSequence();

            if (sequence != null)
            {
                sequence.beginUpdate();
                try
                {
                    final List<ROI> selectedROIs = sequence.getSelectedROIs();
                    final List<ROI> removedROIs = new ArrayList<ROI>();
                    final List<ROI> addedROIs = new ArrayList<ROI>();

                    for (ROI roi : selectedROIs)
                    {
                        final List<ROI> components = ROIUtil.getConnectedComponents(roi);

                        // nothing to do if we obtain only 1 component
                        if (components.size() > 1)
                        {
                            sequence.removeROI(roi);
                            removedROIs.add(roi);

                            for (ROI component : components)
                            {
                                sequence.addROI(component);
                                // add to undo manager
                                addedROIs.add(component);
                            }
                        }
                    }

                    if (!removedROIs.isEmpty())
                        sequence.addUndoableEdit(new ROIReplacesSequenceEdit(sequence, removedROIs, addedROIs,
                                (removedROIs.size() > 1) ? "ROIs separate objects" : "ROI separate objects"));
                }
                catch (UnsupportedOperationException ex)
                {
                    MessageDialog.showDialog("Operation not supported", ex.toString(), MessageDialog.ERROR_MESSAGE);
                }
                finally
                {
                    sequence.endUpdate();
                }

                return true;
            }

            return false;
        }

        @Override
        public boolean isEnabled()
        {
            return super.isEnabled() && (Icy.getMainInterface().getActiveSequence() != null);
        }
    };

    public static IcyAbstractAction upscale2dAction = new IcyAbstractAction("Scale x2 (2D)",
            new IcyIcon(ResourceUtil.ICON_ROI_UPSCALE), "Create up scaled version of selected ROI(s) (2D)",
            "Create 2x factor up scaled version of selected ROI(s) (2D)")
    {
        @Override
        public boolean doAction(ActionEvent e)
        {
            final Sequence sequence = Icy.getMainInterface().getActiveSequence();

            if (sequence != null)
            {
                sequence.beginUpdate();
                try
                {
                    final List<ROI> selectedROIs = sequence.getSelectedROIs();
                    final List<ROI> newROIs = new ArrayList<ROI>();

                    for (ROI roi : selectedROIs)
                        newROIs.add(ROIUtil.getUpscaled(roi, false));

                    if (!newROIs.isEmpty())
                    {
                        for (ROI roi : newROIs)
                            sequence.addROI(roi);

                        sequence.addUndoableEdit(new ROIAddsSequenceEdit(sequence, newROIs,
                                (newROIs.size() > 1) ? "ROIs scale x2" : "ROI scale x2"));
                    }
                }
                catch (UnsupportedOperationException ex)
                {
                    MessageDialog.showDialog("Operation not supported", ex.toString(), MessageDialog.ERROR_MESSAGE);
                }
                finally
                {
                    sequence.endUpdate();
                }

                return true;
            }

            return false;
        }

        @Override
        public boolean isEnabled()
        {
            return super.isEnabled() && (Icy.getMainInterface().getActiveSequence() != null);
        }
    };

    public static IcyAbstractAction upscaleAction = new IcyAbstractAction("Scale x2",
            new IcyIcon(ResourceUtil.ICON_ROI_UPSCALE), "Create x2 scaled version of selected ROI(s)",
            "Create x2 factor scaled version of selected ROI(s)")
    {
        @Override
        public boolean doAction(ActionEvent e)
        {
            final Sequence sequence = Icy.getMainInterface().getActiveSequence();

            if (sequence != null)
            {
                sequence.beginUpdate();
                try
                {
                    final List<ROI> selectedROIs = sequence.getSelectedROIs();
                    final List<ROI> newROIs = new ArrayList<ROI>();

                    for (ROI roi : selectedROIs)
                        newROIs.add(ROIUtil.getUpscaled(roi, true));

                    if (!newROIs.isEmpty())
                    {
                        for (ROI roi : newROIs)
                            sequence.addROI(roi);

                        sequence.addUndoableEdit(new ROIAddsSequenceEdit(sequence, newROIs,
                                (newROIs.size() > 1) ? "ROIs scale x2" : "ROI scale x2"));
                    }
                }
                catch (UnsupportedOperationException ex)
                {
                    MessageDialog.showDialog("Operation not supported", ex.toString(), MessageDialog.ERROR_MESSAGE);
                }
                finally
                {
                    sequence.endUpdate();
                }

                return true;
            }

            return false;
        }

        @Override
        public boolean isEnabled()
        {
            return super.isEnabled() && (Icy.getMainInterface().getActiveSequence() != null);
        }
    };

    public static IcyAbstractAction downscale2dAction = new IcyAbstractAction("Scale /2 (2D)",
            new IcyIcon(ResourceUtil.ICON_ROI_DOWNSCALE), "Create /2 scaled version of selected ROI(s) (2D)",
            "Create /2 factor scaled version of selected ROI(s) (2D)")
    {
        @Override
        public boolean doAction(ActionEvent e)
        {
            final Sequence sequence = Icy.getMainInterface().getActiveSequence();

            if (sequence != null)
            {
                sequence.beginUpdate();
                try
                {
                    final List<ROI> selectedROIs = sequence.getSelectedROIs();
                    final List<ROI> newROIs = new ArrayList<ROI>();

                    for (ROI roi : newROIs)
                        newROIs.add(ROIUtil.getDownscaled(roi, false));

                    if (!newROIs.isEmpty())
                    {
                        for (ROI roi : selectedROIs)
                            sequence.addROI(roi);

                        sequence.addUndoableEdit(new ROIAddsSequenceEdit(sequence, newROIs,
                                (newROIs.size() > 1) ? "ROIs scale /2" : "ROI scale /2"));
                    }
                }
                catch (UnsupportedOperationException ex)
                {
                    MessageDialog.showDialog("Operation not supported", ex.toString(), MessageDialog.ERROR_MESSAGE);
                }
                finally
                {
                    sequence.endUpdate();
                }

                return true;
            }

            return false;
        }

        @Override
        public boolean isEnabled()
        {
            return super.isEnabled() && (Icy.getMainInterface().getActiveSequence() != null);
        }
    };

    public static IcyAbstractAction downscaleAction = new IcyAbstractAction("Scale /2",
            new IcyIcon(ResourceUtil.ICON_ROI_DOWNSCALE), "Create down scaled version of selected ROI(s)",
            "Create 2x factor down scaled version of selected ROI(s)")
    {
        @Override
        public boolean doAction(ActionEvent e)
        {
            final Sequence sequence = Icy.getMainInterface().getActiveSequence();

            if (sequence != null)
            {
                sequence.beginUpdate();
                try
                {
                    final List<ROI> selectedROIs = sequence.getSelectedROIs();
                    final List<ROI> newROIs = new ArrayList<ROI>();

                    for (ROI roi : selectedROIs)
                        newROIs.add(ROIUtil.getDownscaled(roi, true));

                    if (!newROIs.isEmpty())
                    {
                        for (ROI roi : newROIs)
                            sequence.addROI(roi);

                        sequence.addUndoableEdit(new ROIAddsSequenceEdit(sequence, newROIs,
                                (newROIs.size() > 1) ? "ROIs scale /2" : "ROI scale /2"));
                    }
                }
                catch (UnsupportedOperationException ex)
                {
                    MessageDialog.showDialog("Operation not supported", ex.toString(), MessageDialog.ERROR_MESSAGE);
                }
                finally
                {
                    sequence.endUpdate();
                }

                return true;
            }

            return false;
        }

        @Override
        public boolean isEnabled()
        {
            return super.isEnabled() && (Icy.getMainInterface().getActiveSequence() != null);
        }
    };

    public static IcyAbstractAction autoSplitAction = new IcyAbstractAction("Auto split",
            new IcyIcon("split_roi", true), "Automatic split selected ROI",
            "Automatic split selected ROI using shape and size information.")
    {
        @Override
        public boolean doAction(ActionEvent e)
        {
            final Sequence sequence = Icy.getMainInterface().getActiveSequence();

            if (sequence != null)
            {
                sequence.beginUpdate();
                try
                {
                    final List<ROI2D> selectedROIs = sequence.getSelectedROI2Ds();
                    final List<ROI> removedROIs = new ArrayList<ROI>();
                    final List<ROI> addedROIs = new ArrayList<ROI>();

                    for (ROI2D roi : selectedROIs)
                    {
                        // --> TODO
                        // final List<ROI> components = ROIUtil.split(roi);
                        //
                        // nothing to do if we obtain only 1 component
                        // if (components.size() > 1)
                        // {
                        // sequence.removeROI(roi);
                        // removedROIs.add(roi);
                        //
                        // for (ROI component : components)
                        // {
                        // sequence.addROI(component);
                        // // add to undo manager
                        // addedROIs.add(component);
                        // }
                        // }
                    }

                    if (!removedROIs.isEmpty())
                        sequence.addUndoableEdit(new ROIReplacesSequenceEdit(sequence, removedROIs, addedROIs,
                                (removedROIs.size() > 1) ? "ROIs automatic split" : "ROI automatic split"));
                }
                catch (UnsupportedOperationException ex)
                {
                    MessageDialog.showDialog("Operation not supported", ex.toString(), MessageDialog.ERROR_MESSAGE);
                }
                finally
                {
                    sequence.endUpdate();
                }

                return true;
            }

            return false;
        }

        @Override
        public boolean isEnabled()
        {
            return super.isEnabled() && (Icy.getMainInterface().getActiveSequence() != null);
        }
    };

    /**
     * Return all actions of this class
     */
    public static List<IcyAbstractAction> getAllActions()
    {
        final List<IcyAbstractAction> result = new ArrayList<IcyAbstractAction>();

        for (Field field : RoiActions.class.getFields())
        {
            final Class<?> type = field.getType();

            try
            {
                if (ClassUtil.isSubClass(type, IcyAbstractAction[].class))
                    result.addAll(Arrays.asList(((IcyAbstractAction[]) field.get(null))));
                else if (ClassUtil.isSubClass(type, IcyAbstractAction.class))
                    result.add((IcyAbstractAction) field.get(null));
            }
            catch (Exception e)
            {
                // ignore
            }
        }

        return result;
    }
}
