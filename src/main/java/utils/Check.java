package utils;

public class Check {

    public static boolean isInteger(String value) {
        boolean check = true;
        for(String s : value.split("")) {
            if(!s.equals("1") && !s.equals("2") && !s.equals("3") && !s.equals("4") && !s.equals("5") && !s.equals("6") && !s.equals("7") && !s.equals("8") && !s.equals("9") && !s.equals("0")) {
                check = false;
            }
        }
        return check;
    }

}
