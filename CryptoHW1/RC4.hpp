//
// Created by Randi Prince on 3/1/23.
//

#ifndef BLOCKSTREAM_RC4_HPP
#define BLOCKSTREAM_RC4_HPP


#include <array>

class RC4 {
public:
    int i, j;
    std::array<uint8_t, 8> key;
    std::array<uint8_t, 256> S;
public:
    RC4();
    RC4(std::string password);
    uint8_t getNextByte();
};


#endif //BLOCKSTREAM_RC4_HPP
