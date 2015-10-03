package Utilities;

import java.io.Serializable;

import database.Objs.PObjs.User;

/**
 * Created by Dale on 5/29/2015.
 */
public interface UserCallable<V> extends Serializable {

    public V call(User u);
}
