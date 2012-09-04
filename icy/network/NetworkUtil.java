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
import java.net.NetworkInterface;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.security.Security;
import java.util.Enumeration;
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
    public interface NetworkConnectionListener
    {
        /**
         * Network interface connected.
         */
        public void networkConnected();

        /**
         * Network interface disconnected.
         */
        public void networkDisconnected();

        /**
         * Internet connection available.
         */
        public void internetConnected();

        /**
         * Internet connection no more available.
         */
        public void internetDisconnected();
    }

    /**
     * Weak listener wrapper for NetworkConnectionListener.
     * 
     * @author Stephane
     */
    public static class WeakNetworkConnectionListener extends WeakListener<NetworkConnectionListener> implements
            NetworkConnectionListener
    {
        public WeakNetworkConnectionListener(NetworkConnectionListener listener)
        {
            super(listener);
        }

        @Override
        public void removeListener(Object source)
        {
            removeNetworkConnectionListener(this);
        }

        @Override
        public void networkConnected()
        {
            final NetworkConnectionListener listener = getListener();

            if (listener != null)
                listener.networkConnected();
        }

        @Override
        public void networkDisconnected()
        {
            final NetworkConnectionListener listener = getListener();

            if (listener != null)
                listener.networkDisconnected();
        }

        @Override
        public void internetConnected()
        {
            final NetworkConnectionListener listener = getListener();

            if (listener != null)
                listener.internetConnected();
        }

        @Override
        public void internetDisconnected()
        {
            final NetworkConnectionListener listener = getListener();

            if (listener != null)
                listener.internetDisconnected();
        }
    }

    static final String REPORT_URL = "http://icy.bioimageanalysis.org/index.php";

    public static final String USER_INTERRUPT_MESS = "Load interrupted by user";

    /**
     * List of all listeners on network connection changes.
     */
    private final static Set<NetworkConnectionListener> listeners = new HashSet<NetworkConnectionListener>();

    private static boolean networkConnected;
    private static boolean internetConnected;

    public static final Thread internetMonitor = new Thread(new Runnable()
    {
        @Override
        public void run()
        {
            while (true)
            {
                boolean connected = false;

                // only necessary if network connected
                if (hasNetworkConnection())
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
                            connected = true;
                        }
                    }
                    catch (Throwable t)
                    {
                        // ignore
                    }

                    if (!connected)
                    {
                        try
                        {
                            urlConnection = openConnection("http://www.java.com", true, false);
                            if (urlConnection != null)
                            {
                                urlConnection.setConnectTimeout(3000);
                                urlConnection.setReadTimeout(3000);
                                urlConnection.getInputStream();
                                connected = true;
                            }
                        }
                        catch (Throwable t)
                        {
                            // ignore
                        }
                    }
                }

                setInternetConnected(connected);
                ThreadUtil.sleep(1000);
            }
        }
    }, "Internet monitor");

    public static final Thread networkMonitor = new Thread(new Runnable()
    {
        @Override
        public void run()
        {
            while (true)
            {
                boolean connected = false;

                try
                {
                    final Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();

                    while (interfaces.hasMoreElements() && !connected)
                    {
                        final NetworkInterface interf = interfaces.nextElement();

                        if (!interf.isLoopback() && interf.isUp())
                            connected = true;
                    }
                }
                catch (Throwable t)
                {
                    // ignore
                }

                setNetworkConnected(connected);
                ThreadUtil.sleep(1000);
            }
        }
    }, "Network monitor");

    public static void init()
    {
        networkConnected = false;
        internetConnected = false;

        updateNetworkSetting();

        // start monitors threads
        networkMonitor.start();
        internetMonitor.start();
    }

    /**
     * Update network setting from the actual preferences
     */
    public static void updateNetworkSetting()
    {
        final int proxySetting = NetworkPreferences.getProxySetting();

        if (proxySetting == 0)
        {
            // no proxy
            disableProxySetting();
            disableSystemProxy();
        }
        else if (proxySetting == 1)
        {
            // system proxy
            disableProxySetting();
            enableSystemProxy();
        }
        else
        {
            String host;

            // manual proxy
            enableProxySetting();
            disableSystemProxy();

            // HTTP proxy
            host = NetworkPreferences.getProxyHTTPHost();
            if (!StringUtil.isEmpty(host))
            {
                setHTTPProxyHost(host);
                setHTTPProxyPort(NetworkPreferences.getProxyHTTPPort());
            }
            // HTTPS proxy
            host = NetworkPreferences.getProxyHTTPSHost();
            if (!StringUtil.isEmpty(host))
            {
                setHTTPSProxyHost(host);
                setHTTPSProxyPort(NetworkPreferences.getProxyHTTPSPort());
            }
            // FTP proxy
            host = NetworkPreferences.getProxyFTPHost();
            if (!StringUtil.isEmpty(host))
            {
                setFTPProxyHost(host);
                setFTPProxyPort(NetworkPreferences.getProxyFTPPort());
            }
            // SOCKS proxy
            host = NetworkPreferences.getProxySOCKSHost();
            if (!StringUtil.isEmpty(host))
            {
                setSOCKSProxyHost(host);
                setSOCKSProxyPort(NetworkPreferences.getProxySOCKSPort());
            }
        }
    }

    static void setNetworkConnected(boolean value)
    {
        // force "Internet connected" to false
        if (!value)
            setInternetConnected(false);

        if (networkConnected != value)
        {
            networkConnected = value;
            fireNetworkConnectionEvent(value);
        }
    }

    static void setInternetConnected(boolean value)
    {
        if (internetConnected != value)
        {
            internetConnected = value;

            fireInternetConnectionEvent(value);

            // process id audit
            Audit.processIdAudit();
        }
    }

    private static void fireInternetConnectionEvent(boolean value)
    {
        if (value)
        {
            for (NetworkConnectionListener l : listeners)
                l.internetConnected();
        }
        else
        {
            for (NetworkConnectionListener l : listeners)
                l.internetDisconnected();
        }
    }

    private static void fireNetworkConnectionEvent(boolean value)
    {
        if (value)
        {
            for (NetworkConnectionListener l : listeners)
                l.networkConnected();
        }
        else
        {
            for (NetworkConnectionListener l : listeners)
                l.networkDisconnected();
        }
    }

    /**
     * Adds a new listener on network connection change.
     */
    public static void addNetworkConnectionListener(NetworkConnectionListener skinChangeListener)
    {
        listeners.add(skinChangeListener);
    }

    /**
     * Removes a listener on network connection change.
     */
    public static void removeNetworkConnectionListener(NetworkConnectionListener skinChangeListener)
    {
        listeners.remove(skinChangeListener);
    }

    /**
     * Returns true if we currently have network connection (not necessary Internet).
     */
    public static boolean hasNetworkConnection()
    {
        return networkConnected;
    }

    /**
     * Returns true if we currently have Internet connection.
     */
    public static boolean hasInternetConnection()
    {
        return networkConnected && internetConnected;
    }

    /**
     * Open an URL in the default system browser
     */
    public static void openURL(String url)
    {
        openURL(URLUtil.getURL(url));
    }

    /**
     * Open an URL in the default system browser
     */
    public static void openURL(URL url)
    {
        try
        {
            openURL(url.toURI());
        }
        catch (URISyntaxException e)
        {
            // use other method
            systemOpenURL(url.toString());
        }
    }

    /**
     * Open an URL in the default system browser
     */
    public static void openURL(URI uri)
    {
        final Desktop desktop = SystemUtil.getDesktop();
        boolean done = false;

        if ((desktop != null) && desktop.isSupported(Action.BROWSE))
        {
            try
            {
                desktop.browse(uri);
                done = true;
            }
            catch (IOException e)
            {
                // ignore
            }
        }

        // not
        if (!done)
            systemOpenURL(uri.toString());

    }

    /**
     * Open an URL in the default system browser (low level method)
     */
    private static void systemOpenURL(String url)
    {
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
        }
        catch (Exception e)
        {
            System.err.println("Error while opening system browser :\n" + e.toString());
        }
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
                    if (!hasInternetConnection())
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
                result += "&" + URLEncoder.encode(entry.getKey(), "UTF-8") + "="
                        + URLEncoder.encode(entry.getValue(), "UTF-8");
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

        final URLConnection uc = openConnection(target, true, false);

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
        final BufferedReader in = new BufferedReader(new InputStreamReader(getInputStream(uc, false)));

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
                    postData(REPORT_URL, values);
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
        System.setProperty("java.net.useSystemProxies", "true");
    }

    public static void disableSystemProxy()
    {
        System.setProperty("java.net.useSystemProxies", "false");
    }

    public static void enableProxySetting()
    {
        Security.setProperty("proxySet", "true");
        Security.setProperty("http.proxySet", "true");
        Security.setProperty("https.proxySet", "true");
        Security.setProperty("ftp.proxySet", "true");
    }

    public static void disableProxySetting()
    {
        Security.setProperty("proxySet", "false");
        Security.setProperty("http.proxySet", "false");
        Security.setProperty("https.proxySet", "false");
        Security.setProperty("ftp.proxySet", "false");
    }

    public static void setHTTPProxyHost(String host)
    {
        Security.setProperty("http.proxyHost", host);
    }

    public static void setHTTPProxyPort(int port)
    {
        Security.setProperty("http.proxyPort", Integer.toString(port));
    }

    public static void setHTTPProxyUser(String user)
    {
        Security.setProperty("http.proxyUser", user);
    }

    public static void setHTTPProxyPassword(String password)
    {
        Security.setProperty("http.proxyPassword", password);
    }

    public static void setHTTPSProxyHost(String host)
    {
        Security.setProperty("https.proxyHost", host);
    }

    public static void setHTTPSProxyPort(int port)
    {
        Security.setProperty("https.proxyPort", Integer.toString(port));
    }

    public static void setHTTPSProxyUser(String user)
    {
        Security.setProperty("https.proxyUser", user);
    }

    public static void setHTTPSProxyPassword(String password)
    {
        Security.setProperty("https.proxyPassword", password);
    }

    public static void setFTPProxyHost(String host)
    {
        Security.setProperty("ftp.proxyHost", host);
    }

    public static void setFTPProxyPort(int port)
    {
        Security.setProperty("ftp.proxyPort", Integer.toString(port));
    }

    public static void setFTPProxyUser(String user)
    {
        Security.setProperty("ftp.proxyUser", user);
    }

    public static void setFTPProxyPassword(String password)
    {
        Security.setProperty("ftp.proxyPassword", password);
    }

    public static void setSOCKSProxyHost(String host)
    {
        Security.setProperty("socksProxyHost", host);
    }

    public static void setSOCKSProxyPort(int port)
    {
        Security.setProperty("socksProxyPort", Integer.toString(port));
    }

    public static void setSOCKSProxyUser(String user)
    {
        Security.setProperty("socksProxyUser", user);
    }

    public static void setSOCKSProxyPassword(String password)
    {
        Security.setProperty("socksProxyPassword", password);
    }

    public static String getHTTPProxyHost()
    {
        return Security.getProperty("http.proxyHost");
    }

    public static int getHTTPProxyPort()
    {
        try
        {
            return Integer.parseInt(Security.getProperty("http.proxyPort"));
        }
        catch (NumberFormatException e)
        {
            return 0;
        }
    }

    public static String getHTTPSProxyHost()
    {
        return Security.getProperty("https.proxyHost");
    }

    public static int getHTTPSProxyPort()
    {
        try
        {
            return Integer.parseInt(Security.getProperty("https.proxyPort"));
        }
        catch (NumberFormatException e)
        {
            return 0;
        }
    }

    public static String getFTPProxyHost()
    {
        return Security.getProperty("ftp.proxyHost");
    }

    public static int getFTPProxyPort()
    {
        try
        {
            return Integer.parseInt(Security.getProperty("ftp.proxyPort"));
        }
        catch (NumberFormatException e)
        {
            return 0;
        }
    }

    public static String getSOCKSProxyHost()
    {
        return Security.getProperty("socksProxyHost");
    }

    public static int getSOCKSProxyPort()
    {
        try
        {
            return Integer.parseInt(Security.getProperty("socksProxyPort"));
        }
        catch (NumberFormatException e)
        {
            return 0;
        }
    }

}
