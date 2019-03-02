package name.sayid.sql;

import name.sayid.common.StringsHelper;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static java.util.logging.Level.*;

/**
 * Implements maker interface. It performance is translate a json object.
 * @param <T>
 */
public class ValueMaker4JSON<T> implements ValueMaker<T>{
    private final static Logger L
            = Logger.getLogger(ValueMaker4JSON.class.getName());
    private final Class<T> _clazz;
    private SimpleJSON _json;

    /**
     * Constructor, It as a class for the made result.
     * @param clazz The class for made result.
     */
    public ValueMaker4JSON(Class<T> clazz) {
        _clazz = clazz;
    }

    /**
     * Give the resource a json object for translating to correct java object.
     * @param json It is translate from.
     * @return The object is this-self.
     */
    public ValueMaker4JSON<T>
    setJson(SimpleJSON json)
    {
        _json = json;
        return this;
    }

    /**
     * Return the making resource.
     * @return The making resource.
     */
    public SimpleJSON
    getJson()
    {
        return _json;
    }

    /**
     * The implement method. and the method is the effective of object.
     * @return
     * @throws ValueIllegal
     */
    public T makeValue() throws ValueIllegal {
        return makeValue(_clazz, _json.getJSON());
    }

    /**
     * It is helper for effectuation of make value from json object.
     * @param clazz The making result class type.
     * @param expr It will be translate.
     * @param <T>
     * @return The making result.
     * @throws ValueIllegal
     */
    public static <T> T
    makeValue(Class<T> clazz, SimpleJSON.JSONExpr expr)
            throws  ValueIllegal {
        try {
            T o = clazz.getConstructor().newInstance();
            for (Field field: o.getClass().getDeclaredFields()) {
                RespAnnotation annotation
                    = field.getAnnotation(RespAnnotation.class);
                String s = annotation.respField();
                if (s.isEmpty()) continue;

                boolean accessable = field.canAccess(o);
                field.setAccessible(true);
                if (!annotation.isList()) {
                    var v_k = expr.valueOfKey(s);
                    if (v_k == null || v_k.isEmpty()) {
                        L.log(WARNING, "There isn't the common key: " + s);
                        continue;
                    }
                    field.set(o, removeQuoteMark(v_k.toString()));
                } else {
                    var v_k = expr.valueOfKey(s);
                    if (v_k == null || v_k.isEmpty()) {
                        L.log(WARNING, "There isn't the list key: " + s);
                        continue;
                    }
                    List<SimpleJSON.JSONExpr> jsons = listFromExpression(v_k);
                    if (jsons.isEmpty()) {
                        field.set(o, List.of());
                    } else {
                        Class c = annotation.itemClass();
                        var listVal = jsons.stream().map(x->{
                            try {
                                return makeValue(c, x);
                            } catch (ValueIllegal ex) {
                                L.log(WARNING, ex.getMessage());
                                return null;
                            }
                        }).filter(x->x!=null).collect(Collectors.toList());
                        field.set(o, listVal);
                    }
                }
                field.setAccessible(accessable);
            }
            return o;
        } catch (Exception e) {
            throw new ValueIllegal(e);
        }
    }

    private static String addQuoteMark(String s) {
        s.replace("\"", "\\\"");
        return (new StringBuilder(s)).insert(0, "\"")
                .append("\"").toString();
    }

    private static String removeQuoteMark(String s) throws IOException {
        if (s == null) return null;
        if (s.isEmpty()) return s;
        int lsp = s.length() - 1;
        if (lsp == 0) return s;

        var s_= ((s.charAt(0) == '"' && s.charAt(lsp) == '"')
                ||(s.charAt(0) == '\'' && s.charAt(lsp) == '\'')) ?
                (new StringBuilder(s)).deleteCharAt(lsp)
                        .deleteCharAt(0).toString() : s;

        return StringsHelper.unescapeJava(s_);
    }

    private static List<SimpleJSON.JSONExpr>
    listFromExpression(SimpleJSON.Expression expression) throws ValueIllegal {
        if (!(expression instanceof SimpleJSON.ArrayExp))
            throw new
                    ValueIllegal("It should be a List object," +
                    " but it isn't correct!");
        var arr = (SimpleJSON.ArrayExp) expression;
        var ret = (new LinkedList<SimpleJSON.JSONExpr>());
        for (SimpleJSON.Expression member: arr.getMembers()) {
            if(!(member instanceof SimpleJSON.JSONExpr))
                throw new ValueIllegal("The members should" +
                        " must be a JSONExpr object");
            ret.add((SimpleJSON.JSONExpr)member);
        }
        return ret;
    }

}
