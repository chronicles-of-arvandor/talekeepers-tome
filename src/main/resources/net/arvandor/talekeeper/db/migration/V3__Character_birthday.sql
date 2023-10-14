alter table tt_character_creation_context
    add birthday_year int null;

alter table tt_character_creation_context
    add birthday_day int null;

alter table tt_character
    add birthday_year int default 1322 not null;

alter table tt_character
    add birthday_day int default 1 not null;
