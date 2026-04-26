package uk.co.sainsburys.interview.sainsburys.client;

import org.springframework.stereotype.Component;
import uk.co.sainsburys.interview.sainsburys.model.ProductPriceResponse;
import uk.co.sainsburys.interview.sainsburys.model.ProductResponse;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.http.HttpClient;
import java.util.List;

@Component
public class ProductApiClient implements ApiClient {

    HttpClient client;
    ProductApiClient () throws MalformedURLException {
        URL productUrl = new URL("https://s3.eu-west-1.amazonaws.com/hackajob-assets1.p.hackajob/challenges/sainsbury_products/products_v2.json");
        URL productPriceUrl = new URL("https://s3.eu-west-1.amazonaws.com/hackajob-assets1.p.hackajob/challenges/sainsbury_products/products_price_v2.json");
        client = HttpClient.newBuilder().build();
    }

    public List<ProductResponse> fetchProducts() throws MalformedURLException {
        return List.of();
    }

    public List<ProductPriceResponse>  fetchPrices() throws MalformedURLException {
        return List.of();
    }
}
