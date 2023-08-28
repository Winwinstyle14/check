CREATE TABLE password_reset (
    email VARCHAR (191) PRIMARY KEY,
    token VARCHAR (36) NOT NULL ,
    created_at TIMESTAMP (6) NOT NULL
);