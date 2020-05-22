package org.folio.mapping.processor;


import org.folio.mapping.processor.rule.Rule;
import org.folio.mapping.processor.translations.Translation;
import org.folio.mapping.processor.translations.TranslationFunction;
import org.folio.mapping.processor.translations.TranslationsHolder;
import org.folio.mapping.reader.EntityReader;
import org.folio.mapping.reader.values.CompositeValue;
import org.folio.mapping.reader.values.ListValue;
import org.folio.mapping.reader.values.RuleValue;
import org.folio.mapping.reader.values.SimpleValue;
import org.folio.mapping.reader.values.StringValue;
import org.folio.mapping.settings.Settings;
import org.folio.mapping.writer.RecordWriter;
import org.marc4j.marc.VariableField;

import java.util.ArrayList;
import java.util.List;

import static org.folio.mapping.reader.values.SimpleValue.SubType.LIST_OF_STRING;
import static org.folio.mapping.reader.values.SimpleValue.SubType.STRING;

/**
 * RuleProcessor is a central part of mapping.
 * <p>
 * High-level algorithm:
 * # read data by the given rule
 * # translate data
 * # write data
 *
 * @see EntityReader
 * @see TranslationFunction
 * @see RecordWriter
 */
public final class RuleProcessor {
  private static final String LEADER_FIELD = "leader";

  public String process(EntityReader reader, RecordWriter writer, Settings settings, List<Rule> rules) {
    rules.forEach(rule -> {
      if (LEADER_FIELD.equals(rule.getField())) {
        rule.getDataSources().forEach(dataSource -> writer.writeLeader(dataSource.getTranslation()));
      } else {
        processRule(reader, writer, settings, rule);
      }
    });
    return writer.getResult();
  }

  public List<VariableField> processFields(EntityReader reader, RecordWriter writer, Settings settings, List<Rule> rules) {
    rules.forEach(rule -> {
      processRule(reader, writer, settings, rule);
    });
    return writer.getFields();
  }

  private void processRule(EntityReader reader, RecordWriter writer, Settings settings, Rule rule) {
    RuleValue ruleValue = reader.read(rule);
    switch (ruleValue.getType()) {
      case SIMPLE:
        SimpleValue simpleValue = (SimpleValue) ruleValue;
        translate(simpleValue, settings);
        writer.writeField(rule.getField(), simpleValue);
        break;
      case COMPOSITE:
        CompositeValue compositeValue = (CompositeValue) ruleValue;
        translate(compositeValue, settings);
        writer.writeField(rule.getField(), compositeValue);
        break;
      case MISSING:
    }
  }

  private void translate(SimpleValue simpleValue, Settings settings) {
    Translation translation = simpleValue.getDataSource().getTranslation();
    if (translation != null) {
      TranslationFunction translationFunction = TranslationsHolder.lookup(translation.getFunction());
      if (STRING.equals(simpleValue.getSubType())) {
        StringValue stringValue = (StringValue) simpleValue;
        String readValue = stringValue.getValue();
        String translatedValue = translationFunction.apply(readValue, translation, settings);
        stringValue.setValue(translatedValue);
      } else if (LIST_OF_STRING.equals(simpleValue.getSubType())) {
        ListValue listValue = (ListValue) simpleValue;
        List<String> translatedValues = new ArrayList<>();
        for (String readValue : listValue.getValue()) {
          String translatedValue = translationFunction.apply(readValue, translation, settings);
          translatedValues.add(translatedValue);
        }
        listValue.setValue(translatedValues);
      }
    }
  }

  private void translate(CompositeValue compositeValue, Settings settings) {
    List<List<StringValue>> readValues = compositeValue.getValue();
    for (List<StringValue> readEntry : readValues) {
      readEntry.forEach(stringValue -> {
        Translation translation = stringValue.getDataSource().getTranslation();
        if (translation != null) {
          TranslationFunction translationFunction = TranslationsHolder.lookup(translation.getFunction());
          String readValue = stringValue.getValue();
          String translatedValue = translationFunction.apply(readValue, translation, settings);
          stringValue.setValue(translatedValue);
        }
      });
    }
  }
}
