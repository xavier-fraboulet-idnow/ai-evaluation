/*
 Copyright 2024 European Commission

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

      https://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

package eu.assina.sa;

import eu.assina.sa.config.FileStorageConfig;
import eu.assina.sa.config.RSSPClientConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.context.ApplicationPidFileWriter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/** Main Spring Boot application class for Assina application */
@EnableConfigurationProperties({FileStorageConfig.class, RSSPClientConfig.class})
// disable security on the Signing App - all security is on the RSSP
@SpringBootApplication(scanBasePackages = "eu.assina.sa", exclude = SecurityAutoConfiguration.class)
public class AssinaSigningApplication {

	public static void main(String[] args) {
		//		https://stackoverflow.com/questions/26547532/how-to-shutdown-a-spring-boot-application-in-a-correct-way
		SpringApplication application = new SpringApplication(AssinaSigningApplication.class);
		// write a PID to allow for shutdown
		application.addListeners(new ApplicationPidFileWriter("./sa.pid"));
		application.run(args);
	}
}
