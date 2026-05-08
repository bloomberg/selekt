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

<script type="text/javascript">
(function () {
  var script = document.createElement('script');
  script.src = '../benchmarks/jmh/data.js';
  script.onload = function () {
    if (typeof window.BENCHMARK_DATA === 'undefined') return;

    var group = window.BENCHMARK_DATA.entries['JDBC Benchmarks'];
    if (!group || group.length === 0) return;

    // Use the latest run
    var latest = group[group.length - 1];
    var benches = latest.benches;

    // Group by base name (strip "selekt"/"xerial" prefix from method name)
    var pairs = {};
    benches.forEach(function (b) {
      var method = b.name.replace(/^.*\./, '');
      var driver, base;
      if (method.startsWith('selekt')) {
        driver = 'Selekt';
        base = method.substring(6); // strip "selekt"
      } else if (method.startsWith('xerial')) {
        driver = 'Xerial';
        base = method.substring(6); // strip "xerial"
      } else {
        return;
      }
      if (!pairs[base]) pairs[base] = {};
      pairs[base][driver] = b;
    });

    google.charts.load('current', { packages: ['corechart'] });
    google.charts.setOnLoadCallback(function () {
      Object.keys(pairs).sort().forEach(function (base) {
        var p = pairs[base];
        var selekt = p['Selekt'];
        var xerial = p['Xerial'];
        if (!selekt || !xerial) return;

        var unit = selekt.unit || 'ms/op';
        var data = google.visualization.arrayToDataTable([
          ['Driver', base + ' (' + unit + ')', { role: 'style' }],
          ['Selekt', selekt.value, '#4285F4'],
          ['Xerial', xerial.value, '#EA4335']
        ]);

        var container = document.createElement('div');
        container.style.width = '100%';
        container.style.maxWidth = '700px';
        container.style.height = '200px';
        container.style.marginBottom = '24px';
        document.getElementById('jdbc_comparison').appendChild(container);

        new google.visualization.BarChart(container).draw(data, {
          title: base,
          legend: 'none',
          hAxis: { title: unit, minValue: 0 },
          chartArea: { width: '60%' }
        });
      });

      if (document.getElementById('jdbc_comparison').children.length === 0) {
        document.getElementById('jdbc_comparison').textContent =
          'No benchmark data available yet. Results will appear after the next CI run.';
      }
    });
  };
  script.onerror = function () {
    document.getElementById('jdbc_comparison').textContent =
      'Benchmark data not yet available. Results will appear after the first CI run.';
  };
  document.head.appendChild(script);
})();
</script>
<div id="jdbc_comparison"></div>

For full time-series history, see the [benchmark dashboard](benchmarks/jmh/index.html).
