package name.sayid.sql;

import java.sql.ResultSet;
import java.util.LinkedList;
import java.util.List;

/**
 * Make a serial values which are from a result set.
 * @param <T> The item of values, is encapsulate to generics class T.
 */
public class MakeValues4ResultSet<T> {
    private ResultSet _resultSet;

    private final Class<T> _clazz;

    /**
     * Constructor, It ask the class object as input parameter.
     * @param clazz, The class is item class.
     */
    public MakeValues4ResultSet (Class<T> clazz) {
        _clazz = clazz;
    }

    /**
     * Input a result set for making action.
     * @param resultSet The result set from making.
     * @return The object is this-self.
     */
    public MakeValues4ResultSet
    set(ResultSet resultSet)
    {
        _resultSet = resultSet;
        return this;
    }

    /**
     * The getter of resultSet property.
     * @return
     */
    public ResultSet
    getResultSet()
    {
        return _resultSet;
    }

    /**
     * Set the all values in the result set to a list object.
     * @return The list object contains all values in result set.
     * @throws ValueIllegal
     */
    public List<T> makeValues() throws ValueIllegal
    {
        var ret = new LinkedList<T>();
        var maker = new ValueMaker4Record<T>(_clazz);
        try {
            var record = new Record();
            while (_resultSet.next()) {
                record.setCurrent(_resultSet);
                ret.add(maker.setRecord(record).makeValue());
            }
            return ret;
        } catch (Exception ex) {
            throw new ValueIllegal(ex);
        }
    }
}
