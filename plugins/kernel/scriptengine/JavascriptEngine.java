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
package plugins.kernel.scriptengine;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import sun.org.mozilla.javascript.internal.Context;
import sun.org.mozilla.javascript.internal.EvaluatorException;
import sun.org.mozilla.javascript.internal.ImporterTopLevel;
import sun.org.mozilla.javascript.internal.NativeJavaObject;
import sun.org.mozilla.javascript.internal.RhinoException;
import sun.org.mozilla.javascript.internal.Script;
import sun.org.mozilla.javascript.internal.ScriptableObject;

import icy.file.FileUtil;
import icy.plugin.PluginLoader;
import icy.plugin.abstract_.PluginActionable;
import icy.plugin.interface_.PluginScriptEngine;
import icy.script.ScriptEditor;

/**
 * This class is used to run plugin write in script.
 * 
 * @author Will Ouyang & Tprovoost
 */
public class JavascriptEngine extends PluginActionable implements PluginScriptEngine {
	private ScriptableObject scriptable;
	private String lastFileName = "";
	public JavascriptEngine()
	{
		super();
		//init the script engine
		Context context = Context.enter();
		context.setApplicationClassLoader(PluginLoader.getLoader());
		scriptable = new IcyImporterTopLevel(context);
		Context.exit();		
					 
	}
	@Override
	public void run() {		
		new ScriptEditor();
	}

	public void evalFile(File file) throws EvaluatorException
	{
		if(file == null)
			return;
		if (!file.exists() || !file.isFile())
		{
			throw new EvaluatorException("The script file could not be found, please check if it is correctly saved on the disk.", file.getAbsolutePath(), -1);
		}
		
		this.lastFileName = file.getAbsolutePath();
		byte[] bytes = null;
		try
		{
			BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
			bytes = new byte[bis.available()];
			bis.read(bytes);
			bis.close();
			((IcyImporterTopLevel)scriptable).setcwd(FileUtil.getDirectory(file.getPath()));
		} catch (IOException e1)
		{
			throw new EvaluatorException(e1.getMessage(), file.getAbsolutePath(), -1);
		}
		String scripts = new String(bytes);
		eval(scripts);
	
	}
	public void eval(String scripts) throws EvaluatorException
	{
		Context context = Context.enter();
		context.setApplicationClassLoader(PluginLoader.getLoader());
		try
		{
			System.out.println("excuting javascript...");
			Script script = context.compileString(scripts, "script", 0, null);
			script.exec(context, scriptable);
			System.out.println("done!");
		} catch(Exception e)
		{
			System.out.println(e.toString());
		} finally
		{
			Context.exit();
		}
	}	
	class IcyImporterTopLevel extends ImporterTopLevel
	{

		private String cwd = "";
		public IcyImporterTopLevel(Context context)
		{
			super(context);
			String[] names = { "println", "print", "eval", "getcwd" };
			defineFunctionProperties(names, IcyImporterTopLevel.class, ScriptableObject.DONTENUM);
		}

		public void println(Object o)
		{
			System.out.print(Context.toString(o) + "\n");
		}

		public void print(Object o)
		{
			System.out.print(Context.toString(o));
		}
		public String getcwd()
		{
			return cwd;
		}
		public  void setcwd(String s)
		{
			cwd =s;
		}
		public void eval(Object o) throws  FileNotFoundException,EvaluatorException
		{
			File f;
			if (o instanceof NativeJavaObject && ((NativeJavaObject) o).unwrap() instanceof File)
				f = (File) ((NativeJavaObject) o).unwrap();
			else if (o instanceof String)
			{
				String s = (String) o;
				f = new File(s);
				if (!f.exists() && !s.contains(File.separator))
				{
					if (!s.endsWith(".js"))
						s += ".js";
					s = FileUtil.getDirectory(lastFileName) + File.separator + s;
					f = new File(s);
				}
			} else
			{
				// getErrorWriter().write("Argument must be a file of a string.");
				throw new FileNotFoundException("Argument must be a file of a string.");
			}
			BufferedInputStream bis = new BufferedInputStream(new FileInputStream(f));
			byte[] bytes = null;
			try
			{
				bytes = new byte[bis.available()];
				bis.read(bytes);
				bis.close();
			} catch (IOException e1)
			{
				// getErrorWriter().write(e1.getMessage());
			}
			String s = new String(bytes);
			Context context = Context.enter();
			context.setApplicationClassLoader(PluginLoader.getLoader());
			try
			{
				Script script = context.compileString(s, "script", 0, null);
				script.exec(context, scriptable);
			} catch (EvaluatorException e)
			{
				// getErrorWriter().write(e.getMessage());
				throw new EvaluatorException(e.getMessage(), e.sourceName(),  e.columnNumber());
			} catch (RhinoException e3)
			{
				// getErrorWriter().write(e3.getMessage());
				throw new EvaluatorException(e3.getMessage(), e3.sourceName(),  e3.columnNumber());
			} finally
			{
				Context.exit();
			}
		}
	}
	
}	


