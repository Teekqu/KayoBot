package utils;

import java.util.Arrays;

public class Check {

    public static boolean isInteger(String value) {
        for(String s : value.split("")) if(!Character.isDigit(s.charAt(0))) return false;
        return true;
    }

}
