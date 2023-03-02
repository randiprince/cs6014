cmake_minimum_required(VERSION 3.24)
project(BlockStream)

set(CMAKE_CXX_STANDARD 17)

add_executable(BlockStream main.cpp blockCypher.cpp blockCypher.hpp RC4.cpp RC4.hpp)
