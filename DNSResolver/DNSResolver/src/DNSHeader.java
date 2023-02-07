import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;

public class DNSHeader {
    int ID_;
    boolean QR_;
    int OPCODE_;
    boolean AA_, TC_, RD_, RA_, Z_, AD_, CD_;
    int RCODE_;
    int QDCOUNT_;
    int ANCOUNT_;
    int NSCOUNT_;
    int ARCOUNT_;

    static DNSHeader decodeHeader(ByteArrayInputStream inputStream) throws IOException {
        DNSHeader header = new DNSHeader();
        // get id from first two bytes
        byte[] idBytes = inputStream.readNBytes(2);
        header.ID_ = ((idBytes[0] & 0xff) << 8) | (idBytes[1] & 0xff);

        // get flags and opcode from byte 3
        int byte3 = inputStream.read();
        header.QR_ = ((byte3 & 0xff) >> 7) !=0;
        header.OPCODE_ = (byte3 >> 3) & 0xff;
        header.AA_ = (((byte3 << 5) & 0xff) >> 7) !=0;
        header.TC_ = (((byte3 << 6) & 0xff) >> 7) !=0;
        header.RD_ = (((byte3 << 7) & 0xff) >> 7) !=0;

        //get info from byte 4
        int byte4 = inputStream.read();
        header.RA_ = ((byte4 & 0xff) >> 7) !=0;
        header.Z_ = (((byte4 << 1) & 0xff) >> 7) !=0;
        header.AD_ = (((byte4 << 2) & 0xff) >> 7) !=0;
        header.CD_ = (((byte4 << 3) & 0xff) >> 7) !=0;
        header.RCODE_ = byte4 & 0xf;

        // get qdcount which is next two bytes
        byte[] qdBytes = inputStream.readNBytes(2);
        header.QDCOUNT_ = ((qdBytes[0] & 0xff) << 8) | (qdBytes[1] & 0xff);

        // get ancount which is next two bytes
        byte[] anBytes = inputStream.readNBytes(2);
        header.ANCOUNT_ = ((anBytes[0] & 0xff) << 8) | (anBytes[1] & 0xff);

        // get nscount which is next two bytes
        byte[] nsBytes = inputStream.readNBytes(2);
        header.NSCOUNT_ = ((nsBytes[0] & 0xff) << 8) | (nsBytes[1] & 0xff);

        // get arcount which is next two bytes
        byte[] arBytes = inputStream.readNBytes(2);
        header.ARCOUNT_ = ((arBytes[0] & 0xff) << 8) | (arBytes[1] & 0xff);

        return header;
    }


    static DNSHeader buildHeaderForResponse(DNSMessage request, DNSMessage response) {
        DNSHeader header = new DNSHeader();
        header.ID_ = request.header_.ID_; // identifier to be copied from the request to match up replies
        header.QR_ = true; // 1 b/c a response
        header.OPCODE_ = 0; // standard query
        header.AA_ = false; // not authoritative answer
        header.TC_ = false; // not dealing with truncation
        header.RD_ = true; // support recursion b/c more optimal
        header.RA_ = true; // recursion available
        header.Z_ = false; // must be 0 in query and response
        header.AD_ = true;
        header.CD_ = false;
        header.RCODE_ = 0; // no error
        header.QDCOUNT_ = response.questions_.size(); // number of entries in question section
        header.ANCOUNT_ = response.answers_.size(); // number of responses
        header.NSCOUNT_ = response.authorityRecords_.size(); // number of authority records
        header.ARCOUNT_ = response.additionalRecords_.size(); // number of additional records
        return header;
    }

    // function to send bytes back...opposite of decoding done in function above
    void writeBytes(OutputStream outputStream) throws IOException {
        int byte1 = (ID_ >> 8) & 0xff;
        outputStream.write(byte1);
        int byte2 = (ID_ & 0xff);
        outputStream.write(byte2);
        // flags and opcode for byte 3
        int qr = QR_ ? 1 : 0;
        int aa = AA_ ? 1 : 0;
        int tc = TC_ ? 1 : 0;
        int rd = RD_ ? 1 : 0;
        int byte3 = ((qr << 7) & 0xff | (OPCODE_ & 0xff) << 3 | ((aa << 7) & 0xff) >> 5 | ((tc << 7) & 0xff) >> 6 | rd);
        outputStream.write(byte3);
        int ra = (RA_) ? 1 : 0;
        int z = (Z_) ? 1 : 0;
        int ad = (AD_) ? 1 : 0;
        int cd = (CD_) ? 1 : 0;
        int byte4 = ((ra << 7) & 0xff | ((z << 7) & 0xff) >> 1 | ((ad << 7) & 0xff) >> 2 | ((cd << 7) & 0xff) >> 3 | RCODE_);
        outputStream.write(byte4);
        //write qdcount
        int byte5 = (QDCOUNT_ >> 8) & 0xff;
        outputStream.write(byte5);
        int byte6 = (QDCOUNT_ & 0xff);
        outputStream.write(byte6);
        // write ancount
        int byte7 = (ANCOUNT_ >> 8) & 0xff;
        outputStream.write(byte7);
        int byte8 = (ANCOUNT_ & 0xff);
        outputStream.write(byte8);
        // write nscount
        int byte9 = (NSCOUNT_ >> 8) & 0xff;
        outputStream.write(byte9);
        int byte10 = (NSCOUNT_ & 0xff);
        outputStream.write(byte10);
        // write arcount
        int byte11 = (ARCOUNT_ >> 8) & 0xff;
        outputStream.write(byte11);
        int byte12 = (ARCOUNT_ & 0xff);
        outputStream.write(byte12);
    }
}
