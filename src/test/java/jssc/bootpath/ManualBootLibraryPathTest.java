package jssc.bootpath;

import jssc.SerialNativeInterface;
import org.junit.Test;
import org.scijava.nativelib.NativeLibraryUtil;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * Tests if a valid <code>jssc.boot.library.path</code> which DOES contain a native library
 * will predictably pass.  This test can ONLY be run regardless if a native binary was created
 * during the build process.  See also <code>maven.exclude.tests</code>.
 *
 * TODO: This MUST be in its own class to run in a separate JVM (https://stackoverflow.com/questions/68657855)
 * - JUnit does NOT currently offer JVM unloading between methods.
 * - maven-surefire-plugin DOES offer JVM unloading between classes using <code>reuseForks=false</code>
 * - Unloading is needed due to NativeLoader.loadLibrary(...) calls System.loadLibrary(...) which is static
 */
public class ManualBootLibraryPathTest {
    @Test
    public void testBootPathOverride() {
        String nativeLibDir = NativeLibraryUtil.getPlatformLibraryPath(System.getProperty("user.dir") + "/target/cmake/natives/");
        System.setProperty("jssc.boot.library.path", nativeLibDir);
        try {
            final String nativeLibraryVersion = SerialNativeInterface.getNativeLibraryVersion();
            assertThat(nativeLibraryVersion, is(not(nullValue())));
            assertThat(nativeLibraryVersion, is(not("")));
        } catch (UnsatisfiedLinkError linkError) {
            linkError.printStackTrace();
            fail("Should be able to call method!");
        }
    }
}
