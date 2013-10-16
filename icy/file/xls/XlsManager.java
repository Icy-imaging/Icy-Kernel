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
package icy.file.xls;

import icy.util.XLSUtil;

import java.io.File;
import java.io.IOException;

import jxl.Workbook;
import jxl.format.Colour;
import jxl.read.biff.BiffException;
import jxl.write.Label;
import jxl.write.Number;
import jxl.write.WritableCellFormat;
import jxl.write.WritableImage;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

/**
 * @deprecated Use {@link XLSUtil} instead.
 */
@Deprecated
public class XlsManager
{
    WritableSheet excelPage = null;
    WritableWorkbook excelWorkbook;

    /**
     * Create a new file, overwritting existing one.
     * 
     * @param file
     * @throws IOException
     */
    public XlsManager(File file) throws IOException
    {
        excelWorkbook = Workbook.createWorkbook(file);
        // excelPage = excelWorkbook.createSheet("results", 0);
    }

    /**
     * load from existing file
     * 
     * @param file
     * @param load
     * @throws IOException
     * @throws BiffException
     */
    public XlsManager(File file, boolean load) throws IOException, BiffException
    {
        if (load)
        {
            if (!file.exists())
            {
                excelWorkbook = Workbook.createWorkbook(file);
            }
            else
            {
                excelWorkbook = Workbook.createWorkbook(file, Workbook.getWorkbook(file));
            }
        }
        // FIXME: no else here. Class should be changed now that it can load data.
    }

    public XlsManager(String file) throws IOException
    {
        this(new File(file));
    }

    public void SaveAndClose()
    {
        try
        {
            excelWorkbook.write();
            excelWorkbook.close();
        }
        catch (IOException e)
        {
            System.err.println("Error while recording XLS");
        }
        catch (WriteException e)
        {
            System.err.println("Error while recording XLS");
            e.printStackTrace();
        }
    }

    /**
     * Create a new page. If the page already exists, add an incremented number for distinction.
     * 
     * @param title
     */
    public void createNewPage(String title)
    {
        boolean ok = false;
        int counter = 2;
        String pageName = title;
        while (!ok)
        {
            if (excelWorkbook.getSheet(pageName) == null)
            {
                excelPage = excelWorkbook.createSheet(pageName, excelWorkbook.getNumberOfSheets() + 1);
                ok = true;
            }
            pageName = title + " " + counter;
            counter++;
        }
    }

    public void setPageName(String name)
    {
        excelPage.setName(name);
    }

    public void addImage(WritableImage image)
    {
        try
        {
            excelPage.addImage(image);
        }
        catch (Exception e)
        {
            System.err.println("Error while writing Xls data (XlsManager.java) File Already open by an other app ?");
        }

    }

    public void setLabel(int x, int y, String texte, Colour background)
    // public void setLabel(int x, int y, String texte, Color background)
    {
        WritableCellFormat wcf = new WritableCellFormat();
        try
        {
            // Colour colour = Colour.getInternalColour( background.getRGB() );
            wcf.setBackground(background);
        }
        catch (WriteException e1)
        {
            e1.printStackTrace();
        }
        Label label = new Label(x, y, texte, wcf);
        try
        {
            excelPage.addCell(label);
        }
        catch (Exception e)
        {
            System.err.println("Error while writing Xls data (XlsManager.java) File Already open by an other app ?");
        }

    }

    public void setLabel(int x, int y, String texte)
    {
        Label label = new Label(x, y, texte);
        try
        {
            excelPage.addCell(label);
        }
        catch (Exception e)
        {
            System.err.println("Error while writing Xls data (XlsManager.java) File Already open by an other app ?");
        }
    }

    public void setNumber(int x, int y, double n, Colour background)
    {
        WritableCellFormat wcf = new WritableCellFormat();
        try
        {
            // Colour colour = Colour.getInternalColour( background.getRGB() );
            // wcf.setBackground( colour );
            wcf.setBackground(background);
        }
        catch (WriteException e1)
        {
            e1.printStackTrace();
        }
        Number number = new Number(x, y, n, wcf);
        try
        {
            excelPage.addCell(number);
        }
        catch (Exception e)
        {
            System.err.println("Error while writing Xls data (XlsManager.java) File Already open by an other app ?");
        }
    }

    public void setNumber(int x, int y, double n)
    {
        Number number = new Number(x, y, n);
        try
        {
            excelPage.addCell(number);
        }
        catch (Exception e)
        {
            System.err.println("Error while writing Xls data (XlsManager.java) File Already open by an other app ?");
        }
    }

    public WritableSheet getExcelPage()
    {
        return excelPage;
    }

}
