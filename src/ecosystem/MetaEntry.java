package ecosystem;

import ecosystem.util.Unique;
import java.io.Serializable;

class MetaEntry extends Unique implements Serializable {
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
