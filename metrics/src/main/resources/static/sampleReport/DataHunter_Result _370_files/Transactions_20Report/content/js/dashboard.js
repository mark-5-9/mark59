/*
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
var showControllersOnly = false;
var seriesFilter = "";
var filtersOnlySampleSeries = true;

/*
 * Add header in statistics table to group metrics by category
 * format
 *
 */
function summaryTableHeader(header) {
    var newRow = header.insertRow(-1);
    newRow.className = "tablesorter-no-sort";
    var cell = document.createElement('th');
    cell.setAttribute("data-sorter", false);
    cell.colSpan = 1;
    cell.innerHTML = "Requests";
    newRow.appendChild(cell);

    cell = document.createElement('th');
    cell.setAttribute("data-sorter", false);
    cell.colSpan = 3;
    cell.innerHTML = "Executions";
    newRow.appendChild(cell);

    cell = document.createElement('th');
    cell.setAttribute("data-sorter", false);
    cell.colSpan = 6;
    cell.innerHTML = "Response Times (ms)";
    newRow.appendChild(cell);

    cell = document.createElement('th');
    cell.setAttribute("data-sorter", false);
    cell.colSpan = 1;
    cell.innerHTML = "Throughput";
    newRow.appendChild(cell);

    cell = document.createElement('th');
    cell.setAttribute("data-sorter", false);
    cell.colSpan = 2;
    cell.innerHTML = "Network (KB/sec)";
    newRow.appendChild(cell);
}

/*
 * Populates the table identified by id parameter with the specified data and
 * format
 *
 */
function createTable(table, info, formatter, defaultSorts, seriesIndex, headerCreator) {
    var tableRef = table[0];

    // Create header and populate it with data.titles array
    var header = tableRef.createTHead();

    // Call callback is available
    if(headerCreator) {
        headerCreator(header);
    }

    var newRow = header.insertRow(-1);
    for (var index = 0; index < info.titles.length; index++) {
        var cell = document.createElement('th');
        cell.innerHTML = info.titles[index];
        newRow.appendChild(cell);
    }

    var tBody;

    // Create overall body if defined
    if(info.overall){
        tBody = document.createElement('tbody');
        tBody.className = "tablesorter-no-sort";
        tableRef.appendChild(tBody);
        var newRow = tBody.insertRow(-1);
        var data = info.overall.data;
        for(var index=0;index < data.length; index++){
            var cell = newRow.insertCell(-1);
            cell.innerHTML = formatter ? formatter(index, data[index]): data[index];
        }
    }

    // Create regular body
    tBody = document.createElement('tbody');
    tableRef.appendChild(tBody);

    var regexp;
    if(seriesFilter) {
        regexp = new RegExp(seriesFilter, 'i');
    }
    // Populate body with data.items array
    for(var index=0; index < info.items.length; index++){
        var item = info.items[index];
        if((!regexp || filtersOnlySampleSeries && !info.supportsControllersDiscrimination || regexp.test(item.data[seriesIndex]))
                &&
                (!showControllersOnly || !info.supportsControllersDiscrimination || item.isController)){
            if(item.data.length > 0) {
                var newRow = tBody.insertRow(-1);
                for(var col=0; col < item.data.length; col++){
                    var cell = newRow.insertCell(-1);
                    cell.innerHTML = formatter ? formatter(col, item.data[col]) : item.data[col];
                }
            }
        }
    }

    // Add support of columns sort
    table.tablesorter({sortList : defaultSorts});
}

$(document).ready(function() {

    // Customize table sorter default options
    $.extend( $.tablesorter.defaults, {
        theme: 'blue',
        cssInfoBlock: "tablesorter-no-sort",
        widthFixed: true,
        widgets: ['zebra']
    });

    var data = {"OkPercent": 100.0, "KoPercent": 0.0};
    var dataset = [
        {
            "label" : "KO",
            "data" : data.KoPercent,
            "color" : "#FF6347"
        },
        {
            "label" : "OK",
            "data" : data.OkPercent,
            "color" : "#9ACD32"
        }];
    $.plot($("#flot-requests-summary"), dataset, {
        series : {
            pie : {
                show : true,
                radius : 1,
                label : {
                    show : true,
                    radius : 3 / 4,
                    formatter : function(label, series) {
                        return '<div style="font-size:8pt;text-align:center;padding:2px;color:white;">'
                            + label
                            + '<br/>'
                            + Math.round10(series.percent, -2)
                            + '%</div>';
                    },
                    background : {
                        opacity : 0.5,
                        color : '#000'
                    }
                }
            }
        },
        legend : {
            show : true
        }
    });

    // Creates APDEX table
    createTable($("#apdexTable"), {"supportsControllersDiscrimination": true, "overall": {"data": [0.9772727272727273, 500, 1500, "Total"], "isController": false}, "titles": ["Apdex", "T (Toleration threshold)", "F (Frustration threshold)", "Label"], "items": [{"data": [1.0, 500, 1500, "DH-lifecycle-0299-sometimes-I-fail"], "isController": false}, {"data": [1.0, 500, 1500, "DH-lifecycle-9999-finalize-deleteMultiplePolicies"], "isController": false}, {"data": [1.0, 500, 1500, "DH-lifecycle-0100-deleteMultiplePolicies"], "isController": false}, {"data": [1.0, 500, 1500, "DH-lifecycle-0400-countUnusedPoliciesCurrentThread"], "isController": false}, {"data": [1.0, 500, 1500, "DH-lifecycle-0200-addPolicy"], "isController": false}, {"data": [0.8166666666666667, 500, 1500, "DH-lifecycle-0001-gotoDeleteMultiplePoliciesUrl"], "isController": false}, {"data": [1.0, 500, 1500, "DH-lifecycle-0500-useNextPolicy"], "isController": false}, {"data": [1.0, 500, 1500, "DH-lifecycle-0300-countUnusedPolicies"], "isController": false}, {"data": [1.0, 500, 1500, "DH-lifecycle-0600-displaySelectedPolicies"], "isController": false}]}, function(index, item){
        switch(index){
            case 0:
                item = item.toFixed(3);
                break;
            case 1:
            case 2:
                item = formatDuration(item);
                break;
        }
        return item;
    }, [[0, 0]], 3);

    // Create statistics table
    createTable($("#statisticsTable"), {"supportsControllersDiscrimination": true, "overall": {"data": ["Total", 242, 0, 0.0, 152.18595041322314, 0, 1021, 153.70000000000002, 208.19999999999993, 982.6999999999999, 2.762525541945868, 0.0, 0.0], "isController": false}, "titles": ["Label", "#Samples", "KO", "Error %", "Average", "Min", "Max", "90th pct", "95th pct", "99th pct", "Transactions\/s", "Received", "Sent"], "items": [{"data": ["DH-lifecycle-0299-sometimes-I-fail", 19, 0, 0.0, 0.05263157894736843, 0, 1, 0.0, 1.0, 1.0, 0.23858256840413375, 0.0, 0.0], "isController": false}, {"data": ["DH-lifecycle-9999-finalize-deleteMultiplePolicies", 4, 0, 0.0, 136.75, 129, 142, 142.0, 142.0, 142.0, 0.08591249812066411, 0.0, 0.0], "isController": false}, {"data": ["DH-lifecycle-0100-deleteMultiplePolicies", 30, 0, 0.0, 148.03333333333333, 123, 210, 169.70000000000002, 203.39999999999998, 210.0, 0.34706957588097825, 0.0, 0.0], "isController": false}, {"data": ["DH-lifecycle-0400-countUnusedPoliciesCurrentThread", 19, 0, 0.0, 141.73684210526315, 127, 154, 152.0, 154.0, 154.0, 0.23816089648775352, 0.0, 0.0], "isController": false}, {"data": ["DH-lifecycle-0200-addPolicy", 95, 0, 0.0, 128.11578947368426, 114, 151, 140.8, 147.0, 151.0, 1.1512221131590747, 0.0, 0.0], "isController": false}, {"data": ["DH-lifecycle-0001-gotoDeleteMultiplePoliciesUrl", 30, 0, 0.0, 359.5666666666667, 15, 1021, 976.4, 1002.3, 1021.0, 0.343434112165581, 0.0, 0.0], "isController": false}, {"data": ["DH-lifecycle-0500-useNextPolicy", 19, 0, 0.0, 136.57894736842104, 128, 151, 146.0, 151.0, 151.0, 0.23824451410658307, 0.0, 0.0], "isController": false}, {"data": ["DH-lifecycle-0300-countUnusedPolicies", 19, 0, 0.0, 136.1578947368421, 121, 151, 146.0, 151.0, 151.0, 0.23816388182057485, 0.0, 0.0], "isController": false}, {"data": ["DH-lifecycle-0600-displaySelectedPolicies", 7, 0, 0.0, 143.85714285714286, 129, 166, 166.0, 166.0, 166.0, 0.08777209348981845, 0.0, 0.0], "isController": false}]}, function(index, item){
        switch(index){
            // Errors pct
            case 3:
                item = item.toFixed(2) + '%';
                break;
            // Mean
            case 4:
            // Mean
            case 7:
            // Percentile 1
            case 8:
            // Percentile 2
            case 9:
            // Percentile 3
            case 10:
            // Throughput
            case 11:
            // Kbytes/s
            case 12:
            // Sent Kbytes/s
                item = item.toFixed(2);
                break;
        }
        return item;
    }, [[0, 0]], 0, summaryTableHeader);

    // Create error table
    createTable($("#errorsTable"), {"supportsControllersDiscrimination": false, "titles": ["Type of error", "Number of errors", "% in errors", "% in all samples"], "items": []}, function(index, item){
        switch(index){
            case 2:
            case 3:
                item = item.toFixed(2) + '%';
                break;
        }
        return item;
    }, [[1, 1]]);

        // Create top5 errors by sampler
    createTable($("#top5ErrorsBySamplerTable"), {"supportsControllersDiscrimination": false, "overall": {"data": ["Total", 242, 0, null, null, null, null, null, null, null, null, null, null], "isController": false}, "titles": ["Sample", "#Samples", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors"], "items": [{"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}]}, function(index, item){
        return item;
    }, [[0, 0]], 0);

});
