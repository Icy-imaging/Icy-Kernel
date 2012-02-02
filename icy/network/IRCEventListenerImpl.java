/**
 * 
 */
package icy.network;

import org.schwering.irc.lib.IRCEventListener;
import org.schwering.irc.lib.IRCModeParser;
import org.schwering.irc.lib.IRCUser;

/**
 * @author Stephane
 */
public abstract class IRCEventListenerImpl implements IRCEventListener
{
    public abstract void onReceive(String text);

    public void onConnected()
    {
        onReceive("Connected");
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
        onReceive("Disconnected");
    }

    @Override
    public void onError(String msg)
    {
        onReceive("Error: " + msg);
    }

    @Override
    public void onError(int num, String msg)
    {
        onReceive("Error #" + num + ": " + msg);
    }

    @Override
    public void onInvite(String chan, IRCUser u, String nickPass)
    {
        onReceive(chan + "> " + u.getNick() + " invites " + nickPass);
    }

    @Override
    public void onJoin(String chan, IRCUser u)
    {
        onReceive(chan + "> " + u.getNick() + " joins " + chan);
    }

    @Override
    public void onKick(String chan, IRCUser u, String nickPass, String msg)
    {
        onReceive(chan + "> " + u.getNick() + " kicks " + nickPass);
    }

    public void onLeave(String chan, IRCUser u, String msg)
    {
        onReceive(chan + "> " + u.getNick() + " leaves " + chan);
    }

    @Override
    public void onMode(IRCUser u, String nickPass, String mode)
    {
        onReceive("Mode: " + u.getNick() + " sets modes " + mode + " " + nickPass);
    }

    @Override
    public void onMode(String chan, IRCUser u, IRCModeParser mp)
    {
        onReceive(chan + "> " + u.getNick() + " sets mode: " + mp.getLine());
    }

    @Override
    public void onNick(IRCUser u, String nickNew)
    {
        onReceive("Nick: " + u.getNick() + " is now known as " + nickNew);
    }

    @Override
    public void onNotice(String target, IRCUser u, String msg)
    {
        onReceive(target + "> " + u.getNick() + " (notice): " + msg);
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
        onReceive(chan + "> " + u.getNick() + ": " + msg);
    }

    @Override
    public void onQuit(IRCUser u, String msg)
    {
        onReceive("Quit: " + u.getNick());
    }

    @Override
    public void onReply(int num, String value, String msg)
    {
        onReceive("Reply #" + num + ": " + value + " " + msg);
    }

    @Override
    public void onTopic(String chan, IRCUser u, String topic)
    {
        onReceive(chan + "> " + u.getNick() + " changes topic into: " + topic);
    }

    @Override
    public void onPing(String p)
    {
        // do nothing
    }

    @Override
    public void unknown(String a, String b, String c, String d)
    {
        onReceive(a + " " + b + ">" + c + " " + d);
        // onReceive(d);
    }
}
