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
package icy.image.lut;

/**
 * @deprecated
 */
@Deprecated
public class LUTBandEvent
{
    public enum LUTBandEventType
    {
        SCALER_CHANGED, COLORMAP_CHANGED
    }

    private final LUTBand lutband;
    private final LUTBandEventType type;

    /**
     * @param lutband
     * @param type
     */
    public LUTBandEvent(LUTBand lutband, LUTBandEventType type)
    {
        super();

        this.lutband = lutband;
        this.type = type;
    }

    /**
     * @return the lutband
     */
    public LUTBand getLutband()
    {
        return lutband;
    }

    /**
     * @return the type
     */
    public LUTBandEventType getType()
    {
        return type;
    }

}
