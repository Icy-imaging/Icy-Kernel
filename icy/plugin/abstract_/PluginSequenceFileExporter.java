/**
 * 
 */
package icy.plugin.abstract_;

import icy.file.SequenceFileExporter;
import icy.plugin.interface_.PluginNoEDTConstructor;

/**
 * Plugin specialized for Sequence file export operation (see the {@link SequenceFileExporter}
 * interface)
 * 
 * @see PluginSequenceExporter
 * @author Stephane
 */
public abstract class PluginSequenceFileExporter extends Plugin implements SequenceFileExporter, PluginNoEDTConstructor
{

}
