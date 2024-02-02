CREATE TABLE post
(
    id        SERIAL PRIMARY KEY,
    content   TEXT NOT NULL,
    author_id INTEGER REFERENCES "user" (id) NOT NULL,
    upvotes   text,
    downvotes text
);