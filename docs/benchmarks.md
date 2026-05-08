<script type="text/javascript" src="https://www.gstatic.com/charts/loader.js"></script>

## Android Benchmarks

### Insert

<script type="text/javascript">
google.charts.load('current', {'packages':['corechart']}).then(drawChart);
function drawChart() {
    var data = new google.visualization.DataTable();
    data.addColumn('string', 'API');
    data.addColumn('number', 'ms');
    data.addRows([
        ['onConflict', 0.477],
        ['statement', 0.308],
        ['batch', 0.271]
    ]);
    var options = {
        'title': 'WAL and inserting 100 integers in a transaction (Pixel 3a, v0.14.1)',
        'legend': 'none'
    };
    new google.visualization.BarChart(document.getElementById('insert_chart_div'))
        .draw(data, options);
}
</script>
<div id="insert_chart_div"></div>

## JMH

### Common and single object pools

<script type="text/javascript">
google.charts.load('current', {'packages':['corechart']}).then(drawChart);
function drawChart() {
    var data = new google.visualization.DataTable();
    data.addColumn('string', 'Pool');
    data.addColumn('number', 'ops/s');
    data.addRows([
        ['Common', 24502966.814],
        ['Single', 77747041.047]
    ]);
    var options = {
        'title': 'Borrow then return a single pooled object (v0.14.1)',
        'legend': 'none'
    };
    new google.visualization.BarChart(document.getElementById('pool_chart_div'))
        .draw(data, options);
}
</script>
<div id="pool_chart_div"></div>

## JDBC Batch Insert

Latest JMH batch-insert results across drivers. Updated periodically from CI.

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

  function drawPairs(pairs, containerId) {
    var container = document.getElementById(containerId);
    Object.keys(pairs).sort().forEach(function (base) {
      var p = pairs[base];
      var selekt = p['Selekt'];
      var xerial = p['Xerial'];
      if (!selekt || !xerial) return;

      var unit = selekt.unit || 'ms/op';
      var data = new google.visualization.DataTable();
      data.addColumn('string', 'Driver');
      data.addColumn('number', unit);
      data.addColumn({ type: 'string', role: 'style' });
      data.addRows([
        ['Selekt', Number(selekt.value), '#4285F4'],
        ['Xerial', Number(xerial.value), '#EA4335']
      ]);

      var div = document.createElement('div');
      div.style.width = '100%';
      div.style.height = '200px';
      div.style.marginBottom = '24px';
      container.appendChild(div);

      new google.visualization.BarChart(div).draw(data, {
        title: base,
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
        drawPairs(pairBenches(allocData), 'jdbc_allocation');
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
            drawPairs(pairBenches(throughputData), 'jdbc_throughput');
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

  loadScript('jmh/data.js', function () {
    if (window.BENCHMARK_DATA) {
      var group = window.BENCHMARK_DATA.entries['JDBC Benchmarks'];
      if (group && group.length > 0) {
        throughputData = group[group.length - 1].benches;
      }
      window._THROUGHPUT_DATA = window.BENCHMARK_DATA;
      delete window.BENCHMARK_DATA;
    }
    loadScript('jmh-alloc/data.js', function () {
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

For full time-series history, see the throughput [benchmark dashboard](jmh/index.html) and [allocation dashboard](jmh-alloc/index.html).
