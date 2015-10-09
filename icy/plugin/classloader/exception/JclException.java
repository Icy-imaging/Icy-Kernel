/*
 * Copyright 2010-2015 Institut Pasteur.
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

package icy.plugin.classloader.exception;

/**
 * General custom exception
 * 
 * @author Kamran Zafar
 * 
 */
public class JclException extends RuntimeException {
    /**
     * Default serial id
     */
    private static final long serialVersionUID = 1L;

    /**
     * Default constructor
     */
    public JclException() {
        super();
    }

    /**
     * @param message
     */
    public JclException(String message) {
        super( message );
    }

    /**
     * @param cause
     */
    public JclException(Throwable cause) {
        super( cause );
    }

    /**
     * @param message
     * @param cause
     */
    public JclException(String message, Throwable cause) {
        super( message, cause );
    }
}
