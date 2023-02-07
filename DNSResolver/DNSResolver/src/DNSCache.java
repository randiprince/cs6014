import java.util.HashMap;

public class DNSCache {
    HashMap<DNSQuestion, DNSRecord> cache_;

    public DNSCache() {
        cache_ = new HashMap<DNSQuestion, DNSRecord>();
    }

    public boolean containsRequest(DNSQuestion request) {
        if (cache_.containsKey(request)) {
            if (cache_.get(request).isExpired()) {
                return true;
            } else {
                cache_.remove(request);
                return false;
            }
        }
        return false;
    }

    public void addToCache(DNSQuestion request, DNSRecord record) {
        cache_.put(request, record);
    }

    public DNSRecord getRecordFromCache(DNSQuestion request){
        return cache_.get(request);
    }
}
