cmake_minimum_required(VERSION 3.22.1 FATAL_ERROR)

set(REVC_PROJECT_NAME "reVC")
set(REVC_VERSION 0.0.1)
set(REVC_DESCRIPTION "reverted ${REVC_PROJECT_NAME}")
set(REVC_LIB_NAME revc)

project(${REVC_PROJECT_NAME}
        VERSION ${REVC_VERSION}
        LANGUAGES C CXX
        DESCRIPTION ${REVC_DESCRIPTION}
)

get_filename_component(REVC_ROOT_DIR "${CMAKE_CURRENT_SOURCE_DIR}" ABSOLUTE)
set(REVC_GAME_DIR "${REVC_ROOT_DIR}/../../../")
set(VENDOR_DIR "${REVC_GAME_DIR}/vendor")

message(STATUS "reVC: game dir = ${REVC_GAME_DIR}")
message(STATUS "reVC: vendor dir = ${VENDOR_DIR}")
message(STATUS "reVC: ANDROID_ABI = ${ANDROID_ABI}")

# Define ANDROID_x32 for 32-bit ARM (signal handler uses this to pick sigcontext fields)
if(ANDROID_ABI STREQUAL "armeabi-v7a")
    add_compile_definitions(ANDROID_x32=1)
else()
    add_compile_definitions(ANDROID_x32=0)
endif()

# ─────────── Wire vendored Android prebuilt libraries ───────────

# OpenAL (built-in CMake FindOpenAL uses these cache vars)
set(OPENAL_LIBRARY
    "${VENDOR_DIR}/openal-soft/libs/Android/${ANDROID_ABI}/libopenal.so"
    CACHE FILEPATH "" FORCE)
set(OPENAL_INCLUDE_DIR
    "${VENDOR_DIR}/openal-soft/include"
    CACHE PATH "" FORCE)

if(NOT TARGET OpenAL::OpenAL)
    add_library(OpenAL::OpenAL SHARED IMPORTED)
    set_target_properties(OpenAL::OpenAL PROPERTIES
        IMPORTED_LOCATION "${OPENAL_LIBRARY}"
        INTERFACE_INCLUDE_DIRECTORIES "${OPENAL_INCLUDE_DIR}"
    )
endif()

# mpg123 (Findmpg123.cmake uses these cache vars)
set(mpg123_LIBRARIES
    "${VENDOR_DIR}/mpg123/lib/Android/${ANDROID_ABI}/libmpg123.so"
    CACHE FILEPATH "" FORCE)
set(mpg123_INCLUDE_DIR
    "${VENDOR_DIR}/mpg123/include"
    CACHE PATH "" FORCE)

# SDL2 (librw's FindSDL2.cmake uses these cache vars)
set(SDL2_LIBRARY
    "${VENDOR_DIR}/sdl2/libs/Android/${ANDROID_ABI}/libSDL2.so"
    CACHE FILEPATH "" FORCE)
set(SDL2_INCLUDE_DIR
    "${VENDOR_DIR}/sdl2/include"
    CACHE PATH "" FORCE)
set(SDL2_FOUND TRUE CACHE BOOL "" FORCE)

if(NOT TARGET SDL2::SDL2)
    add_library(SDL2::SDL2 SHARED IMPORTED)
    set_target_properties(SDL2::SDL2 PROPERTIES
        IMPORTED_LOCATION "${SDL2_LIBRARY}"
        INTERFACE_INCLUDE_DIRECTORIES "${SDL2_INCLUDE_DIR}"
    )
endif()

# ─────────── Bring in the actual game source ───────────
if(IS_DIRECTORY "${REVC_GAME_DIR}" AND EXISTS "${REVC_GAME_DIR}/CMakeLists.txt")
    message(STATUS "reVC: Including Game module from: ${REVC_GAME_DIR}")
    add_subdirectory(${REVC_GAME_DIR} ${CMAKE_BINARY_DIR}/game_build)
else()
    message(FATAL_ERROR "reVC: Game source directory or CMakeLists.txt not found at: ${REVC_GAME_DIR}")
endif()
