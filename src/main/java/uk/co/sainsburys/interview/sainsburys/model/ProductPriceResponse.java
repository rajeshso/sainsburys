package uk.co.sainsburys.interview.sainsburys.model;

public record ProductPriceResponse(int product_uid, double unit_price, String unit_price_measure, int unit_price_measure_amount) {
}
