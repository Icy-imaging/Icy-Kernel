package icy.searchbar.common;

import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;

public class TextUtil
{

    public static final int DEFAULT_MAX_SIZE = 65;

    public static String truncateText(String fullText, String filter)
    {
        return truncateText(fullText, filter, DEFAULT_MAX_SIZE);
    }

    /**
     * Truncate the text to a specific size, according a filter. The text will
     * be truncated around the place where the filter is found. If the string is
     * found at the beginning, the text will be like this:<br/>
     * <b><center>Lorem ipsum dolor sit amet, consec...</center><b/>
     * 
     * @param fullText
     *        : text to be truncated.
     * @param filter
     *        : string to be found in the text and truncated around.
     * @param maxSize
     *        : max size of the string
     * @return
     */
    public static String truncateText(String fullText, String filter, int maxSize)
    {
        String toReturn = fullText;
        int fullTextSize = fullText.length();
        if (fullTextSize > maxSize)
        {
            int idx = fullText.toLowerCase().indexOf(filter.toLowerCase());
            if (idx == -1)
            {
                return "";
            }
            int firstSpaceAfter;
            String textBeforeWord;
            int lastSpaceBefore;

            // extract the full word from the text
            firstSpaceAfter = fullText.indexOf(' ', idx);
            firstSpaceAfter = firstSpaceAfter == -1 ? fullTextSize : firstSpaceAfter;

            textBeforeWord = fullText.substring(0, idx);
            lastSpaceBefore = textBeforeWord.lastIndexOf(' ');
            lastSpaceBefore = lastSpaceBefore == -1 ? 0 : lastSpaceBefore;

            // determine if we are at the beginning, the end, or at the middle
            if (idx <= maxSize / 2)
            {
                toReturn = fullText.substring(0, maxSize);
                toReturn += "...";
            }
            else if ((fullTextSize - idx) <= maxSize / 2)
            {
                toReturn = fullText.substring(fullTextSize - maxSize, fullTextSize);
                toReturn = "..." + toReturn;
            }
            else
            {
                int beginIndex = idx - maxSize / 2;
                int endIndex = idx + maxSize / 2;
                if (endIndex > fullTextSize)
                    System.out.println(endIndex);
                // beginIndex = beginIndex < 0 ? 0 : beginIndex;
                // endIndex = endIndex > fullTextSize ? fullTextSize : endIndex;
                toReturn = "..." + fullText.substring(beginIndex, endIndex) + "...";
            }
        }
        // add the <b></b> tag
        int idxFinal = toReturn.toLowerCase().indexOf(filter.toLowerCase());
        if (idxFinal != -1)
            toReturn = toReturn.substring(0, idxFinal) + "<b>"
                    + toReturn.substring(idxFinal, idxFinal + filter.length()) + "</b>"
                    + toReturn.substring(idxFinal + filter.length());
        return toReturn;
    }

    /**
     * Automatically adds br/ html tag at the maxSize (or before, if cutting a
     * word).
     * 
     * @param description
     * @param maxSize
     * @return
     */
    public static String toLine(String description, int maxSize)
    {
        if (description.length() > maxSize)
        {
            for (int i = 0; i < description.length(); ++i)
            {
                if (i % maxSize == 0 && i != 0)
                {
                    int iSpace = i;
                    while (description.charAt(iSpace) != ' ' && iSpace > 0)
                        --iSpace;
                    if (iSpace == 0)
                        break;
                    else
                        description = description.substring(0, iSpace) + "<br/>"
                                + description.substring(iSpace, description.length());
                }
            }
        }
        return description;
    }

    /**
     * Automatically adds
     * 
     * <pre>
     * <br/>
     * </pre>
     * 
     * html tag at the maxSize in pixels (or before, if cutting a word).
     * 
     * @param description
     * @param container
     * @param maxSizePx
     * @return
     */
    public static String toLinePixel(String description, Component container, int maxSizePx)
    {
        Font f = container.getFont();
        FontMetrics fm = container.getFontMetrics(f);
        int charWidths[] = fm.getWidths();
        int maxLen = maxLength(description, charWidths, maxSizePx);
        return toLine(description, maxLen);

    }

    /**
     * Calculates the number of chars in the maxSizePx value.
     * 
     * @param description
     * @param charWidths
     * @param maxSizePx
     * @return
     */
    public static int maxLength(String description, int charWidths[], int maxSizePx)
    {
        int maxLen = 0;
        int sizePx = 0;
        while (sizePx < maxSizePx && maxLen < description.length())
        {
            sizePx += charWidths[description.charAt(maxLen)];
            ++maxLen;
        }
        return maxLen;
    }

    /**
     * Puts html bold tags around the regex found.
     * 
     * @param text
     * @param regex
     * @return
     */
    public static String highlight(String text, String filter)
    {
        int idx;
        int filterLength = filter.length();
        boolean isShort = filterLength <= 2;
        String lowerCaseFilter = filter.toLowerCase();
        String result = "";
        do
        {
            idx = text.toLowerCase().indexOf(lowerCaseFilter);
            if (idx >= 0)
            {
                result = result + text.substring(0, idx) + "<b>" + text.substring(idx, idx + filterLength) + "</b>";
                text = text.substring(idx + filterLength);
            }
        }
        while (idx != -1 && !isShort);
        result += text;
        return result;
    }
}
