#!/usr/bin/env python3

from __future__ import annotations

import pathlib
import shutil
import sqlite3
import struct
import sys


def shadow_packet(model: bytes, index: bytes, metadata: bytes) -> bytes:
    return len(model).to_bytes(3, "big") + struct.pack(">H", len(index)) + model + index + metadata


def prepare_shadow(output: pathlib.Path) -> None:
    output.mkdir(parents=True, exist_ok=True)
    model = struct.pack(">6I", 4, 1, 4, 0, 0, 1)
    index_overflow = struct.pack(">3I", 0, 0x40000000, 0)
    empty_index = struct.pack(">3I", 0, 0, 0)
    invalid_tombstone_count = struct.pack(">3I", 0, 0, 0x3B3A)
    valid_index = struct.pack(">4I4f", 0, 1, 0, 1, 0.0, 0.0, 0.0, 0.0)
    truncated_real_metadata = struct.pack(">2I", 8, 1)
    valid_real_metadata = truncated_real_metadata + struct.pack(">d", 1.0)
    quantized_model = (
        struct.pack(">6I", 4, 1, 4, 1, 1, 1)
        + bytes(1 * 4 * 256 * 4)
        + bytes(1 * 4 * 4)
    )
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
    (output / "quantized-truncated-base").write_bytes(
        shadow_packet(quantized_model, b"", b"\x00")
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


def prepare_vec1_campaigns(output: pathlib.Path) -> None:
    lifecycle = output / "lifecycle"
    metadata = output / "metadata"
    training = output / "training"
    distance = output / "distance"
    for directory in (lifecycle, metadata, training, distance):
        directory.mkdir(parents=True, exist_ok=True)

    (lifecycle / "transaction-rebuild-reconnect").write_bytes(
        bytes([0, 0, 0, 6, 2, 0, 0, 1, 0, 9, 0, 6, 0, 1, 0, 3, 0, 4, 1, 13, 1, 10])
    )
    (lifecycle / "savepoint-rowid-boundaries").write_bytes(
        bytes([0, 1, 0, 6, 0, 0, 0, 1, 0, 6, 12, 0, 0, 3, 0, 2, 0, 12])
    )

    (metadata / "null-nul-and-numeric-boundaries").write_bytes(
        bytes([4, 0, 3, 3, 0, 65, 1])
        + struct.pack("<Bq", 1, 2**53 + 1)
        + struct.pack("<Bd", 2, float(2**53))
        + bytes([0, 3, 3, 0, 65, 4, 0])
    )
    (metadata / "signed-zero-subnormal-in").write_bytes(
        bytes([3])
        + struct.pack("<Bd", 2, -0.0)
        + struct.pack("<BQ", 2, 1)
        + bytes([4, 2, 2])
        + struct.pack("<d", 0.0)
        + bytes([2])
        + struct.pack("<Q", 0x8000000000000000)
    )
    (metadata / "no-index-repeated-real-in").write_bytes(bytes([2]))
    (metadata / "empty-value-filter").write_bytes(
        bytes([
            0x3E, 0x04, 0x00, 0x03, 0x03, 0x00, 0x41, 0x01,
            0x01, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x20,
            0x00, 0x02, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x40, 0x43, 0x00, 0x03, 0x03, 0x00, 0x41, 0x04,
            0x00,
        ])
    )
    (metadata / "nul-text-in-no-index-filter").write_bytes(
        bytes([
            0x04, 0x00, 0x03, 0x03, 0x00, 0x41, 0x01, 0x01,
            0x01, 0x00, 0x00, 0x21, 0x00, 0x00, 0x00, 0x20,
            0x00, 0x02, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x40, 0x43, 0x00, 0x03, 0x03, 0x00, 0x41, 0x00,
        ])
    )
    (metadata / "mixed-type-integer-text-ordering").write_bytes(
        bytes([
            0x3E, 0x10, 0x00, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF,
            0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF,
            0x03, 0x03, 0x09, 0x00, 0x00, 0x41, 0x00, 0x05,
            0x01, 0x41, 0x01, 0x01, 0x41, 0x00, 0x02, 0x00,
            0x00, 0x02, 0x58, 0x00,
        ])
    )
    (metadata / "integer-real-precision-in").write_bytes(
        bytes.fromhex(
            "04 00 03 03 00 41 01 01 01 00 00 00 00 00 20 00 "
            "02 00 00 00 00 00 00 40 43 00 03 03 00 45 04 00"
        )
    )

    (training / "identical-zero-vectors").write_bytes(bytes(256))
    (training / "rank-one-padded-codebooks").write_bytes(
        bytes([15, 16, 1, 1, 1, 3]) + struct.pack("<32f", *[float(index % 4) for index in range(32)])
    )

    def distance_packet(values: list[int]) -> bytes:
        dimensions = len(values) // 2
        return bytes([dimensions - 1]) + b"".join(struct.pack("<I", value) for value in values)

    (distance / "lane-boundary-extrema").write_bytes(
        distance_packet([
            0x7F7FFFFF, 0xFF7FFFFF, 0x00000001, 0x80000001,
            0x00000000, 0x80000000, 0x3F800000, 0xBF800000,
            0xFF7FFFFF, 0x7F7FFFFF, 0x80000001, 0x00000001,
            0x80000000, 0x00000000, 0xBF800000, 0x3F800000,
        ])
    )
    (distance / "scalar-tail-near-tie").write_bytes(
        distance_packet([
            0x3F800000, 0x3F800001, 0x3F7FFFFF, 0x00800000, 0x00000001,
            0x3F800001, 0x3F800000, 0x3F800000, 0x00800001, 0x00000002,
        ])
    )


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
    prepare_vec1_campaigns(output)


if __name__ == "__main__":
    main()
