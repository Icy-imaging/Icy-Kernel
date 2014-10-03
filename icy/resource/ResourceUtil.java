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
package icy.resource;

import icy.image.ImageUtil;
import icy.resource.icon.IcyIcon;
import icy.util.StringUtil;

import java.awt.Color;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;

import javax.swing.ImageIcon;

public class ResourceUtil
{
    public static final String separator = "/";

    // image and icon path
    private static final String ICON_PATH = "res/icon/";
    private static final String IMAGE_PATH = "res/image/";

    private static final String ALPHA_ICON_PATH = "alpha/";
    private static final String COLOR_ICON_PATH = "color/";

    // default icon size
    public static final int DEFAULT_ICON_SIZE = IcyIcon.DEFAULT_SIZE;

    // application images
    public static final Image IMAGE_ICY_16 = ResourceUtil.getIconAsImage("icy16.png");
    public static final Image IMAGE_ICY_32 = ResourceUtil.getIconAsImage("icy32.png");
    public static final Image IMAGE_ICY_256 = ResourceUtil.getImage("logo.png");

    public static final Image IMAGE_PLUGIN_SMALL = ResourceUtil.getImage("app/plugin_small.png");
    public static final Image IMAGE_PLUGIN = ResourceUtil.getImage("app/plugin.png");
    public static final Image IMAGE_ACCOLADE_LEFT = ResourceUtil.getImage("app/accol_left.png");

    // alpha mask icons
    public static final ImageIcon ICON_ICY_16 = ResourceUtil.getImageIcon(IMAGE_ICY_16);
    public static final ImageIcon ICON_ICY_32 = ResourceUtil.getImageIcon(IMAGE_ICY_32);

    public static final Image ICON_NULL = ResourceUtil.getAlphaIconAsImage("null.png");

    public static final Image ICON_WINDOW_EXPAND = ResourceUtil.getAlphaIconAsImage("top_right_expand.png");
    public static final Image ICON_WINDOW_COLLAPSE = ResourceUtil.getAlphaIconAsImage("top_right_collapse.png");
    public static final Image ICON_PICTURE = ResourceUtil.getAlphaIconAsImage("picture.png");
    public static final Image ICON_PICTURE_COPY = ResourceUtil.getAlphaIconAsImage("picture_copy.png");
    public static final Image ICON_PICTURE_PASTE = ResourceUtil.getAlphaIconAsImage("picture_paste.png");
    public static final Image ICON_WINDOW = ResourceUtil.getAlphaIconAsImage("app_window.png");
    public static final Image ICON_DETACHED_WINDOW = ResourceUtil.getAlphaIconAsImage("app_detached.png");
    public static final Image ICON_DOC = ResourceUtil.getAlphaIconAsImage("document.png");
    public static final Image ICON_DOC_NEW = ResourceUtil.getAlphaIconAsImage("doc_new.png");
    public static final Image ICON_DOC_COPY = ResourceUtil.getAlphaIconAsImage("doc_copy.png");
    public static final Image ICON_DOC_IMPORT = ResourceUtil.getAlphaIconAsImage("doc_import.png");
    public static final Image ICON_DOC_EXPORT = ResourceUtil.getAlphaIconAsImage("doc_export.png");
    public static final Image ICON_XLS_EXPORT = ResourceUtil.getAlphaIconAsImage("xls_export.png");
    public static final Image ICON_DOCEDIT = ResourceUtil.getAlphaIconAsImage("doc_edit.png");
    public static final Image ICON_DOCTEXT1 = ResourceUtil.getAlphaIconAsImage("doc_lines.png");
    public static final Image ICON_DOCTEXT2 = ResourceUtil.getAlphaIconAsImage("doc_lines_stright.png");
    public static final Image ICON_DUPLICATE = ResourceUtil.getAlphaIconAsImage("duplicate.png");
    public static final Image ICON_OPEN = ResourceUtil.getAlphaIconAsImage("folder_open.png");
    public static final Image ICON_LOAD = ICON_OPEN;
    public static final Image ICON_SAVE = ResourceUtil.getAlphaIconAsImage("save.png");
    public static final Image ICON_CLOSE = ResourceUtil.getAlphaIconAsImage("round_delete.png");
    public static final Image ICON_CLIPBOARD_CLEAR = ResourceUtil.getAlphaIconAsImage("clipboard_clear.png");
    public static final Image ICON_COPY = ResourceUtil.getAlphaIconAsImage("clipboard_copy.png");
    public static final Image ICON_CUT = ResourceUtil.getAlphaIconAsImage("clipboard_cut.png");
    public static final Image ICON_LINK_COPY = ResourceUtil.getAlphaIconAsImage("clipboard_link_copy.png");
    public static final Image ICON_PASTE = ResourceUtil.getAlphaIconAsImage("clipboard_past.png");
    public static final Image ICON_LINK_PASTE = ResourceUtil.getAlphaIconAsImage("clipboard_link_past.png");

    public static final Image ICON_HELP = ResourceUtil.getAlphaIconAsImage("help.png");
    public static final Image ICON_INFO = ResourceUtil.getAlphaIconAsImage("info.png");

    public static final Image ICON_ON_OFF = ResourceUtil.getAlphaIconAsImage("on-off.png");
    public static final Image ICON_TRASH = ResourceUtil.getAlphaIconAsImage("trash.png");
    public static final Image ICON_DELETE = ResourceUtil.getAlphaIconAsImage("delete.png");
    public static final Image ICON_COG = ResourceUtil.getAlphaIconAsImage("cog.png");
    public static final Image ICON_UNCHECKED = ResourceUtil.getAlphaIconAsImage("checkbox_unchecked.png");
    public static final Image ICON_CHECKED = ResourceUtil.getAlphaIconAsImage("checkbox_checked.png");
    public static final Image ICON_LOCK_OPEN = ResourceUtil.getAlphaIconAsImage("unlocked.png");
    public static final Image ICON_LOCK_CLOSE = ResourceUtil.getAlphaIconAsImage("locked.png");
    public static final Image ICON_DOWNLOAD = ResourceUtil.getAlphaIconAsImage("download.png");
    public static final Image ICON_PROPERTIES = ICON_DOCTEXT1;
    public static final Image ICON_TAG = ResourceUtil.getAlphaIconAsImage("tag.png");
    public static final Image ICON_TOOLS = ResourceUtil.getAlphaIconAsImage("wrench_plus_2.png");
    public static final Image ICON_LIGHTING = ResourceUtil.getAlphaIconAsImage("lighting.png");
    public static final Image ICON_SEARCH = ResourceUtil.getAlphaIconAsImage("zoom.png");
    public static final Image ICON_WAIT = ResourceUtil.getAlphaIconAsImage("sand.png");
    public static final Image ICON_VISIBLE = ResourceUtil.getAlphaIconAsImage("eye_open.png");
    public static final Image ICON_NOT_VISIBLE = ResourceUtil.getAlphaIconAsImage("eye_close.png");

    public static final Image ICON_CENTER_IMAGE = ResourceUtil.getAlphaIconAsImage("center.png");
    public static final Image ICON_CROP = ResourceUtil.getAlphaIconAsImage("crop.png");
    public static final Image ICON_FIT_IMAGE = ResourceUtil.getAlphaIconAsImage("fit_in2.png");
    public static final Image ICON_FIT_CANVAS = ResourceUtil.getAlphaIconAsImage("fit_out.png");

    public static final Image ICON_TARGET = ResourceUtil.getAlphaIconAsImage("simple_target.png");
    public static final Image ICON_PENCIL = ResourceUtil.getAlphaIconAsImage("pencil.png");
    public static final Image ICON_BRUSH = ResourceUtil.getAlphaIconAsImage("brush.png");
    public static final Image ICON_LINK = ResourceUtil.getAlphaIconAsImage("link.png");

    public static final Image ICON_LAYER_H1 = ResourceUtil.getAlphaIconAsImage("layers_h1.png");
    public static final Image ICON_LAYER_H2 = ResourceUtil.getAlphaIconAsImage("layers_h2.png");
    public static final Image ICON_LAYER_REVERSE_H = ResourceUtil.getAlphaIconAsImage("layers_reverse_h.png");
    public static final Image ICON_LAYER_EXTRACT_H = ResourceUtil.getAlphaIconAsImage("layers_extract_h.png");
    public static final Image ICON_LAYER_ADD_H = ResourceUtil.getAlphaIconAsImage("layers_add_h.png");
    public static final Image ICON_LAYER_INSERT_H = ResourceUtil.getAlphaIconAsImage("layers_insert_h.png");
    public static final Image ICON_LAYER_REMOVE_H = ResourceUtil.getAlphaIconAsImage("layers_remove_h.png");
    public static final Image ICON_LAYER_REMOVE_ADV_H = ResourceUtil.getAlphaIconAsImage("layers_remove_adv_h.png");
    public static final Image ICON_LAYER_V1 = ResourceUtil.getAlphaIconAsImage("layers_v1.png");
    public static final Image ICON_LAYER_V2 = ResourceUtil.getAlphaIconAsImage("layers_v2.png");
    public static final Image ICON_LAYER_REVERSE_V = ResourceUtil.getAlphaIconAsImage("layers_reverse_v.png");
    public static final Image ICON_LAYER_EXTRACT_V = ResourceUtil.getAlphaIconAsImage("layers_extract_v.png");
    public static final Image ICON_LAYER_ADD_V = ResourceUtil.getAlphaIconAsImage("layers_add_v.png");
    public static final Image ICON_LAYER_INSERT_V = ResourceUtil.getAlphaIconAsImage("layers_insert_v.png");
    public static final Image ICON_LAYER_REMOVE_V = ResourceUtil.getAlphaIconAsImage("layers_remove_v.png");
    public static final Image ICON_LAYER_REMOVE_ADV_V = ResourceUtil.getAlphaIconAsImage("layers_remove_adv_v.png");
    public static final Image ICON_LAYER_ADJUST_HV = ResourceUtil.getAlphaIconAsImage("layers_adjust_hv.png");

    public static final Image ICON_ROI = ResourceUtil.getAlphaIconAsImage("roi.png");
    public static final Image ICON_ROI_POINT = ResourceUtil.getAlphaIconAsImage("roi_point.png");
    public static final Image ICON_ROI_LINE = ResourceUtil.getAlphaIconAsImage("roi_line.png");
    public static final Image ICON_ROI_OVAL = ResourceUtil.getAlphaIconAsImage("roi_oval.png");
    public static final Image ICON_ROI_POLYLINE = ResourceUtil.getAlphaIconAsImage("roi_polyline.png");
    public static final Image ICON_ROI_POLYGON = ResourceUtil.getAlphaIconAsImage("roi_polygon.png");
    public static final Image ICON_ROI_RECTANGLE = ResourceUtil.getAlphaIconAsImage("roi_rectangle.png");
    public static final Image ICON_ROI_AREA = ResourceUtil.getAlphaIconAsImage("roi_area.png");

    public static final Image ICON_ROI_NOT = ResourceUtil.getAlphaIconAsImage("roi_not.png");
    public static final Image ICON_ROI_OR = ResourceUtil.getAlphaIconAsImage("roi_or.png");
    public static final Image ICON_ROI_AND = ResourceUtil.getAlphaIconAsImage("roi_and.png");
    public static final Image ICON_ROI_XOR = ResourceUtil.getAlphaIconAsImage("roi_xor.png");
    public static final Image ICON_ROI_SUB = ResourceUtil.getAlphaIconAsImage("roi_sub.png");

    public static final Image ICON_INDENT_DECREASE = ResourceUtil.getAlphaIconAsImage("indent_decrease.png");
    public static final Image ICON_INDENT_INCREASE = ResourceUtil.getAlphaIconAsImage("indent_increase.png");
    public static final Image ICON_INDENT_REMOVE = ResourceUtil.getAlphaIconAsImage("indent_remove.png");

    public static final Image ICON_TOIJ = ResourceUtil.getAlphaIconAsImage("to_ij.png");
    public static final Image ICON_TOICY = ResourceUtil.getAlphaIconAsImage("to_icy.png");

    public static final Image ICON_PLAY = ResourceUtil.getAlphaIconAsImage("playback_play.png");
    public static final Image ICON_STOP = ResourceUtil.getAlphaIconAsImage("playback_stop.png");
    public static final Image ICON_PAUSE = ResourceUtil.getAlphaIconAsImage("playback_pause.png");
    public static final Image ICON_PLAY_NEXT = ResourceUtil.getAlphaIconAsImage("playback_next.png");
    public static final Image ICON_PLAY_PREV = ResourceUtil.getAlphaIconAsImage("playback_prev.png");
    public static final Image ICON_RELOAD = ResourceUtil.getAlphaIconAsImage("playback_reload.png");

    public static final Image ICON_PLUS = ResourceUtil.getAlphaIconAsImage("plus.png");
    public static final Image ICON_MINUS = ResourceUtil.getAlphaIconAsImage("minus.png");

    public static final Image ICON_ROUND_PLUS = ResourceUtil.getAlphaIconAsImage("round_plus.png");
    public static final Image ICON_ROUND_MINUS = ResourceUtil.getAlphaIconAsImage("round_minus.png");

    public static final Image ICON_ROTATE_UNCLOCK = ResourceUtil.getAlphaIconAsImage("rot_unclock.png");
    public static final Image ICON_ROTATE_CLOCK = ICON_RELOAD;

    public static final Image ICON_ARROW_UP = ResourceUtil.getAlphaIconAsImage("arrow_top.png");
    public static final Image ICON_ARROW_LEFT = ResourceUtil.getAlphaIconAsImage("arrow_left.png");
    public static final Image ICON_ARROW_RIGHT = ResourceUtil.getAlphaIconAsImage("arrow_right.png");
    public static final Image ICON_ARROW_DOWN = ResourceUtil.getAlphaIconAsImage("arrow_bottom.png");
    public static final Image ICON_ARROW_TOP_RIGHT = ResourceUtil.getAlphaIconAsImage("arrow_top_right.png");

    public static final Image ICON_ROUND_ARROW_UP = ResourceUtil.getAlphaIconAsImage("round_arrow_up.png");
    public static final Image ICON_ROUND_ARROW_LEFT = ResourceUtil.getAlphaIconAsImage("round_arrow_left.png");
    public static final Image ICON_ROUND_ARROW_RIGHT = ResourceUtil.getAlphaIconAsImage("round_arrow_right.png");
    public static final Image ICON_ROUND_ARROW_DOWN = ResourceUtil.getAlphaIconAsImage("round_arrow_down.png");

    public static final Image ICON_SQUARE_DOWN = ResourceUtil.getAlphaIconAsImage("sq_down.png");
    public static final Image ICON_SQUARE_UP = ResourceUtil.getAlphaIconAsImage("sq_up.png");
    public static final Image ICON_SQUARE_PREV = ResourceUtil.getAlphaIconAsImage("sq_prev.png");
    public static final Image ICON_SQUARE_NEXT = ResourceUtil.getAlphaIconAsImage("sq_next.png");

    public static final Image ICON_PANEL_EXPAND = ResourceUtil.getAlphaIconAsImage("br_next.png");
    public static final Image ICON_PANEL_COLLAPSE = ResourceUtil.getAlphaIconAsImage("br_down.png");
    public static final Image ICON_PIN = ResourceUtil.getAlphaIconAsImage("pin.png");

    public static final Image ICON_BAND_RIGHT = ResourceUtil.getAlphaIconAsImage("arrow_r.png");
    public static final Image ICON_BAND_LEFT = ResourceUtil.getAlphaIconAsImage("arrow_l.png");

    public static final Image ICON_RESIZE = ResourceUtil.getAlphaIconAsImage("cursor_drag_arrow.png");
    public static final Image ICON_RESIZE_2 = ResourceUtil.getAlphaIconAsImage("cursor_drag_arrow_2.png");
    
    public static final Image ICON_TEXT = ResourceUtil.getAlphaIconAsImage("text_letter_t.png");

    public static final Image ICON_SMILEY_HAPPY = ResourceUtil.getAlphaIconAsImage("emotion_smile.png");
    public static final Image ICON_SMILEY_SAD = ResourceUtil.getAlphaIconAsImage("emotion_sad.png");

    public static final Image ICON_PLUGIN = ResourceUtil.getAlphaIconAsImage("plugin.png");
    public static final Image ICON_DATABASE = ResourceUtil.getAlphaIconAsImage("db.png");
    public static final Image ICON_STAR = ResourceUtil.getAlphaIconAsImage("star_fav.png");
    public static final Image ICON_NETWORK = ResourceUtil.getAlphaIconAsImage("network.png");
    public static final Image ICON_BROWSER = ResourceUtil.getAlphaIconAsImage("browser.png");
    public static final Image ICON_CHAT = ResourceUtil.getAlphaIconAsImage("spechbubble.png");
    public static final Image ICON_PHOTO = ResourceUtil.getAlphaIconAsImage("photo.png");
    public static final Image ICON_PHOTO_SMALL = ResourceUtil.getAlphaIconAsImage("photo_small.png");

    /**
     * @deprecated Use {@link #ICON_DOC_NEW} instead.
     */
    @Deprecated
    public static final Image ICON_NEWDOC = ICON_DOC_NEW;
    /**
     * @deprecated Use {@link #ICON_DOC_COPY} instead.
     */
    @Deprecated
    public static final Image ICON_DOCCOPY = ICON_DOC_COPY;
    /**
     * @deprecated Use {@link #ICON_DOC_IMPORT} instead.
     */
    @Deprecated
    public static final Image ICON_DOCIMPORT = ICON_DOC_IMPORT;
    /**
     * @deprecated Use {@link #ICON_DOC_EXPORT} instead.
     */
    @Deprecated
    public static final Image ICON_DOCEXPORT = ICON_DOC_EXPORT;
    /**
     * @deprecated Use {@link #ICON_ARROW_DOWN} instead.
     */
    @Deprecated
    public static final Image ICON_ARROW_BOTTOM = ICON_ARROW_DOWN;
    /**
     * @deprecated Use {@link #ICON_ARROW_UP} instead.
     */
    @Deprecated
    public static final Image ICON_ARROW_TOP = ICON_ARROW_UP;
    /**
     * @deprecated Use {@link #ICON_WINDOW_EXPAND} instead.
     */
    @Deprecated
    public static final Image ICON_EXPAND = ICON_WINDOW_EXPAND;
    /**
     * @deprecated Use {@link #ICON_WINDOW_COLLAPSE} instead.
     */
    @Deprecated
    public static final Image ICON_COLLAPSE = ICON_WINDOW_COLLAPSE;

    // color icons
    public static final Image ICON_ALPHA_COLOR = ResourceUtil.getColorIconAsImage("alpha.png");
    public static final Image ICON_ARGB_COLOR = ResourceUtil.getColorIconAsImage("argb.png");
    public static final Image ICON_RGB_COLOR = ResourceUtil.getColorIconAsImage("rgb.png");
    public static final Image ICON_GRAY_COLOR = ResourceUtil.getColorIconAsImage("gray.png");

    public static final Image ICON_PLUGIN_COLOR = ResourceUtil.getColorIconAsImage("plugin.png");

    public static ArrayList<Image> getIcyIconImages()
    {
        final ArrayList<Image> result = new ArrayList<Image>();

        result.add(ResourceUtil.IMAGE_ICY_256);
        result.add(ResourceUtil.IMAGE_ICY_32);
        result.add(ResourceUtil.IMAGE_ICY_16);

        return result;
    }

    private static Image scaleImage(Image image, int size)
    {
        // resize if needed
        if ((image != null) && (size != -1))
        {
            // resize only if image has different size
            if ((image.getWidth(null) != size) || (image.getWidth(null) != size))
                return ImageUtil.scale(image, size, size);
        }

        return image;
    }

    /**
     * Return an image with wanted size located in res/image<br>
     * For any other location use the ImageUtil.loadImage() method
     * 
     * @param name
     */
    public static BufferedImage getImage(String name)
    {
        final BufferedImage result;
        final URL url = ResourceUtil.class.getResource("/" + IMAGE_PATH + name);

        if (url != null)
            result = ImageUtil.load(url, true);
        else
            result = ImageUtil.load(new File(IMAGE_PATH + name), true);

        return result;
    }

    /**
     * Return lock image with specified number.
     */
    public static BufferedImage getLockedImage(int number)
    {
        final BufferedImage result = ImageUtil.getCopy(ICON_LOCK_CLOSE);

        // nice for 48 pixels image
        ImageUtil.drawTextTopRight(result, StringUtil.toString(number), 26, true, Color.black);

        return result;
    }

    /**
     * Return lock image with specified letter.
     */
    public static BufferedImage getLockedImage(char letter)
    {
        final BufferedImage result = ImageUtil.getCopy(ICON_LOCK_CLOSE);

        // nice for 48 pixels image
        ImageUtil.drawTextTopRight(result, String.valueOf(letter), 26, true, Color.black);

        return result;
    }

    /**
     * Return an image with wanted size located in res/icon from its name<br>
     * For any other location use the ImageUtil.loadImage() method
     * 
     * @param name
     */
    public static Image getIconAsImage(String name, int size)
    {
        final Image image;
        final URL url = ResourceUtil.class.getResource("/" + ICON_PATH + name);

        if (url != null)
            image = ImageUtil.load(url, true);
        else
            image = ImageUtil.load(new File(ICON_PATH + name), true);

        if (image == null)
        {
            System.out.println("Resource name can't be found: " + name);
            return null;
        }

        return scaleImage(image, size);
    }

    /**
     * Return an image located in res/icon from its name<br>
     * For any other location use the ImageUtil.loadImage() method
     * 
     * @param name
     */
    public static Image getIconAsImage(String name)
    {
        return getIconAsImage(name, -1);
    }

    /**
     * Create an ImageIcon with specified size from the specified image<br>
     */
    public static ImageIcon getImageIcon(Image image, int size)
    {
        if (image != null)
            return new ImageIcon(scaleImage(image, size));

        return null;
    }

    /**
     * Create an ImageIcon from the specified image (default image size is used as icon size)<br>
     */
    public static ImageIcon getImageIcon(Image image)
    {
        if (image != null)
            return new ImageIcon(image);

        return null;
    }

    /**
     * Return an image located in res/icon with specified square size from its name<br>
     */
    public static ImageIcon getImageIcon(String resourceName, int size)
    {
        return getImageIcon(getIconAsImage(resourceName, size));
    }

    /**
     * Return an image located in res/icon from its name<br>
     */
    public static ImageIcon getImageIcon(String resourceName)
    {
        return getImageIcon(getIconAsImage(resourceName));
    }

    /**
     * Return an image located in res/icon/alpha with specified square size from its name<br>
     */
    public static Image getAlphaIconAsImage(String resourceName, int size)
    {
        final String finalName;

        if (resourceName.toLowerCase().endsWith(".png"))
            finalName = resourceName;
        else
            finalName = resourceName + ".png";

        return getIconAsImage(ALPHA_ICON_PATH + finalName, size);
    }

    /**
     * Return an image located in res/icon/alpha from its name<br>
     */
    public static Image getAlphaIconAsImage(String resourceName)
    {
        return getAlphaIconAsImage(resourceName, -1);
    }

    /**
     * Return an ImageIcon located in res/icon/alpha with specified square size<br>
     */
    public static ImageIcon getAlphaIcon(String resourceName, int size)
    {
        final String finalName;

        if (resourceName.toLowerCase().endsWith(".png"))
            finalName = resourceName;
        else
            finalName = resourceName + ".png";

        return getImageIcon(getIconAsImage(ALPHA_ICON_PATH + finalName, size));
    }

    /**
     * Return an ImageIcon located in res/icon/alpha<br>
     */
    public static ImageIcon getAlphaIcon(String resourceName)
    {
        return getAlphaIcon(resourceName, -1);
    }

    /**
     * Return an image located in res/icon/color with specified square size from its name<br>
     */
    public static Image getColorIconAsImage(String resourceName, int size)
    {
        final String finalName;

        if (resourceName.toLowerCase().endsWith(".png"))
            finalName = resourceName;
        else
            finalName = resourceName + ".png";

        return getIconAsImage(COLOR_ICON_PATH + finalName, size);
    }

    /**
     * Return an image located in res/icon/color from its name<br>
     */
    public static Image getColorIconAsImage(String resourceName)
    {
        return getColorIconAsImage(resourceName, -1);
    }

    /**
     * Return an ImageIcon located in res/icon/color with specified square size<br>
     */
    public static ImageIcon getColorIcon(String resourceName, int size)
    {
        final String finalName;

        if (resourceName.toLowerCase().endsWith(".png"))
            finalName = resourceName;
        else
            finalName = resourceName + ".png";

        return getImageIcon(getIconAsImage(COLOR_ICON_PATH + finalName, size));
    }

    /**
     * Return an ImageIcon located in res/icon/color<br>
     */
    public static ImageIcon getColorIcon(String resourceName)
    {
        return getColorIcon(resourceName, -1);
    }

    /**
     * @deprecated use {@link #getAlphaIconAsImage(String, int)} instead
     */
    @Deprecated
    public static Image getBlackIconAsImage(String resourceName, int size)
    {
        return getAlphaIconAsImage(resourceName, size);
    }

    /**
     * @deprecated use {@link #getAlphaIconAsImage(String)} instead
     */
    @Deprecated
    public static Image getBlackIconAsImage(String resourceName)
    {
        return getAlphaIconAsImage(resourceName);
    }

    /**
     * @deprecated use {@link #getAlphaIcon(String, int)} instead
     */
    @Deprecated
    public static ImageIcon getBlackIcon(String resourceName, int size)
    {
        return getAlphaIcon(resourceName, size);
    }

    /**
     * @deprecated use {@link #getAlphaIcon(String)} instead
     */
    @Deprecated
    public static ImageIcon getBlackIcon(String resourceName)
    {
        return getAlphaIcon(resourceName);
    }

    /**
     * @deprecated use {@link #getAlphaIcon(String, int)} instead
     */
    @Deprecated
    public static ImageIcon getWhiteIcon(String resourceName, int size)
    {
        return getAlphaIcon(resourceName, size);
    }

    /**
     * @deprecated use {@link #getAlphaIcon(String)} instead
     */
    @Deprecated
    public static ImageIcon getWhiteIcon(String resourceName)
    {
        return getAlphaIcon(resourceName);
    }

    // public static ImageIcon getColorIcon(String resourceName, int size)
    // {
    // return getImageIcon(getIconAsImage(COLOR_ICON_PATH + resourceName, size));
    // }
    //
    // public static ImageIcon getColorIcon(String resourceName)
    // {
    // return getColorIcon(resourceName, -1);
    // }

    /**
     * @deprecated use {@link #getAlphaIcon(String, int)} instead
     */
    @Deprecated
    public static ImageIcon getIcyIcon(String resourceName, int size)
    {
        return getAlphaIcon(resourceName, size);
    }

    /**
     * @deprecated use {@link #getAlphaIcon(String)} instead
     */
    @Deprecated
    public static ImageIcon getIcyIcon(String resourceName)
    {
        return getAlphaIcon(resourceName);
    }

    /**
     * Return a new ImageIcon scaled with specified size
     */
    public static ImageIcon scaleIcon(ImageIcon icon, int size)
    {
        if (icon != null)
            return getImageIcon(icon.getImage(), size);

        return null;
    }
}
