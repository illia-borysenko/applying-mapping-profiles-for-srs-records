package org.folio.mapping;

import io.vertx.core.json.JsonObject;
import org.folio.mapping.profile.MappingProfile;
import org.folio.mocks.OkapiConnectionParams;
import org.marc4j.marc.VariableField;

import java.util.List;

public interface MappingService {

  /**
   * Performs mapping to marc records
   *
   * @param records FOLIO records
   * @param jobExecutionId job id
   * @param connectionParams okapi connection parameters
   * @return marc records
   */
  List<String> map(List<JsonObject> records, MappingProfile mappingProfile, String jobExecutionId, OkapiConnectionParams connectionParams);

  List<VariableField> mapFields(JsonObject record, MappingProfile mappingProfile, String jobExecutionId, OkapiConnectionParams connectionParams);
}
