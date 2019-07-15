package ecosystem;

import ecosystem.util.DataManager;

public class MetaManager extends DataManager<MetaEntry> {
    public MetaManager(String filename) {
        super(filename);
    }

    public Object getItem(String key) {
        return get(key).getValue();
    }

    public void setItem(String key, Object value) {
        get(key).setValue(value);
    }
}