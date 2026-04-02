package com.task.smartgrocerybe.dto.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenFoodFactsResponse {

    @JsonProperty("status")
    private Integer status;

    @JsonProperty("product")
    private OpenFoodFactsProduct product;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class OpenFoodFactsProduct {

        @JsonProperty("product_name")
        private String productName;

        @JsonProperty("brands")
        private String brands;

        @JsonProperty("image_url")
        private String imageUrl;

        @JsonProperty("categories_tags")
        private List<String> categoryTags;

        @JsonProperty("labels_tags")
        private List<String> labelsTags;
    }
}
