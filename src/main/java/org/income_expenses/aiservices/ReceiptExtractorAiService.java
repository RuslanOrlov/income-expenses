package org.income_expenses.aiservices;

import dev.langchain4j.data.image.Image;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.V;
import dev.langchain4j.service.spring.AiService;
import org.income_expenses.dto.TransactionDto;

@AiService
public interface ReceiptExtractorAiService {

    @SystemMessage("""
    Ты — помощник для извлечения данных из чеков покупок.
    Правила:
    - Ответ должен содержать только валидный JSON без markdown-разметки.
    - Не используй тройные обратные кавычки.
    - Все числовые поля должны быть числами (не строками).
    - Если поле невозможно определить — не включай его в JSON или используй null.
    - Следуй указаниям пользователя о формате ответа.
    """)
    TransactionDto extractTransaction(
            //@V("availableOrganizations") String availableOrganizations,
            //@V("availableTransactionTypes") String availableTransactionTypes,
            //@V("currentDateTime") String currentDateTime,
            @UserMessage dev.langchain4j.data.message.UserMessage message
    );

}
