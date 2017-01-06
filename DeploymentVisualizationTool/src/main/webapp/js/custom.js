
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

    var showAddProfile = function (state, evt, product) {

        var html = "";
        if (product == "wso2am") {
            html =
                '<div style="font-size: 20px;align-content: flex-end;text-align: left;">' +
                '<input type="checkbox" style="margin-right: 5px;" id="check1" value="publisher"/>publisher </br>' +
                '<input type="checkbox" style="margin-right: 5px;" id="check2" value="store"/>store </br>' +
                '<input type="checkbox" style="margin-right: 5px;" id="check3" value="keymanager"/>keymanager </br>' +
                '<input type="checkbox" style="margin-right: 5px;" id="check4" value="traffic-manager"/>traffic-manager </br>' +
                '<input type="checkbox" style="margin-right: 5px;" id="check5" value="gateway-manager"/>gateway-manager </br>' +
                '<input type="checkbox" style="margin-right: 5px;" id="check6" value="gateway-worker"/>gateway-worker </br>' +
                '</div>' +
                '<div style="margin-top: 15px; font-weight: bold;"></div>';
        }

        $('<div></div>').appendTo('body')
            .html(html)
            .dialog({
                modal: true,
                title: 'Choose profiles', zIndex: 10000, autoOpen: true,
                width: '250px', resizable: false,
                buttons: {
                    Ok: function () {
                        addProfileToService(state);
                        $(this).dialog("close");
                    }
                },
                close: function (event, ui) {
                    $(this).remove();
                }
            });
    };

    var addProfileToService = function (state) {
        //Figure out the checked profiles
        var str = "";
        if (document.getElementById("check1").checked) {
            str += '/' + document.getElementById("check1").value;
        }
        if (document.getElementById("check2").checked) {
            str += '/' + document.getElementById("check2").value;
        }
        if (document.getElementById("check3").checked) {
            str += '/' + document.getElementById("check3").value;
        }
        if (document.getElementById("check4").checked) {
            str += '/' + document.getElementById("check4").value;
        }
        if (document.getElementById("check5").checked) {
            str += '/' + document.getElementById("check5").value;
        }
        if (document.getElementById("check6").checked) {
            str += '/' + document.getElementById("check6").value;
        }

        //Set profile as label of the vertex and refresh
        var value = state.cell.value;
        value.setAttribute("label", str.substring(1));
        state.view.refresh();

    };
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

    var getModel = function(editor) {
        var enc = new mxCodec();
        var node = enc.encode(editor.graph.getModel());
        var xml = mxUtils.getPrettyXml(node);

        console.log("getModel");
        $.post("GetJSONModelFromXML", {xml:xml}, function(data) {
            console.log(data);
        });
    };

    var getConfigurations = function(editor) {
        var enc = new mxCodec();
        var node = enc.encode(editor.graph.getModel());
        var xml = mxUtils.getPrettyXml(node);

        console.log(xml);

        $.post("GetConfigFromXML", {xml:xml}, function(data) {
    //        alert(data);
            window.open(data, '_blank');
    //        window.location.href = data;
        });
    };
    
    var getConfigurationsAutoGenLinks = function(editor) {
        var enc = new mxCodec();
        var node = enc.encode(editor.graph.getModel());
        var xml = mxUtils.getPrettyXml(node);

        console.log(xml);

        $.post("GetConfigFromXMLAutoGenLinks", {xml:xml}, function(data) {
            alert(data);
            window.open(data, '_blank');
    //        window.location.href = data;
        });
    };
    
    var genLinks = function(editor) {

        // console.log(editor);
        var linksPath = "links.json";
        $.getJSON(linksPath, function(json) {

            // console.log(json);
            var cells = editor.graph.model.cells;
            var services = new Array();
            var serviceIDs = new Array();
            // Process each vertex in the graph model
            for (i in cells) {
                var cell = cells[i];
                if (cell.vertex == 1) {
                    services.push(getName(cell));
                    serviceIDs.push(cell.id);
                }
            }
            // console.log(serviceIDs);
            for (var i=0; i<services.length; i++) {
                var possibleLinks = json[services[i]];
                if (possibleLinks != null) {
                    for (var j=0; j < services.length; j++) {
                        // console.log("j:" + j + ":" + services[j]);
                        if (possibleLinks.indexOf(services[j]) != -1) {
                            // console.log(services[i] + " -> " + services[j]);
                            // console.log(serviceIDs[i+2] + " : " + serviceIDs[j+2])
                            editor.graph.connectionHandler.insertEdge(cells[1], null, null, cells[serviceIDs[i]], cells[serviceIDs[j]], null);
                            editor.graph.view.refresh();
                        }
                    }
                }
            }
            // var possibleLinks = json[name];
        });
    };

    var getName = function (cell) {
        var name = cell.style;
        if (cell.value.attributes[0].value != "") {
            name += "_" + cell.value.attributes[0].value;
        }
        return name;
    }