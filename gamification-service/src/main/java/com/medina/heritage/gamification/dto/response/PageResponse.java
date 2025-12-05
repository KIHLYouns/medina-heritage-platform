package com.medina.heritage.gamification.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO pour les réponses paginées.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> {

    /**
     * Liste des éléments.
     */
    private List<T> content;

    /**
     * Numéro de page actuel.
     */
    private int page;

    /**
     * Taille de la page.
     */
    private int size;

    /**
     * Nombre total d'éléments.
     */
    private long totalElements;

    /**
     * Nombre total de pages.
     */
    private int totalPages;

    /**
     * Indique s'il y a une page suivante.
     */
    private boolean hasNext;

    /**
     * Indique s'il y a une page précédente.
     */
    private boolean hasPrevious;
}
