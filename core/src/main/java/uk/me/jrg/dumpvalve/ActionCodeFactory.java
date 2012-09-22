package uk.me.jrg.dumpvalve;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ActionCodeFactory {

    public static Object getRequestSetBodyReplayActionCode() throws Exception {
        final Class<?> actionCodeCls = Class.forName("org.apache.coyote.ActionCode");
        if (actionCodeCls.isEnum()) {
            final Method valueOfMethod = actionCodeCls.getMethod("valueOf", String.class);
            return valueOfMethod.invoke(null, "REQ_SET_BODY_REPLAY");
        } else {
            final Field field = actionCodeCls.getDeclaredField("ACTION_REQ_SET_BODY_REPLAY");
            return field.get(null);
        }
    }
}
