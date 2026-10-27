package com.gabriel.backendcontalitros.infrastructure.web;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = "contalitro.mqtt.enabled=false")
@AutoConfigureMockMvc
class DocumentacaoApiTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void raizLevaAoSwaggerUi() throws Exception {
        mvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/swagger-ui.html"));
    }

    @Test
    void documentacaoOpenApiListaAsRotas() throws Exception {
        mvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("Conta Litro API"))
                .andExpect(jsonPath("$.paths['/veiculos']").exists())
                .andExpect(jsonPath("$.paths['/veiculos/{id}/viagens']").exists())
                .andExpect(jsonPath("$.paths['/viagens/{id}']").exists())
                .andExpect(jsonPath("$.paths['/relatorios/consumo']").exists())
                .andExpect(jsonPath("$.paths['/']").doesNotExist());
    }

    @Test
    void swaggerUiEstaDisponivel() throws Exception {
        mvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("swagger-ui")));
    }
}
