package top.symple.symplegraphdisplay.managers.data;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DataListener {
    String label() default "";
    Color color() default @Color(red = 0, green = 0, blue = 0, alpha = -1);
    Color fillColor() default @Color(red = 0, green = 0, blue = 0, alpha = -1);
}
