package org.folio.convertor;

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

public class MarcRecordConvertor implements RecordConvertor{

    public byte[] convert(String jsonRecord){
        MarcReader marcJsonReader = new MarcJsonReader(new ByteArrayInputStream(jsonRecord.getBytes(StandardCharsets.UTF_8)));
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        MarcWriter marcStreamWriter = new MarcStreamWriter(byteArrayOutputStream);
        while (marcJsonReader.hasNext()) {
            Record record = marcJsonReader.next();
            marcStreamWriter.write(record);
        }
        return byteArrayOutputStream.toByteArray();
    }

    public byte[] convert(String jsonRecord, List<VariableField> generatedFields) {
        MarcReader marcJsonReader = new MarcJsonReader(new ByteArrayInputStream(jsonRecord.getBytes(StandardCharsets.UTF_8)));
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        MarcWriter marcStreamWriter = new MarcStreamWriter(byteArrayOutputStream);
        SortedMarcFactoryImpl sortedMarcFactory = new SortedMarcFactoryImpl();
        while (marcJsonReader.hasNext()) {
            Record record = marcJsonReader.next();
            Record sortedRecord = sortedMarcFactory.newRecord();
            for (VariableField srsField: record.getVariableFields()) {
                sortedRecord.addVariableField(srsField);
            }
            for (VariableField generatedField : generatedFields) {
                sortedRecord.addVariableField(generatedField);
            }
            marcStreamWriter.write(sortedRecord);
        }
        return byteArrayOutputStream.toByteArray();
    }

}
