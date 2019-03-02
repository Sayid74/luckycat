package name.sayid.sql;

import name.sayid.common.StringsHelper;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import static java.util.logging.Level.WARNING;

/**
 * This class is helper of making a insert sql statement.
 */
public class InsertSqlMaker {
    private final static Logger L
            = Logger.getLogger(InsertSqlMaker.class.getName());
    private final static String INSERT_SQL = "INSERT INTO %s(%s) VALUES (%s);";
    private final String _tableName;

    /**
     * Constructor, The object constructor must be gave a table name for parameter
     * used to insert sentence.
     * @param tableName Table name of insert sql statement
     */
    public InsertSqlMaker(String tableName) {
        _tableName = tableName;
    }

    /**
     * It can make a insert sentence. By the sentence, the parameter object
     * should be insert into the table named table name property.
     * @param obj It should be insert into the table. The input obj must use
     *            annotations to sign and describe how to map the property
     *            to the table column.
     * @return The insert sql statement has performance inserting the object
     * to the table.
     * @throws IllegalAccessException
     */
    public String makeSql(final Object obj) throws IllegalAccessException {
        final var hm = new HashMap<String, String>();
        Field fiels[] = obj.getClass().getDeclaredFields();
        for(Field x: fiels) {
            boolean b = x.canAccess(obj);
            x.setAccessible(true);
            try {
                var annotation = x.getAnnotation(RespAnnotation.class);
                String clnm = annotation.entityField();
                if (!clnm.isEmpty()) {
                    var v = x.get(obj);
                    if (v==null) {
                        hm.put(clnm, "null");
                    } else {
                        boolean quteReq = annotation.shouldAddQuoteMark();
                        if (quteReq) {
                            var v_ = StringsHelper.mysqlStrAddTrans(v.toString());
                            hm.put(clnm, "\'" + v_ + "\'");
                        } else {
                            String vs = v.toString();
                            hm.put(clnm, vs.trim());
                        }
                    }
                }
            } catch (IllegalAccessException e) {
                L.log(WARNING, e.getMessage());
                L.log(WARNING, StringsHelper.contextOfException(e));
                throw e;
            } finally {
                x.setAccessible(b);
            }
        }
        return make(hm);
    }

    private String make(Map<String, String> parameters) {
        if (parameters.isEmpty()) return null;
        String fields = String.join(",", parameters.keySet());
        String values = String.join(",", parameters.values());
        return String.format(INSERT_SQL, _tableName, fields, values);
    }
}
