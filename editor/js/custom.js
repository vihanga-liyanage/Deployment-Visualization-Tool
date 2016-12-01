
var generateGraph = function (configXML, editor) {
    var graphNode = editor.graph.container;
    graphNode.style.display = '';

    var doc = mxUtils.parseXml(configXML);
    var dec = new mxCodec(doc);
    dec.decode(doc.documentElement, editor.graph.getModel());

    // Makes sure nothing is selected in IE
    if (mxClient.IS_IE)
    {
        mxUtils.clearSelection();
    }
};

var jsonText = '{"services":[' +
'{' +
    '"id":1,' +
    '"type":"database",' +
    '"name":"apim_rdbms",' +
    '"image":"mysql:5.5",' +
    '"ports":[],' +
    '"links":[]' +
'},' +
'{' +
    '"id":2,' +
    '"type":"wso2am",' +
    '"name":"api-manager",' +
    '"image":"docker.wso2.com/wso2am:2.0.0",' +
    '"ports":[' +
    '"443:9443",' +
    '"80:9763",' +
    '"8280:8280",' +
    '"8243:8243"' +
'],' +
    '"links":[' +
    '{' +
        '"service-id":1,' +
        '"type":"database",' +
        '"port":"3306"' +
    '}' +
']' +
'},' +
'{' +
    '"id":3,' +
    '"type":"wso2am-analytics",' +
    '"name":"am-analytics",' +
    '"image":"docker.wso2.com/wso2am-analytics:2.0.0",' +
    '"ports":[' +
    '"9444:9444",' +
    '"9764:9764"' +
'],' +
    '"links":[' +
    '{' +
        '"service-id":1,' +
        '"type":"database",' +
        '"port":"3306"' +
    '}' +
']' +
'}' +
']}';

// console.log(JSON.parse(jsonText));

//function to load json files
function loadJSON(path, callback) {

    var xobj = new XMLHttpRequest();
    xobj.overrideMimeType("application/json");
    xobj.open('GET', path, true);
    xobj.onreadystatechange = function () {
        if (xobj.readyState == 4 && xobj.status == "200") {
            // Required use of an anonymous callback as .open will NOT return a value but simply returns undefined in asynchronous mode
            callback(xobj.responseText);
        }
    };
    xobj.send(null);
}
