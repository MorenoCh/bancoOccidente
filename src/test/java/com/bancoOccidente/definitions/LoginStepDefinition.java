package com.bancoOccidente.definitions;

import com.bancoOccidente.steps.LoginStep;
import com.bancoOccidente.util.Constants;
import com.bancoOccidente.util.UtilAS400;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.java.es.Cuando;
import io.cucumber.java.es.Dado;
import io.cucumber.java.es.Entonces;
import io.cucumber.java.es.Y;
import net.thucydides.core.annotations.Steps;


public class LoginStepDefinition {

    @Steps
    private LoginStep loginStep;

    private Scenario scenario;

    @Before
    public void beforeScenario(Scenario scenario) {
        this.scenario = scenario;
        UtilAS400.saveVariableOnSession(Constants.SCENARIO, this.scenario);
    }

    @Given("^que inicio la sesion de el AS400 (.*)$")
    public void que_inicio_la_sesion_de_el_as400(int intRow, DataTable tbDatosExcel) throws Exception {
        loginStep.readDataDriven(intRow, tbDatosExcel);
        loginStep.iniciar_sesion();
    }

    @When("^estoy en la pantalla de login (.*)$")
    public void estoy_en_la_pantalla_de_login(String pantalla) {
        loginStep.ValidacionPantallaInicio(pantalla);
    }

    @And("^ingreso usuario y clave$")
    public void ingreso_usuario_y_clave() {
        loginStep.ingresar_credenciales();
    }

    @And("^cerrar sesion$")
    public void cerrarSesion(){
        loginStep.cerrarSesion();
    }

    @Then("valido que estoy en la pantalla {string}")
    public void valido_que_estoy_en_la_pantalla(String pantalla) {
        loginStep.ValidacionPantallaInicio(pantalla);
    }

    @Entonces("busco en la pantalla {string}")
    public void buscoEnLaPantalla(String arg0) {
        loginStep.ValidarArchivo(arg0);
    }

    @Y("cierro session AS400")
    public void cierro_session_as400() {
        loginStep.IngresarMenu("90");
    }
}
