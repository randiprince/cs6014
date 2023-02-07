import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class DNSMessage {
    byte[] originalData_;
    DNSHeader header_ = new DNSHeader();
    ArrayList<DNSQuestion> questions_;
    ArrayList<DNSRecord> answers_;
    ArrayList<DNSRecord> authorityRecords_;
    ArrayList<DNSRecord> additionalRecords_;

    static DNSMessage decodeMessage(byte[] bytes) throws IOException {
        DNSMessage message = new DNSMessage();
        message.originalData_ = bytes;
        ByteArrayInputStream input = new ByteArrayInputStream(bytes);
        message.header_ = DNSHeader.decodeHeader(input);

        // get questions / queries
        message.questions_ = new ArrayList<>();
        for (int i = 0; i < message.header_.QDCOUNT_; i++) {
            message.questions_.add(DNSQuestion.decodeQuestion(input, message));
        }

        // get answers / response
        message.answers_ = new ArrayList<>();
        for (int i = 0; i < message.header_.ANCOUNT_; i++) {
            message.answers_.add(DNSRecord.decodeRecord(input, message));
        }

        // get authority records
        message.authorityRecords_ = new ArrayList<>();
        for (int i = 0; i < message.header_.NSCOUNT_; i++) {
            message.authorityRecords_.add(DNSRecord.decodeRecord(input, message));
        }

        // get additional records
        message.additionalRecords_ = new ArrayList<>();
        for (int i = 0; i < message.header_.ARCOUNT_; i++) {
            message.additionalRecords_.add(DNSRecord.decodeRecord(input, message));
        }
        return message;
    }

    String[] readDomainName(ByteArrayInputStream inputStream) throws IOException {
        ArrayList<String> domainName = new ArrayList<>();
        while (true) {
            int length = inputStream.read();
            if (length == 0) {
                break;
            // check to see if first two bits of the first byte are 1 1...if so need to deal with compression
            // mask first byte with 11000000 and then shift 6 to isolate first two bits
            // 0x3 = 0011
            } else if ((length & 0xc0) >> 6 == 0x3) {
                int byte2 = inputStream.read();
                int offset = ((((length & 0xff) << 8) | (byte2 & 0xff)) & 0x3f); // get offset in remaining bits
                return readDomainName(offset);
            } else {
                byte[] nameBytes = inputStream.readNBytes(length);
                domainName.add(new String(nameBytes, StandardCharsets.UTF_8)); // use UTF-8 to allow for casing in future use
            }
        }
        String[] domainNamesArr = new String[domainName.size()];
        for (int i = 0; i < domainName.size(); i++) {
            domainNamesArr[i] = domainName.get(i); // put into array b/c that is return type and parameter type
        }

        return domainNamesArr;
    }

    String[] readDomainName(int firstByte) throws IOException {
        ByteArrayInputStream input = new ByteArrayInputStream(originalData_, firstByte, 63); // check about this length. docs said no more than 63 octets
        return readDomainName(input);
    }

    static DNSMessage buildResponse(DNSMessage request, ArrayList<DNSRecord> answers) {
        DNSMessage response = new DNSMessage();
        response.questions_ = request.questions_;
        response.answers_ = answers; // from parameter
        response.authorityRecords_ = request.authorityRecords_;
        response.additionalRecords_ = request.additionalRecords_;
        response.header_ = DNSHeader.buildHeaderForResponse(request, response); // call static function
        return response;
    }

    byte[] toBytes() throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        HashMap<String, Integer> domainLocation = new HashMap<>();
        //header
        header_.writeBytes(outputStream);
        // requests
        for (DNSQuestion question: questions_) {
            question.writeBytes(outputStream, domainLocation);
        }
        // responses
        for (DNSRecord answer: answers_) {
            answer.writeBytes(outputStream, domainLocation);
        }
        // ns
        for (DNSRecord authorityRecord: authorityRecords_) {
            authorityRecord.writeBytes(outputStream, domainLocation);
        }
        // ar
        for (DNSRecord additionalRecord: additionalRecords_) {
            additionalRecord.writeBytes(outputStream, domainLocation);
        }
        return outputStream.toByteArray();
    }

    static void writeDomainName(ByteArrayOutputStream output, HashMap<String,Integer> domainLocations, String[] domainPieces) throws IOException {
        String domainName = joinDomainName(domainPieces);
        if (domainPieces.length == 0) { // end of domain name or at root. base case
            output.write(0);
        } else if (domainLocations.containsKey(domainName)) {
            // need to deal with  compression here so have to get 1 1 in first two bits of 16 b/c if we have we should compress
            // mask with 0x3fff which is 0011111111111111 to get 0 0 first two bits
            // or with 0xc0000 which is 1100000000000000 to put 1 1 in first two bits and get remaining offset
            // stored pointer to domain name in hashmap
            int value = (0xc000 | (domainLocations.get(domainName)) & 0x3fff);
            int byte1 = (value & 0xff00) >> 8;
            int byte2 = value & 0xff;
            output.write(byte1);
            output.write(byte2);
        } else {
            domainLocations.put(domainName, output.size()); // add domain name and size to hashmap
            // now write length of each piece of domain bc length label then that # of characters
            output.write(domainPieces[0].length());
            output.write(domainPieces[0].getBytes(StandardCharsets.UTF_8));
            String [] subArray = Arrays.copyOfRange(domainPieces, 1, domainPieces.length); // get rid of first piece
            writeDomainName(output, domainLocations, subArray); // call recursively
        }
    }

    public static String joinDomainName(String[] pieces) {
        String name = "";
        for (int i = 0; i < pieces.length; i++) {
            if (i < pieces.length - 1) {
                name += pieces[i] + ".";
            } else {
                name += pieces[i];
            }

        }
        return name;
    }
}
