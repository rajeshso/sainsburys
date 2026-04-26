package uk.co.sainsburys.interview.sainsburys.model;

public record ProductResponse(
        String product_uid,
    String product_type,
    String name,
    String fullUrl,
        Double unitPrice
) {
}
