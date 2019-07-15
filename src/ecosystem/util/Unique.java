package ecosystem.util;

import java.io.Serializable;

public abstract class Unique implements Serializable {
    private String id;

    public Unique(String id) {
        this.id = id;
    }

    public boolean is(String id) {
        return this.id.equals(id);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
