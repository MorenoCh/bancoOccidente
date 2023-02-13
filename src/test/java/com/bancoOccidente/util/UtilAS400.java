package com.bancoOccidente.util;

import net.serenitybdd.core.Serenity;
public class UtilAS400 {

    public static void saveVariableOnSession(String key, Object value) {
        Serenity.setSessionVariable(key).to(value);
    }

    public static <T> T getVariableOnSession(String key) {
        return (T)Serenity.sessionVariableCalled(key);
    }

}
