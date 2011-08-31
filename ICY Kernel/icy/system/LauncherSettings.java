/*
 * Copyright 2010, 2011 Institut Pasteur.
 * 
 * This file is part of ICY.
 * 
 * ICY is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * ICY is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with ICY. If not, see <http://www.gnu.org/licenses/>.
 */
package icy.system;

import icy.file.FileUtil;
import icy.util.StringUtil;
import icy.util.XMLUtil;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * This class permit to get and set the java launcher settings
 * 
 * @author Stephane
 * @deprecated
 */
public class LauncherSettings
{
    private static final String FILE_WIN = "icy.bat";
    private static final String FILE_MAC = "icy.app/Contents/Info.plist";
    // private static final String FILE_UNIX = "./icy";

    private static final String CMD_JAVA = "java";
    private static final String CMD_JAVAW = "javaw";

    private static final String PARAM_JAR = "-jar";
    private static final String PARAM_CP = "-cp";
    private static final String PARAM_CLASSPATH = "-classpath";
    private static final String PARAM_XMX = "-Xmx";
    private static final String PARAM_XSS = "-Xss";

    private static final String JAR_ICY = "icy.jar";

    private int maxMemoryMB;
    private int maxStackSizeKB;
    private String classPath;
    private String extraOptions;

    public LauncherSettings()
    {
        super();

        load();
    }

    public boolean load()
    {
        // load setting depending current system
        boolean result = false;

        // WIN
        if (SystemUtil.isWindow())
        {
            final byte[] data = FileUtil.load(FILE_WIN, false);

            // no empty file ?
            if (data != null)
            {
                for (String line : new String(data).split("\n"))
                {
                    if (line.trim().toLowerCase().startsWith(CMD_JAVA))
                    {
                        load(line);
                        result = true;
                    }
                }
            }
        }
        else if (SystemUtil.isMac())
        {
            // MAC
            final Document doc = XMLUtil.loadDocument(FILE_MAC, false);

            if (doc != null)
                result &= load(doc);
            else
                result = false;
        }
        else
        {
            // TODO: UNIX
        }

        return result;
    }

    private void load(String paramsString)
    {
        maxMemoryMB = -1;
        maxStackSizeKB = -1;
        classPath = null;
        extraOptions = null;

        final String[] params = paramsString.split(" ");

        for (int i = 0; i < params.length; i++)
        {
            final String param = params[i];
            final String lcParam = param.toLowerCase();

            // ignore launch commands
            if (lcParam.equals(CMD_JAVA))
                continue;
            if (lcParam.equals(CMD_JAVAW))
                continue;
            if (lcParam.equals(PARAM_JAR))
                continue;
            if (lcParam.equals(JAR_ICY))
                continue;

            // classpath parameter
            if (lcParam.equals(PARAM_CP) || lcParam.equals(PARAM_CLASSPATH))
            {
                // get classpath string
                i++;

                if (i < params.length)
                    classPath = params[i];
                else
                    classPath = "";
            }
            // max memory
            else if (param.startsWith(PARAM_XMX))
            {
                final int l = PARAM_XMX.length();
                maxMemoryMB = StringUtil
                        .parseInt(param.substring(l, StringUtil.getNextNonDigitCharIndex(param, l)), -1);
            }
            // max stack size
            else if (param.startsWith(PARAM_XSS))
            {
                final int l = PARAM_XSS.length();
                maxStackSizeKB = StringUtil.parseInt(param.substring(l, StringUtil.getNextNonDigitCharIndex(param, l)),
                        -1);
            }
            else if (extraOptions == null)
                extraOptions = param;
            else
                extraOptions += " " + param;
        }
    }

    private Node findVMOptionsNode(Document doc)
    {
        final Node root = XMLUtil.getElement(doc.getDocumentElement(), "dict");

        for (Node node : XMLUtil.getSubNodes(root, "key"))
        {
            if (StringUtil.equals(XMLUtil.getValue((Element) node, ""), "Java"))
            {
                Node nextNode = node.getNextSibling();

                while (nextNode != null)
                {
                    if (nextNode.getNodeName().equals("dict"))
                    {
                        for (Node javaNode : XMLUtil.getSubNodes(nextNode, "key"))
                        {
                            if (StringUtil.equals(XMLUtil.getValue((Element) javaNode, ""), "VMOptions"))
                            {
                                Node nextJavaNode = javaNode.getNextSibling();

                                while (nextJavaNode != null)
                                {
                                    // we finally found that stupid node !
                                    if (nextJavaNode.getNodeName().equals("string"))
                                        return nextJavaNode;

                                    nextJavaNode = nextJavaNode.getNextSibling();
                                }
                            }
                        }
                    }

                    nextNode = nextNode.getNextSibling();
                }
            }
        }

        return null;
    }

    private boolean load(Document doc)
    {
        final Node vmOptionsNode = findVMOptionsNode(doc);

        if (vmOptionsNode != null)
        {
            load(XMLUtil.getValue((Element) vmOptionsNode, ""));
            return true;
        }

        return false;
    }

    public boolean save()
    {
        boolean result = false;

        // WIN
        final byte[] data = FileUtil.load(FILE_WIN, false);

        // no empty file ?
        if (data != null)
        {
            String fileStr = "";

            for (String line : new String(data).split("\n"))
            {
                if (line.trim().toLowerCase().startsWith(CMD_JAVA))
                    fileStr += '\n' + getWinCmd();
                else
                    fileStr += "\n" + line;
            }

            result = FileUtil.save(FILE_WIN, fileStr.getBytes(), false);
        }

        // MAC
        final Document doc = XMLUtil.loadDocument(FILE_MAC, false);

        if (doc != null)
            result &= save(doc);
        else
            result = false;

        // UNIX

        return false;
    }

    private boolean save(Document doc)
    {
        final Node vmOptionsNode = findVMOptionsNode(doc);

        if (vmOptionsNode != null)
        {
            XMLUtil.removeAllChilds(vmOptionsNode);
            XMLUtil.addValue(vmOptionsNode, getParamsString());
            return XMLUtil.saveDocument(doc, FILE_MAC);
        }

        return false;
    }

    private String getWinCmd()
    {
        return CMD_JAVA + getParamsString() + " " + PARAM_JAR + " " + JAR_ICY;
    }

    private String getParamsString()
    {
        String result = "";

        if (maxMemoryMB != -1)
            result += " " + PARAM_XMX + maxMemoryMB + "m";
        if (maxStackSizeKB != -1)
            result += " " + PARAM_XSS + maxStackSizeKB + "k";
        if (classPath != null)
            result += " " + PARAM_CP + " " + classPath;
        if (extraOptions != null)
            result += " " + extraOptions;

        return result;
    }

    /**
     * @return the maxMemoryMB
     */
    public int getMaxMemoryMB()
    {
        return maxMemoryMB;
    }

    /**
     * @param maxMemoryMB
     *        the maxMemoryMB to set
     */
    public void setMaxMemoryMB(int maxMemoryMB)
    {
        this.maxMemoryMB = maxMemoryMB;
    }

    /**
     * @return the maxStackSizeKB
     */
    public int getMaxStackSizeKB()
    {
        return maxStackSizeKB;
    }

    /**
     * @param maxStackSizeKB
     *        the maxStackSizeKB to set
     */
    public void setMaxStackSizeKB(int maxStackSizeKB)
    {
        this.maxStackSizeKB = maxStackSizeKB;
    }

    /**
     * @return the classPath
     */
    public String getClassPath()
    {
        return classPath;
    }

    /**
     * @param classPath
     *        the classPath to set
     */
    public void setClassPath(String classPath)
    {
        this.classPath = classPath;
    }

    /**
     * @return the extraOptions
     */
    public String getExtraOptions()
    {
        return extraOptions;
    }

    /**
     * @param extraOptions
     *        the extraOptions to set
     */
    public void setExtraOptions(String extraOptions)
    {
        this.extraOptions = extraOptions;
    }
}
