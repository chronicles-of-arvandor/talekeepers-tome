create table tt_character(
    id varchar(36) primary key,
    version int not null,
    rpkit_id int not null unique key auto_increment,
    profile_id int not null,
    minecraft_profile_id int null,
    name varchar(256) not null,
    ancestry_id varchar(36) not null,
    sub_ancestry_id varchar(36) not null,
    first_class_id varchar(36) not null,
    background_id varchar(36) not null,
    alignment varchar(16) not null,
    hp int not null,
    temp_hp int not null,
    experience int not null,
    description varchar(4096) not null,
    height double not null,
    weight double not null,
    dead boolean not null,
    world_id varchar(36) not null,
    x double not null,
    y double not null,
    z double not null,
    yaw real not null,
    pitch real not null,
    inventory_contents longblob not null,
    health double not null,
    food_level int not null,
    exhaustion real not null,
    saturation real not null,
    profile_hidden boolean not null,
    name_hidden boolean not null,
    age_hidden boolean not null,
    ancestry_hidden boolean not null,
    description_hidden boolean not null,
    height_hidden boolean not null,
    weight_hidden boolean not null
);

create table tt_pronoun_set(
    id varchar(36) primary key,
    name varchar(64) not null,
    created_by varchar(36),
    subject varchar(32) not null,
    object varchar(32) not null,
    dependent_possessive varchar(32) not null,
    independent_possessive varchar(32) not null,
    reflexive varchar(32) not null
);

create table tt_character_pronouns(
    character_id varchar(36) not null,
    pronoun_set_id varchar(36) not null,
    weight int not null,
    primary key(character_id, pronoun_set_id),
    constraint fk_character_pronouns_character_id foreign key(character_id) references tt_character(id) on delete cascade,
    constraint fk_character_pronouns_pronoun_set_id foreign key(pronoun_set_id) references tt_pronoun_set(id)
);

create table tt_character_class(
    character_id varchar(36) not null,
    class_id varchar(36) not null,
    level int not null,
    subclass_id varchar(36),
    primary key(character_id, class_id),
    constraint fk_character_class_character_id foreign key(character_id) references tt_character(id) on delete cascade
);

create table tt_character_ability_score(
    character_id varchar(36) not null,
    ability varchar(12) not null,
    score int not null,
    primary key(character_id, ability),
    constraint fk_character_ability_score_character_id foreign key(character_id) references tt_character(id) on delete cascade
);

create table tt_character_temp_ability_score(
    character_id varchar(36) not null,
    ability varchar(12) not null,
    score int not null,
    primary key(character_id, ability),
    constraint fk_character_temp_ability_score_character_id foreign key(character_id) references tt_character(id) on delete cascade
);

create table tt_character_choice_option(
    character_id varchar(36) not null,
    choice_id varchar(36) not null,
    option_id varchar(36) not null,
    primary key(character_id, choice_id),
    constraint fk_character_choice_option_character_id foreign key(character_id) references tt_character(id) on delete cascade
);

create table tt_character_creation_context(
    id varchar(36) primary key,
    version int not null,
    profile_id int not null,
    minecraft_profile_id int not null,
    name varchar(256) not null,
    ancestry_id varchar(36),
    sub_ancestry_id varchar(36),
    first_class_id varchar(36),
    background_id varchar(36),
    alignment varchar(16),
    experience int,
    description varchar(4096) not null,
    height double,
    weight double,
    profile_hidden boolean not null,
    name_hidden boolean not null,
    age_hidden boolean not null,
    ancestry_hidden boolean not null,
    description_hidden boolean not null,
    height_hidden boolean not null,
    weight_hidden boolean not null
);

create table tt_character_creation_context_pronoun_set(
    character_creation_context_id varchar(36) not null,
    pronoun_set_id varchar(36) not null,
    weight int not null,
    primary key(character_creation_context_id, pronoun_set_id),
    constraint fk_character_creation_context_pronouns_ctx_id foreign key(character_creation_context_id) references tt_character_creation_context(id) on delete cascade,
    constraint fk_character_creation_context_pronouns_pronoun_set_id foreign key(pronoun_set_id) references tt_pronoun_set(id)
);

create table tt_character_creation_context_class(
    character_creation_context_id varchar(36) not null,
    class_id varchar(36) not null,
    level int not null,
    subclass_id varchar(36),
    primary key(character_creation_context_id, class_id),
    constraint fk_character_creation_context_class_ctx_id foreign key(character_creation_context_id) references tt_character_creation_context(id) on delete cascade
);

create table tt_character_creation_context_ability_score_choice(
    character_creation_context_id varchar(36) not null,
    ability varchar(12) not null,
    choice int not null,
    primary key(character_creation_context_id, ability),
    constraint fk_character_creation_context_ability_ctx_id foreign key (character_creation_context_id) references tt_character_creation_context(id) on delete cascade
);