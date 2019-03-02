package name.sayid.dom;

import java.util.Map;

public class SimpleElement implements IsolatedElement{
    private String _id;
    private String _tagname;
    private String _text;

    private Map<String, String> _attributes;

    @Override
    public String getId() {
        return _id;
    }

    public void setId(String id) {
        _id = id;
    }

    @Override
    public String getTagName() {
        return _tagname;
    }

    public void setTagName(String tagname) {
        _tagname = tagname;
    }

    @Override
    public Map<String, String> getAttributes() {
        return _attributes;
    }

    public void setAttributes(Map<String, String> attributes) {
        _attributes = attributes;
    }

    @Override
    public String getText() {
        return _text;
    }

    public void setText(String text) {
        _text = text;
    }
}
