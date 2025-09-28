package com.example.bankingapp.domain.usecase

import com.example.bankingapp.domain.model.PaymentCard
import com.example.bankingapp.domain.repository.CardRepository
import kotlinx.coroutines.flow.Flow
import com.example.bankingapp.domain.model.Result

class GetCardDetailsUseCase(
    private val cardRepository: CardRepository
) {
    suspend operator fun invoke(forceRefresh: Boolean = false): Flow<Result<List<PaymentCard>>> {
        return cardRepository.getCards(forceRefresh)
    }
}