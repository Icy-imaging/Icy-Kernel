/**
 * 
 */
package icy.gui.component.renderer;

import java.awt.Color;
import java.awt.Component;
import java.awt.Insets;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.ComboBoxUI;
import javax.swing.plaf.ListUI;

import org.pushingpixels.substance.api.ColorSchemeAssociationKind;
import org.pushingpixels.substance.api.ComponentState;
import org.pushingpixels.substance.api.ComponentStateFacet;
import org.pushingpixels.substance.api.SubstanceColorScheme;
import org.pushingpixels.substance.api.SubstanceLookAndFeel;
import org.pushingpixels.substance.api.renderers.SubstanceDefaultComboBoxRenderer;
import org.pushingpixels.substance.internal.animation.StateTransitionTracker;
import org.pushingpixels.substance.internal.animation.StateTransitionTracker.ModelStateInfo;
import org.pushingpixels.substance.internal.animation.StateTransitionTracker.StateContributionInfo;
import org.pushingpixels.substance.internal.ui.SubstanceComboBoxUI;
import org.pushingpixels.substance.internal.ui.SubstanceListUI;
import org.pushingpixels.substance.internal.utils.SubstanceColorSchemeUtilities;
import org.pushingpixels.substance.internal.utils.SubstanceSizeUtils;
import org.pushingpixels.substance.internal.utils.SubstanceStripingUtils;
import org.pushingpixels.substance.internal.utils.SubstanceTextUtilities;
import org.pushingpixels.substance.internal.utils.UpdateOptimizationInfo;

/**
 * CustomComboBox renderer, based on Substance look and feel code.<br>
 * Override the getListCellRendererComponent() or updateItem() methods to do your own rendering.
 * 
 * @author Stephane
 */
public class CustomComboBoxRenderer extends SubstanceDefaultComboBoxRenderer
{
    /**
     * 
     */
    private static final long serialVersionUID = -3392789033779535472L;

    private final JComboBox combo;

    public CustomComboBoxRenderer(JComboBox combo)
    {
        super(combo);

        this.combo = combo;
    }

    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
            boolean cellHasFocus)
    {
        JComponent result = (JComponent) getRendererComponent(list, value, index, isSelected);

        ListUI baseListUI = list.getUI();
        ComboBoxUI baseComboUI = combo.getUI();

        if ((baseListUI instanceof SubstanceListUI) && (baseComboUI instanceof SubstanceComboBoxUI))
        {
            SubstanceListUI listUI = (SubstanceListUI) baseListUI;
            SubstanceComboBoxUI comboUI = (SubstanceComboBoxUI) baseComboUI;

            // special case for the combobox. The selected value is
            // painted using the renderer of the list, and the index
            // is -1.
            if (index == -1)
            {
                StateTransitionTracker stateTransitionTracker = comboUI.getTransitionTracker();
                ModelStateInfo modelStateInfo = stateTransitionTracker.getModelStateInfo();
                ComponentState currState = modelStateInfo.getCurrModelState();
                float comboAlpha = SubstanceColorSchemeUtilities.getAlpha(combo, currState);
                Color fg = SubstanceTextUtilities.getForegroundColor(combo, ((JLabel) result).getText(),
                        modelStateInfo, comboAlpha);
                result.setForeground(fg);
            }
            else
            {
                // use highlight color scheme for selected and rollover
                // elements in the drop down list
                StateTransitionTracker.ModelStateInfo modelStateInfo = listUI.getModelStateInfo(index, result);
                ComponentState currState = listUI.getCellState(index, result);

                if (modelStateInfo == null)
                {
                    SubstanceColorScheme scheme = getColorSchemeForState(list, index, listUI, currState);
                    result.setForeground(new ColorUIResource(scheme.getForegroundColor()));
                }
                else
                {
                    Map<ComponentState, StateContributionInfo> activeStates = modelStateInfo.getStateContributionMap();
                    SubstanceColorScheme colorScheme = getColorSchemeForState(list, index, listUI, currState);

                    if (currState.isDisabled() || (activeStates == null) || (activeStates.size() == 1))
                    {
                        super.setForeground(new ColorUIResource(colorScheme.getForegroundColor()));
                    }
                    else
                    {
                        float aggrRed = 0.0f;
                        float aggrGreen = 0.0f;
                        float aggrBlue = 0.0f;

                        for (Map.Entry<ComponentState, StateTransitionTracker.StateContributionInfo> activeEntry : modelStateInfo
                                .getStateContributionMap().entrySet())
                        {
                            ComponentState activeState = activeEntry.getKey();
                            float activeContribution = activeEntry.getValue().getContribution();
                            if (activeContribution == 0.0f)
                                continue;

                            SubstanceColorScheme scheme = getColorSchemeForState(list, index, listUI, activeState);
                            Color schemeFg = scheme.getForegroundColor();
                            aggrRed += schemeFg.getRed() * activeContribution;
                            aggrGreen += schemeFg.getGreen() * activeContribution;
                            aggrBlue += schemeFg.getBlue() * activeContribution;
                        }

                        result.setForeground(new ColorUIResource(new Color((int) aggrRed, (int) aggrGreen,
                                (int) aggrBlue)));
                    }
                }
            }

            SubstanceStripingUtils.applyStripedBackground(list, index, this);
        }

        result.setEnabled(combo.isEnabled() & isEnabled());

        return result;
    }

    protected Component getRendererComponent(JList list, Object value, int index, boolean isSelected)
    {
        setComponentOrientation(list.getComponentOrientation());

        ListUI listUI = list.getUI();

        if (listUI instanceof SubstanceListUI)
        {
            SubstanceListUI ui = (SubstanceListUI) listUI;

            StateTransitionTracker.ModelStateInfo modelStateInfo = ui.getModelStateInfo(index, this);
            ComponentState currState = ui.getCellState(index, this);

            // special case for drop location
            JList.DropLocation dropLocation = list.getDropLocation();
            boolean isDropLocation = (dropLocation != null && !dropLocation.isInsert() && dropLocation.getIndex() == index);

            if (!isDropLocation && (modelStateInfo != null))
            {
                Map<ComponentState, StateContributionInfo> activeStates = modelStateInfo.getStateContributionMap();
                SubstanceColorScheme colorScheme = getColorSchemeForState(list, ui, currState);

                if (currState.isDisabled() || (activeStates == null) || (activeStates.size() == 1))
                    super.setForeground(new ColorUIResource(colorScheme.getForegroundColor()));
                else
                {
                    float aggrRed = 0;
                    float aggrGreen = 0;
                    float aggrBlue = 0;

                    for (Map.Entry<ComponentState, StateTransitionTracker.StateContributionInfo> activeEntry : modelStateInfo
                            .getStateContributionMap().entrySet())
                    {
                        ComponentState activeState = activeEntry.getKey();
                        SubstanceColorScheme scheme = getColorSchemeForState(list, ui, activeState);
                        Color schemeFg = scheme.getForegroundColor();
                        float contribution = activeEntry.getValue().getContribution();

                        aggrRed += schemeFg.getRed() * contribution;
                        aggrGreen += schemeFg.getGreen() * contribution;
                        aggrBlue += schemeFg.getBlue() * contribution;
                    }

                    super.setForeground(new ColorUIResource(new Color((int) aggrRed, (int) aggrGreen, (int) aggrBlue)));
                }
            }
            else
            {
                SubstanceColorScheme scheme = getColorSchemeForState(list, ui, currState);

                if (isDropLocation)
                {
                    scheme = SubstanceColorSchemeUtilities.getColorScheme(list,
                            ColorSchemeAssociationKind.TEXT_HIGHLIGHT, currState);
                }

                super.setForeground(new ColorUIResource(scheme.getForegroundColor()));
            }
        }
        else
        {
            if (isSelected)
                setForeground(list.getSelectionForeground());
            else
                setForeground(list.getForeground());
        }

        if (SubstanceLookAndFeel.isCurrentLookAndFeel() && (list.getLayoutOrientation() == JList.VERTICAL))
            SubstanceStripingUtils.applyStripedBackground(list, index, this);

        updateItem(list, value);

        Insets ins = SubstanceSizeUtils.getListCellRendererInsets(SubstanceSizeUtils.getComponentFontSize(list));
        setBorder(new EmptyBorder(ins.top, ins.left, ins.bottom, ins.right));
        setOpaque(false);

        return this;
    }

    protected void updateItem(JList list, Object value)
    {
        if (value instanceof Icon)
        {
            setIcon((Icon) value);
            setText("");
        }
        else
        {
            setIcon(null);
            setText((value == null) ? "" : value.toString());
            setToolTipText((value == null) ? "" : value.toString());
        }

        setEnabled(list.isEnabled());
        setFont(list.getFont());
    }

    private SubstanceColorScheme getColorSchemeForState(JList list, SubstanceListUI ui, ComponentState state)
    {
        UpdateOptimizationInfo updateOptimizationInfo = ui.getUpdateOptimizationInfo();

        if (state == ComponentState.ENABLED)
        {
            if (updateOptimizationInfo == null)
                return SubstanceColorSchemeUtilities.getColorScheme(list, state);

            return updateOptimizationInfo.getDefaultScheme();
        }

        if (updateOptimizationInfo == null)
            return SubstanceColorSchemeUtilities.getColorScheme(list, ColorSchemeAssociationKind.HIGHLIGHT, state);

        return updateOptimizationInfo.getHighlightColorScheme(state);
    }

    private SubstanceColorScheme getColorSchemeForState(JList list, int index, SubstanceListUI listUI,
            ComponentState state)
    {
        boolean toUseHighlightKindForCurrState = (index >= 0)
                && (state.isFacetActive(ComponentStateFacet.ROLLOVER) || state
                        .isFacetActive(ComponentStateFacet.SELECTION));

        UpdateOptimizationInfo updateOptimizationInfo = listUI.getUpdateOptimizationInfo();

        if (toUseHighlightKindForCurrState)
        {
            if (updateOptimizationInfo == null)
                return SubstanceColorSchemeUtilities.getColorScheme(list, ColorSchemeAssociationKind.HIGHLIGHT, state);

            return updateOptimizationInfo.getHighlightColorScheme(state);
        }

        if (updateOptimizationInfo == null)
            return SubstanceColorSchemeUtilities.getColorScheme(list, state);

        return updateOptimizationInfo.getDefaultScheme();
    }
}
