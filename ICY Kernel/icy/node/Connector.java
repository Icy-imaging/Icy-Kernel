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
public class Connector
{
    protected NodeEntry connectPoint;
    protected Object state;

    /**
     * 
     */
    public Connector()
    {
        super();

        connectPoint = null;
        state = null;
    }

    /**
     * @return the connectPoint
     */
    public NodeEntry getConnectPoint()
    {
        return connectPoint;
    }

    /**
     * @param connectPoint
     *        the connectPoint to set
     */
    public void setConnectPoint(NodeEntry connectPoint)
    {
        this.connectPoint = connectPoint;
    }

    /**
     * @return the state
     */
    public Object getState()
    {
        return state;
    }

    /**
     * @param state
     *        the state to set
     */
    public void setState(Object state)
    {
        this.state = state;
    }

    public boolean isConnected()
    {
        return connectPoint != null;
    }

}
