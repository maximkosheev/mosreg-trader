create table filters (
  id identity not null primary key,
  name varchar(255),
  options varchar(255),
  type integer
);

create table trades (
  id identity not null primary key,
  trade_id bigint not null,
  activate_time bigint not null,
  min_val decimal(11,2) not null,
  start_price decimal(11,2) not null,
  user_id bigint
);

create table users (
  id identity not null primary key,
  name varchar(50) not null,
  nds integer,
  cert_hash varchar(255),
  cert_name varchar(255),
  create_dt timestamp,
  email varchar(50) not null,
  firstname varchar(20) not null,
  lastname varchar(20) not null,
  fathername varchar(20) not null,
  fax varchar(25),
  phone varchar(25) not null,
  use_nds boolean not null
);

alter table users add constraint uk_user_name unique (name);

alter table trades add constraint fk_users_trades foreign key (user_id) references users;

