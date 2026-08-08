package lk.thefurniturestore.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class AppUtilTest {
    @Test
    void generatesSixDigitVerificationCodes() {
        assertTrue(AppUtil.generateCode().matches("\\d{6}"));
    }
}
