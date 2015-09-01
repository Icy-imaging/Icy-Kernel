package icy.gui.component;

import icy.gui.component.editor.SpecialValueSpinnerEditor;
import icy.gui.component.model.SpecialValueSpinnerModel;

import javax.swing.JComponent;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;

/**
 * JSpinner component using a special value for a specific state.
 * 
 * @author Stephane
 */
public class SpecialValueSpinner extends JSpinner
{
    /**
     * 
     */
    private static final long serialVersionUID = 1858500300780069742L;

    /**
     * Create a new IcySpinner
     */
    public SpecialValueSpinner()
    {
        this(new SpecialValueSpinnerModel());
    }

    /**
     * @param model
     */
    public SpecialValueSpinner(SpecialValueSpinnerModel model)
    {
        super(model);
    }

    @Override
    protected JComponent createEditor(SpinnerModel model)
    {
        if (model instanceof SpecialValueSpinnerModel)
            return new SpecialValueSpinnerEditor(this);

        return super.createEditor(model);
    }

}
