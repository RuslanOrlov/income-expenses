package org.income_expenses.repositories;

import org.income_expenses.models.WalletTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, Long> {

    @Query( nativeQuery = true,
            value =         "SELECT wt.* " +
                                "FROM public.wallet_transaction wt " +
                                "JOIN public.family_wallet fw ON fw.id = wt.wallet_id " +
                                "JOIN public.wallet_member wm ON wm.wallet_id = fw.id " +
                            "WHERE wt.wallet_id = :walletId " +
                                "AND (fw.owner_id = :userId OR wm.member_id = :userId) " +
                                "AND wt.category = 'INCOME'",
            countQuery =    "SELECT COUNT(wt.*) " +
                                "FROM public.wallet_transaction wt " +
                                "JOIN public.family_wallet fw ON fw.id = wt.wallet_id " +
                                "JOIN public.wallet_member wm ON wm.wallet_id = fw.id " +
                            "WHERE wt.wallet_id = :walletId " +
                                "AND (fw.owner_id = :userId OR wm.member_id = :userId) " +
                                "AND wt.category = 'INCOME'" )
    Page<WalletTransaction> getIncomeTransactions(
            @Param("userId") Long userId,
            @Param("walletId") Long walletId,
            Pageable pageable
    );

    @Query( nativeQuery = true,
            value =         "SELECT wt.* " +
                    "FROM public.wallet_transaction wt " +
                    "JOIN public.family_wallet fw ON fw.id = wt.wallet_id " +
                    "JOIN public.wallet_member wm ON wm.wallet_id = fw.id " +
                    "WHERE wt.wallet_id = :walletId " +
                    "AND (fw.owner_id = :userId OR wm.member_id = :userId) " +
                    "AND wt.category = 'EXPENSE'",
            countQuery =    "SELECT COUNT(wt.*) " +
                    "FROM public.wallet_transaction wt " +
                    "JOIN public.family_wallet fw ON fw.id = wt.wallet_id " +
                    "JOIN public.wallet_member wm ON wm.wallet_id = fw.id " +
                    "WHERE wt.wallet_id = :walletId " +
                    "AND (fw.owner_id = :userId OR wm.member_id = :userId) " +
                    "AND wt.category = 'EXPENSE'" )
    Page<WalletTransaction> getExpenseTransactions(
            @Param("userId") Long userId,
            @Param("walletId") Long walletId,
            Pageable pageable
    );

    @Query( nativeQuery = true,
            value = "SELECT COUNT(wt.*) " +
                        "FROM public.wallet_transaction wt " +
                        "JOIN public.family_wallet fw ON fw.id = wt.wallet_id " +
                        "JOIN public.wallet_member wm ON wm.wallet_id = fw.id " +
                    "WHERE wt.wallet_id = :walletId " +
                        "AND (fw.owner_id = :userId OR wm.member_id = :userId) " +
                        "AND wt.category = 'INCOME'" )
    long incomeTransactionsCount(@Param("userId") Long userId, @Param("walletId") Long walletId);

    @Query( nativeQuery = true,
            value = "SELECT COUNT(wt.*) " +
                    "FROM public.wallet_transaction wt " +
                    "JOIN public.family_wallet fw ON fw.id = wt.wallet_id " +
                    "JOIN public.wallet_member wm ON wm.wallet_id = fw.id " +
                    "WHERE wt.wallet_id = :walletId " +
                    "AND (fw.owner_id = :userId OR wm.member_id = :userId) " +
                    "AND wt.category = 'EXPENSE'" )
    long expenseTransactionsCount(@Param("userId") Long userId, @Param("walletId") Long walletId);
}
