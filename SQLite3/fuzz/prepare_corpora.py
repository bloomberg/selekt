#!/usr/bin/env python3

from __future__ import annotations

import pathlib
import shutil
import sqlite3
import struct
import sys


def shadow_packet(model: bytes, index: bytes, metadata: bytes) -> bytes:
    return struct.pack(">HH", len(model), len(index)) + model + index + metadata


def prepare_shadow(output: pathlib.Path) -> None:
    output.mkdir(parents=True, exist_ok=True)
    model = struct.pack(">6I", 4, 1, 4, 0, 0, 1)
    index_overflow = struct.pack(">3I", 0, 0x40000000, 0)
    empty_index = struct.pack(">3I", 0, 0, 0)
    invalid_tombstone_count = struct.pack(">3I", 0, 0, 0x3B3A)
    valid_index = struct.pack(">4I4f", 0, 1, 0, 1, 0.0, 0.0, 0.0, 0.0)
    truncated_real_metadata = struct.pack(">2I", 8, 1)
    valid_real_metadata = truncated_real_metadata + struct.pack(">d", 1.0)
    (output / "index-overflow").write_bytes(shadow_packet(model, index_overflow, b""))
    (output / "empty-index").write_bytes(shadow_packet(b"", empty_index, b""))
    (output / "tombstones-exceed-entry-count").write_bytes(
        shadow_packet(b"", invalid_tombstone_count, b"")
    )
    (output / "truncated-real-metadata").write_bytes(
        shadow_packet(model, valid_index, truncated_real_metadata)
    )
    (output / "valid-index-and-metadata").write_bytes(
        shadow_packet(model, valid_index, valid_real_metadata)
    )
    (output / "empty-components").write_bytes(shadow_packet(b"", b"", b""))


def prepare_database(output: pathlib.Path) -> None:
    output.mkdir(parents=True, exist_ok=True)
    empty = output / "empty.db"
    populated = output / "populated.db"
    for path in (empty, populated):
        path.unlink(missing_ok=True)
    sqlite3.connect(empty).close()
    with sqlite3.connect(populated) as database:
        database.execute("PRAGMA page_size=512")
        database.execute("CREATE TABLE values_table(id INTEGER PRIMARY KEY, value TEXT, payload BLOB)")
        database.execute("CREATE INDEX values_index ON values_table(value)")
        database.executemany(
            "INSERT INTO values_table(value, payload) VALUES(?, ?)",
            [("alpha", bytes(range(32))), ("βeta", bytes(64)), (None, b"")],
        )
        database.execute("CREATE TABLE without_rowid(key TEXT PRIMARY KEY, value INTEGER) WITHOUT ROWID")
        database.execute("INSERT INTO without_rowid VALUES('key', 42)")
        database.commit()
        database.execute("VACUUM")


def main() -> None:
    if len(sys.argv) != 3:
        raise SystemExit("usage: prepare_corpora.py SOURCE_DIR OUTPUT_DIR")
    source = pathlib.Path(sys.argv[1]).resolve()
    output = pathlib.Path(sys.argv[2]).resolve()
    shutil.rmtree(output, ignore_errors=True)
    shutil.copytree(source / "corpora" / "sql", output / "sql")
    shutil.copytree(source / "corpora" / "scalar", output / "scalar")
    prepare_shadow(output / "shadow")
    prepare_database(output / "database")


if __name__ == "__main__":
    main()
