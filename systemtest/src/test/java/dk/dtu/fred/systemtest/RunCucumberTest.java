package dk.dtu.fred.systemtest;

import org.junit.runner.RunWith;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;

@RunWith(Cucumber.class)
@CucumberOptions(features = {"src/test/resources/cucumber-features/"}, format = {"pretty", "html:target/cucumber", "json:target/cucumber.json"})
public class RunCucumberTest {

}
