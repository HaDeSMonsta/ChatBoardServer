CREATE TABLE "user"
(
    id      SERIAL PRIMARY KEY,
    sec_num  integer NOT NULL,
    name    VARCHAR(255)   UNIQUE NOT NULL,
    blocked BOOLEAN DEFAULT FALSE
);

CREATE TABLE post
(
    id        SERIAL PRIMARY KEY,
    content   VARCHAR(500) NOT NULL,
    author_id INTEGER REFERENCES "user" (id) NOT NULL,
    upvotes   text NOT NULL,
    downvotes text NOT NULL
);

CREATE TABLE log
(
    id      SERIAL PRIMARY KEY,
    time_stamp TIMESTAMP NOT NULL,
    matr_num INTEGER NOT NULL
);