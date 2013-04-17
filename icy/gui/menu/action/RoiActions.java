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
package icy.gui.menu.action;

import icy.clipboard.Clipboard;
import icy.common.IcyAbstractAction;
import icy.gui.dialog.LoadDialog;
import icy.gui.dialog.SaveDialog;
import icy.gui.inspector.RoisPanel;
import icy.main.Icy;
import icy.resource.ResourceUtil;
import icy.resource.icon.IcyIcon;
import icy.roi.ROI;
import icy.roi.ROI2D;
import icy.roi.ROI2DRectangle;
import icy.sequence.Sequence;
import icy.util.ShapeUtil.ShapeOperation;
import icy.util.XMLUtil;

import java.awt.event.ActionEvent;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.w3c.dom.Document;

/**
 * Roi actions (open / save / copy / paste / merge...)
 * 
 * @author Stephane
 */
public class RoiActions
{
    public static final String DEFAULT_ROI_DIR = "roi";
    public static final String DEFAULT_ROI_NAME = "roi.xml";

    public static final String ID_ROI_COPY_CLIPBOARD = "RoiCopyClipboardCommand";

    public static IcyAbstractAction loadAction = new IcyAbstractAction("Load", new IcyIcon(ResourceUtil.ICON_OPEN),
            "Load ROI from file")
    {
        /**
         * 
         */
        private static final long serialVersionUID = 2378084039864016238L;

        @Override
        public void doAction(ActionEvent e)
        {
            final String filename = LoadDialog.chooseFile("Load roi(s)...", DEFAULT_ROI_DIR, DEFAULT_ROI_NAME);
            final Sequence sequence = Icy.getMainInterface().getFocusedSequence();

            if ((filename != null) && (sequence != null))
            {
                final Document doc = XMLUtil.loadDocument(filename);

                if (doc != null)
                {
                    final List<ROI> rois = ROI.getROIsFromXML(XMLUtil.getRootElement(doc));

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
                }
            }
        }

        @Override
        public boolean isEnabled()
        {
            return !processing && (Icy.getMainInterface().getFocusedSequence() != null);
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
        public void doAction(ActionEvent e)
        {
            final String filename = SaveDialog.chooseFile("Save roi(s)...", DEFAULT_ROI_DIR, DEFAULT_ROI_NAME);
            final Sequence sequence = Icy.getMainInterface().getFocusedSequence();
            final RoisPanel roisPanel = Icy.getMainInterface().getRoisPanel();

            if ((filename != null) && (sequence != null) && (roisPanel != null))
            {
                final List<ROI> rois = roisPanel.getSelectedRois();

                if (rois.size() > 0)
                {
                    final Document doc = XMLUtil.createDocument(true);

                    if (doc != null)
                    {
                        ROI.setROIsToXML(XMLUtil.getRootElement(doc), rois);
                        XMLUtil.saveDocument(doc, filename);
                    }
                }
            }
        }

        @Override
        public boolean isEnabled()
        {
            return !processing && (Icy.getMainInterface().getFocusedSequence() != null);
        }
    };

    public static IcyAbstractAction copyAction = new IcyAbstractAction("Copy", new IcyIcon(ResourceUtil.ICON_COPY),
            "Copy selected ROI to clipboard")
    {
        /**
         * 
         */
        private static final long serialVersionUID = -4716027958152503425L;

        @Override
        public void doAction(ActionEvent e)
        {
            final RoisPanel roisPanel = Icy.getMainInterface().getRoisPanel();

            if (roisPanel != null)
            {
                // remove previous ROI from clipboard
                Clipboard.remove(ID_ROI_COPY_CLIPBOARD, false);

                final List<ROI> rois = roisPanel.getSelectedRois();

                for (ROI roi : rois)
                    Clipboard.put(roi.getCopy(), ID_ROI_COPY_CLIPBOARD);

                pasteAction.setEnabled(rois.size() > 0);
            }
        }

        @Override
        public boolean isEnabled()
        {
            return !processing && (Icy.getMainInterface().getFocusedSequence() != null);
        }
    };

    public static IcyAbstractAction pasteAction = new IcyAbstractAction("Paste", new IcyIcon(ResourceUtil.ICON_PASTE),
            "Paste ROI from clipboard")
    {
        /**
         * 
         */
        private static final long serialVersionUID = 4878585451006567513L;

        @Override
        public void doAction(ActionEvent e)
        {
            final Sequence sequence = Icy.getMainInterface().getFocusedSequence();

            if (sequence != null)
            {
                final List<Object> rois = Clipboard.get(ID_ROI_COPY_CLIPBOARD, false);

                sequence.beginUpdate();
                try
                {
                    // add to sequence
                    for (Object roi : rois)
                        if (roi instanceof ROI)
                            sequence.addROI((ROI) roi);
                }
                finally
                {
                    sequence.endUpdate();
                }
            }
        }

        @Override
        public boolean isEnabled()
        {
            return !processing && (Icy.getMainInterface().getFocusedSequence() != null)
                    && Clipboard.hasObjects(RoiActions.ID_ROI_COPY_CLIPBOARD, false);
        }
    };

    public static IcyAbstractAction clearClipboardAction = new IcyAbstractAction("Clear", new IcyIcon(
            ResourceUtil.ICON_CLIPBOARD_CLEAR), "Remove ROI saved in clipboard")
    {
        /**
         * 
         */
        private static final long serialVersionUID = 4878585451006567513L;

        @Override
        public void doAction(ActionEvent e)
        {
            Clipboard.remove(ID_ROI_COPY_CLIPBOARD, false);
            pasteAction.setEnabled(false);
        }

        @Override
        public boolean isEnabled()
        {
            return !processing && (Icy.getMainInterface().getFocusedSequence() != null)
                    && Clipboard.hasObjects(RoiActions.ID_ROI_COPY_CLIPBOARD, false);
        }
    };

    public static IcyAbstractAction selectAllAction = new IcyAbstractAction("SelectAll", (IcyIcon) null,
            "Select all ROI(s)")
    {
        @Override
        public void doAction(ActionEvent e)
        {
            final Sequence sequence = Icy.getMainInterface().getFocusedSequence();

            if (sequence != null)
                sequence.setSelectedROIs(sequence.getROIs());
        }

        @Override
        public boolean isEnabled()
        {
            final Sequence sequence = Icy.getMainInterface().getFocusedSequence();

            return !processing && (sequence != null) && (sequence.getROIs().size() > 0);
        }
    };

    public static IcyAbstractAction unselectAction = new IcyAbstractAction("Unselect", (IcyIcon) null,
            "Unselect ROI(s)")
    {
        @Override
        public void doAction(ActionEvent e)
        {
            final Sequence sequence = Icy.getMainInterface().getFocusedSequence();

            if (sequence != null)
                sequence.setSelectedROI(null, false);
        }

        @Override
        public boolean isEnabled()
        {
            return !processing && (Icy.getMainInterface().getFocusedSequence() != null);
        }
    };

    public static IcyAbstractAction deleteAction = new IcyAbstractAction("Delete",
            new IcyIcon(ResourceUtil.ICON_DELETE), "Delete selected ROI(s)")
    {
        /**
         * 
         */
        private static final long serialVersionUID = 9079403002834893222L;

        @Override
        public void doAction(ActionEvent e)
        {
            final Sequence sequence = Icy.getMainInterface().getFocusedSequence();
            final RoisPanel roisPanel = Icy.getMainInterface().getRoisPanel();

            if ((sequence != null) && (roisPanel != null))
            {
                sequence.beginUpdate();
                try
                {
                    // delete selected rois
                    for (ROI roi : roisPanel.getSelectedRois())
                        if (roi.isEditable())
                            sequence.removeROI(roi);
                }
                finally
                {
                    sequence.endUpdate();
                }
            }
        }

        @Override
        public boolean isEnabled()
        {
            return !processing && (Icy.getMainInterface().getFocusedSequence() != null);
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
        public void doAction(ActionEvent e)
        {
            final Sequence sequence = Icy.getMainInterface().getFocusedSequence();
            final RoisPanel roisPanel = Icy.getMainInterface().getRoisPanel();

            if ((sequence != null) && (roisPanel != null))
            {
                // NOT operation
                sequence.beginUpdate();
                try
                {
                    final ArrayList<ROI> selectedROI = roisPanel.getSelectedRois();
                    // only ROI2D supported now
                    final ROI2D[] selectedROI2D = ROI2D.getROI2DList(selectedROI.toArray(new ROI[selectedROI.size()]));

                    // NOT work only on single ROI
                    if (selectedROI2D.length != 1)
                        return;

                    // we do the NOT operation by subtracting current ROI to sequence bounds ROI
                    final ROI mergeROI = ROI2D.subtract(new ROI2DRectangle(sequence.getBounds()), selectedROI2D[0]);
                    mergeROI.setName("Inverse");

                    sequence.addROI(mergeROI);
                    sequence.setSelectedROI(mergeROI, true);
                }
                finally
                {
                    sequence.endUpdate();
                }
            }
        }

        @Override
        public boolean isEnabled()
        {
            return !processing && (Icy.getMainInterface().getFocusedSequence() != null);
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
        public void doAction(ActionEvent e)
        {
            final Sequence sequence = Icy.getMainInterface().getFocusedSequence();
            final RoisPanel roisPanel = Icy.getMainInterface().getRoisPanel();

            if ((sequence != null) && (roisPanel != null))
            {
                // OR operation
                sequence.beginUpdate();
                try
                {
                    final ArrayList<ROI> selectedROI = roisPanel.getSelectedRois();
                    // only ROI2D supported now
                    final ROI2D[] selectedROI2D = ROI2D.getROI2DList(selectedROI.toArray(new ROI[selectedROI.size()]));

                    final ROI mergeROI = ROI2D.merge(selectedROI2D, ShapeOperation.OR);

                    sequence.addROI(mergeROI);
                    sequence.setSelectedROI(mergeROI, true);
                }
                finally
                {
                    sequence.endUpdate();
                }
            }
        }

        @Override
        public boolean isEnabled()
        {
            return !processing && (Icy.getMainInterface().getFocusedSequence() != null);
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
        public void doAction(ActionEvent e)
        {
            final Sequence sequence = Icy.getMainInterface().getFocusedSequence();
            final RoisPanel roisPanel = Icy.getMainInterface().getRoisPanel();

            if ((sequence != null) && (roisPanel != null))
            {
                // AND operation
                sequence.beginUpdate();
                try
                {
                    final ArrayList<ROI> selectedROI = roisPanel.getSelectedRois();
                    // only ROI2D supported now
                    final ROI2D[] selectedROI2D = ROI2D.getROI2DList(selectedROI.toArray(new ROI[selectedROI.size()]));

                    final ROI mergeROI = ROI2D.merge(selectedROI2D, ShapeOperation.AND);

                    sequence.addROI(mergeROI);
                    sequence.setSelectedROI(mergeROI, true);
                }
                finally
                {
                    sequence.endUpdate();
                }
            }
        }

        @Override
        public boolean isEnabled()
        {
            return !processing && (Icy.getMainInterface().getFocusedSequence() != null);
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
        public void doAction(ActionEvent e)
        {
            final Sequence sequence = Icy.getMainInterface().getFocusedSequence();
            final RoisPanel roisPanel = Icy.getMainInterface().getRoisPanel();

            if ((sequence != null) && (roisPanel != null))
            {
                // XOR operation
                sequence.beginUpdate();
                try
                {
                    final ArrayList<ROI> selectedROI = roisPanel.getSelectedRois();
                    // only ROI2D supported now
                    final ROI2D[] selectedROI2D = ROI2D.getROI2DList(selectedROI.toArray(new ROI[selectedROI.size()]));

                    final ROI mergeROI = ROI2D.merge(selectedROI2D, ShapeOperation.XOR);

                    sequence.addROI(mergeROI);
                    sequence.setSelectedROI(mergeROI, true);
                }
                finally
                {
                    sequence.endUpdate();
                }
            }
        }

        @Override
        public boolean isEnabled()
        {
            return !processing && (Icy.getMainInterface().getFocusedSequence() != null);
        }
    };

    public static IcyAbstractAction boolSubtractAction = new IcyAbstractAction("SUBTRACT", new IcyIcon(
            ResourceUtil.ICON_ROI_SUB), "Boolean subtraction",
            "Create a new ROI representing the subtraction of second ROI from the first ROI")
    {
        /**
         * 
         */
        private static final long serialVersionUID = 9094641559971542667L;

        @Override
        public void doAction(ActionEvent e)
        {
            final Sequence sequence = Icy.getMainInterface().getFocusedSequence();
            final RoisPanel roisPanel = Icy.getMainInterface().getRoisPanel();

            if ((sequence != null) && (roisPanel != null))
            {
                // SUB operation
                sequence.beginUpdate();
                try
                {
                    final ArrayList<ROI> selectedROI = roisPanel.getSelectedRois();
                    // only ROI2D supported now
                    final ROI2D[] selectedROI2D = ROI2D.getROI2DList(selectedROI.toArray(new ROI[selectedROI.size()]));

                    // Subtraction work only when 2 ROI are selected
                    if (selectedROI2D.length != 2)
                        return;

                    final ROI mergeROI = ROI2D.subtract(selectedROI2D[0], selectedROI2D[1]);

                    sequence.addROI(mergeROI);
                    sequence.setSelectedROI(mergeROI, true);
                }
                finally
                {
                    sequence.endUpdate();
                }
            }
        }

        @Override
        public boolean isEnabled()
        {
            return !processing && (Icy.getMainInterface().getFocusedSequence() != null);
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
