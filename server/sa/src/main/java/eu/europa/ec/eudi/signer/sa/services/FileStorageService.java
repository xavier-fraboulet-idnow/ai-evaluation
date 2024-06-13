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

package eu.europa.ec.eudi.signer.sa.services;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.apache.tika.detect.DefaultDetector;
import org.apache.tika.detect.Detector;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import eu.europa.ec.eudi.signer.sa.config.FileStorageConfig;
import eu.europa.ec.eudi.signer.sa.error.FileNotFoundException;
import eu.europa.ec.eudi.signer.sa.error.FileStorageException;

@Service
public class FileStorageService {

    private final Path fileStorageLocation;

    @Autowired
    public FileStorageService(FileStorageConfig fileStorageProperties) {
        this.fileStorageLocation = Paths.get(fileStorageProperties.getUploadDir()).toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new FileStorageException("Could not create the directory where the uploaded files will be stored.",
                    ex);
        }
    }

    /**
     * Stores the file in teh local file system and returns an absolute path to it
     *
     * @param file
     * @return
     */
    public String storeFile(MultipartFile file) {
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());

        try {
            // Check if the file's name contains invalid characters
            if (fileName.contains("..")) {
                throw new FileStorageException("Sorry! Filename contains invalid path sequence " + fileName);
            }

            InputStream inputStream = file.getInputStream();
            Detector detector = new DefaultDetector();

            Metadata metadata = new Metadata();

            String tipoArquivo = detector.detect(TikaInputStream.get(inputStream), metadata).toString();

            if (!tipoArquivo.equals("application/pdf")) {
                throw new FileStorageException("Sorry! This file is not a PDF -> " + fileName);
            }

            // Copy file to the target location (Replacing existing file with the same name)
            Path targetLocation = newFilePath(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            return fileName;
        } catch (IOException ex) {
            throw new FileStorageException("Could not store file " + fileName + ". Please try again!", ex);
        }
    }

    public Resource loadFileAsResource(String fileName) {
        try {
            Path filePath = getFilePath(fileName);
            return new UrlResource(filePath.toUri());
        } catch (MalformedURLException ex) {
            throw new FileNotFoundException("File not found " + fileName, ex);
        }
    }

    public Path getFilePath(String fileName) {
        final Path path = newFilePath(fileName);
        if (!Files.exists(path)) {
            throw new FileNotFoundException("File not found " + fileName);
        }
        return path;
    }

    public Path newFilePath(String fileName) {
        return fileStorageLocation.resolve(fileName).normalize();
    }
}
