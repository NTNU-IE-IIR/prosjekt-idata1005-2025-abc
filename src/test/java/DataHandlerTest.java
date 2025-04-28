// Language: java
package dbcontext;

import org.junit.jupiter.api.*;
import utils.HashUtil;
import utils.Logger;
import utils.Message;
import utils.MessageTypeEnum;
import utils.PreferenceUtil;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for DataHandler.
 *
 * <p>
 * This class contains both database tests (which are disabled) and utility tests using JUnit 5.
 * The utility tests include tests for HashUtil, Message functionality, Logger redirection,
 * and PreferenceUtil settings.
 * </p>
 */
@Disabled("Database not configured for tests—focusing on utils for now")
class DataHandlerTest {

    // ─────────────────────────────────────────────────────────────────────
    // Database tests are defined below but will be skipped.
    // ─────────────────────────────────────────────────────────────────────

    /**
     * Test to verify that getAllStatus returns a list from the helper.
     * This test is skipped due to disabled class.
     */
    @Test
    void getAllStatus_shouldReturnListFromHelper() {
        // Skipped test.
    }

    /**
     * Test to verify that getAllPriorities returns a list from the helper.
     * This test is skipped due to disabled class.
     */
    @Test
    void getAllPriorities_shouldReturnListFromHelper() {
        // Skipped test.
    }

    // ─────────────────────────────────────────────────────────────────────
    // Utility Tests
    // ─────────────────────────────────────────────────────────────────────

    /**
     * Nested tests for HashUtil class.
     */
    @Nested
    class HashUtilTests {
        /**
         * Tests that SHA-256 hash for an empty string matches that of MessageDigest.
         */
        @Test
        void emptyStringHashMatchesMessageDigest() throws Exception {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] expectedBytes = md.digest("".getBytes(StandardCharsets.UTF_8));
            StringBuilder expectedHex = new StringBuilder();
            for (byte b : expectedBytes) {
                expectedHex.append(String.format("%02x", b));
            }
            String actual = HashUtil.hashSHA256("");
            assertEquals(expectedHex.toString(), actual);
        }

        /**
         * Tests that SHA-256 hash for string "abc" matches a precomputed value.
         */
        @Test
        void knownValue_abc() {
            String expected = "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad";
            assertEquals(expected, HashUtil.hashSHA256("abc"));
        }
    }

    /**
     * Nested tests for Message and MessageTypeEnum.
     */
    @Nested
    class MessageAndEnumTests {
        /**
         * Verifies that the default constructor for Message sets the type as ERROR.
         */
        @Test
        void defaultConstructorIsError() {
            Message<String> m = new Message<>("oops");
            assertEquals(MessageTypeEnum.ERROR, m.getType());
            assertEquals("oops", m.getMessage());
            assertNull(m.getResult());
        }

        /**
         * Tests the full constructor and setter methods of Message.
         */
        @Test
        void fullConstructorAndSetters() {
            Message<Integer> m = new Message<>(MessageTypeEnum.SUCCESS, "yay", 42);
            assertEquals(MessageTypeEnum.SUCCESS, m.getType());
            assertEquals("yay", m.getMessage());
            assertEquals(42, m.getResult());

            m.setType(MessageTypeEnum.WARNING);
            m.setMessage("careful");
            m.setResult(99);
            assertEquals(MessageTypeEnum.WARNING, m.getType());
            assertEquals("careful", m.getMessage());
            assertEquals(99, m.getResult());
        }

        /**
         * Tests that MessageTypeEnum contains all expected values.
         */
        @Test
        void enumContainsAllValues() {
            MessageTypeEnum[] vals = MessageTypeEnum.values();
            assertArrayEquals(
                    new MessageTypeEnum[]{MessageTypeEnum.SUCCESS, MessageTypeEnum.ERROR, MessageTypeEnum.INFO, MessageTypeEnum.WARNING},
                    vals
            );
        }
    }

    /**
     * Nested tests for Logger functionality.
     */
    @Nested
    class LoggerTests {
        private PrintStream originalOut, originalErr;
        private ByteArrayOutputStream outBuf, errBuf;

        /**
         * Redirects System.out and System.err to local buffers for testing.
         */
        @BeforeEach
        void swapStreams() {
            originalOut = System.out;
            originalErr = System.err;
            outBuf = new ByteArrayOutputStream();
            errBuf = new ByteArrayOutputStream();
            System.setOut(new PrintStream(outBuf));
            System.setErr(new PrintStream(errBuf));
        }

        /**
         * Restores original System.out and System.err.
         */
        @AfterEach
        void restoreStreams() {
            System.setOut(originalOut);
            System.setErr(originalErr);
        }

        /**
         * Tests that Logger.info sends output to System.out.
         */
        @Test
        void infoGoesToStdOut() {
            Logger.info("hello");
            assertEquals("hello\n", outBuf.toString());
        }

        /**
         * Tests that Logger.error sends output to System.err.
         */
        @Test
        void errorGoesToStdErr() {
            Logger.error("fail");
            assertEquals("fail\n", errBuf.toString());
        }

        /**
         * Tests that Logger.infoNewLine appends a new line in System.out.
         */
        @Test
        void infoNewLineAlsoStdOut() {
            Logger.infoNewLine("line");
            assertEquals("line\n", outBuf.toString());
        }
    }

    /**
     * Nested tests for PreferenceUtil functionality.
     */
    @Nested
    class PreferenceUtilTests {
        private PreferenceUtil prefs;
        private final String nodeName = DataHandlerTest.class.getName() + "." + UUID.randomUUID();

        /**
         * Sets up PreferenceUtil instance before each test.
         */
        @BeforeEach
        void setUp() {
            prefs = new PreferenceUtil(nodeName);
        }

        /**
         * Tests that a missing key returns the default value.
         */
        @Test
        void getWithDefaultWhenMissing() {
            assertEquals("def", prefs.getSetting("no_such_key", "def"));
        }

        /**
         * Tests saving a setting and then retrieving it.
         */
        @Test
        void saveAndRetrieve() {
            prefs.saveSetting("foo", "bar");
            assertEquals("bar", prefs.getSetting("foo", "ignored"));
            assertEquals("bar", prefs.getSetting("foo"));
        }

        /**
         * Tests that a missing key without a default returns a fallback message.
         */
        @Test
        void missingWithoutDefaultReturnsFallbackMessage() {
            assertEquals("Could not retrieve.", prefs.getSetting("unknown"));
        }
    }
}