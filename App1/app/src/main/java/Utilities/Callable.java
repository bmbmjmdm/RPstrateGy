package Utilities;

import java.io.Serializable;

/**
 * Created by Dale on 5/31/2015.
 */



public interface Callable<V> extends Serializable {
    public V call();
}
