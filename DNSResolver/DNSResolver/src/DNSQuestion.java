import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;

public class DNSQuestion {
    String[] name_;
    int type_;
    int class_;
    static DNSQuestion decodeQuestion(ByteArrayInputStream input, DNSMessage message) throws IOException {
        DNSQuestion question = new DNSQuestion();
        question.name_ = message.readDomainName(input);
        byte[] type = input.readNBytes(2);
        question.type_ = ((type[0] & 0xff) << 8) | (type[1] & 0xff);
        byte[] qClass = input.readNBytes(2);
        question.class_ = ((qClass[0] & 0xff) << 8) | (qClass[1] & 0xff);
        return question;
    }

    void writeBytes(ByteArrayOutputStream output, HashMap<String,Integer> domainNameLocations) throws IOException {
        DNSMessage.writeDomainName(output, domainNameLocations, name_); //write name

        //write type
        int byte1 = (type_ >> 8) & 0xff;
        output.write(byte1);
        int byte2 = type_ & 0xff;
        output.write(byte2);

        //write class
        int byte3 = (class_ >> 8) & 0xff;
        output.write(byte3);
        int byte4 = class_ & 0xff;
        output.write(byte4);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DNSQuestion question = (DNSQuestion) o;
        return type_ == question.type_ && class_ == question.class_ && Arrays.equals(name_, question.name_);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(type_, class_);
        result = 31 * result + Arrays.hashCode(name_);
        return result;
    }
}
