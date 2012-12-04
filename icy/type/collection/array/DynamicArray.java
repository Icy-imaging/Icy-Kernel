/*
 * Copyright 2010, 2011 Institut Pasteur.
 * 
 * This file is part of ICY.
 * 
 * ICY is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * ICY is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with ICY. If not, see <http://www.gnu.org/licenses/>.
 */
package icy.type.collection.array;

import icy.type.DataType;
import icy.type.TypeUtil;

import java.util.ArrayList;

/**
 * @author Stephane
 */
public abstract class DynamicArray
{
    // MUST BE A POWER OF 2
    public static final int BLOCK_SIZE = 1 << 16;

    /**
     * Create a DynamicArray with specified type
     */
    public static DynamicArray create(DataType type)
    {
        switch (type.getJavaType())
        {
            case BYTE:
                return new DynamicArray.Byte();
            case SHORT:
                return new DynamicArray.Short();
            case INT:
                return new DynamicArray.Int();
            case LONG:
                return new DynamicArray.Long();
            case FLOAT:
                return new DynamicArray.Float();
            case DOUBLE:
                return new DynamicArray.Double();
            default:
                return null;
        }
    }

    /**
     * Create a DynamicArray with specified type ({@link TypeUtil} constant)
     * 
     * @deprecated
     */
    @Deprecated
    public static DynamicArray create(int type)
    {
        return create(DataType.getDataType(type));
    }

    public static class Generic extends DynamicArray
    {
        public void addSingle(Object value)
        {
            final ArrayBlock block = getAvailableBlock(true);
            ((Object[]) block.array)[block.size++] = value;
        }

        @Override
        protected Object createArray(int size)
        {
            return new Object[size];
        }

        @Override
        protected int getArraySize(Object array)
        {
            return ((Object[]) array).length;
        }

        @Override
        public Object[] asArray()
        {
            return (Object[]) super.asArray();
        }
    }

    public static class Byte extends DynamicArray
    {
        public void addSingle(byte value)
        {
            final ArrayBlock block = getAvailableBlock(true);
            ((byte[]) block.array)[block.size++] = value;
        }

        @Override
        protected Object createArray(int size)
        {
            return new byte[size];
        }

        @Override
        protected int getArraySize(Object array)
        {
            return ((byte[]) array).length;
        }

        @Override
        public byte[] asArray()
        {
            return (byte[]) super.asArray();
        }
    }

    public static class Short extends DynamicArray
    {
        public void addSingle(short value)
        {
            final ArrayBlock block = getAvailableBlock(true);
            ((short[]) block.array)[block.size++] = value;
        }

        @Override
        protected Object createArray(int size)
        {
            return new short[size];
        }

        @Override
        protected int getArraySize(Object array)
        {
            return ((short[]) array).length;
        }

        @Override
        public short[] asArray()
        {
            return (short[]) super.asArray();
        }
    }

    public static class Int extends DynamicArray
    {
        public void addSingle(int value)
        {
            final ArrayBlock block = getAvailableBlock(true);
            ((int[]) block.array)[block.size++] = value;
        }

        @Override
        protected Object createArray(int size)
        {
            return new int[size];
        }

        @Override
        protected int getArraySize(Object array)
        {
            return ((int[]) array).length;
        }

        @Override
        public int[] asArray()
        {
            return (int[]) super.asArray();
        }
    }

    public static class Long extends DynamicArray
    {
        public void addSingle(long value)
        {
            final ArrayBlock block = getAvailableBlock(true);
            ((long[]) block.array)[block.size++] = value;
        }

        @Override
        protected Object createArray(int size)
        {
            return new long[size];
        }

        @Override
        protected int getArraySize(Object array)
        {
            return ((long[]) array).length;
        }

        @Override
        public long[] asArray()
        {
            return (long[]) super.asArray();
        }
    }

    public static class Float extends DynamicArray
    {
        public void addSingle(float value)
        {
            final ArrayBlock block = getAvailableBlock(true);
            ((float[]) block.array)[block.size++] = value;
        }

        @Override
        protected Object createArray(int size)
        {
            return new float[size];
        }

        @Override
        protected int getArraySize(Object array)
        {
            return ((float[]) array).length;
        }

        @Override
        public float[] asArray()
        {
            return (float[]) super.asArray();
        }
    }

    public static class Double extends DynamicArray
    {
        public void addSingle(double value)
        {
            final ArrayBlock block = getAvailableBlock(true);
            ((double[]) block.array)[block.size++] = value;
        }

        @Override
        protected Object createArray(int size)
        {
            return new double[size];
        }

        @Override
        protected int getArraySize(Object array)
        {
            return ((double[]) array).length;
        }

        @Override
        public double[] asArray()
        {
            return (double[]) super.asArray();
        }
    }

    protected class ArrayBlock
    {
        protected Object array;
        protected int size;

        public ArrayBlock()
        {
            super();

            array = createArray(BLOCK_SIZE);
            size = 0;
        }

        protected void clear()
        {
            size = 0;
        }

        /**
         * @return the (used) size
         */
        public int getSize()
        {
            return size;
        }

        /**
         * @return the free space
         */
        public int getFreeSpace()
        {
            return BLOCK_SIZE - getSize();
        }

        protected void get(Object out, int inOffset, int outOffset, int len)
        {
            System.arraycopy(array, inOffset, out, outOffset, len);
        }

        protected void add(Object in, int inOffset, int len)
        {
            System.arraycopy(in, inOffset, array, size, len);
            size += len;
        }

        protected void put(Object in, int inOffset, int outOffset, int len)
        {
            System.arraycopy(in, inOffset, array, outOffset, len);
            size = Math.max(size, outOffset + len);
        }
    }

    private final ArrayList<ArrayBlock> blocks;

    public DynamicArray()
    {
        super();

        blocks = new ArrayList<ArrayBlock>();
    }

    protected abstract Object createArray(int size);

    protected abstract int getArraySize(Object array);

    public void clear()
    {
        setSize(0);
    }

    public boolean isEmpty()
    {
        return getSize() == 0;
    }

    protected ArrayBlock addBlock()
    {
        final ArrayBlock result = new ArrayBlock();

        blocks.add(result);

        return result;
    }

    protected void removeBlock()
    {
        final int numBlock = blocks.size();

        // remove last block if it exists
        if (numBlock > 0)
            blocks.remove(numBlock - 1);
    }

    protected void checkCapacity(int size)
    {
        while (getCapacity() < size)
            setSize(size);
    }

    public int getCapacity()
    {
        return blocks.size() * BLOCK_SIZE;
    }

    public int getSize()
    {
        final int lastBlockIndex = getLastBlockIndex();

        if (lastBlockIndex < 0)
            return 0;

        return (BLOCK_SIZE * lastBlockIndex) + blocks.get(lastBlockIndex).getSize();
    }

    protected int getLastBlockIndex()
    {
        return blocks.size() - 1;
    }

    protected ArrayBlock getLastBlock()
    {
        final int lastBlockIndex = getLastBlockIndex();

        if (lastBlockIndex < 0)
            return null;

        return blocks.get(lastBlockIndex);
    }

    protected ArrayBlock getBlockFromOffset(int offset)
    {
        final int blockIndex = offset / BLOCK_SIZE;

        if (blockIndex < blocks.size())
            return blocks.get(blockIndex);

        return null;
    }

    protected ArrayBlock getAvailableBlock(boolean create)
    {
        final ArrayBlock lastBlock = getLastBlock();

        // last block exist and has free space ? --> return it
        if ((lastBlock != null) && (lastBlock.getFreeSpace() > 0))
            return lastBlock;

        if (create)
            return addBlock();

        return null;
    }

    public void setSize(int size)
    {
        // special case
        if (size == 0)
        {
            blocks.clear();
            return;
        }

        // add blocks if needed
        while (getCapacity() < size)
        {
            final ArrayBlock block = addBlock();
            // set block size
            block.size = BLOCK_SIZE;
        }
        // remove blocks if needed
        while ((getCapacity() - BLOCK_SIZE) > size)
            removeBlock();

        // adjust last block size if needed
        if (getCapacity() > size)
            getLastBlock().size = getCapacity() - size;
    }

    public void addAll(DynamicArray in)
    {
        final Object array = in.asArray();

        add(array, 0, getArraySize(array));
    }

    public void add(Object in)
    {
        add(in, 0, getArraySize(in));
    }

    public void get(Object out, int inOffset, int outOffset, int len)
    {
        int srcOffset = inOffset;
        int dstOffset = outOffset;
        int cnt = len;
        while (cnt > 0)
        {
            final ArrayBlock block = getBlockFromOffset(srcOffset);
            final int subSrcOffset = srcOffset & (BLOCK_SIZE - 1);
            final int subLen = Math.min(BLOCK_SIZE - subSrcOffset, cnt);

            block.get(out, subSrcOffset, dstOffset, subLen);
            srcOffset += subLen;
            dstOffset += subLen;
            cnt -= subLen;
        }
    }

    public void add(Object in, int inOffset, int len)
    {
        int offset = inOffset;
        int cnt = len;
        while (cnt > 0)
        {
            final ArrayBlock block = getAvailableBlock(true);
            final int blockSpace = block.getFreeSpace();
            final int toCopy = Math.min(blockSpace, cnt);

            block.add(in, offset, toCopy);
            offset += toCopy;
            cnt -= toCopy;
        }
    }

    public void put(Object in, int inOffset, int outOffset, int len)
    {
        checkCapacity(outOffset + len);

        int srcOffset = inOffset;
        int dstOffset = outOffset;
        int cnt = len;
        while (cnt > 0)
        {
            final ArrayBlock block = getBlockFromOffset(dstOffset);
            final int subDstOffset = dstOffset & (BLOCK_SIZE - 1);
            final int subLen = Math.min(BLOCK_SIZE - subDstOffset, cnt);

            block.put(in, srcOffset, subDstOffset, subLen);
            srcOffset += subLen;
            dstOffset += subLen;
            cnt -= subLen;
        }
    }

    public Object asArray()
    {
        final Object result = createArray(getSize());

        int offset = 0;
        for (ArrayBlock block : blocks)
        {
            final int blockSize = block.getSize();
            block.get(result, 0, offset, blockSize);
            offset += blockSize;
        }

        return result;
    }

}
