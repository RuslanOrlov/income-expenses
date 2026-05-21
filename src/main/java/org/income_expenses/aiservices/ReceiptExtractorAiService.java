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
        - Ответ должен содержать только JSON.
        - Не добавляй markdown, пояснения или комментарии.
        - category всегда должна быть EXPENSE.
        - organization должна быть выбрана только из переданного списка.
        - transactionType должен быть выбран только из переданного списка.
        - Если точного совпадения нет — выбери наиболее близкое по смыслу.
        - Если в чеке присутствуют товары — заполни items.
        - Если дата в чеке отсутствует — используй текущую дату из контекста.
        - amount должен быть итоговой суммой чека.
        """)
    TransactionDto extractTransaction(
            @V("availableOrganizations") String availableOrganizations,
            @V("availableTransactionTypes") String availableTransactionTypes,
            @V("currentDateTime") String currentDateTime,
            @UserMessage dev.langchain4j.data.message.UserMessage message
    );

}
