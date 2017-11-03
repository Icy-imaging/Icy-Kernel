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
// IJMethods.java
//

/*
 * ImageJ software for multidimensional image processing and analysis.
 * 
 * Copyright (c) 2010, ImageJDev.org.
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * Neither the names of the ImageJDev.org developers nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package icy.imagej.patches;

import icy.imagej.ImageJWrapper;
import icy.main.Icy;

import ij.IJ;

/**
 * Overrides {@link IJ} methods.
 * 
 * @author Curtis Rueden
 */
public class IJMethods
{
    /** Resolution to use when converting double progress to int ratio. */
    private static final int PROGRESS_GRANULARITY = 1000;

    private IJMethods()
    {
        // prevent instantiation of utility class
    }

    /** Appends {@link IJ#showProgress(double)}. */
    public static void showProgress(final double progress)
    {
        // approximate progress as int ratio
        final int currentIndex = (int) (PROGRESS_GRANULARITY * progress);
        final int finalIndex = PROGRESS_GRANULARITY;
        showProgress(currentIndex, finalIndex);
    }

    /** Appends {@link IJ#showProgress(int, int)}. */
    public static void showProgress(final int currentIndex, final int finalIndex)
    {
        final ImageJWrapper ijw = Icy.getMainInterface().getImageJ();

        if (ijw != null)
            ijw.showSwingProgress(currentIndex, finalIndex);
    }

    /** Appends {@link IJ#showStatus(String)}. */
    public static void showStatus(final String s)
    {
        
    }
}
