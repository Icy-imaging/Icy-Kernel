package icy.network;

import icy.preferences.ApplicationPreferences;
import icy.util.StringUtil;
import icy.util.XMLUtil;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.w3c.dom.Document;

public class WebInterface
{
    // beta test
    //public static final String BASE_URL = "https://icy.yhello.co/interface/?";
    // official
    public static final String BASE_URL = "https://icy.bioimageanalysis.org/interface/?";

    public static final String PARAM_ACTION = "action";
    public static final String PARAM_CLIENT_ID = "clientId";
    public static final String PARAM_ID = "id";
    public static final String PARAM_CLASSNAME = "classname";
    public static final String PARAM_FIELD = "field";

    public static final String SEARCH_TYPE_PLUGIN = "plugin";
    public static final String SEARCH_TYPE_SCRIPT = "script";
    public static final String SEARCH_TYPE_PROTOCOL = "protocol";

    /**
     * Process search on the website in a specific resource and return result in a XML Document.
     * 
     * @param text
     *        Text used for the search request, it can contains several words and use operators.<br>
     *        Examples:<br>
     *        <li><i>spot detector</i> : any of word should be present</li>
     *        <li><i>+spot +detector</i> : both words should be present</li>
     *        <li>"spot detector"</i> : the exact expression should be present</li>
     *        <li><i>+"spot detector" -tracking</i> : <i>spot detector</i> should be present and <i>tracking</i> absent</li>
     * @param type
     *        type of resource we want to search in.<br>
     *        Accepted values are:<br>
     *        <li>SEARCH_TYPE_PLUGIN</li>
     *        <li>SEARCH_TYPE_SCRIPT</li>
     *        <li>SEARCH_TYPE_PROTOCOL</li>
     *        <li>null (all resources)</li>
     * @return result in XML Document format
     * @throws UnsupportedEncodingException
     */
    public static Document doSearch(String text, String type) throws UnsupportedEncodingException
    {
        // build request (encode search text in UTF8)
        String request = BASE_URL + PARAM_ACTION + "=search&search=" + URLEncoder.encode(text, "UTF-8");

        // specific type ?
        if (!StringUtil.isEmpty(type))
            request += "&pt=" + type;

        // add client id       
//        request += "&" + PARAM_CLIENT_ID + "=2532495";
        request += "&" + PARAM_CLIENT_ID + "=" + ApplicationPreferences.getId();

        // send request to web site and get result
        return XMLUtil.loadDocument(URLUtil.getURL(request), true);
    }

    /**
     * Process search on the website in all resources and return result in a XML Document.
     * 
     * @param text
     *        Text used for the search request, it can contains several words and use operators.<br>
     *        Examples:<br>
     *        <li><i>spot detector</i> : any of word should be present</li>
     *        <li><i>+spot +detector</i> : both words should be present</li>
     *        <li>"spot detector"</i> : the exact expression should be present</li>
     *        <li><i>+"spot detector" -tracking</i> : <i>spot detector</i> should be present and <i>tracking</i> absent</li>
     * @return result in XML Document format
     * @throws UnsupportedEncodingException
     */
    public static Document doSearch(String text) throws UnsupportedEncodingException
    {
        return doSearch(text, null);
    }

}
