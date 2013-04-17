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
package icy.script;

/**
 * @author fab
 */
public class ScriptReader
{

    // import java.io.InputStream;
    // import java.io.InputStreamReader;
    // import java.io.Reader;
    // import java.util.ArrayList;
    // import java.util.List;
    //
    // import javax.script.Invocable;
    // import javax.script.ScriptEngine;
    // import javax.script.ScriptEngineFactory;
    // import javax.script.ScriptEngineManager;
    // import javax.script.ScriptException;
    //

    // System.out.println("");
    // System.out.println("------ Script supportés: ");
    //
    //
    // {
    //
    // ScriptEngineManager mgr = new ScriptEngineManager();
    // List<ScriptEngineFactory> factories =
    // mgr.getEngineFactories();
    // for (ScriptEngineFactory factory: factories) {
    // System.out.println("ScriptEngineFactory Info");
    // String engName = factory.getEngineName();
    // String engVersion = factory.getEngineVersion();
    // String langName = factory.getLanguageName();
    // String langVersion = factory.getLanguageVersion();
    // System.out.printf("\tScript Engine: %s (%s)\n",
    // engName, engVersion);
    // List<String> engNames = factory.getNames();
    // for(String name: engNames) {
    // System.out.printf("\tEngine Alias: %s\n", name);
    // }
    // System.out.printf("\tLanguage: %s (%s)\n",
    // langName, langVersion);
    // }
    // }
    //
    // System.out.println("");
    // System.out.println("------ Script test : ");
    //
    // {
    //
    // ScriptEngineManager mgr = new ScriptEngineManager();
    // ScriptEngine jsEngine = mgr.getEngineByName("JavaScript");
    // try {
    // jsEngine.eval("print('Hello, world!')");
    // } catch (ScriptException ex) {
    // ex.printStackTrace();
    // }
    // }

    // System.out.println("------ Script loading  test : ");

    // {
    // ScriptEngineManager engineMgr = new ScriptEngineManager();
    // ScriptEngine engine = engineMgr.getEngineByName("JavaScript");
    // InputStream is =
    // this.getClass().getResourceAsStream("/script/test.js");
    // try {
    // Reader reader = new InputStreamReader(is);
    // engine.eval(reader);
    // } catch (ScriptException ex) {
    // ex.printStackTrace();
    // }
    // }

    // System.out.println("");
    // System.out.println("------ Script test : using invocable to call function in a script.");
    //
    // {
    // ScriptEngineManager mgr = new ScriptEngineManager();
    // ScriptEngine jsEngine = mgr.getEngineByName("JavaScript");
    // try {
    // jsEngine.eval("function sayHello() {" +
    // "  println('Hello, world!');" +
    // "}");
    // } catch (ScriptException e) {
    // // TODO Auto-generated catch block
    // e.printStackTrace();
    // }
    // Invocable invocableEngine = (Invocable) jsEngine;
    // try {
    // invocableEngine.invokeFunction("sayHello");
    // } catch (ScriptException e) {
    // // TODO Auto-generated catch block
    // e.printStackTrace();
    // } catch (NoSuchMethodException e) {
    // // TODO Auto-generated catch block
    // e.printStackTrace();
    // }
    // }
    //
    // System.out.println("");
    // System.out.println("------ Script test : using script to access java host.");
    //
    // {
    // List<String> namesList = new ArrayList<String>();
    // namesList.add("Jill");
    // namesList.add("Bob");
    // namesList.add("Laureen");
    // namesList.add("Ed");
    //
    // ScriptEngineManager mgr = new ScriptEngineManager();
    // ScriptEngine jsEngine = mgr.getEngineByName("JavaScript");
    //
    // jsEngine.put("namesListKey", namesList);
    // System.out.println("Executing in script environment...");
    // try {
    // jsEngine.eval("var x;" +
    // "var names = namesListKey.toArray();" +
    // "for(x in names) {" +
    // "  println(names[x]);" +
    // "}" +
    // "namesListKey.add(\"Dana\");");
    // } catch (ScriptException ex) {
    // ex.printStackTrace();
    // }
    // System.out.println("Executing in Java environment...");
    // for (String name: namesList) {
    // System.out.println(name);
    // }
    //
    //
    // }
    //
    // System.out.println("");
    // System.out.println("------ Script test : using script function with java's host object as Input.");
    //
    // {
    // List<String> namesList = new ArrayList<String>();
    // namesList.add("Jill");
    // namesList.add("Bob");
    // namesList.add("Laureen");
    // namesList.add("Ed");
    //
    // ScriptEngineManager mgr = new ScriptEngineManager();
    // ScriptEngine jsEngine = mgr.getEngineByName("JavaScript");
    //
    // Invocable invocableEngine = (Invocable)jsEngine;
    // try {
    // jsEngine.eval("function printNames1(namesList) {" +
    // "  var x;" +
    // "  var names = namesList.toArray();" +
    // "  for(x in names) {" +
    // "    println(names[x]);" +
    // "  }" +
    // "}" +
    //
    // "function addName(namesList, name) {" +
    // "  namesList.add(name);" +
    // "}");
    // invocableEngine.invokeFunction("printNames1", namesList);
    // invocableEngine.invokeFunction("addName", namesList, "Dana");
    // } catch (ScriptException ex) {
    // ex.printStackTrace();
    // } catch (NoSuchMethodException ex) {
    // ex.printStackTrace();
    // }
    // }
    //
    // // System.out.println("");
    // // System.out.println("------ Script test : script import a java package.");
    // // {
    // // try {
    // // ScriptEngineManager mgr = new ScriptEngineManager();
    // // ScriptEngine jsEngine = mgr.getEngineByName("JavaScript");
    // //
    // // jsEngine.eval("importPackage(javax.swing);" +
    // // "var optionPane = " +
    // // "  JOptionPane.showMessageDialog(null, 'Hello, world!');");
    // // } catch (ScriptException ex) {
    // // ex.printStackTrace();
    // // }
    // // }
    //
    // System.out.println("");
    // System.out.println("------ Script test : Using with a script.");
    // {
    //
    // // Code Equivalent in icy :
    // Sequence s = getSequenceAt( 0 );
    // // s.setName("name changed !");
    // // for ( int x = 0 ; x < 400 ; x ++ )
    // // for ( int y = 0 ; y < 400 ; y ++ )
    // // s.getImageAt(0).setRGB(x, y, 0 , s.getImageAt( 0 ).getGray8( x, y, 0 )/2 );
    //
    //
    // try
    // {
    //
    // ScriptEngineManager mgr = new ScriptEngineManager();
    // ScriptEngine jsEngine = mgr.getEngineByName("JavaScript");
    // Invocable invocableEngine = (Invocable)jsEngine;
    //
    // jsEngine.eval(
    // "importPackage(javax.swing);" +
    // "function changeName( sequence ) {" +
    // "sequence.setName('new name given by script');"+
    // "image = sequence.getImageAt( 0 ); "+
    // "for ( x = 0 ; x < 400 ; x ++ )"+
    // "for ( y = 0 ; y < 400 ; y ++ )"+
    // "image.setRGB( x , y , 0 ," +
    // "image.getRGB(x, y, 0) / 2"+
    // ");"+
    // "}" );
    //
    // invocableEngine.invokeFunction("changeName", getSequenceAt( 0 ) );
    //
    // } catch (ScriptException ex)
    // {
    // ex.printStackTrace();
    // } catch (NoSuchMethodException e) {
    // // TODO Auto-generated catch block
    // e.printStackTrace();
    // }
    //
    // s.overlayChanged();
    // }
    //
    // System.out.println("");
    // System.out.println("------ Script test : End.");

}
