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
package icy.node;

import java.util.ArrayList;

/**
 * @author Stephane
 */
public class NodeOutput extends NodeEntry
{
    protected final ArrayList<InputConnector> inputs;
    protected boolean dataReady;

    /**
     * 
     */
    public NodeOutput()
    {
        super();

        inputs = new ArrayList<InputConnector>();
        dataReady = false;
    }

    /**
     * @return the inputs
     */
    public ArrayList<InputConnector> getInputs()
    {
        return inputs;
    }

    /**
     * @return the dataReady
     */
    public boolean isDataReady()
    {
        return dataReady;
    }

    /**
     * @param dataReady
     *        the dataReady to set
     */
    public void setDataReady(boolean dataReady)
    {
        this.dataReady = dataReady;
    }

}
