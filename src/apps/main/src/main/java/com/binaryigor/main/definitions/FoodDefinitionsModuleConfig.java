package com.binaryigor.main.definitions;

import com.binaryigor.main.definitions.core.FoodDefinitionRepository;
import com.binaryigor.main.definitions.core.GetFoodDefinitionsHandler;
import com.binaryigor.main.definitions.infra.InMemoryFoodDefinitionRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FoodDefinitionsModuleConfig {

    @Bean
    FoodDefinitionRepository foodDefinitionRepository() {
        return new InMemoryFoodDefinitionRepository();
    }

    @Bean
    GetFoodDefinitionsHandler getFoodDefinitionsHandler(FoodDefinitionRepository foodDefinitionRepository) {
        return new GetFoodDefinitionsHandler(foodDefinitionRepository);
    }
}
