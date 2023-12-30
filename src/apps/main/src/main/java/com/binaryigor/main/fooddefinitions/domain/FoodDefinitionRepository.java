package com.binaryigor.main.fooddefinitions.domain;

import java.util.List;
import java.util.UUID;

public interface FoodDefinitionRepository {

    List<FoodDefinition> allDefinitionsOfUser(UUID userId);
}
