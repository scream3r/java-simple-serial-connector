# Precompiled libraries

The `native` folder contains precompiled libraries for distribution.
If a library is missing, please consider a pull request or open an issue.

Whenever the interface of `SerialNativeInterface.java` changes, developers
are to recompile binaries for each platform.

## Valid paths

The old path structure is ambigious, but supported for compatiblity.
This structure must be used before `native-lib-loader` version 2.4.0.

  * `linux_32`
  * `linux_64`
  * `linux_arm`
  * `linux_arm64`
  * `mac_32`
  * `mac_64`
  * `windows_32`
  * `windows_64`


## Next version

Note: Valid starting from `native-lib-loader` version 2.4.0 or later.
The path names were aligned to the [os-maven-plugin](https://github.com/trustin/os-maven-plugin/).
It follows the same conventions as the [osdetector-gradle-plugin](https://github.com/google/osdetector-gradle-plugin).

  * `linux-arm_32-32`
  * `linux-x86_32-32`  (also alias of `linux-i386-64`)
  * `windows-x86_64-32`
  * `aix-ppc_64-64`
  * `linux-x86_64-64`
  * `linux-aarch_64-64` (instead of `linux-arm64-64`)
  * `linux-ppcle_64-64`
  * `mac-x86_64-64`
  * `mac-ppc_64-64`
  * `windows-x86_32-64`
  * `windows-x86_64-64`

If an architecture, bitness or cpu feature is missing, please consider a pull request against `native-lib-loader`

## Activation

No activation needed, a jar with these libraries is always built.
Optionally, to skip native compilation and only build this artifact:

```bash
mvn clean install -Ppackage
```
