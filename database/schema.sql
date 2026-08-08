-- The Furniture Store: MySQL 8 schema and reference data.
-- Configure DB_URL, DB_USERNAME and DB_PASSWORD before starting the application.

CREATE DATABASE IF NOT EXISTS thefurniturestore CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE thefurniturestore;

CREATE TABLE status (
    id INT AUTO_INCREMENT PRIMARY KEY,
    value VARCHAR(45) NOT NULL UNIQUE
);

CREATE TABLE role (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(45) NOT NULL UNIQUE
);

CREATE TABLE province (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(45) NOT NULL
);

CREATE TABLE district (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(45) NOT NULL,
    province_id INT NOT NULL,
    CONSTRAINT fk_district_province FOREIGN KEY (province_id) REFERENCES province(id)
);

CREATE TABLE city (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    district_id INT,
    CONSTRAINT fk_city_district FOREIGN KEY (district_id) REFERENCES district(id)
);

CREATE TABLE user (
    id INT AUTO_INCREMENT PRIMARY KEY,
    fname VARCHAR(45) NOT NULL,
    lname VARCHAR(45) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(60) NOT NULL,
    verification_code VARCHAR(10) NOT NULL,
    status_id INT NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    CONSTRAINT fk_user_status FOREIGN KEY (status_id) REFERENCES status(id)
);

CREATE TABLE admin (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(45) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(60) NOT NULL,
    role_id INT NOT NULL,
    status_id INT NOT NULL,
    CONSTRAINT fk_admin_role FOREIGN KEY (role_id) REFERENCES role(id),
    CONSTRAINT fk_admin_status FOREIGN KEY (status_id) REFERENCES status(id)
);

CREATE TABLE category (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(45) NOT NULL UNIQUE
);

CREATE TABLE product (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    description TEXT NOT NULL,
    category_id INT,
    price DOUBLE NOT NULL,
    quantity INT NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    CONSTRAINT fk_product_category FOREIGN KEY (category_id) REFERENCES category(id)
);

CREATE TABLE product_images (
    id INT AUTO_INCREMENT PRIMARY KEY,
    img_path VARCHAR(150) NOT NULL UNIQUE,
    product_id INT NOT NULL,
    CONSTRAINT fk_product_image_product FOREIGN KEY (product_id) REFERENCES product(id)
);

CREATE TABLE cart (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    product_id INT NOT NULL,
    quantity INT NOT NULL,
    CONSTRAINT uq_cart_user_product UNIQUE (user_id, product_id),
    CONSTRAINT fk_cart_user FOREIGN KEY (user_id) REFERENCES user(id),
    CONSTRAINT fk_cart_product FOREIGN KEY (product_id) REFERENCES product(id)
);

CREATE TABLE wishlist (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    product_id INT NOT NULL,
    created_at DATETIME NOT NULL,
    CONSTRAINT uq_wishlist_user_product UNIQUE (user_id, product_id),
    CONSTRAINT fk_wishlist_user FOREIGN KEY (user_id) REFERENCES user(id),
    CONSTRAINT fk_wishlist_product FOREIGN KEY (product_id) REFERENCES product(id)
);

CREATE TABLE address (
    id INT AUTO_INCREMENT PRIMARY KEY,
    line1 VARCHAR(100) NOT NULL,
    line2 VARCHAR(100),
    postalcode VARCHAR(10),
    mobile VARCHAR(10) NOT NULL,
    user_id INT,
    city_id INT,
    CONSTRAINT fk_address_user FOREIGN KEY (user_id) REFERENCES user(id),
    CONSTRAINT fk_address_city FOREIGN KEY (city_id) REFERENCES city(id)
);

CREATE TABLE orders (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    CONSTRAINT fk_order_user FOREIGN KEY (user_id) REFERENCES user(id)
);

CREATE TABLE orderd_items (
    id INT AUTO_INCREMENT PRIMARY KEY,
    order_id INT NOT NULL,
    product_id INT NOT NULL,
    qty VARCHAR(255) NOT NULL,
    CONSTRAINT fk_order_item_order FOREIGN KEY (order_id) REFERENCES orders(id),
    CONSTRAINT fk_order_item_product FOREIGN KEY (product_id) REFERENCES product(id)
);

CREATE TABLE delivery_types (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(45) NOT NULL,
    price DOUBLE NOT NULL
);

CREATE TABLE delivery (
    id INT AUTO_INCREMENT PRIMARY KEY,
    orders_id INT NOT NULL,
    delivery_types_id INT NOT NULL,
    status_id INT NOT NULL,
    CONSTRAINT fk_delivery_order FOREIGN KEY (orders_id) REFERENCES orders(id),
    CONSTRAINT fk_delivery_type FOREIGN KEY (delivery_types_id) REFERENCES delivery_types(id),
    CONSTRAINT fk_delivery_status FOREIGN KEY (status_id) REFERENCES status(id)
);

INSERT IGNORE INTO status (value) VALUES
    ('PENDING'), ('VERIFIED'), ('APPROVED'), ('ACTIVE'), ('BLOCKED'),
    ('PACKING'), ('SHIPPED'), ('DELIVERED'), ('CANCELLED'), ('RECEIVED'), ('RETURNED');
INSERT IGNORE INTO role (name) VALUES ('super admin'), ('admin');
INSERT IGNORE INTO delivery_types (name, price) VALUES ('Standard', 500.00), ('Express', 1000.00);
