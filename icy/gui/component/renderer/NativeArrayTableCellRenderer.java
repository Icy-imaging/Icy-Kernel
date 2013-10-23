/**
 * 
 */
package icy.gui.component.renderer;

import icy.math.MathUtil;
import icy.type.collection.array.Array1DUtil;
import icy.type.collection.array.ArrayUtil;

import javax.swing.SwingConstants;

import org.pushingpixels.substance.api.renderers.SubstanceDefaultTableCellRenderer;

/**
 * @author Stephane
 */
public class NativeArrayTableCellRenderer extends SubstanceDefaultTableCellRenderer
{
    /**
     * 
     */
    private static final long serialVersionUID = 7536618123117211456L;

    final boolean signed;

    public NativeArrayTableCellRenderer(boolean signed)
    {
        super();

        this.signed = signed;

        setHorizontalAlignment(SwingConstants.TRAILING);
    }

    public NativeArrayTableCellRenderer()
    {
        this(true);
    }

    @Override
    protected void setValue(Object value)
    {
        if ((value != null) && (ArrayUtil.getDim(value) == 1))
        {
            final int len = ArrayUtil.getLength(value);

            String s;

            if (len == 0)
                s = "";
            else if (len == 1)
                s = Double.toString(MathUtil.roundSignificant(Array1DUtil.getValue(value, 0, signed), 5));
            else
            {
                s = "[" + Double.toString(MathUtil.roundSignificant(Array1DUtil.getValue(value, 0, signed), 5));
                for (int i = 1; i < len; i++)
                    s += " " + MathUtil.roundSignificant(Array1DUtil.getValue(value, i, signed), 5);
                s += "]";
            }

            setText(s);
        }
        else
            super.setValue(value);
    }
}
