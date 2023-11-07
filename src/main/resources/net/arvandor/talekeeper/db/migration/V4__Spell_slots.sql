create table tt_character_spell_slots(
    character_id varchar(36),
    spell_slot_level int,
    used_spell_slots int,
    PRIMARY KEY(character_id, spell_slot_level),
    constraint fk_character_spell_slots_character_id foreign key(character_id) references tt_character(id) on delete cascade
);