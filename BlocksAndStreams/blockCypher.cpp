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
    uint8_t encryptedMsg[message.size()];
    for (int i = 0; i < message.size(); i++) {
        encryptedMsg[i] = message[i];
    }
    for (int i = 0; i < 16; i++) {
        for (int j = 0; j < message.size(); j++) {
            encryptedMsg[j] = encryptedMsg[j] xor key[j % 8];
            encryptedMsg[j] = table[j % 8][encryptedMsg[j]];
        }
        uint8_t tempArr[message.size()];
        for (int j = 0; j < message.size(); j++) {
            tempArr[j] = encryptedMsg[j] >> 7; // shift everything to the right 7 bits
        }
        for(int k = 0; k < message.size(); k++){
            if (k == message.size() - 1) { // for the byte use the first b/c no next byte
                encryptedMsg[k] = (encryptedMsg[k] << 1 | tempArr[0]);
            } else {
                encryptedMsg[k] = (encryptedMsg[k] << 1 | tempArr[k + 1]);
            }

        }

    }
    std::string encryptString;
    for(int i = 0; i < message.size(); i++){
        encryptString += encryptedMsg[i];
    }
    return encryptString;
}

std::string decrypt(std::string &encryptedMessage, std::array<uint8_t, 8>&key, std::array<std::array<uint8_t, 256>, 8>&table) {
    uint8_t decryptedMsg[encryptedMessage.size()];
    for (int p = 0; p < encryptedMessage.size(); p++) {
        decryptedMsg[p] = encryptedMessage[p];
    }
    for (int i = 0; i < 16; i++) {
        uint8_t tempArr[encryptedMessage.size()];
        for (int j = 0; j < encryptedMessage.size(); j++) {
            tempArr[j] = decryptedMsg[j] << 7;
        }

        for(int k = 0; k < encryptedMessage.size(); k++) {
            if (k == 0) {
                decryptedMsg[k] = (decryptedMsg[k] >> 1 | tempArr[encryptedMessage.size() - 1]);
            } else {
                decryptedMsg[k] = (decryptedMsg[k] >> 1 | tempArr[k - 1]);
            }

        }
        for (int j = 0; j < encryptedMessage.size(); j++) {
            for (int n = 255; n >= 0; n--) {
                if (decryptedMsg[j] == table[j % 8][n]) {
                    decryptedMsg[j] = n;
                    break;
                }
            }
        }

        for (int j = 0; j < encryptedMessage.size(); j++) {
            decryptedMsg[j] = decryptedMsg[j] xor key[j % 8];
        }

    }
    std::string decryptString;
    for(int n = 0; n < encryptedMessage.size(); n++){
        decryptString += decryptedMsg[n];
    }
    return decryptString;
}
