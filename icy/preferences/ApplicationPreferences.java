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
package icy.preferences;

import icy.common.Version;
import icy.math.MathUtil;
import icy.network.NetworkUtil;
import icy.system.SystemUtil;

/**
 * @author Stephane
 */
public class ApplicationPreferences
{
    /**
     * id
     */
    private static final String PREF_ID = "icy";

    public static final String ID_ICY_ID = "id";
    public static final String ID_OS = "os";
    public static final String ID_UPDATE_REPOSITORY_BASE = "updateRepositoryBase";
    public static final String ID_UPDATE_REPOSITORY_FILE = "updateRepositoryFile";
    public static final String ID_MAX_MEMORY = "maxMemory";
    public static final String ID_STACK_SIZE = "stackSize";
    public static final String ID_EXTRA_VMPARAMS = "extraVMParams";
    public static final String ID_OS_EXTRA_VMPARAMS = "osExtraVMParams";
    public static final String ID_APP_FOLDER = "appFolder";
    public static final String ID_APP_PARAMS = "appParams";
    public static final String ID_VERSION = "version";
    public static final String ID_SINGLE_INSTANCE = "singleInstance";

    private final static String DEFAULT_UPDATE_REPOSITORY_BASE = NetworkUtil.WEBSITE_URL + "update/";
    private final static String DEFAULT_UPDATE_REPOSITORY_FILE = "update.php";

    /**
     * preferences
     */
    private static XMLPreferences preferences;

    public static void load()
    {
        // load preference
        preferences = IcyPreferences.root().node(PREF_ID);

        // set here settings which need to be initialized
        setMaxMemoryMB(getMaxMemoryMB());
    }

    /**
     * @return the preferences
     */
    public static XMLPreferences getPreferences()
    {
        return preferences;
    }

    public static String getOs()
    {
        return preferences.get(ID_OS, "");
    }

    public static void setOs(String value)
    {
        preferences.put(ID_OS, value);
    }

    public static int getId()
    {
        return preferences.getInt(ID_ICY_ID, -1);
    }

    public static void setId(int value)
    {
        preferences.putInt(ID_ICY_ID, value);
    }

    public static String getUpdateRepositoryBase()
    {
        return preferences.get(ID_UPDATE_REPOSITORY_BASE, DEFAULT_UPDATE_REPOSITORY_BASE);
    }

    public static void setUpdateRepositoryBase(String value)
    {
        preferences.put(ID_UPDATE_REPOSITORY_BASE, value);
    }

    public static String getUpdateRepositoryFile()
    {
        return preferences.get(ID_UPDATE_REPOSITORY_FILE, DEFAULT_UPDATE_REPOSITORY_FILE);
    }

    public static void setUpdateRepositoryFile(String value)
    {
        preferences.put(ID_UPDATE_REPOSITORY_FILE, value);
    }

    static int memoryAlign(int memMB)
    {
        // arrange to get multiple of 32 MB
        return (int) MathUtil.prevMultiple(memMB, 32);
    }

    static int checkMem(int memMB)
    {
        // check we can allocate that much
        return Math.min(getMaxMemoryMBLimit(), memoryAlign(memMB));
    }

    /**
     * Get max memory (in MB)
     */
    public static int getMaxMemoryMB()
    {
        int result = preferences.getInt(ID_MAX_MEMORY, -1);

        // no value ?
        if (result == -1)
            result = getDefaultMemoryMB();

        // arrange to get multiple of 32 MB
        return checkMem(result);
    }

    public static int getDefaultMemoryMB()
    {
        final long freeMemory = SystemUtil.getFreeMemory();

        // take system total memory / 2
        long calculatedMaxMem = SystemUtil.getTotalMemory() / 2;
        // current available memory is low ?
        if (calculatedMaxMem > freeMemory)
            // adjust max memory
            calculatedMaxMem -= (calculatedMaxMem - freeMemory) / 2;

        // get max memory in MB
        return checkMem((int) (calculatedMaxMem / (1024 * 1024)));
    }

    public static int getMaxMemoryMBLimit()
    {
        int result = (int) (SystemUtil.getTotalMemory() / (1024 * 1024));

        // limit maximum memory to 1024 MB for 32 bits system
        if (SystemUtil.is32bits() && (result > 1024))
            result = 1024;

        return memoryAlign(result);
    }

    /**
     * Get stack size (in KB)
     */
    public static int getStackSizeKB()
    {
        // 2MB by default for VTK
        return preferences.getInt(ID_STACK_SIZE, 2048);
    }

    /**
     * Get extra JVM parameters string
     */
    public static String getExtraVMParams()
    {
        // we want a big permgen space for the class loader
        return preferences.get(ID_EXTRA_VMPARAMS,
                "-XX:CompileCommand=exclude,plugins/kernel/importer/LociImporterPlugin.getImage -XX:MaxPermSize=128M");
    }

    /**
     * Get OS specific extra JVM parameters string
     */
    public static String getOSExtraVMParams()
    {
        final String os = SystemUtil.getOSNameId();

        // we have different default extra VM parameters depending OS
        if (os.equals(SystemUtil.SYSTEM_WINDOWS))
            return preferences.get(ID_OS_EXTRA_VMPARAMS + SystemUtil.SYSTEM_WINDOWS, "-Dsun.java2d.d3d=false");
        if (os.equals(SystemUtil.SYSTEM_MAC_OS))
            return preferences.get(ID_OS_EXTRA_VMPARAMS + SystemUtil.SYSTEM_MAC_OS, "-Xdock:name=Icy");
        if (os.equals(SystemUtil.SYSTEM_UNIX))
            return preferences.get(ID_OS_EXTRA_VMPARAMS + SystemUtil.SYSTEM_UNIX, "");

        return "";
    }

    /**
     * Get Icy application folder
     */
    public static String getAppFolder()
    {
        return preferences.get(ID_APP_FOLDER, "");
    }

    /**
     * Get Icy application parameters string
     */
    public static String getAppParams()
    {
        return preferences.get(ID_APP_PARAMS, "");
    }

    /**
     * Get the stored version number (used to detect new installed version).
     */
    public static Version getVersion()
    {
        return new Version(preferences.get(ID_VERSION, "1.0.0.0"));
    }

    /**
     * Set max memory (in MB)
     */
    public static void setMaxMemoryMB(int value)
    {
        preferences.putInt(ID_MAX_MEMORY, Math.min(getMaxMemoryMBLimit(), value));
    }

    /**
     * Set stack size (in KB)
     */
    public static void setStackSizeKB(int value)
    {
        preferences.putInt(ID_STACK_SIZE, value);
    }

    /**
     * Set extra JVM parameters string
     */
    public static void setExtraVMParams(String value)
    {
        preferences.put(ID_EXTRA_VMPARAMS, value);
    }

    /**
     * Set OS specific extra JVM parameters string
     */
    public static void setOSExtraVMParams(String value)
    {
        preferences.put(ID_OS_EXTRA_VMPARAMS + SystemUtil.getOSNameId(), value);
    }

    /**
     * Set Icy application folder
     */
    public static void setAppFolder(String value)
    {
        preferences.put(ID_APP_FOLDER, value);
    }

    /**
     * Set ICY application parameters string
     */
    public static void setAppParams(String value)
    {
        preferences.put(ID_APP_PARAMS, value);
    }

    /**
     * Set the stored version number (used to detect new installed version)
     */
    public static void setVersion(Version value)
    {
        preferences.put(ID_VERSION, value.toString());
    }

}
