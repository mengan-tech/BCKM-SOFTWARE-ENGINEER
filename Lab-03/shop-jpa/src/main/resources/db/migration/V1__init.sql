-- 1. Sequences for ID generation (Must match allocationSize = 50 in Java)
CREATE SEQUENCE product_seq START WITH 1 INCREMENT BY 50;
CREATE SEQUENCE customer_seq START WITH 1 INCREMENT BY 50;
CREATE SEQUENCE tag_seq START WITH 1 INCREMENT BY 50;
CREATE SEQUENCE orders_seq START WITH 1 INCREMENT BY 50;
CREATE SEQUENCE order_line_seq START WITH 1 INCREMENT BY 50;

-- 2. Tables
CREATE TABLE product (
    id        BIGINT        PRIMARY KEY,
    sku       VARCHAR(20)   NOT NULL UNIQUE,
    name      VARCHAR(120)  NOT NULL,
    amount    DECIMAL(12,2) NOT NULL,
    currency  VARCHAR(3)    NOT NULL,
    category  VARCHAR(20)   NOT NULL,
    stock     INT           NOT NULL DEFAULT 0 CHECK (stock >= 0)
);

CREATE TABLE customer (
    id          BIGINT       PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    email       VARCHAR(255) NOT NULL UNIQUE,
    street      VARCHAR(255) NOT NULL,
    city        VARCHAR(255) NOT NULL,
    zip         VARCHAR(255) NOT NULL,
    ship_street VARCHAR(255) NOT NULL,
    ship_city   VARCHAR(255) NOT NULL,
    ship_zip    VARCHAR(255) NOT NULL
);

CREATE TABLE tag (
    id   BIGINT       PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE orders (
    id          BIGINT                   PRIMARY KEY,
    customer_id BIGINT                   NOT NULL,
    status      VARCHAR(20)              NOT NULL,
    placed_at   TIMESTAMP WITH TIME ZONE NOT NULL,
    version     BIGINT                   NOT NULL DEFAULT 0,
    CONSTRAINT fk_orders_customer FOREIGN KEY (customer_id) REFERENCES customer(id)
);

CREATE TABLE order_line (
    id         BIGINT        PRIMARY KEY,
    order_id   BIGINT        NOT NULL,
    product_id BIGINT        NOT NULL,
    quantity   INT           NOT NULL CHECK (quantity > 0),
    amount     DECIMAL(12,2) NOT NULL,
    currency   VARCHAR(3)    NOT NULL,
    CONSTRAINT fk_orderline_order FOREIGN KEY (order_id) REFERENCES orders(id),
    CONSTRAINT fk_orderline_product FOREIGN KEY (product_id) REFERENCES product(id)
);