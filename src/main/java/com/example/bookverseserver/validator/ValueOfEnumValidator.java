package com.example.bookverseserver.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Validator cho annotation @ValueOfEnum
 * Kiểm tra xem giá trị chuỗi có tồn tại trong enum không
 */
public class ValueOfEnumValidator implements ConstraintValidator<ValueOfEnum, CharSequence> {
    private Set<String> acceptedValues;

    @Override
    public void initialize(ValueOfEnum annotation) {
        acceptedValues = Stream.of(annotation.enumClass().getEnumConstants())
                .map(Enum::name)
                .collect(Collectors.toSet());
    }

    @Override
    public boolean isValid(CharSequence value, ConstraintValidatorContext context) {
        if (value == null) {
            // @NotBlank sẽ xử lý trường hợp null hoặc rỗng
            return true;
        }
        return acceptedValues.contains(value.toString());
    }
}


