package org.folio.mocks;

public class LocalFileSystemStorage implements FileStorage {

    @Override
    public FileDefinition saveFileDataBlocking(byte[] data, FileDefinition fileDefinition) {
        //save data to file
        return fileDefinition;
    }
}
