window.BENCHMARK_DATA = {
  "lastUpdate": 1778274131414,
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
      }
    ]
  }
}