package ecosystem;

import ecosystem.util.DataManager;

public class MetaManager extends DataManager<MetaEntry> {
    public MetaManager(String filename) {
        super(filename);
    }

    public Object getItem(String key) {
        MetaEntry entry = this.get(key);
        if (entry == null)
            return null;

        return entry.getValue();
    }

    public boolean setItem(String key, Object value) {
        MetaEntry entry = this.get(key);
        if (entry == null) {
            entry = new MetaEntry(key, value);
            this.add(entry);
        } else {
            entry.setValue(value);
        }

        return update();
    }
}