import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class LazyMap extends HashMap<String, Object> {

    private JSONObject self;

    public LazyMap() {
    }

    @SuppressWarnings("unchecked")
    public <T> T getValue(String key) throws NullPointerException {
        if (self == null)
            self = new JSONObject(this);
        return (T) getValue(self, key);
    }

    public Object query(String jsonPath) {
        if (self == null)
            self = new JSONObject(this);
        return self.query(jsonPath);
    }

    private Object getValue(Object obj, String key) {
        Object value = null;
        if (obj instanceof JSONObject) {
            JSONObject tmp = (JSONObject) obj;
            if (tmp.has(key)) {
                return tmp.get(key);
            } else {
                List<String> keys = Arrays.asList(JSONObject.getNames(tmp));
                for (String k : keys) {
                    value = getValue(tmp.get(k), key);
                    if (value != null)
                        return value;
                }
            }
        } else if (obj instanceof JSONArray) {
            JSONArray tmp = (JSONArray) obj;
            if (tmp.isEmpty()) {
                return null;
            } else {
                for (int i = 0; i < tmp.length(); i++) {
                    value = getValue(tmp.get(i), key);
                    if (value != null)
                        return value;
                }
            }
        }
        return value;
    }
}
