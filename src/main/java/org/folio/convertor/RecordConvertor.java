package org.folio.convertor;

import org.marc4j.marc.VariableField;

import java.util.List;

public interface RecordConvertor {

    byte[] convert(String jsonRecord, List<VariableField> generatedFields);
}
