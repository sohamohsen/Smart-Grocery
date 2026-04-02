package com.task.smartgrocerybe.feign;

import com.task.smartgrocerybe.dto.external.OpenFoodFactsResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = "open-food-facts",
        url = "${service.open.base-url}"
)
public interface OpenFoodFactsClient {

    @GetMapping("/api/v0/product/{barcode}.json")
    OpenFoodFactsResponse getProductByBarcode(@PathVariable("barcode") String barcode);
}