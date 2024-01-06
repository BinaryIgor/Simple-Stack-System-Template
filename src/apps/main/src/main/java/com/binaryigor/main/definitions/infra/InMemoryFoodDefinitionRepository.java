package com.binaryigor.main.definitions.infra;

import com.binaryigor.main.definitions.core.FoodDefinition;
import com.binaryigor.main.definitions.core.FoodDefinitionRepository;

import java.util.List;
import java.util.UUID;

public class InMemoryFoodDefinitionRepository implements FoodDefinitionRepository {

    @Override
    public List<FoodDefinition> allDefinitionsOfUser(UUID userId) {
        return List.of(
                new FoodDefinition(UUID.randomUUID(),
                        userId,
                        "Food 1", 100, 0),
                new FoodDefinition(UUID.randomUUID(),
                        userId,
                        "Food 2", 200, 20),
                new FoodDefinition(UUID.randomUUID(),
                        userId,
                        "Food 3", 150, 10)
        );
    }
}
