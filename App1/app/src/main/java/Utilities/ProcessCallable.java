package Utilities;

import java.io.Serializable;

/**
 * Created by Dale on 5/31/2015.
 */



public interface ProcessCallable extends Serializable {
    public void call(boolean passed);
}
