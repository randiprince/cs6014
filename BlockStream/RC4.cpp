//
// Created by Randi Prince on 3/1/23.
//

#include "RC4.hpp"

#include <utility>
#include "blockCypher.hpp"

RC4::RC4(std::string password) {
    createKey(password, key);
    for (int i = 0; i < 256; i++) { // identity permutation
        S[i] = i;
    }
    j = 0;
    for (int i = 0; i < 256; i++) { // identity permutation
        j = (j + S[i] + key[i % password.length()]) % 256;
        std::swap(S[i], S[j]);
    }
    i = 0;
    j = 0;
}

uint8_t RC4::getNextByte() {
    i = (i + 1) % 256;
    j = (j + S[i]) % 256;
    std::swap(S[i], S[j]);
    int output = S[(S[i] + S[j]) % 256];
    return S[output];
}
