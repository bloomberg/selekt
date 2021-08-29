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
