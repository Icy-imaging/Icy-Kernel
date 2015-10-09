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
// CodeHacker.java
//

/*
 * ImageJ software for multidimensional image processing and analysis.
 * 
 * Copyright (c) 2010, ImageJDev.org.
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * Neither the names of the ImageJDev.org developers nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package icy.system;

import icy.util.StringUtil;

import java.util.ArrayList;

import javassist.CannotCompileException;
import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.NotFoundException;

/**
 * The code hacker provides a mechanism for altering the behavior of classes
 * before they are loaded, for the purpose of injecting new methods and/or
 * altering existing ones.
 * <p>
 * In ImageJ, this mechanism is used to provide new seams into legacy ImageJ1 code, so that (e.g.)
 * the modern UI is aware of IJ1 events as they occur.
 * </p>
 * 
 * @author Curtis Rueden
 * @author Rick Lentz
 * @author Stephane Dallongeville
 */
public class ClassPatcher
{
    private final static String ARG_RESULT = "result";

    private final ClassPool pool;
    private final String patchPackage;
    private final String patchSuffix;

    public ClassPatcher(String patchPackage, String patchSuffix)
    {
        pool = ClassPool.getDefault();
        pool.appendClassPath(new ClassClassPath(getClass()));
//        pool.appendClassPath(new LoaderClassPath(PluginLoader.getLoader()));
        this.patchPackage = patchPackage;
        this.patchSuffix = patchSuffix;
    }

    /**
     * Modifies a class by injecting additional code at the end of the specified
     * method's body.
     * <p>
     * The extra code is defined in the imagej.legacy.patches package, as described in the
     * documentation for {@link #insertMethod(String, String)}.
     * </p>
     * 
     * @param fullClass
     *        Fully qualified name of the class to modify.
     * @param methodSig
     *        Method signature of the method to modify; e.g.,
     *        "public void updateAndDraw()"
     */
    public void insertAfterMethod(final String fullClass, final String methodSig)
    {
        insertAfterMethod(fullClass, methodSig, newCode(fullClass, methodSig));
    }

    /**
     * Modifies a class by injecting the provided code string at the end of the
     * specified method's body.
     * 
     * @param fullClass
     *        Fully qualified name of the class to modify.
     * @param methodSig
     *        Method signature of the method to modify; e.g.,
     *        "public void updateAndDraw()"
     * @param newCode
     *        The string of code to add; e.g., System.out.println(\"Hello
     *        World!\");
     */
    public void insertAfterMethod(final String fullClass, final String methodSig, final String newCode)
    {
        try
        {
            getMethod(fullClass, methodSig).insertAfter(newCode);
        }
        catch (final CannotCompileException e)
        {
            throw new IllegalArgumentException("Cannot modify method: " + methodSig, e);
        }
    }

    /**
     * Modifies a class by injecting additional code at the start of the specified
     * method's body.
     * <p>
     * The extra code is defined in the imagej.legacy.patches package, as described in the
     * documentation for {@link #insertMethod(String, String)}.
     * </p>
     * 
     * @param fullClass
     *        Fully qualified name of the class to override.
     * @param methodSig
     *        Method signature of the method to override; e.g.,
     *        "public void updateAndDraw()"
     */
    public void insertBeforeMethod(final String fullClass, final String methodSig)
    {
        insertBeforeMethod(fullClass, methodSig, newCode(fullClass, methodSig));
    }

    /**
     * Modifies a class by injecting the provided code string at the start of the
     * specified method's body.
     * 
     * @param fullClass
     *        Fully qualified name of the class to override.
     * @param methodSig
     *        Method signature of the method to override; e.g.,
     *        "public void updateAndDraw()"
     * @param newCode
     *        The string of code to add; e.g., System.out.println(\"Hello
     *        World!\");
     */
    public void insertBeforeMethod(final String fullClass, final String methodSig, final String newCode)
    {
        try
        {
            getMethod(fullClass, methodSig).insertBefore(newCode);
        }
        catch (final CannotCompileException e)
        {
            throw new IllegalArgumentException("Cannot modify method: " + methodSig, e);
        }
    }

    /**
     * Modifies a class by injecting a new method.
     * <p>
     * The body of the method is defined in the imagej.legacy.patches package, as described in the
     * {@link #insertMethod(String, String)} method documentation.
     * <p>
     * The new method implementation should be declared in the imagej.legacy.patches package, with
     * the same name as the original class plus "Methods"; e.g., overridden ij.gui.ImageWindow
     * methods should be placed in the imagej.legacy.patches.ImageWindowMethods class.
     * </p>
     * <p>
     * New method implementations must be public static, with an additional first parameter: the
     * instance of the class on which to operate.
     * </p>
     * 
     * @param fullClass
     *        Fully qualified name of the class to override.
     * @param methodSig
     *        Method signature of the method to override; e.g.,
     *        "public void setVisible(boolean vis)"
     */
    public void insertMethod(final String fullClass, final String methodSig)
    {
        insertMethod(fullClass, methodSig, newCode(fullClass, methodSig));
    }

    /**
     * Modifies a class by injecting the provided code string as a new method.
     * 
     * @param fullClass
     *        Fully qualified name of the class to override.
     * @param methodSig
     *        Method signature of the method to override; e.g.,
     *        "public void updateAndDraw()"
     * @param newCode
     *        The string of code to add; e.g., System.out.println(\"Hello
     *        World!\");
     */
    public void insertMethod(final String fullClass, final String methodSig, final String newCode)
    {
        final CtClass classRef = getClass(fullClass);
        final String methodBody = methodSig + " { " + newCode + " } ";
        try
        {
            final CtMethod methodRef = CtNewMethod.make(methodBody, classRef);
            classRef.addMethod(methodRef);
        }
        catch (final CannotCompileException e)
        {
            throw new IllegalArgumentException("Cannot add method: " + methodSig, e);
        }
    }

    /**
     * Modifies a class by replacing the specified method.
     * <p>
     * The new code is defined in the imagej.legacy.patches package, as described in the
     * documentation for {@link #insertMethod(String, String)}.
     * </p>
     * 
     * @param fullClass
     *        Fully qualified name of the class to override.
     * @param methodSig
     *        Method signature of the method to replace; e.g.,
     *        "public void setVisible(boolean vis)"
     */
    public void replaceMethod(final String fullClass, final String methodSig)
    {
        replaceMethod(fullClass, methodSig, newCode(fullClass, methodSig));
    }

    /**
     * Modifies a class by replacing the specified method with the provided code
     * string.
     * 
     * @param fullClass
     *        Fully qualified name of the class to override.
     * @param methodSig
     *        Method signature of the method to replace; e.g.,
     *        "public void setVisible(boolean vis)"
     * @param newCode
     *        The string of code to add; e.g., System.out.println(\"Hello
     *        World!\");
     */
    public void replaceMethod(final String fullClass, final String methodSig, final String newCode)
    {
        try
        {
            getMethod(fullClass, methodSig).setBody(newCode);
        }
        catch (final CannotCompileException e)
        {
            throw new IllegalArgumentException("Cannot modify method: " + methodSig, e);
        }
    }

    /**
     * Loads the given, possibly modified, class.
     * <p>
     * This method must be called to confirm any changes made with {@link #insertAfterMethod},
     * {@link #insertBeforeMethod}, {@link #insertMethod} or {@link #replaceMethod}.
     * </p>
     * 
     * @param fullClass
     *        Fully qualified class name to load.
     * @return the loaded class
     */
    public Class<?> loadClass(final String fullClass)
    {
        final CtClass classRef = getClass(fullClass);
        try
        {
            return classRef.toClass();
        }
        catch (final CannotCompileException e)
        {
            IcyExceptionHandler.showErrorMessage(e, false);
            System.err.println("Cannot load class: " + fullClass);
            return null;
        }
    }

    /** Gets the Javassist class object corresponding to the given class name. */
    private CtClass getClass(final String fullClass)
    {
        try
        {
            return pool.get(fullClass);
        }
        catch (final NotFoundException e)
        {
            throw new IllegalArgumentException("No such class: " + fullClass, e);
        }
    }

    /**
     * Gets the Javassist method object corresponding to the given method
     * signature of the specified class name.
     */
    private CtMethod getMethod(final String fullClass, final String methodSig)
    {
        final CtClass cc = getClass(fullClass);
        final String name = getMethodName(methodSig);
        final String[] argTypes = getMethodArgTypes(methodSig, false);
        final CtClass[] params = new CtClass[argTypes.length];
        for (int i = 0; i < params.length; i++)
        {
            params[i] = getClass(argTypes[i]);
        }
        try
        {
            return cc.getDeclaredMethod(name, params);
        }
        catch (final NotFoundException e)
        {
            throw new IllegalArgumentException("No such method: " + methodSig, e);
        }
    }

    /**
     * Generates a new line of code calling the {@link imagej.legacy.patches} class and method
     * corresponding to the given method signature.
     */
    private String newCode(final String fullClass, final String methodSig)
    {
        final int dotIndex = fullClass.lastIndexOf(".");
        final String className = fullClass.substring(dotIndex + 1);

        final String methodName = getMethodName(methodSig);
        final boolean isStatic = isStatic(methodSig);
        final boolean isVoid = isVoid(methodSig);

        final StringBuilder newCode = new StringBuilder((isVoid ? "" : "return ") + patchPackage + "." + className
                + patchSuffix + "." + methodName + "(");
        boolean firstArg = true;
        if (!isStatic)
        {
            newCode.append("this");
            firstArg = false;
        }
        int i = 1;
        for (String argName : getMethodArgNames(methodSig, true))
        {
            if (firstArg)
                firstArg = false;
            else
                newCode.append(", ");

            if (StringUtil.equals(argName, ARG_RESULT))
                newCode.append("$_");
            else
            {
                newCode.append("$" + i);
                i++;
            }
        }
        newCode.append(");");

        return newCode.toString();
    }

    /** Extracts the method name from the given method signature. */
    private String getMethodName(final String methodSig)
    {
        final int parenIndex = methodSig.indexOf("(");
        final int spaceIndex = methodSig.lastIndexOf(" ", parenIndex);
        return methodSig.substring(spaceIndex + 1, parenIndex);
    }

    private String[] getMethodArgs(final String methodSig, final boolean wantResult)
    {
        final ArrayList<String> result = new ArrayList<String>();

        final int parenIndex = methodSig.indexOf("(");
        final String methodArgs = methodSig.substring(parenIndex + 1, methodSig.length() - 1);
        final String[] args = methodArgs.equals("") ? new String[0] : methodArgs.split(",");
        for (String arg : args)
        {
            final String a = arg.trim();
            if (!StringUtil.equals(a.split(" ")[1], ARG_RESULT) || wantResult)
                result.add(a);
        }

        return result.toArray(new String[result.size()]);
    }

    private String[] getMethodArgTypes(final String methodSig, final boolean wantResult)
    {
        final String[] args = getMethodArgs(methodSig, wantResult);
        for (int i = 0; i < args.length; i++)
            args[i] = args[i].split(" ")[0];
        return args;
    }

    private String[] getMethodArgNames(final String methodSig, final boolean wantResult)
    {
        final String[] args = getMethodArgs(methodSig, wantResult);
        for (int i = 0; i < args.length; i++)
            args[i] = args[i].split(" ")[1];
        return args;
    }

    /** Returns true if the given method signature is static. */
    private boolean isStatic(final String methodSig)
    {
        final int parenIndex = methodSig.indexOf("(");
        final String methodPrefix = methodSig.substring(0, parenIndex);
        for (final String token : methodPrefix.split(" "))
        {
            if (token.equals("static"))
                return true;
        }
        return false;
    }

    /** Returns true if the given method signature returns void. */
    private boolean isVoid(final String methodSig)
    {
        final int parenIndex = methodSig.indexOf("(");
        final String methodPrefix = methodSig.substring(0, parenIndex);
        return methodPrefix.startsWith("void ") || methodPrefix.indexOf(" void ") > 0;
    }

}
