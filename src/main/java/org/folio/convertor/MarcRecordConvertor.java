package org.folio.convertor;

import org.apache.commons.collections4.CollectionUtils;
import org.marc4j.MarcJsonReader;
import org.marc4j.MarcReader;
import org.marc4j.MarcStreamWriter;
import org.marc4j.MarcWriter;
import org.marc4j.marc.Record;
import org.marc4j.marc.VariableField;
import org.marc4j.marc.impl.SortedMarcFactoryImpl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class MarcRecordConvertor implements RecordConvertor {

    private SortedMarcFactoryImpl sortedMarcFactory = new SortedMarcFactoryImpl();

    public byte[] convert(String jsonRecord, List<VariableField> additionalFields) {
        MarcReader marcJsonReader = new MarcJsonReader(new ByteArrayInputStream(jsonRecord.getBytes(StandardCharsets.UTF_8)));
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        MarcWriter marcStreamWriter = new MarcStreamWriter(byteArrayOutputStream);
        while (marcJsonReader.hasNext()) {
            Record record = marcJsonReader.next();
            if (CollectionUtils.isNotEmpty(additionalFields)) {
              record = appendAdditionalFields(record, additionalFields);
            }
            marcStreamWriter.write(record);
        }
        return byteArrayOutputStream.toByteArray();
    }

    private Record appendAdditionalFields(Record record, List<VariableField> additionalFields) {
        Record sortedRecord = sortedMarcFactory.newRecord();
        for (VariableField srsField : record.getVariableFields()) {
            sortedRecord.addVariableField(srsField);
        }
        for (VariableField generatedField : additionalFields) {
            sortedRecord.addVariableField(generatedField);
        }
        return sortedRecord;
    }

}
