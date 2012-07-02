package icy.preferences;

import icy.gui.dialog.IdConfirmDialog;
import icy.gui.frame.progress.ToolTipFrame;
import icy.gui.util.LookAndFeelUtil;

/**
 * @author Stephane
 */
public class GeneralPreferences
{
    /**
     * pref id
     */
    public static final String PREF_GENERAL_ID = "general";
    public static final String TOOLTIPS_ID = "toolTips";
    public static final String CONFIRMS_ID = "confirms";

    /**
     * id general
     */
    public static final String ID_SEQUENCE_PERSISTENCE = "sequencePersistence";
    public static final String ID_AUTO_UPDATE = "autoUpdate";
    public static final String ID_AUTO_CHECK_UPDATE = "autoCheckUpdate";
    public static final String ID_RIBBON_MINIMIZED = "ribbonMinimized";
    public static final String ID_DETACHED_MODE = "detached";
    public static final String ID_ALWAYS_ON_TOP = "alwaysOnTop";
    public static final String ID_GUI_SKIN = "guiSkin";
    public static final String ID_GUI_FONT_SIZE = "guiFontSize";
    public static final String ID_STARTUP_TOOLTIP = "startupTooltip";

    /**
     * id confirm
     */
    public static final String ID_CONFIRM_EXIT = "exit";

    /**
     * preferences
     */
    private static XMLPreferences prefGeneral;
    private static XMLPreferences prefToolTips;
    private static XMLPreferences prefConfirms;

    public static void load()
    {
        // load preferences
        prefGeneral = ApplicationPreferences.getPreferences().node(PREF_GENERAL_ID);
        prefToolTips = prefGeneral.node(TOOLTIPS_ID);
        prefConfirms = prefGeneral.node(CONFIRMS_ID);
    }

    /**
     * @deprecated uses {@link #getPreferences()} instead
     */
    @Deprecated
    public static XMLPreferences getPreferencesGeneral()
    {
        return getPreferences();
    }

    /**
     * @return the preferences
     */
    public static XMLPreferences getPreferences()
    {
        return prefGeneral;
    }

    /**
     * @return the preferences for tool tips ({@link ToolTipFrame}).
     */
    public static XMLPreferences getPreferencesToolTips()
    {
        return prefToolTips;
    }

    /**
     * @return the preferences for confirm dialog ({@link IdConfirmDialog}).
     */
    public static XMLPreferences getPreferencesConfirms()
    {
        return prefConfirms;
    }


    public static boolean getExitConfirm()
    {
        return prefConfirms.getBoolean(ID_CONFIRM_EXIT, true);
    }

    public static boolean getSequencePersistence()
    {
        return prefGeneral.getBoolean(ID_SEQUENCE_PERSISTENCE, true);
    }

    public static boolean getAutomaticUpdate()
    {
        return prefGeneral.getBoolean(ID_AUTO_UPDATE, true);
    }

    public static boolean getAutomaticCheckUpdate()
    {
        return prefGeneral.getBoolean(ID_AUTO_CHECK_UPDATE, true);
    }

    public static boolean getRibbonMinimized()
    {
        return prefGeneral.getBoolean(ID_RIBBON_MINIMIZED, false);
    }

    public static boolean getMultiWindowMode()
    {
        return prefGeneral.getBoolean(ID_DETACHED_MODE, false);
    }

    public static boolean getAlwaysOnTop()
    {
        return prefGeneral.getBoolean(ID_ALWAYS_ON_TOP, false);
    }

    public static boolean getStatupTooltip()
    {
        return prefGeneral.getBoolean(ID_STARTUP_TOOLTIP, true);
    }

    public static int getGuiFontSize()
    {
        return prefGeneral.getInt(ID_GUI_FONT_SIZE, LookAndFeelUtil.getDefaultFontSize());
    }

    public static String getGuiSkin()
    {
        return prefGeneral.get(ID_GUI_SKIN, LookAndFeelUtil.getDefaultSkin());
    }

    public static void setExitConfirm(boolean value)
    {
        prefConfirms.putBoolean(ID_CONFIRM_EXIT, value);
    }

    public static void setSequencePersistence(boolean value)
    {
        prefGeneral.putBoolean(ID_SEQUENCE_PERSISTENCE, value);
    }

    public static void setAutomaticUpdate(boolean value)
    {
        prefGeneral.putBoolean(ID_AUTO_UPDATE, value);
    }

    public static void setAutomaticCheckUpdate(boolean value)
    {
        prefGeneral.putBoolean(ID_AUTO_CHECK_UPDATE, value);
    }

    public static void setRibbonMinimized(boolean value)
    {
        prefGeneral.putBoolean(ID_RIBBON_MINIMIZED, value);
    }

    public static void setMultiWindowMode(boolean value)
    {
        prefGeneral.putBoolean(ID_DETACHED_MODE, value);
    }

    public static void setAlwaysOnTop(boolean value)
    {
        prefGeneral.putBoolean(ID_ALWAYS_ON_TOP, value);
    }

    public static void setStatupTooltip(boolean value)
    {
        prefGeneral.putBoolean(ID_STARTUP_TOOLTIP, value);
    }

    public static void setGuiFontSize(int value)
    {
        prefGeneral.putInt(ID_GUI_FONT_SIZE, value);
    }

    public static void setGuiSkin(String value)
    {
        prefGeneral.put(ID_GUI_SKIN, value);
    }
}
