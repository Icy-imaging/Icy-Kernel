package icy.preferences;

import icy.gui.dialog.IdConfirmDialog;
import icy.gui.frame.progress.ToolTipFrame;
import icy.gui.util.LookAndFeelUtil;
import icy.math.MathUtil;
import icy.system.SystemUtil;

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
    public static final String ID_MAX_MEMORY = "maxMemory";
    public static final String ID_STACK_SIZE = "stackSize";
    public static final String ID_EXTRA_VMPARAMS = "extraVMParams";
    public static final String ID_OS_EXTRA_VMPARAMS = "osExtraVMParams";
    public static final String ID_APP_PARAMS = "appParams";

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

        // set here settings which need to be initialized
        setMaxMemoryMB(GeneralPreferences.getMaxMemoryMB());
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

    /**
     * Get max memory (in MB)
     */
    public static int getMaxMemoryMB()
    {
        int result = prefGeneral.getInt(ID_MAX_MEMORY, -1);

        // no value ?
        if (result == -1)
        {
            final long freeMemory = SystemUtil.getFreeMemory();

            // take system total memory / 2
            long calculatedMaxMem = SystemUtil.getTotalMemory() / 2;
            // current available memory is low ?
            if (calculatedMaxMem > freeMemory)
                // adjust max memory
                calculatedMaxMem -= (calculatedMaxMem - freeMemory) / 2;

            // get max memory in MB
            result = Math.min(getMaxMemoryMBLimit(), (int) (calculatedMaxMem / (1024 * 1024)));
        }

        // arrange to get multiple of 32 MB
        return (int) MathUtil.prevMultiple(result, 32);
    }

    public static int getMaxMemoryMBLimit()
    {
        final int result = (int) (SystemUtil.getTotalMemory() / (1024 * 1024));

        // limit maximum value for 32 bits system
        if (SystemUtil.is32bits() && (result > 1400))
            return 1400;

        return result;
    }

    /**
     * Get stack size (in KB)
     */
    public static int getStackSizeKB()
    {
        return prefGeneral.getInt(ID_STACK_SIZE, 4096);
    }

    /**
     * Get extra JVM parameters string
     */
    public static String getExtraVMParams()
    {
        return prefGeneral.get(ID_EXTRA_VMPARAMS, "-XX:CompileCommand=exclude,icy/image/IcyBufferedImage.createFrom");
    }

    /**
     * Get OS specific extra JVM parameters string
     */
    public static String getOSExtraVMParams()
    {
        final String os = SystemUtil.getOSNameId();

        // we have different default extra VM parameters depending OS
        if (os.equals(SystemUtil.SYSTEM_WINDOWS))
            return prefGeneral.get(ID_OS_EXTRA_VMPARAMS + SystemUtil.SYSTEM_WINDOWS, "-Dsun.java2d.d3d=false");
        if (os.equals(SystemUtil.SYSTEM_MAC_OS))
            return prefGeneral.get(ID_OS_EXTRA_VMPARAMS + SystemUtil.SYSTEM_MAC_OS, "-Xdock:name=Icy");
        if (os.equals(SystemUtil.SYSTEM_UNIX))
            return prefGeneral.get(ID_OS_EXTRA_VMPARAMS + SystemUtil.SYSTEM_UNIX, "");

        return "";
    }

    /**
     * Get ICY application parameters string
     */
    public static String getAppParams()
    {
        return prefGeneral.get(ID_APP_PARAMS, "");
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

    /**
     * Set max memory (in MB)
     */
    public static void setMaxMemoryMB(int value)
    {
        prefGeneral.putInt(ID_MAX_MEMORY, Math.min(getMaxMemoryMBLimit(), value));
    }

    /**
     * Set stack size (in KB)
     */
    public static void setStackSizeKB(int value)
    {
        prefGeneral.putInt(ID_STACK_SIZE, value);
    }

    /**
     * Set extra JVM parameters string
     */
    public static void setExtraVMParams(String value)
    {
        prefGeneral.put(ID_EXTRA_VMPARAMS, value);
    }

    /**
     * Set OS specific extra JVM parameters string
     */
    public static void setOSExtraVMParams(String value)
    {
        prefGeneral.put(ID_OS_EXTRA_VMPARAMS + SystemUtil.getOSNameId(), value);
    }

    /**
     * Set ICY application parameters string
     */
    public static void setAppParams(String value)
    {
        prefGeneral.put(ID_APP_PARAMS, value);
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
