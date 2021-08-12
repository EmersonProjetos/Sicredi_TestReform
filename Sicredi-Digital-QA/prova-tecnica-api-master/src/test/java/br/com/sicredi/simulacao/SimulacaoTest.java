package br.com.sicredi.simulacao;

import br.com.sicredi.simulacao.repository.SimulacaoRepository;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import br.com.sicredi.util.ResourceUtils;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = Application.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SimulacaoTest {

    @LocalServerPort
    private int serverPort;

    @Autowired
    private LoadDatabase loadDatabase;

    @Autowired
    private SimulacaoRepository simulacaoRepository;

    private String jsonSimulacaoSucesso;
    private String jsonSimulacaoFalhaFaltaNome;
    private String getJsonSimulacaoNomeAtualizadoSucesso;

    @BeforeEach
    public void setUp() {
        enableLoggingOfRequestAndResponseIfValidationFails();
        baseURI = "http://localhost";
        port = serverPort;
        basePath = "/api/v1/simulacoes/";

        jsonSimulacaoSucesso = ResourceUtils.getContentFromResource(
                "/simulacao-sucesso.json");
        jsonSimulacaoFalhaFaltaNome = ResourceUtils.getContentFromResource(
                "/simulacao-falha-falta-nome.json");
        getJsonSimulacaoNomeAtualizadoSucesso = ResourceUtils.getContentFromResource(
                "/simulacao-nome-atualizado-sucesso.json");

        try {
            simulacaoRepository.deleteAll();
            loadDatabase.initDatabaseSimulacao(simulacaoRepository).run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void deveRetornarStatus200_QuandoListarAsSimulacoes(){
        given()
                .accept(ContentType.JSON)
        .when()
                .get()
        .then()
                .statusCode(HttpStatus.OK.value())
                .body("id", hasSize(2));
    }

    @Test
    public void deveRetornarStatus200_QuandoConsultarSimulacaoEncontrada(){
        given()
                .accept(ContentType.JSON)
        .when()
                .get("17822386034")
        .then()
                .statusCode(HttpStatus.OK.value())
                .body("nome", equalTo("Deltrano"))
                .body("email", equalTo("deltrano@gmail.com"));
    }

    @Test
    public void deveRetornarStatus404_QuandoConsultarSimulacaoNaoEncontrada(){
        given()
                .accept(ContentType.JSON)
                .when()
                .get("85471203003")
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .body("mensagem", equalTo("CPF 85471203003 não encontrado"));
    }

    @Test
    public void deveRetornarStatus201_QuandoCriarSimulacao(){
        given()
                .body(jsonSimulacaoSucesso)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
        .when()
                .post()
        .then()
                .statusCode(HttpStatus.CREATED.value())
                .body("nome", equalTo("Ciclano de Tal"))
                .body("cpf", equalTo("58253209037"))
                .body("email", equalTo("ciclanodetal@email.com"));
    }

    @Test
    public void deveRetornarStatus400_QuandoCriarSimulacaoFaltandoNome(){
        given()
                .body(jsonSimulacaoFalhaFaltaNome)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
        .when()
                .post()
        .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("erros.nome", equalTo("Nome não pode ser vazio"));
    }

    @Test
    public void deveRetornarStatus409_QuandoCriarSimulacaoJaExistente(){
        deveRetornarStatus201_QuandoCriarSimulacao();

        given()
                .body(jsonSimulacaoSucesso)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
        .when()
                .post()
        .then()
                .statusCode(HttpStatus.CONFLICT.value())
                .body("mensagem", equalTo("CPF duplicado"));
    }

    @Test
    public void deveRetornarStatus200_QuandoAtualizarNomeSimulacao(){
        given()
                .body(getJsonSimulacaoNomeAtualizadoSucesso)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
        .when()
                .put("66414919004")
        .then()
                .statusCode(HttpStatus.OK.value())
                .body("nome", equalTo("Fulano Atualizado"));
    }

    @Test
    public void deveRetornarStatus404_QuandoAtualizarNomeSimulacaoNaoEncontrada(){
        given()
                .body(getJsonSimulacaoNomeAtualizadoSucesso)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
        .when()
                .put("58253209037")
        .then()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .body("mensagem", equalTo("CPF 58253209037 não encontrado"));
    }

    @Test
    public void deveRetornarStatus200_QuandoExcluirSimulacaoCPF_Existente(){
        given()
                .accept(ContentType.JSON)
        .when()
                .delete("66414919004")
        .then()
                .log().all()
                .statusCode(HttpStatus.OK.value());

        Assertions.assertFalse(consultarSimulacaoPorCPF("66414919004"));
    }

    private boolean consultarSimulacaoPorCPF(String cpf) {
        return simulacaoRepository.findByCpf(cpf).isPresent();
    }
}
