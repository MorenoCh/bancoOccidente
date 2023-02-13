#language: es
Característica: Interactuar con el Mainframe
  Yo como usuario
  Quiero ingresar al mainframe del banco
  Para validar los resultados de operacion

  @login
  Escenario: Iniciar Sesion con el Mainframe del banco Prueba
    Dado que inicio la sesion de el AS400
    Cuando estoy en la pantalla de login "Welcome to PUB400.COM"
    Y ingreso usuario "CAMPOS" y clave "campos123*"
    Entonces valido que estoy en la pantalla "Main Menu"
    Entonces busco en la pantalla "Programming"
    Y cierro session AS400