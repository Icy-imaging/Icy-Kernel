/*
 * Copyright 2010-2015 Institut Pasteur.
 * 
 * This file is part of Icy.
 * 
 * Icy is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Icy is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Icy. If not, see <http://www.gnu.org/licenses/>.
 */
package icy.gui.frame;

import icy.main.Icy;
import icy.system.thread.ThreadUtil;

import javax.swing.BoxLayout;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

public class AboutFrame extends IcyFrame
{
    final JTabbedPane tabbedPane;
    final JEditorPane aboutEditorPane;
    final JEditorPane authorEditorPane;
    final JEditorPane thanksToEditorPane;
    final JEditorPane externalEditorPane;
    final JEditorPane changeLogEditorPane;
    final JEditorPane licenseEditorPane;

    public AboutFrame(int defaultTab)
    {
        super("About ICY", false, true, false, false);

        aboutEditorPane = new JEditorPane("text/html", "");
        aboutEditorPane.setEditable(false);
        aboutEditorPane.setCaretPosition(0);

        authorEditorPane = new JEditorPane("text/html", "");
        authorEditorPane.setEditable(false);
        authorEditorPane.setCaretPosition(0);

        thanksToEditorPane = new JEditorPane("text/html", "");
        thanksToEditorPane.setEditable(false);
        thanksToEditorPane.setCaretPosition(0);

        externalEditorPane = new JEditorPane("text/html", "");
        externalEditorPane.setEditable(false);
        externalEditorPane.setCaretPosition(0);

        changeLogEditorPane = new JEditorPane("text/html", "");
        externalEditorPane.setEditable(false);
        externalEditorPane.setCaretPosition(0);

        licenseEditorPane = new JEditorPane("text/html", "");
        licenseEditorPane.setEditable(false);
        licenseEditorPane.setCaretPosition(0);

        tabbedPane = new JTabbedPane();
        tabbedPane.add("About", new JScrollPane(aboutEditorPane));
        // tabbedPane.add("Authors", new JScrollPane(authorEditorPane));
        // tabbedPane.add("Thanks to", new JScrollPane(thanksToEditorPane));
        tabbedPane.add("ChangeLog", new JScrollPane(changeLogEditorPane));
        // tabbedPane.add("External code and library", new JScrollPane(externalEditorPane));
        tabbedPane.add("License", new JScrollPane(licenseEditorPane));

        // select the default tab
        tabbedPane.setSelectedIndex(defaultTab);

        setLayout(new BoxLayout(getContentPane(), BoxLayout.PAGE_AXIS));
        add(tabbedPane);
        setSize(680, 480);
        setVisible(true);
        addToDesktopPane();
        center();
        requestFocus();

        loadInfos();
    }

    private void loadInfos()
    {
        ThreadUtil.bgRun(new Runnable()
        {
            @Override
            public void run()
            {
                final String about = "<html><center>" + "<br>" + "<br><h2>Icy " + Icy.version + "</h2>"
                        + "<br>BioImage Analysis unit" + "<br>Institut Pasteur"
                        + "<br>Unite d'analyse d images quantitative" + "<br>25,28 Rue du Docteur Roux\n"
                        + "<br>75015 Paris - France" + "<br>"
                        + "<br><a href=\"http://icy.bioimageanalysis.com\">http://icy.bioimageanalysis.com</a>"
                        + "</html>";

                final String author = "<html><center><br>" + "<br>" + "<br><font size=3><u>The AIQ Team:</u></font>"
                        + "<br>" + "<br><b>machin</b> bidule" + "<br><b>machin</b> bidule" + "<br><b>machin</b> bidule"
                        + "<br><b>machin</b> bidule" + "</html>";

                final String thanks = "<html><center><br>"
                        + "<br>The authors of <b>bioformat</b> <i>http://www.loci.wisc.edu/ome/formats.html</i> for their fast answer and bugfixes."
                        + "</html>";

                final String external = "<html><center><br>"
                        + "<br>"
                        + "<br><font size=3><u>LIBRARY:</u></font>"
                        + "<br>"
                        + "<br><b>BioFormat</b> - http://www.openmicroscopy.org/site/products/bio-formats"
                        + "<br><b>Substance</b> - https://java.net/projects/substance"
                        + "</html>";

                final String changelog = "<html><pre>" + Icy.getChangeLog() + "</pre></html>";
                final String license = "<html><pre>" + Icy.getLicense() + "</pre></html>";

                aboutEditorPane.setText(about);
                authorEditorPane.setText(author);
                thanksToEditorPane.setText(thanks);
                externalEditorPane.setText(external);
                changeLogEditorPane.setText(changelog);
                licenseEditorPane.setText(license);
            }
        });
    }

    public AboutFrame()
    {
        this(0);
    }

}
