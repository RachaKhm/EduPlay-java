package dev.eduplay.services;

import dev.eduplay.core.AppContext;
import dev.eduplay.entities.CartItem;
import dev.eduplay.entities.Commande;
import dev.eduplay.entities.Product;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Simple in-memory cart service (per application instance). Not persisted.
 */
public class CartService {

    private static CartService instance;
    private final Map<Integer, CartItem> items = new HashMap<>(); // key = productId
    private final CommandeService commandeService = new CommandeService();

    private CartService() {}

    public static CartService getInstance() {
        if (instance == null) instance = new CartService();
        return instance;
    }

    public synchronized void add(Product p, int qty) {
        if (p == null || qty <= 0) return;
        CartItem existing = items.get(p.getId());
        if (existing == null) items.put(p.getId(), new CartItem(p, qty));
        else existing.setQuantity(existing.getQuantity() + qty);
    }

    public synchronized void remove(int productId) { items.remove(productId); }

    public synchronized List<CartItem> list() {
        return new ArrayList<>(items.values());
    }

    public synchronized void clear() { items.clear(); }

    public synchronized double getTotal() {
        return items.values().stream().mapToDouble(i -> i.getProduct().getPrice() * i.getQuantity()).sum();
    }

    /**
     * Checkout: create commandes for the current user from cart items.
     * Returns list of generated commande ids.
     */
    public synchronized List<Integer> checkout() {
        int userId = AppContext.getUserId();
        if (userId == 0) throw new IllegalStateException("Utilisateur non connecté");
        if (items.isEmpty()) return Collections.emptyList();
        List<Integer> created = new ArrayList<>();
        for (CartItem it : list()) {
            Product p = it.getProduct();
            Commande c = new Commande();
            c.setProductId(p.getId());
            c.setParentId(userId);
            c.setQuantity(it.getQuantity());
            c.setTotalPrice(p.getPrice() * it.getQuantity());
            int id = commandeService.ajouter(c);
            created.add(id);
        }
        clear();
        return created;
    }
}

