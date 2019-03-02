package name.sayid.sql;

import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.Retention;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.function.Function;
import java.util.logging.Logger;
import static java.util.logging.Level.*;

public class SimpleJSONMaker
{
    private final static Logger L =
        Logger.getLogger(SimpleJSONMaker.class.getName());

    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface SimpleField
    {
        /**
         * The json element key name.
         * @return The json element key name.
         */
        String handleName();

        /**
         * It ask the corresponding value is from the correct getter method.
         * @return The getter method name.
         */
        String getFuncName();
    }

    /**
     * Make a json object from input object. The json object can serialize
     * to a json format text.
     * @param o The object will be serialized to json object.
     * @return A json object
     * @throws SimpleJSON.JSONFormatException
     */
    public static SimpleJSON makeFromFieldsOf(Object o)
        throws SimpleJSON.JSONFormatException
    {
        Map<String, String> m = fieldsToMap(o);
        return makeFromStringPair(m);
    }

    /**
     * Make a json object from a serial of string pair. In a pair, first
     * represents name and second is value.
     * @param m The serial of string pair organize to a map object.
     * @return A map object contains a serial of string pair.
     * @throws SimpleJSON.JSONFormatException
     */
    public static SimpleJSON makeFromStringPair(Map<String, String> m)
        throws SimpleJSON.JSONFormatException
    {
        if (m.isEmpty()) return null;

        StringBuilder sb = m.entrySet().stream().collect(StringBuilder::new
            , (x, y) -> x.append(",\"").append(y.getKey()).
                append("\":").append(y.getValue())
            , (x1, x2) -> x1.append(x2));

        sb.append('}').setCharAt(0,'{');
        return SimpleJSON.parser(sb.toString());
    }

    /**
     * SimpleJSON.Expression is the value in a json field. One json field composed
     * by name and value. This method make a json object by a serial of name and
     * SimpleJSON.Expression pairs.
     * @param members A map contains a serial name and SimpleJSON.Expression pairs.
     * @return A json object from members.
     * @throws SimpleJSON.JSONFormatException
     */
    public static SimpleJSON
    makeFromKeyExpr(Map<String, SimpleJSON.Expression> members)
            throws SimpleJSON.JSONFormatException
    {

        StringBuilder sb = members.entrySet().stream()
                .collect( StringBuilder::new
                        , (b, x)->b.append(String.format("\"%s\":%s,"
                                , x.getKey(), x.getValue()))
                        , (l, r)->l.append(r));

        sb.deleteCharAt(sb.length() - 1).insert(0,"{").append("}");
        return SimpleJSON.parser(sb.toString());
    }

    /**
     * Translate a json object to a serial of strings pair. every item is name
     * and value. The value is represented by a string.
     * @param obj The object will be translated.
     * @return A map object is translated from a json object.
     */
    public static Map<String, String> fieldsToMap(final Object obj) {
        Class<?> clazz = obj.getClass();
        Field[] fields = clazz.getDeclaredFields();

        Function<Field, String[]> f = (x) ->{
            try {
                SimpleField sfld = x.getAnnotation(SimpleField.class);
                String name = sfld.handleName();
                String funm = sfld.getFuncName();
                Method mthd = clazz.getMethod(funm);
                String retn = (String)mthd.invoke(obj);
                return new String[] {name, retn};
            } catch (Exception ex) {
                L.log(WARNING, ex.getMessage());
                return null;
            }
        };

        return Arrays.stream(fields)
            .filter(x->x.isAnnotationPresent(SimpleField.class))
            .map(f::apply)
            .filter(x-> x != null)
            .collect(Collectors.toMap(x->x[0], x->x[1] == null? "null": x[1]));
    }

    /**
     * It translate a string list to a json object. The json object has one field
     * named value and the field references to an array type value.
     * @param lines It is a string list. every item string can be translate to
     *              a SimpleJSON.Expression.
     * @return A json object which is a expression the name is static "value".
     * @throws SimpleJSON.JSONFormatException
     */
    public static SimpleJSON.Expression makeExpFromList(List<String> lines)
        throws SimpleJSON.JSONFormatException
    {
        var sb = new StringBuilder("{\"value\":[")
            .append(String.join(",", lines)).append("]}");
        return SimpleJSON.parser(sb.toString()).getJSON().valueOfKey("value");
    }
}
