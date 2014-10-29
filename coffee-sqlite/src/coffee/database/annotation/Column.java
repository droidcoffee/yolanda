package coffee.database.annotation;

import java.lang.annotation.Target;
import java.lang.annotation.Retention;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({METHOD, FIELD}) 
@Retention(RUNTIME)
public @interface Column {

    String name() default "";
    boolean unique() default false;
    boolean nullable() default true;

    boolean insertable() default true;

    boolean updatable() default true;
    String columnDefinition() default "";

    String table() default "";

    int length() default 255;

    int precision() default 0;

    int scale() default 0;
}
