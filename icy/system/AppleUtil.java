/**
 * 
 */
package icy.system;

import icy.gui.dialog.ImageLoaderDialog;
import icy.gui.frame.AboutFrame;
import icy.gui.preferences.GeneralPreferencePanel;
import icy.gui.preferences.PreferenceFrame;
import icy.main.Icy;
import icy.resource.ResourceUtil;
import icy.system.thread.ThreadUtil;

import java.awt.Toolkit;
import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * OSX application compatibility class
 * 
 * @author stephane
 */
public class AppleUtil
{
    static final Thread fixThread = new Thread(new Runnable()
    {
        @Override
        public void run()
        {
            appleFixLiveRun();
        }
    }, "AppleFix");

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void init()
    {
        try
        {
            final ClassLoader classLoader = SystemUtil.getSystemClassLoader();
            final Class appClass = classLoader.loadClass("com.apple.eawt.Application");
            final Object app = appClass.newInstance();

            final Class listenerClass = classLoader.loadClass("com.apple.eawt.ApplicationListener");
            final Object listener = Proxy.newProxyInstance(classLoader, new Class[] {listenerClass},
                    new InvocationHandler()
                    {
                        @Override
                        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
                        {
                            final Object applicationEvent = args[0];
                            final Class appEventClass = applicationEvent.getClass();
                            final Method m = appEventClass.getMethod("setHandled", boolean.class);

                            if (method.getName().equals("handleQuit"))
                            {
                                m.invoke(applicationEvent, Boolean.valueOf(Icy.exit(false)));
                            }
                            if (method.getName().equals("handleAbout"))
                            {
                                new AboutFrame();
                                m.invoke(applicationEvent, Boolean.valueOf(true));
                            }
                            if (method.getName().equals("handleOpenFile"))
                            {
                                new ImageLoaderDialog();
                                m.invoke(applicationEvent, Boolean.valueOf(true));
                            }
                            if (method.getName().equals("handlePreferences"))
                            {
                                new PreferenceFrame(GeneralPreferencePanel.NODE_NAME);
                                m.invoke(applicationEvent, Boolean.valueOf(true));
                            }

                            return null;
                        }
                    });

            Method m;

            m = appClass.getMethod("addApplicationListener", listenerClass);
            m.invoke(app, listener);
            m = appClass.getMethod("setDockIconImage", java.awt.Image.class);
            m.invoke(app, ResourceUtil.IMAGE_ICY_256);
            m = appClass.getMethod("addPreferencesMenuItem");
            m.invoke(app);

            // set menu bar name
            System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Icy");

            // start the fix thread
            fixThread.start();
        }
        catch (Exception e)
        {
            System.err.println("Can't install OSX application wrapper...");
        }
    }

    /**
     * Apple fix live run (fixes specific OS X JVM stuff)
     */
    static void appleFixLiveRun()
    {
        while (true)
        {
            final Toolkit toolkit = Toolkit.getDefaultToolkit();

            // fix memory leak introduced in java 1.6.0_29 in Mac OS X JVM
            // TODO : remove this when issue will be resolved in JVM
            final PropertyChangeListener[] leak = toolkit.getPropertyChangeListeners("apple.awt.contentScaleFactor");

            // remove listener
            for (int i = 0; i < leak.length; i++)
                toolkit.removePropertyChangeListener("apple.awt.contentScaleFactor", leak[i]);

            // no need more...
            ThreadUtil.sleep(500);
        }
    }
}
