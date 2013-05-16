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
package icy.type.collection.list;

import icy.file.FileUtil;
import icy.preferences.XMLPreferences;
import icy.util.StringUtil;

import java.io.File;
import java.util.Arrays;

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

    public void addEntry(File[] files)
    {
        // check we are under files limit
        if (files.length > NB_MAX_FILE)
            return;

        final String[] filenames = new String[files.length];

        for (int i = 0; i < files.length; i++)
            filenames[i] = files[i].getAbsolutePath();

        // first remove previous entry
        final int ind = find(filenames);
        if (ind != -1)
            list.remove(ind);

        // add the list
        super.addEntry(filenames);
    }

    protected int find(String[] filenames)
    {
        for (int i = 0; i < list.size(); i++)
            if (Arrays.equals((String[]) list.get(i), filenames))
                return i;

        return -1;
    }

    @Override
    public String[] getEntry(int index)
    {
        return (String[]) super.getEntry(index);
    }

    public File[] getEntryAsFiles(int index)
    {
        final String[] filenames = getEntry(index);
        final File[] result = new File[filenames.length];

        for (int i = 0; i < filenames.length; i++)
            result[i] = new File(filenames[i]);

        return result;
    }

    public String getEntryAsName(int index, int maxlen, boolean tailLimit)
    {
        final String[] filenames = getEntry(index);

        if ((filenames == null) || (filenames.length == 0))
            return "";

        if (filenames.length == 1)
            return StringUtil.limit(filenames[0], maxlen, tailLimit);

        String result = filenames[0];

        for (int i = 1; i < filenames.length; i++)
            result += ", " + FileUtil.getFileName(filenames[i]);

        return "[" + StringUtil.limit(result, maxlen, tailLimit) + "]";
    }

    /**
     * Remove invalid files from the list
     */
    public void clean()
    {
        for (int i = list.size() - 1; i >= 0; i--)
        {
            final File[] files = getEntryAsFiles(i);

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
    protected String[] loadEntry(String key)
    {
        if (preferences.nodeExists(key))
        {
            final XMLPreferences pref = preferences.node(key);

            // load size
            final int numFile = pref.getInt(ID_NB_FILE, 0);
            final String[] result = new String[numFile];

            // load filenames
            for (int i = 0; i < numFile; i++)
                result[i] = pref.get(ID_FILE + i, "");

            return result;
        }

        return null;
    }

    @Override
    protected void saveEntry(String key, Object value)
    {
        final XMLPreferences pref = preferences.node(key);

        // remove all children
        pref.removeChildren();

        // then save
        if (value != null)
        {
            final String[] filenames = (String[]) value;
            final int numFile = filenames.length;

            // save size
            pref.putInt(ID_NB_FILE, numFile);

            // save filenames
            for (int i = 0; i < numFile; i++)
                pref.put(ID_FILE + i, filenames[i]);
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
