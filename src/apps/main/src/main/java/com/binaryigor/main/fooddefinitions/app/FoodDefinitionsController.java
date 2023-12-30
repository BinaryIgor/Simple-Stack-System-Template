package com.binaryigor.main.fooddefinitions.app;

import com.binaryigor.main._commons.app.WebViews;
import com.binaryigor.main.fooddefinitions.core.FoodDefinition;
import com.binaryigor.main.fooddefinitions.core.GetFoodDefinitionsHandler;
import com.binaryigor.main.fooddefinitions.core.GetFoodDefinitionsRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.UUID;

@Controller
@RequestMapping("/food-definitions")
public class FoodDefinitionsController {

    private final GetFoodDefinitionsHandler getFoodDefinitionsHandler;

    public FoodDefinitionsController(GetFoodDefinitionsHandler getFoodDefinitionsHandler) {
        this.getFoodDefinitionsHandler = getFoodDefinitionsHandler;
    }

    @GetMapping("/{name}")
    String getDefinition(@PathVariable String name,
                         Model model) {
        model.addAttribute("name", name);
        return WebViews.fragmentOrFullPage(model, "food-definition");
    }

    @GetMapping
    String getDefinitions(Model model) {
        //TODO: get current user!
        var definitions = getFoodDefinitionsHandler.handle(new GetFoodDefinitionsRequest(UUID.randomUUID()))
                .stream()
                .map(FoodDefinition::name)
                .toList();

        model.addAttribute("definitions", definitions);

        return WebViews.fragmentOrFullPage(model, "food-definitions");
    }
}
