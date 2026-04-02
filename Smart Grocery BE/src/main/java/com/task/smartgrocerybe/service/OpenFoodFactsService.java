package com.task.smartgrocerybe.service;

import com.task.smartgrocerybe.dto.external.OpenFoodFactsResponse;
import com.task.smartgrocerybe.exception.ResourceNotFoundException;
import com.task.smartgrocerybe.feign.OpenFoodFactsClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OpenFoodFactsService {

    private final OpenFoodFactsClient openFoodFactsClient;

    public OpenFoodFactsResponse fetchByBarcode(String barcode) {
        try {
            OpenFoodFactsResponse response =
                    openFoodFactsClient.getProductByBarcode(barcode);

            if (response == null
                    || response.getStatus() == null
                    || response.getStatus() == 0
                    || response.getProduct() == null) {
                throw new ResourceNotFoundException(
                        "No product found for barcode: " + barcode);
            }

            return response;

        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Open Food Facts API error for barcode {}: {}",
                    barcode, e.getMessage());
            throw new ResourceNotFoundException(
                    "Failed to fetch product for barcode: " + barcode);
        }
    }
}