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
package icy.system;

import java.security.Permission;

/**
 * @author Stephane
 */
public class IcySecurityManager extends SecurityManager
{
    private static IcySecurityManager securityManager = null;

    public static synchronized void init()
    {
        // initialize security manager
        if (securityManager == null)
        {
            securityManager = new IcySecurityManager();
            System.setSecurityManager(securityManager);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.SecurityManager#checkPermission(java.security.Permission, java.lang.Object)
     */
    @Override
    public void checkPermission(Permission perm, Object context)
    {
        checkPermission(perm);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.SecurityManager#checkPermission(java.security.Permission)
     */
    @Override
    public void checkPermission(Permission perm)
    {
        // // TODO: handle plugins permissions here
        // for (Class<?> c : getClassContext())
        // {
        // if (Plugin.class.isAssignableFrom(c))
        // {
        // // System.out.println(c.getSimpleName() + " : permission " + perm.toString());
        // }
        // }
    }

}
