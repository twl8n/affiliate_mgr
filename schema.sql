

create table entry (
        id integer primary key autoincrement,
        title text,
        desc text,
        stars integer,
        isbn text
);


-- For now, only a single file name. Alt sizes use the same base name, with some suffix convention.

create table image (
        id integer primary key autoincrement,
        entry_id integer, -- fk to entry.id
        title text,
        caption text,
        file text
);
