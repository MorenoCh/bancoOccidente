package com.bancoOccidente.util;

public class Excel {
	
	private String rutaExcel;
	private String hojaExcel;
	private boolean contieneCabecera;
	private int filaLeer;
	
	
	/**
	 * Método constructor
	 * 
	 * @params strRutaExcel String con ruta a leer
	 * @params strHojaExcel String con hoja a leer
	 * @params boolContieneCabecera booleano que indica si se lee la cabecera
	 * @params intFilaLeer entero con fila a leer
	 */
	public Excel(String strRutaExcel, String strHojaExcel, boolean boolContieneCabecera, int intFilaLeer) {
		super();
		this.rutaExcel = strRutaExcel;
		this.hojaExcel = strHojaExcel;
		this.contieneCabecera = boolContieneCabecera;
		this.filaLeer = intFilaLeer;
	}
	
	/**
	 * Serie de método set y get que exponen los objetos de la clase	 
	 */
	
	public String getRutaExcel() {
		return rutaExcel;
	}
	
	public void setRutaExcel(String strRutaExcel) {
		this.rutaExcel = strRutaExcel;
	}
	
	public String getHojaExcel() {
		return hojaExcel;
	}
	
	public void setHojaExcel(String strHojaExcel) {
		this.hojaExcel = strHojaExcel;
	}
	
	public boolean isContieneCabecera() {
		return contieneCabecera;
	}
	
	public void setContieneCabecera(boolean boolContieneCabecera) {
		this.contieneCabecera = boolContieneCabecera;
	}
	
	public int getFilaLeer() {
		return filaLeer;
	}
	
	public void setFilaLeer(int intFilaLeer) {
		this.filaLeer = intFilaLeer;
	}

}
