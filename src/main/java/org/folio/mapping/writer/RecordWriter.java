package org.folio.mapping.writer;


import org.folio.mapping.processor.translations.Translation;
import org.folio.mapping.reader.values.CompositeValue;
import org.folio.mapping.reader.values.RuleValue;
import org.folio.mapping.reader.values.SimpleValue;
import org.marc4j.marc.VariableField;

import java.util.List;

/**
 * The root interface for writers.
 * Writer is responsible to write given value to underlying marc record
 *
 * @see RuleValue
 * @see SimpleValue
 * @see CompositeValue
 */
public interface RecordWriter {

  /**
   * Updates leader using information from the given translation
   *
   * @param translation translation of the mapping rule to update leader
   */
  void writeLeader(Translation translation);

  /**
   * Writes simple value to record whether control field or data field.
   *
   * @param field   destination field
   * @param simpleValue simple value
   */
  <S extends SimpleValue> void writeField(String field, S simpleValue);

  /**
   * Writes composite value to record. This can be only data field.
   *
   * @param field   destination field
   * @param compositeValue composite value
   */
  void writeField(String field, CompositeValue compositeValue);

  /**
   * Returns final result of writing
   *
   * @return underlying record in string representation
   */
  String getResult();

  List<VariableField> getFields();
}
