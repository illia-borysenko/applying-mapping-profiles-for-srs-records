package org.folio;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.apache.commons.collections4.CollectionUtils;
import org.folio.convertor.MarcRecordConvertor;
import org.folio.convertor.RecordConvertor;
import org.folio.mapping.MappingService;
import org.folio.mapping.MappingServiceImpl;
import org.folio.mapping.profile.MappingProfile;
import org.folio.mapping.profile.RecordType;
import org.folio.mocks.FileDefinition;
import org.folio.mocks.FileStorage;
import org.folio.mocks.LocalFileSystemStorage;
import org.folio.mocks.OkapiConnectionParams;
import org.marc4j.marc.VariableField;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class SrsRecordService {

    private MappingService mappingService = new MappingServiceImpl();
    private RecordConvertor recordConvertor = new MarcRecordConvertor();
    private FileStorage fileStorage = new LocalFileSystemStorage();

    private List<JsonObject> holdings;
    private List<JsonObject> items;

    public List<String> exportSrsRecord(MappingProfile mappingProfile, JsonArray srsRecords, FileDefinition fileDefinition, String jobExecutionId, OkapiConnectionParams connectionParams) {
        List<RecordType> mappingProfileRecordTypes = mappingProfile.getRecordTypes();
        List<String> marcRecords = new ArrayList<>();
        for (Object o : srsRecords) {
            JsonObject srsRecord = (JsonObject) o;
            //generate record fields by mapping profile
            List<VariableField> mappedFields = getMappedFields(mappingProfile, jobExecutionId, connectionParams, mappingProfileRecordTypes, srsRecord);
            String encodedRecordContent = getEncodedRecordContent(srsRecord);
            //convert srs record to marc and append generated fields
            byte[] marcRecordContent = recordConvertor.convert(encodedRecordContent, mappedFields);
            //save marc record content to file
            fileStorage.saveFileDataBlocking(marcRecordContent, fileDefinition);
            String marcRecord = new String(marcRecordContent);
            marcRecords.add(marcRecord);
        }
        return marcRecords;
    }

    private List<VariableField> getMappedFields(MappingProfile mappingProfile, String jobExecutionId, OkapiConnectionParams connectionParams, List<RecordType> mappingProfileRecordTypes, JsonObject srsRecord) {
        List<VariableField> mappedFields = Collections.emptyList();
        if (CollectionUtils.isNotEmpty(mappingProfileRecordTypes)
                && (mappingProfileRecordTypes.contains(RecordType.HOLDINGS) || mappingProfileRecordTypes.contains(RecordType.ITEM))) {
            JsonObject externalIdsHolder = srsRecord.getJsonObject("externalIdsHolder");
            if (externalIdsHolder != null) {
                String instanceId = externalIdsHolder.getString("instanceId");
                if (isNotBlank(instanceId)) {
                    JsonObject holdingsAndItem = fetchHoldingsAndItems(instanceId);
                    mappedFields = mappingService.mapFields(holdingsAndItem, mappingProfile, jobExecutionId, connectionParams);
                }
            }
        }
        return mappedFields;
    }

    private JsonObject fetchHoldingsAndItems(String instanceId) {
        List<JsonObject> holdings = loadInventoryHoldingsByInstanceId(instanceId);
        List<JsonObject> items = new ArrayList<>();
        JsonObject holdingsAndItems = new JsonObject();
        for (JsonObject holding : holdings) {
            items.addAll(loadInventoryItemsByHoldingsId(holding.getString("id")));
        }
        holdingsAndItems.put("holdings", new JsonArray(holdings));
        holdingsAndItems.put("items", new JsonArray(items));
        return holdingsAndItems;
    }

    private List<JsonObject> loadInventoryHoldingsByInstanceId(String instanceId) {
        return holdings.stream()
                .filter(record -> instanceId.equals(record.getString("instanceId")))
                .collect(Collectors.toList());
    }

    private List<JsonObject> loadInventoryItemsByHoldingsId(String holdingsId) {
        return items.stream()
                .filter(record -> holdingsId.equals(record.getString("holdingsRecordId")))
                .collect(Collectors.toList());
    }

    private String getEncodedRecordContent(JsonObject srsRecord) {
        return srsRecord.getJsonObject("parsedRecord").getJsonObject("content").encode();
    }

    public List<JsonObject> getHoldings() {
        return holdings;
    }

    public void setHoldings(List<JsonObject> holdings) {
        this.holdings = holdings;
    }

    public List<JsonObject> getItems() {
        return items;
    }

    public void setItems(List<JsonObject> items) {
        this.items = items;
    }
}
