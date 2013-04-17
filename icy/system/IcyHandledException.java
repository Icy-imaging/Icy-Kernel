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
package icy.system;

/**
 * Runtime handled exception.<br>
 * The Icy exception handler display a simple error dialog instead of the complete report dialog<br>
 * when it catches this exception.<br>
 * Also no log is saved in the console.
 * 
 * @author Stephane
 */
public class IcyHandledException extends RuntimeException
{
    /**
     * 
     */
    private static final long serialVersionUID = -1167116427799704383L;

    /**
     * 
     */
    public IcyHandledException()
    {
        super();
    }

    /**
     * @param message
     * @param cause
     */
    public IcyHandledException(String message, Throwable cause)
    {
        super(message, cause);
    }

    /**
     * @param message
     */
    public IcyHandledException(String message)
    {
        super(message);
    }

    /**
     * @param cause
     */
    public IcyHandledException(Throwable cause)
    {
        super(cause);
    }
}
