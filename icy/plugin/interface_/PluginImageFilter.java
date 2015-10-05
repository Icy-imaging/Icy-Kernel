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
package icy.plugin.interface_;

import icy.image.IcyBufferedImage;

import javax.swing.JPanel;

/**
 * The ImageFilter plugin is designed to be called at any time to process a given image.
 * The idea is to create a chain of ImageFilter to process an image.
 * It would be much appreciated in a streaming-heavy-size-set of file context.
 * 
 * @author Fabrice de Chaumont
 * @deprecated
 */
@Deprecated
public interface PluginImageFilter
{
    public IcyBufferedImage processImage(IcyBufferedImage icyBufferedImage);

    public JPanel getInterface();
}
