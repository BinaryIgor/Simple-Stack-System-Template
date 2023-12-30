package com.binaryigor.main.fooddefinitions;

import com.binaryigor.main.fooddefinitions.domain.FoodDefinitionRepository;
import com.binaryigor.main.fooddefinitions.domain.GetFoodDefinitionsHandler;
import com.binaryigor.main.fooddefinitions.infra.InMemoryFoodDefinitionRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FoodDefinitionsConfig {

    @Bean
    FoodDefinitionRepository foodDefinitionRepository() {
        return new InMemoryFoodDefinitionRepository();
    }

    @Bean
    GetFoodDefinitionsHandler getFoodDefinitionsHandler(FoodDefinitionRepository foodDefinitionRepository) {
        return new GetFoodDefinitionsHandler(foodDefinitionRepository);
    }
}
