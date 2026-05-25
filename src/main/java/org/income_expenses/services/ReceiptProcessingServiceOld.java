package org.income_expenses.services;

import dev.langchain4j.data.image.Image;
import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.data.message.UserMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.income_expenses.aiservices.ReceiptExtractorAiService;
import org.income_expenses.dto.TransactionDto;
import org.income_expenses.models.Organization;
import org.income_expenses.models.TransactionType;
import org.income_expenses.repositories.OrganizationRepository;
import org.income_expenses.repositories.TransactionTypeRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

import org.income_expenses.models.TransactionCategory;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReceiptProcessingServiceOld {

    private final ReceiptExtractorAiService receiptExtractor;
    private final OrganizationRepository organizationRepository;
    private final TransactionTypeRepository transactionTypeRepository;

    public TransactionDto processReceipt(byte[] fileBytes, String contentType) {

        List<Organization> organizations = organizationRepository.findAll();
        List<TransactionType> transactionTypes = transactionTypeRepository.findAll();

        // 1. Подготавливаем строку с перечнем организаций для передачи в ИИ сервис
        organizations = organizations.stream()
                .filter(organization -> organization.getCategory() == TransactionCategory.EXPENSE)
                .collect(Collectors.toList());
        String availableOrganizations = organizations.stream()
                .map(organization -> String.format("- ID: %d, Название: '%s', Описание: '%s'", organization.getId(), organization.getOrganizationName(), organization.getDescription()))
                .collect(Collectors.joining("\n"));

        // 2.Подготавливаем строку с перечнем типов транзакций для передачи в ИИ сервис
        transactionTypes = transactionTypes.stream()
                .filter(transactionType -> transactionType.getCategory() == TransactionCategory.EXPENSE)
                .collect(Collectors.toList());
        String availableTransactionTypes = transactionTypes.stream()
                .map(transactionType -> String.format("- ID: %d, Название: '%s'", transactionType.getId(), transactionType.getTransactionTypeName()))
                .collect(Collectors.joining("\n"));

        // 3. Получаем текущую дату и время в формате строки для передачи в ИИ сервис
        String currentDateTime = LocalDateTime.now().toString();

        // 4. Преобразуем объект фотографии в формат Image для передачи в ИИ сервис
        Image image = null;

        if (contentType != null && contentType.startsWith("image/")) {
            String base64Data = Base64.getEncoder().encodeToString(fileBytes);
            image = Image.builder()
                    .base64Data(base64Data)
                    .mimeType(contentType)
                    .build();
        } else {
            log.error("Файл не загружен или не правильного формата");
            throw new IllegalArgumentException("Загрузите файл с изображением (jpg, png)");
        }

        // 5. Готовим UserMessage с промптом и изображением для отправки в ИИ сервис
        UserMessage message = UserMessage.from(
                TextContent.from(String.format(
                        "Ты — эксперт по извлечению данных из казахстанских фискальных чеков. " +
                                "Проанализируй изображение чека и верни ТОЛЬКО JSON без пояснений.\n\n" +
                                "Правила извлечения:\n" +
                                "1. Сумма чека (amount) — это итоговая стоимость (обычно после слов 'ИТОГО' или 'БАРЛЫҒЫ').\n" +
                                "2. Дата транзакции (whenPerformed) — извлекай из содержания дату и время. Если в чеке нет такого атрибута — используй текущую дату: %s\n" +
                                "3. Организация (organization) — выбери из доступного списка наиболее близкое по смыслу (обычно после 'ИП' или 'ТОО'):\n%s\n" +
                                "4. Тип транзакции (transactionType) — выбери из доступного списка наиболее близкое по смыслу:\n%s\n" +
                                "5. Позиции товаров (items) — извлекай из каждой строки чека:\n" +
                                "   - name: название товара\n" +
                                "   - quantity: количество\n" +
                                "   - price: цена за единицу\n" +
                                "   - amount: стоимость позиции (количество × цена)\n" +
                                "6. В поле description кратко опиши, что удалось извлечь.\n\n" +
                                "ВНИМАНИЕ: Не добавляй markdown, ```json или пояснения. Только чистый JSON.",
                        currentDateTime,
                        availableOrganizations,
                        availableTransactionTypes
                )),
                ImageContent.from(image)
        );

        // 6. Отправляем UserMessage в ИИ сервис для обработки и получения проекта транзакции
        log.info("Отправка файла в LangChain4j с динамическим списком организаций и типов транзакций...");
        TransactionDto transactionProject = receiptExtractor.extractTransaction(
                /*availableOrganizations, availableTransactionTypes, currentDateTime,*/ message);

        log.info("Чек успешно распознан и модель выбрала организацию {} и тип транзакции {}, распознано позиций {}",
                transactionProject.getOrganization() != null ? transactionProject.getOrganization().getOrganizationName() : "Не определено",
                transactionProject.getTransactionType() != null ? transactionProject.getTransactionType().getTransactionTypeName() : "Не определено",
                transactionProject.getItems() != null ? transactionProject.getItems().size() : "не распознано");

        return transactionProject;
    }

}
