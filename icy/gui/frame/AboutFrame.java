/*
 * Copyright 2010-2013 Institut Pasteur.
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

import icy.file.FileUtil;
import icy.main.Icy;

import javax.swing.BoxLayout;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

public class AboutFrame extends IcyFrame
{
    JTabbedPane tabbedPane = new JTabbedPane();

    public AboutFrame()
    {
        super("About ICY", false, true, false, false);

        setLayout(new BoxLayout(getContentPane(), BoxLayout.PAGE_AXIS));
        add(tabbedPane);

        String about = "<html><center>" + "<br>" + "<br><b>ICY</b>" + "<br><br><b><i>" + Icy.version + "</i></b><br>"
                + "<br>Quantitative Image Analysis Unit" + "<br>Institut Pasteur"
                + "<br>Unite d'analyse d images quantitative" + "<br>25,28 Rue du Docteur Roux\n"
                + "<br>75015 Paris - France" + "<br>" + "<br><i>http://www.bioimageanalysis.com/icy</i>" + "</html>";

        String author = "<html><center><br>" + "<br>" + "<br><font size=3><u>The AIQ Team:</u></font>" + "<br>"
                + "<br><b>machin</b> bidule" + "<br><b>machin</b> bidule" + "<br><b>machin</b> bidule"
                + "<br><b>machin</b> bidule" + "</html>";

        String thanks = "<html><center><br>"
                + "<br>The authors of <b>bioformat</b> <i>http://www.loci.wisc.edu/ome/formats.html</i> for their fast answer and bugfixes."
                + "</html>";

        String external = "<html><center><br>"
                + "<br>"
                + "<br><font size=3><u>LIBRARY:</u></font>"
                + "<br>"
                + "<br><b>BioFormat</b> - http://www.loci.wisc.edu/ome/formats.html"
                + "<br>"
                + "<br><b>Substance</b> - a completer : http://substance"
                + "<br>"
                + "<br><font size=3><u>CODE RE-USED:</u></font>"
                + "<br>"
                + "<br>Class finder by Jorg Hohwiller for the m-m-m project ({@link http://m-m-m.sf.net}) used for dynamic plugin load."
                + "</html>";

        // String license = "<html><center>" + "<br><font size=3><u>LICENSE:</u></font>" + "<br>" +
        // "<br>todo" + "</html>";
        String license = "<html><pre>" + new String(FileUtil.load("COPYING.txt", false)) + "</pre></html>";

        JEditorPane aboutEditorPane = new JEditorPane("text/html", about);
        aboutEditorPane.setEditable(false);
        aboutEditorPane.setCaretPosition(0);

        JEditorPane authorEditorPane = new JEditorPane("text/html", author);
        authorEditorPane.setEditable(false);
        authorEditorPane.setCaretPosition(0);

        JEditorPane thanksToEditorPane = new JEditorPane("text/html", thanks);
        thanksToEditorPane.setEditable(false);
        thanksToEditorPane.setCaretPosition(0);

        JEditorPane externalEditorPane = new JEditorPane("text/html", external);
        externalEditorPane.setEditable(false);
        externalEditorPane.setCaretPosition(0);

        JEditorPane licenseEditorPane = new JEditorPane("text/html", license);
        licenseEditorPane.setEditable(false);
        licenseEditorPane.setCaretPosition(0);

        setSize(680, 480);

        tabbedPane.add("About", new JScrollPane(aboutEditorPane));
        // tabbedPane.add("Authors", new JScrollPane(authorEditorPane));
        // tabbedPane.add("Thanks to", new JScrollPane(thanksToEditorPane));
        // tabbedPane.add("External code and library", new JScrollPane(externalEditorPane));
        tabbedPane.add("License", new JScrollPane(licenseEditorPane));

        setVisible(true);
        addToMainDesktopPane();
        center();
        requestFocus();
    }
}
