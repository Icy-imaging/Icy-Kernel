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
package icy.gui.frame.progress;

import icy.util.StringUtil;

/**
 * display a loading TaskFrame
 * 
 * @author Stephane
 */
public class FileFrame extends CancelableProgressFrame
{
    private final String action;
    private String filename;

    public FileFrame(String action, String filename)
    {
        super("");

        this.action = action;
        setFilename(filename);
    }

    @Override
    protected String buildMessage(String text)
    {
        if (StringUtil.isEmpty(action))
            return "";

        if (StringUtil.isEmpty(text))
            return super.buildMessage(action + "...");

        return super.buildMessage(action + " " + text);
    }

    public void setFilename(final String filename)
    {
        if (!StringUtil.equals(this.filename, filename))
        {
            this.filename = filename;
            // set to message
            setMessage(filename);
        }
    }
}
