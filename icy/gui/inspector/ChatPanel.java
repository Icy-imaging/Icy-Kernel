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
package icy.gui.inspector;

import icy.gui.component.CloseableTabbedPane;
import icy.gui.component.CloseableTabbedPane.CloseableTabbedPaneListener;
import icy.gui.component.ExternalizablePanel;
import icy.gui.component.button.IcyButton;
import icy.gui.component.button.IcyToggleButton;
import icy.gui.frame.progress.AnnounceFrame;
import icy.gui.frame.progress.ToolTipFrame;
import icy.gui.main.IcyDesktopPane;
import icy.gui.main.IcyDesktopPane.AbstractDesktopOverlay;
import icy.gui.main.MainFrame;
import icy.gui.preferences.ChatPreferencePanel;
import icy.gui.preferences.PreferenceFrame;
import icy.gui.util.ComponentUtil;
import icy.gui.util.FontUtil;
import icy.gui.util.GuiUtil;
import icy.main.Icy;
import icy.network.IRCClient;
import icy.network.IRCEventListenerImpl;
import icy.network.IRCUtil;
import icy.network.NetworkUtil;
import icy.network.NetworkUtil.InternetAccessListener;
import icy.preferences.ChatPreferences;
import icy.resource.ResourceUtil;
import icy.resource.icon.IcyIcon;
import icy.system.IcyExceptionHandler;
import icy.system.thread.ThreadUtil;
import icy.type.collection.CollectionUtil;
import icy.util.DateUtil;
import icy.util.GraphicsUtil;
import icy.util.StringUtil;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import org.schwering.irc.lib.IRCModeParser;
import org.schwering.irc.lib.IRCUser;

public class ChatPanel extends ExternalizablePanel implements InternetAccessListener
{
    /**
     * 
     */
    private static final long serialVersionUID = -3449422097073285247L;

    private static final int MAX_SIZE = 1 * 1024 * 1024; // 1 MB

    private static final String DEFAULT_CHANNEL = "#icy";

    private class CustomIRCClient extends IRCClient
    {
        public CustomIRCClient(String host, int port, String pass, String nickName, String userName, String realName)
        {
            super(host, port, pass, nickName, userName, realName);

            setName("Chat IRC listener");
        }
    }

    private class DesktopOverlay extends AbstractDesktopOverlay
    {
        final static int FG_ALPHA = 0xC0;
        final static int BG_ALPHA = 0xA0;

        final Color defaultBgColor;
        final Color defaultFgColor;
        Color bgColor;
        Color fgColor;

        public DesktopOverlay()
        {
            super();

            // default text colors
            defaultFgColor = getFgColor(Color.black);
            defaultBgColor = getBgColor(Color.lightGray);
            fgColor = defaultFgColor;
            bgColor = defaultBgColor;
        }

        private Color getFgColor(Color c)
        {
            return new Color(c.getRed(), c.getGreen(), c.getBlue(), FG_ALPHA);
        }

        private Color getBgColor(Color c)
        {
            return new Color(c.getRed(), c.getGreen(), c.getBlue(), BG_ALPHA);
        }

        private int setAttribute(Graphics2D g2, CharSequence text, int index)
        {
            final int len = text.length();

            // no more text
            if (index >= len)
                return len;

            int result = index + 1;
            int end;
            Font f;

            switch (text.charAt(index))
            {
                case IRCUtil.CHAR_RESET:
                    // reset to normal
                    g2.setFont(FontUtil.setStyle(g2.getFont(), Font.PLAIN));
                    fgColor = defaultFgColor;
                    bgColor = defaultBgColor;
                    break;

                case IRCUtil.CHAR_BOLD:
                    // switch bold
                    f = g2.getFont();
                    g2.setFont(FontUtil.setStyle(f, f.getStyle() ^ Font.BOLD));
                    break;

                case IRCUtil.CHAR_ITALIC:
                    // switch italic
                    f = g2.getFont();
                    g2.setFont(FontUtil.setStyle(f, f.getStyle() ^ Font.ITALIC));
                    break;

                case IRCUtil.CHAR_COLOR:
                    end = StringUtil.getNextNonDigitCharIndex(text, result);
                    // no more than 2 digits to encode color
                    if ((end == -1) || (end > (result + 2)))
                        end = Math.min(text.length(), result + 2);

                    // no color info --> restore default
                    if (end == result)
                    {
                        fgColor = defaultFgColor;
                        bgColor = defaultBgColor;
                    }
                    else
                    {
                        // get foreground color
                        fgColor = getFgColor(IRCUtil.getIRCColor(Integer.parseInt(text.subSequence(result, end)
                                .toString())));

                        // update position
                        result = end;

                        // search if we have background color
                        if ((result < len) && (text.charAt(result) == ','))
                        {
                            result++;

                            end = StringUtil.getNextNonDigitCharIndex(text, result);
                            // no more than 2 digits to encode color
                            if ((end == -1) || (end > (result + 2)))
                                end = Math.min(text.length(), result + 2);

                            // get background color
                            if (end != result)
                            {
                                // we don't want to support background color...
                                // bgColor =
                                // getBgColor(IRCUtil.getIRCColor(Integer.parseInt(text.subSequence(result,
                                // end)
                                // .toString())));

                                // update position
                                result = end;
                            }
                        }
                    }
                    break;

                default:
                    // System.out.println("code " + Integer.toString(text.charAt(index)));
                    break;
            }

            return result;
        }

        private int firstPreviousIndexOf(char c, int from)
        {
            int ind = from;
            if (ind >= content.length())
                return -1;

            while (ind >= 0)
            {
                if (content.charAt(ind) == c)
                    return ind;

                ind--;
            }

            return ind;
        }

        private int firstNextIndexOf(char c, int from)
        {
            final int len = content.length();
            int ind = from;

            while (ind < len)
            {
                if (content.charAt(ind) == c)
                    return ind;

                ind++;
            }

            return -1;
        }

        private boolean isChannelVisible(String channel)
        {
            // no channel name --> assume visible
            if (StringUtil.isEmpty(channel))
                return true;
            // private message --> assume visible
            if (channel.charAt(0) != '#')
                return true;

            for (String chan : ChatPreferences.getDesktopChannels().split(";"))
                if (channel.equalsIgnoreCase(fixChannelName(chan)))
                    return true;

            return false;
        }

        @Override
        public void paint(Graphics g, int width, int height)
        {
            final Graphics2D g2 = (Graphics2D) g.create();

            // modify to Monospaced font for easy space calculation
            g2.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

            // fixed size font, every character has the same bounds
            final Rectangle2D charRect = GraphicsUtil.getStringBounds(g2, "M");
            final float charHeight = (float) charRect.getHeight();
            final float charWidth = (float) charRect.getWidth();
            // keep 20 pixels margins
            final int charByLine = (int) ((width - 20) / charRect.getWidth());

            // no enough space to draw text
            if (charByLine <= 0)
                return;

            // start y position
            float y = height;
            // start at last character
            int position = content.length() - 1;

            while ((position > 0) && (y > 0))
            {
                // get paragraph offsets
                final int start = firstPreviousIndexOf('\n', position - 1) + 1;
                final int end = position;
                final int len = end - start;

                // update position
                position = start - 1;

                // get channel name end position
                final int chanEnd = firstNextIndexOf(':', start);
                // display only if this channel is visible on desktop
                if ((chanEnd != -1) && isChannelVisible(content.substring(start, chanEnd)))
                {

                    // calculate number of lines taken by the paragraph
                    int numLineParagraph = len / charByLine;
                    if ((len % charByLine) != 0)
                        numLineParagraph++;
                    // get paragraph height
                    final float paragraphHeight = numLineParagraph * charHeight;

                    // position to end of paragraph
                    y -= paragraphHeight;

                    // set default attributes
                    g2.setFont(FontUtil.setStyle(g2.getFont(), Font.PLAIN));
                    fgColor = defaultFgColor;
                    bgColor = defaultBgColor;

                    // process paragraph
                    int index = start;
                    while (index < end)
                    {
                        final int lineEnd = Math.min(index + charByLine, end);
                        float x = 10;

                        // process line
                        while (index < lineEnd)
                        {
                            int ctrlIndex = StringUtil.getNextCtrlCharIndex(content, index);
                            // end of line
                            if ((ctrlIndex == -1) || (ctrlIndex > lineEnd))
                                ctrlIndex = lineEnd;

                            // something to draw ?
                            if (index != ctrlIndex)
                            {
                                // get String to draw
                                final String str = content.substring(index, ctrlIndex);

                                // draw string
                                g2.setColor(bgColor);
                                g2.drawString(str, x - 1, y + 1);
                                g2.setColor(fgColor);
                                g2.drawString(str, x, y);

                                // set new X position
                                x += charWidth * str.length();
                            }

                            if (ctrlIndex < lineEnd)
                                index = setAttribute(g2, content, ctrlIndex);
                            else
                                index = lineEnd;
                        }

                        // pass to next line
                        y += charHeight;
                    }

                    // set position back to end of paragraph
                    y -= paragraphHeight;
                }
            }

            g2.dispose();
        }
    }

    private class CustomIRCClientListener extends IRCEventListenerImpl
    {
        public CustomIRCClientListener()
        {
            super();
        }

        @Override
        public void onConnected()
        {
            super.onConnected();

            // join default channel
            client.doJoin(DEFAULT_CHANNEL);
            // join extras channels
            for (String extraChannel : ChatPreferences.getExtraChannels().split(";"))
                if (!StringUtil.isEmpty(extraChannel))
                    client.doJoin(fixChannelName(extraChannel));
            // authentication for registered user
            final String pass = ChatPreferences.getUserPassword();
            if (!StringUtil.isEmpty(pass))
                client.doPrivmsg("NickServ", "identify " + ChatPreferences.getNickname() + " " + pass);

            connectButton.setEnabled(true);
            refreshGUI();
        }

        @Override
        public void onDisconnected()
        {
            super.onDisconnected();

            connectButton.setEnabled(true);
            refreshGUI();
            refreshUsers();
        }

        @Override
        public void onError(int num, String msg)
        {
            super.onError(num, msg);

            switch (num)
            {
                case 432:
                case 433:
                    // Erroneous Nickname
                    if (!connectButton.isEnabled())
                    {
                        // if we were connecting, we disconnect
                        disconnect("Incorrect nickname");
                        if (num == 432)
                            onReceive(null, null, "Your nickname contains invalid caracters.");
                    }
                    refreshGUI();
                    break;

                case 437:
                    client.doNick(client.getNick() + "_");
                    break;
            }
        }

        @Override
        public void onJoin(String chan, IRCUser u)
        {
            if (isCurrentUser(u))
            {
                // add the channel pane if needed
                addChannelPane(chan);
                if (getShowStatusMessages())
                    onReceive(null, chan, "Welcome to " + IRCUtil.getBoldString(chan.substring(1)) + ".");
            }
            else
            {
                if (getShowStatusMessages())
                    onReceive(null, chan, u.getNick() + " joined.");
            }

            // refresh user list
            refreshUsers();
        }

        @Override
        public void onKick(String chan, IRCUser u, String nickPass, String msg)
        {
            super.onKick(chan, u, nickPass, msg);

            // refresh user list
            refreshUsers();
        }

        @Override
        public void onLeave(String chan, IRCUser u, String msg)
        {
            if (getShowStatusMessages())
            {
                if (StringUtil.isEmpty(msg))
                    onReceive(null, chan, u.getNick() + " left.");
                else
                    onReceive(null, chan, u.getNick() + " left" + " (" + msg + ").");
            }

            // remove the channel pane if needed
            if (isCurrentUser(u))
                removeChannelPane(chan);

            // refresh user list
            refreshUsers();
        }

        @Override
        public void onMode(IRCUser u, String nickPass, String mode)
        {
            // ignore mode set message
            // super.onMode(u, nickPass, mode);
        }

        @Override
        public void onMode(String chan, IRCUser u, IRCModeParser mp)
        {
            // ignore mode set message
            // super.onMode(chan, u, mp);
        }

        @Override
        public void onNick(IRCUser u, String nickNew)
        {
            super.onNick(u, nickNew);

            // update nickname
            if (isCurrentUser(u))
                ChatPreferences.setNickname(nickNew);
            refreshGUI();
            refreshUsers();
        }

        @Override
        public void onNotice(String target, IRCUser u, String msg)
        {
            if (msg != null)
            {
                if (getShowStatusMessages())
                {
                    if (msg.indexOf("Looking up your hostname") != -1)
                        onReceive(null, null, "Connecting...");
                }

                // ignore all others notices...

                // else
                // {
                // if (msg.indexOf("Checking Ident") != -1)
                // return;
                // if (msg.indexOf("your hostname") != -1)
                // return;
                // if (msg.indexOf("No Ident response") != -1)
                // return;
                //
                // super.onNotice(target, u, msg);
                // }
            }
        }

        @Override
        public void onQuit(IRCUser u, String msg)
        {
            super.onQuit(u, msg);

            refreshUsers();
        }

        @Override
        public void unknown(String a, String b, String c, String d)
        {
            onReceive(null, null, d);
        }

        @Override
        public void onReply(int num, String value, String msg)
        {
            switch (num)
            {
                case 353:
                    // add users to tmp list
                    tmpUserList.addAll(CollectionUtil.asList(msg.split(" ")));
                    break;

                case 366:
                    // end of user list
                    userList.setListData(tmpUserList.toArray(new String[tmpUserList.size()]));
                    tmpUserList.clear();
                    break;

                case 1:
                case 2:
                case 3:
                case 4:
                case 5:
                case 250:
                case 251:
                case 252:
                case 253:
                case 254:
                case 255:
                case 256:
                case 257:
                case 258:
                case 259:
                case 261:
                case 262:
                case 263:
                case 265:
                case 266:
                case 372:
                case 375:
                case 376:
                    // ignore
                    break;

                default:
                    onReceive(null, null, msg);
            }
        }

        @Override
        public void onReceive(String nick, String target, String msg)
        {
            final String n;
            final String t;

            // ignore close link
            if (msg.startsWith("Error: Closing Link:"))
                return;

            // ignore CTCP version request (can come from hacker)
            if (msg.equals("\001VERSION\001"))
                return;

            // target is current user --> incoming private message
            if (isCurrentUser(target))
            {
                // get the source name of private message
                if (StringUtil.isEmpty(nick))
                    n = "serv";
                else
                    n = nick;

                // incoming private message --> use source as target name
                t = n;

                // add private channel
                addChannelPane(n);
            }
            else
            {
                n = nick;
                t = target;
            }

            // show message
            addMessage(n, t, msg);
        }

        @Override
        protected boolean getShowStatusMessages()
        {
            return ChatPreferences.getShowStatusMessages();
        }
    }

    private class ChannelPanel
    {
        final String name;

        final JScrollPane scrollPane;
        final JTextPane editor;
        final StyledDocument doc;

        public ChannelPanel(String name)
        {
            super();

            this.name = name;

            editor = new JTextPane();
            editor.setEditable(false);
            doc = editor.getStyledDocument();

            scrollPane = new JScrollPane(editor);
        }
    }

    /**
     * GUI
     */
    JPanel panelBottom;
    CloseableTabbedPane tabPane;
    JScrollPane usersScrollPane;
    JList userList;
    JTextField sendEditor;
    JTextField txtNickName;
    IcyToggleButton connectButton;
    IcyToggleButton showUserPaneButton;
    IcyToggleButton desktopOverlayButton;
    IcyButton advancedButton;

    /**
     * Desktop GUI
     */
    final JPanel desktopPanel;
    final JTextField sendEditorDesktop;
    final IcyButton hideDesktopChatButton;
    final DesktopOverlay desktopOverlay;

    /**
     * IRC client
     */
    CustomIRCClient client;

    /**
     * internals
     */
    final ArrayList<ChannelPanel> channelPanes;
    final ArrayList<String> tmpUserList;
    final SimpleAttributeSet attributes;
    final CustomIRCClientListener ircListener;
    final StringBuilder content;
    String lastCmd;

    public ChatPanel()
    {
        super("Chat room", "chatPanel");

        channelPanes = new ArrayList<ChannelPanel>();
        tmpUserList = new ArrayList<String>();
        attributes = new SimpleAttributeSet();
        content = new StringBuilder();
        lastCmd = "";
        client = null;

        // build GUI
        initialize();

        // add default channel panel (need to be done when base GUI is done)
        addChannelPane(DEFAULT_CHANNEL);

        // build desktop GUI
        sendEditorDesktop = new JTextField();
        sendEditorDesktop.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                sendEditContent((JTextField) e.getSource());
            }
        });
        sendEditorDesktop.addKeyListener(new KeyAdapter()
        {
            @Override
            public void keyPressed(KeyEvent e)
            {
                if (e.getKeyCode() == KeyEvent.VK_UP)
                    sendEditorDesktop.setText(lastCmd);
            }
        });

        hideDesktopChatButton = new IcyButton(new IcyIcon(ResourceUtil.ICON_SQUARE_DOWN, 20));
        hideDesktopChatButton.setFlat(true);
        ComponentUtil.setFixedWidth(hideDesktopChatButton, 20);
        hideDesktopChatButton.setToolTipText("Disable desktop chat overlay");
        hideDesktopChatButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                ChatPreferences.setDesktopOverlay(false);
                refreshDesktopOverlayState();
                new ToolTipFrame("<b>Desktop chat overlay</b><br><br>"
                        + "You just disabled the desktop chat overlay<br>"
                        + "but you can always access and enable it<br>" + "from the inspector \"Chat\" tab.",
                        "chat.overlay");
            }
        });

        // desktop bottom panel
        desktopPanel = GuiUtil.createLineBoxPanel(sendEditorDesktop, Box.createHorizontalStrut(2),
                Box.createHorizontalGlue(), hideDesktopChatButton);

        desktopOverlay = new DesktopOverlay();

        StyleConstants.setFontFamily(attributes, "arial");
        StyleConstants.setFontSize(attributes, 11);
        StyleConstants.setForeground(attributes, Color.black);

        sendEditor.setFont(new Font("arial", 0, 11));
        sendEditor.addKeyListener(new KeyAdapter()
        {
            @Override
            public void keyPressed(KeyEvent e)
            {
                if (e.getKeyCode() == KeyEvent.VK_UP)
                    sendEditor.setText(lastCmd);
            }
        });

        ircListener = new CustomIRCClientListener();

        refreshGUI();
        refreshDesktopOverlayState();

        addStateListener(new StateListener()
        {
            @Override
            public void stateChanged(ExternalizablePanel source, boolean externalized)
            {
                refreshGUI();
            }
        });

        // call default internet connection callback to process auto connect
        if (NetworkUtil.hasInternetAccess())
            internetUp();

        NetworkUtil.addInternetAccessListener(this);
    }

    public boolean isConnected()
    {
        return (client != null) && client.isConnected();
    }

    /**
     * Do IRC connection
     */
    public void connect()
    {
        if (!isConnected())
        {
            // connecting
            connectButton.setEnabled(false);
            connectButton.setToolTipText("connecting...");

            final String nickName = txtNickName.getText();
            // apply nickname
            ChatPreferences.setNickname(nickName);
            String userName = nickName;
            String realName = ChatPreferences.getRealname();

            if (StringUtil.isEmpty(userName))
                userName = nickName;
            if (StringUtil.isEmpty(realName))
                realName = nickName;

            // remove listener from previous client
            if (client != null)
                client.removeListener(ircListener);

            // we need to recreate the client
            client = new CustomIRCClient(ChatPreferences.getServer(), ChatPreferences.getPort(),
                    ChatPreferences.getServerPassword(), nickName, userName, realName);
            client.addListener(ircListener);

            // process connection in a separate thread as it can take sometime
            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        client.connect();
                    }
                    catch (IOException e)
                    {
                        // error while connecting
                        IcyExceptionHandler.showErrorMessage(e, false, false);
                        System.out.println("Cannot connect to chat.");
                        System.out.println("If you use a proxy, verify you have valid SOCKS settings.");

                        // update GUI
                        ThreadUtil.invokeLater(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                connectButton.setEnabled(true);
                                connectButton.setToolTipText("Not connected - Click to connect");
                            }
                        });
                    }
                }
            }, "IRC client connection").start();
        }
    }

    public void disconnect(String message)
    {
        if (isConnected())
        {
            // closing connection
            connectButton.setEnabled(false);
            connectButton.setToolTipText("closing connexion...");
            client.doQuit(message);
        }
    }

    protected void sendEditContent(JTextField txtField)
    {
        final String text = txtField.getText();

        if (isConnected() && !StringUtil.isEmpty(text))
        {
            // send from desktop editor ?
            if (txtField == sendEditorDesktop)
                // send text to main default channel
                client.send(DEFAULT_CHANNEL, text);
            else
                // send text to current target
                client.send(getCurrentChannel(), text);
        }

        txtField.setText("");
        lastCmd = text;
    }

    /**
     * Build the panel
     */
    private void initialize()
    {
        setLayout(new BorderLayout(0, 0));

        tabPane = new CloseableTabbedPane(SwingConstants.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
        tabPane.addCloseableTabbedPaneListener(new CloseableTabbedPaneListener()
        {
            @Override
            public void tabClosed(int index, String title)
            {
                // it was a channel tab, leave channel
                if (isConnected() && title.startsWith("#"))
                    client.doPart(title);
                else
                    // directly remove the channel pane
                    removeChannelPane(title);
            }

            @Override
            public boolean tabClosing(int index, String title)
            {
                return true;
            }
        });
        tabPane.addChangeListener(new ChangeListener()
        {
            @Override
            public void stateChanged(ChangeEvent e)
            {
                // set back default tab color
                tabPane.setBackgroundAt(tabPane.getSelectedIndex(), tabPane.getBackground());
                refreshUsers();
            }
        });
        add(tabPane, BorderLayout.CENTER);

        usersScrollPane = new JScrollPane();
        usersScrollPane.setPreferredSize(new Dimension(130, 200));

        JLabel lblUtilisateur = new JLabel("Users");
        lblUtilisateur.setFont(new Font("Tahoma", Font.BOLD, 11));
        lblUtilisateur.setBorder(new LineBorder(Color.GRAY, 1, true));
        lblUtilisateur.setHorizontalAlignment(SwingConstants.CENTER);
        usersScrollPane.setColumnHeaderView(lblUtilisateur);

        userList = new JList();
        userList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        userList.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                if (!isConnected())
                    return;

                // double click
                if (e.getClickCount() == 2)
                {
                    final int index = userList.locationToIndex(e.getPoint());

                    if (index != -1)
                    {
                        final String nick = (String) userList.getSelectedValue();

                        // add private chat nick pane
                        if (!isCurrentUser(nick) && !StringUtil.isEmpty(nick))
                            addChannelPane(nick);
                    }
                }
            }
        });
        userList.setToolTipText("Double click on an username to send private message");

        usersScrollPane.setViewportView(userList);

        panelBottom = new JPanel();
        add(panelBottom, BorderLayout.SOUTH);
        panelBottom.setLayout(new BorderLayout(0, 0));

        sendEditor = new JTextField();
        sendEditor.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                sendEditContent((JTextField) e.getSource());
            }
        });
        sendEditor.setColumns(10);
        panelBottom.add(sendEditor, BorderLayout.SOUTH);

        JPanel topPanel = new JPanel();
        topPanel.setBorder(new EmptyBorder(0, 0, 2, 0));
        add(topPanel, BorderLayout.NORTH);

        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.LINE_AXIS));

        txtNickName = new JTextField();
        txtNickName.setMaximumSize(new Dimension(2147483647, 24));
        txtNickName.setText(ChatPreferences.getNickname());
        txtNickName.setToolTipText("Nick name");
        txtNickName.setColumns(10);
        topPanel.add(txtNickName);

        Component horizontalStrut_3 = Box.createHorizontalStrut(4);
        topPanel.add(horizontalStrut_3);

        JButton btnSetNickName = new JButton("Set");
        btnSetNickName.setFocusPainted(false);
        btnSetNickName.setPreferredSize(new Dimension(40, 23));
        btnSetNickName.setMaximumSize(new Dimension(40, 23));
        btnSetNickName.setMinimumSize(new Dimension(40, 23));
        btnSetNickName.setMargin(new Insets(2, 8, 2, 8));
        btnSetNickName.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                final String nickName = txtNickName.getText();

                if (isConnected())
                    client.doNick(nickName);
                else
                    ChatPreferences.setNickname(nickName);
            }
        });
        btnSetNickName.setToolTipText("Set nick name");
        topPanel.add(btnSetNickName);

        Component horizontalGlue = Box.createHorizontalGlue();
        topPanel.add(horizontalGlue);

        Component horizontalStrut_2 = Box.createHorizontalStrut(10);
        topPanel.add(horizontalStrut_2);

        connectButton = new IcyToggleButton(new IcyIcon(ResourceUtil.ICON_ON_OFF, 20));
        connectButton.setFocusPainted(false);
        connectButton.setMaximumSize(new Dimension(32, 32));
        connectButton.setMinimumSize(new Dimension(24, 24));
        connectButton.setPreferredSize(new Dimension(24, 24));
        connectButton.setToolTipText("Connect");
        connectButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                if (connectButton.isSelected())
                {
                    if (!NetworkUtil.hasInternetAccess())
                    {
                        new AnnounceFrame("You need internet connection to connect to the chat.", 10);
                        connectButton.setSelected(false);
                    }
                    else
                        connect();
                }
                else
                    disconnect("Manual disconnect");
            }
        });
        topPanel.add(connectButton);

        Component horizontalStrut_1 = Box.createHorizontalStrut(2);
        topPanel.add(horizontalStrut_1);

        showUserPaneButton = new IcyToggleButton(new IcyIcon("user", 20));
        showUserPaneButton.setFocusPainted(false);
        showUserPaneButton.setMaximumSize(new Dimension(32, 32));
        showUserPaneButton.setSelected(ChatPreferences.getShowUsersPanel());
        showUserPaneButton.setMinimumSize(new Dimension(24, 24));
        showUserPaneButton.setPreferredSize(new Dimension(24, 24));
        showUserPaneButton.setToolTipText("Show connected users");
        showUserPaneButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                final boolean visible = showUserPaneButton.isSelected();

                ChatPreferences.setShowUsersPanel(visible);
                refreshGUI();
            }
        });
        topPanel.add(showUserPaneButton);

        Component horizontalStrut_5 = Box.createHorizontalStrut(2);
        topPanel.add(horizontalStrut_5);

        desktopOverlayButton = new IcyToggleButton(new IcyIcon(ResourceUtil.ICON_CHAT, 20));
        desktopOverlayButton.setFocusPainted(false);
        desktopOverlayButton.setMaximumSize(new Dimension(32, 32));
        desktopOverlayButton.setSelected(ChatPreferences.getDesktopOverlay());
        desktopOverlayButton.setMinimumSize(new Dimension(24, 24));
        desktopOverlayButton.setPreferredSize(new Dimension(24, 24));
        desktopOverlayButton.setToolTipText("Enabled chat on desktop");
        desktopOverlayButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                final boolean visible = desktopOverlayButton.isSelected();

                ChatPreferences.setDesktopOverlay(visible);
                refreshDesktopOverlayState();
            }
        });
        topPanel.add(desktopOverlayButton);

        Component horizontalStrut_6 = Box.createHorizontalStrut(2);
        topPanel.add(horizontalStrut_6);

        advancedButton = new IcyButton(new IcyIcon(ResourceUtil.ICON_COG, 20));
        advancedButton.setFocusPainted(false);
        advancedButton.setMaximumSize(new Dimension(32, 32));
        advancedButton.setMinimumSize(new Dimension(24, 24));
        advancedButton.setPreferredSize(new Dimension(24, 24));
        advancedButton.setToolTipText("Advanced settings");
        advancedButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                new PreferenceFrame(ChatPreferencePanel.NODE_NAME);
            }
        });
        topPanel.add(advancedButton);
    }

    protected String fixChannelName(String channel)
    {
        if (!StringUtil.isEmpty(channel) && (channel.charAt(0) != '#'))
            return "#" + channel;

        return channel;
    }

    public void addChannelPane(final String channel)
    {
        // already exists...
        if (getChannelPaneIndex(channel) != -1)
            return;

        ThreadUtil.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                final ChannelPanel channelPane = new ChannelPanel(channel);

                // add to list
                channelPanes.add(channelPane);

                // and add to gui
                int index = tabPane.indexOfTab(channel);
                // add only if not already present (should alway be the case)
                if (index == -1)
                {
                    tabPane.addTab(channel, channelPane.scrollPane);
                    // get index
                    index = tabPane.indexOfTab(channel);
                    // default channel cannot be closed
                    if (channel.equals(DEFAULT_CHANNEL))
                        tabPane.setTabClosable(index, false);
                    tabPane.setSelectedIndex(index);
                }
            }
        });
    }

    public void removeChannelPane(final String channel)
    {
        final int ind = getChannelPaneIndex(channel);

        // channel exists --> remove it
        if (ind != -1)
        {
            ThreadUtil.invokeLater(new Runnable()
            {
                @Override
                public void run()
                {
                    channelPanes.remove(ind);

                    // remove from tabbed pane if needed
                    final int indTab = tabPane.indexOfTab(channel);
                    if (indTab != -1)
                        tabPane.removeTabAt(indTab);
                }
            });
        }
    }

    public int getChannelPaneIndex(String channel)
    {
        final String c;

        // empty channel means default channel
        if (StringUtil.isEmpty(channel))
            c = DEFAULT_CHANNEL;
        else
            c = channel;

        for (int i = 0; i < channelPanes.size(); i++)
        {
            final ChannelPanel cp = channelPanes.get(i);

            if (cp.name.equalsIgnoreCase(c))
                return i;
        }

        return -1;
    }

    public ChannelPanel getChannelPane(String channel)
    {
        final int ind = getChannelPaneIndex(channel);

        if (ind != -1)
            return channelPanes.get(ind);

        return null;
    }

    /**
     * Returns the current visible channel (tab channel visible).
     */
    public String getCurrentChannel()
    {
        final int ind = tabPane.getSelectedIndex();

        if (ind != -1)
            return tabPane.getTitleAt(ind);

        return null;
    }

    /**
     * Mark channel pane with blue color.
     */
    protected void markChannelPane(String channel)
    {
        final int index = getChannelPaneIndex(channel);

        if ((index != -1) && (tabPane.getSelectedIndex() != index))
        {
            ThreadUtil.invokeLater(new Runnable()
            {
                @Override
                public void run()
                {
                    // change output console tab color when new data
                    if (index < tabPane.getTabCount())
                        tabPane.setBackgroundAt(index, Color.blue);
                }
            });
        }
    }

    public JTextPane getChannelEditor(String channel)
    {
        final ChannelPanel cp = getChannelPane(channel);

        if (cp != null)
            return cp.editor;

        return null;
    }

    public StyledDocument getChannelDocument(String channel)
    {
        final ChannelPanel cp = getChannelPane(channel);

        if (cp != null)
            return cp.doc;

        return null;
    }

    protected boolean isCurrentUser(String nick)
    {
        return StringUtil.equals(nick, ChatPreferences.getNickname());
    }

    protected boolean isCurrentUser(IRCUser u)
    {
        if (u != null)
            return isCurrentUser(u.getNick());

        return false;
    }

    // int getUsersScrollPaneWidth()
    // {
    // int result = usersScrollPane.getSize().width;
    // if (result == 0)
    // return usersScrollPane.getPreferredSize().width;
    // return result;
    // }
    //
    // int getCurrentWidth()
    // {
    // int result = getSize().width;
    // if (result == 0)
    // return getPreferredSize().width;
    // return result;
    // }

    void addMessage(final String nick, final String target, final String msg)
    {
        ThreadUtil.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                final String channel;
                final String nickStr;

                if (StringUtil.isEmpty(target))
                    channel = DEFAULT_CHANNEL;
                else
                    channel = target;

                if (StringUtil.isEmpty(nick))
                    nickStr = "";
                else
                    nickStr = "<" + nick + "> ";

                final String timeStr = DateUtil.now("[HH:mm] ");

                synchronized (content)
                {
                    content.append(channel + ": " + timeStr + nickStr + msg + "\n");

                    // limit to maximum size
                    if (content.length() > MAX_SIZE)
                        content.delete(0, content.length() - MAX_SIZE);

                    refreshDesktopOverlay();
                }

                try
                {
                    final JTextPane editor = getChannelEditor(channel);
                    final StyledDocument doc = getChannelDocument(channel);

                    if ((editor != null) && (doc != null))
                    {
                        synchronized (editor)
                        {
                            IRCUtil.insertString(timeStr + nickStr + msg + "\n", doc, attributes);

                            // limit to maximum size
                            if (doc.getLength() > MAX_SIZE)
                                doc.remove(0, doc.getLength() - MAX_SIZE);

                            editor.setCaretPosition(doc.getLength());
                        }
                    }
                }
                catch (BadLocationException e)
                {
                    e.printStackTrace();
                }

                // mark channel pane color
                markChannelPane(channel);
            }
        });
    }

    /**
     * Refresh GUI state
     */
    void refreshGUI()
    {
        ThreadUtil.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                if (isConnected())
                {
                    final String nick = client.getNick();

                    if (!StringUtil.equals(txtNickName.getText(), nick))
                        txtNickName.setText(nick);

                    sendEditor.setEditable(true);
                    sendEditorDesktop.setEditable(true);
                    connectButton.setSelected(true);
                    connectButton.setToolTipText("Connected - Click to disconnect");
                }
                else
                {
                    sendEditor.setEditable(false);
                    sendEditorDesktop.setEditable(false);
                    connectButton.setSelected(false);
                    connectButton.setToolTipText("Not connected - Click to connect");
                }

                // user panel visible
                remove(usersScrollPane);
                panelBottom.remove(usersScrollPane);
                if (ChatPreferences.getShowUsersPanel())
                {
                    if ((getWidth() * 1.5) > getHeight())
                        add(usersScrollPane, BorderLayout.EAST);
                    else
                        panelBottom.add(usersScrollPane, BorderLayout.CENTER);
                }

                validate();
            }
        });
    }

    /**
     * Refresh users list
     */
    void refreshUsers()
    {
        if (isConnected())
        {
            final String channel = getCurrentChannel();

            if (!StringUtil.isEmpty(channel))
            {
                // private channel ?
                if (channel.charAt(0) != '#')
                    // display current user and channel name as users
                    userList.setListData(new String[] {ChatPreferences.getNickname(), channel});
                else
                    client.doNames(channel);
            }
        }
        else
            userList.setListData(new String[0]);
    }

    /**
     * Refresh desktop overlay state
     */
    public void refreshDesktopOverlayState()
    {
        final MainFrame mainFrame = Icy.getMainInterface().getMainFrame();
        final IcyDesktopPane desktopPane = Icy.getMainInterface().getDesktopPane();

        if ((mainFrame != null) && (desktopPane != null))
        {
            final JPanel centerPanel = mainFrame.getCenterPanel();

            // desktop overlay enable ?
            if (ChatPreferences.getDesktopOverlay())
            {
                // add desktop overlay
                desktopPane.addOverlay(desktopOverlay);
                centerPanel.add(desktopPanel, BorderLayout.SOUTH);
                centerPanel.revalidate();
            }
            else
            {
                // remove desktop overlay
                desktopPane.removeOverlay(desktopOverlay);
                centerPanel.remove(desktopPanel);
                centerPanel.revalidate();
            }

            // refresh desktop
            desktopPane.repaint();
        }

        desktopOverlayButton.setSelected(ChatPreferences.getDesktopOverlay());
    }

    /**
     * Refresh desktop overlay
     */
    void refreshDesktopOverlay()
    {
        final IcyDesktopPane desktopPane = Icy.getMainInterface().getDesktopPane();

        // refresh desktop overlay
        if ((desktopPane != null) && ChatPreferences.getDesktopOverlay())
            desktopPane.repaint();
    }

    @Override
    public void internetUp()
    {
        if (!isConnected() && ChatPreferences.getAutoConnect())
            connect();
    }

    @Override
    public void internetDown()
    {
        // disconnect("Connection lost");
    }
}
