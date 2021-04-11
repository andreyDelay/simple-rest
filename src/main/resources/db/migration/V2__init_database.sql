/*use rest;*/

CREATE TABLE IF NOT EXISTS users(
    user_id integer not null auto_increment,
    name varchar(32) not null,
    surname varchar(32) not null,
    age integer not null,
    registration DATETIME DEFAULT CURRENT_TIMESTAMP,
    primary key(user_id)
) engine = innoDB;

CREATE TABLE IF NOT EXISTS events(
    event_id integer not null auto_increment,
    event_name varchar(255) not null,
    event_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    user_id integer not null,
    CONSTRAINT fk_ue_user_id FOREIGN KEY (user_id) REFERENCES users(user_id),
    primary key(event_id)
) engine = innoDB;

CREATE TABLE IF NOT EXISTS accounts(
    account_id integer not null auto_increment,
    account_name varchar(255) not null,
    account_status varchar(32) default 'ACTIVE' not null,
    user_id integer not null,
    CONSTRAINT fk_account_user_id FOREIGN KEY(user_id) REFERENCES users(user_id),
    primary key(account_id)
) engine = innoDB;

CREATE TABLE IF NOT EXISTS files(
    file_id integer not null auto_increment,
    filename varchar(512) not null,
    file_type varchar(256) not null default 'unknown',
    size bigint unsigned,
    user_id integer not null,
    file_status varchar(32),
    file_path varchar(512) not null,
    CONSTRAINT fk_files_user_id FOREIGN KEY (user_id) REFERENCES users(user_id),
    primary key(file_id)
) engine = innoDB;