package com.binaryigor.main.definitions.app;

import com.binaryigor.main._common.app.HTMX;
import com.binaryigor.main.definitions.core.FoodDefinition;
import com.binaryigor.main.definitions.core.GetFoodDefinitionsHandler;
import com.binaryigor.main.definitions.core.GetFoodDefinitionsRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.UUID;

@Controller
@RequestMapping("/definitions")
public class DefinitionsController {

    private final GetFoodDefinitionsHandler getFoodDefinitionsHandler;

    public DefinitionsController(GetFoodDefinitionsHandler getFoodDefinitionsHandler) {
        this.getFoodDefinitionsHandler = getFoodDefinitionsHandler;
    }

    @GetMapping
    String getDefinitionsPage(Model model) {
        return HTMX.fragmentOrFullPage(model, "definitions/definitions");
    }

    @GetMapping("/food/{name}")
    String getFoodDefinition(@PathVariable String name,
                         Model model) {
        model.addAttribute("name", name);
        return HTMX.fragmentOrFullPage(model, "definitions/single-food");
    }

    @GetMapping("/food")
    String getFoodDefinitions(Model model) {
        //TODO: get current user!
        var definitions = getFoodDefinitionsHandler.handle(new GetFoodDefinitionsRequest(UUID.randomUUID()))
                .stream()
                .map(FoodDefinition::name)
                .toList();

        model.addAttribute("definitions", definitions);

        return HTMX.fragmentOrFullPage(model, "/definitions/food");
    }
}
