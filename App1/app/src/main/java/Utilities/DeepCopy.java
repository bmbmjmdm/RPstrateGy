package Utilities;

import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;

/**
 * Utility for making deep copies (vs. clone()'s shallow copies) of
 * objects. Objects are first serialized and then deserialized. Error
 * checking is fairly minimal in this implementation. If an object is
 * encountered that cannot be serialized (or that references an object
 * that cannot be serialized) an error is printed to System.err and
 * null is returned. Depending on your specific application, it might
 * make more sense to have copy(...) re-throw the exception.
 */
public class DeepCopy {

    /**
     * Returns a copy of the object, or null if the object cannot
     * be serialized.
     */
    public static Object copy(Object orig) throws IOException, ClassNotFoundException{
        Object obj = null;
        FastByteArrayOutputStream fbos = null;
        ObjectOutputStream out = null;
        ObjectInputStream in = null;

        try {
            // Write the object out to a byte array
            fbos = new FastByteArrayOutputStream();
            out = new ObjectOutputStream(fbos);
            out.writeObject(orig);
            out.flush();
            out.close();
            fbos.close();

            // Retrieve an input stream from the byte array and read
            // a copy of the object back in.
            in = new ObjectInputStream(fbos.getInputStream());
            obj = in.readObject();
            in.close();
        }
        catch(IOException e) {
            e.printStackTrace();
            if(fbos != null) {
                try {
                    fbos.close();
                } catch (Exception x) {
                }
            }
            if(out != null) {
                try {
                    out.close();
                } catch (Exception x) {
                }
            }
            if(in != null) {
                try {
                    in.close();
                } catch (Exception x) {
                }
            }
            throw e;
        }
        catch(ClassNotFoundException cnfe) {
            cnfe.printStackTrace();
            if(fbos != null) {
                try {
                    fbos.close();
                } catch (Exception x) {
                }
            }
            if(out != null) {
                try {
                    out.close();
                } catch (Exception x) {
                }
            }
            if(in != null) {
                try {
                    in.close();
                } catch (Exception x) {
                }
            }
            throw cnfe;
        }
        return obj;
    }

}