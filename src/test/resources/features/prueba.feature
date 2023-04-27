Feature: Uno

  @requestTravel
  Scenario Outline: Uno
    Given que inicio la sesion de el AS400 <Row>
      | Route Excel | src/test/resources/datadriven/Data_BancoOccidente.xlsx |
      | Tab         | creacion                                               |
    And ingreso usuario y clave
    And cerrar sesion

    Examples:
      | Row |
      ##@externaldata@src/test/resources/datadriven/Data_BancoOccidente.xlsx@creacion
|1|
|2|
|3|
|4|
|5|
|6|
|7|
