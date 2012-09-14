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

import icy.common.listener.weak.WeakListener;
import icy.image.ImageUtil;
import icy.preferences.GeneralPreferences;
import icy.system.IcyExceptionHandler;
import icy.system.SystemUtil;
import icy.system.thread.ThreadUtil;
import icy.util.ClassUtil;
import icy.util.ReflectionUtil;
import icy.util.StringUtil;
import ij.util.Java2;

import java.awt.Color;
import java.awt.Component;
import java.awt.Image;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Map;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.InternalFrameUI;

import org.pushingpixels.flamingo.api.common.CommandToggleButtonGroup;
import org.pushingpixels.flamingo.api.common.JCommandToggleMenuButton;
import org.pushingpixels.flamingo.api.common.popup.JCommandPopupMenu;
import org.pushingpixels.substance.api.ComponentState;
import org.pushingpixels.substance.api.DecorationAreaType;
import org.pushingpixels.substance.api.SubstanceColorScheme;
import org.pushingpixels.substance.api.SubstanceLookAndFeel;
import org.pushingpixels.substance.api.SubstanceSkin;
import org.pushingpixels.substance.api.fonts.FontPolicy;
import org.pushingpixels.substance.api.fonts.FontSet;
import org.pushingpixels.substance.api.fonts.SubstanceFontUtilities;
import org.pushingpixels.substance.api.skin.SkinChangeListener;
import org.pushingpixels.substance.api.skin.SkinInfo;
import org.pushingpixels.substance.internal.ui.SubstanceInternalFrameUI;
import org.pushingpixels.substance.internal.utils.SubstanceColorSchemeUtilities;
import org.pushingpixels.substance.internal.utils.SubstanceInternalFrameTitlePane;
import org.pushingpixels.substance.internal.utils.SubstanceTitlePane;

/**
 * @author Stephane
 */
public class LookAndFeelUtil
{
    /**
     * Weak listener wrapper for SkinChangeListener.
     * 
     * @author Stephane
     */
    public static class WeakSkinChangeListener extends WeakListener<SkinChangeListener> implements SkinChangeListener
    {
        public WeakSkinChangeListener(SkinChangeListener listener)
        {
            super(listener);
        }

        @Override
        public void removeListener(Object source)
        {
            removeSkinChangeListener(this);
        }

        @Override
        public void skinChanged()
        {
            final SkinChangeListener listener = getListener();

            if (listener != null)
                listener.skinChanged();
        }
    }

    static int defaultFontSize;
    static Map<String, SkinInfo> map;
    private static ArrayList<SkinInfo> skins;

    static int currentFontSize;

    public static void init()
    {
        try
        {
            // so ImageJ won't change look and feel later
            ReflectionUtil.getField(Java2.class, "lookAndFeelSet", true).set(null, Boolean.valueOf(true));
        }
        catch (Exception e)
        {
            // do it in another way (slower)
            Java2.setSystemLookAndFeel();
        }

        // enable or not EDT checking in substance
        SystemUtil.setProperty("insubstantial.checkEDT", "false");
        SystemUtil.setProperty("insubstantial.logEDT", "true");

        // funny features of substance

        // AnimationConfigurationManager.getInstance().allowAnimations(AnimationFacet.FOCUS_LOOP_ANIMATION);
        // AnimationConfigurationManager.getInstance().allowAnimations(AnimationFacet.GHOSTING_BUTTON_PRESS);
        // AnimationConfigurationManager.getInstance().allowAnimations(AnimationFacet.GHOSTING_ICON_ROLLOVER);
        // AnimationConfigurationManager.getInstance().allowAnimations(AnimationFacet.ICON_GLOW);

        // UIManager.put(SubstanceLookAndFeel.USE_THEMED_DEFAULT_ICONS, Boolean.TRUE);
        // UIManager.put(SubstanceLookAndFeel.SHOW_EXTRA_WIDGETS, Boolean.TRUE);
        // UIManager.put(SubstanceLookAndFeel.WATERMARK_VISIBLE, Boolean.TRUE);

        // SubstanceWidgetManager.getInstance().register(null, true,
        // SubstanceWidgetType.TITLE_PANE_HEAP_STATUS);

        // enabled LAF decoration instead of native ones
        JFrame.setDefaultLookAndFeelDecorated(true);
        JDialog.setDefaultLookAndFeelDecorated(true);

        map = SubstanceLookAndFeel.getAllSkins();
        skins = new ArrayList<SkinInfo>(map.values());

        final LookAndFeelInfo[] lafInfos = new LookAndFeelInfo[skins.size()];

        // install Substance look and feel
        for (int i = 0; i < skins.size(); i++)
        {
            final SkinInfo skin = skins.get(i);
            final String className = skin.getClassName();
            final String simpleName = ClassUtil.getSimpleClassName(className);
            final int len = simpleName.length();
            // remove the "skin" suffix from simple name
            final String lafClassName = ClassUtil.getPackageName(className) + ".Substance"
                    + simpleName.substring(0, len - 4) + "LookAndFeel";

            lafInfos[i] = new LookAndFeelInfo(skin.getDisplayName(), lafClassName);
        }

        // replace system LAF by substance LAF
        UIManager.setInstalledLookAndFeels(lafInfos);

        // get default LAF
        // String lookAndFeelToLoad = GeneralPreferences.getGuiLookAndFeel();

        // try
        // {
        // UIManager.setLookAndFeel(lookAndFeelToLoad);
        // }
        // catch (Exception e)
        // {
        // System.err.println("Look & Feel not found : " + lookAndFeelToLoad);
        // }

        // get default skin
        setSkin(GeneralPreferences.getGuiSkin());

        // get default font size
        final FontPolicy fontPolicy = SubstanceFontUtilities.getDefaultFontPolicy();
        final FontSet fontSet = fontPolicy.getFontSet("Substance", null);

        defaultFontSize = fontSet.getMessageFont().getSize();
        currentFontSize = defaultFontSize;

        // set default font size
        setFontSize(GeneralPreferences.getGuiFontSize());

        // Define custom PopupFactory :
        // That fix Ribbon repaint bugs on Medium Weight components for OSX
        // but that also add problem on JComboBox in JDialog (FIXME)
        // PopupFactory.setSharedInstance(new CustomPopupFactory());
    }

    /**
     * Add skin change listener (automatically add weak listener)
     */
    public static void addSkinChangeListener(SkinChangeListener listener)
    {
        SubstanceLookAndFeel.registerSkinChangeListener(listener);
    }

    /**
     * Remove skin change listener
     */
    public static void removeSkinChangeListener(SkinChangeListener listener)
    {
        SubstanceLookAndFeel.unregisterSkinChangeListener(listener);
    }

    /**
     * get current Substance skin
     */
    public static SubstanceSkin getCurrentSkin()
    {
        return SubstanceLookAndFeel.getCurrentSkin();
    }

    /**
     * get current Substance skin display name
     */
    public static String getCurrentSkinName()
    {
        final SubstanceSkin skin = getCurrentSkin();

        if (skin != null)
            return skin.getDisplayName();

        return null;
    }

    // /**
    // * get current Look And Feel
    // */
    // public static LookAndFeel getLookAndFeel()
    // {
    // return UIManager.getLookAndFeel();
    // }
    //
    // /**
    // * get current Look And Feel name
    // */
    // public static String getLookAndFeelName()
    // {
    // final LookAndFeel laf = getLookAndFeel();
    //
    // if (laf instanceof SubstanceLookAndFeel)
    // return SubstanceLookAndFeel.getCurrentSkin().getDisplayName();
    //
    // return UIManager.getLookAndFeel().getName();
    // }

    public static JCommandPopupMenu getLookAndFeelMenu()
    {
        final JCommandPopupMenu result = new JCommandPopupMenu();

        final CommandToggleButtonGroup buttonGroup = new CommandToggleButtonGroup();
        final String currentSkinName = getCurrentSkinName();

        for (SkinInfo skin : skins)
        {
            final String skinName = skin.getDisplayName();

            final JCommandToggleMenuButton button = new JCommandToggleMenuButton(skinName, null);

            button.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    // set LAF
                    LookAndFeelUtil.setSkin(skinName);
                    // and save to preferences
                    GeneralPreferences.setGuiSkin(skinName);
                }
            });

            result.addMenuButton(button);

            // TODO: why this is needed ? ribbon bug ?
            button.getUI().installUI(button);

            buttonGroup.add(button);
            buttonGroup.setSelected(button, button.getText().equals(currentSkinName));
        }

        return result;
    }

    /**
     * get default Look And Feel font size
     */
    public static int getDefaultFontSize()
    {
        return defaultFontSize;
    }

    /**
     * get default Substance skin
     */
    public static String getDefaultSkin()
    {
        // should be Cerulean
        return skins.get(4).getDisplayName();
    }

    // /**
    // * get default Look And Feel
    // */
    // public static String getDefaultLookAndFeel()
    // {
    // return "org.pushingpixels.substance.api.skin.SubstanceCremeCoffeeLookAndFeel";
    // // return UIManager.getSystemLookAndFeelClassName();
    // }

    /**
     * Get current LookAndFeel font size
     */
    public static int getFontSize()
    {
        return currentFontSize;
    }

    /**
     * Set LookAndFeel font size
     */
    public static void setFontSize(final int size)
    {
        if (size != currentFontSize)
        {
            ThreadUtil.invokeLater(new Runnable()
            {
                @Override
                public void run()
                {
                    final float scaleFactor = (float) size / (float) defaultFontSize;

                    try
                    {
                        // will fail here if look and feel is not a substance one
                        SubstanceLookAndFeel.setFontPolicy(SubstanceFontUtilities.getScaledFontPolicy(scaleFactor));
                        currentFontSize = size;
                    }
                    catch (Exception e)
                    {
                        System.err.println("LookAndFeelUtil.setFontSize(...) error :");
                        IcyExceptionHandler.showErrorMessage(e, false);
                    }
                }
            });
        }
    }

    /**
     * Set the specified LookAndFeel skin (skin display name)
     */
    public static void setSkin(final String skinName)
    {
        if (!StringUtil.equals(skinName, getCurrentSkinName()))
        {
            ThreadUtil.invokeLater(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        SubstanceLookAndFeel.setSkin(map.get(skinName).getClassName());
                    }
                    catch (Exception e)
                    {
                        System.err.println("LookAndFeelUtil.setSkin(...) error :");
                        IcyExceptionHandler.showErrorMessage(e, false);
                    }
                }
            });
        }
    }

    /**
     * Return a foreground component color image depending original alpha intensity image
     */
    public static Image getForegroundImageFromAlphaImage(Component c, Image alphaImage)
    {
        return paintForegroundImageFromAlphaImage(c, alphaImage, null);
    }

    /**
     * Return a background component color image depending original alpha intensity image
     */
    public static Image getBackgroundImageFromAlphaImage(Component c, Image alphaImage)
    {
        return paintBackgroundImageFromAlphaImage(c, alphaImage, null);
    }

    /**
     * Paint foreground component color in 'out' image<br>
     * depending original alpha intensity from 'alphaImage'
     */
    public static Image paintForegroundImageFromAlphaImage(Component c, Image alphaImage, Image out)
    {
        return ImageUtil.paintColorImageFromAlphaImage(alphaImage, out, getForeground(c));
    }

    /**
     * Paint background component color in 'out' image<br>
     * depending original alpha intensity from 'alphaImage'
     */
    public static Image paintBackgroundImageFromAlphaImage(Component c, Image alphaImage, Image out)
    {
        return ImageUtil.paintColorImageFromAlphaImage(alphaImage, out, getBackground(c));
    }

    public static SubstanceColorScheme getActiveColorSheme(DecorationAreaType d)
    {
        return getCurrentSkin().getActiveColorScheme(d);
    }

    public static SubstanceColorScheme getBackgroundColorScheme(DecorationAreaType d)
    {
        return getCurrentSkin().getBackgroundColorScheme(d);
    }

    public static SubstanceColorScheme getDisabledColorScheme(DecorationAreaType d)
    {
        return getCurrentSkin().getDisabledColorScheme(d);
    }

    public static SubstanceColorScheme getEnabledColorScheme(DecorationAreaType d)
    {
        return getCurrentSkin().getDisabledColorScheme(d);
    }

    public static SubstanceSkin getSkin(Component c)
    {
        return SubstanceLookAndFeel.getCurrentSkin(c);
    }

    public static DecorationAreaType getDecoration(Component c)
    {
        return SubstanceLookAndFeel.getDecorationType(c);
    }

    public static SubstanceColorScheme getActiveColorSheme(Component c)
    {
        final SubstanceSkin skin = getSkin(c);
        final DecorationAreaType decoration = getDecoration(c);

        if ((skin != null) && (decoration != null))
            return skin.getActiveColorScheme(decoration);

        return null;
    }

    public static SubstanceColorScheme getActiveColorSheme(Component c, ComponentState state)
    {
        return SubstanceColorSchemeUtilities.getActiveColorScheme(c, state);
    }

    public static SubstanceColorScheme getBackgroundColorScheme(Component c)
    {
        final SubstanceSkin skin = getSkin(c);
        final DecorationAreaType decoration = getDecoration(c);

        if ((skin != null) && (decoration != null))
            return skin.getBackgroundColorScheme(decoration);

        return null;
    }

    public static SubstanceColorScheme getDisabledColorScheme(Component c)
    {
        final SubstanceSkin skin = getSkin(c);
        final DecorationAreaType decoration = getDecoration(c);

        if ((skin != null) && (decoration != null))
            return skin.getDisabledColorScheme(decoration);

        return null;
    }

    public static SubstanceColorScheme getEnabledColorScheme(Component c)
    {
        final SubstanceSkin skin = getSkin(c);
        final DecorationAreaType decoration = getDecoration(c);

        if ((skin != null) && (decoration != null))
            return skin.getEnabledColorScheme(decoration);

        return null;
    }

    /**
     * Return the background color for the specified component
     */
    public static Color getForeground(Component c)
    {
        final SubstanceColorScheme colorScheme;

        if (c.isEnabled())
            colorScheme = getEnabledColorScheme(c);
        else
            colorScheme = getDisabledColorScheme(c);

        if (colorScheme != null)
            return new ColorUIResource(colorScheme.getForegroundColor());

        return c.getForeground();
    }

    /**
     * Return the background color for the specified component
     */
    public static Color getBackground(Component c)
    {
        final SubstanceColorScheme colorScheme;

        if (c.isEnabled())
            colorScheme = getEnabledColorScheme(c);
        else
            colorScheme = getDisabledColorScheme(c);

        if (colorScheme != null)
            return new ColorUIResource(colorScheme.getBackgroundFillColor());

        return c.getBackground();
    }

    public static SubstanceTitlePane getTitlePane(Window window)
    {
        return (SubstanceTitlePane) SubstanceLookAndFeel.getTitlePaneComponent(window);
    }

    public static SubstanceInternalFrameTitlePane getTitlePane(JInternalFrame frame)
    {
        final InternalFrameUI ui = frame.getUI();

        if (ui instanceof SubstanceInternalFrameUI)
            return ((SubstanceInternalFrameUI) ui).getTitlePane();

        return null;
    }

    public static void setTitlePane(JInternalFrame frame, SubstanceInternalFrameTitlePane titlePane)
    {
        final InternalFrameUI ui = frame.getUI();

        if (ui instanceof SubstanceInternalFrameUI)
            ((SubstanceInternalFrameUI) ui).setNorthPane(titlePane);
    }
}
