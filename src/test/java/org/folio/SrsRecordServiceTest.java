package org.folio;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.apache.commons.io.IOUtils;
import org.folio.mapping.profile.MappingProfile;
import org.folio.mapping.profile.MappingProfileRule;
import org.folio.mocks.FileDefinition;
import org.folio.mocks.OkapiConnectionParams;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.folio.mapping.profile.RecordType.HOLDINGS;
import static org.folio.mapping.profile.RecordType.INSTANCE;
import static org.folio.mapping.profile.RecordType.ITEM;
import static org.junit.Assert.assertEquals;

public class SrsRecordServiceTest {

    private SrsRecordService srsRecordService = new SrsRecordService();

    private MappingProfile mappingProfile;

    @Before
    public void setUp() {
        setUpHoldingsAndItems();
        setUpMappingProfile();
    }

    @Test
    public void shouldAppendHoldingsAndItems() throws IOException {
        JsonArray sourceRecords = new JsonObject(getResourceAsString("srs_records.json")).getJsonArray("sourceRecords");

        List<String> marcRecords = srsRecordService.exportSrsRecord(mappingProfile, sourceRecords, new FileDefinition(),
                "jobExecutionId", new OkapiConnectionParams());

        List<String> expectedMarcRecords = getExpectedMarcRecords("expected_marc_records_with_transformations.mrc");
        assertEquals(expectedMarcRecords.get(0), marcRecords.get(0));
        assertEquals(expectedMarcRecords.get(1), marcRecords.get(1));
    }

    @Test
    public void shouldNotAppendHoldingsAndItems_whenMappingProfileContainsInstanceTypeOnly() throws IOException {
        mappingProfile.setRecordTypes(Arrays.asList(INSTANCE));
        JsonArray sourceRecords = new JsonObject(getResourceAsString("srs_records.json")).getJsonArray("sourceRecords");

        List<String> marcRecords = srsRecordService.exportSrsRecord(mappingProfile, sourceRecords, new FileDefinition(),
                "jobExecutionId", new OkapiConnectionParams());

        List<String> expectedMarcRecords = getExpectedMarcRecords("expected_marc_records.mrc");
        assertEquals(expectedMarcRecords.get(0), marcRecords.get(0));
        assertEquals(expectedMarcRecords.get(1), marcRecords.get(1));
    }

    private static String getResourceAsString(String path) {
        try {
            ClassLoader classLoader = SrsRecordServiceTest.class.getClassLoader();
            URL url = classLoader.getResource(path);
            return IOUtils.toString(url, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }

    private List<String> getExpectedMarcRecords(String fileName) throws IOException {
        return Files.lines(Paths.get("src/test/resources/" + fileName))
                .collect(Collectors.toList());
    }

    private void setUpMappingProfile() {
        mappingProfile = new MappingProfile();
        mappingProfile.setRecordTypes(Arrays.asList(HOLDINGS, ITEM));

        MappingProfileRule mappingProfileRule1 = new MappingProfileRule();
        mappingProfileRule1.setId("callNumber");
        mappingProfileRule1.setPath("holdings[*].callNumber");
        mappingProfileRule1.setEnabled(true);
        mappingProfileRule1.setRecordType(HOLDINGS);
        mappingProfileRule1.setTransformation("900 $a");

        MappingProfileRule mappingProfileRule2 = new MappingProfileRule();
        mappingProfileRule2.setId("materialTypeId");
        mappingProfileRule2.setPath("items[*].materialTypeId");
        mappingProfileRule2.setEnabled(true);
        mappingProfileRule2.setRecordType(ITEM);
        mappingProfileRule2.setTransformation("800 $b");

        mappingProfile.setMappingProfileRules(Arrays.asList(mappingProfileRule1, mappingProfileRule2));
    }

    private void setUpHoldingsAndItems() {
        List<JsonObject> holdings = new ArrayList<>();
        holdings.add(new JsonObject()
                .put("id", "ba823e2a-5ab2-473d-8620-4e01360ff308")
                .put("instanceId", "d3a4f598-ef82-49e1-8a4c-e65a434a39a1")
                .put("callNumber", "callNumberValue"));
        holdings.add(new JsonObject()
                .put("id", "139652b7-e5ad-4e99-a69f-4481ba7ce4b6")
                .put("instanceId", "5eeb697a-1727-4232-b4d3-65c34d48cdf9")
                .put("callNumber", "callNumberValue"));

        List<JsonObject> items = new ArrayList<>();
        items.add(new JsonObject()
                .put("id", "e9db0580-0b61-4a23-a4f2-96fa212176f9")
                .put("holdingsRecordId", "ba823e2a-5ab2-473d-8620-4e01360ff308")
                .put("materialTypeId", "materialTypeIdValue"));
        items.add(new JsonObject()
                .put("id", "60a85355-6738-4c8d-aba4-75be8ffd762e")
                .put("holdingsRecordId", "ba823e2a-5ab2-473d-8620-4e01360ff308")
                .put("materialTypeId", "materialTypeIdValue"));
        items.add(new JsonObject()
                .put("id", "3246d55e-687b-4967-bdec-f4d50e5014fd")
                .put("holdingsRecordId", "139652b7-e5ad-4e99-a69f-4481ba7ce4b6")
                .put("materialTypeId", "materialTypeIdValue"));
        items.add(new JsonObject()
                .put("id", "ea89575e-f840-4f65-b3f5-9cbabe6eb4ee")
                .put("holdingsRecordId", "139652b7-e5ad-4e99-a69f-4481ba7ce4b6")
                .put("materialTypeId", "materialTypeIdValue"));

        srsRecordService.setHoldings(holdings);
        srsRecordService.setItems(items);
    }
}