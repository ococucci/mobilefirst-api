package com.orderfirst.api.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDate;
import java.time.ZoneId;

import static org.hamcrest.Matchers.containsInAnyOrder;
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

    @Autowired
    private MockMvc mvc;

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
        mvc.perform(get("/api/v1/customers").param("msisdn", "0690000001")).andExpect(status().isNotFound());
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
                .andExpect(jsonPath("$.existing.id").value("CL-000101"));
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
        mvc.perform(get("/api/v1/numbers/available")).andExpect(jsonPath("$").value(not(hasItem("0690123481"))));
        send("/api/v1/hardware/iccid-checks", "{\"iccid\":\"8933010000000000019\"}").andExpect(jsonPath("$.result").value("inconnu"));
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
    }
}
