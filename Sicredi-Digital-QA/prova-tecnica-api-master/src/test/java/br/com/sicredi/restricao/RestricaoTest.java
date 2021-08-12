package br.com.sicredi.restricao;

import br.com.sicredi.simulacao.Application;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = Application.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class RestricaoTest {

    @LocalServerPort
    private int serverPort;

    @BeforeEach
    public void setUp(){
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        baseURI = "http://localhost";
        port = serverPort;
        basePath = "/api/v1/restricoes/";
    }

    @Test
    public void deveRetornarStatus200_QuandoConsultarCPF_ComRestricao(){
        given()
                .accept(ContentType.JSON)
        .when()
                .get("97093236014")
        .then()
                .statusCode(HttpStatus.OK.value())
                .body("mensagem", equalTo("O CPF 97093236014 tem problema"));
    }

    @Test
    public void deveRetornarStatus204_QuandoConsultarCPF_SemRestricao(){
        given()
                .accept(ContentType.JSON)
        .when()
                .get("85471203003")
        .then()
                .statusCode(HttpStatus.NO_CONTENT.value());
    }
}
