package top.symple.symplegraphdisplay.managers.data;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
public @interface Color {
    int red();
    int green();
    int blue();
    float alpha() default 1f;
}
