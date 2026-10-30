package com.orderfirst.api.web;

import com.orderfirst.api.domain.Model;
import com.orderfirst.api.port.OrderEventPublisher;
import com.orderfirst.api.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Parcours de bout en bout sur l'API, avec les API du SI bouchonnées. */
@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class OrderFirstApiTest {
    private static final String MARIE = """
            {"civilite":"Mme","nom":"Dupont","prenom":"Marie","dateNaissance":"1988-03-14",
             "departementNaissance":"971 · Guadeloupe","nationalite":"Française","pieceType":"CNI",
             "pieceNumero":"12AB34567","mobileContact":"0690123456","email":"marie.dupont@exemple.fr"}""";
    private static final String ADDRESS = """
            {"ligne":"12 rue des Flamboyants","codePostal":"97100","commune":"Basse-Terre","complement":""}""";
    private static final String TODAY = LocalDate.now(ZoneId.of("America/Guadeloupe")).toString();

    /** Messages publiés dans le bus PubSub pendant le test. */
    private static final List<Object> PUBLISHED = new ArrayList<>();

    @TestConfiguration
    static class CaptureBus {
        @Bean
        @Primary
        OrderEventPublisher capture() {
            return PUBLISHED::add;
        }
    }

    @Autowired
    private MockMvc mvc;

    @BeforeEach
    void clearBus() {
        PUBLISHED.clear();
    }

    private static OrderService.OrderValidated published() {
        return (OrderService.OrderValidated) PUBLISHED.getLast();
    }

    private ResultActions send(final String url, final String json) throws Exception {
        return mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post(url)
                .contentType(MediaType.APPLICATION_JSON).content(json));
    }

    private String mandate() throws Exception {
        final var body = send("/api/v1/sepa-mandates", """
                {"holder":"Marie Dupont","iban":"FR76 3000 6000 0112 3456 7890 189"}""")
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return body.replaceAll(".*\"reference\":\"([^\"]+)\".*", "$1");
    }

    private String contract(final String order) throws Exception {
        final var body = send("/api/v1/contracts", order).andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return body.replaceAll(".*\"documentId\":\"([^\"]+)\".*", "$1");
    }

    private static String confirmed(final String order, final String contractRef) {
        return order.substring(0, order.lastIndexOf('}'))
                + ",\"contractRef\":\"%s\",\"contractSigned\":true,\"cgvAccepted\":true,\"privacyAccepted\":true}".formatted(contractRef);
    }

    @Test
    void identificationByMsisdn() throws Exception {
        mvc.perform(get("/api/v1/customers").param("msisdn", "06 90 12 34 56"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("CL-000101"))
                .andExpect(jsonPath("$.lines[2].isCard").value(true))
                .andExpect(jsonPath("$.lines[2].monthlyPrice").isEmpty());
        mvc.perform(get("/api/v1/customers").param("msisdn", "0690000001"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
        mvc.perform(get("/api/v1/customers/CL-000101")).andExpect(status().isOk());
        mvc.perform(get("/api/v1/customers/CL-999999")).andExpect(status().isNoContent());
        mvc.perform(get("/api/v1/contracts/DOC-inconnu")).andExpect(status().isNoContent());
        mvc.perform(get("/api/v1/customers").param("msisdn", "12"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("msisdn"));
        mvc.perform(get("/api/v1/customers/CL-000101/pending-lines")).andExpect(jsonPath("$[0]").value("0690550125"));
    }

    @Test
    void customerCreationRunsPreventel() throws Exception {
        send("/api/v1/customers", """
                {"identity":{"civilite":"M.","nom":"Rouge","prenom":"Test","dateNaissance":"1990-01-01","pieceType":"CNI","pieceNumero":"AB1234567"}}""")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.score").value("rouge"))
                .andExpect(jsonPath("$.customer").isEmpty());
        send("/api/v1/customers", """
                {"identity":{"civilite":"M.","nom":"Orange","prenom":"Test","dateNaissance":"1990-01-01","pieceType":"CNI","pieceNumero":"AB1234567"}}""")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.score").value("orange"))
                .andExpect(jsonPath("$.customer.id").value(startsWith("CL-9")));
        send("/api/v1/customers", "{\"identity\":{\"nom\":\"\",\"pieceNumero\":\"X1\"}}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[*].field").value(containsInAnyOrder(
                        "identity.nom", "identity.prenom", "identity.dateNaissance", "identity.pieceType", "identity.pieceNumero")));
        send("/api/v1/customers/existence-checks", "{\"identity\":" + MARIE + "}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("CL-000101"));
        send("/api/v1/customers/existence-checks", "{\"identity\":" + MARIE.replace("Dupont", "Durand") + "}")
                .andExpect(status().isNoContent());
    }

    @Test
    void updateRescoresOnlyWhenNameChanges() throws Exception {
        mvc.perform(put("/api/v1/customers/CL-000101").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"identity\":" + MARIE.replace("\"email\":\"marie.dupont@exemple.fr\"", "\"email\":\"m@exemple.fr\"") + ",\"address\":" + ADDRESS + "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rescored").value(false))
                .andExpect(jsonPath("$.customer.identity.email").value("m@exemple.fr"));
        mvc.perform(put("/api/v1/customers/CL-000101").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"identity\":" + MARIE.replace("Dupont", "Rouge") + ",\"address\":" + ADDRESS + "}"))
                .andExpect(jsonPath("$.rescored").value(true))
                .andExpect(jsonPath("$.score").value("rouge"))
                .andExpect(jsonPath("$.customer.identity.nom").value("Dupont"));
    }

    @Test
    void acquisitionWithTerminalAndOpenMl() throws Exception {
        final var order = """
                {"flow":"acquisition","customerId":"CL-000101","holder":%s,"billingAddress":%s,
                 "number":{"mode":"nouveau","msisdn":"0690123481"},"offerId":"bl-60","options":["multisim"],
                 "openMlSponsor":"0690 65 43 21","sim":{"type":"physique","iccid":"8933 0100 0000 0000 019"},
                 "terminal":{"id":"tel-a15","imei":"490154203237518"},"sepaMandateRef":"%s"}"""
                .formatted(MARIE, ADDRESS, mandate());
        final var contractRef = contract(order);
        mvc.perform(get("/api/v1/contracts/" + contractRef))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/pdf"))
                .andExpect(content().string(startsWith("%PDF-1.4")));
        send("/api/v1/orders", order).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[*].field").value(containsInAnyOrder("contractRef", "contractSigned", "cgvAccepted", "privacyAccepted")));
        send("/api/v1/orders", confirmed(order, contractRef))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderNumber").value(startsWith("CMD-")))
                .andExpect(jsonPath("$.cashDue").value(149.0));
        // L'offre vendue est recopiée dans le message : bl-60 22,99 + Multi-SIM 3 − Open ML 10, puis −10 € pendant 6 mois.
        final var offer = published().offer();
        assertEquals("bl-60", offer.id());
        assertEquals(List.of("multisim"), offer.options().stream().map(Model.OfferOption::id).toList());
        assertEquals("promo-10e-6m", offer.promos().getFirst().id());
        assertEquals(new OrderService.OpenMlApplied("0690654321", 10), offer.openMl());
        assertEquals(15.99, offer.monthlyPrice());
        assertEquals(5.99, offer.monthlyPriceDuringPromo());
        mvc.perform(get("/api/v1/numbers/available")).andExpect(jsonPath("$").value(not(hasItem("0690123481"))));
        send("/api/v1/hardware/iccid-checks", "{\"iccid\":\"8933010000000000019\"}").andExpect(jsonPath("$.result").value("inconnu"));
    }

    @Test
    void catalogOffersCarryTheirOptionsAndPromos() throws Exception {
        mvc.perform(get("/api/v1/catalog/offers"))
                .andExpect(jsonPath("$[?(@.id == 'bl-60')].promos[0].monthlyDiscount").value(hasItem(10.0)))
                .andExpect(jsonPath("$[?(@.id == 'bl-120')].options[?(@.id == 'europe')].included").value(hasItem(true)))
                .andExpect(jsonPath("$[?(@.id == 'nb-200')].options[?(@.id == 'international')].price").value(hasItem(8.0)))
                .andExpect(jsonPath("$[?(@.id == 'pp-classique')].openMl.eligible").value(hasItem(false)));
        mvc.perform(get("/api/v1/catalog/options")).andExpect(status().isNotFound());
    }

    @Test
    void optionsMustBelongToTheOffer() throws Exception {
        send("/api/v1/contracts", """
                {"flow":"forfait","customerId":"CL-000101","lineMsisdn":"0690550123","offerId":"bl-60","options":["international"]}""")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].message").value("Option non proposée avec cette offre."));
    }

    @Test
    void includedOptionIsNeitherAddedNorRemoved() throws Exception {
        // Ligne pro en Forfait 200 Go : Europe incluse, Appels internationaux à 8 € déjà actifs.
        final var unchanged = """
                {"flow":"options","customerId":"CL-000201","lineMsisdn":"0690400500","options":["international","europe"],
                 "cgvAccepted":true,"privacyAccepted":true}""";
        send("/api/v1/orders", unchanged).andExpect(status().isBadRequest());
        send("/api/v1/orders", unchanged.replace("\"europe\"", "\"multisim\"")).andExpect(status().isCreated());
        final var change = published().optionsChange();
        assertEquals(List.of("multisim"), change.added().stream().map(Model.OfferOption::id).toList());
        assertTrue(change.removed().isEmpty());
        assertEquals(3.0, change.monthlyDelta());
        assertNull(published().offer());
    }

    @Test
    void acquisitionReportsEveryInvalidField() throws Exception {
        send("/api/v1/orders", """
                {"flow":"acquisition","customerId":"CL-000101","holder":{"nom":"Dupont"},
                 "number":{"mode":"portabilite","rio":"01X","portingDate":"2020-01-01"},
                 "terminal":{"id":"","imei":"1"},"sim":{"type":"physique","iccid":"1234"}}""")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[*].field").value(hasItem("holder.prenom")))
                .andExpect(jsonPath("$.errors[*].field").value(hasItem("billingAddress")))
                .andExpect(jsonPath("$.errors[*].field").value(hasItem("number.rio")))
                .andExpect(jsonPath("$.errors[*].field").value(hasItem("number.portingDate")))
                .andExpect(jsonPath("$.errors[*].field").value(hasItem("offerId")))
                .andExpect(jsonPath("$.errors[*].field").value(hasItem("sim.iccid")))
                .andExpect(jsonPath("$.errors[*].field").value(hasItem("terminal.imei")))
                .andExpect(jsonPath("$.errors[*].field").value(hasItem("sepaMandateRef")));
    }

    @Test
    void prepaidCardForOrangeScore() throws Exception {
        final var created = send("/api/v1/customers", """
                {"identity":{"civilite":"M.","nom":"Orange","prenom":"Test","dateNaissance":"1990-01-01","pieceType":"CNI","pieceNumero":"AB1234567"}}""")
                .andReturn().getResponse().getContentAsString();
        final var id = created.replaceAll(".*\"customer\":\\{\"id\":\"([^\"]+)\".*", "$1");
        final var holder = MARIE.replace("Dupont", "Orange");
        final var order = """
                {"flow":"acquisition","customerId":"%s","holder":%s,"billingAddress":%s,
                 "number":{"mode":"portabilite","rio":"01P123456ABC","portedMsisdn":"0690112233","operator":"FREE","portingDate":"%s"},
                 "offerId":"%s","sim":{"type":"physique","iccid":"8933010000000000027"}}""";
        send("/api/v1/orders", order.formatted(id, holder, ADDRESS, TODAY, "bl-60"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[?(@.field == 'offerId')].message").value(hasItem("Score orange : seule l'offre Carte Prépayée est vendable.")));
        final var prepaid = order.formatted(id, holder, ADDRESS, TODAY, "pp-classique");
        send("/api/v1/orders", confirmed(prepaid, contract(prepaid)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.cashDue").value(10.0));
    }

    @Test
    void businessRulesRefuseTheOrder() throws Exception {
        send("/api/v1/orders", "{\"flow\":\"acquisition\",\"customerId\":\"CL-000201\"}")
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.detail").value(startsWith("Client entreprise")));
        send("/api/v1/orders", "{\"flow\":\"sim\",\"customerId\":\"CL-000103\"}")
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.detail").value("Score Préventel rouge : aucune vente possible."));
        send("/api/v1/orders", "{\"flow\":\"forfait\",\"customerId\":\"CL-000101\",\"lineMsisdn\":\"0690550125\"}")
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.detail").value(startsWith("La ligne 0690550125 a une commande en cours")));
        mvc.perform(post("/api/v1/customers/CL-000102/lines/0696778899/rescoring")).andExpect(jsonPath("$.score").value("orange"));
        send("/api/v1/orders", "{\"flow\":\"forfait\",\"customerId\":\"CL-000102\",\"lineMsisdn\":\"0696778899\",\"offerId\":\"nb-10\"}")
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.detail").value(startsWith("Re-scoring Préventel orange")));
    }

    @Test
    void cardLineChangesPlanWithoutOpenMl() throws Exception {
        final var order = """
                {"flow":"forfait","customerId":"CL-000101","lineMsisdn":"0690550124","offerId":"nb-10","openMlSponsor":"0690654321"}""";
        send("/api/v1/contracts", order).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("openMlSponsor"));
        final var withoutOpenMl = order.replace(",\"openMlSponsor\":\"0690654321\"", "");
        send("/api/v1/orders", confirmed(withoutOpenMl, contract(withoutOpenMl))).andExpect(status().isCreated());
        mvc.perform(get("/api/v1/customers/CL-000101/pending-lines")).andExpect(jsonPath("$").value(hasItem("0690550124")));
    }

    @Test
    void optionsChangeNeedsNoContract() throws Exception {
        final var unchanged = """
                {"flow":"options","customerId":"CL-000101","lineMsisdn":"0690123456","options":["multisim","europe"],"cgvAccepted":true,"privacyAccepted":true}""";
        send("/api/v1/orders", unchanged).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].message").value("Ajoutez ou retirez au moins une option."));
        send("/api/v1/contracts", unchanged).andExpect(status().isUnprocessableContent());
        send("/api/v1/orders", unchanged.replace("\"multisim\",\"europe\"", "\"europe\",\"securite\""))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.cashDue").value(0.0));
    }

    @Test
    void lostSimFeeIsCollectedAtTheCounter() throws Exception {
        final var order = """
                {"flow":"sim","customerId":"CL-000101","lineMsisdn":"0690550123","simReason":"perte-vol",
                 "sim":{"type":"esim","iccid":"8933010000000000035"}}""";
        final var body = send("/api/v1/orders", confirmed(order, contract(order)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.cashDue").value(10.0))
                .andReturn().getResponse().getContentAsString();
        final var number = body.replaceAll(".*\"orderNumber\":\"([^\"]+)\".*", "$1");
        send("/api/v1/orders/" + number + "/cash-payments", "{\"amount\":5}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].message").value("Le montant encaissé doit être de 10,00 €."));
        send("/api/v1/orders/" + number + "/cash-payments", "{\"amount\":10}")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.reference").value(startsWith("PAY-")));
        // Une action sur une commande inconnue reste une erreur 404, pas un succès 204.
        send("/api/v1/orders/CMD-inconnue/cash-payments", "{\"amount\":10}").andExpect(status().isNotFound());
    }
}
