package com.binaryigor.main.definitions.core;

import java.util.List;

public class GetFoodDefinitionsHandler {

    private final FoodDefinitionRepository foodDefinitionRepository;

    public GetFoodDefinitionsHandler(FoodDefinitionRepository foodDefinitionRepository) {
        this.foodDefinitionRepository = foodDefinitionRepository;
    }

    //TODO: more complicated than that!
    public List<FoodDefinition> handle(GetFoodDefinitionsRequest request) {
        return foodDefinitionRepository.allDefinitionsOfUser(request.userId());
    }
}
