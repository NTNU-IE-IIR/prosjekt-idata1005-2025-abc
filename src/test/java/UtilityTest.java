// Language: java
package utils;

import org.junit.jupiter.api.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * UtilityTest contains nested test classes for HashUtil, Message, Logger, and PreferenceUtil.
 * It verifies the correct behavior of these utility components.
 */
class UtilityTest {

    /**
     * Tests for the HashUtil utility.
     */
    @Nested
    class HashUtilTest {
        /**
         * Verifies that hashing an empty string with SHA-256 produces the expected result.
         *
         * @throws Exception if the MessageDigest algorithm is not available
         */
        @Test
        void hashSHA256_emptyString() throws Exception {
            // Compute expected hash using Java MessageDigest directly.
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] emptyHash = md.digest("".getBytes(StandardCharsets.UTF_8));
            StringBuilder expected = new StringBuilder();
            for (byte b : emptyHash) {
                expected.append(String.format("%02x", b));
            }

            String actual = HashUtil.hashSHA256("");
            assertEquals(expected.toString(), actual, "SHA-256 of empty string should match");
        }

        /**
         * Verifies that hashing the string "abc" produces the known SHA-256 value.
         */
        @Test
        void hashSHA256_knownValue() {
            // Precomputed SHA-256 hash for "abc".
            String expected = "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad";
            assertEquals(expected, HashUtil.hashSHA256("abc"));
        }
    }

    /**
     * Tests for the Message class and MessageTypeEnum.
     */
    @Nested
    class MessageAndTypeEnumTest {
        /**
         * Verifies that the default constructor for Message sets the type to ERROR.
         */
        @Test
        void defaultErrorConstructor() {
            Message<String> m = new Message<>("oops");
            assertEquals(MessageTypeEnum.ERROR, m.getType());
            assertEquals("oops", m.getMessage());
            assertNull(m.getResult());
        }

        /**
         * Verifies that using the full constructor and setters of Message works as expected.
         */
        @Test
        void fullConstructorAndSetters() {
            MessageTypeEnum type = MessageTypeEnum.SUCCESS;
            Message<Integer> m = new Message<>(type, "all good", 123);
            assertEquals(type, m.getType());
            assertEquals("all good", m.getMessage());
            assertEquals(123, m.getResult());

            m.setType(MessageTypeEnum.WARNING);
            m.setMessage("be careful");
            m.setResult(999);
            assertEquals(MessageTypeEnum.WARNING, m.getType());
            assertEquals("be careful", m.getMessage());
            assertEquals(999, m.getResult());
        }

        /**
         * Verifies that MessageTypeEnum contains all expected values.
         */
        @Test
        void messageTypeEnum_values() {
            MessageTypeEnum[] values = MessageTypeEnum.values();
            assertArrayEquals(
                    new MessageTypeEnum[]{MessageTypeEnum.SUCCESS, MessageTypeEnum.ERROR, MessageTypeEnum.INFO, MessageTypeEnum.WARNING},
                    values
            );
        }
    }

    /**
     * Tests for the Logger utility methods.
     */
    @Nested
    class LoggerTest {
        private final PrintStream originalOut = System.out;
        private final PrintStream originalErr = System.err;
        private ByteArrayOutputStream outContent;
        private ByteArrayOutputStream errContent;

        /**
         * Redirects System.out and System.err to capture output.
         */
        @BeforeEach
        void setUpStreams() {
            outContent = new ByteArrayOutputStream();
            errContent = new ByteArrayOutputStream();
            System.setOut(new PrintStream(outContent));
            System.setErr(new PrintStream(errContent));
        }

        /**
         * Restores System.out and System.err after tests.
         */
        @AfterEach
        void restoreStreams() {
            System.setOut(originalOut);
            System.setErr(originalErr);
        }

        /**
         * Verifies that Logger.info writes output to System.out.
         */
        @Test
        void info_writesToStdOut() {
            Logger.info("hello");
            assertEquals("hello\n", outContent.toString());
        }

        /**
         * Verifies that Logger.error writes output to System.err.
         */
        @Test
        void error_writesToStdErr() {
            Logger.error("uh-oh");
            assertEquals("uh-oh\n", errContent.toString());
        }

        /**
         * Verifies that Logger.infoNewLine writes output to System.out with a newline.
         */
        @Test
        void infoNewLine_writesToStdOut() {
            Logger.infoNewLine("line");
            assertEquals("line\n", outContent.toString());
        }
    }

    /**
     * Tests for the PreferenceUtil utility.
     */
    @Nested
    class PreferenceUtilTest {
        private PreferenceUtil prefs;
        private final String nodeName = UtilityTest.class.getName() + "." + UUID.randomUUID();

        /**
         * Initializes the PreferenceUtil instance before each test.
         */
        @BeforeEach
        void init() {
            prefs = new PreferenceUtil(nodeName);
        }

        /**
         * Verifies that saving a setting and then retrieving it with a default value works as expected.
         */
        @Test
        void saveAndGetSetting_withDefault() {
            String key = "key1";
            assertEquals("def", prefs.getSetting(key, "def"), "Should return default when no value saved");
            prefs.saveSetting(key, "val1");
            assertEquals("val1", prefs.getSetting(key, "whatever"), "Should return saved value, ignoring supplied default");
        }

        /**
         * Verifies that retrieving a setting without a default returns the stored value or a fallback message if missing.
         */
        @Test
        void getSetting_withoutDefault() {
            String key = "key2";
            assertEquals("Could not retrieve.", prefs.getSetting(key), "Missing key yields fallback message");
            prefs.saveSetting(key, "xyz");
            assertEquals("xyz", prefs.getSetting(key), "Now returns stored value");
        }
    }
}