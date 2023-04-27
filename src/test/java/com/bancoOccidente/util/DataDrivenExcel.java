package com.bancoOccidente.util;

import io.cucumber.datatable.DataTable;
import net.serenitybdd.core.pages.PageObject;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.sql.Types.NUMERIC;

public class DataDrivenExcel extends PageObject{

    private static DecimalFormat df = new DecimalFormat("0.###");
    static Map<String, String> data = new HashMap<>();
    static Logger logger = Logger.getLogger( DataDrivenExcel.class.getName());
    static Map<String, String> mapDatosExcel = new HashMap<>();

    /**
     * Metodo que lee el contenido de un archivo Excel
     *
     * @param excel variable con los datos de ruta y hoja a leer de libro de Excel
     */
    public static Map<String, String> leerExcel(Excel excel) {
        // Variable que contendra todas las filas

        try {
            // Invocacion y uso del excel
            FileInputStream arcExcel = new FileInputStream(new File(excel.getRutaExcel()));
            Workbook libroExcel = new XSSFWorkbook(arcExcel);
            // Hoja del excel a usar
            Sheet hojaArcExcel = libroExcel.getSheet(excel.getHojaExcel());
            Iterator<Row> iterator = hojaArcExcel.iterator();

            FileOutputStream salida = new FileOutputStream(excel.getRutaExcel());
            libroExcel.write(salida);

            arcExcel.close();
            salida.close();

            ArrayList<String> cabeceras = new ArrayList<>();

            iteraction(iterator, excel, cabeceras);

        } catch (IOException e) {
            logger.log(Level.INFO, e.getMessage());
        }
        return mapDatosExcel;
    }

    /**
     * Metodo que genera las interacciones con el Excel
     *
     * @param excel variable con los datos de ruta y hoja a leer de libro de Excel
     * @param iterator variable que lleva el conteo de filas del Excel
     * @param cabeceras variable que contiene el valor de las cabeceras de columna
     */
    public static void iteraction(Iterator<Row> iterator, Excel excel, List<String> cabeceras) {
        // Ciclo de iteraci�n por cada fila
        while (iterator.hasNext()) {
            Row filaActual = iterator.next();
            Iterator<Cell> iteratorCelda = filaActual.iterator();
            // Variable que almacenara cada fila
            int intNumFila = filaActual.getRowNum();

            if ((excel.isContieneCabecera() && intNumFila == 0) || intNumFila == excel.getFilaLeer()) {

                // Ciclo de celdas o columnas de la hoja del excel
                while (iteratorCelda.hasNext()) {
                    Cell celdaActual = iteratorCelda.next();
                    fillDataMap(celdaActual, excel, intNumFila, cabeceras);
                }
            }
        }
    }

    /**
     * Metodo que genera llena el mapa de datos
     *
     * @param excel variable con los datos de ruta y hoja a leer de libro de Excel
     * @param celdaActual variable que contiene el valor de la celda actual que esta siendo leida
     * @param cabeceras variable que contiene el valor de las cabeceras de columna
     * @param intNumFila contador que da limite a las filas a leer
     */
    public static void fillDataMap(Cell celdaActual, Excel excel, int intNumFila, List<String> cabeceras) {

        String strValorCelda = "";

        // Validar tipo de celda para procesarla
        if (celdaActual.getCellType()==Cell.CELL_TYPE_NUMERIC) {
            if (DateUtil.isCellDateFormatted(celdaActual)) {
                strValorCelda = "" + celdaActual.getDateCellValue().getTime();
            } else {
                strValorCelda = df.format(celdaActual.getNumericCellValue());
            }
        }else {
            strValorCelda = celdaActual.getStringCellValue();
        }

        // Validar si tiene cabecera o no
        if (excel.isContieneCabecera()) {
            if (intNumFila == 0) {
                cabeceras.add(strValorCelda);
            } else {
                mapDatosExcel.put(cabeceras.get(celdaActual.getColumnIndex()), strValorCelda);
            }
        } else {
            if (intNumFila == excel.getFilaLeer()) {
                mapDatosExcel.put("" + celdaActual.getColumnIndex() + "", strValorCelda);
            }
        }
    }

    /**
     * Metodo que extrae los datos de ruta y hoja de libro Excel
     *
     * @param intRow entero de fila a leer de Excel
     * @param tbDatosExcel datos de ruta y hoja de libro de Excel
     */
    public static void readDataDrivenAVE(int intRow, DataTable tbDatosExcel) {
        data = tbDatosExcel.asMap(String.class, String.class);
        Excel excel = new Excel(data.get("Route Excel"), data.get("Tab"), true, intRow);
        data = leerExcel(excel);
    }

    /**
     * Metodo que extrae los datos de mapa
     *
     * @param strDatoSolicitado String con valor de key a consultar en el mapa
     */
    public String getDataMap(String strDatoSolicitado){
        return data.get(strDatoSolicitado);
    }

    /**
     * Metodo que agrega datos al mapa
     *
     * @param strKey String con valor de key a agregar en el mapa
     * @param strValue String con valor de dato a agregar en el mapa
     */
    public void setDataMap(String strKey, String strValue) {
        data.put(strKey, strValue);
    }


    /**
     * Metodo que reemplaza datos al mapa
     *
     * @param strKey String con valor de key a reemplazar en el mapa
     * @param strValue String con valor de dato a reemplazar en el mapa
     */
    public void replaceDataMap(String strKey, String strValue) {
        data.replace(strKey, strValue);
    }

    public int getCountMap(){
      return  mapDatosExcel.size() - 1;
    }
}
