DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS locations CASCADE;
DROP TABLE IF EXISTS coordinates CASCADE;
DROP TABLE IF EXISTS lines CASCADE;

CREATE TABLE users
(
    seq           bigint      NOT NULL AUTO_INCREMENT,      --사용자 PK
    name          varchar(10) NOT NULL,                     --사용자명
    email         varchar(50) NOT NULL,                     --로그인 이메일
    password      varchar(80) NOT NULL,                     --로그인 비밀번호
    last_login_at datetime             DEFAULT NULL,        --최종 로그인 일자
    create_at     datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP(),
    roles         varchar(50) NOT NULL DEFAULT 'ROLE_USER', --사용자 권한
    access_token  varchar(300)         DEFAULT NULL,        --토큰
    refresh_token varchar(300)         DEFAULT NULL,        --리프레시 토큰
    PRIMARY KEY (seq),
    CONSTRAINT unq_user_email UNIQUE (email)
);

CREATE TABLE locations
(
    seq        bigint      NOT NULL AUTO_INCREMENT, --위치 PK
    name       varchar(50) NOT NULL,                --위치명
    details    varchar(1000)        DEFAULT NULL,   --위치설명
    address    varchar(100)         DEFAULT NULL,   --주소
    x_axis     double      NOT NULL,                --위치 x축
    y_axis     double      NOT NULL,                --위치 y축
    user_seq   bigint      NOT NULL,                --사용자 FK
    create_at  datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP(),
    image_path varchar(100)         DEFAULT NULL,   --이미지 경로
    PRIMARY KEY (seq),
    CONSTRAINT fk_locations_to_users FOREIGN KEY (user_seq) REFERENCES users (seq) ON DELETE RESTRICT ON UPDATE RESTRICT
);


CREATE TABLE lines
(
    seq     bigint NOT NULL AUTO_INCREMENT, --라인 PK
    distance double NOT NULL,               --라인 길이
    seq_one bigint NOT NULL,                --좌표1 PK
    seq_two bigint NOT NULL,                --좌표2 PK
    PRIMARY KEY (seq),
    CONSTRAINT fk_lines_to_coordinates_one FOREIGN KEY (seq_one) REFERENCES locations (seq) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_lines_to_coordinates_two FOREIGN KEY (seq_two) REFERENCES locations (seq) ON DELETE RESTRICT ON UPDATE RESTRICT
);