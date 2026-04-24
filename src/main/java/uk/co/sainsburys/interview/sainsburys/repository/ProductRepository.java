package uk.co.sainsburys.interview.sainsburys.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.co.sainsburys.interview.sainsburys.model.Product;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Integer> {
    Optional<Product> findByProduct_uid(Integer product_uid);
}
