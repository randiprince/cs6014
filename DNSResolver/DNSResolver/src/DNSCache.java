import java.util.HashMap;

public class DNSCache {
    HashMap<DNSQuestion, DNSRecord> cache_;

    public DNSCache() {
        cache_ = new HashMap<DNSQuestion, DNSRecord>();
    }

    public boolean containsRequest(DNSMessage request) {
        if (cache_.containsKey(request.questions_.get(0))) {
            if (cache_.get(request.questions_.get(0)).isExpired()) {
                return true;
            } else {
                cache_.remove(request.questions_.get(0));
                return false;
            }
        }
        return false;
    }

    public void addToCache(DNSQuestion request, DNSRecord record) {
        cache_.put(request, record);
    }

    public DNSRecord getRecordFromCache(DNSMessage request){
        return cache_.get(request.questions_.get(0));
    }
}
