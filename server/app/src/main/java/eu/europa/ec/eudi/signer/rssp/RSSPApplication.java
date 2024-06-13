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

package eu.europa.ec.eudi.signer.rssp;

import eu.europa.ec.eudi.signer.rssp.common.config.AppProperties;
import eu.europa.ec.eudi.signer.rssp.common.config.CSCProperties;
import eu.europa.ec.eudi.signer.rssp.common.config.TrustedIssuersCertificatesProperties;

import java.io.File;
import java.util.Date;
import eu.europa.ec.eudi.signer.rssp.common.config.VerifierProperties;
import eu.europa.ec.eudi.signer.rssp.ejbca.EJBCAProperties;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.ApplicationPidFileWriter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/** Main Spring Boot application class for Signer application */
@SpringBootApplication(scanBasePackages = "eu.europa.ec.eudi.signer.rssp")
@EnableConfigurationProperties({ AppProperties.class, CSCProperties.class, VerifierProperties.class,
        EJBCAProperties.class, TrustedIssuersCertificatesProperties.class })
public class RSSPApplication {

    // private final static threadConfig threadConfig = new threadConfig();

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(RSSPApplication.class);
        application.addListeners(new ApplicationPidFileWriter("./rssp.pid"));

        String diretorio = threadConfig.diretorio;
        VerificadorPDFs verificador = new VerificadorPDFs(diretorio);
        verificador.start();

        try {
            application.run(args);
        } catch (Exception e) {
            System.err.println("RSSP Application Failed to Start.");
            System.exit(1);
        }
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