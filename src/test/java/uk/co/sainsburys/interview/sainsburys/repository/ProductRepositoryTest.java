package uk.co.sainsburys.interview.sainsburys.repository;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import uk.co.sainsburys.interview.sainsburys.model.Product;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

//with H2 in-memory database, we can test the repository layer without needing a real database.
@DataJpaTest
public class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private EntityManager entityManager;

    //Test: save a product, find by product_uid, assert fields match
    @Test
    public void testSaveProduct(){
        //Given
        Product product = new Product();
        product.setProductUid("6447344");
        product.setName("Test Product");
        product.setProductType("Test Type");
        product.setFullUrl("https://example.com/product/123");

        //When
        Product savedProduct = productRepository.save(product);
        Optional<Product> foundProductOpt = productRepository.findByProductUid("6447344");

        //Then
        assertTrue(foundProductOpt.isPresent());
        Product foundProduct = foundProductOpt.get();
        assertEquals(savedProduct.getProductUid(), foundProduct.getProductUid());
        assertEquals(savedProduct.getName(), foundProduct.getName());
        assertEquals(savedProduct.getProductType(), foundProduct.getProductType());
        assertEquals(savedProduct.getFullUrl(), foundProduct.getFullUrl());
    }

    //Test: save same product_uid twice, assert no duplicate
    @Test
    public void testDuplicateProduct_uid(){
        //Given
        Product product1 = new Product();
        product1.setProductUid("6447344");
        product1.setName("Test Product 1");
        product1.setProductType("Test Type");
        product1.setFullUrl("https://example.com/product/123");
        Product savedProduct1 = productRepository.save(product1);

        Product product2 = new Product();
        product2.setProductUid("6447344");
        product2.setName("Test Product 2");
        product2.setProductType("Test Type");
        product2.setFullUrl("http://example.com/product/123");

        //When //Then
        assertThrows(DataIntegrityViolationException.class, () -> {
            productRepository.saveAndFlush(product2);
        });
    }

    @Test
    public void testUpsert(){
        //Given
        Product product = new Product();
        product.setProductUid("6447344");
        product.setName("Test Product");
        product.setProductType("Test Type");
        product.setFullUrl("https://example.com/product/123");
        Product savedProduct = productRepository.save(product);

        //When
        savedProduct.setName("Updated Test Product");
        Product updatedProduct = productRepository.save(savedProduct);

        //Then
        assertEquals("Updated Test Product", updatedProduct.getName());
        entityManager.flush(); //force the changes to be written to the database
        Optional<Product> foundProductOpt = productRepository.findByProductUid("6447344");
        assertTrue(foundProductOpt.isPresent());
        assertEquals("Updated Test Product", foundProductOpt.get().getName());
    }
}
