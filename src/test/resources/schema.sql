CREATE TABLE product (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    price DOUBLE PRECISION NOT NULL,
    quantity INT NOT NULL,
    description VARCHAR(1024)
);

CREATE TABLE bench_user (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL
);

CREATE TABLE user_profile (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT UNIQUE NOT NULL REFERENCES bench_user(id) ON DELETE CASCADE,
    bio TEXT,
    avatar_url VARCHAR(512)
);

CREATE TABLE author (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    country VARCHAR(255)
);

CREATE TABLE book (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    author_id BIGINT NOT NULL REFERENCES author(id) ON DELETE CASCADE,
    published_year INT,
    isbn VARCHAR(20)
);

CREATE TABLE versioned_item (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE inventory (
    sku VARCHAR(64) PRIMARY KEY,
    quantity INT NOT NULL,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE task (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    status INT NOT NULL,
    priority VARCHAR(32) NOT NULL
);

CREATE TABLE student (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL
);

CREATE TABLE course (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL
);

CREATE TABLE student_course (
    student_id BIGINT NOT NULL REFERENCES student(id) ON DELETE CASCADE,
    course_id BIGINT NOT NULL REFERENCES course(id) ON DELETE CASCADE,
    PRIMARY KEY (student_id, course_id)
);

CREATE TABLE audited_note (
    id BIGSERIAL PRIMARY KEY,
    created_by VARCHAR(64),
    body VARCHAR(255) NOT NULL
);

CREATE TABLE catalog_item (
    id BIGSERIAL PRIMARY KEY,
    item_type VARCHAR(16),
    name VARCHAR(128) NOT NULL,
    bundle_size INT,
    sku_code VARCHAR(64)
);

CREATE TABLE joined_item (
    id BIGSERIAL PRIMARY KEY,
    item_type VARCHAR(16),
    name VARCHAR(128) NOT NULL
);

CREATE TABLE joined_bundle (
    item_id BIGINT PRIMARY KEY REFERENCES joined_item (id),
    bundle_size INT
);

CREATE TABLE joined_sku (
    item_id BIGINT PRIMARY KEY REFERENCES joined_item (id),
    sku_code VARCHAR(64)
);

CREATE TABLE ticket (
    id VARCHAR(64) PRIMARY KEY,
    title VARCHAR(128) NOT NULL
);

CREATE TABLE ticket_comment (
    comment_id VARCHAR(64) PRIMARY KEY,
    ticket_id VARCHAR(64) NOT NULL,
    body VARCHAR(255) NOT NULL
);

CREATE TABLE json_doc (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(64) NOT NULL,
    attributes TEXT,
    tags TEXT,
    spec TEXT,
    payload JSONB
);
