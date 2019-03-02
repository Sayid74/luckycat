package name.sayid.application;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public class PropertyAnnotation {

    /**
     * It is used for Property annotation of from element.
     * It denotes the origin is from properties file.
     */
    public static final String CONFIG = "config";

    /**
     * It is used for Property annotation of from element.
     * It denotes the origin is from System Propeties.
     */
    public static final String SYSTEM = "system";

    /**
     * It orders the field must with a not null value.
     */
    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Requist { }

    /**
     * It describs the field value origin. The value should from properties
     * file or System propery and what name in the properties
     * set.
     */
    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Property {
        String from() default CONFIG;
        String at();
    }

}
