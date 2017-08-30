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
package icy.util;

import icy.system.IcyExceptionHandler;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;

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
 * XLS (excel) utilities class (create and write XLS documents).
 * 
 * @author Stephane
 */
public class XLSUtil
{
    public static final String FILE_EXTENSION = "xls";
    public static final String FILE_DOT_EXTENSION = "." + FILE_EXTENSION;

    /**
     * Creates and returns a new Workbook file.<br>
     * Previous existing file is overwritten.
     */
    public static WritableWorkbook createWorkbook(File file) throws IOException
    {
        return Workbook.createWorkbook(file);
    }

    /**
     * Creates and returns a new Workbook file.<br>
     * Previous existing file is overwritten.
     */
    public static WritableWorkbook createWorkbook(String filename) throws IOException
    {
        return createWorkbook(new File(filename));
    }

    /**
     * Loads and returns Workbook from an existing file (read operation only)
     */
    public static Workbook loadWorkbookForRead(File file) throws IOException, BiffException
    {
        return Workbook.getWorkbook(file);
    }

    /**
     * Loads and returns Workbook from an existing file (for write operation).<br>
     * If the file does not exist a new empty Workbook is returned.<br>
     * <br>
     * WARNING: don't forget to end by {@link #saveAndClose(WritableWorkbook)} even if you don't
     * change the Workbook else you lost all previous data already present.
     */
    public static WritableWorkbook loadWorkbookForWrite(File file) throws IOException, BiffException
    {
        if (!file.exists())
            return createWorkbook(file);

        return Workbook.createWorkbook(file, Workbook.getWorkbook(file));
    }

    /**
     * @deprecated Use {@link #loadWorkbookForRead(File)} or {@link #loadWorkbookForWrite(File)}
     *             depending your needs.
     */
    @Deprecated
    public static WritableWorkbook loadWorkbook(File file) throws IOException, BiffException
    {
        if (!file.exists())
            return createWorkbook(file);

        final WritableWorkbook result = Workbook.createWorkbook(file, Workbook.getWorkbook(file));
        // need to do it as the createWorkbook method does erase the old one
        result.write();

        return result;
    }

    /**
     * Saves and closes the specified Workbook.
     * 
     * @throws IOException
     * @throws WriteException
     */
    public static void saveAndClose(WritableWorkbook workbook) throws IOException, WriteException
    {
        workbook.write();
        workbook.close();
    }

    /**
     * Searches for the specified page in workbook and returns it.<br>
     * If the page does not exists it creates and returns a new page.<br>
     * 
     * @see #createNewPage(WritableWorkbook, String)
     */
    public static WritableSheet getPage(WritableWorkbook workbook, String title)
    {
        WritableSheet result = workbook.getSheet(title);

        if (result == null)
            result = workbook.createSheet(title, workbook.getNumberOfSheets() + 1);

        return result;
    }

    /**
     * Creates and returns a new page for the specified workbook.<br>
     * If the page already exists, add an incremented number for distinction.
     * 
     * @see #getPage(WritableWorkbook, String)
     */
    public static WritableSheet createNewPage(WritableWorkbook workbook, String title)
    {
        if (workbook.getSheet(title) == null)
            return workbook.createSheet(title, workbook.getNumberOfSheets() + 1);

        int counter = 2;
        while (true)
        {
            final String pageName = title + " " + counter;

            if (workbook.getSheet(pageName) == null)
                return workbook.createSheet(pageName, workbook.getNumberOfSheets() + 1);

            counter++;
        }
    }

    /**
     * Clear the specified workbook (remove all pages).
     */
    public static void clear(WritableWorkbook workbook)
    {
        while (workbook.getNumberOfSheets() > 0)
            workbook.removeSheet(workbook.getNumberOfSheets() - 1);
    }

    /**
     * Clear the specified page (remove all rows)
     */
    public static void clearPage(WritableSheet sheet, String name)
    {
        while (sheet.getRows() > 0)
            sheet.removeRow(sheet.getRows() - 1);
    }

    /**
     * Sets name of specified Sheet
     */
    public static void setPageName(WritableSheet sheet, String name)
    {
        sheet.setName(name);
    }

    /**
     * Adds an image to the specified Sheet.<br>
     * Returns false if the operation failed.
     */
    public static boolean addImage(WritableSheet sheet, WritableImage image)
    {
        try
        {
            sheet.addImage(image);
            return true;
        }
        catch (Exception e)
        {
            IcyExceptionHandler.showErrorMessage(e, false, true);
        }

        return false;
    }

    /**
     * Sets cell content in string format of specified Sheet.<br>
     * Returns false if the operation failed.
     */
    public static boolean setCellString(WritableSheet sheet, int x, int y, String value, Colour background)
    {
        final WritableCellFormat wcf = new WritableCellFormat();

        try
        {
            wcf.setBackground(background);
        }
        catch (WriteException e)
        {
            // not a fatal error
            IcyExceptionHandler.showErrorMessage(e, false, true);
        }

        final Label label = new Label(x, y, value, wcf);

        try
        {
            sheet.addCell(label);
            return true;
        }
        catch (Exception e)
        {
            IcyExceptionHandler.showErrorMessage(e, false, true);
        }

        return false;
    }

    /**
     * Sets cell content in string format of specified Sheet.<br>
     * Returns false if the operation failed.
     */
    public static boolean setCellString(WritableSheet sheet, int x, int y, String value)
    {
        final Label label = new Label(x, y, value);

        try
        {
            sheet.addCell(label);
            return true;
        }
        catch (Exception e)
        {
            IcyExceptionHandler.showErrorMessage(e, false, true);
        }

        return false;
    }

    /**
     * Sets cell content in double format of specified Sheet.<br>
     * Returns false if the operation failed.
     */
    public static boolean setCellNumber(WritableSheet sheet, int x, int y, double value, Colour background)
    {
        final WritableCellFormat wcf = new WritableCellFormat();

        try
        {
            wcf.setBackground(background);
        }
        catch (WriteException e)
        {
            // not a fatal error
            IcyExceptionHandler.showErrorMessage(e, false, true);
        }

        final Number number = new Number(x, y, value, wcf);

        try
        {
            sheet.addCell(number);
            return true;
        }
        catch (Exception e)
        {
            IcyExceptionHandler.showErrorMessage(e, false, true);
        }

        return false;
    }

    /**
     * Sets cell content in double format of specified Sheet.<br>
     * Returns false if the operation failed.
     */
    public static boolean setCellNumber(WritableSheet sheet, int x, int y, double value)
    {
        final Number number = new Number(x, y, value);

        try
        {
            sheet.addCell(number);
            return true;
        }
        catch (Exception e)
        {
            IcyExceptionHandler.showErrorMessage(e, false, true);
        }

        return false;
    }

    /**
     * Fill sheet content from CSV text.
     * 
     *  @return <code>true</code> if the operation succeed
     */
    public static boolean setFromCSV(WritableSheet sheet, String csvContent)
    {
        final BufferedReader br = new BufferedReader(new StringReader(csvContent));

        String line;
        int y = 0;
        try
        {
            while ((line = br.readLine()) != null)
            {
                int x = 0;

                // use tab as separator
                for (String col : line.split("\t"))
                {
                    XLSUtil.setCellString(sheet, x, y, col);
                    x++;
                }

                y++;
            }
            
            return true;
        }
        catch (IOException e)
        {
            IcyExceptionHandler.showErrorMessage(e, false, true);
            return false;
        }
    }
}
