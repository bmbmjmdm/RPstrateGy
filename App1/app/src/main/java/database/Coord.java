package database;

import java.io.Serializable;

/**
 * Created by Dale on 12/29/2014.
 */
public class Coord implements Serializable {
    final public int x;
    final public int y;
    final public int z;

    public Coord(int X, int Y){
        x = X;
        y = Y;
        z = 0;
    }

    public Coord(int X, int Y, int Z){
        x = X;
        y = Y;
        z = Z;
    }

    public boolean eq(Coord c){
        if(c.x == this.x)
            if(c.y == this.y)
                if(c.z == this.z)
                    return true;

        return false;
    }

    @Override
    public boolean equals(Object obj){
        if(obj instanceof Coord){
            Coord c = (Coord) obj;
            if(c.x == this.x)
                if(c.y == this.y)
                    if(c.z == this.z)
                        return true;
        }

        return false;
    }

    @Override
    public int hashCode() {
        int result = x;
        result = 31 * result + y;
        result = 31 * result + z;
        return result;
    }


    //gives the x,y distance, in the shortest path possible
    public int distance(Coord c){
        int xMod = Math.abs(x - c.x);
        int yMod = Math.abs(y - c.y);

        return Math.max(xMod, yMod);
    }
}
