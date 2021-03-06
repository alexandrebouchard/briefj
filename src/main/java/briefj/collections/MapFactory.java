package briefj.collections;

import java.io.Serializable;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.WeakHashMap;

/**
 * The MapFactory is a mechanism for specifying what kind of map is to be used
 * by some object.  For example, if you want a Counter which is backed by an
 * IdentityHashMap instead of the default HashMap, you can pass in an
 * IdentityHashMapFactory.
 *
 * @author Dan Klein
 */

public abstract class MapFactory<K,V> implements Serializable {
  private static final long serialVersionUID = 5724671156522771657L;
  public static class LinkedHashMapFactory<K,V> extends MapFactory<K,V> {
    private static final long serialVersionUID = 5071310986893643248L;
    public Map<K,V> buildMap() {
      return new LinkedHashMap<K,V>();
    }
  }

  public static class IdentityHashMapFactory<K,V> extends MapFactory<K,V> {
    private static final long serialVersionUID = 5071311986893643248L;
    public Map<K,V> buildMap() {
      return new IdentityHashMap<K,V>();
    }
  }

  public static class TreeMapFactory<K,V> extends MapFactory<K,V> {
    private static final long serialVersionUID = 5071310986993643248L;
    public Map<K,V> buildMap() {
      return new TreeMap<K,V>();
    }
  }

  public static class WeakHashMapFactory<K,V> extends MapFactory<K,V> {
    private static final long serialVersionUID = 5071311186893643248L;
    public Map<K,V> buildMap() {
      return new WeakHashMap<K,V>();
    }
  }

  public abstract Map<K,V> buildMap();
}

