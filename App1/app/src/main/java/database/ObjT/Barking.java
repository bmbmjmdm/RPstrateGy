package database.ObjT;

/**
 * Created by Dale on 4/13/2015.
 */
public class Barking extends ObjT {
    public double intimidation;

    public Barking(int oID, double intimidating){
        super("Barking", -1, oID, 0);
        intimidation = intimidating;

    }

}
