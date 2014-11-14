# An Explanation of the Make Process for jSSC Native Binaries

The Make system is only configured to work for Linux builds at this time.

If you just need to build a native binary for the environment on which you are
building, simply run "make". The resulting .so file will be in the "target" 
directory. You will need to rename the file to match the following format (arch
will be ommitted on a default build):

    libjSSC-[JSSC_VERSION_NUMBER]_[ARCHITECTURE].so

Below is a list of recognized strings for the [ARCHITECTURE] field:

    * "x86" or "x86_64" for i386, i686, and AMD architectures (32-bit and 
      64-bit, respectively).
    * "armsf" or "armhf" for ARM architectures (soft-float and hard-float, 
      respectively).
    * "mips" for MIPS architectures.

If you need to cross-compile the native binary, that can be done by specifying 
an architecture and a toolchain prefix on the command line when executing make.
These can be specified with the following syntax:

    make ARCH=[ARCHITECTURE] CROSS_COMPILE=[TOOLCHAIN_PREFIX]

This follows the example of Linux kernel modules. The architecture should be 
one of the ones specified above. The toolchain prefix is whatever prefix your 
toolchain puts before the gnu compiler commands. For example, if I want to 
compile for the MIPS processor using the uclibc toolchain (for which the usual 
command to launch g++ would be "mips-linux-g++"), I would specify my toolchain 
prefix as "mips-linux-". This can also be specified as an absolute path, such 
as "/opt/toolchains/uclibc-crosstools-gcc/usr/bin/mips-linux-".
