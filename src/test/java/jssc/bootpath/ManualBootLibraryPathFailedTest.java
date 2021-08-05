package jssc.bootpath;

import jssc.SerialNativeInterface;
import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Tests if a valid <code>jssc.boot.library.path</code> which does NOT contain a native library
 * will predictably fail.  This test can be run regardless of whether or not a native binary was
 * created during the build process.
 *
 * TODO: This MUST be in its own class to run in a separate JVM (https://stackoverflow.com/questions/68657855)
 * - JUnit does NOT currently offer JVM unloading between methods.
 * - maven-surefire-plugin DOES offer JVM unloading between classes using <code>reuseForks=false</code>
 * - Unloading is needed due to NativeLoader.loadLibrary(...) calls System.loadLibrary(...) which is static
 */
public class ManualBootLibraryPathFailedTest {
    @Test
    public void testBootPathOverride() {
        String nativeLibDir = "/"; // This should be valid on all platforms
        System.setProperty("jssc.boot.library.path", nativeLibDir);
        try {
            SerialNativeInterface.getNativeLibraryVersion();
            fail("Library loading should fail if path provided exists but does not contain a native library");
        } catch (UnsatisfiedLinkError ignore) {
            assertTrue("Library loading failed as expected with an invalid jssc.boot.library.path", true);
        }
    }
}
