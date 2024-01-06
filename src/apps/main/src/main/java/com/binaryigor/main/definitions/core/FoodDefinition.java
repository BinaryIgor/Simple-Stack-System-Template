package com.binaryigor.main.definitions.core;

import java.util.UUID;

public record FoodDefinition(UUID id, UUID userId,
                             String name,
                             int kcal,
                             double protein) {
}
