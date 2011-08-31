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
package icy.plugin.interface_;

import icy.image.IcyBufferedImage;

import javax.swing.JPanel;

/**
 * @author Fabrice de Chaumont
 *         The ImageFilter plugin is designed to be called at any time to process a given image.
 *         The idea is to create a chain of ImageFilter to process an image.
 *         It would be much appreciated in a streaming-heavy-size-set of file context.
 */
public interface PluginImageFilter
{

    public IcyBufferedImage processImage(IcyBufferedImage icyBufferedImage);

    public JPanel getInterface();

}
