cmake_minimum_required(VERSION 3.24)
project(CryptoHW1)

set(CMAKE_CXX_STANDARD 14)

include_directories(.)

add_executable(CryptoHW1
        blockCypher.cpp
        blockCypher.hpp
        main.cpp
        RC4.cpp
        RC4.hpp)
