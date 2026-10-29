package com.orderfirst.api.stub;

import com.orderfirst.api.port.DocumentPort;
import com.orderfirst.api.port.EpcPort;
import com.orderfirst.api.port.OrasPort;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/** Bouchons documentaires et adresse : api EPC, Api-document et Api-ORAS. */
final class DocumentStubs {
    private DocumentStubs() {
    }

    /** Génère un PDF texte d'une page (même rendu que le bouchon du front). */
    @Stub
    static class Epc implements EpcPort {
        @Override
        public byte[] generateContract(final String title, final List<String> lines) {
            final var body = new StringBuilder("BT /F1 16 Tf 50 790 Td\n(").append(clean(title)).append(") Tj\n/F1 10 Tf 0 -28 Td 14 TL\n");
            lines.forEach(l -> body.append('(').append(clean(l)).append(") '\n"));
            body.append("ET");
            final var objects = List.of(
                    "<< /Type /Catalog /Pages 2 0 R >>",
                    "<< /Type /Pages /Kids [3 0 R] /Count 1 >>",
                    "<< /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] /Contents 4 0 R /Resources << /Font << /F1 5 0 R >> >> >>",
                    "<< /Length " + body.length() + " >>\nstream\n" + body + "\nendstream",
                    "<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica /Encoding /WinAnsiEncoding >>");
            final var pdf = new StringBuilder("%PDF-1.4\n");
            final var offsets = new int[objects.size()];
            for (int i = 0; i < objects.size(); i++) {
                offsets[i] = pdf.length();
                pdf.append(i + 1).append(" 0 obj\n").append(objects.get(i)).append("\nendobj\n");
            }
            final int xref = pdf.length();
            pdf.append("xref\n0 ").append(objects.size() + 1).append("\n0000000000 65535 f \n");
            Arrays.stream(offsets).forEach(o -> pdf.append("%010d 00000 n \n".formatted(o)));
            pdf.append("trailer\n<< /Size ").append(objects.size() + 1).append(" /Root 1 0 R >>\nstartxref\n").append(xref).append("\n%%EOF");
            // Un caractère = un octet (Latin-1), pour que les positions de la table xref restent justes.
            return pdf.toString().getBytes(StandardCharsets.ISO_8859_1);
        }

        private static String clean(final String s) {
            return s.replace('’', '\'').replace('•', '-').replace('·', '-').replace('−', '-')
                    .replaceAll("[^\\x20-\\xff]", "")
                    .replaceAll("([\\\\()])", "\\\\$1");
        }
    }

    @Stub
    static class Documents implements DocumentPort {
        private final Map<String, byte[]> documents = new ConcurrentHashMap<>();
        private final AtomicInteger sequence = new AtomicInteger(1);

        @Override
        public String store(final String customerId, final byte[] document) {
            final var id = "DOC-%s-%d".formatted(customerId, sequence.getAndIncrement());
            documents.put(id, document);
            return id;
        }

        @Override
        public boolean exists(final String documentId) {
            return documentId != null && documents.containsKey(documentId);
        }

        @Override
        public Optional<byte[]> find(final String documentId) {
            return Optional.ofNullable(documentId == null ? null : documents.get(documentId));
        }
    }

    @Stub
    static class Oras implements OrasPort {
        @Override
        public List<AddressSuggestion> search(final String query) {
            final var words = Arrays.stream((query == null ? "" : query).toLowerCase(Locale.ROOT).split("[\\s,]+"))
                    .filter(w -> w.length() > 1).toList();
            if (words.isEmpty()) {
                return List.of();
            }
            return StubData.ADDRESSES.stream()
                    .filter(a -> {
                        final var text = (a.ligne() + " " + a.codePostal() + " " + a.commune()).toLowerCase(Locale.ROOT);
                        return words.stream().allMatch(text::contains);
                    })
                    .limit(5)
                    .toList();
        }
    }
}
