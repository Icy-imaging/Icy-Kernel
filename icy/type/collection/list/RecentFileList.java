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
package icy.type.collection.list;

import icy.file.FileUtil;
import icy.preferences.XMLPreferences;
import icy.util.StringUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author stephane
 */
public class RecentFileList extends RecentList
{
    protected final static int NB_MAX_ENTRY = 10;
    protected final static int NB_MAX_FILE = 1000;

    protected final static String ID_NB_FILE = "nbFile";
    protected final static String ID_FILE = "file";

    public RecentFileList(XMLPreferences preferences)
    {
        super(preferences, NB_MAX_ENTRY);

        // clean the list
        clean();
    }

    public void addEntry(List<File> files)
    {
        // check we are under files limit
        if (files.size() > NB_MAX_FILE)
            return;

        final ArrayList<String> filenames = new ArrayList<String>();

        for (File file : files)
            filenames.add(file.getAbsolutePath());

        // add the list
        super.addEntry(filenames);
    }

    @SuppressWarnings("unchecked")
    @Override
    public ArrayList<String> getEntry(int index)
    {
        return (ArrayList<String>) super.getEntry(index);
    }

    public ArrayList<File> getEntryAsFiles(int index)
    {
        final ArrayList<String> filenames = getEntry(index);
        final int len = filenames.size();
        final ArrayList<File> result = new ArrayList<File>(len);

        for (int i = 0; i < len; i++)
            result.add(new File(filenames.get(i)));

        return result;
    }

    public String getEntryAsName(int index, int maxlen, boolean tailLimit)
    {
        final ArrayList<String> filenames = getEntry(index);

        if ((filenames == null) || (filenames.size() == 0))
            return "";

        if (filenames.size() == 1)
            return StringUtil.limit(filenames.get(0), maxlen, tailLimit);

        String result = filenames.get(0);

        for (int i = 1; i < filenames.size(); i++)
            result += ", " + FileUtil.getFileName(filenames.get(i));

        return "[" + StringUtil.limit(result, maxlen, tailLimit) + "]";
    }

    /**
     * Remove invalid files from the list
     */
    public void clean()
    {
        for (int i = list.size() - 1; i >= 0; i--)
        {
            final ArrayList<File> files = getEntryAsFiles(i);

            boolean allExists = true;
            for (File file : files)
                allExists = allExists && file.exists();

            // one of the files doesn't exist anymore ?
            if (!allExists)
                // remove it from the list
                list.remove(i);
        }

        // save to pref
        save();
        // inform about change
        changed();
    }

    @Override
    protected ArrayList<String> loadEntry(String key)
    {
        if (preferences.nodeExists(key))
        {
            final ArrayList<String> result = new ArrayList<String>();
            final XMLPreferences pref = preferences.node(key);

            // load size
            final int numFile = pref.getInt(ID_NB_FILE, 0);

            // load filenames
            for (int i = 0; i < numFile; i++)
                result.add(pref.get(ID_FILE + i, ""));

            return result;
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void saveEntry(String key, Object value)
    {
        final XMLPreferences pref = preferences.node(key);

        // remove all children
        pref.removeChildren();

        // then save
        if (value != null)
        {
            final ArrayList<String> filenames = (ArrayList<String>) value;
            final int numFile = filenames.size();

            // save size
            pref.putInt(ID_NB_FILE, numFile);

            // save filenames
            for (int i = 0; i < numFile; i++)
                pref.put(ID_FILE + i, filenames.get(i));
        }
        else
        {
            // save size
            pref.putInt(ID_NB_FILE, 0);
        }

        // then clean
        pref.clean();
    }
}
