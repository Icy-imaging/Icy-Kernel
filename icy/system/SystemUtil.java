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
package icy.system;

import java.awt.BufferCapabilities;
import java.awt.Desktop;
import java.awt.DisplayMode;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.ImageCapabilities;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.VolatileImage;
import java.io.File;
import java.lang.management.ManagementFactory;

import com.sun.management.OperatingSystemMXBean;

/**
 * @author stephane
 */
public class SystemUtil
{
    public static final String SYSTEM_WINDOWS = "win";
    public static final String SYSTEM_MAC_OS = "mac";
    public static final String SYSTEM_UNIX = "unix";

    /**
     * internal
     */
    private static long lastNano = 0;
    private static long lastCpu = 0;
    private static int lastCpuLoad = 0;

    public static Process execJAR(String jarPath, String vmArgs, String appArgs)
    {
        return exec("java " + vmArgs + " -jar " + jarPath + " " + appArgs);
    }

    public static Process execJAR(String jarPath, String appArgs)
    {
        return execJAR(jarPath, "", appArgs);
    }

    public static Process execJAR(String jarPath)
    {
        return execJAR(jarPath, "", "");
    }

    public static Process exec(String cmd)
    {
        try
        {
            return Runtime.getRuntime().exec(cmd, null, new File("."));
        }
        catch (Exception e)
        {
            System.err.println("SystemUtil.exec(" + cmd + ") error :");
            IcyExceptionHandler.showErrorMessage(e, false);
            return null;
        }
    }

    public static BufferedImage createCompatibleImage(int width, int height)
    {
        return getSystemGraphicsConfiguration().createCompatibleImage(width, height);
    }

    public static BufferedImage createCompatibleImage(int width, int height, int transparency)
    {
        return getSystemGraphicsConfiguration().createCompatibleImage(width, height, transparency);
    }

    public static VolatileImage createCompatibleVolatileImage(int width, int height)
    {
        return getSystemGraphicsConfiguration().createCompatibleVolatileImage(width, height);
    }

    public static VolatileImage createCompatibleVolatileImage(int width, int height, int transparency)
    {
        return getSystemGraphicsConfiguration().createCompatibleVolatileImage(width, height, transparency);
    }

    public static Desktop getDesktop()
    {
        if (Desktop.isDesktopSupported())
            return Desktop.getDesktop();

        return null;
    }

    public static int getSystemCtrlMask()
    {
        return Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
    }

    public static GraphicsEnvironment getLocalGraphicsEnvironment()
    {
        return GraphicsEnvironment.getLocalGraphicsEnvironment();
    }

    public static GraphicsDevice getDefaultScreenDevice()
    {
        return getLocalGraphicsEnvironment().getDefaultScreenDevice();
    }

    public static GraphicsConfiguration getSystemGraphicsConfiguration()
    {
        return getDefaultScreenDevice().getDefaultConfiguration();
    }

    public static ClassLoader getContextClassLoader()
    {
        return Thread.currentThread().getContextClassLoader();
    }

    public static ClassLoader getSystemClassLoader()
    {
        return ClassLoader.getSystemClassLoader();
    }

    public static BufferCapabilities getSystemBufferCapabilities()
    {
        return getSystemGraphicsConfiguration().getBufferCapabilities();
    }

    public static ImageCapabilities getSystemImageCapabilities()
    {
        return getSystemGraphicsConfiguration().getImageCapabilities();
    }

    public static ColorModel getSystemColorModel()
    {
        return getSystemGraphicsConfiguration().getColorModel();
    }

    public static ColorModel getSystemColorModel(int transparency)
    {
        return getSystemGraphicsConfiguration().getColorModel(transparency);
    }

    /**
     * Return the entire desktop bounds (take multi screens in account)
     */
    public static Rectangle getDesktopBounds()
    {
        Rectangle result = new Rectangle();
        final GraphicsDevice[] gs = getLocalGraphicsEnvironment().getScreenDevices();

        for (int j = 0; j < gs.length; j++)
            result = result.union(gs[j].getDefaultConfiguration().getBounds());

        return result;
    }

    /**
     * {@link GraphicsEnvironment#getMaximumWindowBounds()}
     */
    public static Rectangle getMaximumWindowBounds()
    {
        return getLocalGraphicsEnvironment().getMaximumWindowBounds();
    }

    public static DisplayMode getSystemDisplayMode()
    {
        return getDefaultScreenDevice().getDisplayMode();
    }

    /**
     * Return total number of processors or cores available to the JVM (same as system)
     */
    public static int getAvailableProcessors()
    {
        return Runtime.getRuntime().availableProcessors();
    }

    /**
     * Return total amount of free memory available to the JVM
     */
    public static long getJavaFreeMemory()
    {
        return Runtime.getRuntime().freeMemory();
    }

    /**
     * Return maximum amount of memory the JVM will attempt to use
     */
    public static long getJavaMaxMemory()
    {
        return Runtime.getRuntime().maxMemory();
    }

    /**
     * Return total memory currently in use by the JVM
     */
    public static long getJavaTotalMemory()
    {
        return Runtime.getRuntime().totalMemory();
    }

    private static OperatingSystemMXBean getOSMXBean()
    {
        final java.lang.management.OperatingSystemMXBean bean = ManagementFactory.getOperatingSystemMXBean();

        if (bean instanceof OperatingSystemMXBean)
            return (OperatingSystemMXBean) bean;

        // for (Method method : bean.getClass().getDeclaredMethods())
        // {
        // final int modifiers=method.getModifiers();
        //
        // method.setAccessible(true);
        // if (method.getName().startsWith("get")
        // && Modifier.isPublic(modifiers)) {
        // Object value;
        // try {
        // value = method.invoke(bean);
        // } catch (Exception e) {
        // value = e;
        // } // try
        // System.out.println(method.getName() + " = " + value);
        // } // if
        // } // for

        return null;
    }

    /**
     * Return total physic memory of system
     */
    public static long getSystemTotalMemory()
    {
        final OperatingSystemMXBean bean = getOSMXBean();

        if (bean != null)
            return bean.getTotalPhysicalMemorySize();

        return -1L;
    }

    /**
     * Return free physic memory of system
     */
    public static long getSystemFreeMemory()
    {
        final OperatingSystemMXBean bean = getOSMXBean();

        if (bean != null)
            return bean.getFreePhysicalMemorySize();

        return -1L;
    }

    /**
     * Return system process CPU time
     */
    public static long getSystemProcessCpuTime()
    {
        final OperatingSystemMXBean bean = getOSMXBean();

        if (bean != null)
            return bean.getProcessCpuTime();

        return -1L;
    }

    /**
     * Return average CPU load of the application processes from the last call<br>
     * (-1 if no available)
     */
    public static int getSystemCpuLoad()
    {
        final OperatingSystemMXBean bean = getOSMXBean();

        if (bean != null)
        {
            // first call
            if (lastNano == 0)
            {
                lastNano = System.nanoTime();
                lastCpu = bean.getProcessCpuTime();
            }
            else
            {
                final long nanoAfter = System.nanoTime();
                final long cpuAfter = bean.getProcessCpuTime();

                final long dNano = nanoAfter - lastNano;
                final long dCpu = cpuAfter - lastCpu;

                // below 0.5s the reported value isn't very significant
                if (dNano > 500000000L)
                {
                    lastCpuLoad = (int) ((dCpu * 100L) / (dNano * getAvailableProcessors()));
                    lastNano = nanoAfter;
                    lastCpu = cpuAfter;
                }
            }

            return lastCpuLoad;
        }

        return -1;
    }

    public static String getJavaName()
    {
        return System.getProperty("java.runtime.name");
    }

    public static String getJavaVersion()
    {
        return System.getProperty("java.runtime.version");
    }

    public static int getJavaArchDataModel()
    {
        return Integer.parseInt(System.getProperty("sun.arch.data.model"));
    }

    public static String getOSName()
    {
        return System.getProperty("os.name");
    }

    public static String getOSArch()
    {
        return System.getProperty("os.arch");
    }

    public static String getOSVersion()
    {
        return System.getProperty("os.version");
    }

    /**
     * Return an id OS string :<br>
     * <br>
     * Windows system return <code>SystemUtil.SYSTEM_WINDOWS</code><br>
     * MAC OS return <code>SystemUtil.SYSTEM_MAC_OS</code><br>
     * Unix system return <code>SystemUtil.SYSTEM_UNIX</code><br>
     * <br>
     * An empty string is returned is OS is unknown.
     */
    public static String getOSNameId()
    {
        if (isWindow())
            return SYSTEM_WINDOWS;
        if (isMac())
            return SYSTEM_MAC_OS;
        if (isUnix())
            return SYSTEM_UNIX;

        return "";
    }

    /**
     * Return an id OS architecture string<br>
     * example : "win32", "win64", "mac32", "mac64", "unix32"...<br>
     * The bits number depends only from current installed JVM (32 or 64 bit)
     * and not directly from host OS.<br>
     * An empty string is returned if OS is unknown.
     */
    public static String getOSArchIdString()
    {
        final String javaBit = Integer.toString(getJavaArchDataModel());

        if (isWindow())
            return SYSTEM_WINDOWS + javaBit;
        if (isMac())
            return SYSTEM_MAC_OS + javaBit;
        if (isUnix())
            return SYSTEM_UNIX + javaBit;

        return "";
    }

    public static boolean isLinkSupported()
    {
        return isMac() || isUnix();
    }

    public static boolean is32bits()
    {
        return getJavaArchDataModel() == 32;
    }

    public static boolean is64bits()
    {
        return getJavaArchDataModel() == 64;
    }

    public static boolean isWindow()
    {
        return (getOSName().toLowerCase().indexOf("win") >= 0);
    }

    public static boolean isMac()
    {
        return (getOSName().toLowerCase().indexOf("mac") >= 0);
    }

    public static boolean isUnix()
    {
        final String os = getOSName().toLowerCase();
        return (os.indexOf("nix") >= 0) || (os.indexOf("nux") >= 0);
    }
}
