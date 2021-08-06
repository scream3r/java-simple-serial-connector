set(CMAKE_SYSTEM_NAME Windows)

# URL for llvm-mingw
set(URL "https://github.com/mstorsjo/llvm-mingw/releases/download/20201020/llvm-mingw-20201020-msvcrt-ubuntu-18.04.tar.xz")
set(SHA256 "96e94e469665ee5632fff32a19b589ae1698859189d85615f3062b1544510b75")
get_filename_component(ARCHIVE_NAME "${URL}" NAME)
set(ARCHIVE "${CMAKE_BINARY_DIR}/llvm-mingw/${ARCHIVE_NAME}")

# Try to predict the subdirectory name from the file name
string(REPLACE ".tar.xz" "" SUBDIR "${ARCHIVE_NAME}")

# Prevent extract process from running more than once
if(NOT TOOLCHAIN_LOCATION)
  if(NOT EXISTS "${CMAKE_BINARY_DIR}/llvm-mingw/${SUBDIR}")
    if(NOT EXISTS "${ARCHIVE}")
      file(MAKE_DIRECTORY "${CMAKE_BINARY_DIR}/llvm-mingw")
      message(STATUS "Downloading ${URL} to ${ARCHIVE}...")
      file(DOWNLOAD ${URL} ${ARCHIVE} TIMEOUT 60 EXPECTED_HASH SHA256=${SHA256})
    endif()
    message(STATUS "Extracting ${ARCHIVE} to ${CMAKE_BINARY_DIR}/llvm-mingw/${SUBDIR}/...")
    if(${CMAKE_VERSION} VERSION_GREATER_EQUAL "3.18.0")
      file(ARCHIVE_EXTRACT INPUT "${ARCHIVE}" DESTINATION "${CMAKE_BINARY_DIR}/llvm-mingw")
    else()
      execute_process(COMMAND tar xf "${ARCHIVE}" -C "${CMAKE_BINARY_DIR}/llvm-mingw")
    endif()
  endif()
endif()

set(TOOLCHAIN_LOCATION "${CMAKE_BINARY_DIR}/llvm-mingw/${SUBDIR}/bin")
set(TOOLCHAIN_PREFIX "${TOOLCHAIN_LOCATION}/aarch64-w64-mingw32")
set(CMAKE_C_COMPILER "${TOOLCHAIN_PREFIX}-gcc")
set(CMAKE_CXX_COMPILER "${TOOLCHAIN_PREFIX}-g++")
set(CMAKE_RC_COMPILER "${TOOLCHAIN_PREFIX}-windres")
set(CMAKE_STRIP "${TOOLCHAIN_PREFIX}-strip" CACHE FILEPATH "" FORCE)
set(CMAKE_FIND_ROOT_PATH_MODE_PROGRAM NEVER)
set(CMAKE_FIND_ROOT_PATH_MODE_LIBRARY BOTH)
set(CMAKE_FIND_ROOT_PATH_MODE_INCLUDE BOTH)
