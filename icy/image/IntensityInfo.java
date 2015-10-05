/*
 * Copyright 2010-2015 Institut Pasteur.
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
package icy.image;

/**
 * Intensity information
 * 
 * @author Stephane
 */
public class IntensityInfo
{
    /**
     * Minimum intensity
     */
    public double minIntensity;
    /**
     * Mean intensity
     */
    public double meanIntensity;
    /**
     * Maximum intensity
     */
    public double maxIntensity;

    public IntensityInfo()
    {
        super();

        minIntensity = 0d;
        meanIntensity = 0d;
        maxIntensity = 0d;
    }
}
