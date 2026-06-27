window.BENCHMARK_DATA = {
  "lastUpdate": 1782535269572,
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
      }
    ]
  }
}