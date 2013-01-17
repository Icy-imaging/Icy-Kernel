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
package icy.network;

import icy.common.listener.ProgressListener;
import icy.common.listener.weak.WeakListener;
import icy.preferences.ApplicationPreferences;
import icy.preferences.NetworkPreferences;
import icy.system.Audit;
import icy.system.IcyExceptionHandler;
import icy.system.SystemUtil;
import icy.system.thread.ThreadUtil;
import icy.util.StringUtil;

import java.awt.Desktop;
import java.awt.Desktop.Action;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import sun.misc.BASE64Encoder;

/**
 * @author stephane
 */
public class NetworkUtil
{
    public static final int NO_PROXY = 0;
    public static final int SYSTEM_PROXY = 1;
    public static final int USER_PROXY = 2;

    public interface InternetAccessListener
    {
        /**
         * Internet connection available.
         */
        public void internetUp();

        /**
         * Internet connection no more available.
         */
        public void internetDown();
    }

    /**
     * Weak listener wrapper for NetworkConnectionListener.
     * 
     * @author Stephane
     */
    public static class WeakInternetAccessListener extends WeakListener<InternetAccessListener> implements
            InternetAccessListener
    {
        public WeakInternetAccessListener(InternetAccessListener listener)
        {
            super(listener);
        }

        @Override
        public void removeListener(Object source)
        {
            removeInternetAccessListener(this);
        }

        @Override
        public void internetUp()
        {
            final InternetAccessListener listener = getListener();

            if (listener != null)
                listener.internetUp();
        }

        @Override
        public void internetDown()
        {
            final InternetAccessListener listener = getListener();

            if (listener != null)
                listener.internetDown();
        }
    }

    /**
     * URL
     */
    public static final String WEBSITE_URL = "http://icy.bioimageanalysis.org/";

    static final String REPORT_URL = WEBSITE_URL + "index.php";
    static final String USER_INTERRUPT_MESS = "Load interrupted by user";

    /**
     * Parameters id
     */
    public static final String ID_KERNELVERSION = "kernelVersion";
    public static final String ID_JAVANAME = "javaName";
    public static final String ID_JAVAVERSION = "javaVersion";
    public static final String ID_JAVABITS = "javaBits";
    public static final String ID_OSNAME = "osName";
    public static final String ID_OSVERSION = "osVersion";
    public static final String ID_OSARCH = "osArch";
    public static final String ID_PLUGINCLASSNAME = "pluginClassName";
    public static final String ID_PLUGINVERSION = "pluginVersion";
    public static final String ID_DEVELOPERID = "developerId";
    public static final String ID_ERRORLOG = "errorLog";

    /**
     * List of all listeners on network connection changes.
     */
    private final static Set<InternetAccessListener> listeners = new HashSet<InternetAccessListener>();;

    /**
     * Internet monitor
     */
    public static final Thread internetMonitor = new Thread(new Runnable()
    {
        @Override
        public void run()
        {
            while (true)
            {
                boolean up = false;
                int retry = 5;

                while (!up && (retry > 0))
                {
                    URLConnection urlConnection;

                    try
                    {
                        urlConnection = openConnection("http://www.google.com", true, false);
                        if (urlConnection != null)
                        {
                            urlConnection.setConnectTimeout(3000);
                            urlConnection.setReadTimeout(3000);
                            urlConnection.getInputStream();
                            up = true;
                        }
                    }
                    catch (Throwable t)
                    {
                        // ignore
                    }

                    if (!up)
                    {
                        try
                        {
                            urlConnection = openConnection("http://www.java.com", true, false);
                            if (urlConnection != null)
                            {
                                urlConnection.setConnectTimeout(3000);
                                urlConnection.setReadTimeout(3000);
                                urlConnection.getInputStream();
                                up = true;
                            }
                        }
                        catch (Throwable t)
                        {
                            // ignore
                        }
                    }

                    retry--;
                    ThreadUtil.sleep(1000);
                }

                setInternetAccess(up);
                ThreadUtil.sleep(5000);
            }
        }
    }, "Internet monitor");

    /**
     * Internet access up flag
     */
    private static boolean internetAccess;

    public static void init()
    {
        internetAccess = false;

        updateNetworkSetting();

        // start monitor thread
        internetMonitor.setPriority(Thread.MIN_PRIORITY);
        internetMonitor.start();
    }

    /**
     * Update network setting from the actual preferences
     */
    public static void updateNetworkSetting()
    {
        final int proxySetting = NetworkPreferences.getProxySetting();

        if (proxySetting == NO_PROXY)
        {
            // no proxy
            disableProxySetting();
            disableHTTPProxySetting();
            disableHTTPSProxySetting();
            disableFTPProxySetting();
            disableSOCKSProxySetting();
            disableSystemProxy();
        }
        else if (proxySetting == SYSTEM_PROXY)
        {
            // system proxy
            disableProxySetting();
            disableHTTPProxySetting();
            disableHTTPSProxySetting();
            disableFTPProxySetting();
            disableSOCKSProxySetting();
            enableSystemProxy();
        }
        else
        {
            String host;

            // manual proxy
            disableSystemProxy();

            // HTTP proxy (use it as general proxy)
            host = NetworkPreferences.getProxyHTTPHost();
            if (!StringUtil.isEmpty(host))
            {
                final int port = NetworkPreferences.getProxyHTTPPort();

                setProxyHost(host);
                setProxyPort(port);
                setHTTPProxyHost(host);
                setHTTPProxyPort(port);

                enableProxySetting();
                enableHTTPProxySetting();
            }
            else
            {
                disableProxySetting();
                disableHTTPProxySetting();
            }

            // HTTPS proxy
            host = NetworkPreferences.getProxyHTTPSHost();
            if (!StringUtil.isEmpty(host))
            {
                setHTTPSProxyHost(host);
                setHTTPSProxyPort(NetworkPreferences.getProxyHTTPSPort());
                enableHTTPSProxySetting();
            }
            else
                disableHTTPSProxySetting();

            // FTP proxy
            host = NetworkPreferences.getProxyFTPHost();
            if (!StringUtil.isEmpty(host))
            {
                setFTPProxyHost(host);
                setFTPProxyPort(NetworkPreferences.getProxyFTPPort());
                enableFTPProxySetting();
            }
            else
                disableFTPProxySetting();

            // SOCKS proxy
            host = NetworkPreferences.getProxySOCKSHost();
            if (!StringUtil.isEmpty(host))
            {
                setSOCKSProxyHost(host);
                setSOCKSProxyPort(NetworkPreferences.getProxySOCKSPort());
                enableSOCKSProxySetting();
            }
            else
                disableSOCKSProxySetting();
        }
    }

    static void setInternetAccess(boolean value)
    {
        if (internetAccess != value)
        {
            internetAccess = value;

            fireInternetConnectionEvent(value);

            // process id audit
            Audit.processIdAudit();
        }
    }

    private static void fireInternetConnectionEvent(boolean value)
    {
        if (value)
        {
            for (InternetAccessListener l : listeners)
                l.internetUp();
        }
        else
        {
            for (InternetAccessListener l : listeners)
                l.internetDown();
        }
    }

    /**
     * Adds a new listener on internet access change.
     */
    public static void addInternetAccessListener(InternetAccessListener listener)
    {
        listeners.add(listener);
    }

    /**
     * Removes a listener on internet access change.
     */
    public static void removeInternetAccessListener(InternetAccessListener listener)
    {
        listeners.remove(listener);
    }

    /**
     * Returns true if we currently have Internet connection.
     */
    public static boolean hasInternetAccess()
    {
        return internetAccess;
    }

    /**
     * @deprecated Uses {@link #hasInternetAccess()} instead.
     */
    @Deprecated
    public static boolean hasInternetConnection()
    {
        return hasInternetAccess();
    }

    /**
     * Open an URL in the default system browser
     */
    public static boolean openBrowser(String url)
    {
        return openBrowser(URLUtil.getURL(url));
    }

    /**
     * Open an URL in the default system browser
     */
    public static boolean openBrowser(URL url)
    {
        if (url == null)
            return false;

        try
        {
            return openBrowser(url.toURI());
        }
        catch (URISyntaxException e)
        {
            // use other method
            return systemOpenBrowser(url.toString());
        }
    }

    /**
     * Open an URL in the default system browser
     */
    public static boolean openBrowser(URI uri)
    {
        if (uri == null)
            return false;

        final Desktop desktop = SystemUtil.getDesktop();

        if ((desktop != null) && desktop.isSupported(Action.BROWSE))
        {
            try
            {
                desktop.browse(uri);
                return true;
            }
            catch (IOException e)
            {
                // ignore
            }
        }

        // not
        return systemOpenBrowser(uri.toString());
    }

    /**
     * Open an URL in the default system browser (low level method)
     */
    private static boolean systemOpenBrowser(String url)
    {
        if (StringUtil.isEmpty(url))
            return false;

        try
        {
            if (SystemUtil.isMac())
            {
                Class<?> fileMgr = Class.forName("com.apple.eio.FileManager");
                Method openURL = fileMgr.getDeclaredMethod("openURL", new Class[] {String.class});
                openURL.invoke(null, new Object[] {url});
            }
            else if (SystemUtil.isWindow())
                Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url);
            else
            {
                // assume Unix or Linux
                String[] browsers = {"firefox", "opera", "konqueror", "epiphany", "mozilla", "netscape"};
                String browser = null;
                for (int count = 0; count < browsers.length && browser == null; count++)
                {
                    if (Runtime.getRuntime().exec("which " + browsers[count]).waitFor() == 0)
                        browser = browsers[count];
                }
                if (browser == null)
                    throw new Exception("Could not find web browser");

                Runtime.getRuntime().exec(new String[] {browser, url});
            }

            return true;
        }
        catch (Exception e)
        {
            System.err.println("Error while opening system browser :\n" + e.toString());
            return false;
        }
    }

    /**
     * @deprecated Uses {@link #openBrowser(String)} instead.
     */
    @Deprecated
    public static void openURL(String url)
    {
        openBrowser(url);
    }

    /**
     * @deprecated Uses {@link #openBrowser(URL)} instead.
     */
    @Deprecated
    public static void openURL(URL url)
    {
        openBrowser(url);
    }

    /**
     * @deprecated Uses {@link #openBrowser(URI)} instead.
     */
    @Deprecated
    public static void openURL(URI uri)
    {
        openBrowser(uri);
    }

    /**
     * Download data from specified URL string and return it as an array of byte
     */
    public static byte[] download(String path, ProgressListener listener, boolean displayError)
    {
        return download(path, null, null, listener, displayError);
    }

    /**
     * Download data from specified URL string and return it as an array of byte
     * Process authentication process if login / pass are not null.
     */
    public static byte[] download(String path, String login, String pass, ProgressListener listener,
            boolean displayError)
    {
        final File file = new File(path);
        if (file.exists())
            // authentication not supported on file download
            return download(file, listener, displayError);

        final URL url = URLUtil.getURL(path);
        if (url != null)
            return download(url, login, pass, listener, displayError);

        if (displayError)
            System.out.println("Can't download '" + path + "', incorrect path !");

        return null;
    }

    /**
     * Download data from specified URL and return it as an array of byte
     */
    public static byte[] download(URL url, ProgressListener listener, boolean displayError)
    {
        return download(url, null, null, listener, displayError);
    }

    /**
     * Download data from specified URL and return it as an array of byte.
     * Process authentication process if login / pass are not null.
     */
    public static byte[] download(URL url, String login, String pass, ProgressListener listener, boolean displayError)
    {
        // check if this is a file
        if ((url != null) && URLUtil.isFileURL(url))
        {
            try
            {
                return download(new File(url.toURI()), listener, displayError);
            }
            catch (URISyntaxException e)
            {
                if (displayError)
                    System.out.println("Can't download '" + url + "', incorrect path !");

                return null;
            }
        }

        // disable cache
        final URLConnection uc = openConnection(url, true, displayError);
        // process authentication if needed
        if (!(StringUtil.isEmpty(login) || StringUtil.isEmpty(pass)))
            setAuthentication(uc, login, pass);
        // get input stream with coherence verification
        final InputStream ip = getInputStream(uc, displayError);
        if (ip != null)
        {
            try
            {
                return download(ip, uc.getContentLength(), listener);
            }
            catch (Exception e)
            {
                if (displayError)
                {
                    final String urlString = url.toString();

                    // obfuscation
                    System.out.println("Error while downloading from '" + urlString + "' :");
                    IcyExceptionHandler.showErrorMessage(e, false, false);

                    return null;
                }
            }
        }

        return null;
    }

    /**
     * Download data from File and return it as an array of byte
     */
    public static byte[] download(File f, ProgressListener listener, boolean displayError)
    {
        if (!f.exists())
        {
            System.err.println("File not found : " + f.getPath());
            return null;
        }

        try
        {
            return download(new FileInputStream(f), f.length(), listener);
        }
        catch (Exception e)
        {
            if (displayError)
            {
                System.out.println("NetworkUtil.download('" + f.getPath() + "',...) error :");
                IcyExceptionHandler.showErrorMessage(e, false, false);
            }
            return null;
        }
    }

    /**
     * Download data from specified InputStream and return it as an array of byte
     */
    public static byte[] download(InputStream in, long len, ProgressListener listener) throws IOException
    {
        final int READ_BLOCKSIZE = 64 * 1024;
        final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        // read per block of 64 KB
        final byte[] data = new byte[READ_BLOCKSIZE];

        try
        {
            int off = 0;
            int count = 0;
            while (count >= 0)
            {
                count = in.read(data);
                if (count < 0)
                {
                    // unexpected length
                    if ((len != -1) && (off != len))
                        throw new EOFException();
                }
                else
                    off += count;

                // copy to dynamic buffer
                if (count > 0)
                    buffer.write(data, 0, count);

                if (listener != null)
                {
                    // download canceled ?
                    if (!listener.notifyProgress(off, len))
                    {
                        in.close();
                        System.out.println(USER_INTERRUPT_MESS);
                        return null;
                    }
                }
            }
        }
        finally
        {
            in.close();
        }

        return buffer.toByteArray();
    }

    /**
     * Download data from specified InputStream and return it as an array of byte
     */
    public static byte[] download(InputStream in) throws IOException
    {
        return download(in, -1, null);
    }

    /**
     * Returns a new {@link URLConnection} from specified URL (null if an error occurred).
     * 
     * @param url
     *        url to connect.
     * @param login
     *        login if the connection requires authentication.<br>
     *        Set it to null if no authentication needed.
     * @param pass
     *        login if the connection requires authentication.
     *        Set it to null if no authentication needed.
     * @param disableCache
     *        Disable proxy cache if any.
     * @param displayError
     *        Display error message in console if something wrong happen.
     */
    public static URLConnection openConnection(URL url, String login, String pass, boolean disableCache,
            boolean displayError)
    {
        try
        {
            final URLConnection uc = url.openConnection();

            if (disableCache)
                disableCache(uc);

            // authentication
            if (!StringUtil.isEmpty(login) && !StringUtil.isEmpty(pass))
                NetworkUtil.setAuthentication(uc, login, pass);

            return uc;
        }
        catch (IOException e)
        {
            if (displayError)
            {
                System.out.println("NetworkUtil.openConnection('" + url + "',...) error :");
                IcyExceptionHandler.showErrorMessage(e, false, false);
            }
        }

        return null;
    }

    /**
     * Returns a new {@link URLConnection} from specified URL (null if an error occurred).
     * 
     * @param url
     *        url to connect.
     * @param auth
     *        Authentication informations.
     * @param disableCache
     *        Disable proxy cache if any.
     * @param displayError
     *        Display error message in console if something wrong happen.
     */
    public static URLConnection openConnection(URL url, AuthenticationInfo auth, boolean disableCache,
            boolean displayError)
    {
        if ((auth != null) && (auth.isEnabled()))
            return openConnection(url, auth.getLogin(), auth.getPassword(), disableCache, displayError);

        return openConnection(url, null, null, disableCache, displayError);
    }

    /**
     * Returns a new {@link URLConnection} from specified URL (null if an error occurred).
     * 
     * @param url
     *        url to connect.
     * @param disableCache
     *        Disable proxy cache if any.
     * @param displayError
     *        Display error message in console if something wrong happen.
     */
    public static URLConnection openConnection(URL url, boolean disableCache, boolean displayError)
    {
        return openConnection(url, null, null, disableCache, displayError);
    }

    /**
     * Returns a new {@link URLConnection} from specified path.<br>
     * Returns null if an error occurred.
     * 
     * @param path
     *        path to connect.
     * @param disableCache
     *        Disable proxy cache if any.
     * @param displayError
     *        Display error message in console if something wrong happen.
     */
    public static URLConnection openConnection(String path, boolean disableCache, boolean displayError)
    {
        return openConnection(URLUtil.getURL(path), disableCache, displayError);
    }

    /**
     * Returns a new {@link InputStream} from specified {@link URLConnection} (null if an error
     * occurred).
     * 
     * @param uc
     *        URLConnection object.
     * @param displayError
     *        Display error message in console if something wrong happen.
     */
    public static InputStream getInputStream(URLConnection uc, boolean displayError)
    {
        if (uc != null)
        {
            try
            {
                final URL prevUrl = uc.getURL();
                final InputStream ip = uc.getInputStream();

                // we have to test that as sometime url are automatically modified / fixed by host!
                if (!uc.getURL().toString().toLowerCase().equals(prevUrl.toString().toLowerCase()))
                {
                    // TODO : do something better
                    System.out.println("Host URL change rejected : " + prevUrl.toString() + " --> "
                            + uc.getURL().toString());
                    return null;
                }

                return ip;
            }
            catch (IOException e)
            {
                if (displayError)
                {
                    if (!hasInternetAccess())
                        System.out.println("You are not connected to internet.");
                    else
                    {
                        String urlString = uc.getURL().toString();

                        // obfuscation
                        if (urlString.startsWith(ApplicationPreferences.getUpdateRepositoryBase()))
                        {
                            if (e instanceof FileNotFoundException)
                                System.out.println("Update site URL does not exists or file '" + uc.getURL().getPath()
                                        + "' does not exists.");
                            else if (e.getMessage().indexOf("HTTP response code: 500 ") != -1)
                                System.out.println("Network error : can't connect to update site");
                            else
                                System.out.println("Can't connect to update site...");
                        }
                        else
                        {
                            urlString = "'" + urlString + "'";

                            if (e instanceof FileNotFoundException)
                                System.out.println("Address " + urlString + " does not exists.");
                            else if (e.getMessage().indexOf("HTTP response code: 500 ") != -1)
                                System.out.println("Network error : can't connect to " + urlString);
                            else
                                System.out.println("NetworkUtil.getInputStream(" + urlString + ",...) error :");

                            IcyExceptionHandler.showErrorMessage(e, false, false);
                        }
                    }
                }
            }
        }

        return null;
    }

    /**
     * Returns a new {@link InputStream} from specified URL (null if an error occurred).
     * 
     * @param url
     *        url we want to connect and retrieve the InputStream.
     * @param login
     *        login if the connection requires authentication.<br>
     *        Set it to null if no authentication needed.
     * @param pass
     *        login if the connection requires authentication.
     *        Set it to null if no authentication needed.
     * @param disableCache
     *        Disable proxy cache if any.
     * @param displayError
     *        Display error message in console if something wrong happen.
     */
    public static InputStream getInputStream(URL url, String login, String pass, boolean disableCache,
            boolean displayError)
    {
        if (url != null)
        {
            try
            {
                return getInputStream(openConnection(url, login, pass, disableCache, displayError), displayError);
            }
            catch (Exception e)
            {
                if (displayError)
                {
                    System.out.println("NetworkUtil.getInputStream(" + url + ", ...) error :");
                    IcyExceptionHandler.showErrorMessage(e, false, false);
                }
            }
        }

        return null;
    }

    /**
     * Returns a new {@link InputStream} from specified URL (null if an error occurred).
     * 
     * @param url
     *        url we want to connect and retrieve the InputStream.
     * @param auth
     *        Authentication informations.
     * @param disableCache
     *        Disable proxy cache if any.
     * @param displayError
     *        Display error message in console if something wrong happen.
     */
    public static InputStream getInputStream(URL url, AuthenticationInfo auth, boolean disableCache,
            boolean displayError)
    {
        if ((auth != null) && (auth.isEnabled()))
            return getInputStream(url, auth.getLogin(), auth.getPassword(), disableCache, displayError);

        return getInputStream(url, null, null, disableCache, displayError);
    }

    public static void disableCache(URLConnection uc)
    {
        uc.setDefaultUseCaches(false);
        uc.setUseCaches(false);
        uc.setRequestProperty("Cache-Control", "no-cache");
        uc.setRequestProperty("Pragma", "no-cache");
    }

    /**
     * Process authentication on specified {@link URLConnection} with specified login and pass.
     */
    public static void setAuthentication(URLConnection uc, String login, String pass)
    {
        final String req = login + ":" + pass;
        final String encoded = new BASE64Encoder().encode(req.getBytes());

        uc.setRequestProperty("Authorization", "Basic " + encoded);
    }

    public static String getContentString(HashMap<String, String> values)
    {
        String result = "";

        for (Entry<String, String> entry : values.entrySet())
        {
            try
            {
                final String key = entry.getKey();

                if (!StringUtil.isEmpty(key))
                {
                    final String value = entry.getValue();

                    result += "&" + URLEncoder.encode(key, "UTF-8") + "=";

                    if (!StringUtil.isEmpty(value))
                        result += URLEncoder.encode(value, "UTF-8");
                }
            }
            catch (UnsupportedEncodingException e)
            {
                // no encoding
                result += "&" + entry.getKey() + "=" + entry.getValue();
            }
        }

        // remove the initial "&" character
        return result.substring(1);
    }

    public static String postData(String target, HashMap<String, String> values, String login, String pass)
            throws IOException
    {
        return postData(target, getContentString(values), login, pass);
    }

    public static String postData(String target, String content, String login, String pass) throws IOException
    {
        String response = "";

        final URLConnection uc = openConnection(target, true, true);

        if (uc == null)
            return null;

        // set connection parameters
        uc.setDoInput(true);
        uc.setDoOutput(true);

        // authentication needed ?
        if (login != null)
            setAuthentication(uc, login, pass);

        // make server believe we are form data...
        uc.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

        final DataOutputStream out = new DataOutputStream(uc.getOutputStream());

        // write out the bytes of the content string to the stream.
        out.writeBytes(content);

        out.flush();
        out.close();

        // read response from the input stream.
        final InputStream inStream = getInputStream(uc, false);
        if (inStream == null)
            return null;

        final BufferedReader in = new BufferedReader(new InputStreamReader(inStream));

        try
        {
            String temp;
            while ((temp = in.readLine()) != null)
                response += temp + "\n";
        }
        finally
        {
            in.close();
        }

        return response;
    }

    public static String postData(String target, HashMap<String, String> values) throws IOException
    {
        return postData(target, values, null, null);
    }

    public static String postData(String target, String content) throws IOException
    {
        return postData(target, content, null, null);
    }

    /**
     * Send report (asynchronous processing)<br>
     * 
     * @param values
     *        list of <key,value>
     */
    public static void report(final HashMap<String, String> values)
    {
        ThreadUtil.bgRun(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    if (postData(REPORT_URL, values) == null)
                        System.out.println("Error while reporting data, verifying your internet connection.");
                }
                catch (IOException e)
                {
                    System.out.println("Error while reporting data :");
                    IcyExceptionHandler.showErrorMessage(e, false, false);
                }
            }
        });
    }

    public static void enableSystemProxy()
    {
        SystemUtil.setProperty("java.net.useSystemProxies", "true");
    }

    public static void disableSystemProxy()
    {
        SystemUtil.setProperty("java.net.useSystemProxies", "false");
    }

    public static void enableProxySetting()
    {
        SystemUtil.setProperty("proxySet", "true");
    }

    public static void disableProxySetting()
    {
        SystemUtil.setProperty("proxySet", "false");
    }

    public static void enableHTTPProxySetting()
    {
        SystemUtil.setProperty("http.proxySet", "true");
    }

    public static void disableHTTPProxySetting()
    {
        SystemUtil.setProperty("http.proxySet", "false");
    }

    public static void enableHTTPSProxySetting()
    {
        SystemUtil.setProperty("https.proxySet", "true");
    }

    public static void disableHTTPSProxySetting()
    {
        SystemUtil.setProperty("https.proxySet", "false");
    }

    public static void enableFTPProxySetting()
    {
        SystemUtil.setProperty("ftp.proxySet", "true");
    }

    public static void disableFTPProxySetting()
    {
        SystemUtil.setProperty("ftp.proxySet", "false");
    }

    public static void enableSOCKSProxySetting()
    {
        SystemUtil.setProperty("socksProxySet", "true");
    }

    public static void disableSOCKSProxySetting()
    {
        SystemUtil.setProperty("socksProxySet", "false");
    }

    public static void setProxyHost(String host)
    {
        SystemUtil.setProperty("proxy.server", host);
    }

    public static void setProxyPort(int port)
    {
        SystemUtil.setProperty("proxy.port", Integer.toString(port));
    }

    public static void setHTTPProxyHost(String host)
    {
        SystemUtil.setProperty("http.proxyHost", host);
    }

    public static void setHTTPProxyPort(int port)
    {
        SystemUtil.setProperty("http.proxyPort", Integer.toString(port));
    }

    public static void setHTTPProxyUser(String user)
    {
        SystemUtil.setProperty("http.proxyUser", user);
    }

    public static void setHTTPProxyPassword(String password)
    {
        SystemUtil.setProperty("http.proxyPassword", password);
    }

    public static void setHTTPSProxyHost(String host)
    {
        SystemUtil.setProperty("https.proxyHost", host);
    }

    public static void setHTTPSProxyPort(int port)
    {
        SystemUtil.setProperty("https.proxyPort", Integer.toString(port));
    }

    public static void setHTTPSProxyUser(String user)
    {
        SystemUtil.setProperty("https.proxyUser", user);
    }

    public static void setHTTPSProxyPassword(String password)
    {
        SystemUtil.setProperty("https.proxyPassword", password);
    }

    public static void setFTPProxyHost(String host)
    {
        SystemUtil.setProperty("ftp.proxyHost", host);
    }

    public static void setFTPProxyPort(int port)
    {
        SystemUtil.setProperty("ftp.proxyPort", Integer.toString(port));
    }

    public static void setFTPProxyUser(String user)
    {
        SystemUtil.setProperty("ftp.proxyUser", user);
    }

    public static void setFTPProxyPassword(String password)
    {
        SystemUtil.setProperty("ftp.proxyPassword", password);
    }

    public static void setSOCKSProxyHost(String host)
    {
        SystemUtil.setProperty("socksProxyHost", host);
    }

    public static void setSOCKSProxyPort(int port)
    {
        SystemUtil.setProperty("socksProxyPort", Integer.toString(port));
    }

    public static void setSOCKSProxyUser(String user)
    {
        SystemUtil.setProperty("socksProxyUser", user);
    }

    public static void setSOCKSProxyPassword(String password)
    {
        SystemUtil.setProperty("socksProxyPassword", password);
    }

    public static String getProxyHost()
    {
        return SystemUtil.getProperty("proxy.server");
    }

    public static int getProxyPort()
    {
        try
        {
            return Integer.parseInt(SystemUtil.getProperty("proxy.port"));
        }
        catch (NumberFormatException e)
        {
            return 0;
        }
    }

    public static String getHTTPProxyHost()
    {
        return SystemUtil.getProperty("http.proxyHost");
    }

    public static int getHTTPProxyPort()
    {
        try
        {
            return Integer.parseInt(SystemUtil.getProperty("http.proxyPort"));
        }
        catch (NumberFormatException e)
        {
            return 0;
        }
    }

    public static String getHTTPSProxyHost()
    {
        return SystemUtil.getProperty("https.proxyHost");
    }

    public static int getHTTPSProxyPort()
    {
        try
        {
            return Integer.parseInt(SystemUtil.getProperty("https.proxyPort"));
        }
        catch (NumberFormatException e)
        {
            return 0;
        }
    }

    public static String getFTPProxyHost()
    {
        return SystemUtil.getProperty("ftp.proxyHost");
    }

    public static int getFTPProxyPort()
    {
        try
        {
            return Integer.parseInt(SystemUtil.getProperty("ftp.proxyPort"));
        }
        catch (NumberFormatException e)
        {
            return 0;
        }
    }

    public static String getSOCKSProxyHost()
    {
        return SystemUtil.getProperty("socksProxyHost");
    }

    public static int getSOCKSProxyPort()
    {
        try
        {
            return Integer.parseInt(SystemUtil.getProperty("socksProxyPort"));
        }
        catch (NumberFormatException e)
        {
            return 0;
        }
    }

}
