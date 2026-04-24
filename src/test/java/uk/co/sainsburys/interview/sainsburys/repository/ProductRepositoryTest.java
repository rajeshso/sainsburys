package uk.co.sainsburys.interview.sainsburys.repository;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import uk.co.sainsburys.interview.sainsburys.model.Product;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

//with H2 in-memory database, we can test the repository layer without needing a real database.
@DataJpaTest
public class ProductRepositoryTest {

    //Test: save a product, find by product_uid, assert fields match
    @Test
    public void testSaveProduct(){
        //Given
        Product product = new Product();
        product.setProduct_uid(6447344);
        product.setName("Test Product");
        product.setProduct_type("Test Type");
        product.setFull_url("http://example.com/product/123");

        //When
        Product savedProduct = productRepository.save(product);
        Optional<Product> foundProductOpt = productRepository.findByProduct_uid(6447344);

        //Then
        assertTrue(foundProductOpt.isPresent());
        Product foundProduct = foundProductOpt.get();
        assertEquals(savedProduct.getProduct_uid(), foundProduct.getProduct_uid());
        assertEquals(savedProduct.getName(), foundProduct.getName());
        assertEquals(savedProduct.getProduct_type(), foundProduct.getProduct_type());
        assertEquals(savedProduct.getFull_url(), foundProduct.getFull_url());
    }

    //Test: save same product_uid twice, assert no duplicate
    @Test
    public void testDuplicateProduct_uid(){
        //Given
        Product product1 = new Product();
        product1.setProduct_uid(6447344);
        product1.setName("Test Product 1");
        product1.setProduct_type("Test Type");
        product1.setFull_url("http://example.com/product/123");

        Product product2 = new Product();
        product2.setProduct_uid(6447344);
        product2.setName("Test Product 2");
        product2.setProduct_type("Test Type");
        product2.setFull_url("http://example.com/product/123");

        //When
        productRepository.save(product1);
        Product savedProduct2 = productRepository.save(product2);
        Optional<Product> foundProductOpt = productRepository.findByProduct_uid(6447344);

        //Then
        assertTrue(foundProductOpt.isPresent());
        Product foundProduct = foundProductOpt.get();
        assertEquals(foundProduct.getName(), "Test Product 2");
        assertEquals(foundProduct.getProduct_uid(), savedProduct2.getProduct_uid());
    }
}
