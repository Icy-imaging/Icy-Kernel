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
package icy.network;

import icy.util.StringUtil;

import java.util.ArrayList;

import org.schwering.irc.lib.IRCConnection;

/**
 * Simple IRCClient class.
 * 
 * @author Stephane
 */
public class IRCClient extends IRCConnection
{
    /** Listeners */
    protected final ArrayList<IRCEventListenerImpl> listeners;

    /**
     * Creates a new IRCConnection instance and starts the thread.
     * If you get confused by the two setDaemon()s: The conn.setDaemon(false) marks the
     * IRCConnection thread as user thread and the setDaemon(true) marks this class's thread
     * (which just listens for keyboard input) as daemon thread. Thus, if the IRCConnection
     * breaks, this console application shuts down, because due to the setDaemon(true) it
     * will no longer wait for keyboard input (no input would make sense without being
     * connected to a server).
     */
    public IRCClient(String host, int port, String pass, String nickName, String userName, String realName)
    {
        super(host, new int[] {port}, pass, nickName, userName, realName);

        listeners = new ArrayList<IRCEventListenerImpl>();

        // default parameters
        setEncoding("UTF-8");
        setPong(true);
        setDaemon(false);
        setColors(true);
        // 1 hour for timeout
        setTimeout(1 * 60 * 60 * 1000);
    }

    /**
     * Called on receive text event.
     */
    protected void receive(String nick, String target, String text)
    {
        // notify listeners about receive
        fireReceiveEvent(nick, target, text);
    }

    /**
     * Send unparsed text to IRC server.
     */
    public void send(String target, String text)
    {
        if (StringUtil.isEmpty(text))
            return;

        if (text.charAt(0) == '/')
        {
            // we want to see command we are sending
            receive(null, target, text);

            // prevent some commands
            if (startsWith(text, "/TARGET") || startsWith(text, "/LIST"))
                receive(null, target, "Command not autorized.");
            else
                send(text.substring(1));
        }
        else
        {
            doPrivmsg(target, text);
            // just to see what we send
            receive(getNick(), target, text);
        }
    }

    /**
     * Checks whether a string starts with another string (case insensitive).
     */
    private boolean startsWith(String s1, String s2)
    {
        return (s1.length() >= s2.length()) ? s1.substring(0, s2.length()).equalsIgnoreCase(s2) : false;
    }

    /**
     * Fire receive event
     */
    protected void fireReceiveEvent(String nick, String target, String text)
    {
        for (IRCEventListenerImpl l : listeners)
            l.onReceive(nick, target, text);
    }

    public void addListener(IRCEventListenerImpl listener)
    {
        if (!listeners.contains(listener))
            listeners.add(listener);
        addIRCEventListener(listener);
    }

    public boolean removeListener(IRCEventListenerImpl listener)
    {
        listeners.remove(listener);
        return removeIRCEventListener(listener);
    }

}
