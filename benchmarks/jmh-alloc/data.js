window.BENCHMARK_DATA = {
  "lastUpdate": 1790394199178,
  "repoUrl": "https://github.com/bloomberg/selekt",
  "entries": {
    "JDBC Allocations": [
      {
        "commit": {
          "author": {
            "name": "Ken Shackleton",
            "username": "kennethshackleton",
            "email": "kennethshackleton@users.noreply.github.com"
          },
          "committer": {
            "name": "GitHub",
            "username": "web-flow",
            "email": "noreply@github.com"
          },
          "id": "a60c181982033afe924595cee781feaf47282782",
          "message": "Merge pull request #914 from kennethshackleton/alloc-benchmarks\n\nAllocation benchmarks",
          "timestamp": "2026-05-08T12:49:30Z",
          "url": "https://github.com/bloomberg/selekt/commit/a60c181982033afe924595cee781feaf47282782"
        },
        "date": 1778245452425,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": null,
            "unit": null,
            "extra": "iterations: undefined\nforks: undefined\nthreads: undefined"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": null,
            "unit": null,
            "extra": "iterations: undefined\nforks: undefined\nthreads: undefined"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": null,
            "unit": null,
            "extra": "iterations: undefined\nforks: undefined\nthreads: undefined"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": null,
            "unit": null,
            "extra": "iterations: undefined\nforks: undefined\nthreads: undefined"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": null,
            "unit": null,
            "extra": "iterations: undefined\nforks: undefined\nthreads: undefined"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": null,
            "unit": null,
            "extra": "iterations: undefined\nforks: undefined\nthreads: undefined"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": null,
            "unit": null,
            "extra": "iterations: undefined\nforks: undefined\nthreads: undefined"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": null,
            "unit": null,
            "extra": "iterations: undefined\nforks: undefined\nthreads: undefined"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": null,
            "unit": null,
            "extra": "iterations: undefined\nforks: undefined\nthreads: undefined"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": null,
            "unit": null,
            "extra": "iterations: undefined\nforks: undefined\nthreads: undefined"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": null,
            "unit": null,
            "extra": "iterations: undefined\nforks: undefined\nthreads: undefined"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": null,
            "unit": null,
            "extra": "iterations: undefined\nforks: undefined\nthreads: undefined"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "name": "Ken Shackleton",
            "username": "kennethshackleton",
            "email": "kennethshackleton@users.noreply.github.com"
          },
          "committer": {
            "name": "GitHub",
            "username": "web-flow",
            "email": "noreply@github.com"
          },
          "id": "cc6a0a545222c011a1d3cc2aaa61bd95c95e2665",
          "message": "Merge pull request #921 from kennethshackleton/fix-benchmakrs-pin-action\n\nFix benchmarks action by pinning to 1.20.4",
          "timestamp": "2026-05-08T20:47:38Z",
          "url": "https://github.com/bloomberg/selekt/commit/cc6a0a545222c011a1d3cc2aaa61bd95c95e2665"
        },
        "date": 1778274130679,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 54510.98407211654,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 54517.1008944729,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 66623.97462120505,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 54472.187218204235,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 54485.77148610338,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 66590.7168494256,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 101722.27523726474,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 101749.19981366876,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 113654.58600632919,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 68511.35696879159,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 68536.13026249569,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 80503.0297846072,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "name": "Ken Shackleton",
            "username": "kennethshackleton",
            "email": "kennethshackleton@users.noreply.github.com"
          },
          "committer": {
            "name": "GitHub",
            "username": "web-flow",
            "email": "noreply@github.com"
          },
          "id": "e4b1b4190e6be1053fed2ccaf853f7824895ffdb",
          "message": "Merge pull request #923 from kennethshackleton/full-integrity-check\n\nPerform a full integrity check",
          "timestamp": "2026-05-08T21:42:29Z",
          "url": "https://github.com/bloomberg/selekt/commit/e4b1b4190e6be1053fed2ccaf853f7824895ffdb"
        },
        "date": 1778294373220,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 54505.49839439594,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 54517.20139892766,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 66626.45272416284,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 54473.57370786583,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 54486.052225173844,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 66598.4431018846,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 101722.33619173888,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 101746.03407043483,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 113652.96287651904,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 68511.2195483609,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 68537.73494530938,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 80502.83792142912,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "name": "Ken Shackleton",
            "username": "kennethshackleton",
            "email": "kennethshackleton@users.noreply.github.com"
          },
          "committer": {
            "name": "GitHub",
            "username": "web-flow",
            "email": "noreply@github.com"
          },
          "id": "98c9b551e4d66796e63e46ada86081b7c3a83a98",
          "message": "Merge pull request #924 from kennethshackleton/slab-if-ascii\n\nAllocate from slab arena if text is ASCII",
          "timestamp": "2026-05-09T03:22:56Z",
          "url": "https://github.com/bloomberg/selekt/commit/98c9b551e4d66796e63e46ada86081b7c3a83a98"
        },
        "date": 1778297777362,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 30504.529363422313,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 30520.02456928517,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 42540.833862433865,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 30472.29034089464,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 30489.539055460406,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 42482.81594504003,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 101711.13334422736,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 101728.1374712501,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 119880.44025250287,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 68498.19060139268,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 68516.23954906265,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 86790.6016649426,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "name": "Ken Shackleton",
            "username": "kennethshackleton",
            "email": "kennethshackleton@users.noreply.github.com"
          },
          "committer": {
            "name": "GitHub",
            "username": "web-flow",
            "email": "noreply@github.com"
          },
          "id": "98c9b551e4d66796e63e46ada86081b7c3a83a98",
          "message": "Merge pull request #924 from kennethshackleton/slab-if-ascii\n\nAllocate from slab arena if text is ASCII",
          "timestamp": "2026-05-09T03:22:56Z",
          "url": "https://github.com/bloomberg/selekt/commit/98c9b551e4d66796e63e46ada86081b7c3a83a98"
        },
        "date": 1778300886208,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 30499.60155614573,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 30516.238521212334,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 35424.341763063436,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 30467.6449445323,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 30481.87234596443,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 35401.8893257406,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 101717.07697501234,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 101739.09187868844,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 113662.12949158638,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 68503.68818221019,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 68525.94014925028,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 80492.12230886993,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "name": "Ken Shackleton",
            "username": "kennethshackleton",
            "email": "kennethshackleton@users.noreply.github.com"
          },
          "committer": {
            "name": "GitHub",
            "username": "web-flow",
            "email": "noreply@github.com"
          },
          "id": "821372df796a817e3e12f5daeaa33e09345348e5",
          "message": "Merge pull request #925 from kennethshackleton/tidy-benchmarks\n\nOrganise the benchmarks page",
          "timestamp": "2026-05-09T07:10:15Z",
          "url": "https://github.com/bloomberg/selekt/commit/821372df796a817e3e12f5daeaa33e09345348e5"
        },
        "date": 1778311800262,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 30503.558872602658,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 30516.61578652278,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 35419.75135646943,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 30474.535347223107,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 30483.061047168838,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 35406.06158634604,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 101725.1575371127,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 101746.87037773963,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 113659.34131304765,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 68515.85369037456,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 68540.20624964067,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 80507.12172751893,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "name": "Ken Shackleton",
            "username": "kennethshackleton",
            "email": "kennethshackleton@users.noreply.github.com"
          },
          "committer": {
            "name": "GitHub",
            "username": "web-flow",
            "email": "noreply@github.com"
          },
          "id": "c11c52315d595c924c50ceade6f4b2b0a36e3460",
          "message": "Merge pull request #930 from kennethshackleton/primitive-bindings\n\nBind as primitives to save boxing",
          "timestamp": "2026-05-09T11:02:02Z",
          "url": "https://github.com/bloomberg/selekt/commit/c11c52315d595c924c50ceade6f4b2b0a36e3460"
        },
        "date": 1778325367782,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 577.0102593845147,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 606.6453223871648,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 5867.6719541538105,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 546.1640536324272,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 562.6547119564883,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 5827.42433540596,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 101712.67063054134,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 101733.46458040192,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 113658.15193347292,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 68500.92118555165,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 68520.96068969001,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 80501.87344451934,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "name": "Ken Shackleton",
            "username": "kennethshackleton",
            "email": "kennethshackleton@users.noreply.github.com"
          },
          "committer": {
            "name": "GitHub",
            "username": "web-flow",
            "email": "noreply@github.com"
          },
          "id": "94f19a3af3387013dd995be02a0903daf1809e3f",
          "message": "Merge pull request #934 from kennethshackleton/jdbc-guide-formatting\n\nLine-breaks and formatting in JDBC getting started",
          "timestamp": "2026-05-09T13:56:53Z",
          "url": "https://github.com/bloomberg/selekt/commit/94f19a3af3387013dd995be02a0903daf1809e3f"
        },
        "date": 1778336006381,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 580.5030526852922,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 622.693093825751,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 5864.746933145901,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 550.4335179999287,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 581.8660868107497,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 5821.054133594892,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 101719.54213090679,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 101741.65555709504,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 113669.6497870723,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 68509.25373460475,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 68532.62910036533,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 80507.78151754946,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "name": "Ken Shackleton",
            "username": "kennethshackleton",
            "email": "kennethshackleton@users.noreply.github.com"
          },
          "committer": {
            "name": "GitHub",
            "username": "web-flow",
            "email": "noreply@github.com"
          },
          "id": "6d34600cc9acbe90869ceda582ccbc02c498cafe",
          "message": "Merge pull request #935 from kennethshackleton/memory-segment-global\n\nUse static memory segment when getting confined Strings",
          "timestamp": "2026-05-09T16:29:44Z",
          "url": "https://github.com/bloomberg/selekt/commit/6d34600cc9acbe90869ceda582ccbc02c498cafe"
        },
        "date": 1778345050263,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 583.8262251891908,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 598.3734117772658,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 5866.994525996752,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 572.711466237918,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 565.9787309627759,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 5822.173639843585,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 101719.5314219357,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 101741.66629008442,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 113658.98412360584,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 68509.0061737426,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 68532.63360564128,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 80500.15136527334,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "name": "Ken Shackleton",
            "username": "kennethshackleton",
            "email": "kennethshackleton@users.noreply.github.com"
          },
          "committer": {
            "name": "GitHub",
            "username": "web-flow",
            "email": "noreply@github.com"
          },
          "id": "ac90e71b4ecc5fa8138b30bccf7959b3204282b7",
          "message": "Merge pull request #944 from kennethshackleton/prepare-0.33.1\n\nPrepare version 0.33.1",
          "timestamp": "2026-05-10T16:54:37Z",
          "url": "https://github.com/bloomberg/selekt/commit/ac90e71b4ecc5fa8138b30bccf7959b3204282b7"
        },
        "date": 1778432425257,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 551.0894281079535,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 570.6790908157825,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 5792.418707714523,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 519.2103752238471,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 537.9355530036125,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 5744.501763664964,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 101719.8246813403,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 101742.68193224653,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 113658.42438707661,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 68510.8570403234,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 68532.19847319702,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 80506.73583851785,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "name": "Ken Shackleton",
            "username": "kennethshackleton",
            "email": "kennethshackleton@users.noreply.github.com"
          },
          "committer": {
            "name": "GitHub",
            "username": "web-flow",
            "email": "noreply@github.com"
          },
          "id": "03159b5ff5793bb9e0e10e3e9f16e4026b51280c",
          "message": "Merge pull request #950 from bloomberg/dependabot/github_actions/github/codeql-action-4.35.4\n\nBump github/codeql-action from 4.35.3 to 4.35.4",
          "timestamp": "2026-05-11T13:41:31Z",
          "url": "https://github.com/bloomberg/selekt/commit/03159b5ff5793bb9e0e10e3e9f16e4026b51280c"
        },
        "date": 1778530857354,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 550.8630901824847,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 554.1863818020375,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 5769.311690727899,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 508.4342063057501,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 497.356873446388,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 5742.159412390671,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 101713.66810890251,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 101735.13774917563,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 113651.96759236064,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 68498.71308604997,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 68521.1830391579,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 80506.37923353483,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "name": "Ken Shackleton",
            "username": "kennethshackleton",
            "email": "kennethshackleton@users.noreply.github.com"
          },
          "committer": {
            "name": "GitHub",
            "username": "web-flow",
            "email": "noreply@github.com"
          },
          "id": "29bce224bf0de819c2878213612ac64761acb588",
          "message": "Merge pull request #951 from bloomberg/dependabot/github_actions/benchmark-action/github-action-benchmark-1.22.1\n\nBump benchmark-action/github-action-benchmark from 1.20.4 to 1.22.1",
          "timestamp": "2026-05-12T05:55:03Z",
          "url": "https://github.com/bloomberg/selekt/commit/29bce224bf0de819c2878213612ac64761acb588"
        },
        "date": 1778566132409,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 553.553605154119,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 569.216264465222,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 5784.340749570108,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 517.5725786934943,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 535.0812401590904,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 5751.420884208104,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 101721.24224254058,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 101743.69664862468,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 113657.93769328536,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 68510.5668499491,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 68535.8770576927,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 80505.00632074362,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "name": "Ken Shackleton",
            "username": "kennethshackleton",
            "email": "kennethshackleton@users.noreply.github.com"
          },
          "committer": {
            "name": "GitHub",
            "username": "web-flow",
            "email": "noreply@github.com"
          },
          "id": "e037ce8e863bb8f9a1b58ed891c2cb610f2f8e54",
          "message": "Merge pull request #955 from kennethshackleton/prepare-0.33.2\n\nPrepare version 0.33.2",
          "timestamp": "2026-05-12T19:07:00Z",
          "url": "https://github.com/bloomberg/selekt/commit/e037ce8e863bb8f9a1b58ed891c2cb610f2f8e54"
        },
        "date": 1778615205490,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 543.5470107120784,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 560.1186947992439,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 5765.706659731008,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 513.865897437311,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 531.2179335990191,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 5747.815402509041,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 101713.41232547569,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 101734.09022523982,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 113645.94068389479,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 68500.63580833745,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 68522.73045672642,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 80507.46639588856,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "name": "Ken Shackleton",
            "username": "kennethshackleton",
            "email": "kennethshackleton@users.noreply.github.com"
          },
          "committer": {
            "name": "GitHub",
            "username": "web-flow",
            "email": "noreply@github.com"
          },
          "id": "a15fddd92f9693d6e9cecc6f51761797e7d0f7fd",
          "message": "Merge pull request #956 from kennethshackleton/readme-android-rationale\n\nScope the rationale in the README to Android",
          "timestamp": "2026-05-15T08:28:52Z",
          "url": "https://github.com/bloomberg/selekt/commit/a15fddd92f9693d6e9cecc6f51761797e7d0f7fd"
        },
        "date": 1778905965832,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 545.4450884434639,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 563.2504923347382,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 5785.431046652032,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 489.34268576727874,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 528.8473756537053,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 5743.561047723213,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 101711.35392905945,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 101733.32352925018,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 113662.69238847189,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 68499.79102652373,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 68521.83234625639,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 80506.90789843543,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "name": "Ken Shackleton",
            "username": "kennethshackleton",
            "email": "kennethshackleton@users.noreply.github.com"
          },
          "committer": {
            "name": "GitHub",
            "username": "web-flow",
            "email": "noreply@github.com"
          },
          "id": "fff28c6e67d709554a724d8fb2c6527b121964dd",
          "message": "Merge pull request #960 from kennethshackleton/update-overview\n\nUpdate docs to mention JDBC",
          "timestamp": "2026-05-20T19:12:28Z",
          "url": "https://github.com/bloomberg/selekt/commit/fff28c6e67d709554a724d8fb2c6527b121964dd"
        },
        "date": 1779510966122,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 545.7567231465415,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 584.0097967285179,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 5790.927632048091,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 519.3905779807459,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 529.2024468639577,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 5741.856759910721,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 101713.95120095497,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 101734.94777890373,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 113654.89039645283,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 68499.5892226499,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 68520.99143571507,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 80508.09187686405,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "name": "Ken Shackleton",
            "username": "kennethshackleton",
            "email": "kennethshackleton@users.noreply.github.com"
          },
          "committer": {
            "name": "GitHub",
            "username": "web-flow",
            "email": "noreply@github.com"
          },
          "id": "5b545e874ad82134784738db83b46883b17c32a3",
          "message": "Merge pull request #963 from bloomberg/dependabot/github_actions/github/codeql-action-4.36.0\n\nBump github/codeql-action from 4.35.5 to 4.36.0",
          "timestamp": "2026-05-25T19:52:11Z",
          "url": "https://github.com/bloomberg/selekt/commit/5b545e874ad82134784738db83b46883b17c32a3"
        },
        "date": 1780116080106,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 547.6604028722684,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 565.6599574980851,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 5779.028502817235,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 539.3962415465151,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 533.4631880993164,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 5748.695855761977,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 101719.99460201789,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 101743.40032208385,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 113662.90724171742,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 68510.2616095905,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 68536.60226699931,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 80504.63398874244,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "name": "Ken Shackleton",
            "username": "kennethshackleton",
            "email": "kennethshackleton@users.noreply.github.com"
          },
          "committer": {
            "name": "GitHub",
            "username": "web-flow",
            "email": "noreply@github.com"
          },
          "id": "5a5d3a6ac2e03da3ba57b46b440bcd4e69642e69",
          "message": "Merge pull request #969 from kennethshackleton/prepare-0.34.1\n\nPrepare version 0.34.1",
          "timestamp": "2026-06-04T08:27:52Z",
          "url": "https://github.com/bloomberg/selekt/commit/5a5d3a6ac2e03da3ba57b46b440bcd4e69642e69"
        },
        "date": 1780720876048,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 538.7600258769289,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 563.021813635765,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 8772.100927524745,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 508.0264598248449,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 527.8411472843743,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 13392.902923006313,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 101741.7326561367,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 101743.92327271955,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 116434.42042717952,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 68499.91702579717,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 68574.85344914002,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 83755.87440886677,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "name": "Ken Shackleton",
            "username": "kennethshackleton",
            "email": "kennethshackleton@users.noreply.github.com"
          },
          "committer": {
            "name": "GitHub",
            "username": "web-flow",
            "email": "noreply@github.com"
          },
          "id": "2e3f4d6823740f43cd61c67e92ff1aa9088c519f",
          "message": "Merge pull request #972 from kennethshackleton/prepare-0.34.2\n\nPrepare version 0.34.2",
          "timestamp": "2026-06-06T14:46:34Z",
          "url": "https://github.com/bloomberg/selekt/commit/2e3f4d6823740f43cd61c67e92ff1aa9088c519f"
        },
        "date": 1780758113958,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 549.6043468469253,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 565.2243027803795,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 5769.241496809683,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 522.8782222613212,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 533.4678018231684,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 5738.134158323746,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 101721.47854747908,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 101743.24142661774,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 113653.39349071027,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 68510.84440119658,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 68537.4115273568,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 80505.13621930471,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "name": "Ken Shackleton",
            "username": "kennethshackleton",
            "email": "kennethshackleton@users.noreply.github.com"
          },
          "committer": {
            "name": "GitHub",
            "username": "web-flow",
            "email": "noreply@github.com"
          },
          "id": "35bfa21bf4ce06f3b982898b51bc8a706a1fa01c",
          "message": "Merge pull request #976 from kennethshackleton/prepare-0.34.3\n\nPrepare version 0.34.3",
          "timestamp": "2026-06-12T11:01:40Z",
          "url": "https://github.com/bloomberg/selekt/commit/35bfa21bf4ce06f3b982898b51bc8a706a1fa01c"
        },
        "date": 1781326188621,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 543.4508466843378,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 558.4082255392595,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 5786.504179109421,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 512.8164272299268,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 529.2574189382478,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 5741.307924210908,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 101713.99657862753,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 101735.09549862596,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 113653.50458155992,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 68502.08266149822,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 68524.97660060956,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 80510.50000803209,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "name": "Ken Shackleton",
            "username": "kennethshackleton",
            "email": "kennethshackleton@users.noreply.github.com"
          },
          "committer": {
            "name": "GitHub",
            "username": "web-flow",
            "email": "noreply@github.com"
          },
          "id": "db0f23db231b616e8c7f249b118fe2499ebf3a6f",
          "message": "Merge pull request #974 from bloomberg/dependabot/github_actions/github/codeql-action-4.36.2\n\nBump github/codeql-action from 4.36.0 to 4.36.2",
          "timestamp": "2026-06-19T20:13:24Z",
          "url": "https://github.com/bloomberg/selekt/commit/db0f23db231b616e8c7f249b118fe2499ebf3a6f"
        },
        "date": 1781930924534,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 575.1680341448078,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 558.6056340334691,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 5828.685371713213,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 509.4226240637994,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 515.034708254905,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 5776.98981997613,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 101713.35596572865,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 101734.49921211267,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 113665.10108187674,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 68500.43857759022,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 68523.2591553758,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 80514.314856688,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "name": "Ken Shackleton",
            "username": "kennethshackleton",
            "email": "kennethshackleton@users.noreply.github.com"
          },
          "committer": {
            "name": "GitHub",
            "username": "web-flow",
            "email": "noreply@github.com"
          },
          "id": "830eaeee445661c9175737ce4cdb314611678dde",
          "message": "Merge pull request #993 from kennethshackleton/simplify-project-group\n\nMove group declaration to gradle.properties",
          "timestamp": "2026-06-20T05:45:30Z",
          "url": "https://github.com/bloomberg/selekt/commit/830eaeee445661c9175737ce4cdb314611678dde"
        },
        "date": 1781990356846,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 579.6724197307265,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 544.8882862650308,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 5832.859040889674,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 474.5911482394934,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 530.5639260066237,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 5773.141366200994,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 101720.52470638682,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 101743.57251917839,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 113662.69899351895,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 68509.35116303948,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 68534.01585369589,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 80533.84373920606,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "name": "Ken Shackleton",
            "username": "kennethshackleton",
            "email": "kennethshackleton@users.noreply.github.com"
          },
          "committer": {
            "name": "GitHub",
            "username": "web-flow",
            "email": "noreply@github.com"
          },
          "id": "2c41108e78f59db9110f9a3a940c92d0b0f4a0d9",
          "message": "Merge pull request #997 from kennethshackleton/gradle-9.6.0\n\nGradle 9.6.0",
          "timestamp": "2026-06-26T11:08:34Z",
          "url": "https://github.com/bloomberg/selekt/commit/2c41108e78f59db9110f9a3a940c92d0b0f4a0d9"
        },
        "date": 1782535268880,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 555.8465784154034,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 565.8284020255687,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 5827.104203762495,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 518.9982893985225,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 554.3177175577518,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 5747.417739108432,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 101721.70972751394,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 101744.6217330699,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 113668.43484608209,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 68510.7560961955,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 68535.73142947633,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 80518.13794529275,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "name": "Ken Shackleton",
            "username": "kennethshackleton",
            "email": "kennethshackleton@users.noreply.github.com"
          },
          "committer": {
            "name": "GitHub",
            "username": "web-flow",
            "email": "noreply@github.com"
          },
          "id": "af9efac7088f37018cce067c41ef43a1c8a543ec",
          "message": "Merge pull request #998 from bloomberg/dependabot/github_actions/actions/cache-6\n\nBump actions/cache from 5 to 6",
          "timestamp": "2026-07-03T11:04:14Z",
          "url": "https://github.com/bloomberg/selekt/commit/af9efac7088f37018cce067c41ef43a1c8a543ec"
        },
        "date": 1783139602474,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 552.5801285929795,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 589.0157472953878,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 5810.507304796658,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 519.4147599572573,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 534.3542720697202,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 5786.681858937783,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 101722.0694057315,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 101746.65875103953,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 113660.51852667905,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 68511.46442001851,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 68536.93884036019,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 80512.85945272392,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "name": "Ken Shackleton",
            "username": "kennethshackleton",
            "email": "kennethshackleton@users.noreply.github.com"
          },
          "committer": {
            "name": "GitHub",
            "username": "web-flow",
            "email": "noreply@github.com"
          },
          "id": "2a42b1413103cd842f06195772b3670e8accb900",
          "message": "Merge pull request #1003 from kennethshackleton/prepare-0.35.3\n\nPrepare version 0.35.3",
          "timestamp": "2026-07-07T11:26:52Z",
          "url": "https://github.com/bloomberg/selekt/commit/2a42b1413103cd842f06195772b3670e8accb900"
        },
        "date": 1783424548734,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 548.6791415560996,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 562.5691714023981,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 5824.006892645202,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 520.0637862902438,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 531.8953740270872,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 5772.219876758036,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 101720.64201161769,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 101743.39543583084,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 113666.35041679484,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 68511.06425402651,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 68536.21265710176,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 80511.75665174909,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "name": "Ken Shackleton",
            "username": "kennethshackleton",
            "email": "kennethshackleton@users.noreply.github.com"
          },
          "committer": {
            "name": "GitHub",
            "username": "web-flow",
            "email": "noreply@github.com"
          },
          "id": "da220fe24e836baf8dc9c93537622fa95fc6bc19",
          "message": "Merge pull request #1008 from kennethshackleton/prepare-0.35.4\n\nPrepare version 0.35.4",
          "timestamp": "2026-07-10T18:50:10Z",
          "url": "https://github.com/bloomberg/selekt/commit/da220fe24e836baf8dc9c93537622fa95fc6bc19"
        },
        "date": 1783743586675,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 554.6104153234336,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 591.5653129511489,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 5802.423524745458,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 518.5098837673484,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 533.5732418386704,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 5775.12766135619,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 101735.36931584956,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 101761.1342516683,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 113665.40318573434,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 68524.02870375388,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 68556.89085701379,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 80514.83031425136,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "name": "Ken Shackleton",
            "username": "kennethshackleton",
            "email": "kennethshackleton@users.noreply.github.com"
          },
          "committer": {
            "name": "GitHub",
            "username": "web-flow",
            "email": "noreply@github.com"
          },
          "id": "c94a10e114a8f9506eebee15c10823ee9b5e12c1",
          "message": "Merge pull request #1009 from bloomberg/dependabot/github_actions/github/codeql-action/upload-sarif-4.37.0\n\nBump github/codeql-action/upload-sarif from 4.36.3 to 4.37.0",
          "timestamp": "2026-07-13T07:41:29Z",
          "url": "https://github.com/bloomberg/selekt/commit/c94a10e114a8f9506eebee15c10823ee9b5e12c1"
        },
        "date": 1784348176645,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 543.8968777340788,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 566.9114246981908,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 11130.335476216369,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 511.08290946105797,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 493.17328106010916,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 13028.037602939976,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 101755.16687939032,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 101755.40739578506,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 117952.36918005967,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 68543.81425344458,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 68580.5323290869,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 84989.80892125312,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "name": "Ken Shackleton",
            "username": "kennethshackleton",
            "email": "kennethshackleton@users.noreply.github.com"
          },
          "committer": {
            "name": "GitHub",
            "username": "web-flow",
            "email": "noreply@github.com"
          },
          "id": "1d116e45b06fa6e74a0ca677c0329e8ce51bb274",
          "message": "Merge pull request #1013 from bloomberg/dependabot/github_actions/github/codeql-action/upload-sarif-4.37.1\n\nBump github/codeql-action/upload-sarif from 4.37.0 to 4.37.1",
          "timestamp": "2026-07-20T13:44:35Z",
          "url": "https://github.com/bloomberg/selekt/commit/1d116e45b06fa6e74a0ca677c0329e8ce51bb274"
        },
        "date": 1784953166319,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 550.4310205397653,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 583.4831071486052,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 5807.682080034914,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 517.3955958552609,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 533.9306007695218,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 5777.156950375768,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 101719.32036876825,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 101741.2559629885,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 113664.0835902753,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 68508.55215763744,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 68533.29247557416,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 80511.0231456486,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "name": "Ken Shackleton",
            "username": "kennethshackleton",
            "email": "kennethshackleton@users.noreply.github.com"
          },
          "committer": {
            "name": "GitHub",
            "username": "web-flow",
            "email": "noreply@github.com"
          },
          "id": "60c4b0712c689faef578fab6367e2f75989a0906",
          "message": "Merge pull request #1015 from bloomberg/dependabot/github_actions/actions/checkout-7.0.1\n\nBump actions/checkout from 7.0.0 to 7.0.1",
          "timestamp": "2026-07-31T20:23:10Z",
          "url": "https://github.com/bloomberg/selekt/commit/60c4b0712c689faef578fab6367e2f75989a0906"
        },
        "date": 1785558356923,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 550.5710735669369,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 566.2972993348186,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 5818.4050958839125,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 517.649848311096,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 535.9495460446246,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 5769.699244902268,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 101723.53963898335,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 101747.37831562207,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 113660.3738168684,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 68512.31227085802,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 68538.4602599493,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 80511.6057949222,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "name": "Ken Shackleton",
            "username": "kennethshackleton",
            "email": "kennethshackleton@users.noreply.github.com"
          },
          "committer": {
            "name": "GitHub",
            "username": "web-flow",
            "email": "noreply@github.com"
          },
          "id": "345da154c17734cbb36c74734d35409dc2555984",
          "message": "Merge pull request #1017 from bloomberg/dependabot/github_actions/actions/stale-11\n\nBump actions/stale from 10 to 11",
          "timestamp": "2026-08-03T07:53:22Z",
          "url": "https://github.com/bloomberg/selekt/commit/345da154c17734cbb36c74734d35409dc2555984"
        },
        "date": 1786160883780,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 541.4388628294222,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 554.0124071040699,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 5824.260837182155,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 507.326726305255,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 528.5510091660683,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 5786.056860699439,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 101712.13777790523,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 101734.6107123693,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 113662.71108086283,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 68500.59008558973,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 68522.26814709461,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 80508.19257618436,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "name": "Ken Shackleton",
            "username": "kennethshackleton",
            "email": "kennethshackleton@users.noreply.github.com"
          },
          "committer": {
            "name": "GitHub",
            "username": "web-flow",
            "email": "noreply@github.com"
          },
          "id": "1b74561313a8aec387dc78f60bbe5d7ab643229a",
          "message": "Merge pull request #1034 from kennethshackleton/jdbc-blob-streaming\n\nFaciliate blob streaming via JdbcConnection",
          "timestamp": "2026-08-13T07:17:08Z",
          "url": "https://github.com/bloomberg/selekt/commit/1b74561313a8aec387dc78f60bbe5d7ab643229a"
        },
        "date": 1786764762445,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 523.2460457811293,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 555.577392111011,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 5766.225564237013,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 509.2052664723848,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 523.7046486552297,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 5775.87658731931,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 101712.11888928374,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 101733.23406557918,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 113658.7392295487,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 68500.5760069241,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 68520.23335189268,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 80501.14951423093,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "name": "Ken Shackleton",
            "username": "kennethshackleton",
            "email": "kennethshackleton@users.noreply.github.com"
          },
          "committer": {
            "name": "GitHub",
            "username": "web-flow",
            "email": "noreply@github.com"
          },
          "id": "7ed8fec7947e9577e6702da805f2f9ce10467f0b",
          "message": "Merge pull request #1043 from kennethshackleton/prepare-1.0.1\n\nPrepare version 1.0.1",
          "timestamp": "2026-08-21T19:11:33Z",
          "url": "https://github.com/bloomberg/selekt/commit/7ed8fec7947e9577e6702da805f2f9ce10467f0b"
        },
        "date": 1787369621881,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 531.887379716066,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 587.390668869975,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 5774.946676587735,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 499.85643445627386,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 553.0476600361495,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 5776.07187462941,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 101723.42911018555,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 101746.96394395661,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 113665.15488761128,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 68512.1631512724,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 68538.63276737914,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 80511.29106400767,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "name": "Ken Shackleton",
            "username": "kennethshackleton",
            "email": "kennethshackleton@users.noreply.github.com"
          },
          "committer": {
            "name": "GitHub",
            "username": "web-flow",
            "email": "noreply@github.com"
          },
          "id": "71a373c65d3a8eeb90df338f842ce32088f05d4d",
          "message": "Merge pull request #1057 from kennethshackleton/prepare-1.0.2\n\nPrepare version 1.0.2",
          "timestamp": "2026-08-27T22:44:25Z",
          "url": "https://github.com/bloomberg/selekt/commit/71a373c65d3a8eeb90df338f842ce32088f05d4d"
        },
        "date": 1787974449826,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 548.2486493653421,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 591.5387687840881,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 5814.851832097283,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 516.9801030097921,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 532.7317629356016,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 5784.162886804918,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 101722.48785430088,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 101745.31929571349,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 113661.17787843822,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 68514.4631784164,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 68536.68214092821,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 80513.58672513241,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "name": "Ken Shackleton",
            "username": "kennethshackleton",
            "email": "kennethshackleton@users.noreply.github.com"
          },
          "committer": {
            "name": "GitHub",
            "username": "web-flow",
            "email": "noreply@github.com"
          },
          "id": "498526cb7f3504e3015f0cfde1f503a369142458",
          "message": "Merge pull request #1076 from kennethshackleton/mc-publication-service\n\nMigrate publishing to Central Portal Publisher API",
          "timestamp": "2026-09-04T19:35:53Z",
          "url": "https://github.com/bloomberg/selekt/commit/498526cb7f3504e3015f0cfde1f503a369142458"
        },
        "date": 1788579211228,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 549.525625368665,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 565.4138905450725,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 5785.994258809313,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 507.7300298692201,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 534.5976634952524,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 5732.351128128641,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 101722.53714483322,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 101747.21576180946,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 113664.20683920088,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 68512.94818156933,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 68535.51990962712,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 80514.37788795735,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "name": "Ken Shackleton",
            "username": "kennethshackleton",
            "email": "kennethshackleton@users.noreply.github.com"
          },
          "committer": {
            "name": "GitHub",
            "username": "web-flow",
            "email": "noreply@github.com"
          },
          "id": "58155ef0b4ad2936ef8d484a740a1ee0a0e7b465",
          "message": "Merge pull request #1078 from kennethshackleton/prepare-1.1.0\n\nPrepare version 1.1.0",
          "timestamp": "2026-09-05T21:04:22Z",
          "url": "https://github.com/bloomberg/selekt/commit/58155ef0b4ad2936ef8d484a740a1ee0a0e7b465"
        },
        "date": 1788642676189,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 552.8767849327169,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 575.8818682478343,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 10841.371428571429,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 519.4930043573836,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 542.3624467572046,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 10852.700076394194,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 101723.83288807663,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 101749.08842644634,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 119137.11000809369,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 68516.32604957487,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 68540.81527002921,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 85925.25224040018,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektAsciiStreamASCII ( {} )",
            "value": 4392176.060300836,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektAsciiStreamLATIN1 ( {} )",
            "value": 2800175.450897266,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektAsciiStreamUTF16 ( {} )",
            "value": 2800175.547799087,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialAsciiStreamASCII ( {} )",
            "value": 10376252.763097646,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialAsciiStreamLATIN1 ( {} )",
            "value": 11584251.576565657,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialAsciiStreamUTF16 ( {} )",
            "value": 16312099.836734693,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektCharacterStreamASCII ( {} )",
            "value": 3192170.566763359,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektCharacterStreamLATIN1 ( {} )",
            "value": 3200172.2134692753,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektCharacterStreamUTF16 ( {} )",
            "value": 3200172.865726103,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialCharacterStreamASCII ( {} )",
            "value": 11184244.928524591,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialCharacterStreamLATIN1 ( {} )",
            "value": 11992254.113207549,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialCharacterStreamUTF16 ( {} )",
            "value": 14800095.476018101,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "name": "Ken Shackleton",
            "username": "kennethshackleton",
            "email": "kennethshackleton@users.noreply.github.com"
          },
          "committer": {
            "name": "GitHub",
            "username": "web-flow",
            "email": "noreply@github.com"
          },
          "id": "778a1ccaa5afae5e92a7d5298fe813874200d4c2",
          "message": "Merge pull request #1082 from kennethshackleton/fix-publication\n\nFix publication of selekt-bom",
          "timestamp": "2026-09-05T21:32:14Z",
          "url": "https://github.com/bloomberg/selekt/commit/778a1ccaa5afae5e92a7d5298fe813874200d4c2"
        },
        "date": 1788644585967,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 524.9435585818077,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 556.4007785165459,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 5777.759964068069,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 511.91406233987857,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 526.5308695031783,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 5772.660784804035,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 101713.5951554522,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 101735.941936834,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 113662.82499811988,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 68502.10378728788,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 68524.6536598502,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 80515.02196690769,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektAsciiStreamASCII",
            "value": 2792173.98354099,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektAsciiStreamLATIN1",
            "value": 2800175.303097376,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektAsciiStreamUTF16",
            "value": 2800175.7798076193,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialAsciiStreamASCII",
            "value": 10376261.533687944,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialAsciiStreamLATIN1",
            "value": 11984270.454122622,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialAsciiStreamUTF16",
            "value": 15912109.620295983,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektCharacterStreamASCII",
            "value": 3192171.137473748,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektCharacterStreamLATIN1",
            "value": 3200174.6804151414,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektCharacterStreamUTF16",
            "value": 3200174.7499371218,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialCharacterStreamASCII",
            "value": 10384261.416666668,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialCharacterStreamLATIN1",
            "value": 12792256.956862744,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialCharacterStreamUTF16",
            "value": 15600106.709898988,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "name": "Ken Shackleton",
            "username": "kennethshackleton",
            "email": "kennethshackleton@users.noreply.github.com"
          },
          "committer": {
            "name": "GitHub",
            "username": "web-flow",
            "email": "noreply@github.com"
          },
          "id": "0afce26d4d60b948b32511ad15f1a8fd42fb6c76",
          "message": "Merge pull request #1098 from kennethshackleton/keep-progress-alive\n\nKeep progress handler callbacks alive until connection close",
          "timestamp": "2026-09-06T15:05:49Z",
          "url": "https://github.com/bloomberg/selekt/commit/0afce26d4d60b948b32511ad15f1a8fd42fb6c76"
        },
        "date": 1788707385075,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 1041.7113167844714,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 1041.2323640492966,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 6279.11137064146,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 1011.854022852491,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 1050.272367164036,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 6225.2449890158405,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 101723.2133001253,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 101746.84862964532,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 113667.96019363955,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 68512.14763110175,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 68536.15308928452,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 80511.90455528135,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektAsciiStreamASCII",
            "value": 2792267.926464386,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektAsciiStreamLATIN1",
            "value": 2800271.7983945822,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektAsciiStreamUTF16",
            "value": 2800266.938684835,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialAsciiStreamASCII",
            "value": 9976261.483687945,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialAsciiStreamLATIN1",
            "value": 11984279.28205128,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialAsciiStreamUTF16",
            "value": 16312122.806477735,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektCharacterStreamASCII",
            "value": 3192262.782735192,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektCharacterStreamLATIN1",
            "value": 3200268.2034817114,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektCharacterStreamUTF16",
            "value": 3200272.452239006,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialCharacterStreamASCII",
            "value": 11184259.853061225,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialCharacterStreamLATIN1",
            "value": 12792262.416666668,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialCharacterStreamUTF16",
            "value": 15600106.432077294,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "name": "Ken Shackleton",
            "username": "kennethshackleton",
            "email": "kennethshackleton@users.noreply.github.com"
          },
          "committer": {
            "name": "GitHub",
            "username": "web-flow",
            "email": "noreply@github.com"
          },
          "id": "1cf3a1a177dc73034359d1039af6296727d0a0ef",
          "message": "Merge pull request #1110 from kennethshackleton/revise-readme\n\nAdjust rationale wording in README",
          "timestamp": "2026-09-07T05:49:10Z",
          "url": "https://github.com/bloomberg/selekt/commit/1cf3a1a177dc73034359d1039af6296727d0a0ef"
        },
        "date": 1788765087277,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 1037.6001481472545,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 1119.0826916861333,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 12993.066079922926,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 985.9352255088783,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 1099.1292525163467,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 12456.652054306856,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 101819.38858216305,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 101827.17056992713,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 120394.21374442804,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 68609.84563096671,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 68688.79868665626,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 89405.0809526542,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektAsciiStreamASCII",
            "value": 2792252.9964321344,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektAsciiStreamLATIN1",
            "value": 2800255.599901362,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektAsciiStreamUTF16",
            "value": 2800253.967194979,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialAsciiStreamASCII",
            "value": 9976227.790695718,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialAsciiStreamLATIN1",
            "value": 11984233.152472092,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialAsciiStreamUTF16",
            "value": 17112074.90410959,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektCharacterStreamASCII",
            "value": 3192246.7611401305,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektCharacterStreamLATIN1",
            "value": 3200250.4055628437,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektCharacterStreamUTF16",
            "value": 3200253.0354350107,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialCharacterStreamASCII",
            "value": 11184227.640775213,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialCharacterStreamLATIN1",
            "value": 11992232.792603131,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialCharacterStreamUTF16",
            "value": 14800074.380921382,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "name": "Ken Shackleton",
            "username": "kennethshackleton",
            "email": "kennethshackleton@users.noreply.github.com"
          },
          "committer": {
            "name": "GitHub",
            "username": "web-flow",
            "email": "noreply@github.com"
          },
          "id": "8145126a7fc09b1c473b88e7a0ac643b510e0112",
          "message": "Merge pull request #1122 from kennethshackleton/prepare-1.2.4\n\nPrepare version 1.2.4",
          "timestamp": "2026-09-07T15:21:42Z",
          "url": "https://github.com/bloomberg/selekt/commit/8145126a7fc09b1c473b88e7a0ac643b510e0112"
        },
        "date": 1788795793925,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 1202.7876744356422,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 1258.6182464625751,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 12649.620633755449,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 1105.3643934533484,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 1155.9813261595168,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 14964.37573863172,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 101806.23171581184,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 101802.00240025837,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 124830.07637853303,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 68586.69625106727,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 68856.98554055861,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 101643.50152284197,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektAsciiStreamASCII",
            "value": 2792409.24331946,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektAsciiStreamLATIN1",
            "value": 2800410.0128883785,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektAsciiStreamUTF16",
            "value": 2800409.9327169964,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialAsciiStreamASCII",
            "value": 9976223.999863427,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialAsciiStreamLATIN1",
            "value": 11584225.752917942,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialAsciiStreamUTF16",
            "value": 16312074.10517191,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektCharacterStreamASCII",
            "value": 3192406.1734448094,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektCharacterStreamLATIN1",
            "value": 3200406.2288279375,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektCharacterStreamUTF16",
            "value": 3200407.7186756423,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialCharacterStreamASCII",
            "value": 11184223.472519714,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialCharacterStreamLATIN1",
            "value": 12792227.101965187,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialCharacterStreamUTF16",
            "value": 15600065.664757555,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "name": "Ken Shackleton",
            "username": "kennethshackleton",
            "email": "kennethshackleton@users.noreply.github.com"
          },
          "committer": {
            "name": "GitHub",
            "username": "web-flow",
            "email": "noreply@github.com"
          },
          "id": "5cd9e2cf0ef64975117a2ed8b424152b03d76a8f",
          "message": "Merge pull request #1130 from kennethshackleton/prepare-1.3.0\n\nPrepare version 1.3.0",
          "timestamp": "2026-09-07T18:13:02Z",
          "url": "https://github.com/bloomberg/selekt/commit/5cd9e2cf0ef64975117a2ed8b424152b03d76a8f"
        },
        "date": 1788806131158,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 1181.6085274875873,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 1205.0622292365297,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 6911.178590609663,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 1156.9672308316844,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 1159.2008810186398,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 6848.729588736785,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 101723.61898666677,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 101745.9722397563,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 113671.41337313876,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 68512.51149926012,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 68537.66330123853,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 80517.97066757118,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektAsciiStreamASCII",
            "value": 2792429.3230024287,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektAsciiStreamLATIN1",
            "value": 2800434.670080549,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektAsciiStreamUTF16",
            "value": 2800433.7274077646,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialAsciiStreamASCII",
            "value": 9976261.416666668,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialAsciiStreamLATIN1",
            "value": 11584270.317124734,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialAsciiStreamUTF16",
            "value": 16312125.758558556,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektCharacterStreamASCII",
            "value": 3192427.162360114,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektCharacterStreamLATIN1",
            "value": 3200433.4056945606,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektCharacterStreamUTF16",
            "value": 3200433.703103648,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialCharacterStreamASCII",
            "value": 11184262.433687944,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialCharacterStreamLATIN1",
            "value": 12792264.676040702,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialCharacterStreamUTF16",
            "value": 15600113.619047621,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "name": "Ken Shackleton",
            "username": "kennethshackleton",
            "email": "kennethshackleton@users.noreply.github.com"
          },
          "committer": {
            "name": "GitHub",
            "username": "web-flow",
            "email": "noreply@github.com"
          },
          "id": "27ec7e7ecd445f37525d6a88e31606653296f22a",
          "message": "Merge pull request #1134 from kennethshackleton/jdbc-consistent-docs\n\nMake default JDBC configurations and the documentation consistent",
          "timestamp": "2026-09-08T07:39:10Z",
          "url": "https://github.com/bloomberg/selekt/commit/27ec7e7ecd445f37525d6a88e31606653296f22a"
        },
        "date": 1788855851066,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 1146.507912751706,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 1221.274436022156,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 6895.194254199179,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 1142.2818862804552,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 1162.5282296526523,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 6855.908028834453,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 101715.00978523452,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 101736.40129770972,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 113670.3731280454,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 68504.42581342238,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 68524.70895332079,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 80516.09906119667,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektAsciiStreamASCII",
            "value": 2792429.891651651,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektAsciiStreamLATIN1",
            "value": 2800430.8942108704,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektAsciiStreamUTF16",
            "value": 2800432.389346256,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialAsciiStreamASCII",
            "value": 9976261.736231884,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialAsciiStreamLATIN1",
            "value": 11584268.52938689,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialAsciiStreamUTF16",
            "value": 15912109.626638476,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektCharacterStreamASCII",
            "value": 3192423.0415851655,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektCharacterStreamLATIN1",
            "value": 3200424.6569262603,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektCharacterStreamUTF16",
            "value": 3200426.6662789905,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialCharacterStreamASCII",
            "value": 11184249.192982456,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialCharacterStreamLATIN1",
            "value": 12792258.46302041,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialCharacterStreamUTF16",
            "value": 15600103.168177614,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "name": "Ken Shackleton",
            "username": "kennethshackleton",
            "email": "kennethshackleton@users.noreply.github.com"
          },
          "committer": {
            "name": "GitHub",
            "username": "web-flow",
            "email": "noreply@github.com"
          },
          "id": "93a1ced35a13df0c676a07ef0212911c195c4926",
          "message": "Merge pull request #1138 from kennethshackleton/fix-vec1-train-size\n\nFix vec1_train integer overflow in model sizing",
          "timestamp": "2026-09-08T10:10:35Z",
          "url": "https://github.com/bloomberg/selekt/commit/93a1ced35a13df0c676a07ef0212911c195c4926"
        },
        "date": 1788862756604,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 1190.3350097570528,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 1209.8577646338215,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 6898.783023053465,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 1134.6340658360764,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 1174.6921537103967,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 6850.190064272322,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 101742.69798894336,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 101768.13477633047,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 113661.51025913046,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 68538.28814977843,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 68560.05789789699,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 80518.06924690725,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektAsciiStreamASCII",
            "value": 2792430.212813628,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektAsciiStreamLATIN1",
            "value": 2800432.6727299253,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektAsciiStreamUTF16",
            "value": 2800432.018270337,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialAsciiStreamASCII",
            "value": 9976264.7293247,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialAsciiStreamLATIN1",
            "value": 11984276.949230766,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialAsciiStreamUTF16",
            "value": 15912118.21368421,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektCharacterStreamASCII",
            "value": 3192428.9965948323,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektCharacterStreamLATIN1",
            "value": 3200429.87096112,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektCharacterStreamUTF16",
            "value": 3200432.7180641247,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialCharacterStreamASCII",
            "value": 10384270.419571126,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialCharacterStreamLATIN1",
            "value": 12792268.5154334,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialCharacterStreamUTF16",
            "value": 14800115.857560974,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "name": "Ken Shackleton",
            "username": "kennethshackleton",
            "email": "kennethshackleton@users.noreply.github.com"
          },
          "committer": {
            "name": "GitHub",
            "username": "web-flow",
            "email": "noreply@github.com"
          },
          "id": "91644e86f231d21f71ec2ec64025412e105dad96",
          "message": "Merge pull request #1154 from kennethshackleton/remove-vec1-def\n\nRemoved the redundant Vec1Tab typedef",
          "timestamp": "2026-09-09T10:50:29Z",
          "url": "https://github.com/bloomberg/selekt/commit/91644e86f231d21f71ec2ec64025412e105dad96"
        },
        "date": 1788951567131,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 1172.8404028201974,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 1214.3492748056995,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 13249.502821441569,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 1166.6526164458078,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 1219.3070091055508,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 17918.09514173193,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 101848.61725798679,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 102049.61382729556,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 122872.4144274847,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 68608.8218427837,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 68636.80189082734,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 89141.08492492847,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektAsciiStreamASCII",
            "value": 2792436.6904268144,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektAsciiStreamLATIN1",
            "value": 2800438.268643297,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektAsciiStreamUTF16",
            "value": 2800437.721992968,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialAsciiStreamASCII",
            "value": 10376242.831854839,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialAsciiStreamLATIN1",
            "value": 11584242.110733485,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialAsciiStreamUTF16",
            "value": 15912085.787022326,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektCharacterStreamASCII",
            "value": 3192429.0851177806,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektCharacterStreamLATIN1",
            "value": 3200431.00933149,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektCharacterStreamUTF16",
            "value": 3200433.1254738,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialCharacterStreamASCII",
            "value": 11184237.085714286,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialCharacterStreamLATIN1",
            "value": 12792240.17051108,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialCharacterStreamUTF16",
            "value": 14800086.97516084,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "name": "Ken Shackleton",
            "username": "kennethshackleton",
            "email": "kennethshackleton@users.noreply.github.com"
          },
          "committer": {
            "name": "GitHub",
            "username": "web-flow",
            "email": "noreply@github.com"
          },
          "id": "bcdd22659bfa95204085e2ea6f9c88c420f973c6",
          "message": "Merge pull request #1171 from kennethshackleton/prepare-1.3.3\n\nPrepare version 1.3.3",
          "timestamp": "2026-09-10T05:30:28Z",
          "url": "https://github.com/bloomberg/selekt/commit/bcdd22659bfa95204085e2ea6f9c88c420f973c6"
        },
        "date": 1789018930075,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 25180.800531054334,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 25211.453719257348,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 38100.11729528733,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 25159.264299052655,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 25238.751921795818,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 38051.96476775049,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 101723.74788400104,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 101746.50778158577,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 113668.76335649929,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 68512.17747150388,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 68537.37932656457,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 80517.8566940621,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektAsciiStreamASCII",
            "value": 2792443.6430330095,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektAsciiStreamLATIN1",
            "value": 2800445.2823758526,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektAsciiStreamUTF16",
            "value": 2800445.561465306,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialAsciiStreamASCII",
            "value": 9976259.28,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialAsciiStreamLATIN1",
            "value": 11984274.342857143,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialAsciiStreamUTF16",
            "value": 15912119.257435894,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektCharacterStreamASCII",
            "value": 3192442.3183775293,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektCharacterStreamLATIN1",
            "value": 3200446.8161048074,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektCharacterStreamUTF16",
            "value": 3200447.6785611715,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialCharacterStreamASCII",
            "value": 11184258.159673471,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialCharacterStreamLATIN1",
            "value": 11992274.57317073,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialCharacterStreamUTF16",
            "value": 15600108.45454545,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "name": "Ken Shackleton",
            "username": "kennethshackleton",
            "email": "kennethshackleton@users.noreply.github.com"
          },
          "committer": {
            "name": "GitHub",
            "username": "web-flow",
            "email": "noreply@github.com"
          },
          "id": "7db0c88b83aa4b7931ef7053ba332c191d9116c1",
          "message": "Merge pull request #1175 from kennethshackleton/prepare-1.4.0\n\nPrepare version 1.4.0",
          "timestamp": "2026-09-10T07:18:37Z",
          "url": "https://github.com/bloomberg/selekt/commit/7db0c88b83aa4b7931ef7053ba332c191d9116c1"
        },
        "date": 1789025355559,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 1158.6631808336542,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 1181.3987923442066,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 6888.764646431654,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 1156.8667706665105,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 1157.066262467289,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 6859.320454309505,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 101723.85123487003,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 101748.24274117437,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 113673.55751889749,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 68514.39960650698,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 68539.09411147184,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 80511.19762563857,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektAsciiStreamASCII",
            "value": 4392450.366157787,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektAsciiStreamLATIN1",
            "value": 2800447.0653631156,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektAsciiStreamUTF16",
            "value": 2800448.0229599345,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialAsciiStreamASCII",
            "value": 9976259.836734695,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialAsciiStreamLATIN1",
            "value": 11584270.269767439,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialAsciiStreamUTF16",
            "value": 15912116.729230767,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektCharacterStreamASCII",
            "value": 3192444.4222785244,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektCharacterStreamLATIN1",
            "value": 3200450.7075973996,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektCharacterStreamUTF16",
            "value": 3200450.040797082,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialCharacterStreamASCII",
            "value": 11184259.869387757,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialCharacterStreamLATIN1",
            "value": 11992268.254545454,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialCharacterStreamUTF16",
            "value": 15600108.274841433,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "name": "Ken Shackleton",
            "username": "kennethshackleton",
            "email": "kennethshackleton@users.noreply.github.com"
          },
          "committer": {
            "name": "GitHub",
            "username": "web-flow",
            "email": "noreply@github.com"
          },
          "id": "7db0c88b83aa4b7931ef7053ba332c191d9116c1",
          "message": "Merge pull request #1175 from kennethshackleton/prepare-1.4.0\n\nPrepare version 1.4.0",
          "timestamp": "2026-09-10T07:18:37Z",
          "url": "https://github.com/bloomberg/selekt/commit/7db0c88b83aa4b7931ef7053ba332c191d9116c1"
        },
        "date": 1789025779134,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 1154.3357827375874,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 1207.63424227942,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 12052.333875157103,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 1131.216101528563,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 1165.457157186721,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 15380.805486221705,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 101747.01723869491,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 101759.8384239214,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 120014.01331966708,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 68519.39501339823,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 68527.4422246658,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 84426.4388486701,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektAsciiStreamASCII",
            "value": 2792433.8154310635,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektAsciiStreamLATIN1",
            "value": 2800436.3962059407,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektAsciiStreamUTF16",
            "value": 2800436.3533572764,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialAsciiStreamASCII",
            "value": 9976243.004403481,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialAsciiStreamLATIN1",
            "value": 11584243.804024074,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialAsciiStreamUTF16",
            "value": 16312096.61779789,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektCharacterStreamASCII",
            "value": 3192431.5924732266,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektCharacterStreamLATIN1",
            "value": 3200433.515652152,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektCharacterStreamUTF16",
            "value": 3200433.9990410367,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialCharacterStreamASCII",
            "value": 10384245.797514126,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialCharacterStreamLATIN1",
            "value": 11992252.711842233,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialCharacterStreamUTF16",
            "value": 15600092.64904281,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "name": "Ken Shackleton",
            "username": "kennethshackleton",
            "email": "kennethshackleton@users.noreply.github.com"
          },
          "committer": {
            "name": "GitHub",
            "username": "web-flow",
            "email": "noreply@github.com"
          },
          "id": "b0c3425df61a8c9e4e8b46b8ad20a2e65bba7078",
          "message": "Merge pull request #1176 from kennethshackleton/reuse-prep-buffers\n\nReuse prepared statement buffers for Java 25 ASCII binds",
          "timestamp": "2026-09-10T15:49:38Z",
          "url": "https://github.com/bloomberg/selekt/commit/b0c3425df61a8c9e4e8b46b8ad20a2e65bba7078"
        },
        "date": 1789056484994,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 1169.8986295797895,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 33233.53599061211,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 48929.449661996434,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 1182.9516061008333,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 33187.92960441684,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 53052.06091598553,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 102052.05798130094,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 102063.51363466869,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 140848.97265727323,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 68784.336052068,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 69081.11107126971,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 101945.98656243365,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektAsciiStreamASCII",
            "value": 2792430.0303003374,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektAsciiStreamLATIN1",
            "value": 2800431.501394232,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektAsciiStreamUTF16",
            "value": 2800432.8390089995,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialAsciiStreamASCII",
            "value": 10376230.486840446,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialAsciiStreamLATIN1",
            "value": 11584231.112275079,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialAsciiStreamUTF16",
            "value": 16312078.651237402,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektCharacterStreamASCII",
            "value": 3192424.357859108,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektCharacterStreamLATIN1",
            "value": 3200426.043030221,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektCharacterStreamUTF16",
            "value": 3200427.1180289434,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialCharacterStreamASCII",
            "value": 10384233.952089699,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialCharacterStreamLATIN1",
            "value": 11992237.719819754,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialCharacterStreamUTF16",
            "value": 15600076.268506777,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "name": "Ken Shackleton",
            "username": "kennethshackleton",
            "email": "kennethshackleton@users.noreply.github.com"
          },
          "committer": {
            "name": "GitHub",
            "username": "web-flow",
            "email": "noreply@github.com"
          },
          "id": "570cdfb008e8d59cd0e9e259e09db29559b08972",
          "message": "Merge pull request #1178 from kennethshackleton/reduce-java25-blob-alloc\n\nReduce Java 25 BLOB bind allocations with scoped slab copies",
          "timestamp": "2026-09-10T20:02:36Z",
          "url": "https://github.com/bloomberg/selekt/commit/570cdfb008e8d59cd0e9e259e09db29559b08972"
        },
        "date": 1789071619042,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 1169.663539634341,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 1197.3795675244673,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 16529.002823310373,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 1161.879865525482,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 1264.7995023098742,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 16588.602591754137,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 102205.09903068938,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 102168.46802102646,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 136261.55697809177,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 68998.0988943183,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 69102.51461008341,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 104867.07676530222,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektAsciiStreamASCII",
            "value": 2792432.030323208,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektAsciiStreamLATIN1",
            "value": 2800433.096974301,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektAsciiStreamUTF16",
            "value": 2800432.686918607,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialAsciiStreamASCII",
            "value": 10376229.93613511,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialAsciiStreamLATIN1",
            "value": 11984232.94244845,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialAsciiStreamUTF16",
            "value": 15912076.78565627,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektCharacterStreamASCII",
            "value": 3192423.3623877233,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektCharacterStreamLATIN1",
            "value": 3200425.9869400235,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektCharacterStreamUTF16",
            "value": 3200428.5100689125,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialCharacterStreamASCII",
            "value": 11184226.284370262,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialCharacterStreamLATIN1",
            "value": 12792227.663711295,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialCharacterStreamUTF16",
            "value": 15600071.588968402,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "name": "Ken Shackleton",
            "username": "kennethshackleton",
            "email": "kennethshackleton@users.noreply.github.com"
          },
          "committer": {
            "name": "GitHub",
            "username": "web-flow",
            "email": "noreply@github.com"
          },
          "id": "1f56e9f3e2f8075c3450cac7b7f6038f6d603831",
          "message": "Merge pull request #1181 from kennethshackleton/validate-secret-key-length\n\nValidate native secret capacity before key access",
          "timestamp": "2026-09-11T11:34:14Z",
          "url": "https://github.com/bloomberg/selekt/commit/1f56e9f3e2f8075c3450cac7b7f6038f6d603831"
        },
        "date": 1789128076054,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 1226.855048352576,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 1202.5114977009114,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 16433.27282646653,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 1127.0466056354185,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 1196.4857899641443,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 16149.682481044492,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 101768.71114222505,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 101874.51404251705,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 124065.53166416698,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 68678.9894436718,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 68884.35802895305,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 93550.03183829425,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektAsciiStreamASCII",
            "value": 2792434.4644951606,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektAsciiStreamLATIN1",
            "value": 2800436.016,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektAsciiStreamUTF16",
            "value": 2800436.4019180634,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialAsciiStreamASCII",
            "value": 9976241.033846155,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialAsciiStreamLATIN1",
            "value": 11584247.145762712,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialAsciiStreamUTF16",
            "value": 15912092.841649193,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektCharacterStreamASCII",
            "value": 3192430.9771822575,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektCharacterStreamLATIN1",
            "value": 3200436.3591076597,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektCharacterStreamUTF16",
            "value": 3200437.50880829,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialCharacterStreamASCII",
            "value": 10384243.071377367,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialCharacterStreamLATIN1",
            "value": 11992245.848361582,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialCharacterStreamUTF16",
            "value": 15600091.417977074,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "name": "Ken Shackleton",
            "username": "kennethshackleton",
            "email": "kennethshackleton@users.noreply.github.com"
          },
          "committer": {
            "name": "GitHub",
            "username": "web-flow",
            "email": "noreply@github.com"
          },
          "id": "c8e7487ca724bdaa347d1a63a0e65dbc3330fa9b",
          "message": "Merge pull request #1185 from kennethshackleton/prepare-1.4.2\n\nPrepare version 1.4.2",
          "timestamp": "2026-09-11T16:24:16Z",
          "url": "https://github.com/bloomberg/selekt/commit/c8e7487ca724bdaa347d1a63a0e65dbc3330fa9b"
        },
        "date": 1789144937882,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 1179.4902101491693,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 1184.9404476404475,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 6912.921451992761,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 1152.747712524706,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 1165.4214648005593,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 6858.521705545257,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 101721.80408018494,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 101744.3845077891,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 113677.71864170372,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 68511.88899814308,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 68536.91794058456,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 80512.93352247275,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektAsciiStreamASCII",
            "value": 2792447.6867888425,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektAsciiStreamLATIN1",
            "value": 2800448.667362299,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektAsciiStreamUTF16",
            "value": 2800450.077660133,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialAsciiStreamASCII",
            "value": 9976264.555781687,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialAsciiStreamLATIN1",
            "value": 11984274.634146338,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialAsciiStreamUTF16",
            "value": 15912114.657560974,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektCharacterStreamASCII",
            "value": 3192444.441103965,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektCharacterStreamLATIN1",
            "value": 3200452.186462437,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektCharacterStreamUTF16",
            "value": 3200454.1451644376,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialCharacterStreamASCII",
            "value": 11184258.144,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialCharacterStreamLATIN1",
            "value": 11992274.442926828,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialCharacterStreamUTF16",
            "value": 15600108.576321352,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "name": "Ken Shackleton",
            "username": "kennethshackleton",
            "email": "kennethshackleton@users.noreply.github.com"
          },
          "committer": {
            "name": "GitHub",
            "username": "web-flow",
            "email": "noreply@github.com"
          },
          "id": "ff51fd0ba0f6fe69e55a48b4ecaa49410a1e5d18",
          "message": "Merge pull request #1186 from kennethshackleton/fix-javadoc-publication\n\nDisable the Javadoc task for selekt-sqlite3-classes",
          "timestamp": "2026-09-11T17:20:00Z",
          "url": "https://github.com/bloomberg/selekt/commit/ff51fd0ba0f6fe69e55a48b4ecaa49410a1e5d18"
        },
        "date": 1789184217310,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 1144.483945779437,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 1194.931220153033,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 6886.155427243878,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 1125.0868666233655,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 1147.4861845683981,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 6861.34282849452,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 101712.87491687098,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 101733.07604399146,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 113680.67312991913,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 68500.88650823288,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 68520.90956376486,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 80523.78188043053,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektAsciiStreamASCII",
            "value": 2792443.902294933,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektAsciiStreamLATIN1",
            "value": 2800446.8189351046,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektAsciiStreamUTF16",
            "value": 2800447.381335302,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialAsciiStreamASCII",
            "value": 9976252.927183786,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialAsciiStreamLATIN1",
            "value": 11584266.488888888,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialAsciiStreamUTF16",
            "value": 15912111.748610323,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektCharacterStreamASCII",
            "value": 3192438.892373486,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektCharacterStreamLATIN1",
            "value": 3200445.0175314653,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektCharacterStreamUTF16",
            "value": 3200446.217866778,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialCharacterStreamASCII",
            "value": 10384256.831372548,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialCharacterStreamLATIN1",
            "value": 12792257.678431371,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialCharacterStreamUTF16",
            "value": 15600106.382222224,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "name": "Ken Shackleton",
            "username": "kennethshackleton",
            "email": "kennethshackleton@users.noreply.github.com"
          },
          "committer": {
            "name": "GitHub",
            "username": "web-flow",
            "email": "noreply@github.com"
          },
          "id": "9d9ae186fb60849b98008375ba2ece29f8943ab9",
          "message": "Merge pull request #1187 from kennethshackleton/simplify-builds\n\nMake the cross runtime tests task of type Test, reduce boilerplate build-logic",
          "timestamp": "2026-09-12T06:06:17Z",
          "url": "https://github.com/bloomberg/selekt/commit/9d9ae186fb60849b98008375ba2ece29f8943ab9"
        },
        "date": 1789194203431,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 222249.93880138797,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 222297.14114333852,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 235666.15523475176,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 1117.9599491086733,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 1157.602800737141,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 16806.333205776817,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 101815.36828376408,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 101879.00886986591,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 125004.07209995289,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 68714.84976280601,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 68738.88206900258,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 93256.9318664073,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektAsciiStreamASCII",
            "value": 2792457.317858365,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektAsciiStreamLATIN1",
            "value": 2800459.789552086,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektAsciiStreamUTF16",
            "value": 4400463.051968977,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialAsciiStreamASCII",
            "value": 9976226.14243639,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialAsciiStreamLATIN1",
            "value": 11984232.894736843,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialAsciiStreamUTF16",
            "value": 15912071.641025642,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektCharacterStreamASCII",
            "value": 6792449.434379613,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektCharacterStreamLATIN1",
            "value": 3200449.4419691754,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektCharacterStreamUTF16",
            "value": 3200453.9877325,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialCharacterStreamASCII",
            "value": 11184226.193534514,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialCharacterStreamLATIN1",
            "value": 12792228.24924506,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialCharacterStreamUTF16",
            "value": 15600069.89477683,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "name": "Ken Shackleton",
            "username": "kennethshackleton",
            "email": "kennethshackleton@users.noreply.github.com"
          },
          "committer": {
            "name": "GitHub",
            "username": "web-flow",
            "email": "noreply@github.com"
          },
          "id": "372c14a3abd1fac2bce34f1a991308ae4bf2e500",
          "message": "Merge pull request #1192 from kennethshackleton/reuse-batch-storage\n\nReuse scrubbed batch storage in statement pool",
          "timestamp": "2026-09-12T06:37:51Z",
          "url": "https://github.com/bloomberg/selekt/commit/372c14a3abd1fac2bce34f1a991308ae4bf2e500"
        },
        "date": 1789196019305,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 5125.164738928253,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 5132.578772302705,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 17083.143668879755,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 1107.19436217514,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 1199.861243512687,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 11445.096027725378,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 101733.13388867333,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 101742.04018804406,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 121189.06871442069,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 68534.20666291578,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 68556.2529825042,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 91794.22876361843,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektAsciiStreamASCII",
            "value": 4392458.003799581,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektAsciiStreamLATIN1",
            "value": 2800448.913252532,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektAsciiStreamUTF16",
            "value": 2800450.3060297905,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialAsciiStreamASCII",
            "value": 9976220.368303549,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialAsciiStreamLATIN1",
            "value": 11584223.961477323,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialAsciiStreamUTF16",
            "value": 15912070.550385037,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektCharacterStreamASCII",
            "value": 3192440.419766835,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektCharacterStreamLATIN1",
            "value": 3200446.582878613,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektCharacterStreamUTF16",
            "value": 3200446.4110353747,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialCharacterStreamASCII",
            "value": 11184220.428254789,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialCharacterStreamLATIN1",
            "value": 12792225.808134368,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialCharacterStreamUTF16",
            "value": 14800068.687628563,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "name": "Ken Shackleton",
            "username": "kennethshackleton",
            "email": "kennethshackleton@users.noreply.github.com"
          },
          "committer": {
            "name": "GitHub",
            "username": "web-flow",
            "email": "noreply@github.com"
          },
          "id": "376f06eb54cef835470834f4b8a8e0532f473b9d",
          "message": "Merge pull request #1211 from kennethshackleton/reuse-jdbc-success-array-across-pooled\n\nReuse JDBC batch success arrays across pooled statements",
          "timestamp": "2026-09-14T20:46:49Z",
          "url": "https://github.com/bloomberg/selekt/commit/376f06eb54cef835470834f4b8a8e0532f473b9d"
        },
        "date": 1789419909791,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 4008.4809790272584,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 4083.8882747479365,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 9516.902350680251,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 4003.7715829742656,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 4022.0822114946104,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 9565.951232908696,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 101725.32066160117,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 101746.29068172272,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 113663.55703923672,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 68508.88378060205,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 68528.09857226709,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 80504.27631533357,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektAsciiStreamASCII",
            "value": 4466667.568727257,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektAsciiStreamLATIN1",
            "value": 2874648.934656854,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektAsciiStreamUTF16",
            "value": 2874629.3139230898,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialAsciiStreamASCII",
            "value": 9976265.808325626,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialAsciiStreamLATIN1",
            "value": 11984262.966191795,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialAsciiStreamUTF16",
            "value": 15912102.876271972,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektCharacterStreamASCII",
            "value": 3266521.5869926894,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektCharacterStreamLATIN1",
            "value": 3274571.426916281,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektCharacterStreamUTF16",
            "value": 3274645.7107342095,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialCharacterStreamASCII",
            "value": 10384268.52938689,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialCharacterStreamLATIN1",
            "value": 12792255.30678733,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialCharacterStreamUTF16",
            "value": 14800103.063829785,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "name": "Ken Shackleton",
            "username": "kennethshackleton",
            "email": "kennethshackleton@users.noreply.github.com"
          },
          "committer": {
            "name": "GitHub",
            "username": "web-flow",
            "email": "noreply@github.com"
          },
          "id": "71dfd5a9007f2013c7bbb601abe74e61ecbf1d48",
          "message": "Merge pull request #1213 from kennethshackleton/obsolete-android-sdk\n\nStop installing obsolete Android SDK tools",
          "timestamp": "2026-09-15T04:35:31Z",
          "url": "https://github.com/bloomberg/selekt/commit/71dfd5a9007f2013c7bbb601abe74e61ecbf1d48"
        },
        "date": 1789447806844,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 4009.8469775947524,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 4024.439951363839,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 9562.61405309405,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 4007.3817388476104,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 4086.633074029565,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 9523.816648950247,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 101721.9724601662,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 101744.99228728602,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 113668.3669396189,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 68512.58468337997,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 68535.14020196383,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 80514.42433631513,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektAsciiStreamASCII",
            "value": 2866673.538993168,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektAsciiStreamLATIN1",
            "value": 2874718.7144252607,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektAsciiStreamUTF16",
            "value": 2874722.4957098514,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialAsciiStreamASCII",
            "value": 10376266.522125605,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialAsciiStreamLATIN1",
            "value": 11584266.233816426,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialAsciiStreamUTF16",
            "value": 15912114.419512194,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektCharacterStreamASCII",
            "value": 3266652.6574246297,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektCharacterStreamLATIN1",
            "value": 3274712.668727214,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektCharacterStreamUTF16",
            "value": 3274843.4433896295,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialCharacterStreamASCII",
            "value": 11184254.127140785,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialCharacterStreamLATIN1",
            "value": 11992264.782608699,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialCharacterStreamUTF16",
            "value": 14800114.536585364,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "name": "Ken Shackleton",
            "username": "kennethshackleton",
            "email": "kennethshackleton@users.noreply.github.com"
          },
          "committer": {
            "name": "GitHub",
            "username": "web-flow",
            "email": "noreply@github.com"
          },
          "id": "fac8f664d9d0a22cea56fed5a42311fd4fc4453f",
          "message": "Merge pull request #1218 from kennethshackleton/fix-ci\n\nFix Java toolchain requirements in CI",
          "timestamp": "2026-09-15T20:17:42Z",
          "url": "https://github.com/bloomberg/selekt/commit/fac8f664d9d0a22cea56fed5a42311fd4fc4453f"
        },
        "date": 1789503878358,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 4052.669232730778,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 4048.903145728368,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 12666.709443559026,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 4017.6174242855004,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 4013.8678553795203,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 12688.65576889376,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 101721.36090283145,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 101741.93675195592,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 116011.53436896976,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 68510.28271082473,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 68534.90912355948,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 82876.8119092593,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektJniBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 912.846878596999,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektJniBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 872.9025521939911,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektJniBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 8999.13271693226,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektJniReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 880.6249584355819,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektJniReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 966.7305311845723,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektJniReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 9060.14740369342,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektAsciiStreamASCII",
            "value": 8066705.945036292,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektAsciiStreamLATIN1",
            "value": 2874607.3224061625,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektAsciiStreamUTF16",
            "value": 2874583.044067231,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialAsciiStreamASCII",
            "value": 10376243.01392729,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialAsciiStreamLATIN1",
            "value": 11984247.765209636,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialAsciiStreamUTF16",
            "value": 16312089.241102755,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektCharacterStreamASCII",
            "value": 3266349.928686361,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektCharacterStreamLATIN1",
            "value": 3274403.917865654,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektCharacterStreamUTF16",
            "value": 3274446.7127151135,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialCharacterStreamASCII",
            "value": 11184237.88010303,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialCharacterStreamLATIN1",
            "value": 12792241.254667055,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialCharacterStreamUTF16",
            "value": 14800089.146521479,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektJniAsciiStreamASCII",
            "value": 3200641.9123725016,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektJniAsciiStreamLATIN1",
            "value": 3520686.9918172224,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektJniAsciiStreamUTF16",
            "value": 3592695.350728804,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektJniCharacterStreamASCII",
            "value": 3200635.762482953,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektJniCharacterStreamLATIN1",
            "value": 3520663.8444663906,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektJniCharacterStreamUTF16",
            "value": 3592684.210363922,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "name": "Ken Shackleton",
            "username": "kennethshackleton",
            "email": "kennethshackleton@users.noreply.github.com"
          },
          "committer": {
            "name": "GitHub",
            "username": "web-flow",
            "email": "noreply@github.com"
          },
          "id": "3d96131c17d780b8aa983cab9378ee78954f2c05",
          "message": "Merge pull request #1224 from kennethshackleton/prepare-1.5.1\n\nPrepare version 1.5.1",
          "timestamp": "2026-09-17T18:14:20Z",
          "url": "https://github.com/bloomberg/selekt/commit/3d96131c17d780b8aa983cab9378ee78954f2c05"
        },
        "date": 1789677760165,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 3999.3005990995252,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 4065.554635944518,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 9556.929173948683,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 4005.2442478593853,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 4139.539112613993,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 9541.499176780642,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 101721.67563552805,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 101743.65330746837,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 113665.26982011562,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 68510.98528588972,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 68534.18972072817,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 80515.3914898381,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektJniBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 858.3009300303078,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektJniBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 889.4913485676985,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektJniBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 6036.633604461534,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektJniReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 880.9125555663592,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektJniReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 913.0617517532862,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektJniReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 6016.10611953937,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektAsciiStreamASCII",
            "value": 6466710.5328175025,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektAsciiStreamLATIN1",
            "value": 2874688.3581113857,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektAsciiStreamUTF16",
            "value": 4474773.039014401,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialAsciiStreamASCII",
            "value": 10376268.6372093,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialAsciiStreamLATIN1",
            "value": 11984270.41860465,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialAsciiStreamUTF16",
            "value": 16312119.621426413,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektCharacterStreamASCII",
            "value": 3266666.979066738,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektCharacterStreamLATIN1",
            "value": 3274758.6597984903,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektCharacterStreamUTF16",
            "value": 3274741.730480768,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialCharacterStreamASCII",
            "value": 11184256.862745097,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialCharacterStreamLATIN1",
            "value": 11992272.546520147,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialCharacterStreamUTF16",
            "value": 15600109.526060602,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektJniAsciiStreamASCII",
            "value": 3200738.375006453,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektJniAsciiStreamLATIN1",
            "value": 3520842.1940740747,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektJniAsciiStreamUTF16",
            "value": 3592766.144754992,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektJniCharacterStreamASCII",
            "value": 3200738.125490196,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektJniCharacterStreamLATIN1",
            "value": 3520767.000838791,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektJniCharacterStreamUTF16",
            "value": 3592753.7173153805,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "name": "Ken Shackleton",
            "username": "kennethshackleton",
            "email": "kennethshackleton@users.noreply.github.com"
          },
          "committer": {
            "name": "GitHub",
            "username": "web-flow",
            "email": "noreply@github.com"
          },
          "id": "9f78550bd60a0f5ee7d4cb2648f79bd979243667",
          "message": "Merge pull request #1230 from kennethshackleton/optimise-ffm-column\n\nOptimize FFM column byte copying",
          "timestamp": "2026-09-18T17:00:23Z",
          "url": "https://github.com/bloomberg/selekt/commit/9f78550bd60a0f5ee7d4cb2648f79bd979243667"
        },
        "date": 1789752289121,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 4000.1449185250394,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 4018.3160964579133,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 9532.536378446153,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 3998.7167950045687,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 4017.4806529981934,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 9524.588878383147,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 101714.29385645392,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 101734.21666078697,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 113676.13371194301,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 68502.68071534978,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 68522.07799029918,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 80511.50925443592,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektJniBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 848.2485038633492,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektJniBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 877.1968580930303,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektJniBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 6045.879629187873,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektJniReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 872.1816019493011,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektJniReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 901.4004723697824,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektJniReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 6044.789430961764,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektAsciiStreamASCII",
            "value": 2874682.439255499,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektAsciiStreamLATIN1",
            "value": 2874712.393213528,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektAsciiStreamUTF16",
            "value": 2874667.5567870997,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialAsciiStreamASCII",
            "value": 10400261.516666668,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialAsciiStreamLATIN1",
            "value": 11584258.400941176,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialAsciiStreamUTF16",
            "value": 17184272.647619046,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektCharacterStreamASCII",
            "value": 3274572.788911634,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektCharacterStreamLATIN1",
            "value": 3274634.757872499,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektCharacterStreamUTF16",
            "value": 3274608.168073562,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialCharacterStreamASCII",
            "value": 11200252.846329965,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialCharacterStreamLATIN1",
            "value": 12792259.836734695,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialCharacterStreamUTF16",
            "value": 15600263.150416281,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektJniAsciiStreamASCII",
            "value": 3200668.711619047,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektJniAsciiStreamLATIN1",
            "value": 3200684.8087094026,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektJniAsciiStreamUTF16",
            "value": 3200681.636190476,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektJniCharacterStreamASCII",
            "value": 3200668.699443438,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektJniCharacterStreamLATIN1",
            "value": 3200695.4801466153,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektJniCharacterStreamUTF16",
            "value": 3200692.587928514,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "name": "Ken Shackleton",
            "username": "kennethshackleton",
            "email": "kennethshackleton@users.noreply.github.com"
          },
          "committer": {
            "name": "GitHub",
            "username": "web-flow",
            "email": "noreply@github.com"
          },
          "id": "11496e0edd1e492e3ef66fa2d6108cf31e1042b3",
          "message": "Merge pull request #1231 from kennethshackleton/prepare-1.6.1\n\nPrepare version 1.6.1",
          "timestamp": "2026-09-18T19:27:02Z",
          "url": "https://github.com/bloomberg/selekt/commit/11496e0edd1e492e3ef66fa2d6108cf31e1042b3"
        },
        "date": 1789789384362,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 4098.981862146041,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 4019.5404940648127,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 9510.037804781048,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 3999.002267244926,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 4020.952088727324,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 9527.153729114827,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 101716.92381646522,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 101735.0248224901,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 113671.95659823719,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 68502.24966899211,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 68521.70266798245,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 80514.66291141407,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektJniBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 849.6138851132931,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektJniBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 873.9505621422666,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektJniBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 6057.249663315693,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektJniReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 919.4142362411188,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektJniReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 904.3582418637916,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektJniReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 6048.292261653562,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektAsciiStreamASCII",
            "value": 2874655.7038643574,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektAsciiStreamLATIN1",
            "value": 2874679.7753387326,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektAsciiStreamUTF16",
            "value": 2874669.4333348973,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialAsciiStreamASCII",
            "value": 10000261.666666668,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialAsciiStreamLATIN1",
            "value": 11584258.32,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialAsciiStreamUTF16",
            "value": 17184277.044615384,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektCharacterStreamASCII",
            "value": 3274580.176471767,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektCharacterStreamLATIN1",
            "value": 3274640.6866429304,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektCharacterStreamUTF16",
            "value": 3274616.5245743166,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialCharacterStreamASCII",
            "value": 11200255.6,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialCharacterStreamLATIN1",
            "value": 12792257.098039215,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialCharacterStreamUTF16",
            "value": 15600264.19389454,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektJniAsciiStreamASCII",
            "value": 3200666.318872541,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektJniAsciiStreamLATIN1",
            "value": 3200689.6949556614,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektJniAsciiStreamUTF16",
            "value": 3200687.0194535246,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektJniCharacterStreamASCII",
            "value": 3200671.2217290318,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektJniCharacterStreamLATIN1",
            "value": 3200693.385629291,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektJniCharacterStreamUTF16",
            "value": 3200707.65757623,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "name": "Ken Shackleton",
            "username": "kennethshackleton",
            "email": "kennethshackleton@users.noreply.github.com"
          },
          "committer": {
            "name": "GitHub",
            "username": "web-flow",
            "email": "noreply@github.com"
          },
          "id": "9f584b6ec176eaae16ba9d71d276e99878fb40dc",
          "message": "Merge pull request #1234 from kennethshackleton/pack-chunked-rows\n\nPack chunked parameter rows for direct batch binding",
          "timestamp": "2026-09-19T11:05:01Z",
          "url": "https://github.com/bloomberg/selekt/commit/9f584b6ec176eaae16ba9d71d276e99878fb40dc"
        },
        "date": 1789817362473,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 19504.7464806464,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 19599.922155955443,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 24763.222472736532,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 19505.249278767235,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 19518.83979421715,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 24754.352367666095,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 101724.68921919235,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 101749.33009897475,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 113673.45521722017,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 68515.10122507898,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 68540.76157840434,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 80510.20297480232,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektJniBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 16312.0903761868,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektJniBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 16336.881541490215,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektJniBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 21159.128100590337,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektJniReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 16311.922990287814,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektJniReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 16381.876060794286,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektJniReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 21155.229868326744,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektAsciiStreamASCII",
            "value": 2874691.5536996936,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektAsciiStreamLATIN1",
            "value": 2874747.547292425,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektAsciiStreamUTF16",
            "value": 2874686.1447293963,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialAsciiStreamASCII",
            "value": 10000263.063829787,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialAsciiStreamLATIN1",
            "value": 11584266.577777777,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialAsciiStreamUTF16",
            "value": 16384281.825101215,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektCharacterStreamASCII",
            "value": 3274643.1428097514,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektCharacterStreamLATIN1",
            "value": 3274658.6154253064,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektCharacterStreamUTF16",
            "value": 3274628.816361389,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialCharacterStreamASCII",
            "value": 11200261.416666668,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialCharacterStreamLATIN1",
            "value": 11992268.415757576,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialCharacterStreamUTF16",
            "value": 15600266.577777779,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektJniAsciiStreamASCII",
            "value": 3200741.948594059,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektJniAsciiStreamLATIN1",
            "value": 3200738.2078819647,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektJniAsciiStreamUTF16",
            "value": 3200738.237701417,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektJniCharacterStreamASCII",
            "value": 3200692.552608312,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektJniCharacterStreamLATIN1",
            "value": 3200753.743490874,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektJniCharacterStreamUTF16",
            "value": 3200730.942195669,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "name": "Ken Shackleton",
            "username": "kennethshackleton",
            "email": "kennethshackleton@users.noreply.github.com"
          },
          "committer": {
            "name": "GitHub",
            "username": "web-flow",
            "email": "noreply@github.com"
          },
          "id": "131257b5086ac186578e5a404b8c0b39558cfb0f",
          "message": "Merge pull request #1235 from kennethshackleton/fix-boxing\n\nAvoid boxing packed row offsets during batch binding",
          "timestamp": "2026-09-19T12:04:22Z",
          "url": "https://github.com/bloomberg/selekt/commit/131257b5086ac186578e5a404b8c0b39558cfb0f"
        },
        "date": 1789820837762,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 4017.3637273662725,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 4030.293035250793,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 14362.210405372545,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 4014.0971509230612,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 4027.625079975596,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 14971.297412183509,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 101743.71332735558,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 101793.23890544822,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 119799.48894680999,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 68591.74055576934,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 68565.33549256265,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 87859.15364483134,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektJniBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 818.585309767697,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektJniBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 838.6054212538635,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektJniBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 13285.712474996268,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektJniReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 858.3037265849132,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektJniReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 912.4963997323414,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektJniReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 12678.952295779382,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektAsciiStreamASCII",
            "value": 2874415.24340157,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektAsciiStreamLATIN1",
            "value": 2874451.9565125136,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektAsciiStreamUTF16",
            "value": 2874503.5371447285,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialAsciiStreamASCII",
            "value": 10000241.0192005,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialAsciiStreamLATIN1",
            "value": 11984251.367655504,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialAsciiStreamUTF16",
            "value": 15984249.205012534,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektCharacterStreamASCII",
            "value": 3274336.088370347,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektCharacterStreamLATIN1",
            "value": 3274414.328290996,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektCharacterStreamUTF16",
            "value": 3274373.29830544,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialCharacterStreamASCII",
            "value": 11200240.384708626,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialCharacterStreamLATIN1",
            "value": 11992246.994272355,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialCharacterStreamUTF16",
            "value": 14800250.357142854,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektJniAsciiStreamASCII",
            "value": 3200599.681558163,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektJniAsciiStreamLATIN1",
            "value": 3200617.2381308046,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektJniAsciiStreamUTF16",
            "value": 3200613.9439795194,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektJniCharacterStreamASCII",
            "value": 3200593.7817590768,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektJniCharacterStreamLATIN1",
            "value": 3200612.087146805,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektJniCharacterStreamUTF16",
            "value": 3200620.766905681,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "name": "Ken Shackleton",
            "username": "kennethshackleton",
            "email": "kennethshackleton@users.noreply.github.com"
          },
          "committer": {
            "name": "GitHub",
            "username": "web-flow",
            "email": "noreply@github.com"
          },
          "id": "957500ae9342722ce349461cef1607aa4f2e0655",
          "message": "Merge pull request #1237 from kennethshackleton/prepare-1.6.2\n\nPrepare version 1.6.2",
          "timestamp": "2026-09-19T15:58:19Z",
          "url": "https://github.com/bloomberg/selekt/commit/957500ae9342722ce349461cef1607aa4f2e0655"
        },
        "date": 1789833562909,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 3973.1068375992772,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 3990.6938856566267,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 13078.997938017039,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 3965.9483000672362,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 4025.972821305039,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 12890.714170040486,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 101710.86717738761,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 101727.6814468003,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 116010.54610884057,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 68495.16216580156,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 68516.08311668715,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 82883.01309732089,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektJniBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 825.7822472936183,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektJniBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 821.9397976156912,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektJniBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 9645.758397981897,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektJniReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 825.2221063359459,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektJniReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 894.4688795919377,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektJniReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 9129.75456523796,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektAsciiStreamASCII",
            "value": 2874431.446602471,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektAsciiStreamLATIN1",
            "value": 2874452.1340647754,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektAsciiStreamUTF16",
            "value": 2874492.9449968934,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialAsciiStreamASCII",
            "value": 10000239.498507464,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialAsciiStreamLATIN1",
            "value": 11584242.157142857,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialAsciiStreamUTF16",
            "value": 15984250.357142856,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektCharacterStreamASCII",
            "value": 3274336.632442405,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektCharacterStreamLATIN1",
            "value": 3274370.1172589157,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektCharacterStreamUTF16",
            "value": 3274363.6942432895,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialCharacterStreamASCII",
            "value": 11200239.533242878,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialCharacterStreamLATIN1",
            "value": 11992245.933333335,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialCharacterStreamUTF16",
            "value": 14800249.230547216,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektJniAsciiStreamASCII",
            "value": 3200607.2380479733,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektJniAsciiStreamLATIN1",
            "value": 3200607.230769231,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektJniAsciiStreamUTF16",
            "value": 3200610.656509486,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektJniCharacterStreamASCII",
            "value": 3200598.7001994187,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektJniCharacterStreamLATIN1",
            "value": 3200620.8055776637,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektJniCharacterStreamUTF16",
            "value": 3200628.0647761514,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "name": "Ken Shackleton",
            "username": "kennethshackleton",
            "email": "kennethshackleton@users.noreply.github.com"
          },
          "committer": {
            "name": "GitHub",
            "username": "web-flow",
            "email": "noreply@github.com"
          },
          "id": "1c0a95431e5a324a101021ab7608bee35f54776e",
          "message": "Merge pull request #1239 from kennethshackleton/stabilise-benchmarks\n\nStabilize JDBC batch benchmarks with RAM-backed storage",
          "timestamp": "2026-09-20T05:43:04Z",
          "url": "https://github.com/bloomberg/selekt/commit/1c0a95431e5a324a101021ab7608bee35f54776e"
        },
        "date": 1789884430779,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 3966.1110085953937,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 4031.541079403113,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 5619.275108607187,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 3972.474724683319,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 3945.7832268399093,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 5622.880920614848,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 101702.94449585385,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 101719.7554512553,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 110525.20192690981,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 68486.76463291889,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 68504.27044278276,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 77337.08864305352,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektJniBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 803.5293460762612,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektJniBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 895.1864253764682,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektJniBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 2523.0873270271363,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektJniReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 826.6850185571411,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektJniReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 896.2371280778771,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektJniReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 2547.198636249639,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektAsciiStreamASCII",
            "value": 2874683.7529788096,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektAsciiStreamLATIN1",
            "value": 2874686.624077026,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektAsciiStreamUTF16",
            "value": 2874744.991884408,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialAsciiStreamASCII",
            "value": 10000256.911613876,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialAsciiStreamLATIN1",
            "value": 11984268.583932346,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialAsciiStreamUTF16",
            "value": 16384272.533333335,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektCharacterStreamASCII",
            "value": 3274587.3047371204,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektCharacterStreamLATIN1",
            "value": 3274628.576551659,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektCharacterStreamUTF16",
            "value": 3274647.750664705,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialCharacterStreamASCII",
            "value": 10400259.853061225,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialCharacterStreamLATIN1",
            "value": 12792259.96734694,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialCharacterStreamUTF16",
            "value": 15600263.18371878,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektJniAsciiStreamASCII",
            "value": 3200684.2104470474,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektJniAsciiStreamLATIN1",
            "value": 3200684.1873949575,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektJniAsciiStreamUTF16",
            "value": 3200684.2046346674,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektJniCharacterStreamASCII",
            "value": 3200666.250453493,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektJniCharacterStreamLATIN1",
            "value": 3200681.66138667,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektJniCharacterStreamUTF16",
            "value": 3200695.5051597445,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "name": "Ken Shackleton",
            "username": "kennethshackleton",
            "email": "kennethshackleton@users.noreply.github.com"
          },
          "committer": {
            "name": "GitHub",
            "username": "web-flow",
            "email": "noreply@github.com"
          },
          "id": "5c412a832cfe73b323bba109a78d8cf8e3e4d980",
          "message": "Merge pull request #1240 from kennethshackleton/reuse-progress\n\nReuse progress-handler infrastructure per connection",
          "timestamp": "2026-09-20T07:46:36Z",
          "url": "https://github.com/bloomberg/selekt/commit/5c412a832cfe73b323bba109a78d8cf8e3e4d980"
        },
        "date": 1789891838538,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 1115.0191520090277,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 1135.191626345461,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 2795.112341775974,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 1131.6310319541828,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 1143.725504410213,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 2875.2148645831503,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 101703.42821491272,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 101722.46656716298,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 110501.6668226101,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 68490.63325938498,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 68507.96435355963,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 77308.61357565103,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektJniBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 805.7604855613679,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektJniBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 829.1019906396945,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektJniBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 2463.324487608229,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektJniReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 853.9232639157026,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektJniReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 876.6543359045988,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektJniReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 2487.1437673464666,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektAsciiStreamASCII",
            "value": 2800609.521020188,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektAsciiStreamLATIN1",
            "value": 2800642.0031579197,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektAsciiStreamUTF16",
            "value": 2800629.528261691,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialAsciiStreamASCII",
            "value": 10000261.467730496,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialAsciiStreamLATIN1",
            "value": 11584270.29682875,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialAsciiStreamUTF16",
            "value": 16384274.881463414,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektCharacterStreamASCII",
            "value": 3200599.208791209,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektCharacterStreamLATIN1",
            "value": 3200614.4698552783,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektCharacterStreamUTF16",
            "value": 3200613.223971737,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialCharacterStreamASCII",
            "value": 10400266.597171718,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialCharacterStreamLATIN1",
            "value": 12792261.416666668,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialCharacterStreamUTF16",
            "value": 15600268.509090906,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektJniAsciiStreamASCII",
            "value": 3200731.0084867473,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektJniAsciiStreamLATIN1",
            "value": 3200749.78117102,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektJniAsciiStreamUTF16",
            "value": 3200730.987085768,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektJniCharacterStreamASCII",
            "value": 3200713.9777777777,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektJniCharacterStreamLATIN1",
            "value": 3200753.7214469016,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektJniCharacterStreamUTF16",
            "value": 3200770.4783477695,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "name": "Ken Shackleton",
            "username": "kennethshackleton",
            "email": "kennethshackleton@users.noreply.github.com"
          },
          "committer": {
            "name": "GitHub",
            "username": "web-flow",
            "email": "noreply@github.com"
          },
          "id": "b3389218a6556bab9463e957db05b5e693823d98",
          "message": "Merge pull request #1249 from kennethshackleton/prepare-1.6.4\n\nPrepare version 1.6.4",
          "timestamp": "2026-09-21T05:04:35Z",
          "url": "https://github.com/bloomberg/selekt/commit/b3389218a6556bab9463e957db05b5e693823d98"
        },
        "date": 1789968562515,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 1133.5745678313915,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 1206.435380544765,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 2781.4136035023907,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 1177.709868895447,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 1141.4471082223797,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 2740.2356485724426,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 101704.23156585798,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 101721.3952637899,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 110457.0241917199,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 68489.43657012662,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 68507.62197888215,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 77251.69591835947,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektJniBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 806.8360118230378,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektJniBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 829.479444467401,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektJniBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 2955.7863147734183,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektJniReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 805.9937442929331,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektJniReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 854.113618905363,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektJniReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 2480.7719768207144,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektAsciiStreamASCII",
            "value": 2800643.6106180344,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektAsciiStreamLATIN1",
            "value": 2800664.004997018,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektAsciiStreamUTF16",
            "value": 2800631.8011971703,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialAsciiStreamASCII",
            "value": 10000261.400340136,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialAsciiStreamLATIN1",
            "value": 11584266.577777777,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialAsciiStreamUTF16",
            "value": 15984270.418604651,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektCharacterStreamASCII",
            "value": 3200622.5279046674,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektCharacterStreamLATIN1",
            "value": 3200643.60023733,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektCharacterStreamUTF16",
            "value": 3200645.1753000584,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialCharacterStreamASCII",
            "value": 11200259.280000003,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialCharacterStreamLATIN1",
            "value": 12792263.063829785,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialCharacterStreamUTF16",
            "value": 15600266.577777779,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektJniAsciiStreamASCII",
            "value": 3200712.4572025267,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektJniAsciiStreamLATIN1",
            "value": 3200757.7205524636,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektJniAsciiStreamUTF16",
            "value": 3200757.688632859,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektJniCharacterStreamASCII",
            "value": 3200738.934578044,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektJniCharacterStreamLATIN1",
            "value": 3200778.396156486,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektJniCharacterStreamUTF16",
            "value": 3200749.994772277,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "name": "Ken Shackleton",
            "username": "kennethshackleton",
            "email": "kennethshackleton@users.noreply.github.com"
          },
          "committer": {
            "name": "GitHub",
            "username": "web-flow",
            "email": "noreply@github.com"
          },
          "id": "c18dd1c9b4f2dc8dc8295e0799b876d4470fd562",
          "message": "Merge pull request #1250 from kennethshackleton/optimise-blob-bind\n\nOptimise JNI BLOB binding with SQLite-owned buffers",
          "timestamp": "2026-09-21T06:23:47Z",
          "url": "https://github.com/bloomberg/selekt/commit/c18dd1c9b4f2dc8dc8295e0799b876d4470fd562"
        },
        "date": 1789973249934,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 1125.6577984398148,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 1150.61617335627,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 2735.6982296124065,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 1105.8525349312445,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 1154.9160940342686,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 2861.0447674792003,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 101701.82179094316,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 101717.13539076675,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 110500.7795122336,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 68485.72844854665,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 68503.44054856579,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 77276.98915556385,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektJniBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 803.6750898013886,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektJniBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 851.7339540751846,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektJniBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 3085.753486649394,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektJniReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 827.676443916133,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektJniReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 899.8347146941403,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektJniReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 3119.539194169898,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektAsciiStreamASCII",
            "value": 2800621.246256928,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektAsciiStreamLATIN1",
            "value": 2800638.913635299,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektAsciiStreamUTF16",
            "value": 2800623.789157938,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialAsciiStreamASCII",
            "value": 10400266.631111111,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialAsciiStreamLATIN1",
            "value": 11584266.670801032,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialAsciiStreamUTF16",
            "value": 17184272.59047619,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektCharacterStreamASCII",
            "value": 3200603.718370489,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektCharacterStreamLATIN1",
            "value": 3200609.407270358,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektCharacterStreamUTF16",
            "value": 3200614.0065075033,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialCharacterStreamASCII",
            "value": 10400263.181914894,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialCharacterStreamLATIN1",
            "value": 11992266.631111111,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialCharacterStreamUTF16",
            "value": 15600264.839104086,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektJniAsciiStreamASCII",
            "value": 3200676.74667404,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektJniAsciiStreamLATIN1",
            "value": 3200703.4844851256,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektJniAsciiStreamUTF16",
            "value": 3200700.6444377815,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektJniCharacterStreamASCII",
            "value": 3200687.0518312785,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektJniCharacterStreamLATIN1",
            "value": 3200712.457896265,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektJniCharacterStreamUTF16",
            "value": 3200709.390398197,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "name": "Ken Shackleton",
            "username": "kennethshackleton",
            "email": "kennethshackleton@users.noreply.github.com"
          },
          "committer": {
            "name": "GitHub",
            "username": "web-flow",
            "email": "noreply@github.com"
          },
          "id": "3e050580c494c3e2c53ca77c2984be6a83edc6f3",
          "message": "Merge pull request #1251 from bloomberg/revert-1250-optimise-blob-bind\n\nRevert \"Optimise JNI BLOB binding with SQLite-owned buffers\"",
          "timestamp": "2026-09-21T07:08:10Z",
          "url": "https://github.com/bloomberg/selekt/commit/3e050580c494c3e2c53ca77c2984be6a83edc6f3"
        },
        "date": 1789975880960,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 1092.2311565130435,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 1104.7195947271307,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 2065.836549724287,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 1067.68964936171,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 1104.9231925664494,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 2112.579594228562,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 101686.36278827568,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 101696.24414220797,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 109917.35221625376,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 68467.17484567627,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 68477.56505204641,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 76681.74737791237,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektJniBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 805.3555438843754,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektJniBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 826.5941133560758,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektJniBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 1869.0835747962392,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektJniReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 814.9615157187251,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektJniReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 898.0637425199408,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektJniReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 2283.5552555413883,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektAsciiStreamASCII",
            "value": 2800539.9835780784,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektAsciiStreamLATIN1",
            "value": 2800549.1263209023,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektAsciiStreamUTF16",
            "value": 2800547.517993223,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialAsciiStreamASCII",
            "value": 10000225.304691914,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialAsciiStreamLATIN1",
            "value": 11584228.823117357,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialAsciiStreamUTF16",
            "value": 15984234.994547028,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektCharacterStreamASCII",
            "value": 3200512.260229838,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektCharacterStreamLATIN1",
            "value": 3200523.2885440653,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektCharacterStreamUTF16",
            "value": 3200520.8304267763,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialCharacterStreamASCII",
            "value": 11200223.121490322,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialCharacterStreamLATIN1",
            "value": 12792223.993291719,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialCharacterStreamUTF16",
            "value": 14800233.506959733,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektJniAsciiStreamASCII",
            "value": 3200594.8464634633,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektJniAsciiStreamLATIN1",
            "value": 3200596.234429787,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektJniAsciiStreamUTF16",
            "value": 3200594.8840539698,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektJniCharacterStreamASCII",
            "value": 3200588.3819429525,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektJniCharacterStreamLATIN1",
            "value": 3200606.0176290255,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektJniCharacterStreamUTF16",
            "value": 3200620.0693432596,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "name": "Ken Shackleton",
            "username": "kennethshackleton",
            "email": "kennethshackleton@users.noreply.github.com"
          },
          "committer": {
            "name": "GitHub",
            "username": "web-flow",
            "email": "noreply@github.com"
          },
          "id": "540b1cf3acb80ff7e40870bc60784df8d754695f",
          "message": "Merge pull request #1258 from kennethshackleton/optimise-jni-ascii-text-read\n\nOptimise short ARM64 JNI text reads with NEON",
          "timestamp": "2026-09-22T09:39:58Z",
          "url": "https://github.com/bloomberg/selekt/commit/540b1cf3acb80ff7e40870bc60784df8d754695f"
        },
        "date": 1790071890256,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 1155.336374261445,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 1133.273957979681,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 2882.3206490122166,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 1136.204186525566,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 1142.8220094932435,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 3011.620977116797,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 101704.53851783086,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 101719.98828883769,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 110609.9667988562,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 68489.91662178471,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 68507.3513203275,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 77388.8359389831,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektJniBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 806.7641612618279,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektJniBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 851.1902907917301,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektJniBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 3378.2092384550933,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektJniReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 805.8630863495224,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektJniReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 851.2542705801136,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektJniReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 2723.56143619572,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektAsciiStreamASCII",
            "value": 2800622.501470457,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektAsciiStreamLATIN1",
            "value": 2800662.1771717384,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektAsciiStreamUTF16",
            "value": 2800627.675343399,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialAsciiStreamASCII",
            "value": 10000261.48544652,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialAsciiStreamLATIN1",
            "value": 11984272.54308943,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialAsciiStreamUTF16",
            "value": 17184277.023076925,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektCharacterStreamASCII",
            "value": 3200620.014977256,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektCharacterStreamLATIN1",
            "value": 3200636.070014878,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektCharacterStreamUTF16",
            "value": 3200655.0398985464,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialCharacterStreamASCII",
            "value": 10400266.575142734,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialCharacterStreamLATIN1",
            "value": 11992268.549682874,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialCharacterStreamUTF16",
            "value": 14800274.634146338,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektJniAsciiStreamASCII",
            "value": 3200728.5679421616,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektJniAsciiStreamLATIN1",
            "value": 3200757.6053365334,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektJniAsciiStreamUTF16",
            "value": 3200774.119882961,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektJniCharacterStreamASCII",
            "value": 3200706.467132433,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektJniCharacterStreamLATIN1",
            "value": 3200765.686182411,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektJniCharacterStreamUTF16",
            "value": 3200801.473074565,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "name": "Ken Shackleton",
            "username": "kennethshackleton",
            "email": "kennethshackleton@users.noreply.github.com"
          },
          "committer": {
            "name": "GitHub",
            "username": "web-flow",
            "email": "noreply@github.com"
          },
          "id": "04d3e5e5dff92f91a96167c113668e8caf8dc932",
          "message": "Merge pull request #1255 from bloomberg/dependabot/github_actions/github/codeql-action/upload-sarif-4.38.1\n\nBump github/codeql-action/upload-sarif from 4.38.0 to 4.38.1",
          "timestamp": "2026-09-22T11:09:31Z",
          "url": "https://github.com/bloomberg/selekt/commit/04d3e5e5dff92f91a96167c113668e8caf8dc932"
        },
        "date": 1790075980883,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 1113.1895723946134,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 1135.257952298874,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 2685.9381221634303,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 1172.818440590686,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 1133.4349617010837,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 2785.530038827574,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 101704.50696363207,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 101721.3730874988,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 110459.15773817843,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 68489.857565316,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 68507.8547986833,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 77304.48734103193,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektJniBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 806.7123929451582,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektJniBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 826.2011415778303,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektJniBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 2467.1926794298197,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektJniReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 853.4535117812032,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektJniReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 868.7606882567186,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektJniReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 2482.174970854908,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektAsciiStreamASCII",
            "value": 2800629.032520042,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektAsciiStreamLATIN1",
            "value": 2800636.0856941203,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektAsciiStreamUTF16",
            "value": 2800655.362436769,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialAsciiStreamASCII",
            "value": 10000266.65050505,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialAsciiStreamLATIN1",
            "value": 11984273.71382114,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialAsciiStreamUTF16",
            "value": 15984279.28205128,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektCharacterStreamASCII",
            "value": 3200631.8019133704,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektCharacterStreamLATIN1",
            "value": 3200633.0273579704,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektCharacterStreamUTF16",
            "value": 3200640.4998834496,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialCharacterStreamASCII",
            "value": 10400268.474841436,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialCharacterStreamLATIN1",
            "value": 11992266.577777777,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialCharacterStreamUTF16",
            "value": 14800270.524916943,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektJniAsciiStreamASCII",
            "value": 3200718.79056511,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektJniAsciiStreamLATIN1",
            "value": 3200778.430199039,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektJniAsciiStreamUTF16",
            "value": 3200769.8794736844,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektJniCharacterStreamASCII",
            "value": 3200722.010601427,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektJniCharacterStreamLATIN1",
            "value": 3200728.768118497,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektJniCharacterStreamUTF16",
            "value": 3200791.9308424913,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "name": "Ken Shackleton",
            "username": "kennethshackleton",
            "email": "kennethshackleton@users.noreply.github.com"
          },
          "committer": {
            "name": "GitHub",
            "username": "web-flow",
            "email": "noreply@github.com"
          },
          "id": "d63d453b56ebd629dfad1c573dce04a2da9df8ae",
          "message": "Merge pull request #1260 from kennethshackleton/optimise-ffm-ascii-text-read\n\nOptimise Java 25 FFM text reads with reusable buffers",
          "timestamp": "2026-09-22T15:41:13Z",
          "url": "https://github.com/bloomberg/selekt/commit/d63d453b56ebd629dfad1c573dce04a2da9df8ae"
        },
        "date": 1790093630034,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 1113.8751707253234,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 1135.6148294531154,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 2736.9466118317428,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 1152.8001112606185,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 1140.3460730533538,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 2695.215953874442,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 101702.6178838625,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 101720.04922881818,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 110468.2616398482,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 68488.61874599462,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 68504.73434685594,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 77195.18780590937,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektJniBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 804.770419665633,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektJniBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 826.9010097116443,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektJniBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 2383.7608045101683,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektJniReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 828.6117389708068,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektJniReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 866.9257311537534,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektJniReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 2486.8218571911793,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektAsciiStreamASCII",
            "value": 2800622.463294927,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektAsciiStreamLATIN1",
            "value": 2800645.148019505,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektAsciiStreamUTF16",
            "value": 2800637.4924942697,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialAsciiStreamASCII",
            "value": 10000259.869387757,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialAsciiStreamLATIN1",
            "value": 11584270.398308668,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialAsciiStreamUTF16",
            "value": 17184274.634146344,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektCharacterStreamASCII",
            "value": 3200629.0560227567,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektCharacterStreamLATIN1",
            "value": 3200633.2106506457,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektCharacterStreamUTF16",
            "value": 3200629.043730917,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialCharacterStreamASCII",
            "value": 10400261.416666668,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialCharacterStreamLATIN1",
            "value": 11992268.454545453,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialCharacterStreamUTF16",
            "value": 15600266.46647343,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektJniAsciiStreamASCII",
            "value": 3200738.963256161,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektJniAsciiStreamLATIN1",
            "value": 3200738.982929692,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektJniAsciiStreamUTF16",
            "value": 3200761.673556096,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektJniCharacterStreamASCII",
            "value": 3200721.9778407933,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektJniCharacterStreamLATIN1",
            "value": 3200753.74780198,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektJniCharacterStreamUTF16",
            "value": 3200769.8708030386,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "name": "Ken Shackleton",
            "username": "kennethshackleton",
            "email": "kennethshackleton@users.noreply.github.com"
          },
          "committer": {
            "name": "GitHub",
            "username": "web-flow",
            "email": "noreply@github.com"
          },
          "id": "64dda980a8cfda2d3d5a9accde32ee9a5b169c2a",
          "message": "Merge pull request #1261 from kennethshackleton/ffm-batch-text-read\n\nOptimize Java 25 text reads with adaptive native batching",
          "timestamp": "2026-09-23T10:26:05Z",
          "url": "https://github.com/bloomberg/selekt/commit/64dda980a8cfda2d3d5a9accde32ee9a5b169c2a"
        },
        "date": 1790160518634,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 1095.881600720805,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 1105.0244895637468,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 2190.071756929668,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 1065.9992525638313,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 1159.7654135356102,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 2124.1684228691006,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 101686.01740348099,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 101694.75682093129,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 109831.00823485025,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 68465.19920093709,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 68476.6120743913,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 76618.01349183194,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektJniBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 828.9237047770615,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektJniBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 799.5455127908514,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektJniBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 1767.5167255694573,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektJniReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 786.2214297213693,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektJniReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 823.0558248603887,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektJniReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 1920.5897209058191,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektAsciiStreamASCII",
            "value": 2800532.308477371,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektAsciiStreamLATIN1",
            "value": 2800535.9675194984,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektAsciiStreamUTF16",
            "value": 2800533.514948569,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialAsciiStreamASCII",
            "value": 10000225.573693765,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialAsciiStreamLATIN1",
            "value": 11984230.767617311,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialAsciiStreamUTF16",
            "value": 16384232.319146957,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektCharacterStreamASCII",
            "value": 3200504.3319136715,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektCharacterStreamLATIN1",
            "value": 3200517.416271541,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektCharacterStreamUTF16",
            "value": 3200519.3148217853,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialCharacterStreamASCII",
            "value": 10400227.77310038,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialCharacterStreamLATIN1",
            "value": 12792223.176823447,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialCharacterStreamUTF16",
            "value": 15600227.660346244,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektJniAsciiStreamASCII",
            "value": 3200579.8948857486,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektJniAsciiStreamLATIN1",
            "value": 3200596.10691675,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektJniAsciiStreamUTF16",
            "value": 3200584.787455205,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektJniCharacterStreamASCII",
            "value": 3200573.5247288626,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektJniCharacterStreamLATIN1",
            "value": 3200585.861483655,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektJniCharacterStreamUTF16",
            "value": 3200609.112418967,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "name": "Ken Shackleton",
            "username": "kennethshackleton",
            "email": "kennethshackleton@users.noreply.github.com"
          },
          "committer": {
            "name": "GitHub",
            "username": "web-flow",
            "email": "noreply@github.com"
          },
          "id": "85595d847619b188dc20ce5545921282d8663cb2",
          "message": "Merge pull request #1262 from kennethshackleton/jni-batch-text-read\n\nBatch short ASCII text reads through JNI",
          "timestamp": "2026-09-23T14:56:53Z",
          "url": "https://github.com/bloomberg/selekt/commit/85595d847619b188dc20ce5545921282d8663cb2"
        },
        "date": 1790176842119,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 1141.4154683117672,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 1129.201222633327,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 2710.318734341226,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 1112.3262564867587,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 1138.4998623099827,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 2735.7743988488965,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 101703.20227459291,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 101719.16450122048,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 110437.9506248892,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 68489.60962208573,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 68505.95826891644,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 77263.12575123576,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektJniBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 805.841257809551,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektJniBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 825.3489829732629,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektJniBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 2456.9746968158315,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektJniReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 805.1818594783131,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektJniReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 873.118714098104,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektJniReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 2438.931501858615,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektAsciiStreamASCII",
            "value": 2800633.2059977786,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektAsciiStreamLATIN1",
            "value": 2800634.6391203054,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektAsciiStreamUTF16",
            "value": 2800625.049371678,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialAsciiStreamASCII",
            "value": 10000259.853061225,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialAsciiStreamLATIN1",
            "value": 11584270.483358502,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialAsciiStreamUTF16",
            "value": 16384276.949230766,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektCharacterStreamASCII",
            "value": 3200615.112280099,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektCharacterStreamLATIN1",
            "value": 3200637.5405747127,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektCharacterStreamUTF16",
            "value": 3200642.0432724007,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialCharacterStreamASCII",
            "value": 10400261.416666668,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialCharacterStreamLATIN1",
            "value": 12792263.063829785,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialCharacterStreamUTF16",
            "value": 15600264.819710147,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektJniAsciiStreamASCII",
            "value": 3200728.7396226414,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektJniAsciiStreamLATIN1",
            "value": 3200753.8558482784,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektJniAsciiStreamUTF16",
            "value": 3200769.9373774645,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektJniCharacterStreamASCII",
            "value": 3200742.563339806,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektJniCharacterStreamLATIN1",
            "value": 3200718.6982431305,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektJniCharacterStreamUTF16",
            "value": 3200742.586631815,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "name": "Ken Shackleton",
            "username": "kennethshackleton",
            "email": "kennethshackleton@users.noreply.github.com"
          },
          "committer": {
            "name": "GitHub",
            "username": "web-flow",
            "email": "noreply@github.com"
          },
          "id": "bdd3c2dcf787896930d1b2993cd7263abbaab4eb",
          "message": "Merge pull request #1271 from kennethshackleton/optimise-jdbc-lease\n\nOptimise JDBC prepared statement lease allocation",
          "timestamp": "2026-09-25T11:12:30Z",
          "url": "https://github.com/bloomberg/selekt/commit/bdd3c2dcf787896930d1b2993cd7263abbaab4eb"
        },
        "date": 1790336386111,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 5162.119961507395,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 5165.372826320956,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 6821.5009122620595,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 5125.58111544563,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 5149.112975863725,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 6813.474605926844,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 101701.60373213672,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 101717.17234426357,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 110551.2654301462,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 68486.26329172734,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 68504.92896834461,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 77451.3584722222,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektJniBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 4849.187717768723,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektJniBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 4880.535436840701,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektJniBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 7573.018207763334,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektJniReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 4844.958453582434,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektJniReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 4864.199968159917,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektJniReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 6584.040477692617,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektAsciiStreamASCII",
            "value": 2800668.4302633335,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektAsciiStreamLATIN1",
            "value": 2800686.8852989143,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektAsciiStreamUTF16",
            "value": 2800674.1633850024,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialAsciiStreamASCII",
            "value": 10000255.692307692,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialAsciiStreamLATIN1",
            "value": 11984268.604228329,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialAsciiStreamUTF16",
            "value": 16384272.565389086,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektCharacterStreamASCII",
            "value": 3200658.8807822047,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektCharacterStreamLATIN1",
            "value": 3200671.2295816233,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektCharacterStreamUTF16",
            "value": 3200662.221635933,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialCharacterStreamASCII",
            "value": 10400261.584751774,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialCharacterStreamLATIN1",
            "value": 12792256.892862743,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialCharacterStreamUTF16",
            "value": 15600261.43368794,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektJniAsciiStreamASCII",
            "value": 3200701.747537162,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektJniAsciiStreamLATIN1",
            "value": 3200721.3720089374,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektJniAsciiStreamUTF16",
            "value": 3200719.1624542656,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektJniCharacterStreamASCII",
            "value": 3200701.8005105187,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektJniCharacterStreamLATIN1",
            "value": 3200721.308718699,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektJniCharacterStreamUTF16",
            "value": 3200752.6266666665,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "name": "Ken Shackleton",
            "username": "kennethshackleton",
            "email": "kennethshackleton@users.noreply.github.com"
          },
          "committer": {
            "name": "GitHub",
            "username": "web-flow",
            "email": "noreply@github.com"
          },
          "id": "8c65053757aca7ef6a96a7f2c104656c2bf31a93",
          "message": "Merge pull request #1272 from kennethshackleton/optimise-ffm-down\n\nOptimise FFM downcalls when callbacks are inactive",
          "timestamp": "2026-09-25T21:16:01Z",
          "url": "https://github.com/bloomberg/selekt/commit/8c65053757aca7ef6a96a7f2c104656c2bf31a93"
        },
        "date": 1790372387153,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 5182.406879618615,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 5184.351223122752,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 6798.6839351740155,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 5139.090218303334,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 5164.757468653716,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 6761.996474924507,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 101703.9989221757,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 101721.3556038634,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 110477.82038171058,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 68490.08162331379,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 68507.45071521237,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 77254.83604416005,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektJniBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 4855.232518887964,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektJniBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 4873.936668258403,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektJniBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 6504.052567316297,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektJniReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 4870.404490247045,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektJniReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 4916.8637732028965,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektJniReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 6510.075397531204,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektAsciiStreamASCII",
            "value": 2800741.145352243,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektAsciiStreamLATIN1",
            "value": 2800759.635232781,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektAsciiStreamUTF16",
            "value": 2800746.487172044,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialAsciiStreamASCII",
            "value": 10000261.416666668,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialAsciiStreamLATIN1",
            "value": 11584266.577777777,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialAsciiStreamUTF16",
            "value": 15984276.80634146,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektCharacterStreamASCII",
            "value": 3200743.2402326623,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektCharacterStreamLATIN1",
            "value": 3200745.3909542193,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektCharacterStreamUTF16",
            "value": 3200746.494330394,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialCharacterStreamASCII",
            "value": 11200259.951020408,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialCharacterStreamLATIN1",
            "value": 12792261.433687944,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialCharacterStreamUTF16",
            "value": 14800272.476190478,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektJniAsciiStreamASCII",
            "value": 3200755.4969549193,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektJniAsciiStreamLATIN1",
            "value": 3200761.273385231,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektJniAsciiStreamUTF16",
            "value": 3200752.7111687358,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektJniCharacterStreamASCII",
            "value": 3200732.497684475,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektJniCharacterStreamLATIN1",
            "value": 3200752.847851577,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektJniCharacterStreamUTF16",
            "value": 3200755.408013794,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "name": "Ken Shackleton",
            "username": "kennethshackleton",
            "email": "kennethshackleton@users.noreply.github.com"
          },
          "committer": {
            "name": "GitHub",
            "username": "web-flow",
            "email": "noreply@github.com"
          },
          "id": "8c65053757aca7ef6a96a7f2c104656c2bf31a93",
          "message": "Merge pull request #1272 from kennethshackleton/optimise-ffm-down\n\nOptimise FFM downcalls when callbacks are inactive",
          "timestamp": "2026-09-25T21:16:01Z",
          "url": "https://github.com/bloomberg/selekt/commit/8c65053757aca7ef6a96a7f2c104656c2bf31a93"
        },
        "date": 1790394197823,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 5161.815167618957,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 5185.425384445575,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 6832.058162624513,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 5161.885562923426,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 5161.510204519147,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 6772.687016716865,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 101704.0406429164,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 101721.61137314803,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 110495.731113357,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 68489.67158362114,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 68507.22271359898,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 77270.49029303729,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektJniBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 4853.972261514832,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektJniBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 4875.959299162059,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektJniBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 6505.809746998385,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektJniReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 4846.84005322075,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektJniReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 4867.926888565353,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektJniReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 6528.190854376922,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektAsciiStreamASCII",
            "value": 2800744.311699619,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektAsciiStreamLATIN1",
            "value": 2800742.2073049634,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektAsciiStreamUTF16",
            "value": 2800741.1301556025,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialAsciiStreamASCII",
            "value": 10400266.577777777,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialAsciiStreamLATIN1",
            "value": 11584270.41860465,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialAsciiStreamUTF16",
            "value": 17184279.282051288,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektCharacterStreamASCII",
            "value": 3200739.1158859716,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektCharacterStreamLATIN1",
            "value": 3200755.834930962,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektCharacterStreamUTF16",
            "value": 3200757.1342592323,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialCharacterStreamASCII",
            "value": 11200256.862745097,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialCharacterStreamLATIN1",
            "value": 12792261.318707485,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.xerialCharacterStreamUTF16",
            "value": 15600268.357575756,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektJniAsciiStreamASCII",
            "value": 3200739.604672058,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektJniAsciiStreamLATIN1",
            "value": 3200755.389902288,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektJniAsciiStreamUTF16",
            "value": 3200749.990574258,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektJniCharacterStreamASCII",
            "value": 3200719.2199817896,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektJniCharacterStreamLATIN1",
            "value": 3200761.0459657065,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcStringStreamBenchmark.selektJniCharacterStreamUTF16",
            "value": 3200770.2285160096,
            "unit": "B/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      }
    ]
  }
}