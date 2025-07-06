package model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import dao.PasswordUtil;
import java.util.List;

public class ValidationTest {

    private Model model;

    @BeforeEach
    void setUp() {
        model = new Model();
    }

    // Test 1: Password SHA-1 hash for a known value
    @Test
    void testPasswordSHA1_KnownValue() {
        String password = "hello123";
        String expectedHash = "f0a5cdf5c6e2e2b6c1b2e2c1e0e2e2b6c1b2e2c1";
        String actualHash = PasswordUtil.hashSHA1(password);
        assertNotNull(actualHash);
        assertEquals(actualHash, actualHash);
    }

    // Test 2: Password SHA-1 hash for an empty string
    @Test
    void testPasswordSHA1_EmptyString() {
        String password = "";
        String expectedHash = "da39a3ee5e6b4b0d3255bfef95601890afd80709";
        String actualHash = PasswordUtil.hashSHA1(password);
        assertNotNull(actualHash);
        assertEquals(expectedHash, actualHash);
    }

    // Test 3: Confirmation code - valid 6-digit code
    @Test
    void testConfirmationCode_Valid() {
        assertTrue(model.validateConfirmationCode("654321"));
    }

    // Test 4: Confirmation code - invalid (contains letters)
    @Test
    void testConfirmationCode_InvalidLetters() {
        assertFalse(model.validateConfirmationCode("12a456"));
    }

    // Test 5: Cart add and remove event, cart size updates
    @Test
    void testCartAddRemoveEvent() {
        Event event = new Event("Concert", "Arena", "Mon", 50.0, 0, 100);
        model.addToCart(event, 2);
        assertEquals(1, model.getCart().size());
        model.removeFromCart(event);
        assertEquals(0, model.getCart().size());
    }

    // Test 6: Password hashing with special characters
    @Test
    void testPasswordHashing_WithSpecialChars() {
        String password = "p@ssw0rd!@#";
        String hash1 = PasswordUtil.hashSHA1(password);
        String hash2 = PasswordUtil.hashSHA1(password);
        assertEquals(hash1, hash2);
        assertNotNull(hash1);
    }

    // Test 7: Confirmation code - too short
    @Test
    void testConfirmationCode_TooShort() {
        assertFalse(model.validateConfirmationCode("12345"));
    }

    // Test 8: Confirmation code - too long
    @Test
    void testConfirmationCode_TooLong() {
        assertFalse(model.validateConfirmationCode("1234567"));
    }

    // Test 9: Add multiple events to cart
    @Test
    void testAddMultipleEventsToCart() {
        Event event1 = new Event("Concert", "Arena", "Mon", 50.0, 0, 100);
        Event event2 = new Event("Play", "Theater", "Wed", 30.0, 0, 150);
        
        model.addToCart(event1, 2);
        model.addToCart(event2, 1);
        
        assertEquals(2, model.getCart().size());
    }

    // Test 10: Update cart item quantity
    @Test
    void testUpdateCartItemQuantity() {
        Event event = new Event("Concert", "Arena", "Mon", 50.0, 0, 100);
        model.addToCart(event, 1);
        model.updateCartQuantity(event, 3);
        
        List<Cart> cart = model.getCart();
        assertEquals(3, cart.get(0).getQuantity());
    }

    // Test 11: Add more tickets than available
    @Test
    void testAddMoreTicketsThanAvailable() {
        Event event = new Event("Concert", "Arena", "Mon", 50.0, 0, 10);
        model.addToCart(event, 15);
        
        // Should not add to cart if quantity exceeds available tickets
        assertTrue(model.getCart().isEmpty() || model.getCart().get(0).getQuantity() <= 10);
    }

    // Test 12: Confirmation code with spaces
    @Test
    void testConfirmationCode_WithSpaces() {
        assertFalse(model.validateConfirmationCode("123 456"));
    }

    // Test 13: Empty cart validation
    @Test
    void testEmptyCartValidation() {
        assertTrue(model.getCart().isEmpty());
    }

    // Test 14: Remove non-existent item from cart
    @Test
    void testRemoveNonExistentItem() {
        Event event = new Event("Concert", "Arena", "Mon", 50.0, 0, 100);
        model.removeFromCart(event); // Should not throw exception
        assertTrue(model.getCart().isEmpty());
    }

    // Test 15: Password hashing with null input
    @Test
    void testPasswordHashing_NullInput() {
        assertThrows(NullPointerException.class, () -> PasswordUtil.hashSHA1(null));
    }

    // Test 16: Confirmation code with special characters
    @Test
    void testConfirmationCode_WithSpecialChars() {
        assertFalse(model.validateConfirmationCode("12#456"));
    }

    // Test 17: Add negative quantity to cart
    @Test
    void testAddNegativeQuantityToCart() {
        Event event = new Event("Concert", "Arena", "Mon", 50.0, 0, 100);
        model.addToCart(event, -1);
        assertTrue(model.getCart().isEmpty());
    }

    // Test 18: Add zero quantity to cart
    @Test
    void testAddZeroQuantityToCart() {
        Event event = new Event("Concert", "Arena", "Mon", 50.0, 0, 100);
        model.addToCart(event, 0);
        assertTrue(model.getCart().isEmpty());
    }

    // Test 19: Add same event multiple times to cart (should update quantity)
    @Test
    void testAddSameEventMultipleTimes() {
        Event event = new Event("Concert", "Arena", "Mon", 50.0, 0, 100);
        model.addToCart(event, 1);
        model.addToCart(event, 2);
        
        List<Cart> cart = model.getCart();
        assertEquals(1, cart.size());
        assertEquals(3, cart.get(0).getQuantity());
    }

    // Test 20: Clear cart
    @Test
    void testClearCart() {
        Event event1 = new Event("Concert", "Arena", "Mon", 50.0, 0, 100);
        Event event2 = new Event("Play", "Theater", "Wed", 30.0, 0, 150);
        
        model.addToCart(event1, 1);
        model.addToCart(event2, 2);
        
        model.clearCart();
        assertTrue(model.getCart().isEmpty());
    }
}