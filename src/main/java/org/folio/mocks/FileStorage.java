package org.folio.mocks;

public interface FileStorage {

    FileDefinition saveFileDataBlocking(byte[] data, FileDefinition fileDefinition);
}