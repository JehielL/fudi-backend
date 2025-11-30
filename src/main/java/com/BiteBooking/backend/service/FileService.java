package com.BiteBooking.backend.service;

import com.BiteBooking.backend.exception.FileException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class FileService {

    private static final List<String> ALLOWED_IMAGE_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png", "gif", "webp");

    public String store(MultipartFile file) {
        String newFileName = generateUniqueName(file);
        try {
            Path uploadPath = Paths.get("uploads");
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            Path filePath = uploadPath.resolve(newFileName);
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
            }
            return newFileName;
        } catch (IOException e) {
            log.error("Error al leer/guardar archivo", e);
            throw new FileException("Error al guardar archivo");
        }
    }

    public Resource load(String name) {
        Path file = Paths.get("uploads").resolve(name);
        try {
            Resource resource = new UrlResource(file.toUri());
            if (!resource.exists() || !resource.isReadable())
                throw new FileException("Error al intentar cargar el archivo");

            return resource;
        } catch (MalformedURLException e) {
            throw new FileException("Error al intentar cargar el archivo");
        }
    }

    public void delete(String fileName) {
        try {
            Path file = Paths.get("uploads").resolve(fileName);
            Files.deleteIfExists(file);
            log.info("Archivo eliminado: {}", fileName);
        } catch (IOException e) {
            log.error("Error al eliminar archivo: {}", fileName, e);
            throw new FileException("Error al eliminar archivo");
        }
    }

    public boolean isImage(MultipartFile file) {
        String originalFileName = file.getOriginalFilename();
        if (!StringUtils.hasLength(originalFileName)) {
            return false;
        }
        String extension = StringUtils.getFilenameExtension(originalFileName);
        return extension != null && ALLOWED_IMAGE_EXTENSIONS.contains(extension.toLowerCase());
    }

    private String generateUniqueName(MultipartFile file) {
        String originalFileName = file.getOriginalFilename();
        if (!StringUtils.hasLength(originalFileName) || file.isEmpty())
            throw new FileException("Error al leer archivo");

        String fileName = StringUtils.cleanPath(originalFileName);
        String extension = StringUtils.getFilenameExtension(fileName);
        String fileNameWithoutExt = fileName.replace("." + extension, "");
        return fileNameWithoutExt + "-" + UUID.randomUUID() + "." + extension;
    }

}
