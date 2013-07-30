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

import icy.math.RateMeter;
import icy.math.UnitUtil;
import icy.util.StringUtil;

/**
 * @author stephane
 */
public class DownloadFrame extends CancelableProgressFrame
{
    /**
     * calculated download rate
     */
    private double rate;

    /**
     * internal
     */
    private final RateMeter meter;

    public DownloadFrame(String path)
    {
        this(path, 0);
    }

    public DownloadFrame(String path, double length)
    {
        super(StringUtil.limit("Downloading " + path, 64));

        meter = new RateMeter();
        this.length = length;
        rate = 0;
    }

    public void setPath(String path)
    {
        setMessage(StringUtil.limit("Downloading " + path, 64));
    }

    /**
     * @deprecated use setMessage(..) instead
     */
    @Deprecated
    public void setMessageBase(String messageBase)
    {
        setMessage(messageBase);
    }

    @Override
    protected String buildMessage(String text)
    {
        String mess = text + "  [";

        // information on position
        if (position != -1d)
            mess += UnitUtil.getBytesString(position);
        else
            mess += "???";

        mess += " / ";

        if (length > 0d)
            mess += UnitUtil.getBytesString(length);
        else
            mess += "???";

        if (rate > 0)
            mess += " - " + UnitUtil.getBytesString(rate) + "/s";

        mess += "]";

        return super.buildMessage(mess);
    }

    @Override
    public void setLength(double length)
    {
        if (this.length != length)
            meter.reset();

        super.setLength(length);
    }

    @Override
    public void setPosition(double position)
    {
        // update rate
        if (this.position <= position)
            rate = meter.updateFromTotal(position);
        else
            rate = 0;

        super.setPosition(position);
    }

}
