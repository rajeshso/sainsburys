package uk.co.sainsburys.interview.sainsburys.model;

public record ProductResponse(
    int product_uid,
    String product_type,
    String name,
    String full_url
) {
}
