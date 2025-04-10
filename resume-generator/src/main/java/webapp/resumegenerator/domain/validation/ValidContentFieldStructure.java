package webapp.resumegenerator.domain.validation;


import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = ContentFieldStructureValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidContentFieldStructure {

    String message() default "Некорректная структура JSON.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
