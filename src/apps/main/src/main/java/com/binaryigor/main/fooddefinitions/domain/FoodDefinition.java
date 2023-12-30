package com.binaryigor.main.fooddefinitions.domain;

import java.util.UUID;

public record FoodDefinition(UUID id, UUID userId,
                             String name,
                             int kcal,
                             double protein) {
}
