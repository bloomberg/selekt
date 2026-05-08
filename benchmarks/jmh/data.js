window.BENCHMARK_DATA = {
  "lastUpdate": 1778259557172,
  "repoUrl": "https://github.com/bloomberg/selekt",
  "entries": {
    "JDBC Benchmarks": [
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
          "id": "3dfa907187bbde1a3acf772409ea417eeffde629",
          "message": "Merge pull request #910 from kennethshackleton/protect-benchmarks-page\n\nProtect benchmarks page when publishing documentation",
          "timestamp": "2026-05-08T08:31:11Z",
          "url": "https://github.com/bloomberg/selekt/commit/3dfa907187bbde1a3acf772409ea417eeffde629"
        },
        "date": 1778229915563,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 0.5702115955317124,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 0.596787951028931,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 6.47510858081358,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 0.5408982641010411,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 0.5969080665713351,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 6.485969257052233,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 1.1247776687742475,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 1.1886142236104826,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 6.518353373297565,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 1.097036656436918,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 1.1793527906427488,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 6.506274584203209,
            "unit": "ms/op",
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
          "id": "6b5e893c8f03d09e3b9fd84a8ac76e8ce33ad6d6",
          "message": "Merge pull request #912 from kennethshackleton/fix-benchmark-link\n\nFix benchmarks link",
          "timestamp": "2026-05-08T10:55:42Z",
          "url": "https://github.com/bloomberg/selekt/commit/6b5e893c8f03d09e3b9fd84a8ac76e8ce33ad6d6"
        },
        "date": 1778238623372,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 0.5833842990912147,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 0.6322969843430177,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 6.47720352135453,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 0.5882111919187251,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 0.6365580095795497,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 6.466191995433216,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 1.2935563383102218,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 1.359804383482198,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 6.517988171277118,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 1.2745431221086947,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 1.3394508995434187,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 6.508493179879532,
            "unit": "ms/op",
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
          "id": "a60c181982033afe924595cee781feaf47282782",
          "message": "Merge pull request #914 from kennethshackleton/alloc-benchmarks\n\nAllocation benchmarks",
          "timestamp": "2026-05-08T12:49:30Z",
          "url": "https://github.com/bloomberg/selekt/commit/a60c181982033afe924595cee781feaf47282782"
        },
        "date": 1778245440284,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 0.5545312997543752,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 0.5918442593111768,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 6.464486997709294,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 0.5528992972848782,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 0.6046849365468974,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 6.473415261594679,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 1.122468527623828,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 1.1683189071378652,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 6.527316416813214,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 1.0965641891921183,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 1.1776926054442447,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 6.5078471039876975,
            "unit": "ms/op",
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
          "id": "2003d7b5932aaa0ff49d520ffce009dc43eb7c4a",
          "message": "Merge pull request #916 from kennethshackleton/fix-gitignore\n\nFix .gitignore to exclude some generated files",
          "timestamp": "2026-05-08T13:43:17Z",
          "url": "https://github.com/bloomberg/selekt/commit/2003d7b5932aaa0ff49d520ffce009dc43eb7c4a"
        },
        "date": 1778259548383,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 0.5673962372836305,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 0.6262705952062786,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 6.493136889436362,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 0.5846363321673786,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 0.6211058374497551,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 6.484467454846057,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 1.2001300218966202,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 1.3125345486056346,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 6.523377956751072,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 1.2012197966288858,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 1.3572162375094012,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 6.517742423956283,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      }
    ]
  }
}