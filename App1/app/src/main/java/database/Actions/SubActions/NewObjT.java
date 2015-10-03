package database.Actions.SubActions;

import android.util.Log;

import java.lang.reflect.InvocationTargetException;

import Managers.GameManager;
import Utilities.ArgCaller;
import Utilities.RemovedException;
import database.ObjT.ObjT;
import database.Objs.Obj;
import database.Objs.PObjs.User;
import database.State;

/**
 * Created by Dale on 7/3/2015.
 */
public class NewObjT extends SubAction{
    Class newObjT;
    Object[] args;
    Integer ownerID = null;
    ObjTModCallable modObjT = null;

    Integer speedPosition = null;

    int speedMod = 0;

    Integer newObjTID;
    public ArgCaller<Integer> getObjTID = new ArgCaller<Integer>() {
        @Override
        public Integer call() {
            return newObjTID;
        }
    };

    public NewObjT(Class objTClass, Object[] args){
        this.args = args;
        newObjT = objTClass;
    }

    //this constructor is used if the passed "speed" parameter needs to be an argument. In which case, it will be added to the args list at the given position
    //if this is the case, be sure that the argument in args at index position is null (it will be thrown away)
    public NewObjT(Class objTClass, int position, Object[] args){
        this.args = args;
        newObjT = objTClass;
        speedPosition = position;
    }

    //same as last one, but mod the given speed by speedMod
    public NewObjT(Class objTClass, int position, int speedMod, Object[] args){
        this.args = args;
        newObjT = objTClass;
        speedPosition = position;
        this.speedMod = speedMod;
    }


    public void setOwnerID(Integer i){
        ownerID = i;
    }

    public void setModObjT(ObjTModCallable i){
        modObjT = i;
    }



    private void updateArgs(int speed){
        if(speedPosition != null){
            args[speedPosition] = speed + speedMod;
        }

        for(int i = 0; i<args.length; i++){
            if(args[i] instanceof ArgCaller){
                args[i] = ((ArgCaller) args[i]).call();
            }
        }
    }

    public void useContinue(User u, int speed){
        updateArgs(speed);
        try {
            ObjT newlyMade = (ObjT) newObjT.getConstructors()[0].newInstance(args);

            newObjTID = newlyMade.id;


            if(modObjT != null){
                modObjT.call(newlyMade, u);
            }


            State s = GameManager.getInstance().getState();

            if(ownerID != null){
                try{
                    Obj o = s.getObjID(ownerID);
                    o.addType(newlyMade);
                }
                catch(RemovedException e){
                    Log.e("NewObjT>useContinue", "Can't find owner for new ObjT: "+ownerID);
                }
            }

        }
        catch(InstantiationException | IllegalAccessException | InvocationTargetException e){
            Class[] parameterTypes = new Class[args.length];

            for(int i = 0; i < args.length; i++){
                parameterTypes[i] = args[i].getClass();
            }

            String argumentsString = "";
            for(Class c: parameterTypes){
                argumentsString = argumentsString + c + ", ";
            }
            Log.e("NewObjT>useContinue", "improper class, constructor or arguments, unable to create new ObjT. Class: " + newObjT+". Arguments Classes: "+argumentsString);
        }
    }

    //if you want the ObjT to be added at useWhen, set eotTime to anything but null
    public void useStop(User u, int speed){
        updateArgs(speed);
         try {
            ObjT newlyMade = (ObjT) newObjT.getConstructors()[0].newInstance(args);

            newObjTID = newlyMade.id;



            if(modObjT != null){
                modObjT.call(newlyMade, u);
            }



            State s = GameManager.getInstance().getState();

            if(ownerID != null){
                try{
                    Obj o = s.getObjID(ownerID);
                    o.addType(newlyMade);
                }
                catch(RemovedException e){
                    Log.e("NewObjT>useStop", "Can't find owner for new ObjT: "+ownerID);
                }
            }

        }
        catch(InstantiationException | IllegalAccessException | InvocationTargetException e){
            Class[] parameterTypes = new Class[args.length];

            for(int i = 0; i < args.length; i++){
                parameterTypes[i] = args[i].getClass();
            }


            String argumentsString = "";
            for(Class c: parameterTypes){
                argumentsString = argumentsString + c + ", ";
            }
            Log.e("NewObjT>useStop", "improper class, constructor or arguments, unable to create new ObjT. Class: " + newObjT+". Arguments Classes: "+argumentsString);
        }
    }
}
