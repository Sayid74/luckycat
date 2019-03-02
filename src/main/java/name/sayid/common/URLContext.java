package name.sayid.common;

import java.net.MalformedURLException;
import java.net.URL;

public class URLContext implements Comparable<URLContext>
{
    private String _userInfo;
    private String _protocol;
    private String _host;
    private int    _port;
    private String _path;
    private String _query;
    private String _segment;

    /**
     * The construct. Because URLContext is the capsulate of a url. So
     * the wrapping url must be given, it is the core of the object.
     * @param url It is the object core data.
     */
    public URLContext(URL url)
    {
        _protocol = url.getProtocol();
        _userInfo = url.getUserInfo();
        _host     = url.getHost();
        _port     = url.getPort();
        _path     = StringsHelper.fitnessAbsUrlPath(url.getPath());
        _query    = url.getQuery();
        _segment  = url.getRef();
    }

    /**
     * Rebuild a url format string, witch can represents all the context in object.
     * @return The correct string fits requirements.
     */
    public String toString()
    {
        var sb = new StringBuilder(_protocol).append("://");
        if (StringsHelper.isNotNullAndEmpty(_userInfo))
            sb.append(_userInfo).append('@');
        sb.append(_host);
        if (_port != -1)
            sb.append(':').append(_port);
        sb.append(_path);
        if (StringsHelper.isNotNullAndEmpty(_query))
            sb.append('?').append(_query);
        if (StringsHelper.isNotNullAndEmpty(_segment) )
            sb.append('#').append(_segment);
        return sb.toString();
    }

    /**
     * Rebuild a url object, witch represents all the context in object.
     * @return The correct string fits requirements.
     * @throws MalformedURLException
     */
    public URL makeURL() throws MalformedURLException
    {
        return new URL(toString());
    }

    /**
     * Overrides from comparable interface.
     *
     * @param o the o
     * @return the int
     */
    @Override
    public int compareTo(final URLContext o) {
        return toString().compareTo(o.toString());
    }


    /**
     * Overrides from comparable interface.
     *
     * @param obj
     * @return
     */
    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof URL) {
            try {
                return makeURL().sameFile((URL)obj);
            } catch (MalformedURLException e) {
                return false;
            }
        } else if(obj instanceof URLContext) {
            return toString().equals(toString());
        } else {
            return false;
        }
    }

    /**
     * Overrides from Object Class interface.
     *
     * @return new Cloned object
     */
    @Override
    public URLContext clone() {
        try {
            URLContext cloned = (URLContext) super.clone();
            cloned._userInfo  = _userInfo;
            cloned._host      = _host;
            cloned._path      = _path;
            cloned._port      = _port;
            cloned._protocol  = _protocol;
            cloned._query     = _query;
            cloned._segment   = _segment;
            return clone();
        } catch (CloneNotSupportedException e) {
            return new URLContext(this);
        }

    }

    private URLContext(URLContext other) {
        _userInfo  = other._userInfo;
        _host      = other._host;
        _path      = other._path;
        _port      = other._port;
        _protocol  = other._protocol;
        _query     = other._query;
        _segment   = other._segment;
    }

    /**
     * Return userInfo property default value is described by input url object.
     * @return User information.
     */
    public String getUserInfo() {
        return _userInfo;
    }

    /**
     * The userInfo property setter.
     * @param userInfo
     */
    public void setUserInfo(String userInfo) {
        _userInfo = userInfo;
    }

    /**
     * The getter of protocol property. The default value is from
     * constructor parameter
     * @return
     */
    public String getProtocol() {
        return _protocol;
    }

    /**
     * The setter of protocol property. The default value is from constructor
     * parameter.
     * @param protocol
     */
    public void setProtocol(String protocol) {
        _protocol = protocol;
    }

    /**
     * The getter of host property. The default value is from constructor
     * parameter.
     * @return Host.
     */
    public String getHost() {
        return _host;
    }

    /**
     * The setter of host property. The default value is from constructor
     * parameter.
     * @param host The host of url context.
     */
    public void setHost(String host) {
        _host = host;
    }

    /**
     * The getter of port property. The default value is from constructor
     * parameter.
     * @return The port of url context
     */
    public int getPort() {
        return _port;
    }

    /**
     * The setter of port property. The default value is from constructor
     * parameter.
     * @parameter The port is url context.
     */
    public void setPort(int port) {
        _port = port;
    }

    /**
     * The getter of path property. The default value is from constructor
     * parameter.
     * return The path. Path is the segment of url.
     */
    public String getPath() {
        return _path;
    }

    /**
     * The setter of path property. The default value is from constructor
     * parameter.
     * return The path. Path is the segment of url.
     */
    public void setPath(String path) {
        _path = path;
    }

    /**
     * The getter of query options property. The default value is from constructor
     * parameter.
     * return The query options property with string object.
     * is the segment of url.
     */
    public String getQuery() {
        return _query;
    }

    /**
     * Adds some options to url query part. One query option is a key and
     * value pair.
     * @param key It is query option key.
     * @param value It is query option value.
     */
    public void addQuery(String key, String value) {
        var sb = new StringBuilder();
        if (StringsHelper.isNotNullAndEmpty(_query))
            sb.append(_query).append('&');
        sb.append(key).append("=").append(value);
        _query = sb.toString();
    }

    /**
     * Make the query options to empty.
     */
    public void clearQuery() {
        _query = null;
    }

    /**
     * URL segment is the part of after '#' mark. It represents reference to a
     * page segment.
     * @return segment part.
     */
    public String getSegment() {
        return _segment;
    }

    /**
     * The setter for segment property.
     * @param segment property of url.
     */
    public void setSegment(String segment) {
        _segment = segment;
    }

    /**
     * Parser a string to a url and pick out port property from url.
     * @param url Is input string.
     * @return A string represents the protocol of the url the input described.
     */
    public static String parseProtocol(String url)
    {
        if (StringsHelper.isNullOrEmpty(url)) return StringsHelper.EMPTY;
        int p = url.indexOf(':');
        if (p == -1) return null;
        else if (p == 0)  return "";
        else {
            return url.substring(0, p).trim();
        }
    }
}
