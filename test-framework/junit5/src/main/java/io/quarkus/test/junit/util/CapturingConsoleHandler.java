package io.quarkus.test.junit.util;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public final class CapturingConsoleHandler {

    private static final VarHandle MODIFIERS;

    static {
        try {
            //            Module java_base = Field.class.getModule();
            //            var unnamed = FieldHelper.class.getModule();
            //            java_base.addOpens("java.lang.reflect", unnamed);
            //            java_base.addOpens("java.util", unnamed);
            Module unnamed = Thread.currentThread().getContextClassLoader().getUnnamedModule();
            Module java_base = Field.class.getModule();
            java_base.addOpens("java.lang", unnamed);
            java_base.addOpens("java.lang.reflect", unnamed);
            //                this.getClass().getClassLoader().getUnnamedModule().addOpens("java.lang.reflect",
            //                        this.getClass().getClassLoader().getUnnamedModule());

            var lookup = MethodHandles.privateLookupIn(Field.class, MethodHandles.lookup());
            MODIFIERS = lookup.findVarHandle(Field.class, "modifiers", int.class);
        } catch (IllegalAccessException | NoSuchFieldException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static void makeNonFinal(Field field) throws Exception {

        //        Module java_base = Field.class.getModule();
        //        var unnamed = this.getClass().getModule();
        //        System.out.println("UNNAMED " + unnamed);
        //        this.getClass().getClassLoader().getUnnamedModule().addOpens("java.lang",
        //                this.getClass().getClassLoader().getUnnamedModule());
        //        this.getClass().getClassLoader().getUnnamedModule().addOpens("java.lang.reflect",
        //                this.getClass().getClassLoader().getUnnamedModule());
        //        java_base.addOpens("java.lang.reflect", this.getClass().getClassLoader().getUnnamedModule());
        //        java_base.addOpens("java.util", this.getClass().getClassLoader().getUnnamedModule());
        //
        //        java_base.addOpens("java.lang", unnamed);
        //        java_base.addOpens("java.lang.reflect", unnamed);
        //        java_base.addOpens("java.util", unnamed);

        //        var lookup = MethodHandles.privateLookupIn(Field.class, MethodHandles.lookup());
        //        MODIFIERS = lookup.findVarHandle(Field.class, "modifiers", int.class);

        int mods = field.getModifiers();
        if (Modifier.isFinal(mods)) {
            MODIFIERS.set(field, mods & ~Modifier.FINAL);
        }
    }

}
