package no.uio.ifi.trackfind.backend.data.providers;

import com.google.common.collect.Multimap;
import com.google.gson.Gson;
import no.uio.ifi.trackfind.backend.configuration.TrackFindProperties;
import no.uio.ifi.trackfind.backend.dao.Dataset;
import no.uio.ifi.trackfind.backend.dao.Mapping;
import no.uio.ifi.trackfind.backend.data.providers.ihec.IHECDataProvider;
import no.uio.ifi.trackfind.backend.repositories.DatasetRepository;
import no.uio.ifi.trackfind.backend.repositories.MappingRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

// TODO: think about test for DataProvider.crawlRemoteRepository
@RunWith(SpringRunner.class)
public class DataProviderTests {

    private static String SAMPLE_DATASET = "{\"analysis_attributes\":{\"alignment_software\":\"Pash\",\"alignment_software_version\":\"3.0\"},\"browser\":{\"signal\":[{\"big_data_url\":\"http://epigenomesportal.ca/tracks/Roadmap/hg19/30218.Roadmap.SRS167289_95_453.H3K27me3.signal.bigWig\",\"md5sum\":\"9048ff7fbbdcccf74e94d3cf67c82c59\",\"primary\":true}]},\"experiment_attributes\":{\"experiment_id\":\"\\u003ca href\\u003d\\u0027http://www.ncbi.nlm.nih.gov/sra?term\\u003dSRX041064\\u0027 target\\u003d_blank\\u003eSRX041064\\u003c/a\\u003e\",\"experiment_type\":\"Histone_H3K27me3\"},\"hub_description\":{\"assembly\":\"hg19\",\"date\":\"2018-09-14\",\"description\":\"Data hub generated by the IHEC Data Portal, with the following parameters: {\\u0027data_release_id\\u0027: \\u00275\\u0027}\",\"email\":\"info@epigenomesportal.ca\",\"publishing_group\":\"Roadmap\",\"releasing_group\":\"Roadmap\",\"taxon_id\":9606.0},\"ihec_data_portal\":{\"assay\":\"H3K27me3\",\"assay_category\":\"Histone Modifications\",\"cell_type\":\"Adipose_Nuclei\",\"cell_type_category\":\"Fat\",\"publishing_group\":\"NIH Roadmap\",\"releasing_group\":\"Roadmap\"},\"other_attributes\":{\"experiment_geo_id\":\"\\u003ca href\\u003d\\u0027http://www.ncbi.nlm.nih.gov/geo/query/acc.cgi?acc\\u003dGSM670017\\u0027 target\\u003d_blank\\u003eGSM670017\\u003c/a\\u003e\",\"lab\":\"BI\"},\"sample_data\":{\"BIOMATERIAL_TYPE\":\"Primary_Tissue\",\"CELL_TYPE\":\"Adipose_Nuclei\",\"DISEASE\":\"none\",\"DONOR_AGE\":\"81\",\"DONOR_ETHNICITY\":\"NA\",\"DONOR_HEALTH_STATUS\":\"NA\",\"DONOR_ID\":\"95\",\"DONOR_SEX\":\"Female\",\"MOLECULE\":\"genomic_DNA\",\"SAMPLE_ID\":\"\\u003ca href\\u003d\\u0027http://www.ncbi.nlm.nih.gov/biosample/?term\\u003dSRS167289\\u0027 target\\u003d_blank\\u003eSRS167289\\u003c/a\\u003e\",\"TISSUE_DEPOT\":\"abdomen\",\"TISSUE_TYPE\":\"Adipose_Nuclei\"},\"sample_id\":\"SRS167289_95_453\"}";

    @InjectMocks
    private IHECDataProvider dataProvider;

    private Gson gson = new Gson();

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private DatasetRepository datasetRepository;

    @Mock
    private MappingRepository mappingRepository;

    private Dataset originalDataset;

    @Test
    public void getName() {
        assertThat(dataProvider.getName()).isEqualToIgnoringCase("IHEC");
    }

    // TODO: test dynamic mappings
    @Test
    public void applyMappings() {
        dataProvider.applyMappings();
        Dataset dataset = datasetRepository.findByRepositoryAndVersion("0", 0L).iterator().next();
        assertThat(dataset).isNotNull();
        assertThat(dataset.getBasicDataset()).isNotEmpty();
        assertThat(dataset.getBasicDataset()).isEqualToIgnoringCase("{\"software\":\"Pash\"}");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void getMetamodelTree() {
        Map<String, Object> metamodelTree = dataProvider.getMetamodelTree();
        assertThat(metamodelTree).hasSize(1);
        assertThat(metamodelTree).containsOnlyKeys("level1");
        assertThat(metamodelTree.get("level1")).isInstanceOf(Map.class);
        Map<Object, Object> innerMap = (Map<Object, Object>) metamodelTree.get("level1");
        assertThat(innerMap).hasSize(1);
        assertThat(innerMap).containsOnlyKeys("level2");
        assertThat(innerMap.get("level2")).isInstanceOf(Collection.class);
        Collection values = (Collection) innerMap.get("level2");
        assertThat(values).hasSize(2);
        assertThat(values).containsExactlyInAnyOrder("value1", "value2");
    }

    @Test
    public void getMetamodelFlat() {
        Multimap<String, String> metamodelFlat = dataProvider.getMetamodelFlat();
        assertThat(metamodelFlat.asMap()).hasSize(1);
        assertThat(metamodelFlat.asMap()).containsOnlyKeys("level1>level2");
        assertThat(metamodelFlat.asMap().get("level1>level2")).hasSize(2);
        assertThat(metamodelFlat.asMap().get("level1>level2")).containsExactlyInAnyOrder("value1", "value2");
    }

    @Test
    public void search() {
        Collection<Dataset> result = dataProvider.search("", 0);
        assertThat(result).hasSize(1);
        assertThat(result.iterator().next()).isEqualTo(originalDataset);
    }

    @Test
    public void fetch() {
        Dataset dataset = dataProvider.fetch("0", "0");
        assertThat(dataset).isNotNull();
        assertThat(dataset).isEqualTo(originalDataset);
        assertThat(dataset.getRawDataset()).isNotEmpty();
        assertThat(dataset.getRawDataset()).isEqualTo(SAMPLE_DATASET);
    }

    @Before
    public void setUp() {
        dataProvider.setGson(gson);
        TrackFindProperties trackFindProperties = new TrackFindProperties();
        trackFindProperties.setLevelsSeparator(">");
        dataProvider.setProperties(trackFindProperties);

        Mapping mapping = new Mapping();
        mapping.setFrom("analysis_attributes>alignment_software");
        mapping.setTo("software");
        mapping.setStaticMapping(true);

        originalDataset = new Dataset();
        when(mappingRepository.findByRepository(anyString())).thenReturn(Collections.singleton(mapping));
        when(datasetRepository.findByRepositoryAndVersion(anyString(), anyLong())).thenReturn(Collections.singleton(originalDataset));
        when(datasetRepository.findByIdAndVersion(anyLong(), anyLong())).thenReturn(originalDataset);
        when(datasetRepository.findByIdIn(any())).thenReturn(Collections.singleton(originalDataset));
        when(jdbcTemplate.queryForObject(anyString(), eq(Long.TYPE))).thenReturn(0L);
        when(jdbcTemplate.queryForList(anyString(), eq(Long.TYPE), anyLong(), anyString(), anyString(), anyInt())).thenReturn(Collections.singletonList(0L));
        HashMap<String, Object> attributeValueMap1 = new HashMap<>();
        attributeValueMap1.put("attribute", "level1>level2");
        attributeValueMap1.put("value", "value1");
        HashMap<String, Object> attributeValueMap2 = new HashMap<>();
        attributeValueMap2.put("attribute", "level1>level2");
        attributeValueMap2.put("value", "value2");
        when(jdbcTemplate.queryForList(anyString(), anyString(), anyLong())).thenReturn(Arrays.asList(attributeValueMap1, attributeValueMap2));
        originalDataset.setId(0L);
        originalDataset.setVersion(0L);
        originalDataset.setRawDataset(SAMPLE_DATASET);
    }

}
