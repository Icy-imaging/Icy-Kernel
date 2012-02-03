package icy.gui.inspector;

import icy.gui.component.ComponentUtil;
import icy.gui.component.ExternalizablePanel;
import icy.gui.component.button.IcyButton;
import icy.gui.component.button.IcyToggleButton;
import icy.gui.frame.progress.ToolTipFrame;
import icy.gui.main.IcyDesktopPane;
import icy.gui.main.IcyDesktopPane.AbstractDesktopOverlay;
import icy.gui.main.MainFrame;
import icy.gui.util.GuiUtil;
import icy.main.Icy;
import icy.network.IRCClient;
import icy.network.IRCEventListenerImpl;
import icy.preferences.ApplicationPreferences;
import icy.preferences.GeneralPreferences;
import icy.preferences.XMLPreferences;
import icy.resource.ResourceUtil;
import icy.system.IcyExceptionHandler;
import icy.system.thread.ThreadUtil;
import icy.util.DateUtil;
import icy.util.Random;
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
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextLayout;
import java.io.IOException;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import org.schwering.irc.lib.IRCModeParser;
import org.schwering.irc.lib.IRCUser;

public class ChatPanel extends ExternalizablePanel
// public class ChatPanel extends JPanel
{
    private class CustomIRCClient extends IRCClient
    {
        public CustomIRCClient(String host, int port, String pass, String nickName, String userName, String realName)
        {
            super(host, port, pass, nickName, userName, realName);
        }

        @Override
        public void writeMsg(String nick, String target, String msg)
        {
            // we want also to see send text
            if (StringUtil.equals(target.substring(1), ApplicationPreferences.getIrcChannel()))
                receive(DateUtil.now("[HH:mm]") + " <" + nick + "> " + msg);
            else
                receive(DateUtil.now("[HH:mm]") + " <" + nick + "> " + target + "> " + msg);
        }
    }

    private class DesktopOverlay extends AbstractDesktopOverlay
    {
        final ArrayList<TextLayout> layouts;
        final Color bgColor;
        final Color fgColor;
        int position;

        public DesktopOverlay()
        {
            super();

            layouts = new ArrayList<TextLayout>();
            // default text colors
            fgColor = new Color(0f, 0f, 0f, 0.6f);
            bgColor = new Color(0.5f, 0.5f, 0.5f, 0.6f);
        }

        /**
         * Return the next paragraph
         */
        private AttributedCharacterIterator getNextParagraph()
        {
            try
            {
                if (position >= 0)
                {
                    final Element elem = doc.getParagraphElement(position);
                    final int start = elem.getStartOffset();
                    // update position
                    position = start - 1;
                    return new AttributedString(doc.getText(start, elem.getEndOffset() - start)).getIterator();
                }
            }
            catch (BadLocationException e)
            {
                // should not happen
                IcyExceptionHandler.showErrorMessage(e, true);
            }

            return null;
        }

        @Override
        public void Paint(Graphics g, int width, int height)
        {
            final Graphics2D g2 = (Graphics2D) g.create();
            final FontRenderContext frc = g2.getFontRenderContext();

            // start at last character
            position = doc.getLength() - 1;

            float x;
            float y = height;

            // get first paragraph
            AttributedCharacterIterator charIterator = getNextParagraph();

            while ((charIterator != null) && (y > 0))
            {
                final LineBreakMeasurer lbm = new LineBreakMeasurer(charIterator, frc);
                final int end = charIterator.getEndIndex();
                float sy = y;
                layouts.clear();

                while ((lbm.getPosition() < end) && (sy > 0))
                {
                    final TextLayout textLayout = lbm.nextLayout(width - 10);

                    sy -= textLayout.getAscent() + textLayout.getDescent() + textLayout.getLeading();

                    layouts.add(textLayout);
                }

                // get paragraph height
                final float paragraphHeight = y - sy;
                // position to end of paragraph
                y -= paragraphHeight;

                for (TextLayout textLayout : layouts)
                {
                    x = textLayout.isLeftToRight() ? 10 : width - (textLayout.getAdvance() + 10);

                    g2.setColor(bgColor);
                    textLayout.draw(g2, x - 1, y + 1);
                    g2.setColor(fgColor);
                    textLayout.draw(g2, x, y);

                    // write paragraph in usual top/bottom order
                    y += textLayout.getAscent() + textLayout.getDescent() + textLayout.getLeading();
                }

                // set position back to end of paragraph
                y -= paragraphHeight;

                // get next paragraph
                charIterator = getNextParagraph();
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

            // join channel
            client.doJoin("#" + ApplicationPreferences.getIrcChannel());

            connectButton.setEnabled(true);
            refreshGUI();
        }

        @Override
        public void onDisconnected()
        {
            super.onDisconnected();

            connectButton.setEnabled(true);
            refreshGUI();
        }

        @Override
        public void onError(int num, String msg)
        {
            super.onError(num, msg);

            switch (num)
            {
                case 432:
                    // Erroneous Nickname
                    if (!connectButton.isEnabled())
                    {
                        // if we were connecting, we disconnect
                        disconnect();
                        onReceive("Your nickname contains invalid caracters.");
                    }
                    refreshGUI();
                    break;

                case 437:
                    client.doNick(client.getNick() + "_");
                    break;
            }
        }

        @Override
        public void onInvite(String chan, IRCUser u, String nickPass)
        {
            onReceive(u.getNick() + " invites " + nickPass + ".");
        }

        @Override
        public void onJoin(String chan, IRCUser u)
        {
            onReceive(u.getNick() + " joins chat.");

            if (u.getNick().equals(client.getNick()))
            {
                // clear text on channel join
                if (!cleared)
                {
                    synchronized (receiveEditor)
                    {
                        receiveEditor.setText("");
                    }
                    cleared = true;
                    onReceive(client.getNick() + " connected.");
                }
            }

            // refresh user list
            refreshUsers();
        }

        @Override
        public void onKick(String chan, IRCUser u, String nickPass, String msg)
        {
            onReceive(u.getNick() + " kicks " + nickPass + ".");

            // refresh user list
            refreshUsers();
        }

        @Override
        public void onLeave(String chan, IRCUser u, String msg)
        {
            onReceive(u.getNick() + " leaves chat.");

            // refresh user list
            refreshUsers();
        }

        @Override
        public void onMode(IRCUser u, String nickPass, String mode)
        {
            onReceive(u.getNick() + " sets modes " + mode + " " + nickPass + ".");
        }

        @Override
        public void onMode(String chan, IRCUser u, IRCModeParser mp)
        {
            onReceive(u.getNick() + " sets mode: " + mp.getLine() + ".");
        }

        @Override
        public void onNick(IRCUser u, String nickNew)
        {
            onReceive(u.getNick() + " is now known as " + nickNew + ".");
            // update nickname
            if (u.getNick().equals(getIrcNickname()))
                setIrcNickname(nickNew);
            refreshGUI();
            refreshUsers();
        }

        @Override
        public void onNotice(String target, IRCUser u, String msg)
        {
            onReceive(u.getNick() + " (notice): " + msg);
        }

        @Override
        public void onPrivmsg(String chan, IRCUser u, String msg)
        {
            client.writeMsg(u.getNick(), chan, msg);
        }

        @Override
        public void onQuit(IRCUser u, String msg)
        {
            if (StringUtil.isEmpty(msg))
                onReceive(u.getNick() + " quits chat.");
            else
                onReceive(u.getNick() + " quits chat (" + msg + ").");
        }

        @Override
        public void onTopic(String chan, IRCUser u, String topic)
        {
            onReceive(u.getNick() + " changes topic into: " + topic);
        }

        @Override
        public void unknown(String a, String b, String c, String d)
        {
            onReceive(d);
        }

        @Override
        public void onReply(int num, String value, String msg)
        {
            switch (num)
            {
                case 353:
                    // add users to tmp list
                    tmpUserList.addAll(Arrays.asList(msg.split(" ")));
                    break;

                case 366:
                    // end of user list
                    userList.setListData(tmpUserList.toArray());
                    tmpUserList.clear();
                    break;

                default:
                    onReceive(msg);
            }
        }

        @Override
        public void onReceive(String text)
        {
            addText(text);
        }
    }

    /**
     * 
     */
    private static final long serialVersionUID = -3449422097073285247L;

    private static final int MAX_SIZE = 4 * 1024 * 1024; // 4 MB

    /**
     * pref id
     */
    private static final String CHAT_ID = "chat";

    /**
     * id
     */
    private static final String ID_IRC_NICKNAME = "ircNickname";
    private static final String ID_IRC_USERNAME = "ircUsername";
    private static final String ID_IRC_REALNAME = "ircRealname";
    private static final String ID_IRC_PASSWORD = "ircPassword";
    private static final String ID_AUTO_CONNECT = "autoConnect";
    private static final String ID_SHOW_USERS_PANEL = "showUsersPanel";
    private static final String ID_DESKTOP_OVERLAY = "desktopOverlay";
    private static final String ID_USERS_PANEL_WIDTH = "usersPanelWidth";

    /**
     * GUI
     */
    JSplitPane mainSplitPane;
    JScrollPane receiveScrollPane;
    JTextPane receiveEditor;
    JScrollPane usersScrollPane;
    JList userList;
    JTextField sendEditor;
    JTextField txtNickName;
    IcyToggleButton connectButton;
    IcyToggleButton showUserPaneButton;
    IcyToggleButton desktopOverlayButton;
    JCheckBox autoConnectCheckBox;

    /**
     * Desktop GUI
     */
    final JPanel desktopPanel;
    final JTextField sendEditorDesktop;
    final IcyButton hideDesktopChatButton;
    final DesktopOverlay desktopOverlay;

    /**
     * preferences
     */
    private final XMLPreferences pref;

    /**
     * IRC client
     */
    CustomIRCClient client;

    /**
     * internals
     */
    final StyledDocument doc;
    final ArrayList<String> tmpUserList;
    final SimpleAttributeSet attributes;
    final CustomIRCClientListener ircListener;
    boolean cleared;

    public ChatPanel()
    {
        super("Chat room", "chatPanel");
        // super();

        pref = GeneralPreferences.getPreferences().node(CHAT_ID);
        tmpUserList = new ArrayList<String>();
        cleared = false;
        attributes = new SimpleAttributeSet();

        // build GUI
        initialize();

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

        hideDesktopChatButton = new IcyButton(ResourceUtil.ICON_SQUARE_DOWN, 20);
        hideDesktopChatButton.setFlat(true);
        ComponentUtil.setFixedWidth(hideDesktopChatButton, 20);
        hideDesktopChatButton.setToolTipText("Disable desktop chat overlay");
        hideDesktopChatButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                setDesktopOverlay(false);
                refreshDesktopOverlayState();
                new ToolTipFrame("<b>Desktop chat overlay</b><br><br>"
                        + "You just disabled the desktop chat overlay<br>"
                        + "but you can always access and enable it<br>" + "from the inspector \"Chat\" tab.", CHAT_ID);
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

        doc = receiveEditor.getStyledDocument();
        ircListener = new CustomIRCClientListener();

        refreshGUI();
        refreshDesktopOverlayState();

        if (getAutoConnect())
            connect();
    }

    public boolean isConnected()
    {
        return (client != null) && client.isConnected();
    }

    /**
     * Do IRC connection
     */
    void connect()
    {
        // connecting
        connectButton.setEnabled(false);
        connectButton.setToolTipText("connecting...");

        final String nickName = txtNickName.getText();
        // apply nickname
        setIrcNickname(nickName);
        String userName = getIrcUsername();
        String realName = getIrcRealname();

        if (StringUtil.isEmpty(userName))
            userName = nickName;
        if (StringUtil.isEmpty(realName))
            realName = nickName;

        // we need to recreate the client
        client = new CustomIRCClient(ApplicationPreferences.getIrcServer(), ApplicationPreferences.getIrcPort(),
                getIrcPassword(), nickName, userName, realName);
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
                    IcyExceptionHandler.showErrorMessage(e, false);
                    System.err.println("Cannot connect to chat.");

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
        }).start();
    }

    void disconnect()
    {
        if (isConnected())
        {
            // closing connection
            connectButton.setEnabled(false);
            connectButton.setToolTipText("closing connexion...");
            client.doQuit();
            cleared = false;
        }
    }

    protected void sendEditContent(JTextField txtField)
    {
        final String text = txtField.getText();

        if (isConnected() && !StringUtil.isEmpty(text))
            client.sendText(text);

        txtField.setText("");
    }

    public String getRandomNickname()
    {
        return "guest" + Random.nextInt(10000);
    }

    public String getIrcNickname()
    {
        return pref.get(ID_IRC_NICKNAME, getRandomNickname());
    }

    public String getIrcUsername()
    {
        return pref.get(ID_IRC_USERNAME, "");
    }

    public String getIrcRealname()
    {
        return pref.get(ID_IRC_REALNAME, "");
    }

    public String getIrcPassword()
    {
        return pref.get(ID_IRC_PASSWORD, "");
    }

    public boolean getAutoConnect()
    {
        return pref.getBoolean(ID_AUTO_CONNECT, true);
    }

    public boolean getShowUsersPanel()
    {
        return pref.getBoolean(ID_SHOW_USERS_PANEL, false);
    }

    public boolean getDesktopOverlay()
    {
        return pref.getBoolean(ID_DESKTOP_OVERLAY, true);
    }

    public int getUsersPanelWidth()
    {
        return pref.getInt(ID_USERS_PANEL_WIDTH, 120);
    }

    public void setIrcNickname(String value)
    {
        pref.put(ID_IRC_NICKNAME, value);
    }

    public void setIrcUsername(String value)
    {
        pref.put(ID_IRC_USERNAME, value);
    }

    public void setIrcRealname(String value)
    {
        pref.put(ID_IRC_REALNAME, value);
    }

    public void setIrcPassword(String value)
    {
        pref.put(ID_IRC_PASSWORD, value);
    }

    public void setAutoConnect(boolean value)
    {
        pref.putBoolean(ID_AUTO_CONNECT, value);
    }

    public void setShowUsersPanel(boolean value)
    {
        pref.putBoolean(ID_SHOW_USERS_PANEL, value);
    }

    public void setDesktopOverlay(boolean value)
    {
        pref.putBoolean(ID_DESKTOP_OVERLAY, value);
    }

    public void setUsersPanelWidth(int value)
    {
        pref.putInt(ID_USERS_PANEL_WIDTH, value);
    }

    /**
     * Build the panel
     */
    private void initialize()
    {
        setLayout(new BorderLayout(0, 0));

        JPanel panel = new JPanel();
        add(panel, BorderLayout.CENTER);
        panel.setLayout(new BorderLayout(0, 0));

        mainSplitPane = new JSplitPane();
        panel.add(mainSplitPane);
        mainSplitPane.setDividerSize(6);
        mainSplitPane.setContinuousLayout(true);
        mainSplitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);

        receiveScrollPane = new JScrollPane();
        receiveScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        receiveScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        mainSplitPane.setLeftComponent(receiveScrollPane);

        receiveEditor = new JTextPane();
        receiveEditor.setEditable(false);
        receiveScrollPane.setViewportView(receiveEditor);

        usersScrollPane = new JScrollPane();
        usersScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        usersScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        usersScrollPane.addComponentListener(new ComponentAdapter()
        {
            @Override
            public void componentResized(ComponentEvent e)
            {
                setUsersPanelWidth(getUsersScrollPaneWidth());
            }
        });
        mainSplitPane.setRightComponent(usersScrollPane);

        JLabel lblUtilisateur = new JLabel("Users");
        lblUtilisateur.setFont(new Font("Tahoma", Font.BOLD, 11));
        lblUtilisateur.setBorder(new LineBorder(Color.GRAY, 1, true));
        lblUtilisateur.setHorizontalAlignment(SwingConstants.CENTER);
        usersScrollPane.setColumnHeaderView(lblUtilisateur);

        userList = new JList();
        userList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        usersScrollPane.setViewportView(userList);

        JPanel panelBottom = new JPanel();
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
        panelBottom.add(sendEditor, BorderLayout.CENTER);

        JPanel topPanel = new JPanel();
        topPanel.setBorder(new EmptyBorder(0, 0, 2, 0));
        add(topPanel, BorderLayout.NORTH);

        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.LINE_AXIS));

        autoConnectCheckBox = new JCheckBox("Auto connect");
        autoConnectCheckBox.setFocusPainted(false);
        autoConnectCheckBox.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                setAutoConnect(autoConnectCheckBox.isSelected());
            }
        });
        autoConnectCheckBox.setSelected(getAutoConnect());
        autoConnectCheckBox.setToolTipText("Auto connect at start up");
        topPanel.add(autoConnectCheckBox);

        Component horizontalStrut = Box.createHorizontalStrut(10);
        topPanel.add(horizontalStrut);

        txtNickName = new JTextField();
        txtNickName.setMaximumSize(new Dimension(2147483647, 24));
        txtNickName.setText(getIrcNickname());
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
                    setIrcNickname(nickName);
            }
        });
        btnSetNickName.setToolTipText("Set nick name");
        topPanel.add(btnSetNickName);

        Component horizontalGlue = Box.createHorizontalGlue();
        topPanel.add(horizontalGlue);

        Component horizontalStrut_2 = Box.createHorizontalStrut(10);
        topPanel.add(horizontalStrut_2);

        connectButton = new IcyToggleButton(ResourceUtil.ICON_ON_OFF, 20);
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
                    connect();
                else
                    disconnect();
            }
        });
        topPanel.add(connectButton);

        Component horizontalStrut_1 = Box.createHorizontalStrut(2);
        topPanel.add(horizontalStrut_1);

        showUserPaneButton = new IcyToggleButton("user", 20);
        showUserPaneButton.setFocusPainted(false);
        showUserPaneButton.setMaximumSize(new Dimension(32, 32));
        showUserPaneButton.setSelected(getShowUsersPanel());
        showUserPaneButton.setMinimumSize(new Dimension(24, 24));
        showUserPaneButton.setPreferredSize(new Dimension(24, 24));
        showUserPaneButton.setToolTipText("Show connected users");
        showUserPaneButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                final boolean visible = showUserPaneButton.isSelected();

                setShowUsersPanel(visible);
                refreshGUI();
            }
        });
        topPanel.add(showUserPaneButton);

        Component horizontalStrut_5 = Box.createHorizontalStrut(2);
        topPanel.add(horizontalStrut_5);

        desktopOverlayButton = new IcyToggleButton("spechbubble_sq_line", 20);
        desktopOverlayButton.setFocusPainted(false);
        desktopOverlayButton.setMaximumSize(new Dimension(32, 32));
        desktopOverlayButton.setSelected(getDesktopOverlay());
        desktopOverlayButton.setMinimumSize(new Dimension(24, 24));
        desktopOverlayButton.setPreferredSize(new Dimension(24, 24));
        desktopOverlayButton.setToolTipText("Enabled desktop overlay");
        desktopOverlayButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                final boolean visible = desktopOverlayButton.isSelected();

                setDesktopOverlay(visible);
                refreshDesktopOverlayState();
            }
        });
        topPanel.add(desktopOverlayButton);
    }

    int getUsersScrollPaneWidth()
    {
        int result = usersScrollPane.getSize().width;
        if (result == 0)
            return usersScrollPane.getPreferredSize().width;
        return result;
    }

    int getCurrentWidth()
    {
        int result = getSize().width;
        if (result == 0)
            return getPreferredSize().width;
        return result;
    }

    void addText(final String text)
    {
        ThreadUtil.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    synchronized (receiveEditor)
                    {
                        doc.insertString(doc.getLength(), text + "\n", attributes);

                        // limit to maximum size
                        if (doc.getLength() > MAX_SIZE)
                            doc.remove(0, doc.getLength() - MAX_SIZE);

                        receiveEditor.setCaretPosition(doc.getLength());
                    }

                    refreshDesktopOverlay();
                }
                catch (BadLocationException e)
                {
                    e.printStackTrace();
                }
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
                if (getShowUsersPanel())
                {
                    usersScrollPane.setVisible(true);
                    mainSplitPane.setDividerSize(6);
                    mainSplitPane.setDividerLocation(getCurrentWidth()
                            - (getUsersPanelWidth() + mainSplitPane.getDividerSize()));
                }
                else
                {
                    usersScrollPane.setVisible(false);
                    mainSplitPane.setDividerSize(0);
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
            client.doNames("#" + ApplicationPreferences.getIrcChannel());
        else
            userList.setListData(new String[0]);
    }

    /**
     * Refresh desktop overlay state
     */
    void refreshDesktopOverlayState()
    {
        final MainFrame mainFrame = Icy.getMainInterface().getMainFrame();
        final IcyDesktopPane desktopPane = Icy.getMainInterface().getDesktopPane();

        if ((mainFrame != null) && (desktopPane != null))
        {
            final JPanel centerPanel = mainFrame.getCenterPanel();

            // desktop overlay enable ?
            if (getDesktopOverlay())
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

        desktopOverlayButton.setSelected(getDesktopOverlay());
    }

    /**
     * Refresh desktop overlay
     */
    void refreshDesktopOverlay()
    {
        final IcyDesktopPane desktopPane = Icy.getMainInterface().getDesktopPane();

        // refresh desktop overlay
        if ((desktopPane != null) && getDesktopOverlay())
            desktopPane.repaint();
    }
}
