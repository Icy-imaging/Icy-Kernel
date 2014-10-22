/**
 * 
 */
package icy.plugin.abstract_;

import icy.file.FileImporter;
import icy.plugin.interface_.PluginNoEDTConstructor;
import icy.plugin.interface_.PluginThreaded;

/**
 * Plugin specialized for File import operation (see the {@link FileImporter} interface)
 * 
 * @see PluginImporter
 * @see PluginSequenceImporter
 * @see PluginSequenceFileImporter
 * @see PluginSequenceIdImporter
 * @author Stephane
 */
public abstract class PluginFileImporter extends Plugin implements FileImporter, PluginNoEDTConstructor
{

}
