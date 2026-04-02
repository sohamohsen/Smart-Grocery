package com.task.smartgrocerybe.feign;

import com.task.smartgrocerybe.exception.ResourceNotFoundException;
import feign.Response;
import feign.codec.ErrorDecoder;
import org.springframework.stereotype.Component;

@Component
public class FeignErrorDecoder implements ErrorDecoder {

    @Override
    public Exception decode(String methodKey, Response response) {
        return switch (response.status()) {
            case 404 -> new ResourceNotFoundException(
                    "Product not found in Open Food Facts");
            case 400 -> new IllegalArgumentException(
                    "Invalid barcode format");
            default -> new RuntimeException(
                    "Open Food Facts API error: " + response.status());
        };
    }
}