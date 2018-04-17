package icy.network;

import icy.main.Icy;
import icy.math.UnitUtil;
import icy.plugin.PluginDescriptor;
import icy.plugin.PluginDescriptor.PluginIdent;
import icy.plugin.PluginLauncher;
import icy.plugin.PluginLoader;
import icy.plugin.interface_.PluginBundled;
import icy.preferences.ApplicationPreferences;
import icy.system.IcyExceptionHandler;
import icy.system.SystemUtil;
import icy.system.thread.ThreadUtil;
import icy.util.StringUtil;
import icy.util.XMLUtil;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Document;

public class WebInterface
{
    // beta test
    public static final String BASE_URL_BETA = "https://icy.yhello.co/interface/";
    // official
    public static final String BASE_URL = "https://icy.bioimageanalysis.org/interface/";

    public static final String PARAM_ACTION = "action";
    public static final String PARAM_CLIENT_ID = "clientId";
    public static final String PARAM_ID = "id";
    public static final String PARAM_CLASSNAME = "classname";
    public static final String PARAM_FIELD = "field";

    // search specific parameter(s)
    public static final String PARAM_SEARCH = "search";
    public static final String PARAM_POSTTYPE = "pt";

    // bug report specific parameter(s)
    public static final String PARAM_KERNELVERSION = "kernelVersion";
    public static final String PARAM_JAVANAME = "javaName";
    public static final String PARAM_JAVAVERSION = "javaVersion";
    public static final String PARAM_JAVABITS = "javaBits";
    public static final String PARAM_OSNAME = "osName";
    public static final String PARAM_OSVERSION = "osVersion";
    public static final String PARAM_OSARCH = "osArch";
    public static final String PARAM_PLUGINCLASSNAME = "pluginClassName";
    public static final String PARAM_PLUGINVERSION = "pluginVersion";
    public static final String PARAM_DEVELOPERID = "developerId";
    public static final String PARAM_ERRORLOG = "errorLog";

    // action types
    public static final String ACTION_TYPE_SEARCH = "search";
    public static final String ACTION_TYPE_BUGREPORT = "bugReport";
    // search types
    public static final String SEARCH_TYPE_PLUGIN = "plugin";
    public static final String SEARCH_TYPE_SCRIPT = "script";
    public static final String SEARCH_TYPE_PROTOCOL = "protocol";

    /**
     * Process search on the website in a specific resource and return result in a XML Document.
     * 
     * @param text
     *        Text used for the search request, it can contains several words and use operators.<br>
     *        Examples:<br>
     *        <li><i>spot detector</i> : any of word should be present</li>
     *        <li><i>+spot +detector</i> : both words should be present</li>
     *        <li>"spot detector"</i> : the exact expression should be present</li>
     *        <li><i>+"spot detector" -tracking</i> : <i>spot detector</i> should be present and <i>tracking</i> absent</li>
     * @param type
     *        type of resource we want to search in.<br>
     *        Accepted values are:<br>
     *        <li>SEARCH_TYPE_PLUGIN</li>
     *        <li>SEARCH_TYPE_SCRIPT</li>
     *        <li>SEARCH_TYPE_PROTOCOL</li>
     *        <li>null (all resources)</li>
     * @return result in XML Document format
     * @throws UnsupportedEncodingException
     */
    public static Document doSearch(String text, String type) throws UnsupportedEncodingException
    {
        // build request (encode search text in UTF8)
        String request = BASE_URL + " ?" + PARAM_ACTION + "=" + ACTION_TYPE_SEARCH + "&" + PARAM_SEARCH + "="
                + URLEncoder.encode(text, "UTF-8");

        // specific type ?
        if (!StringUtil.isEmpty(type))
            request += "&" + PARAM_POSTTYPE + "=" + type;

        // add client id
        // request += "&" + PARAM_CLIENT_ID + "=2532495";
        request += "&" + PARAM_CLIENT_ID + "=" + ApplicationPreferences.getId();

        // send request to web site and get result
        return XMLUtil.loadDocument(URLUtil.getURL(request), true);
    }

    /**
     * Process search on the website in all resources and return result in a XML Document.
     * 
     * @param text
     *        Text used for the search request, it can contains several words and use operators.<br>
     *        Examples:<br>
     *        <li><i>spot detector</i> : any of word should be present</li>
     *        <li><i>+spot +detector</i> : both words should be present</li>
     *        <li>"spot detector"</i> : the exact expression should be present</li>
     *        <li><i>+"spot detector" -tracking</i> : <i>spot detector</i> should be present and <i>tracking</i> absent</li>
     * @return result in XML Document format
     * @throws UnsupportedEncodingException
     */
    public static Document doSearch(String text) throws UnsupportedEncodingException
    {
        return doSearch(text, null);
    }

    /**
     * Report an error log from a given plugin or developer id to Icy web site.
     * 
     * @param plugin
     *        The plugin responsible of the error or <code>null</code> if the error comes from the
     *        application or if we are not able to get the plugin descriptor.
     * @param devId
     *        The developer id of the plugin responsible of the error when the plugin descriptor was
     *        not found or <code>null</code> if the error comes from the application.
     * @param errorLog
     *        Error log to report.
     */
    public static void reportError(PluginDescriptor plugin, String devId, String errorLog)
    {
        final String icyId;
        final String javaId;
        final String osId;
        final String memory;
        String pluginId;
        String pluginDepsId;
        final Map<String, String> values = new HashMap<String, String>();

        // bug report action
        values.put(PARAM_ACTION, ACTION_TYPE_BUGREPORT);

        // add informations about java / system / OS
        values.put(PARAM_KERNELVERSION, Icy.version.toString());
        values.put(PARAM_JAVANAME, SystemUtil.getJavaName());
        values.put(PARAM_JAVAVERSION, SystemUtil.getJavaVersion());
        values.put(PARAM_JAVABITS, Integer.toString(SystemUtil.getJavaArchDataModel()));
        values.put(PARAM_OSNAME, SystemUtil.getOSName());
        values.put(PARAM_OSVERSION, SystemUtil.getOSVersion());
        values.put(PARAM_OSARCH, SystemUtil.getOSArch());

        icyId = "Icy Version " + Icy.version + "\n";
        javaId = SystemUtil.getJavaName() + " " + SystemUtil.getJavaVersion() + " (" + SystemUtil.getJavaArchDataModel()
                + " bit)\n";
        osId = "Running on " + SystemUtil.getOSName() + " " + SystemUtil.getOSVersion() + " (" + SystemUtil.getOSArch()
                + ")\n";
        memory = "Max java memory : " + UnitUtil.getBytesString(SystemUtil.getJavaMaxMemory()) + "\n";

        if (plugin != null)
        {
            final String className = plugin.getClassName();

            // add plugin informations if available
            values.put(PARAM_PLUGINCLASSNAME, className);
            values.put(PARAM_PLUGINVERSION, plugin.getVersion().toString());

            pluginId = "Plugin " + plugin.toString();
            // determine origin plugin
            PluginDescriptor originPlugin = plugin;

            // bundled plugin ?
            if (plugin.isBundled())
            {
                try
                {
                    // get original plugin
                    originPlugin = PluginLoader
                            .getPlugin(((PluginBundled) PluginLauncher.create(plugin)).getMainPluginClassName());
                    // add bundle info
                    pluginId = "Bundled in " + originPlugin.toString();
                }
                catch (Throwable t)
                {
                    // miss bundle info
                    pluginId = "Bundled plugin (could not retrieve origin plugin)";
                }
            }

            pluginId += "\n\n";

            if (originPlugin.getRequired().size() > 0)
            {
                pluginDepsId = "Dependances:\n";
                for (PluginIdent ident : originPlugin.getRequired())
                {
                    final PluginDescriptor installed = PluginLoader.getPlugin(ident.getClassName());

                    if (installed == null)
                        pluginDepsId += "Class " + ident.getClassName() + " not found !\n";
                    else
                        pluginDepsId += "Plugin " + installed.toString() + " is correctly installed\n";
                }
                pluginDepsId += "\n";
            }
            else
                pluginDepsId = "";
        }
        else
        {
            // no plugin information available
            values.put(PARAM_PLUGINCLASSNAME, "");
            values.put(PARAM_PLUGINVERSION, "");
            pluginId = "";
            pluginDepsId = "";
        }

        // add dev id
        if (StringUtil.isEmpty(devId))
            values.put(PARAM_DEVELOPERID, devId);
        else
            values.put(PARAM_DEVELOPERID, "");

        // add client id
        values.put(PARAM_CLIENT_ID, "2532495");
        // values.put(PARAM_CLIENT_ID, Integer.toString(ApplicationPreferences.getId()));

        // and finally the error log itself
        values.put(PARAM_ERRORLOG, icyId + javaId + osId + memory + "\n" + pluginId + pluginDepsId + errorLog);

        // TODO : change when ready !
        // NetworkUtil.report(values);

        // send report in background task (we don't want to wait for response from server)
        ThreadUtil.bgRun(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    // if (NetworkUtil.postData(BASE_URL, values) == null)
                    final String result = NetworkUtil.postData(BASE_URL_BETA, values);

                    if (result == null)
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
}
