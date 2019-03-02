package name.sayid.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static java.util.logging.Level.WARNING;

public class StringsHelper {
    public static final String EMPTY = "";
    private final static Logger L =
            Logger.getLogger(StringsHelper.class.getName());

    /**
     * Make the input string to another formatted string.
     * The formatter is like to URLEncode.
     * <p>
     *     1、The encoded result is string as before.
     * </p>
     * <p>
     *     2、All input string will be encode. This operation is
     *        differed to url encode.
     * </p>
     * <p>
     *     3、Every character is represented to %xx. xx is tow hexadecimals
     *     identifying on character. This method is equal to url processing.
     * </p>
     * @param s The characters sequence should be encoded.
     * @return encoded result.
     */
    public static String similarURLEncode(String s) {
        if (s.isEmpty()) return "";

        StringBuilder sb = new StringBuilder();
        for (char e : s.toCharArray()) {
            sb.append("%").append(Integer.toHexString((int) e));
        }
        return sb.toString();
    }

    /**
     * Erase the single quotation or double quotation at string header and tail.
     * Erase must oby the rule: if remove the header character is single
     * quotation mark the tail removing must still is the single mark. The double
     * quotation must oby this rule either.
     * @param s The processing string.
     * @return Processed result. Remove quotes
     */
    public static String disbark(String s) {
        if (s.isEmpty()) return "";

        int i0 = 0;
        int i1 = 0;

        if ((i0 = s.indexOf('\'')) != -1) {
            i1 = s.indexOf('\'', i0 + 1);
            return i1 <= 0 ? "" : s.substring(i0 + 1, i1);
        } else if ((i0 = s.indexOf('\"')) != -1) {
            i1 = s.indexOf('\"', i0 + 1);
            return i1 <= 0 ? "" : s.substring(i0 + 1, i1);
        } else
            return "";
    }

    ///////////////////////////////////////////////////////////////
    //
    // Retrieve string from between Brackets or braces
    //

    /**
     * Picks out content between ‘(’ and ‘)’ from input characters sequence.
     * @param s The input character sequence.
     * @return  The picked out content.
     */
    public static String inBrackets(String s)
    {
        return inBB(s, '(', ')');
    }

    /**
     * Picks out content between ‘{’ and ‘}’ from input characters sequence.
     * @param s The input character sequence.
     * @return  The picked out content.
     */
    public static String inBraces(String s)
    {
        return inBB(s, '{', '}');
    }

    /**
     * Picks out content between ‘[’ and ‘]’ from input characters sequence.
     * @param s The input character sequence.
     * @return  The picked out content.
     */
    public static String inSquareBrackets(String s)
    {
        return inBB(s, '[', ']');
    }
    //
    ////////////////////////////////////////////////////////////////

    /**
     * Picks out content between tow special characters (b0 and b1)
     *   from input characters sequence.
     * @param s The input character sequence.
     * @param b0 The front of the surrounding.
     * @param b1 The tail of the surrounding.
     * @return  The picked out content.
     */
    private static String inBB(String s, char b0, char b1)
    {
        if (s.isEmpty()) return "";

        int p0 = s.indexOf(b0);
        int p1 = s.lastIndexOf(b1);

        if (p0 >= p1) return "";

        return s.substring(p0 + 1, p1);
    }

    /**
     * Reading a character sequence from a reader instance. When the
     * reading action is blocked, it will wait static times. If over
     * the time, it should return.
     * @param reader The reading source.
     * @param timeout The static times for waiting when block.
     * @param unit The unit for Date structure. It reference to java api.
     * @return Return all data read from source.
     */
    public static String readLine(BufferedReader reader
            , long timeout
            , TimeUnit unit)
    {
        Callable<String> readTask = () -> {
            try {
                return reader.readLine();
            } catch (Exception ex) {
                L.log(WARNING, ex.getMessage());
                return null;
            }
        };

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<String> future = executor.submit(readTask);
        String s = null;
        try {
            s = future.get(timeout, unit);
        } catch (InterruptedException | ExecutionException | TimeoutException ex) {
            L.log(WARNING, ex.getMessage());
        }
        return s;
    }

    /**
     * Download a correct http page from corresponding url.
     * @param url，The identifier of the downloading page.
     * @param connectTimeout, The limiter value for connecting time.
     * @param readTimeout， The limiter value for reading time.
     * @param sb， The buffer for reading out.
     * @return 网页内容子穿串长度。
     * @throws IOException
     */
    public static int downloadPage(URL url
            , int connectTimeout
            , int readTimeout
            , StringBuilder sb)
            throws IOException
    {
        URLConnection _cnn = url.openConnection();
        if (!(_cnn instanceof HttpURLConnection))
            throw new ProtocolException("URL: "
                    + url + " isn't a http request.");

        HttpURLConnection cnn = (HttpURLConnection) _cnn;

        cnn.connect();
        cnn.setConnectTimeout(connectTimeout);
        cnn.setReadTimeout(readTimeout);

        int respCode = cnn.getResponseCode();
        if (cnn.getResponseCode() != HttpURLConnection.HTTP_OK) {
            cnn.disconnect();
            return respCode;
        }

        try(InputStream is = cnn.getInputStream()) {
            byte[] bb = new byte[4096];
            int read = is.read(bb);
            while (read > 0) {
                sb.append(new String(bb, 0, read));
                read = is.read(bb);
            }
            cnn.disconnect();
            return 200;
        }
    }

    /**
     * Retrieves all the content from a Exception object context.
     * @param ex The exception will be processed.
     * @return The content in the exception context.
     */
    public static String contextOfException(Throwable ex)
    {
        return Arrays.stream(ex.getStackTrace())
                .collect(StringBuilder::new
                        , (x, y)->x.append(x.length()>0? "\r\n" + y: y)
                        , (x, y)->x.append(y)).toString();
    }

    /**
     * Unescape a string by the escaping rule ordered by java sdk.
     * @param str The processing string.
     * @return The processed result, doe'nt contain any escaped characters.
     */
    public static String
    unescapeJava(String str)
    {
        var out = new StringWriter();
        if (str != null){
            int sz = str.length();
            StringBuilder unicode = new StringBuilder(4);
            boolean hadSlash = false;
            boolean inUnicode = false;
            for (int i = 0; i < sz; ++i){
                char ch = str.charAt(i);
                if (inUnicode){
                    unicode.append(ch);
                    if (unicode.length() == 4){
                        try{
                            int nfe = Integer.parseInt(unicode.toString(), 16);
                            out.write((char) nfe);
                            unicode.setLength(0);
                            inUnicode = false;
                            hadSlash = false;
                        }catch (NumberFormatException var9){

                        }
                    }
                }
                else if (hadSlash) {
                    hadSlash = false;
                    switch (ch){
                        case '\"':
                            out.write(34);
                            break;
                        case '\'':
                            out.write(39);
                            break;
                        case '\\':
                            out.write(92);
                            break;
                        case 'b':
                            out.write(8);
                            break;
                        case 'f':
                            out.write(12);
                            break;
                        case 'n':
                            out.write(10);
                            break;
                        case 'r':
                            out.write(13);
                            break;
                        case 't':
                            out.write(9);
                            break;
                        case 'u':
                            inUnicode = true;
                            break;
                        default:
                            out.write(ch);
                    }
                }
                else if (ch == 92)
                {
                    hadSlash = true;
                }else {
                    out.write(ch);
                }
            }
            if (hadSlash) {
                out.write(92);
            }
        }
        return out.toString();
    }

    /**
     * Splits a string into sequence same length segments. If we concat the each
     * segments by the splitting order, the result will equal to original string.
     * Because the splitting length my be not submultiple of value the length of
     * input string.
     * @param str The string will be split.
     * @param len every segment length.
     * @return A list, every item in it is a splitting segment. And the list
     * indexed by splitting order.
     */
    public static List<String>
    divideEqually(String str, int len)
    {
        if (str == null) return List.of();
        int strLen = str.length();
        if (len > strLen) return List.of(str);

        var ret = new ArrayList<String>();
        int i = 0;
        do {
            ret.add(str.substring(i, (strLen - i)%len));
            i += strLen;
        } while (i<strLen);
        return Collections.unmodifiableList(ret);
    }

    /**
     * Determine the input string is not null and not Empty.
     * @param s The checking string.
     * @return False when input string is null or empty.
     */
    public static boolean
    isNotNullAndEmpty(String s)
    {
        return (s != null) && (!s.isEmpty());
    }

    /**
     * Determine the input string is null or Empty.
     * @param s The checking string.
     * @return True when input string is null or empty.
     */
    public static boolean
    isNullOrEmpty(String s)
    {
        return s == null || s.isEmpty();
    }

    /**
     * The regular operating of path with operator: "/", ".", "..".
     * The ".." represents upper level folder. If the operating level
     * position is at top, ".." operator will do nothing.
     * @param path
     * @return
     */
    public static String
    fitnessAbsUrlPath(String path) {
        if (path.isEmpty()) return "/";

        var pathItems = new LinkedList<>(
                Arrays.asList(path.split("/")).stream()
                        .filter(StringsHelper::isNotNullAndEmpty)
                        .collect(Collectors.toList())

        );

        if (pathItems.isEmpty()) return "/";

        var itemBuffer = new LinkedList<String>();
        do {
            String s = pathItems.pop();
            if (".".equals(s)) ;
            else if ("..".equals(s)) {
                if (! itemBuffer.isEmpty())
                    itemBuffer.removeLast();
            }
            else {
                itemBuffer.addLast(s);
            }
        } while (!pathItems.isEmpty());

        return  (itemBuffer.isEmpty())? "/":
            (new StringBuilder("/"))
                .append(String.join("/", itemBuffer))
                .toString();

    }

    /**
     * Add translation characters to a string. The doing is want the string to
     * fit mysql string rules.
     * @param str It should be translated.
     * @return A string can fit to mysql rule.
     */
    public static String mysqlStrAddTrans(String str)
    {
        return str.replace("\'", "\\\'")
                .replace("\"", "\\\"");
    }

}
