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
package icy.preferences;

import icy.file.FileUtil;
import icy.gui.dialog.IdConfirmDialog;
import icy.gui.frame.progress.ToolTipFrame;
import icy.gui.util.LookAndFeelUtil;
import icy.roi.ROI.ROIPainter;

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
    public static final String ROIOVERLAY_ID = "roiOverlay";

    /**
     * id general
     */
    public static final String ID_SEQUENCE_PERSISTENCE = "sequencePersistence";
    public static final String ID_SAVE_NEW_SEQUENCE = "saveNewSequence";
    public static final String ID_VIRTUAL_MODE = "virtualMode";
    public static final String ID_AUTO_UPDATE = "autoUpdate";
    public static final String ID_LAST_UPDATECHECK_TIME = "lastUpdateCheckTime";
    public static final String ID_RIBBON_MINIMIZED = "ribbonMinimized";
    public static final String ID_DETACHED_MODE = "detached";
    public static final String ID_ALWAYS_ON_TOP = "alwaysOnTop";
    public static final String ID_USAGE_STATS_REPORT = "usageStatsReport";
    public static final String ID_GUI_SKIN = "guiSkin";
    public static final String ID_GUI_FONT_SIZE = "guiFontSize";
    public static final String ID_STARTUP_TOOLTIP = "startupTooltip";
    public static final String ID_LOADER_FOLDER = "loaderFolder";
    public static final String ID_RESULT_FOLDER = "resultFolder";
    public static final String ID_USER_LOGIN = "userLogin";
    public static final String ID_USER_NAME = "userName";
    public static final String ID_USER_EMAIL = "userEmail";
    public static final String ID_HISTORY_SIZE = "historySize";
    public static final String ID_OUTPUT_LOG_SIZE = "outputLogSize";
    public static final String ID_OUTPUT_LOG_FILE = "outputLogFile";

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
    private static XMLPreferences prefRoiOverlay;

    public static void load()
    {
        // load preferences
        prefGeneral = ApplicationPreferences.getPreferences().node(PREF_GENERAL_ID);
        prefToolTips = prefGeneral.node(TOOLTIPS_ID);
        prefConfirms = prefGeneral.node(CONFIRMS_ID);
        prefRoiOverlay = prefGeneral.node(ROIOVERLAY_ID);
    }

    /**
     * @deprecated Use {@link #getPreferences()} instead
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
     * @return the root preferences for tool tips ({@link ToolTipFrame}).
     */
    public static XMLPreferences getPreferencesToolTips()
    {
        return prefToolTips;
    }

    /**
     * @return the root preferences for confirm dialog ({@link IdConfirmDialog}).
     */
    public static XMLPreferences getPreferencesConfirms()
    {
        return prefConfirms;
    }

    /**
     * @return the root preferences for ROI overlay setting ({@link ROIPainter}).
     */
    public static XMLPreferences getPreferencesRoiOverlay()
    {
        return prefRoiOverlay;
    }

    public static boolean getExitConfirm()
    {
        return prefConfirms.getBoolean(ID_CONFIRM_EXIT, true);
    }

    public static boolean getSaveNewSequence()
    {
        return prefGeneral.getBoolean(ID_SAVE_NEW_SEQUENCE, false);
    }

    public static boolean getSequencePersistence()
    {
        return prefGeneral.getBoolean(ID_SEQUENCE_PERSISTENCE, true);
    }

    public static boolean getAutomaticUpdate()
    {
        return prefGeneral.getBoolean(ID_AUTO_UPDATE, true);
    }

    public static long getLastUpdateCheckTime()
    {
        return prefGeneral.getLong(ID_LAST_UPDATECHECK_TIME, 0);
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

    public static boolean getUsageStatisticsReport()
    {
        return prefGeneral.getBoolean(ID_USAGE_STATS_REPORT, true);
    }

    public static boolean getStatupTooltip()
    {
        return prefGeneral.getBoolean(ID_STARTUP_TOOLTIP, true);
    }

    public static String getLoaderFolder()
    {
        return prefGeneral.get(ID_LOADER_FOLDER, "");
    }

    public static String getResultFolder()
    {
        return prefGeneral.get(ID_RESULT_FOLDER, FileUtil.APPLICATION_DIRECTORY + FileUtil.separator + "result");
    }

    public static String getUserLogin()
    {
        return prefGeneral.get(ID_USER_LOGIN, "");
    }

    public static String getUserName()
    {
        return prefGeneral.get(ID_USER_NAME, "");
    }

    public static String getUserEmail()
    {
        return prefGeneral.get(ID_USER_EMAIL, "");
    }

    public static int getGuiFontSize()
    {
        return prefGeneral.getInt(ID_GUI_FONT_SIZE, LookAndFeelUtil.getDefaultFontSize());
    }

    public static String getGuiSkin()
    {
        return prefGeneral.get(ID_GUI_SKIN, LookAndFeelUtil.getDefaultSkin());
    }

    public static int getHistorySize()
    {
        return prefGeneral.getInt(ID_HISTORY_SIZE, 50);
    }

    public static int getOutputLogSize()
    {
        return prefGeneral.getInt(ID_OUTPUT_LOG_SIZE, 10000);
    }

    public static boolean getOutputLogToFile()
    {
        return prefGeneral.getBoolean(ID_OUTPUT_LOG_FILE, false);
    }

    public static boolean getVirtualMode()
    {
        return prefGeneral.getBoolean(ID_VIRTUAL_MODE, false);
    }

    public static void setExitConfirm(boolean value)
    {
        prefConfirms.putBoolean(ID_CONFIRM_EXIT, value);
    }

    public static void setSaveNewSequence(boolean value)
    {
        prefGeneral.putBoolean(ID_SAVE_NEW_SEQUENCE, value);
    }

    public static void setSequencePersistence(boolean value)
    {
        prefGeneral.putBoolean(ID_SEQUENCE_PERSISTENCE, value);
    }

    public static void setAutomaticUpdate(boolean value)
    {
        prefGeneral.putBoolean(ID_AUTO_UPDATE, value);
    }

    public static void setUsageStatisticsReport(boolean value)
    {
        prefGeneral.putBoolean(ID_USAGE_STATS_REPORT, value);
    }

    public static void setLastUpdateCheckTime(long time)
    {
        prefGeneral.putLong(ID_LAST_UPDATECHECK_TIME, time);
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

    public static void setLoaderFolder(String value)
    {
        prefGeneral.put(ID_LOADER_FOLDER, value);
    }

    public static void setResultFolder(String value)
    {
        prefGeneral.put(ID_RESULT_FOLDER, value);
    }

    public static void setUserLogin(String value)
    {
        prefGeneral.put(ID_USER_LOGIN, value);
    }

    public static void setUserName(String value)
    {
        prefGeneral.put(ID_USER_NAME, value);
    }

    public static void setUserEmail(String value)
    {
        prefGeneral.put(ID_USER_EMAIL, value);
    }

    public static void setGuiFontSize(int value)
    {
        prefGeneral.putInt(ID_GUI_FONT_SIZE, value);
    }

    public static void setGuiSkin(String value)
    {
        prefGeneral.put(ID_GUI_SKIN, value);
    }

    public static void setHistorySize(int value)
    {
        prefGeneral.putInt(ID_HISTORY_SIZE, value);
    }

    public static void setOutputLogSize(int value)
    {
        prefGeneral.putInt(ID_OUTPUT_LOG_SIZE, value);
    }

    public static void setOutputLogFile(boolean value)
    {
        prefGeneral.putBoolean(ID_OUTPUT_LOG_FILE, value);
    }

    public static void setVirtualMode(boolean value)
    {
        prefGeneral.putBoolean(ID_VIRTUAL_MODE, value);
    }
}
