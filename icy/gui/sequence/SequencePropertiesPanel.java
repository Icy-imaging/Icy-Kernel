package icy.gui.sequence;

import icy.gui.component.ComponentUtil;
import icy.math.UnitUtil;
import icy.math.UnitUtil.UnitPrefix;
import icy.sequence.Sequence;
import icy.util.StringUtil;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;

public class SequencePropertiesPanel extends JPanel {
	/**
     * 
     */
	private static final long serialVersionUID = -1568878218022361239L;

	private JTextField nameField;
	private JTextField tfPxSizeX;
	private JTextField tfPxSizeY;
	private JTextField tfPxSizeZ;
	private JComboBox cbPxSizeX;
	private JComboBox cbPxSizeY;
	private JComboBox cbPxSizeZ;
	private JTextField tfTimeInterval;
	private JPanel panelChannels;
	private JTextField[] tfsChannels;
	private JLabel lblX;
	private JLabel lblY;
	private JLabel lblZ;
	private JLabel lblT;
	private JPanel panelPxSizeX;
	private JPanel panelPxSizeY;
	private JPanel panelPxSizeZ;
	private JPanel panelPxSizeT;
	private JCheckBox checkLinked;
	private JLabel lblMs;
	private JPanel panel_1;
	private Component horizontalGlue;
	private JPanel panel_2;
	private JPanel panel_3;
	private JPanel panel_4;
	private JPanel panel_5;
	private JPanel panel_6;
	private JPanel panel_7;
	private JPanel panel_8;
	private Component horizontalGlue_1;

	/**
	 * Create the panel.
	 */
	public SequencePropertiesPanel() {
		super();

		initialize();
	}

	void initialize() {
		setLayout(new BorderLayout(0, 0));

		JPanel panelMain = new JPanel();
		panelMain.setBorder(new EmptyBorder(4, 4, 0, 4));
		add(panelMain, BorderLayout.NORTH);
		panelMain.setLayout(new BoxLayout(panelMain, BoxLayout.Y_AXIS));

		JPanel panelName = new JPanel();
		panelName.setBorder(new TitledBorder(null, "Name", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panelMain.add(panelName);
		panelName.setLayout(new BoxLayout(panelName, BoxLayout.LINE_AXIS));

		nameField = new JTextField();
		nameField.setPreferredSize(new Dimension(200, 20));
		nameField.setMinimumSize(new Dimension(80, 20));
		panelName.add(nameField);

		GridLayout gl_panel_2 = new GridLayout();
		gl_panel_2.setColumns(1);
		gl_panel_2.setRows(4);
		JPanel panelPxSizeConfig = new JPanel(gl_panel_2);
		panelPxSizeConfig.setBorder(new TitledBorder(null, "Pixel Size Config", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panelMain.add(panelPxSizeConfig);

		UnitPrefix[] upValues = UnitPrefix.values();
		String[] cbModel = new String[upValues.length];
		for (int i = 0; i < upValues.length; ++i) {
			cbModel[i] = upValues[i].toString() +"m";
		}

		panelPxSizeX = new JPanel(new GridLayout());
		panelPxSizeConfig.add(panelPxSizeX);
		
		panel_1 = new JPanel();
		panelPxSizeX.add(panel_1);
		panel_1.setLayout(new BorderLayout(0, 0));

		lblX = new JLabel("X: ");
		panel_1.add(lblX, BorderLayout.WEST);

		tfPxSizeX = new JTextField();
		tfPxSizeX.addCaretListener(new CaretListener() {
			
			@Override
			public void caretUpdate(CaretEvent arg0) {
				if (checkLinked.isSelected()) {
					tfPxSizeY.setText(tfPxSizeX.getText());
				}
			}
		});
		tfPxSizeX.setToolTipText("X pixel size.");
		panel_1.add(tfPxSizeX);
		
		panel_2 = new JPanel(new GridLayout());
		panelPxSizeX.add(panel_2);
		cbPxSizeX = new JComboBox(cbModel);
		panel_2.add(cbPxSizeX);
		
		horizontalGlue = Box.createHorizontalGlue();
		panel_2.add(horizontalGlue);

		panelPxSizeY = new JPanel(new GridLayout());
		panelPxSizeConfig.add(panelPxSizeY);
		
		panel_3 = new JPanel();
		panelPxSizeY.add(panel_3);
		panel_3.setLayout(new BorderLayout(0, 0));

		lblY = new JLabel("Y: ");
		panel_3.add(lblY, BorderLayout.WEST);

		tfPxSizeY = new JTextField();
		panel_3.add(tfPxSizeY);
		tfPxSizeY.setPreferredSize(new Dimension(60, 20));
		tfPxSizeY.setToolTipText("Y pixel size.");
		
		panel_4 = new JPanel();
		panelPxSizeY.add(panel_4);
		panel_4.setLayout(new GridLayout(0, 2, 0, 0));
		cbPxSizeY = new JComboBox(cbModel);
		panel_4.add(cbPxSizeY);

		checkLinked = new JCheckBox("linked");
		panel_4.add(checkLinked);
		checkLinked.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (checkLinked.isSelected()) {
					tfPxSizeY.setEnabled(false);
					tfPxSizeY.setText(tfPxSizeX.getText());
					cbPxSizeY.setEnabled(false);
				} else {
					tfPxSizeY.setEnabled(true);
					cbPxSizeY.setEnabled(true);
				}
			}
		});

		panelPxSizeZ = new JPanel(new GridLayout());
		panelPxSizeConfig.add(panelPxSizeZ);
		
		panel_5 = new JPanel();
		panelPxSizeZ.add(panel_5);
		panel_5.setLayout(new BorderLayout(0, 0));

		lblZ = new JLabel("Z: ");
		panel_5.add(lblZ, BorderLayout.WEST);

		tfPxSizeZ = new JTextField();
		panel_5.add(tfPxSizeZ);
		tfPxSizeZ.setPreferredSize(new Dimension(40, 20));
		tfPxSizeZ.setMinimumSize(new Dimension(40, 20));
		tfPxSizeZ.setToolTipText("Z pixel size.");
		panel_6 = new JPanel();
		panelPxSizeZ.add(panel_6);
		panel_6.setLayout(new GridLayout(0, 2, 0, 0));
		cbPxSizeZ = new JComboBox(cbModel);
		panel_6.add(cbPxSizeZ);
		
		horizontalGlue_1 = Box.createHorizontalGlue();
		panel_6.add(horizontalGlue_1);

		panelPxSizeT = new JPanel(new GridLayout());
		panelPxSizeConfig.add(panelPxSizeT);
		
		panel_7 = new JPanel();
		panelPxSizeT.add(panel_7);
		panel_7.setLayout(new BorderLayout(0, 0));

		lblT = new JLabel("T: ");
		panel_7.add(lblT, BorderLayout.WEST);

		tfTimeInterval = new JTextField();
		panel_7.add(tfTimeInterval);
		tfTimeInterval.setPreferredSize(new Dimension(40, 20));
		tfTimeInterval.setMinimumSize(new Dimension(40, 20));
		tfTimeInterval.setToolTipText("T time resolution (in ms).");
		
		panel_8 = new JPanel();
		panelPxSizeT.add(panel_8);
		panel_8.setLayout(new GridLayout(0, 2, 0, 0));
		
		lblMs = new JLabel("ms");
		panel_8.add(lblMs);

		panelChannels = new JPanel();
		panelChannels.setBorder(new TitledBorder(null, "Channels", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panelMain.add(panelChannels);
		panelChannels.setLayout(new BoxLayout(panelChannels, BoxLayout.Y_AXIS));
	}

	public void setSequence(Sequence sequence) {
		nameField.setText(sequence.getName());

		final double pxSizeX = sequence.getPixelSizeX();
		final double pxSizeY = sequence.getPixelSizeY();
		final double pxSizeZ = sequence.getPixelSizeZ();

		final UnitPrefix pxSizeXUnit = UnitUtil.getBestUnit(pxSizeX, UnitPrefix.MILLI);
		final UnitPrefix pxSizeYUnit = UnitUtil.getBestUnit(pxSizeX, UnitPrefix.MILLI);
		final UnitPrefix pxSizeZUnit = UnitUtil.getBestUnit(pxSizeX, UnitPrefix.MILLI);
		cbPxSizeX.setSelectedItem(pxSizeXUnit.toString() + "m");
		cbPxSizeY.setSelectedItem(pxSizeYUnit.toString() + "m");
		cbPxSizeZ.setSelectedItem(pxSizeZUnit.toString() + "m");

		tfPxSizeX.setText(StringUtil.toString(UnitUtil.getValueInUnit(pxSizeX, UnitPrefix.MILLI, pxSizeXUnit)));
		tfPxSizeY.setText(StringUtil.toString(UnitUtil.getValueInUnit(pxSizeY, UnitPrefix.MILLI, pxSizeYUnit)));
		tfPxSizeZ.setText(StringUtil.toString(UnitUtil.getValueInUnit(pxSizeZ, UnitPrefix.MILLI, pxSizeZUnit)));
		
		if (tfPxSizeX.getText().equals(tfPxSizeY.getText()) && cbPxSizeX.getSelectedIndex() == cbPxSizeY.getSelectedIndex()) {
			checkLinked.doClick();
		}

		tfTimeInterval.setText(StringUtil.toString(sequence.getTimeInterval()));

		final int sizeC = sequence.getSizeC();

		panelChannels.removeAll();
		tfsChannels = new JTextField[sizeC];

		for (int c = 0; c < sizeC; c++) {
			final JPanel panel = new JPanel();
			panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));

			final JLabel label = new JLabel("Channel " + c + " name");
			label.setToolTipText("Channel " + c + " name");
			ComponentUtil.setFixedWidth(label, 100);

			final JTextField field = new JTextField();
			field.setText(sequence.getChannelName(c));

			panel.add(label);
			panel.add(field);

			tfsChannels[c] = field;
			panelChannels.add(panel);
		}

		panelChannels.revalidate();
	}

	public String getNameFieldValue() {
		return nameField.getText();
	}

	public double getPixelSizeXFieldValue() {
		return StringUtil.parseDouble(tfPxSizeX.getText(), 1d);
	}

	public UnitPrefix getPixelSizeXUnit() {
		return UnitPrefix.values()[cbPxSizeX.getSelectedIndex()];
	}

	public double getPixelSizeYFieldValue() {
		if (checkLinked.isSelected())
			return StringUtil.parseDouble(tfPxSizeX.getText(), 1d);
		else
			return StringUtil.parseDouble(tfPxSizeY.getText(), 1d);
	}

	public UnitPrefix getPixelSizeYUnit() {
		if (checkLinked.isSelected())
			return UnitPrefix.values()[cbPxSizeX.getSelectedIndex()];
		else
			return UnitPrefix.values()[cbPxSizeY.getSelectedIndex()];
	}

	public double getPixelSizeZFieldValue() {
		return StringUtil.parseDouble(tfPxSizeZ.getText(), 1d);
	}

	public UnitPrefix getPixelSizeZUnit() {
		return UnitPrefix.values()[cbPxSizeZ.getSelectedIndex()];
	}

	public double getTimeIntervalFieldValue() {
		return StringUtil.parseDouble(tfTimeInterval.getText(), 1d);
	}

	public String getChannelNameFieldValue(int index) {
		return tfsChannels[index].getText();
	}
}
