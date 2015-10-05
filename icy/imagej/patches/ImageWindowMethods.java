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
// ImageWindowMethods.java
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
import ij.WindowManager;
import ij.gui.ImageWindow;

import java.awt.event.WindowEvent;

/**
 * Overrides {@link ImageWindow} methods.
 * 
 * @author Curtis Rueden
 * @author Barry DeZonia
 */
public final class ImageWindowMethods
{
    private ImageWindowMethods()
    {
        // prevent instantiation of utility class
    }

    /** Replaces {@link ImageWindow#setVisible(boolean)}. */
    public static void setVisible(final ImageWindow obj, final boolean visible)
    {

    }

    /** Replaces {@link ImageWindow#show()}. */
    public static void show(final ImageWindow obj)
    {

    }

    /** Appends {@link ImageWindow#close()}. */
    public static void close(final ImageWindow obj)
    {

    }

    /** Appends {@link ImageWindow#windowActivated(WindowEvent)}. */
    public static void windowActivated(final ImageWindow obj, WindowEvent e)
    {
        final ImageJWrapper ij = Icy.getMainInterface().getImageJ();

        if (ij != null)
            ij.setActiveImage(obj);
    }

    /** Appends {@link ImageWindow#windowClosed(WindowEvent)}. */
    public static void windowClosed(final ImageWindow obj, WindowEvent e)
    {
        final ImageJWrapper ij = Icy.getMainInterface().getImageJ();

        if (ij != null)
            ij.setActiveImage(WindowManager.getCurrentWindow());
    }
}
