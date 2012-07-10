package icy.gui.dialog;

import icy.gui.component.ComponentUtil;
import icy.gui.component.ThumbnailComponent;
import icy.image.IcyBufferedImage;
import icy.main.Icy;
import icy.resource.ResourceUtil;
import icy.system.thread.ThreadUtil;
import icy.type.DataType;
import icy.util.OMEUtil;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

import loci.formats.IFormatReader;
import loci.formats.meta.IMetadata;
import loci.formats.ome.OMEXMLMetadataImpl;

public class SeriesSelectionDialog extends ActionDialog implements Runnable
{
    /**
     * 
     */
    private static final long serialVersionUID = -2133845128887305016L;

    protected static final int NUM_COL = 4;
    protected static final int THUMB_X = 160;
    protected static final int THUMB_Y = 140;

    // GUI
    protected JScrollPane scrollPane;
    protected JPanel gridPanel;
    protected ThumbnailComponent[] serieComponents;
    protected JButton selectAllBtn;
    protected JButton unselectAllBtn;

    // internal
    protected IFormatReader reader;
    protected OMEXMLMetadataImpl metadata;
    protected ArrayList<Integer> selectedSeries;

    /**
     * Create the dialog.
     */
    public SeriesSelectionDialog(IFormatReader reader)
    {
        super(Icy.getMainInterface().getMainFrame(), "Series selection");

        this.reader = reader;

        initialize();

        final int series;

        if (reader != null)
        {
            metadata = OMEUtil.getOMEMetadata((IMetadata) reader.getMetadataStore());
            series = reader.getSeriesCount();
        }
        else
        {
            metadata = null;
            series = 0;
        }

        serieComponents = new ThumbnailComponent[series];

        // adjust number of row
        int numRow = series / NUM_COL;
        if (series > (NUM_COL * numRow))
            numRow++;

        ((GridLayout) gridPanel.getLayout()).setRows(numRow);

        for (int i = 0; i < numRow; i++)
        {
            for (int j = 0; j < NUM_COL; j++)
            {
                final int index = (i * NUM_COL) + j;

                if (index < series)
                {
                    final ThumbnailComponent thumb = new ThumbnailComponent(true);

                    serieComponents[index] = thumb;
                    thumb.setEnabled(true);
                    thumb.setTitle("loading...");
                    thumb.setInfos("");
                    thumb.setInfos2("");
                    gridPanel.add(thumb);
                }
                else
                    gridPanel.add(Box.createGlue());
            }
        }

        // load thumbnails...
        ThreadUtil.bgRun(this);

        // action on "OK"
        setOkAction(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                selectedSeries = new ArrayList<Integer>();

                for (int i = 0; i < serieComponents.length; i++)
                    if (serieComponents[i].isSelected())
                        selectedSeries.add(Integer.valueOf(i));
            }
        });

        // action on "Select All"
        selectAllBtn.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                for (ThumbnailComponent thumb : serieComponents)
                    thumb.setSelected(true);
            }
        });

        // action on "Unselect All"
        unselectAllBtn.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                for (ThumbnailComponent thumb : serieComponents)
                    thumb.setSelected(false);
            }
        });

        setPreferredSize(new Dimension(740, 520));
        pack();
        ComponentUtil.center(SeriesSelectionDialog.this);
        setVisible(true);
    }

    /**
     * @return the selectedSeries
     */
    public ArrayList<Integer> getSelectedSeries()
    {
        return selectedSeries;
    }

    void initialize()
    {
        mainPanel.setLayout(new BorderLayout(0, 0));

        JPanel panel = new JPanel();
        getContentPane().add(panel, BorderLayout.NORTH);
        panel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

        JLabel lblSelect = new JLabel("Click on a serie to select / unselect it.");
        ComponentUtil.setFontBold(lblSelect);
        ComponentUtil.setFontSize(lblSelect, 12);
        panel.add(lblSelect);

        scrollPane = new JScrollPane(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        gridPanel = new JPanel();
        scrollPane.setViewportView(gridPanel);
        gridPanel.setLayout(new GridLayout(2, NUM_COL, 0, 0));

        buttonPanel.removeAll();

        selectAllBtn = new JButton("Select all");
        unselectAllBtn = new JButton("Unselect all");

        buttonPanel.add(Box.createHorizontalStrut(4));
        buttonPanel.add(selectAllBtn);
        buttonPanel.add(Box.createHorizontalStrut(8));
        buttonPanel.add(unselectAllBtn);
        buttonPanel.add(Box.createHorizontalStrut(8));
        buttonPanel.add(Box.createHorizontalGlue());
        buttonPanel.add(Box.createHorizontalStrut(8));
        buttonPanel.add(okBtn);
        buttonPanel.add(Box.createHorizontalStrut(8));
        buttonPanel.add(cancelBtn);
        buttonPanel.add(Box.createHorizontalStrut(4));
    }

    @Override
    public void run()
    {
        for (int i = 0; i < serieComponents.length; i++)
        {
            try
            {
                reader.setSeries(i);

                final int sizeC = reader.getSizeC();

                final IcyBufferedImage img = IcyBufferedImage.createThumbnailFrom(reader, reader.getSizeZ() / 2,
                        reader.getSizeT() / 2);
                serieComponents[i].setImage(img.getARGBImage());
                serieComponents[i].setTitle(metadata.getImageName(i));
                serieComponents[i].setInfos(reader.getSizeX() + " x " + reader.getSizeY() + " - " + reader.getSizeZ()
                        + "Z x " + reader.getSizeT() + "T");
                serieComponents[i].setInfos2(sizeC + ((sizeC > 1) ? " channels (" : " channel (")
                        + DataType.getDataTypeFromFormatToolsType(reader.getPixelType()) + ")");
            }
            catch (Exception e)
            {
                // error image, we just totally ignore error here...
                serieComponents[i].setImage(ResourceUtil.ICON_DELETE);
                serieComponents[i].setTitle("Cannot read file");
                serieComponents[i].setInfos("");
                serieComponents[i].setInfos2("");
            }
        }
    }
}
