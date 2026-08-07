package com.periferia.social.feed.infrastructure;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

/**
 * Métricas de negocio, no solo de plataforma.
 *
 * Saber que el servicio responde con latencia p95 de 20 ms no dice si la gente
 * está publicando. Estas tres cuentan lo que le importa al producto.
 */
@Component
public class FeedMetrics {

    private final Counter postsCreated;
    private final Counter likesRegistered;

    public FeedMetrics(MeterRegistry registry, LikeWebSocketHandler webSocketHandler) {
        // "published" y no "created": Micrometer elimina el sufijo `created` porque
        // `_created` está reservado en OpenMetrics, y la métrica acabaría llamándose
        // `periferia_posts_total`, que no dice qué cuenta.
        this.postsCreated = Counter.builder("periferia.posts.published")
            .description("Publicaciones creadas")
            .register(registry);

        this.likesRegistered = Counter.builder("periferia.likes")
            .description("Likes registrados")
            .register(registry);

        // Gauge: se consulta en el momento del scrape, no se acumula.
        registry.gauge("periferia.websocket.sessions.active", webSocketHandler,
                       LikeWebSocketHandler::activeSessions);
    }

    public void postCreated() {
        postsCreated.increment();
    }

    public void likeRegistered() {
        likesRegistered.increment();
    }
}
