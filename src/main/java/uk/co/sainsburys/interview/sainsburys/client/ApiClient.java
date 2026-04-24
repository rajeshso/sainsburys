package uk.co.sainsburys.interview.sainsburys.client;

import uk.co.sainsburys.interview.sainsburys.model.ProductPriceResponse;
import uk.co.sainsburys.interview.sainsburys.model.ProductResponse;

import java.net.MalformedURLException;
import java.util.List;

public interface ApiClient {
    List<ProductResponse> fetchProducts() throws MalformedURLException;;

    List<ProductPriceResponse>  fetchPrices() throws MalformedURLException;
}
