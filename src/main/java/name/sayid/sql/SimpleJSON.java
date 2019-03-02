package name.sayid.sql;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * JSON数据的简单解析器。该解析器采用正则方法对字符串进行分析，因此性能为O(n)。
 * 此外，由于是简单解析器，对标准采取了取舍。这个取舍建立在前提：<p>
 *     当你需要使用此解析器对某个JSON进行解析之前，你有足够的知识，了解json数
 *     据中的全部信息。包括，数据中全部字段名称，以及名称对应的数据类型，取值范
 *     围。解析器，不负责分析数据类型和取值范围。</p>
 * 此解析器最好和SimpleJSONMaker类配合使用。SimpleJSONMaker类中定义了一套
 * annotation。可以通过使用 annotation 将一个传统的 java 类输出成json字符
 * 串。
 */
public class SimpleJSON
{
    private final static Logger L
        = Logger.getLogger(SimpleJSON.class.getName());
    private final static Set EMPT_CHARS = Set.of('\r', '\n', '\t', ' ');

    /**
     * 对于无法完成解析的字符串，会抛出这个错误。
     */
    public static class JSONFormatException extends Exception
    {
        public JSONFormatException(String message)
        {
            super(message);
        }
    }

    /**
     * JSON 中值表达式（右值表达式）的接口。这个接口假设右值可能为空。另外可以以
     * 字符串输出。在这个假设前提下，有三种表达式实现了该接口：
     * <p>
     *     1、原生数据（NativeExp）这是简单右值形式，类似于 f(x) -> x 这样的
     *     函数形式。也就是说，对数据不采用任何分析和操作。
     * </p>
     * <p>
     *     2、数组数据（NativeExp) 该数据，由表达式集合构成，以数组对象保存。在
     *     json中用 ‘[’ 和 ‘]’ 包围起来。
     * </p>
     * <p>
     *     3、JSON数据（JsonExp) 该数据是一组以命值对形式的集合，以Map对象保存。
     *     在 json中用 ‘{’ 和 ‘}’ 包围起来。JSON 中名字用两个‘"’包围或者用两个
     *     ‘'’符号包围。名字与值（表达式）之间用 ‘:’ 分割。
     * </p>
     */
    public interface Expression
    {
        /**
         * 表示该表达式表示一个没有任何数据的值。
         * @return
         */
        boolean isEmpty();

        /**
         * 将表达式序列化成一个字符串。
         * @return
         */
        String toString();
    }

    /**
     * 原生数据类型。原生数据类型不做解析。原生数据类型的判断条件：
     * <p>
     *     1、是被 ‘{’ ‘}’ 包围,不是被 ‘[’‘]’包围的数据。并且
     *     是在名字后面。
     * </p>
     * <p>
     *     2、在被 ‘[’ 和 ‘]’ 包围，不是被 ‘{’‘}’包围的数据。由
     *     ‘,’ 分割。
     * </p>
     */
    public static class NativeExp implements Expression
    {
        private String _content = null;

        private static int parser(String s, int entry, NativeExp exp)
            throws JSONFormatException
        {
            StringBuilder sb = new StringBuilder();
            int i = next(s, entry);
            char c_ = s.charAt(i);
            boolean isQtm = c_ == '\"';
            boolean isQts = c_ == '\'';
            boolean isEscape = false;
            if (isQtm || isQts){
                sb.append(c_);
                i++;
            }
            for(; i < s.length(); i++)
            {
                char c = s.charAt(i);
                if (!isQtm && !isQts) {
                    if (Set.of( '{' ,'[').contains(c)) {
                        throw new JSONFormatException(
                                "JSonExpresion has illigual character at "
                                        + (i - 1));
                    } else if (Set.of(',', '}', ']').contains(c)) {
                        exp._content = sb.toString();
                        return --i;
                    }
                    sb.append(c);
                }
                else {
                    if (! isEscape){
                        if ((c == '\"' && isQtm)||(c=='\'' && isQts)) {
                            sb.append(c);
                            int n = next(s, ++i);
                            c = s.charAt(n);
                            if (! Set.of(',', '}', ']').contains(c))
                                throw new JSONFormatException("JSonExpresion" +
                                        " has illigual character at " + n);
                            else {
                                exp._content = sb.toString();
                                return --n;
                            }
                        }
                        isEscape = c == '\\';
                        sb.append(c);
                    }
                    else {
                        sb.append(c);
                        isEscape = false;
                    }
                }
            }
            throw new JSONFormatException (
                "Native expression is unterminate.");
        }

        /**
         * 将表达式序列化成字符串，由于是原始表达式，所以输出与
         * 不做任何处理。
         * @return 返回表达式内容（不做解析的原始字符串）
         */
        @Override
        public String toString()
        {
            return _content;
        }

        /**
         *
         * @return
         */
        @Override
        public boolean isEmpty()
        {
            return _content == null || _content.isEmpty();
        }

        public String getValue()
        {
            return _content;
        }
    }

    /**
     * 数组表达式，将数据存放在列表中。数据表达式中的元素放被包围在 ‘[’ 和 ‘]’
     * 中。每个子元素由‘,’ 分割。
     */
    public static class ArrayExp implements Expression
    {
        LinkedList<Expression> members = new LinkedList<>();

        private static int parser(String s, int entry, ArrayExp exp)
            throws JSONFormatException
        {
            int i = entry;
            while(i < s.length())
            {
                i = next(s, i);
                assertLen(i, s);

                char c = s.charAt(i);
                Expression e;
                if (c == '{') {
                    e = new JSONExpr();
                    i = JSONExpr.parser(s, ++i, (JSONExpr)e);
                }
                else if (c == '[') {
                    e = new ArrayExp();
                    i = ArrayExp.parser(s, ++i, (ArrayExp)e);
                }
                else {
                    e = new NativeExp();
                    i = NativeExp.parser(s, i, (NativeExp)e);
                }

                if (!e.isEmpty()) exp.members.add(e);

                i = next(s, ++i);
                assertLen(i, s);
                c = s.charAt(i);
                if (c == ',') {
                    i++;
                } else if (c == ']') {
                    return i;
                } else {
                    throw new JSONFormatException(
                        "When array expression parsering, "
                            + "There is an unkown charactor at " + i);
                }
            }

            throw new JSONFormatException(
                "Array expression is unterminate.");
        }

        @Override
        public String toString()
        {
            if (members.size() == 0) return "[]";
            StringBuilder sb = members.stream()
                .collect( StringBuilder::new
                    , (b, x)->b.append(x.toString()).append(',')
                    , (l, r)->l.append(r));

            int len = sb.length();
            sb.deleteCharAt(len - 1);
            return "[" + sb.toString() + "]";
        }

        @Override
        public boolean isEmpty()
        {
            return members.isEmpty();
        }

        /**
         * 将数据放在不可修改的列表中返回。
         * @return 返回一个列表，列表中包括了全部的表达式成员。
         */
        public List<Expression> getMembers()
        {
            return Collections.unmodifiableList(members);
        }
    }

    /**
     * JSON表达式。每个json数据的跟都是一个json表达式。json表达式的数据被保存在一个
     * Map结构中。
     */
    public static class JSONExpr
        implements Expression
    {
        private Map<String, Expression> members = new HashMap<>();

        public Map<String, Expression> getMembers() {
            return Collections.unmodifiableMap(members);
        }

        public static int parser(String s, int entry, JSONExpr exp)
            throws JSONFormatException
        {
            int i = next(s, entry);
            while (i < s.length()) {
                StringBuilder sb = new StringBuilder();
                i = recognizeKey(s, i, sb);
                if (sb.length() == 0)
                    throw new JSONFormatException(
                        "The key is empty.");

                i = acrossToExp(s, i);
                assertLen(i, s);

                char c = s.charAt(i);

                Expression e;
                if (c == '{') {
                    e = new JSONExpr();
                    i = JSONExpr.parser(s, ++i, (JSONExpr)e);
                } else if (c == '[') {
                    e = new ArrayExp();
                    i = ArrayExp.parser(s, ++i, (ArrayExp)e);
                } else {
                    e = new NativeExp();
                    i = NativeExp.parser(s, i, (NativeExp)e);
                }

                assertLen(i, s);

                exp.members.put(sb.toString(), e);

                i = next(s, ++i);
                assertLen(i, s);
                c = s.charAt(i);
                if (c == ',') {
                    i++;
                } else if (c == '}') {
                    return i;
                } else {
                    throw new JSONFormatException(
                        "When json expression parsering, "
                            +" There is an unkown charactor at " + i);
                }
            }
            throw new JSONFormatException(
                "Array expression isn't terminate.");

        }

        @Override
        public String toString()
        {
            StringBuilder sb = members.entrySet().stream()
                .collect( StringBuilder::new
                    , (b, x)->b.append(String.format("\"%s\":%s,"
                                    , x.getKey(), x.getValue()))
                    , (l, r)->l.append(r));

            sb.deleteCharAt(sb.length() - 1);
            return sb.insert(0, "{").append("}").toString();
        }

        @Override
        public boolean isEmpty()
        {
            return members.isEmpty();
        }

        /**
         * 通过 key(名值对中的名) 找到对应的值（一个表达式：NativeExp, ArrayExp,
         * JSONExpr 中的一种。）
         * @param key 名值对中的名
         * @return 名对应的值(NativeExp, ArrayExp, JSONExpr 中一种类型的值)
         */
        public Expression valueOfKey(String key)
        {
            return members.get(key);
        }

        /**
         * 通过该函数，可以得到一个json表达式中的全部名字集合。
         * @return 返回名字集合。
         */
        public Set<String> keys()
        {
            return members.keySet();
        }
    }

    /**
     * 判断游标所处位置是不是超过了字符串的长度。
     * @param position 游标所处的位置。
     * @param s 被检查的字符串。
     * @throws JSONFormatException 如果游标位置超出了字符串就抛出错误。
     */
    public static void assertLen(int position, String s)
            throws JSONFormatException
    {
        if (position >= s.length())
            throw new JSONFormatException(
                "The index is out of length.");
    }

    /**
     * 从字符串的某个位置找到一个key(名)，放入StringBuilder中。返回操作后来的位置
     * 也就是名最后一个字符后面的一个字符的位置。
     * @param s 被操作的字符串。
     * @param entry 入口位置。
     * @param sb 保存名的字符串的容器对象。
     * @return 返回名最后一个字符后面的一个字符的位置。
     * @throws JSONFormatException 当名字后面不是被两个‘'’或两个‘"’ 包围就会抛
     * 出错误信息。
     */
    public static int recognizeKey(String s, int entry, StringBuilder sb)
        throws JSONFormatException
    {
        int i = next(s, entry);

        assertLen(i, s);

        var quoteSet = Set.of('\"', '\'');
        var header = s.charAt(i++);

        if (!quoteSet.contains(header)) {
            L.log(Level.SEVERE, "Error at :" + i);
            L.log(Level.SEVERE, "The error json str: " + s);
            throw new JSONFormatException("JSON Key mast start with \" or \'");
        }

        while(i < s.length()) {
            char c = s.charAt(i++);
            if (c == header)
                break;
            else
                sb.append(c);
        }

        return i;
    }

    /**
     * 这个函数的意义是到达表达式的开始位置，输入是一个被操作的字符串，以及
     * 操作的起始位置。从这个起始位置开始找。
     * @param s 被操作的字符串。
     * @param entry 操作的起始位置。
     * @return
     * @throws JSONFormatException 当名字后面不是 ‘:’ 符号时，就抛出
     * 错误信息。
     */
    public static int acrossToExp(String s, int entry)
        throws JSONFormatException
    {
        int i = next(s, entry);
        assertLen(i, s);
        char c = s.charAt(i);
        if (c != ':')
            throw new JSONFormatException (
            "There must be a ':' charactor.");
        return next(s, ++i);
    }

    /**
     * 越过所有空白符，到达有效字符。这个操作是从字符串的某个起始位置开始向后查找
     * 。直到找到第一个有效字符。
     * @param s 被操作的字符串。
     * @param entry 查找的起始位置。
     * @return 返回遇见的第一个有效字符的位置。
     */
    private static int next(String s, int entry) {
        for(int i = entry; i < s.length(); i++) {
            if (!EMPT_CHARS.contains(s.charAt(i))) return i;
        }
        return s.length();
    }

    /**
     * 简单JSON分析类的构造方法。通过一个字符串，构造出一个SimpleJSON类
     * 的实例。
     * @param s 被分析的字符串。
     * @return 返回 SimpleJSON实例。
     * @throws JSONFormatException 在解析过程中遇到任何错误都会抛出。
     */
    public static SimpleJSON parser(String s)
        throws JSONFormatException
    {
        JSONExpr json = new JSONExpr();
        int i = next(s, 0);
        assertLen(i, s);

        char c = s.charAt(i++);
        if (c != '{')
            throw new JSONFormatException(
                "JSONValue mast start '{'");

        i = JSONExpr.parser(s, i, json);
        i = next(s, ++i);
        if (i < s.length())
            throw new JSONFormatException(
                "After " + i
                    + ", the left charaters can't be explained.");

        return new SimpleJSON(json);
    }

    /**
     * 通过该函数，可以获得最根部的那个JSONExp表达式。通过这个表达式可以遍历
     * 每个key 以及对应的表达式。完成对json树的遍历。
     * @return 返回最根部的那个JSONExp 表达式。
     */
    public JSONExpr getJSON()
    {
        return _jsonExpression;
    }

    /**
     * 将SimpleJSON 实例对象以json格式字符串输出。
     * @return
     */
    public String toString()
    {
        return _jsonExpression.toString();
    }

    private final JSONExpr _jsonExpression;
    private SimpleJSON(JSONExpr jsonExpression)
    {
        _jsonExpression = jsonExpression;
    }

}
