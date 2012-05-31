package icy.gui.menu.tools;

import java.awt.BorderLayout;

import javax.swing.JPanel;

public class SequenceMerge extends JPanel
{
    /**
     * 
     */
    private static final long serialVersionUID = 7384352969078386250L;

    /**
     * Create the panel.
     */
    public SequenceMerge()
    {
        super();
        setLayout(new BorderLayout(0, 0));
        
        JPanel sourcesPanel = new JPanel();
        add(sourcesPanel);
        
        JPanel resultPanel = new JPanel();
        add(resultPanel, BorderLayout.SOUTH);
        
        

    }

}
