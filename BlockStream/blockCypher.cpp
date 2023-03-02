//
// Created by Randi Prince on 2/27/23.
//

#include "blockCypher.hpp"

void createKey(std::string &password, std::array<uint8_t, 8>&key) {
    for (int i = 0; i < 8; i++) {
        key[i] = 0;
    }
    for (int i = 0; i < password.length(); i++) {
        key[i % 8] = key[i % 8] xor password[i];
    }
}

void copyArray(std::array<uint8_t, 256>&arrOne, std::array<uint8_t, 256>&arrTwo) {
    for (int i = 0; i < 256; i++) {
        arrTwo[i] = arrOne[i];
    }
}

void shuffle(std::array<uint8_t, 256>&arrOne) {
    for (int i = 255; i > 0; i--) {
        int j = rand() % i;
        std::swap(arrOne[i], arrOne[j]);
    }
}

void createTables(std::array<std::array<uint8_t, 256>, 8>&table) {
    for (int j = 0; j < 256; j++) {
        table[0][j] = j;
    }
    for (int i = 0; i < 8; i++) {
        copyArray(table[i], table[i + 1]);
        shuffle(table[i+1]);
    }
}

std::string encrypt(std::string &message, std::array<uint8_t, 8>&key, std::array<std::array<uint8_t, 256>, 8>&table) {
    uint8_t m[message.size()];
    for (int i = 0; i < message.size(); i++) {
        m[i] = message[i];
    }
    for (int i = 0; i < 16; i++) {
        for (int j = 0; j < message.size(); j++) {
            m[j] = m[j] xor key[j % 8];
            m[j] = table[j % 8][m[j]];
        }
        uint8_t temp[message.size()];
        for (int j = 0; j < message.size(); j++) {
            temp[j] = m[j] >> 7; // shift everything to the right 7 bits
        }
        for(int k = 0; k < message.size(); k++){
            if (k == message.size() - 1) { // for the byte use the first b/c no next byte
                m[k] = (m[k] << 1 | temp[0]);
            } else {
                m[k] = (m[k] << 1 | temp[k + 1]);
            }

        }

    }
    std::string encryptedString;
    for(int n = 0; n < message.size(); n++){
        encryptedString += m[n];
    }
    return encryptedString;
}

std::string decrypt(std::string &encryptedMessage, std::array<uint8_t, 8>&key, std::array<std::array<uint8_t, 256>, 8>&table) {
    uint8_t m[encryptedMessage.size()];
    for (int p = 0; p < encryptedMessage.size(); p++) {
        m[p] = encryptedMessage[p];
    }
    for (int i = 0; i < 16; i++) {
        uint8_t temp[encryptedMessage.size()];
        for (int j = 0; j < encryptedMessage.size(); j++) {
            temp[j] = m[j] << 7;
        }

        for(int k = 0; k < encryptedMessage.size(); k++) {
            if (k == 0) {
                m[k] = (m[k] >> 1 | temp[encryptedMessage.size() - 1]);
            } else {
                m[k] = (m[k] >> 1 | temp[k - 1]);
            }

        }
        for (int j = 0; j < encryptedMessage.size(); j++) {
            for (int n = 255; n >= 0; n--) {
                if (m[j] == table[j % 8][n]) {
                    m[j] = n;
                    break;
                }
            }
        }

        for (int j = 0; j < encryptedMessage.size(); j++) {
            m[j] = m[j] xor key[j % 8];
        }

    }
    std::string encryptedString;
    for(int n = 0; n < encryptedMessage.size(); n++){
        encryptedString += m[n];
    }
    return encryptedString;
}
