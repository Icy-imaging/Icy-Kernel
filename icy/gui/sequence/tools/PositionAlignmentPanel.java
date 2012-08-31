package icy.gui.sequence.tools;

import icy.gui.component.button.IcyToggleButton;
import icy.resource.ResourceUtil;
import icy.resource.icon.IcyIcon;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.event.EventListenerList;

public class PositionAlignmentPanel extends JPanel
{
    /**
     * 
     */
    private static final long serialVersionUID = -811970435734479103L;

    public static class PositionBox extends IcyToggleButton
    {
        /**
         * 
         */
        private static final long serialVersionUID = -6952409965950366299L;

        public PositionBox()
        {
            super(new IcyIcon(ResourceUtil.ICON_NULL, 16));

            setSelectedIcon(new IcyIcon(ResourceUtil.ICON_PICTURE, 16));
            setBorder(BorderFactory.createLineBorder(Color.black));
            setFocusPainted(false);
        }
    }

    private PositionBox topLeftBox;
    private PositionBox topBox;
    private PositionBox topRightBox;
    private PositionBox leftBox;
    private PositionBox centerBox;
    private PositionBox rightBox;
    private PositionBox bottomLeftBox;
    private PositionBox bottomBox;
    private PositionBox bottomRightBox;

    private final ButtonGroup positionGroup;

    /**
     * Create the panel.
     */
    public PositionAlignmentPanel()
    {
        super();

        initialize();

        positionGroup = new ButtonGroup();

        positionGroup.add(topLeftBox);
        positionGroup.add(topBox);
        positionGroup.add(topRightBox);
        positionGroup.add(leftBox);
        positionGroup.add(centerBox);
        positionGroup.add(rightBox);
        positionGroup.add(bottomLeftBox);
        positionGroup.add(bottomBox);
        positionGroup.add(bottomRightBox);

        centerBox.setSelected(true);

        final ActionListener listener = new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                // forward event
                fireActionPerformed(e);
            }
        };

        topLeftBox.addActionListener(listener);
        topBox.addActionListener(listener);
        topRightBox.addActionListener(listener);
        leftBox.addActionListener(listener);
        centerBox.addActionListener(listener);
        rightBox.addActionListener(listener);
        bottomLeftBox.addActionListener(listener);
        bottomBox.addActionListener(listener);
        bottomRightBox.addActionListener(listener);
    }

    @Override
    public void setEnabled(boolean enabled)
    {
        super.setEnabled(enabled);

        topLeftBox.setEnabled(enabled);
        topBox.setEnabled(enabled);
        topRightBox.setEnabled(enabled);
        leftBox.setEnabled(enabled);
        centerBox.setEnabled(enabled);
        rightBox.setEnabled(enabled);
        bottomLeftBox.setEnabled(enabled);
        bottomBox.setEnabled(enabled);
        bottomRightBox.setEnabled(enabled);
    }

    /**
     * Returns the selected horizontal alignment.<br>
     * Possible values are <code>SwingConstants.LEFT / CENTER / RIGHT</code>
     **/
    public int getXAlign()
    {
        final ButtonModel model = positionGroup.getSelection();

        if ((model == topLeftBox.getModel()) || (model == leftBox.getModel()) || (model == bottomLeftBox.getModel()))
            return SwingConstants.LEFT;
        if ((model == topBox.getModel()) || (model == centerBox.getModel()) || (model == bottomBox.getModel()))
            return SwingConstants.CENTER;

        return SwingConstants.RIGHT;
    }

    /**
     * Return the selected vertical alignment.<br>
     * Possible values are <code>SwingConstants.TOP / CENTER / BOTTOM</code>
     **/
    public int getYAlign()
    {
        final ButtonModel model = positionGroup.getSelection();

        if ((model == topLeftBox.getModel()) || (model == topBox.getModel()) || (model == topRightBox.getModel()))
            return SwingConstants.TOP;
        if ((model == leftBox.getModel()) || (model == centerBox.getModel()) || (model == rightBox.getModel()))
            return SwingConstants.CENTER;

        return SwingConstants.BOTTOM;
    }

    /**
     * Notifies all listeners that have registered interest for
     * notification on this event type. The event instance
     * is lazily created using the <code>event</code> parameter.
     * 
     * @param event
     *        the <code>ActionEvent</code> object
     * @see EventListenerList
     */
    protected void fireActionPerformed(ActionEvent event)
    {
        for (ActionListener listener : getActionListeners())
            listener.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, event.getActionCommand(),
                    event.getWhen(), event.getModifiers()));
    }

    /**
     * Adds an <code>ActionListener</code> to the panel.
     * 
     * @param l
     *        the <code>ActionListener</code> to be added
     */
    public void addActionListener(ActionListener l)
    {
        listenerList.add(ActionListener.class, l);
    }

    /**
     * Removes an <code>ActionListener</code> from the panel.
     * If the listener is the currently set <code>Action</code> for the button, then the
     * <code>Action</code> is set to <code>null</code>.
     * 
     * @param l
     *        the listener to be removed
     */
    public void removeActionListener(ActionListener l)
    {
        listenerList.remove(ActionListener.class, l);
    }

    /**
     * Returns an array of all the <code>ActionListener</code>s added
     * to this AbstractButton with addActionListener().
     * 
     * @return all of the <code>ActionListener</code>s added or an empty
     *         array if no listeners have been added
     * @since 1.4
     */
    public ActionListener[] getActionListeners()
    {
        return listenerList.getListeners(ActionListener.class);
    }

    private void initialize()
    {
        GridBagLayout gridBagLayout = new GridBagLayout();
        gridBagLayout.columnWidths = new int[] {0, 0, 0, 0};
        gridBagLayout.rowHeights = new int[] {0, 0, 0, 0};
        gridBagLayout.columnWeights = new double[] {0.0, 0.0, 0.0, Double.MIN_VALUE};
        gridBagLayout.rowWeights = new double[] {0.0, 0.0, 0.0, Double.MIN_VALUE};
        setLayout(gridBagLayout);

        topLeftBox = new PositionBox();
        topLeftBox.setToolTipText("Align content to top left");
        GridBagConstraints gbc_topLeftBox = new GridBagConstraints();
        gbc_topLeftBox.insets = new Insets(0, 0, 5, 5);
        gbc_topLeftBox.gridx = 0;
        gbc_topLeftBox.gridy = 0;
        add(topLeftBox, gbc_topLeftBox);

        topBox = new PositionBox();
        topBox.setToolTipText("Align content to top");
        GridBagConstraints gbc_topBox = new GridBagConstraints();
        gbc_topBox.insets = new Insets(0, 0, 5, 5);
        gbc_topBox.gridx = 1;
        gbc_topBox.gridy = 0;
        add(topBox, gbc_topBox);

        topRightBox = new PositionBox();
        topRightBox.setToolTipText("Align content to top right");
        GridBagConstraints gbc_topRightBox = new GridBagConstraints();
        gbc_topRightBox.insets = new Insets(0, 0, 5, 0);
        gbc_topRightBox.gridx = 2;
        gbc_topRightBox.gridy = 0;
        add(topRightBox, gbc_topRightBox);

        leftBox = new PositionBox();
        leftBox.setToolTipText("Align content to left");
        GridBagConstraints gbc_leftBox = new GridBagConstraints();
        gbc_leftBox.insets = new Insets(0, 0, 5, 5);
        gbc_leftBox.gridx = 0;
        gbc_leftBox.gridy = 1;
        add(leftBox, gbc_leftBox);

        centerBox = new PositionBox();
        centerBox.setToolTipText("Align content to center");
        GridBagConstraints gbc_centerBox = new GridBagConstraints();
        gbc_centerBox.insets = new Insets(0, 0, 5, 5);
        gbc_centerBox.gridx = 1;
        gbc_centerBox.gridy = 1;
        add(centerBox, gbc_centerBox);

        rightBox = new PositionBox();
        rightBox.setToolTipText("Align content to right");
        GridBagConstraints gbc_rightBox = new GridBagConstraints();
        gbc_rightBox.insets = new Insets(0, 0, 5, 0);
        gbc_rightBox.gridx = 2;
        gbc_rightBox.gridy = 1;
        add(rightBox, gbc_rightBox);

        bottomLeftBox = new PositionBox();
        bottomLeftBox.setToolTipText("Align content to bottom left");
        GridBagConstraints gbc_bottomLeftBox = new GridBagConstraints();
        gbc_bottomLeftBox.insets = new Insets(0, 0, 0, 5);
        gbc_bottomLeftBox.gridx = 0;
        gbc_bottomLeftBox.gridy = 2;
        add(bottomLeftBox, gbc_bottomLeftBox);

        bottomBox = new PositionBox();
        bottomBox.setToolTipText("Align content to bottom");
        GridBagConstraints gbc_bottomBox = new GridBagConstraints();
        gbc_bottomBox.insets = new Insets(0, 0, 0, 5);
        gbc_bottomBox.gridx = 1;
        gbc_bottomBox.gridy = 2;
        add(bottomBox, gbc_bottomBox);

        bottomRightBox = new PositionBox();
        bottomRightBox.setToolTipText("Align content to bottom right");
        GridBagConstraints gbc_bottomRightBox = new GridBagConstraints();
        gbc_bottomRightBox.gridx = 2;
        gbc_bottomRightBox.gridy = 2;
        add(bottomRightBox, gbc_bottomRightBox);
    }

}
