cmake_minimum_required(VERSION 3.0)
cmake_policy(SET CMP0048 OLD)
cmake_policy(SET CMP0042 NEW)
# Determine if compiling on Apple and host arch before starting project
if(APPLE AND NOT CMAKE_OSX_ARCHITECTURES)
	execute_process(COMMAND uname  -m OUTPUT_VARIABLE ARCH_TYPE OUTPUT_STRIP_TRAILING_WHITESPACE)	
	SET(CMAKE_OSX_ARCHITECTURES "${ARCH_TYPE}" CACHE STRING "Build architectures for Mac OS X" FORCE)
endif()





project(jssc CXX)

find_package(Java)
find_package(JNI)

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
if(CMAKE_CXX_COMPILER_ID STREQUAL "GNU")
	if(WIN32)
		# Assume mingw, use "-static"
		set(CMAKE_CXX_STANDARD_LIBRARIES "-static ${CMAKE_CXX_STANDARD_LIBRARIES}")
	elseif(NOT CMAKE_SYSTEM_NAME STREQUAL SunOS)
		set(CMAKE_CXX_STANDARD_LIBRARIES "-static-libgcc -static-libstdc++ ${CMAKE_CXX_STANDARD_LIBRARIES}")
	endif()
	execute_process(COMMAND uname OUTPUT_VARIABLE OS_TYPE OUTPUT_STRIP_TRAILING_WHITESPACE)
	if(OS_TYPE STREQUAL "Darwin")
		# Try to locate JNI again for edge-case (e.g. cross-compiling)
		if(NOT JNI_FOUND)
			execute_process(COMMAND /usr/libexec/java_home OUTPUT_VARIABLE JAVA_HOME OUTPUT_STRIP_TRAILING_WHITESPACE)
			set(ENV{JAVA_HOME} ${JAVA_HOME})
			find_package(JNI)
		endif()
	endif()
elseif(MSVC)
	set(CMAKE_CXX_FLAGS_RELEASE "${CMAKE_CXX_FLAGS_RELEASE} /MT")
endif()

if(WIN32)
	set(JSSC_PLATFORM "windows")
	# Don't append "lib" to windows binaries
	set(CMAKE_SHARED_LIBRARY_PREFIX "")
	# Disable optimization for Release builds (XP/Server 2003)
	if(NOT MSVC)
		set(CMAKE_CXX_FLAGS_RELEASE "${CMAKE_CXX_FLAGS_RELEASE} -O0")
	else()
		set(CMAKE_CXX_FLAGS_RELEASE "${CMAKE_CXX_FLAGS_RELEASE} /Od")
	endif()
else()
	set(JSSC_PLATFORM "_nix_based")
	# Sane level of optimization for Release builds
	set(CMAKE_CXX_FLAGS_RELEASE "${CMAKE_CXX_FLAGS_RELEASE} -O2")
endif()

# Detect platform if -DNATIVE_LIB_DIR is not provided
# TODO: Handle arm, hardfloat, etc
if(NOT NATIVE_LIB_DIR)
	# windows, linux, darwin, etc
	string(TOLOWER "${CMAKE_SYSTEM_NAME}" OS_NAME)
	if(OS_NAME MATCHES "darwin")
		set(OS_NAME "osx")
	endif()

	# 32-bit or 64-bit
	#FIXME: Might fail on cross-compile
	if(FORCE_M32)
		set(OS_BITS 32)
	elseif(FORCE_M64)
		set(OS_BITS 64)
	else()
		if("${CMAKE_SIZEOF_VOID_P}" EQUAL "8")
			set(OS_BITS 64)
		else()
			set(OS_BITS 32)
		endif()
	endif()
	SET(NATIVE_LIB_DIR ${OS_NAME}_${OS_BITS})
endif()
set(CMAKE_LIBRARY_OUTPUT_DIRECTORY ${CMAKE_CURRENT_BINARY_DIR}/natives/${NATIVE_LIB_DIR})

# version.h using #cmakedefine for version from pom.xml.
set(JSSC_VERSION "0.0.0-UNKNOWN")
file(STRINGS ${CMAKE_CURRENT_SOURCE_DIR}/pom.xml POM_FILE)
foreach(POM_LINE ${POM_FILE})
	# Assume first "<version>" is the project version
	if(POM_LINE MATCHES "<version>")
		string(REGEX REPLACE "^[ \t]+|<[^>]*>" "" DETECTED_VERSION "${POM_LINE}")
		string(STRIP "${DETECTED_VERSION}" DETECTED_VERSION)
		if(DETECTED_VERSION STREQUAL "")
			MESSAGE(WARNING "Could not parse JSSC version from pom.xml, defaulting to \"${JSSC_VERSION}\"")
		else()
			SET(JSSC_VERSION "${DETECTED_VERSION}")
			MESSAGE(STATUS "Found JSSC version \"${JSSC_VERSION}\" in pom.xml")
		endif()
		break()
	endif()
endforeach()
configure_file(src/main/cpp/version.h.in ${CMAKE_CURRENT_BINARY_DIR}/version.h @ONLY)

add_library(jssc SHARED src/main/cpp/${JSSC_PLATFORM}/jssc.cpp)

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
if(FORCE_M32)
	# Build 32-bit binary on Linux
	set_target_properties(jssc PROPERTIES COMPILE_FLAGS -m32 LINK_FLAGS -m32)
elseif(FORCE_M64)
	# Build 64-bit binary on Linux
	set_target_properties(jssc PROPERTIES COMPILE_FLAGS -m64 LINK_FLAGS -m64)
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
