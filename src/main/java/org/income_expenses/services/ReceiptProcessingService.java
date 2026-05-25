package org.income_expenses.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.data.image.Image;
import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.income_expenses.aiservices.ReceiptExtractorAiService;
import org.income_expenses.dto.TransactionDto;
import org.income_expenses.models.Organization;
import org.income_expenses.models.TransactionCategory;
import org.income_expenses.models.TransactionType;
import org.income_expenses.repositories.OrganizationRepository;
import org.income_expenses.repositories.TransactionTypeRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReceiptProcessingService {

    private final ReceiptExtractorAiService receiptExtractor;
    private final OrganizationRepository organizationRepository;
    private final TransactionTypeRepository transactionTypeRepository;
    private final ChatModel chatModel;
    private final ObjectMapper objectMapper;

    public TransactionDto processReceipt(byte[] fileBytes, String contentType) {

        // 1. Подготавливаем для передачи в ИИ сервис:
        //    - перечень организаций
        //    - перечень типов транзакций
        //    - текущую дату и время
        List<Organization> organizations = organizationRepository.findAll();
        organizations = organizations.stream()
                .filter(organization -> organization.getCategory() == TransactionCategory.EXPENSE)
                .collect(Collectors.toList());
        String availableOrganizations = organizations.stream()
                .map(organization -> String.format("- ID: %d, Название: '%s', Описание: '%s'", organization.getId(), organization.getOrganizationName(), organization.getDescription()))
                .collect(Collectors.joining("\n"));

        List<TransactionType> transactionTypes = transactionTypeRepository.findAll();
        transactionTypes = transactionTypes.stream()
                .filter(transactionType -> transactionType.getCategory() == TransactionCategory.EXPENSE)
                .collect(Collectors.toList());
        String availableTransactionTypes = transactionTypes.stream()
                .map(transactionType -> String.format("- ID: %d, Название: '%s'", transactionType.getId(), transactionType.getTransactionTypeName()))
                .collect(Collectors.joining("\n"));

        String currentDateTime = LocalDateTime.now().toString();

        // 2. Подготавливаем промпт с правилами извлечения данных из фотографии чека
        String prompt = String.format(
                "Ты — эксперт по извлечению данных из казахстанских фискальных чеков. " +
                        "Проанализируй изображение чека и верни ТОЛЬКО JSON без пояснений.\n\n" +
                        "Правила извлечения:\n" +
                        "1. Сумма чека (amount) — это итоговая стоимость (обычно после слов 'ИТОГО' или 'БАРЛЫҒЫ').\n" +
                        "2. Дата транзакции (whenPerformed) — извлеки из чека дату и время. Если в чеке нет такого атрибута — используй текущую дату: %s\n" +
                        "3. Организация (organization) — выбери из списка наиболее близкое по смыслу:\n%s\n" +
                        "4. Тип транзакции (transactionType) — выбери из списка наиболее близкое по смыслу:\n%s\n" +
                        "5. Категория транзакции (category) — всегда EXPENSE\n" +
                        "6. Позиции товаров (items) — извлекай из каждой позиции чека:\n" +
                        "   - name: название товара\n" +
                        "   - quantity: количество\n" +
                        "   - price: цена за единицу\n" +
                        "   - amount: стоимость позиции (количество × цена)\n" +
                        "6. В поле description кратко опиши, что удалось извлечь.\n\n" +
                        "ВНИМАНИЕ: Не добавляй markdown, ```json или пояснения. Только чистый JSON.",
                currentDateTime,
                availableOrganizations,
                availableTransactionTypes
        );

        // 3. Преобразуем фотографию в формат Image для передачи в ИИ сервис
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

        // 4. Готовим UserMessage с промптом и изображением для отправки в ИИ сервис
        UserMessage message = UserMessage.from(
                TextContent.from(prompt),
                ImageContent.from(image)
        );

        // 5. Отправка UserMessage в ИИ сервис
        ChatResponse response = chatModel.chat(message);
        String json = response.aiMessage().text();
        log.info("Ответ от AI: {}", json);

        try {
            TransactionDto transactionProject = objectMapper.readValue(json, TransactionDto.class);

            log.info("Чек успешно распознан: организацию={} тип транзакции={}, позиций={}",
                    transactionProject.getOrganization() != null ? transactionProject.getOrganization().getOrganizationName() : "Не определено",
                    transactionProject.getTransactionType() != null ? transactionProject.getTransactionType().getTransactionTypeName() : "Не определено",
                    transactionProject.getItems() != null ? transactionProject.getItems().size() : "не распознано");

            return transactionProject;

        } catch (JsonProcessingException e) {
            log.error("Ошибка парсинга JSON от модели: {}", json, e);
            throw new RuntimeException("Не удалось распознать чек: " + e.getMessage());
        }
    }
}
