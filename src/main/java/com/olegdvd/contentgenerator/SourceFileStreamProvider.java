package com.olegdvd.contentgenerator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

class SourceFileStreamProvider {

    FileInputStream getInputSourceStream(String fileName) {

        File sourceXlsxFile = new File(fileName);
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(sourceXlsxFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return fileInputStream;
    }

    FileOutputStream getOutputSourceStream(String fileName) {

        File destXlsxFile = new File(fileName);
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(destXlsxFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return fileOutputStream;
    }
}
