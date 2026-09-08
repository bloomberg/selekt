<script type="text/javascript" src="https://www.gstatic.com/charts/loader.js"></script>

### Batch Insert

Latest JMH batch-insert results across drivers, updated periodically from CI. The Selekt benchmarks run on Java 11 using its JNI backend. Lower is better for both metrics.

=== "Allocation"

    <div id="jdbc_allocation"></div>

=== "Throughput"

    <div id="jdbc_throughput"></div>

### Text Stream Reads

Latest JMH results for querying and fully consuming 50,000 text values through JDBC on Java 11 using Selekt's JNI backend. Lower is better for both metrics.

=== "Allocation"

    <div id="jdbc_stream_allocation"></div>

=== "Throughput"

    <div id="jdbc_stream_throughput"></div>

<script type="text/javascript">
(function () {
  function loadScript(src, cb) {
    var s = document.createElement('script');
    s.src = src;
    s.onload = cb;
    s.onerror = function () { cb(null); };
    document.head.appendChild(s);
  }

  function pairBenches(benches) {
    var pairs = {};
    benches.forEach(function (b) {
      var method = b.name.replace(/^.*\./, '');
      var driver, base;
      if (method.startsWith('selekt')) {
        driver = 'Selekt';
        base = method.substring(6);
      } else if (method.startsWith('xerial')) {
        driver = 'Xerial';
        base = method.substring(6);
      } else {
        return;
      }
      if (!pairs[base]) pairs[base] = {};
      pairs[base][driver] = b;
    });
    return pairs;
  }

  var BATCH_BENCH_ORDER = ['BatchInsertSIMPLE', 'BatchInsertMIXED', 'BatchInsertBLOB'];
  var STREAM_BENCH_ORDER = [
    'AsciiStreamASCII',
    'AsciiStreamLATIN1',
    'AsciiStreamUTF16',
    'CharacterStreamASCII',
    'CharacterStreamLATIN1',
    'CharacterStreamUTF16'
  ];
  var STREAM_BENCH_LABELS = {
    'AsciiStreamASCII': 'getAsciiStream() — ASCII',
    'AsciiStreamLATIN1': 'getAsciiStream() — Latin-1 characters',
    'AsciiStreamUTF16': 'getAsciiStream() — emoji',
    'CharacterStreamASCII': 'getCharacterStream() — ASCII',
    'CharacterStreamLATIN1': 'getCharacterStream() — Latin-1 characters',
    'CharacterStreamUTF16': 'getCharacterStream() — emoji'
  };

  function drawPairs(pairs, containerId, defaultUnit, prefix, benchOrder, labels) {
    var container = document.getElementById(containerId);
    var keys = Object.keys(pairs).filter(function (k) {
      return k.startsWith(prefix);
    });
    keys.sort(function (a, b) {
      var ai = benchOrder.indexOf(a);
      var bi = benchOrder.indexOf(b);
      if (ai === -1) ai = benchOrder.length;
      if (bi === -1) bi = benchOrder.length;
      return ai - bi;
    });
    keys.forEach(function (base) {
      var p = pairs[base];
      var selekt = p['Selekt'];
      var xerial = p['Xerial'];
      if (!selekt || !xerial) return;

      var unit = selekt.unit || defaultUnit || 'ms/op';
      var sv = Number(selekt.value);
      var xv = Number(xerial.value);
      var selektColor = sv <= xv ? '#34A853' : '#EA4335';
      var xerialColor = xv <= sv ? '#34A853' : '#EA4335';

      var data = new google.visualization.DataTable();
      data.addColumn('string', 'Driver');
      data.addColumn('number', unit);
      data.addColumn({ type: 'string', role: 'style' });
      data.addRows([
        ['Selekt', sv, selektColor],
        ['Xerial', xv, xerialColor]
      ]);

      var div = document.createElement('div');
      div.style.width = '100%';
      div.style.height = '220px';
      div.style.marginBottom = '24px';
      container.appendChild(div);

      new google.visualization.BarChart(div).draw(data, {
        title: labels[base] || base,
        legend: 'none',
        hAxis: { title: unit, minValue: 0 },
        chartArea: { width: '70%' }
      });
    });

    if (container.children.length === 0) {
      container.textContent =
        'No benchmark data available yet. Results will appear after the next CI run.';
    }
  }

  var throughputData = null;
  var allocData = null;
  var loaded = 0;

  function onAllLoaded() {
    google.charts.load('current', { packages: ['corechart'] });
    google.charts.setOnLoadCallback(function () {
      if (allocData) {
        var allocationPairs = pairBenches(allocData);
        drawPairs(
          allocationPairs,
          'jdbc_allocation',
          'B/op',
          'BatchInsert',
          BATCH_BENCH_ORDER,
          {}
        );
        drawPairs(
          allocationPairs,
          'jdbc_stream_allocation',
          'B/op',
          'AsciiStream',
          STREAM_BENCH_ORDER,
          STREAM_BENCH_LABELS
        );
        drawPairs(
          allocationPairs,
          'jdbc_stream_allocation',
          'B/op',
          'CharacterStream',
          STREAM_BENCH_ORDER,
          STREAM_BENCH_LABELS
        );
      } else {
        document.getElementById('jdbc_allocation').textContent =
          'Allocation data not yet available. Results will appear after the first CI run.';
        document.getElementById('jdbc_stream_allocation').textContent =
          'Allocation data not yet available. Results will appear after the first CI run.';
      }
      var throughputTargets = [
        {
          id: 'jdbc_throughput',
          prefix: 'BatchInsert',
          order: BATCH_BENCH_ORDER,
          labels: {},
          drawn: false
        },
        {
          id: 'jdbc_stream_throughput',
          prefix: 'AsciiStream',
          order: STREAM_BENCH_ORDER,
          labels: STREAM_BENCH_LABELS,
          drawn: false
        },
        {
          id: 'jdbc_stream_throughput',
          prefix: 'CharacterStream',
          order: STREAM_BENCH_ORDER,
          labels: STREAM_BENCH_LABELS,
          drawn: false
        }
      ];
      function drawThroughputIfNeeded(target) {
        if (target.drawn) return;
        var el = document.getElementById(target.id);
        if (el && el.offsetWidth > 0) {
          target.drawn = true;
          if (throughputData) {
            drawPairs(
              pairBenches(throughputData),
              target.id,
              'ms/op',
              target.prefix,
              target.order,
              target.labels
            );
          } else {
            el.textContent =
              'Benchmark data not yet available. Results will appear after the first CI run.';
          }
        }
      }
      document.querySelectorAll('input[name^="__tabbed_"]').forEach(function (input) {
        input.addEventListener('change', function () {
          setTimeout(function () {
            throughputTargets.forEach(drawThroughputIfNeeded);
          }, 50);
        });
      });
      throughputTargets.forEach(drawThroughputIfNeeded);
    });
  }

  function check() {
    loaded++;
    if (loaded === 2) onAllLoaded();
  }

  loadScript('../benchmarks/jmh/data.js', function () {
    if (window.BENCHMARK_DATA) {
      var group = window.BENCHMARK_DATA.entries['JDBC Java 11 JNI Benchmarks'];
      if (group && group.length > 0) {
        throughputData = group[group.length - 1].benches;
      }
      window._THROUGHPUT_DATA = window.BENCHMARK_DATA;
      delete window.BENCHMARK_DATA;
    }
    loadScript('../benchmarks/jmh-alloc/data.js', function () {
      if (window.BENCHMARK_DATA) {
        var group = window.BENCHMARK_DATA.entries['JDBC Java 11 JNI Allocations'];
        if (group && group.length > 0) {
          allocData = group[group.length - 1].benches;
        }
      }
      check(); check();
    });
  });
})();
</script>

For full time-series history, see the throughput [benchmark dashboard](../benchmarks/jmh/index.html) and [allocation dashboard](../benchmarks/jmh-alloc/index.html).
