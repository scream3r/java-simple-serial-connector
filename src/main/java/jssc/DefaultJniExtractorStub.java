/**
 * License: https://opensource.org/licenses/BSD-3-Clause
 */
package jssc;

import org.scijava.nativelib.DefaultJniExtractor;
import org.scijava.nativelib.NativeLibraryUtil;

import java.io.File;
import java.io.IOException;

/**
 * @author A. Tres Finocchiaro
 *
 * Stub <code>DefaultJniExtractor</code> class to allow native-lib-loader to conditionally
 * use a statically defined native search path <code>bootPath</code> when provided.
 */
public class DefaultJniExtractorStub extends DefaultJniExtractor {
    private File bootPath;

    /**
     * Default constructor
     */
    public DefaultJniExtractorStub(Class<?> libraryJarClass) throws IOException {
        super(libraryJarClass);
    }

    /**
     * Force native-lib-loader to first look in the location defined as <code>bootPath</code>
     * prior to extracting a native library, useful for sandboxed environments.
     *  <code>
     *  NativeLoader.setJniExtractor(new DefaultJniExtractorStub(null, "/opt/nativelibs")));
     *  NativeLoader.loadLibrary("mylibrary");
     *  </code>
     */
    public DefaultJniExtractorStub(Class<?> libraryJarClass, String bootPath) throws IOException {
        this(libraryJarClass);

        if(bootPath != null) {
            File bootTest = new File(bootPath);
            if(bootTest.exists()) {
                // assume a static, existing directory will contain the native libs
                this.bootPath = bootTest;
            } else {
                System.err.println("WARNING " + DefaultJniExtractorStub.class.getCanonicalName() + ": Boot path " + bootPath + " not found, falling back to default extraction behavior.");
            }
        }
    }

    /**
     * If a <code>bootPath</code> was provided to the constructor and exists,
     * calculate the <code>File</code> path without any extraction logic.
     *
     * If a <code>bootPath</code> was NOT provided or does NOT exist, fallback on
     * the default extraction behavior.
     */
    @Override
    public File extractJni(String libPath, String libName) throws IOException {
        // Lie and pretend it's already extracted at the bootPath location
        if(bootPath != null) {
            return new File(bootPath, NativeLibraryUtil.getPlatformLibraryName(libName));
        }
        // Fallback on default behavior
        return super.extractJni(libPath, libName);
    }

    @Override
    public void extractRegistered() throws IOException {
        if(bootPath != null) {
            return; // no-op
        }
        super.extractRegistered();
    }
}
