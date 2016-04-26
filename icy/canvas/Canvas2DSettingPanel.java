/**
 * 
 */
package icy.canvas;

import icy.gui.component.button.ColorChooserButton;
import icy.gui.component.button.ColorChooserButton.ColorChangeListener;
import icy.gui.component.button.IcyButton;
import icy.gui.util.ComponentUtil;
import icy.resource.ResourceUtil;
import icy.resource.icon.IcyIcon;
import icy.util.EventUtil;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Setting panel for Canvas2D
 * 
 * @author Stephane
 */
public class Canvas2DSettingPanel extends JPanel
{
    /**
     * 
     */
    private static final long serialVersionUID = -6747952036222386920L;

    final Canvas2D canvas2D;

    /**
     * gui
     */
    JComboBox zoomComboBox;
    JComboBox rotationComboBox;

    private IcyButton zoomFitImageButton;
    private IcyButton centerImageButton;
    private IcyButton zoomPlus;
    private IcyButton zoomMinus;
    private IcyButton rotateUnclock;
    private IcyButton rotateClock;
    ColorChooserButton bgColorButton;

    public Canvas2DSettingPanel(Canvas2D cnv)
    {
        super();

        this.canvas2D = cnv;

        initialize();

        // as scale isn't necessary changed (if already 100%)
        zoomComboBox.setSelectedItem(Integer.toString((int) (canvas2D.getScaleX() * 100)));
        bgColorButton.setColor(new Color(canvas2D.preferences.getInt(Canvas2D.ID_BG_COLOR, 0xFFFFFF)));
        bgColorButton.setSelected(canvas2D.preferences.getBoolean(Canvas2D.ID_BG_COLOR_ENABLED, false));

        zoomComboBox.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                if (!canvas2D.modifyingZoom)
                {
                    try
                    {
                        final double scale = Double.parseDouble((String) zoomComboBox.getSelectedItem()) / 100;

                        // set mouse position on view center
                        canvas2D.centerMouseOnView();
                        // set new scale
                        canvas2D.setScale(scale, scale, true, true);
                    }
                    catch (NumberFormatException E)
                    {
                        // ignore change
                    }
                }
            }
        });
        rotationComboBox.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                if (!canvas2D.modifyingRotation)
                {
                    try
                    {
                        final double angle = Double.parseDouble((String) rotationComboBox.getSelectedItem());
                        // we first apply modulo
                        canvas2D.setRotation(canvas2D.getRotation(), false);
                        // then set new angle
                        canvas2D.setRotation((angle * Math.PI) / 180d, true);
                    }
                    catch (NumberFormatException E)
                    {
                        // ignore change
                    }
                }
            }
        });
        zoomPlus.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                final double scale = canvas2D.smoothTransform.getDestValue(Canvas2D.SCALE_X) * 1.25;

                // set mouse position on view center
                canvas2D.centerMouseOnView();
                // apply scale
                canvas2D.setScale(scale, scale, true, true);
            }
        });
        zoomMinus.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                final double scale = canvas2D.smoothTransform.getDestValue(Canvas2D.SCALE_X) * 0.8;

                // set mouse position on view center
                canvas2D.centerMouseOnView();
                // apply scale
                canvas2D.setScale(scale, scale, true, true);
            }
        });
        rotateUnclock.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                canvas2D.setRotation(canvas2D.smoothTransform.getDestValue(Canvas2D.ROT) + (Math.PI / 8), true);
            }
        });
        rotateClock.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                canvas2D.setRotation(canvas2D.smoothTransform.getDestValue(Canvas2D.ROT) - (Math.PI / 8), true);
            }
        });
        zoomFitImageButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                canvas2D.fitCanvasToImage();
            }
        });
        centerImageButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                canvas2D.centerImage();
            }
        });
        bgColorButton.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                if (EventUtil.isRightMouseButton(e))
                {
                    bgColorButton.setSelected(!bgColorButton.isSelected());
                    canvas2D.backgroundColorEnabledChanged();
                }
            };
        });
        bgColorButton.addColorChangeListener(new ColorChangeListener()
        {
            @Override
            public void colorChanged(ColorChooserButton source)
            {
                canvas2D.backgroundColorChanged();
            }
        });
    }

    private void initialize()
    {
        // subPanel.add(GuiUtil.createLineBoxPanel(Box.createHorizontalStrut(4),
        // GuiUtil.createFixedWidthBoldLabel("Zoom", 70), zoomComboBox,
        // GuiUtil.createFixedWidthBoldLabel("%", 20), Box.createHorizontalGlue(), zoomMinus,
        // Box.createHorizontalStrut(4), zoomPlus, Box.createHorizontalStrut(4)));
        // subPanel.add(Box.createVerticalStrut(4));
        // subPanel.add(GuiUtil.createLineBoxPanel(Box.createHorizontalStrut(4),
        // GuiUtil.createFixedWidthBoldLabel("Rotation", 70), rotationComboBox,
        // GuiUtil.createFixedWidthBoldLabel("°", 20), Box.createHorizontalGlue(), rotateUnclock,
        // Box.createHorizontalStrut(4), rotateClock, Box.createHorizontalStrut(4)));

        setLayout(new BorderLayout());

        JPanel panel = new JPanel();
        add(panel, BorderLayout.SOUTH);
        GridBagLayout gbl_panel = new GridBagLayout();
        gbl_panel.columnWidths = new int[] {60, 0, 20, 0, 0, 8, 0, 0};
        gbl_panel.rowHeights = new int[] {0, 0, 0};
        gbl_panel.columnWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0};
        gbl_panel.rowWeights = new double[] {0.0, 0.0, Double.MIN_VALUE};
        panel.setLayout(gbl_panel);

        JLabel label_1 = new JLabel("Zoom");
        label_1.setFont(new Font("Tahoma", Font.BOLD, 11));
        GridBagConstraints gbc_label_1 = new GridBagConstraints();
        gbc_label_1.anchor = GridBagConstraints.EAST;
        gbc_label_1.insets = new Insets(0, 0, 5, 5);
        gbc_label_1.gridx = 0;
        gbc_label_1.gridy = 0;
        panel.add(label_1, gbc_label_1);

        zoomComboBox = new JComboBox(new String[] {"10", "50", "100", "200", "400", "1000"});
        zoomComboBox.setEditable(true);
        zoomComboBox.setToolTipText("Select zoom factor");
        zoomComboBox.setSelectedIndex(2);
        ComponentUtil.setFixedWidth(zoomComboBox, 64);
        GridBagConstraints gbc_zoomComboBox = new GridBagConstraints();
        gbc_zoomComboBox.fill = GridBagConstraints.HORIZONTAL;
        gbc_zoomComboBox.insets = new Insets(0, 0, 5, 1);
        gbc_zoomComboBox.gridx = 1;
        gbc_zoomComboBox.gridy = 0;
        panel.add(zoomComboBox, gbc_zoomComboBox);

        JLabel label_2 = new JLabel("%");
        label_2.setFont(new Font("Tahoma", Font.BOLD, 11));
        GridBagConstraints gbc_label_2 = new GridBagConstraints();
        gbc_label_2.anchor = GridBagConstraints.WEST;
        gbc_label_2.insets = new Insets(0, 0, 5, 5);
        gbc_label_2.gridx = 2;
        gbc_label_2.gridy = 0;
        panel.add(label_2, gbc_label_2);

        zoomMinus = new IcyButton(new IcyIcon(ResourceUtil.ICON_MINUS, Canvas2D.ICON_SIZE));
        zoomMinus.setFlat(true);
        zoomMinus.setToolTipText("Reduce zoom factor");
        GridBagConstraints gbc_zoomMinus_1 = new GridBagConstraints();
        gbc_zoomMinus_1.insets = new Insets(0, 0, 5, 1);
        gbc_zoomMinus_1.gridx = 3;
        gbc_zoomMinus_1.gridy = 0;
        panel.add(zoomMinus, gbc_zoomMinus_1);

        zoomPlus = new IcyButton(new IcyIcon(ResourceUtil.ICON_PLUS, Canvas2D.ICON_SIZE));
        zoomPlus.setFlat(true);
        zoomPlus.setToolTipText("Increase zoom factor");
        GridBagConstraints gbc_zoomPlus_1 = new GridBagConstraints();
        gbc_zoomPlus_1.insets = new Insets(0, 0, 5, 5);
        gbc_zoomPlus_1.gridx = 4;
        gbc_zoomPlus_1.gridy = 0;
        panel.add(zoomPlus, gbc_zoomPlus_1);

        JLabel label_3 = new JLabel("Rotation");
        label_3.setFont(new Font("Tahoma", Font.BOLD, 11));
        GridBagConstraints gbc_label_3 = new GridBagConstraints();
        gbc_label_3.anchor = GridBagConstraints.EAST;
        gbc_label_3.insets = new Insets(0, 0, 0, 5);
        gbc_label_3.gridx = 0;
        gbc_label_3.gridy = 1;
        panel.add(label_3, gbc_label_3);

        rotationComboBox = new JComboBox(new String[] {"0", "45", "90", "135", "180", "225", "270", "315"});
        ComponentUtil.setFixedWidth(rotationComboBox, 64);
        rotationComboBox.setEditable(true);
        rotationComboBox.setToolTipText("Select rotation angle");
        rotationComboBox.setSelectedIndex(0);
        GridBagConstraints gbc_rotationComboBox = new GridBagConstraints();
        gbc_rotationComboBox.fill = GridBagConstraints.HORIZONTAL;
        gbc_rotationComboBox.insets = new Insets(0, 0, 0, 1);
        gbc_rotationComboBox.gridx = 1;
        gbc_rotationComboBox.gridy = 1;
        panel.add(rotationComboBox, gbc_rotationComboBox);

        JLabel label_4 = new JLabel("°");
        label_4.setFont(new Font("Tahoma", Font.BOLD, 11));
        GridBagConstraints gbc_label_4 = new GridBagConstraints();
        gbc_label_4.anchor = GridBagConstraints.WEST;
        gbc_label_4.insets = new Insets(0, 0, 0, 5);
        gbc_label_4.gridx = 2;
        gbc_label_4.gridy = 1;
        panel.add(label_4, gbc_label_4);

        rotateUnclock = new IcyButton(new IcyIcon(ResourceUtil.ICON_ROTATE_UNCLOCK, Canvas2D.ICON_SIZE));
        rotateUnclock.setFlat(true);
        rotateUnclock.setToolTipText("Rotate counter clockwise");
        GridBagConstraints gbc_rotateUnclock_1 = new GridBagConstraints();
        gbc_rotateUnclock_1.insets = new Insets(0, 0, 0, 1);
        gbc_rotateUnclock_1.gridx = 3;
        gbc_rotateUnclock_1.gridy = 1;
        panel.add(rotateUnclock, gbc_rotateUnclock_1);

        rotateClock = new IcyButton(new IcyIcon(ResourceUtil.ICON_ROTATE_CLOCK, Canvas2D.ICON_SIZE));
        rotateClock.setFlat(true);
        rotateClock.setToolTipText("Rotate clockwise");
        GridBagConstraints gbc_rotateClock_1 = new GridBagConstraints();
        gbc_rotateClock_1.insets = new Insets(0, 0, 0, 5);
        gbc_rotateClock_1.gridx = 4;
        gbc_rotateClock_1.gridy = 1;
        panel.add(rotateClock, gbc_rotateClock_1);

        zoomFitImageButton = new IcyButton(new IcyIcon(Canvas2D.ICON_FIT_IMAGE));
        zoomFitImageButton.setFlat(true);
        zoomFitImageButton.setToolTipText("Fit window to image size");
        GridBagConstraints gbc_zoomFitImage = new GridBagConstraints();
        gbc_zoomFitImage.insets = new Insets(0, 0, 0, 5);
        gbc_zoomFitImage.gridx = 6;
        gbc_zoomFitImage.gridy = 1;
        panel.add(zoomFitImageButton, gbc_zoomFitImage);

        centerImageButton = new IcyButton(new IcyIcon(Canvas2D.ICON_CENTER_IMAGE));
        centerImageButton.setFlat(true);
        centerImageButton.setToolTipText("Center image in window");
        GridBagConstraints gbc_centerImageButton = new GridBagConstraints();
        gbc_centerImageButton.gridx = 7;
        gbc_centerImageButton.gridy = 1;
        panel.add(centerImageButton, gbc_centerImageButton);

        bgColorButton = new ColorChooserButton();
        bgColorButton
                .setToolTipText("Left click to change background color, right click to enable/disable background color");
        GridBagConstraints gbc_bgColorButton = new GridBagConstraints();
        gbc_bgColorButton.gridwidth = 2;
        gbc_bgColorButton.fill = GridBagConstraints.HORIZONTAL;
        gbc_bgColorButton.insets = new Insets(0, 0, 5, 0);
        gbc_bgColorButton.gridx = 6;
        gbc_bgColorButton.gridy = 0;
        panel.add(bgColorButton, gbc_bgColorButton);

    }

    public void updateZoomState(String zoom)
    {
        // try to select current zoom level
        zoomComboBox.setSelectedItem(zoom);
    }

    public void updateRotationState(String rotInfo)
    {
        // try to select current rotation angle
        rotationComboBox.setSelectedItem(rotInfo);
    }

    public boolean isBackgroundColorEnabled()
    {
        return bgColorButton.isSelected();
    }

    public void setBackgroundColorEnabled(boolean value)
    {
        bgColorButton.setSelected(value);
    }

    public Color getBackgroundColor()
    {
        return bgColorButton.getColor();
    }

    public void setBackgroundColor(Color color)
    {
        bgColorButton.setColor(color);
    }
}
