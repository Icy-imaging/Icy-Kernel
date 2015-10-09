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
package icy.system;

import java.io.FileDescriptor;
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

    @Override
    public void checkConnect(String host, int port)
    {
        // ignore for now
    }

    @Override
    public void checkConnect(String host, int port, Object context)
    {
        // ignore for now
    }

    @Override
    public void checkDelete(String file)
    {
        // ignore for now
    }

    @Override
    public boolean checkTopLevelWindow(Object window)
    {
        return true;
    }

    @Override
    public void checkListen(int port)
    {
        // ignore for now
    }

    @Override
    public void checkRead(String file)
    {
        // ignore for now
    }

    @Override
    public void checkRead(FileDescriptor fd)
    {
        // ignore for now
    }

    @Override
    public void checkRead(String file, Object context)
    {
        // ignore for now
    }

    @Override
    public void checkWrite(FileDescriptor fd)
    {
        // ignore for now
    }

    @Override
    public void checkWrite(String file)
    {// ignore for now
    }

    @Override
    public void checkPackageAccess(String pkg)
    {
        // ignore for now
    }

    @Override
    public void checkPackageDefinition(String pkg)
    {
        // ignore for now
    }

    @Override
    public void checkPropertiesAccess()
    {
        // ignore for now
    }

    @Override
    public void checkPropertyAccess(String key)
    {
        // ignore for now
    }

    @Override
    public void checkAccept(String host, int port)
    {
        // ignore for now
    }

    @Override
    public void checkAccess(Thread t)
    {
        // ignore for now
    }

    @Override
    public void checkAccess(ThreadGroup g)
    {
        // ignore for now
    }

    @Override
    public void checkMemberAccess(Class<?> clazz, int which)
    {
        // ignore for now
    }

    @Override
    public void checkPermission(Permission perm, Object context)
    {
        checkPermission(perm);
    }

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
