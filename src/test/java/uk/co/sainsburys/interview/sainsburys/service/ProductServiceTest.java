package uk.co.sainsburys.interview.sainsburys.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.sainsburys.interview.sainsburys.client.ProductApiClient;
import uk.co.sainsburys.interview.sainsburys.model.ProductPriceResponse;
import uk.co.sainsburys.interview.sainsburys.model.ProductResponse;
import uk.co.sainsburys.interview.sainsburys.repository.ProductRepository;

import java.net.MalformedURLException;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {

    @Mock
    private ProductApiClient productApiClient;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    @Test
    void ingest_newProduct_savesWithMatchedPrice() throws MalformedURLException {
        when(productApiClient.fetchProducts()).thenReturn(List.of(
                new ProductResponse("uid-1", "BASIC", "Salmon", "http://example.com", null)
        ));
        when(productApiClient.fetchPrices()).thenReturn(List.of(
                new ProductPriceResponse("uid-1", 15.63, "kg", 1)
        ));

        productService.ingestProducts();

        verify(productRepository).saveAll(argThat(list ->
                list.iterator().next().getProductUid().equals("uid-1") &&
                list.iterator().next().getUnitPrice() == 15.63
        ));
    }

    @Test
    void ingest_productWithNoPrice_persistsWithNullUnitPrice() throws MalformedURLException {
        when(productApiClient.fetchProducts()).thenReturn(List.of(
                new ProductResponse("uid-2", "BASIC", "No Price Item", "http://example.com", null)
        ));
        when(productApiClient.fetchPrices()).thenReturn(List.of());

        productService.ingestProducts();

        verify(productRepository).saveAll(argThat(list ->
                list.iterator().next().getUnitPrice() == null
        ));
    }

    @Test
    void ingest_duplicatePriceEntries_lastWriteWins() throws MalformedURLException {
        when(productApiClient.fetchProducts()).thenReturn(List.of(
                new ProductResponse("uid-3", "BASIC", "Salmon", "http://example.com", null)
        ));
        when(productApiClient.fetchPrices()).thenReturn(List.of(
                new ProductPriceResponse("uid-3", 10.00, "kg", 1),
                new ProductPriceResponse("uid-3", 20.00, "kg", 1)
        ));

        productService.ingestProducts();

        verify(productRepository).saveAll(argThat(list ->
                list.iterator().next().getUnitPrice() == 20.00
        ));
    }

    @Test
    void ingest_calledTwice_saveAllCalledTwice() throws MalformedURLException {
        when(productApiClient.fetchProducts()).thenReturn(List.of(
                new ProductResponse("uid-1", "BASIC", "Salmon", "http://example.com", null)
        ));
        when(productApiClient.fetchPrices()).thenReturn(List.of(
                new ProductPriceResponse("uid-1", 15.63, "kg", 1)
        ));

        productService.ingestProducts();
        productService.ingestProducts();

        // idempotency is guaranteed by DB upsert — service always calls saveAll with same data
        verify(productRepository, times(2)).saveAll(anyList());
    }
}
