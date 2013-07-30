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
package icy.sequence;

import icy.common.EventHierarchicalChecker;
import icy.util.StringUtil;

public class SequenceEvent implements EventHierarchicalChecker
{
    public enum SequenceEventSourceType
    {
        SEQUENCE_TYPE, SEQUENCE_META, SEQUENCE_COLORMAP, SEQUENCE_COMPONENTBOUNDS, SEQUENCE_DATA, SEQUENCE_ROI,
        /**
         * @deprecated
         **/
        @Deprecated
        SEQUENCE_PAINTER, SEQUENCE_OVERLAY
    }

    public enum SequenceEventType
    {
        CHANGED, ADDED, REMOVED
    }

    private final Sequence sequence;
    private final SequenceEventSourceType sourceType;
    private SequenceEventType type;
    private Object source;
    private int param;

    public SequenceEvent(Sequence sequence, SequenceEventSourceType sourceType)
    {
        this(sequence, sourceType, null, SequenceEventType.CHANGED, -1);
    }

    public SequenceEvent(Sequence sequence, SequenceEventSourceType sourceType, Object source)
    {
        this(sequence, sourceType, source, SequenceEventType.CHANGED, -1);
    }

    public SequenceEvent(Sequence sequence, SequenceEventSourceType sourceType, Object source, int param)
    {
        this(sequence, sourceType, source, SequenceEventType.CHANGED, param);
    }

    public SequenceEvent(Sequence sequence, SequenceEventSourceType sourceType, SequenceEventType type)
    {
        this(sequence, sourceType, null, type, -1);
    }

    public SequenceEvent(Sequence sequence, SequenceEventSourceType sourceType, Object source, SequenceEventType type)
    {
        this(sequence, sourceType, source, type, -1);
    }

    public SequenceEvent(Sequence sequence, SequenceEventSourceType sourceType, Object source, SequenceEventType type,
            int param)
    {
        super();

        this.sequence = sequence;
        this.sourceType = sourceType;
        this.source = source;
        this.type = type;
        this.param = param;
    }

    /**
     * @return the sequence
     */
    public Sequence getSequence()
    {
        return sequence;
    }

    /**
     * SourceType define the object type of <code>source<code><br>
     * <br>
     * The following source types are available :<br>
     * <code>SEQUENCE_TYPE</code> --> source object is null<br>
     * <code>SEQUENCE_META</code> --> source object define the meta data id (String)<br>
     * <code>SEQUENCE_COLORMAP</code> --> source object is an instance of IcyColorModel<br>
     * <code>SEQUENCE_COMPONENTBOUNDS</code> --> source object is an instance of IcyColorModel<br>
     * <code>SEQUENCE_DATA</code> --> source object is an instance of IcyBufferedImage<br>
     * source object can be null when severals images has been modified<br>
     * <code>SEQUENCE_ROI</code> --> source object is an instance of ROI<br>
     * source object can be null when severals images has been modified<br>
     * <code>SEQUENCE_OVERLAY</code> --> source object is an instance of Overlay<br>
     * source object can be null when severals images has been modified<br>
     * <code>SEQUENCE_PAINTER</code> --> source object is an instance of Painter<br>
     * source object can be null when severals images has been modified<br>
     * <br>
     */
    public SequenceEventSourceType getSourceType()
    {
        return sourceType;
    }

    /**
     * Source object of the event.<br>
     * The object type here depend of the <code>sourceType</code> value.<br>
     */
    public Object getSource()
    {
        return source;
    }

    /**
     * Type define the type of event.<br>
     * <br>
     * When <code>sourceType</code> is one of the following :<br>
     * <code>SEQUENCE_TYPE, SEQUENCE_META, SEQUENCE_COLORMAP, SEQUENCE_COMPONENTBOUNDS</code><br>
     * the type can only be <code>SequenceEventType.CHANGED</code><br>
     * <br>
     * When <code>sourceType</code> is one of the following :<br>
     * <code>SEQUENCE_DATA, SEQUENCE_ROI, SEQUENCE_PAINTER, SEQUENCE_OVERLAY</code><br>
     * the type can also be <code>SequenceEventType.ADDED</code> or
     * <code>SequenceEventType.REMOVED</code><br>
     * That mean a specific image, roi or painter (if <code>source != null</code>) has been added or
     * removed from the sequence.<br>
     * If <code>source == null</code> that mean we have a global change event and some stuff need to
     * be recalculated.<br>
     * Severals ADDED / CHANGED / REMOVE events can be compacted to one CHANGED event with a null
     * source (global change) for SEQUENCE_DATA source type.
     */
    public SequenceEventType getType()
    {
        return type;
    }

    /**
     * Extra parameter of event.<br>
     * <br>
     * It's used to specify the component number when <code>sourceType</code> is
     * <code>SEQUENCE_COLORMAP</code> or <code>SEQUENCE_COMPONENTBOUNDS</code> (in both case source
     * is instance of <code>IcyColorModel</code>).<br>
     * Also used internally...
     */
    public int getParam()
    {
        return param;
    }

    /**
     * Collapse event
     */
    private boolean collapseWith(SequenceEvent e)
    {
        // same source type
        if (e.getSourceType() == sourceType)
        {
            switch (sourceType)
            {
                case SEQUENCE_META:
                    if (StringUtil.equals((String) e.getSource(), (String) source))
                        return true;

                case SEQUENCE_COLORMAP:
                case SEQUENCE_COMPONENTBOUNDS:
                    // join events in one global event
                    if (e.getParam() != param)
                        param = -1;
                    return true;

                case SEQUENCE_DATA:
                    // optimize different type event to a single CHANGED event (for DATA only)
                    if (e.getType() != type)
                        type = SequenceEventType.CHANGED;
                    if (e.getSource() != source)
                        source = null;
                    return true;

                case SEQUENCE_PAINTER:
                case SEQUENCE_OVERLAY:
                case SEQUENCE_ROI:
                    // same type ?
                    if (e.getType() == type)
                    {
                        // join events in one global event
                        if (e.getSource() != source)
                            source = null;
                        return true;
                    }
                    break;

                case SEQUENCE_TYPE:
                    return true;
            }
        }

        return false;
    }

    @Override
    public boolean isEventRedundantWith(EventHierarchicalChecker event)
    {
        if (event instanceof SequenceEvent)
            return collapseWith((SequenceEvent) event);

        return false;
    }
}
