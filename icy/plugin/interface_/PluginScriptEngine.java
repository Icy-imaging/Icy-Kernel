package icy.plugin.interface_;

import java.io.File;

public interface PluginScriptEngine {
	public void eval(String scripts);
	public void evalFile(File file);
	
}
