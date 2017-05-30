

create table entry (
        id integer primary key autoincrement,
        title text,
        desc text,
        stars integer,
        isbn text
);

-- Keyword tags for entries. 
create table tag (
        id integer primary key autoincrement,
        name text,
        desc text
);

-- many to many between entry and tag. Each entry can have multiple tags. Each tag may be used on multiple entries.
create table etotag (
        eid integer, -- fk to entry.id
        tid integer  -- fk to tag.id
);


-- For now, only a single file name. Alt sizes use the same base name, with some suffix convention.

create table image (
        id integer primary key autoincrement,
        entry_id integer, -- fk to entry.id
        title text,
        caption text,
        file text
);

-- What is this? Found it in the db, but it didn't exist in the schema file?
-- CREATE TABLE news (date text, url text, title text, body text);
