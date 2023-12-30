package com.binaryigor.main.fooddefinitions.infra;

import com.binaryigor.main.fooddefinitions.domain.FoodDefinition;
import com.binaryigor.main.fooddefinitions.domain.FoodDefinitionRepository;

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
