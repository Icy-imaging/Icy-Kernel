/**
 * 
 */
package icy.preferences;

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
    public static final String ID_APP_PARAMS = "appParams";

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

    /**
     * Get max memory (in MB)
     */
    public static int getMaxMemoryMB()
    {
        int result = preferences.getInt(ID_MAX_MEMORY, -1);

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
        return preferences.getInt(ID_STACK_SIZE, 4096);
    }

    /**
     * Get extra JVM parameters string
     */
    public static String getExtraVMParams()
    {
        return preferences.get(ID_EXTRA_VMPARAMS,
                "-XX:CompileCommand=exclude,icy/image/IcyBufferedImage.createFrom -XX:MaxPermSize=128M");
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
     * Get ICY application parameters string
     */
    public static String getAppParams()
    {
        return preferences.get(ID_APP_PARAMS, "");
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
     * Set ICY application parameters string
     */
    public static void setAppParams(String value)
    {
        preferences.put(ID_APP_PARAMS, value);
    }
}
