package name.sayid.sql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * The record object is a metadata for one result set from a sql statement.
 */
public class Record {
    ResultSet _resultSet;

    String[]  _names;
    String[]  _values;
    String[]  _typeNames;
    Integer[] _types;

    /**
     * Set result set object,  The metadata will be retrieved from it.
     * @param _resultSet, It will be used to retrieve metadata.
     * @return The object is this-self.
     * @throws SQLException
     */
    public Record setCurrent(ResultSet _resultSet) throws SQLException {
        var metadata = _resultSet.getMetaData();
        int len = metadata.getColumnCount();
        _names      = new String[len];
        _values     = new String[len];
        _typeNames  = new String[len];
        _types      = new Integer[len];
        for(int i = 0; i < len ; i++) {
            int indx = i + 1;
            var name     = metadata.getColumnName(indx);
            var typeName = metadata.getColumnTypeName(indx);
            var type     = metadata.getColumnType(indx);
            var value    = _resultSet.getObject(indx).toString();

            _names[i]     = name;
            _values[i]    = value;
            _typeNames[i] = typeName;
            _types[i]     = type;
        }
        return this;
    }

    private <T> Map<String, T> makeMap(String[] ks, T[] vs) {
        return new AbstractMap<String, T>() {
            @Override
            public Set<Entry<String, T>> entrySet() {
                int len = ks.length;
                Entry<String, T> entries[] = new Entry[len];
                for(int i = 0; i < len; i++) {
                    entries[i] = new SimpleImmutableEntry(ks[i], vs[i]);
                }
                return Set.of(entries);
            }
        };
    }

    /**
     * From this method cant gain all name value pairs. The name is column name
     * the value is field value reference to the column.
     * @return A serial of name value pairs.
     */
    public Map<String, String>
    getNameValues()
    {
        return makeMap(_names, _values);
    }

    /**
     * From this method cant gain all name and type-name pairs.
     * The name is column name the type-name describes column type.
     * @return A serial of name and type-name pairs.
     */
    public Map<String, String>
    getNameTypeNames()
    {
        return makeMap(_names, _typeNames);
    }

    /**
     * From this method cant gain all name and type-name pairs.
     * The name is column name the column-type. The column-type
     * is identified by a integer value which reference in java api.
     * @return A serial of name and type-name pairs.
     */
    public Map<String, Integer>
    getNameTypes()
    {
        return makeMap(_names, _types);
    }

}
