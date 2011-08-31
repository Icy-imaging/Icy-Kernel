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
package icy.gui.util;

import icy.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

import org.pushingpixels.flamingo.api.common.AbstractCommandButton;
import org.pushingpixels.flamingo.api.ribbon.AbstractRibbonBand;
import org.pushingpixels.flamingo.api.ribbon.JFlowRibbonBand;
import org.pushingpixels.flamingo.api.ribbon.JRibbon;
import org.pushingpixels.flamingo.api.ribbon.JRibbonBand;
import org.pushingpixels.flamingo.api.ribbon.RibbonElementPriority;
import org.pushingpixels.flamingo.api.ribbon.RibbonTask;
import org.pushingpixels.flamingo.api.ribbon.resize.CoreRibbonResizePolicies.FlowThreeRows;
import org.pushingpixels.flamingo.api.ribbon.resize.CoreRibbonResizePolicies.FlowTwoRows;
import org.pushingpixels.flamingo.api.ribbon.resize.CoreRibbonResizePolicies.High2Low;
import org.pushingpixels.flamingo.api.ribbon.resize.CoreRibbonResizePolicies.High2Mid;
import org.pushingpixels.flamingo.api.ribbon.resize.CoreRibbonResizePolicies.Low2Mid;
import org.pushingpixels.flamingo.api.ribbon.resize.CoreRibbonResizePolicies.Mid2Low;
import org.pushingpixels.flamingo.api.ribbon.resize.CoreRibbonResizePolicies.Mid2Mid;
import org.pushingpixels.flamingo.api.ribbon.resize.CoreRibbonResizePolicies.Mirror;
import org.pushingpixels.flamingo.api.ribbon.resize.CoreRibbonResizePolicies.None;
import org.pushingpixels.flamingo.api.ribbon.resize.IconRibbonBandResizePolicy;
import org.pushingpixels.flamingo.api.ribbon.resize.RibbonBandResizePolicy;
import org.pushingpixels.flamingo.internal.ui.ribbon.JBandControlPanel;
import org.pushingpixels.flamingo.internal.ui.ribbon.JBandControlPanel.ControlPanelGroup;
import org.pushingpixels.flamingo.internal.ui.ribbon.JFlowBandControlPanel;

/**
 * @author Stephane
 */
public class RibbonUtil
{
    private static final int DEFAULT_HEIGHT = 48;
    private static final int DEFAULT_GAP = 4;

    // High2Low --> LOW, LOW, LOW
    // High2Mid --> MED, LOW, LOW
    // Mid2Low --> TOP, LOW, LOW
    // Mirror --> TOP, MED, LOW
    // Mid2Mid --> TOP, MED, MED
    // Low2Mid --> TOP, TOP, MED
    // None --> TOP, TOP, TOP

    private static List<RibbonBandResizePolicy> getPermissiveResizePolicies(final JBandControlPanel controlPanel)
    {
        final ArrayList<RibbonBandResizePolicy> result = new ArrayList<RibbonBandResizePolicy>();

        final None none = new None(controlPanel);
        final Low2Mid low2Mid = new Low2Mid(controlPanel);
        final Mid2Mid mid2Mid = new Mid2Mid(controlPanel);
        final Mirror mirror = new Mirror(controlPanel);
        final Mid2Low mid2Low = new Mid2Low(controlPanel);
        final High2Mid high2Mid = new High2Mid(controlPanel);
        final High2Low high2Low = new High2Low(controlPanel);
        final IconRibbonBandResizePolicy icon = new IconRibbonBandResizePolicy(controlPanel);

        final int noneW = none.getPreferredWidth(DEFAULT_HEIGHT, DEFAULT_GAP);
        final int low2MidW = low2Mid.getPreferredWidth(DEFAULT_HEIGHT, DEFAULT_GAP);
        final int mid2MidW = mid2Mid.getPreferredWidth(DEFAULT_HEIGHT, DEFAULT_GAP);
        final int mirrorW = mirror.getPreferredWidth(DEFAULT_HEIGHT, DEFAULT_GAP);
        final int mid2LowW = mid2Low.getPreferredWidth(DEFAULT_HEIGHT, DEFAULT_GAP);
        final int high2MidW = high2Mid.getPreferredWidth(DEFAULT_HEIGHT, DEFAULT_GAP);
        final int high2LowW = high2Low.getPreferredWidth(DEFAULT_HEIGHT, DEFAULT_GAP);
        final int iconW = icon.getPreferredWidth(DEFAULT_HEIGHT, DEFAULT_GAP);

        result.add(none);
        if (low2MidW < noneW)
            result.add(low2Mid);
        if (mid2MidW < low2MidW)
            result.add(mid2Mid);
        if (mirrorW < mid2MidW)
            result.add(mirror);
        if (mid2LowW < mirrorW)
            result.add(mid2Low);
        if (high2MidW < mid2LowW)
            result.add(high2Mid);
        if (high2LowW < high2MidW)
            result.add(high2Low);
        // icon resize policy should be in last position
        if (iconW < high2LowW)
            result.add(icon);

        return result;
    }

    private static List<RibbonBandResizePolicy> getRestrictiveResizePolicies(final JBandControlPanel controlPanel)
    {
        final ArrayList<RibbonBandResizePolicy> result = new ArrayList<RibbonBandResizePolicy>();

        final Mirror mirror = new Mirror(controlPanel);
        final Mid2Low mid2Low = new Mid2Low(controlPanel);
        final High2Mid high2Mid = new High2Mid(controlPanel);
        final High2Low high2Low = new High2Low(controlPanel);
        final IconRibbonBandResizePolicy icon = new IconRibbonBandResizePolicy(controlPanel);

        final int mirrorW = mirror.getPreferredWidth(DEFAULT_HEIGHT, DEFAULT_GAP);
        final int mid2LowW = mid2Low.getPreferredWidth(DEFAULT_HEIGHT, DEFAULT_GAP);
        final int high2MidW = high2Mid.getPreferredWidth(DEFAULT_HEIGHT, DEFAULT_GAP);
        final int high2LowW = high2Low.getPreferredWidth(DEFAULT_HEIGHT, DEFAULT_GAP);
        final int iconW = icon.getPreferredWidth(DEFAULT_HEIGHT, DEFAULT_GAP);

        result.add(mirror);
        if (mid2LowW < mirrorW)
            result.add(mid2Low);
        if (high2MidW < mid2LowW)
            result.add(high2Mid);
        if (high2LowW < high2MidW)
            result.add(high2Low);
        // icon resize policy should be in last position
        if (iconW < high2LowW)
            result.add(icon);

        return result;
    }

    private static List<RibbonBandResizePolicy> getFixedResizePolicies(JBandControlPanel controlPanel)
    {
        final ArrayList<RibbonBandResizePolicy> result = new ArrayList<RibbonBandResizePolicy>();

        result.add(new Mirror(controlPanel));

        return result;
    }

    private static List<RibbonBandResizePolicy> getRestrictiveResizePolicies(JFlowBandControlPanel controlPanel,
            int step)
    {
        final ArrayList<RibbonBandResizePolicy> result = new ArrayList<RibbonBandResizePolicy>();

        final FlowTwoRows twoRows = new FlowTwoRows(controlPanel);
        final FlowThreeRows threeRows = new FlowThreeRows(controlPanel);
        final IconRibbonBandResizePolicy icon = new IconRibbonBandResizePolicy(controlPanel);

        final int twoRowsW = twoRows.getPreferredWidth(DEFAULT_HEIGHT, DEFAULT_GAP);
        final int threeRowsW = threeRows.getPreferredWidth(DEFAULT_HEIGHT, DEFAULT_GAP);
        final int iconW = twoRows.getPreferredWidth(DEFAULT_HEIGHT, DEFAULT_GAP);

        if (twoRowsW > threeRowsW)
        {
            // add two rows policy first
            for (int i = 0; i < step; i++)
                result.add(new FlowTwoRows(controlPanel));
            for (int i = 0; i < step; i++)
                result.add(new FlowThreeRows(controlPanel));
            // icon resize policy should be in last position
            if (iconW < threeRowsW)
                result.add(icon);
        }
        else
        {
            // add three rows policy first
            for (int i = 0; i < step; i++)
                result.add(new FlowThreeRows(controlPanel));
            for (int i = 0; i < step; i++)
                result.add(new FlowTwoRows(controlPanel));
            // icon resize policy should be in last position
            if (iconW < twoRowsW)
                result.add(icon);
        }

        return result;
    }

    public static void setPermissiveResizePolicies(JRibbonBand band)
    {
        // equivalent to getCorePoliciesPermissive(band);
        band.setResizePolicies(getPermissiveResizePolicies(band.getControlPanel()));
    }

    public static void setRestrictiveResizePolicies(JRibbonBand band)
    {
        // equivalent to getCorePoliciesRestrictive(band);
        band.setResizePolicies(getRestrictiveResizePolicies(band.getControlPanel()));
    }

    public static void setFixedResizePolicies(JRibbonBand band)
    {
        // equivalent to getCorePoliciesNone(band);
        band.setResizePolicies(getFixedResizePolicies(band.getControlPanel()));
    }

    public static void setRestrictiveResizePolicies(JFlowRibbonBand band, int step)
    {
        // equivalent to getCoreFlowPoliciesRestrictive(band, step);
        band.setResizePolicies(getRestrictiveResizePolicies(band.getControlPanel(), step));
    }

    public static ArrayList<RibbonTask> getTasks(JRibbon ribbon)
    {
        final ArrayList<RibbonTask> result = new ArrayList<RibbonTask>();

        if (ribbon != null)
        {
            final int taskCount = ribbon.getTaskCount();

            for (int i = 0; i < taskCount; i++)
                result.add(ribbon.getTask(i));
        }

        return result;
    }

    public static RibbonTask getTask(JRibbon ribbon, String name)
    {
        if (ribbon != null)
        {
            final int taskCount = ribbon.getTaskCount();

            for (int i = 0; i < taskCount; i++)
            {
                final RibbonTask ribbonTask = ribbon.getTask(i);

                if (StringUtil.equals(ribbonTask.getTitle(), name))
                    return ribbonTask;
            }
        }

        return null;
    }

    public static JRibbonBand getBand(RibbonTask ribbonTask, String name)
    {
        if (ribbonTask != null)
        {
            for (AbstractRibbonBand<?> ribbonBand : ribbonTask.getBands())
            {
                if (StringUtil.equals(ribbonBand.getTitle(), name))
                    if (ribbonBand instanceof JRibbonBand)
                        return (JRibbonBand) ribbonBand;
            }
        }

        return null;
    }

    public static JFlowRibbonBand getFlowBand(RibbonTask ribbonTask, String name)
    {
        if (ribbonTask != null)
        {
            for (AbstractRibbonBand<?> ribbonBand : ribbonTask.getBands())
            {
                if (StringUtil.equals(ribbonBand.getTitle(), name))
                    if (ribbonBand instanceof JFlowRibbonBand)
                        return (JFlowRibbonBand) ribbonBand;
            }
        }

        return null;
    }

    public static RibbonElementPriority getButtonPriority(JRibbonBand band, AbstractCommandButton button)
    {
        final JBandControlPanel controlPanel = band.getControlPanel();

        if (controlPanel != null)
        {
            for (ControlPanelGroup panelGroup : controlPanel.getControlPanelGroups())
            {
                for (AbstractCommandButton b : panelGroup.getRibbonButtons(RibbonElementPriority.LOW))
                    if (b == button)
                        return RibbonElementPriority.LOW;

                for (AbstractCommandButton b : panelGroup.getRibbonButtons(RibbonElementPriority.MEDIUM))
                    if (b == button)
                        return RibbonElementPriority.MEDIUM;

                for (AbstractCommandButton b : panelGroup.getRibbonButtons(RibbonElementPriority.TOP))
                    if (b == button)
                        return RibbonElementPriority.TOP;
            }
        }

        // return LOW if not found
        return RibbonElementPriority.LOW;
    }

    public static int getButtonPosition(JRibbonBand band, AbstractCommandButton button)
    {
        int result = 0;

        final JBandControlPanel controlPanel = band.getControlPanel();

        if (controlPanel != null)
        {
            for (ControlPanelGroup panelGroup : controlPanel.getControlPanelGroups())
            {
                for (AbstractCommandButton b : panelGroup.getRibbonButtons(RibbonElementPriority.LOW))
                {
                    if (b == button)
                        return result;
                    result++;
                }

                for (AbstractCommandButton b : panelGroup.getRibbonButtons(RibbonElementPriority.MEDIUM))
                {
                    if (b == button)
                        return result;
                    result++;
                }

                for (AbstractCommandButton b : panelGroup.getRibbonButtons(RibbonElementPriority.TOP))
                {
                    if (b == button)
                        return result;
                    result++;
                }
            }
        }

        // return -1 if not found
        return -1;
    }

    public static ArrayList<AbstractCommandButton> getButtons(JRibbonBand band)
    {
        final ArrayList<AbstractCommandButton> result = new ArrayList<AbstractCommandButton>();

        final JBandControlPanel controlPanel = band.getControlPanel();

        if (controlPanel != null)
        {
            for (ControlPanelGroup panelGroup : controlPanel.getControlPanelGroups())
            {
                result.addAll(panelGroup.getRibbonButtons(RibbonElementPriority.LOW));
                result.addAll(panelGroup.getRibbonButtons(RibbonElementPriority.MEDIUM));
                result.addAll(panelGroup.getRibbonButtons(RibbonElementPriority.TOP));
            }
        }

        return result;
    }

    private static List<AbstractCommandButton> findButtonList(JRibbonBand band, String name)
    {
        List<AbstractCommandButton> result;

        final JBandControlPanel controlPanel = band.getControlPanel();

        if (controlPanel != null)
        {
            for (ControlPanelGroup panelGroup : controlPanel.getControlPanelGroups())
            {
                result = panelGroup.getRibbonButtons(RibbonElementPriority.LOW);
                if (findButton(result, name) != null)
                    return result;
                result = panelGroup.getRibbonButtons(RibbonElementPriority.MEDIUM);
                if (findButton(result, name) != null)
                    return result;
                result = panelGroup.getRibbonButtons(RibbonElementPriority.TOP);
                if (findButton(result, name) != null)
                    return result;
            }
        }

        return null;
    }

    public static AbstractCommandButton findButton(List<AbstractCommandButton> buttons, String name)
    {
        for (AbstractCommandButton button : buttons)
            if (StringUtil.equals(button.getName(), name))
                return button;

        return null;
    }

    public static AbstractCommandButton findButton(JRibbonBand band, String name)
    {
        return findButton(getButtons(band), name);
    }

    /**
     * ! Ribbon doesn't support button removing so use it at your own risk !
     */
    private static void removeButton(JBandControlPanel bandControlPanel, List<AbstractCommandButton> buttons,
            AbstractCommandButton button)
    {
        if ((bandControlPanel != null) && (buttons != null) && (button != null))
        {
            // directly remove from the internal list
            buttons.remove(button);
            // remove from container
            bandControlPanel.remove(button);
            bandControlPanel.validate();

            // rebuild resize policies (used only for plugins so we use the restrictive one)
            if (bandControlPanel.getRibbonBand() instanceof JRibbonBand)
                setRestrictiveResizePolicies((JRibbonBand) bandControlPanel.getRibbonBand());
        }
    }

    /**
     * ! Ribbon doesn't support button removing so use it at your own risk !
     */
    public static void removeButton(JRibbonBand band, String name)
    {
        // we get the internal list reference
        final List<AbstractCommandButton> buttons = findButtonList(band, name);

        if (buttons != null)
            removeButton(band.getControlPanel(), buttons, findButton(buttons, name));
    }
}
