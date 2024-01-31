CREATE TABLE "user"
(
    id    SERIAL PRIMARY KEY,
    pub_id integer UNIQUE NOT NULL,
    name  VARCHAR(255) NOT NULL
);