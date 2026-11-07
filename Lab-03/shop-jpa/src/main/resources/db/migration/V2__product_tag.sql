CREATE TABLE product_tag (
    product_id BIGINT NOT NULL,
    tag_id     BIGINT NOT NULL,
    CONSTRAINT pk_product_tag PRIMARY KEY (product_id, tag_id),
    CONSTRAINT fk_product_tag_product FOREIGN KEY (product_id) REFERENCES product(id),
    CONSTRAINT fk_product_tag_tag FOREIGN KEY (tag_id) REFERENCES tag(id)
);
