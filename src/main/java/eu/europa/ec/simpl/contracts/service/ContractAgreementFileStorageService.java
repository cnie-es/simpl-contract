package eu.europa.ec.simpl.contracts.service;

import eu.europa.ec.simpl.contracts.entity.ContractAgreement;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
@Slf4j
public class ContractAgreementFileStorageService {

    @Value("${spring.file-folder}")
    private String fileFolder;

    public void storeFileForContractAgreement(ContractAgreement contractAgreement) {
        try {
            createFileWithData(contractAgreement);
        } catch (IOException e) {
            log.error("Unable to store file", e);
        }
    }

    public String readFile(UUID contractAgreementId) {
        var fileName = getFileName(contractAgreementId);
        var filePath = Paths.get(fileFolder, fileName);
        if (Files.exists(filePath)) {
            try {
                return Files.readString(filePath);
            } catch (IOException e) {
                log.error("Unable to read file: {}", filePath, e);
                return null;
            }
        } else {
            log.error("File does not exist: {}", filePath);
            return null;
        }
    }

    private static String getFileName(UUID contractAgreementId) {
        return FilenameUtils.getName(contractAgreementId.toString() + ".txt");
    }

    private void createFolderIfNeeded() {
        var folder = new File(fileFolder);
        if (!folder.exists()) {
            if (folder.mkdirs()) {
                log.debug("Folder created: " + fileFolder);
            } else {
                log.error("Failed to create folder: " + fileFolder);
            }
        }
    }

    private void createFileWithData(ContractAgreement contractAgreement) throws IOException {
        createFolderIfNeeded();
        var fileName = getFileName(contractAgreement.getContractAgreementId());
        var file = new File(fileFolder, fileName);
        if (!file.exists()) {
            if (file.createNewFile()) {
                log.debug("File created: " + file.getAbsolutePath());
            } else {
                log.error("Failed to create file: " + file.getAbsolutePath());
                return;
            }
        }
        try (var writer = new BufferedWriter(new FileWriter(file))) {
            writer.write("CONTRACT");
            writer.newLine();
            writer.write("-------------");
            writer.newLine();
            writer.write("CONTRACT AGREEMENT: " + contractAgreement.getContractAgreementId().toString());
            writer.newLine();
            writer.write("CONTRACT DEFINITION: " + contractAgreement.getContractDefinitionId());
            writer.newLine();
            writer.write("ASSET: " + contractAgreement.getAssetId());
            writer.newLine();
            writer.write("PROVIDER ID: " + contractAgreement.getProviderId());
            writer.newLine();
            writer.write("PROVIDER SIGNATURE DATE: " + contractAgreement.getProviderSignatureDate()
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            writer.newLine();
            writer.write("CONSUMER: " + contractAgreement.getConsumerId());
            writer.newLine();
            writer.write("CONSUMER SIGNATURE DATE: " + contractAgreement.getConsumerSignatureDate()
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        } catch (IOException e) {
            log.error("Problem writing file content", e);
        }
    }
}
