package com.binaryigor.main.fooddefinitions.core;

import java.util.List;
import java.util.UUID;

public interface FoodDefinitionRepository {

    List<FoodDefinition> allDefinitionsOfUser(UUID userId);
}
