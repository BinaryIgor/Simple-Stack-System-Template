package com.binaryigor.main._commons;

import org.springframework.ui.Model;

public class WebViews {

    private static final String FULL_PAGE = "index";

    public static String fragmentOrFullPage(Model model, String templateName) {
        if (true) {
            //TODO: get from config!
            model.addAttribute("stylesPath", "/live-styles.css");
            model.addAttribute("template", templateName);
            return FULL_PAGE;
        }
        return templateName;
    }
}
