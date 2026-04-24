package uk.co.sainsburys.interview.sainsburys.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import uk.co.sainsburys.interview.sainsburys.service.ProductService;

import org.mockito.Mockito;

import uk.co.sainsburys.interview.sainsburys.exception.DatabaseException;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    @Test
    void ingest_shouldReturn202Accepted() throws Exception {
        mockMvc.perform(post("/products/ingest"))
                .andExpect(status().isAccepted());
    }

    @Test
    void ingest_whenDatabaseUnavailable_shouldReturn503WithMessage() throws Exception {
        Mockito.doThrow(new DatabaseException("Database unavailable"))
                .when(productService).ingestProducts();

        mockMvc.perform(post("/products/ingest"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(content().string("Database unavailable"));
    }
}
