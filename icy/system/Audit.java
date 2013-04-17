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

import icy.main.Icy;
import icy.network.NetworkUtil;
import icy.preferences.ApplicationPreferences;
import icy.util.XMLUtil;

import java.util.HashMap;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * General audit tools class.
 * 
 * @author Stephane
 */
public class Audit
{
    // network URL
    private static final String URL_REGISTER = NetworkUtil.WEBSITE_URL + "register/registerClient.php?";
    private static final String URL_AUDIT_VERSION = NetworkUtil.WEBSITE_URL + "register/auditVersion.php?";

    // network id
    private static final String ID_REQUEST = "requestId";
    private static final String ID_CLIENT_ARCH = "clientArch";
    private static final String ID_CLIENT_ID = "clientId";
    private static final String ID_CLIENT_VERSION = "clientVersion";

    // xml id
    private static final String XMLID_CLIENT_ID_REQUESTED = "client_id_requested";

    private static boolean idAuditDone = false;

    public static boolean isIdAuditDone()
    {
        return idAuditDone;
    }

    public static void processIdAudit()
    {
        if (!idAuditDone)
        {
            final HashMap<String, String> values = new HashMap<String, String>();
            final int id = ApplicationPreferences.getId();

            values.put(ID_CLIENT_ARCH, SystemUtil.getOSArchIdString());
            values.put(ID_CLIENT_VERSION, Icy.version.toString());

            // need to register
            if (id == -1)
            {
                // ask for registration
                values.put(ID_REQUEST, "1");

                final Document doc = XMLUtil.loadDocument(URL_REGISTER + NetworkUtil.getContentString(values));

                if (doc != null)
                {
                    final Node root = XMLUtil.getRootElement(doc);

                    final int newId = XMLUtil.getElementIntValue(root, XMLID_CLIENT_ID_REQUESTED, -1);

                    // valid id --> save it
                    if (newId != -1)
                        ApplicationPreferences.setId(newId);
                }
            }
            else
            {
                // just audit version
                values.put(ID_CLIENT_ID, Integer.toString(id));

                final Document doc = XMLUtil.loadDocument(URL_AUDIT_VERSION + NetworkUtil.getContentString(values));

                if (doc != null)
                {
                    //
                }
            }
        }
    }
}
