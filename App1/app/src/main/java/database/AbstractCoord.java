package database;

/**
 * Created by Dale on 6/13/2015.
 */
public class AbstractCoord {
    public int x;
    public int y;
    public int z;

    public AbstractCoord(int X, int Y){
        x = X;
        y = Y;
        z = 0;
    }

    public AbstractCoord(int X, int Y, int Z){
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
}
