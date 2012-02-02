/**
 * 
 */
package icy.network;

import java.util.ArrayList;

import org.schwering.irc.lib.IRCConnection;

/**
 * Simple IRCClient class.
 * 
 * @author Stephane
 */
public class IRCClient extends IRCConnection
{
    /** The current default target of PRIVMSGs (a channel or nickname). */
    protected String target;

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
    protected void receive(String text)
    {
        // notify listeners about receive
        fireReceiveEvent(text);
    }

    /**
     * Send unparsed text to IRC server.
     */
    public void sendText(String text)
    {
        if (text == null || text.length() == 0)
            return;

        if (text.charAt(0) == '/')
        {
            // we want to see command we are sending
            receive(text);

            // prevent some commands
            if (startsWith(text, "/TARGET") || startsWith(text, "/JOIN") || startsWith(text, "/LIST")
                    || startsWith(text, "/PART"))
                receive("Command not autorized.");
            else
                send(text.substring(1));
        }
        else
        {
            doPrivmsg(target, text);
            writeMsg(getNick(), target, text);
        }
    }

    /**
     * Write message.
     */
    public void writeMsg(String nick, String target, String msg)
    {
        // we want also to see send text
        receive(target + "> " + msg);
    }

    @Override
    public void doJoin(String chan)
    {
        super.doJoin(chan);
        target = chan;
    }

    public String getTarget()
    {
        return target;
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
    protected void fireReceiveEvent(String text)
    {
        for (IRCEventListenerImpl l : listeners)
            l.onReceive(text);
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
