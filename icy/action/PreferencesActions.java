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
package icy.action;

import icy.gui.preferences.CanvasPreferencePanel;
import icy.gui.preferences.ChatPreferencePanel;
import icy.gui.preferences.GeneralPreferencePanel;
import icy.gui.preferences.NetworkPreferencePanel;
import icy.gui.preferences.PluginLocalPreferencePanel;
import icy.gui.preferences.PluginOnlinePreferencePanel;
import icy.gui.preferences.PluginPreferencePanel;
import icy.gui.preferences.PluginStartupPreferencePanel;
import icy.gui.preferences.PreferenceFrame;
import icy.gui.preferences.RepositoryPreferencePanel;
import icy.gui.preferences.WorkspaceLocalPreferencePanel;
import icy.gui.preferences.WorkspaceOnlinePreferencePanel;
import icy.gui.preferences.WorkspacePreferencePanel;
import icy.resource.ResourceUtil;
import icy.resource.icon.IcyIcon;
import icy.util.ClassUtil;

import java.awt.event.ActionEvent;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Preference actions.
 * 
 * @author Stephane
 */
public class PreferencesActions
{
    public static IcyAbstractAction preferencesAction = new IcyAbstractAction("Preferences  ", new IcyIcon(
            ResourceUtil.ICON_TOOLS), "Show the preferences window", "Setup Icy preferences")
    {
        /**
         * 
         */
        private static final long serialVersionUID = 1536708346834850905L;

        @Override
        public boolean doAction(ActionEvent e)
        {
            new PreferenceFrame(GeneralPreferencePanel.NODE_NAME);
            return true;
        }
    };

    public static IcyAbstractAction generalPreferencesAction = new IcyAbstractAction("Preferences", new IcyIcon(
            ResourceUtil.ICON_TOOLS), "Show the general preferences window",
            "Setup general setting as font size, automatic update, maximum memory...")
    {
        /**
         * 
         */
        private static final long serialVersionUID = 1536708346834850905L;

        @Override
        public boolean doAction(ActionEvent e)
        {
            new PreferenceFrame(GeneralPreferencePanel.NODE_NAME);
            return true;
        }
    };

    public static IcyAbstractAction canvasPreferencesAction = new IcyAbstractAction("Canvas preferences", new IcyIcon(
            ResourceUtil.ICON_PICTURE), "Show the canvas preferences window",
            "Setup canvas setting as filtering, mouse wheel sensivity and reverse mouse axis...")
    {
        /**
         * 
         */
        private static final long serialVersionUID = 5758147926869943436L;

        @Override
        public boolean doAction(ActionEvent e)
        {
            new PreferenceFrame(CanvasPreferencePanel.NODE_NAME);
            return true;
        }
    };

    public static IcyAbstractAction chatPreferencesAction = new IcyAbstractAction("Chat preferences", new IcyIcon(
            ResourceUtil.ICON_CHAT), "Show the chat preferences window",
            "Setup chat setting as auto connect, real name, chat password...")
    {
        /**
         * 
         */
        private static final long serialVersionUID = 7557101963461320397L;

        @Override
        public boolean doAction(ActionEvent e)
        {
            new PreferenceFrame(ChatPreferencePanel.NODE_NAME);
            return true;
        }
    };

    public static IcyAbstractAction networkPreferencesAction = new IcyAbstractAction("Network preferences",
            new IcyIcon(ResourceUtil.ICON_NETWORK), "Show the network preferences window",
            "Setup network setting as proxy server.")
    {
        /**
         * 
         */
        private static final long serialVersionUID = -8056321522618950702L;

        @Override
        public boolean doAction(ActionEvent e)
        {
            new PreferenceFrame(NetworkPreferencePanel.NODE_NAME);
            return true;
        }
    };

    public static IcyAbstractAction pluginPreferencesAction = new IcyAbstractAction("Plugin preferences", new IcyIcon(
            ResourceUtil.ICON_PLUGIN), "Show the plugin preferences window",
            "Setup plugin setting as automatic update and enable beta version.")
    {
        /**
         * 
         */
        private static final long serialVersionUID = 1703582841917110419L;

        @Override
        public boolean doAction(ActionEvent e)
        {
            new PreferenceFrame(PluginPreferencePanel.NODE_NAME);
            return true;
        }
    };

    public static IcyAbstractAction localPluginPreferencesAction = new IcyAbstractAction("Local plugin", new IcyIcon(
            ResourceUtil.ICON_PLUGIN), "Show the local plugin window",
            "Browse, remove, update and show informations about installed plugin.")
    {
        /**
         * 
         */
        private static final long serialVersionUID = -8604088116271591026L;

        @Override
        public boolean doAction(ActionEvent e)
        {
            new PreferenceFrame(PluginLocalPreferencePanel.NODE_NAME);
            return true;
        }
    };

    public static IcyAbstractAction onlinePluginPreferencesAction = new IcyAbstractAction("Online plugin", new IcyIcon(
            ResourceUtil.ICON_PLUGIN), "Show the online plugin window", "Browse online plugins and install them.")
    {
        /**
         * 
         */
        private static final long serialVersionUID = -4583665324845708263L;

        @Override
        public boolean doAction(ActionEvent e)
        {
            new PreferenceFrame(PluginOnlinePreferencePanel.NODE_NAME);
            return true;
        }
    };

    public static IcyAbstractAction startupPluginPreferencesAction = new IcyAbstractAction("Startup plugin",
            new IcyIcon(ResourceUtil.ICON_PLUGIN), "Show the startup plugin window",
            "Enable / disable startup plugins.")
    {
        /**
         * 
         */
        private static final long serialVersionUID = 3354219389334167804L;

        @Override
        public boolean doAction(ActionEvent e)
        {
            new PreferenceFrame(PluginStartupPreferencePanel.NODE_NAME);
            return true;
        }
    };

    public static IcyAbstractAction repositoryPreferencesAction = new IcyAbstractAction("Repository preferences",
            new IcyIcon(ResourceUtil.ICON_TOOLS), "Show the repository preferences window",
            "Add, edit or remove repository address.")
    {
        /**
         * 
         */
        private static final long serialVersionUID = -8186738344041266273L;

        @Override
        public boolean doAction(ActionEvent e)
        {
            new PreferenceFrame(RepositoryPreferencePanel.NODE_NAME);
            return true;
        }
    };

    public static IcyAbstractAction workspacePreferencesAction = new IcyAbstractAction("Workspace preferences",
            new IcyIcon(ResourceUtil.ICON_TOOLS), "Show the workspace preferences window")
    {
        /**
         * 
         */
        private static final long serialVersionUID = -7568519363461531069L;

        @Override
        public boolean doAction(ActionEvent e)
        {
            new PreferenceFrame(WorkspacePreferencePanel.NODE_NAME);
            return true;
        }
    };

    public static IcyAbstractAction localWorkspacePreferencesAction = new IcyAbstractAction("Local workspace",
            new IcyIcon(ResourceUtil.ICON_TOOLS), "Show the local workspace window",
            "Enable / disable or remove installed workspaces.")
    {
        /**
         * 
         */
        private static final long serialVersionUID = 5843627734779598519L;

        @Override
        public boolean doAction(ActionEvent e)
        {
            new PreferenceFrame(WorkspaceLocalPreferencePanel.NODE_NAME);
            return true;
        }
    };
    public static IcyAbstractAction onlineWorkspacePreferencesAction = new IcyAbstractAction("Online workspace",
            new IcyIcon(ResourceUtil.ICON_TOOLS), "Show the online workspace window",
            "Browse online workspaces and install them.")
    {
        /**
         * 
         */
        private static final long serialVersionUID = 4739347012951517215L;

        @Override
        public boolean doAction(ActionEvent e)
        {
            new PreferenceFrame(WorkspaceOnlinePreferencePanel.NODE_NAME);
            return true;
        }
    };

    /**
     * Return all actions of this class
     */
    public static List<IcyAbstractAction> getAllActions()
    {
        final List<IcyAbstractAction> result = new ArrayList<IcyAbstractAction>();

        for (Field field : PreferencesActions.class.getFields())
        {
            final Class<?> type = field.getType();

            try
            {
                if (ClassUtil.isSubClass(type, IcyAbstractAction[].class))
                    result.addAll(Arrays.asList(((IcyAbstractAction[]) field.get(null))));
                else if (ClassUtil.isSubClass(type, IcyAbstractAction.class))
                    result.add((IcyAbstractAction) field.get(null));
            }
            catch (Exception e)
            {
                // ignore
            }
        }

        return result;
    }
}
