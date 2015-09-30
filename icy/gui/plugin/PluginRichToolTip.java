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
package icy.gui.plugin;

import icy.plugin.PluginDescriptor;
import icy.util.StringUtil;

import java.awt.Image;

import javax.swing.ImageIcon;

import org.pushingpixels.flamingo.api.common.RichTooltip;

/**
 * {@link RichTooltip} component for a {@link PluginDescriptor}.
 * 
 * @author Stephane
 */
public class PluginRichToolTip extends RichTooltip
{
    public PluginRichToolTip(PluginDescriptor plugin)
    {
        super();

        final String name = plugin.getName();
        final String description = plugin.getDescription();
        final String website = plugin.getWeb();
        final String author = plugin.getAuthor();
        final ImageIcon plugIcon = plugin.getIcon();
        final Image plugImg = plugin.getImage();

        setTitle(name);
        if (plugIcon != PluginDescriptor.DEFAULT_ICON)
            setMainImage(plugIcon.getImage());

        if (!StringUtil.isEmpty(description))
        {
            for (String str : description.split("\n"))
                if (!StringUtil.isEmpty(str))
                    addDescriptionSection(str);
        }
        if (!StringUtil.isEmpty(website))
            addDescriptionSection(website);
        if (!StringUtil.isEmpty(author))
            addDescriptionSection(author);

        if (plugImg != PluginDescriptor.DEFAULT_IMAGE)
            setFooterImage(plugin.getImage());
    }
}
