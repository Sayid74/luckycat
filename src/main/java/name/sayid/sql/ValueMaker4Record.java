package name.sayid.sql;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * Make value from a recode. The recode describes a item of a JDBC ResultSet
 * object.
 * @param <T> The made result's class type
 */
public class ValueMaker4Record<T> implements ValueMaker<T>{

    private final Class<T> _clazz;
    private Record _record;

    /**
     * Constructor, It ask a class for made result.
     * @param clazz Class type for made result.
     */
    public ValueMaker4Record(Class<T> clazz)
    {
        _clazz = clazz;
    }

    /**
     * Give the record for making.
     * @param record It is for make.
     * @return The object is this-self.
     */
    public ValueMaker4Record<T>
    setRecord(Record record)
    {
        _record = record;
        return this;
    }

    /**
     * The record property getter.
     * @return record property.
     */
    public Record
    getRecord()
    {
        return _record;
    }

    /**
     * Implement make method. The making is from recode object.
     * @return making result.
     * @throws ValueIllegal
     */
    @Override public T
    makeValue() throws ValueIllegal
    {
        Map<String, String> nameValue = _record.getNameValues();
        try {
            T o = _clazz.getConstructor().newInstance();
            for (Field field: o.getClass().getDeclaredFields()) {
                RespAnnotation annotation
                        = field.getAnnotation(RespAnnotation.class);
                String s = annotation.entityField();
                if (s.isEmpty()) continue;

                boolean accessable = field.canAccess(o);
                try {
                    field.setAccessible(true);
                    var value = nameValue.get(s);
                    field.set(o, value == null? "null" : value);
                } finally {
                    field.setAccessible(accessable);
                }
            }
            return o;
        } catch (Exception ex) {
            throw new ValueIllegal(ex);
        }
    }
}
