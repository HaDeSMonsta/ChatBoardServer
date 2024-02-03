CREATE TABLE "user"
(
    id      SERIAL PRIMARY KEY,
    sec_num  integer NOT NULL,
    name    VARCHAR(255)   UNIQUE NOT NULL,
    blocked BOOLEAN DEFAULT FALSE
);