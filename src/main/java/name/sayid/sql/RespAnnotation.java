package name.sayid.sql;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
/**
 * RespoAnnotation uses to annotate to a response object. Response object
 * is from a json format text content. it describes the relations between
 * json element, jopo property and table column.
 */
public @interface RespAnnotation {
    /**
     *  The json key
     * @return the kson key
     */
    String respField()           default "";

    /**
     * The table column name
     * @return the table column name.
     */
    String entityField()         default "";

    /**
     * Is the field a list class.
     * @return Is the field a list class.
     */
    boolean isList()             default false;

    /**
     * The class style for List element generic type.
     * @return The class style for List element generic type.
     */
    Class itemClass()            default Object.class;

    /**
     * Should add quote mark, when generating string value.
     * @return Should add quote mark, when generating string value.
     */
    boolean shouldAddQuoteMark() default false;
}
