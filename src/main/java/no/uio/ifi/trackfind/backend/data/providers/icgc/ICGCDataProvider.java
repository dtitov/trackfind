package no.uio.ifi.trackfind.backend.data.providers.icgc;

import alexh.weak.Dynamic;
import com.google.common.collect.Multimap;
import lombok.extern.slf4j.Slf4j;
import no.uio.ifi.trackfind.backend.annotations.VersionedComponent;
import no.uio.ifi.trackfind.backend.data.providers.PaginationAwareDataProvider;
import org.apache.lucene.index.IndexWriter;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Fetches data from <a href="http://docs.icgc.org/">ICGC</a>.
 *
 * @author Dmytro Titov
 */
@Slf4j
@VersionedComponent
public class ICGCDataProvider extends PaginationAwareDataProvider { // TODO: Fetch more data.

    private static final String DONORS = "https://dcc.icgc.org/api/v1/donors?";
    private static final String SUBMIT = "https://dcc.icgc.org/api/v1/download/submit?filters={%22donor%22:{%22id%22:{%22is%22:[%22__DONOR_ID__%22]}}}&info=[{%22key%22:%22__DATA_TYPE__%22,%22value%22:%22TSV%22}]";
    private static final String DOWNLOAD = "https://dcc.icgc.org/api/v1/download/";
    private static final String AVAILABLE_DATA_TYPES = "availableDataTypes";

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    protected void fetchData(IndexWriter indexWriter) throws Exception {
        log.info("Fetching donors...");
        fetchPages(indexWriter, DONORS, DONORS, ICGCPage.class);
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    protected void postProcessPage(Collection<Map> page) {
        for (Map dataset : page) {
            Map<String, Collection<String>> browser = new HashMap<>();
            String donorId = (String) dataset.get("id");
            Collection<String> availableDataTypes = (Collection<String>) dataset.get(AVAILABLE_DATA_TYPES);
            for (String dataType : availableDataTypes) {
                browser.computeIfAbsent(dataType, k -> new HashSet<>()).add(SUBMIT.replace("__DONOR_ID__", donorId).replace("__DATA_TYPE__", dataType));
            }
            dataset.put(properties.getBrowserAttribute(), browser);
            dataset.remove(AVAILABLE_DATA_TYPES);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override // hack for ICGC's "two-steps download"
    public Multimap<String, Map> search(String query, int limit) {
        String separator = properties.getLevelsSeparator();
        String advancedURL = properties.getAdvancedSectionName() + separator + properties.getDataURLAttribute();
        Multimap<String, Map> result = super.search(query, limit);
        for (Map map : result.values()) {
            Dynamic dynamic = Dynamic.from(map);
            String submitURL = dynamic.get(advancedURL, separator).asString();
            try (InputStream inputStream = new URL(submitURL).openStream();
                 InputStreamReader reader = new InputStreamReader(inputStream)) {
                Download download = gson.fromJson(reader, Download.class);
                dynamic.get(properties.getAdvancedSectionName(), separator).asMap().put(properties.getDataURLAttribute(), DOWNLOAD + download.getDownloadId());
            } catch (IOException e) {
                log.error(e.getMessage(), e);
                return null;
            }
        }
        return result;
    }

}
