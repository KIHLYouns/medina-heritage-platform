package com.medina.heritage.gamification.mapper;

import com.medina.heritage.gamification.dto.response.PointTransactionResponse;
import com.medina.heritage.gamification.entity.PointTransaction;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper pour convertir entre PointTransaction Entity et DTOs.
 */
@Component
public class PointTransactionMapper {

    /**
     * Convertit une entité PointTransaction vers un PointTransactionResponse DTO.
     */
    public PointTransactionResponse toPointTransactionResponse(PointTransaction transaction) {
        if (transaction == null) {
            return null;
        }

        PointTransactionResponse response = new PointTransactionResponse();
        response.setId(transaction.getId());
        response.setUserId(transaction.getUserId());
        response.setPoints(transaction.getPoints());
        response.setTransactionType(transaction.getTransactionType());
        response.setReasonCode(transaction.getReasonCode());
        response.setDescription(transaction.getDescription());
        response.setReferenceId(transaction.getReferenceId());
        response.setReferenceType(transaction.getReferenceType());
        response.setBalanceAfter(transaction.getBalanceAfter());
        response.setCreatedAt(transaction.getCreatedAt());

        return response;
    }

    /**
     * Convertit une liste de PointTransaction en liste de PointTransactionResponse.
     */
    public List<PointTransactionResponse> toPointTransactionResponseList(List<PointTransaction> transactions) {
        return transactions.stream()
                .map(this::toPointTransactionResponse)
                .collect(Collectors.toList());
    }
}
