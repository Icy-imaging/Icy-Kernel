/**
 * 
 */
package icy.sequence;

import icy.file.SequenceFileImporter;

/**
 * Sequence importer interface.<br>
 * Used to define a specific {@link Sequence} importer visible in the <b>Import</b> section.<br>
 * Can take any resource type as input and return a Sequence as result.
 * Note that you have {@link SequenceFileImporter} interface which allow to import {@link Sequence}
 * from file(s).
 * 
 * @author Stephane
 */

public interface SequenceImporter
{
    /**
     * Launch the importer.<br>
     * The importer is responsible to handle its own UI and should return a {@link Sequence} as
     * result.
     * 
     * @return the loaded {@link Sequence}
     */
    public Sequence load() throws Exception;
}
