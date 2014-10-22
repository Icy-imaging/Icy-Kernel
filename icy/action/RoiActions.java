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
package icy.action;

import icy.clipboard.Clipboard;
import icy.file.FileUtil;
import icy.gui.dialog.MessageDialog;
import icy.gui.dialog.OpenDialog;
import icy.gui.dialog.SaveDialog;
import icy.gui.inspector.RoisPanel;
import icy.main.Icy;
import icy.preferences.GeneralPreferences;
import icy.resource.ResourceUtil;
import icy.resource.icon.IcyIcon;
import icy.roi.ROI;
import icy.roi.ROIUtil;
import icy.sequence.Sequence;
import icy.sequence.edit.ROIAddSequenceEdit;
import icy.sequence.edit.ROIAddsSequenceEdit;
import icy.system.SystemUtil;
import icy.util.StringUtil;
import icy.util.XLSUtil;
import icy.util.XMLUtil;

import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

import org.w3c.dom.Document;

import plugins.kernel.roi.roi2d.ROI2DRectangle;

/**
 * Roi actions (open / save / copy / paste / merge...)
 * 
 * @author Stephane
 */
public class RoiActions
{
    public static final String DEFAULT_ROI_DIR = "roi";
    public static final String DEFAULT_ROI_NAME = "roi.xml";

    public static IcyAbstractAction loadAction = new IcyAbstractAction("Load", new IcyIcon(ResourceUtil.ICON_OPEN),
            "Load ROI from file")
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

    public static IcyAbstractAction saveAction = new IcyAbstractAction("Save", new IcyIcon(ResourceUtil.ICON_SAVE),
            "Save selected ROI to file")
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
                        rois.set(i, rois.get(i).getCopy());

                    // save in the Icy clipboard
                    Clipboard.put(Clipboard.TYPE_ROILIST, rois);
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

    public static IcyAbstractAction copyLinkAction = new IcyAbstractAction("Copy link", new IcyIcon(
            ResourceUtil.ICON_LINK_COPY), "Copy link of selected ROI to clipboard (Alt+C)", KeyEvent.VK_C,
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
                @SuppressWarnings("unchecked")
                final List<ROI> rois = (List<ROI>) Clipboard.get(Clipboard.TYPE_ROILIST);

                if ((rois != null) && (rois.size() > 0))
                {
                    sequence.beginUpdate();
                    try
                    {
                        // unselect all rois
                        sequence.setSelectedROI(null);

                        // add copy to sequence (so we can do the paste operation severals time)
                        for (ROI roi : rois)
                        {
                            final ROI newROI = roi.getCopy();
                            // select the ROI
                            newROI.setSelected(true);
                            // and add it
                            sequence.addROI(newROI);
                        }
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
                    && Clipboard.getType().equals(Clipboard.TYPE_ROILIST);
        }
    };

    public static IcyAbstractAction pasteLinkAction = new IcyAbstractAction("Paste link", new IcyIcon(
            ResourceUtil.ICON_LINK_PASTE), "Paste ROI link from clipboard (Alt+V)", KeyEvent.VK_V, InputEvent.ALT_MASK)
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
            new IcyIcon(ResourceUtil.ICON_DELETE), "Delete selected ROI(s)", KeyEvent.VK_DELETE)
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

    public static IcyAbstractAction boolNotAction = new IcyAbstractAction("NOT",
            new IcyIcon(ResourceUtil.ICON_ROI_NOT), "Boolean NOT operation",
            "Create a new ROI representing the inverse of selected ROI")
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

                    // NOT work only on single ROI
                    if (selectedROI.size() != 1)
                        return false;

                    // we do the NOT operation by subtracting current ROI to sequence bounds ROI
                    final ROI mergeROI = ROIUtil.subtract(new ROI2DRectangle(sequence.getBounds2D()),
                            selectedROI.get(0));

                    if (mergeROI != null)
                    {
                        mergeROI.setName("Inverse");

                        sequence.addROI(mergeROI);
                        sequence.setSelectedROI(mergeROI);

                        // add to undo manager
                        sequence.addUndoableEdit(new ROIAddSequenceEdit(sequence, mergeROI, "ROI Inverse"));
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

    public static IcyAbstractAction boolOrAction = new IcyAbstractAction("OR", new IcyIcon(ResourceUtil.ICON_ROI_OR),
            "Boolean OR operation", "Create a new ROI representing the union of selected ROIs")
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

    public static IcyAbstractAction boolAndAction = new IcyAbstractAction("AND",
            new IcyIcon(ResourceUtil.ICON_ROI_AND), "Boolean AND operation",
            "Create a new ROI representing the intersection of selected ROIs")
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

    public static IcyAbstractAction boolXorAction = new IcyAbstractAction("XOR",
            new IcyIcon(ResourceUtil.ICON_ROI_XOR), "Boolean XOR operation",
            "Create a new ROI representing the exclusive union of selected ROIs")
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

    public static IcyAbstractAction boolSubtractAction = new IcyAbstractAction("SUBTRACT", new IcyIcon(
            ResourceUtil.ICON_ROI_SUB), "Boolean subtraction",
            "Create 2 ROIs representing the result of (A - B) and (B - A)")
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

    public static IcyAbstractAction xlsExportAction = new IcyAbstractAction("Export", new IcyIcon(
            ResourceUtil.ICON_XLS_EXPORT), "ROI Excel export", "Export all ROI informations in XLS file")
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

                if (StringUtil.isEmpty(content))
                {
                    MessageDialog.showDialog("Nothing to export !", MessageDialog.INFORMATION_MESSAGE);
                    return true;
                }

                // get the global result folder
                final String dir = GeneralPreferences.getResultFolder();
                // create it if needed
                FileUtil.createDir(dir);

                final String filename = SaveDialog.chooseFile("Export ROIs...", dir, "result", ".xls");

                if (filename != null)
                {
                    // update result folder
                    GeneralPreferences.setResultFolder(FileUtil.getDirectory(filename));

                    // CSV format wanted ?
                    if (FileUtil.getFileExtension(filename, false).equalsIgnoreCase("csv"))
                    {
                        try
                        {
                            // just write CSV content
                            final PrintWriter out = new PrintWriter(filename);
                            out.println(content);
                            out.close();
                        }
                        catch (FileNotFoundException e1)
                        {
                            MessageDialog.showDialog("Error", e1.getMessage(), MessageDialog.ERROR_MESSAGE);
                        }
                    }
                    // XLS export
                    else
                    {
                        try
                        {
                            final WritableWorkbook workbook = XLSUtil.createWorkbook(filename);
                            final WritableSheet sheet = XLSUtil.createNewPage(workbook, "ROIS");
                            final BufferedReader br = new BufferedReader(new StringReader(content));

                            String line;
                            int y = 0;
                            while ((line = br.readLine()) != null)
                            {
                                int x = 0;

                                // use tab as separator
                                for (String col : line.split("\t"))
                                {
                                    XLSUtil.setCellString(sheet, x, y, col);
                                    x++;
                                }

                                y++;
                            }

                            XLSUtil.saveAndClose(workbook);
                        }
                        catch (Exception e1)
                        {
                            MessageDialog.showDialog("Error", e1.getMessage(), MessageDialog.ERROR_MESSAGE);
                        }
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
                if (type.isAssignableFrom(IcyAbstractAction[].class))
                    result.addAll(Arrays.asList(((IcyAbstractAction[]) field.get(null))));
                else if (type.isAssignableFrom(IcyAbstractAction.class))
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
