/**
 * 
 */
package icy.system.audit;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.w3c.dom.Node;

import icy.file.FileUtil;
import icy.file.xml.XMLPersistent;
import icy.file.xml.XMLPersistentHelper;
import icy.network.NetworkUtil;
import icy.plugin.PluginDescriptor;
import icy.plugin.PluginDescriptor.PluginIdent;
import icy.plugin.PluginLoader;
import icy.plugin.abstract_.Plugin;
import icy.system.IcyExceptionHandler;
import icy.util.DateUtil;
import icy.util.XMLUtil;

/**
 * @author Stephane
 */
public class AuditStorage implements XMLPersistent
{
    private static final String ID_PLUGIN = "plugin";

    private static final String AUDIT_FILENAME = "icy_usage.xml";

    private static final long SAVE_INTERVAL = 1000 * 60;

    private final Map<PluginIdent, PluginStorage> pluginStats;
    private long lastSaveTime;

    public AuditStorage()
    {
        super();

        pluginStats = new HashMap<PluginIdent, PluginStorage>();

        try
        {
            // load usage data from XML file
            XMLPersistentHelper.loadFromXML(this, FileUtil.getTempDirectory() + FileUtil.separator + AUDIT_FILENAME);
            // clean obsoletes data
            clean();
        }
        catch (Exception e)
        {
            System.out.println("Warning: can't reload usage statistics data.");
            IcyExceptionHandler.showErrorMessage(e, false, false);
        }

        lastSaveTime = System.currentTimeMillis();
    }

    public void save()
    {
        try
        {
            // save XML data
            XMLPersistentHelper.saveToXML(this, FileUtil.getTempDirectory() + FileUtil.separator + AUDIT_FILENAME);
        }
        catch (Exception e)
        {
            System.out.println("Warning: can't save usage statistics data.");
            IcyExceptionHandler.showErrorMessage(e, false, false);
        }
    }

    private void clean()
    {
        final List<PluginIdent> empties = new ArrayList<PluginIdent>();

        synchronized (pluginStats)
        {
            // clean statistics
            for (Entry<PluginIdent, PluginStorage> entry : pluginStats.entrySet())
            {
                entry.getValue().clean();
                if (entry.getValue().isEmpty())
                    empties.add(entry.getKey());
            }

            // remove empty ones
            for (PluginIdent ident : empties)
                pluginStats.remove(ident);
        }
    }

    private void autoSave()
    {
        final long currentTime = System.currentTimeMillis();

        // interval elapsed
        if ((currentTime - lastSaveTime) > SAVE_INTERVAL)
        {
            // save statistics to disk
            save();
            lastSaveTime = currentTime;
        }
    }

    private PluginStorage getStorage(PluginIdent ident, boolean autoCreate)
    {
        PluginStorage result;

        synchronized (pluginStats)
        {
            result = pluginStats.get(ident);

            if ((result == null) && autoCreate)
            {
                result = new PluginStorage();
                pluginStats.put(ident, result);
            }
        }

        return result;
    }

    public void pluginLaunched(Plugin plugin)
    {
        PluginDescriptor descriptor = null;

        try
        {
            if (plugin.isBundled())
                descriptor = PluginLoader.getPlugin(plugin.getOwnerClassName());
            else
                descriptor = plugin.getDescriptor();
        }
        catch (Throwable t)
        {
            // ignore possible ClassNotFound error here...
        }

        // ignore if no descriptor
        if (descriptor == null)
            return;
        // ignore if missing version info
        if (descriptor.getVersion().isEmpty())
            return;
        // ignore kernel plugins
        if (descriptor.isKernelPlugin())
            return;

        // increment launch
        getStorage(descriptor.getIdent(), true).incLaunch(DateUtil.keepDay(System.currentTimeMillis()));

        // save to disk if needed
        autoSave();
    }

    public void pluginInstanced(Plugin plugin)
    {
        PluginDescriptor descriptor = null;

        try
        {
            if (plugin.isBundled())
                descriptor = PluginLoader.getPlugin(plugin.getOwnerClassName());
            else
                descriptor = plugin.getDescriptor();
        }
        catch (Throwable t)
        {
            // ignore possible ClassNotFound error here...
        }

        // ignore if no descriptor
        if (descriptor == null)
            return;
        // ignore if missing version info
        if (descriptor.getVersion().isEmpty())
            return;
        // ignore kernel plugins
        if (descriptor.isKernelPlugin())
            return;

        // increment instance
        getStorage(descriptor.getIdent(), true).incInstance(DateUtil.keepDay(System.currentTimeMillis()));
        // save to disk if needed
        autoSave();
    }

    /**
     * Upload statistics to website
     */
    public boolean upload(int id)
    {
        final List<PluginIdent> dones = new ArrayList<PluginIdent>();
        final List<Entry<PluginIdent, PluginStorage>> entries;

        synchronized (pluginStats)
        {
            entries = new ArrayList<Entry<PluginIdent, PluginStorage>>(pluginStats.entrySet());
        }

        try
        {
            for (Entry<PluginIdent, PluginStorage> entry : entries)
            {
                if (entry.getValue().upload(id, entry.getKey()))
                    dones.add(entry.getKey());

                // interrupt upload
                if (Thread.interrupted())
                    break;
            }
        }
        finally
        {
            // remove stats which has been correctly uploaded
            synchronized (pluginStats)
            {
                for (PluginIdent ident : dones)
                    pluginStats.remove(ident);
            }
        }

        return pluginStats.isEmpty();
    }

    @Override
    public boolean loadFromXML(Node node)
    {
        if (node == null)
            return false;

        synchronized (pluginStats)
        {
            pluginStats.clear();
            for (Node n : XMLUtil.getChildren(node, ID_PLUGIN))
            {
                final PluginIdent ident = new PluginIdent();
                final PluginStorage storage = new PluginStorage();

                ident.loadFromXMLShort(n);
                storage.loadFromXML(n);

                pluginStats.put(ident, storage);
            }
        }

        return true;
    }

    @Override
    public boolean saveToXML(Node node)
    {
        if (node == null)
            return false;

        XMLUtil.removeAllChildren(node);

        synchronized (pluginStats)
        {
            for (Entry<PluginIdent, PluginStorage> entry : pluginStats.entrySet())
            {
                final Node n = XMLUtil.addElement(node, ID_PLUGIN);

                entry.getKey().saveToXMLShort(n);
                entry.getValue().saveToXML(n);
            }
        }

        return true;
    }

    private class PluginStorage implements XMLPersistent
    {
        private static final String ID_CLASSNAME = PluginIdent.ID_CLASSNAME;
        private static final String ID_VERSION = PluginIdent.ID_VERSION;

        private static final String ID_LAUNCH = "launch";
        private static final String ID_INSTANCE = "instance";

        private static final String ID_STATS_LAUNCH = "stats_" + ID_LAUNCH;
        private static final String ID_STATS_INSTANCE = "stats_" + ID_INSTANCE;
        private static final String ID_DATE = "date";
        private static final String ID_VALUE = "value";

        private static final long DAY_TO_KEEP = 30L;

        private final Map<Long, Long> launchStats;
        private final Map<Long, Long> instanceStats;

        public PluginStorage()
        {
            super();

            launchStats = new HashMap<Long, Long>();
            instanceStats = new HashMap<Long, Long>();
        }

        public void clean()
        {
            List<Long> olds = new ArrayList<Long>();
            final long dayInterval = 1000 * 60 * 60 * 24;
            final long timeLimit = System.currentTimeMillis() - (DAY_TO_KEEP * dayInterval);

            // find obsoletes entries
            olds.clear();
            for (Long date : launchStats.keySet())
                if (date.longValue() < timeLimit)
                    olds.add(date);

            // remove them
            for (Long date : olds)
                launchStats.remove(date);

            // find obsoletes entries
            olds.clear();
            for (Long date : instanceStats.keySet())
                if (date.longValue() < timeLimit)
                    olds.add(date);

            // remove them
            for (Long date : olds)
                instanceStats.remove(date);
        }

        public boolean isEmpty()
        {
            return launchStats.isEmpty() && instanceStats.isEmpty();
        }

        public long getLaunch(Long date)
        {
            final Long result = launchStats.get(date);

            if (result == null)
                return 0L;

            return result.longValue();
        }

        public long getInstance(Long date)
        {
            final Long result = instanceStats.get(date);

            if (result == null)
                return 0L;

            return result.longValue();
        }

        public void incLaunch(long date)
        {
            final Long key = Long.valueOf(date);
            launchStats.put(key, Long.valueOf(getLaunch(key) + 1L));
        }

        public void incInstance(long date)
        {
            final Long key = Long.valueOf(date);
            instanceStats.put(key, Long.valueOf(getInstance(key) + 1L));
        }

        private Map<String, String> getIdParam(int id)
        {
            // id ok ?
            if (id != -1)
            {
                final Map<String, String> values = new HashMap<String, String>();

                // set id
                values.put(Audit.ID_ICY_ID, Integer.toString(id));

                return values;
            }

            return null;
        }

        public boolean upload(int id, PluginIdent ident)
        {
            // init params
            final Map<String, String> params = getIdParam(id);
            int offset;

            // set plugin identity
            params.put(ID_CLASSNAME, ident.getClassName());
            params.put(ID_VERSION, ident.getVersion().toString());

            offset = 0;
            // build params for launch statistic
            for (Entry<Long, Long> entry : launchStats.entrySet())
            {
                // set date
                params.put(ID_STATS_LAUNCH + "[" + offset + "][" + ID_DATE + "]", entry.getKey().toString());
                // set value
                params.put(ID_STATS_LAUNCH + "[" + offset + "][" + ID_VALUE + "]", entry.getValue().toString());
                offset++;
            }

            offset = 0;
            // build params for instance statistic
            for (Entry<Long, Long> entry : instanceStats.entrySet())
            {
                // set date
                params.put(ID_STATS_INSTANCE + "[" + offset + "][" + ID_DATE + "]", entry.getKey().toString());
                // set value
                params.put(ID_STATS_INSTANCE + "[" + offset + "][" + ID_VALUE + "]", entry.getValue().toString());
                offset++;
            }

            try
            {
                // null return means website did not accepted them...
                if (NetworkUtil.postData(Audit.URL_AUDIT_PLUGIN, params) == null)
                    return false;
            }
            catch (IOException e)
            {
                return false;
            }

            // clear stats just to be sure to not send them twice
            launchStats.clear();
            instanceStats.clear();

            return true;
        }

        @Override
        public boolean loadFromXML(Node node)
        {
            if (node == null)
                return false;

            launchStats.clear();
            for (Node n : XMLUtil.getChildren(node, ID_LAUNCH))
            {
                final long date = XMLUtil.getElementLongValue(n, ID_DATE, 0L);
                final long value = XMLUtil.getElementLongValue(n, ID_VALUE, 0L);

                launchStats.put(Long.valueOf(date), Long.valueOf(value));
            }

            instanceStats.clear();
            for (Node n : XMLUtil.getChildren(node, ID_INSTANCE))
            {
                final long date = XMLUtil.getElementLongValue(n, ID_DATE, 0L);
                final long value = XMLUtil.getElementLongValue(n, ID_VALUE, 0L);

                instanceStats.put(Long.valueOf(date), Long.valueOf(value));
            }

            return true;
        }

        @Override
        public boolean saveToXML(Node node)
        {
            if (node == null)
                return false;

            for (Entry<Long, Long> entry : launchStats.entrySet())
            {
                final Node n = XMLUtil.addElement(node, ID_LAUNCH);

                XMLUtil.setElementLongValue(n, ID_DATE, entry.getKey().longValue());
                XMLUtil.setElementLongValue(n, ID_VALUE, entry.getValue().longValue());
            }

            for (Entry<Long, Long> entry : instanceStats.entrySet())
            {
                final Node n = XMLUtil.addElement(node, ID_INSTANCE);

                XMLUtil.setElementLongValue(n, ID_DATE, entry.getKey().longValue());
                XMLUtil.setElementLongValue(n, ID_VALUE, entry.getValue().longValue());
            }

            return true;
        }
    }

}
