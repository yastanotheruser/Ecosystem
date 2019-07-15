package ecosystem.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DataManager<T extends Unique> {
    private String filename;
    private String path;

    public ArrayList<T> list;
    protected final Map<String, T> map;

    public DataManager(String filename, boolean initialize) {
        list = new ArrayList<>();
        map = new HashMap();
        this.filename = filename;

        path = System.getenv("LOCALAPPDATA");
        if (path == null)
            path = ".";

        path += "\\Ecosystem\\";
        new File(path).mkdir();

        if (initialize)
            this.init();
    }

    public DataManager(String filename) {
        this(filename, false);
    }

    public boolean update() {
        try {
            FileOutputStream fout = new FileOutputStream(getFilePath());
            ObjectOutputStream oos = new ObjectOutputStream(fout);

            oos.writeObject(list);
            fout.close();
            oos.close();
            return true;
        } catch (FileNotFoundException ex) {
            Logger.getLogger(DataManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(DataManager.class.getName()).log(Level.SEVERE, null, ex);
        }

        return false;
    }

    public boolean add(T item, boolean update) {
        if (map.containsKey(item.getId()))
            return false;

        if (!list.add(item))
            throw new Error("An error occurred when adding a new user");

        map.put(item.getId(), item);
        if (update)
            return this.update();

        return true;
    }

    public boolean add(T item) {
        return add(item, true);
    }

    public boolean add(T[] items) {
        for (T item : items) {
            if (!this.add(item))
                return false;
        }

        return true;
    }

    public boolean addFrom(Class type, Class[] constructorTypes, Object[][] argsVector) {
        try {
            Constructor<T> constructor = type.getConstructor(constructorTypes);
            for (Object[] args : argsVector) {
                T instance = (T) constructor.newInstance(args);
                if (!this.add(instance, false))
                    return false;
            }

            return true;
        } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            Logger.getLogger(DataManager.class.getName()).log(Level.SEVERE, null, ex);
        }

        return false;
    }

    public boolean delete(String id, boolean update) {
        if (!list.removeIf(o -> o.is(id)))
            return false;

        if (map.remove(id) == null)
            return false;

        if (update)
            return this.update();

        return true;
    }

    public boolean delete(String id) {
        return delete(id, true);
    }

    public boolean delete(T item, boolean update) {
        if (!list.remove(item))
            return false;

        if (map.remove(item.getId()) == null)
            return false;

        if (update)
            return update();

        return true;
    }

    public boolean delete(T item) {
        return delete(item, true);
    }

    public T get(String id) {
        return map.get(id);
    }

    public final void init() {
        try {
            FileInputStream fin = new FileInputStream(path + filename);
            ObjectInputStream ois = new ObjectInputStream(fin);
            list = (ArrayList<T>) ois.readObject();
            fin.close();
            ois.close();
        } catch (FileNotFoundException ex) {
            update();
        } catch (IOException | ClassNotFoundException ex) {
            Logger.getLogger(DataManager.class.getName()).log(Level.SEVERE, null, ex);
        }

        for (T item : list)
            map.put(item.getId(), item);
    }

    public boolean clear() {
        list.clear();
        map.clear();
        return update();
    }

    public boolean exists(String id) {
        return map.containsKey(id);
    }

    public boolean updateEntry(T item, String newId, boolean updateFile) {
        if (exists(newId))
            return false;

        map.remove(item.getId());
        map.put(newId, item);
        item.setId(newId);

        if (!updateFile)
            return true;

        return update();
    }

    public boolean updateEntry(T item, String newId) {
        return this.updateEntry(item, newId, true);
    }

    public boolean updateEntry(T item, T newItem, boolean updateFile) {
        if (exists(newItem.getId()))
            return false;

        if (!delete(item, false))
            return false;

        add(newItem, false);
        if (!updateFile)
            return true;

        return update();
    }

    public boolean updateEntry(T item, T newItem) {
        return this.updateEntry(item, newItem, true);
    }

    public String getFilePath() {
        return path + filename;
    }

    public File getFile() {
        return new File(getFilePath());
    }
}
