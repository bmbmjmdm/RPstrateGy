package Utilities;

import java.io.Serializable;

/**
 * Created by Dale on 1/11/2015.
 */
public class StringInt implements Serializable{
    String s;
    public int i;

    public StringInt(String S, int I){
        s = S;
        i = I;
    }

    public String toString()
    {
        return s;
    }

}
