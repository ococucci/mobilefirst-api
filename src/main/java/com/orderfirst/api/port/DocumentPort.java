package com.orderfirst.api.port;

import java.util.Optional;

/** Api-document : création et stockage du document (contrat). */
public interface DocumentPort {
    String store(String customerId, byte[] document);

    boolean exists(String documentId);

    Optional<byte[]> find(String documentId);
}
