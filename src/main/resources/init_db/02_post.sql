CREATE TABLE post
(
    id        SERIAL PRIMARY KEY,
    content   VARCHAR(255) NOT NULL,
    author_id INTEGER REFERENCES "user" (id) NOT NULL,
    upvotes   text,
    downvotes text
);