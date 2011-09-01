/*
 * Copyright 2010, 2011 Institut Pasteur.
 * 
 * This file is part of ICY.
 * 
 * ICY is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * ICY is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with ICY. If not, see <http://www.gnu.org/licenses/>.
 */
package icy.gui.viewer;

import icy.canvas.IcyCanvas;
import icy.gui.component.ColorComponent;
import icy.gui.component.ComponentUtil;
import icy.gui.util.GuiUtil;
import icy.image.IcyBufferedImage;
import icy.image.lut.LUT;
import icy.math.MathUtil;
import icy.sequence.Sequence;
import icy.type.collection.array.ArrayUtil;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class MouseImageInfosPanel extends JPanel
{
    /**
     * 
     */
    private static final long serialVersionUID = -5994107451349072824L;

    private static final int SIGNIFICANT_DIGIT = 5;

    private final JLabel xLabel;
    private final JLabel yLabel;
    private final JLabel zLabel;
    private final JLabel tLabel;
    private final JLabel cLabel;
    private final JLabel dataLabel;
    private final JLabel xValue;
    private final JLabel yValue;
    private final JLabel zValue;
    private final JLabel tValue;
    private final JLabel cValue;
    private final JLabel dataValue;

    private final ColorComponent colorComp;

    private boolean infoXVisible;
    private boolean infoYVisible;
    private boolean infoZVisible;
    private boolean infoTVisible;
    private boolean infoCVisible;
    private boolean infoDataVisible;
    private boolean infoColorVisible;

    public MouseImageInfosPanel()
    {
        super(true);

        infoXVisible = true;
        infoYVisible = true;
        infoTVisible = true;
        infoZVisible = true;
        infoCVisible = true;
        infoDataVisible = true;
        infoColorVisible = true;

        colorComp = new ColorComponent();
        ComponentUtil.setFixedSize(colorComp, new Dimension(30, 14));
        xValue = new JLabel();
        ComponentUtil.setFixedWidth(xValue, 58);
        yValue = new JLabel();
        ComponentUtil.setFixedWidth(yValue, 58);
        zValue = new JLabel();
        ComponentUtil.setFixedWidth(zValue, 40);
        tValue = new JLabel();
        ComponentUtil.setFixedWidth(tValue, 40);
        cValue = new JLabel();
        ComponentUtil.setFixedWidth(cValue, 40);
        dataValue = new JLabel();
        ComponentUtil.setFixedWidth(dataValue, 200);

        xLabel = GuiUtil.createBoldLabel(" X  ");
        yLabel = GuiUtil.createBoldLabel(" Y  ");
        zLabel = GuiUtil.createBoldLabel(" Z  ");
        tLabel = GuiUtil.createBoldLabel(" T  ");
        cLabel = GuiUtil.createBoldLabel(" C  ");
        dataLabel = GuiUtil.createBoldLabel(" Value  ");

        final JPanel infosPanel = new JPanel();
        infosPanel.setLayout(new BoxLayout(infosPanel, BoxLayout.LINE_AXIS));

        infosPanel.add(colorComp);
        infosPanel.add(Box.createHorizontalStrut(10));
        infosPanel.add(xLabel);
        infosPanel.add(xValue);
        infosPanel.add(yLabel);
        infosPanel.add(yValue);
        infosPanel.add(zLabel);
        infosPanel.add(zValue);
        infosPanel.add(tLabel);
        infosPanel.add(tValue);
        infosPanel.add(cLabel);
        infosPanel.add(cValue);
        infosPanel.add(dataLabel);
        infosPanel.add(dataValue);

        setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        setLayout(new BorderLayout());

        add(infosPanel, BorderLayout.WEST);
        add(new JPanel(), BorderLayout.CENTER);

        validate();

        updateInfos(null);
    }

    /**
     * @return the xInfoVisible
     */
    public boolean isInfoXVisible()
    {
        return infoXVisible;
    }

    /**
     * @param value
     *        the xInfoVisible to set
     */
    public void setInfoXVisible(boolean value)
    {
        if (infoXVisible != value)
        {
            infoXVisible = value;
            xLabel.setVisible(value);
            xValue.setVisible(value);
        }
    }

    /**
     * @return the yInfoVisible
     */
    public boolean isInfoYVisible()
    {
        return infoYVisible;
    }

    /**
     * @param value
     *        the yInfoVisible to set
     */
    public void setInfoYVisible(boolean value)
    {
        if (infoYVisible != value)
        {
            infoYVisible = value;
            yLabel.setVisible(value);
            yValue.setVisible(value);
        }
    }

    /**
     * @return the zInfoVisible
     */
    public boolean isInfoZVisible()
    {
        return infoZVisible;
    }

    /**
     * @param value
     *        the zInfoVisible to set
     */
    public void setInfoZVisible(boolean value)
    {
        if (infoZVisible != value)
        {
            infoZVisible = value;
            zLabel.setVisible(value);
            zValue.setVisible(value);
        }
    }

    /**
     * @return the tInfoVisible
     */
    public boolean isInfoTVisible()
    {
        return infoTVisible;
    }

    /**
     * @param value
     *        the tInfoVisible to set
     */
    public void setInfoTVisible(boolean value)
    {
        if (infoTVisible != value)
        {
            infoTVisible = value;
            tLabel.setVisible(value);
            tValue.setVisible(value);
        }
    }

    /**
     * @return the cInfoVisible
     */
    public boolean isInfoCVisible()
    {
        return infoCVisible;
    }

    /**
     * @param value
     *        the cInfoVisible to set
     */
    public void setInfoCVisible(boolean value)
    {
        if (infoCVisible != value)
        {
            infoCVisible = value;
            cLabel.setVisible(value);
            cValue.setVisible(value);
        }
    }

    /**
     * @return the dataInfoVisible
     */
    public boolean isInfoDataVisible()
    {
        return infoDataVisible;
    }

    /**
     * @param value
     *        the dataInfoVisible to set
     */
    public void setInfoDataVisible(boolean value)
    {
        if (infoDataVisible != value)
        {
            infoDataVisible = value;
            dataLabel.setVisible(value);
            dataValue.setVisible(value);
        }
    }

    /**
     * @return the colorInfoVisible
     */
    public boolean isInfoColorVisible()
    {
        return infoColorVisible;
    }

    /**
     * @param value
     *        the colorInfoVisible to set
     */
    public void setInfoColorVisible(boolean value)
    {
        if (infoColorVisible != value)
        {
            infoColorVisible = value;
            colorComp.setVisible(value);
        }
    }

    public void updateInfos(IcyCanvas canvas)
    {
        if (canvas != null)
        {
            final Sequence seq = canvas.getSequence();
            final LUT lut = canvas.getLut();

            final double x = canvas.getMouseImagePosX();
            final double y = canvas.getMouseImagePosY();
            final double z = canvas.getMouseImagePosZ();
            final double t = canvas.getMouseImagePosT();
            final double c = canvas.getMouseImagePosC();
            final int xi = (int) x;
            final int yi = (int) y;
            final int zi = (int) z;
            final int ti = (int) t;
            final int ci = (int) c;

            final String xs;
            final String ys;
            final String zs;
            final String ts;
            final String cs;

            if (x == xi)
                xs = Integer.toString(xi);
            else
                xs = Double.toString(MathUtil.roundSignificant(x, SIGNIFICANT_DIGIT, true));
            if (y == yi)
                ys = Integer.toString(yi);
            else
                ys = Double.toString(MathUtil.roundSignificant(y, SIGNIFICANT_DIGIT, true));
            if (z == zi)
                zs = Integer.toString(zi);
            else
                zs = Double.toString(MathUtil.roundSignificant(z, SIGNIFICANT_DIGIT, true));
            if (t == ti)
                ts = Integer.toString(ti);
            else
                ts = Double.toString(MathUtil.roundSignificant(t, SIGNIFICANT_DIGIT, true));
            if (c == ci)
                cs = Integer.toString(ci);
            else
                cs = Double.toString(MathUtil.roundSignificant(c, SIGNIFICANT_DIGIT, true));

            xValue.setText(xs);
            yValue.setText(ys);
            zValue.setText(zs);
            tValue.setText(ts);
            cValue.setText(cs);

            xValue.setToolTipText(xValue.getText());
            yValue.setToolTipText(yValue.getText());
            zValue.setToolTipText(zValue.getText());
            tValue.setToolTipText(tValue.getText());
            cValue.setToolTipText(cValue.getText());

            final IcyBufferedImage image = seq.getImage(ti, zi);

            if ((image != null) && (image.isInside(xi, yi)))
            {
                // FIXME : should take C value in account to retrieve single component color
                colorComp.setColor(image.getRGB(xi, yi, lut));

                if (ci == -1)
                {
                    // all components values
                    dataValue.setText(ArrayUtil.array1DToString(image.getDataCopyC(xi, yi), image.isSignedDataType(),
                            false, " : ", 5));
                }
                else
                {
                    // single component value
                    final double v = image.getData(xi, yi, ci);
                    final int vi = (int) v;
                    final String vs;

                    if (v == vi)
                        vs = Integer.toString(vi);
                    else
                        vs = Double.toString(MathUtil.roundSignificant(v, SIGNIFICANT_DIGIT, true));

                    dataValue.setText(vs);
                }

                dataValue.setToolTipText(dataLabel.getText());
            }
            else
            {
                dataValue.setText("-");
                colorComp.setColor(null);

                dataValue.setToolTipText(null);
            }
        }
        else
        {
            xValue.setText("-");
            yValue.setText("-");
            zValue.setText("-");
            tValue.setText("-");
            cValue.setText("-");
            dataValue.setText("-");
            colorComp.setColor(null);

            xValue.setToolTipText(null);
            yValue.setToolTipText(null);
            zValue.setToolTipText(null);
            tValue.setToolTipText(null);
            cValue.setToolTipText(null);
            dataValue.setToolTipText(null);
        }
    }
}
