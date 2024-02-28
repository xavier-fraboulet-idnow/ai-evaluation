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

package eu.assina.rssp;

import eu.assina.rssp.common.config.AppProperties;
import eu.assina.rssp.common.config.CSCProperties;
import eu.assina.rssp.common.config.DemoProperties;
import java.io.File;
import java.util.Date;
import eu.assina.rssp.common.config.VerifierProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.ApplicationPidFileWriter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/** Main Spring Boot application class for Assina application */
@SpringBootApplication(scanBasePackages = "eu.assina.rssp")
@EnableConfigurationProperties({AppProperties.class, CSCProperties.class, DemoProperties.class, VerifierProperties.class})
public class AssinaRSSPApplication {
    
    private final static threadConfig threadConfig = new threadConfig();
	public static void main(String[] args) {
//		https://stackoverflow.com/questions/26547532/how-to-shutdown-a-spring-boot-application-in-a-correct-way
		SpringApplication application = new SpringApplication(AssinaRSSPApplication.class);
		// write a PID to allow for shutdown
		application.addListeners(new ApplicationPidFileWriter("./rssp.pid"));
        String diretorio = threadConfig.diretorio;
        VerificadorPDFs verificador = new VerificadorPDFs(diretorio);
        verificador.start();
		application.run(args);
	}

	private static class VerificadorPDFs extends Thread {
        private String diretorio;


        public VerificadorPDFs(String diretorio) {
            this.diretorio = diretorio;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    
                    int timeSleepThread = threadConfig.timeSleepThread;
                    Thread.sleep(timeSleepThread * 1000);
                    
                    // Verificar os arquivos no diretório
                    File[] arquivos = new File(diretorio).listFiles();
                    if (arquivos != null) {
                        for (File arquivo : arquivos) {
                            if (arquivo.isFile() && arquivo.getName().toLowerCase().endsWith(".pdf")) {
                                long diff = new Date().getTime() - arquivo.lastModified();
                                // Verificar se o arquivo tem mais de 1 minuto de idade
								
                                int fileAgeDelete = threadConfig.fileAgeDelete;
                                if (diff > fileAgeDelete * 1000) {
                                    System.out.println("Removed: " + arquivo.getName());
                                    arquivo.delete();
                                }
                            }
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
