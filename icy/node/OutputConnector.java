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

/**
 * @author Stephane
 */
public class OutputConnector extends Connector
{
    private boolean consumed;

    /**
     * 
     */
    public OutputConnector()
    {
        super();

        consumed = false;
    }

    /**
     * @return the consumed
     */
    public boolean isConsumed()
    {
        return consumed;
    }

    /**
     * @param consumed
     *        the consumed to set
     */
    public void setConsumed(boolean consumed)
    {
        this.consumed = consumed;
    }

    public NodeInput getInput()
    {
        return (NodeInput) getConnectPoint();
    }

    public void setInput(NodeInput input)
    {
        setConnectPoint(input);
    }
}
