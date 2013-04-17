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
package icy.network;

import icy.util.StringUtil;

import org.schwering.irc.lib.IRCEventListener;
import org.schwering.irc.lib.IRCModeParser;
import org.schwering.irc.lib.IRCUser;

/**
 * @author Stephane
 */
public abstract class IRCEventListenerImpl implements IRCEventListener
{
    /**
     * Called on message receive event.
     * 
     * @param nick
     *        nickname of the author of message if any (can be null).
     * @param target
     *        channel or nickname of destination if any (can be null).
     * @param text
     *        message
     */
    public abstract void onReceive(String nick, String target, String text);

    public void onConnected()
    {
        if (getShowStatusMessages())
            onReceive(null, null, "Connected");
    }

    @Override
    public void onRegistered()
    {
        // redirect
        onConnected();
    }

    @Override
    public void onDisconnected()
    {
        if (getShowStatusMessages())
            onReceive(null, null, "Disconnected.");
    }

    @Override
    public void onError(String msg)
    {
        onReceive(null, null, "Error: " + msg);
    }

    @Override
    public void onError(int num, String msg)
    {
        onReceive(null, null, "Error #" + num + ": " + msg);
    }

    @Override
    public void onInvite(String chan, IRCUser u, String nickPass)
    {
        if (getShowStatusMessages())
            onReceive(null, chan, u.getNick() + " invites " + nickPass + ".");
    }

    @Override
    public void onJoin(String chan, IRCUser u)
    {
        if (getShowStatusMessages())
            onReceive(null, chan, u.getNick() + " joins " + chan + ".");
    }

    @Override
    public void onKick(String chan, IRCUser u, String nickPass, String msg)
    {
        if (getShowStatusMessages())
            onReceive(null, chan, u.getNick() + " kicks " + nickPass + ".");
    }

    public void onLeave(String chan, IRCUser u, String msg)
    {
        if (getShowStatusMessages())
        {
            if (StringUtil.isEmpty(msg))
                onReceive(null, chan, u.getNick() + " leaves " + chan + ".");
            else
                onReceive(null, chan, u.getNick() + " leaves " + chan + "(" + msg + ").");
        }
    }

    @Override
    public void onMode(IRCUser u, String nickPass, String mode)
    {
        if (getShowStatusMessages())
            onReceive(null, null, u.getNick() + " sets modes " + mode + " " + nickPass + ".");
    }

    @Override
    public void onMode(String chan, IRCUser u, IRCModeParser mp)
    {
        if (getShowStatusMessages())
            onReceive(null, chan, u.getNick() + " sets mode: " + mp.getLine() + ".");
    }

    @Override
    public void onNick(IRCUser u, String nickNew)
    {
        if (getShowStatusMessages())
            onReceive(null, null, u.getNick() + " is now known as " + nickNew + ".");
    }

    @Override
    public void onNotice(String target, IRCUser u, String msg)
    {
        onReceive(u.getNick(), target, "notice: " + msg);
    }

    @Override
    public void onPart(String chan, IRCUser u, String msg)
    {
        // redirect
        onLeave(chan, u, msg);
    }

    @Override
    public void onPrivmsg(String chan, IRCUser u, String msg)
    {
        onReceive(u.getNick(), chan, msg);
    }

    @Override
    public void onQuit(IRCUser u, String msg)
    {
        if (getShowStatusMessages())
        {
            if (StringUtil.isEmpty(msg))
                onReceive(null, null, u.getNick() + " quits chat.");
            else
                onReceive(null, null, u.getNick() + " quits chat (" + msg + ").");
        }
    }

    @Override
    public void onReply(int num, String value, String msg)
    {
        onReceive(null, null, "Reply #" + num + ": " + value + " " + msg);
    }

    @Override
    public void onTopic(String chan, IRCUser u, String topic)
    {
        onReceive(null, chan, u.getNick() + " changes topic into: " + topic + ".");
    }

    @Override
    public void onPing(String p)
    {
        // do nothing
    }

    @Override
    public void unknown(String a, String b, String c, String d)
    {
        onReceive(null, null, a + " " + b + ">" + c + " " + d);
        // onReceive(d);
    }

    protected abstract boolean getShowStatusMessages();
}
