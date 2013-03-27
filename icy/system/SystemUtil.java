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
import java.awt.HeadlessException;
import java.awt.ImageCapabilities;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.VolatileImage;
import java.io.File;
import java.lang.management.ManagementFactory;
import java.util.Properties;

import sun.java2d.SunGraphicsEnvironment;

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
     * internals
     */
    private static Properties props = System.getProperties();

    private static long lastNano = 0;
    private static long lastCpu = 0;
    private static int lastCpuLoad = 0;

    /**
     * Launch specified jar file.
     * 
     * @param jarPath
     *        jar file path.
     * @param vmArgs
     *        arguments for the java virtual machine.
     * @param appArgs
     *        arguments for jar application.
     * @param workDir
     *        working directory.
     */
    public static Process execJAR(String jarPath, String vmArgs, String appArgs, String workDir)
    {
        return exec("java " + vmArgs + " -jar " + jarPath + " " + appArgs, workDir);
    }

    /**
     * Launch specified jar file.
     * 
     * @param jarPath
     *        jar file path.
     * @param vmArgs
     *        arguments for the java virtual machine.
     * @param appArgs
     *        arguments for jar application.
     */
    public static Process execJAR(String jarPath, String vmArgs, String appArgs)
    {
        return exec("java " + vmArgs + " -jar " + jarPath + " " + appArgs);
    }

    /**
     * Launch specified jar file.
     * 
     * @param jarPath
     *        jar file path.
     * @param appArgs
     *        arguments for jar application.
     */
    public static Process execJAR(String jarPath, String appArgs)
    {
        return execJAR(jarPath, "", appArgs);
    }

    /**
     * Launch specified jar file.
     * 
     * @param jarPath
     *        jar file path.
     */
    public static Process execJAR(String jarPath)
    {
        return execJAR(jarPath, "", "");
    }

    /**
     * Execute a system command and return the attached process.
     * 
     * @param cmd
     *        system command to execute.
     */
    public static Process exec(String cmd)
    {
        return exec(cmd, ".");
    }

    /**
     * Execute a system command and return the attached process.
     * 
     * @param cmd
     *        system command to execute.
     * @param dir
     *        the working directory of the subprocess, or null if the subprocess should inherit the
     *        working directory of the current process.
     */
    public static Process exec(String cmd, String dir)
    {
        try
        {
            return Runtime.getRuntime().exec(cmd, null, new File(dir));
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
        return getDefaultGraphicsConfiguration().createCompatibleImage(width, height);
    }

    public static BufferedImage createCompatibleImage(int width, int height, int transparency)
    {
        return getDefaultGraphicsConfiguration().createCompatibleImage(width, height, transparency);
    }

    public static VolatileImage createCompatibleVolatileImage(int width, int height)
    {
        return getDefaultGraphicsConfiguration().createCompatibleVolatileImage(width, height);
    }

    public static VolatileImage createCompatibleVolatileImage(int width, int height, int transparency)
    {
        return getDefaultGraphicsConfiguration().createCompatibleVolatileImage(width, height, transparency);
    }

    public static Desktop getDesktop()
    {
        if (Desktop.isDesktopSupported())
            return Desktop.getDesktop();

        return null;
    }

    /**
     * @see System#getProperty(String)
     */
    public static String getProperty(String name)
    {
        return props.getProperty(name);
    }

    /**
     * @see System#getProperty(String, String)
     */
    public static String getProperty(String name, String defaultValue)
    {
        return props.getProperty(name, defaultValue);
    }

    /**
     * @see System#setProperty(String, String)
     */
    public static String setProperty(String name, String value)
    {
        return (String) props.setProperty(name, value);
    }

    /**
     * @deprecated Use {@link #getMenuCtrlMask()} instead.
     */
    @Deprecated
    public static int getCtrlMask()
    {
        return getMenuCtrlMask();
    }

    /**
     * Return the CTRL key mask used for Menu shortcut.
     */
    public static int getMenuCtrlMask()
    {
        return Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
    }

    /**
     * @deprecated Use {@link #getMenuCtrlMask()} instead
     */
    @Deprecated
    public static int getSystemCtrlMask()
    {
        return getMenuCtrlMask();
    }

    public static GraphicsEnvironment getLocalGraphicsEnvironment()
    {
        return GraphicsEnvironment.getLocalGraphicsEnvironment();
    }

    /**
     * Return the default screen device.
     */
    public static GraphicsDevice getDefaultScreenDevice()
    {
        try
        {
            return getLocalGraphicsEnvironment().getDefaultScreenDevice();
        }
        catch (HeadlessException e)
        {
            return null;
        }
    }

    /**
     * Return the default graphics configuration.
     */
    public static GraphicsConfiguration getDefaultGraphicsConfiguration()
    {
        try
        {
            return getDefaultScreenDevice().getDefaultConfiguration();
        }
        catch (Exception e)
        {
            return null;
        }
    }

    /**
     * @deprecated Use {@link #getDefaultGraphicsConfiguration()} instead.
     */
    @Deprecated
    public static GraphicsConfiguration getSystemGraphicsConfiguration()
    {
        return getDefaultGraphicsConfiguration();
    }

    /**
     * Return the number of screen device.
     */
    public static int getScreenDeviceCount()
    {
        try
        {
            return getLocalGraphicsEnvironment().getScreenDevices().length;
        }
        catch (HeadlessException e)
        {
            return 0;
        }
    }

    /**
     * Return the screen device corresponding to specified index.
     */
    public static GraphicsDevice getScreenDevice(int index)
    {
        try
        {
            return getLocalGraphicsEnvironment().getScreenDevices()[index];
        }
        catch (HeadlessException e)
        {
            return null;
        }
    }

    /**
     * Return true if current system is "head less" (no screen output device).
     */
    public static boolean isHeadLess()
    {
        return getLocalGraphicsEnvironment().isHeadlessInstance();
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
            result = result.union(SunGraphicsEnvironment.getUsableBounds(gs[j]));

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
    public static long getTotalMemory()
    {
        final OperatingSystemMXBean bean = getOSMXBean();

        if (bean != null)
            return bean.getTotalPhysicalMemorySize();

        return -1L;
    }

    /**
     * @deprecated Use {@link #getTotalMemory()} instead
     */
    @Deprecated
    public static long getSystemTotalMemory()
    {
        return getTotalMemory();
    }

    /**
     * Return free physic memory of system
     */
    public static long getFreeMemory()
    {
        final OperatingSystemMXBean bean = getOSMXBean();

        if (bean != null)
            return bean.getFreePhysicalMemorySize();

        return -1L;
    }

    /**
     * @deprecated Use {@link #getFreeMemory()} instead
     */
    @Deprecated
    public static long getSystemFreeMemory()
    {
        return getFreeMemory();
    }

    /**
     * Return system process CPU time
     */
    public static long getProcessCpuTime()
    {
        final OperatingSystemMXBean bean = getOSMXBean();

        if (bean != null)
            return bean.getProcessCpuTime();

        return -1L;
    }

    /**
     * @deprecated Use {@link #getProcessCpuTime()} instead
     */
    @Deprecated
    public static long getSystemProcessCpuTime()
    {
        return getProcessCpuTime();
    }

    /**
     * Return average CPU load of the application processes from the last call<br>
     * (-1 if no available)
     */
    public static int getCpuLoad()
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

    /**
     * @deprecated Use {@link #getCpuLoad()} instead
     */
    @Deprecated
    public static int getSystemCpuLoad()
    {
        return getCpuLoad();
    }

    /**
     * Returns the user name.
     */
    public static String getUserName()
    {
        return getProperty("user.name");
    }

    /**
     * Returns the JVM name.
     */
    public static String getJavaName()
    {
        return getProperty("java.runtime.name");
    }

    /**
     * Returns the JVM version.
     */
    public static String getJavaVersion()
    {
        return getProperty("java.runtime.version");
    }

    /**
     * Returns the JVM data architecture model.
     */
    public static int getJavaArchDataModel()
    {
        return Integer.parseInt(getProperty("sun.arch.data.model"));
    }

    /**
     * Returns the Operating System name.
     */
    public static String getOSName()
    {
        return getProperty("os.name");
    }

    /**
     * Returns the Operating System architecture name.
     */
    public static String getOSArch()
    {
        return getProperty("os.arch");
    }

    /**
     * Returns the Operating System version.
     */
    public static String getOSVersion()
    {
        return getProperty("os.version");
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
