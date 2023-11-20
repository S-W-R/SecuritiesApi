create table if not exists securities
(
    --id uuid default gen_random_uuid() primary key,
    id int primary key, -- id передается, поэтому не генерируем на своей стороне
    sec_id varchar unique,
    sec_name varchar not null,
    regnumber varchar not null,
    emitent_title varchar not null
);
create table if not exists history_records
(
    sec_id varchar not null references securities(sec_id),
    trade_date date not null,
    num_trades decimal not null,
    open_price decimal,
    close_price decimal,
    primary key (sec_id, trade_date)
);