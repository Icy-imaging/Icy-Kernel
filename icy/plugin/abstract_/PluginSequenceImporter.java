/**
 * 
 */
package icy.plugin.abstract_;

import icy.sequence.SequenceImporter;

/**
 * Plugin specialized for Sequence import operation (see the {@link SequenceImporter} interface)
 * 
 * @see PluginImporter
 * @see PluginFileImporter
 * @see PluginSequenceFileImporter
 * @see PluginSequenceIdImporter
 * @author Stephane
 */
public abstract class PluginSequenceImporter extends Plugin implements SequenceImporter
{

}
