// src/main/java/org/simpleskills/util/Paginator.java
package org.simpleskills.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Paginer une liste d'éléments en pages de taille fixe.
 */
public class Paginator<T> {
    private final List<T> items;
    private final int itemsPerPage;

    public Paginator(List<T> items, int itemsPerPage) {
        if (itemsPerPage <= 0) throw new IllegalArgumentException("itemsPerPage must be > 0");
        this.items = new ArrayList<>(items);
        this.itemsPerPage = itemsPerPage;
    }

    /** Nombre total de pages */
    public int getTotalPages() {
        return (items.size() + itemsPerPage - 1) / itemsPerPage;
    }

    /**
     * Retourne la sous-liste correspondant à la page (1-indexée).
     * Si page <1, renvoie la 1ère page ; si > total, renvoie la dernière.
     */
    public List<T> getPage(int page) {
        int tp = getTotalPages();
        int p = Math.min(Math.max(page, 1), tp);
        int start = (p - 1) * itemsPerPage;
        int end = Math.min(start + itemsPerPage, items.size());
        return items.subList(start, end);
    }
}
