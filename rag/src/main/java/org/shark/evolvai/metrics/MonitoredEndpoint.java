
package org.shark.evolvai.metrics;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marca métodos para ser monitoreados por AOP.
 * Si no se especifica `name`, se infiere a partir del nombre del controlador y del método.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface MonitoredEndpoint {
    String name() default "";
}

