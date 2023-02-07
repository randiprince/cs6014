import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.HashMap;

public class DNSRecord {
    String[] name_;
    int type_;
    int class_;
    int ttl_;
    int rdlength_;
    String rdata_;
    Calendar date_;

    static DNSRecord decodeRecord(ByteArrayInputStream input, DNSMessage message) throws IOException {
        DNSRecord record = new DNSRecord();
        record.name_ = message.readDomainName(input);
        byte[] type = input.readNBytes(2);
        record.type_ = ((type[0] & 0xff) << 8) | (type[1] & 0xff);
        byte[] rClass = input.readNBytes(2);
        record.class_ = ((rClass[0] & 0xff) << 8) | (rClass[1] & 0xff);
        byte[] ttl = input.readNBytes(4);
        record.ttl_ = ((ttl[0] & 0xff) << 24) | ((ttl[1] & 0xff) << 16) | ((ttl[2] & 0xff) << 8) | (ttl[3] & 0xff);
        byte[] rdlength = input.readNBytes(2);
        record.rdlength_ = ((rdlength[0] & 0xff) << 8) | (rdlength[1] & 0xff);
        // The format of this information varies
        //  according to the TYPE and CLASS of the resource record.
        //  For example, the if the TYPE is A ( aka 1 )and the CLASS is IN (aka 1),
        // the RDATA field is a 4 octet ARPA Internet address
        if (record.type_ == 1 && record.class_ == 1) {
            byte[] address = input.readNBytes(4);
            record.rdata_ = (address[0] & 0xff) + "." + (address[1] & 0xff) + "." + (address[2] & 0xff) + "." + (address[3] & 0xff);
        } else {
            // else we get byte array from full length and stringify it
            byte[] rdata = input.readNBytes(record.rdlength_);
            record.rdata_ = new String(rdata, StandardCharsets.US_ASCII); // needs to be case insensitive
        }

        record.date_ = Calendar.getInstance();
        return record;
    }

    public boolean isExpired() {
        Calendar now = Calendar.getInstance();
        date_.add(Calendar.SECOND, ttl_); // add ttl to record creation date
        return now.before(date_); // return if right now is before or after ttl
    }

    public void writeBytes(ByteArrayOutputStream outputStream, HashMap<String, Integer> domainLocations) throws IOException {
        DNSMessage.writeDomainName(outputStream, domainLocations, name_); //write name
        //write type
        int byte1 = (type_ >> 8) & 0xff;
        outputStream.write(byte1);
        int byte2 = type_ & 0xff;
        outputStream.write(byte2);

        //write class
        int byte3 = (class_ >> 8) & 0xff;
        outputStream.write(byte3);
        int byte4 = class_ & 0xff;
        outputStream.write(byte4);
        //write ttl
        int byte5 = (ttl_ >> 24) & 0xff;
        outputStream.write(byte5);
        int byte6 = (ttl_ >> 16) & 0xff;
        outputStream.write(byte6);
        int byte7 = (ttl_ >> 8) & 0xff;
        outputStream.write(byte7);
        int byte8 = ttl_ & 0xff;
        outputStream.write(byte8);

        //write rdlength
        int byte9 = (rdlength_ >> 8) & 0xff;
        outputStream.write(byte9);
        int byte10 = rdlength_ & 0xff;
        outputStream.write(byte10);
        // rdata
        if (type_ == 1 && class_ == 1) {
            String[] address = rdata_.split("\\.");
            for (int i = 0; i < rdlength_; i++) {
                outputStream.write(Integer.parseInt(address[i]));
            }
        } else {
            outputStream.write(rdata_.getBytes(), 0, rdlength_);
        }

    }
}
