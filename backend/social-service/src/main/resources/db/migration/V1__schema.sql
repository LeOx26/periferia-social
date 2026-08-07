-- Sin clave foránea hacia users: esa tabla vive en authdb, que pertenece a otro
-- bounded context y a otro servicio. El alias del autor va denormalizado para que
-- el feed no necesite ninguna consulta fuera de esta base.
CREATE TABLE posts (
    id           UUID         PRIMARY KEY,
    author_id    UUID         NOT NULL,
    author_alias VARCHAR(30)  NOT NULL,
    message      VARCHAR(280) NOT NULL,
    created_at   TIMESTAMPTZ  NOT NULL,
    like_count   INTEGER      NOT NULL DEFAULT 0
);

CREATE INDEX idx_posts_created_at ON posts (created_at DESC);

CREATE TABLE post_likes (
    post_id    UUID        NOT NULL REFERENCES posts(id) ON DELETE CASCADE,
    user_id    UUID        NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    -- La clave primaria compuesta hace imposible el doble like a nivel de motor.
    -- La regla vive en el dominio; esto es la red de seguridad ante concurrencia.
    PRIMARY KEY (post_id, user_id)
);

CREATE INDEX idx_post_likes_user ON post_likes (user_id);
