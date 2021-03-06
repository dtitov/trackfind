package no.uio.ifi.trackfind.backend.pojo;

import com.fasterxml.jackson.annotation.JsonAnyGetter;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("rawtypes")
public class SearchResult {

    private Map<String, Map> content = new HashMap<>();

    @JsonAnyGetter
    public Map<String, Map> getContent() {
        return content;
    }

    public void setContent(Map<String, Map> content) {
        this.content = content;
    }

}
