package name.sayid.dom;

import java.util.Map;
import java.util.function.Supplier;

/**
 * This interface describes a isolated dom element. By the describes,
 * It can translate the element to a json format string.
 */
public interface IsolatedElement
{
    /**
     * The id property of a dom element.
     * @return Id property.
     */
    String getId();

    /**
     * The tag name property of a dom element.
     * @return The tag name.
     */
    String getTagName();

    /**
     * The getter of getting text attribute content.
     * @return text attribute content.
     */
    String getText();
    Map<String, String> getAttributes();

    /**
     * Translate dom element to json format string.
     * @return Json format string.
     */
    default String toJsonString() {
        Supplier<String> dataset =() -> {
            String sf = "{\"%s\":\"%s\"}";
            var es = (String[])getAttributes().entrySet().stream()
                .map((a) ->String.format(sf, a.getKey(), a.getValue()))
                .toArray();
            return new StringBuilder('[')
                .append(String.join(",", es))
                .append(']').toString();
        };

        return new StringBuilder("\"id\":")
            .append('"').append(getId()).append('"')
            .append("\"tagName\":")
            .append('"').append(getTagName()).append('"')
            .append("\"dataset\":")
            .append(dataset.get())
            .toString();
    }
}
