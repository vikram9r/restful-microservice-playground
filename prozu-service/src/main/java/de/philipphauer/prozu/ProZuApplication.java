package de.philipphauer.prozu;

import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hubspot.dropwizard.guice.GuiceBundle;

import de.philipphauer.prozu.healthchecks.TemplateHealthCheck;
import de.philipphauer.prozu.repo.mongodb.MongoDBConfig;
import de.philipphauer.prozu.rest.EmployeeResource;
import de.philipphauer.prozu.util.ser.DummyDataInitializer;

public class ProZuApplication extends Application<ProZuConfiguration> {

	private GuiceBundle<ProZuConfiguration> guiceBundle;
	private ProZuModule module;

	public static void main(String[] args) throws Exception {
		new ProZuApplication().run(args);
	}

	@Override
	public String getName() {
		return "prozu";
	}

	@Override
	public void initialize(Bootstrap<ProZuConfiguration> bootstrap) {
		ObjectMapper objectMapper = bootstrap.getObjectMapper();
		MongoDBConfig config = new MongoDBConfig("test", "employees");
		module = new ProZuModule(objectMapper, config);

		guiceBundle = GuiceBundle.<ProZuConfiguration> newBuilder()
				.addModule(module)
				.setConfigClass(ProZuConfiguration.class)
				// .enableAutoConfig("de.itemis.prozu.repo.inmemory")
				.enableAutoConfig(getClass().getPackage().getName())
				.build();
		bootstrap.addBundle(guiceBundle);

	}

	@Override
	public void run(ProZuConfiguration configuration, Environment environment) {
		TemplateHealthCheck healthCheck = new TemplateHealthCheck(configuration.getTemplate());
		environment.healthChecks().register("template", healthCheck);
		environment.jersey().register(EmployeeResource.class);

		//TODO close connection to mongodb
		DummyDataInitializer dummyInitializer = guiceBundle.getInjector().getInstance(DummyDataInitializer.class);
		dummyInitializer.initDummyData();
	}

}