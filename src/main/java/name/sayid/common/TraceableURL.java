package name.sayid.common;

import java.io.File;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.function.Function;
import java.util.logging.Logger;

import static java.util.logging.Level.WARNING;

/**
 * The type Archivable url.
 * <p>
 */
public class TraceableURL implements Comparable<TraceableURL> {
    private static final Logger L =
            Logger.getLogger(TraceableURL .class.getName());

    /**
     * return a md5 digest value from a regular path witch represents
     * a digest file storing.
     *
     * @param digestPath the digest path
     * @return the big integer
     * @throws NumberFormatException the number format exception
     */
    public static BigInteger valOfDigestPath(final String digestPath)
            throws NumberFormatException {
        String dp = ((Function<String, String>) a -> {
            String b = a.replace('_', '-');
            int p = b.lastIndexOf(".");
            return (p != -1) ? b.substring(0, p) : b;
        }).apply(digestPath);

        StringBuilder sb = new StringBuilder(dp);
        Arrays.stream(dp.split("/")).forEach(sb::append);

        String biStr = sb.toString();
        return new BigInteger(biStr);
    }

    /**
     * Members defintion
     */

    private final URLContext _urlContext;
    private final URLContext _source;

    private volatile boolean _connected = false;
    private volatile int _failedCount = 0;

    /**
     * Constructor
     */
    public TraceableURL(URL url) {
        _urlContext = new URLContext(url);
        _source = null;
    }

    /**
     * Constructor
     */
    public TraceableURL(URL url, URL source) {
        _urlContext = new URLContext(url);
        _source     = new URLContext(source);
    }

    /**
     * To string string.
     *
     * @return the string
     */
   @Override
    public String toString() {
        return _urlContext.toString();
    }

    /**
     * Compare to int.
     *
     * @param o the o
     * @return the int
     */
    @Override
    public int compareTo(final TraceableURL o) {
        return _urlContext.compareTo(o._urlContext);
    }

    /**
     * Equals boolean.
     *
     * @param obj the obj
     * @return the boolean
     */
    @Override
    public boolean equals(Object obj)
    {
        return _urlContext.equals(obj);
    }

    /**
     * Gets md 5 value.
     *
     * @return the md 5 value
     */
    public BigInteger getMD5Value()
    {
        String s = _urlContext.toString();
        if (s == null || s.isEmpty()) return BigInteger.ZERO;

        MessageDigest md5 = null;
        try {
            md5 = (MessageDigest) MessageDigest.getInstance("MD5").clone();
        } catch (NoSuchAlgorithmException | CloneNotSupportedException ex) {
            L.log(WARNING, ex.getMessage());
        }

        return (md5 == null) ?
                BigInteger.ZERO :
                new BigInteger(md5.digest(s.getBytes()));
    }

    /**
     * Gets digest file path.
     * @return the digest file path
     */
    public Path getDigestFilePath()
    {
        String md5Str = getMD5Value().toString().replace('-', '_');
        LinkedList<String> strl = new LinkedList<String>();
        int len = md5Str.length();
        int i = 0;
        int n = 0;
        do {
            n = (i + 4) > len ? len : i + 4;
            strl.add(md5Str.substring(i, n));
            i = n;
        } while (n < len);

        String pathItems[] = strl.toArray(new String[strl.size()]);

        int lastIndex = pathItems.length - 1;
        String lastItem = pathItems[lastIndex];
        pathItems[lastIndex] = lastItem + ".html";
        return Paths.get("", pathItems);
    }

    /**
     * Gets digest file path.
     * @param extension the dflt extension
     *
     * @return the digest file path
     */
    public Path getDigestFilePath(String extension)
    {
       var s = String.join(".", getDigestFilePath().toString() , extension);
       return Paths.get(s);
    }

    /**
     * Is below to url boolean.
     *
     * @param url the url
     * @return true， only if the owner url is sub-link of input url.
     */
    public boolean isBelowToURL(TraceableURL url)
    {
        String inputHost = url._urlContext.getHost();
        String nativehost = _urlContext.getHost();

        if (inputHost.toLowerCase().startsWith("www."))
            inputHost = inputHost.substring(4);
        if (inputHost.isEmpty())
            return false;

        if (!nativehost.toLowerCase().endsWith(inputHost.toLowerCase()))
            return false;

        Function<String, String> f = (a) -> {
            String fn = new File(a).getName();
            int alen = a.length();
            int flen = fn.length();

            return (alen == flen) ? "" :
                    a.substring(0, alen - flen);
        };

        String inputPathStr = f.apply(url._urlContext.getPath());
        String nativePathStr = f.apply(_urlContext.getPath());

        if (inputPathStr.isEmpty() || "/".equals(inputPathStr))
            return true;
        else if (nativePathStr.isEmpty())
            return false;
        else {
            var items1 = inputPathStr.split("/");
            var items2 = nativehost.split("/");
            if (items1.length > items1.length) return false;
            for (int i = 0; i < items1.length; i++) {
                if (!(items1[i].equals(items2[i]))) return false;
            }
            return true;
        }
    }

    /**
     * Return a cloned self url
     *
     * @return the url
     */
    public URL getURL() throws MalformedURLException
    {
        return _urlContext.makeURL();
    }

    /**
     * Gets source.
     * @return the source
     */
    public URL getSource() throws MalformedURLException
    {
        return _source.makeURL();
    }

    /**
     * If _url equals to _source
     *
     * @return the boolean
     */
    public boolean isEntry()
    {
        return _source == null;
    }

    /**
     * Inching the failed time.
     */
    public void incFailed()
    {
        _failedCount++;
    }

    /**
     * Failed count int.
     *
     * @return the int
     */
    public int failedCount()
    {
        return _failedCount;
    }
}
