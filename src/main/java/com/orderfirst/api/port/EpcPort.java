package com.orderfirst.api.port;

import java.util.List;

/** api EPC : génération du contrat (PDF). */
public interface EpcPort {
    byte[] generateContract(String title, List<String> lines);
}
