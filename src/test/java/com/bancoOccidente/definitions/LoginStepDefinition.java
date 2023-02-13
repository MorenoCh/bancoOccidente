package com.bancoOccidente.definitions;

import com.bancoOccidente.steps.LoginStep;
import com.bancoOccidente.util.Constants;
import com.bancoOccidente.util.UtilAS400;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
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

    @Dado("que inicio la sesion de el AS400")
    public void que_inicio_la_sesion_de_el_as400() throws Exception {
        loginStep.iniciar_sesion();
    }

    @Cuando("estoy en la pantalla de login {string}")
    public void estoy_en_la_pantalla_de_login(String pantalla) {
        loginStep.ValidacionPantallaInicio(pantalla);
    }

    @Y("ingreso usuario {string} y clave {string}")
    public void ingreso_usuario_y_clave(String usuario, String clave) {
        loginStep.ingresar_credenciales(usuario, clave);
    }

    @Entonces("valido que estoy en la pantalla {string}")
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
