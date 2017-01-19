
    var modelStr = '{"services":[';
    var serviceArray = [];

    var KNOWLEDGE_BASE_LOCATION = "resources/knowledge-base/";

    //Variable to store merge meta data
    var mergeDocPath = KNOWLEDGE_BASE_LOCATION + "merge.json";
    var mergeData = null;//JSON.parse(readFile(mergeDocPath));

    //variable to store each versions of produccts
    var versionData = {};

    var serviceNode = function ()
    {
        this.id = 0;
        this.type = "";
        this.name = "";
        this.image = "";
        this.ports = [];
        this.links = [];
        this.profile = "";
    };

    //Show a dialog box to select product profiles.
    var showAddProfile = function (state, evt, product)
    {
        var html = "";
        var profiles;

        if (product == "wso2am-2.0.0") {
            profiles = ['publisher', 'store', 'keymanager', 'traffic-manager', 'gateway-manager', 'gateway-worker'];
            html = '<div style="font-size: 20px;align-content: flex-end;text-align: left;">';

            //check for already added profiles
            var oldProfiles = "";

            //If a profile is already selected
            if (state.cell.value.getAttribute("label")) {
                oldProfiles = state.cell.value.getAttribute("label");
            }

            for (var i in profiles) {

                html += '<label><input type="checkbox" onclick="validateProfileSelection(\'' + product + '\', \'' + i + '\', \'' +
                    profiles + '\')" style="margin-right: 5px;" id="check' + i + '" value="' + profiles[i] + '"';

                //If a profile is added already, check the checkbox by default
                if (oldProfiles.includes(profiles[i])) {
                    html += ' checked';
                }
                html += '/>' + profiles[i] + '</label></br>';
            }

            html += '</div><div style="margin-top: 15px; font-weight: bold;"></div>';

        }

        $('<div></div>').appendTo('body')
            .html(html)
            .dialog({
                modal: true,
                title: 'Choose Profiles', zIndex: 10000, autoOpen: true,
                width: '250px', resizable: false,
                position: { my: 'left top', at: 'left+' + evt.clientX + ' top+' + evt.clientY},
                buttons: {
                    Ok: function () {
                        addProfileToService(state);
                        $(this).dialog("close");
                    }
                },
                close: function (event, ui) {
                    $(this).remove();
                },
                open: initValidateProfileSelection(product, profiles)
            });
    };

    //Modify the label of the cell according to selected profiles
    var addProfileToService = function (state)
    {
        //Get the checked profiles
        var str = "";
        if (document.getElementById("check0").checked) {
            str += '/' + document.getElementById("check0").value;
        }
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

        //Add profiles to the label of the vertex and refresh
        var value = state.cell.value;
        value.setAttribute("label", str.substring(1));
        state.view.refresh();

    };

    //Validate profiles if one or more profiles already selected
    var initValidateProfileSelection = function (product, profiles)
    {
        //Read merge meta data if not already done
        if (mergeData == null)
            mergeData = JSON.parse(readFile(mergeDocPath));

        for (var i=0; i<profiles.length; i++) {
            if (document.getElementById("check" + i).checked) {
                validateProfileSelection(product, i, "" + profiles)
            }
        }
    };

    //Enable or disable profiles based on selected profiles
    var validateProfileSelection = function (product, id, profiles)
    {
        profiles = profiles.split(",");
        var status = false;

        //If all checkboxes are unchecked, all should be enabled (unchecked action)
        for (var i=0; i<profiles.length; i++) {
            status |= document.getElementById("check" + i).checked;
        }
        if (!status) {
            for (var i in profiles) {
                document.getElementById("check" + i).disabled = false;
            }
        //Checked action - disable un-mergeable profiles
        } else {

            //Read merge meta data if not already done
            if (mergeData == null)
                mergeData = JSON.parse(readFile(mergeDocPath));

            var profile = profiles[id];

            if (typeof mergeData[product][profile] != 'undefined') {
                var mergeableProfiles = mergeData[product][profile];
                for (var i in profiles) {
                    if ((i != id) && (!mergeableProfiles.includes(profiles[i]))) {
                        document.getElementById("check" + i).disabled = true;
                    }
                }
            }
        }
    };

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

    //Read a file and send data as text
    var readFile = function (file)
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
    };

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
    var loadJSON = function (path, callback)
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
    };

    //call back end and let the user download the generated configuration directory
    var getConfigurations = function(editor)
    {
        var enc = new mxCodec();
        var node = enc.encode(editor.graph.getModel());
        var xml = mxUtils.getPrettyXml(node);

        console.log(xml);

        $.post("GetConfigFromXML", {xml:xml}, function(data) {
            window.open(data, '_blank');
        });
    };

    //call back end and let the user download the generated configuration directory by auto generating the links
    var getConfigurationsAutoGenLinks = function(editor)
    {
        var enc = new mxCodec();
        var node = enc.encode(editor.graph.getModel());
        var xml = mxUtils.getPrettyXml(node);

        console.log(xml);

        $.post("GetConfigFromXMLAutoGenLinks", {xml:xml}, function(data) {
            if (data == "Diagram is empty!") {
                alert(data);
            } else {
                window.open(data, '_blank');
            }
        });
    };

    //Generate graph links
    var genLinks = function(editor)
    {
        // get the required knowledge to build the links
        var linksPath = KNOWLEDGE_BASE_LOCATION + "links.json";

        $.getJSON(linksPath, function(json) {

            var cells = editor.graph.model.cells;
            var services = []; //store services data -> name and id

            // Push service data into array
            for (var i in cells) {
                var cell = cells[i];
                if (cell.vertex == 1) {
                    services.push([getName(cell), cell.id]);
                }
            }
            for (var i=0; i<services.length; i++) {
                //get possible links to each service from json knowledge base
                var possibleLinks = json[services[i][0]];
                if (possibleLinks != null) {
                    for (var j=0; j < services.length; j++) {

                        // draw connections if links should exists and not already their.
                        if (possibleLinks.indexOf(services[j][0]) != -1) {
                            if (!isConnected(cells[services[i][1]], cells[services[j][1]]))
                            {
                                editor.graph.connectionHandler.insertEdge(
                                    cells[1], //parent
                                    null, //id
                                    null, //value
                                    cells[services[i][1]], //source
                                    cells[services[j][1]], //target
                                    null //style
                                );
                            }
                        }
                    }
                    editor.graph.view.refresh();
                }
            }
        });
        editor.graph.container.focus();
    };

    //Check if two services have a connection
    var isConnected = function (cell1, cell2)
    {
        if (cell1.getEdgeCount() == 0)
            return false;
        for (var i = 0; i < cell1.getEdgeCount(); i++)
        {
            source = cell1.getEdgeAt(i).source;
            target = cell1.getEdgeAt(i).target;
            if (source == cell2 || target == cell2)
                return true;
        }
        return false;
    };

    //Construct service name -> <product>[_<profile-1>][/<profile-2>]...
    var getName = function (cell)
    {
        var name = cell.style;
        if (cell.value.attributes[0].value != "")
        {
            name += "_" + sortProfiles(cell.value.attributes[0].value)
        }
        return name;
    };

    //Sort a given profile string and return with format
    var sortProfiles = function (profiles)
    {
        var profileArray = profiles.split("/");
        profileArray.sort();
        var out = profileArray[0];
        for (var i=1; i<profileArray.length; i++) {
            out += "/" + profileArray[i];
        }
        return out;
    };

    //Clear all links in the graph
    var clearLinks = function (editor) 
    {
        //select all edges
        editor.graph.selectCells(false, true);
        editor.graph.removeCells();
        editor.graph.container.focus();
    };

    //Load predefine diagram
    var showLoadDiagramDialog = function (editor)
    {
        //These names will be used later to retrieve the xml file by replacing white spaces
        //apim 2.0.0 pattern-1 -> apim-2.0.0-pattern-1.xml
        var diagrams = ['apim 2.0.0 pattern-1', 'apim 2.0.0 pattern-2', 'apim 2.0.0 pattern-3', 'apim 2.0.0 pattern-6'];

        var html = '<div style="font-size: 20px;align-content: flex-end;text-align: left;">' +
            '<select id="diagramSelect" style="width: 100%;">';

        for (var i in diagrams) {
            html += '<option style="padding: 2px;" value="' + diagrams[i] + '">' + diagrams[i] + '</option>';
        }

        html += '</select></div><div style="margin-top: 15px; font-weight: bold;"></div>';

        $('<div></div>').appendTo('body')
            .html(html)
            .dialog({
                modal: true,
                title: 'Choose Diagram', zIndex: 10000, autoOpen: true,
                width: '300px', resizable: false,
                buttons: {
                    Ok: function () {
                        var x = document.getElementById("diagramSelect");
                        var i = x.selectedIndex;
                        loadDiagram(editor, x.options[i].text);
                        $(this).dialog("close");
                    }
                },
                close: function (event, ui) {
                    $(this).remove();
                }
            });
    };

    //Load a given diagram
    var loadDiagram = function (editor, diagram)
    {
        var diagramPath = KNOWLEDGE_BASE_LOCATION + diagram + ".xml";
        while (diagramPath.includes(" "))
            diagramPath = diagramPath.replace(" ", "-");

        var client = new XMLHttpRequest();
        client.open('GET', diagramPath);
        client.onreadystatechange = function() {
            var doc = mxUtils.parseXml(client.responseText);
            var dec = new mxCodec(doc);
            dec.decode(doc.documentElement, editor.graph.getModel());

            resetVersionData(editor);
            genLinks(editor);
            editor.graph.container.focus();
        };
        client.send();

    };

    //Highlight target cell when dragging another if those cells are mergeable
    var initCellMerge = function(currentCell, highlighter, me, editor, cellMerge)
    {
        var tmp = editor.graph.view.getState(me.getCell());

        //Apply only when dragging on top of a vertex
        if (editor.graph.isMouseDown || (tmp != null && !editor.graph.getModel().isVertex(tmp.cell)))
        {
            //Apply only if target cell is not the dragging cell
            if (tmp.cell.id != currentCell.id) {
                //Apply only if the target is not already highlighted
                if (highlighter.state == null) {

                    //Read merge meta data if not already done
                    if (mergeData == null)
                        mergeData = JSON.parse(readFile(mergeDocPath));

                    var sourceProduct = currentCell.style;
                    var targetProduct = tmp.cell.style;

                    //To merge, both cells should be of same product, and merge meta data should exist.
                    if ((sourceProduct == targetProduct) && (mergeData[sourceProduct])) {

                        var sourceProfile = currentCell.value.getAttribute("label").split("/")[0];
                        var targetProfile = tmp.cell.value.getAttribute("label").split("/")[0];
                        var mergeableProfiles = mergeData[sourceProduct][sourceProfile];

                        //Highlight target and set isMergeable true if two cells are mergeable.
                        if ((mergeableProfiles) && (mergeableProfiles.includes(targetProfile))) {
                            highlighter.highlight(editor.graph.view.getState(tmp.cell));
                            if (!cellMerge.isMergeable) {
                                cellMerge.isMergeable = true;
                                cellMerge.source = currentCell;
                                cellMerge.target = tmp.cell;
                            }
                        } else {
                            if (cellMerge.isMergeable)
                                cellMerge.isMergeable = false;
                        }
                    } else {
                        if (cellMerge.isMergeable)
                            cellMerge.isMergeable = false;
                    }
                }
            }
        }

    };

    //Merge two given cells
    var mergeCells = function (editor, source, target)
    {
        var sourceProfile = source.value.getAttribute("label");
        var targetProfile = target.value.getAttribute("label");

        var newLabel = sourceProfile + "/" + targetProfile;
        newLabel = sortProfiles(newLabel);

        var value = source.value;
        value.setAttribute("label", newLabel);

        editor.graph.removeCells([target]);
        editor.graph.view.refresh();

        genLinks(editor);
    };

    //Validate the product versions when dropping them
    var validateProductVersion = function (editor, sender, me)
    {
        var style = me.properties.cells[0].style;

        //Check if the cell is a vertex and have a product with a version (check for a dot in style)
        if (me.properties.cells[0].vertex && (style.indexOf(".") !== -1)) {
            //separate product and version
            var temp = style.split("-");
            var product = temp[0];
            if (temp.length > 2) {
                for (var i=1; i<temp.length-1; i++) {
                    product += "-" + temp[1];
                }
            }
            var version = temp[temp.length-1];

            //Remove newly added cell if it's version doesn't match
            if (versionData[product] != null && version != versionData[product]) {
                editor.graph.removeCells([me.properties.cells[0]]);
                editor.graph.container.focus();
                alert("Cannot use multiple versions of " + product + " together!");
            } else {
                //Save each product version on first time
                versionData[product] = version;
            }
        }
    };

    //Update the versionDate when removing vertices
    var updateVersionData = function (sender, me)
    {
        var style = me.properties.cells[0].style;

        //Check if the cell is a vertex and have a product with a version (check for a dot in style)
        if (me.properties.cells[0].vertex && (style.indexOf(".") !== -1)) {
            //separate product and version
            var temp = style.split("-");
            var product = temp[0];
            if (temp.length > 2) {
                for (var i=1; i<temp.length-1; i++) {
                    product += "-" + temp[1];
                }
            }
            var version = temp[temp.length-1];

            //Loop through all cells to find out if the deleted cell is last of it's kind
            var cells = sender.model.cells;
            var isLastProduct = true;
            for (var i in cells) {
                if (cells[i].style == product + "-" + version) {
                    isLastProduct = false;
                    break;
                }
            }

            //If the cell was the last one, update the version data
            if (isLastProduct)
                versionData[product] = null;
        }
    };

    //Reset the version data when user load a predefined diagram
    var resetVersionData = function (editor)
    {
        versionData = {};
        var cells = editor.graph.model.cells;
        for (var i in cells) {
            //Apply only if it's a product with versions
            if (cells[i].vertex && (cells[i].style.indexOf(".") !== -1)) {
                //separate product and version
                var temp = cells[i].style.split("-");
                var product = temp[0];
                if (temp.length > 2) {
                    for (var i=1; i<temp.length-1; i++) {
                        product += "-" + temp[1];
                    }
                }
                var version = temp[temp.length-1];
                if (versionData[product] == null)
                    versionData[product] = version;
            }
        }
    };