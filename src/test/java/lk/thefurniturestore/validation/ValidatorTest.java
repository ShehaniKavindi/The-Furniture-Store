package lk.thefurniturestore.validation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ValidatorTest {
    @Test
    void acceptsValidRegistrationData() {
        assertTrue("student@example.com".matches(Validator.EMAIL_VALIDATION));
        assertTrue("Strong@Pass1".matches(Validator.PASSWORD_VALIDATION));
    }

    @Test
    void rejectsWeakPasswordsAndInvalidEmails() {
        assertFalse("weak".matches(Validator.PASSWORD_VALIDATION));
        assertFalse("not-an-email".matches(Validator.EMAIL_VALIDATION));
    }
}
