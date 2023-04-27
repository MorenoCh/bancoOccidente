package com.bancoOccidente.steps;

import com.bancoOccidente.util.Constants;
import com.bancoOccidente.util.DataDrivenExcel;
import com.bancoOccidente.util.UtilAS400;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.Scenario;
import net.serenitybdd.core.Serenity;
import net.thucydides.core.annotations.Step;
import tn5250.TerminalDriver;

import static org.junit.Assert.assertTrue;

public class LoginStep {

    private Scenario scenario;

    private TerminalDriver driver;

    DataDrivenExcel dataDrivenUtil;

    @Step
    public void iniciar_sesion() throws Exception {
        scenario = UtilAS400.getVariableOnSession(Constants.SCENARIO);
        driver = new TerminalDriver("PUB400.com", "23").connect();
        assertTrue(driver.isConnected());
        addCucumber("Iniciar session");
    }

    @Step("Validacion de pantalla de inicio")
    public void ValidacionPantallaInicio(String val) {
        if(driver.assertScreen(val)){

        }
        addCucumber("Validacion pantalla de inicio" + " " + val);
    }

    @Step("Ingreso las credenciales")
    public void ingresar_credenciales() {
        String usuario = dataDrivenUtil.getDataMap("Usuario");
        String contraseña = dataDrivenUtil.getDataMap("Contra");
        driver.writeOnField(usuario,0);
        driver.writeOnField(contraseña,1);
        addCucumber("Ingreso credenciales");
        driver.sendEnter();
        addCucumber("Valido la sesion");

    }

    @Step("ff")
    public void cerrarSesion(){
        driver.disconectedSession();
    }

    @Step("Validar Archivo")
    public void ValidarArchivo(String archivo) {
        String resp = driver.ScreenText();
        assertTrue(resp.contains(archivo));
        addCucumber("Buscar en pantalla" + " " + archivo);
    }

    @Step
    public void readDataDriven(int intRow, DataTable tbDatosExcel){
        DataDrivenExcel.readDataDrivenAVE(intRow,tbDatosExcel);
    }

    @Step("Ingreso al menu")
    public void IngresarMenu(String menu) {
        //driver.writeOnField(menu,0);
        //.sendEnter();


        addCucumber("Cerar session");
    }

    private void addCucumber(String titulo){
        String resp = driver.ScreenText();

        //driver.ScreenText().substring().replaceAll().trim()

        Serenity.recordReportData().withTitle(titulo).andContents(resp);
    }
}
