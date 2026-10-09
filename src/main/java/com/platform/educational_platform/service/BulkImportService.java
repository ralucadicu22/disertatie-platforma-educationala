package com.platform.educational_platform.service;


import com.platform.educational_platform.dto.BulkImportPreviewResponse;
import com.platform.educational_platform.dto.BulkImportRowResult;
import com.platform.educational_platform.model.ImportHistory;
import com.platform.educational_platform.model.User;
import com.platform.educational_platform.model.enums.UserRole;
import com.platform.educational_platform.repository.ImportHistoryRepository;
import com.platform.educational_platform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import tools.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class BulkImportService {

    private static final Set<String> VALID_ROLES = Set.of("ELEV", "PROFESOR", "PARINTE", "EDITOR", "ADMIN");
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)*\\.[a-zA-Z]{2,}$"
    );
    private static final String CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final ImportHistoryRepository importHistoryRepository;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper;
    private final EmailService emailService;


    public BulkImportPreviewResponse preview(MultipartFile file) throws IOException {
        List<RawRow> rawRows = parseFile(file);
        return buildPreview(rawRows, /* existingEmails from DB */ loadExistingEmails());
    }
    public BulkImportPreviewResponse importUsers(MultipartFile file, User admin) throws IOException {
        List<RawRow> rawRows = parseFile(file);
        Set<String> existingEmails = loadExistingEmails();
        Set<String> seenInFile = new HashSet<>();

        List<BulkImportRowResult> results = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        int successCount = 0;
        int errorCount = 0;
        int skippedCount = 0;

        for (RawRow raw : rawRows) {
            BulkImportRowResult result = validate(raw, existingEmails, seenInFile);

            if ("VALID".equals(result.getStatus())) {
                String tempPassword = generateTempPassword();
                try {
                    User user = User.builder()
                            .email(result.getEmail().toLowerCase().trim())
                            .name(result.getName() != null ? result.getName() : result.getEmail())
                            .password(passwordEncoder.encode(tempPassword))
                            .role(UserRole.valueOf(result.getRole().toUpperCase()))
                            .active(true)
                            .createdAt(LocalDateTime.now())
                            .build();
                    userRepository.save(user);
                    existingEmails.add(result.getEmail().toLowerCase().trim());
                    result.setTempPassword(tempPassword);
                    try {
                        emailService.sendTempPassword(user.getEmail(), user.getName(), tempPassword);
                        Thread.sleep(1100);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    } catch (Exception e) {
                        log.warn("Nu s-a putut trimite emailul către {}: {}", user.getEmail(), e.getMessage());
                    }

                    successCount++;
                    existingEmails.add(result.getEmail().toLowerCase().trim());
                    result.setTempPassword(tempPassword);
                    successCount++;
                } catch (Exception e) {
                    log.error("Failed to create user at row {}: {}", raw.rowNumber(), e.getMessage());
                    result.setStatus("INVALID");
                    result.setErrorMessage("Eroare internă la creare: " + e.getMessage());
                    result.setTempPassword(null);
                    errorCount++;
                    errors.add("Rândul " + raw.rowNumber() + ": " + e.getMessage());
                }
            } else if ("DUPLICATE".equals(result.getStatus())) {
                skippedCount++;
                errors.add("Rândul " + raw.rowNumber() + ": email duplicat (" + raw.email() + ")");
            } else {
                errorCount++;
                errors.add("Rândul " + raw.rowNumber() + ": " + result.getErrorMessage());
            }

            if (!"VALID".equals(result.getStatus()) || result.getTempPassword() != null) {
                results.add(result);
            } else {
                results.add(result);
            }
        }
        saveHistory(admin, file.getOriginalFilename(),
                rawRows.size(), successCount, errorCount, skippedCount, errors);

        return BulkImportPreviewResponse.builder()
                .totalRows(rawRows.size())
                .validCount(successCount)
                .invalidCount(errorCount)
                .duplicateCount(skippedCount)
                .rows(results)
                .build();
    }



    public List<ImportHistory> getHistory() {
        return importHistoryRepository.findTop20ByOrderByImportedAtDesc();
    }

    private BulkImportPreviewResponse buildPreview(List<RawRow> rawRows, Set<String> existingEmails) {
        Set<String> seenInFile = new HashSet<>();
        List<BulkImportRowResult> results = new ArrayList<>();
        int validCount = 0, invalidCount = 0, duplicateCount = 0;

        for (RawRow raw : rawRows) {
            BulkImportRowResult result = validate(raw, existingEmails, seenInFile);
            results.add(result);
            switch (result.getStatus()) {
                case "VALID" -> validCount++;
                case "DUPLICATE" -> duplicateCount++;
                default -> invalidCount++;
            }
        }

        return BulkImportPreviewResponse.builder()
                .totalRows(rawRows.size())
                .validCount(validCount)
                .invalidCount(invalidCount)
                .duplicateCount(duplicateCount)
                .rows(results)
                .build();
    }

    private BulkImportRowResult validate(RawRow raw, Set<String> existingEmails, Set<String> seenInFile) {
        if (raw.email() == null || raw.email().isBlank()) {
            return BulkImportRowResult.builder()
                    .rowNumber(raw.rowNumber()).email("").name(raw.name()).role(raw.role())
                    .status("INVALID").errorMessage("Email lipsă").build();
        }
        String email = raw.email().toLowerCase().trim();
        String localPart = email.contains("@") ? email.substring(0, email.indexOf('@')) : email;
        if (localPart.contains("..") || localPart.startsWith(".") || localPart.endsWith(".")) {
            return BulkImportRowResult.builder()
                    .rowNumber(raw.rowNumber()).email(email).name(raw.name()).role(raw.role())
                    .status("INVALID").errorMessage("Format email invalid (puncte consecutive)").build();
        }

        if (!EMAIL_PATTERN.matcher(email).matches()) {
            return BulkImportRowResult.builder()
                    .rowNumber(raw.rowNumber()).email(email).name(raw.name()).role(raw.role())
                    .status("INVALID").errorMessage("Format email invalid").build();
        }

        if (raw.role() == null || raw.role().isBlank()) {
            return BulkImportRowResult.builder()
                    .rowNumber(raw.rowNumber()).email(email).name(raw.name()).role("")
                    .status("INVALID").errorMessage("Rol lipsă").build();
        }
        String role = raw.role().toUpperCase().trim();
        if (!VALID_ROLES.contains(role)) {
            return BulkImportRowResult.builder()
                    .rowNumber(raw.rowNumber()).email(email).name(raw.name()).role(role)
                    .status("INVALID").errorMessage("Rol invalid: " + role + ". Valori acceptate: " + VALID_ROLES).build();
        }

        if (existingEmails.contains(email) || seenInFile.contains(email)) {
            return BulkImportRowResult.builder()
                    .rowNumber(raw.rowNumber()).email(email).name(raw.name()).role(role)
                    .status("DUPLICATE").errorMessage("Email deja există").build();
        }

        seenInFile.add(email);
        return BulkImportRowResult.builder()
                .rowNumber(raw.rowNumber()).email(email).name(raw.name()).role(role)
                .status("VALID").build();
    }

    private List<RawRow> parseFile(MultipartFile file) throws IOException {
        String filename = file.getOriginalFilename() != null ? file.getOriginalFilename().toLowerCase() : "";
        if (filename.endsWith(".xlsx") || filename.endsWith(".xls")) {
            return parseExcel(file.getInputStream());
        }
        return parseCsv(file.getInputStream());
    }

    private List<RawRow> parseCsv(InputStream is) throws IOException {
        List<RawRow> rows = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
             CSVParser parser = CSVFormat.DEFAULT.builder()
                     .setHeader()
                     .setSkipHeaderRecord(true)
                     .setIgnoreHeaderCase(true)
                     .setTrim(true)
                     .build()
                     .parse(reader)) {

            int rowNum = 2;
            for (CSVRecord record : parser) {
                rows.add(new RawRow(
                        rowNum++,
                        getOptional(record, "email"),
                        getOptional(record, "name"),
                        getOptional(record, "role")
                ));
            }
        }
        return rows;
    }

    private List<RawRow> parseExcel(InputStream is) throws IOException {
        List<RawRow> rows = new ArrayList<>();
        try (Workbook workbook = new XSSFWorkbook(is)) {
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null || sheet.getPhysicalNumberOfRows() < 2) return rows;

            Row headerRow = sheet.getRow(0);
            Map<String, Integer> colIndex = buildColumnIndex(headerRow);

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                rows.add(new RawRow(
                        i + 1,
                        getCellValue(row, colIndex.get("email")),
                        getCellValue(row, colIndex.get("name")),
                        getCellValue(row, colIndex.get("role"))
                ));
            }
        }
        return rows;
    }

    private Map<String, Integer> buildColumnIndex(Row headerRow) {
        Map<String, Integer> index = new HashMap<>();
        if (headerRow == null) return index;
        for (Cell cell : headerRow) {
            String header = cell.getStringCellValue().trim().toLowerCase();
            index.put(header, cell.getColumnIndex());
        }
        return index;
    }

    private String getCellValue(Row row, Integer colIdx) {
        if (colIdx == null) return null;
        Cell cell = row.getCell(colIdx);
        if (cell == null) return null;
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default -> null;
        };
    }

    private String getOptional(CSVRecord record, String header) {
        try { return record.get(header); } catch (Exception e) { return null; }
    }


    private Set<String> loadExistingEmails() {

        return new HashSet<>(userRepository.findAllEmails());
    }

    private String generateTempPassword() {
        StringBuilder sb = new StringBuilder(10);
        for (int i = 0; i < 10; i++) sb.append(CHARS.charAt(RANDOM.nextInt(CHARS.length())));
        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    private void saveHistory(User admin, String fileName, int total, int success, int error, int skipped,
                             List<String> errors) {
        String errorsJson;
        try {
            errorsJson = objectMapper.writeValueAsString(errors);
        } catch (Exception e) {
            errorsJson = "[]";
        }
        importHistoryRepository.save(ImportHistory.builder()
                .adminId(admin.getId())
                .adminEmail(admin.getEmail())
                .adminName(admin.getName())
                .fileName(fileName)
                .importedAt(LocalDateTime.now())
                .totalRows(total)
                .successCount(success)
                .errorCount(error)
                .skippedCount(skipped)
                .errorsJson(errorsJson)
                .build());
    }

    private record RawRow(int rowNumber, String email, String name, String role) {}
}
