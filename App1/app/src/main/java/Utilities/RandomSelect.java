package Utilities;

import java.util.Collection;

/**
 * Created by Dale on 10/25/2015.
 */
public class RandomSelect<T> {

    //DO NOT CALL THIS WITH A COLLECTION OF SIZE LESS THAN 1 OR IT WILL RETURN NULL
    public T getRandom(Collection<T> collection){
        double ran = Math.random();
        int index = (int) Math.round(ran * (collection.size()-1));
        int looking = 0;
        for(T element: collection){
            if(looking == index){
                return element;
            }

            looking++;
        }

        return null;
    }
}
