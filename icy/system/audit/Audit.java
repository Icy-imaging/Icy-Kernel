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
package icy.system.audit;

import icy.file.FileUtil;
import icy.gui.frame.progress.CancelableProgressFrame;
import icy.gui.main.MainFrame;
import icy.main.Icy;
import icy.network.NetworkUtil;
import icy.plugin.abstract_.Plugin;
import icy.preferences.ApplicationPreferences;
import icy.preferences.GeneralPreferences;
import icy.preferences.XMLPreferences;
import icy.system.IcyExceptionHandler;
import icy.system.SystemUtil;
import icy.util.StringUtil;
import icy.util.XMLUtil;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * General audit tools class.
 * 
 * @author Stephane
 */
public class Audit
{
    // network URL
    static final String URL_REGISTER = NetworkUtil.WEBSITE_URL + "register/registerClient.php?";
    static final String URL_LINK_USER = NetworkUtil.WEBSITE_URL + "register/linkUser.php?";
    static final String URL_GET_USER_INFO = NetworkUtil.WEBSITE_URL + "register/getLinkedUserInfo.php?";
    static final String URL_AUDIT_VERSION = NetworkUtil.WEBSITE_URL + "register/auditVersion.php?";
    static final String URL_AUDIT_PLUGIN = NetworkUtil.WEBSITE_URL + "register/auditPlugin.php?";

    // prefs & network id
    static final String ID_REQUEST = "requestId";
    static final String ID_ACTION = "action";
    static final String ID_ICY_ID = "IcyId";
    static final String ID_CLIENT_ARCH = "clientArch";
    static final String ID_CLIENT_ID = "clientId";
    static final String ID_CLIENT_VERSION = "clientVersion";
    static final String ID_CLIENT_CPUNUMBER = "clientCpuNumber";
    static final String ID_CLIENT_TOTAL_MEMORY = "clientTotalMemory";
    static final String ID_CLIENT_MAXJAVA_MEMORY = "clientMaxJavaMemory";
    static final String ID_JAVA_NAME = "javaName";
    static final String ID_JAVA_VERSION = "javaVersion";
    static final String ID_JAVA_ARCH = "javaArch";
    static final String ID_LAST_UPLOAD_DATE = "lastUploadDate";

    // xml id
    static final String XMLID_CLIENT_ID_REQUESTED = "client_id_requested";
    static final String XMLID_USER_LOGIN = "user_login";
    static final String XMLID_USER_NAME = "user_name";

    // directly use application preferences here
    static XMLPreferences prefs;

    private static AuditStorage storage;
    private static boolean initialized = false;
    private static boolean auditDone;

    /**
     * Audit process on application start.<br>
     * Check id, register...
     */
    public static synchronized void prepare()
    {
        if (initialized)
            return;

        // get preferences
        prefs = ApplicationPreferences.getPreferences();

        // probably a new installation --> need to reset id
        if (needToResetId())
        {
            // reset user info if needed
            unlinkUser();
            // reset id
            ApplicationPreferences.setId(-1);
        }

        // store current infos
        storeInfos();
        // init audit storage
        storage = new AuditStorage();

        final int id = ApplicationPreferences.getId();

        // id assigned ?
        if (id != -1)
        {
            final long currentTime = System.currentTimeMillis();
            final long dayInterval = 1000 * 60 * 60 * 24;

            // upload each 24 hours
            if (currentTime > (prefs.getLong(ID_LAST_UPLOAD_DATE, 0L) + dayInterval))
            {
                // upload usage statistics
                storage.upload(id);
                // save upload time whatever happened
                prefs.putLong(ID_LAST_UPLOAD_DATE, System.currentTimeMillis());
            }
        }

        initialized = true;
        auditDone = false;
    }

    /**
     * Audit process on network connection
     */
    public static void onConnect()
    {
        prepare();

        if (!auditDone)
            processIdAudit();

        updateUserLink();

        final MainFrame frame = Icy.getMainInterface().getMainFrame();
        // refresh user infos
        if (frame != null)
            frame.refreshUserInfos();
    }

    /**
     * Save audit data
     */
    public static void save()
    {
        // save audit data
        if (initialized)
            storage.save();
    }

    /**
     * Plugin launched event audit
     */
    public static void pluginLaunched(Plugin plugin)
    {
        prepare();
        storage.pluginLaunched(plugin);
    }

    /**
     * Plugin instancied event audit
     */
    public static void pluginInstancied(Plugin plugin)
    {
        prepare();
        storage.pluginInstancied(plugin);
    }

    /**
     * Returns <code>true</code> if we need to reset the internal id (usually mean new installation)
     */
    private static boolean needToResetId()
    {
        if (!StringUtil.equals(ApplicationPreferences.getOs(), SystemUtil.getOSArchIdString()))
            return true;

        final int cpu = prefs.getInt(ID_CLIENT_CPUNUMBER, 0);
        final long mem = prefs.getLong(ID_CLIENT_TOTAL_MEMORY, 0);
        final String appFolder = ApplicationPreferences.getAppFolder();

        // ignore difference on first launch else it will regenerate id for everyone
        if ((cpu != 0) && (cpu != SystemUtil.getNumberOfCPUs()))
            return true;
        if ((mem != 0) && (mem != SystemUtil.getTotalMemory()))
            return true;
        if (!StringUtil.isEmpty(appFolder) && !StringUtil.equals(appFolder, FileUtil.getApplicationDirectory()))
            return true;

        return false;
    }

    /**
     * Store system and application informations in preferences
     */
    private static void storeInfos()
    {
        ApplicationPreferences.setOs(SystemUtil.getOSArchIdString());
        prefs.putInt(ID_CLIENT_CPUNUMBER, SystemUtil.getNumberOfCPUs());
        prefs.putLong(ID_CLIENT_TOTAL_MEMORY, SystemUtil.getTotalMemory());
        ApplicationPreferences.setAppFolder(FileUtil.getApplicationDirectory());
    }

    private static void processIdAudit()
    {
        final Map<String, String> values = new HashMap<String, String>();
        final int id = ApplicationPreferences.getId();

        values.put(ID_CLIENT_ARCH, SystemUtil.getOSArchIdString());
        values.put(ID_CLIENT_VERSION, Icy.version.toString());

        // need to register
        if (id == -1)
        {
            // ask for registration
            values.put(ID_REQUEST, "1");

            final Document doc = XMLUtil.loadDocument(URL_REGISTER + NetworkUtil.getContentString(values));

            if (doc != null)
            {
                final Node root = XMLUtil.getRootElement(doc);
                final int newId = XMLUtil.getElementIntValue(root, XMLID_CLIENT_ID_REQUESTED, -1);

                // valid id --> save it
                if (newId != -1)
                    ApplicationPreferences.setId(newId);
            }
        }
        else
        {
            // just audit infos
            values.put(ID_CLIENT_ID, Integer.toString(id));

            values.put(ID_CLIENT_CPUNUMBER, Integer.toString(SystemUtil.getNumberOfCPUs()));
            values.put(ID_CLIENT_TOTAL_MEMORY, Long.toString(SystemUtil.getTotalMemory() / 10485760L));
            values.put(ID_CLIENT_MAXJAVA_MEMORY, Long.toString(SystemUtil.getJavaMaxMemory() / 10485760L));
            values.put(ID_JAVA_NAME, SystemUtil.getJavaName());
            values.put(ID_JAVA_VERSION, SystemUtil.getJavaVersion());
            values.put(ID_JAVA_ARCH, Integer.toString(SystemUtil.getJavaArchDataModel()));

            try
            {
                NetworkUtil.postData(URL_AUDIT_VERSION, values);
            }
            catch (IOException e)
            {
                // silent fail...
                // IcyExceptionHandler.showErrorMessage(e, false, false);
            }
        }

        auditDone = true;
    }

    private static Map<String, String> getIdParam()
    {
        final int id = ApplicationPreferences.getId();

        // id ok ?
        if (id != -1)
        {
            final Map<String, String> values = new HashMap<String, String>();

            // set id
            values.put(ID_ICY_ID, Integer.toString(id));

            return values;
        }

        return null;
    }

    public static void updateUserLink()
    {
        final Map<String, String> params = getIdParam();

        // id param ok ?
        if (params != null)
        {
            // and retrieve user infos
            final Document doc = XMLUtil.loadDocument(URL_GET_USER_INFO + NetworkUtil.getContentString(params));

            if (doc != null)
            {
                final Node root = XMLUtil.getRootElement(doc);

                // set attached user login and name
                GeneralPreferences.setUserLogin(XMLUtil.getElementValue(root, XMLID_USER_LOGIN, ""));
                GeneralPreferences.setUserName(XMLUtil.getElementValue(root, XMLID_USER_NAME, ""));
            }
        }
    }

    public static boolean isUserLinked()
    {
        return !StringUtil.isEmpty(GeneralPreferences.getUserLogin());
    }

    public static void linkUser()
    {
        final int id = ApplicationPreferences.getId();

        // id ok ?
        if (id != -1)
        {
            // launch browser with link identity request
            NetworkUtil.openBrowser(URL_LINK_USER + ID_ICY_ID + "=" + id + "&" + ID_ACTION + "=link");

            if (!Icy.getMainInterface().isHeadLess())
            {
                // display linking in progress
                new Thread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        final CancelableProgressFrame waitFrame = new CancelableProgressFrame(
                                "Waiting for user to link account...");

                        while (!Thread.interrupted() && !waitFrame.isCancelRequested())
                        {
                            try
                            {
                                Thread.sleep(2000);
                                updateUserLink();

                                // user linked !
                                if (isUserLinked())
                                {
                                    // stop wait
                                    waitFrame.cancel();

                                    // refresh user infos
                                    final MainFrame frame = Icy.getMainInterface().getMainFrame();
                                    if (frame != null)
                                        frame.refreshUserInfos();
                                }
                            }
                            catch (InterruptedException e)
                            {
                                waitFrame.cancel();
                            }
                        }

                        // close wait frame
                        waitFrame.close();
                    }
                }).start();
            }
        }
    }

    public static void unlinkUser()
    {
        final Map<String, String> params = getIdParam();

        // id param ok ?
        if (params != null)
        {
            // set action
            params.put(ID_ACTION, "unlink");

            try
            {
                // and post
                NetworkUtil.postData(URL_LINK_USER, params);
            }
            catch (IOException e)
            {
                // can't unlink on web site, not a big deal...
                System.err.print("Warning: cannot unlink online user infos.");
                IcyExceptionHandler.showErrorMessage(e, false, false);
            }
        }

        // reset attached user login and name
        GeneralPreferences.setUserLogin("");
        GeneralPreferences.setUserName("");
    }
}
