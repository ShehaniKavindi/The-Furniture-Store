package lk.thefurniturestore.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordUtilTest {
    @Test
    void hashesAndVerifiesPasswords() {
        String password = "Secure@Password1";
        String hash = PasswordUtil.hash(password);

        assertNotEquals(password, hash);
        assertTrue(PasswordUtil.matches(password, hash));
        assertFalse(PasswordUtil.matches("Wrong@Password1", hash));
    }

    @Test
    void supportsLegacyPasswordsForOneTimeMigration() {
        assertTrue(PasswordUtil.matches("OldPassword", "OldPassword"));
        assertTrue(PasswordUtil.needsUpgrade("OldPassword"));
    }
}
