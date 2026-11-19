package com.orderfirst.api.web;

import com.orderfirst.api.port.CataloguePort;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Le territoire de l'adresse du client est transmis au catalogue (Castor) pour filtrer les offres. */
@SpringBootTest
@AutoConfigureMockMvc
class CatalogTerritoryTest {
    @Autowired
    private MockMvc mvc;

    @MockitoSpyBean
    private CataloguePort catalogue;

    @Test
    void territoryIsPassedToTheCatalogue() throws Exception {
        mvc.perform(get("/api/v1/catalog/offers").param("territoire", "GUADELOUPE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").exists());
        verify(catalogue).offers("GUADELOUPE");
    }

    @Test
    void withoutTerritoryAllOffers() throws Exception {
        mvc.perform(get("/api/v1/catalog/offers").param("territoire", " ")).andExpect(status().isOk());
        verify(catalogue).offers(isNull());
    }
}
