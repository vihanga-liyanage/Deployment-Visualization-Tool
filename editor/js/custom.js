
var modelStr = '{"services":[';
function serviceNode () {
    this.id = 0;
    this.type = "";
    this.name = "";
    this.image = "";
    this.ports = [];
    this.links = [];
    this.profile = "";
}
// var serviceNode = {
//     id:0,
//     type:"",
//     name:"",
//     image:"",
//     ports:[],
//     links:[],
//     profile:""
// };
var serviceArray = [];

var getModelFromDocker = function(baseDir, ymlFile, callback)
{
    var ymlText = readFile(ymlFile);
    var data = jsyaml.load(ymlText);

    var serviceID = 1;
    for (var key in data.services)
    {
        processService(serviceID, key, data.services[key], baseDir);
        serviceID ++;
    }
    modelStr = modelStr.slice(0, -1) + ']}';
    callback(JSON.parse(modelStr));
};

var processService = function (serviceID, key, service, baseDir)
{
    var node = new serviceNode();
    node.id = serviceID;
    modelStr += '{"id":' + serviceID + ',';

    //Extracting type and name from key
    var dashIndex = key.indexOf('-');
    var type = key.substring(0, dashIndex);
    var name = key.substring(dashIndex + 1);
    node.type = type;
    node.name = name;
    modelStr += '"type":"' + type + '",';
    modelStr += '"name":"' + name + '",';

    //set image
    node.image = service.image;
    modelStr += '"image":"' + service.image + '",';

    //set ports
    modelStr += '"ports":[';
    if (service.ports != null)
    {
        var ports = service.ports;
        for (p in ports)
        {
            node.ports.push(ports[p]);
            modelStr += '"' + ports[p] + '",';
        }
        modelStr = modelStr.slice(0, -1);
    }

    //set links
    modelStr += '],"links":[';

    if (service.build != null)
    {
        var dockerfile = baseDir + service.build.dockerfile;
        var dockerFileContent = readFile(dockerfile);

        console.log(key);
        console.log(dockerFileContent);

        var lines = dockerFileContent.split('\n');
        for (i in lines)
        {
            var parts = lines[i].split(' ');
            if (parts[0] == 'COPY') {
                // console.log(parts[0] + ":" + parts[1]);
            }
        }
        // console.log(lines);
    }

    node.profile = name;
    modelStr += '],"profile":"' + name + '"},';

    console.log(node);
    serviceArray.push(node);
};

var getModelString = function () {
    var modelStr = '{"services":[';
    for (i in serviceArray)
    {

    }
};

function readFile(file)
{
    var allText = "Error";
    var rawFile = new XMLHttpRequest();
    rawFile.open("GET", file, false);
    rawFile.onreadystatechange = function ()
    {
        if(rawFile.readyState === 4)
        {
            if(rawFile.status === 200 || rawFile.status == 0)
            {
                allText = rawFile.responseText;
            }
        }
    };
    rawFile.send(null);
    return allText;
}

//generate the graph by reading the configuration xml
var generateGraph = function (configXML, editor)
{
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

//function to load the model json file
function loadJSON(path, callback)
{
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
