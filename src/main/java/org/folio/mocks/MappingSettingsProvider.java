package org.folio.mocks;

import org.folio.mapping.settings.Settings;

public class MappingSettingsProvider {

    public Settings getSettings(String jobExecutionId, OkapiConnectionParams connectionParams) {
        return new Settings();
    }
}
