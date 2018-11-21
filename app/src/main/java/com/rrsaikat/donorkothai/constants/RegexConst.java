package com.rrsaikat.donorkothai.constants;


final public class RegexConst {
    private RegexConst() {
    }

    public final static String PASSWORD_PATTERN =
            "^(?=.*[0-9])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{4,}$";
}
