package com.bancoOccidente.runners;

import com.bancoOccidente.util.FeatureOverright;
import io.cucumber.junit.CucumberOptions;
import net.serenitybdd.cucumber.CucumberWithSerenity;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.junit.Before;
import org.junit.jupiter.api.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.RunWith;

import java.io.IOException;


public class Runner {

    @Test
    public void test(){
        JUnitCore.runClasses(TestRunner.class);
    }

    @RunWith(CucumberWithSerenity.class)
    @CucumberOptions(features="src/test/resources/features/",
            glue = "com.bancoOccidente",
            tags = "@requestTravel",
            snippets = CucumberOptions.SnippetType.CAMELCASE
    )
    public class TestRunner {

    }
    @Before
    public void antes() throws IOException, InvalidFormatException {
        FeatureOverright.overrideFeatureFiles("src/test/resources/features");
    }
}