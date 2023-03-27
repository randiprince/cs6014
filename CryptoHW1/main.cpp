//
// Created by Randi Prince on 3/1/23.
//

#include <iostream>
#include "blockCypher.hpp"
#include "RC4.hpp"

void blockCypher() {
    std::array<uint8_t, 8> key;
    std::array<std::array<uint8_t, 256>, 8>table;
    std::string password = "password";

    createKey(password, key);
    createTables(table);

    std::string message = "hello";
    std::string encrypted = encrypt(message, key, table);
    std::cout << "encrypted: " << encrypted << "\n";
    std::string decrypted = decrypt(encrypted, key, table);
    std::cout << decrypted << "\n";
}


std::string RC4encrypt(std::string message, std::string password) {
    uint8_t encryptedMsg[message.size()];
    for (int i = 0; i < message.size(); i++) {
        encryptedMsg[i] = message[i];
    }
    RC4 rc4 = RC4(password);
    std::string encrypt;
    for (int i = 0; i < message.size(); i++) {
        encrypt += rc4.getNextByte() xor encryptedMsg[i]; // encrypt but xor each piece of message with next byte
    }
    return encrypt;
}

std::string RC4decrypt(std::string encryptedMsg, std::string password) {
    uint8_t decryptedMsg[encryptedMsg.size()];
    for (int i = 0; i < encryptedMsg.size(); i++) {
        decryptedMsg[i] = encryptedMsg[i];
    }
    RC4 rc4 = RC4(password);
    std::string decrypt;
    for (int i = 0; i < encryptedMsg.size(); i++) {
        decrypt += rc4.getNextByte() xor decryptedMsg[i]; // encrypt but xor each piece of message with next byte
    }
    return decrypt;
}

int main() {

//    blockCypher(); // tests out block cypher
    std::string password = "password";
    std::string passwordTwo = "passwd";
    std::string message = "hello world!";
    std::string messageTwo = "hello world?";
    std::cout << "test encryption and decryption of 'hello world!' using SAME password: " << std::endl;
    std::string  encryptOne = RC4encrypt(message, password);
    std::cout << "encrypted: " << encryptOne<< "\n";
    std::string decryptOne = RC4decrypt(encryptOne, password);
    std::cout << "decrypted with same password: " << decryptOne<< "\n";
    std::cout << "----------------------------------" << std::endl;
    std::cout << "test encryption and decryption of 'hello world!' using INCORRECT password: " << std::endl;
    //Verify that decrypting a message with a different key than the encryption key does not reveal the plaintext.
    std::string encrypt = RC4encrypt(message, password);
    std::cout << "encrypted: " << encrypt<< "\n"; // output is =�Q����+CVe
    std::string decrypt = RC4decrypt(encrypt, passwordTwo);
    std::cout << "decrypted with wrong password: " << decrypt<< "\n"; // output with diff password is �-�0�Ȑ	_�D aka plaintext not revealed
    std::cout << "----------------------------------" << std::endl;

    // Verify that encrypting 2 messages using the same keystream is insecure
    // (what do you expect to see if you xor the two encrypted messages?)
    std::cout << "test encryption insecurity of two messages 'hello world!' and 'hello world?' and with same keystream: " << std::endl;
    std::string RCencrypt2 = RC4encrypt(messageTwo, password);
    std::cout << "encrypted msg 1: " << encryptOne << "\n";
    std::cout << "encrypted msg 2: " << RCencrypt2<< "\n";
    std::string combinedOutput;
    for(int i = 0; i < encrypt.size(); i++){
        combinedOutput += encrypt[i] ^ RCencrypt2[i];
//        std::cout <<  combinedOutput << "\n";
    }
    std::cout << "xor of two msg encrypted with same key: " <<  combinedOutput + "\n"; // NULLNULLNULLNULL
    std::cout << "----------------------------------" << std::endl;

    std::cout << "test to show possibility of bit flipping attack on 'Your salary is $1000': " << std::endl;
    //Modify part of a message using a bit-flipping attack. For example, try sending
    // the message "Your salary is $1000" encrypted with RC4. Modify the cyphertext so that
    // when decrypted is says that your salary is 9999 instead. Hint: this should just require using xor.
    std::string bitFlipEncrypt = RC4encrypt("Your salary is $1000", password);
    std::cout << "bit flip encrypted: " << bitFlipEncrypt<< "\n";
    std::string bitflipMsgToDecrypt;
    for (int i = 0; i < bitFlipEncrypt.size(); i++) {
        if (i >= 17) { // 17, 18, 19 are zeros. want to change them to 9s
            bitflipMsgToDecrypt += bitFlipEncrypt[i] xor 0x09; // xor 00000000 with 00001001 gives you 00001001
        } else if (i == 16) { // need to change 1 to 9
            bitflipMsgToDecrypt += bitFlipEncrypt[i] xor 0x08; // xor 00000001 with 00001000 gives you 00001001
        } else { // else just add to the string
            bitflipMsgToDecrypt += bitFlipEncrypt[i];
        }
    }
    std::string bitFlipDecrypt = RC4encrypt(bitflipMsgToDecrypt, password); // pass in bitflip msg to decrypt
    std::cout << "bit flip decrypted: " << bitFlipDecrypt << "\n"; // gives you: Your salary is $9999
    std::cout << "----------------------------------" << std::endl;
    return 0;
}