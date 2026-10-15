package com.gymapp.backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

// By extending our new base class, this test will now trigger Docker!
@SpringBootTest
class BackendApplicationTests extends AbstractIntegrationTest {

	@Test
	void contextLoads() {
		// If the application context loads without crashing,
		// it means Testcontainers successfully spun up PostgreSQL,
		// and Flyway successfully built your tables!
	}
}