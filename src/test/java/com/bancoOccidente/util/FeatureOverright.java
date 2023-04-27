package com.bancoOccidente.util;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.bancoOccidente.util.ExcelReader;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.base.Charsets;


public class FeatureOverright {

    static final Logger logger = LoggerFactory.getLogger(FeatureOverright.class);

    private FeatureOverright() {

        throw new IllegalStateException("FeatureOverright");
    }

    private static List<String> setExcelDataToFeature(File featureFile) throws InvalidFormatException, IOException {

        List<String> fileData = new ArrayList<>();

        try (BufferedReader buffReader = new BufferedReader( new InputStreamReader(new BufferedInputStream(new FileInputStream(featureFile)), Charsets.UTF_8))) {

            String data;
            boolean featureData = false;

            while ((data = buffReader.readLine()) != null) {

                if (data.trim().contains("##@externaldata")) {

                    fileData = fefe(data,fileData);
                    featureData = true;
                }else {

                    if(data.startsWith("|")||data.endsWith("|")){

                        if(!featureData){

                            fileData.add(data);
                        }
                    }else {

                        fileData.add(data);
                        featureData = false;
                    }
                }
            }
        }

        return fileData;
    }

    private static List<String> fefe(String data, List<String> fileData) throws InvalidFormatException, IOException{

        String sheetName = null;
        String excelFilePath = null;
        excelFilePath = data.substring(StringUtils.ordinalIndexOf(data, "@", 2)+1, data.lastIndexOf('@'));
        sheetName = data.substring(data.lastIndexOf('@')+1, data.length());
        fileData.add(data);

        return iteracionArraysData(new ExcelReader().getData(excelFilePath, sheetName), fileData);
    }

    private static List<File> listOfFeatureFiles(File folder) {

        List<File> featureFiles = new ArrayList<>();

        for (File fileEntry : folder.listFiles()) {

            if (fileEntry.isDirectory()) {

                featureFiles.addAll(listOfFeatureFiles(fileEntry));

            }else {

                if (fileEntry.isFile() && fileEntry.getName().endsWith(".feature")) {

                    featureFiles.add(fileEntry);
                }
            }

        }

        return featureFiles;
    }

    public static void overrideFeatureFiles(String featuresDirectoryPath) throws IOException, InvalidFormatException {

        List<File> listOfFeatureFiles = listOfFeatureFiles(new File(featuresDirectoryPath));

        for (File featureFile : listOfFeatureFiles) {

            if(!featureFile.setWritable(true)) {

                logger.info("No escribe el feature {} " , featureFile );
            }


            List<String> featureWithExcelData = setExcelDataToFeature(featureFile);

            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(featureFile), Charsets.UTF_8));) {

                for (String string : featureWithExcelData) {
                    writer.write(string);
                    writer.write("\n");
                }
            }


        }
    }

    private static List<String> iteracionArraysData(List<Map<String, String>> excelData, List<String> fileData) {

        int cont = 1;

        for (int rowNumber = 0; rowNumber < excelData.size()-1; rowNumber++) {

            StringBuilder cellData = new StringBuilder();

            for (Entry<String, String> mapData : excelData.get(rowNumber).entrySet()) {

                if(mapData.getValue().equalsIgnoreCase(String.valueOf(cont))) {

                    cellData.append("|" + mapData.getValue());
                    cont++;
                }
            }

            fileData.add(cellData.toString() + "|");
        }

        logger.info("Escritura correcta del feature");

        return fileData;
    }
}