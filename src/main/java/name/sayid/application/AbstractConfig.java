package name.sayid.application;

import name.sayid.common.StringsHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static name.sayid.application.PropertyAnnotation.*;

public abstract class AbstractConfig {
    private static Logger L
            = Logger.getLogger(AbstractConfig.class.getName());
    private HashMap<String, String> _customProperties = new HashMap<>();
    private HashMap<String, String> _systemProperties = new HashMap<>();

    /**
     * You can custom configure at this place by implements this method.
     * It must be called when a configure being initialize. When you want
     * do custom you should add custom options to customProperties.
     * @param customProperties you can add custom options to it.
     * @throws ConfigException When you find some option isn't right you
     * can throws the exception to caller and finished initialing.
     */
    public abstract void init(HashMap customProperties) throws ConfigException;

    /**
     * Binds a value to a field with fieldName. The method is protected.
     * It gives an opportunity for custom some value to field. When the
     * not string value field doesn't have a string parameter method.
     *
     * @param fieldName The name of the field should be binding value.
     *                  In another said the field has not a set method
     *                  with a only string method.
     *
     * @return The method returns an object, It is translate from the
     *         input value.
     */
    protected Object
    bindValue(String fieldName, String value) {
        Field field;
        try {
            field = this.getClass().getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            return null;
        }
        return field.getType() == String.class ? value : null;
    }

    /**
     * Setup means set all properties values in a properties file to
     * a corresponding annotated field of this class implementation.
     * It my be strigger bindValue method if you custome it.
     */
    public void
    setup() throws ConfigException
    {
        setDefValues();
        init(_customProperties);
        setFields();
        checkFields();
    }

    /**
     * It returns all names of fields annotated.
     * @return The set includs all the names of annotated fields.
     */
    public Set<String> configFields( )
    {
        return Arrays.stream(AbstractConfig.class.getDeclaredFields())
                .map(Field::getName).collect(Collectors.toSet());
    }

    /**
     * It gives an opportunity of retrieving properties file by youself,
     * if you implements it. But, if shoulden't it sends a default
     * processing for you. The subclass instance can call it for a
     * default performance loading a stream from a properties file.
     * @param sourceFile the properties file
     * @return It returns a map structure. Every entry key is property's
     *         name and value is property's value. the properties is
     *         in the properties file.
     *
     */
    protected final Map<Object, Object>
    readProperties(File sourceFile)
            throws IOException
    {
        try(var in = new FileInputStream(sourceFile)) {
            return loadConfig(in);
        }
    }

    private void
    setDefValues() throws ConfigException
    {
        Map<Object, Object> defCnf = null;
        try {
            defCnf = defaultConfig();
        } catch (IOException e) {
            throw new ConfigException(e);
        }

        var fields = this.getClass().getDeclaredFields();
        for (var field: fields) {
            var ppt = field.getDeclaredAnnotation(Property.class);
            if (ppt == null) continue;
            boolean accessible = field.canAccess(this);
            field.setAccessible(true);
            try {
                if (ppt != null) {
                    var from = ppt.from();
                    var name = ppt.at();
                    if (CONFIG.equals(from)) {
                        if (defCnf.containsKey(name)) {
                            String value = defCnf.get(name).toString();
                            _customProperties.put(name, value);
                        }
                    } else if(SYSTEM.equals(from)) {
                        String value = System.getProperty(name);
                        if (value != null)
                            _systemProperties.put(name, value);
                    } else {
                        throw new ConfigException(
                                "Illegal property from : " + from);
                    }
                }
            } finally {
                field.setAccessible(accessible);
            }
        }
    }

    private void setFields()
            throws ConfigException
    {
        var fields = this.getClass().getDeclaredFields();
        for (var field: fields) {
            var ppt = field.getDeclaredAnnotation(Property.class);
            if (ppt == null) continue;
            boolean accessible = field.canAccess(this);
            field.setAccessible(true);
            try {
                if (ppt != null) {
                    var from = ppt.from();
                    var name = ppt.at();
                    if (CONFIG.equals(from)) {
                        String value = _customProperties.get(name);
                        setValueOn(field, value);
                    } else if(SYSTEM.equals(from)) {
                        String value = _systemProperties.get(name);
                        setValueOn(field, value);
                    } else {
                        throw new ConfigException(
                                "Illegal property from : " + from);
                    }
                }
            } finally {
                field.setAccessible(accessible);
            }
        }

    }

    private void checkFields() throws ConfigException
    {
        for (Field f :this.getClass().getDeclaredFields()) {
            var must = f.getDeclaredAnnotation(Requist.class);
            if (must == null) continue;
            boolean accessible = f.canAccess(this);
            f.setAccessible(true);
            try {
                if (f.get(this) == null) {
                    throw new ConfigException(
                        "Field \"" + f.getName() + "\" must not null!" );
                }
            } catch (IllegalAccessException e) {
                L.log(Level.SEVERE, StringsHelper.contextOfException(e));
                e.printStackTrace();
            } finally {
                f.setAccessible(accessible);
            }
        }
    }

    private Map<Object, Object>
    defaultConfig()
            throws IOException
    {
        try (var in = this.getClass().getClassLoader()
                .getResourceAsStream("default.properties")) {
            return loadConfig(in);
        }
    }

    private Map<Object, Object>
    loadConfig(InputStream is)
            throws IOException
    {
        var ppt = new Properties();
        ppt.load(is);
        return new AbstractMap<>() {
            @Override
            public Set<Entry<Object, Object>> entrySet() {
                return  ppt.entrySet();
            }
        };
    }

    private Method
    setMethodOf(String fieldName)
    {
        StringBuilder sb = new StringBuilder(fieldName);
        if (sb.charAt(0) == '_') sb.deleteCharAt(0);
        sb.setCharAt(0, Character.toUpperCase(sb.charAt(0)));
        String methodName = sb.insert(0,"set").toString();
        try {
            return this.getClass().getMethod(methodName, String.class);
        } catch (NoSuchMethodException ex) {
            return null;
        }
    }

    private void
    setValueOn(Field field, String value)
            throws ConfigException
    {
        String fieldName = field.getName();
        boolean accessible = field.canAccess(this);
        field.setAccessible(true);
        try {
            Method m = setMethodOf(fieldName);
            if (m != null)
                m.invoke(this, value);
            else
                field.set(this, bindValue(fieldName, value));
        } catch (Exception ex) {
            throw new ConfigException(ex);
        } finally {
            field.setAccessible(accessible);
        }
    }


    public static String
    getOS()
    {
        return System.getProperty("os.name");
    }

    public static boolean
    isWindows()
    {
        String s = getOS();
        return s != null && s.startsWith("window");
    }
}
