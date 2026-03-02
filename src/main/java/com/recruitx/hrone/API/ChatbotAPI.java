package com.recruitx.hrone.API;

import com.recruitx.hrone.Repository.EvenementRepository;
import com.recruitx.hrone.Repository.ActiviteRepository;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class ChatbotAPI {

    private final EvenementRepository evenementService = new EvenementRepository();
    private final ActiviteRepository activiteService = new ActiviteRepository();

    /**
     * Simule un appel à une API d'Intelligence Artificielle.
     */
    public CompletableFuture<String> getResponse(String userMessage) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Simulation du délai réseau (Round Trip Time)
                TimeUnit.MILLISECONDS.sleep(1200);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            String msg = userMessage.toLowerCase();

            // --- Logique de détection de quantité ---
            int limit = 3; // Par défaut
            boolean showAll = msg.contains("tous") || msg.contains("tout") || msg.contains("toute")
                    || msg.contains("all");

            // Extraction de nombre simple
            if (msg.contains(" 1 ") || msg.contains("un ") || msg.startsWith("1 ") || msg.contains("dernier"))
                limit = 1;
            if (msg.contains(" 2 ") || msg.contains("deux"))
                limit = 2;
            if (msg.contains(" 3 ") || msg.contains("trois"))
                limit = 3;
            if (msg.contains(" 4 ") || msg.contains("quatre"))
                limit = 4;
            if (msg.contains(" 5 ") || msg.contains("cinq"))
                limit = 5;

            if (msg.contains("bonjour") || msg.contains("salut")) {
                int count = evenementService.getAll().size();
                return "Bonjour ! Je suis l'assistant HR One. Nous avons actuellement " + count
                        + " événements enregistrés. Comment puis-je vous aider ?";
            }

            // --- Traitement des Activités ---
            if (msg.contains("activit")) {
                var list = activiteService.getAll();
                if (list.isEmpty())
                    return "Désolé, aucune activité n'est disponible.";

                int finalLimit = showAll ? list.size() : Math.min(limit, list.size());
                StringBuilder sb = new StringBuilder(showAll ? "Voici toutes nos activités : \n"
                        : "Voici les " + finalLimit + " dernières activités : \n");

                list.stream().limit(finalLimit).forEach(a -> sb.append("- ").append(a.getTitre()).append("\n"));
                return sb.toString();
            }

            // --- Traitement des Événements ---
            if (msg.contains("evenement") || msg.contains("événement")) {
                var list = evenementService.getAll();
                if (list.isEmpty())
                    return "Désolé, aucun événement n'est planifié pour le moment.";

                int finalLimit = showAll ? list.size() : Math.min(limit, list.size());
                StringBuilder sb = new StringBuilder(showAll ? "Voici tous nos événements : \n"
                        : "Voici les " + finalLimit + " derniers événements (par date) : \n");

                list.stream().limit(finalLimit).forEach(e -> sb.append("- ").append(e.getTitre())
                        .append(" à ").append(e.getLocalisation()).append("\n"));
                return sb.toString();
            }

            return "Je peux vous renseigner sur nos événements ou nos activités. Essayez : 'tous les événements' ou 'les 2 dernières activités'.";
        });
    }
}
