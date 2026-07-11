window.BENCHMARK_DATA = {
  "lastUpdate": 1783743584591,
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
          "id": "15bc22d1a80b9eeb082701bf6c397fd5d0874539",
          "message": "Merge pull request #917 from kennethshackleton/fix-benchmarks-workflow\n\nFix benchmarks workflow",
          "timestamp": "2026-05-08T17:40:25Z",
          "url": "https://github.com/bloomberg/selekt/commit/15bc22d1a80b9eeb082701bf6c397fd5d0874539"
        },
        "date": 1778262862806,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 0.6995383718228848,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 0.9720650328736191,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 35.01031828654251,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 0.7805434366750433,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 0.831934818978579,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 21.50772348058816,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 4.415797630252997,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 8.006048919610134,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 51.541905528831116,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 7.4451790901193196,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 4.28222313708479,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 51.15987802050081,
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
          "id": "f490088f25c50e5158090c3e01c91ea27a63140f",
          "message": "Merge pull request #918 from kennethshackleton/fix-benchmarks-workflow\n\nFix benchmarks workflow by preserving all fields",
          "timestamp": "2026-05-08T19:11:50Z",
          "url": "https://github.com/bloomberg/selekt/commit/f490088f25c50e5158090c3e01c91ea27a63140f"
        },
        "date": 1778268378940,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 0.604529909870106,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 0.641165689944344,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 6.474303527000329,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 0.5835146637934523,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 0.6678760888011374,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 6.461822144857682,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 1.2519200686322551,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 1.3393621587412434,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 6.510643876156282,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 1.2408147478718614,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 1.303200651852656,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 6.521143940084849,
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
          "id": "fb576d25b4d428336c30b85f9a62affc974676c5",
          "message": "Merge pull request #920 from kennethshackleton/fix-benchmark-fallback\n\nFix benchmark default unit in fallback",
          "timestamp": "2026-05-08T20:17:36Z",
          "url": "https://github.com/bloomberg/selekt/commit/fb576d25b4d428336c30b85f9a62affc974676c5"
        },
        "date": 1778271821235,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 0.5377873624301674,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 0.5913552950355954,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 6.479663651120818,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 0.5622164079185448,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 0.6021808773044336,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 6.480278759147208,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 1.1179267798227375,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 1.1976351291262886,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 6.5188839622601975,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 1.1150962065563017,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 1.1743447028378924,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 6.521727626622381,
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
          "id": "cc6a0a545222c011a1d3cc2aaa61bd95c95e2665",
          "message": "Merge pull request #921 from kennethshackleton/fix-benchmakrs-pin-action\n\nFix benchmarks action by pinning to 1.20.4",
          "timestamp": "2026-05-08T20:47:38Z",
          "url": "https://github.com/bloomberg/selekt/commit/cc6a0a545222c011a1d3cc2aaa61bd95c95e2665"
        },
        "date": 1778274118144,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 0.5859026139072367,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 0.6495638920278026,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 6.479138296941294,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 0.5809353819361475,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 0.6597732584783367,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 6.476129511785831,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 1.3217207653102774,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 1.4643415222603826,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 6.521301573139984,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 1.2958587839690883,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 1.3922472485568826,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 6.515088058517843,
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
          "id": "e4b1b4190e6be1053fed2ccaf853f7824895ffdb",
          "message": "Merge pull request #923 from kennethshackleton/full-integrity-check\n\nPerform a full integrity check",
          "timestamp": "2026-05-08T21:42:29Z",
          "url": "https://github.com/bloomberg/selekt/commit/e4b1b4190e6be1053fed2ccaf853f7824895ffdb"
        },
        "date": 1778294362201,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 0.6036325767916934,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 0.6515287891019061,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 6.470970849260778,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 0.6049010731448716,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 0.6643877427146104,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 6.467859698411042,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 1.3255880061675311,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 1.4148145867381137,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 6.517865561031977,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 1.292413484337928,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 1.417489669693087,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 6.516141712313646,
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
          "id": "98c9b551e4d66796e63e46ada86081b7c3a83a98",
          "message": "Merge pull request #924 from kennethshackleton/slab-if-ascii\n\nAllocate from slab arena if text is ASCII",
          "timestamp": "2026-05-09T03:22:56Z",
          "url": "https://github.com/bloomberg/selekt/commit/98c9b551e4d66796e63e46ada86081b7c3a83a98"
        },
        "date": 1778297766485,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 0.5872410487466249,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 0.6981404332632086,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 15.932259478306879,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 0.5818498009884702,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 0.7152384460363225,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 15.942974178237623,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 1.0653848717399166,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 1.1156935337820268,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 14.955053353459709,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 1.0508217861181808,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 1.1055997132268682,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 14.902482006470123,
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
          "id": "98c9b551e4d66796e63e46ada86081b7c3a83a98",
          "message": "Merge pull request #924 from kennethshackleton/slab-if-ascii\n\nAllocate from slab arena if text is ASCII",
          "timestamp": "2026-05-09T03:22:56Z",
          "url": "https://github.com/bloomberg/selekt/commit/98c9b551e4d66796e63e46ada86081b7c3a83a98"
        },
        "date": 1778300873916,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 0.4970827797760052,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 0.6366962404379606,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 6.468122841557717,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 0.4984223608195644,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 0.604184526647592,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 6.476907349328762,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 1.2000931328748576,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 1.2939714535566196,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 6.517604095731118,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 1.1514022162390205,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 1.2460713863674608,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 6.516580890867854,
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
          "id": "821372df796a817e3e12f5daeaa33e09345348e5",
          "message": "Merge pull request #925 from kennethshackleton/tidy-benchmarks\n\nOrganise the benchmarks page",
          "timestamp": "2026-05-09T07:10:15Z",
          "url": "https://github.com/bloomberg/selekt/commit/821372df796a817e3e12f5daeaa33e09345348e5"
        },
        "date": 1778311789959,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 0.5652303889095969,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 0.6426727697275563,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 6.4733725550622525,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 0.6221269695800957,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 0.6208811411028164,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 6.482195586056546,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 1.3793112342230702,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 1.4415416538161838,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 6.523029454682103,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 1.3775032826157143,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 1.46541225226933,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 6.522387811486875,
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
          "id": "c11c52315d595c924c50ceade6f4b2b0a36e3460",
          "message": "Merge pull request #930 from kennethshackleton/primitive-bindings\n\nBind as primitives to save boxing",
          "timestamp": "2026-05-09T11:02:02Z",
          "url": "https://github.com/bloomberg/selekt/commit/c11c52315d595c924c50ceade6f4b2b0a36e3460"
        },
        "date": 1778325357574,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 0.5600066329366901,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 0.6053611374152924,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 6.489946425417922,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 0.5711964983356339,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 0.6371226557503317,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 6.474369000174863,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 1.1012371185650622,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 1.1950019208960536,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 6.518231371925111,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 1.1058455038308523,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 1.1737255525387833,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 6.512643821989677,
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
          "id": "94f19a3af3387013dd995be02a0903daf1809e3f",
          "message": "Merge pull request #934 from kennethshackleton/jdbc-guide-formatting\n\nLine-breaks and formatting in JDBC getting started",
          "timestamp": "2026-05-09T13:56:53Z",
          "url": "https://github.com/bloomberg/selekt/commit/94f19a3af3387013dd995be02a0903daf1809e3f"
        },
        "date": 1778335995594,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 0.5910270829102328,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 0.6676359267231613,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 6.471178307630636,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 0.6079913185498451,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 0.6615278457925556,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 6.463550549744136,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 1.2558960525691183,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 1.3342209493685193,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 6.525218323283854,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 1.2517826779597314,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 1.3438089505585626,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 6.522551849764414,
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
          "id": "6d34600cc9acbe90869ceda582ccbc02c498cafe",
          "message": "Merge pull request #935 from kennethshackleton/memory-segment-global\n\nUse static memory segment when getting confined Strings",
          "timestamp": "2026-05-09T16:29:44Z",
          "url": "https://github.com/bloomberg/selekt/commit/6d34600cc9acbe90869ceda582ccbc02c498cafe"
        },
        "date": 1778345038132,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 0.6193640522258885,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 0.6652064178153563,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 6.474584326929188,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 0.5926766119229178,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 0.6623980292385776,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 6.476685382790786,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 1.2580823505593677,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 1.3396166397981681,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 6.523370707389757,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 1.2491406537600676,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 1.3461680397576123,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 6.522668629358715,
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
          "id": "ac90e71b4ecc5fa8138b30bccf7959b3204282b7",
          "message": "Merge pull request #944 from kennethshackleton/prepare-0.33.1\n\nPrepare version 0.33.1",
          "timestamp": "2026-05-10T16:54:37Z",
          "url": "https://github.com/bloomberg/selekt/commit/ac90e71b4ecc5fa8138b30bccf7959b3204282b7"
        },
        "date": 1778432413618,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 0.6140379890062668,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 0.6997435835212517,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 6.493491585714575,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 0.615772911709198,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 0.6944647691190766,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 6.492310794324108,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 1.26700240064613,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 1.3569640332811026,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 6.523259392289778,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 1.289941122479848,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 1.3370144625367373,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 6.524842795872449,
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
          "id": "03159b5ff5793bb9e0e10e3e9f16e4026b51280c",
          "message": "Merge pull request #950 from bloomberg/dependabot/github_actions/github/codeql-action-4.35.4\n\nBump github/codeql-action from 4.35.3 to 4.35.4",
          "timestamp": "2026-05-11T13:41:31Z",
          "url": "https://github.com/bloomberg/selekt/commit/03159b5ff5793bb9e0e10e3e9f16e4026b51280c"
        },
        "date": 1778530845291,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 0.6122570980127009,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 0.5723626231532949,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 6.472389967392203,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 0.522598198349717,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 0.5662122209367142,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 6.468380370283104,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 1.1298329036498678,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 1.2272274159796681,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 6.514947801806612,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 1.066891525873602,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 1.182052507492016,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 6.521423584927265,
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
          "id": "29bce224bf0de819c2878213612ac64761acb588",
          "message": "Merge pull request #951 from bloomberg/dependabot/github_actions/benchmark-action/github-action-benchmark-1.22.1\n\nBump benchmark-action/github-action-benchmark from 1.20.4 to 1.22.1",
          "timestamp": "2026-05-12T05:55:03Z",
          "url": "https://github.com/bloomberg/selekt/commit/29bce224bf0de819c2878213612ac64761acb588"
        },
        "date": 1778566120382,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 0.6350846588371285,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 0.6880619259919136,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 6.478588163531976,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 0.601684089077304,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 0.6711849669753414,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 6.481476700083613,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 1.2974578191023338,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 1.3770492630455473,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 6.517270998399613,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 1.2842336482711327,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 1.395512966615996,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 6.516502525393548,
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
          "id": "e037ce8e863bb8f9a1b58ed891c2cb610f2f8e54",
          "message": "Merge pull request #955 from kennethshackleton/prepare-0.33.2\n\nPrepare version 0.33.2",
          "timestamp": "2026-05-12T19:07:00Z",
          "url": "https://github.com/bloomberg/selekt/commit/e037ce8e863bb8f9a1b58ed891c2cb610f2f8e54"
        },
        "date": 1778615194587,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 0.5491725485263822,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 0.6181352333790369,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 6.449636542084325,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 0.5695268397814496,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 0.6425101512059057,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 6.472558377362463,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 1.1227423863017258,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 1.2089658851831353,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 6.516921983421584,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 1.1052686546123363,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 1.2025551897641913,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 6.524212283978758,
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
          "id": "a15fddd92f9693d6e9cecc6f51761797e7d0f7fd",
          "message": "Merge pull request #956 from kennethshackleton/readme-android-rationale\n\nScope the rationale in the README to Android",
          "timestamp": "2026-05-15T08:28:52Z",
          "url": "https://github.com/bloomberg/selekt/commit/a15fddd92f9693d6e9cecc6f51761797e7d0f7fd"
        },
        "date": 1778905953531,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 0.5650923227319248,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 0.642379385422353,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 6.46851185715213,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 0.5647885017505485,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 0.6244082631315045,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 6.489736873359225,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 1.0744490500494526,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 1.195579360641131,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 6.521090128679068,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 1.087708199052344,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 1.1895407018867747,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 6.522715372545747,
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
          "id": "fff28c6e67d709554a724d8fb2c6527b121964dd",
          "message": "Merge pull request #960 from kennethshackleton/update-overview\n\nUpdate docs to mention JDBC",
          "timestamp": "2026-05-20T19:12:28Z",
          "url": "https://github.com/bloomberg/selekt/commit/fff28c6e67d709554a724d8fb2c6527b121964dd"
        },
        "date": 1779510955540,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 0.5680784439947018,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 0.6164049118911035,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 6.491284570294907,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 0.6171474945674693,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 0.6270903641408136,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 6.473243743804753,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 1.1309015153525124,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 1.2222434390441055,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 6.518734980412094,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 1.0822333031853872,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 1.1773085749439383,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 6.524630856048515,
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
          "id": "5b545e874ad82134784738db83b46883b17c32a3",
          "message": "Merge pull request #963 from bloomberg/dependabot/github_actions/github/codeql-action-4.36.0\n\nBump github/codeql-action from 4.35.5 to 4.36.0",
          "timestamp": "2026-05-25T19:52:11Z",
          "url": "https://github.com/bloomberg/selekt/commit/5b545e874ad82134784738db83b46883b17c32a3"
        },
        "date": 1780116068445,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 0.5847025101408241,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 0.6608359324959123,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 6.485527751145756,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 0.5824630319787948,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 0.6592783656460179,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 6.4743110918020665,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 1.2742472250353463,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 1.3738727670166333,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 6.521086419106213,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 1.2778494837529728,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 1.4039385331021212,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 6.520152535529142,
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
          "id": "5a5d3a6ac2e03da3ba57b46b440bcd4e69642e69",
          "message": "Merge pull request #969 from kennethshackleton/prepare-0.34.1\n\nPrepare version 0.34.1",
          "timestamp": "2026-06-04T08:27:52Z",
          "url": "https://github.com/bloomberg/selekt/commit/5a5d3a6ac2e03da3ba57b46b440bcd4e69642e69"
        },
        "date": 1780720863884,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 0.509651504239774,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 0.6474410910424252,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 10.201119559433355,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 0.5173984062249908,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 0.6205751564406705,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 16.10497387054702,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 1.7180165545622006,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 1.2558389348071386,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 10.288125265283114,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 1.1029167340694204,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 2.107024324815483,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 10.886920663436388,
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
          "id": "2e3f4d6823740f43cd61c67e92ff1aa9088c519f",
          "message": "Merge pull request #972 from kennethshackleton/prepare-0.34.2\n\nPrepare version 0.34.2",
          "timestamp": "2026-06-06T14:46:34Z",
          "url": "https://github.com/bloomberg/selekt/commit/2e3f4d6823740f43cd61c67e92ff1aa9088c519f"
        },
        "date": 1780758102752,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 0.5967979215702119,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 0.657480017994768,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 6.464904706894413,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 0.612735002545622,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 0.6591160740091248,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 6.474225353988848,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 1.3196206421167167,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 1.37269154639192,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 6.521833109617941,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 1.3058304529665683,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 1.42967268951936,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 6.51967648855953,
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
          "id": "35bfa21bf4ce06f3b982898b51bc8a706a1fa01c",
          "message": "Merge pull request #976 from kennethshackleton/prepare-0.34.3\n\nPrepare version 0.34.3",
          "timestamp": "2026-06-12T11:01:40Z",
          "url": "https://github.com/bloomberg/selekt/commit/35bfa21bf4ce06f3b982898b51bc8a706a1fa01c"
        },
        "date": 1781326177358,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 0.5478500112715371,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 0.6043415003933251,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 6.456585205669891,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 0.5600902530792763,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 0.6271186524435246,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 6.472275012901446,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 1.1411973264863982,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 1.2203058030681455,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 6.5239234046569745,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 1.1298589406380344,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 1.2307459955057545,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 6.522674825518249,
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
          "id": "db0f23db231b616e8c7f249b118fe2499ebf3a6f",
          "message": "Merge pull request #974 from bloomberg/dependabot/github_actions/github/codeql-action-4.36.2\n\nBump github/codeql-action from 4.36.0 to 4.36.2",
          "timestamp": "2026-06-19T20:13:24Z",
          "url": "https://github.com/bloomberg/selekt/commit/db0f23db231b616e8c7f249b118fe2499ebf3a6f"
        },
        "date": 1781930912457,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 0.5450005742018421,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 0.5870576778000218,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 6.475181165326194,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 0.5304200300712952,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 0.5780921990931033,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 6.4683064142367925,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 1.0913816172579172,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 1.1899865361887128,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 6.513585386815265,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 1.0774925972384455,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 1.188645776829077,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 6.527304889700559,
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
          "id": "830eaeee445661c9175737ce4cdb314611678dde",
          "message": "Merge pull request #993 from kennethshackleton/simplify-project-group\n\nMove group declaration to gradle.properties",
          "timestamp": "2026-06-20T05:45:30Z",
          "url": "https://github.com/bloomberg/selekt/commit/830eaeee445661c9175737ce4cdb314611678dde"
        },
        "date": 1781990345949,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 0.5840558826511774,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 0.6232173075385774,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 6.485527088649269,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 0.5750456999295339,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 0.6367810146405264,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 6.472086458430558,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 1.2500267443398658,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 1.3464490662843374,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 6.512644692706266,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 1.2348213289721244,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 1.344369409071432,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 6.547356628556182,
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
          "id": "2c41108e78f59db9110f9a3a940c92d0b0f4a0d9",
          "message": "Merge pull request #997 from kennethshackleton/gradle-9.6.0\n\nGradle 9.6.0",
          "timestamp": "2026-06-26T11:08:34Z",
          "url": "https://github.com/bloomberg/selekt/commit/2c41108e78f59db9110f9a3a940c92d0b0f4a0d9"
        },
        "date": 1782535255907,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 0.6388553923100277,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 0.661463413802508,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 6.4790544541828865,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 0.6130116821659393,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 0.659440941996358,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 6.439389338262458,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 1.2778073032403874,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 1.365623524537645,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 6.51443563081897,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 1.2597372874162516,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 1.3671599776956158,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 6.523478336568393,
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
          "id": "af9efac7088f37018cce067c41ef43a1c8a543ec",
          "message": "Merge pull request #998 from bloomberg/dependabot/github_actions/actions/cache-6\n\nBump actions/cache from 5 to 6",
          "timestamp": "2026-07-03T11:04:14Z",
          "url": "https://github.com/bloomberg/selekt/commit/af9efac7088f37018cce067c41ef43a1c8a543ec"
        },
        "date": 1783139590460,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 0.6253364038946587,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 0.655118990008514,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 6.47377721948695,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 0.6164375624555138,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 0.6660367350872156,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 6.484136137966949,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 1.2791068435177497,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 1.399729847433139,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 6.518615436096968,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 1.2746119765413322,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 1.3858821539428958,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 6.516294810346655,
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
          "id": "2a42b1413103cd842f06195772b3670e8accb900",
          "message": "Merge pull request #1003 from kennethshackleton/prepare-0.35.3\n\nPrepare version 0.35.3",
          "timestamp": "2026-07-07T11:26:52Z",
          "url": "https://github.com/bloomberg/selekt/commit/2a42b1413103cd842f06195772b3670e8accb900"
        },
        "date": 1783424537258,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 0.5926166686343031,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 0.6361329917741662,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 6.476537837321328,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 0.6013554680483867,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 0.6472014147407321,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 6.471700030712652,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 1.2501539197023748,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 1.3443277480114717,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 6.522012281003209,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 1.2648859146767166,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 1.375118929529364,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 6.525073198600144,
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
          "id": "da220fe24e836baf8dc9c93537622fa95fc6bc19",
          "message": "Merge pull request #1008 from kennethshackleton/prepare-0.35.4\n\nPrepare version 0.35.4",
          "timestamp": "2026-07-10T18:50:10Z",
          "url": "https://github.com/bloomberg/selekt/commit/da220fe24e836baf8dc9c93537622fa95fc6bc19"
        },
        "date": 1783743575562,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 0.6437217606207366,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 0.6748205243904081,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 6.470094662054359,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 0.6088624162425613,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 0.6602637506586594,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.selektReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 6.4784597970634605,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 1.5734660172217114,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 1.6250594471278421,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 6.5178770619249535,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"SIMPLE\"} )",
            "value": 1.4900158509480985,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"MIXED\"} )",
            "value": 1.6725958238742202,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.bloomberg.selekt.jdbc.benchmarks.JdbcBatchBenchmark.xerialReusedBatchInsert ( {\"batchSize\":\"1000\",\"dataType\":\"LARGE_BLOBS\"} )",
            "value": 6.512779592327936,
            "unit": "ms/op",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      }
    ]
  }
}