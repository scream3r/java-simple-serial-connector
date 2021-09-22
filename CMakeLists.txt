cmake_minimum_required(VERSION 3.0)
cmake_policy(SET CMP0048 NEW)
cmake_policy(SET CMP0042 NEW)

# Get some architecture information
if(WIN32)
    string(TOLOWER $ENV{PROCESSOR_ARCHITECTURE} OS_ARCH)
    set(OS_TYPE "Windows")
else()
	execute_process(COMMAND uname -m OUTPUT_VARIABLE OS_ARCH OUTPUT_STRIP_TRAILING_WHITESPACE)
	execute_process(COMMAND uname OUTPUT_VARIABLE OS_TYPE OUTPUT_STRIP_TRAILING_WHITESPACE)
endif()

# Determine if compiling on Apple and host arch before starting project
if(APPLE AND NOT CMAKE_OSX_ARCHITECTURES)
	set(CMAKE_OSX_ARCHITECTURES "${OS_ARCH}" CACHE STRING "Build architectures for Mac OS X" FORCE)
endif()

project(jssc VERSION "" LANGUAGES CXX)

# Use JAVA_HOME from Maven
if(JAVA_HOME)
    # Sanitize path for cmake
    file(TO_CMAKE_PATH "${JAVA_HOME}" JAVA_HOME)
    message(STATUS "Picked up JAVA_HOME from Maven: ${JAVA_HOME}")
    # Sanitize accidental jre selection on Linux
    if(JAVA_HOME MATCHES "/jre$")
        STRING(REGEX REPLACE "/jre$" "/jdk" JAVA_HOME "${JAVA_HOME}")
        message(WARNING "Maven JAVA_HOME appears to be a JRE,  trying to fix: ${JAVA_HOME}")
    endif()
endif()

find_package(Java)
# Workaround for FindJNI, per https://stackoverflow.com/a/51764145/3196753
set(JAVA_AWT_LIBRARY HeaderOnly)
set(JAVA_JVM_LIBRARY HeaderOnly)
set(JAVA_AWT_INCLUDE_PATH HeaderOnly)
find_package(JNI REQUIRED)

# Kitware-recommended technique for defaulting to 'Release' mode
if(NOT CMAKE_BUILD_TYPE)
	message(STATUS "Setting build type to 'Release' as none was specified.")
	set(CMAKE_BUILD_TYPE Release CACHE STRING "Choose the type of build." FORCE)
	# Set the possible values of build type for cmake-gui
	set_property(CACHE CMAKE_BUILD_TYPE PROPERTY STRINGS "Debug" "Release" "MinSizeRel" "RelWithDebInfo")
endif()

if(APPLE)
	# Locate IOKit framework for serial/ioss.h
	find_library(IOKit IOKit)
	if(IOKit)
		find_path(IOKit_INCLUDE_DIR NAMES IOKitLib.h HINTS ${IOKit_INC_SEARCH_PATH} ${IOKit_PKGC_INCLUDE_DIRS} PATH_SUFFIXES IOKit)
		list(APPEND JSSC_ADDITIONAL_INCLUDES ${IOKit_INCLUDE_DIR})
	endif()
endif()

# Statically link gcc/c++
if(MSVC)
	# /MT = Multithread, static version of the run-time library
	set(CMAKE_CXX_FLAGS_RELEASE "${CMAKE_CXX_FLAGS_RELEASE} /MT")
elseif(WIN32)
	# Assume mingw, use "-static"
	set(CMAKE_CXX_STANDARD_LIBRARIES "-static ${CMAKE_CXX_STANDARD_LIBRARIES}")
elseif(CMAKE_CXX_COMPILER_ID STREQUAL "GNU" AND NOT CMAKE_SYSTEM_NAME STREQUAL "SunOS")
	set(CMAKE_CXX_STANDARD_LIBRARIES "-static-libgcc -static-libstdc++ ${CMAKE_CXX_STANDARD_LIBRARIES}")
endif()

if(WIN32)
	set(CPP_SOURCE "windows")
	# Remove "lib" prefix from windows binaries
	set(CMAKE_SHARED_LIBRARY_PREFIX "")
	# Disable optimization for Release builds (XP/Server 2003)
	if(NOT MSVC)
		set(CMAKE_CXX_FLAGS_RELEASE "${CMAKE_CXX_FLAGS_RELEASE} -O0")
	else()
		set(CMAKE_CXX_FLAGS_RELEASE "${CMAKE_CXX_FLAGS_RELEASE} /Od")
	endif()
else()
	set(CPP_SOURCE "_nix_based")
	# Sane level of optimization for Release builds
	set(CMAKE_CXX_FLAGS_RELEASE "${CMAKE_CXX_FLAGS_RELEASE} -O2")
endif()

# Detect platform if -DNATIVE_LIB_DIR is not provided
# TODO: Handle arm, hardfloat, etc
if(NOT NATIVE_LIB_DIR OR "${NATIVE_LIB_DIR}" STREQUAL "")
	# windows, linux, darwin, etc
	string(TOLOWER "${CMAKE_SYSTEM_NAME}" OS_NAME)
	if(OS_NAME MATCHES "darwin")
		set(OS_NAME "osx")
	endif()

	# bitness/suffix
	if(CMAKE_OSX_ARCHITECTURES)
        # can occur when calling cmake directly
        if(CMAKE_OSX_ARCHITECTURES STREQUAL "arm64")
            set(OS_SUFFIX "arm64")
        else()
            set(OS_SUFFIX 64)
        endif()
	elseif("${OS_ARCH}" STREQUAL "aarch64" OR "${OS_ARCH}" STREQUAL "arm64")
		set(OS_SUFFIX "arm64")
	elseif("${OS_ARCH}" MATCHES "arm")
		set(OS_SUFFIX "arm")
	elseif(FORCE_M32)
		set(OS_SUFFIX 32)
	elseif(FORCE_M64)
		set(OS_SUFFIX 64)
	else()
		if("${CMAKE_SIZEOF_VOID_P}" EQUAL "8")
			set(OS_SUFFIX 64)
		else()
			set(OS_SUFFIX 32)
		endif()
	endif()

	# handle warnings
	if(OS_SUFFIX STREQUAL "arm")
        if(${CMAKE_VERSION} VERSION_LESS "3.10.0")
            message(WARNING "Cmake < 3.10.0 can't detect HAS_FPU.  This only affects arm 32-bit builds.")
        else()
            cmake_host_system_information(RESULT OS_SUFFIX_SF QUERY HAS_FPU)
            # warn of native-lib-loader shortcomming
            IF(NOT OS_SUFFIX_SF EQUAL 1)
                message(WARNING "Soft float detected. We can't determine this at runtime; Compatibility will suffer.")
            ENDIF()
        endif()
    endif()

	set(NATIVE_LIB_DIR ${OS_NAME}_${OS_SUFFIX})
endif()
set(CMAKE_LIBRARY_OUTPUT_DIRECTORY ${CMAKE_CURRENT_BINARY_DIR}/natives/${NATIVE_LIB_DIR})

# version.h using #cmakedefine for version from pom.xml.
if(NOT JSSC_VERSION)
    # Fallback to parsing pom.xml if not provided
    set(JSSC_VERSION "0.0.0-UNKNOWN")
    file(STRINGS ${CMAKE_CURRENT_SOURCE_DIR}/pom.xml POM_FILE)
    foreach(POM_LINE ${POM_FILE})
        # Assume first "<version>" is the project version
        if(POM_LINE MATCHES "<version>")
            string(REGEX REPLACE "^[ \t]+|<[^>]*>" "" DETECTED_VERSION "${POM_LINE}")
            string(STRIP "${DETECTED_VERSION}" DETECTED_VERSION)
            if(DETECTED_VERSION STREQUAL "")
                message(WARNING "Could not parse JSSC version from pom.xml, defaulting to \"${JSSC_VERSION}\"")
            else()
                set(JSSC_VERSION "${DETECTED_VERSION}")
                message(STATUS "Found JSSC version \"${JSSC_VERSION}\" in pom.xml")
            endif()
            break()
        endif()
    endforeach()
else()
    message(STATUS "Release JSSC version based on pom.xml: \"${JSSC_VERSION}\"")
endif()
configure_file(src/main/cpp/version.h.in ${CMAKE_CURRENT_BINARY_DIR}/version.h @ONLY)

add_library(jssc SHARED src/main/cpp/${CPP_SOURCE}/jssc.cpp)

# Fall-back header for when maven is not available
list(APPEND JSSC_ADDITIONAL_INCLUDES src/main/cpp/)
target_include_directories(jssc PRIVATE ${JNI_INCLUDE_DIRS} ${CMAKE_CURRENT_BINARY_DIR} ${JSSC_ADDITIONAL_INCLUDES})

set_target_properties(jssc PROPERTIES PUBLIC_HEADER ${CMAKE_CURRENT_BINARY_DIR}/jssc_SerialNativeInterface.h)
set_target_properties(jssc PROPERTIES POSITION_INDEPENDENT_CODE ON)
if(WIN32)
	# Fix paths for MSVC (Debug/Release) and MINGW
	set_target_properties(jssc PROPERTIES RUNTIME_OUTPUT_DIRECTORY_DEBUG "${CMAKE_LIBRARY_OUTPUT_DIRECTORY}")
	set_target_properties(jssc PROPERTIES RUNTIME_OUTPUT_DIRECTORY_RELEASE "${CMAKE_LIBRARY_OUTPUT_DIRECTORY}")
endif()

if(CMAKE_CXX_COMPILER_ID STREQUAL "GNU" AND NOT WIN32 AND NOT FORCE_IGNORE)
    if(FORCE_M32)
        # Build 32-bit binary on Linux
        set_target_properties(jssc PROPERTIES COMPILE_FLAGS -m32 LINK_FLAGS -m32)
    elseif(FORCE_M64)
        # Build 64-bit binary on Linux
        set_target_properties(jssc PROPERTIES COMPILE_FLAGS -m64 LINK_FLAGS -m64)
    endif()
endif()

# Call strip on non-debug builds
if(CMAKE_STRIP AND NOT CMAKE_BUILD_TYPE MATCHES "Deb")
	if(CMAKE_CXX_COMPILER_ID MATCHES "Clang")
		set(STRIP_ARGS -x)
	endif()
	add_custom_command(TARGET jssc POST_BUILD COMMAND "${CMAKE_STRIP}" ${STRIP_ARGS} $<TARGET_FILE:jssc>)
endif()

# Copy native library back to source tree
add_custom_command(TARGET jssc POST_BUILD
    COMMAND ${CMAKE_COMMAND} -E copy_directory ${CMAKE_CURRENT_BINARY_DIR}/natives/ ${CMAKE_CURRENT_SOURCE_DIR}/src/main/resources-precompiled/natives/
)

# Handle compiler warnings
if(MSVC)
	#TODO Treat warnings as errors /WX
	target_compile_options(jssc PRIVATE /W4)
else()
	#TODO Treat warnings as errors -Werror
	target_compile_options(jssc PRIVATE -Wall -Wextra -pedantic -Wno-long-long)
endif()
