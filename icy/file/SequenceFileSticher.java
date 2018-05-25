/*
 * Copyright 2010-2018 Institut Pasteur.
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
package icy.file;

import icy.gui.frame.progress.FileFrame;
import icy.sequence.DimensionId;
import icy.sequence.MetaDataUtil;
import icy.type.DataType;
import icy.util.StringUtil;
import icy.util.StringUtil.AlphanumComparator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import ome.xml.meta.OMEXMLMetadata;
import plugins.kernel.importer.LociImporterPlugin;

/**
 * This class is an utility class aim to help in grouping a list of <i>file path</id> representing image to form a complete and valid Sequence.
 * 
 * @author Stephane
 */
public class SequenceFileSticher
{
    public static class SequenceType
    {
        public int sizeX;
        public int sizeY;
        public int sizeZ;
        public int sizeT;
        public int sizeC;
        public DataType dataType;
        public double pixelSizeX;
        public double pixelSizeY;
        public double pixelSizeZ;
        public double timeInterval;

        // internal
        int hc;

        public SequenceType()
        {
            super();

            // undetermined
            sizeX = 0;
            sizeY = 0;
            sizeZ = 0;
            sizeT = 0;
            sizeC = 0;
            dataType = null;
            pixelSizeX = 0d;
            pixelSizeY = 0d;
            pixelSizeZ = 0d;
            timeInterval = 0d;
        }

        void computeHashCode()
        {
            hc = (sizeX << 0) ^ (sizeY << 4) ^ (sizeZ << 8) ^ (sizeT << 12) ^ (sizeC << 16)
                    ^ (Float.floatToIntBits((float) pixelSizeX) << 20)
                    ^ (Float.floatToIntBits((float) pixelSizeY) << 24) ^ (Float.floatToIntBits((float) pixelSizeZ) >> 4)
                    ^ (Float.floatToIntBits((float) timeInterval) >> 8)
                    ^ ((dataType != null) ? (dataType.ordinal() >> 12) : 0);
        }

        @Override
        public int hashCode()
        {
            return hc;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (obj instanceof SequenceType)
            {
                final SequenceType st = (SequenceType) obj;

                return (st.sizeX == sizeX) && (st.sizeY == sizeY) && (st.sizeZ == sizeZ) && (st.sizeT == sizeT)
                        && (st.sizeC == sizeC) && (st.pixelSizeX == pixelSizeX) && (st.pixelSizeY == pixelSizeY)
                        && (st.pixelSizeZ == pixelSizeZ) && (st.timeInterval == timeInterval)
                        && (st.dataType == dataType);
            }

            return super.equals(obj);
        }
    }

    public static class SequenceIdent
    {
        /**
         * base path pattern (identical part of the path in this group)
         */
        public final String base;
        /**
         * Series index for this group
         */
        public final int series;
        /**
         * base image type for this group (correspond to the type of each image)
         */
        public final SequenceType baseType;
        /**
         * Compatible importer capable of loading this image group
         */
        public final SequenceFileImporter importer;

        private final int hc;

        public SequenceIdent(String base, int series, SequenceType type, SequenceFileImporter importer)
        {
            super();

            this.base = base;
            this.series = series;
            this.baseType = type;
            this.importer = importer;

            hc = base.hashCode() ^ series;
        }

        public SequenceIdent(String base, int series)
        {
            this(base, series, null, null);
        }

        @Override
        public int hashCode()
        {
            return hc;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (obj instanceof SequenceIdent)
            {
                final SequenceIdent ident = (SequenceIdent) obj;

                boolean result = base.equals(ident.base) && (series == ident.series);

                // test baseType only if defined
                if (result && (baseType != null) && (ident.baseType != null))
                    result &= baseType.equals(ident.baseType);

                return result;
            }

            return super.equals(obj);
        }
    }

    public static class SequenceAbsolutePosition implements Comparable<SequenceAbsolutePosition>
    {
        // absolute position from metadata
        public double posX;
        public double posY;
        public double posZ;
        public double posT;
        public double indX;
        public double indY;
        public double indZ;
        public double indT;

        // internal
        protected int hc;

        public SequenceAbsolutePosition()
        {
            super();

            // undetermined
            posX = -1d;
            posY = -1d;
            posZ = -1d;
            posT = -1d;
            indX = -1d;
            indY = -1d;
            indZ = -1d;
            indT = -1d;
        }

        /**
         * Set index X from absolute position and sequence properties (pixel size, dimension)
         */
        public void setIndexX(SequenceType type)
        {
            // -1 mean not set
            if (posX != -1d)
            {
                // get pixel position if possible
                final double pixPos;

                if (type.pixelSizeX > 0d)
                    // we use * 2 to avoid index duplication because of rounding, anyway we will fix interval later
                    pixPos = (2d * posX) / type.pixelSizeX;
                else
                    // use absolute position as pixel pos
                    pixPos = posX;

                // index = (pixel position) / (image size)
                indX = Math.round(pixPos / type.sizeX);
            }
        }

        /**
         * Set index X from absolute position and sequence properties (pixel size, dimension)
         */
        public void setIndexY(SequenceType type)
        {
            // -1 mean not set
            if (posY != -1d)
            {
                // get pixel position if possible
                final double pixPos;

                if (type.pixelSizeY > 0d)
                    // we use * 2 to avoid index duplication because of rounding, anyway we will fix interval later
                    pixPos = (2d * posY) / type.pixelSizeY;
                else
                    // use absolute position as pixel pos
                    pixPos = posY;

                // index = (pixel position) / (image size)
                indY = Math.round(pixPos / type.sizeY);
            }
        }

        /**
         * Set index X from absolute position and sequence properties (pixel size, dimension)
         */
        public void setIndexZ(SequenceType type)
        {
            // -1 mean not set
            if (posZ != -1d)
            {
                // get pixel position if possible
                final double pixPos;

                if (type.pixelSizeZ > 0d)
                    // we use * 2 to avoid index duplication because of rounding, anyway we will fix interval later
                    pixPos = (2d * posZ) / type.pixelSizeZ;
                else
                    // use absolute position as pixel pos
                    pixPos = posZ;

                // index = (pixel position) / (image size)
                indZ = Math.round(pixPos / type.sizeZ);
            }
        }

        /**
         * Set index T from absolute time position and sequence properties (pixel size, dimension)
         */
        public void setIndexT(SequenceType type)
        {
            // -1 mean not set
            if (posT != -1d)
            {
                // get time position in second if possible
                final double timePos;

                if (type.timeInterval > 0d)
                    // we use * 2 to avoid index duplication because of rounding, anyway we will fix interval later
                    timePos = (2d * posT) / type.timeInterval;
                else
                    // use absolute position as time pos
                    timePos = posT;

                // index = (time position) / (image size)
                indT = Math.round(timePos / type.sizeT);
            }
        }

        public void clearIndX()
        {
            indX = -1d;
        }

        public void clearIndY()
        {
            indY = -1d;
        }

        public void clearIndZ()
        {
            indZ = -1d;
        }

        public void clearIndT()
        {
            indT = -1d;
        }

        public DimensionId getDifference(SequenceAbsolutePosition sap)
        {
            if (compare(indT, sap.indT) != 0)
                return DimensionId.T;
            if (compare(indZ, sap.indZ) != 0)
                return DimensionId.Z;
            if (compare(indY, sap.indY) != 0)
                return DimensionId.Y;
            if (compare(indX, sap.indX) != 0)
                return DimensionId.X;

            return null;
        }

        public void computeHashCode()
        {
            hc = Float.floatToIntBits((float) indX) ^ (Float.floatToIntBits((float) indY) << 8)
                    ^ (Float.floatToIntBits((float) indZ) << 16) ^ (Float.floatToIntBits((float) indT) << 24)
                    ^ Float.floatToIntBits((float) posX) ^ (Float.floatToIntBits((float) posY) >> 8)
                    ^ (Float.floatToIntBits((float) posZ) >> 16) ^ (Float.floatToIntBits((float) posT) >> 24);
        }

        @Override
        public int hashCode()
        {
            return hc;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (obj instanceof SequenceAbsolutePosition)
            {
                final SequenceAbsolutePosition sap = (SequenceAbsolutePosition) obj;

                return (sap.indX == indX) && (sap.indY == indY) && (sap.indZ == indZ) && (sap.indT == indT);
            }

            return super.equals(obj);
        }

        @Override
        public int compareTo(SequenceAbsolutePosition sap)
        {
            int result = compare(indT, sap.indT);
            if (result == 0)
                result = compare(indZ, sap.indZ);
            if (result == 0)
                result = compare(indY, sap.indY);
            if (result == 0)
                result = compare(indX, sap.indX);

            return result;
        }

    }

    public static class SequenceIndexPosition implements Comparable<SequenceIndexPosition>
    {
        public int x;
        public int y;
        public int z;
        public int t;
        public int c;

        public SequenceIndexPosition()
        {
            super();

            // undetermined
            x = -1;
            y = -1;
            z = -1;
            t = -1;
            c = -1;
        }

        public DimensionId getDifference(SequenceIndexPosition sip)
        {
            if (SequenceFileSticher.compare(t, sip.t) != 0)
                return DimensionId.T;
            if (SequenceFileSticher.compare(z, sip.z) != 0)
                return DimensionId.Z;
            if (SequenceFileSticher.compare(c, sip.c) != 0)
                return DimensionId.C;
            if (SequenceFileSticher.compare(y, sip.y) != 0)
                return DimensionId.Y;
            if (SequenceFileSticher.compare(x, sip.x) != 0)
                return DimensionId.X;

            return null;
        }

        @Override
        public int hashCode()
        {
            return x ^ (y << 6) ^ (z << 12) ^ (t << 18) ^ (c << 24);
        }

        @Override
        public boolean equals(Object obj)
        {
            if (obj instanceof SequenceIndexPosition)
            {
                final SequenceIndexPosition sip = (SequenceIndexPosition) obj;

                return (sip.x == x) && (sip.y == y) && (sip.z == z) && (sip.t == t) && (sip.c == c);
            }

            return super.equals(obj);
        }

        public int compare(SequenceIndexPosition sip)
        {
            int result = 0;

            if (result == 0)
                result = SequenceFileSticher.compare(t, sip.t);
            if (result == 0)
                result = SequenceFileSticher.compare(z, sip.z);
            if (result == 0)
                result = SequenceFileSticher.compare(c, sip.c);
            if (result == 0)
                result = SequenceFileSticher.compare(y, sip.y);
            if (result == 0)
                result = SequenceFileSticher.compare(x, sip.x);

            return result;
        }

        @Override
        public int compareTo(SequenceIndexPosition sip)
        {
            return compare(sip);
        }
    }

    public static class SequencePosition implements Comparable<SequencePosition>
    {
        // /**
        // * file path
        // */
        // final String path;
        //
        // /**
        // * importer for this path
        // */
        // SequenceFileImporter importer;
        // /**
        // * metadata for this path
        // */
        // OMEXMLMetadata metadata;

        // /**
        // * Absolute position
        // */
        // final SequenceAbsolutePosition absPos;

        /**
         * index position
         */
        final SequenceIndexPosition indPos;

        // /**
        // * size & type info
        // */
        // final SequenceType type;

        /**
         * helper to find position
         */
        FilePosition filePosition;

        // public SequencePosition(String path)
        // {
        // super();
        //
        // // type = new SequenceType();
        // filePosition = new FilePosition(path);
        // indPos = new SequenceIndexPosition();
        //
        // // not affected
        // // absPos = new SequenceAbsolutePosition();
        // // importer = null;
        // // metadata = null;
        // }

        public SequencePosition(FilePosition filePosition)
        {
            super();

            // type = new SequenceType();
            this.filePosition = filePosition;
            indPos = new SequenceIndexPosition();

            // not affected
            // absPos = new SequenceAbsolutePosition();
            // importer = null;
            // metadata = null;
        }

        public String getBase()
        {
            return filePosition.base;
        }

        public String getPath()
        {
            return filePosition.path;
        }

        public int getIndexS()
        {
            int result = filePosition.getValue(DimensionId.NULL);

            // if not defined then series = 0
            if (result == -1)
                result = 0;

            return result;
        }

        public int getIndexX()
        {
            if (indPos.x != -1)
                return indPos.x;

            // default
            return 0;
        }

        public int getIndexY()
        {
            if (indPos.y != -1)
                return indPos.x;

            // default
            return 0;
        }

        public int getIndexC()
        {
            if (indPos.c != -1)
                return indPos.c;

            // default
            return 0;
        }

        public int getIndexZ()
        {
            if (indPos.z != -1)
                return indPos.z;

            // default
            return 0;
        }

        public int getIndexT()
        {
            if (indPos.t != -1)
                return indPos.t;

            // default
            return 0;
        }

        // public int getSizeZ()
        // {
        // if (type.sizeZ != -1)
        // return type.sizeZ;
        //
        // // default
        // return 1;
        // }
        //
        // public int getSizeT()
        // {
        // if (type.sizeT != -1)
        // return type.sizeT;
        //
        // // default
        // return 1;
        // }
        //
        // public int getSizeC()
        // {
        // if (type.sizeC != -1)
        // return type.sizeC;
        //
        // // default
        // return 1;
        // }

        public int compareSeries(SequencePosition sp)
        {
            return filePosition.compareSeries(sp.filePosition);
        }

        public DimensionId getDifference(SequencePosition sp)
        {
            // not the same series (always compare first)
            if (compareSeries(sp) != 0)
                return DimensionId.NULL;

            // DimensionId result = absPos.getDifference(sp.absPos);
            // if (result == null)
            DimensionId result = filePosition.getDifference(sp.filePosition, false);
            if (result == null)
                result = indPos.getDifference(indPos);

            return result;
        }

        @Override
        public int compareTo(SequencePosition sp)
        {
            int result = compareSeries(sp);
            if (result == 0)
                // result = absPos.compareTo(sp.absPos);
                // if (result == 0)
                result = filePosition.compare(sp.filePosition, false);
            if (result == 0)
                result = indPos.compare(sp.indPos);

            return result;
        }

        @Override
        public String toString()
        {
            return "Path=" + getPath() + " Position=[S:" + getIndexS() + " T:" + getIndexT() + " Z:" + getIndexZ()
                    + " C:" + getIndexC() + " Y:" + getIndexY() + " X:" + getIndexX() + "]";
        }
    }

    /**
     * Class used to build a FilePosition from an <i>path</i>
     * 
     * @author Stephane
     */
    public static class FilePosition implements Comparable<FilePosition>
    {
        /**
         * Class representing a position for a specific dimension.
         * 
         * @author Stephane
         */
        private static class PositionChunk
        {
            /** X dimension prefixes */
            static final String[] prefixesX = {"x", "xpos", "posx", "xposition", "positionx"};

            /** Y dimension prefixes */
            static final String[] prefixesY = {"y", "ypos", "posy", "yposition", "positiony"};

            /** Depth (Z) dimension prefixes (taken from Bio-Formats for almost) */
            static final String[] prefixesZ = {"fp", "sec", "z", "zs", "plane", "focal", "focalplane"};

            /** Time (T) dimension prefixes (taken from Bio-Formats for almost) */
            static final String[] prefixesT = {"t", "tl", "tp", "time", "frame"};

            /** Channel (C) dimension prefixes (taken from Bio-Formats for almost) */
            static final String[] prefixesC = {"c", "ch", "channel", "b", "band", "w", "wl", "wave", "wavelength"};

            /** Series (S)dimension prefixes (taken from Bio-Formats for almost) */
            static final String[] prefixesS = {"s", "series", "sp", "f", "field"};

            public DimensionId dim;
            public int value;

            PositionChunk(String prefix, int value)
            {
                super();

                dim = null;
                if (!StringUtil.isEmpty(prefix))
                {
                    final String prefixLC = prefix.toLowerCase();

                    if (dim == null)
                        dim = getDim(prefixLC, prefixesX, DimensionId.X);
                    if (dim == null)
                        dim = getDim(prefixLC, prefixesY, DimensionId.Y);
                    if (dim == null)
                        dim = getDim(prefixLC, prefixesZ, DimensionId.Z);
                    if (dim == null)
                        dim = getDim(prefixLC, prefixesT, DimensionId.T);
                    if (dim == null)
                        dim = getDim(prefixLC, prefixesC, DimensionId.C);
                    if (dim == null)
                        dim = getDim(prefixLC, prefixesS, DimensionId.NULL);
                }

                this.value = value;
            }

            private static DimensionId getDim(String prefix, String prefixes[], DimensionId d)
            {
                for (String p : prefixes)
                    if (prefix.endsWith(p))
                        return d;

                return null;
            }
        }

        final String path;
        final String base;
        final List<PositionChunk> chunks;

        FilePosition(String path)
        {
            super();

            this.path = path;
            this.base = getBase(path);

            chunks = new ArrayList<PositionChunk>();

            build();
        }

        private void build()
        {
            // we need to extract position from filename (not from the complete path)
            final String name = FileUtil.getFileName(path);
            final int len = name.length();

            // int value;
            int index = 0;
            while (index < len)
            {
                // get starting digit char index
                final int startInd = StringUtil.getNextDigitCharIndex(name, index);

                // we find a digit char ?
                if (startInd >= 0)
                {
                    // get ending digit char index
                    int endInd = StringUtil.getNextNonDigitCharIndex(name, startInd);
                    if (endInd < 0)
                        endInd = len;

                    // add number only if < 100000 (else it can be a date or id...)
                    // if ((endInd - startInd) < 6)
                    // {
                    // get prefix
                    final String prefix = getPositionPrefix(name, startInd - 1);
                    // get value
                    final int value = StringUtil.parseInt(name.substring(startInd, endInd), -1);

                    // add the position info
                    addChunk(prefix, value);
                    // }

                    // adjust index
                    index = endInd;
                }
                else
                    index = len;
            }
        }

        private static String getBase(String path)
        {
            final String folder = FileUtil.getDirectory(path, true);

            // we extract position from filename (not from the complete path)
            String result = FileUtil.getFileName(path);
            int pos = 0;

            while (pos < result.length())
            {
                final int st = StringUtil.getNextDigitCharIndex(result, pos);

                if (st != -1)
                {
                    // get ending digit char index
                    int end = StringUtil.getNextNonDigitCharIndex(result, st);
                    if (end < 0)
                        end = result.length();
                    // final int size = end - st;

                    // remove number from name if number size < 6
                    // if (size < 6)
                    result = result.substring(0, st) + result.substring(end);
                    // pass to next
                    // else
                    // pos = end;
                }
                else
                    // done
                    break;
            }

            return folder + result;
        }

        private static String getPositionPrefix(String text, int ind)
        {
            if ((ind >= 0) && (ind < text.length()))
            {
                // we have a letter at this position
                if (Character.isLetter(text.charAt(ind)))
                    // get complete prefix
                    return text.substring(StringUtil.getPreviousNonLetterCharIndex(text, ind) + 1, ind + 1);
            }

            return "";
        }

        private void addChunk(String prefix, int value)
        {
            final PositionChunk chunk = new PositionChunk(prefix, value);
            // get the previous chunk for this dimension
            final PositionChunk previousChunk = getChunk(chunk.dim, false);

            // // already have a chunk for this dimension ? --> remove it (keep last found)
            // if (previousChunk != null)
            // removeChunk(previousChunk);
            // already have a chunk for this dimension --> detach its from current dim (keep last found)
            if (previousChunk != null)
                previousChunk.dim = null;

            // add the chunk
            chunks.add(chunk);
        }

        private boolean removeChunk(PositionChunk chunk)
        {
            return chunks.remove(chunk);
        }

        boolean removeChunk(DimensionId dim)
        {
            return removeChunk(getChunk(dim, true));
        }

        public int getValue(DimensionId dim)
        {
            final PositionChunk chunk = getChunk(dim, true);

            if (chunk != null)
                return chunk.value;

            // -1 --> dimension not affected
            return -1;
        }

        boolean isUnknowDim(DimensionId dim)
        {
            return getChunk(dim, false) == null;
        }

        public PositionChunk getChunk(DimensionId dim, boolean allowUnknown)
        {
            if (dim != null)
            {
                for (PositionChunk chunk : chunks)
                    if (chunk.dim == dim)
                        return chunk;

                if (allowUnknown)
                    return getChunkFromUnknown(dim);
            }

            return null;
        }

        /**
         * Try to attribute given dimension position from unknown chunk(x).<br>
         * Work only for Z, T and C dimension (unlikely to have unaffected X and Y dimension)
         */
        private PositionChunk getChunkFromUnknown(DimensionId dim)
        {
            final boolean hasCChunk = (getChunk(DimensionId.C, false) != null);
            final boolean hasZChunk = (getChunk(DimensionId.Z, false) != null);
            final boolean hasTChunk = (getChunk(DimensionId.T, false) != null);

            // priority order for affectation: T, Z, C
            switch (dim)
            {
                case Z:
                    // shouldn't happen (Z already affected)
                    if (hasZChunk)
                        return null;

                    // T chunk present --> Z = unknown[0]
                    if (hasTChunk)
                        return getUnknownChunk(0);

                    // T chunk not present --> T = unknown[0]; Z = unknown[1]
                    return getUnknownChunk(1);

                case T:
                    // shouldn't happen (T already affected)
                    if (hasTChunk)
                        return null;

                    // T = unknown[0]
                    return getUnknownChunk(0);

                case C:
                    // shouldn't happen (C already affected)
                    if (hasCChunk)
                        return null;

                    if (hasTChunk)
                    {
                        // T and Z chunk present --> C = unknown[0]
                        if (hasZChunk)
                            return getUnknownChunk(0);

                        // T chunk present --> Z = unknown[0]; C = unknown[1]
                        return getUnknownChunk(1);
                    }
                    // Z chunk present --> T = unknown[0]; C = unknown[1]
                    else if (hasZChunk)
                        return getUnknownChunk(1);

                    // no other chunk present --> T = unknown[0]; Z = unknown[1]; C = unknown[2]
                    return getUnknownChunk(2);
            }

            return null;
        }

        private PositionChunk getUnknownChunk(int i)
        {
            int ind = 0;

            for (PositionChunk chunk : chunks)
            {
                if (chunk.dim == null)
                {
                    if (ind == i)
                        return chunk;

                    ind++;
                }
            }

            return null;
        }

        int getUnknownChunkCount()
        {
            int result = 0;

            for (PositionChunk chunk : chunks)
                if (chunk.dim == null)
                    result++;

            return result;
        }

        public int compareSeries(FilePosition ipb)
        {
            int result = 0;
            final String bn1 = base;
            final String bn2 = ipb.base;

            // can compare on base name ?
            if (!StringUtil.isEmpty(bn1) && !StringUtil.isEmpty(bn2))
                result = bn1.compareTo(bn2);

            // compare on series path position
            if (result == 0)
                result = SequenceFileSticher.compare(getValue(DimensionId.NULL), ipb.getValue(DimensionId.NULL));

            return result;
        }

        public int compare(FilePosition ipb, boolean compareSeries)
        {
            int result = 0;

            // always compare series first
            if (compareSeries)
                result = compareSeries(ipb);

            if (result == 0)
                result = SequenceFileSticher.compare(getValue(DimensionId.T), ipb.getValue(DimensionId.T));
            if (result == 0)
                result = SequenceFileSticher.compare(getValue(DimensionId.Z), ipb.getValue(DimensionId.Z));
            if (result == 0)
                result = SequenceFileSticher.compare(getValue(DimensionId.C), ipb.getValue(DimensionId.C));
            if (result == 0)
                result = SequenceFileSticher.compare(getValue(DimensionId.Y), ipb.getValue(DimensionId.Y));
            if (result == 0)
                result = SequenceFileSticher.compare(getValue(DimensionId.X), ipb.getValue(DimensionId.X));

            return result;
        }

        public DimensionId getDifference(FilePosition ipb, boolean compareSeries)
        {
            if (compareSeries)
            {
                // always compare series first
                if (compareSeries(ipb) != 0)
                    return DimensionId.NULL;
            }

            if (SequenceFileSticher.compare(getValue(DimensionId.T), ipb.getValue(DimensionId.T)) != 0)
                return DimensionId.T;
            if (SequenceFileSticher.compare(getValue(DimensionId.Z), ipb.getValue(DimensionId.Z)) != 0)
                return DimensionId.Z;
            if (SequenceFileSticher.compare(getValue(DimensionId.C), ipb.getValue(DimensionId.C)) != 0)
                return DimensionId.C;
            if (SequenceFileSticher.compare(getValue(DimensionId.Y), ipb.getValue(DimensionId.Y)) != 0)
                return DimensionId.Y;
            if (SequenceFileSticher.compare(getValue(DimensionId.X), ipb.getValue(DimensionId.X)) != 0)
                return DimensionId.X;

            return null;
        }

        @Override
        public int compareTo(FilePosition ipb)
        {
            return compare(ipb, false);
        }

        @Override
        public String toString()
        {
            return "FilePosition [S:" + getValue(DimensionId.NULL) + " C:" + getValue(DimensionId.C) + " T:"
                    + getValue(DimensionId.T) + " Z:" + getValue(DimensionId.Z) + " Y:" + getValue(DimensionId.Y)
                    + " X:" + getValue(DimensionId.X) + "]";
        }
    }

    public static class SequenceFileGroup
    {
        public final SequenceIdent ident;
        public final List<SequencePosition> positions;

        // final sequence dimension
        public int totalSizeX;
        public int totalSizeY;
        public int totalSizeZ;
        public int totalSizeT;
        public int totalSizeC;

        /**
         * Internal use only, use {@link SequenceFileSticher#groupFiles(SequenceFileImporter, Collection, boolean, FileFrame)} instead.
         */
        public SequenceFileGroup(SequenceIdent ident)
        {
            super();

            this.ident = ident;
            positions = new ArrayList<SequencePosition>();

            // we will compute them with buildIndexesFromPositions
            totalSizeX = 0;
            totalSizeY = 0;
            totalSizeZ = 0;
            totalSizeT = 0;
            totalSizeC = 0;
        }

        // void cleanFixedAbsPos()
        // {
        // // nothing to do
        // if (positions.isEmpty())
        // return;
        //
        // final SequencePosition fpos = positions.get(0);
        //
        // // init
        // double indX = fpos.absPos.indX;
        // double indY = fpos.absPos.indY;
        // double indZ = fpos.absPos.indZ;
        // double indT = fpos.absPos.indT;
        // boolean posXChanged = false;
        // boolean posYChanged = false;
        // boolean posZChanged = false;
        // boolean posTChanged = false;
        //
        // for (int i = 1; i < positions.size(); i++)
        // {
        // final SequencePosition pos = positions.get(i);
        //
        // if (indX != pos.absPos.indX)
        // posXChanged = true;
        // if (indY != pos.absPos.indY)
        // posYChanged = true;
        // if (indZ != pos.absPos.indZ)
        // posZChanged = true;
        // if (indT != pos.absPos.indT)
        // posTChanged = true;
        // }
        //
        // for (SequencePosition pos : positions)
        // {
        // // fixed X position --> useless
        // if (!posXChanged)
        // pos.absPos.clearIndX();
        // // fixed Y position --> useless
        // if (!posYChanged)
        // pos.absPos.clearIndY();
        // // fixed Z position --> useless
        // if (!posZChanged)
        // pos.absPos.clearIndZ();
        // // fixed T position --> useless
        // if (!posTChanged)
        // pos.absPos.clearIndT();
        // }
        // }

        void checkZTDimIdPos()
        {
            final boolean zMulti = ident.baseType.sizeZ > 1;
            final boolean tMulti = ident.baseType.sizeT > 1;

            // determine if we need to swap out Z and T position (if only one of the dimension can move)
            if (tMulti ^ zMulti)
            {
                boolean tSet = false;
                boolean tCanChange = true;
                boolean zSet = false;
                boolean zCanChange = true;

                for (SequencePosition pos : positions)
                {
                    final FilePosition idPos = pos.filePosition;

                    if (idPos != null)
                    {
                        if (idPos.getValue(DimensionId.T) != -1)
                            tSet = true;
                        if (idPos.getValue(DimensionId.Z) != -1)
                            zSet = true;

                        if (!idPos.isUnknowDim(DimensionId.T))
                            tCanChange = false;
                        if (!idPos.isUnknowDim(DimensionId.Z))
                            zCanChange = false;
                    }
                }

                // Z and T position are not fixed, and one of the dimension , try to swap if possible
                if (tCanChange && zCanChange)
                {
                    boolean swapZT = false;

                    // multi T but single Z
                    if (tMulti)
                    {
                        // T position set but can be swapped with Z
                        if (tSet && tCanChange && !zSet)
                            swapZT = true;
                    }
                    else
                    // multi Z but single T
                    {
                        // Z position set but can be swapped with T
                        if (zSet && zCanChange && !tSet)
                            swapZT = true;
                    }

                    // swap T and Z dimension
                    if (swapZT)
                    {
                        for (SequencePosition pos : positions)
                        {
                            final FilePosition idPos = pos.filePosition;

                            if (idPos != null)
                            {
                                final FilePosition.PositionChunk zChunk = idPos.getChunk(DimensionId.Z, true);
                                final FilePosition.PositionChunk tChunk = idPos.getChunk(DimensionId.T, true);

                                // swap dim
                                if (zChunk != null)
                                    zChunk.dim = DimensionId.T;
                                if (tChunk != null)
                                    tChunk.dim = DimensionId.Z;
                            }
                        }
                    }
                }
            }
        }

        void buildIndexesAndSizesFromPositions(boolean findPosition)
        {
            final int size = positions.size();

            // nothing to do
            if (size <= 0)
                return;

            final SequenceType baseType = ident.baseType;

            // store final sequence dimension
            final int sc = baseType.sizeC;
            final int st = baseType.sizeT;
            final int sz = baseType.sizeZ;
            final int sy = baseType.sizeY;
            final int sx = baseType.sizeX;

            // compact indexes
            int t = 0;
            int z = 0;
            int c = 0;
            int y = 0;
            int x = 0;
            int mt = 0;
            int mz = 0;
            int mc = 0;
            int my = 0;
            int mx = 0;

            SequencePosition previous = positions.get(0);
            SequenceIndexPosition indPos = previous.indPos;

            indPos.t = t;
            indPos.z = z;
            indPos.c = c;
            indPos.y = y;
            indPos.x = x;

            for (int i = 1; i < size; i++)
            {
                final SequencePosition current = positions.get(i);
                DimensionId diff = null;

                // if we don't want real position we just use T ordering
                if (findPosition)
                    diff = previous.getDifference(current);

                // default = T dimension
                if (diff == null)
                    diff = DimensionId.T;

                // base path changed
                switch (diff)
                {
                    // // series position change (shouldn't arrive, group are here for that)
                    // case NULL:
                    // s++;
                    // // keep maximum
                    // ms = Math.max(ms, s);
                    // // reset others indexes
                    // t = 0;
                    // z = 0;
                    // c = 0;
                    // y = 0;
                    // x = 0;
                    // break;

                    // T position changed (default case)
                    case T:
                    default:
                        t += st;
                        // keep maximum
                        mt = Math.max(mt, t);
                        // reset others indexes
                        z = 0;
                        c = 0;
                        y = 0;
                        x = 0;
                        break;

                    // Z position changed
                    case Z:
                        z += sz;
                        // keep maximum
                        mz = Math.max(mz, z);
                        // reset others indexes
                        c = 0;
                        y = 0;
                        x = 0;
                        break;

                    // C position changed
                    case C:
                        c += sc;
                        // keep maximum
                        mc = Math.max(mc, c);
                        // reset others indexes
                        y = 0;
                        x = 0;
                        break;

                    // Y position changed
                    case Y:
                        y++;
                        // keep maximum
                        my = Math.max(my, y);
                        // reset others indexes
                        x = 0;
                        break;

                    // X position changed
                    case X:
                        x++;
                        // keep maximum
                        mx = Math.max(mx, x);
                        break;
                }

                // update current position
                indPos = current.indPos;
                indPos.t = t;
                indPos.z = z;
                indPos.c = c;
                indPos.y = y;
                indPos.x = x;

                // keep trace of last position
                previous = current;
            }

            // we want size for each dimension
            mt++;
            mz++;
            mc++;
            my++;
            mx++;

            // normally we want the equality here
            if ((mt * mz * mc * my * mx) != size)
            {
                System.err.println("Warning: SequenceFileSticher - number of image doesn't match: " + size
                        + " (expected = " + (mt * mz * mc * my * mx) + ")");
            }

            // store final sequence dimension
            totalSizeC = mc * sc;
            totalSizeT = mt * st;
            totalSizeZ = mz * sz;
            totalSizeY = my * sy;
            totalSizeX = mx * sx;
        }

        /**
         * Return all contained path in this group
         */
        public List<String> getPaths()
        {
            final List<String> results = new ArrayList<String>();

            for (SequencePosition pos : positions)
                results.add(pos.getPath());

            return results;
        }

    }

    /**
     * Same as {@link SequenceFileSticher#groupFiles(SequenceFileImporter, Collection, boolean, FileFrame)} except it does several groups if all image file path
     * cannot be grouped to form a single Sequence.<br>
     * The grouping is done using the path name information (recognizing and parsing specific patterns in the path) and assume file shares the same properties
     * (dimensions).<br>
     * The method returns a set of {@link SequenceFileGroup} where each group define a Sequence.<br>
     * 
     * @param importer
     *        {@link SequenceFileImporter} to use to open image.<br>
     *        If set to <i>null</i> the method automatically try to find a compatible {@link SequenceFileImporter}.
     * @param paths
     *        image file paths we want to group
     * @param findPosition
     *        if true we try to determine the X, Y, Z, T and C image position otherwise a simple ascending T ordering is done
     * @param loadingFrame
     *        Loading dialog if any to show progress
     * @see #groupFiles(SequenceFileImporter, Collection, boolean, FileFrame)
     */
    public static Collection<SequenceFileGroup> groupAllFiles(SequenceFileImporter importer, Collection<String> paths,
            boolean findPosition, FileFrame loadingFrame)
    {
        final List<String> sortedPaths = Loader.cleanNonImageFile(new ArrayList<String>(paths));

        if (sortedPaths.isEmpty())
            return new ArrayList<SequenceFileGroup>();

        // final List<FilePosition> filePositions = new ArrayList<FilePosition>();

        if (loadingFrame != null)
            loadingFrame.setAction("Sort paths...");

        // sort paths on name using smart sorter
        if (sortedPaths.size() > 1)
            Collections.sort(sortedPaths, new AlphanumComparator());

        // we do a 1st pass to build all FilePosition
        if (loadingFrame != null)
            loadingFrame.setAction("Extracting positions from paths...");

        // group FilePosition by 'base' path
        final Map<String, List<FilePosition>> pathPositionsMap = new HashMap<String, List<FilePosition>>();

        // build FilePosition
        for (String path : sortedPaths)
        {
            final FilePosition filePosition = new FilePosition(path);
            final String base = filePosition.base;

            // we want to group by 'base' path
            List<FilePosition> positions = pathPositionsMap.get(base);

            // list not yet created ?
            if (positions == null)
            {
                // create and add it
                positions = new ArrayList<FilePosition>();
                pathPositionsMap.put(base, positions);
            }

            // add it
            positions.add(filePosition);
            // // add it to global list as well
            // filePositions.add(filePosition);
        }

        final Map<SequenceIdent, SequenceFileGroup> result = new HashMap<SequenceIdent, SequenceFileGroup>();

        // clean FilePosition grouped by base path and add them to group
        for (Entry<String, List<FilePosition>> entry : pathPositionsMap.entrySet())
        {
            // get positions
            final List<FilePosition> positions = entry.getValue();

            // remove position information which never change
            while (cleanPositions(positions, DimensionId.NULL))
                ;
            while (cleanPositions(positions, DimensionId.T))
                ;
            while (cleanPositions(positions, DimensionId.Z))
                ;
            while (cleanPositions(positions, DimensionId.C))
                ;
            while (cleanPositions(positions, DimensionId.Y))
                ;
            while (cleanPositions(positions, DimensionId.X))
                ;

            // add position to group(s)
            for (FilePosition pos : positions)
                addToGroup(result, new SequencePosition(pos), importer);
        }

        /*
         * if (loadingFrame != null)
         * loadingFrame.setAction("Get positions information from metadata...");
         * 
         * SequenceFileImporter imp = importer;
         * int indT = 0;
         * 
         * for (int i = 0; i < sortedPaths.size(); i++)
         * {
         * final String path = sortedPaths.get(i);
         * final SequencePosition position = new SequencePosition(path);
         * final SequenceType type = position.type;
         * final SequenceAbsolutePosition absPos = position.absPos;
         * final SequenceIndexPosition indPos = position.indPos;
         * 
         * // try to open the image
         * imp = tryOpen(imp, path);
         * 
         * // correctly opened ?
         * if (imp != null)
         * {
         * try
         * {
         * // get metadata
         * final OMEXMLMetadata meta = imp.getOMEXMLMetaData();
         * 
         * // set type information
         * type.sizeX = MetaDataUtil.getSizeX(meta, 0);
         * type.sizeY = MetaDataUtil.getSizeY(meta, 0);
         * type.sizeZ = MetaDataUtil.getSizeZ(meta, 0);
         * type.sizeT = MetaDataUtil.getSizeT(meta, 0);
         * type.sizeC = MetaDataUtil.getSizeC(meta, 0);
         * type.dataType = MetaDataUtil.getDataType(meta, 0);
         * // use -1 as default value to detect when position is not set
         * type.pixelSizeX = MetaDataUtil.getPixelSizeX(meta, 0, 0d);
         * type.pixelSizeY = MetaDataUtil.getPixelSizeY(meta, 0, 0d);
         * type.pixelSizeZ = MetaDataUtil.getPixelSizeZ(meta, 0, 0d);
         * type.timeInterval = MetaDataUtil.getTimeInterval(meta, 0, 0d);
         * // can compute hash code
         * type.computeHashCode();
         * 
         * if (findPosition)
         * {
         * // use -1 as default value to detect when position is not set
         * absPos.posX = MathUtil.roundSignificant(MetaDataUtil.getPositionX(meta, 0, 0, 0, 0, -1d),
         * 5);
         * absPos.posY = MathUtil.roundSignificant(MetaDataUtil.getPositionY(meta, 0, 0, 0, 0, -1d),
         * 5);
         * absPos.posZ = MathUtil.roundSignificant(MetaDataUtil.getPositionZ(meta, 0, 0, 0, 0, -1d),
         * 5);
         * absPos.posT = MathUtil.roundSignificant(MetaDataUtil.getPositionT(meta, 0, 0, 0, 0, -1d),
         * 5);
         * // try to compute index from absolute and pixel size info
         * absPos.setIndexX(type);
         * absPos.setIndexY(type);
         * absPos.setIndexZ(type);
         * absPos.setIndexT(type);
         * // can compute hash code
         * absPos.computeHashCode();
         * }
         * 
         * // store importer & metadata object
         * position.importer = imp;
         * position.metadata = meta;
         * }
         * catch (Throwable t)
         * {
         * // error while retrieve metadata
         * t.printStackTrace();
         * }
         * finally
         * {
         * try
         * {
         * // close importer
         * imp.close();
         * }
         * catch (IOException e)
         * {
         * // just ignore...
         * }
         * }
         * 
         * // store filePosition in position object
         * if (findPosition)
         * position.filePosition = filePositions.get(i);
         * else
         * {
         * indPos.x = 0;
         * indPos.y = 0;
         * indPos.z = 0;
         * // simple T ordering
         * indPos.t = indT;
         * indPos.c = 0;
         * 
         * // next T position
         * indT += type.sizeT;
         * }
         * 
         * // add to result map (important to have position informations first)
         * addToGroup(result, position);
         * }
         * }
         * 
         */
        if (loadingFrame != null)
            loadingFrame.setAction("Cleanup up positions and rebuilding indexes...");

        // need to improve position informations
        for (SequenceFileGroup group : result.values())
        {
            // // clean absolute positions
            // group.cleanFixedAbsPos();
            // check if we can revert Z and T dimension from FilePosition
            if (findPosition)
            {
                group.checkZTDimIdPos();
                // sort group positions on cleaned up position (S, T, Z, C, Y, X order)
                Collections.sort(group.positions);
            }

            // build final index position from internal position (absolute or path)
            group.buildIndexesAndSizesFromPositions(findPosition);
        }

        return result.values();
    }

    /**
     * Take a list of image file path as input and try to group them to form a unique Sequence.<br>
     * The grouping is done using the path name information (recognizing and parsing specific patterns in the path) and assume file shares the same properties
     * (dimensions).<br>
     * The method returns the "biggest" group found, use {@link SequenceFileSticher#groupAllFiles(SequenceFileImporter, Collection, boolean, FileFrame)} to
     * retrieve all possible groups.<br>
     * 
     * @param importer
     *        {@link SequenceFileImporter} to use to open image.<br>
     *        If set to <i>null</i> the method automatically try to find a compatible
     *        {@link SequenceFileImporter}
     * @param paths
     *        image file paths we want to group
     * @param findPosition
     *        if true we try to determine the X, Y, Z, T and C image position otherwise a simple ascending T ordering is done
     * @param loadingFrame
     *        Loading dialog if any to show progress
     * @see #groupAllFiles(SequenceFileImporter, Collection, boolean, FileFrame)
     */
    public static SequenceFileGroup groupFiles(SequenceFileImporter importer, Collection<String> paths,
            boolean findPosition, FileFrame loadingFrame)
    {
        SequenceFileGroup result = null;

        for (SequenceFileGroup group : groupAllFiles(importer, paths, findPosition, loadingFrame))
        {
            if (result == null)
                result = group;
            else if (result.positions.size() < group.positions.size())
                result = group;
        }

        return result;
    }

    static int compare(double v1, double v2)
    {
        // can compare ?
        if ((v1 != -1d) && (v2 != -1d))
        {
            if (v1 < v2)
                return -1;
            else if (v1 > v2)
                return 1;
        }

        return 0;
    }

    /**
     * Returns opened {@link SequenceFileImporter} or <i>null</i> if we can't open the given path
     */
    @SuppressWarnings("resource")
    static SequenceFileImporter tryOpen(SequenceFileImporter importer, String path)
    {
        final boolean tryAnotherImporter;
        SequenceFileImporter imp;

        // importer not defined ?
        if (importer == null)
        {
            // try to find a compatible file importer
            imp = Loader.getSequenceFileImporter(path, true);
            // we don't need to try another importer
            tryAnotherImporter = false;
        }
        else
        {
            // use given importer
            imp = importer;
            // we may need to test another importer
            tryAnotherImporter = true;
        }

        // we have an importer ?
        if (imp != null)
        {
            // disable original metadata for LOCI importer
            if (imp instanceof LociImporterPlugin)
            {
                // disable grouping and extra metadata
                ((LociImporterPlugin) imp).setGroupFiles(false);
                ((LociImporterPlugin) imp).setReadOriginalMetadata(false);
            }

            try
            {
                // try to open it (require default metadata otherwise pixel size may miss)
                imp.open(path, 0);
            }
            catch (Throwable t)
            {
                // can't be opened... try with an other importer
                if (tryAnotherImporter)
                    return tryOpen(null, path);

                // can't open importer
                return null;
            }
        }

        return imp;
    }

    // private static void addToGroup(Map<SequenceIdent, SequenceFileGroup> groups, SequencePosition position)
    // {
    // final SequenceIdent ident = new SequenceIdent(position.getBase(), position.getSeriesFromPath(), position.type,
    // position.importer);
    // SequenceFileGroup group = groups.get(ident);
    //
    // // group not yet created ?
    // if (group == null)
    // {
    // // create and add it
    // group = new SequenceFileGroup(ident);
    // groups.put(ident, group);
    // }
    //
    // // add to the group
    // group.positions.add(position);
    // }

    private static void addToGroup(Map<SequenceIdent, SequenceFileGroup> groups, SequencePosition position,
            SequenceFileImporter importer)
    {
        SequenceFileGroup group = groups.get(new SequenceIdent(position.getBase(), position.getIndexS()));

        // no group yet for this base path
        if (group == null)
        {
            // get complete ident for this position
            final SequenceIdent ident = getSequenceIdent(importer, position);

            // can't add this position...
            if (ident == null)
                return;

            // create and add it
            group = new SequenceFileGroup(ident);
            groups.put(ident, group);
        }

        // add to the group
        group.positions.add(position);
    }

    /**
     * Build and return sequence ident for specified {@link SequencePosition}
     */
    private static SequenceIdent getSequenceIdent(SequenceFileImporter importer, SequencePosition position)
    {
        // try to open the image
        final SequenceFileImporter imp = tryOpen(importer, position.getPath());

        // can't open it ? --> return null
        if (imp == null)
            return null;

        try
        {
            // get metadata
            final OMEXMLMetadata meta = imp.getOMEXMLMetaData();
            final SequenceType type = new SequenceType();

            // set type information
            type.sizeX = MetaDataUtil.getSizeX(meta, 0);
            type.sizeY = MetaDataUtil.getSizeY(meta, 0);
            type.sizeZ = MetaDataUtil.getSizeZ(meta, 0);
            type.sizeT = MetaDataUtil.getSizeT(meta, 0);
            type.sizeC = MetaDataUtil.getSizeC(meta, 0);
            type.dataType = MetaDataUtil.getDataType(meta, 0);
            // use -1 as default value to detect when position is not set
            type.pixelSizeX = MetaDataUtil.getPixelSizeX(meta, 0, 0d);
            type.pixelSizeY = MetaDataUtil.getPixelSizeY(meta, 0, 0d);
            type.pixelSizeZ = MetaDataUtil.getPixelSizeZ(meta, 0, 0d);
            type.timeInterval = MetaDataUtil.getTimeInterval(meta, 0, 0d);
            // can compute hash code
            type.computeHashCode();

            return new SequenceIdent(position.getBase(), position.getIndexS(), type, imp);
        }
        catch (Throwable t)
        {
            // error while retrieve metadata
            t.printStackTrace();
            return null;
        }
        finally
        {
            try
            {
                // close importer
                imp.close();
            }
            catch (IOException e)
            {
                // just ignore...
            }
        }
    }

    private static boolean cleanPositions(Collection<FilePosition> filePositions, DimensionId dim)
    {
        // remove fixed dim
        int value = -1;
        for (FilePosition position : filePositions)
        {
            final int v = position.getValue(dim);

            if (v != -1)
            {
                if (value == -1)
                    value = v;
                else if (value != v)
                {
                    // variable --> stop
                    value = -1;
                    break;
                }
            }
        }

        // fixed dimension ? --> remove it
        if (value != -1)
        {
            for (FilePosition position : filePositions)
            {
                if (position.getValue(dim) != -1)
                    position.removeChunk(dim);
            }

            return true;
        }

        return false;
    }
}
