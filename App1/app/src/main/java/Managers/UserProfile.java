package Managers;

import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;

import database.Players.CharacterPlayer;

/**
 * Created by Dale on 1/2/2015.
 */
public final class UserProfile implements Serializable{
    static UserProfile inst = null;
    public int idCounter = 0;
    public CharacterPlayer curChar = null;



    private UserProfile(){

    }

    public static UserProfile getInstance(){
        if(inst == null) {
            FileInputStream fis = null;
            ObjectInputStream ois = null;
            try {
                fis = new FileInputStream(Environment.getExternalStorageDirectory() +"/RPstrateGy/profile");
                ois = new ObjectInputStream(fis);
                inst = (UserProfile) ois.readObject();
                fis.close();
                ois.close();
            } catch (Exception e) {
                try {
                    if (fis != null)
                        fis.close();
                } catch (Exception r) {
                }
                try {
                    if (ois != null)
                        ois.close();
                } catch (Exception r) {
                }
                inst = new UserProfile();
            }
        }
        return inst;
     }



    public void saveProfile() throws IOException {
            FileOutputStream fos = null;
            ObjectOutputStream oos = null;
            try {
                fos = new FileOutputStream(Environment.getExternalStorageDirectory() +"/RPstrateGy/profile");
                oos = new ObjectOutputStream(fos);
                oos.writeObject(this);
                oos.flush();
                fos.close();
                oos.close();
            } catch (IOException e) {
                try {
                    if (fos != null)
                        fos.close();
                } catch (Exception p) {
                }

                try {
                    if (oos != null)
                        oos.close();
                } catch (Exception p) {
                }

                throw e;
            }
    }

    public void saveChar() throws Exception {
        FileOutputStream fos = null;
        ObjectOutputStream oos = null;
        try{
            fos = new FileOutputStream(Environment.getExternalStorageDirectory() +"/RPstrateGy/characters/"+curChar.charId);
            oos = new ObjectOutputStream(fos);
            oos.writeObject(curChar);
            oos.flush();
            fos.close();
            oos.close();
        } catch (Exception e) {
            try {
                if (fos != null)
                    fos.close();
            } catch (Exception p) {
            }

            try {
                if (oos != null)
                    oos.close();
            } catch (Exception p) {
            }

            throw e;
        }

        saveProfile();
    }

    public void loadChar(int id) throws Exception {
        FileInputStream fis = null;
        ObjectInputStream ois = null;
        try{
            fis = new FileInputStream(Environment.getExternalStorageDirectory() +"/RPstrateGy/characters/"+id);
            ois = new ObjectInputStream(fis);
            curChar = (CharacterPlayer) ois.readObject();
            fis.close();
            ois.close();
        } catch (Exception e) {
            try {
                if (fis != null)
                    fis.close();
            } catch (Exception p) {
            }

            try {
                if (ois != null)
                    ois.close();
            } catch (Exception p) {
            }

            throw e;
        }
        saveProfile();
    }

    public HashMap<Integer, String> getAllChar() throws Exception {
        HashMap<Integer, String> reMe = new HashMap<Integer, String>();
        File charFold = new File(Environment.getExternalStorageDirectory() +"/RPstrateGy/characters");
        File[] fs = charFold.listFiles();
        for(File f :  fs){
            FileInputStream fis = null;
            ObjectInputStream ois = null;
            try {
                fis = new FileInputStream(Environment.getExternalStorageDirectory() +"/RPstrateGy/characters/" + f.getName());
                ois = new ObjectInputStream(fis);
                CharacterPlayer c = (CharacterPlayer) ois.readObject();
                reMe.put(c.charId, c.name);
                fis.close();
                ois.close();
            } catch (Exception e) {
                try {
                    if (fis != null)
                        fis.close();
                } catch (Exception p) {
                }

                try {
                    if (ois != null)
                        ois.close();
                } catch (Exception p) {
                }

                throw e;
            }
        }
        return reMe;
    }

    public void deleteChar() throws Exception {
        File f = new File(Environment.getExternalStorageDirectory()+"/RPstrateGy/characters/"+curChar.charId);
        f.delete();
        curChar = null;
        File charFold = new File(Environment.getExternalStorageDirectory() +"/RPstrateGy/characters");
        File[] fs = charFold.listFiles();
        if(fs.length > 0)
            loadChar(Integer.decode(fs[0].getName()));
        saveProfile();
    }

    public HashMap<Integer, String> getFriendly() throws Exception{
        HashMap<Integer, String> reMe = new HashMap<Integer, String>();
        File charFold = new File(Environment.getExternalStorageDirectory() + "/RPstrateGy/friendly/" + curChar.charId);
        //incase doesnt exist
        charFold.mkdir();
        File[] fs = charFold.listFiles();
        for(File f :  fs){
            String fn = f.getName();
            if(fn.contains("Name")) {
                //id+Name
                FileInputStream fis = null;
                ObjectInputStream ois = null;
                try {
                    fis = new FileInputStream(Environment.getExternalStorageDirectory() + "/RPstrateGy/friendly/" + curChar.charId + "/" + fn);
                    ois = new ObjectInputStream(fis);
                    String s = (String) ois.readObject();
                    //get JUST id
                    reMe.put(Integer.decode(fn.substring(0, fn.length()-4)), s);
                    fis.close();
                    ois.close();
                } catch (Exception e) {
                    try {
                        if (fis != null)
                            fis.close();
                    } catch (Exception p) {
                    }

                    try {
                        if (ois != null)
                            ois.close();
                    } catch (Exception p) {
                    }

                    throw e;
                }
            }
        }
        return reMe;
    }




    public int getId() throws IOException {
        idCounter++;
        saveProfile();
        return idCounter;
    }
}
