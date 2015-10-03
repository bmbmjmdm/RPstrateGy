package Utilities;

import java.io.Serializable;

import database.Coord;
import database.Objs.PObjs.User;

/**
 * Created by Dale on 5/29/2015.
 */
public interface CoordCallable<V> extends Serializable {

    public V call(Coord c);
}
