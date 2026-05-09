<script type="text/javascript" src="https://www.gstatic.com/charts/loader.js"></script>

### Batch Insert

Latest JMH batch-insert results across drivers, updated periodically from CI.

!!! info "Reading the charts"
    **Lower is better** for every chart on this page — both allocation (fewer bytes) and throughput (less time per operation). The <span style="color:#34A853">green</span> bar is the winner.

=== "Allocation"

    <div id="jdbc_allocation"></div>

=== "Throughput"

    <div id="jdbc_throughput"></div>

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

  var BENCH_ORDER = ['BatchInsertSIMPLE', 'BatchInsertMIXED', 'BatchInsertBLOB'];

  function drawPairs(pairs, containerId, defaultUnit) {
    var container = document.getElementById(containerId);
    var keys = Object.keys(pairs).filter(function (k) {
      return k.startsWith('BatchInsert');
    });
    keys.sort(function (a, b) {
      var ai = BENCH_ORDER.indexOf(a);
      var bi = BENCH_ORDER.indexOf(b);
      if (ai === -1) ai = BENCH_ORDER.length;
      if (bi === -1) bi = BENCH_ORDER.length;
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
        title: base + '  ↓ Lower is better',
        legend: 'none',
        hAxis: { title: unit + '  (lower is better)', minValue: 0 },
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
        drawPairs(pairBenches(allocData), 'jdbc_allocation', 'B/op');
      } else {
        document.getElementById('jdbc_allocation').textContent =
          'Allocation data not yet available. Results will appear after the first CI run.';
      }
      var throughputDrawn = false;
      function drawThroughputIfNeeded() {
        if (throughputDrawn) return;
        var el = document.getElementById('jdbc_throughput');
        if (el && el.offsetWidth > 0) {
          throughputDrawn = true;
          if (throughputData) {
            drawPairs(pairBenches(throughputData), 'jdbc_throughput', 'ms/op');
          } else {
            el.textContent =
              'Benchmark data not yet available. Results will appear after the first CI run.';
          }
        }
      }
      document.querySelectorAll('input[name^="__tabbed_"]').forEach(function (input) {
        input.addEventListener('change', function () {
          setTimeout(drawThroughputIfNeeded, 50);
        });
      });
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
