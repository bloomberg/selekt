<script type="text/javascript" src="https://www.gstatic.com/charts/loader.js"></script>

Selekt FFM and Xerial run on Java 25; Selekt JNI runs on Java 11. Comparisons between the Selekt backends therefore include JVM-version effects as well as the native-call mechanism.

### Batch Insert

Latest JMH batch-insert results across Xerial and both Selekt backends, updated periodically from CI. Lower is better for both metrics.

=== "Allocation"

    <div id="jdbc_allocation"></div>

=== "Throughput"

    <div id="jdbc_throughput"></div>

### Text Stream Reads

Latest JMH results for querying and fully consuming 50,000 text values through JDBC. Lower is better for both metrics.

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

  function groupBenches(benches) {
    var groups = {};
    benches.forEach(function (b) {
      var method = b.name.replace(/^.*\./, '');
      var driver, base;
      if (method.startsWith('selektJni')) {
        driver = 'Selekt JNI (Java 11)';
        base = method.substring(9);
      } else if (method.startsWith('selekt')) {
        driver = 'Selekt FFM (Java 25)';
        base = method.substring(6);
      } else if (method.startsWith('xerial')) {
        driver = 'Xerial (Java 25)';
        base = method.substring(6);
      } else {
        return;
      }
      if (!groups[base]) groups[base] = {};
      groups[base][driver] = b;
    });
    return groups;
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

  function drawComparisons(groups, containerId, defaultUnit, prefix, benchOrder, labels) {
    var container = document.getElementById(containerId);
    var keys = Object.keys(groups).filter(function (k) {
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
      var group = groups[base];
      var selektFfm = group['Selekt FFM (Java 25)'];
      var selektJni = group['Selekt JNI (Java 11)'];
      var xerial = group['Xerial (Java 25)'];
      if (!selektFfm || !selektJni || !xerial) return;

      var unit = selektFfm.unit || defaultUnit || 'ms/op';
      var fv = Number(selektFfm.value);
      var jv = Number(selektJni.value);
      var xv = Number(xerial.value);
      var best = Math.min(fv, jv, xv);
      function color(value) {
        return value === best ? '#34A853' : '#EA4335';
      }

      var data = new google.visualization.DataTable();
      data.addColumn('string', 'Driver');
      data.addColumn('number', unit);
      data.addColumn({ type: 'string', role: 'style' });
      data.addRows([
        ['Selekt FFM (Java 25)', fv, color(fv)],
        ['Selekt JNI (Java 11)', jv, color(jv)],
        ['Xerial (Java 25)', xv, color(xv)]
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
        var allocationGroups = groupBenches(allocData);
        drawComparisons(
          allocationGroups,
          'jdbc_allocation',
          'B/op',
          'BatchInsert',
          BATCH_BENCH_ORDER,
          {}
        );
        drawComparisons(
          allocationGroups,
          'jdbc_stream_allocation',
          'B/op',
          'AsciiStream',
          STREAM_BENCH_ORDER,
          STREAM_BENCH_LABELS
        );
        drawComparisons(
          allocationGroups,
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
            drawComparisons(
              groupBenches(throughputData),
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
      var group = window.BENCHMARK_DATA.entries['JDBC Benchmarks'];
      if (group && group.length > 0) {
        throughputData = group[group.length - 1].benches;
      }
      window._THROUGHPUT_DATA = window.BENCHMARK_DATA;
      delete window.BENCHMARK_DATA;
    }
    loadScript('../benchmarks/jmh-alloc/data.js', function () {
      if (window.BENCHMARK_DATA) {
        var group = window.BENCHMARK_DATA.entries['JDBC Allocations'];
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
