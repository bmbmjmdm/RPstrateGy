package Utilities;

/**
 * Created by Dale on 9/9/2015.
 */
public class StringModifier {

    public static String addSpaces(String s){
        String[] parts = s.split(" ");
        String newString = "";

        for(String part: parts){
            newString = newString+part+" &#160;";
        }

        return newString.substring(0, newString.length()-7);
    }
}
