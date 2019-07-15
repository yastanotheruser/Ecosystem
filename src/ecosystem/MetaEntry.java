package ecosystem;

import ecosystem.util.Unique;

class MetaEntry extends Unique {
    private Object value;

    public MetaEntry(String key, Object value) {
        super(key);
        this.value = value;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }
}
