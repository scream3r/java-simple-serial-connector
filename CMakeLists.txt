cmake_minimum_required(VERSION 3.9)
project(jssc VERSION 2.8.0 DESCRIPTION "Java Simple Serial Connector")

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
	else()
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

add_library(jssc SHARED
    src/cpp/${JSSC_PLATFORM}/jssc.cpp
)

target_include_directories(jssc PRIVATE ${JNI_INCLUDE_DIRS} ${JSSC_ADDITIONAL_INCLUDES})

set_target_properties(jssc PROPERTIES VERSION ${PROJECT_VERSION})
set_target_properties(jssc PROPERTIES PUBLIC_HEADER src/jssc_SerialNativeInterface.h)
set_target_properties(jssc PROPERTIES POSITION_INDEPENDENT_CODE ON)

# Call strip on non-debug builds
if(CMAKE_STRIP AND NOT CMAKE_BUILD_TYPE MATCHES "Deb")
	if(CMAKE_CXX_COMPILER_ID MATCHES "Clang")
		set(STRIP_ARGS -x)
	endif()
	add_custom_command(TARGET jssc POST_BUILD COMMAND "${CMAKE_STRIP}" ${STRIP_ARGS} $<TARGET_FILE:jssc>)
endif()
