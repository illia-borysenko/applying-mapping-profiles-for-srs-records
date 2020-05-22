package org.folio.mapping;

import io.vertx.core.json.JsonObject;
import org.folio.mapping.processor.RuleFactory;
import org.folio.mapping.processor.RuleProcessor;
import org.folio.mapping.processor.rule.Rule;
import org.folio.mapping.profile.MappingProfile;
import org.folio.mapping.reader.EntityReader;
import org.folio.mapping.reader.JPathSyntaxEntityReader;
import org.folio.mapping.settings.Settings;
import org.folio.mapping.writer.RecordWriter;
import org.folio.mapping.writer.impl.MarcRecordWriter;
import org.folio.mocks.MappingSettingsProvider;
import org.folio.mocks.OkapiConnectionParams;
import org.marc4j.marc.VariableField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;

public class MappingServiceImpl implements MappingService {
  private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private MappingSettingsProvider settingsProvider;
  private RuleFactory ruleFactory;
  private RuleProcessor ruleProcessor;

  public MappingServiceImpl() {
    this.settingsProvider = new MappingSettingsProvider();
    this.ruleProcessor = new RuleProcessor();
    this.ruleFactory = new RuleFactory();
  }

  @Override
  public List<String> map(List<JsonObject> instances, MappingProfile mappingProfile, String jobExecutionId, OkapiConnectionParams connectionParams) {
    List<String> records = new ArrayList<>();
    Settings settings = settingsProvider.getSettings(jobExecutionId, connectionParams);
    List<Rule> rules = ruleFactory.create(mappingProfile);
    for (JsonObject instance : instances) {
      EntityReader entityReader = new JPathSyntaxEntityReader(instance);
      RecordWriter recordWriter = new MarcRecordWriter();
      String record = this.ruleProcessor.process(entityReader, recordWriter, settings, rules);
      records.add(record);
    }
    return records;
  }

  @Override
  public List<VariableField> mapFields(JsonObject record, MappingProfile mappingProfile, String jobExecutionId, OkapiConnectionParams connectionParams) {
    Settings settings = settingsProvider.getSettings(jobExecutionId, connectionParams);
    List<Rule> rules = ruleFactory.create(mappingProfile);
    EntityReader entityReader = new JPathSyntaxEntityReader(record);
    RecordWriter recordWriter = new MarcRecordWriter();
    return this.ruleProcessor.processFields(entityReader, recordWriter, settings, rules);
  }


}
